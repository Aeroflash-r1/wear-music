package com.example.domain.model

data class PlayerUiState(
    val title: String = "",
    val artist: String = "",
    val duration: Long = 0L,
    val currentPosition: Long = 0L,
    val isPlaying: Boolean = false,
    val shuffleEnabled: Boolean = false,
    val repeatMode: Int = 0, // 0: None, 1: All, 2: One
    val buffering: Boolean = false,
    val playbackSpeed: Float = 1.0f,
    val sleepTimerRemainingMs: Long = 0L,
    val currentTrackId: String = "",
    val queue: List<Track> = emptyList(),
    val queueIndex: Int = 0
)
