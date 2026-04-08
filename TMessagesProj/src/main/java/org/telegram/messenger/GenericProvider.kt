package org.telegram.messenger

fun interface GenericProvider<F, T> {
    fun provide(obj: F): T
}
