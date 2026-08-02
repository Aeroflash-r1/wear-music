package com.example.domain.repository

import com.example.domain.model.BackendResult
import com.example.domain.model.DownloadedTrack
import com.example.domain.model.SearchResult
import com.example.domain.model.SettingsUiState
import com.example.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface TrackRepository {
    fun getQueue(): Flow<List<Track>>
    fun getFavorites(): Flow<List<Track>>
    fun getRecentlyPlayed(): Flow<List<Track>>
    fun getRecentSearches(): Flow<List<String>>
    suspend fun search(query: String, filter: String? = null): BackendResult<List<SearchResult>>
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
    val serverUrl: Flow<String>
    suspend fun setServerUrl(url: String)
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
