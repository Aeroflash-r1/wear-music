package com.example.domain.model

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val duration: String
)

data class DownloadedTrack(
    val id: String,
    val title: String,
    val artist: String,
    val duration: String,
    val quality: String,
    val size: String,
    val streamUrl: String? = null
)

data class SearchResult(
    val id: String,
    val title: String,
    val artist: String,
    val duration: String,
    val type: String = "song" // "song", "album", "artist", "playlist"
)
