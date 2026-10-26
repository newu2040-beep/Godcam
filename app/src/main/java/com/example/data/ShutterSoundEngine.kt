package com.example.data

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.model.ShutterSoundStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

/**
 * Real-time acoustic waveform synthesizer for tactile camera shutter and video recording sounds.
 * Generates authentic mechanical and optical leaf shutter transients (Fuji X-Series Mechanical,
 * Fuji X100 Leaf Shutter, Rangefinder M, Classic 35mm SLR mirror + curtain, and Digital Pro chirp)
 * directly in 44.1 kHz 16-bit PCM via Android's [AudioTrack].
 */
object ShutterSoundEngine {

    private const val SAMPLE_RATE = 44100
    private val audioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun playShutterSound(style: ShutterSoundStyle) {
        if (style == ShutterSoundStyle.OFF) return
        audioScope.launch {
            runCatching {
                val pcm = when (style) {
                    ShutterSoundStyle.OFF -> return@launch
                    ShutterSoundStyle.FUJI_MECHANICAL -> synthesizeFujiMechanical()
                    ShutterSoundStyle.FUJI_LEAF -> synthesizeFujiLeaf()
                    ShutterSoundStyle.LEICA_RANGEFINDER -> synthesizeRangefinderClick()
                    ShutterSoundStyle.CLASSIC_SLR -> synthesizeClassicSlr()
                    ShutterSoundStyle.DIGITAL_PRO -> synthesizeDigitalPro()
                }
                playPcmBuffer(pcm)
            }
        }
    }

    fun playTimerCountdownTick(style: ShutterSoundStyle, isFinalSecond: Boolean = false) {
        if (style == ShutterSoundStyle.OFF) return
        audioScope.launch {
            runCatching {
                val durationMs = if (isFinalSecond) 55 else 35
                val numSamples = (SAMPLE_RATE * durationMs) / 1000
                val buffer = ShortArray(numSamples)
                val freq = if (isFinalSecond) 1760.0 else 1180.0
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / SAMPLE_RATE
                    val env = exp(-t * 85.0)
                    val wave = sin(2.0 * PI * freq * t) * 0.55 + sin(2.0 * PI * (freq * 2.0) * t) * 0.25
                    buffer[i] = (wave * env * Short.MAX_VALUE * 0.45).toInt()
                        .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
                playPcmBuffer(buffer)
            }
        }
    }

    fun playVideoRecordTone(style: ShutterSoundStyle, isStarting: Boolean) {
        if (style == ShutterSoundStyle.OFF) return
        audioScope.launch {
            runCatching {
                val durationMs = 110
                val numSamples = (SAMPLE_RATE * durationMs) / 1000
                val buffer = ShortArray(numSamples)
                val f1 = if (isStarting) 880.0 else 1046.5
                val f2 = if (isStarting) 1318.5 else 659.25
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / SAMPLE_RATE
                    val freq = if (t < 0.048) f1 else f2
                    val localT = if (t < 0.048) t else (t - 0.048)
                    val env = exp(-localT * 38.0) * (1.0 - exp(-localT * 450.0))
                    val click = if (i < 160) (Random.nextDouble(-1.0, 1.0) * exp(-t * 260.0) * 0.35) else 0.0
                    val sample = (sin(2.0 * PI * freq * t) * 0.65 + sin(2.0 * PI * freq * 2.0 * t) * 0.18) * env + click
                    buffer[i] = (sample * Short.MAX_VALUE * 0.42).toInt()
                        .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
                playPcmBuffer(buffer)
            }
        }
    }

    /**
     * Fujifilm X-T5 / X-Pro3 style crisp dual-curtain focal-plane mechanical shutter:
     * Front curtain metallic release at t=0ms + dampened rear curtain arrest at t=26ms.
     */
    private fun synthesizeFujiMechanical(): ShortArray {
        val durationMs = 88
        val numSamples = (SAMPLE_RATE * durationMs) / 1000
        val out = ShortArray(numSamples)
        val rng = Random(108)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            // Curtain 1 impulse at t = 0
            val env1 = exp(-t * 165.0)
            val metallic1 = (sin(2.0 * PI * 2450.0 * t) * 0.42 +
                sin(2.0 * PI * 4120.0 * t) * 0.31 +
                sin(2.0 * PI * 680.0 * t) * 0.35 +
                rng.nextDouble(-1.0, 1.0) * 0.48) * env1

            // Curtain 2 impulse at t = 25ms
            val t2 = t - 0.025
            val curtain2 = if (t2 >= 0.0) {
                val env2 = exp(-t2 * 145.0)
                (sin(2.0 * PI * 1980.0 * t2) * 0.45 +
                    sin(2.0 * PI * 3650.0 * t2) * 0.28 +
                    sin(2.0 * PI * 520.0 * t2) * 0.40 +
                    rng.nextDouble(-1.0, 1.0) * 0.42) * env2 * 0.88
            } else 0.0

            val combined = (metallic1 + curtain2).coerceIn(-1.0, 1.0)
            out[i] = (combined * Short.MAX_VALUE * 0.72).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return out
    }

    /**
     * Fujifilm X100VI / X100V style ultra-delicate optical leaf shutter:
     * Short, crisp high-frequency iris blade snick with near-zero body vibration.
     */
    private fun synthesizeFujiLeaf(): ShortArray {
        val durationMs = 46
        val numSamples = (SAMPLE_RATE * durationMs) / 1000
        val out = ShortArray(numSamples)
        val rng = Random(204)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val env1 = exp(-t * 290.0)
            val tick1 = (sin(2.0 * PI * 3400.0 * t) * 0.50 +
                sin(2.0 * PI * 5400.0 * t) * 0.30 +
                rng.nextDouble(-1.0, 1.0) * 0.35) * env1

            val t2 = t - 0.011
            val tick2 = if (t2 >= 0.0) {
                val env2 = exp(-t2 * 260.0)
                (sin(2.0 * PI * 2900.0 * t2) * 0.42 +
                    rng.nextDouble(-1.0, 1.0) * 0.28) * env2 * 0.65
            } else 0.0

            val combined = (tick1 + tick2).coerceIn(-1.0, 1.0)
            out[i] = (combined * Short.MAX_VALUE * 0.60).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return out
    }

    /**
     * Classic Rangefinder M dampened cloth-curtain click with warm low-mid brass body thud.
     */
    private fun synthesizeRangefinderClick(): ShortArray {
        val durationMs = 75
        val numSamples = (SAMPLE_RATE * durationMs) / 1000
        val out = ShortArray(numSamples)
        val rng = Random(309)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val env = exp(-t * 120.0)
            val brassThud = sin(2.0 * PI * 340.0 * t) * 0.55 +
                sin(2.0 * PI * 1120.0 * t) * 0.32 +
                rng.nextDouble(-1.0, 1.0) * 0.30 * exp(-t * 210.0)

            val t2 = t - 0.018
            val second = if (t2 >= 0.0) {
                (sin(2.0 * PI * 420.0 * t2) * 0.45 + sin(2.0 * PI * 1480.0 * t2) * 0.25) * exp(-t2 * 135.0)
            } else 0.0

            val combined = (brassThud * env + second).coerceIn(-1.0, 1.0)
            out[i] = (combined * Short.MAX_VALUE * 0.68).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return out
    }

    /**
     * Classic 35mm Film SLR: mirror up slap + focal plane curtain + mirror return resonance.
     */
    private fun synthesizeClassicSlr(): ShortArray {
        val durationMs = 135
        val numSamples = (SAMPLE_RATE * durationMs) / 1000
        val out = ShortArray(numSamples)
        val rng = Random(415)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            // 1. Mirror up + first curtain
            val p1 = (sin(2.0 * PI * 480.0 * t) * 0.45 +
                sin(2.0 * PI * 1850.0 * t) * 0.35 +
                rng.nextDouble(-1.0, 1.0) * 0.55) * exp(-t * 110.0)

            // 2. Second curtain
            val t2 = t - 0.032
            val p2 = if (t2 >= 0.0) {
                (sin(2.0 * PI * 1620.0 * t2) * 0.40 +
                    rng.nextDouble(-1.0, 1.0) * 0.45) * exp(-t2 * 130.0) * 0.85
            } else 0.0

            // 3. Mirror return
            val t3 = t - 0.068
            val p3 = if (t3 >= 0.0) {
                (sin(2.0 * PI * 390.0 * t3) * 0.50 +
                    sin(2.0 * PI * 1150.0 * t3) * 0.25 +
                    rng.nextDouble(-1.0, 1.0) * 0.30) * exp(-t3 * 95.0) * 0.75
            } else 0.0

            val combined = (p1 + p2 + p3).coerceIn(-1.0, 1.0)
            out[i] = (combined * Short.MAX_VALUE * 0.76).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return out
    }

    /**
     * Modern Digital Studio Electronic Shutter: subtle double harmonic confirmation + crisp micro-click.
     */
    private fun synthesizeDigitalPro(): ShortArray {
        val durationMs = 68
        val numSamples = (SAMPLE_RATE * durationMs) / 1000
        val out = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val tone1 = sin(2.0 * PI * 1960.0 * t) * exp(-t * 140.0) * 0.55
            val t2 = t - 0.022
            val tone2 = if (t2 >= 0.0) {
                sin(2.0 * PI * 2637.0 * t2) * exp(-t2 * 130.0) * 0.60
            } else 0.0
            val combined = (tone1 + tone2).coerceIn(-1.0, 1.0)
            out[i] = (combined * Short.MAX_VALUE * 0.52).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return out
    }

    private fun playPcmBuffer(samples: ShortArray) {
        val byteCount = samples.size * 2
        val minBuf = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(byteCount)

        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(minBuf)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        try {
            track.write(samples, 0, samples.size)
            track.play()
            val sleepMs = ((samples.size * 1000L) / SAMPLE_RATE) + 45L
            Thread.sleep(sleepMs)
        } finally {
            runCatching { track.stop() }
            runCatching { track.release() }
        }
    }
}
