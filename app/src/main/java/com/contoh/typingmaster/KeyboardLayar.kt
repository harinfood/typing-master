package com.contoh.typingmaster

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

/**
 * Keyboard di layar. Dipakai bila keyboard fisik belum tersambung,
 * atau anak-anak ingin mengetuk tombol langsung.
 * Token: "A".."Z", "0".."9", ".", ",", "?", "!", "-", "'", "SPASI", "HAPUS", "ENTER".
 */
class KeyboardLayar(ctx: Context, private val onTekan: (String) -> Unit) : LinearLayout(ctx) {

    private val tombol = HashMap<String, TextView>()
    private val handler = Handler(Looper.getMainLooper())
    private val barisView = ArrayList<LinearLayout>()
    private var tinggiBaris = 46

    init {
        orientation = VERTICAL
        setPadding(ctx.dp(2), ctx.dp(6), ctx.dp(2), ctx.dp(4))
        tambahBaris(listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0").map { Triple(it, it, 1f) })
        tambahBaris("QWERTYUIOP".map { Triple(it.toString(), it.toString(), 1f) })
        tambahBaris("ASDFGHJKL".map { Triple(it.toString(), it.toString(), 1f) })
        tambahBaris("ZXCVBNM".map { Triple(it.toString(), it.toString(), 1f) })
        tambahBaris(listOf(Triple("Hapus", "HAPUS", 1.6f), Triple("Spasi", "SPASI", 4f), Triple("Enter", "ENTER", 2f)))
        tambahBaris(listOf(".", ",", "?", "!", "-", "'").map { Triple(it, it, 1f) })
    }

    private fun tambahBaris(daftar: List<Triple<String, String, Float>>) {
        val b = LinearLayout(context)
        b.orientation = HORIZONTAL
        for ((label, token, bobot) in daftar) {
            val t = buatTombol(label, token)
            val p = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, bobot)
            p.setMargins(context.dp(3), context.dp(3), context.dp(3), context.dp(3))
            b.addView(t, p)
        }
        barisView.add(b)
        addView(b, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, context.dp(tinggiBaris)))
    }

    private fun buatTombol(label: String, token: String): TextView {
        val t = TextView(context)
        t.text = label
        t.textSize = 14f
        t.setTextColor(Tema.teks)
        t.gravity = Gravity.CENTER
        t.typeface = Typeface.DEFAULT_BOLD
        t.isFocusable = false
        t.isFocusableInTouchMode = false
        t.background = riak(context, Tema.tombol, 10)
        t.setOnClickListener {
            onTekan(token)
            sorotToken(token)
        }
        tombol[token] = t
        return t
    }

    private fun tokenDari(c: Char): String = if (c == ' ') "SPASI" else c.uppercaseChar().toString()

    private fun sorotToken(token: String) {
        val t = tombol[token] ?: return
        t.background = riak(context, Tema.sorot, 10)
        handler.postDelayed({ t.background = riak(context, Tema.tombol, 10) }, 220L)
    }

    /** Landscape: tuts lebih pendek agar seluruh keyboard tetap terlihat. */
    fun aturKompak(landscape: Boolean) {
        val tinggi = if (landscape) 34 else 46
        if (tinggi == tinggiBaris) return
        tinggiBaris = tinggi
        for (b in barisView) {
            b.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, context.dp(tinggi))
        }
        requestLayout()
    }

    /** Kilatan saat tombol ditekan (keyboard fisik maupun layar). */
    fun sorot(c: Char) {
        sorotToken(tokenDari(c))
    }

    /** Menandai tombol yang seharusnya ditekan berikutnya (kuning). */
    fun tandai(c: Char?) {
        for ((_, v) in tombol) v.background = riak(context, Tema.tombol, 10)
        if (c == null) return
        tombol[tokenDari(c)]?.background = riak(context, Tema.hint, 10)
    }
}
