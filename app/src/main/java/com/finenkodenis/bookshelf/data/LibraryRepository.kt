package com.finenkodenis.bookshelf.data

import com.finenkodenis.bookshelf.data.local.BookDao
import com.finenkodenis.bookshelf.data.local.BookEntity
import com.finenkodenis.bookshelf.data.local.LibraryBookRow
import com.finenkodenis.bookshelf.data.local.ReadingDayRow
import com.finenkodenis.bookshelf.data.local.ReadingSessionDao
import com.finenkodenis.bookshelf.data.local.ReadingSessionEntity
import com.finenkodenis.bookshelf.data.local.ReadingStatus
import com.finenkodenis.bookshelf.data.local.UserBookDao
import com.finenkodenis.bookshelf.data.local.UserBookEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LibraryRepository(
    private val bookDao: BookDao,
    private val userBookDao: UserBookDao,
    private val readingSessionDao: ReadingSessionDao,
    private val recommendationEngine: RecommendationEngine = RecommendationEngine()
) {
    fun observeLibrary(userId: Long, status: ReadingStatus? = null): Flow<List<LibraryBook>> {
        return userBookDao.observeLibrary(userId, status?.name).map { rows ->
            rows.map { it.toLibraryBook() }
        }
    }

    fun observeLibraryBook(userBookId: Long): Flow<LibraryBook?> {
        return userBookDao.observeLibraryBook(userBookId).map { it?.toLibraryBook() }
    }

    fun observeStats(userId: Long): Flow<LibraryStats> {
        return combine(
            observeLibrary(userId),
            readingSessionDao.observeReadingDays(userId)
        ) { library, readingDays ->
            library.toStats(readingDays)
        }
    }

    suspend fun addOrUpdateBook(
        userId: Long,
        book: Book,
        status: ReadingStatus,
        rating: Int? = null,
        review: String? = null
    ): Long {
        val bookId = saveBook(book)
        val existing = userBookDao.getByUserAndBook(userId, bookId)
        val now = System.currentTimeMillis()

        return if (existing == null) {
            userBookDao.insert(
                UserBookEntity(
                    userId = userId,
                    bookId = bookId,
                    status = status,
                    rating = rating,
                    review = review,
                    startedAt = if (status == ReadingStatus.READING) now else null,
                    finishedAt = if (status == ReadingStatus.READ) now else null,
                    updatedAt = now
                )
            )
        } else {
            userBookDao.update(
                existing.copy(
                    status = status,
                    rating = rating,
                    review = review,
                    startedAt = existing.startedAt ?: if (status == ReadingStatus.READING) now else null,
                    finishedAt = if (status == ReadingStatus.READ) existing.finishedAt ?: now else existing.finishedAt,
                    updatedAt = now
                )
            )
            existing.userBookId
        }
    }

    suspend fun updateLibraryBook(
        userBookId: Long,
        status: ReadingStatus,
        rating: Int?,
        review: String?
    ) {
        val existing = userBookDao.getById(userBookId) ?: return
        val now = System.currentTimeMillis()
        userBookDao.update(
            existing.copy(
                status = status,
                rating = rating,
                review = review,
                startedAt = existing.startedAt ?: if (status == ReadingStatus.READING) now else null,
                finishedAt = if (status == ReadingStatus.READ) existing.finishedAt ?: now else existing.finishedAt,
                updatedAt = now
            )
        )
    }

    suspend fun deleteLibraryBook(userBookId: Long) {
        userBookDao.deleteById(userBookId)
    }

    suspend fun addReadingSession(
        userBookId: Long,
        minutesRead: Int,
        pagesRead: Int,
        note: String? = null,
        readDate: String = today()
    ) {
        readingSessionDao.insert(
            ReadingSessionEntity(
                userBookId = userBookId,
                readDate = readDate,
                minutesRead = minutesRead.coerceAtLeast(0),
                pagesRead = pagesRead.coerceAtLeast(0),
                note = note
            )
        )
    }

    suspend fun ensureBookForReading(userId: Long, book: Book): LibraryBook {
        val bookId = saveBook(book)
        val existing = userBookDao.getByUserAndBook(userId, bookId)
        val now = System.currentTimeMillis()
        val savedBook = book.copy(localId = bookId)

        if (existing == null) {
            val userBook = UserBookEntity(
                userId = userId,
                bookId = bookId,
                status = ReadingStatus.READING,
                startedAt = now,
                updatedAt = now
            )
            val userBookId = userBookDao.insert(userBook)
            return userBook.copy(userBookId = userBookId).toLibraryBook(savedBook)
        }

        val updated = if (existing.status == ReadingStatus.WANT_TO_READ) {
            val readingBook = existing.copy(
                status = ReadingStatus.READING,
                startedAt = existing.startedAt ?: now,
                updatedAt = now
            )
            userBookDao.update(
                readingBook
            )
            readingBook
        } else {
            existing
        }

        return updated.toLibraryBook(savedBook)
    }

    suspend fun seedDemoData(userId: Long) {
        val userBookIds = mutableMapOf<String, Long>()

        DemoLibrarySeed.books.forEach { seed ->
            val userBookId = addOrUpdateBook(
                userId = userId,
                book = seed.book,
                status = seed.status,
                rating = seed.rating,
                review = seed.review
            )
            seed.book.externalId?.let { userBookIds[it] = userBookId }
        }

        readingSessionDao.deleteByNote(userId, DemoLibrarySeed.SESSION_NOTE)

        DemoLibrarySeed.monthSessions().forEach { session ->
            val userBookId = userBookIds[session.bookExternalId] ?: return@forEach
            addReadingSession(
                userBookId = userBookId,
                minutesRead = session.minutesRead,
                pagesRead = session.pagesRead,
                note = DemoLibrarySeed.SESSION_NOTE,
                readDate = session.readDate
            )
        }
    }

    fun topGenres(library: List<LibraryBook>): List<GenreStat> {
        return recommendationEngine.topGenres(library)
    }

    private suspend fun saveBook(book: Book): Long {
        val externalId = book.externalId
        if (!externalId.isNullOrBlank()) {
            val existing = bookDao.getBySourceAndExternalId(book.source, externalId)
            if (existing != null) {
                bookDao.update(book.toEntity(existing.bookId))
                return existing.bookId
            }
        }

        val insertedId = bookDao.insert(book.toEntity())
        return if (insertedId == -1L && !externalId.isNullOrBlank()) {
            bookDao.getBySourceAndExternalId(book.source, externalId)?.bookId ?: insertedId
        } else {
            insertedId
        }
    }

    private fun Book.toEntity(bookId: Long = 0): BookEntity {
        return BookEntity(
            bookId = bookId,
            source = source,
            externalId = externalId,
            title = title.ifBlank { "Без названия" },
            authors = authors,
            description = description,
            categories = categories,
            publishedDate = publishedDate,
            pageCount = pageCount,
            language = language,
            thumbnailUrl = imageLink,
            previewLink = previewLink
        )
    }

    private fun LibraryBookRow.toLibraryBook(): LibraryBook {
        return LibraryBook(
            userBookId = userBookId,
            userId = userId,
            status = status,
            rating = rating,
            review = review,
            addedAt = addedAt,
            startedAt = startedAt,
            finishedAt = finishedAt,
            book = Book(
                localId = bookId,
                externalId = externalId,
                source = source,
                title = title,
                authors = authors,
                description = description,
                categories = categories,
                publishedDate = publishedDate,
                pageCount = pageCount,
                language = language,
                previewLink = previewLink,
                imageLink = thumbnailUrl
            )
        )
    }

    private fun UserBookEntity.toLibraryBook(book: Book): LibraryBook {
        return LibraryBook(
            userBookId = userBookId,
            userId = userId,
            book = book,
            status = status,
            rating = rating,
            review = review,
            addedAt = addedAt,
            startedAt = startedAt,
            finishedAt = finishedAt
        )
    }

    private fun List<LibraryBook>.toStats(readingRows: List<ReadingDayRow>): LibraryStats {
        val statusCounts = groupingBy { it.status }.eachCount()
        val rated = mapNotNull { it.rating }
        val readingDays = readingRows.map {
            ReadingDay(
                date = it.readDate,
                totalMinutes = it.totalMinutes,
                totalPages = it.totalPages
            )
        }

        return LibraryStats(
            totalBooks = size,
            statusCounts = statusCounts,
            readBooks = statusCounts[ReadingStatus.READ] ?: 0,
            readingBooks = statusCounts[ReadingStatus.READING] ?: 0,
            wantToReadBooks = statusCounts[ReadingStatus.WANT_TO_READ] ?: 0,
            averageRating = rated.takeIf { it.isNotEmpty() }?.average(),
            totalMinutes = readingDays.sumOf { it.totalMinutes },
            totalPages = readingDays.sumOf { it.totalPages },
            readingDays = readingDays,
            topGenres = recommendationEngine.popularGenres(this)
        )
    }

    private fun today(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }
}
