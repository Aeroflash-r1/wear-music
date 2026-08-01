package com.example.domain.model

enum class SyncStatus {
    IDLE,
    SYNCING,
    SUCCESS,
    ERROR
}

data class SyncUiState(
    val lastSyncTimeMs: Long = 0L,
    val status: SyncStatus = SyncStatus.IDLE,
    val activeBackend: String = "Not configured",
    val networkState: NetworkState = NetworkState.CONNECTED_WIFI,
    val isOffline: Boolean = false,
    val pendingDownloadsCount: Int = 0,
    val failedDownloadsCount: Int = 0,
    val autoSyncEnabled: Boolean = true,
    val syncOnWifiOnly: Boolean = true,
    val maxConcurrentDownloads: Int = 2,
    val downloadQueuePriority: String = "Standard"
)
