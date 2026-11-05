package com.contoh.typingmaster

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView

/** Piano huruf: setiap tuts berbunyi nada dan menampilkan hurufnya. */
class PianoActivity : BaseKeyboardActivity() {

    private lateinit var suara: Suara
    private lateinit var piano: PianoView
    private lateinit var txtHuruf: TextView
    private lateinit var txtNada: TextView
    private lateinit var txtRiwayat: TextView
    private lateinit var chipSuara: TextView
    private lateinit var chipUcap: TextView
    private val handler = Handler(Looper.getMainLooper())
    private val riwayat = ArrayDeque<Char>()
    private var suaraAktif = true
    private var ucapAktif = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        suara = Suara(this)
        suaraAktif = Pengaturan.pianoSuara(this)
        ucapAktif = Pengaturan.pianoUcap(this)
        setContentView(buatLayar())
        segarkanChip()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        NadaPiano.lepasSemua()
        suara.tutup()
        super.onDestroy()
    }

    /** Keyboard fisik: huruf A sampai Z memainkan tuts yang sesuai. */
    override fun tekanHuruf(c: Char) {
        val l = c.lowercaseChar()
        if (l !in 'a'..'z') return
        val s = l - 'a'
        piano.kilatKunci(s)
        mainkan(s)
    }

    private fun mainkan(s: Int) {
        if (suaraAktif) NadaPiano.mainkan(s)
        val h = 'A' + s
        txtHuruf.text = h.toString()
        txtNada.text = "Nada ${PianoView.namaNada(s)}"
        riwayat.addLast(h)
        while (riwayat.size > 24) riwayat.removeFirst()
        txtRiwayat.text = riwayat.joinToString("  ")
        if (ucapAktif) suara.bicara(h.toString())
        pulsa()
    }

    private fun pulsa() {
        txtHuruf.animate().scaleX(1.2f).scaleY(1.2f).setDuration(90L).withEndAction {
            txtHuruf.animate().scaleX(1f).scaleY(1f).setDuration(180L).start()
        }.start()
    }

    private fun segarkanChip() {
        chipSuara.text = if (suaraAktif) "🎵 Suara piano: Aktif" else "🎵 Suara piano: Mati"
        chipSuara.background = if (suaraAktif) kotakGradien(this, Tema.biru, Tema.ungu, 14) else kotak(this, Tema.tombol, 14)
        chipUcap.text = if (ucapAktif) "🗣 Ucapkan huruf: Aktif" else "🗣 Ucapkan huruf: Mati"
        chipUcap.background = if (ucapAktif) kotakGradien(this, Tema.hijau, Tema.kuning, 14) else kotak(this, Tema.tombol, 14)
    }

    private fun buatLayar(): View {
        val root = akarLatar()
        root.addView(barAtas("Piano Huruf") { finish() })

        // Kartu huruf dan nada yang sedang dimainkan
        val kartu = kartuTema(this)
        kartu.gravity = Gravity.CENTER_HORIZONTAL
        txtHuruf = teks(this, "–", 120f, Tema.kuning, true)
        txtHuruf.gravity = Gravity.CENTER
        txtNada = teks(this, "Tekan tuts atau huruf di keyboard", 15f, Tema.redup)
        txtNada.gravity = Gravity.CENTER
        txtRiwayat = teks(this, "", 16f, Tema.teks, true)
        txtRiwayat.gravity = Gravity.CENTER
        kartu.addView(txtHuruf, lp(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        kartu.addView(txtNada, lp(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        kartu.addView(txtRiwayat, lp(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0f, dp(6), 0, 0, 0))
        root.addView(kartu, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f).apply { setMargins(0, dp(6), 0, dp(8)) })

        // Pengaturan suara
        val baris = linear(this, false)
        chipSuara = teks(this, "", 13f, Tema.teks, true)
        chipSuara.gravity = Gravity.CENTER
        chipSuara.setPadding(dp(8), dp(9), dp(8), dp(9))
        chipSuara.setOnClickListener {
            suaraAktif = !suaraAktif
            Pengaturan.simpanPianoSuara(this, suaraAktif)
            segarkanChip()
        }
        chipUcap = teks(this, "", 13f, Tema.teks, true)
        chipUcap.gravity = Gravity.CENTER
        chipUcap.setPadding(dp(8), dp(9), dp(8), dp(9))
        chipUcap.setOnClickListener {
            ucapAktif = !ucapAktif
            Pengaturan.simpanPianoUcap(this, ucapAktif)
            segarkanChip()
        }
        baris.addView(chipSuara, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { setMargins(0, 0, dp(4), 0) })
        baris.addView(chipUcap, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { setMargins(dp(4), 0, 0, 0) })
        root.addView(baris, lp(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0f, 0, 0, 0, dp(8)))

        // Piano: tinggi mengikuti sisa layar
        piano = PianoView(this) { s -> mainkan(s) }
        root.addView(piano, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.3f))

        root.addView(
            teks(this, "A sampai Z dari kiri ke kanan. Setiap tuts berbunyi nada dan menampilkan hurufnya.", 12f, Tema.redup).apply {
                gravity = Gravity.CENTER
                setPadding(dp(8), dp(8), dp(8), 0)
            }
        )
        return root
    }
}
