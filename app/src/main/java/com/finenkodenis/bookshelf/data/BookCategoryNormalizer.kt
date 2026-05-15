package com.finenkodenis.bookshelf.data

import java.util.Locale

object BookCategoryNormalizer {
    fun normalize(rawCategories: List<String>, limit: Int = MAX_CATEGORIES): List<String> {
        return rawCategories
            .flatMap { it.splitCategoryText() }
            .map { it.trimCategoryLabel() }
            .filter { it.isUsefulCategory() }
            .distinctBy { it.lowercase(Locale.ROOT) }
            .take(limit)
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
            .substringAfterLast(" -- ")
            .trim()
    }

    private fun String.isUsefulCategory(): Boolean {
        val normalized = lowercase(Locale.ROOT).trim().trimEnd('.')
        return isNotBlank() &&
            length <= MAX_CATEGORY_LENGTH &&
            HAS_LETTER_REGEX.containsMatchIn(this) &&
            HUMAN_READABLE_CATEGORY_REGEX.matches(this) &&
            !TECHNICAL_CATEGORY_REGEX.containsMatchIn(this) &&
            normalized !in IGNORED_CATEGORY_LABELS
    }

    private const val MAX_CATEGORIES = 8
    private const val MAX_CATEGORY_LENGTH = 80

    private val HAS_LETTER_REGEX = Regex("\\p{L}")
    private val HUMAN_READABLE_CATEGORY_REGEX = Regex("^[\\p{L}\\p{N}][\\p{L}\\p{N}\\s'’&/.-]*$")
    private val TECHNICAL_CATEGORY_REGEX = Regex(
        pattern = "(https?://|www\\.|[a-z0-9]+:[^\\s]+|=|\\d{4}-\\d{2}-\\d{2}|^[a-z0-9]+(-[a-z0-9]+){2,}$)",
        option = RegexOption.IGNORE_CASE
    )

    private val IGNORED_CATEGORY_LABELS = setOf(
        "catalog",
        "etc",
        "et cetera",
        "general collections",
        "open library staff picks",
        "open syllabus project",
        "misc",
        "miscellaneous"
    )
}
