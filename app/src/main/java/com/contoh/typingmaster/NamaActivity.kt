
package com.contoh.typingmaster

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView

/** Layar memasukkan nama peserta belajar. Nama dipakai untuk sapaan dan pengucapan "salah tekan ...". */
class NamaActivity : BaseKeyboardActivity() {

    private lateinit var suara: Suara
    private val handler = Handler(Looper.getMainLooper())
    private val nama = StringBuilder()
    private lateinit var tampilan: TextView
    private lateinit var tombolMulai: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        suara = Suara(this)
        nama.append(Pengaturan.nama(this))
        setContentView(buatLayar())
        onOrientasi(isLandscape())
        tampil()
        suara.bicara("Siapa nama kamu? Ketik namamu, lalu tekan Enter.")
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        suara.tutup()
        super.onDestroy()
    }

    override fun onBackPressed() {
        // Belum ada nama: keluar dari aplikasi agar tidak kembali ke layar ini berulang-ulang
        if (Pengaturan.nama(this).isEmpty()) finishAffinity() else super.onBackPressed()
    }

    override fun tekanHuruf(c: Char) {
        if (c.isLetter() && nama.length < 20) {
            nama.append(c)
            tampil()
        }
    }

    override fun tekanSpasi() {
        if (nama.isNotEmpty() && nama.last() != ' ' && nama.length < 20) {
            nama.append(' ')
            tampil()
        }
    }

    override fun tekanHapus() {
        if (nama.isNotEmpty()) {
            nama.deleteCharAt(nama.length - 1)
            tampil()
        }
    }

    override fun tekanEnter() = simpan()

    private fun tampil() {
        val t = nama.toString()
        tampilan.text = if (t.isEmpty()) "Ketik namamu…" else t
        tampilan.setTextColor(if (t.isEmpty()) Tema.redup else Tema.kuning)
        tombolMulai.alpha = if (t.isBlank()) 0.45f else 1f
    }

    private fun simpan() {
        val n = nama.toString().trim()
        if (n.isEmpty()) {
            suara.bicara("Ketik nama kamu dulu")
            return
        }
        Pengaturan.simpanNama(this, n)
        suara.bicara("Halo, $n! Ayo belajar mengetik.")
        handler.postDelayed({ finish() }, 1200L)
    }

    private fun buatLayar(): View {
        val root = akarLatar()

        val tengah = linear(this)
        tengah.gravity = Gravity.CENTER
        tengah.addView(teks(this, "🙋", 56f, Tema.teks).apply { gravity = Gravity.CENTER })
        tengah.addView(
            teks(this, "Siapa nama kamu?", 24f, Tema.teks, true).apply {
                gravity = Gravity.CENTER
                setPadding(0, dp(6), 0, dp(6))
            }
        )
        tengah.addView(
            teks(this, "Nama ini dipakai untuk menyapa dan membimbingmu saat belajar.", 13f, Tema.redup).apply {
                gravity = Gravity.CENTER
                setPadding(dp(12), 0, dp(12), 0)
            }
        )

        val kartu = kartuTema(this)
        kartu.gravity = Gravity.CENTER
        tampilan = teks(this, "", 30f, Tema.kuning, true)
        tampilan.gravity = Gravity.CENTER
        kartu.addView(tampilan, lp(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        tengah.addView(kartu, lp(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0f, dp(16), dp(24), dp(24), 0))

        tombolMulai = teks(this, "▶  Mulai Belajar", 18f, Tema.teks, true)
        tombolMulai.gravity = Gravity.CENTER
        tombolMulai.setPadding(dp(18), dp(14), dp(18), dp(14))
        tombolMulai.background = riak(this, Tema.biru, 18)
        tombolMulai.setOnClickListener { simpan() }
        tengah.addView(tombolMulai, lp(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0f, dp(16), 0, 0, 0))

        root.addView(tengah, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))

        val kb = KeyboardLayar(this) { token -> tekanDariLayar(token) }
        pasangKeyboardLayar(kb)
        root.addView(kb, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        return root
    }
}
