package org.telegram.ui.Components.chat.layouts

import android.view.View

fun interface ButtonOnLongClickListener {
    fun onLongClick(buttonId: Int, v: View?): Boolean
}
