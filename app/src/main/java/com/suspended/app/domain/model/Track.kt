package com.suspended.app.domain.model

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val artistId: String? = null,
    val duration: Long = 0L, // duration in seconds
    val thumbnailUrl: String? = null,
    val albumName: String? = null,
    val albumId: String? = null,
    val streamUrl: String? = null,
    val localPath: String? = null,
    val isDownloaded: Boolean = false,
    val addedAt: Long = System.currentTimeMillis()
)
