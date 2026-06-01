package com.finenkodenis.bookshelf.data

fun String.toBookSourceLabel(): String {
    return when (this) {
        OPEN_LIBRARY_SOURCE -> "Open Library"
        GUTENDEX_SOURCE -> "Gutendex"
        INTERNET_ARCHIVE_SOURCE -> "Internet Archive"
        LIBRARY_OF_CONGRESS_SOURCE -> "Library of Congress"
        LOCAL_SERVER_SOURCE -> "Наш сервер"
        MANUAL_SOURCE -> "Локальная запись"
        else -> this
    }
}
