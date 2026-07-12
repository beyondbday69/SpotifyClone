package com.suspendedrn.bridge

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableArray
import com.facebook.react.bridge.WritableMap
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.chaquo.python.Python
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Native module that exposes the embedded yt-dlp python bridge to JS.
 *
 * Mirrors the Kotlin app's `MusipApi` shape:
 *   - search(query, maxResults) -> [{id, title, artist, duration, thumbnailUrl, ...}, ...]
 *   - resolveStreamUrl(videoId) -> String url
 *   - download(videoId, dir) -> String filepath
 *
 * Heavy work (yt-dlp calls) runs on Dispatchers.IO. The blocking python
 * invocation lives in `runOnIoBlocking` which uses the Chaquopy-blessed
 * main python thread dispatch.
 */
class YtDlpModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    override fun getName(): String = NAME

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val gson = Gson()

    @ReactMethod
    fun search(query: String, maxResults: Int, promise: Promise) {
        scope.launch {
            runCatching {
                withContext(Dispatchers.Main) {
                    runSearch(query, maxResults)
                }
            }
                .onSuccess { result -> promise.resolve(result.toJsArray()) }
                .onFailure { e -> promise.reject("E_SEARCH", e.message ?: "search failed", e) }
        }
    }

    @ReactMethod
    fun resolveStreamUrl(videoId: String, promise: Promise) {
        scope.launch {
            runCatching {
                withContext(Dispatchers.Main) {
                    runResolve(videoId)
                }
            }
                .onSuccess { url -> promise.resolve(url) }
                .onFailure { e -> promise.reject("E_RESOLVE", e.message ?: "resolve failed", e) }
        }
    }

    @ReactMethod
    fun download(videoId: String, outputDir: String, promise: Promise) {
        scope.launch {
            runCatching {
                withContext(Dispatchers.Main) {
                    runDownload(videoId, outputDir)
                }
            }
                .onSuccess { path -> promise.resolve(path) }
                .onFailure { e -> promise.reject("E_DOWNLOAD", e.message ?: "download failed", e) }
        }
    }

    @ReactMethod
    fun deleteVideo(videoId: String, promise: Promise) {
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    val dir = filesDirForDownloads()
                    for (ext in listOf("opus", "webm", "m4a", "mp3")) {
                        val candidate = File(dir, "$videoId.$ext")
                        if (candidate.exists()) candidate.delete()
                    }
                }
            }
                .onSuccess { promise.resolve(true) }
                .onFailure { e -> promise.reject("E_DELETE", e.message ?: "delete failed", e) }
        }
    }

    // --- repo re-uses: state cache + utility exposed to JS ---

    /**
     * Persist a transient stream URL so we can replay it without re-resolving.
     * Mirrors the cacheStreamUrl() on MusicRepository.
     */
    @ReactMethod
    fun cacheStreamUrl(videoId: String, url: String, promise: Promise) {
        scope.launch {
            runCatching {
                SharedStreamCache.put(videoId, url)
            }
                .onSuccess { promise.resolve(true) }
                .onFailure { e -> promise.reject("E_CACHE", e.message, e) }
        }
    }

    @ReactMethod
    fun getCachedStreamUrl(videoId: String, promise: Promise) {
        scope.launch {
            runCatching { SharedStreamCache.get(videoId) }
                .onSuccess { url -> promise.resolve(url ?: "") }
                .onFailure { e -> promise.reject("E_CACHEREAD", e.message, e) }
        }
    }

    // ---------------- internal: runs on main thread (Python interpreter owns main thread) ----------------

    private fun runSearch(query: String, maxResults: Int): List<Map<String, Any?>> {
        val python = Python.getInstance()
        val bridge = python.getModule("ytdlp_bridge")
        val json = bridge.callAttr("search", query, maxResults).toString()
        val type = object : TypeToken<List<Map<String, Any?>>>() {}.type
        @Suppress("UNCHECKED_CAST")
        return gson.fromJson<List<Map<String, Any?>>>(json, type) ?: emptyList()
    }

    private fun runResolve(videoId: String): String {
        val python = Python.getInstance()
        val bridge = python.getModule("ytdlp_bridge")
        return bridge.callAttr("resolve", videoId).toString()
    }

    private fun runDownload(videoId: String, outputDir: String): String {
        val dir = if (outputDir.isNotBlank()) File(outputDir) else filesDirForDownloads()
        dir.mkdirs()
        val python = Python.getInstance()
        val bridge = python.getModule("ytdlp_bridge")
        return bridge.callAttr("download", videoId, dir.absolutePath).toString()
    }

    private fun filesDirForDownloads(): File {
        val ctx = reactApplicationContext
        val external: File? = ctx.getExternalFilesDir(null)
        val base = external ?: ctx.filesDir
        return File(base, "downloads")
    }

    // ---------------- JS <-> Kotlin converter ----------------

    private fun List<Map<String, Any?>>.toJsArray(): WritableArray {
        val arr: WritableArray = Arguments.createArray()
        forEach { row ->
            arr.pushMap(row.toJsMap())
        }
        return arr
    }

    private fun Map<String, Any?>.toJsMap(): WritableMap {
        val out: WritableMap = Arguments.createMap()
        forEach { (k, v) ->
            when (v) {
                null -> out.putNull(k)
                is Boolean -> out.putBoolean(k, v)
                is Int -> out.putInt(k, v)
                is Long -> out.putDouble(k, v.toDouble())
                is Double -> out.putDouble(k, v)
                is Float -> out.putDouble(k, v.toDouble())
                is Number -> out.putDouble(k, v.toDouble())
                else -> out.putString(k, v.toString())
            }
        }
        // Always normalize to the App's Track shape so JS can ignore the raw yt-dlp fields.
        val id = this["id"]?.toString() ?: ""
        out.merge(
            Arguments.createMap().apply {
                putString("id", id)
                putString("title", this@toJsMap["title"]?.toString() ?: "Unknown")
                putString(
                    "artist",
                    this@toJsMap["uploader"]?.toString()
                        ?: this@toJsMap["channel"]?.toString()
                        ?: "Unknown Artist"
                )
                putString("artistId", this@toJsMap["channel_id"]?.toString() ?: "")
                putDouble(
                    "duration",
                    (this@toJsMap["duration"] as? Number)?.toDouble() ?: 0.0
                )
                putString("thumbnailUrl", this@toJsMap["thumbnail"]?.toString() ?: "")
                putString("albumName", this@toJsMap["album"]?.toString() ?: "")
            }
        )
        return out
    }

    companion object {
        const val NAME = "YtDlp"
    }
}

/**
 * Tiny in-memory cache of resolved stream URLs.
 * The Kotlin app mirrors this with a Room table (`stream_cache`); the RN port
 * keeps it in-memory for simplicity and re-resolves as needed. Replace with a
 * Room/SQLite-backed cache later if needed.
 */
internal object SharedStreamCache {
    private val cache = HashMap<String, String>()

    @Synchronized
    fun put(id: String, url: String) {
        cache[id] = url
    }

    @Synchronized
    fun get(id: String): String? = cache[id]
}
