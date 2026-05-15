package com.finenkodenis.bookshelf.data

enum class BookSearchSource(val title: String) {
    ALL("Все"),
    GOOGLE("Google Books"),
    OPEN_LIBRARY("Open Library"),
    HTML_PARSER("HTML-парсер"),
    LOCAL("Локальный каталог")
}
