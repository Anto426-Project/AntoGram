package org.telegram.ui.Components

import android.graphics.drawable.Drawable

abstract class StatusDrawable : Drawable() {
    abstract fun start()
    abstract fun stop()
    abstract fun setIsChat(value: Boolean)
    abstract fun setColor(color: Int)
}
