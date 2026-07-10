package com.suspended.app.playback

import com.suspended.app.domain.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class QueueManager {
    private val _queue = MutableStateFlow<List<Track>>(emptyList())
    val queue: StateFlow<List<Track>> = _queue.asStateFlow()

    private var originalQueue = listOf<Track>()
    private var currentIndex = 0
    private var isShuffled = false

    fun setQueue(tracks: List<Track>, startIndex: Int = 0) {
        originalQueue = tracks
        currentIndex = startIndex
        _queue.value = if (isShuffled) {
            val current = tracks[startIndex]
            val shuffled = tracks.toMutableList().apply {
                removeAt(startIndex)
                shuffle()
                add(0, current)
            }
            currentIndex = 0
            shuffled
        } else {
            tracks
        }
    }

    fun next(): Track? {
        val q = _queue.value
        if (q.isEmpty()) return null
        currentIndex = (currentIndex + 1).let {
            if (it >= q.size) 0 else it
        }
        return q.getOrNull(currentIndex)
    }

    fun previous(): Track? {
        val q = _queue.value
        if (q.isEmpty()) return null
        currentIndex = (currentIndex - 1).let {
            if (it < 0) q.size - 1 else it
        }
        return q.getOrNull(currentIndex)
    }

    fun addToQueue(track: Track) {
        _queue.value = _queue.value + track
        originalQueue = originalQueue + track
    }

    fun setShuffle(shuffle: Boolean) {
        isShuffled = shuffle
        val current = _queue.value.getOrNull(currentIndex)
        if (shuffle) {
            val shuffled = _queue.value.toMutableList().apply {
                if (current != null) {
                    remove(current)
                    shuffle()
                    add(0, current)
                } else {
                    shuffle()
                }
            }
            _queue.value = shuffled
            currentIndex = 0
        } else {
            _queue.value = originalQueue
            currentIndex = if (current != null) originalQueue.indexOf(current).coerceAtLeast(0) else 0
        }
    }

    fun currentTrack(): Track? = _queue.value.getOrNull(currentIndex)
}
