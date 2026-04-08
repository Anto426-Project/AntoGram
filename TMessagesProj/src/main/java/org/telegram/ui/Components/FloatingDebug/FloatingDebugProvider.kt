package org.telegram.ui.Components.FloatingDebug

interface FloatingDebugProvider {
    fun onGetDebugItems(): List<FloatingDebugController.DebugItem?>?
}
