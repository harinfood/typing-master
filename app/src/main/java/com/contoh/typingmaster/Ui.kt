package com.contoh.typingmaster

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.widget.LinearLayout
import android.widget.TextView

fun Context.dp(v: Int): Int = Math.round(v * resources.displayMetrics.density)

fun Context.dpF(v: Float): Float = v * resources.displayMetrics.density

/** Warna tema neon Typing Master. */
object Tema {
    val latarAtas = Color.parseColor("#0F0C1D")
    val latarBawah = Color.parseColor("#1A1436")
    val kartu = Color.parseColor("#1F1A3D")
    val garis = Color.parseColor("#3A3368")
    val tombol = Color.parseColor("#2A2550")
    val sorot = Color.parseColor("#00E5FF")
    val hint = Color.parseColor("#FFEA00")
    val merah = Color.parseColor("#FF3D71")
    val hijau = Color.parseColor("#00E676")
    val biru = Color.parseColor("#2979FF")
    val kuning = Color.parseColor("#FFEA00")
    val ungu = Color.parseColor("#D500F9")
    val oranye = Color.parseColor("#FF9100")
    val teks = Color.WHITE
    val redup = Color.parseColor("#A59CC9")
}

fun kotak(ctx: Context, warna: Int, radiusDp: Int, garis: Int = 0): GradientDrawable {
    val g = GradientDrawable()
    g.setColor(warna)
    g.setCornerRadius(ctx.dpF(radiusDp.toFloat()))
    if (garis != 0) g.setStroke(ctx.dp(1), garis)
    return g
}

fun kotakGradien(ctx: Context, c1: Int, c2: Int, radiusDp: Int): GradientDrawable {
    val g = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(c1, c2))
    g.setCornerRadius(ctx.dpF(radiusDp.toFloat()))
    return g
}

/** Latar kartu dengan efek riak saat disentuh. */
fun riak(ctx: Context, warna: Int, radiusDp: Int, garis: Int = 0): RippleDrawable =
    RippleDrawable(ColorStateList.valueOf(Color.parseColor("#44FFFFFF")), kotak(ctx, warna, radiusDp, garis), null)

fun kartuTema(ctx: Context): LinearLayout {
    val k = linear(ctx)
    k.setPadding(ctx.dp(16), ctx.dp(14), ctx.dp(16), ctx.dp(14))
    k.background = kotak(ctx, Tema.kartu, 20, Tema.garis)
    return k
}

fun teks(ctx: Context, isi: String, sp: Float, warna: Int, tebal: Boolean = false): TextView {
    val t = TextView(ctx)
    t.text = isi
    t.textSize = sp
    t.setTextColor(warna)
    if (tebal) t.typeface = Typeface.DEFAULT_BOLD
    return t
}

fun linear(ctx: Context, vertikal: Boolean = true): LinearLayout {
    val l = LinearLayout(ctx)
    l.orientation = if (vertikal) LinearLayout.VERTICAL else LinearLayout.HORIZONTAL
    return l
}

fun lp(
    lebar: Int,
    tinggi: Int,
    bobot: Float = 0f,
    atas: Int = 0,
    kiri: Int = 0,
    kanan: Int = 0,
    bawah: Int = 0
): LinearLayout.LayoutParams {
    val p = LinearLayout.LayoutParams(lebar, tinggi, bobot)
    p.setMargins(kiri, atas, kanan, bawah)
    return p
}

/** Latar utama layar: gradien gelap, responsif karena memakai bobot tata letak. */
fun Context.akarLatar(): LinearLayout {
    val r = linear(this)
    r.setPadding(dp(14), dp(10), dp(14), dp(14))
    r.background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(Tema.latarAtas, Tema.latarBawah))
    return r
}

/** Bar atas: tombol kembali dan judul layar. */
fun Context.barAtas(judul: String, onKembali: () -> Unit): LinearLayout {
    val bar = linear(this, false)
    bar.gravity = android.view.Gravity.CENTER_VERTICAL
    bar.setPadding(0, dp(6), 0, dp(10))
    val kembali = teks(this, "←", 24f, Tema.kuning, true)
    kembali.gravity = android.view.Gravity.CENTER
    kembali.setPadding(dp(8), 0, dp(14), 0)
    kembali.setOnClickListener { onKembali() }
    bar.addView(kembali)
    bar.addView(teks(this, judul, 20f, Tema.teks, true), LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
    return bar
}

fun bintangTeks(n: Int): String = "★".repeat(n) + "☆".repeat(3 - n)
