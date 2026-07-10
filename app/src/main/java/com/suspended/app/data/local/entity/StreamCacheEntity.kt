package com.suspended.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stream_cache")
data class StreamCacheEntity(
    @PrimaryKey
    val trackId: String,
    val streamUrl: String,
    val cachedAt: Long = System.currentTimeMillis(),
    val ttlSeconds: Long = 3600L
)
