package com.pulse.server

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Loose kotlinx.serialization models for yt-dlp's `--dump-json` / `--dump-single-json`
 * output. YouTube's shape changes frequently, so every field is nullable and unknown
 * keys are ignored by the shared Json instance.
 */
@Serializable
data class YtDlpPlaylist(
    @SerialName("_type") val type: String? = null,
    val id: String? = null,
    val title: String? = null,
    val uploader: String? = null,
    val thumbnail: String? = null,
    val thumbnails: List<YtDlpThumbnail>? = null,
    val entries: List<YtDlpEntry>? = null
)

@Serializable
data class YtDlpEntry(
    @SerialName("_type") val type: String? = null,
    val id: String? = null,
    val title: String? = null,
    val url: String? = null,
    val duration: Long? = null,
    val uploader: String? = null,
    val channel: String? = null,
    @SerialName("channel_id") val channelId: String? = null,
    val thumbnails: List<YtDlpThumbnail>? = null
)

@Serializable
data class YtDlpThumbnail(
    val url: String? = null,
    val width: Int? = null,
    val height: Int? = null
)

@Serializable
data class YtDlpVideo(
    val id: String? = null,
    val title: String? = null,
    val uploader: String? = null,
    val duration: Long? = null,
    val thumbnail: String? = null,
    val thumbnails: List<YtDlpThumbnail>? = null,
    val url: String? = null,
    val ext: String? = null,
    val abr: Double? = null,
    val asr: Long? = null,
    val format: String? = null,
    val webpageUrl: String? = null,
    @SerialName("_type") val type: String? = null,
    val entries: List<YtDlpEntry>? = null,
    val uploaderId: String? = null
)
