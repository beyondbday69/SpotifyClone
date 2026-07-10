package com.suspended.app.data.repository

import android.content.Context
import com.suspended.app.data.local.dao.TrackDao
import com.suspended.app.data.mapper.toDomain
import com.suspended.app.data.mapper.toEntity
import com.suspended.app.data.remote.YtDlpDataSource
import com.suspended.app.domain.model.Track
import com.suspended.app.domain.repository.DownloadRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadRepositoryImpl @Inject constructor(
    private val trackDao: TrackDao,
    private val ytDlpDataSource: YtDlpDataSource,
    @ApplicationContext private val context: Context
) : DownloadRepository {

    override suspend fun downloadTrack(track: Track): Result<String> {
        return try {
            val outputDir = File(context.getExternalFilesDir(null), "downloads").absolutePath
            // Ensure track is in database before downloading
            val existingEntity = trackDao.getTrackById(track.id)
            if (existingEntity == null) {
                trackDao.insertTrack(track.toEntity())
            }
            val filePath = ytDlpDataSource.download(track.id, outputDir)
            trackDao.updateDownloadPath(track.id, filePath)
            Result.success(filePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getDownloadedTracks(): Flow<List<Track>> {
        return trackDao.getDownloadedTracks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun deleteDownload(trackId: String) {
        val entity = trackDao.getTrackById(trackId)
        entity?.localPath?.let { path ->
            val file = File(path)
            if (file.exists()) {
                file.delete()
            }
        }
        trackDao.updateDownloadPath(trackId, "")
        // Reset download status - re-insert with isDownloaded = false
        entity?.let {
            trackDao.insertTrack(it.copy(isDownloaded = false, localPath = null))
        }
    }

    override suspend fun isDownloaded(trackId: String): Boolean {
        return trackDao.isDownloaded(trackId)
    }
}
