package com.finenkodenis.bookshelf.data

import org.jsoup.Jsoup
import org.jsoup.nodes.Element

object GoogleBooksHtmlParser {
    fun parseSearchResults(html: String, limit: Int): List<Book> {
        if (html.isBlank() || limit <= 0 || html.isBlockedOrJsOnlyPage()) return emptyList()

        val document = Jsoup.parse(html, GOOGLE_BOOKS_BASE_URL)
        return document.select("a[href*='/books?id'], a[href*='books?id']")
            .mapNotNull { it.toBook() }
            .distinctBy { it.externalId ?: it.title.lowercase() }
            .take(limit)
    }

    private fun Element.toBook(): Book? {
        val href = attr("href")
        val bookId = href.toGoogleBookId() ?: return null
        val container = nearestResultContainer()
        val title = extractTitle(container).takeIf { it.isUsefulTitle() } ?: return null
        val imageLink = container.selectFirst("img[src]")
            ?.absUrl("src")
            ?.takeIf { it.isNotBlank() }
        val authors = container.extractAuthors()
        val description = container.extractDescription(title)
        val previewLink = absUrl("href").takeIf { it.isNotBlank() }

        return Book(
            externalId = bookId,
            source = GOOGLE_BOOKS_HTML_SOURCE,
            title = title,
            authors = authors,
            description = description,
            categories = emptyList(),
            publishedDate = null,
            pageCount = null,
            language = null,
            previewLink = previewLink,
            imageLink = imageLink
        )
    }

    private fun Element.nearestResultContainer(): Element {
        var current: Element = this
        repeat(5) {
            val parent = current.parent() ?: return current
            val parentLinks = parent.select("a[href*='/books?id'], a[href*='books?id']").size
            val textLength = parent.cleanText().length
            if (parentLinks <= 3 && textLength in 20..1200) {
                current = parent
            }
        }
        return current
    }

    private fun Element.extractTitle(container: Element): String {
        val rawTitle = selectFirst("h1, h2, h3, [role=heading]")
            ?.cleanText()
            ?.takeIf { it.isNotBlank() }
            ?: ownText().takeIf { it.isNotBlank() }
            ?: attr("title").takeIf { it.isNotBlank() }
            ?: selectFirst("img[alt]")?.attr("alt")?.takeIf { it.isNotBlank() }
            ?: container.selectFirst("h1, h2, h3, [role=heading], a[href*='/books?id'], a[href*='books?id']")
                ?.cleanText()
                ?.takeIf { it.isNotBlank() }
            ?: container.selectFirst("img[alt]")?.attr("alt")?.takeIf { it.isNotBlank() }
            ?: ""
        return rawTitle.replace(WHITESPACE_REGEX, " ").trim()
    }

    private fun Element.extractAuthors(): List<String> {
        val explicitAuthors = select("[class*=author], [class*=Author], [class*=byline], [class*=Byline]")
            .flatMap { it.cleanText().toAuthorParts() }
            .filter { it.isNotBlank() }
            .distinct()

        if (explicitAuthors.isNotEmpty()) return explicitAuthors

        val text = cleanText()
        val byMatch = BY_AUTHOR_REGEX.find(text)?.groupValues?.getOrNull(1).orEmpty()
        return byMatch.toAuthorParts().filter { it.isNotBlank() }.distinct()
    }

    private fun Element.extractDescription(title: String): String? {
        val explicitDescription = selectFirst("[class*=snippet], [class*=Snippet], [class*=description], [class*=Description]")
            ?.cleanText()
            ?.takeIf { it.isNotBlank() }

        if (!explicitDescription.isNullOrBlank()) return explicitDescription

        return cleanText()
            .removePrefix(title)
            .replace(WHITESPACE_REGEX, " ")
            .trim()
            .takeIf { it.length in 40..600 }
    }

    private fun String.toGoogleBookId(): String? {
        val normalized = replace("&amp;", "&")
        return GOOGLE_BOOK_ID_REGEX.find(normalized)
            ?.groupValues
            ?.getOrNull(1)
            ?.takeIf { it.isNotBlank() }
    }

    private fun String.toAuthorParts(): List<String> {
        return replace("Автор:", "")
            .replace("Authors:", "")
            .replace("Author:", "")
            .replace("By ", "")
            .split(",", ";", " и ", " and ")
            .map { it.trim() }
            .filter { it.isNotBlank() && it.length <= 80 }
    }

    private fun String.isBlockedOrJsOnlyPage(): Boolean {
        val marker = lowercase()
        return ("enablejs" in marker && "books?id" !in marker) ||
            "our systems have detected unusual traffic" in marker ||
            "www.google.com/images/errors/robot.png" in marker ||
            "прежде чем перейти к google поиску" in marker
    }

    private fun String.isUsefulTitle(): Boolean {
        val normalized = trim()
        return normalized.isNotBlank() &&
            normalized.length <= 180 &&
            !normalized.equals("Google Books", ignoreCase = true) &&
            !normalized.equals("Google Search", ignoreCase = true)
    }

    private fun Element.cleanText(): String = text().replace(WHITESPACE_REGEX, " ").trim()

    private const val GOOGLE_BOOKS_BASE_URL = "https://books.google.com/"
    private val WHITESPACE_REGEX = Regex("\\s+")
    private val GOOGLE_BOOK_ID_REGEX = Regex("[?&]id=([^&#]+)")
    private val BY_AUTHOR_REGEX = Regex("(?:By|Автор:)\\s+([^\\-\\n\\r]+)")
}
