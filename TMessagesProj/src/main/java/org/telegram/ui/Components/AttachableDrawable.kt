package org.telegram.ui.Components

import android.view.View
import org.telegram.messenger.ImageReceiver

interface AttachableDrawable {
    fun onAttachedToWindow(parent: ImageReceiver?)
    fun onDetachedFromWindow(parent: ImageReceiver?)

    fun setParent(view: View?) {
    }
}
