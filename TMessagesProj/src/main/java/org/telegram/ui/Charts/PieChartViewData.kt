package org.telegram.ui.Charts

import android.animation.Animator
import org.telegram.ui.Charts.data.ChartData
import org.telegram.ui.Charts.view_data.StackLinearViewData

class PieChartViewData(line: ChartData.Line) : StackLinearViewData(line) {
    @JvmField
    var selectionA: Float = 0f

    @JvmField
    var drawingPart: Float = 0f

    @JvmField
    var animator: Animator? = null
}
