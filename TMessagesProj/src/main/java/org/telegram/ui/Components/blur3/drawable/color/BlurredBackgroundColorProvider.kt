package org.telegram.ui.Components.blur3.drawable.color

import androidx.annotation.ColorInt

interface BlurredBackgroundColorProvider {
    @ColorInt
    fun getShadowColor(): Int

    @ColorInt
    fun getBackgroundColor(): Int

    @ColorInt
    fun getStrokeColorTop(): Int

    @ColorInt
    fun getStrokeColorBottom(): Int
}
