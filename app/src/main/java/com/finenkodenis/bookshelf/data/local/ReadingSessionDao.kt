package com.finenkodenis.bookshelf.data.local

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

data class ReadingDayRow(
    @ColumnInfo(name = "read_date")
    val readDate: String,
    @ColumnInfo(name = "total_minutes")
    val totalMinutes: Int,
    @ColumnInfo(name = "total_pages")
    val totalPages: Int
)

@Dao
interface ReadingSessionDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(session: ReadingSessionEntity): Long

    @Query(
        """
        SELECT
            rs.read_date,
            SUM(rs.minutes_read) AS total_minutes,
            SUM(rs.pages_read) AS total_pages
        FROM reading_sessions rs
        INNER JOIN user_books ub ON ub.user_book_id = rs.user_book_id
        WHERE ub.user_id = :userId
        GROUP BY rs.read_date
        ORDER BY rs.read_date DESC
        """
    )
    fun observeReadingDays(userId: Long): Flow<List<ReadingDayRow>>
}
