package org.telegram.ui.Components.blur3.source

import android.graphics.Canvas
import org.telegram.ui.Components.blur3.drawable.BlurredBackgroundDrawable

interface BlurredBackgroundSource {
    fun createDrawable(): BlurredBackgroundDrawable?

    fun draw(canvas: Canvas?, left: Float, top: Float, right: Float, bottom: Float)

    fun dispatchOnDrawablesRelativePositionChange() {
    }
}
