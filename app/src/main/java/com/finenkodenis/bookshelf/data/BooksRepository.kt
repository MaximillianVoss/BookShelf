package com.finenkodenis.bookshelf.data

import com.example.bookshelf.Items
import com.finenkodenis.bookshelf.network.model.BookService
import com.finenkodenis.bookshelf.network.model.OpenLibraryDoc
import com.finenkodenis.bookshelf.network.model.OpenLibraryService
import kotlin.math.max
import kotlin.math.min

interface BooksRepository {
    suspend fun getBooks(
        query: String,
        maxResults: Int,
        source: BookSearchSource = BookSearchSource.ALL
    ) : List<Book>
}

class NetworkBooksRepository(
    private val bookService: BookService,
    private val openLibraryService: OpenLibraryService
) : BooksRepository {
    override suspend fun getBooks(
        query: String,
        maxResults: Int,
        source: BookSearchSource
    ): List<Book> {
        val normalizedQuery = query.trim().ifBlank { "book" }
        val remoteBooks = when (source) {
            BookSearchSource.ALL -> getMergedRemoteBooks(normalizedQuery, maxResults)
            BookSearchSource.GOOGLE -> getGoogleBooks(normalizedQuery, min(max(maxResults, 1), 40))
            BookSearchSource.OPEN_LIBRARY -> getOpenLibraryBooks(normalizedQuery, min(max(maxResults, 1), 50))
            BookSearchSource.LOCAL -> emptyList()
        }

        if (source == BookSearchSource.LOCAL) {
            return fallbackBooksForQuery(normalizedQuery, maxResults)
        }

        return remoteBooks.ifEmpty {
            if (source == BookSearchSource.ALL) fallbackBooksForQuery(normalizedQuery, maxResults) else emptyList()
        }
    }

    private suspend fun getMergedRemoteBooks(query: String, maxResults: Int): List<Book> {
        val googleLimit = min(max(maxResults / 2, 1), 40)
        val openLibraryLimit = min(max(maxResults - googleLimit, 1), 50)
        val googleBooks = getGoogleBooks(query, googleLimit)
        val openLibraryBooks = getOpenLibraryBooks(query, openLibraryLimit)

        return (googleBooks + openLibraryBooks)
            .distinctBy { it.externalId ?: "${it.source}:${it.title.lowercase()}" }
            .take(maxResults)
    }

    private suspend fun getGoogleBooks(query: String, limit: Int): List<Book> {
        return runCatching {
            bookService.bookSearch(query, limit).items.map { it.toBook() }
        }.getOrDefault(emptyList())
    }

    private suspend fun getOpenLibraryBooks(query: String, limit: Int): List<Book> {
        val openLibraryQuery = query.removePrefix("subject:").trim().ifBlank { query }
        return runCatching {
            openLibraryService.searchBooks(openLibraryQuery, limit).docs.map { it.toBook() }
        }.getOrDefault(emptyList())
    }

    private fun Items.toBook(): Book {
        val info = volumeInfo
        return Book(
            externalId = id,
            source = GOOGLE_BOOKS_SOURCE,
            title = info?.title?.takeIf { it.isNotBlank() } ?: "Без названия",
            authors = info?.authors.orEmpty(),
            description = info?.description,
            categories = info?.categories.orEmpty(),
            publishedDate = info?.publishedDate,
            pageCount = info?.pageCount,
            language = info?.language,
            previewLink = info?.previewLink,
            imageLink = info?.imageLinks?.thumbnail
        )
    }

    private fun OpenLibraryDoc.toBook(): Book {
        val coverUrl = coverId?.let { "https://covers.openlibrary.org/b/id/$it-M.jpg" }
        return Book(
            externalId = key,
            source = OPEN_LIBRARY_SOURCE,
            title = title?.takeIf { it.isNotBlank() } ?: "Без названия",
            authors = authors.orEmpty(),
            description = firstSentenceText,
            categories = subjects.orEmpty().take(8),
            publishedDate = firstPublishYear?.toString(),
            pageCount = null,
            language = languages.orEmpty().firstOrNull(),
            previewLink = key?.let { "https://openlibrary.org$it" },
            imageLink = coverUrl
        )
    }
}
