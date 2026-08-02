package com.example.data.repository

import com.example.data.remote.MemoryCache
import com.example.data.remote.api.PulseApi
import com.example.data.remote.dto.PulseApiError
import com.example.data.remote.dto.PulseArtist
import com.example.data.remote.dto.PulsePlaylist
import com.example.data.remote.dto.PulseSearchItem
import com.example.data.remote.dto.PulseStream
import com.example.data.remote.dto.PulseTrack
import com.example.domain.model.AlbumDetails
import com.example.domain.model.ArtistDetails
import com.example.domain.model.AudioStreamInfo
import com.example.domain.model.BackendError
import com.example.domain.model.BackendResult
import com.example.domain.model.PlaylistDetails
import com.example.domain.model.SearchResultItem
import com.example.domain.model.TrackDetails
import com.example.domain.repository.BackendRepository
import com.squareup.moshi.Moshi
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Talks to the self-hosted pulse-server (Ktor + yt-dlp) exclusively. The old
 * Piped/Invidious instance-failover backend is gone — the server URL is the one
 * configured in Settings (see ServerConfig/ServerUrlInterceptor).
 */
@Singleton
class BackendRepositoryImpl @Inject constructor(
    private val pulseApi: PulseApi,
    private val memoryCache: MemoryCache,
    private val moshi: Moshi
) : BackendRepository {

    override suspend fun search(query: String, filter: String?, page: Int): BackendResult<List<SearchResultItem>> {
        val cacheKey = "search_${query}_${filter}_$page"
        memoryCache.get<List<SearchResultItem>>(cacheKey)?.let { return BackendResult.Success(it) }

        val result = call { pulseApi.search(query, filter) }.fold(
            onSuccess = { resp ->
                if (resp.isSuccessful) {
                    BackendResult.Success(resp.body()?.items.orEmpty().map { it.toDomain() })
                } else {
                    BackendResult.Error(mapHttpError(resp))
                }
            },
            onFailure = { BackendResult.Error(it.toBackendError()) }
        )

        if (result is BackendResult.Success) {
            memoryCache.put(cacheKey, result.data, ttlMs = 300_000L)
        }
        return result
    }

    override suspend fun getTrack(trackId: String): BackendResult<TrackDetails> {
        val cacheKey = "track_$trackId"
        memoryCache.get<TrackDetails>(cacheKey)?.let { return BackendResult.Success(it) }

        val result = call { pulseApi.track(trackId) }.fold(
            onSuccess = { resp ->
                if (resp.isSuccessful) {
                    val t = resp.body() ?: return@fold BackendResult.Error(BackendError.Unknown("Empty track response"))
                    BackendResult.Success(
                        TrackDetails(
                            id = t.id ?: trackId,
                            title = t.title ?: "Unknown Title",
                            artist = t.artist ?: "Unknown Artist",
                            album = t.album,
                            duration = t.duration ?: "0:00",
                            thumbnailUrl = t.thumbnailUrl,
                            streamUrl = t.streamUrl,
                            relatedTracks = t.relatedTracks.orEmpty().map { it.toDomain() }
                        )
                    )
                } else {
                    BackendResult.Error(mapHttpError(resp))
                }
            },
            onFailure = { BackendResult.Error(it.toBackendError()) }
        )

        if (result is BackendResult.Success) {
            memoryCache.put(cacheKey, result.data, ttlMs = 600_000L)
        }
        return result
    }

    override suspend fun getAudioStream(trackId: String): BackendResult<AudioStreamInfo> {
        val cacheKey = "stream_$trackId"
        memoryCache.get<AudioStreamInfo>(cacheKey)?.let { return BackendResult.Success(it) }

        val result = call { pulseApi.streams(trackId) }.fold(
            onSuccess = { resp ->
                if (resp.isSuccessful) {
                    val s = resp.body() ?: return@fold BackendResult.Error(BackendError.Unknown("Empty stream response"))
                    if (s.audioUrl.isNullOrBlank()) {
                        BackendResult.Error(BackendError.Unknown("No audio stream available"))
                    } else {
                        BackendResult.Success(
                            AudioStreamInfo(
                                audioUrl = s.audioUrl,
                                bitrate = s.bitrate ?: 128000,
                                mimeType = s.mimeType ?: "audio/mp4",
                                sampleRate = s.sampleRate ?: 44100
                            )
                        )
                    }
                } else {
                    BackendResult.Error(mapHttpError(resp))
                }
            },
            onFailure = { BackendResult.Error(it.toBackendError()) }
        )

        if (result is BackendResult.Success) {
            memoryCache.put(cacheKey, result.data, ttlMs = 3_000_000L)
        }
        return result
    }

    override suspend fun getRecommendations(trackId: String?): BackendResult<List<SearchResultItem>> {
        val cacheKey = "recs_${trackId ?: "default"}"
        memoryCache.get<List<SearchResultItem>>(cacheKey)?.let { return BackendResult.Success(it) }

        val result = if (trackId != null) {
            when (val trackRes = getTrack(trackId)) {
                is BackendResult.Success ->
                    // The server's track endpoint doesn't resolve related tracks yet, so
                    // fall back to trending rather than showing an empty rail.
                    if (trackRes.data.relatedTracks.isNotEmpty()) {
                        BackendResult.Success(trackRes.data.relatedTracks)
                    } else {
                        getTrending()
                    }
                is BackendResult.Error -> getTrending()
            }
        } else {
            getTrending()
        }

        if (result is BackendResult.Success) {
            memoryCache.put(cacheKey, result.data, ttlMs = 300_000L)
        }
        return result
    }

    override suspend fun getPlaylist(playlistId: String): BackendResult<PlaylistDetails> {
        val cacheKey = "playlist_$playlistId"
        memoryCache.get<PlaylistDetails>(cacheKey)?.let { return BackendResult.Success(it) }

        val result = call { pulseApi.playlist(playlistId) }.fold(
            onSuccess = { resp ->
                if (resp.isSuccessful) {
                    val p = resp.body() ?: return@fold BackendResult.Error(BackendError.Unknown("Empty playlist response"))
                    BackendResult.Success(
                        PlaylistDetails(
                            id = p.id ?: playlistId,
                            title = p.title ?: "Playlist",
                            author = p.author,
                            thumbnailUrl = p.thumbnailUrl,
                            tracks = p.tracks.orEmpty().map { it.toDomain() }
                        )
                    )
                } else {
                    BackendResult.Error(mapHttpError(resp))
                }
            },
            onFailure = { BackendResult.Error(it.toBackendError()) }
        )

        if (result is BackendResult.Success) {
            memoryCache.put(cacheKey, result.data, ttlMs = 600_000L)
        }
        return result
    }

    /** Albums are playlists on YouTube Music, so this is a thin wrapper over getPlaylist(). */
    override suspend fun getAlbum(albumId: String): BackendResult<AlbumDetails> {
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

    override suspend fun getArtist(artistId: String): BackendResult<ArtistDetails> {
        val cacheKey = "artist_$artistId"
        memoryCache.get<ArtistDetails>(cacheKey)?.let { return BackendResult.Success(it) }

        val result = call { pulseApi.channel(artistId) }.fold(
            onSuccess = { resp ->
                if (resp.isSuccessful) {
                    val a = resp.body() ?: return@fold BackendResult.Error(BackendError.Unknown("Empty artist response"))
                    BackendResult.Success(
                        ArtistDetails(
                            id = a.id ?: artistId,
                            name = a.name ?: "Unknown Artist",
                            thumbnailUrl = a.thumbnailUrl,
                            topTracks = a.topTracks.orEmpty().map { it.toDomain() }
                        )
                    )
                } else {
                    BackendResult.Error(mapHttpError(resp))
                }
            },
            onFailure = { BackendResult.Error(it.toBackendError()) }
        )

        if (result is BackendResult.Success) {
            memoryCache.put(cacheKey, result.data, ttlMs = 600_000L)
        }
        return result
    }

    override suspend fun getTrending(): BackendResult<List<SearchResultItem>> {
        val cacheKey = "trending"
        memoryCache.get<List<SearchResultItem>>(cacheKey)?.let { return BackendResult.Success(it) }

        val result = call { pulseApi.trending() }.fold(
            onSuccess = { resp ->
                if (resp.isSuccessful) {
                    BackendResult.Success(resp.body()?.items.orEmpty().map { it.toDomain() })
                } else {
                    BackendResult.Error(mapHttpError(resp))
                }
            },
            onFailure = { BackendResult.Error(it.toBackendError()) }
        )

        if (result is BackendResult.Success) {
            memoryCache.put(cacheKey, result.data, ttlMs = 600_000L)
        }
        return result
    }

    // ------------------------------------------------------------------ mapping

    private fun PulseSearchItem.toDomain(): SearchResultItem = SearchResultItem(
        id = id ?: "",
        title = title ?: "Unknown Title",
        artist = artist ?: "Unknown Artist",
        duration = duration ?: "0:00",
        thumbnailUrl = thumbnailUrl,
        type = type ?: "song"
    )

    private fun mapHttpError(resp: Response<*>): BackendError {
        // The server returns {"error": "..."} with actionable diagnostics (e.g.
        // "yt-dlp failed to search") — surface that instead of the generic HTTP
        // reason phrase so the user sees what actually went wrong.
        val serverMessage = runCatching {
            resp.errorBody()?.string()?.let { raw ->
                moshi.adapter(PulseApiError::class.java).fromJson(raw)?.error
            }
        }.getOrNull()

        return when (resp.code()) {
            401 -> BackendError.Unauthorized(serverMessage ?: resp.message() ?: "Unauthorized")
            404 -> BackendError.Unknown(serverMessage ?: "Not found (HTTP 404)")
            429 -> BackendError.RateLimited(serverMessage ?: resp.message() ?: "Rate limited")
            in 500..599 -> BackendError.BackendUnavailable(serverMessage ?: resp.message() ?: "Server error")
            in 400..499 -> BackendError.Unknown(serverMessage ?: "Request failed (HTTP ${resp.code()})")
            else -> BackendError.BackendUnavailable(serverMessage ?: "HTTP ${resp.code()}: ${resp.message()}")
        }
    }

    private fun Throwable.toBackendError(): BackendError = when (this) {
        is SocketTimeoutException -> BackendError.Timeout(message ?: "Timeout")
        is IOException -> BackendError.Network(message ?: "Network failure")
        else -> BackendError.Unknown(message ?: "Unknown error")
    }

    private suspend fun <T> call(block: suspend () -> T): Result<T> = runCatching { block() }
}
