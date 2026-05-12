package com.finenkodenis.bookshelf

import com.finenkodenis.bookshelf.data.fallbackBooksForQuery
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FallbackBooksTest {
    @Test
    fun fallbackBooksForQuery_returnsMatchingTitle() {
        val books = fallbackBooksForQuery("war and peace", 10)

        assertEquals(listOf("War and Peace"), books.map { it.title })
    }

    @Test
    fun fallbackBooksForQuery_supportsSubjectGenreQueries() {
        val books = fallbackBooksForQuery("subject:fantasy", 10)

        assertTrue(books.any { it.title == "The Hobbit" })
        assertTrue(books.any { it.title.contains("Harry Potter") })
    }

    @Test
    fun fallbackBooksForQuery_returnsDefaultCatalogForUnknownQuery() {
        val books = fallbackBooksForQuery("unknown-query-value", 3)

        assertEquals(3, books.size)
    }
}
