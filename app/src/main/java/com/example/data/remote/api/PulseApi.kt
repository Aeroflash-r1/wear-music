package com.example.data.remote.api

import com.example.data.remote.dto.PulseArtist
import com.example.data.remote.dto.PulseHealth
import com.example.data.remote.dto.PulsePlaylist
import com.example.data.remote.dto.PulseSearchResponse
import com.example.data.remote.dto.PulseStream
import com.example.data.remote.dto.PulseTrack
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit interface for the self-hosted pulse-server (Ktor + yt-dlp).
 * The base URL is a placeholder — an OkHttp interceptor rewrites the host to the
 * server URL configured in Settings, so this interface is stable regardless of
 * the Tailscale address the user enters.
 */
interface PulseApi {

    @GET("api/search")
    suspend fun search(
        @Query("q") query: String,
        @Query("filter") filter: String? = null
    ): Response<PulseSearchResponse>

    @GET("api/streams/{id}")
    suspend fun streams(@Path("id") id: String): Response<PulseStream>

    @GET("api/track/{id}")
    suspend fun track(@Path("id") id: String): Response<PulseTrack>

    @GET("api/playlist/{id}")
    suspend fun playlist(@Path("id") id: String): Response<PulsePlaylist>

    @GET("api/channel/{id}")
    suspend fun channel(@Path("id") id: String): Response<PulseArtist>

    @GET("api/trending")
    suspend fun trending(): Response<PulseSearchResponse>

    @GET("health")
    suspend fun health(): Response<PulseHealth>
}
