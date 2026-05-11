package com.finenkodenis.bookshelf.data.local

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class LibraryBookRow(
    @ColumnInfo(name = "user_book_id")
    val userBookId: Long,
    @ColumnInfo(name = "user_id")
    val userId: Long,
    @ColumnInfo(name = "status")
    val status: ReadingStatus,
    @ColumnInfo(name = "rating")
    val rating: Int?,
    @ColumnInfo(name = "review")
    val review: String?,
    @ColumnInfo(name = "added_at")
    val addedAt: Long,
    @ColumnInfo(name = "started_at")
    val startedAt: Long?,
    @ColumnInfo(name = "finished_at")
    val finishedAt: Long?,
    @ColumnInfo(name = "book_id")
    val bookId: Long,
    @ColumnInfo(name = "source")
    val source: String,
    @ColumnInfo(name = "external_id")
    val externalId: String?,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "authors")
    val authors: List<String>,
    @ColumnInfo(name = "description")
    val description: String?,
    @ColumnInfo(name = "categories")
    val categories: List<String>,
    @ColumnInfo(name = "published_date")
    val publishedDate: String?,
    @ColumnInfo(name = "page_count")
    val pageCount: Int?,
    @ColumnInfo(name = "language")
    val language: String?,
    @ColumnInfo(name = "thumbnail_url")
    val thumbnailUrl: String?,
    @ColumnInfo(name = "preview_link")
    val previewLink: String?
)

@Dao
interface UserBookDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(userBook: UserBookEntity): Long

    @Update
    suspend fun update(userBook: UserBookEntity)

    @Query(
        """
        SELECT * FROM user_books
        WHERE user_id = :userId AND book_id = :bookId
        LIMIT 1
        """
    )
    suspend fun getByUserAndBook(userId: Long, bookId: Long): UserBookEntity?

    @Query(
        """
        SELECT * FROM user_books
        WHERE user_book_id = :userBookId
        LIMIT 1
        """
    )
    suspend fun getById(userBookId: Long): UserBookEntity?

    @Transaction
    @Query(
        """
        SELECT
            ub.user_book_id,
            ub.user_id,
            ub.status,
            ub.rating,
            ub.review,
            ub.added_at,
            ub.started_at,
            ub.finished_at,
            b.book_id,
            b.source,
            b.external_id,
            b.title,
            b.authors,
            b.description,
            b.categories,
            b.published_date,
            b.page_count,
            b.language,
            b.thumbnail_url,
            b.preview_link
        FROM user_books ub
        INNER JOIN books b ON b.book_id = ub.book_id
        WHERE ub.user_id = :userId
          AND (:status IS NULL OR ub.status = :status)
        ORDER BY ub.updated_at DESC, ub.added_at DESC
        """
    )
    fun observeLibrary(userId: Long, status: String?): Flow<List<LibraryBookRow>>

    @Transaction
    @Query(
        """
        SELECT
            ub.user_book_id,
            ub.user_id,
            ub.status,
            ub.rating,
            ub.review,
            ub.added_at,
            ub.started_at,
            ub.finished_at,
            b.book_id,
            b.source,
            b.external_id,
            b.title,
            b.authors,
            b.description,
            b.categories,
            b.published_date,
            b.page_count,
            b.language,
            b.thumbnail_url,
            b.preview_link
        FROM user_books ub
        INNER JOIN books b ON b.book_id = ub.book_id
        WHERE ub.user_book_id = :userBookId
        LIMIT 1
        """
    )
    fun observeLibraryBook(userBookId: Long): Flow<LibraryBookRow?>
}
