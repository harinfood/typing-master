package com.contoh.typingmaster

import android.app.Activity
import android.content.res.Configuration
import android.view.KeyEvent

/**
 * Activity dengan input keyboard fisik (OTG) dan keyboard di layar.
 * Turunan cukup menimpa tekanHuruf / tekanSpasi / tekanHapus / tekanEnter.
 */
abstract class BaseKeyboardActivity : Activity() {

    protected var layarKeyboard: KeyboardLayar? = null

    protected open fun tekanHuruf(c: Char) {}
    protected open fun tekanSpasi() {}
    protected open fun tekanHapus() {}
    protected open fun tekanEnter() {}

    protected fun pasangKeyboardLayar(layar: KeyboardLayar) {
        layarKeyboard = layar
    }

    /** Input dari keyboard di layar. */
    protected fun tekanDariLayar(token: String) {
        when (token) {
            "SPASI" -> tekanSpasi()
            "HAPUS" -> tekanHapus()
            "ENTER" -> tekanEnter()
            else -> {
                val c = token[0]
                tekanHuruf(if (c.isLetter()) c.lowercaseChar() else c)
            }
        }
    }

    protected fun sorot(c: Char) {
        layarKeyboard?.sorot(c)
    }

    protected fun isLandscape(): Boolean =
        resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    /** Dipanggil saat layar dibuat dan saat orientasi berubah. Turunan menyesuaikan tata letaknya. */
    protected open fun onOrientasi(landscape: Boolean) {
        layarKeyboard?.aturKompak(landscape)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        onOrientasi(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
    }

    /**
     * Spasi dan Tab ditangani di sini (sebelum komponen tampilan dan perilaku bawaan Android),
     * sehingga tidak memicu geser halaman, fokus, atau tombol Back. Kedua aksi (tekan dan lepas)
     * dikonsumsi, hanya aksi tekan yang diproses.
     */
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        when (event.keyCode) {
            KeyEvent.KEYCODE_SPACE, KeyEvent.KEYCODE_TAB,
            KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                // Enter tidak pernah diteruskan ke sistem, jadi tidak memicu tombol Back
                if (event.action == KeyEvent.ACTION_DOWN) onKeyDown(event.keyCode, event)
                return true
            }
            KeyEvent.KEYCODE_ESCAPE -> {
                // Esc adalah tombol fisik untuk kembali
                if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) onEsc()
                return true
            }
        }
        return super.dispatchKeyEvent(event)
    }

    /** Tombol Esc: kembali ke layar sebelumnya. */
    protected open fun onEsc() {
        onBackPressed()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (event.repeatCount > 0 && keyCode != KeyEvent.KEYCODE_DEL) return true
        when (keyCode) {
            KeyEvent.KEYCODE_DEL, KeyEvent.KEYCODE_FORWARD_DEL -> {
                tekanHapus()
                return true
            }
            KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                tekanEnter()
                return true
            }
            KeyEvent.KEYCODE_SPACE -> {
                sorot(' ')
                tekanSpasi()
                return true
            }
            KeyEvent.KEYCODE_TAB -> return true
        }
        val u = event.unicodeChar
        if (u > 0 && !Character.isISOControl(u)) {
            val c = u.toChar()
            sorot(c)
            if (c == ' ') tekanSpasi() else tekanHuruf(c)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}
