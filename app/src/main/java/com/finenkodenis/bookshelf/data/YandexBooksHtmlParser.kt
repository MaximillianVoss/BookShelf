package com.finenkodenis.bookshelf.data

import org.jsoup.Jsoup
import org.jsoup.nodes.Element

object YandexBooksHtmlParser {
    fun parseSearchResults(html: String, limit: Int): List<Book> {
        if (html.isBlank() || limit <= 0) return emptyList()

        val document = Jsoup.parse(html, YANDEX_BOOKS_BASE_URL)
        return document.select("[data-test-id=SNIPPET]")
            .mapNotNull { it.toBook() }
            .distinctBy { it.externalId ?: it.title.lowercase() }
            .take(limit)
    }

    private fun Element.toBook(): Book? {
        val titleLink = selectFirst("[data-test-id=SNIPPET_TITLE] a[href^=/books/]")
            ?: selectFirst("a[href^=/books/]")
            ?: return null
        val title = titleLink.cleanText().takeIf { it.isNotBlank() } ?: return null
        val href = titleLink.attr("href").substringBefore("?")
        val imageLink = selectFirst("[data-test-id=COVER] img[src], img[src]")
            ?.absUrl("src")
            ?.takeIf { it.isNotBlank() }
        val authors = select("[data-test-id=SNIPPET_AUTHORS]")
            .map { it.cleanText() }
            .filter { it.isNotBlank() && it != "и др." }
            .distinct()
        val badge = selectFirst("[data-test-id=SNIPPET_DETAILS_BADGE]")
            ?.cleanText()
            ?.takeIf { it.isNotBlank() }

        return Book(
            externalId = href,
            source = YANDEX_BOOKS_HTML_SOURCE,
            title = title,
            authors = authors,
            description = null,
            categories = listOfNotNull(badge).filter { it != "Книга" },
            publishedDate = null,
            pageCount = null,
            language = "ru",
            previewLink = titleLink.absUrl("href").takeIf { it.isNotBlank() },
            imageLink = imageLink
        )
    }

    private fun Element.cleanText(): String = text().replace(WHITESPACE_REGEX, " ").trim()

    private const val YANDEX_BOOKS_BASE_URL = "https://books.yandex.ru/"
    private val WHITESPACE_REGEX = Regex("\\s+")
}
