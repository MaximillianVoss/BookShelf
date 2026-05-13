package com.finenkodenis.bookshelf.data

enum class BookSearchSource(val title: String) {
    ALL("Все"),
    GOOGLE("Google Books"),
    OPEN_LIBRARY("Open Library"),
    LOCAL("Локальный каталог")
}
