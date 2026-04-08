package org.telegram.ui.Stories.recorder

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorSpace
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.RadialGradient
import android.graphics.Shader
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import androidx.annotation.Nullable
import androidx.core.graphics.ColorUtils
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.Utilities
import org.telegram.ui.Components.CubicBezierInterpolator
import org.telegram.ui.LaunchActivity
import java.util.ArrayList
import kotlin.math.abs

class FlashViews(
    private val context: Context,
    @Nullable private val windowManager: WindowManager?,
    private val windowView: View?,
    @Nullable private val windowViewParams: WindowManager.LayoutParams?
) {

    @JvmField
    val backgroundView: View = object : View(context) {
        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            invalidateGradient()
        }

        override fun dispatchDraw(canvas: Canvas) {
            gradientMatrix.reset()
            drawGradient(canvas, true)
        }
    }

    @JvmField
    val foregroundView: View = object : View(context) {
        override fun dispatchDraw(canvas: Canvas) {
            gradientMatrix.reset()
            gradientMatrix.postTranslate(-x, -y + AndroidUtilities.statusBarHeight)
            gradientMatrix.postScale(1f / scaleX, 1f / scaleY, pivotX, pivotY)
            drawGradient(canvas, false)
        }
    }

    private val invertableViews = ArrayList<Invertable>()

    private var invert = 0f
    private var animator: ValueAnimator? = null

    private var lastWidth = 0
    private var lastHeight = 0
    private var lastColor = 0
    private var lastInvert = 0f
    private var color = 0

    @JvmField
    var colorIndex = 0

    @JvmField
    var warmth = 0.75f

    @JvmField
    var intensity = 1f

    private val gradientMatrix = Matrix()
    private var gradient: RadialGradient? = null
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        paint.alpha = 0
    }

    fun flash(takePicture: Utilities.Callback<Utilities.Callback<Runnable>>) {
        setScreenBrightness(intensityValue())
        flashTo(
            1f,
            320
        ) {
            AndroidUtilities.runOnUIThread(
                {
                    takePicture.run(
                        Utilities.Callback { done: Runnable? ->
                            setScreenBrightness(-1f)
                            AndroidUtilities.runOnUIThread(
                                {
                                    flashTo(0f, 240, done)
                                },
                                80
                            )
                        }
                    )
                },
                320
            )
        }
    }

    private fun setScreenBrightness(value: Float) {
        if (windowView != null && windowViewParams != null) {
            windowViewParams.screenBrightness = value
            windowManager?.updateViewLayout(windowView, windowViewParams)
            return
        }
        var activity: Activity? = AndroidUtilities.findActivity(context)
        if (activity == null) {
            activity = LaunchActivity.instance
        }
        if (activity == null || activity.isFinishing) {
            return
        }
        val window: Window = activity.window ?: return
        val layoutParams = window.attributes
        layoutParams.screenBrightness = value
        window.attributes = layoutParams
    }

    fun previewStart() {
        flashTo(.85f, 240, null)
    }

    fun previewEnd() {
        flashTo(0f, 240, null)
    }

    fun flashIn(done: Runnable?) {
        setScreenBrightness(intensityValue())
        flashTo(1f, 320, done)
    }

    fun flashOut() {
        setScreenBrightness(-1f)
        flashTo(0f, 240, null)
    }

    private fun flashTo(value: Float, duration: Long, whenDone: Runnable?) {
        animator?.cancel()
        animator = null
        if (duration <= 0) {
            invert = value
            update()
            whenDone?.run()
            return
        }
        animator = ValueAnimator.ofFloat(invert, value).apply {
            addUpdateListener { anm ->
                invert = anm.animatedValue as Float
                update()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    invert = value
                    update()
                    whenDone?.run()
                }
            })
            setDuration(duration)
            interpolator = CubicBezierInterpolator.EASE_IN
            start()
        }
    }

    private fun update() {
        for (invertableView in invertableViews) {
            invertableView.setInvert(invert)
            invertableView.invalidate()
        }
        paint.alpha = (0xff * intensityValue() * invert).toInt()
        backgroundView.invalidate()
        foregroundView.invalidate()
    }

    private fun intensityValue(): Float {
        return intensity
    }

    fun add(view: Invertable) {
        view.setInvert(invert)
        invertableViews.add(view)
    }

    fun remove(view: Invertable) {
        invertableViews.remove(view)
    }

    fun setIntensity(intensity: Float) {
        this.intensity = intensity
        update()
    }

    fun setWarmth(warmth: Float) {
        this.warmth = warmth
        color = getColor(warmth)
        invalidateGradient()
    }

    private fun invalidateGradient() {
        if (lastColor != color ||
            lastWidth != backgroundView.measuredWidth ||
            lastHeight != backgroundView.measuredHeight ||
            abs(lastInvert - invert) > 0.005f
        ) {
            lastColor = color
            lastWidth = backgroundView.measuredWidth
            lastHeight = backgroundView.measuredHeight
            lastInvert = invert

            if (lastWidth > 0 && lastHeight > 0) {
                gradient = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    RadialGradient(
                        lastWidth * .5f,
                        lastHeight * .4f,
                        minOf(lastWidth, lastHeight) / 2f * 1.35f * (2f - invert),
                        longArrayOf(
                            Color.valueOf(
                                Color.red(color) / 255f,
                                Color.green(color) / 255f,
                                Color.blue(color) / 255f,
                                0.0f,
                                ColorSpace.get(ColorSpace.Named.EXTENDED_SRGB)
                            ).pack(),
                            Color.valueOf(
                                Color.red(color) / 255f,
                                Color.green(color) / 255f,
                                Color.blue(color) / 255f,
                                1.0f,
                                ColorSpace.get(ColorSpace.Named.EXTENDED_SRGB)
                            ).pack()
                        ),
                        floatArrayOf(AndroidUtilities.lerp(.9f, 0.22f, invert), 1f),
                        Shader.TileMode.CLAMP
                    )
                } else {
                    RadialGradient(
                        lastWidth * .5f,
                        lastHeight * .4f,
                        minOf(lastWidth, lastHeight) / 2f * 1.35f * (2f - invert),
                        intArrayOf(ColorUtils.setAlphaComponent(color, 0), color),
                        floatArrayOf(AndroidUtilities.lerp(.9f, 0.22f, invert), 1f),
                        Shader.TileMode.CLAMP
                    )
                }
                paint.shader = gradient
                invalidate()
            }
        }
    }

    private fun invalidate() {
        backgroundView.invalidate()
        foregroundView.invalidate()
    }

    fun drawGradient(canvas: Canvas, bg: Boolean) {
        val currentGradient = gradient ?: return
        invalidateGradient()
        currentGradient.setLocalMatrix(gradientMatrix)
        if (bg) {
            canvas.drawRect(0f, 0f, lastWidth.toFloat(), lastHeight.toFloat(), paint)
        } else {
            AndroidUtilities.rectTmp.set(0f, 0f, foregroundView.measuredWidth.toFloat(), foregroundView.measuredHeight.toFloat())
            canvas.drawRoundRect(AndroidUtilities.rectTmp, AndroidUtilities.dp(12f) - 2f, AndroidUtilities.dp(12f) - 2f, paint)
        }
    }

    interface Invertable {
        fun setInvert(invert: Float)
        fun invalidate()
    }

    class ImageViewInvertable(context: Context) : ImageView(context), Invertable {
        override fun setInvert(invert: Float) {
            colorFilter = PorterDuffColorFilter(
                ColorUtils.blendARGB(Color.WHITE, Color.BLACK, invert),
                PorterDuff.Mode.MULTIPLY
            )
        }
    }

    companion object {
        @JvmField
        val COLORS = intArrayOf(0xffffffff.toInt(), 0xfffeee8c.toInt(), 0xff8cdfff.toInt())

        @JvmStatic
        fun getColor(warmth: Float): Int {
            return if (warmth < .5f) {
                ColorUtils.blendARGB(
                    0xff8cdfff.toInt(),
                    0xffffffff.toInt(),
                    Utilities.clamp(warmth / .5f, 1f, 0f)
                )
            } else {
                ColorUtils.blendARGB(
                    0xffffffff.toInt(),
                    0xfffeee8c.toInt(),
                    Utilities.clamp((warmth - .5f) / .5f, 1f, 0f)
                )
            }
        }
    }
}
