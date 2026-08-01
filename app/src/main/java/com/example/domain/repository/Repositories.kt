package com.example.domain.repository

import com.example.domain.model.DownloadedTrack
import com.example.domain.model.SearchResult
import com.example.domain.model.Track
import com.example.ui.screens.player.PlayerUiState
import com.example.ui.screens.settings.SettingsUiState
import kotlinx.coroutines.flow.Flow

interface TrackRepository {
    fun getQueue(): Flow<List<Track>>
    fun getFavorites(): Flow<List<Track>>
    fun getRecentlyPlayed(): Flow<List<Track>>
    fun getRecentSearches(): Flow<List<String>>
    suspend fun search(query: String): List<SearchResult>
}

interface PlayerRepository {
    val playerUiState: Flow<PlayerUiState>
    fun togglePlayPause()
    fun nextTrack()
    fun previousTrack()
    fun toggleShuffle()
    fun toggleRepeat()
}

interface DownloadsRepository {
    fun getDownloads(): Flow<List<DownloadedTrack>>
    fun observeDownloadProgress(): Flow<List<com.example.domain.model.DownloadProgressState>>
    fun getTotalDownloads(): Int
    fun getTotalStorageUsed(): String
    fun getStorageLimit(): String
    suspend fun enqueueDownload(trackId: String, title: String, artist: String)
    suspend fun pauseDownload(trackId: String)
    suspend fun resumeDownload(trackId: String)
    suspend fun cancelDownload(trackId: String)
    suspend fun removeDownload(trackId: String)
    suspend fun clearCache()
    suspend fun retryFailedDownloads()
    fun setMaxParallelDownloads(count: Int)
}

interface SettingsRepository {
    val settingsState: Flow<SettingsUiState>
    fun toggleAudioOffload()
    fun toggleGaplessPlayback()
    fun toggleNormalizeVolume()
    fun toggleAutoDownload()
    fun toggleDownloadOverWifi()
    fun toggleDynamicColor()
    fun toggleAmoledDarkTheme()
    fun onVersionClicked()
    fun showClearCacheDialog()
    fun hideClearCacheDialog()
    fun clearCache()
}
