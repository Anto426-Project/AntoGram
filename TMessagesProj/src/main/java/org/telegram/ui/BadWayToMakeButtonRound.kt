package org.telegram.ui

import android.view.View
import org.telegram.messenger.utils.ViewOutlineProviderImpl

class BadWayToMakeButtonRound {
    companion object {
        @JvmStatic
        fun round(view: View?) {
            view?.setOutlineProvider(ViewOutlineProviderImpl.BOUNDS_ROUND_RECT)
            view?.clipToOutline = true
        }
    }
}
