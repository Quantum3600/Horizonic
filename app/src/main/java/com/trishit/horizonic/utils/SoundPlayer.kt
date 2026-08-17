package com.trishit.horizonic.utils

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.time.Duration.Companion.milliseconds

object SoundPlayer {
    private var audioTrack: AudioTrack? = null
    private var isPlaying = false
    private var job: Job? = null
    private val playerScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    val playingState = MutableStateFlow(false)
    val remainingSeconds = MutableStateFlow(0)
    val currentFrequency = MutableStateFlow(100f)
    val currentDurationSeconds = MutableStateFlow(60)

    var onStateChanged: (() -> Unit)? = null

    private fun notifyStateChanged() {
        onStateChanged?.invoke()
    }

    fun startPlaying(
        frequency: Float = currentFrequency.value,
        durationSeconds: Int = currentDurationSeconds.value,
        scope: CoroutineScope = playerScope
    ) {
        if(isPlaying) stopPlaying()
        isPlaying = true
        playingState.value = true
        currentFrequency.value = frequency
        currentDurationSeconds.value = durationSeconds
        remainingSeconds.value = durationSeconds
        notifyStateChanged()

        job = scope.launch(Dispatchers.Default) {
            try {
                val sampleRate = 44100
                val minBufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                if (minBufferSize <= 0) return@launch

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(minBufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
                audioTrack = track
                track.play()

                val bufferSize = minBufferSize/2
                val buffer = ShortArray(bufferSize)
                var phase = 0.0

                val startTime = System.currentTimeMillis()
                val durationMs = durationSeconds * 1000L
                val isInfinite = durationSeconds <= 0
                var lastSecUpdate = startTime

                while (isPlaying && (isInfinite || (System.currentTimeMillis() - startTime) < durationMs)) {
                    val freq = currentFrequency.value
                    val phaseIncrement = 2 * Math.PI * freq / sampleRate

                    for (i in 0 until bufferSize) {
                        buffer[i] = (sin(phase) * 32767).toInt().toShort()
                        phase += phaseIncrement
                        if (phase > 2 * Math.PI) {
                            phase -= 2 * Math.PI
                        }
                    }
                    track.write(buffer, 0, buffer.size)

                    val now = System.currentTimeMillis()
                    if (now - lastSecUpdate >= 1000L && !isInfinite) {
                        val elapsed = ((now - startTime) / 1000).toInt()
                        remainingSeconds.value = (durationSeconds - elapsed).coerceAtLeast(0)
                        lastSecUpdate = now
                        notifyStateChanged()
                    }
                    delay(1.milliseconds)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                stopPlaying()
            }
        }
    }

    fun stopPlaying() {
        isPlaying = false
        playingState.value = false
        remainingSeconds.value = 0
        notifyStateChanged()
        job?.cancel()
        job = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Exception) {}
        audioTrack = null
    }
}