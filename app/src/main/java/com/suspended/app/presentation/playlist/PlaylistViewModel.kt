package com.suspended.app.presentation.playlist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suspended.app.domain.model.Playlist
import com.suspended.app.domain.model.Track
import com.suspended.app.domain.usecase.ManagePlaylistUseCase
import com.suspended.app.playback.PlaybackController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlaylistDetailUiState(
    val playlist: Playlist? = null,
    val isLoading: Boolean = true,
    val currentPlayingTrackId: String? = null
)

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val managePlaylistUseCase: ManagePlaylistUseCase,
    private val playbackController: PlaybackController
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlaylistDetailUiState())
    val uiState: StateFlow<PlaylistDetailUiState> = _uiState.asStateFlow()

    init {
        val playlistId = savedStateHandle.get<String>("playlistId")?.toLongOrNull()
        if (playlistId != null) {
            loadPlaylist(playlistId)
        } else {
            _uiState.update { it.copy(isLoading = false) }
        }

        viewModelScope.launch {
            playbackController.currentTrack.collect { track ->
                _uiState.update { it.copy(currentPlayingTrackId = track?.id) }
            }
        }
    }

    private fun loadPlaylist(id: Long) {
        viewModelScope.launch {
            val playlist = managePlaylistUseCase.getPlaylistWithTracks(id)
            _uiState.update { it.copy(playlist = playlist, isLoading = false) }
        }
    }

    fun playAll() {
        val playlist = _uiState.value.playlist
        if (playlist != null && playlist.tracks.isNotEmpty()) {
            playbackController.play(playlist.tracks.first(), playlist.tracks)
        }
    }

    fun playTrack(track: Track) {
        val playlist = _uiState.value.playlist
        if (playlist != null) {
            playbackController.play(track, playlist.tracks)
        }
    }

    fun removeTrack(trackId: String) {
        val playlistId = _uiState.value.playlist?.id ?: return
        viewModelScope.launch {
            managePlaylistUseCase.removeTrack(playlistId, trackId)
            loadPlaylist(playlistId)
        }
    }

    fun addToQueue(track: Track) {
        playbackController.addToQueue(track)
    }
}
