package com.suspended.app.domain.usecase

import com.suspended.app.domain.model.Artist
import com.suspended.app.domain.model.Playlist
import com.suspended.app.domain.model.Track
import com.suspended.app.domain.repository.DownloadRepository
import com.suspended.app.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

data class LibraryData(
    val tracks: Flow<List<Track>>,
    val artists: Flow<List<Artist>>,
    val playlists: Flow<List<Playlist>>,
    val downloaded: Flow<List<Track>>
)

class GetLibraryUseCase @Inject constructor(
    private val musicRepository: MusicRepository,
    private val downloadRepository: DownloadRepository
) {
    operator fun invoke(): LibraryData {
        return LibraryData(
            tracks = musicRepository.getLibraryTracks(),
            artists = musicRepository.getLibraryArtists(),
            playlists = musicRepository.getPlaylists(),
            downloaded = downloadRepository.getDownloadedTracks()
        )
    }
}
