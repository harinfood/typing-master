package com.contoh.typingmaster

import android.app.Activity
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
