package com.example.data.sync

import com.example.data.remote.ServerConfig
import com.example.domain.model.DownloadStatus
import com.example.domain.model.NetworkState
import com.example.domain.model.SyncStatus
import com.example.domain.model.SyncUiState
import com.example.domain.repository.BackendRepository
import com.example.domain.repository.DownloadsRepository
import com.example.domain.repository.OfflineRepository
import com.example.domain.repository.SyncRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepositoryImpl @Inject constructor(
    private val syncManager: SyncManager,
    private val offlineRepository: OfflineRepository,
    private val downloadsRepository: DownloadsRepository,
    private val backendRepository: BackendRepository,
    private val serverConfig: ServerConfig
) : SyncRepository {

    private val _lastSyncTime = MutableStateFlow(System.currentTimeMillis())
    private val _syncStatus = MutableStateFlow(SyncStatus.IDLE)
    private val _autoSync = MutableStateFlow(true)
    private val _syncOnWifi = MutableStateFlow(true)
    private val _maxConcurrentDownloads = MutableStateFlow(2)
    private val _downloadQueuePriority = MutableStateFlow("Standard")

    init {
        syncManager.schedulePeriodicSync(true)
    }

    override fun getSyncUiState(): Flow<SyncUiState> {
        return combine(
            offlineRepository.isOffline(),
            offlineRepository.getNetworkState(),
            downloadsRepository.observeDownloadProgress(),
            _lastSyncTime.asStateFlow(),
            _syncStatus.asStateFlow(),
            _autoSync.asStateFlow(),
            _syncOnWifi.asStateFlow(),
            _maxConcurrentDownloads.asStateFlow(),
            _downloadQueuePriority.asStateFlow()
        ) { args ->
            val isOffline = args[0] as Boolean
            val netState = args[1] as NetworkState
            @Suppress("UNCHECKED_CAST")
            val downloads = args[2] as List<com.example.domain.model.DownloadProgressState>
            val lastSync = args[3] as Long
            val status = args[4] as SyncStatus
            val autoSync = args[5] as Boolean
            val syncOnWifi = args[6] as Boolean
            val maxConcurrent = args[7] as Int
            val priority = args[8] as String

            val pendingCount = downloads.count { it.status == DownloadStatus.QUEUED || it.status == DownloadStatus.DOWNLOADING }
            val failedCount = downloads.count { it.status == DownloadStatus.FAILED }

            SyncUiState(
                lastSyncTimeMs = lastSync,
                status = status,
                activeBackend = serverConfig.baseUrl.ifBlank { "Not configured" },
                networkState = netState,
                isOffline = isOffline,
                pendingDownloadsCount = pendingCount,
                failedDownloadsCount = failedCount,
                autoSyncEnabled = autoSync,
                syncOnWifiOnly = syncOnWifi,
                maxConcurrentDownloads = maxConcurrent,
                downloadQueuePriority = priority
            )
        }
    }

    override suspend fun triggerManualSync(): Boolean {
        return performFullSync()
    }

    override suspend fun performFullSync(): Boolean {
        _syncStatus.value = SyncStatus.SYNCING
        return try {
            backendRepository.getTrending()
            backendRepository.getRecommendations(null)
            _lastSyncTime.value = System.currentTimeMillis()
            _syncStatus.value = SyncStatus.SUCCESS
            true
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.ERROR
            false
        }
    }

    override fun setAutoSyncEnabled(enabled: Boolean) {
        _autoSync.value = enabled
        if (enabled) {
            syncManager.schedulePeriodicSync(_syncOnWifi.value)
        } else {
            syncManager.cancelPeriodicSync()
        }
    }

    override fun setSyncOnWifiOnly(wifiOnly: Boolean) {
        _syncOnWifi.value = wifiOnly
        if (_autoSync.value) {
            syncManager.schedulePeriodicSync(wifiOnly)
        }
    }

    override fun setMaxConcurrentDownloads(count: Int) {
        _maxConcurrentDownloads.value = count
    }

    override fun setDownloadQueuePriority(priority: String) {
        _downloadQueuePriority.value = priority
    }

    override suspend fun retryFailedDownloads() {
        downloadsRepository.retryFailedDownloads()
    }
}
