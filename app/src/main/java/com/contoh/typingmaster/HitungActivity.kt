
package com.contoh.typingmaster

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
 * Belajar angka dan matematika. Soal seperti "1 + 5 = ?", jawaban diketik lalu Enter.
 * Benar: "Anda benar, <nama>". Salah: "Anda salah, <nama>".
 */
class HitungActivity : BaseKeyboardActivity() {

    companion object {
        const val TOTAL_SOAL = 10
        val MODE_LABEL = listOf("🔢 Angka", "➕ Tambah", "➖ Kurang", "✖ Kali", "🎲 Campur")
        val LEVEL_LABEL = listOf("Angka 1–5", "Angka 1–10", "Angka 1–20")
        private val SATUAN = arrayOf("nol", "satu", "dua", "tiga", "empat", "lima", "enam", "tujuh", "delapan", "sembilan", "sepuluh", "sebelas")

        /** Mengubah angka menjadi kata Bahasa Indonesia, misalnya 12 -> "dua belas". */
        fun kataAngka(n: Int): String = when {
            n < 0 -> "min ${kataAngka(-n)}"
            n < 12 -> SATUAN[n]
            n < 20 -> "${kataAngka(n - 10)} belas"
            n < 100 -> {
                val sisa = n % 10
                "${kataAngka(n / 10)} puluh" + if (sisa > 0) " ${kataAngka(sisa)}" else ""
            }
            n < 200 -> "seratus" + if (n % 100 > 0) " ${kataAngka(n - 100)}" else ""
            n < 1000 -> "${kataAngka(n / 100)} ratus" + if (n % 100 > 0) " ${kataAngka(n % 100)}" else ""
            else -> n.toString()
        }
    }

    private lateinit var suara: Suara
    private val handler = Handler(Looper.getMainLooper())
    private var modeIdx = 1
    private var levelIdx = 0
    private var nomorSoal = 0
    private var skor = 0
    private var salahSoal = 0
    private var hasilBenar = 0
    private var jawaban = ""
    private var sibuk = false
    private var selesai = false

    private val chipMode = ArrayList<TextView>()
    private val chipLevel = ArrayList<TextView>()
    private lateinit var kartuSoal: LinearLayout
    private lateinit var txtSoal: TextView
    private lateinit var txtKet: TextView
    private lateinit var txtJawab: TextView
    private lateinit var txtUmpan: TextView
    private lateinit var txtInfo: TextView
    private lateinit var lapisHasil: FrameLayout
    private lateinit var txtHasil: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        suara = Suara(this)
        setContentView(buatLayar())
        onOrientasi(isLandscape())
        mulai()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        suara.tutup()
        super.onDestroy()
    }

    override fun onOrientasi(landscape: Boolean) {
        super.onOrientasi(landscape)
        txtSoal.textSize = if (landscape) 52f else 66f
        txtJawab.textSize = if (landscape) 26f else 34f
    }

    private fun namaPeserta(): String = Pengaturan.nama(this)

    /** ", nama" bila nama peserta sudah diisi. */
    private fun sapa(): String {
        val n = namaPeserta()
        return if (n.isEmpty()) "" else ", $n"
    }

    private val maksLevel: Int
        get() = when (levelIdx) {
            0 -> 5
            1 -> 10
            else -> 20
        }

    private fun mulai() {
        nomorSoal = 0
        skor = 0
        selesai = false
        sibuk = false
        lapisHasil.visibility = View.GONE
        segarkanChip()
        soalBerikut()
    }

    private fun soalBerikut() {
        if (nomorSoal >= TOTAL_SOAL) {
            selesaiSesi()
            return
        }
        nomorSoal++
        salahSoal = 0
        jawaban = ""
        sibuk = false
        txtUmpan.text = ""
        txtUmpan.setTextColor(Tema.teks)
        val mode = if (modeIdx == 4) Random.nextInt(0, 4) else modeIdx
        if (mode == 0) soalAngka() else soalMath(mode)
        tampilJawab()
        txtInfo.text = "Soal $nomorSoal dari $TOTAL_SOAL   ·   Skor $skor"
    }

    /** Belajar angka: gambar apel sebanyak angka, anak mengetik angkanya. */
    private fun soalAngka() {
        val lim = if (levelIdx == 0) 5 else 10
        val n = Random.nextInt(0, lim + 1)
        hasilBenar = n
        txtSoal.text = if (n == 0) "—" else "🍎".repeat(n)
        txtKet.text = "Ada ${kataAngka(n)} apel. Tekan angkanya!"
        suara.bicara("Ada ${kataAngka(n)} apel. Tekan angka $n.")
    }

    /** Soal tambah, kurang, atau kali. Mengembalikan (teks soal, jawaban, ucapan). */
    private fun soalMath(mode: Int) {
        val maks = maksLevel
        val (teksSoal, hasil, ucapan) = when (mode) {
            1 -> {
                val a = Random.nextInt(0, maks + 1)
                val b = Random.nextInt(0, maks + 1)
                Triple("$a + $b = ?", a + b, "$a tambah $b berapa?")
            }
            2 -> {
                val a = Random.nextInt(0, maks + 1)
                val b = Random.nextInt(0, a + 1)
                Triple("$a − $b = ?", a - b, "$a kurang $b berapa?")
            }
            else -> {
                val lim = if (levelIdx == 0) 5 else 10
                val a = Random.nextInt(0, lim + 1)
                val b = Random.nextInt(0, lim + 1)
                Triple("$a × $b = ?", a * b, "$a kali $b berapa?")
            }
        }
        hasilBenar = hasil
        txtSoal.text = teksSoal
        txtKet.text = "Ketik jawabannya, lalu tekan Enter"
        suara.bicara(ucapan)
    }

    override fun tekanHuruf(c: Char) {
        if (sibuk || selesai) return
        if (c.isDigit() && jawaban.length < 3) {
            jawaban += c
            tampilJawab()
        }
    }

    override fun tekanHapus() {
        if (sibuk || selesai) return
        if (jawaban.isNotEmpty()) {
            jawaban = jawaban.dropLast(1)
            tampilJawab()
        }
    }

    override fun tekanEnter() = cekJawaban()

    private fun tampilJawab() {
        txtJawab.text = if (jawaban.isEmpty()) "?" else jawaban
    }

    private fun cekJawaban() {
        if (sibuk || selesai || jawaban.isEmpty()) return
        val v = jawaban.toIntOrNull() ?: return
        if (v == hasilBenar) {
            sibuk = true
            if (salahSoal == 0) skor++
            txtUmpan.text = "✅ Anda benar${sapa()}"
            txtUmpan.setTextColor(Tema.hijau)
            suara.bicara("Anda benar${sapa()}")
            denyutKartu()
            handler.postDelayed({ soalBerikut() }, 1600L)
        } else {
            salahSoal++
            jawaban = ""
            tampilJawab()
            goyangKartu()
            txtUmpan.setTextColor(Tema.merah)
            suara.bicara("Anda salah${sapa()}")
            if (salahSoal >= 3) {
                // Setelah 3 kali salah, jawaban diberitahukan lalu lanjut
                sibuk = true
                txtUmpan.text = "Jawabannya: $hasilBenar"
                handler.postDelayed({
                    suara.bicara("Jawabannya adalah ${kataAngka(hasilBenar)}")
                }, 1700L)
                handler.postDelayed({ soalBerikut() }, 3600L)
            } else {
                txtUmpan.text = "❌ Anda salah${sapa()}, coba lagi"
            }
        }
    }

    private fun selesaiSesi() {
        selesai = true
        Pengaturan.simpanHitungBest(this, modeIdx, skor)
        val bintang = when {
            skor >= 9 -> 3
            skor >= 6 -> 2
            else -> 1
        }
        txtHasil.text = "🎉 Selesai!\n\n${bintangTeks(bintang)}\n\nSkor: $skor dari $TOTAL_SOAL\nTerbaik: ${Pengaturan.hitungBest(this, modeIdx)}"
        lapisHasil.visibility = View.VISIBLE
        lapisHasil.alpha = 0f
        lapisHasil.animate().alpha(1f).setDuration(350L).start()
        suara.bicara("Selesai! Skor kamu $skor dari $TOTAL_SOAL${sapa()}")
    }

    private fun denyutKartu() {
        kartuSoal.animate().scaleX(1.08f).scaleY(1.08f).setDuration(160L).withEndAction {
            kartuSoal.animate().scaleX(1f).scaleY(1f).setDuration(200L).start()
        }.start()
    }

    private fun goyangKartu() {
        kartuSoal.animate().translationX(-14f).setDuration(60L).withEndAction {
            kartuSoal.animate().translationX(14f).setDuration(60L).withEndAction {
                kartuSoal.animate().translationX(0f).setDuration(60L).start()
            }.start()
        }.start()
    }

    private fun segarkanChip() {
        for ((i, t) in chipMode.withIndex()) styleChip(t, i == modeIdx)
        for ((i, t) in chipLevel.withIndex()) styleChip(t, i == levelIdx)
    }

    private fun styleChip(t: TextView, aktif: Boolean) {
        t.background = if (aktif) kotakGradien(this, Tema.biru, Tema.ungu, 12) else kotak(this, Tema.tombol, 12)
        t.setTextColor(if (aktif) Color.WHITE else Tema.redup)
    }

    private fun chip(label: String, aksi: () -> Unit): TextView {
        val t = teks(this, label, 12f, Tema.teks, true)
        t.gravity = Gravity.CENTER
        t.setPadding(dp(4), dp(8), dp(4), dp(8))
        t.setOnClickListener { aksi() }
        return t
    }

    private fun buatLayar(): View {
        val frame = FrameLayout(this)
        val root = akarLatar()
        frame.addView(root, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))

        root.addView(barAtas("Angka & Matematika") { finish() })

        // Pilihan mode
        val barisMode = linear(this, false)
        for ((i, label) in MODE_LABEL.withIndex()) {
            val t = chip(label) {
                modeIdx = i
                mulai()
            }
            chipMode.add(t)
            barisMode.addView(t, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { setMargins(dp(2), 0, dp(2), 0) })
        }
        root.addView(barisMode, lp(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0f, 0, 0, 0, dp(4)))

        // Pilihan rentang angka
        val barisLevel = linear(this, false)
        for ((i, label) in LEVEL_LABEL.withIndex()) {
            val t = chip(label) {
                levelIdx = i
                mulai()
            }
            chipLevel.add(t)
            barisLevel.addView(t, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { setMargins(dp(2), 0, dp(2), 0) })
        }
        root.addView(barisLevel, lp(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0f, 0, 0, 0, dp(6)))

        txtInfo = teks(this, "", 13f, Tema.kuning, true)
        txtInfo.gravity = Gravity.CENTER
        root.addView(txtInfo, lp(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0f, 0, 0, 0, dp(4)))

        // Kartu soal
        kartuSoal = kartuTema(this)
        kartuSoal.gravity = Gravity.CENTER
        txtSoal = teks(this, "", 66f, Tema.teks, true)
        txtSoal.gravity = Gravity.CENTER
        txtKet = teks(this, "", 16f, Tema.redup)
        txtKet.gravity = Gravity.CENTER
        kartuSoal.addView(txtSoal, lp(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        kartuSoal.addView(txtKet, lp(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0f, dp(8), 0, 0, 0))
        root.addView(kartuSoal, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))

        // Jawaban yang sedang diketik
        val barisJawab = linear(this, false)
        barisJawab.gravity = Gravity.CENTER
        barisJawab.addView(teks(this, "Jawabanmu:", 16f, Tema.redup, true))
        txtJawab = teks(this, "?", 34f, Tema.kuning, true)
        txtJawab.gravity = Gravity.CENTER
        barisJawab.addView(txtJawab, lp(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0f, 0, dp(10), 0, 0))
        root.addView(barisJawab, lp(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0f, dp(6), 0, 0, 0))

        txtUmpan = teks(this, "", 15f, Tema.teks, true)
        txtUmpan.gravity = Gravity.CENTER
        root.addView(txtUmpan, lp(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0f, dp(2), 0, 0, dp(4)))

        val kb = KeyboardLayar(this) { token -> tekanDariLayar(token) }
        pasangKeyboardLayar(kb)
        root.addView(kb, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))

        val petunjuk = teks(this, "Enter = periksa jawaban  ·  Esc = kembali", 12f, Tema.redup)
        petunjuk.gravity = Gravity.CENTER
        root.addView(petunjuk, lp(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0f, 0, 0, 0, dp(4)))

        // Layar hasil akhir sesi
        lapisHasil = FrameLayout(this)
        lapisHasil.setBackgroundColor(Color.parseColor("#E00F0C1D"))
        lapisHasil.visibility = View.GONE
        lapisHasil.isClickable = true
        val kartuHasil = kartuTema(this)
        txtHasil = teks(this, "", 22f, Tema.teks, true)
        txtHasil.gravity = Gravity.CENTER
        kartuHasil.addView(txtHasil)
        val tombol = linear(this, false)
        tombol.addView(
            aksiTombol("🔁 Main Lagi") { mulai() },
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { setMargins(0, dp(14), dp(5), 0) }
        )
        tombol.addView(
            aksiTombol("🏠 Menu") { finish() },
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { setMargins(dp(5), dp(14), 0, 0) }
        )
        kartuHasil.addView(tombol)
        lapisHasil.addView(
            kartuHasil,
            FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER).apply {
                setMargins(dp(24), 0, dp(24), 0)
            }
        )
        frame.addView(lapisHasil, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
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
