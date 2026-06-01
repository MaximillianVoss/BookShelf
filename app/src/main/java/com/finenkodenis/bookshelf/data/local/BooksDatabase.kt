package com.finenkodenis.bookshelf.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        UserEntity::class,
        BookEntity::class,
        UserBookEntity::class,
        ReadingSessionEntity::class,
        RecommendationCacheEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(AppConverters::class)
abstract class BooksDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun bookDao(): BookDao
    abstract fun userBookDao(): UserBookDao
    abstract fun readingSessionDao(): ReadingSessionDao
    abstract fun recommendationCacheDao(): RecommendationCacheDao

    companion object {
        @Volatile
        private var instance: BooksDatabase? = null

        fun getDatabase(context: Context): BooksDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    BooksDatabase::class.java,
                    "bookshelf.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { instance = it }
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_books ADD COLUMN last_reading_url TEXT")
                db.execSQL("ALTER TABLE user_books ADD COLUMN last_scroll_y INTEGER NOT NULL DEFAULT 0")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS recommendation_cache (
                        user_id INTEGER NOT NULL PRIMARY KEY,
                        cache_key TEXT NOT NULL,
                        books_json TEXT NOT NULL,
                        cached_at INTEGER NOT NULL,
                        FOREIGN KEY(user_id) REFERENCES users(user_id)
                            ON UPDATE CASCADE
                            ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE UNIQUE INDEX IF NOT EXISTS index_recommendation_cache_user_id
                    ON recommendation_cache(user_id)
                    """.trimIndent()
                )
            }
        }
    }
}
