package com.codephrex.chirp.orb

import org.junit.Assert.*
import org.junit.Test
import kotlin.math.*

class AudioAnalysisTest {
    private fun tone(hz: Int) = ShortArray(512) { (12000 * sin(2 * PI * hz * it / 16000)).toInt().toShort() }
    @Test fun silenceAndDc() {
        val a = AudioAnalysis()
        assertEquals(0f, a.analyze(ShortArray(512)).amplitude, 0f)
        assertEquals(0f, a.analyze(ShortArray(512) { 1000 }).amplitude, 0f)
    }
    @Test fun separatesBands() {
        val low = AudioAnalysis().analyze(tone(188))
        val mid = AudioAnalysis().analyze(tone(1000))
        val high = AudioAnalysis().analyze(tone(3000))
        assertTrue(low.low > low.mid && low.low > low.high)
        assertTrue(mid.mid > mid.low && mid.mid > mid.high)
        assertTrue(high.high > high.low && high.high > high.mid)
    }
    @Test fun decayAndReset() {
        val a = AudioAnalysis()
        val loud = a.analyze(tone(1000))
        val quiet = a.analyze(ShortArray(512))
        assertTrue(quiet.amplitude in 0f..loud.amplitude)
        assertTrue(quiet.amplitude < loud.amplitude)
        a.reset()
        assertEquals(0f, a.analyze(ShortArray(512)).amplitude, 0f)
    }
}
