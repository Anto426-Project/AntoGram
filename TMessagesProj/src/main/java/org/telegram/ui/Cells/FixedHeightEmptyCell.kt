package org.telegram.ui.Cells

import android.content.Context
import android.view.View
import org.telegram.messenger.AndroidUtilities

class FixedHeightEmptyCell(context: Context, @JvmField var heightInDp: Int) : View(context) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(
            widthMeasureSpec,
            MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(heightInDp.toFloat()), MeasureSpec.EXACTLY)
        )
    }
}
