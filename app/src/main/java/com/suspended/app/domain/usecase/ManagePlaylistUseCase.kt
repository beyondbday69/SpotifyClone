package com.suspended.app.domain.usecase

import com.suspended.app.domain.model.Playlist
import com.suspended.app.domain.model.Track
import com.suspended.app.domain.repository.MusicRepository
import javax.inject.Inject

class ManagePlaylistUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    suspend fun createPlaylist(name: String, description: String = ""): Long {
        return musicRepository.createPlaylist(name, description)
    }

    suspend fun deletePlaylist(playlistId: Long) {
        musicRepository.deletePlaylist(playlistId)
    }

    suspend fun addTrack(playlistId: Long, track: Track) {
        musicRepository.addTrackToPlaylist(playlistId, track)
    }

    suspend fun removeTrack(playlistId: Long, trackId: String) {
        musicRepository.removeTrackFromPlaylist(playlistId, trackId)
    }

    suspend fun getPlaylistWithTracks(playlistId: Long): Playlist? {
        return musicRepository.getPlaylistWithTracks(playlistId)
    }
}
