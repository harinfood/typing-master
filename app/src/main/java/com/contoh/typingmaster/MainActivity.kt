package com.contoh.typingmaster

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.hardware.input.InputManager
import android.os.Handler
import android.os.Looper
import android.view.InputDevice
import android.view.MotionEvent
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : BaseKeyboardActivity() {

    private lateinit var inputManager: InputManager
    private lateinit var chipKeyboard: LinearLayout
    private lateinit var txtKeyboard: TextView
    private lateinit var txtHalo: TextView

    private val pendengarInput = object : InputManager.InputDeviceListener {
        override fun onInputDeviceAdded(deviceId: Int) = segarkanKeyboard()
        override fun onInputDeviceRemoved(deviceId: Int) = segarkanKeyboard()
        override fun onInputDeviceChanged(deviceId: Int) = segarkanKeyboard()
    }

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        inputManager = getSystemService(Context.INPUT_SERVICE) as InputManager
        setContentView(buatLayar())
    }

    override fun onResume() {
        super.onResume()
        inputManager.registerInputDeviceListener(pendengarInput, Handler(Looper.getMainLooper()))
        segarkanKeyboard()
        val nama = Pengaturan.nama(this)
        if (nama.isEmpty()) {
            startActivity(Intent(this, NamaActivity::class.java))
        } else {
            txtHalo.text = "Halo, $nama!  ✏ ganti nama"
        }
    }

    override fun onPause() {
        inputManager.unregisterInputDeviceListener(pendengarInput)
        super.onPause()
    }

    private fun buatLayar(): View {
        val scroll = ScrollView(this)
        scroll.isFillViewport = true
        scroll.isFocusable = false
        scroll.isFocusableInTouchMode = false
        scroll.setBackgroundColor(Tema.latarAtas)

        val root = akarLatar()
        scroll.addView(root)

        root.addView(buatHeader())

        chipKeyboard = linear(this, false)
        chipKeyboard.setPadding(dp(14), dp(10), dp(14), dp(10))
        chipKeyboard.background = kotak(this, Tema.kartu, 16, Tema.garis)
        txtKeyboard = teks(this, "", 14f, Tema.teks, true)
        chipKeyboard.addView(txtKeyboard)
        root.addView(chipKeyboard, lp(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0f, dp(14)))

        val baris1 = linear(this, false)
        baris1.addView(
            kartuMenu("Mengetik Bebas", "Ketik apa saja. Huruf dan kata langsung diucapkan", "⌨️", Tema.biru, Tema.ungu) {
                startActivity(Intent(this, BebasActivity::class.java))
            },
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { setMargins(dp(5), 0, dp(5), 0) }
        )
        baris1.addView(
            kartuMenu("Latihan Mengetik", "Latihan jari dari baris huruf sampai kalimat", "🎯", Tema.oranye, Tema.merah) {
                startActivity(Intent(this, PilihLatihanActivity::class.java))
            },
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { setMargins(dp(5), 0, dp(5), 0) }
        )
        root.addView(baris1, lp(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0f, dp(4)))

        val baris2 = linear(this, false)
        baris2.addView(
            kartuMenu("Belajar Huruf", "Game huruf A sampai Z untuk anak-anak", "🔤", Tema.hijau, Tema.kuning) {
                startActivity(Intent(this, HurufActivity::class.java))
            },
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { setMargins(dp(5), 0, dp(5), 0) }
        )
        baris2.addView(
            kartuMenu("Statistik", "Lihat hasil terbaik dan kemajuan belajar", "📊", Tema.merah, Tema.ungu) {
                tampilStatistik()
            },
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { setMargins(dp(5), 0, dp(5), 0) }
        )
        root.addView(baris2, lp(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0f, dp(4)))

        root.addView(
            kartuMenu("Piano Huruf", "Tekan tuts piano: berbunyi nada dan tampil hurufnya", "🎹", Tema.merah, Tema.kuning) {
                startActivity(Intent(this, PianoActivity::class.java))
            },
            lp(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0f, dp(4), dp(5), dp(5), 0)
        )
        root.addView(
            teks(this, "Colokkan keyboard lewat kabel OTG, lalu mulai mengetik.\nTidak punya keyboard? Gunakan keyboard di layar.", 13f, Tema.redup).apply {
                gravity = android.view.Gravity.CENTER
                setPadding(dp(8), dp(18), dp(8), dp(8))
            }
        )
        return scroll
    }

    private fun buatHeader(): View {
        val h = linear(this, false)
        h.gravity = android.view.Gravity.CENTER_VERTICAL
        h.setPadding(dp(4), dp(8), dp(4), dp(14))

        val logo = ImageView(this)
        logo.setImageResource(R.drawable.logo_typing)
        logo.scaleType = ImageView.ScaleType.FIT_CENTER
        logo.scaleX = 0.5f
        logo.scaleY = 0.5f
        logo.animate().scaleX(1f).scaleY(1f).setDuration(800L).setInterpolator(OvershootInterpolator()).start()
        h.addView(logo, LinearLayout.LayoutParams(dp(76), dp(76)).apply { marginEnd = dp(12) })

        val kolom = linear(this)
        val judul = teks(this, "TYPING MASTER", 24f, Tema.kuning, true)
        judul.letterSpacing = 0.06f
        kolom.addView(judul)
        kolom.addView(teks(this, "Belajar mengetik by Harinfood", 13f, Tema.redup))
        txtHalo = teks(this, "Halo!", 14f, Tema.hijau, true)
        txtHalo.setOnClickListener { startActivity(Intent(this, NamaActivity::class.java)) }
        kolom.addView(txtHalo)
        h.addView(kolom, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        return h
    }

    private fun kartuMenu(judul: String, deskripsi: String, emoji: String, c1: Int, c2: Int, aksi: () -> Unit): LinearLayout {
        val k = linear(this)
        k.setPadding(dp(16), dp(16), dp(16), dp(16))
        k.background = kotakGradien(this, c1, c2, 20)
        k.addView(teks(this, emoji, 32f, Color.WHITE))
        k.addView(teks(this, judul, 17f, Color.WHITE, true).apply { setPadding(0, dp(8), 0, 0) })
        k.addView(teks(this, deskripsi, 12f, Color.parseColor("#F2F2F2")).apply { setPadding(0, dp(4), 0, 0) })
        k.isClickable = true
        k.setOnClickListener { aksi() }
        // Animasi tekan: kartu mengecil sedikit, lalu membal kembali
        k.setOnTouchListener { v, e ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> v.animate().scaleX(0.94f).scaleY(0.94f).setDuration(90L).start()
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                    v.animate().scaleX(1f).scaleY(1f).setDuration(260L).setInterpolator(OvershootInterpolator()).start()
            }
            false
        }
        return k
    }

    private fun adaKeyboardFisik(): Boolean = InputDevice.getDeviceIds().any { id ->
        val d = InputDevice.getDevice(id)
        d != null && !d.isVirtual && d.keyboardType == InputDevice.KEYBOARD_TYPE_ALPHABETIC
    }

    private fun segarkanKeyboard() {
        val ada = adaKeyboardFisik()
        txtKeyboard.text = if (ada) {
            "⌨  Keyboard fisik terhubung. Siap mengetik!"
        } else {
            "⚠  Keyboard belum terdeteksi. Colokkan lewat OTG atau pakai keyboard di layar."
        }
        txtKeyboard.setTextColor(if (ada) Tema.hijau else Tema.oranye)
    }

    private fun tampilStatistik() {
        val sb = StringBuilder()
        for ((i, lv) in Data.LEVEL.withIndex()) {
            sb.append("${lv.nama}: ${Pengaturan.bestWpm(this, i)} kpm  ${bintangTeks(Pengaturan.bestBintang(this, i))}\n")
        }
        sb.append("\nHuruf dikenal: ${Pengaturan.hurufDikenal(this).length}/26\n")
        sb.append("Bintang game huruf: ${Pengaturan.totalBintangGame(this)}\n")
        sb.append("Total huruf diketik: ${Pengaturan.totalHuruf(this)}")
        AlertDialog.Builder(this)
            .setTitle("Statistik")
            .setMessage(sb.toString())
            .setPositiveButton("Tutup", null)
            .show()
    }

    // Di layar utama tidak ada tulisan yang diketik, jadi input keyboard diabaikan
    override fun tekanHuruf(c: Char) {}
}
