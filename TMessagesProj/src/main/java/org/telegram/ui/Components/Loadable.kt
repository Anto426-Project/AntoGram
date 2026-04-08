package org.telegram.ui.Components

interface Loadable {
    fun setLoading(loading: Boolean)
    fun isLoading(): Boolean
}
