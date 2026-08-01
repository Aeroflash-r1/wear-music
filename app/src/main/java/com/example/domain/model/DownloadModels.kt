package com.example.domain.model

enum class DownloadStatus {
    QUEUED,
    DOWNLOADING,
    PAUSED,
    COMPLETED,
    FAILED,
    CANCELLED
}

data class DownloadProgressState(
    val trackId: String,
    val title: String,
    val artist: String,
    val bytesDownloaded: Long = 0L,
    val totalBytes: Long = 0L,
    val percentage: Float = 0f,
    val status: DownloadStatus = DownloadStatus.QUEUED,
    val downloadSpeedBytesPerSec: Long = 0L,
    val failureReason: String? = null
)

sealed class DownloadError {
    data class StorageFull(val message: String) : DownloadError()
    data class NetworkUnavailable(val message: String) : DownloadError()
    data class BackendUnavailable(val message: String) : DownloadError()
    data class Unknown(val message: String) : DownloadError()
}
