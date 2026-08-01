package com.pulse.server

import kotlinx.serialization.Serializable

@Serializable
data class ApiSearchItem(
    val id: String,
    val title: String,
    val artist: String,
    val duration: String,
    val thumbnailUrl: String? = null,
    val type: String = "song"
)

@Serializable
data class ApiStream(
    val audioUrl: String,
    val bitrate: Int = 128000,
    val mimeType: String = "audio/mp4",
    val sampleRate: Int = 44100
)

@Serializable
data class ApiTrack(
    val id: String,
    val title: String,
    val artist: String,
    val album: String? = null,
    val duration: String,
    val thumbnailUrl: String? = null,
    val streamUrl: String? = null,
    val relatedTracks: List<ApiSearchItem> = emptyList()
)

@Serializable
data class ApiPlaylist(
    val id: String,
    val title: String,
    val author: String? = null,
    val thumbnailUrl: String? = null,
    val tracks: List<ApiSearchItem> = emptyList()
)

@Serializable
data class ApiArtist(
    val id: String,
    val name: String,
    val thumbnailUrl: String? = null,
    val topTracks: List<ApiSearchItem> = emptyList()
)

@Serializable
data class ApiAlbum(
    val id: String,
    val title: String,
    val artist: String,
    val thumbnailUrl: String? = null,
    val tracks: List<ApiSearchItem> = emptyList()
)

@Serializable
data class ApiError(val error: String)

@Serializable
data class ApiHealth(
    val status: String,
    val version: String,
    val ytDlpVersion: String? = null,
    val cache: String = ""
)

internal fun YtDlpPlaylist.avatarUrl(): String? =
    thumbnails?.firstOrNull()?.url ?: thumbnail
