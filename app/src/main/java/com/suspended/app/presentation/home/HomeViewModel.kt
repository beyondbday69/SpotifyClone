package com.suspended.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suspended.app.domain.model.Playlist
import com.suspended.app.domain.model.Track
import com.suspended.app.domain.repository.MusicRepository
import com.suspended.app.playback.PlaybackController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class HomeUiState(
    val recentlyPlayed: List<Track> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val libraryTracks: List<Track> = emptyList(),
    val isLoading: Boolean = true,
    val greeting: String = ""
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val playbackController: PlaybackController
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(greeting = getGreetingTime()))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            musicRepository.getRecentlyPlayed().collect { tracks ->
                _uiState.update { it.copy(recentlyPlayed = tracks, isLoading = false) }
            }
        }
        viewModelScope.launch {
            musicRepository.getPlaylists().collect { playlists ->
                _uiState.update { it.copy(playlists = playlists) }
            }
        }
        viewModelScope.launch {
            musicRepository.getLibraryTracks().collect { tracks ->
                _uiState.update { it.copy(libraryTracks = tracks) }
            }
        }
    }

    fun playTrack(track: Track, queue: List<Track> = listOf(track)) {
        playbackController.play(track, queue)
        viewModelScope.launch {
            musicRepository.addToRecentlyPlayed(track)
        }
    }

    private fun getGreetingTime(): String {
        val c = Calendar.getInstance()
        return when (c.get(Calendar.HOUR_OF_DAY)) {
            in 0..11 -> "Good morning"
            in 12..15 -> "Good afternoon"
            in 16..20 -> "Good evening"
            else -> "Good evening"
        }
    }
}
