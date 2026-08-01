package com.example.data.repository

import com.example.data.remote.BackendProvider
import com.example.data.remote.MemoryCache
import com.example.domain.model.AlbumDetails
import com.example.domain.model.ArtistDetails
import com.example.domain.model.AudioStreamInfo
import com.example.domain.model.BackendResult
import com.example.domain.model.PlaylistDetails
import com.example.domain.model.SearchResultItem
import com.example.domain.model.TrackDetails
import com.example.domain.repository.BackendRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackendRepositoryImpl @Inject constructor(
    private val backendProvider: BackendProvider,
    private val memoryCache: MemoryCache
) : BackendRepository {

    override suspend fun search(query: String, filter: String?, page: Int): BackendResult<List<SearchResultItem>> {
        val cacheKey = "search_${query}_${filter}_$page"
        val cached: List<SearchResultItem>? = memoryCache.get(cacheKey)
        if (cached != null) {
            return BackendResult.Success(cached)
        }

        val result = backendProvider.search(query, filter, page)
        if (result is BackendResult.Success) {
            memoryCache.put(cacheKey, result.data, ttlMs = 300_000L) // Cache for 5 mins
        }
        return result
    }

    override suspend fun getTrack(trackId: String): BackendResult<TrackDetails> {
        val cacheKey = "track_$trackId"
        val cached: TrackDetails? = memoryCache.get(cacheKey)
        if (cached != null) {
            return BackendResult.Success(cached)
        }

        val result = backendProvider.getTrack(trackId)
        if (result is BackendResult.Success) {
            memoryCache.put(cacheKey, result.data, ttlMs = 600_000L)
        }
        return result
    }

    override suspend fun getAudioStream(trackId: String): BackendResult<AudioStreamInfo> {
        val cacheKey = "stream_$trackId"
        val cached: AudioStreamInfo? = memoryCache.get(cacheKey)
        if (cached != null && cached.expiresAt > System.currentTimeMillis()) {
            return BackendResult.Success(cached)
        }

        val result = backendProvider.getAudioStream(trackId)
        if (result is BackendResult.Success) {
            memoryCache.put(cacheKey, result.data, ttlMs = 3_000_000L)
        }
        return result
    }

    override suspend fun getRecommendations(trackId: String?): BackendResult<List<SearchResultItem>> {
        val cacheKey = "recs_${trackId ?: "default"}"
        val cached: List<SearchResultItem>? = memoryCache.get(cacheKey)
        if (cached != null) {
            return BackendResult.Success(cached)
        }

        val result = if (trackId != null) {
            when (val trackRes = getTrack(trackId)) {
                is BackendResult.Success -> BackendResult.Success(trackRes.data.relatedTracks)
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
        val cached: PlaylistDetails? = memoryCache.get(cacheKey)
        if (cached != null) return BackendResult.Success(cached)

        val result = backendProvider.getPlaylist(playlistId)
        if (result is BackendResult.Success) {
            memoryCache.put(cacheKey, result.data, ttlMs = 600_000L)
        }
        return result
    }

    override suspend fun getAlbum(albumId: String): BackendResult<AlbumDetails> {
        val cacheKey = "album_$albumId"
        val cached: AlbumDetails? = memoryCache.get(cacheKey)
        if (cached != null) return BackendResult.Success(cached)

        val result = backendProvider.getAlbum(albumId)
        if (result is BackendResult.Success) {
            memoryCache.put(cacheKey, result.data, ttlMs = 600_000L)
        }
        return result
    }

    override suspend fun getArtist(artistId: String): BackendResult<ArtistDetails> {
        val cacheKey = "artist_$artistId"
        val cached: ArtistDetails? = memoryCache.get(cacheKey)
        if (cached != null) return BackendResult.Success(cached)

        val result = backendProvider.getArtist(artistId)
        if (result is BackendResult.Success) {
            memoryCache.put(cacheKey, result.data, ttlMs = 600_000L)
        }
        return result
    }

    override suspend fun getTrending(): BackendResult<List<SearchResultItem>> {
        val cacheKey = "trending"
        val cached: List<SearchResultItem>? = memoryCache.get(cacheKey)
        if (cached != null) {
            return BackendResult.Success(cached)
        }

        val result = backendProvider.getTrending()
        if (result is BackendResult.Success) {
            memoryCache.put(cacheKey, result.data, ttlMs = 600_000L)
        }
        return result
    }
}
