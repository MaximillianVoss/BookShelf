package com.finenkodenis.bookshelf.data.local

enum class ReadingStatus(val title: String) {
    WANT_TO_READ("Хочу прочитать"),
    READING("Читаю"),
    READ("Прочитано"),
    PAUSED("Отложено"),
    DROPPED("Брошено")
}
