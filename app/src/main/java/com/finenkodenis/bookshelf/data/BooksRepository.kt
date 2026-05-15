package com.finenkodenis.bookshelf.data

import com.example.bookshelf.Items
import com.finenkodenis.bookshelf.network.model.BookService
import com.finenkodenis.bookshelf.network.model.HtmlBookSearchService
import com.finenkodenis.bookshelf.network.model.OpenLibraryDoc
import com.finenkodenis.bookshelf.network.model.OpenLibraryService
import com.finenkodenis.bookshelf.network.model.YandexBooksHtmlService
import java.net.URLEncoder
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
    private val openLibraryService: OpenLibraryService,
    private val htmlBookSearchService: HtmlBookSearchService? = null,
    private val yandexBooksHtmlService: YandexBooksHtmlService? = null,
    private val googleBooksApiKey: String = ""
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
            BookSearchSource.HTML_PARSER -> getHtmlParsedBooks(normalizedQuery, maxResults)
            BookSearchSource.YANDEX_HTML -> getYandexHtmlBooks(normalizedQuery, maxResults)
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
        val googleBooks = runCatching { getGoogleBooks(query, googleLimit) }.getOrDefault(emptyList())
        val openLibraryBooks = runCatching { getOpenLibraryBooks(query, openLibraryLimit) }.getOrDefault(emptyList())
        val htmlBooks = if (googleBooks.size + openLibraryBooks.size < maxResults) {
            runCatching { getHtmlParsedBooks(query, maxResults) }.getOrDefault(emptyList())
        } else {
            emptyList()
        }
        val yandexBooks = if (googleBooks.size + openLibraryBooks.size + htmlBooks.size < maxResults) {
            runCatching { getYandexHtmlBooks(query, maxResults) }.getOrDefault(emptyList())
        } else {
            emptyList()
        }

        return (googleBooks + openLibraryBooks + htmlBooks + yandexBooks)
            .distinctBy { it.externalId ?: "${it.source}:${it.title.lowercase()}" }
            .take(maxResults)
    }

    private suspend fun getGoogleBooks(query: String, limit: Int): List<Book> {
        val apiKey = googleBooksApiKey.trim().takeIf { it.isNotBlank() }
        return bookService.bookSearch(query, limit, apiKey).items.map { it.toBook() }
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

    private suspend fun getYandexHtmlBooks(query: String, limit: Int): List<Book> {
        val service = yandexBooksHtmlService ?: return emptyList()
        val yandexQuery = query.removePrefix("subject:").trim().ifBlank { query }
        val encodedQuery = URLEncoder.encode(yandexQuery, "UTF-8").replace("+", "%20")
        val response = service.searchBooksHtml(encodedQuery)
        if (!response.isSuccessful) return emptyList()
        val html = response.body()?.string().orEmpty()
        return YandexBooksHtmlParser.parseSearchResults(html, limit)
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
