package com.contoh.typingmaster

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/** Pengucapan teks dalam Bahasa Indonesia (TTS). */
class Suara(ctx: Context) {

    private var tts: TextToSpeech? = null
    private var siap = false
    private var tertunda: String? = null
    private var urutan = 0

    init {
        tts = TextToSpeech(ctx.applicationContext) { status -> aturSiap(status) }
    }

    private fun aturSiap(status: Int) {
        val t = tts ?: return
        if (status != TextToSpeech.SUCCESS) return
        val hasil = t.setLanguage(Locale("id", "ID"))
        if (hasil == TextToSpeech.LANG_MISSING_DATA || hasil == TextToSpeech.LANG_NOT_SUPPORTED) {
            t.setLanguage(Locale.getDefault())
        }
        t.setSpeechRate(0.9f)
        siap = true
        val p = tertunda
        tertunda = null
        if (p != null) bicara(p)
    }

    fun bicara(teks: String) {
        if (teks.isBlank()) return
        if (!siap) {
            tertunda = teks
            return
        }
        urutan++
        tts?.speak(teks, TextToSpeech.QUEUE_FLUSH, null, "tm$urutan")
    }

    fun berhenti() {
        tts?.stop()
    }

    fun tutup() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        siap = false
    }
}
