package org.telegram.ui.Charts.view_data

import android.graphics.Paint
import org.telegram.ui.Charts.BaseChartView
import org.telegram.ui.Charts.data.ChartData

open class StackLinearViewData(line: ChartData.Line) : LineViewData(line, false) {
    init {
        paint.style = Paint.Style.FILL
        if (BaseChartView.USE_LINES) {
            paint.isAntiAlias = false
        }
    }
}
