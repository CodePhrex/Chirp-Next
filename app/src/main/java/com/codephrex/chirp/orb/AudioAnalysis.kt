package com.codephrex.chirp.orb

import kotlin.math.*

/** PCM16 analysis, independent of Android capture and GPU lifecycle. */
internal class AudioAnalysis(private val sampleRate: Int = 16000, private val size: Int = 512) {
    data class Frame(val amplitude: Float, val low: Float, val mid: Float, val high: Float)
    init { require(sampleRate >= 8000 && size >= 64 && size and (size - 1) == 0) }
    private val window = DoubleArray(size) { 0.5 - 0.5 * cos(2 * PI * it / (size - 1)) }
    private var previous = Frame(0f, 0f, 0f, 0f)
    fun reset() { previous = Frame(0f, 0f, 0f, 0f) }
    fun analyze(pcm: ShortArray): Frame {
        require(pcm.size == size)
        val mean = pcm.sumOf { it.toDouble() } / size
        val re = DoubleArray(size)
        val im = DoubleArray(size)
        var energy = 0.0
        for (i in pcm.indices) {
            val x = (pcm[i] - mean) / 32768.0
            energy += x * x
            re[i] = x * window[i]
        }
        var j = 0
        for (i in 1 until size) {
            var bit = size shr 1
            while (j and bit != 0) { j = j xor bit; bit = bit shr 1 }
            j = j xor bit
            if (i < j) { val t = re[i]; re[i] = re[j]; re[j] = t }
        }
        var length = 2
        while (length <= size) {
            for (start in 0 until size step length) {
                for (k in 0 until length / 2) {
                    val angle = -2 * PI * k / length
                    val a = start + k
                    val b = a + length / 2
                    val tr = re[b] * cos(angle) - im[b] * sin(angle)
                    val ti = re[b] * sin(angle) + im[b] * cos(angle)
                    re[b] = re[a] - tr; im[b] = im[a] - ti
                    re[a] += tr; im[a] += ti
                }
            }
            length *= 2
        }
        val bands = DoubleArray(3)
        for (k in 1 until size / 2) {
            val hz = k.toDouble() * sampleRate / size
            val band = when { hz < 80 -> continue; hz < 300 -> 0; hz < 2000 -> 1; else -> 2 }
            bands[band] += re[k] * re[k] + im[k] * im[k]
        }
        fun smooth(old: Float, raw: Double): Float {
            val target = raw.coerceIn(0.0, 1.0).toFloat()
            return old + (target - old) * if (target > old) 0.65f else 0.25f
        }
        previous = Frame(smooth(previous.amplitude, sqrt(energy / size) * 4),
            smooth(previous.low, sqrt(bands[0]) * 8 / size),
            smooth(previous.mid, sqrt(bands[1]) * 8 / size),
            smooth(previous.high, sqrt(bands[2]) * 8 / size))
        return previous
    }
}
