package com.codephrex.chirp.orb

/** Worker-confined accumulator; never publishes incomplete or stale PCM frames. */
internal class PcmFramer(private val size: Int = 512) {
    init { require(size > 0) }
    private val buffer = ShortArray(size)
    private var used = 0

    fun reset() {
        used = 0
        buffer.fill(0)
    }

    fun append(samples: ShortArray, count: Int, onFrame: (ShortArray) -> Unit) {
        require(count in 0..samples.size)
        var offset = 0
        while (offset < count) {
            val n = minOf(size - used, count - offset)
            samples.copyInto(buffer, used, offset, offset + n)
            used += n
            offset += n
            if (used == size) {
                val frame = buffer.copyOf()
                used = 0
                onFrame(frame)
            }
        }
    }
}
