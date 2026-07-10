package com.suspended.app.domain.model

data class Playlist(
    val id: Long = 0L,
    val name: String,
    val description: String = "",
    val coverUrl: String? = null,
    val tracks: List<Track> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)
