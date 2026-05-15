package com.finenkodenis.bookshelf.data

import org.jsoup.Jsoup
import org.jsoup.nodes.Element

object OpenLibraryHtmlParser {
    fun parseSearchResults(html: String, limit: Int): List<Book> {
        if (html.isBlank() || limit <= 0) return emptyList()

        val document = Jsoup.parse(html, OPEN_LIBRARY_BASE_URL)
        return document.select("li.searchResultItem")
            .mapNotNull { it.toBook() }
            .distinctBy { it.externalId ?: it.title.lowercase() }
            .take(limit)
    }

    private fun Element.toBook(): Book? {
        val titleLink = selectFirst(".resultTitle h3.booktitle a.results, h3.booktitle a.results")
            ?: return null
        val title = titleLink.cleanText().takeIf { it.isNotBlank() } ?: return null
        val href = titleLink.attr("href")
        val workPath = href.toOpenLibraryWorkPath()
        val previewLink = titleLink.absUrl("href").takeIf { it.isNotBlank() }
        val imageLink = selectFirst(".bookcover img[itemprop=image], .bookcover img")
            ?.absUrl("src")
            ?.takeIf { it.isNotBlank() }
        val authors = select(".bookauthor a")
            .map { it.cleanText() }
            .filter { it.isNotBlank() }
            .distinct()
        val categories = select(".srw__subjects ol-chip")
            .map { it.cleanText() }
            .filter { it.isNotBlank() }
            .distinct()
            .take(8)

        return Book(
            externalId = workPath ?: previewLink,
            source = OPEN_LIBRARY_HTML_SOURCE,
            title = title,
            authors = authors,
            description = null,
            categories = categories,
            publishedDate = null,
            pageCount = null,
            language = null,
            previewLink = previewLink,
            imageLink = imageLink
        )
    }

    private fun Element.cleanText(): String = text().replace(WHITESPACE_REGEX, " ").trim()

    private fun String.toOpenLibraryWorkPath(): String? {
        val path = substringBefore("?")
        val parts = path.split("/").filter { it.isNotBlank() }
        return if (parts.size >= 2 && parts[0] == "works") "/works/${parts[1]}" else null
    }

    private const val OPEN_LIBRARY_BASE_URL = "https://openlibrary.org/"
    private val WHITESPACE_REGEX = Regex("\\s+")
}
