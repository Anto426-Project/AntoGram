package org.telegram.ui

import android.content.Context
import android.view.View
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.Components.SizeNotifierFrameLayout

open class EmptyBaseFragment : BaseFragment() {
    override fun createView(context: Context): View {
        fragmentView = SizeNotifierFrameLayout(context)
        return fragmentView
    }
}
