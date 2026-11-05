package com.contoh.typingmaster

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

/** Daftar level latihan mengetik. */
class PilihLatihanActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(buatLayar())
    }

    private fun buatLayar(): View {
        val root = akarLatar()
        root.addView(barAtas("Latihan Mengetik") { finish() })

        val scroll = ScrollView(this)
        scroll.isFillViewport = true
        val daftar = linear(this)
        daftar.setPadding(0, 0, 0, dp(16))
        for ((i, lv) in Data.LEVEL.withIndex()) {
            daftar.addView(kartuLevel(i, lv), lp(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0f, 0, 0, 0, dp(10)))
        }
        scroll.addView(daftar)
        root.addView(scroll, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))
        return root
    }

    private fun kartuLevel(i: Int, lv: Level): View {
        val k = kartuTema(this)
        k.background = riak(this, Tema.kartu, 20, Tema.garis)
        k.addView(teks(this, "${i + 1}. ${lv.nama}", 17f, Tema.teks, true))
        k.addView(teks(this, lv.deskripsi, 13f, Tema.redup).apply { setPadding(0, dp(4), 0, dp(6)) })
        val skor: TextView = teks(
            this,
            "Terbaik: ${Pengaturan.bestWpm(this, i)} kpm   ${bintangTeks(Pengaturan.bestBintang(this, i))}",
            13f,
            Tema.kuning,
            true
        )
        k.addView(skor)
        k.setOnClickListener {
            startActivity(Intent(this, LatihanActivity::class.java).putExtra("level", i))
        }
        return k
    }
}
