package org.telegram.tgnet

fun interface RequestTimeDelegate {
    fun run(time: Long)
}
