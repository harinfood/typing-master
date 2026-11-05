package com.contoh.typingmaster

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.View.MeasureSpec

/**
 * Piano 26 tuts (C4 sampai C#6) dengan huruf A sampai Z dari kiri ke kanan.
 * Setiap tuts putih juga menampilkan nama solmisasi (Do, Re, Mi, ...) dan nama nadanya.
 * Lebar tuts tetap agar mudah ditekan; piano digulir horizontal bila lebih lebar dari layar.
 */
class PianoView(ctx: Context, private val onTekan: (Int) -> Unit) : View(ctx) {

    companion object {
        const val LEBAR_TUTS_DP = 54
        val NAMA_NADA = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
        val SOLMISASI = arrayOf("Do", "Do#", "Re", "Re#", "Mi", "Fa", "Fa#", "Sol", "Sol#", "La", "La#", "Si")
        private val WARNA_SOLMISASI = mapOf(
            0 to "#FF5252", 2 to "#FF9100", 4 to "#FFEA00", 5 to "#76FF03",
            7 to "#00E5FF", 9 to "#2979FF", 11 to "#D500F9"
        )
        fun adalahHitam(s: Int): Boolean = (s % 12) in setOf(1, 3, 6, 8, 10)
        fun namaNada(s: Int): String = "${NAMA_NADA[s % 12]}${4 + s / 12}"
        fun solmisasi(s: Int): String = SOLMISASI[s % 12]
        fun warnaSolmisasi(s: Int): Int = Color.parseColor(WARNA_SOLMISASI[s % 12] ?: "#FFFFFF")
    }

    /** Tuts yang harus ditekan berikutnya (disorot kuning). -1 = tidak ada. */
    var targetKey: Int = -1
        set(value) {
            field = value
            invalidate()
        }

    private val putih = ArrayList<Int>()
    private val hitam = ArrayList<Int>()
    private val rectPutih = HashMap<Int, RectF>()
    private val rectHitam = HashMap<Int, RectF>()
    private val kilat = HashSet<Int>()
    private val penunjuk = HashMap<Int, Int>()
    private val handler = Handler(Looper.getMainLooper())

    private val pPutih = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#F5F5FA") }
    private val pPutihTekan = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#00E5FF") }
    private val pHitam = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#14102A") }
    private val pHitamTekan = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#D500F9") }
    private val pGaris = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = context.dpF(1.5f)
        color = Color.parseColor("#9A93C4")
    }
    private val pTarget = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = context.dpF(3.5f)
        color = Color.parseColor("#FFEA00")
    }
    private val pHuruf = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    private val pSolmisasi = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    private val pNada = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        color = Color.parseColor("#7A74A8")
    }

    init {
        for (s in 0 until 26) if (adalahHitam(s)) hitam.add(s) else putih.add(s)
    }

    /** Lebar piano = jumlah tuts putih x lebar tuts tetap; tinggi mengikuti ruang yang diberikan layout. */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val lebarDiminta = putih.size * context.dp(LEBAR_TUTS_DP)
        val lebar = maxOf(MeasureSpec.getSize(widthMeasureSpec), lebarDiminta)
        val tinggi = if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            context.dp(220)
        } else {
            MeasureSpec.getSize(heightMeasureSpec)
        }
        setMeasuredDimension(lebar, tinggi)
    }

    override fun onSizeChanged(w: Int, h: Int, ow: Int, oh: Int) {
        val ww = w.toFloat() / putih.size
        rectPutih.clear()
        rectHitam.clear()
        for ((i, s) in putih.withIndex()) {
            rectPutih[s] = RectF(i * ww, 0f, (i + 1) * ww, h.toFloat())
        }
        for (s in hitam) {
            val kiri = putih.indexOf(s - 1)
            val c = (kiri + 1) * ww
            rectHitam[s] = RectF(c - ww * 0.32f, 0f, c + ww * 0.32f, h * 0.62f)
        }
    }

    override fun onDraw(c: Canvas) {
        super.onDraw(c)
        val ww = width.toFloat() / putih.size
        val h = height.toFloat()
        val radius = ww * 0.1f
        val inset = context.dpF(2.5f)

        for (s in putih) {
            val r = rectPutih[s] ?: continue
            c.drawRoundRect(r, radius, radius, if (s in kilat) pPutihTekan else pPutih)
            c.drawRoundRect(r, radius, radius, pGaris)
            if (s == targetKey) {
                val t = RectF(r.left + inset, r.top + inset, r.right - inset, r.bottom - inset)
                c.drawRoundRect(t, radius, radius, pTarget)
            }
            // Huruf keyboard (besar)
            pHuruf.color = Color.parseColor("#14102A")
            pHuruf.textSize = ww * 0.42f
            c.drawText(('A' + s).toString(), r.centerX(), h * 0.66f, pHuruf)
            // Solmisasi berwarna
            pSolmisasi.color = warnaSolmisasi(s)
            pSolmisasi.textSize = ww * 0.27f
            c.drawText(solmisasi(s), r.centerX(), h * 0.8f, pSolmisasi)
            // Nama nada kecil
            pNada.textSize = ww * 0.2f
            c.drawText(namaNada(s), r.centerX(), h * 0.93f, pNada)
        }
        for (s in hitam) {
            val r = rectHitam[s] ?: continue
            c.drawRoundRect(r, radius, radius, if (s in kilat) pHitamTekan else pHitam)
            if (s == targetKey) {
                val t = RectF(r.left + inset, r.top + inset, r.right - inset, r.bottom - inset)
                c.drawRoundRect(t, radius, radius, pTarget)
            }
            pSolmisasi.color = Color.WHITE
            pSolmisasi.textSize = ww * 0.2f
            c.drawText(solmisasi(s), r.centerX(), r.top + r.height() * 0.45f, pSolmisasi)
            pHuruf.color = Color.WHITE
            pHuruf.textSize = ww * 0.36f
            c.drawText(('A' + s).toString(), r.centerX(), r.bottom - ww * 0.2f, pHuruf)
        }
    }

    private fun keyAt(x: Float, y: Float): Int {
        for (s in hitam) if (rectHitam[s]?.contains(x, y) == true) return s
        for (s in putih) if (rectPutih[s]?.contains(x, y) == true) return s
        return -1
    }

    /** Efek kilat pada tuts (dipanggil juga dari keyboard fisik). */
    fun kilatKunci(s: Int) {
        kilat.add(s)
        invalidate()
        handler.postDelayed({
            kilat.remove(s)
            invalidate()
        }, 180L)
    }

    private fun tekanKunci(s: Int) {
        kilatKunci(s)
        onTekan(s)
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                val idx = e.actionIndex
                val id = e.getPointerId(idx)
                val s = keyAt(e.getX(idx), e.getY(idx))
                penunjuk[id] = s
                if (s >= 0) tekanKunci(s)
            }
            MotionEvent.ACTION_MOVE -> {
                for (i in 0 until e.pointerCount) {
                    val id = e.getPointerId(i)
                    val s = keyAt(e.getX(i), e.getY(i))
                    if (s >= 0 && penunjuk[id] != s) {
                        penunjuk[id] = s
                        tekanKunci(s)
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> penunjuk.remove(e.getPointerId(e.actionIndex))
            MotionEvent.ACTION_CANCEL -> penunjuk.clear()
        }
        return true
    }
}
