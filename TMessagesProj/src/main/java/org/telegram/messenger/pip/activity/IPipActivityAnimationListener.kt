package org.telegram.messenger.pip.activity

interface IPipActivityAnimationListener {
    fun onEnterAnimationStart(estimatedDuration: Long) {
    }

    fun onEnterAnimationEnd(duration: Long) {
    }

    fun onLeaveAnimationStart(estimatedDuration: Long) {
    }

    fun onLeaveAnimationEnd(duration: Long) {
    }

    fun onTransitionAnimationFrame() {
    }

    fun onTransitionAnimationProgress(estimatedProgress: Float) {
    }
}
