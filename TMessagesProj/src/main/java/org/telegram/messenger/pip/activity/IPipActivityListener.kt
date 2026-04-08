package org.telegram.messenger.pip.activity

interface IPipActivityListener {
    fun onStartEnterToPip() {
    }

    fun onCompleteEnterToPip() {
    }

    fun onStartExitFromPip(byActivityStop: Boolean) {
    }

    fun onCompleteExitFromPip(byActivityStop: Boolean) {
    }
}
