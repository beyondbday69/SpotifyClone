package com.suspended.app.data.repository

import com.suspended.app.data.local.dao.CacheDao
import com.suspended.app.data.local.dao.PlaylistDao
import com.suspended.app.data.local.dao.TrackDao
import com.suspended.app.data.local.entity.PlaylistEntity
import com.suspended.app.data.local.entity.PlaylistTrackCrossRef
import com.suspended.app.data.local.entity.StreamCacheEntity
import com.suspended.app.data.mapper.toDomain
import com.suspended.app.data.mapper.toEntity
import com.suspended.app.data.remote.YtDlpDataSource
import com.suspended.app.domain.model.Artist
import com.suspended.app.domain.model.Playlist
import com.suspended.app.domain.model.Track
import com.suspended.app.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepositoryImpl @Inject constructor(
    private val trackDao: TrackDao,
    private val playlistDao: PlaylistDao,
    private val cacheDao: CacheDao,
    private val ytDlpDataSource: YtDlpDataSource
) : MusicRepository {

    override suspend fun searchTracks(query: String): Result<List<Track>> {
        return try {
            val tracks = ytDlpDataSource.search(query)
            Result.success(tracks)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun resolveStreamUrl(trackId: String): Result<String> {
        return try {
            val url = ytDlpDataSource.resolveStreamUrl(trackId)
            Result.success(url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getRecentlyPlayed(): Flow<List<Track>> {
        return trackDao.getRecentlyPlayed().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getLibraryTracks(): Flow<List<Track>> {
        return trackDao.getLibraryTracks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getLibraryArtists(): Flow<List<Artist>> {
        return trackDao.getArtists().map { artistInfoList ->
            artistInfoList.map { it.toDomain() }
        }
    }

    override fun getPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getAllPlaylists().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getPlaylistWithTracks(playlistId: Long): Playlist? {
        val playlistEntity = playlistDao.getPlaylistById(playlistId) ?: return null
        val trackEntities = playlistDao.getTracksForPlaylist(playlistId)
        val tracks = trackEntities.map { it.toDomain() }
        return playlistEntity.toDomain(tracks)
    }

    override suspend fun createPlaylist(name: String, description: String): Long {
        val entity = PlaylistEntity(
            name = name,
            description = description,
            createdAt = System.currentTimeMillis()
        )
        return playlistDao.insertPlaylist(entity)
    }

    override suspend fun addTrackToPlaylist(playlistId: Long, track: Track) {
        // Ensure track exists in database
        trackDao.insertTrack(track.toEntity())
        val existingTracks = playlistDao.getTracksForPlaylist(playlistId)
        val crossRef = PlaylistTrackCrossRef(
            playlistId = playlistId,
            trackId = track.id,
            position = existingTracks.size,
            addedAt = System.currentTimeMillis()
        )
        playlistDao.insertPlaylistTrack(crossRef)
    }

    override suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: String) {
        playlistDao.removePlaylistTrack(playlistId, trackId)
    }

    override suspend fun deletePlaylist(playlistId: Long) {
        playlistDao.deleteAllPlaylistTracks(playlistId)
        playlistDao.deletePlaylistById(playlistId)
    }

    override suspend fun addToLibrary(track: Track) {
        val existingEntity = trackDao.getTrackById(track.id)
        if (existingEntity != null) {
            // Track already exists, just mark as in library
            trackDao.insertTrack(existingEntity.copy(isInLibrary = true))
        } else {
            trackDao.insertTrack(track.toEntity(isInLibrary = true))
        }
    }

    override suspend fun removeFromLibrary(trackId: String) {
        trackDao.removeFromLibrary(trackId)
    }

    override suspend fun addToRecentlyPlayed(track: Track) {
        val existingEntity = trackDao.getTrackById(track.id)
        val now = System.currentTimeMillis()
        if (existingEntity != null) {
            trackDao.updateLastPlayed(track.id, now)
        } else {
            trackDao.insertTrack(track.toEntity(lastPlayedAt = now))
        }
    }

    override suspend fun getCachedStreamUrl(trackId: String): String? {
        val cacheEntry = cacheDao.getCacheEntry(trackId) ?: return null
        val expirationTime = cacheEntry.cachedAt + (cacheEntry.ttlSeconds * 1000)
        return if (expirationTime > System.currentTimeMillis()) {
            cacheEntry.streamUrl
        } else {
            cacheDao.deleteCacheEntry(trackId)
            null
        }
    }

    override suspend fun cacheStreamUrl(trackId: String, url: String, ttlSeconds: Long) {
        val cacheEntity = StreamCacheEntity(
            trackId = trackId,
            streamUrl = url,
            cachedAt = System.currentTimeMillis(),
            ttlSeconds = ttlSeconds
        )
        cacheDao.insertCache(cacheEntity)
    }

    override fun getLikedSongs(): Flow<List<Track>> {
        return trackDao.getLikedTracks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getLikedTrackIds(): Flow<Set<String>> {
        return trackDao.getLikedTrackIds().map { it.toSet() }
    }

    override suspend fun toggleLike(track: Track): Boolean {
        val existingEntity = trackDao.getTrackById(track.id)
        val newLikedState = !(existingEntity?.isLiked ?: false)
        setLikedInternal(track, existingEntity, newLikedState)
        return newLikedState
    }

    override suspend fun setLiked(track: Track, liked: Boolean) {
        val existingEntity = trackDao.getTrackById(track.id)
        setLikedInternal(track, existingEntity, liked)
    }

    private suspend fun setLikedInternal(
        track: Track,
        existingEntity: com.suspended.app.data.local.entity.TrackEntity?,
        liked: Boolean
    ) {
        val timestamp = if (liked) System.currentTimeMillis() else null
        if (existingEntity != null) {
            trackDao.setLiked(track.id, liked, timestamp)
        } else {
            // Track isn't cached locally yet (e.g. a fresh search result) - persist it first.
            trackDao.insertTrack(track.toEntity(isLiked = liked, likedAt = timestamp))
        }
    }
}
