package com.suspended.app.domain.usecase

import com.suspended.app.domain.model.Track
import com.suspended.app.domain.repository.MusicRepository
import javax.inject.Inject

/**
 * Flips a track's "liked" state and persists it, so it shows up in / drops out of
 * the user's Liked Songs collection.
 */
class ToggleLikeUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    suspend operator fun invoke(track: Track): Boolean {
        return musicRepository.toggleLike(track)
    }
}
