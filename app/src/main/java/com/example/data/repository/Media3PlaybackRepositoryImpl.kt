package com.example.data.repository

import com.example.domain.model.BackendResult
import com.example.domain.repository.BackendRepository
import com.example.domain.repository.PlaybackRepository
import com.example.domain.repository.PlayerRepository
import com.example.service.PulsePlayerManager
import com.example.ui.screens.player.PlayerUiState
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Media3PlaybackRepositoryImpl @Inject constructor(
    private val playerManager: PulsePlayerManager,
    private val backendRepository: BackendRepository
) : PlaybackRepository, PlayerRepository {

    override val playerUiState: StateFlow<PlayerUiState> = playerManager.uiState

    override suspend fun playTrackStream(trackId: String, title: String, artist: String) {
        when (val res = backendRepository.getAudioStream(trackId)) {
            is BackendResult.Success -> {
                playerManager.playStream(res.data.audioUrl, title, artist)
            }
            is BackendResult.Error -> {
                playerManager.play()
            }
        }
    }

    override fun play() {
        playerManager.play()
    }

    override fun pause() {
        playerManager.pause()
    }

    override fun togglePlayPause() {
        playerManager.togglePlayPause()
    }

    override fun seekTo(positionMs: Long) {
        playerManager.seekTo(positionMs)
    }

    override fun nextTrack() {
        playerManager.nextTrack()
    }

    override fun previousTrack() {
        playerManager.previousTrack()
    }

    override fun toggleShuffle() {
        playerManager.toggleShuffle()
    }

    override fun toggleRepeat() {
        playerManager.toggleRepeat()
    }

    override fun stop() {
        playerManager.stop()
    }

    override fun setPlaybackSpeed(speed: Float) {
        playerManager.setPlaybackSpeed(speed)
    }

    override fun setSleepTimer(minutes: Int) {
        playerManager.setSleepTimer(minutes)
    }

    override fun cancelSleepTimer() {
        playerManager.cancelSleepTimer()
    }

    override fun playQueue(tracks: List<com.example.domain.model.Track>, startIndex: Int) {
        playerManager.playQueue(tracks, startIndex)
    }

    override fun addToQueue(track: com.example.domain.model.Track) {
        playerManager.addToQueue(track)
    }

    override fun playNext(track: com.example.domain.model.Track) {
        playerManager.playNext(track)
    }

    override fun removeFromQueue(index: Int) {
        playerManager.removeFromQueue(index)
    }

    override fun moveQueueItem(fromIndex: Int, toIndex: Int) {
        playerManager.moveQueueItem(fromIndex, toIndex)
    }

    override fun clearQueue() {
        playerManager.clearQueue()
    }
}
