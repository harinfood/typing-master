package com.contoh.typingmaster

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Handler
import android.os.Looper
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin

/** Sintesis nada piano sederhana: gelombang sinus dengan harmonik dan peluruhan, dibuat di memori. */
object NadaPiano {
    private const val RATE = 44100
    private val buffer = HashMap<Int, ShortArray>()
    private val aktif = ArrayList<AudioTrack>()
    private val handler = Handler(Looper.getMainLooper())

    /** s = jarak semitone dari C4 (0 = C4). */
    fun frekuensi(s: Int): Double = 261.63 * 2.0.pow(s / 12.0)

    private fun buat(s: Int): ShortArray {
        val f = frekuensi(s)
        val n = (RATE * 1.6).toInt()
        return ShortArray(n) { k ->
            val t = k / RATE.toDouble()
            val env = exp(-t * 2.6) * min(1.0, t / 0.004)
            val v = sin(2 * PI * f * t) + 0.45 * sin(4 * PI * f * t) + 0.2 * sin(6 * PI * f * t)
            (v / 1.65 * env * 0.75 * Short.MAX_VALUE).toInt().toShort()
        }
    }

    fun mainkan(s: Int) {
        val data = buffer.getOrPut(s) { buat(s) }
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
                    .setSampleRate(RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(data.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()
        track.write(data, 0, data.size)
        track.play()
        aktif.add(track)
        handler.postDelayed({ lepas(track) }, 1700L)
    }

    private fun lepas(t: AudioTrack) {
        try { t.stop() } catch (e: Exception) { }
        try { t.release() } catch (e: Exception) { }
        aktif.remove(t)
    }

    fun lepasSemua() {
        handler.removeCallbacksAndMessages(null)
        aktif.toList().forEach { lepas(it) }
    }
}
