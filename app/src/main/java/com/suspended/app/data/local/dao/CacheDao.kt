package com.suspended.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.suspended.app.data.local.entity.StreamCacheEntity

@Dao
interface CacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCache(cache: StreamCacheEntity)

    @Query("SELECT * FROM stream_cache WHERE trackId = :trackId")
    suspend fun getCacheEntry(trackId: String): StreamCacheEntity?

    @Query("DELETE FROM stream_cache WHERE trackId = :trackId")
    suspend fun deleteCacheEntry(trackId: String)

    @Query("DELETE FROM stream_cache WHERE cachedAt + (ttlSeconds * 1000) < :currentTime")
    suspend fun clearExpiredCache(currentTime: Long = System.currentTimeMillis())
}
