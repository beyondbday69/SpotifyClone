package com.suspended.app.domain.model

data class Album(
    val id: String,
    val title: String,
    val artist: String,
    val thumbnailUrl: String? = null,
    val tracks: List<Track> = emptyList(),
    val year: Int? = null
)
