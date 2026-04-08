package org.telegram.tgnet

interface InputSerializedData {
    fun getDataSourceType(): TLDataSourceType

    fun readBool(exception: Boolean): Boolean
    fun readInt32(exception: Boolean): Int
    fun readInt64(exception: Boolean): Long
    fun readByte(exception: Boolean): Byte
    fun readBytes(b: ByteArray?, exception: Boolean)
    fun readData(count: Int, exception: Boolean): ByteArray?
    fun readString(exception: Boolean): String?
    fun readByteArray(exception: Boolean): ByteArray?
    fun readFloat(exception: Boolean): Float
    fun readDouble(exception: Boolean): Double
    fun readByteBuffer(exception: Boolean): NativeByteBuffer?

    fun length(): Int
    fun skip(count: Int)
    fun getPosition(): Int
    fun remaining(): Int
}
