package org.telegram.tgnet

abstract class AbstractSerializedData : InputSerializedData, OutputSerializedData {
    private var dataSourceType: TLDataSourceType = TLDataSourceType.UNKNOWN

    fun setDataSourceType(dataSourceType: TLDataSourceType) {
        this.dataSourceType = dataSourceType
    }

    override fun getDataSourceType(): TLDataSourceType {
        return dataSourceType
    }

    abstract override fun writeInt32(value: Int)

    abstract override fun writeInt64(value: Long)

    abstract override fun writeBool(value: Boolean)

    abstract override fun writeBytes(b: ByteArray?)

    abstract override fun writeBytes(b: ByteArray?, offset: Int, count: Int)

    abstract override fun writeByte(i: Int)

    abstract override fun writeByte(b: Byte)

    abstract override fun writeString(s: String?)

    abstract override fun writeByteArray(b: ByteArray?, offset: Int, count: Int)

    abstract override fun writeByteArray(b: ByteArray?)

    abstract override fun writeDouble(d: Double)

    abstract override fun writeByteBuffer(buffer: NativeByteBuffer?)

    abstract override fun writeFloat(f: Float)

    abstract override fun readInt32(exception: Boolean): Int

    abstract override fun readBool(exception: Boolean): Boolean

    abstract override fun readInt64(exception: Boolean): Long

    abstract override fun readByte(exception: Boolean): Byte

    abstract override fun readBytes(b: ByteArray?, exception: Boolean)

    abstract override fun readData(count: Int, exception: Boolean): ByteArray?

    abstract override fun readString(exception: Boolean): String?

    abstract override fun readByteArray(exception: Boolean): ByteArray?

    abstract override fun readFloat(exception: Boolean): Float

    abstract override fun readByteBuffer(exception: Boolean): NativeByteBuffer?

    abstract override fun readDouble(exception: Boolean): Double

    abstract override fun length(): Int

    abstract override fun skip(count: Int)

    abstract override fun getPosition(): Int

    abstract override fun remaining(): Int
}
