package com.contoh.typingmaster

import android.content.Context

/** Penyimpanan pengaturan, hasil latihan, dan kemajuan belajar huruf. */
object Pengaturan {
    private const val NAMA = "typing_master"

    private fun p(c: Context) = c.getSharedPreferences(NAMA, Context.MODE_PRIVATE)

    fun modeTts(c: Context): String = p(c).getString("mode_tts", "kata") ?: "kata"

    fun simpanModeTts(c: Context, m: String) {
        p(c).edit().putString("mode_tts", m).apply()
    }

    fun bestWpm(c: Context, level: Int): Int = p(c).getInt("wpm_$level", 0)

    fun bestBintang(c: Context, level: Int): Int = p(c).getInt("bintang_$level", 0)

    fun simpanHasilLatihan(c: Context, level: Int, wpm: Int, bintang: Int) {
        val e = p(c).edit()
        if (wpm > bestWpm(c, level)) e.putInt("wpm_$level", wpm)
        if (bintang > bestBintang(c, level)) e.putInt("bintang_$level", bintang)
        e.apply()
    }

    fun totalHuruf(c: Context): Int = p(c).getInt("total_huruf", 0)

    fun tambahTotalHuruf(c: Context, n: Int) {
        p(c).edit().putInt("total_huruf", totalHuruf(c) + n).apply()
    }

    fun hurufDikenal(c: Context): String = p(c).getString("huruf_dikenal", "") ?: ""

    fun tambahHurufDikenal(c: Context, h: Char) {
        val s = hurufDikenal(c)
        if (!s.contains(h)) p(c).edit().putString("huruf_dikenal", s + h).apply()
    }

    fun totalBintangGame(c: Context): Int = p(c).getInt("bintang_game", 0)

    fun tambahBintangGame(c: Context, n: Int) {
        p(c).edit().putInt("bintang_game", totalBintangGame(c) + n).apply()
    }

    fun pianoSuara(c: Context): Boolean = p(c).getBoolean("piano_suara", true)

    fun simpanPianoSuara(c: Context, v: Boolean) {
        p(c).edit().putBoolean("piano_suara", v).apply()
    }

    fun pianoUcap(c: Context): Boolean = p(c).getBoolean("piano_ucap", false)

    fun simpanPianoUcap(c: Context, v: Boolean) {
        p(c).edit().putBoolean("piano_ucap", v).apply()
    }

    /** Nomor kelompok 10 huruf berikutnya untuk mode urut A-Z. */
    fun batchHuruf(c: Context): Int = p(c).getInt("batch_huruf", 0)

    fun simpanBatchHuruf(c: Context, n: Int) {
        p(c).edit().putInt("batch_huruf", n).apply()
    }
}
