package org.telegram.tgnet

fun interface RequestDelegateInternal {
    fun run(
        response: Long,
        errorCode: Int,
        errorText: String?,
        networkType: Int,
        timestamp: Long,
        requestMsgId: Long,
        dcId: Int
    )
}
