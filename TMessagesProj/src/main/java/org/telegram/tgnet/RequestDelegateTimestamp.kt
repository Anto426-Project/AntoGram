package org.telegram.tgnet

fun interface RequestDelegateTimestamp {
    fun run(response: TLObject?, error: TLRPC.TL_error?, responseTime: Long)
}
