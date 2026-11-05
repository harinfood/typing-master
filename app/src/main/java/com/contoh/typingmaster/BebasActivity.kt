package com.contoh.typingmaster

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

/** Mengetik bebas: setiap ketikan bisa langsung diucapkan (huruf, kata, atau kalimat). */
class BebasActivity : BaseKeyboardActivity() {

    private lateinit var suara: Suara
    private val buffer = StringBuilder()
    private var mode = "kata"
    private lateinit var tampilan: TextView
    private lateinit var txtStat: TextView
    private val tombolMode = HashMap<String, TextView>()
    private val handler = Handler(Looper.getMainLooper())
    private var kursorTampil = true

    private val kursor = object : Runnable {
        override fun run() {
            kursorTampil = !kursorTampil
            tampilkan()
            handler.postDelayed(this, 500L)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        suara = Suara(this)
        mode = Pengaturan.modeTts(this)
        setContentView(buatLayar())
        onOrientasi(isLandscape())
        tampilkan()
        handler.post(kursor)
    }

    override fun onDestroy() {
        handler.removeCallbacks(kursor)
        suara.tutup()
        super.onDestroy()
    }

    override fun tekanHuruf(c: Char) {
        buffer.append(c)
        Pengaturan.tambahTotalHuruf(this, 1)
        when (mode) {
            "huruf" -> suara.bicara(c.toString())
            "kalimat" -> if (c == '.' || c == '!' || c == '?') suara.bicara(barisTerakhir())
        }
        tampilkan()
    }

    override fun tekanSpasi() {
        buffer.append(' ')
        Pengaturan.tambahTotalHuruf(this, 1)
        if (mode == "kata") suara.bicara(kataTerakhir())
        tampilkan()
    }

    override fun tekanHapus() {
        if (buffer.isNotEmpty()) buffer.deleteCharAt(buffer.length - 1)
        tampilkan()
    }

    override fun tekanEnter() {
        buffer.append('\n')
        suara.bicara(barisTerakhir())
        tampilkan()
    }

    private fun kataTerakhir(): String =
        buffer.toString().trimEnd().split(Regex("\\s+")).lastOrNull() ?: ""

    private fun barisTerakhir(): String =
        buffer.toString().trimEnd().split('\n').last().trim()

    private fun tampilkan() {
        val kosong = buffer.isEmpty()
        tampilan.text = if (kosong) {
            "Ketik sesuatu…" + if (kursorTampil) "▌" else " "
        } else {
            buffer.toString() + if (kursorTampil) "▌" else " "
        }
        tampilan.setTextColor(if (kosong) Tema.redup else Tema.teks)
        val huruf = buffer.count { !it.isWhitespace() }
        val kata = buffer.toString().trim().split(Regex("\\s+")).count { it.isNotEmpty() }
        txtStat.text = "Huruf: $huruf  ·  Kata: $kata"
    }

    private fun segarkanMode() {
        for ((kode, t) in tombolMode) {
            val aktif = kode == mode
            t.background = if (aktif) kotakGradien(this, Tema.biru, Tema.ungu, 14) else kotak(this, Tema.tombol, 14)
            t.setTextColor(if (aktif) Color.WHITE else Tema.redup)
        }
    }

    private fun buatLayar(): View {
        val root = akarLatar()
        root.addView(barAtas("Mengetik Bebas") { finish() })

        // Pilihan cara pengucapan
        val baris = linear(this, false)
        baris.gravity = Gravity.CENTER
        for ((kode, label) in listOf("huruf" to "🔤 Huruf", "kata" to "💬 Kata", "kalimat" to "📝 Kalimat")) {
            val t = teks(this, label, 13f, Tema.teks, true)
            t.gravity = Gravity.CENTER
            t.setPadding(dp(10), dp(9), dp(10), dp(9))
            t.setOnClickListener {
                mode = kode
                Pengaturan.simpanModeTts(this, kode)
                segarkanMode()
            }
            tombolMode[kode] = t
            baris.addView(t, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { setMargins(dp(4), 0, dp(4), 0) })
        }
        root.addView(baris, lp(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0f, dp(4)))
        segarkanMode()

        // Layar tulisan (ikut tinggi layar, bisa digulir)
        val kartu = kartuTema(this)
        tampilan = teks(this, "", 26f, Tema.teks)
        tampilan.setLineSpacing(dp(6).toFloat(), 1f)
        val scroll = ScrollView(this)
        scroll.isFillViewport = true
        scroll.isFocusable = false
        scroll.isFocusableInTouchMode = false
        scroll.addView(tampilan)
        kartu.addView(scroll, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))
        root.addView(kartu, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f).apply { setMargins(0, dp(8), 0, dp(6)) })

        txtStat = teks(this, "", 12f, Tema.redup)
        txtStat.gravity = Gravity.CENTER
        root.addView(txtStat)

        val aksi = linear(this, false)
        aksi.addView(
            aksiTombol("🔊 Ucapkan Semua") { suara.bicara(buffer.toString()) },
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { setMargins(0, dp(6), dp(4), dp(4)) }
        )
        aksi.addView(
            aksiTombol("🗑 Hapus Semua") {
                buffer.clear()
                tampilkan()
            },
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { setMargins(dp(4), dp(6), 0, dp(4)) }
        )
        root.addView(aksi)

        val kb = KeyboardLayar(this) { token -> tekanDariLayar(token) }
        pasangKeyboardLayar(kb)
        root.addView(kb, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(4) })
        return root
    }

    private fun aksiTombol(label: String, aksi: () -> Unit): TextView {
        val t = teks(this, label, 14f, Tema.teks, true)
        t.gravity = Gravity.CENTER
        t.setPadding(dp(10), dp(12), dp(10), dp(12))
        t.background = riak(this, Tema.tombol, 14)
        t.setOnClickListener { aksi() }
        return t
    }
}
