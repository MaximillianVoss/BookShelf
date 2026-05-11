package com.finenkodenis.bookshelf.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface BookDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(book: BookEntity): Long

    @Update
    suspend fun update(book: BookEntity)

    @Query(
        """
        SELECT * FROM books
        WHERE source = :source AND external_id = :externalId
        LIMIT 1
        """
    )
    suspend fun getBySourceAndExternalId(source: String, externalId: String): BookEntity?

    @Query("SELECT * FROM books WHERE book_id = :bookId LIMIT 1")
    suspend fun getById(bookId: Long): BookEntity?
}
