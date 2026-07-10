package com.suspended.app.domain.repository

import com.suspended.app.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface DownloadRepository {
    suspend fun downloadTrack(track: Track): Result<String>
    fun getDownloadedTracks(): Flow<List<Track>>
    suspend fun deleteDownload(trackId: String)
    suspend fun isDownloaded(trackId: String): Boolean
}
