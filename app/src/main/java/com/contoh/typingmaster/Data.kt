package com.contoh.typingmaster

data class Level(val nama: String, val deskripsi: String, val jenis: String, val isi: List<String>)

data class Huruf(val huruf: Char, val kata: String, val emoji: String)

object Data {

    private val KATA = listOf(
        "makan", "minum", "rumah", "sekolah", "buku", "pensil", "meja", "kursi",
        "ibu", "ayah", "adik", "kakak", "bola", "ikan", "kucing", "bunga",
        "pagi", "malam", "nasi", "sayur", "buah", "jalan", "mobil", "sepeda",
        "langit", "laut", "gunung", "sungai", "hujan", "angin", "matahari", "bulan",
        "senang", "sehat", "belajar", "membaca", "menulis", "bermain", "teman", "sahabat"
    )

    private val KALIMAT = listOf(
        "Saya suka makan nasi goreng.",
        "Adik pergi ke sekolah setiap pagi.",
        "Ayah membaca koran di teras rumah.",
        "Ibu memasak sayur bayam hari ini.",
        "Kucing itu tidur di atas kursi.",
        "Mari belajar mengetik dengan rajin!",
        "Matahari terbit di ufuk timur.",
        "Kami bermain bola di lapangan."
    )

    val LEVEL = listOf(
        Level("Baris Tengah", "Latihan jari di baris tengah: A S D F G H J K L", "huruf", listOf("asdfghjkl")),
        Level("Baris Atas", "Latihan jari di baris atas: Q W E R T Y U I O P", "huruf", listOf("qwertyuiop")),
        Level("Baris Bawah", "Latihan jari di baris bawah: Z X C V B N M", "huruf", listOf("zxcvbnm")),
        Level("Semua Huruf", "Campuran seluruh huruf dari A sampai Z", "huruf", listOf("abcdefghijklmnopqrstuvwxyz")),
        Level("Kata Sederhana", "Ketik kata-kata yang sering dipakai sehari-hari", "kata", KATA),
        Level("Kalimat", "Ketik kalimat lengkap dengan huruf besar dan tanda baca", "kalimat", KALIMAT)
    )

    val HURUF = listOf(
        Huruf('A', "Apel", "🍎"), Huruf('B', "Bola", "⚽"), Huruf('C', "Cicak", "🦎"),
        Huruf('D', "Domba", "🐑"), Huruf('E', "Elang", "🦅"), Huruf('F', "Ikan", "🐟"),
        Huruf('G', "Gajah", "🐘"), Huruf('H', "Hujan", "🌧"), Huruf('I', "Itik", "🦆"),
        Huruf('J', "Jeruk", "🍊"), Huruf('K', "Kucing", "🐱"), Huruf('L', "Lampu", "💡"),
        Huruf('M', "Mobil", "🚗"), Huruf('N', "Nanas", "🍍"), Huruf('O', "Ombak", "🌊"),
        Huruf('P', "Pisang", "🍌"), Huruf('Q', "Quiz", "❓"), Huruf('R', "Rumah", "🏠"),
        Huruf('S', "Sapi", "🐄"), Huruf('T', "Topi", "🧢"), Huruf('U', "Ular", "🐍"),
        Huruf('V', "Van", "🚐"), Huruf('W', "Wortel", "🥕"), Huruf('X', "Xilofon", "🎶"),
        Huruf('Y', "Yoyo", "🪀"), Huruf('Z', "Zebra", "🦓")
    )

    /** Teks latihan yang dibuat acak setiap kali dibuka. */
    fun teksLatihan(l: Level): String = when (l.jenis) {
        "huruf" -> {
            val pool = l.isi[0]
            (1..8).joinToString(" ") { (1..4).map { pool.random() }.joinToString("") }
        }
        "kata" -> KATA.shuffled().take(10).joinToString(" ")
        else -> KALIMAT.shuffled().take(2).joinToString(" ")
    }
}
