package com.suspended.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.suspended.app.data.local.dao.CacheDao
import com.suspended.app.data.local.dao.PlaylistDao
import com.suspended.app.data.local.dao.TrackDao
import com.suspended.app.data.local.entity.PlaylistEntity
import com.suspended.app.data.local.entity.PlaylistTrackCrossRef
import com.suspended.app.data.local.entity.StreamCacheEntity
import com.suspended.app.data.local.entity.TrackEntity

@Database(
    entities = [
        TrackEntity::class,
        PlaylistEntity::class,
        PlaylistTrackCrossRef::class,
        StreamCacheEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun cacheDao(): CacheDao
}
