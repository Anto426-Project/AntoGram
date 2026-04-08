package org.telegram.tgnet

interface OutputSerializedData {
    fun writeInt32(value: Int)
    fun writeInt64(value: Long)
    fun writeBool(value: Boolean)
    fun writeBytes(b: ByteArray?)
    fun writeBytes(b: ByteArray?, offset: Int, count: Int)
    fun writeByte(i: Int)
    fun writeByte(b: Byte)
    fun writeString(s: String?)
    fun writeByteArray(b: ByteArray?, offset: Int, count: Int)
    fun writeByteArray(b: ByteArray?)
    fun writeFloat(f: Float)
    fun writeDouble(d: Double)
    fun writeByteBuffer(buffer: NativeByteBuffer?)

    fun skip(count: Int)
    fun getPosition(): Int
}
