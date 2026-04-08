/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.telegram.messenger.camera

class Size(
    @JvmField val mWidth: Int,
    @JvmField val mHeight: Int
) {

    fun getWidth(): Int {
        return mWidth
    }

    fun getHeight(): Int {
        return mHeight
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        }
        if (this === other) {
            return true
        }
        return other is Size && mWidth == other.mWidth && mHeight == other.mHeight
    }

    override fun toString(): String {
        return mWidth.toString() + "x" + mHeight
    }

    override fun hashCode(): Int {
        return mHeight xor ((mWidth shl (Int.SIZE_BITS / 2)) or (mWidth ushr (Int.SIZE_BITS / 2)))
    }

    companion object {
        @JvmStatic
        @Throws(NumberFormatException::class)
        fun parseSize(string: String): Size {
            var sepIx = string.indexOf('*')
            if (sepIx < 0) {
                sepIx = string.indexOf('x')
            }
            if (sepIx < 0) {
                throw invalidSize(string)
            }
            return try {
                Size(string.substring(0, sepIx).toInt(), string.substring(sepIx + 1).toInt())
            } catch (_: NumberFormatException) {
                throw invalidSize(string)
            }
        }

        private fun invalidSize(value: String): NumberFormatException {
            throw NumberFormatException("Invalid Size: \"$value\"")
        }
    }
}
