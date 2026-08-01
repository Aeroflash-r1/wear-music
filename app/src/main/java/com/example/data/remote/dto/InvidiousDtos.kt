package com.example.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class InvidiousSearchItem(
    @Json(name = "type") val type: String? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "videoId") val videoId: String? = null,
    @Json(name = "author") val author: String? = null,
    @Json(name = "authorId") val authorId: String? = null,
    @Json(name = "lengthSeconds") val lengthSeconds: Long? = null,
    @Json(name = "videoThumbnails") val videoThumbnails: List<InvidiousThumbnail>? = null,
    @Json(name = "playlistId") val playlistId: String? = null
)

@JsonClass(generateAdapter = true)
data class InvidiousThumbnail(
    @Json(name = "quality") val quality: String? = null,
    @Json(name = "url") val url: String? = null
)

@JsonClass(generateAdapter = true)
data class InvidiousVideoResponse(
    @Json(name = "title") val title: String? = null,
    @Json(name = "videoId") val videoId: String? = null,
    @Json(name = "author") val author: String? = null,
    @Json(name = "lengthSeconds") val lengthSeconds: Long? = null,
    @Json(name = "adaptiveFormats") val adaptiveFormats: List<InvidiousAdaptiveFormat>? = null,
    @Json(name = "recommendedVideos") val recommendedVideos: List<InvidiousSearchItem>? = null
)

@JsonClass(generateAdapter = true)
data class InvidiousAdaptiveFormat(
    @Json(name = "url") val url: String? = null,
    @Json(name = "type") val type: String? = null,
    @Json(name = "bitrate") val bitrate: String? = null,
    @Json(name = "container") val container: String? = null,
    @Json(name = "audioQuality") val audioQuality: String? = null,
    @Json(name = "audioSampleRate") val audioSampleRate: String? = null
)

@JsonClass(generateAdapter = true)
data class InvidiousPlaylistResponse(
    @Json(name = "title") val title: String? = null,
    @Json(name = "author") val author: String? = null,
    @Json(name = "playlistThumbnail") val playlistThumbnail: String? = null,
    @Json(name = "videos") val videos: List<InvidiousSearchItem>? = null
)

@JsonClass(generateAdapter = true)
data class InvidiousChannelResponse(
    @Json(name = "author") val author: String? = null,
    @Json(name = "authorId") val authorId: String? = null,
    @Json(name = "authorThumbnails") val authorThumbnails: List<InvidiousThumbnail>? = null,
    @Json(name = "latestVideos") val latestVideos: List<InvidiousSearchItem>? = null
)
