package org.telegram.ui.Components.inset

import androidx.core.graphics.Insets
import androidx.core.view.WindowInsetsCompat

interface WindowInsetsProvider {
    fun getAnimatedMaxBottomInset(): Float
    fun getAnimatedImeBottomInset(): Float
    fun getAnimatedKeyboardVisibility(): Float

    fun getCurrentMaxBottomInset(): Int

    fun getInsets(type: Int): Insets

    fun getCurrentNavigationBarInset(): Int {
        return getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
    }

    fun inAppViewIsVisible(): Boolean

    fun getInAppKeyboardRecommendedViewHeight(): Int
}
