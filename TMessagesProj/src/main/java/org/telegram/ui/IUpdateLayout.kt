package org.telegram.ui

import android.app.Activity
import android.view.ViewGroup
import androidx.annotation.Keep

@Keep
abstract class IUpdateLayout @Keep constructor(activity: Activity?, sideMenuContainer: ViewGroup?) {
    @Keep
    open fun updateFileProgress(args: Array<Any?>?) {
    }

    @Keep
    open fun createUpdateUI(currentAccount: Int) {
    }

    @Keep
    open fun updateAppUpdateViews(currentAccount: Int, animated: Boolean) {
    }
}
