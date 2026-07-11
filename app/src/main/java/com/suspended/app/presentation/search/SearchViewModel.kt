package com.suspended.app.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suspended.app.domain.model.Track
import com.suspended.app.domain.repository.MusicRepository
import com.suspended.app.domain.usecase.SearchTracksUseCase
import com.suspended.app.domain.usecase.ToggleLikeUseCase
import com.suspended.app.playback.PlaybackController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val results: List<Track> = emptyList(),
    val isSearching: Boolean = false,
    val error: String? = null,
    val isActive: Boolean = false,
    val likedTrackIds: Set<String> = emptySet()
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchTracksUseCase: SearchTracksUseCase,
    private val musicRepository: MusicRepository,
    private val toggleLikeUseCase: ToggleLikeUseCase,
    private val playbackController: PlaybackController
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            musicRepository.getLikedTrackIds().collect { ids ->
                _uiState.update { it.copy(likedTrackIds = ids) }
            }
        }
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
        searchJob?.cancel()
        if (query.isNotBlank()) {
            _uiState.update { it.copy(isSearching = true) }
            searchJob = viewModelScope.launch {
                delay(500) // debounce
                val result = searchTracksUseCase(query)
                _uiState.update {
                    it.copy(
                        isSearching = false,
                        results = result.getOrDefault(emptyList()),
                        error = result.exceptionOrNull()?.message
                    )
                }
            }
        } else {
            _uiState.update { it.copy(results = emptyList(), isSearching = false) }
        }
    }

    fun playTrack(track: Track) {
        playbackController.play(track, _uiState.value.results)
    }

    fun toggleLike(track: Track) {
        viewModelScope.launch {
            toggleLikeUseCase(track)
        }
    }
}
