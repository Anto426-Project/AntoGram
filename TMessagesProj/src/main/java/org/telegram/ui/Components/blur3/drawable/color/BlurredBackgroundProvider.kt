package org.telegram.ui.Components.blur3.drawable.color

import androidx.annotation.ColorInt
import androidx.annotation.Px

interface BlurredBackgroundProvider : BlurredBackgroundColorProvider {
    @ColorInt
    override fun getShadowColor(): Int

    @ColorInt
    override fun getBackgroundColor(): Int

    @ColorInt
    override fun getStrokeColorTop(): Int

    @ColorInt
    override fun getStrokeColorBottom(): Int

    @Px
    fun getStrokeWidthTop(): Float

    @Px
    fun getStrokeWidthBottom(): Float

    @Px
    fun getShadowRadius(): Float

    @Px
    fun getShadowDx(): Float

    @Px
    fun getShadowDy(): Float
}
