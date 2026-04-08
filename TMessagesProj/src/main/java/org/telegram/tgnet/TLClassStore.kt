package org.telegram.tgnet

import android.util.SparseArray
import org.telegram.messenger.FileLog

class TLClassStore {
    private val classStore = SparseArray<Class<*>>()

    init {
        classStore.put(TLRPC.TL_error.constructor, TLRPC.TL_error::class.java)
        classStore.put(TLRPC.TL_decryptedMessageService.constructor, TLRPC.TL_decryptedMessageService::class.java)
        classStore.put(TLRPC.TL_decryptedMessage.constructor, TLRPC.TL_decryptedMessage::class.java)
        classStore.put(TLRPC.TL_decryptedMessageLayer.constructor, TLRPC.TL_decryptedMessageLayer::class.java)
        classStore.put(TLRPC.TL_decryptedMessage_layer17.constructor, TLRPC.TL_decryptedMessage::class.java)
        classStore.put(TLRPC.TL_decryptedMessage_layer45.constructor, TLRPC.TL_decryptedMessage_layer45::class.java)
        classStore.put(TLRPC.TL_decryptedMessageService_layer8.constructor, TLRPC.TL_decryptedMessageService_layer8::class.java)
        classStore.put(TLRPC.TL_decryptedMessage_layer8.constructor, TLRPC.TL_decryptedMessage_layer8::class.java)
        classStore.put(TLRPC.TL_message_secret.constructor, TLRPC.TL_message_secret::class.java)
        classStore.put(TLRPC.TL_message_secret_layer72.constructor, TLRPC.TL_message_secret_layer72::class.java)
        classStore.put(TLRPC.TL_message_secret_old.constructor, TLRPC.TL_message_secret_old::class.java)
        classStore.put(TLRPC.TL_messageEncryptedAction.constructor, TLRPC.TL_messageEncryptedAction::class.java)
        classStore.put(TLRPC.TL_null.constructor, TLRPC.TL_null::class.java)

        classStore.put(TLRPC.TL_updateShortChatMessage.constructor, TLRPC.TL_updateShortChatMessage::class.java)
        classStore.put(TLRPC.TL_updates.constructor, TLRPC.TL_updates::class.java)
        classStore.put(TLRPC.TL_updateShortMessage.constructor, TLRPC.TL_updateShortMessage::class.java)
        classStore.put(TLRPC.TL_updateShort.constructor, TLRPC.TL_updateShort::class.java)
        classStore.put(TLRPC.TL_updatesCombined.constructor, TLRPC.TL_updatesCombined::class.java)
        classStore.put(TLRPC.TL_updateShortSentMessage.constructor, TLRPC.TL_updateShortSentMessage::class.java)
        classStore.put(TLRPC.TL_updatesTooLong.constructor, TLRPC.TL_updatesTooLong::class.java)
    }

    fun TLdeserialize(stream: NativeByteBuffer, constructor: Int, exception: Boolean): TLObject? {
        val objClass = classStore[constructor] ?: return null
        val response = try {
            objClass.getDeclaredConstructor().newInstance() as TLObject
        } catch (e: Throwable) {
            FileLog.e(e)
            return null
        }
        response.readParams(stream, exception)
        return response
    }

    companion object {
        private var store: TLClassStore? = null

        @JvmStatic
        fun Instance(): TLClassStore {
            if (store == null) {
                store = TLClassStore()
            }
            return store!!
        }
    }
}
