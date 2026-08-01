package com.example.domain.repository

import com.example.domain.model.AlbumDetails
import com.example.domain.model.ArtistDetails
import com.example.domain.model.BackendResult
import com.example.domain.model.PlaylistDetails
import com.example.domain.model.SearchResultItem
import com.example.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface RecommendationRepository {
    suspend fun getRecommendations(trackId: String? = null): BackendResult<List<SearchResultItem>>
    suspend fun getTrending(): BackendResult<List<SearchResultItem>>
}

interface ArtistRepository {
    suspend fun getArtistDetails(artistId: String): BackendResult<ArtistDetails>
}

interface AlbumRepository {
    suspend fun getAlbumDetails(albumId: String): BackendResult<AlbumDetails>
}

interface PlaylistRepository {
    suspend fun getPlaylistDetails(playlistId: String): BackendResult<PlaylistDetails>
}

interface LibraryRepository {
    fun getFavoriteTracks(): Flow<List<Track>>
    fun getFavoriteAlbums(): Flow<List<AlbumDetails>>
    fun getFavoriteArtists(): Flow<List<ArtistDetails>>
    fun getFavoritePlaylists(): Flow<List<PlaylistDetails>>
    suspend fun toggleFavoriteTrack(track: Track)
    suspend fun toggleFavoriteAlbum(album: AlbumDetails)
    suspend fun toggleFavoriteArtist(artist: ArtistDetails)
    suspend fun toggleFavoritePlaylist(playlist: PlaylistDetails)
    suspend fun isFavorite(id: String): Boolean
}
