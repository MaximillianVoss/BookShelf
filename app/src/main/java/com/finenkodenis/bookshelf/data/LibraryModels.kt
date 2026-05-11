package com.finenkodenis.bookshelf.data

import com.finenkodenis.bookshelf.data.local.ReadingStatus

data class LibraryBook(
    val userBookId: Long,
    val userId: Long,
    val book: Book,
    val status: ReadingStatus,
    val rating: Int?,
    val review: String?,
    val addedAt: Long,
    val startedAt: Long?,
    val finishedAt: Long?
)

data class ReadingDay(
    val date: String,
    val totalMinutes: Int,
    val totalPages: Int
)

data class LibraryStats(
    val totalBooks: Int = 0,
    val statusCounts: Map<ReadingStatus, Int> = emptyMap(),
    val readBooks: Int = 0,
    val readingBooks: Int = 0,
    val wantToReadBooks: Int = 0,
    val averageRating: Double? = null,
    val totalMinutes: Int = 0,
    val totalPages: Int = 0,
    val readingDays: List<ReadingDay> = emptyList(),
    val topGenres: List<GenreStat> = emptyList()
)

data class GenreStat(
    val genre: String,
    val count: Int
)
