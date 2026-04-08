package org.telegram.ui.Components.blur3.capture

import android.graphics.Canvas
import android.graphics.RectF

interface IBlur3Capture {
    fun capture(canvas: Canvas?, position: RectF?)

    fun captureCalculateHash(builder: IBlur3Hash, position: RectF?) {
        builder.unsupported()
    }
}
