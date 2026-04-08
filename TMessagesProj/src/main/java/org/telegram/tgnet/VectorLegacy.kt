package org.telegram.tgnet

object VectorLegacy {
    @JvmStatic
    fun deserialize_IntAsLong(stream: InputSerializedData, exception: Boolean): ArrayList<Long> {
        val res = Vector.deserializeInt(stream, exception)
        val result = ArrayList<Long>(res.size)
        for (num in res) {
            result.add(num.toLong())
        }
        return result
    }

    @JvmStatic
    fun serialize_LongAsInt(stream: OutputSerializedData, objects: ArrayList<Long>) {
        val ser = ArrayList<Int>(objects.size)
        for (num in objects) {
            ser.add(num.toInt())
        }
        Vector.serializeInt(stream, ser)
    }

    @JvmStatic
    fun deserialize_IntUserIdAsPeer(stream: InputSerializedData, exception: Boolean): ArrayList<TLRPC.Peer> {
        val userIds = Vector.deserializeInt(stream, exception)
        val result = ArrayList<TLRPC.Peer>(userIds.size)
        for (userId in userIds) {
            val user = TLRPC.TL_peerUser()
            user.user_id = userId.toLong()
            result.add(user)
        }
        return result
    }

    @JvmStatic
    fun serialize_PeerAsIntUserId(stream: OutputSerializedData, objects: ArrayList<TLRPC.Peer>) {
        stream.writeInt32(Vector.constructor)
        val count = objects.size
        stream.writeInt32(count)
        for (a in 0 until count) {
            stream.writeInt32(objects[a].user_id.toInt())
        }
    }

    @JvmStatic
    fun deserialize_LongUserIdAsPeer(stream: InputSerializedData, exception: Boolean): ArrayList<TLRPC.Peer> {
        val userIds = Vector.deserializeLong(stream, exception)
        val result = ArrayList<TLRPC.Peer>(userIds.size)
        for (userId in userIds) {
            val user = TLRPC.TL_peerUser()
            user.user_id = userId
            result.add(user)
        }
        return result
    }

    @JvmStatic
    fun serialize_PeerAsLongUserId(stream: OutputSerializedData, objects: ArrayList<TLRPC.Peer>) {
        stream.writeInt32(Vector.constructor)
        val count = objects.size
        stream.writeInt32(count)
        for (a in 0 until count) {
            stream.writeInt64(objects[a].user_id)
        }
    }
}
