package org.telegram.tgnet

abstract class TLMethod<T : TLObject> : TLObject() {
    final override fun deserializeResponse(
        stream: InputSerializedData,
        constructor: Int,
        exception: Boolean
    ): TLObject {
        return deserializeResponseT(stream, constructor, exception)
    }

    abstract fun deserializeResponseT(
        stream: InputSerializedData,
        constructor: Int,
        exception: Boolean
    ): T
}
