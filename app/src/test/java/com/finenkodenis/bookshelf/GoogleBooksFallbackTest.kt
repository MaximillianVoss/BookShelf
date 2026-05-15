package com.finenkodenis.bookshelf

import com.finenkodenis.bookshelf.data.Book
import com.finenkodenis.bookshelf.data.BookSearchSource
import com.finenkodenis.bookshelf.data.BooksRepository
import com.finenkodenis.bookshelf.ui.theme.BooksUiState
import com.finenkodenis.bookshelf.ui.theme.loadGoogleBooksFallbackState
import com.finenkodenis.bookshelf.ui.theme.shouldFallbackToOpenLibraryAfterGoogleError
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GoogleBooksFallbackTest {

    @Test
    fun fallbackEnabledForGoogleQuotaAndTemporaryFailure() {
        assertTrue(shouldFallbackToOpenLibraryAfterGoogleError(BookSearchSource.GOOGLE, 429))
        assertTrue(shouldFallbackToOpenLibraryAfterGoogleError(BookSearchSource.GOOGLE, 503))
        assertTrue(shouldFallbackToOpenLibraryAfterGoogleError(BookSearchSource.GOOGLE))
    }

    @Test
    fun fallbackDisabledForOtherSourcesAndAccessErrors() {
        assertFalse(shouldFallbackToOpenLibraryAfterGoogleError(BookSearchSource.OPEN_LIBRARY, 503))
        assertFalse(shouldFallbackToOpenLibraryAfterGoogleError(BookSearchSource.LOCAL, 503))
        assertFalse(shouldFallbackToOpenLibraryAfterGoogleError(BookSearchSource.GOOGLE, 403))
    }

    @Test
    fun googleFallbackPrefersOpenLibraryApiBeforeHtmlParsers() = runTest {
        val repository = StaticBooksRepository(
            results = mapOf(
                BookSearchSource.OPEN_LIBRARY to listOf(Book(title = "Open Library API result")),
                BookSearchSource.HTML_PARSER to listOf(Book(title = "Open Library HTML result")),
                BookSearchSource.YANDEX_HTML to listOf(Book(title = "Yandex HTML result")),
                BookSearchSource.LOCAL to listOf(Book(title = "Local result"))
            )
        )

        val state = loadGoogleBooksFallbackState(
            booksRepository = repository,
            query = "war and peace",
            maxResults = 10,
            reason = "Google Books вернул HTTP 503."
        )

        val success = state as BooksUiState.Success
        assertEquals("Open Library API result", success.bookSearch.single().title)
        assertEquals(
            listOf(BookSearchSource.OPEN_LIBRARY),
            repository.requestedSources
        )
        assertEquals(
            "Google Books вернул HTTP 503. Результаты: Open Library API (резервный источник).",
            success.message
        )
    }

    @Test
    fun googleFallbackUsesHtmlParserWhenOpenLibraryApiIsEmpty() = runTest {
        val repository = StaticBooksRepository(
            results = mapOf(
                BookSearchSource.HTML_PARSER to listOf(Book(title = "Open Library HTML result")),
                BookSearchSource.YANDEX_HTML to listOf(Book(title = "Yandex HTML result")),
                BookSearchSource.LOCAL to listOf(Book(title = "Local result"))
            )
        )

        val state = loadGoogleBooksFallbackState(
            booksRepository = repository,
            query = "war and peace",
            maxResults = 10,
            reason = "Google Books вернул HTTP 503."
        )

        val success = state as BooksUiState.Success
        assertEquals("Open Library HTML result", success.bookSearch.single().title)
        assertEquals(
            listOf(BookSearchSource.OPEN_LIBRARY, BookSearchSource.HTML_PARSER),
            repository.requestedSources
        )
    }

    private class StaticBooksRepository(
        private val results: Map<BookSearchSource, List<Book>>
    ) : BooksRepository {
        val requestedSources = mutableListOf<BookSearchSource>()

        override suspend fun getBooks(
            query: String,
            maxResults: Int,
            source: BookSearchSource
        ): List<Book> {
            requestedSources += source
            return results[source].orEmpty()
        }
    }
}
