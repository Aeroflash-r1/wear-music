package com.pulse.server

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

/**
 * Wraps the yt-dlp binary: builds commands, runs the subprocess with a hard
 * timeout, parses JSON output, and maps it into the server's API models.
 *
 * Every yt-dlp call runs on [Dispatchers.IO] so the Ktor event loop is never
 * blocked — a slow search must not freeze /health or concurrent requests.
 * Methods return `null` when yt-dlp itself failed (missing binary, timeout,
 * non-zero exit) so the route layer can return a real 5xx instead of a
 * misleading "empty results".
 */
class YtDlpClient(private val config: ServerConfig) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val searchCache = TtlCache<String, List<ApiSearchItem>>(15 * 60_000L)
    private val streamCache = TtlCache<String, ApiStream>(60 * 60_000L)
    private val trackCache = TtlCache<String, ApiTrack>(60 * 60_000L)
    private val playlistCache = TtlCache<String, ApiPlaylist>(60 * 60_000L)
    private val artistCache = TtlCache<String, ApiArtist>(60 * 60_000L)
    private val trendingCache = TtlCache<String, List<ApiSearchItem>>(30 * 60_000L)

    private val cacheTtlMap = mapOf(
        "search" to searchCache,
        "stream" to streamCache,
        "track" to trackCache,
        "playlist" to playlistCache,
        "artist" to artistCache,
        "trending" to trendingCache
    )

    fun cacheStats(): String = cacheTtlMap.entries.joinToString(", ") { "${it.key}:${it.value.size()}" }

    // ------------------------------------------------------------------ yt-dlp

    /**
     * Runs yt-dlp with the given args. Returns stdout on success, `null` on any
     * failure (launch error, timeout, non-zero exit). Runs on Dispatchers.IO so
     * the caller's dispatcher is never blocked by the subprocess.
     */
    private suspend fun run(vararg args: String): String? = withContext(Dispatchers.IO) {
        val process = try {
            ProcessBuilder(listOf(config.ytDlpBin) + args)
                .redirectErrorStream(true)
                .start()
        } catch (e: Exception) {
            System.err.println("yt-dlp launch failed: ${e.message}")
            null
        } ?: return@withContext null

        // Read stdout on a worker thread so the waitFor timeout below can actually
        // fire — a hung yt-dlp must not block the server thread forever.
        val outputFuture = java.util.concurrent.CompletableFuture.supplyAsync {
            process.inputStream.bufferedReader().readText()
        }

        if (!process.waitFor(config.ytDlpTimeoutSeconds, TimeUnit.SECONDS)) {
            process.destroyForcibly()
            outputFuture.cancel(true)
            System.err.println("yt-dlp timed out after ${config.ytDlpTimeoutSeconds}s: ${args.joinToString(" ").take(120)}")
            null
        } else if (process.exitValue() != 0) {
            System.err.println("yt-dlp exited ${process.exitValue()}: ${args.joinToString(" ").take(120)}")
            null
        } else {
            try {
                // Process exited cleanly — the reader hits EOF quickly, so a
                // generous timeout only guards against reader deadlock.
                outputFuture.get(30, TimeUnit.SECONDS)
            } catch (e: Exception) {
                System.err.println("yt-dlp output read failed: ${e.message}")
                null
            }
        }
    }

    private fun parseVideo(raw: String): YtDlpVideo? = runCatching { json.decodeFromString<YtDlpVideo>(raw) }.getOrNull()

    private fun parsePlaylist(raw: String): YtDlpPlaylist? = runCatching {
        json.decodeFromString<YtDlpPlaylist>(raw)
    }.getOrNull()

    private fun encodeQuery(query: String): String =
        URLEncoder.encode(query, StandardCharsets.UTF_8).replace("+", "%20")

    // ------------------------------------------------------------------ mapping

    private fun YtDlpEntry.toSearchItem(type: String): ApiSearchItem = ApiSearchItem(
        id = id ?: extractId(url),
        title = title ?: "Unknown Title",
        artist = uploader ?: channel ?: "Unknown Artist",
        duration = formatDuration(duration ?: 0L),
        thumbnailUrl = thumbnails?.lastOrNull()?.url,
        type = type
    )

    private fun formatDuration(seconds: Long): String {
        val m = seconds / 60
        val s = seconds % 60
        return "%d:%02d".format(m, s)
    }

    private fun extractId(url: String?): String {
        if (url.isNullOrBlank()) return ""
        url.substringAfter("watch?v=", "").let { if (it.isNotEmpty()) return it.substringBefore('&') }
        url.substringAfter("list=", "").let { if (it.isNotEmpty()) return it.substringBefore('&') }
        if (url.startsWith("/channel/")) return url.removePrefix("/channel/").substringBefore('/')
        if (url.startsWith("https://www.youtube.com/channel/")) return url.removePrefix("https://www.youtube.com/channel/").substringBefore('/')
        return url.removePrefix("/").removePrefix("https://www.youtube.com/")
    }

    private fun mimeFor(ext: String?): String = when (ext) {
        "m4a", "mp4", "aac" -> "audio/mp4"
        "webm" -> "audio/webm"
        "opus" -> "audio/ogg"
        else -> "audio/${ext ?: "mp4"}"
    }

    // ------------------------------------------------------------------ API ops

    /**
     * Searches YouTube Music-style. `filter` mirrors the app: music_songs,
     * music_albums, channels. Returns `null` when yt-dlp failed; an empty list
     * means the search genuinely had no hits.
     */
    suspend fun search(query: String, filter: String?): List<ApiSearchItem>? {
        val cacheKey = "$filter::$query"
        searchCache.get(cacheKey)?.let { return it }

        val items = when (filter) {
            "music_albums" -> searchAlbums(query)
            "channels" -> searchChannels(query)
            else -> searchSongs(query)
        }

        if (items != null && items.isNotEmpty()) searchCache.put(cacheKey, items)
        return items
    }

    private suspend fun searchSongs(query: String): List<ApiSearchItem>? {
        // --flat-playlist keeps this to a single fast request (like albums/channels);
        // full --dump-json extraction of 10 videos would take 20-60s on a phone.
        val raw = run("ytsearch10:$query", "--flat-playlist", "--dump-single-json", "--no-warnings", "--no-playlist")
            ?: return null
        val playlist = parsePlaylist(raw) ?: return null
        return playlist.entries.orEmpty().mapNotNull { it.toSearchItem("song") }
    }

    private suspend fun searchAlbums(query: String): List<ApiSearchItem>? {
        val url = "https://www.youtube.com/results?search_query=${encodeQuery(query)}&sp=EgJAAQ%3D%3D"
        val raw = run(url, "--flat-playlist", "--dump-single-json", "--no-warnings") ?: return null
        val playlist = parsePlaylist(raw) ?: return null
        return playlist.entries.orEmpty().mapNotNull { it.toSearchItem("album") }
    }

    private suspend fun searchChannels(query: String): List<ApiSearchItem>? {
        val url = "https://www.youtube.com/results?search_query=${encodeQuery(query)}&sp=EgIQAg%3D%3D"
        val raw = run(url, "--flat-playlist", "--dump-single-json", "--no-warnings") ?: return null
        val playlist = parsePlaylist(raw) ?: return null
        return playlist.entries.orEmpty().mapNotNull { it.toSearchItem("artist") }
    }

    suspend fun getAudioStream(videoId: String): ApiStream? {
        streamCache.get(videoId)?.let { return it }
        val raw = run(
            "https://www.youtube.com/watch?v=$videoId",
            "--dump-json", "--no-playlist",
            "-f", "bestaudio[ext=m4a]/bestaudio",
            "--no-warnings"
        ) ?: return null
        val video = parseVideo(raw) ?: return null
        val stream = ApiStream(
            audioUrl = video.url ?: return null,
            bitrate = (video.abr?.toInt() ?: 128) * 1000,
            mimeType = mimeFor(video.ext),
            sampleRate = video.asr?.toInt() ?: 44100
        )
        streamCache.put(videoId, stream)
        return stream
    }

    suspend fun getTrack(videoId: String): ApiTrack? {
        trackCache.get(videoId)?.let { return it }
        val raw = run(
            "https://www.youtube.com/watch?v=$videoId",
            "--dump-json", "--no-playlist",
            "-f", "bestaudio[ext=m4a]/bestaudio",
            "--no-warnings"
        ) ?: return null
        val video = parseVideo(raw) ?: return null
        val track = ApiTrack(
            id = video.id ?: videoId,
            title = video.title ?: "Unknown Title",
            artist = video.uploader ?: "Unknown Artist",
            album = null,
            duration = formatDuration(video.duration ?: 0L),
            thumbnailUrl = video.thumbnail ?: video.thumbnails?.lastOrNull()?.url,
            streamUrl = video.url,
            relatedTracks = emptyList()
        )
        trackCache.put(videoId, track)
        return track
    }

    suspend fun getPlaylist(playlistId: String): ApiPlaylist? {
        playlistCache.get(playlistId)?.let { return it }
        val raw = run(
            "https://www.youtube.com/playlist?list=$playlistId",
            "--flat-playlist", "--dump-single-json", "--no-warnings"
        ) ?: return null
        val playlist = parsePlaylist(raw) ?: return null
        val result = ApiPlaylist(
            id = playlist.id ?: playlistId,
            title = playlist.title ?: "Playlist",
            author = playlist.uploader,
            thumbnailUrl = playlist.thumbnail ?: playlist.thumbnails?.lastOrNull()?.url,
            tracks = playlist.entries.orEmpty().mapNotNull { it.toSearchItem("song") }
        )
        playlistCache.put(playlistId, result)
        return result
    }

    suspend fun getArtist(channelId: String): ApiArtist? {
        artistCache.get(channelId)?.let { return it }
        val raw = run(
            "https://www.youtube.com/channel/$channelId",
            "--flat-playlist", "--dump-single-json", "--no-warnings"
        ) ?: return null
        val playlist = parsePlaylist(raw) ?: return null
        val result = ApiArtist(
            id = playlist.id ?: channelId,
            name = playlist.title ?: "Unknown Artist",
            thumbnailUrl = playlist.avatarUrl(),
            topTracks = playlist.entries.orEmpty().mapNotNull { it.toSearchItem("song") }
        )
        artistCache.put(channelId, result)
        return result
    }

    suspend fun getTrending(): List<ApiSearchItem>? {
        trendingCache.get("global")?.let { return it }
        val raw = run(
            "https://www.youtube.com/feed/trending",
            "--flat-playlist", "--dump-single-json", "--no-warnings"
        ) ?: return null
        val playlist = parsePlaylist(raw) ?: return null
        val items = playlist.entries.orEmpty().mapNotNull { it.toSearchItem("song") }
        if (items.isNotEmpty()) trendingCache.put("global", items)
        return items
    }

    suspend fun health(): ApiHealth = ApiHealth(
        status = "ok",
        version = "1.0.0",
        ytDlpVersion = run("--version")?.trim(),
        cache = cacheStats()
    )
}
