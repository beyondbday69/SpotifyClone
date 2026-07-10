package com.suspended.app.domain.model

data class Artist(
    val id: String,
    val name: String,
    val thumbnailUrl: String? = null,
    val trackCount: Int = 0
)
