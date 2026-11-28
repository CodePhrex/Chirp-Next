package com.codephrex.chirp.orb

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder

/** One worker owns the recorder. Call start/stop on the main thread.
 * Callbacks run on the worker; consumers must marshal UI updates.
 * No PCM is persisted or transmitted.
 */
internal class MicrophoneCapture(context: Context) {
    private val app = context.applicationContext
    @Volatile private var requested = false
    private var worker: Thread? = null

    @Synchronized fun start(onFrame: (AudioAnalysis.Frame) -> Unit,
                            onFailure: (String) -> Unit): Boolean {
        if (worker?.isAlive == true) return false
        if (app.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) return false
        requested = true
        worker = Thread({
            var recorder: AudioRecord? = null
            try {
                val rate = 16000
                val channel = AudioFormat.CHANNEL_IN_MONO
                val format = AudioFormat.ENCODING_PCM_16BIT
                val minimum = AudioRecord.getMinBufferSize(rate, channel, format)
                check(minimum > 0) { "Microphone format unavailable" }
                if (!requested) return@Thread
                check(app.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) { "Microphone permission revoked" }
                val input = AudioRecord(MediaRecorder.AudioSource.MIC, rate, channel, format, maxOf(minimum, 2048))
                recorder = input
                check(input.state == AudioRecord.STATE_INITIALIZED) { "Microphone initialization failed" }
                if (requested) {
                    input.startRecording()
                    check(input.recordingState == AudioRecord.RECORDSTATE_RECORDING) { "Microphone start failed" }
                    val samples = ShortArray(512)
                    val framer = PcmFramer()
                    val analysis = AudioAnalysis()
                    while (requested) {
                        val count = input.read(samples, 0, samples.size, AudioRecord.READ_NON_BLOCKING)
                        check(count >= 0) { "Microphone read failed" }
                        if (count == 0) Thread.sleep(5)
                        else framer.append(samples, count) { pcm ->
                            if (requested) onFrame(analysis.analyze(pcm))
                        }
                    }
                }
            } catch (e: Exception) {
                if (requested) onFailure("Microphone capture failed")
            } finally {
                try {
                    recorder?.let { if (it.recordingState == AudioRecord.RECORDSTATE_RECORDING) it.stop() }
                } finally {
                    try { recorder?.release() } finally { requested = false }
                }
            }
        }, "Chirp-Microphone").also { it.start() }
        return true
    }

    /** Nonblocking cancellation; restart is rejected until the worker exits. */
    @Synchronized fun stop() { requested = false }
}
