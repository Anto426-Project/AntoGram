package org.telegram.tgnet

fun interface RequestDelegate {
    fun run(response: TLObject?, error: TLRPC.TL_error?)
}
