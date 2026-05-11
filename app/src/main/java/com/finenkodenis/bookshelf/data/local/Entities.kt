package com.finenkodenis.bookshelf.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["username"], unique = true),
        Index(value = ["email"], unique = true)
    ]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "user_id")
    val userId: Long = 0,
    val username: String,
    val email: String? = null,
    @ColumnInfo(name = "password_hash")
    val passwordHash: String,
    @ColumnInfo(name = "password_salt")
    val passwordSalt: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "last_login_at")
    val lastLoginAt: Long? = null
)

@Entity(
    tableName = "books",
    indices = [
        Index(value = ["source", "external_id"], unique = true),
        Index(value = ["title"])
    ]
)
data class BookEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "book_id")
    val bookId: Long = 0,
    val source: String,
    @ColumnInfo(name = "external_id")
    val externalId: String?,
    val title: String,
    val authors: List<String> = emptyList(),
    val description: String? = null,
    val categories: List<String> = emptyList(),
    @ColumnInfo(name = "published_date")
    val publishedDate: String? = null,
    @ColumnInfo(name = "page_count")
    val pageCount: Int? = null,
    val language: String? = null,
    @ColumnInfo(name = "thumbnail_url")
    val thumbnailUrl: String? = null,
    @ColumnInfo(name = "preview_link")
    val previewLink: String? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "user_books",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["book_id"],
            childColumns = ["book_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["user_id", "book_id"], unique = true),
        Index(value = ["user_id", "status"]),
        Index(value = ["book_id"])
    ]
)
data class UserBookEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "user_book_id")
    val userBookId: Long = 0,
    @ColumnInfo(name = "user_id")
    val userId: Long,
    @ColumnInfo(name = "book_id")
    val bookId: Long,
    val status: ReadingStatus = ReadingStatus.WANT_TO_READ,
    val rating: Int? = null,
    val review: String? = null,
    @ColumnInfo(name = "added_at")
    val addedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "started_at")
    val startedAt: Long? = null,
    @ColumnInfo(name = "finished_at")
    val finishedAt: Long? = null,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "reading_sessions",
    foreignKeys = [
        ForeignKey(
            entity = UserBookEntity::class,
            parentColumns = ["user_book_id"],
            childColumns = ["user_book_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["user_book_id", "read_date"])]
)
data class ReadingSessionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "session_id")
    val sessionId: Long = 0,
    @ColumnInfo(name = "user_book_id")
    val userBookId: Long,
    @ColumnInfo(name = "read_date")
    val readDate: String,
    @ColumnInfo(name = "minutes_read")
    val minutesRead: Int = 0,
    @ColumnInfo(name = "pages_read")
    val pagesRead: Int = 0,
    val note: String? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
