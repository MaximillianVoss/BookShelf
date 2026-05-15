package com.finenkodenis.bookshelf

import com.finenkodenis.bookshelf.data.BookSearchSource
import org.junit.Assert.assertEquals
import org.junit.Test

class BookSearchSourceTest {
    @Test
    fun titles_areUserFacingSourceNames() {
        val titles = BookSearchSource.values().map { it.title }

        assertEquals(
            listOf("Все", "Google Books", "Open Library", "HTML-парсер", "Локальный каталог"),
            titles
        )
    }
}
