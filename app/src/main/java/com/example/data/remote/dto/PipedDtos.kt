package com.example.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PipedSearchResponse(
    @Json(name = "items") val items: List<PipedSearchItem>? = null,
    @Json(name = "nextpage") val nextPage: String? = null
)

@JsonClass(generateAdapter = true)
data class PipedSearchItem(
    @Json(name = "url") val url: String? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "uploaderName") val uploaderName: String? = null,
    @Json(name = "uploaderUrl") val uploaderUrl: String? = null,
    @Json(name = "duration") val duration: Long? = null,
    @Json(name = "thumbnail") val thumbnail: String? = null,
    @Json(name = "type") val type: String? = null
)

@JsonClass(generateAdapter = true)
data class PipedStreamResponse(
    @Json(name = "title") val title: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "uploader") val uploader: String? = null,
    @Json(name = "uploaderUrl") val uploaderUrl: String? = null,
    @Json(name = "duration") val duration: Long? = null,
    @Json(name = "audioStreams") val audioStreams: List<PipedAudioStream>? = null,
    @Json(name = "relatedStreams") val relatedStreams: List<PipedSearchItem>? = null,
    @Json(name = "thumbnailUrl") val thumbnailUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class PipedAudioStream(
    @Json(name = "url") val url: String? = null,
    @Json(name = "format") val format: String? = null,
    @Json(name = "quality") val quality: String? = null,
    @Json(name = "mimeType") val mimeType: String? = null,
    @Json(name = "bitrate") val bitrate: Int? = null,
    @Json(name = "initStart") val initStart: Long? = null,
    @Json(name = "initEnd") val initEnd: Long? = null,
    @Json(name = "indexStart") val indexStart: Long? = null,
    @Json(name = "indexEnd") val indexEnd: Long? = null
)

@JsonClass(generateAdapter = true)
data class PipedPlaylistResponse(
    @Json(name = "name") val name: String? = null,
    @Json(name = "uploader") val uploader: String? = null,
    @Json(name = "thumbnailUrl") val thumbnailUrl: String? = null,
    @Json(name = "relatedStreams") val relatedStreams: List<PipedSearchItem>? = null
)

@JsonClass(generateAdapter = true)
data class PipedChannelResponse(
    @Json(name = "name") val name: String? = null,
    @Json(name = "avatarUrl") val avatarUrl: String? = null,
    @Json(name = "relatedStreams") val relatedStreams: List<PipedSearchItem>? = null
)
