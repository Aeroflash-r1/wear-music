package com.example.ui.screens.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.repository.PlaybackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

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
    val queue: List<com.example.domain.model.Track> = emptyList(),
    val queueIndex: Int = 0
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playbackRepository: PlaybackRepository
) : ViewModel() {

    val uiState: StateFlow<PlayerUiState> = playbackRepository.playerUiState

    fun togglePlayPause() {
        playbackRepository.togglePlayPause()
    }

    fun play() {
        playbackRepository.play()
    }

    fun pause() {
        playbackRepository.pause()
    }

    fun seekTo(positionMs: Long) {
        playbackRepository.seekTo(positionMs)
    }

    fun nextTrack() {
        playbackRepository.nextTrack()
    }

    fun previousTrack() {
        playbackRepository.previousTrack()
    }

    fun toggleShuffle() {
        playbackRepository.toggleShuffle()
    }

    fun toggleRepeat() {
        playbackRepository.toggleRepeat()
    }

    fun stop() {
        playbackRepository.stop()
    }

    fun setPlaybackSpeed(speed: Float) {
        playbackRepository.setPlaybackSpeed(speed)
    }

    fun setSleepTimer(minutes: Int) {
        playbackRepository.setSleepTimer(minutes)
    }

    fun cancelSleepTimer() {
        playbackRepository.cancelSleepTimer()
    }

    fun playQueue(tracks: List<com.example.domain.model.Track>, startIndex: Int = 0) {
        playbackRepository.playQueue(tracks, startIndex)
    }

    fun addToQueue(track: com.example.domain.model.Track) {
        playbackRepository.addToQueue(track)
    }

    fun playNext(track: com.example.domain.model.Track) {
        playbackRepository.playNext(track)
    }

    fun removeFromQueue(index: Int) {
        playbackRepository.removeFromQueue(index)
    }

    fun moveQueueItem(fromIndex: Int, toIndex: Int) {
        playbackRepository.moveQueueItem(fromIndex, toIndex)
    }

    fun clearQueue() {
        playbackRepository.clearQueue()
    }
}
