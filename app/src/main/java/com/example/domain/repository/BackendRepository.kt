package com.example.domain.repository

import com.example.domain.model.AlbumDetails
import com.example.domain.model.ArtistDetails
import com.example.domain.model.AudioStreamInfo
import com.example.domain.model.BackendResult
import com.example.domain.model.PlaylistDetails
import com.example.domain.model.SearchResultItem
import com.example.domain.model.TrackDetails

interface BackendRepository {
    suspend fun search(query: String, filter: String? = null, page: Int = 1): BackendResult<List<SearchResultItem>>
    suspend fun getTrack(trackId: String): BackendResult<TrackDetails>
    suspend fun getAudioStream(trackId: String): BackendResult<AudioStreamInfo>
    suspend fun getRecommendations(trackId: String? = null): BackendResult<List<SearchResultItem>>
    suspend fun getPlaylist(playlistId: String): BackendResult<PlaylistDetails>
    suspend fun getAlbum(albumId: String): BackendResult<AlbumDetails>
    suspend fun getArtist(artistId: String): BackendResult<ArtistDetails>
    suspend fun getTrending(): BackendResult<List<SearchResultItem>>
}
