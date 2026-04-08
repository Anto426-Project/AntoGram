package org.telegram.ui.Components

import android.view.animation.Interpolator

class Easings private constructor() {
    companion object {
        @JvmField
        val easeOutSine: Interpolator = CubicBezierInterpolator(0.39, 0.575, 0.565, 1.0)

        @JvmField
        val easeInOutSine: Interpolator = CubicBezierInterpolator(0.445, 0.05, 0.55, 0.95)

        @JvmField
        val easeInQuad: Interpolator = CubicBezierInterpolator(0.55, 0.085, 0.68, 0.53)

        @JvmField
        val easeOutQuad: Interpolator = CubicBezierInterpolator(0.25, 0.46, 0.45, 0.94)

        @JvmField
        val easeInOutQuad: Interpolator = CubicBezierInterpolator(0.455, 0.03, 0.515, 0.955)
    }
}
