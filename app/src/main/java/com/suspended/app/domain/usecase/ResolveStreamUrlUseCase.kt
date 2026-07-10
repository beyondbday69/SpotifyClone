package com.suspended.app.domain.usecase

import com.suspended.app.domain.repository.MusicRepository
import javax.inject.Inject

class ResolveStreamUrlUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    suspend operator fun invoke(trackId: String): Result<String> {
        // Check cache first
        val cached = musicRepository.getCachedStreamUrl(trackId)
        if (cached != null) {
            return Result.success(cached)
        }

        // Resolve fresh URL
        val result = musicRepository.resolveStreamUrl(trackId)
        result.onSuccess { url ->
            musicRepository.cacheStreamUrl(trackId, url)
        }
        return result
    }
}
