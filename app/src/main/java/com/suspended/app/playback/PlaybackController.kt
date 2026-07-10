package com.suspended.app.playback

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import com.suspended.app.domain.model.Track
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

enum class RepeatMode { OFF, ONE, ALL }

@Singleton
class PlaybackController @Inject constructor(
    @ApplicationContext private val context: Context,
    val exoPlayer: ExoPlayer,
    val mediaSession: MediaSession
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    val queueManager = QueueManager()

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _isShuffled = MutableStateFlow(false)
    val isShuffled: StateFlow<Boolean> = _isShuffled.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    // Callback for when a new track needs its stream URL resolved
    var onTrackNeedsResolve: (suspend (Track) -> String?)? = null

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    skipNext()
                }
            }

            override fun onIsPlayingChanged(playing: Boolean) {
                _isPlaying.value = playing
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                _duration.value = exoPlayer.duration.coerceAtLeast(0L)
            }
        })

        // Position tracking coroutine
        scope.launch {
            while (true) {
                if (exoPlayer.isPlaying) {
                    val pos = exoPlayer.currentPosition
                    val dur = exoPlayer.duration.coerceAtLeast(1L)
                    _currentPosition.value = pos
                    _duration.value = dur
                    _progress.value = (pos.toFloat() / dur.toFloat()).coerceIn(0f, 1f)
                }
                delay(250L)
            }
        }
    }

    fun play(track: Track, queue: List<Track> = listOf(track)) {
        queueManager.setQueue(queue, queue.indexOf(track).coerceAtLeast(0))
        _currentTrack.value = track
        playTrackInternal(track)
    }

    private fun playTrackInternal(track: Track) {
        scope.launch {
            val url = track.localPath ?: track.streamUrl ?: onTrackNeedsResolve?.invoke(track)
            if (url != null) {
                val mediaItem = MediaItem.Builder()
                    .setUri(Uri.parse(url))
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(track.title)
                            .setArtist(track.artist)
                            .setArtworkUri(track.thumbnailUrl?.let { Uri.parse(it) })
                            .build()
                    )
                    .build()
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                exoPlayer.play()
            }
        }
    }

    fun resume() { exoPlayer.play() }
    fun pause() { exoPlayer.pause() }
    fun seekTo(position: Long) { exoPlayer.seekTo(position) }

    fun skipNext() {
        queueManager.next()?.let { track ->
            _currentTrack.value = track
            playTrackInternal(track)
        }
    }

    fun skipPrevious() {
        if (exoPlayer.currentPosition > 3000L) {
            exoPlayer.seekTo(0L)
        } else {
            queueManager.previous()?.let { track ->
                _currentTrack.value = track
                playTrackInternal(track)
            }
        }
    }

    fun addToQueue(track: Track) { queueManager.addToQueue(track) }

    fun toggleShuffle() {
        _isShuffled.value = !_isShuffled.value
        queueManager.setShuffle(_isShuffled.value)
    }

    fun toggleRepeat() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        exoPlayer.repeatMode = when (_repeatMode.value) {
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
        }
    }

    fun release() {
        mediaSession.release()
        exoPlayer.release()
    }
}
