package com.ludovish.app.core

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

class SoundEngine(private val context: Context) {

    private val engineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var soundPool: SoundPool? = null

    @Volatile private var isLoaded = false
    @Volatile var isEnabled: Boolean = true
    @Volatile var masterVolume: Float = 0.85f

    private var sndGlassTick = 0
    private var sndDiceRattle = 0
    private var sndTokenHop = 0
    private var sndCaptureImpact = 0
    private var sndBonusChime = 0
    private var sndVictoryFanfare = 0

    init {
        initializeSoundPool()
    }

    private fun initializeSoundPool() {
        engineScope.launch {
            runCatching {
                val attrs = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()

                val pool = SoundPool.Builder()
                    .setMaxStreams(10)
                    .setAudioAttributes(attrs)
                    .build()

                // Synthesize crystal-clear Apple-style acoustic waveforms in background
                sndGlassTick = pool.load(createWavFile("glass_tick.wav", synthGlassTick()), 1)
                sndDiceRattle = pool.load(createWavFile("dice_rattle.wav", synthDiceTumble()), 1)
                sndTokenHop = pool.load(createWavFile("token_hop.wav", synthTokenHop()), 1)
                sndCaptureImpact = pool.load(createWavFile("capture_impact.wav", synthCaptureImpact()), 1)
                sndBonusChime = pool.load(createWavFile("bonus_chime.wav", synthBonusChime()), 1)
                sndVictoryFanfare = pool.load(createWavFile("victory_fanfare.wav", synthVictoryChord()), 1)

                soundPool = pool
                isLoaded = true
            }
        }
    }

    /** Subtle glass refraction click for dock & sliders */
    fun playGlassTick(pan: Float = 0f) {
        play(sndGlassTick, volumeScale = 0.55f, rate = 1.05f, pan = pan)
    }

    /** Multi-impulse wooden/crystal dice tumble */
    fun playDiceRoll() {
        val randomPitch = 0.94f + Random.nextFloat() * 0.14f
        play(sndDiceRattle, volumeScale = 0.90f, rate = randomPitch)
    }

    /**
     * Pitch-escalating token hop: pitch rises slightly with each step in a move
     * @param stepOffset 0..5 within the current move
     */
    fun playTokenHop(stepOffset: Int = 0, pan: Float = 0f) {
        val pitchRate = (0.96f + (stepOffset.coerceIn(0, 8) * 0.045f)).coerceIn(0.8f, 1.6f)
        play(sndTokenHop, volumeScale = 0.85f, rate = pitchRate, pan = pan)
    }

    /** Sub-bass drop + crystalline shatter when capturing an opponent token */
    fun playCapture() {
        play(sndCaptureImpact, volumeScale = 1.0f, rate = 1.0f)
    }

    /** Shimmering harmonic fifth when rolling a 6 or unlocking a token */
    fun playBonusSix() {
        play(sndBonusChime, volumeScale = 0.88f, rate = 1.0f)
    }

    /** Major 9th arpeggio fanfare when reaching the center crown */
    fun playVictory() {
        play(sndVictoryFanfare, volumeScale = 1.0f, rate = 1.0f)
    }

    private fun play(
        soundId: Int,
        volumeScale: Float = 1.0f,
        rate: Float = 1.0f,
        pan: Float = 0f
    ) {
        if (!isEnabled || !isLoaded || soundId == 0) return
        val pool = soundPool ?: return
        val clampedPan = pan.coerceIn(-1f, 1f)
        val baseVol = (masterVolume * volumeScale).coerceIn(0f, 1f)
        val leftVol = (baseVol * if (clampedPan > 0f) 1f - clampedPan * 0.5f else 1f).coerceIn(0f, 1f)
        val rightVol = (baseVol * if (clampedPan < 0f) 1f + clampedPan * 0.5f else 1f).coerceIn(0f, 1f)

        runCatching {
            pool.play(soundId, leftVol, rightVol, 1, 0, rate.coerceIn(0.5f, 2.0f))
        }
    }

    fun release() {
        engineScope.cancel()
        runCatching {
            soundPool?.release()
            soundPool = null
        }
    }

    // =========================================================================
    // PROCEDURAL 44.1kHz 16-BIT PCM WAVEFORM SYNTHESIZERS
    // =========================================================================

    private val sampleRate = 44100

    private fun synthGlassTick(): ShortArray {
        val samples = (sampleRate * 0.045).toInt()
        return ShortArray(samples) { i ->
            val t = i.toDouble() / sampleRate
            val env = exp(-t * 95.0)
            val wave = 0.65 * sin(2.0 * PI * 1480.0 * t) + 0.35 * sin(2.0 * PI * 2960.0 * t)
            (wave * env * Short.MAX_VALUE * 0.65).toInt().toShort()
        }
    }

    private fun synthTokenHop(): ShortArray {
        val samples = (sampleRate * 0.095).toInt()
        return ShortArray(samples) { i ->
            val t = i.toDouble() / sampleRate
            // Rising frequency chirp (520Hz -> 880Hz) with warm glass bell decay
            val freq = 520.0 + (360.0 * (i.toDouble() / samples))
            val env = exp(-t * 38.0)
            val wave = 0.75 * sin(2.0 * PI * freq * t) + 0.25 * sin(2.0 * PI * freq * 2.0 * t)
            (wave * env * Short.MAX_VALUE * 0.8).toInt().toShort()
        }
    }

    private fun synthDiceTumble(): ShortArray {
        val samples = (sampleRate * 0.28).toInt()
        val hitTimes = doubleArrayOf(0.0, 0.065, 0.135, 0.205)
        val hitFreqs = doubleArrayOf(620.0, 740.0, 680.0, 890.0)
        return ShortArray(samples) { i ->
            val t = i.toDouble() / sampleRate
            var acc = 0.0
            for (h in hitTimes.indices) {
                val dt = t - hitTimes[h]
                if (dt >= 0.0) {
                    val env = exp(-dt * 58.0)
                    acc += env * (
                        0.6 * sin(2.0 * PI * hitFreqs[h] * dt) +
                        0.4 * sin(2.0 * PI * (hitFreqs[h] * 1.618) * dt)
                    )
                }
            }
            (acc.coerceIn(-1.0, 1.0) * Short.MAX_VALUE * 0.78).toInt().toShort()
        }
    }

    private fun synthCaptureImpact(): ShortArray {
        val samples = (sampleRate * 0.36).toInt()
        return ShortArray(samples) { i ->
            val t = i.toDouble() / sampleRate
            // Deep sub sweep (240Hz -> 55Hz) + high crystal overtone burst
            val sweepFreq = 55.0 + 185.0 * exp(-t * 14.0)
            val envSub = exp(-t * 9.5)
            val envHigh = exp(-t * 24.0)
            val sub = 0.72 * sin(2.0 * PI * sweepFreq * t) * envSub
            val shimmer = 0.28 * (sin(2.0 * PI * 1174.6 * t) + sin(2.0 * PI * 1567.9 * t)) * envHigh
            ((sub + shimmer).coerceIn(-1.0, 1.0) * Short.MAX_VALUE * 0.9).toInt().toShort()
        }
    }

    private fun synthBonusChime(): ShortArray {
        val samples = (sampleRate * 0.32).toInt()
        return ShortArray(samples) { i ->
            val t = i.toDouble() / sampleRate
            val env1 = exp(-t * 12.0)
            val dt2 = (t - 0.07).coerceAtLeast(0.0)
            val env2 = if (t >= 0.07) exp(-dt2 * 10.0) else 0.0
            // C6 (1046.5Hz) -> G6 (1567.98Hz)
            val note1 = 0.5 * sin(2.0 * PI * 1046.5 * t) * env1
            val note2 = 0.5 * sin(2.0 * PI * 1567.98 * dt2) * env2
            ((note1 + note2).coerceIn(-1.0, 1.0) * Short.MAX_VALUE * 0.8).toInt().toShort()
        }
    }

    private fun synthVictoryChord(): ShortArray {
        val samples = (sampleRate * 0.72).toInt()
        // C5, E5, G5, B5, D6 arpeggiated Liquid Glass Major 9th chord
        val notes = doubleArrayOf(523.25, 659.25, 783.99, 987.77, 1174.66)
        val onsets = doubleArrayOf(0.0, 0.07, 0.14, 0.21, 0.28)
        return ShortArray(samples) { i ->
            val t = i.toDouble() / sampleRate
            var sum = 0.0
            for (n in notes.indices) {
                val dt = t - onsets[n]
                if (dt >= 0.0) {
                    val env = exp(-dt * 4.8)
                    sum += 0.24 * (sin(2.0 * PI * notes[n] * dt) + 0.3 * sin(2.0 * PI * notes[n] * 2.0 * dt)) * env
                }
            }
            (sum.coerceIn(-1.0, 1.0) * Short.MAX_VALUE * 0.88).toInt().toShort()
        }
    }

    private fun createWavFile(fileName: String, pcmData: ShortArray): String {
        val file = File(context.cacheDir, fileName)
        val byteDataSize = pcmData.size * 2
        val totalFileSize = 36 + byteDataSize

        val buffer = ByteBuffer.allocate(44 + byteDataSize).order(ByteOrder.LITTLE_ENDIAN)
        // RIFF header
        buffer.put("RIFF".toByteArray(Charsets.US_ASCII))
        buffer.putInt(totalFileSize)
        buffer.put("WAVE".toByteArray(Charsets.US_ASCII))
        // fmt chunk
        buffer.put("fmt ".toByteArray(Charsets.US_ASCII))
        buffer.putInt(16) // PCM chunk size
        buffer.putShort(1) // AudioFormat = 1 (PCM)
        buffer.putShort(1) // Channels = 1 (Mono source, panned dynamically by SoundPool)
        buffer.putInt(sampleRate)
        buffer.putInt(sampleRate * 2) // ByteRate
        buffer.putShort(2) // BlockAlign
        buffer.putShort(16) // BitsPerSample
        // data chunk
        buffer.put("data".toByteArray(Charsets.US_ASCII))
        buffer.putInt(byteDataSize)
        for (sample in pcmData) {
            buffer.putShort(sample)
        }

        FileOutputStream(file).use { fos ->
            fos.write(buffer.array())
        }
        return file.absolutePath
    }
}
