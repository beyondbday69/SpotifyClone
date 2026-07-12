package com.suspendedrn.playback

import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

/**
 * Foreground media session service for background playback.
 *
 * The Kotlin app uses Hilt + PlaybackController. We keep it simpler:
 * PlaybackController.get(context) creates a singleton that survives
 * the service lifecycle.
 */
@OptIn(UnstableApi::class)
class PlaybackService : MediaSessionService() {

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession {
        return PlaybackController.get(this).mediaSession
    }

    override fun onTaskRemoved(rootIntent: android.content.Intent?) {
        val player = PlaybackController.get(this).exoPlayer
        if (!player.playWhenReady || player.mediaItemCount == 0) stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    override fun onStartCommand(intent: android.content.Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }
}
