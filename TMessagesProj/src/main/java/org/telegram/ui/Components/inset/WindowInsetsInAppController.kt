package org.telegram.ui.Components.inset

import org.telegram.messenger.AndroidUtilities

interface WindowInsetsInAppController {
    fun requestInAppKeyboardHeightIncludeNavbar(inAppKeyboardHeight: Int) {
        if (inAppKeyboardHeight > 0) {
            requestInAppKeyboardHeight(inAppKeyboardHeight + AndroidUtilities.navigationBarHeight)
        } else {
            resetInAppKeyboardHeight(true)
        }
    }

    fun requestInAppKeyboardHeight(inAppKeyboardHeight: Int)
    fun resetInAppKeyboardHeight(waitKeyboardOpen: Boolean)
}
