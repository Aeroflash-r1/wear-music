package com.example.data.remote

import com.example.data.remote.api.InvidiousApi
import com.example.data.remote.api.PipedApi
import com.example.domain.model.AlbumDetails
import com.example.domain.model.ArtistDetails
import com.example.domain.model.AudioStreamInfo
import com.example.domain.model.BackendError
import com.example.domain.model.BackendResult
import com.example.domain.model.PlaylistDetails
import com.example.domain.model.SearchResultItem
import com.example.domain.model.TrackDetails
import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackendProvider @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val moshiConverterFactory: MoshiConverterFactory
) {

    private val pipedInstances = listOf(
        "https://pipedapi.kavin.rocks/",
        "https://piped-api.garudalinux.org/",
        "https://api.piped.privacydev.net/"
    )

    private val invidiousInstances = listOf(
        "https://inv.tux.pizza/",
        "https://invidious.nerdvpn.de/",
        "https://invidious.projectsegfau.lt/"
    )

    private val blacklistedUntil = ConcurrentHashMap<String, Long>()
    private val apiCache = ConcurrentHashMap<String, Any>()

    private fun isBlacklisted(baseUrl: String): Boolean {
        val until = blacklistedUntil[baseUrl] ?: return false
        if (System.currentTimeMillis() > until) {
            blacklistedUntil.remove(baseUrl)
            return false
        }
        return true
    }

    private fun blacklist(baseUrl: String) {
        blacklistedUntil[baseUrl] = System.currentTimeMillis() + 60_000L // 1 minute blacklist
    }

    private fun getPipedApi(baseUrl: String): PipedApi {
        return apiCache.getOrPut(baseUrl) {
            Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(moshiConverterFactory)
                .build()
                .create(PipedApi::class.java)
        } as PipedApi
    }

    private fun getInvidiousApi(baseUrl: String): InvidiousApi {
        return apiCache.getOrPut(baseUrl) {
            Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(moshiConverterFactory)
                .build()
                .create(InvidiousApi::class.java)
        } as InvidiousApi
    }

    suspend fun <T : Any> executeWithFailover(
        block: suspend (PipedApi, InvidiousApi) -> T?
    ): BackendResult<T> {
        var backoffMs = 200L
        val availablePiped = pipedInstances.filter { !isBlacklisted(it) }
        val availableInvidious = invidiousInstances.filter { !isBlacklisted(it) }

        val targets = (availablePiped + availableInvidious).ifEmpty {
            pipedInstances + invidiousInstances
        }

        var lastError: BackendError = BackendError.BackendUnavailable("All backend providers failed")

        for (baseUrl in targets) {
            try {
                val pipedApi = getPipedApi(baseUrl)
                val invidiousApi = getInvidiousApi(baseUrl)
                val result = block(pipedApi, invidiousApi)
                if (result != null) {
                    return BackendResult.Success(result)
                }
            } catch (e: Exception) {
                blacklist(baseUrl)
                lastError = when {
                    e is java.net.SocketTimeoutException -> BackendError.Timeout(e.message ?: "Timeout")
                    e is java.io.IOException -> BackendError.Network(e.message ?: "Network failure")
                    else -> BackendError.Unknown(e.message ?: "Unknown error")
                }
                delay(backoffMs)
                backoffMs *= 2
            }
        }

        return BackendResult.Error(lastError)
    }

    suspend fun search(query: String, filter: String? = null, page: Int = 1): BackendResult<List<SearchResultItem>> {
        return executeWithFailover { pipedApi, invidiousApi ->
            try {
                val response = pipedApi.search(query, filter ?: "music_songs")
                if (response.isSuccessful) {
                    val items = response.body()?.items ?: emptyList()
                    return@executeWithFailover items.map { item ->
                        SearchResultItem(
                            id = extractId(item.url),
                            title = item.title ?: "Unknown Title",
                            artist = item.uploaderName ?: "Unknown Artist",
                            duration = formatDurationSeconds(item.duration ?: 0L),
                            thumbnailUrl = item.thumbnail,
                            type = item.type ?: "song"
                        )
                    }
                }
            } catch (_: Exception) {}

            val invResponse = invidiousApi.search(query)
            if (invResponse.isSuccessful) {
                val items = invResponse.body() ?: emptyList()
                return@executeWithFailover items.map { item ->
                    SearchResultItem(
                        id = item.videoId ?: item.playlistId ?: "",
                        title = item.title ?: "Unknown Title",
                        artist = item.author ?: "Unknown Artist",
                        duration = formatDurationSeconds(item.lengthSeconds ?: 0L),
                        thumbnailUrl = item.videoThumbnails?.firstOrNull()?.url,
                        type = item.type ?: "song"
                    )
                }
            }
            null
        }
    }

    suspend fun getAudioStream(trackId: String): BackendResult<AudioStreamInfo> {
        return executeWithFailover { pipedApi, invidiousApi ->
            try {
                val response = pipedApi.getStream(trackId)
                if (response.isSuccessful) {
                    val body = response.body()
                    val bestAudio = body?.audioStreams?.maxByOrNull { it.bitrate ?: 0 }
                    if (bestAudio?.url != null) {
                        return@executeWithFailover AudioStreamInfo(
                            audioUrl = bestAudio.url,
                            bitrate = bestAudio.bitrate ?: 128000,
                            mimeType = bestAudio.mimeType ?: "audio/webm",
                            sampleRate = 44100
                        )
                    }
                }
            } catch (_: Exception) {}

            val invResponse = invidiousApi.getVideo(trackId)
            if (invResponse.isSuccessful) {
                val body = invResponse.body()
                val bestAudio = body?.adaptiveFormats?.filter { it.type?.contains("audio") == true }
                    ?.maxByOrNull { it.bitrate?.toIntOrNull() ?: 0 }
                if (bestAudio?.url != null) {
                    return@executeWithFailover AudioStreamInfo(
                        audioUrl = bestAudio.url,
                        bitrate = bestAudio.bitrate?.toIntOrNull() ?: 128000,
                        mimeType = bestAudio.type ?: "audio/webm",
                        sampleRate = bestAudio.audioSampleRate?.toIntOrNull() ?: 44100
                    )
                }
            }
            null
        }
    }

    suspend fun getTrack(trackId: String): BackendResult<TrackDetails> {
        return executeWithFailover { pipedApi, _ ->
            val response = pipedApi.getStream(trackId)
            if (response.isSuccessful) {
                val body = response.body()
                val bestAudio = body?.audioStreams?.maxByOrNull { it.bitrate ?: 0 }
                val related = body?.relatedStreams?.map {
                    SearchResultItem(
                        id = extractId(it.url),
                        title = it.title ?: "",
                        artist = it.uploaderName ?: "",
                        duration = formatDurationSeconds(it.duration ?: 0L),
                        thumbnailUrl = it.thumbnail
                    )
                } ?: emptyList()

                return@executeWithFailover TrackDetails(
                    id = trackId,
                    title = body?.title ?: "Unknown Title",
                    artist = body?.uploader ?: "Unknown Artist",
                    album = "Pulse Mix",
                    duration = formatDurationSeconds(body?.duration ?: 0L),
                    thumbnailUrl = body?.thumbnailUrl,
                    streamUrl = bestAudio?.url,
                    relatedTracks = related
                )
            }
            null
        }
    }

    suspend fun getTrending(): BackendResult<List<SearchResultItem>> {
        return executeWithFailover { pipedApi, invidiousApi ->
            try {
                val response = pipedApi.getTrending("US")
                if (response.isSuccessful) {
                    val items = response.body() ?: emptyList()
                    return@executeWithFailover items.map { item ->
                        SearchResultItem(
                            id = extractId(item.url),
                            title = item.title ?: "",
                            artist = item.uploaderName ?: "",
                            duration = formatDurationSeconds(item.duration ?: 0L),
                            thumbnailUrl = item.thumbnail
                        )
                    }
                }
            } catch (_: Exception) {}

            val invResponse = invidiousApi.getTrending("music")
            if (invResponse.isSuccessful) {
                val items = invResponse.body() ?: emptyList()
                return@executeWithFailover items.map { item ->
                    SearchResultItem(
                        id = item.videoId ?: "",
                        title = item.title ?: "",
                        artist = item.author ?: "",
                        duration = formatDurationSeconds(item.lengthSeconds ?: 0L),
                        thumbnailUrl = item.videoThumbnails?.firstOrNull()?.url
                    )
                }
            }
            null
        }
    }

    /**
     * Fetches a real playlist by ID. YouTube Music "albums" are just playlists under
     * the hood (their IDs start with OLAK5uy_ instead of PL), so getAlbum() below reuses
     * this same call.
     */
    suspend fun getPlaylist(playlistId: String): BackendResult<PlaylistDetails> {
        return executeWithFailover { pipedApi, invidiousApi ->
            try {
                val response = pipedApi.getPlaylist(playlistId)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        val tracks = body.relatedStreams?.map { item ->
                            SearchResultItem(
                                id = extractId(item.url),
                                title = item.title ?: "Unknown Title",
                                artist = item.uploaderName ?: (body.uploader ?: "Unknown Artist"),
                                duration = formatDurationSeconds(item.duration ?: 0L),
                                thumbnailUrl = item.thumbnail
                            )
                        } ?: emptyList()
                        return@executeWithFailover PlaylistDetails(
                            id = playlistId,
                            title = body.name ?: "Playlist",
                            author = body.uploader,
                            thumbnailUrl = body.thumbnailUrl,
                            tracks = tracks
                        )
                    }
                }
            } catch (_: Exception) {}

            val invResponse = invidiousApi.getPlaylist(playlistId)
            if (invResponse.isSuccessful) {
                val body = invResponse.body()
                if (body != null) {
                    val tracks = body.videos?.map { item ->
                        SearchResultItem(
                            id = item.videoId ?: "",
                            title = item.title ?: "Unknown Title",
                            artist = item.author ?: (body.author ?: "Unknown Artist"),
                            duration = formatDurationSeconds(item.lengthSeconds ?: 0L),
                            thumbnailUrl = item.videoThumbnails?.firstOrNull()?.url
                        )
                    } ?: emptyList()
                    return@executeWithFailover PlaylistDetails(
                        id = playlistId,
                        title = body.title ?: "Playlist",
                        author = body.author,
                        thumbnailUrl = body.playlistThumbnail,
                        tracks = tracks
                    )
                }
            }
            null
        }
    }

    /** Albums are playlists on YouTube Music, so this is a thin wrapper over getPlaylist(). */
    suspend fun getAlbum(albumId: String): BackendResult<AlbumDetails> {
        return when (val result = getPlaylist(albumId)) {
            is BackendResult.Success -> BackendResult.Success(
                AlbumDetails(
                    id = albumId,
                    title = result.data.title,
                    artist = result.data.author ?: "Unknown Artist",
                    thumbnailUrl = result.data.thumbnailUrl,
                    tracks = result.data.tracks
                )
            )
            is BackendResult.Error -> result
        }
    }

    suspend fun getArtist(artistId: String): BackendResult<ArtistDetails> {
        return executeWithFailover { pipedApi, invidiousApi ->
            try {
                val response = pipedApi.getChannel(artistId)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        val topTracks = body.relatedStreams?.map { item ->
                            SearchResultItem(
                                id = extractId(item.url),
                                title = item.title ?: "Unknown Title",
                                artist = body.name ?: "Unknown Artist",
                                duration = formatDurationSeconds(item.duration ?: 0L),
                                thumbnailUrl = item.thumbnail
                            )
                        } ?: emptyList()
                        return@executeWithFailover ArtistDetails(
                            id = artistId,
                            name = body.name ?: "Unknown Artist",
                            thumbnailUrl = body.avatarUrl,
                            topTracks = topTracks
                            // No reliable "albums" concept exposed by Piped/Invidious channel
                            // endpoints, so this is left empty rather than fabricated.
                        )
                    }
                }
            } catch (_: Exception) {}

            val invResponse = invidiousApi.getChannel(artistId)
            if (invResponse.isSuccessful) {
                val body = invResponse.body()
                if (body != null) {
                    val topTracks = body.latestVideos?.map { item ->
                        SearchResultItem(
                            id = item.videoId ?: "",
                            title = item.title ?: "Unknown Title",
                            artist = body.author ?: "Unknown Artist",
                            duration = formatDurationSeconds(item.lengthSeconds ?: 0L),
                            thumbnailUrl = item.videoThumbnails?.firstOrNull()?.url
                        )
                    } ?: emptyList()
                    return@executeWithFailover ArtistDetails(
                        id = artistId,
                        name = body.author ?: "Unknown Artist",
                        thumbnailUrl = body.authorThumbnails?.firstOrNull()?.url,
                        topTracks = topTracks
                    )
                }
            }
            null
        }
    }

    private fun formatDurationSeconds(seconds: Long): String {
        val m = seconds / 60
        val s = seconds % 60
        return "%d:%02d".format(m, s)
    }

    /**
     * Piped returns relative URLs like "/watch?v=xxx" (songs), "/playlist?list=xxx"
     * (playlists/albums), or "/channel/xxx" (artists) instead of clean IDs. Normalize
     * all of them down to a bare ID so they're safe to use as nav-route path segments
     * and as direct arguments to getPlaylist/getChannel/getStream.
     */
    private fun extractId(url: String?): String {
        if (url.isNullOrBlank()) return ""
        url.substringAfter("watch?v=", "").let { if (it.isNotEmpty()) return it.substringBefore('&') }
        url.substringAfter("list=", "").let { if (it.isNotEmpty()) return it.substringBefore('&') }
        if (url.startsWith("/channel/")) return url.removePrefix("/channel/").substringBefore('/')
        return url.removePrefix("/")
    }
}
