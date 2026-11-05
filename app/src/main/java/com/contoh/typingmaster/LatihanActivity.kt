package com.contoh.typingmaster

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import kotlin.math.max
import kotlin.math.roundToInt

/** Latihan mengetik: teks harus diketik berurutan, huruf yang salah tidak maju. */
class LatihanActivity : BaseKeyboardActivity() {

    private lateinit var suara: Suara
    private lateinit var level: Level
    private var levelIdx = 0
    private var target = ""
    private var idx = 0
    private var benar = 0
    private var salah = 0
    private var mulaiMs = 0L
    private var selesai = false

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var kartuTeks: LinearLayout
    private lateinit var tampilanTeks: TextView
    private lateinit var txtStat: TextView
    private lateinit var overlay: FrameLayout
    private lateinit var txtHasil: TextView

    private val penyegar = object : Runnable {
        override fun run() {
            segarkanStat()
            handler.postDelayed(this, 500L)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        levelIdx = intent.getIntExtra("level", 0).coerceIn(0, Data.LEVEL.size - 1)
        level = Data.LEVEL[levelIdx]
        suara = Suara(this)
        target = Data.teksLatihan(level)
        setContentView(buatLayar())
        onOrientasi(isLandscape())
        render()
    }

    override fun onDestroy() {
        handler.removeCallbacks(penyegar)
        suara.tutup()
        super.onDestroy()
    }

    override fun tekanHuruf(c: Char) = prosesKarakter(c)

    override fun tekanSpasi() = prosesKarakter(' ')

    private fun prosesKarakter(c: Char) {
        if (selesai) return
        if (mulaiMs == 0L) {
            mulaiMs = System.currentTimeMillis()
            handler.post(penyegar)
        }
        val harap = target[idx]
        if (c.equals(harap, ignoreCase = true)) {
            benar++
            idx++
            render()
            if (idx >= target.length) selesaiLatihan() else layarKeyboard?.tandai(target[idx])
        } else {
            salah++
            kedipSalah()
            segarkanStat()
            val n = Pengaturan.nama(this)
            if (n.isNotEmpty()) suara.bicara("Salah tekan $n")
        }
    }

    private fun kedipSalah() {
        kartuTeks.background = kotak(this, Color.parseColor("#55FF3D71"), 18, Tema.garis)
        handler.postDelayed({ kartuTeks.background = kotak(this, Tema.kartu, 18, Tema.garis) }, 180L)
    }

    private fun render() {
        val sb = SpannableStringBuilder(target)
        for (i in target.indices) {
            val warna = when {
                i < idx -> Tema.hijau
                i == idx -> Tema.kuning
                else -> Tema.redup
            }
            sb.setSpan(ForegroundColorSpan(warna), i, i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        if (idx < target.length) {
            sb.setSpan(BackgroundColorSpan(Color.parseColor("#44FFEA00")), idx, idx + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        tampilanTeks.text = sb
        segarkanStat()
    }

    private fun hitungWpm(): Double {
        val menit = if (mulaiMs == 0L) 0.0 else (System.currentTimeMillis() - mulaiMs) / 60000.0
        return (benar / 5.0) / max(menit, 0.02)
    }

    private fun akurasi(): Int {
        val total = benar + salah
        return if (total == 0) 100 else (benar * 100.0 / total).roundToInt()
    }

    private fun segarkanStat() {
        val detik = if (mulaiMs == 0L) 0L else (System.currentTimeMillis() - mulaiMs) / 1000L
        txtStat.text = "Kecepatan: ${hitungWpm().roundToInt()} kpm   ·   Akurasi: ${akurasi()}%   ·   ${"%02d:%02d".format(detik / 60, detik % 60)}"
    }

    private fun selesaiLatihan() {
        selesai = true
        handler.removeCallbacks(penyegar)
        val wpm = hitungWpm().roundToInt()
        val acc = akurasi()
        val bintang = when {
            acc >= 95 && wpm >= 20 -> 3
            acc >= 90 -> 2
            else -> 1
        }
        Pengaturan.simpanHasilLatihan(this, levelIdx, wpm, bintang)
        txtHasil.text = "🎉 Selesai!\n\n${bintangTeks(bintang)}\n\nKecepatan: $wpm kpm\nAkurasi: $acc%\nTerbaik: ${Pengaturan.bestWpm(this, levelIdx)} kpm"
        overlay.visibility = View.VISIBLE
        overlay.alpha = 0f
        overlay.animate().alpha(1f).setDuration(350L).start()
        suara.bicara("Selesai! Kamu mendapat $bintang bintang. Kecepatanmu $wpm kata per menit.")
    }

    private fun buatLayar(): View {
        val frame = FrameLayout(this)
        val root = akarLatar()
        frame.addView(root, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))

        root.addView(barAtas(level.nama) { finish() })
        root.addView(teks(this, level.deskripsi, 13f, Tema.redup).apply { setPadding(dp(6), 0, dp(6), dp(8)) })

        txtStat = teks(this, "", 13f, Tema.kuning, true)
        txtStat.gravity = Gravity.CENTER
        root.addView(txtStat, lp(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0f, 0, 0, 0, dp(8)))

        kartuTeks = kartuTema(this)
        tampilanTeks = teks(this, "", 24f, Tema.teks)
        tampilanTeks.setLineSpacing(dp(8).toFloat(), 1f)
        val scroll = ScrollView(this)
        scroll.isFillViewport = true
        scroll.isFocusable = false
        scroll.isFocusableInTouchMode = false
        scroll.addView(tampilanTeks)
        kartuTeks.addView(scroll, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))
        root.addView(kartuTeks, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))

        val kb = KeyboardLayar(this) { token -> tekanDariLayar(token) }
        pasangKeyboardLayar(kb)
        root.addView(kb, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(6) })

        // Layar hasil (muncul setelah teks selesai)
        overlay = FrameLayout(this)
        overlay.setBackgroundColor(Color.parseColor("#E00F0C1D"))
        overlay.visibility = View.GONE
        overlay.isClickable = true
        val kartuHasil = kartuTema(this)
        kartuHasil.gravity = Gravity.CENTER
        txtHasil = teks(this, "", 20f, Tema.teks, true)
        txtHasil.gravity = Gravity.CENTER
        kartuHasil.addView(txtHasil)
        val tombol = linear(this, false)
        tombol.gravity = Gravity.CENTER
        tombol.addView(
            hasilTombol("🔁 Ulangi") { recreate() },
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { setMargins(0, dp(14), dp(5), 0) }
        )
        tombol.addView(
            hasilTombol("📋 Daftar Latihan") { finish() },
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { setMargins(dp(5), dp(14), 0, 0) }
        )
        kartuHasil.addView(tombol)
        overlay.addView(
            kartuHasil,
            FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER).apply {
                setMargins(dp(24), 0, dp(24), 0)
            }
        )
        frame.addView(overlay, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
        return frame
    }

    private fun hasilTombol(label: String, aksi: () -> Unit): TextView {
        val t = teks(this, label, 14f, Tema.teks, true)
        t.gravity = Gravity.CENTER
        t.setPadding(dp(8), dp(12), dp(8), dp(12))
        t.background = riak(this, Tema.biru, 14)
        t.setOnClickListener { aksi() }
        return t
    }
}
