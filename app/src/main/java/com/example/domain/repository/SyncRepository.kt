package com.example.domain.repository

import com.example.domain.model.SyncUiState
import kotlinx.coroutines.flow.Flow

interface SyncRepository {
    fun getSyncUiState(): Flow<SyncUiState>
    suspend fun triggerManualSync(): Boolean
    suspend fun performFullSync(): Boolean
    fun setAutoSyncEnabled(enabled: Boolean)
    fun setSyncOnWifiOnly(wifiOnly: Boolean)
    fun setMaxConcurrentDownloads(count: Int)
    fun setDownloadQueuePriority(priority: String)
    suspend fun retryFailedDownloads()
}
