package com.suspended.app.presentation.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suspended.app.domain.model.Artist
import com.suspended.app.domain.model.Playlist
import com.suspended.app.domain.model.Track
import com.suspended.app.domain.usecase.GetLibraryUseCase
import com.suspended.app.domain.usecase.ManagePlaylistUseCase
import com.suspended.app.playback.PlaybackController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LibraryFilter { PLAYLISTS, TRACKS, ARTISTS, DOWNLOADED }

data class LibraryUiState(
    val selectedFilter: LibraryFilter = LibraryFilter.PLAYLISTS,
    val playlists: List<Playlist> = emptyList(),
    val tracks: List<Track> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val downloadedTracks: List<Track> = emptyList(),
    val showCreatePlaylistDialog: Boolean = false
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val getLibraryUseCase: GetLibraryUseCase,
    private val managePlaylistUseCase: ManagePlaylistUseCase,
    private val playbackController: PlaybackController
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        loadLibrary()
    }

    private fun loadLibrary() {
        val libraryData = getLibraryUseCase()
        viewModelScope.launch {
            libraryData.playlists.collect { list ->
                _uiState.update { it.copy(playlists = list) }
            }
        }
        viewModelScope.launch {
            libraryData.tracks.collect { list ->
                _uiState.update { it.copy(tracks = list) }
            }
        }
        viewModelScope.launch {
            libraryData.artists.collect { list ->
                _uiState.update { it.copy(artists = list) }
            }
        }
        viewModelScope.launch {
            libraryData.downloaded.collect { list ->
                _uiState.update { it.copy(downloadedTracks = list) }
            }
        }
    }

    fun setFilter(filter: LibraryFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun showCreatePlaylistDialog(show: Boolean) {
        _uiState.update { it.copy(showCreatePlaylistDialog = show) }
    }

    fun createPlaylist(name: String, description: String = "") {
        viewModelScope.launch {
            managePlaylistUseCase.createPlaylist(name, description)
            showCreatePlaylistDialog(false)
        }
    }

    fun playTrack(track: Track) {
        val queue = when (_uiState.value.selectedFilter) {
            LibraryFilter.TRACKS -> _uiState.value.tracks
            LibraryFilter.DOWNLOADED -> _uiState.value.downloadedTracks
            else -> listOf(track)
        }
        playbackController.play(track, queue)
    }
}
