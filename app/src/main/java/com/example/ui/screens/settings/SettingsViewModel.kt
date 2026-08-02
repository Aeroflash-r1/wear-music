package com.example.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.BackendResult
import com.example.domain.model.SettingsUiState
import com.example.domain.model.SyncUiState
import com.example.domain.repository.BackendRepository
import com.example.domain.repository.OfflineRepository
import com.example.domain.repository.SettingsRepository
import com.example.domain.repository.SyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val syncRepository: SyncRepository,
    private val offlineRepository: OfflineRepository,
    private val backendRepository: BackendRepository
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

    val serverUrl: StateFlow<String> = settingsRepository.serverUrl
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val forceOfflineMode: StateFlow<Boolean> = offlineRepository.isForceOfflineMode()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _connectionTest = MutableStateFlow<String?>(null)
    val connectionTest: StateFlow<String?> = _connectionTest.asStateFlow()

    fun setServerUrl(url: String) {
        _connectionTest.value = null
        viewModelScope.launch {
            settingsRepository.setServerUrl(url)
        }
    }

    /** Pings the configured server and reports the outcome in the settings UI. */
    fun testConnection() {
        _connectionTest.value = "Testing…"
        viewModelScope.launch {
            _connectionTest.value = when (backendRepository.getTrending()) {
                is BackendResult.Success -> "Connected ✓"
                is BackendResult.Error -> "Failed ✗"
            }
        }
    }

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
