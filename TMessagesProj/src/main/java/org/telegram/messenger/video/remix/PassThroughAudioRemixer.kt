package org.telegram.messenger.video.remix

import java.nio.ShortBuffer

/**
 * The simplest [AudioRemixer] that does nothing.
 */
class PassThroughAudioRemixer : AudioRemixer {
    override fun remix(
        inputBuffer: ShortBuffer,
        inputChannelCount: Int,
        outputBuffer: ShortBuffer,
        outputChannelCount: Int
    ) {
        outputBuffer.put(inputBuffer)
    }

    override fun getRemixedSize(inputSize: Int, inputChannelCount: Int, outputChannelCount: Int): Int {
        return inputSize
    }
}
