package com.suspendedrn.playback

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod

/**
 * JS-facing playback controller. Exposes:
 *   - play(streamUrl, title, artist, artworkUrl, mediaId)
 *   - pause() / resume() / togglePlay()
 *   - seek(positionMs)
 *   - stop() / release()
 *   - getState() returns a snapshot of {isPlaying, position, duration, mediaId}
 *
 * Paired with PlaybackController.kt and wired into PlaybackService via
 * the singleton PlaybackController.get() facade.
 */
class PlaybackModule(private val reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {

    override fun getName(): String = NAME

    init {
        PlaybackController._reactContext = reactContext
    }

    private val controller: PlaybackController
        get() = PlaybackController.get(reactContext)

    @ReactMethod
    fun play(streamUrl: String, title: String, artist: String, artworkUrl: String?, mediaId: String, promise: Promise) {
        val started = controller.play(streamUrl, title, artist, artworkUrl, mediaId)
        promise.resolve(started)
    }

    @ReactMethod
    fun pause(promise: Promise) {
        controller.pause()
        promise.resolve(true)
    }

    @ReactMethod
    fun resume(promise: Promise) {
        controller.resume()
        promise.resolve(true)
    }

    @ReactMethod
    fun togglePlay(promise: Promise) {
        controller.togglePlay()
        promise.resolve(controller.isPlaying.value)
    }

    @ReactMethod
    fun seek(positionMs: Double, promise: Promise) {
        controller.seekTo(positionMs.toLong())
        promise.resolve(true)
    }

    @ReactMethod
    fun stop(promise: Promise) {
        controller.stop()
        promise.resolve(true)
    }

    @ReactMethod
    fun getState(promise: Promise) {
        val state = Arguments.createMap().apply {
            putBoolean("isPlaying", controller.isPlaying.value)
            putDouble("position", controller.position.value.toDouble())
            putDouble("duration", controller.duration.value.toDouble())
            putString("mediaId", controller.currentMediaId.value ?: "")
        }
        promise.resolve(state)
    }

    companion object {
        const val NAME = "Playback"
    }
}
