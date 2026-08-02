package com.example.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PulseSearchResponse(
    @Json(name = "items") val items: List<PulseSearchItem>? = null
)

@JsonClass(generateAdapter = true)
data class PulseSearchItem(
    @Json(name = "id") val id: String? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "artist") val artist: String? = null,
    @Json(name = "duration") val duration: String? = null,
    @Json(name = "thumbnailUrl") val thumbnailUrl: String? = null,
    @Json(name = "type") val type: String? = null
)

@JsonClass(generateAdapter = true)
data class PulseStream(
    @Json(name = "audioUrl") val audioUrl: String? = null,
    @Json(name = "bitrate") val bitrate: Int? = null,
    @Json(name = "mimeType") val mimeType: String? = null,
    @Json(name = "sampleRate") val sampleRate: Int? = null
)

@JsonClass(generateAdapter = true)
data class PulseTrack(
    @Json(name = "id") val id: String? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "artist") val artist: String? = null,
    @Json(name = "album") val album: String? = null,
    @Json(name = "duration") val duration: String? = null,
    @Json(name = "thumbnailUrl") val thumbnailUrl: String? = null,
    @Json(name = "streamUrl") val streamUrl: String? = null,
    @Json(name = "relatedTracks") val relatedTracks: List<PulseSearchItem>? = null
)

@JsonClass(generateAdapter = true)
data class PulsePlaylist(
    @Json(name = "id") val id: String? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "author") val author: String? = null,
    @Json(name = "thumbnailUrl") val thumbnailUrl: String? = null,
    @Json(name = "tracks") val tracks: List<PulseSearchItem>? = null
)

@JsonClass(generateAdapter = true)
data class PulseArtist(
    @Json(name = "id") val id: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "thumbnailUrl") val thumbnailUrl: String? = null,
    @Json(name = "topTracks") val topTracks: List<PulseSearchItem>? = null
)

@JsonClass(generateAdapter = true)
data class PulseHealth(
    @Json(name = "status") val status: String? = null,
    @Json(name = "version") val version: String? = null,
    @Json(name = "ytDlpVersion") val ytDlpVersion: String? = null,
    @Json(name = "cache") val cache: String? = null
)

/** Mirrors the server's ApiError: `{"error": "..."}`. */
@JsonClass(generateAdapter = true)
data class PulseApiError(
    @Json(name = "error") val error: String? = null
)
