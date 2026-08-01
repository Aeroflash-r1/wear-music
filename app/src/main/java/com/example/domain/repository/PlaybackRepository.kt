package com.example.domain.repository

import com.example.ui.screens.player.PlayerUiState
import kotlinx.coroutines.flow.StateFlow

interface PlaybackRepository {
    val playerUiState: StateFlow<PlayerUiState>
    fun play()
    fun pause()
    fun togglePlayPause()
    fun seekTo(positionMs: Long)
    fun nextTrack()
    fun previousTrack()
    fun toggleShuffle()
    fun toggleRepeat()
    fun stop()
    suspend fun playTrackStream(trackId: String, title: String, artist: String)
    fun setPlaybackSpeed(speed: Float)
    fun setSleepTimer(minutes: Int)
    fun cancelSleepTimer()
    fun playQueue(tracks: List<com.example.domain.model.Track>, startIndex: Int)
    fun addToQueue(track: com.example.domain.model.Track)
    fun playNext(track: com.example.domain.model.Track)
    fun removeFromQueue(index: Int)
    fun moveQueueItem(fromIndex: Int, toIndex: Int)
    fun clearQueue()
}
