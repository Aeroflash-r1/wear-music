package com.example.domain.model

data class AudioStreamInfo(
    val audioUrl: String,
    val bitrate: Int,
    val mimeType: String,
    val sampleRate: Int = 44100,
    val expiresAt: Long = System.currentTimeMillis() + 3600000L
)

data class SearchResultItem(
    val id: String,
    val title: String,
    val artist: String,
    val duration: String,
    val thumbnailUrl: String? = null,
    val type: String = "song", // "song", "album", "artist", "playlist"
    val streamInfo: AudioStreamInfo? = null
)

data class TrackDetails(
    val id: String,
    val title: String,
    val artist: String,
    val album: String? = null,
    val duration: String,
    val thumbnailUrl: String? = null,
    val streamUrl: String? = null,
    val relatedTracks: List<SearchResultItem> = emptyList()
)

data class AlbumDetails(
    val id: String,
    val title: String,
    val artist: String,
    val thumbnailUrl: String? = null,
    val tracks: List<SearchResultItem> = emptyList()
)

data class ArtistDetails(
    val id: String,
    val name: String,
    val thumbnailUrl: String? = null,
    val topTracks: List<SearchResultItem> = emptyList(),
    val albums: List<AlbumDetails> = emptyList()
)

data class PlaylistDetails(
    val id: String,
    val title: String,
    val author: String? = null,
    val thumbnailUrl: String? = null,
    val tracks: List<SearchResultItem> = emptyList()
)

enum class NetworkState {
    CONNECTED_WIFI,
    CONNECTED_CELLULAR,
    DISCONNECTED
}
