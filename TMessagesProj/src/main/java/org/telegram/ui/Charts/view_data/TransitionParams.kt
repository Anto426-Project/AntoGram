package org.telegram.ui.Charts.view_data

class TransitionParams {
    @JvmField
    var pickerStartOut: Float = 0f

    @JvmField
    var pickerEndOut: Float = 0f

    @JvmField
    var xPercentage: Float = 0f

    @JvmField
    var date: Long = 0L

    @JvmField
    var pX: Float = 0f

    @JvmField
    var pY: Float = 0f

    @JvmField
    var needScaleY: Boolean = true

    @JvmField
    var progress: Float = 0f

    @JvmField
    var startX: FloatArray? = null

    @JvmField
    var startY: FloatArray? = null

    @JvmField
    var endX: FloatArray? = null

    @JvmField
    var endY: FloatArray? = null

    @JvmField
    var angle: FloatArray? = null
}
