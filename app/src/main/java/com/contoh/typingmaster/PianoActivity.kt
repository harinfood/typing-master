package com.contoh.typingmaster

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView

/**
 * Piano huruf: setiap tuts berbunyi nada, menampilkan huruf dan solmisasi (Do Re Mi ...).
 * Mode latihan memandu urutan Do Re Mi Fa Sol La Si Do sambil mengetik hurufnya.
 */
class PianoActivity : BaseKeyboardActivity() {

    private val urutanLatihan = intArrayOf(0, 2, 4, 5, 7, 9, 11, 12) // Do Re Mi Fa Sol La Si Do

    private lateinit var suara: Suara
    private lateinit var piano: PianoView
    private lateinit var hsv: HorizontalScrollView
    private lateinit var txtHuruf: TextView
    private lateinit var txtNada: TextView
    private lateinit var txtRiwayat: TextView
    private lateinit var txtTarget: TextView
    private lateinit var chipSuara: TextView
    private lateinit var chipUcap: TextView
    private lateinit var chipLatihan: TextView
    private val handler = Handler(Looper.getMainLooper())
    private val riwayat = ArrayDeque<Char>()
    private var suaraAktif = true
    private var ucapAktif = false
    private var latihanAktif = false
    private var latIdx = 0

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
        txtHuruf.setTextColor(if (PianoView.adalahHitam(s)) Color.WHITE else PianoView.warnaSolmisasi(s))
        txtNada.text = "Nada ${PianoView.namaNada(s)}  ·  ${PianoView.solmisasi(s)}"
        riwayat.addLast(h)
        while (riwayat.size > 24) riwayat.removeFirst()
        txtRiwayat.text = riwayat.joinToString("  ")
        if (ucapAktif) {
            suara.bicara(if (PianoView.adalahHitam(s)) h.toString() else "$h, ${PianoView.solmisasi(s)}")
        }
        pulsa()
        if (latihanAktif) cekLatihan(s)
    }

    private fun labelTarget(): String {
        val s = urutanLatihan[latIdx]
        return "${PianoView.solmisasi(s)} (huruf ${'A' + s})"
    }

    private fun cekLatihan(s: Int) {
        if (s == urutanLatihan[latIdx]) {
            latIdx++
            if (latIdx >= urutanLatihan.size) {
                latIdx = 0
                latihanAktif = false
                piano.targetKey = -1
                txtTarget.text = "🎉 Hebat! Kamu sudah bermain Do Re Mi Fa Sol La Si Do"
                txtTarget.setTextColor(Tema.hijau)
                suara.bicara("Hebat! Kamu sudah bermain Do Re Mi Fa Sol La Si Do")
                segarkanChip()
            } else {
                piano.targetKey = urutanLatihan[latIdx]
                txtTarget.text = "✅ Benar! Sekarang tekan: ${labelTarget()}"
                txtTarget.setTextColor(Tema.hijau)
                suara.bicara(PianoView.solmisasi(urutanLatihan[latIdx]))
            }
        } else {
            txtTarget.text = "❌ Belum tepat. Cari: ${labelTarget()}"
            txtTarget.setTextColor(Tema.merah)
        }
    }

    private fun pulsa() {
        txtHuruf.animate().scaleX(1.2f).scaleY(1.2f).setDuration(90L).withEndAction {
            txtHuruf.animate().scaleX(1f).scaleY(1f).setDuration(180L).start()
        }.start()
    }

    private fun segarkanChip() {
        chipSuara.text = if (suaraAktif) "🎵 Suara: Aktif" else "🎵 Suara: Mati"
        chipSuara.background = if (suaraAktif) kotakGradien(this, Tema.biru, Tema.ungu, 14) else kotak(this, Tema.tombol, 14)
        chipUcap.text = if (ucapAktif) "🗣 Ucapkan: Aktif" else "🗣 Ucapkan: Mati"
        chipUcap.background = if (ucapAktif) kotakGradien(this, Tema.hijau, Tema.kuning, 14) else kotak(this, Tema.tombol, 14)
        chipLatihan.text = if (latihanAktif) "🎼 Latihan: Aktif" else "🎼 Latihan Do Re Mi"
        chipLatihan.background = if (latihanAktif) kotakGradien(this, Tema.oranye, Tema.merah, 14) else kotak(this, Tema.tombol, 14)
    }

    private fun buatLayar(): View {
        val root = akarLatar()
        root.addView(barAtas("Piano Huruf") { finish() })

        // Kartu huruf dan nada yang sedang dimainkan
        val kartu = kartuTema(this)
        kartu.gravity = Gravity.CENTER_HORIZONTAL
        txtHuruf = teks(this, "–", 110f, Tema.kuning, true)
        txtHuruf.gravity = Gravity.CENTER
        txtNada = teks(this, "Tekan tuts atau huruf di keyboard", 15f, Tema.redup)
        txtNada.gravity = Gravity.CENTER
        txtRiwayat = teks(this, "", 16f, Tema.teks, true)
        txtRiwayat.gravity = Gravity.CENTER
        kartu.addView(txtHuruf, lp(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        kartu.addView(txtNada, lp(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        kartu.addView(txtRiwayat, lp(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0f, dp(6), 0, 0, 0))
        root.addView(kartu, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f).apply { setMargins(0, dp(6), 0, dp(6)) })

        // Petunjuk latihan (tampil saat mode latihan aktif)
        txtTarget = teks(this, "Aktifkan \"Latihan Do Re Mi\" untuk belajar urutan nada", 14f, Tema.teks, true)
        txtTarget.gravity = Gravity.CENTER
        root.addView(txtTarget, lp(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0f, 0, 0, 0, dp(6)))

        // Tombol pengaturan
        val baris = linear(this, false)
        chipSuara = chipTombol()
        chipSuara.setOnClickListener {
            suaraAktif = !suaraAktif
            Pengaturan.simpanPianoSuara(this, suaraAktif)
            segarkanChip()
        }
        chipUcap = chipTombol()
        chipUcap.setOnClickListener {
            ucapAktif = !ucapAktif
            Pengaturan.simpanPianoUcap(this, ucapAktif)
            segarkanChip()
        }
        chipLatihan = chipTombol()
        chipLatihan.setOnClickListener {
            latihanAktif = !latihanAktif
            latIdx = 0
            piano.targetKey = if (latihanAktif) urutanLatihan[0] else -1
            txtTarget.text = if (latihanAktif) "Mulai! Tekan: ${labelTarget()}" else "Aktifkan \"Latihan Do Re Mi\" untuk belajar urutan nada"
            txtTarget.setTextColor(Tema.teks)
            segarkanChip()
        }
        baris.addView(chipSuara, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { setMargins(0, 0, dp(4), 0) })
        baris.addView(chipUcap, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { setMargins(dp(4), 0, dp(4), 0) })
        baris.addView(chipLatihan, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.3f).apply { setMargins(dp(4), 0, 0, 0) })
        root.addView(baris, lp(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0f, 0, 0, 0, dp(8)))

        // Navigasi geser piano
        val navigasi = linear(this, false)
        navigasi.gravity = Gravity.CENTER_VERTICAL
        val kiri = teks(this, "◀ Geser", 13f, Tema.teks, true)
        kiri.setPadding(dp(10), dp(6), dp(10), dp(6))
        kiri.background = kotak(this, Tema.tombol, 12)
        kiri.setOnClickListener { hsv.smoothScrollBy(-dp(270), 0) }
        val info = teks(this, "C4 · Do   →   C#6", 12f, Tema.redup)
        info.gravity = Gravity.CENTER
        val kanan = teks(this, "Geser ▶", 13f, Tema.teks, true)
        kanan.setPadding(dp(10), dp(6), dp(10), dp(6))
        kanan.background = kotak(this, Tema.tombol, 12)
        kanan.setOnClickListener { hsv.smoothScrollBy(dp(270), 0) }
        navigasi.addView(kiri)
        navigasi.addView(info, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        navigasi.addView(kanan)
        root.addView(navigasi, lp(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0f, 0, 0, 0, dp(4)))

        // Piano dalam gulir horizontal
        hsv = HorizontalScrollView(this)
        hsv.isHorizontalScrollBarEnabled = false
        piano = PianoView(this) { s -> mainkan(s) }
        hsv.addView(piano, FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.MATCH_PARENT))
        root.addView(hsv, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.4f))

        root.addView(
            teks(this, "Setiap tuts putih menampilkan solmisasi (Do Re Mi ...) dan huruf keyboard-nya.", 12f, Tema.redup).apply {
                gravity = Gravity.CENTER
                setPadding(dp(8), dp(8), dp(8), 0)
            }
        )
        return root
    }

    private fun chipTombol(): TextView {
        val t = teks(this, "", 12f, Tema.teks, true)
        t.gravity = Gravity.CENTER
        t.setPadding(dp(6), dp(9), dp(6), dp(9))
        return t
    }
}
