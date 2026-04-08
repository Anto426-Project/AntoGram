package org.telegram.tgnet

import org.telegram.messenger.FileLog

class TLParseException private constructor(message: String) : RuntimeException(message) {
    companion object {
        @JvmStatic
        fun doThrowOrLog(
            stream: InputSerializedData?,
            tlTypeName: String,
            constructorId: Int,
            throwEnabled: Boolean
        ) {
            val dataSourceType = stream?.getDataSourceType()
            val message = String.format(
                "can't parse magic %x in %s. Source: %s",
                constructorId,
                tlTypeName,
                dataSourceType
            )
            val tlParseException = TLParseException(message)

            FileLog.e(tlParseException, constructorId != 0xcd78e586.toInt())

            if (throwEnabled) {
                throw tlParseException
            }
        }
    }
}
