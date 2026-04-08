package org.telegram.messenger.voip

import java.nio.ByteBuffer

class Resampler {
    companion object {
        @JvmStatic
        external fun convert44to48(from: ByteBuffer?, to: ByteBuffer?): Int

        @JvmStatic
        external fun convert48to44(from: ByteBuffer?, to: ByteBuffer?): Int
    }
}
