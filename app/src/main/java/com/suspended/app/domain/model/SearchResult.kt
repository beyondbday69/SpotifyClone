package com.suspended.app.domain.model

data class SearchResult(
    val tracks: List<Track> = emptyList(),
    val query: String = ""
)
