package com.finenkodenis.bookshelf.data

enum class BookSearchSource(val title: String) {
    ALL("Все"),
    OPEN_LIBRARY("Open Library"),
    GUTENDEX("Gutendex"),
    INTERNET_ARCHIVE("Internet Archive"),
    LIBRARY_OF_CONGRESS("Library of Congress"),
    LOCAL_SERVER("Наш сервер"),
    LOCAL("Локальный каталог")
}
