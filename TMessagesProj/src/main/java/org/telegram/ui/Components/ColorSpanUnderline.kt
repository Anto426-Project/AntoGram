package org.telegram.ui.Components

import android.text.TextPaint
import android.text.style.ForegroundColorSpan

class ColorSpanUnderline(color: Int) : ForegroundColorSpan(color) {
    override fun updateDrawState(ds: TextPaint) {
        super.updateDrawState(ds)
        ds.isUnderlineText = true
    }
}
