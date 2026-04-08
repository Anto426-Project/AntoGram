package org.telegram.ui.Components.Paint

class Swatch(color: Int, colorLocation: Float, brushWeight: Float) {
    @JvmField
    var color: Int = color

    @JvmField
    var colorLocation: Float = colorLocation

    @JvmField
    var brushWeight: Float = brushWeight

    fun clone(): Swatch {
        return Swatch(color, colorLocation, brushWeight)
    }
}
