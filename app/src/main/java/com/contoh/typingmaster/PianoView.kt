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

/**
 * Piano 26 tuts (C4 sampai C#6) dengan huruf A sampai Z dari kiri ke kanan.
 * Mendukung banyak jari (multi-touch) dan geser jari di atas tuts.
 */
class PianoView(ctx: Context, private val onTekan: (Int) -> Unit) : View(ctx) {

    companion object {
        val NAMA_NADA = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
        fun adalahHitam(s: Int): Boolean = (s % 12) in setOf(1, 3, 6, 8, 10)
        fun namaNada(s: Int): String = "${NAMA_NADA[s % 12]}${4 + s / 12}"
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
    private val pHuruf = Paint(Paint.ANTI_ALIAS_FLAG).apply {
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
        val radius = ww * 0.08f

        for (s in putih) {
            val r = rectPutih[s] ?: continue
            c.drawRoundRect(r, radius, radius, if (s in kilat) pPutihTekan else pPutih)
            c.drawRoundRect(r, radius, radius, pGaris)
            pHuruf.color = Color.parseColor("#14102A")
            pHuruf.textSize = ww * 0.5f
            c.drawText(('A' + s).toString(), r.centerX(), h * 0.8f, pHuruf)
            pNada.textSize = ww * 0.22f
            c.drawText(namaNada(s), r.centerX(), h * 0.93f, pNada)
        }
        for (s in hitam) {
            val r = rectHitam[s] ?: continue
            c.drawRoundRect(r, radius, radius, if (s in kilat) pHitamTekan else pHitam)
            pHuruf.color = Color.WHITE
            pHuruf.textSize = ww * 0.36f
            c.drawText(('A' + s).toString(), r.centerX(), r.bottom - ww * 0.22f, pHuruf)
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
