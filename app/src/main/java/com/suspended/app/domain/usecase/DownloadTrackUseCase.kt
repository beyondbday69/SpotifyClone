package com.suspended.app.domain.usecase

import com.suspended.app.domain.model.Track
import com.suspended.app.domain.repository.DownloadRepository
import javax.inject.Inject

class DownloadTrackUseCase @Inject constructor(
    private val downloadRepository: DownloadRepository
) {
    suspend operator fun invoke(track: Track): Result<String> {
        return downloadRepository.downloadTrack(track)
    }
}
