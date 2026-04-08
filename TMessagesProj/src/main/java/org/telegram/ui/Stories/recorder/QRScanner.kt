package org.telegram.ui.Stories.recorder

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import android.text.TextUtils
import android.view.TextureView
import android.view.View
import androidx.annotation.NonNull
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.MessagesController
import org.telegram.messenger.SharedConfig
import org.telegram.messenger.UserConfig
import org.telegram.messenger.Utilities
import org.telegram.messenger.camera.CameraView
import org.telegram.ui.Components.AnimatedFloat
import org.telegram.ui.Components.CubicBezierInterpolator
import java.util.concurrent.atomic.AtomicBoolean

class QRScanner(context: Context, whenScanned: Utilities.Callback<Detected>) {

    private val paused = AtomicBoolean(false)
    private val listener: Utilities.Callback<Detected> = whenScanned
    private val prefix: String = MessagesController.getInstance(UserConfig.selectedAccount).linkPrefix
    private val mlKitScanner = StoriesQrMlKitScanner()

    private var lastDetected: Detected? = null
    private var previewTextureView: TextureView? = null
    private var cacheBitmap: Bitmap? = null

    init {
        Utilities.globalQueue.postRunnable { attach(previewTextureView) }
    }

    fun getDetected(): Detected? {
        return lastDetected
    }

    fun destroy() {
        previewTextureView = null
        Utilities.globalQueue.cancelRunnable(process)
    }

    @Deprecated("Use attach(TextureView?)")
    fun attach(cameraView: CameraView?) {
        attach(cameraView?.textureView)
    }

    fun attach(textureView: TextureView?) {
        previewTextureView = textureView
        if (!paused.get()) {
            Utilities.globalQueue.cancelRunnable(process)
            Utilities.globalQueue.postRunnable(process, getTimeout())
        }
    }

    fun setPaused(pause: Boolean) {
        if (paused.getAndSet(pause) == pause) {
            return
        }

        if (pause) {
            Utilities.globalQueue.cancelRunnable(process)
            if (lastDetected != null) {
                lastDetected = null
                AndroidUtilities.runOnUIThread { listener.run(null) }
            }
        } else {
            Utilities.globalQueue.cancelRunnable(process)
            Utilities.globalQueue.postRunnable(process, getTimeout())
        }
    }

    fun isPaused(): Boolean {
        return paused.get()
    }

    private val process: Runnable = object : Runnable {
        override fun run() {
            if (previewTextureView == null || paused.get()) {
                return
            }

            val textureView: TextureView? = previewTextureView
            if (textureView != null) {
                val maxSide = getMaxSide()
                var w = textureView.width
                var h = textureView.height
                if (w > maxSide || h > maxSide) {
                    val scale = minOf(maxSide.toFloat() / w, maxSide.toFloat() / h)
                    w = (w * scale).toInt()
                    h = (h * scale).toInt()
                }
                w = maxOf(1, w)
                h = maxOf(1, h)
                if (cacheBitmap == null || w != cacheBitmap!!.width || h != cacheBitmap!!.height) {
                    cacheBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                }
                textureView.getBitmap(cacheBitmap!!)
                val detected = detect(cacheBitmap!!)
                if ((lastDetected != null) != (detected != null) || (detected != null && lastDetected != null && !detected.equals(lastDetected))) {
                    lastDetected = detected
                    AndroidUtilities.runOnUIThread { listener.run(detected) }
                }
            }

            if (!paused.get()) {
                Utilities.globalQueue.cancelRunnable(this)
                Utilities.globalQueue.postRunnable(this, getTimeout())
            }
        }
    }

    private fun detect(bitmap: Bitmap?): Detected? {
        if (bitmap == null) {
            return null
        }

        try {
            val detection = mlKitScanner.detect(bitmap, prefix)
            if (detection != null) {
                return Detected(detection.link, detection.points)
            }
        } catch (_: Throwable) {
        }

        return null
    }

    fun getMaxSide(): Int {
        return when (SharedConfig.getDevicePerformanceClass()) {
            SharedConfig.PERFORMANCE_CLASS_HIGH -> 720
            SharedConfig.PERFORMANCE_CLASS_AVERAGE -> 540
            else -> 540
        }
    }

    fun getTimeout(): Long {
        if (lastDetected == null) {
            return 750
        }
        return when (SharedConfig.getDevicePerformanceClass()) {
            SharedConfig.PERFORMANCE_CLASS_HIGH -> 80
            SharedConfig.PERFORMANCE_CLASS_AVERAGE -> 400
            else -> 800
        }
    }

    fun detach() {
        // No-op: ML Kit scanner is managed by StoriesQrMlKitScanner.
    }

    class Detected(
        @JvmField val link: String,
        @JvmField val points: Array<PointF>
    ) {
        @JvmField
        val cx: Float

        @JvmField
        val cy: Float

        init {
            var cx = 0f
            var cy = 0f
            for (point in points) {
                cx += point.x
                cy += point.y
            }
            if (points.isNotEmpty()) {
                cx /= points.size.toFloat()
                cy /= points.size.toFloat()
            }
            this.cx = cx
            this.cy = cy
        }

        fun equals(d: Detected?): Boolean {
            if (d == null) {
                return false
            }
            if (!TextUtils.equals(link, d.link)) {
                return false
            }
            if (points === d.points) {
                return true
            }
            if (points.size != d.points.size) {
                return false
            }
            for (i in points.indices) {
                if (kotlin.math.abs(points[i].x - d.points[i].x) > 0.001f || kotlin.math.abs(points[i].y - d.points[i].y) > 0.001f) {
                    return false
                }
            }
            return true
        }
    }

    class QrRegionView(context: Context) : View(context) {
        @JvmField
        val drawer = QrRegionDrawer(this::invalidate)
        private val rect = RectF()

        override fun dispatchDraw(@NonNull canvas: Canvas) {
            if (drawer.hasNoDraw()) {
                return
            }
            rect.set(0f, 0f, width.toFloat(), height.toFloat())
            drawer.draw(canvas, rect)
        }
    }

    class QrRegionDrawer(private val invalidate: Runnable) {

        private var hasQrResult = false
        private var qrResult: Detected? = null
        private val animatedQr = AnimatedFloat(0f, invalidate, 0L, 320L, CubicBezierInterpolator.EASE_OUT)
        private val animatedQrCX = AnimatedFloat(0f, invalidate, 0L, 160L, CubicBezierInterpolator.EASE_OUT)
        private val animatedQrCY = AnimatedFloat(0f, invalidate, 0L, 160L, CubicBezierInterpolator.EASE_OUT)
        private val animatedQPX = arrayOf(
            AnimatedFloat(0f, invalidate, 0L, 160L, CubicBezierInterpolator.EASE_OUT),
            AnimatedFloat(0f, invalidate, 0L, 160L, CubicBezierInterpolator.EASE_OUT),
            AnimatedFloat(0f, invalidate, 0L, 160L, CubicBezierInterpolator.EASE_OUT),
            AnimatedFloat(0f, invalidate, 0L, 160L, CubicBezierInterpolator.EASE_OUT)
        )
        private val animatedQPY = arrayOf(
            AnimatedFloat(0f, invalidate, 0L, 160L, CubicBezierInterpolator.EASE_OUT),
            AnimatedFloat(0f, invalidate, 0L, 160L, CubicBezierInterpolator.EASE_OUT),
            AnimatedFloat(0f, invalidate, 0L, 160L, CubicBezierInterpolator.EASE_OUT),
            AnimatedFloat(0f, invalidate, 0L, 160L, CubicBezierInterpolator.EASE_OUT)
        )

        private val qrPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = 0xFFFFDE07.toInt()
            strokeWidth = AndroidUtilities.dp(6f).toFloat()
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            setShadowLayer(0x40666666.toFloat(), 0f, AndroidUtilities.dp(3f).toFloat(), AndroidUtilities.dp(6f))
        }
        private val qrPath = Path()

        fun draw(canvas: Canvas, rect: RectF) {
            val currentQrResult = qrResult ?: return
            if (currentQrResult.points.isEmpty()) {
                return
            }

            val qrAlpha = animatedQr.set(hasQrResult)
            val cx = animatedQrCX.set(currentQrResult.cx)
            val cxPx = rect.left + cx * rect.width()
            val cy = animatedQrCY.set(currentQrResult.cy)
            val cyPx = rect.top + cy * rect.height()
            val qrScale = AndroidUtilities.lerp(0.5f, 1.1f, qrAlpha)

            canvas.save()
            canvas.scale(qrScale, qrScale, cxPx, cyPx)
            if (qrAlpha > 0) {
                qrPath.rewind()
                val len = minOf(4, currentQrResult.points.size)
                for (i in 0 until len) {
                    val li = if (i - 1 < 0) len - 1 else i - 1
                    val ri = if (i + 1 >= len) 0 else i + 1

                    val l = currentQrResult.points[li]
                    val p = currentQrResult.points[i]
                    val r = currentQrResult.points[ri]

                    val lx = rect.left + (animatedQPX[li].set(l.x - currentQrResult.cx) + cx) * rect.width()
                    val ly = rect.top + (animatedQPY[li].set(l.y - currentQrResult.cy) + cy) * rect.height()
                    val px = rect.left + (animatedQPX[i].set(p.x - currentQrResult.cx) + cx) * rect.width()
                    val py = rect.top + (animatedQPY[i].set(p.y - currentQrResult.cy) + cy) * rect.height()
                    val rx = rect.left + (animatedQPX[ri].set(r.x - currentQrResult.cx) + cx) * rect.width()
                    val ry = rect.top + (animatedQPY[ri].set(r.y - currentQrResult.cy) + cy) * rect.height()

                    val lvx = lx - px
                    val lvy = ly - py
                    val rvx = rx - px
                    val rvy = ry - py

                    qrPath.moveTo(
                        px + lvx * .18f,
                        py + lvy * .18f
                    )
                    qrPath.lineTo(px, py)
                    qrPath.lineTo(
                        px + rvx * .18f,
                        py + rvy * .18f
                    )
                }
                qrPaint.alpha = (0xFF * qrAlpha).toInt()
                canvas.drawPath(qrPath, qrPaint)
            }
            canvas.restore()
        }

        fun setQrDetected(qrResult: Detected?) {
            if (qrResult != null) {
                this.qrResult = qrResult
            }
            if (qrResult != null && !hasQrResult) {
                animatedQrCX.set(qrResult.cx, true)
                animatedQrCY.set(qrResult.cy, true)
                for (i in 0 until minOf(4, qrResult.points.size)) {
                    animatedQPX[i].set(qrResult.points[i].x - qrResult.cx, true)
                    animatedQPY[i].set(qrResult.points[i].y - qrResult.cy, true)
                }
            }
            hasQrResult = qrResult != null
            invalidate.run()
        }

        fun hasNoDraw(): Boolean {
            return !hasQrResult && animatedQr.get() <= 0
        }
    }
}
