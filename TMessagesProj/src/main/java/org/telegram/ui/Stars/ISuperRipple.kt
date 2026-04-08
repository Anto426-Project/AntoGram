package org.telegram.ui.Stars

import android.view.View

abstract class ISuperRipple(@JvmField val view: View?) {
    open fun animate(cx: Float, cy: Float, intensity: Float) {
    }
}
