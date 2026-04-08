package org.telegram.tgnet

interface ResultCallback<T> {
    fun onComplete(result: T)

    fun onError(error: TLRPC.TL_error?) {
    }

    fun onError(throwable: Throwable?) {
    }
}
