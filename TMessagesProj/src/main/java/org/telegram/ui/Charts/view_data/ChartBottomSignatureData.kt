package org.telegram.ui.Charts.view_data

class ChartBottomSignatureData(step: Int, stepMax: Int, stepMin: Int) {
    @JvmField
    val step: Int = step

    @JvmField
    val stepMax: Int = stepMax

    @JvmField
    val stepMin: Int = stepMin

    @JvmField
    var alpha: Int = 0

    @JvmField
    var fixedAlpha: Int = 255
}
