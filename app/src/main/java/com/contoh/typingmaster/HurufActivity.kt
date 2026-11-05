package com.contoh.typingmaster

import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.random.Random

/**
 * Game belajar huruf untuk anak: tekan huruf yang disebutkan.
 * Setiap sesi 10 huruf. Suara menyebut huruf dan contoh katanya.
 */
class HurufActivity : BaseKeyboardActivity() {

    private lateinit var suara: Suara
    private val handler = Handler(Looper.getMainLooper())
    private var sesi: List<Huruf> = emptyList()
    private var ronde = 0
    private var bintang = 0
    private var sibuk = false
    private var selesai = false
    private var acak = true

    private lateinit var kartuHuruf: LinearLayout
    private lateinit var besar: TextView
    private lateinit var emojiTxt: TextView
    private lateinit var kataTxt: TextView
    private lateinit var infoTxt: TextView
    private lateinit var lapisEfek: FrameLayout
    private lateinit var overlay: FrameLayout
    private lateinit var txtHasil: TextView
    private lateinit var chipAcak: TextView
    private lateinit var chipUrut: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        suara = Suara(this)
        setContentView(buatLayar())
        mulaiSesi()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        suara.tutup()
        super.onDestroy()
    }

    private fun mulaiSesi() {
        sesi = if (acak) {
            Data.HURUF.shuffled().take(10)
        } else {
            val mulai = (Pengaturan.batchHuruf(this) * 10) % 26
            (0 until 10).map { Data.HURUF[(mulai + it) % 26] }
        }
        ronde = 0
        bintang = 0
        selesai = false
        overlay.visibility = View.GONE
        aturChip()
        tampilRonde()
    }

    private fun tampilRonde() {
        val h = sesi[ronde]
        besar.text = h.huruf.toString()
        emojiTxt.text = h.emoji
        kataTxt.text = "${h.huruf} untuk ${h.kata}"
        infoTxt.text = "Ronde ${ronde + 1} dari ${sesi.size}   ·   ⭐ $bintang"
        layarKeyboard?.tandai(h.huruf)
        sibuk = false
        ucapkanSoal()
    }

    private fun ucapkanSoal() {
        val h = sesi[ronde]
        suara.bicara("Tekan huruf ${h.huruf}. ${h.huruf} untuk ${h.kata}.")
    }

    override fun tekanHuruf(c: Char) {
        if (sibuk || selesai) return
        val h = sesi[ronde]
        if (c.equals(h.huruf, ignoreCase = true)) benar(h) else salah(h)
    }

    private fun benar(h: Huruf) {
        sibuk = true
        bintang++
        Pengaturan.tambahHurufDikenal(this, h.huruf)
        Pengaturan.tambahBintangGame(this, 1)
        suara.bicara("Benar! ${h.huruf} untuk ${h.kata}.")
        infoTxt.text = "Ronde ${ronde + 1} dari ${sesi.size}   ·   ⭐ $bintang"
        denyutKartu()
        efekBintang()
        handler.postDelayed({ lanjut() }, 1800L)
    }

    private fun salah(h: Huruf) {
        goyang()
        suara.bicara("Coba lagi. Cari huruf ${h.huruf}.")
    }

    private fun lanjut() {
        ronde++
        if (ronde >= sesi.size) selesaiSesi() else tampilRonde()
    }

    private fun selesaiSesi() {
        selesai = true
        if (!acak) {
            Pengaturan.simpanBatchHuruf(this, Pengaturan.batchHuruf(this) + 1)
        }
        txtHasil.text = "🎉 Hebat!\n\nKamu mendapat\n${"⭐".repeat(bintang)}\n$bintang bintang"
        overlay.visibility = View.VISIBLE
        overlay.alpha = 0f
        overlay.animate().alpha(1f).setDuration(350L).start()
        suara.bicara("Hebat! Kamu mendapat $bintang bintang!")
    }

    private fun denyutKartu() {
        kartuHuruf.animate().scaleX(1.12f).scaleY(1.12f).setDuration(180L).withEndAction {
            kartuHuruf.animate().scaleX(1f).scaleY(1f).setDuration(220L).start()
        }.start()
    }

    private fun goyang() {
        ObjectAnimator.ofFloat(kartuHuruf, "translationX", 0f, -18f, 18f, -12f, 12f, -6f, 0f).apply {
            setDuration(480L)
            start()
        }
    }

    private fun efekBintang() {
        val w = lapisEfek.width.toFloat().coerceAtLeast(1f)
        val h = lapisEfek.height.toFloat().coerceAtLeast(1f)
        val ikon = listOf("⭐", "🎉", "✨")
        repeat(8) { i ->
            val v = TextView(this)
            v.text = ikon[i % ikon.size]
            v.textSize = 28f
            v.x = w * (0.2f + Random.nextFloat() * 0.6f)
            v.y = h * (0.3f + Random.nextFloat() * 0.2f)
            lapisEfek.addView(v, FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT))
            v.animate()
                .translationYBy(-dpF(160f) - Random.nextFloat() * dpF(120f))
                .alpha(0f)
                .setDuration(1100L)
                .withEndAction { lapisEfek.removeView(v) }
                .start()
        }
    }

    private fun aturChip() {
        chipAcak.background = if (acak) kotakGradien(this, Tema.hijau, Tema.kuning, 14) else kotak(this, Tema.tombol, 14)
        chipAcak.setTextColor(if (acak) Color.BLACK else Tema.redup)
        chipUrut.background = if (!acak) kotakGradien(this, Tema.hijau, Tema.kuning, 14) else kotak(this, Tema.tombol, 14)
        chipUrut.setTextColor(if (!acak) Color.BLACK else Tema.redup)
    }

    private fun buatLayar(): View {
        val frame = FrameLayout(this)
        val root = akarLatar()
        frame.addView(root, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))

        root.addView(barAtas("Belajar Huruf") { finish() })

        infoTxt = teks(this, "", 14f, Tema.kuning, true)
        infoTxt.gravity = Gravity.CENTER
        root.addView(infoTxt, lp(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0f, 0, 0, 0, dp(6)))

        // Pilihan mode: acak atau urut A-Z
        val baris = linear(this, false)
        baris.gravity = Gravity.CENTER
        chipAcak = teks(this, "🎲 Acak", 13f, Tema.teks, true)
        chipUrut = teks(this, "🔤 A – Z", 13f, Tema.teks, true)
        for (c in listOf(chipAcak, chipUrut)) {
            c.setPadding(dp(14), dp(8), dp(14), dp(8))
            c.gravity = Gravity.CENTER
        }
        chipAcak.setOnClickListener { acak = true; mulaiSesi() }
        chipUrut.setOnClickListener { acak = false; mulaiSesi() }
        baris.addView(chipAcak, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { setMargins(dp(4), 0, dp(4), 0) })
        baris.addView(chipUrut, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { setMargins(dp(4), 0, dp(4), 0) })
        root.addView(baris, lp(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0f, 0, 0, 0, dp(8)))

        // Kartu huruf besar; ketuk untuk mendengar lagi
        kartuHuruf = kartuTema(this)
        kartuHuruf.gravity = Gravity.CENTER
        besar = teks(this, "A", 150f, Tema.hijau, true)
        besar.gravity = Gravity.CENTER
        emojiTxt = teks(this, "🍎", 54f, Tema.teks)
        emojiTxt.gravity = Gravity.CENTER
        kataTxt = teks(this, "", 22f, Tema.teks, true)
        kataTxt.gravity = Gravity.CENTER
        kartuHuruf.addView(besar)
        kartuHuruf.addView(emojiTxt)
        kartuHuruf.addView(kataTxt)
        kartuHuruf.isClickable = true
        kartuHuruf.setOnClickListener { ucapkanSoal() }
        root.addView(kartuHuruf, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))

        teks(this, "Ketuk kartu untuk mendengar lagi", 12f, Tema.redup).also { root.addView(it) }

        val kb = KeyboardLayar(this) { token -> tekanDariLayar(token) }
        pasangKeyboardLayar(kb)
        root.addView(kb, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(6) })

        // Lapisan efek bintang (tidak menghalangi sentuhan)
        lapisEfek = FrameLayout(this)
        lapisEfek.isClickable = false
        frame.addView(lapisEfek, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))

        // Layar hasil akhir sesi
        overlay = FrameLayout(this)
        overlay.setBackgroundColor(Color.parseColor("#E00F0C1D"))
        overlay.visibility = View.GONE
        overlay.isClickable = true
        val kartuHasil = kartuTema(this)
        txtHasil = teks(this, "", 22f, Tema.teks, true)
        txtHasil.gravity = Gravity.CENTER
        kartuHasil.addView(txtHasil)
        val tombol = linear(this, false)
        tombol.addView(
            aksiTombol("🔁 Main Lagi") { mulaiSesi() },
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { setMargins(0, dp(14), dp(5), 0) }
        )
        tombol.addView(
            aksiTombol("🏠 Menu") { finish() },
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

    private fun aksiTombol(label: String, aksi: () -> Unit): TextView {
        val t = teks(this, label, 14f, Tema.teks, true)
        t.gravity = Gravity.CENTER
        t.setPadding(dp(8), dp(12), dp(8), dp(12))
        t.background = riak(this, Tema.biru, 14)
        t.setOnClickListener { aksi() }
        return t
    }
}
