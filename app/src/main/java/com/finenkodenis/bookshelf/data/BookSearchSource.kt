package com.finenkodenis.bookshelf.data

enum class BookSearchSource(val title: String) {
    ALL("Все"),
    OPEN_LIBRARY("Open Library"),
    HTML_PARSER("Open Library HTML"),
    YANDEX_HTML("Яндекс Книги"),
    LOCAL("Локальный каталог")
}
