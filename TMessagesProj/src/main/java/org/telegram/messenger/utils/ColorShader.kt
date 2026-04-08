package org.telegram.messenger.utils

import android.graphics.LinearGradient

class ColorShader(color: Int) : LinearGradient(
    0f,
    0f,
    1f,
    0f,
    intArrayOf(color, color),
    null,
    TileMode.CLAMP
)
