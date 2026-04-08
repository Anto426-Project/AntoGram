package org.telegram.ui.Stories.recorder

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.HapticFeedbackConstants
import android.view.ViewGroup
import android.widget.FrameLayout
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.UserConfig
import org.telegram.messenger.Utilities
import org.telegram.messenger.camera.CameraController
import org.telegram.messenger.camera.CameraView
import org.telegram.ui.ActionBar.Theme
import org.telegram.ui.Components.CubicBezierInterpolator
import org.telegram.ui.Components.Paint.Views.RoundView
import java.io.File

open class RoundVideoRecorder(context: Context) : FrameLayout(context) {

    @JvmField
    val cameraView: CameraView

    @JvmField
    val file: File = StoryEntry.makeCacheFile(UserConfig.selectedAccount, true)

    private var recordingStarted = -1L
    private var recordingStopped = -1L

    @JvmField
    val MAX_DURATION = 59_500L

    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val stopRunnable = Runnable { stop() }

    private var onDoneCallback: Utilities.Callback3<File, String, Long>? = null
    private var onDestroyCallback: Runnable? = null
    private var alpha = 1f
    private var roundView: RoundView? = null
    private var cameraViewAnimator: ValueAnimator? = null
    private var cancelled = false
    private var destroyAnimator: ValueAnimator? = null
    private var destroyT = 0f

    init {
        progressPaint.style = Paint.Style.STROKE
        progressPaint.strokeCap = Paint.Cap.ROUND
        progressPaint.strokeJoin = Paint.Join.ROUND

        cameraView = object : CameraView(context, true, false) {
            private val circlePath = Path()

            override fun dispatchDraw(canvas: Canvas) {
                canvas.save()
                circlePath.rewind()
                circlePath.addCircle(width / 2f, height / 2f, kotlin.math.min(width / 2f, height / 2f), Path.Direction.CW)
                canvas.clipPath(circlePath)
                super.dispatchDraw(canvas)
                canvas.restore()
            }

            override fun square(): Boolean {
                return true
            }

            override fun receivedAmplitude(amplitude: Double) {
                this@RoundVideoRecorder.receivedAmplitude(amplitude)
            }
        }
        cameraView.scaleX = 0f
        cameraView.scaleY = 0f
        addView(cameraView)
        cameraView.setDelegate {
            if (recordingStarted > 0) {
                return@setDelegate
            }
            CameraController.getInstance().recordVideo(
                cameraView.cameraSessionObject,
                file,
                false,
                { thumbPath, duration ->
                    recordingStopped = System.currentTimeMillis()
                    AndroidUtilities.cancelRunOnUIThread(stopRunnable)
                    if (cancelled) {
                        return@recordVideo
                    }
                    if (duration > 1000) {
                        cameraView.destroy(true, null)
                        onDoneCallback?.run(file, thumbPath, duration)
                    } else {
                        destroy(false)
                    }
                },
                {
                    cameraView.animate().scaleX(1f).scaleY(1f)
                        .setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT)
                        .setDuration(280)
                        .start()
                    recordingStarted = System.currentTimeMillis()
                    invalidate()

                    try {
                        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    } catch (_: Exception) {
                    }

                    AndroidUtilities.runOnUIThread(stopRunnable, MAX_DURATION)
                },
                cameraView,
                true
            )
        }
        cameraView.initTexture()

        setWillNotDraw(false)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)

        val side = (kotlin.math.min(width, height) * .43f).toInt()
        cameraView.measure(
            MeasureSpec.makeMeasureSpec(side, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(side, MeasureSpec.EXACTLY)
        )

        setMeasuredDimension(width, height)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val x = (right - left) - cameraView.measuredWidth - AndroidUtilities.dp(16f)
        val y = AndroidUtilities.dp(72f)
        cameraView.layout(x, y, x + cameraView.measuredWidth, y + cameraView.measuredHeight)
    }

    protected open fun receivedAmplitude(amplitude: Double) {
    }

    fun onDone(onDoneCallback: Utilities.Callback3<File, String, Long>): RoundVideoRecorder {
        this.onDoneCallback = onDoneCallback
        return this
    }

    fun onDestroy(onDestroyCallback: Runnable): RoundVideoRecorder {
        this.onDestroyCallback = onDestroyCallback
        return this
    }

    override fun dispatchDraw(canvas: Canvas) {
        AndroidUtilities.rectTmp.set(
            cameraView.x + cameraView.width / 2f * (1f - cameraView.scaleX),
            cameraView.y + cameraView.height / 2f * (1f - cameraView.scaleY),
            cameraView.x + cameraView.width - cameraView.width / 2f * (1f - cameraView.scaleX),
            cameraView.y + cameraView.height - cameraView.height / 2f * (1f - cameraView.scaleY)
        )

        shadowPaint.setShadowLayer(AndroidUtilities.dp(2f).toFloat(), 0f, AndroidUtilities.dp(.66f).toFloat(), Theme.multAlpha(0x20000000, alpha))
        shadowPaint.alpha = (0xff * alpha).toInt()
        canvas.drawCircle(
            AndroidUtilities.rectTmp.centerX(),
            AndroidUtilities.rectTmp.centerY(),
            kotlin.math.min(AndroidUtilities.rectTmp.width() / 2f, AndroidUtilities.rectTmp.height() / 2f) - 1,
            shadowPaint
        )

        super.dispatchDraw(canvas)
        val currentRoundView = roundView
        if (currentRoundView != null && currentRoundView.width > 0 && currentRoundView.height > 0) {
            canvas.save()
            canvas.translate(AndroidUtilities.rectTmp.left, AndroidUtilities.rectTmp.top)
            canvas.scale(
                AndroidUtilities.rectTmp.width() / currentRoundView.width,
                AndroidUtilities.rectTmp.height() / currentRoundView.height
            )
            val wasAlpha = currentRoundView.alpha
            currentRoundView.setDraw(true)
            currentRoundView.alpha = 1f - alpha
            currentRoundView.draw(canvas)
            currentRoundView.alpha = wasAlpha
            currentRoundView.setDraw(false)
            canvas.restore()
        }

        if (recordingStarted > 0) {
            val t = Utilities.clamp(sinceRecording() / MAX_DURATION.toFloat(), 1f, 0f)

            progressPaint.strokeWidth = AndroidUtilities.dp(3.33f).toFloat()
            progressPaint.color = Theme.multAlpha(0xbeffffff.toInt(), alpha)
            progressPaint.setShadowLayer(AndroidUtilities.dp(1f).toFloat(), 0f, AndroidUtilities.dp(.33f).toFloat(), Theme.multAlpha(0x20000000, alpha))
            AndroidUtilities.rectTmp.inset(-AndroidUtilities.dp(3.33f / 2f + 6).toFloat(), -AndroidUtilities.dp(3.33f / 2f + 6).toFloat())
            canvas.drawArc(AndroidUtilities.rectTmp, -90f, 360f * t, false, progressPaint)

            if (recordingStopped <= 0) {
                invalidate()
            }
        }
    }

    fun sinceRecording(): Long {
        return if (recordingStarted < 0) {
            0
        } else {
            kotlin.math.min(
                MAX_DURATION,
                (if (recordingStopped < 0) System.currentTimeMillis() else recordingStopped) - recordingStarted
            )
        }
    }

    fun sinceRecordingText(): String {
        val fullms = sinceRecording()
        var sec = (fullms / 1000).toInt()
        val ms = ((fullms - sec * 1000L) / 100).toInt()
        val min = sec / 60
        sec %= 60
        return "$min:${if (sec < 10) "0" else ""}$sec.$ms"
    }

    fun hideTo(roundView: RoundView?) {
        if (roundView == null) {
            destroy(false)
            return
        }

        AndroidUtilities.cancelRunOnUIThread(stopRunnable)
        cameraView.destroy(true, null)
        roundView.setDraw(false)
        post {
            if (roundView.width <= 0) {
                cameraView.animate().scaleX(0f).scaleY(1f).withEndAction {
                    if (parent is ViewGroup) {
                        (parent as ViewGroup).removeView(this)
                    }
                }.start()
                return@post
            }

            val scale = roundView.width.toFloat() / cameraView.width
            cameraViewAnimator?.cancel()
            cameraViewAnimator = ValueAnimator.ofFloat(0f, 1f)
            val fromScale = cameraView.scaleX
            val toX = (roundView.x + roundView.width / 2f) - (cameraView.x + cameraView.width / 2f)
            val toY = (roundView.y + roundView.height / 2f) - (cameraView.y + cameraView.height / 2f)
            cameraViewAnimator!!.addUpdateListener { anm ->
                val t = anm.animatedValue as Float
                cameraView.scaleX = AndroidUtilities.lerp(fromScale, scale, t)
                cameraView.scaleY = AndroidUtilities.lerp(fromScale, scale, t)
                cameraView.translationX = toX * t
                cameraView.translationY = toY * t
                cameraView.alpha = 1f - t
                alpha = 1f - t
                invalidate()
            }
            cameraViewAnimator!!.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    roundView.setDraw(true)
                    if (parent is ViewGroup) {
                        (parent as ViewGroup).removeView(this@RoundVideoRecorder)
                    }
                }
            })
            cameraViewAnimator!!.duration = 320
            cameraViewAnimator!!.interpolator = CubicBezierInterpolator.EASE_OUT_QUINT
            this.roundView = roundView
            cameraViewAnimator!!.start()
        }
    }

    open fun stop() {
        AndroidUtilities.cancelRunOnUIThread(stopRunnable)
        if (recordingStarted <= 0) {
            destroy(true)
        } else {
            CameraController.getInstance().stopVideoRecording(cameraView.cameraSessionRecording, false, false)
        }
    }

    fun cancel() {
        cancelled = true
        AndroidUtilities.cancelRunOnUIThread(stopRunnable)
        CameraController.getInstance().stopVideoRecording(cameraView.cameraSessionRecording, false, false)
        destroy(false)
    }

    fun destroy(instant: Boolean) {
        if (onDestroyCallback != null) {
            onDestroyCallback!!.run()
            onDestroyCallback = null
        }
        AndroidUtilities.cancelRunOnUIThread(stopRunnable)
        cameraView.destroy(true, null)
        try {
            file.delete()
        } catch (_: Exception) {
        }
        if (instant) {
            if (parent is ViewGroup) {
                (parent as ViewGroup).removeView(this)
            }
            return
        }
        destroyAnimator?.cancel()
        destroyAnimator = ValueAnimator.ofFloat(destroyT, 1f)
        destroyAnimator!!.addUpdateListener { anm ->
            destroyT = anm.animatedValue as Float
            cameraView.scaleX = 1f - destroyT
            cameraView.scaleY = 1f - destroyT
            invalidate()
        }
        destroyAnimator!!.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                if (parent is ViewGroup) {
                    (parent as ViewGroup).removeView(this@RoundVideoRecorder)
                }
            }
        })
        destroyAnimator!!.interpolator = CubicBezierInterpolator.EASE_OUT_QUINT
        destroyAnimator!!.duration = 280
        destroyAnimator!!.start()
    }
}
