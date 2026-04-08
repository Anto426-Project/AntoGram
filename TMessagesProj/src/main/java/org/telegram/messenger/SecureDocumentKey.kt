package org.telegram.messenger

class SecureDocumentKey(key: ByteArray?, iv: ByteArray?) {
    @JvmField
    var file_key: ByteArray? = key

    @JvmField
    var file_iv: ByteArray? = iv
}
