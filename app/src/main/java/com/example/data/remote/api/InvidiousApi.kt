package com.example.data.remote.api

import com.example.data.remote.dto.InvidiousChannelResponse
import com.example.data.remote.dto.InvidiousPlaylistResponse
import com.example.data.remote.dto.InvidiousSearchItem
import com.example.data.remote.dto.InvidiousVideoResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface InvidiousApi {

    @GET("api/v1/search")
    suspend fun search(
        @Query("q") query: String,
        @Query("type") type: String = "video"
    ): Response<List<InvidiousSearchItem>>

    @GET("api/v1/videos/{id}")
    suspend fun getVideo(
        @Path("id") id: String
    ): Response<InvidiousVideoResponse>

    @GET("api/v1/playlists/{id}")
    suspend fun getPlaylist(
        @Path("id") id: String
    ): Response<InvidiousPlaylistResponse>

    @GET("api/v1/channels/{id}")
    suspend fun getChannel(
        @Path("id") id: String
    ): Response<InvidiousChannelResponse>

    @GET("api/v1/trending")
    suspend fun getTrending(
        @Query("type") type: String = "music"
    ): Response<List<InvidiousSearchItem>>
}
