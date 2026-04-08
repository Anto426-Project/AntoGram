package org.telegram.ui.Components

import android.graphics.drawable.Drawable

abstract class RecyclableDrawable : Drawable() {
    abstract fun recycle()
}
