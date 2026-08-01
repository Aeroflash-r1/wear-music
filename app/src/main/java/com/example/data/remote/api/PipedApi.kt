package com.example.data.remote.api

import com.example.data.remote.dto.PipedChannelResponse
import com.example.data.remote.dto.PipedPlaylistResponse
import com.example.data.remote.dto.PipedSearchItem
import com.example.data.remote.dto.PipedSearchResponse
import com.example.data.remote.dto.PipedStreamResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PipedApi {

    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("filter") filter: String = "music_songs"
    ): Response<PipedSearchResponse>

    @GET("streams/{videoId}")
    suspend fun getStream(
        @Path("videoId") videoId: String
    ): Response<PipedStreamResponse>

    @GET("playlists/{playlistId}")
    suspend fun getPlaylist(
        @Path("playlistId") playlistId: String
    ): Response<PipedPlaylistResponse>

    @GET("channel/{channelId}")
    suspend fun getChannel(
        @Path("channelId") channelId: String
    ): Response<PipedChannelResponse>

    @GET("trending")
    suspend fun getTrending(
        @Query("region") region: String = "US"
    ): Response<List<PipedSearchItem>>
}
