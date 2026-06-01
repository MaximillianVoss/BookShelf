package com.finenkodenis.bookshelf.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RecommendationCacheDao {
    @Query(
        """
        SELECT * FROM recommendation_cache
        WHERE user_id = :userId
        LIMIT 1
        """
    )
    suspend fun getByUser(userId: Long): RecommendationCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(cache: RecommendationCacheEntity)

    @Query("DELETE FROM recommendation_cache WHERE user_id = :userId")
    suspend fun deleteByUser(userId: Long)
}
