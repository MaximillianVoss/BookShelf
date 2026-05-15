package com.finenkodenis.bookshelf.data

import com.finenkodenis.bookshelf.network.model.GutendexBook
import com.finenkodenis.bookshelf.network.model.GutendexService
import com.finenkodenis.bookshelf.network.model.InternetArchiveDoc
import com.finenkodenis.bookshelf.network.model.InternetArchiveService
import com.finenkodenis.bookshelf.network.model.LibraryOfCongressItem
import com.finenkodenis.bookshelf.network.model.LibraryOfCongressService
import com.finenkodenis.bookshelf.network.model.OpenLibraryDoc
import com.finenkodenis.bookshelf.network.model.OpenLibraryService
import com.google.gson.JsonElement
import kotlin.math.ceil
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
    private val gutendexService: GutendexService,
    private val internetArchiveService: InternetArchiveService,
    private val libraryOfCongressService: LibraryOfCongressService
) : BooksRepository {
    override suspend fun getBooks(
        query: String,
        maxResults: Int,
        source: BookSearchSource
    ): List<Book> {
        val normalizedQuery = query.trim().ifBlank { "book" }
        val safeLimit = max(maxResults, 1)
        val remoteBooks = when (source) {
            BookSearchSource.ALL -> getMergedRemoteBooks(normalizedQuery, safeLimit)
            BookSearchSource.OPEN_LIBRARY -> getOpenLibraryBooks(normalizedQuery, min(safeLimit, 50))
            BookSearchSource.GUTENDEX -> getGutendexBooks(normalizedQuery, safeLimit)
            BookSearchSource.INTERNET_ARCHIVE -> getInternetArchiveBooks(normalizedQuery, safeLimit)
            BookSearchSource.LIBRARY_OF_CONGRESS -> getLibraryOfCongressBooks(normalizedQuery, safeLimit)
            BookSearchSource.LOCAL -> emptyList()
        }

        if (source == BookSearchSource.LOCAL) {
            return fallbackBooksForQuery(normalizedQuery, safeLimit)
        }

        return remoteBooks.withRequestedCategory(normalizedQuery).ifEmpty {
            if (source == BookSearchSource.ALL) fallbackBooksForQuery(normalizedQuery, safeLimit) else emptyList()
        }
    }

    private suspend fun getMergedRemoteBooks(query: String, maxResults: Int): List<Book> {
        val sourceLimit = min(max(ceil(maxResults / REMOTE_SOURCE_COUNT.toDouble()).toInt(), MIN_RESULTS_PER_SOURCE), maxResults)
        val openLibraryBooks = runCatching { getOpenLibraryBooks(query, min(sourceLimit, 50)) }.getOrDefault(emptyList())
        val gutendexBooks = runCatching { getGutendexBooks(query, sourceLimit) }.getOrDefault(emptyList())
        val internetArchiveBooks = runCatching { getInternetArchiveBooks(query, sourceLimit) }.getOrDefault(emptyList())
        val libraryOfCongressBooks = runCatching { getLibraryOfCongressBooks(query, sourceLimit) }.getOrDefault(emptyList())

        return (openLibraryBooks + gutendexBooks + internetArchiveBooks + libraryOfCongressBooks)
            .distinctBy { it.deduplicationKey() }
            .take(maxResults)
    }

    private suspend fun getOpenLibraryBooks(query: String, limit: Int): List<Book> {
        val openLibraryQuery = query.toPlainBookQuery()
        return openLibraryService.searchBooks(openLibraryQuery, limit).docs.map { it.toBook() }
    }

    private suspend fun getGutendexBooks(query: String, limit: Int): List<Book> {
        val gutendexQuery = query.toPlainBookQuery()
        return gutendexService.searchBooks(gutendexQuery).results
            .mapNotNull { it.toBook() }
            .take(limit)
    }

    private suspend fun getInternetArchiveBooks(query: String, limit: Int): List<Book> {
        return internetArchiveService.searchBooks(
            query = query.toInternetArchiveQuery(),
            rows = min(limit, 50)
        ).response.docs.mapNotNull { it.toBook() }
    }

    private suspend fun getLibraryOfCongressBooks(query: String, limit: Int): List<Book> {
        return libraryOfCongressService.searchBooks(
            query = query.toPlainBookQuery(),
            count = min(limit, 50)
        ).results.mapNotNull { it.toBook() }
    }

    private fun OpenLibraryDoc.toBook(): Book {
        val coverUrl = coverId?.let { "https://covers.openlibrary.org/b/id/$it-M.jpg" }
        return Book(
            externalId = key,
            source = OPEN_LIBRARY_SOURCE,
            title = title?.takeIf { it.isNotBlank() } ?: "Без названия",
            authors = authors.orEmpty(),
            description = firstSentenceText,
            categories = subjects.orEmpty().toBookCategories(),
            publishedDate = firstPublishYear?.toString(),
            pageCount = null,
            language = languages.orEmpty().firstOrNull(),
            previewLink = key?.let { "https://openlibrary.org$it" },
            imageLink = coverUrl
        )
    }

    private fun GutendexBook.toBook(): Book? {
        val bookId = id ?: return null
        val htmlUrl = formats.firstValueForPrefix("text/html")
        val textUrl = formats.firstValueForPrefix("text/plain")
        return Book(
            externalId = bookId.toString(),
            source = GUTENDEX_SOURCE,
            title = title?.takeIf { it.isNotBlank() } ?: return null,
            authors = authors.mapNotNull { it.name?.takeIf(String::isNotBlank) },
            description = summaries.firstOrNull(),
            categories = (subjects + bookshelves).toBookCategories(),
            publishedDate = null,
            pageCount = null,
            language = languages.firstOrNull(),
            previewLink = htmlUrl ?: textUrl ?: "https://www.gutenberg.org/ebooks/$bookId",
            imageLink = formats["image/jpeg"]
        )
    }

    private fun InternetArchiveDoc.toBook(): Book? {
        val archiveId = identifier?.takeIf { it.isNotBlank() } ?: return null
        return Book(
            externalId = archiveId,
            source = INTERNET_ARCHIVE_SOURCE,
            title = title.asTextList().firstOrNull()?.takeIf { it.isNotBlank() } ?: return null,
            authors = creator.asTextList(),
            description = description.asTextList().firstOrNull(),
            categories = subject.asTextList().toBookCategories(),
            publishedDate = date.asTextList().firstOrNull(),
            pageCount = null,
            language = language.asTextList().firstOrNull(),
            previewLink = "https://archive.org/details/$archiveId",
            imageLink = "https://archive.org/services/img/$archiveId"
        )
    }

    private fun LibraryOfCongressItem.toBook(): Book? {
        val titleValue = title?.takeIf { it.isNotBlank() } ?: return null
        val itemUrl = url?.takeIf { it.isNotBlank() }
        return Book(
            externalId = itemUrl ?: titleValue,
            source = LIBRARY_OF_CONGRESS_SOURCE,
            title = titleValue,
            authors = (creator.asTextList() + contributor.asTextList()).distinct(),
            description = description.asTextList().firstOrNull(),
            categories = (subject.asTextList() + genre.asTextList()).toBookCategories(),
            publishedDate = date,
            pageCount = null,
            language = language.asTextList().firstOrNull(),
            previewLink = itemUrl,
            imageLink = imageUrl.firstOrNull()
        )
    }

    private fun String.toPlainBookQuery(): String {
        return removePrefix("subject:").trim().ifBlank { this }
    }

    private fun List<Book>.withRequestedCategory(query: String): List<Book> {
        val requestedCategory = query.toRequestedCategory() ?: return this
        return map { book ->
            val mergedCategories = (listOf(requestedCategory) + book.categories)
                .toBookCategories()
            book.copy(categories = mergedCategories)
        }
    }

    private fun String.toRequestedCategory(): String? {
        val trimmedQuery = trim()
        if (!trimmedQuery.startsWith("subject:", ignoreCase = true)) return null

        return trimmedQuery
            .substringAfter(":")
            .trim()
            .takeIf { it.isNotBlank() }
            ?.toDisplayCategory()
    }

    private fun String.toDisplayCategory(): String {
        return split(Regex("\\s+"))
            .joinToString(" ") { part ->
                part.replaceFirstChar { char ->
                    if (char.isLowerCase()) char.titlecase() else char.toString()
                }
            }
    }

    private fun String.toInternetArchiveQuery(): String {
        val plainQuery = toPlainBookQuery().replace("\"", "\\\"")
        return "mediatype:texts AND (title:\"$plainQuery\" OR creator:\"$plainQuery\" OR subject:\"$plainQuery\")"
    }

    private fun JsonElement?.asTextList(): List<String> {
        if (this == null || isJsonNull) return emptyList()
        return when {
            isJsonPrimitive -> listOf(asString)
            isJsonArray -> asJsonArray.mapNotNull { it.asTextList().firstOrNull() }
            isJsonObject -> asJsonObject.entrySet().mapNotNull { it.value.asTextList().firstOrNull() }
            else -> emptyList()
        }.map { it.trim() }.filter { it.isNotBlank() }.distinct()
    }

    private fun List<String>.toBookCategories(): List<String> {
        return flatMap { it.splitCategoryText() }
            .map { it.trimCategoryLabel() }
            .filter { it.isUsefulCategory() }
            .distinctBy { it.lowercase() }
            .take(MAX_CATEGORIES)
    }

    private fun String.splitCategoryText(): List<String> {
        return split(",", ";")
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }

    private fun String.trimCategoryLabel(): String {
        return removePrefix("Category:")
            .replace("_", " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun String.isUsefulCategory(): Boolean {
        val normalized = lowercase()
        return isNotBlank() &&
            length <= MAX_CATEGORY_LENGTH &&
            normalized !in IGNORED_CATEGORY_LABELS
    }

    private fun Map<String, String>.firstValueForPrefix(prefix: String): String? {
        return entries.firstOrNull { it.key.startsWith(prefix) }?.value?.takeIf { it.isNotBlank() }
    }

    private fun Book.deduplicationKey(): String {
        val normalizedTitle = title.lowercase().trim()
        val normalizedAuthor = authors.firstOrNull().orEmpty().lowercase().trim()
        return "$normalizedTitle|$normalizedAuthor"
    }

    private companion object {
        const val REMOTE_SOURCE_COUNT = 4
        const val MIN_RESULTS_PER_SOURCE = 6
        const val MAX_CATEGORIES = 8
        const val MAX_CATEGORY_LENGTH = 80

        val IGNORED_CATEGORY_LABELS = setOf(
            "catalog",
            "general collections",
            "open library staff picks",
            "open syllabus project"
        )
    }
}
