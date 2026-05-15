package com.finenkodenis.bookshelf.data

import com.finenkodenis.bookshelf.network.model.GoogleBooksHtmlService
import com.finenkodenis.bookshelf.network.model.HtmlBookSearchService
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
    private val openLibraryService: OpenLibraryService,
    private val htmlBookSearchService: HtmlBookSearchService? = null,
    private val googleBooksHtmlService: GoogleBooksHtmlService? = null
) : BooksRepository {
    override suspend fun getBooks(
        query: String,
        maxResults: Int,
        source: BookSearchSource
    ): List<Book> {
        val normalizedQuery = query.trim().ifBlank { "book" }
        val remoteBooks = when (source) {
            BookSearchSource.ALL -> getMergedRemoteBooks(normalizedQuery, maxResults)
            BookSearchSource.OPEN_LIBRARY -> getOpenLibraryBooks(normalizedQuery, min(max(maxResults, 1), 50))
            BookSearchSource.HTML_PARSER -> getHtmlParsedBooks(normalizedQuery, maxResults)
            BookSearchSource.GOOGLE_BOOKS_HTML -> getGoogleBooksHtmlBooks(normalizedQuery, maxResults)
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
        val openLibraryLimit = min(max(maxResults, 1), 50)
        val openLibraryBooks = runCatching { getOpenLibraryBooks(query, openLibraryLimit) }.getOrDefault(emptyList())
        val htmlBooks = if (openLibraryBooks.size < maxResults) {
            runCatching { getHtmlParsedBooks(query, maxResults) }.getOrDefault(emptyList())
        } else {
            emptyList()
        }
        val googleBooks = if (openLibraryBooks.size + htmlBooks.size < maxResults) {
            runCatching { getGoogleBooksHtmlBooks(query, maxResults) }.getOrDefault(emptyList())
        } else {
            emptyList()
        }

        return (openLibraryBooks + htmlBooks + googleBooks)
            .distinctBy { it.externalId ?: "${it.source}:${it.title.lowercase()}" }
            .take(maxResults)
    }

    private suspend fun getOpenLibraryBooks(query: String, limit: Int): List<Book> {
        val openLibraryQuery = query.removePrefix("subject:").trim().ifBlank { query }
        return openLibraryService.searchBooks(openLibraryQuery, limit).docs.map { it.toBook() }
    }

    private suspend fun getHtmlParsedBooks(query: String, limit: Int): List<Book> {
        val service = htmlBookSearchService ?: return emptyList()
        val htmlQuery = query.removePrefix("subject:").trim().ifBlank { query }
        val response = service.searchBooksHtml(htmlQuery)
        if (!response.isSuccessful) return emptyList()
        val html = response.body()?.string().orEmpty()
        return OpenLibraryHtmlParser.parseSearchResults(html, limit)
    }

    private suspend fun getGoogleBooksHtmlBooks(query: String, limit: Int): List<Book> {
        val service = googleBooksHtmlService ?: return emptyList()
        val googleBooksQuery = query.removePrefix("subject:").trim().ifBlank { query }
        val response = service.searchBooksHtml(googleBooksQuery)
        if (!response.isSuccessful) return emptyList()
        val html = response.body()?.string().orEmpty()
        return GoogleBooksHtmlParser.parseSearchResults(html, limit)
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
