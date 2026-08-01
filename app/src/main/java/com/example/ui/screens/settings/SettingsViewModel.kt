package com.example.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.SyncUiState
import com.example.domain.repository.OfflineRepository
import com.example.domain.repository.SettingsRepository
import com.example.domain.repository.SyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val audioQuality: String = "High",
    val audioOffload: Boolean = true,
    val gaplessPlayback: Boolean = true,
    val normalizeVolume: Boolean = false,
    
    val downloadQuality: String = "High",
    val autoDownload: Boolean = true,
    val downloadOverWifi: Boolean = true,
    
    val currentCacheSize: String = "156 MB",
    val cacheLimit: String = "1 GB",
    
    val backendStatus: String = "Connected",
    val backendUrl: String = "https://api.pulse.example",
    
    val dynamicColor: Boolean = true,
    val amoledDarkTheme: Boolean = true,
    val animationSpeed: String = "Normal",
    
    val pulseVersion: String = "1.0.0 (42)",
    val buildType: String = "Release",
    val appSize: String = "45 MB",
    
    val developerOptionsUnlocked: Boolean = false,
    val devClickCount: Int = 0,
    
    val showClearCacheDialog: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val syncRepository: SyncRepository,
    private val offlineRepository: OfflineRepository
) : ViewModel() {

    val syncUiState: StateFlow<SyncUiState> = syncRepository.getSyncUiState()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SyncUiState()
        )

    val uiState: StateFlow<SettingsUiState> = settingsRepository.settingsState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsUiState()
        )

    fun triggerSync() {
        viewModelScope.launch {
            syncRepository.triggerManualSync()
        }
    }

    fun toggleAutoSync(enabled: Boolean) {
        syncRepository.setAutoSyncEnabled(enabled)
    }

    fun toggleSyncOnWifiOnly(wifiOnly: Boolean) {
        syncRepository.setSyncOnWifiOnly(wifiOnly)
    }

    fun toggleForceOfflineMode(enabled: Boolean) {
        offlineRepository.setForceOfflineMode(enabled)
    }

    fun retryFailedDownloads() {
        viewModelScope.launch {
            syncRepository.retryFailedDownloads()
        }
    }

    fun setMaxConcurrentDownloads(count: Int) {
        syncRepository.setMaxConcurrentDownloads(count)
    }

    fun toggleAudioOffload() {
        settingsRepository.toggleAudioOffload()
    }

    fun toggleGaplessPlayback() {
        settingsRepository.toggleGaplessPlayback()
    }

    fun toggleNormalizeVolume() {
        settingsRepository.toggleNormalizeVolume()
    }

    fun toggleAutoDownload() {
        settingsRepository.toggleAutoDownload()
    }

    fun toggleDownloadOverWifi() {
        settingsRepository.toggleDownloadOverWifi()
    }

    fun toggleDynamicColor() {
        settingsRepository.toggleDynamicColor()
    }

    fun toggleAmoledDarkTheme() {
        settingsRepository.toggleAmoledDarkTheme()
    }

    fun onVersionClicked() {
        settingsRepository.onVersionClicked()
    }

    fun showClearCacheDialog() {
        settingsRepository.showClearCacheDialog()
    }

    fun hideClearCacheDialog() {
        settingsRepository.hideClearCacheDialog()
    }

    fun clearCache() {
        settingsRepository.clearCache()
    }
}
