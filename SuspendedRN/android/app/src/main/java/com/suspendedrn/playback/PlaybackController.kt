package com.suspendedrn.playback

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.modules.core.DeviceEventManagerModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Singleton playback engine bridged between Native (Media3) and JS (RN event bus).
 *
 * Mirrors the existing Kotlin app's `PlaybackController.kt` (now in
 * `app/src/main/.../playback/PlaybackController.kt`). Behavior kept identical:
 *   - ExoPlayer with USAGE_MEDIA audio attributes
 *   - MediaSession for lockscreen control
 *   - Auto-advance on STATE_ENDED
 *   - Position polling emits events to JS at 4Hz while playing
 */
@OptIn(UnstableApi::class)
class PlaybackController private constructor(private val context: Context) {

    val exoPlayer: ExoPlayer = ExoPlayer.Builder(context)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
                .build(),
            /* handleAudioFocus = */ true
        )
        .setHandleAudioBecomingNoisy(true)
        .build()

    val mediaSession: MediaSession = MediaSession.Builder(context, exoPlayer).build()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _position = MutableStateFlow(0L)
    val position: StateFlow<Long> = _position.asStateFlow()

    private val _currentMediaId = MutableStateFlow<String?>(null)
    val currentMediaId: StateFlow<String?> = _currentMediaId.asStateFlow()

    private var positionPollJob: Job? = null

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_ENDED -> {
                        // Hand off to JS for queue handling
                        emit("PlaybackEnded", Arguments.createMap().apply {
                            putString("mediaId", _currentMediaId.value)
                        })
                    }
                    Player.STATE_READY -> {
                        _duration.value = exoPlayer.duration.coerceAtLeast(0L)
                    }
                    Player.STATE_BUFFERING -> {
                        emit("PlaybackBuffering", Arguments.createMap())
                    }
                    Player.STATE_IDLE -> {
                        emit("PlaybackIdle", Arguments.createMap())
                    }
                }
            }

            override fun onIsPlayingChanged(playing: Boolean) {
                _isPlaying.value = playing
                emit("PlaybackPlaying", Arguments.createMap().apply {
                    putBoolean("isPlaying", playing)
                })
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                Log.e(TAG, "ExoPlayer error", error)
                emit("PlaybackError", Arguments.createMap().apply {
                    putString("message", error.message ?: "unknown")
                })
            }
        })

        exoPlayer.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val id = mediaItem?.mediaId
                _currentMediaId.value = id
                _duration.value = exoPlayer.duration.coerceAtLeast(0L)
                emit("PlaybackTrackChanged", Arguments.createMap().apply {
                    putString("mediaId", id ?: "")
                    putString("title", mediaItem?.mediaMetadata?.title?.toString() ?: "")
                    putString("artist", mediaItem?.mediaMetadata?.artist?.toString() ?: "")
                    putString("artwork", mediaItem?.mediaMetadata?.artworkUri?.toString() ?: "")
                })
            }
        })

        // Position polling → JS tick (RN uses this for the seek-bar UI).
        startPositionPolling()
    }

    private fun startPositionPolling() {
        positionPollJob?.cancel()
        positionPollJob = scope.launch {
            while (true) {
                if (exoPlayer.isPlaying) {
                    val pos = exoPlayer.currentPosition
                    val dur = exoPlayer.duration.coerceAtLeast(1L)
                    _position.value = pos
                    val percent = (pos.toDouble() / dur.toDouble()).coerceIn(0.0, 1.0)
                    if (pos % 250 < 50) {  // ~4Hz when running
                        emit("PlaybackTick", Arguments.createMap().apply {
                            putDouble("position", pos.toDouble())
                            putDouble("duration", dur.toDouble())
                            putDouble("percent", percent)
                        })
                    }
                }
                delay(250L)
            }
        }
    }

    fun play(streamUrl: String, trackTitle: String, trackArtist: String, artworkUrl: String?, mediaId: String): Boolean {
        if (streamUrl.isBlank()) return false
        val mediaItem = MediaItem.Builder()
            .setMediaId(mediaId)
            .setUri(Uri.parse(streamUrl))
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(trackTitle)
                    .setArtist(trackArtist)
                    .setArtworkUri(artworkUrl?.let { Uri.parse(it) })
                    .build()
            )
            .build()
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
        return true
    }

    fun pause() {
        exoPlayer.playWhenReady = false
    }

    fun resume() {
        exoPlayer.playWhenReady = true
    }

    fun togglePlay() {
        if (exoPlayer.isPlaying) pause() else resume()
    }

    fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs.coerceAtLeast(0L))
    }

    fun stop() {
        exoPlayer.stop()
        _currentMediaId.value = null
        _duration.value = 0L
        _position.value = 0L
    }

    fun release() {
        try {
            positionPollJob?.cancel()
            mediaSession.release()
            exoPlayer.release()
        } catch (_: Throwable) {}
        INSTANCE = null
    }

    private fun emit(event: String, params: com.facebook.react.bridge.WritableMap) {
        try {
            // Resolve the RN bridge context lazily. The playback service may run
            // before RN has booted; we silently skip in that window.
            val reactContext = getReactContext() ?: return
            if (reactContext.hasActiveReactInstance()) {
                reactContext
                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                    .emit(event, params)
            }
        } catch (t: Throwable) {
            Log.w(TAG, "Skipping emit: ${t.message}")
        }
    }

    companion object {
        private const val TAG = "PlaybackController"

        @Volatile
        private var INSTANCE: PlaybackController? = null

        fun get(context: Context): PlaybackController {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PlaybackController(context.applicationContext).also { INSTANCE = it }
            }
        }

        // Set by the playback module as soon as RN has a running React context.
        @Volatile
        @Suppress("ObjectPropertyName")
        var _reactContext: ReactApplicationContext? = null

        private fun getReactContext(): ReactApplicationContext? = _reactContext
    }
}
