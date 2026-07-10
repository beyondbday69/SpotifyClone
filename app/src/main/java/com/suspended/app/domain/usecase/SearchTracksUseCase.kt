package com.suspended.app.domain.usecase

import com.suspended.app.domain.model.Track
import com.suspended.app.domain.repository.MusicRepository
import javax.inject.Inject

class SearchTracksUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    suspend operator fun invoke(query: String): Result<List<Track>> {
        return musicRepository.searchTracks(query)
    }
}
