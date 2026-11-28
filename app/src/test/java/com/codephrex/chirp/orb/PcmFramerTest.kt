package com.codephrex.chirp.orb

import org.junit.Assert.*
import org.junit.Test

class PcmFramerTest {
    @Test fun partialReadsAndMultipleFrames() {
        val framer = PcmFramer(4)
        val frames = mutableListOf<ShortArray>()
        framer.append(shortArrayOf(1, 2, 99), 2) { frames.add(it) }
        assertTrue(frames.isEmpty())
        framer.append(shortArrayOf(3, 4, 5, 6, 7, 8), 6) { frames.add(it) }
        assertEquals(2, frames.size)
        assertArrayEquals(shortArrayOf(1, 2, 3, 4), frames[0])
        assertArrayEquals(shortArrayOf(5, 6, 7, 8), frames[1])
    }

    @Test fun resetDiscardsPartialFrame() {
        val framer = PcmFramer(2)
        val frames = mutableListOf<ShortArray>()
        framer.append(shortArrayOf(99), 1) { frames.add(it) }
        framer.reset()
        framer.append(shortArrayOf(1, 2), 2) { frames.add(it) }
        assertEquals(1, frames.size)
        assertArrayEquals(shortArrayOf(1, 2), frames.single())
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsNegativeReadCount() {
        PcmFramer().append(shortArrayOf(1), -1) {}
    }
}
