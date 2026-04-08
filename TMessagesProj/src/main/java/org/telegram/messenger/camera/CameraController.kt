/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.telegram.messenger.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.graphics.drawable.BitmapDrawable
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.params.StreamConfigurationMap
import android.media.MediaMetadataRetriever
import android.provider.MediaStore
import android.util.Size as AndroidSize
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.FileLoader
import org.telegram.messenger.FileLog
import org.telegram.messenger.ImageLoader
import org.telegram.messenger.NotificationCenter
import org.telegram.messenger.SendMessagesHelper
import org.telegram.messenger.SharedConfig
import org.telegram.messenger.Utilities
import java.io.File
import java.io.FileOutputStream
import java.util.ArrayList
import java.util.Collections
import java.util.Comparator
import java.util.concurrent.CountDownLatch
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class CameraController private constructor() {

    interface ICameraView {
        fun stopRecording()
        fun startRecording(file: File, runnable: Runnable): Boolean
    }

    fun interface VideoTakeCallback {
        fun onFinishVideoRecording(thumbPath: String?, duration: Long)
    }

    interface ErrorCallback {
        fun onError(errorId: Int, cameraSession: CameraSessionWrapper) {
        }
    }

    private val threadPool = ThreadPoolExecutor(
        CORE_POOL_SIZE,
        MAX_POOL_SIZE,
        KEEP_ALIVE_SECONDS,
        TimeUnit.SECONDS,
        LinkedBlockingQueue()
    )

    @Volatile
    private var cameraInfos: ArrayList<CameraInfo>? = null
    private var cameraInitied = false
    private var loadingCameras = false

    private val onFinishCameraInitRunnables = ArrayList<Runnable>()
    private var errorCallbacks: ArrayList<ErrorCallback>? = null

    private var recordingCurrentCameraView: ICameraView? = null
    private var onVideoTakeCallback: VideoTakeCallback? = null
    private var recordedFile: String? = null
    private var mirrorRecorderVideo = false

    fun addOnErrorListener(callback: ErrorCallback) {
        if (errorCallbacks == null) {
            errorCallbacks = ArrayList()
        }
        errorCallbacks?.remove(callback)
        errorCallbacks?.add(callback)
    }

    fun removeOnErrorListener(callback: ErrorCallback) {
        errorCallbacks?.remove(callback)
    }

    fun cancelOnInitRunnable(onInitRunnable: Runnable?) {
        if (onInitRunnable != null) {
            onFinishCameraInitRunnables.remove(onInitRunnable)
        }
    }

    fun initCamera(onInitRunnable: Runnable?) {
        if (cameraInitied) {
            if (onInitRunnable != null) {
                AndroidUtilities.runOnUIThread(onInitRunnable)
            }
            return
        }
        if (onInitRunnable != null && !onFinishCameraInitRunnables.contains(onInitRunnable)) {
            onFinishCameraInitRunnables.add(onInitRunnable)
        }
        if (loadingCameras || cameraInitied) {
            return
        }
        loadingCameras = true
        threadPool.execute {
            try {
                cameraInfos = loadCameraInfos()
                AndroidUtilities.runOnUIThread {
                    loadingCameras = false
                    cameraInitied = !cameraInfos.isNullOrEmpty()
                    if (onFinishCameraInitRunnables.isNotEmpty()) {
                        for (a in 0 until onFinishCameraInitRunnables.size) {
                            onFinishCameraInitRunnables[a].run()
                        }
                        onFinishCameraInitRunnables.clear()
                    }
                    if (cameraInitied) {
                        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.cameraInitied)
                    }
                }
            } catch (e: Throwable) {
                FileLog.e(e)
                AndroidUtilities.runOnUIThread {
                    onFinishCameraInitRunnables.clear()
                    loadingCameras = false
                    cameraInitied = false
                }
            }
        }
    }

    private fun loadCameraInfos(): ArrayList<CameraInfo> {
        val result = ArrayList<CameraInfo>()
        val context: Context = ApplicationLoader.applicationContext ?: return result
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager ?: return result

        val comparator = Comparator<Size> { o1, o2 ->
            when {
                o1.mWidth < o2.mWidth -> 1
                o1.mWidth > o2.mWidth -> -1
                o1.mHeight < o2.mHeight -> 1
                o1.mHeight > o2.mHeight -> -1
                else -> 0
            }
        }

        val cameraIds = cameraManager.cameraIdList
        for (cameraIdString in cameraIds) {
            val characteristics = cameraManager.getCameraCharacteristics(cameraIdString)
            val lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING)
            val frontFace = if (lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_FRONT) 1 else 0
            val cameraId = cameraIdString.toIntOrNull() ?: cameraIdString.hashCode()

            val info = CameraInfo(cameraId, frontFace)
            val map: StreamConfigurationMap? = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            if (map != null) {
                val previewSizes: Array<AndroidSize>? = map.getOutputSizes(SurfaceTexture::class.java)
                if (previewSizes != null) {
                    for (size in previewSizes) {
                        val w = size.width
                        val h = size.height
                        if (w < 2160 && h < 2160) {
                            info.previewSizes.add(Size(w, h))
                        }
                    }
                }

                val pictureSizes: Array<AndroidSize>? = map.getOutputSizes(ImageFormat.JPEG)
                if (pictureSizes != null) {
                    for (size in pictureSizes) {
                        info.pictureSizes.add(Size(size.width, size.height))
                    }
                }
            }

            Collections.sort(info.previewSizes, comparator)
            Collections.sort(info.pictureSizes, comparator)
            result.add(info)
        }
        return result
    }

    fun isCameraInitied(): Boolean {
        return cameraInitied && !cameraInfos.isNullOrEmpty()
    }

    fun getCameras(): ArrayList<CameraInfo>? {
        return cameraInfos
    }

    fun close(session: CameraSession?, countDownLatch: CountDownLatch?, beforeDestroyRunnable: Runnable?) {
        close(session, countDownLatch, beforeDestroyRunnable, null)
    }

    fun close(
        session: CameraSession?,
        countDownLatch: CountDownLatch?,
        beforeDestroyRunnable: Runnable?,
        afterDestroyRunnable: Runnable?
    ) {
        session?.destroy()
        beforeDestroyRunnable?.run()
        countDownLatch?.countDown()
        if (afterDestroyRunnable != null) {
            AndroidUtilities.runOnUIThread(afterDestroyRunnable)
        }
    }

    fun startPreview(sessionObject: Any?) {
        // Camera2 sessions stream continuously.
    }

    fun stopPreview(sessionObject: Any?) {
        // Camera2 sessions stream continuously.
    }

    fun openRound(session: CameraSession?, texture: SurfaceTexture?, callback: Runnable?, configureCallback: Runnable?) {
        if (session == null || texture == null) {
            return
        }
        configureCallback?.run()
        session.setInitied()
        if (callback != null) {
            AndroidUtilities.runOnUIThread(callback)
        }
    }

    fun open(session: CameraSession?, texture: SurfaceTexture?, callback: Runnable?, prestartCallback: Runnable?) {
        if (session == null || texture == null) {
            return
        }
        prestartCallback?.run()
        session.setInitied()
        if (callback != null) {
            AndroidUtilities.runOnUIThread(callback)
        }
    }

    private fun unwrapSession(sessionObject: Any?): Any? {
        return if (sessionObject is CameraSessionWrapper) {
            sessionObject.getObject()
        } else {
            sessionObject
        }
    }

    fun takePicture(
        path: File,
        ignoreOrientation: Boolean,
        sessionObject: Any?,
        callback: Utilities.Callback<Int>?
    ): Boolean {
        val unwrapped = unwrapSession(sessionObject)
        return if (unwrapped is Camera2Session) {
            unwrapped.takePicture(path, callback)
        } else {
            false
        }
    }

    fun recordVideo(
        session: Any?,
        path: File,
        mirror: Boolean,
        callback: VideoTakeCallback?,
        onVideoStartRecord: Runnable?,
        cameraView: ICameraView?
    ) {
        recordVideo(session, path, mirror, callback, onVideoStartRecord, cameraView, true)
    }

    fun recordVideo(
        sessionObject: Any?,
        path: File,
        mirror: Boolean,
        callback: VideoTakeCallback?,
        onVideoStartRecord: Runnable?,
        cameraView: ICameraView?,
        createThumbnail: Boolean
    ) {
        val unwrapped = unwrapSession(sessionObject)
        if (cameraView == null) {
            return
        }
        recordingCurrentCameraView = cameraView
        onVideoTakeCallback = callback
        recordedFile = path.absolutePath
        mirrorRecorderVideo = mirror

        threadPool.execute {
            try {
                if (unwrapped is Camera2Session) {
                    unwrapped.setRecordingVideo(true)
                }
                AndroidUtilities.runOnUIThread {
                    val activeView = recordingCurrentCameraView ?: return@runOnUIThread
                    activeView.startRecording(path, Runnable { finishRecordingVideo(createThumbnail) })
                    onVideoStartRecord?.run()
                }
            } catch (e: Throwable) {
                FileLog.e(e)
            }
        }
    }

    private fun finishRecordingVideo(createThumbnail: Boolean) {
        val sourceFile = recordedFile ?: return
        var mediaMetadataRetriever: MediaMetadataRetriever? = null
        var duration = 0L
        try {
            mediaMetadataRetriever = MediaMetadataRetriever()
            mediaMetadataRetriever.setDataSource(sourceFile)
            val d = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            if (d != null) {
                duration = d.toLong()
            }
        } catch (e: Exception) {
            FileLog.e(e)
        } finally {
            try {
                mediaMetadataRetriever?.release()
            } catch (e: Exception) {
                FileLog.e(e)
            }
        }

        val cacheFile: File?
        var bitmap: Bitmap? = null
        if (createThumbnail) {
            bitmap = SendMessagesHelper.createVideoThumbnail(sourceFile, MediaStore.Video.Thumbnails.MINI_KIND)
            if (bitmap != null && mirrorRecorderVideo) {
                val mirrored = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(mirrored)
                canvas.scale(-1f, 1f, mirrored.width / 2f, mirrored.height / 2f)
                canvas.drawBitmap(bitmap, 0f, 0f, null)
                bitmap.recycle()
                bitmap = mirrored
            }
            val fileName = Integer.MIN_VALUE.toString() + "_" + SharedConfig.getLastLocalId() + ".jpg"
            cacheFile = File(FileLoader.getDirectory(FileLoader.MEDIA_DIR_CACHE), fileName)
            var stream: FileOutputStream? = null
            try {
                stream = FileOutputStream(cacheFile)
                bitmap?.compress(Bitmap.CompressFormat.JPEG, 87, stream)
            } catch (e: Throwable) {
                FileLog.e(e)
            } finally {
                try {
                    stream?.close()
                } catch (ignore: Throwable) {
                }
            }
        } else {
            cacheFile = null
        }

        SharedConfig.saveConfig()
        val durationFinal = duration
        val bitmapFinal = bitmap
        AndroidUtilities.runOnUIThread {
            val callback = onVideoTakeCallback ?: return@runOnUIThread
            var thumbPath: String? = null
            if (cacheFile != null) {
                thumbPath = cacheFile.absolutePath
                if (bitmapFinal != null) {
                    ImageLoader.getInstance().putImageToCache(
                        BitmapDrawable(bitmapFinal),
                        Utilities.MD5(thumbPath),
                        false
                    )
                }
            }
            callback.onFinishVideoRecording(thumbPath, durationFinal)
            onVideoTakeCallback = null
        }
    }

    fun stopVideoRecording(sessionObject: Any?, abandon: Boolean) {
        stopVideoRecording(sessionObject, abandon, true)
    }

    fun stopVideoRecording(sessionObject: Any?, abandon: Boolean, createThumbnail: Boolean) {
        if (recordingCurrentCameraView != null) {
            recordingCurrentCameraView?.stopRecording()
            recordingCurrentCameraView = null
            return
        }
        val unwrapped = unwrapSession(sessionObject)
        threadPool.execute {
            try {
                if (unwrapped is Camera2Session) {
                    unwrapped.setRecordingVideo(false)
                }
                if (!abandon && onVideoTakeCallback != null) {
                    finishRecordingVideo(createThumbnail)
                } else {
                    onVideoTakeCallback = null
                }
            } catch (e: Throwable) {
                FileLog.e(e)
            }
        }
    }

    companion object {
        private const val CORE_POOL_SIZE = 1
        private const val MAX_POOL_SIZE = 1
        private const val KEEP_ALIVE_SECONDS = 60L

        @Volatile
        private var instance: CameraController? = null

        @JvmStatic
        fun getInstance(): CameraController {
            var localInstance = instance
            if (localInstance == null) {
                synchronized(CameraController::class.java) {
                    localInstance = instance
                    if (localInstance == null) {
                        localInstance = CameraController()
                        instance = localInstance
                    }
                }
            }
            return localInstance!!
        }

        @JvmStatic
        fun chooseOptimalSize(
            choices: List<Size>,
            width: Int,
            height: Int,
            aspectRatio: Size,
            notBigger: Boolean
        ): Size {
            val bigEnoughWithAspectRatio = ArrayList<Size>(choices.size)
            val bigEnough = ArrayList<Size>(choices.size)
            val w = aspectRatio.getWidth()
            val h = aspectRatio.getHeight()
            for (option in choices) {
                if (notBigger && (option.getHeight() > height || option.getWidth() > width)) {
                    continue
                }
                if (option.getHeight() == option.getWidth() * h / w && option.getWidth() >= width && option.getHeight() >= height) {
                    bigEnoughWithAspectRatio.add(option)
                } else if (option.getHeight() * option.getWidth() <= width * height * 4) {
                    bigEnough.add(option)
                }
            }
            return when {
                bigEnoughWithAspectRatio.isNotEmpty() -> Collections.min(bigEnoughWithAspectRatio, CompareSizesByArea())
                bigEnough.isNotEmpty() -> Collections.min(bigEnough, CompareSizesByArea())
                else -> Collections.max(choices, CompareSizesByArea())
            }
        }
    }

    private class CompareSizesByArea : Comparator<Size> {
        override fun compare(lhs: Size, rhs: Size): Int {
            return java.lang.Long.signum(lhs.getWidth().toLong() * lhs.getHeight() - rhs.getWidth().toLong() * rhs.getHeight())
        }
    }
}
