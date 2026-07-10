package com.suspended.app.data.remote

import com.chaquo.python.Python
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.suspended.app.domain.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YtDlpDataSource @Inject constructor(
    private val gson: Gson
) {
    private val python by lazy { Python.getInstance() }
    private val bridge by lazy { python.getModule("ytdlp_bridge") }

    suspend fun search(query: String, maxResults: Int = 15): List<Track> = withContext(Dispatchers.IO) {
        try {
            val resultJson = bridge.callAttr("search", query, maxResults).toString()
            val type = object : TypeToken<List<Map<String, Any?>>>() {}.type
            val rawResults: List<Map<String, Any?>> = gson.fromJson(resultJson, type)
            rawResults.map { map ->
                Track(
                    id = map["id"]?.toString() ?: "",
                    title = map["title"]?.toString() ?: "Unknown",
                    artist = map["uploader"]?.toString() ?: map["channel"]?.toString() ?: "Unknown Artist",
                    artistId = map["channel_id"]?.toString(),
                    duration = (map["duration"] as? Number)?.toLong() ?: 0L,
                    thumbnailUrl = map["thumbnail"]?.toString(),
                    albumName = map["album"]?.toString(),
                )
            }
        } catch (e: Exception) {
            throw RuntimeException("yt-dlp search failed: ${e.message}", e)
        }
    }

    suspend fun resolveStreamUrl(videoId: String): String = withContext(Dispatchers.IO) {
        try {
            bridge.callAttr("resolve", videoId).toString()
        } catch (e: Exception) {
            throw RuntimeException("yt-dlp resolve failed: ${e.message}", e)
        }
    }

    suspend fun download(videoId: String, outputDir: String): String = withContext(Dispatchers.IO) {
        try {
            bridge.callAttr("download", videoId, outputDir).toString()
        } catch (e: Exception) {
            throw RuntimeException("yt-dlp download failed: ${e.message}", e)
        }
    }
}
