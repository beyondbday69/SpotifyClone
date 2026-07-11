package com.suspended.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val artist: String,
    val artistId: String? = null,
    val duration: Long = 0L,
    val thumbnailUrl: String? = null,
    val albumName: String? = null,
    val albumId: String? = null,
    val localPath: String? = null,
    val isDownloaded: Boolean = false,
    val isInLibrary: Boolean = false,
    val lastPlayedAt: Long? = null,
    val isLiked: Boolean = false,
    val likedAt: Long? = null,
    val addedAt: Long = System.currentTimeMillis()
)
