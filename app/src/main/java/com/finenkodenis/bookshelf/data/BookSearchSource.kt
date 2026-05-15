package com.finenkodenis.bookshelf.data

enum class BookSearchSource(val title: String) {
    ALL("Все"),
    OPEN_LIBRARY("Open Library"),
    HTML_PARSER("Open Library HTML"),
    GOOGLE_BOOKS_HTML("Google Books HTML"),
    LOCAL("Локальный каталог")
}
