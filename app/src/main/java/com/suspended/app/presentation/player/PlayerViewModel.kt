package com.suspended.app.presentation.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suspended.app.domain.model.Track
import com.suspended.app.domain.repository.MusicRepository
import com.suspended.app.domain.usecase.ResolveStreamUrlUseCase
import com.suspended.app.playback.PlaybackController
import com.suspended.app.playback.RepeatMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playbackController: PlaybackController,
    private val resolveStreamUrlUseCase: ResolveStreamUrlUseCase,
    private val musicRepository: MusicRepository
) : ViewModel() {

    val currentTrack: StateFlow<Track?> = playbackController.currentTrack
    val isPlaying: StateFlow<Boolean> = playbackController.isPlaying
    val progress: StateFlow<Float> = playbackController.progress
    val duration: StateFlow<Long> = playbackController.duration
    val currentPosition: StateFlow<Long> = playbackController.currentPosition
    val isShuffled: StateFlow<Boolean> = playbackController.isShuffled
    val repeatMode: StateFlow<RepeatMode> = playbackController.repeatMode
    val queue: StateFlow<List<Track>> = playbackController.queueManager.queue
    val playbackState = playbackController.playbackState
    val errorMessage = playbackController.errorMessage
    init {
        playbackController.onTrackNeedsResolve = { track ->
            val result = resolveStreamUrlUseCase(track.id)
            result.getOrNull()
        }
    }

    fun playPause() {
        if (playbackController.isPlaying.value) {
            playbackController.pause()
        } else {
            playbackController.resume()
        }
    }

    fun skipNext() {
        playbackController.skipNext()
    }

    fun skipPrevious() {
        playbackController.skipPrevious()
    }

    fun seekTo(fraction: Float) {
        val positionMs = (fraction * playbackController.duration.value).toLong()
        playbackController.seekTo(positionMs)
    }

    fun toggleShuffle() {
        playbackController.toggleShuffle()
    }

    fun toggleRepeat() {
        playbackController.toggleRepeat()
    }

    fun addToLibrary(track: Track) {
        viewModelScope.launch {
            musicRepository.addToLibrary(track)
        }
    }

    fun clearError() = playbackController.clearError()
}
