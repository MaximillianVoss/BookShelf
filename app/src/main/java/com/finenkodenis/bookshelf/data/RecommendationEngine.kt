package com.finenkodenis.bookshelf.data

import com.finenkodenis.bookshelf.data.local.ReadingStatus

class RecommendationEngine {
    fun topGenres(library: List<LibraryBook>, limit: Int = 3): List<GenreStat> {
        return library
            .filter { it.status == ReadingStatus.READ }
            .flatMap { it.book.categories }
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
            .take(limit)
            .map { GenreStat(genre = it.key, count = it.value) }
    }

    fun recommendationQueries(topGenres: List<GenreStat>): List<String> {
        val genreQueries = topGenres.map { "subject:${it.genre}" }
        return genreQueries.ifEmpty {
            listOf("subject:fiction", "subject:science", "subject:history")
        }
    }

    fun filterAlreadyAdded(candidates: List<Book>, library: List<LibraryBook>): List<Book> {
        val existingExternalIds = library.mapNotNull { it.book.externalId }.toSet()
        val existingTitles = library.map { it.book.title.lowercase() }.toSet()

        return candidates
            .filter { candidate ->
                val notAddedByExternalId = candidate.externalId == null || candidate.externalId !in existingExternalIds
                val notAddedByTitle = candidate.title.lowercase() !in existingTitles
                notAddedByExternalId && notAddedByTitle
            }
            .distinctBy { it.externalId ?: it.title.lowercase() }
    }
}
