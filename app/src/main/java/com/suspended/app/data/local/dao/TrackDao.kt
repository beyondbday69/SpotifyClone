package com.suspended.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.suspended.app.data.local.entity.TrackEntity
import kotlinx.coroutines.flow.Flow

data class ArtistInfo(
    val artist: String,
    val artistId: String?,
    val trackCount: Int
)

@Dao
interface TrackDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: TrackEntity)

    @Query("SELECT * FROM tracks WHERE isInLibrary = 1 ORDER BY addedAt DESC")
    fun getLibraryTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE lastPlayedAt IS NOT NULL ORDER BY lastPlayedAt DESC LIMIT 20")
    fun getRecentlyPlayed(): Flow<List<TrackEntity>>

    @Query("SELECT DISTINCT artist, artistId, COUNT(*) as trackCount FROM tracks WHERE isInLibrary = 1 GROUP BY artist")
    fun getArtists(): Flow<List<ArtistInfo>>

    @Query("DELETE FROM tracks WHERE id = :trackId")
    suspend fun deleteTrack(trackId: String)

    @Query("UPDATE tracks SET isInLibrary = 0 WHERE id = :trackId")
    suspend fun removeFromLibrary(trackId: String)

    @Query("SELECT * FROM tracks WHERE isDownloaded = 1")
    fun getDownloadedTracks(): Flow<List<TrackEntity>>

    @Query("UPDATE tracks SET localPath = :path, isDownloaded = 1 WHERE id = :trackId")
    suspend fun updateDownloadPath(trackId: String, path: String)

    @Query("UPDATE tracks SET lastPlayedAt = :timestamp WHERE id = :trackId")
    suspend fun updateLastPlayed(trackId: String, timestamp: Long)

    @Query("SELECT * FROM tracks WHERE id = :trackId")
    suspend fun getTrackById(trackId: String): TrackEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM tracks WHERE id = :trackId AND isDownloaded = 1)")
    suspend fun isDownloaded(trackId: String): Boolean
}
