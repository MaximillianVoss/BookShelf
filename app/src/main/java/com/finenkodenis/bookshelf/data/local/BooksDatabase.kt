package com.finenkodenis.bookshelf.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        UserEntity::class,
        BookEntity::class,
        UserBookEntity::class,
        ReadingSessionEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(AppConverters::class)
abstract class BooksDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun bookDao(): BookDao
    abstract fun userBookDao(): UserBookDao
    abstract fun readingSessionDao(): ReadingSessionDao

    companion object {
        @Volatile
        private var instance: BooksDatabase? = null

        fun getDatabase(context: Context): BooksDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    BooksDatabase::class.java,
                    "bookshelf.db"
                ).build().also { instance = it }
            }
        }
    }
}
