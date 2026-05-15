package com.finenkodenis.bookshelf.data

import com.finenkodenis.bookshelf.data.local.ReadingStatus
import java.util.Locale

class RecommendationEngine {
    fun topGenres(library: List<LibraryBook>, limit: Int = 3): List<GenreStat> {
        return library
            .filter { it.status == ReadingStatus.READ }
            .toGenreStats(limit)
    }

    fun popularGenres(library: List<LibraryBook>, limit: Int = 3): List<GenreStat> {
        return library.toGenreStats(limit)
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

    private fun List<LibraryBook>.toGenreStats(limit: Int): List<GenreStat> {
        val counts = linkedMapOf<String, GenreStat>()

        forEach { libraryBook ->
            BookCategoryNormalizer.normalize(libraryBook.book.categories, limit = Int.MAX_VALUE)
                .distinctBy { it.lowercase(Locale.ROOT) }
                .forEach { genre ->
                    val key = genre.lowercase(Locale.ROOT)
                    val current = counts[key]
                    counts[key] = current?.copy(count = current.count + 1) ?: GenreStat(genre, 1)
                }
        }

        return counts.values
            .sortedWith(compareByDescending<GenreStat> { it.count }.thenBy { it.genre.lowercase(Locale.ROOT) })
            .take(limit)
    }
}
