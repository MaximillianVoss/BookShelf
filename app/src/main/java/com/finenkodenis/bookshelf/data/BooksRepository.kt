package com.finenkodenis.bookshelf.data

import com.example.bookshelf.Items
import com.finenkodenis.bookshelf.network.model.BookService
import com.finenkodenis.bookshelf.network.model.OpenLibraryDoc
import com.finenkodenis.bookshelf.network.model.OpenLibraryService
import kotlin.math.max
import kotlin.math.min

interface BooksRepository {
    suspend fun getBooks(query: String, maxResults: Int) : List<Book>
}

class NetworkBooksRepository(
    private val bookService: BookService,
    private val openLibraryService: OpenLibraryService
) : BooksRepository {
    override suspend fun getBooks(
        query: String,
        maxResults: Int
    ): List<Book> {
        val normalizedQuery = query.trim().ifBlank { "book" }
        val openLibraryQuery = normalizedQuery.removePrefix("subject:").trim().ifBlank { normalizedQuery }
        val googleLimit = min(max(maxResults / 2, 1), 40)
        val openLibraryLimit = min(max(maxResults - googleLimit, 1), 50)

        val googleBooks = runCatching {
            bookService.bookSearch(normalizedQuery, googleLimit).items.map { it.toBook() }
        }.getOrDefault(emptyList())

        val openLibraryBooks = runCatching {
            openLibraryService.searchBooks(openLibraryQuery, openLibraryLimit).docs.map { it.toBook() }
        }.getOrDefault(emptyList())

        val remoteBooks = (googleBooks + openLibraryBooks)
            .distinctBy { it.externalId ?: "${it.source}:${it.title.lowercase()}" }
            .take(maxResults)

        return remoteBooks.ifEmpty {
            fallbackBooksForQuery(normalizedQuery, maxResults)
        }
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
