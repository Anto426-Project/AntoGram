package org.telegram.ui.Components.chat.layouts

import android.view.View

fun interface ButtonOnClickListener {
    fun onClick(buttonId: Int, v: View?)
}
