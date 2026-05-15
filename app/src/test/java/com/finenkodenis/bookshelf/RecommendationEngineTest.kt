package com.finenkodenis.bookshelf

import com.finenkodenis.bookshelf.data.Book
import com.finenkodenis.bookshelf.data.GenreStat
import com.finenkodenis.bookshelf.data.LibraryBook
import com.finenkodenis.bookshelf.data.RecommendationEngine
import com.finenkodenis.bookshelf.data.local.ReadingStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecommendationEngineTest {
    private val engine = RecommendationEngine()

    @Test
    fun topGenres_usesOnlyReadBooksAndSortsByCount() {
        val library = listOf(
            libraryBook("Dune", ReadingStatus.READ, listOf("Science Fiction", "Adventure")),
            libraryBook("Foundation", ReadingStatus.READ, listOf("Science Fiction")),
            libraryBook("Clean Code", ReadingStatus.READING, listOf("Programming"))
        )

        val result = engine.topGenres(library)

        assertEquals("Science Fiction", result[0].genre)
        assertEquals(2, result[0].count)
        assertTrue(result.none { it.genre == "Programming" })
    }

    @Test
    fun popularGenres_includesAllLibraryStatusesAndFiltersTechnicalTags() {
        val library = listOf(
            libraryBook("Dune", ReadingStatus.READ, listOf("Science Fiction", "Adventure")),
            libraryBook("Foundation", ReadingStatus.WANT_TO_READ, listOf("science fiction")),
            libraryBook(
                title = "The Hobbit",
                status = ReadingStatus.READING,
                genres = listOf(
                    "Adventure, Fantasy",
                    "nyt:young-adult-paperback-monthly=2022-09-04",
                    "etc."
                )
            )
        )

        val result = engine.popularGenres(library, limit = 4)

        assertEquals(
            listOf(
                GenreStat("Adventure", 2),
                GenreStat("Science Fiction", 2),
                GenreStat("Fantasy", 1)
            ),
            result
        )
    }

    @Test
    fun filterAlreadyAdded_removesBooksByExternalIdAndTitle() {
        val library = listOf(
            libraryBook(
                title = "Dune",
                status = ReadingStatus.READ,
                genres = listOf("Science Fiction"),
                externalId = "google-1"
            )
        )
        val candidates = listOf(
            Book(externalId = "google-1", title = "Duplicate by id"),
            Book(externalId = "google-2", title = "Dune"),
            Book(externalId = "google-3", title = "Hyperion")
        )

        val result = engine.filterAlreadyAdded(candidates, library)

        assertEquals(listOf("Hyperion"), result.map { it.title })
    }

    @Test
    fun recommendationQueries_returnsFallbackWhenGenresAreEmpty() {
        val result = engine.recommendationQueries(emptyList())

        assertEquals(listOf("subject:fiction", "subject:science", "subject:history"), result)
    }

    private fun libraryBook(
        title: String,
        status: ReadingStatus,
        genres: List<String>,
        externalId: String = title
    ): LibraryBook {
        return LibraryBook(
            userBookId = externalId.hashCode().toLong(),
            userId = 1,
            book = Book(
                externalId = externalId,
                title = title,
                categories = genres
            ),
            status = status,
            rating = null,
            review = null,
            addedAt = 0,
            startedAt = null,
            finishedAt = null
        )
    }
}
