package org.telegram.messenger.camera

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.media.Image
import android.media.ImageReader
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.util.Range
import android.util.Size
import android.view.Surface
import android.view.WindowManager
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.FileLog
import org.telegram.messenger.Utilities
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.util.ArrayList
import java.util.Arrays
import java.util.Collections
import java.util.Comparator
import kotlin.math.abs

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class Camera2Session private constructor(
    context: Context,
    private val isFront: Boolean,
    @JvmField val cameraId: String,
    private val previewSize: Size
) {

    private var isError = false
    private var isSuccess = false
    private var isClosed = false

    private val cameraManager: CameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var cameraCharacteristics: CameraCharacteristics? = null

    private val thread: HandlerThread = HandlerThread("tg_camera2")
    private val handler: Handler

    private var cameraDevice: CameraDevice? = null
    private var surfaceTexture: SurfaceTexture? = null
    private var captureSession: CameraCaptureSession? = null
    private var surface: Surface? = null

    private val cameraStateCallback: CameraDevice.StateCallback
    private val captureStateCallback: CameraCaptureSession.StateCallback
    private var captureRequestBuilder: CaptureRequest.Builder? = null

    private var sensorSize: Rect? = null
    private var maxZoom = 1f
    private var currentZoom = 1f

    private var imageReader: ImageReader?
    private var lastTime: Long = 0

    private var doneCallback: Runnable? = null
    private var opened = false

    private val cropRegion = Rect()
    private var flashing = false
    private var flashMode = CameraSession.FLASH_MODE_OFF
    private var recordingVideo = false
    private var scanningBarcode = false
    private var nightMode = false

    init {
        thread.start()
        handler = Handler(thread.looper)

        cameraStateCallback = object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                cameraDevice = camera
                lastTime = System.currentTimeMillis()
                FileLog.d("Camera2Session camera #$cameraId opened")
                checkOpen()
            }

            override fun onDisconnected(camera: CameraDevice) {
                cameraDevice = camera
                FileLog.d("Camera2Session camera #$cameraId disconnected")
            }

            override fun onError(camera: CameraDevice, error: Int) {
                cameraDevice = camera
                FileLog.e("Camera2Session camera #$cameraId received $error error")
                AndroidUtilities.runOnUIThread { isError = true }
            }
        }

        captureStateCallback = object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(session: CameraCaptureSession) {
                captureSession = session
                FileLog.e("Camera2Session camera #$cameraId capture session configured")
                lastTime = System.currentTimeMillis()
                try {
                    updateCaptureRequest()
                    AndroidUtilities.runOnUIThread {
                        isSuccess = true
                        doneCallback?.run()
                        doneCallback = null
                    }
                } catch (e: Exception) {
                    FileLog.e(e)
                }
            }

            override fun onConfigureFailed(session: CameraCaptureSession) {
                captureSession = session
                FileLog.e("Camera2Session camera #$cameraId capture session failed to configure")
                AndroidUtilities.runOnUIThread { isError = true }
            }
        }

        lastTime = System.currentTimeMillis()
        imageReader = ImageReader.newInstance(previewSize.width, previewSize.height, ImageFormat.JPEG, 1)

        try {
            cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId)
            sensorSize = cameraCharacteristics?.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE)
            val value = cameraCharacteristics?.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM)
            maxZoom = if (value == null || value < 1f) 1f else value
            openCamera()
        } catch (e: Exception) {
            FileLog.e(e)
            AndroidUtilities.runOnUIThread { isError = true }
        }
    }

    fun whenDone(doneCallback: Runnable) {
        if (isInitiated()) {
            doneCallback.run()
            this.doneCallback = null
        } else {
            this.doneCallback = doneCallback
        }
    }

    fun open(surfaceTexture: SurfaceTexture?) {
        handler.post {
            this.surfaceTexture = surfaceTexture
            if (surfaceTexture != null) {
                surfaceTexture.setDefaultBufferSize(getPreviewWidth(), getPreviewHeight())
            }
            checkOpen()
        }
    }

    private fun checkOpen() {
        if (opened || surfaceTexture == null || cameraDevice == null) {
            return
        }
        opened = true
        surface = Surface(surfaceTexture)
        try {
            val surfaces = ArrayList<Surface>()
            surfaces.add(surface!!)
            imageReader?.surface?.let { surfaces.add(it) }
            cameraDevice?.createCaptureSession(surfaces, captureStateCallback, null)
        } catch (e: Exception) {
            FileLog.e(e)
            AndroidUtilities.runOnUIThread { isError = true }
        }
    }

    fun isInitiated(): Boolean {
        return !isError && isSuccess && !isClosed
    }

    fun getDisplayOrientation(): Int {
        return try {
            val context = ApplicationLoader.applicationContext ?: return 0
            val rotation = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.rotation
            val degrees = when (rotation) {
                Surface.ROTATION_0 -> 0
                Surface.ROTATION_90 -> 90
                Surface.ROTATION_180 -> 180
                Surface.ROTATION_270 -> 270
                else -> 0
            }

            val sensorOrientation = cameraCharacteristics?.get(CameraCharacteristics.SENSOR_ORIENTATION) ?: 0
            if (isFront) {
                var displayOrientation = (sensorOrientation + degrees) % 360
                displayOrientation = (360 - displayOrientation) % 360
                displayOrientation
            } else {
                (sensorOrientation - degrees + 360) % 360
            }
        } catch (e: Exception) {
            FileLog.e(e)
            0
        }
    }

    private fun getJpegOrientation(): Int {
        return try {
            val context = ApplicationLoader.applicationContext ?: return 0
            val rotation = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.rotation
            val degrees = when (rotation) {
                Surface.ROTATION_0 -> 0
                Surface.ROTATION_90 -> 90
                Surface.ROTATION_180 -> 180
                Surface.ROTATION_270 -> 270
                else -> 0
            }

            val sensorOrientation = cameraCharacteristics?.get(CameraCharacteristics.SENSOR_ORIENTATION) ?: 0
            if (isFront) {
                var jpegOrientation = (sensorOrientation + degrees) % 360
                jpegOrientation = (360 - jpegOrientation) % 360
                jpegOrientation
            } else {
                (sensorOrientation - degrees + 360) % 360
            }
        } catch (e: Exception) {
            FileLog.e(e)
            0
        }
    }

    fun getWorldAngle(): Int {
        val displayOrientation = getDisplayOrientation()
        val jpegOrientation = getJpegOrientation()
        var diffOrientation = jpegOrientation - displayOrientation
        if (diffOrientation < 0) {
            diffOrientation += 360
        }
        return diffOrientation
    }

    fun getCurrentOrientation(): Int {
        return getJpegOrientation()
    }

    fun setZoom(value: Float) {
        if (!isInitiated()) {
            return
        }
        if (captureRequestBuilder == null || cameraDevice == null || sensorSize == null) {
            return
        }
        currentZoom = Utilities.clamp(value, maxZoom, 1f)
        updateCaptureRequest()
        try {
            captureSession?.setRepeatingRequest(captureRequestBuilder?.build() ?: return, null, handler)
        } catch (e: Exception) {
            FileLog.e(e)
        }
    }

    fun setFlash(flash: Boolean) {
        if (flashing != flash) {
            flashing = flash
            updateCaptureRequest()
        }
    }

    fun getFlash(): Boolean {
        return flashing
    }

    fun setFlashMode(mode: String?) {
        flashMode = when (mode) {
            CameraSession.FLASH_MODE_ON -> CameraSession.FLASH_MODE_ON
            CameraSession.FLASH_MODE_AUTO -> CameraSession.FLASH_MODE_AUTO
            else -> CameraSession.FLASH_MODE_OFF
        }
        setFlash(flashMode == CameraSession.FLASH_MODE_ON)
    }

    fun getFlashMode(): String {
        return flashMode
    }

    fun getNextFlashMode(): String {
        return when (flashMode) {
            CameraSession.FLASH_MODE_OFF -> CameraSession.FLASH_MODE_AUTO
            CameraSession.FLASH_MODE_AUTO -> CameraSession.FLASH_MODE_ON
            else -> CameraSession.FLASH_MODE_OFF
        }
    }

    fun hasFlashModes(): Boolean {
        return !isFront && (cameraCharacteristics?.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true)
    }

    fun setFlipFront(flip: Boolean) {
        // Camera2 mirror behavior is handled in higher-level renderers.
    }

    fun isSameTakePictureOrientation(): Boolean {
        return true
    }

    fun updateRotation() {
        // Orientation is queried dynamically via getDisplayOrientation/getCurrentOrientation.
    }

    fun focusToRect(focusRect: Rect?, meteringRect: Rect?) {
        // TODO: optional Camera2 metering regions.
    }

    fun getZoom(): Float {
        return currentZoom
    }

    fun getMaxZoom(): Float {
        return maxZoom
    }

    fun getMinZoom(): Float {
        return 1f
    }

    fun getPreviewWidth(): Int {
        return previewSize.width
    }

    fun getPreviewHeight(): Int {
        return previewSize.height
    }

    fun destroy(async: Boolean) {
        destroy(async, null)
    }

    fun destroy(async: Boolean, afterCallback: Runnable?) {
        isClosed = true
        if (async) {
            handler.post {
                captureSession?.close()
                captureSession = null
                cameraDevice?.close()
                cameraDevice = null
                imageReader?.close()
                imageReader = null
                thread.quitSafely()
                AndroidUtilities.runOnUIThread {
                    try {
                        thread.join()
                    } catch (e: Exception) {
                        FileLog.e(e)
                    }
                    afterCallback?.run()
                }
            }
        } else {
            captureSession?.close()
            captureSession = null
            cameraDevice?.close()
            cameraDevice = null
            imageReader?.close()
            imageReader = null
            thread.quitSafely()
            try {
                thread.join()
            } catch (e: Exception) {
                FileLog.e(e)
            }
            if (afterCallback != null) {
                AndroidUtilities.runOnUIThread(afterCallback)
            }
        }
    }

    fun setRecordingVideo(recording: Boolean) {
        if (recordingVideo != recording) {
            recordingVideo = recording
            updateCaptureRequest()
        }
    }

    fun setScanningBarcode(scanning: Boolean) {
        if (scanningBarcode != scanning) {
            scanningBarcode = scanning
            updateCaptureRequest()
        }
    }

    fun setNightMode(enable: Boolean) {
        if (nightMode != enable) {
            nightMode = enable
            updateCaptureRequest()
        }
    }

    private fun updateCaptureRequest() {
        if (cameraDevice == null || surface == null || captureSession == null) {
            return
        }
        try {
            val template = when {
                recordingVideo -> CameraDevice.TEMPLATE_RECORD
                scanningBarcode -> CameraDevice.TEMPLATE_STILL_CAPTURE
                else -> CameraDevice.TEMPLATE_PREVIEW
            }

            captureRequestBuilder = cameraDevice?.createCaptureRequest(template)
            val builder = captureRequestBuilder ?: return

            if (scanningBarcode) {
                builder.set(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_BARCODE)
            } else if (nightMode) {
                builder.set(
                    CaptureRequest.CONTROL_SCENE_MODE,
                    if (isFront) CameraMetadata.CONTROL_SCENE_MODE_NIGHT_PORTRAIT else CameraMetadata.CONTROL_SCENE_MODE_NIGHT
                )
            }

            builder.set(
                CaptureRequest.FLASH_MODE,
                if (flashing) {
                    if (recordingVideo) CaptureRequest.FLASH_MODE_TORCH else CaptureRequest.FLASH_MODE_SINGLE
                } else {
                    CaptureRequest.FLASH_MODE_OFF
                }
            )

            if (recordingVideo) {
                builder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, Range(30, 60))
                builder.set(CaptureRequest.CONTROL_CAPTURE_INTENT, CaptureRequest.CONTROL_CAPTURE_INTENT_VIDEO_RECORD)
            }

            val currentSensorSize = sensorSize
            if (currentSensorSize != null && abs(currentZoom - 1f) >= 0.01f) {
                val centerX = currentSensorSize.width() / 2
                val centerY = currentSensorSize.height() / 2
                val deltaX = (0.5f * currentSensorSize.width() / currentZoom).toInt()
                val deltaY = (0.5f * currentSensorSize.height() / currentZoom).toInt()
                cropRegion.set(centerX - deltaX, centerY - deltaY, centerX + deltaX, centerY + deltaY)
                builder.set(CaptureRequest.SCALER_CROP_REGION, cropRegion)
            }

            builder.addTarget(surface!!)
            captureSession?.setRepeatingRequest(builder.build(), null, handler)
        } catch (e: Exception) {
            FileLog.e("Camera2Sessions setRepeatingRequest error in updateCaptureRequest", e)
        }
    }

    fun takePicture(file: File, whenDone: Utilities.Callback<Int>?): Boolean {
        if (cameraDevice == null || captureSession == null) {
            return false
        }
        try {
            val localBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE) ?: return false
            val orientation = getJpegOrientation()
            localBuilder.set(CaptureRequest.JPEG_ORIENTATION, orientation)

            imageReader?.setOnImageAvailableListener({ reader ->
                var image: Image? = null
                var output: FileOutputStream? = null
                try {
                    image = reader.acquireLatestImage()
                    if (image == null) {
                        return@setOnImageAvailableListener
                    }
                    val buffer: ByteBuffer = image.planes[0].buffer
                    val bytes = ByteArray(buffer.remaining())
                    buffer.get(bytes)
                    output = FileOutputStream(file)
                    output.write(bytes)
                } catch (e: IOException) {
                    e.printStackTrace()
                } finally {
                    image?.close()
                    if (output != null) {
                        try {
                            output.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }

                AndroidUtilities.runOnUIThread {
                    whenDone?.run(orientation)
                }
            }, null)

            if (scanningBarcode) {
                localBuilder.set(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_BARCODE)
            }

            val readerSurface = imageReader?.surface ?: return false
            localBuilder.addTarget(readerSurface)
            captureSession?.capture(localBuilder.build(), object : CameraCaptureSession.CaptureCallback() {}, null)
            return true
        } catch (e: Exception) {
            FileLog.e("Camera2Sessions takePicture error", e)
            return false
        }
    }

    @SuppressLint("MissingPermission")
    private fun openCamera() {
        cameraManager.openCamera(cameraId, cameraStateCallback, handler)
    }

    companion object {

        @JvmStatic
        fun create(front: Boolean, viewWidth: Int, viewHeight: Int): Camera2Session? {
            val context = ApplicationLoader.applicationContext
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

            var bestAspectRatio = 0f
            var bestSize: Size? = null
            var cameraId: String? = null
            try {
                val cameraIds = cameraManager.cameraIdList
                for (id in cameraIds) {
                    val characteristics = cameraManager.getCameraCharacteristics(id) ?: continue
                    if (characteristics.get(CameraCharacteristics.LENS_FACING) !=
                        (if (front) CameraCharacteristics.LENS_FACING_FRONT else CameraCharacteristics.LENS_FACING_BACK)
                    ) {
                        continue
                    }

                    val confMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    val pixelSize = characteristics.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE)
                    var cameraAspectRatio = if (pixelSize == null) 0f else pixelSize.width.toFloat() / pixelSize.height.toFloat()
                    if ((viewWidth.toFloat() / viewHeight.toFloat() >= 1f) != (cameraAspectRatio >= 1f)) {
                        cameraAspectRatio = 1f / cameraAspectRatio
                    }

                    if (bestAspectRatio <= 0f ||
                        abs(viewWidth.toFloat() / viewHeight.toFloat() - bestAspectRatio) >
                        abs(viewWidth.toFloat() / viewHeight.toFloat() - cameraAspectRatio)
                    ) {
                        if (confMap != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            val size = chooseOptimalSize(confMap.getOutputSizes(SurfaceTexture::class.java), viewWidth, viewHeight, false)
                            if (size != null) {
                                bestAspectRatio = cameraAspectRatio
                                cameraId = id
                                bestSize = size
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                FileLog.e(e)
            }

            if (cameraId == null || bestSize == null) {
                return null
            }
            return Camera2Session(context, front, cameraId, bestSize)
        }

        @JvmStatic
        fun chooseOptimalSize(choices: Array<Size>, width: Int, height: Int, notBigger: Boolean): Size {
            val bigEnoughWithAspectRatio = ArrayList<Size>(choices.size)
            val bigEnough = ArrayList<Size>(choices.size)
            val w = width
            val h = height
            for (option in choices) {
                if (notBigger && (option.height > height || option.width > width)) {
                    continue
                }
                if (option.height == option.width * h / w && option.width >= width && option.height >= height) {
                    bigEnoughWithAspectRatio.add(option)
                } else if (option.height * option.width <= width * height * 4 && option.width >= width && option.height >= height) {
                    bigEnough.add(option)
                }
            }
            return when {
                bigEnoughWithAspectRatio.isNotEmpty() -> Collections.min(bigEnoughWithAspectRatio, CompareSizesByArea())
                bigEnough.isNotEmpty() -> Collections.min(bigEnough, CompareSizesByArea())
                else -> Collections.max(Arrays.asList(*choices), CompareSizesByArea())
            }
        }
    }

    private class CompareSizesByArea : Comparator<Size> {
        override fun compare(lhs: Size, rhs: Size): Int {
            return java.lang.Long.signum(lhs.width.toLong() * lhs.height - rhs.width.toLong() * rhs.height)
        }
    }
}
