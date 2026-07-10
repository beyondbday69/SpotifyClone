package com.suspended.app.domain.repository

import com.suspended.app.domain.model.*
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    suspend fun searchTracks(query: String): Result<List<Track>>
    suspend fun resolveStreamUrl(trackId: String): Result<String>
    fun getRecentlyPlayed(): Flow<List<Track>>
    fun getLibraryTracks(): Flow<List<Track>>
    fun getLibraryArtists(): Flow<List<Artist>>
    fun getPlaylists(): Flow<List<Playlist>>
    suspend fun getPlaylistWithTracks(playlistId: Long): Playlist?
    suspend fun createPlaylist(name: String, description: String = ""): Long
    suspend fun addTrackToPlaylist(playlistId: Long, track: Track)
    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: String)
    suspend fun deletePlaylist(playlistId: Long)
    suspend fun addToLibrary(track: Track)
    suspend fun removeFromLibrary(trackId: String)
    suspend fun addToRecentlyPlayed(track: Track)
    suspend fun getCachedStreamUrl(trackId: String): String?
    suspend fun cacheStreamUrl(trackId: String, url: String, ttlSeconds: Long = 3600L)
}
