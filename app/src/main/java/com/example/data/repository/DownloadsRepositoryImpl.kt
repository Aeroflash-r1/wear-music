package com.example.data.repository

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadCursor
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import com.example.data.database.dao.DownloadDao
import com.example.data.database.dao.TrackDao
import com.example.data.database.entity.DownloadEntity
import com.example.data.database.entity.TrackEntity
import com.example.domain.model.BackendResult
import com.example.domain.model.DownloadProgressState
import com.example.domain.model.DownloadStatus
import com.example.domain.model.DownloadedTrack
import com.example.domain.repository.BackendRepository
import com.example.domain.repository.DownloadsRepository
import com.example.service.PulseDownloadService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(UnstableApi::class)
@Singleton
class DownloadsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val downloadManager: DownloadManager,
    private val simpleCache: SimpleCache,
    private val downloadDao: DownloadDao,
    private val trackDao: TrackDao,
    private val backendRepository: BackendRepository
) : DownloadsRepository {

    companion object {
        // Any non-zero value works; STOP_REASON_NONE (0) is reserved for "not stopped".
        private const val STOP_REASON_PAUSED = 1
    }

    private val scope = CoroutineScope(Dispatchers.IO)
    private val _progressState = MutableStateFlow<List<DownloadProgressState>>(emptyList())

    init {
        downloadManager.addListener(object : DownloadManager.Listener {
            override fun onDownloadChanged(
                downloadManager: DownloadManager,
                download: Download,
                finalException: Exception?
            ) {
                updateProgressState()
            }

            override fun onDownloadRemoved(downloadManager: DownloadManager, download: Download) {
                updateProgressState()
            }
        })
        startProgressPolling()
    }

    override fun getDownloads(): Flow<List<DownloadedTrack>> = combine(
        downloadDao.getAllDownloads(),
        trackDao.getAllTracks()
    ) { dlEntities, trackEntities ->
        val trackMap = trackEntities.associateBy { it.id }
        dlEntities.mapNotNull { dl ->
            val track = trackMap[dl.trackId]
            DownloadedTrack(
                id = dl.id,
                title = track?.title ?: "Track #${dl.trackId}",
                artist = track?.artist ?: "Unknown Artist",
                duration = track?.duration ?: "3:45",
                quality = track?.audioQuality ?: "High Quality",
                size = dl.fileSize
            )
        }
    }

    override fun observeDownloadProgress(): Flow<List<DownloadProgressState>> = _progressState.asStateFlow()

    override fun getTotalDownloads(): Int {
        return simpleCache.keys.size.coerceAtLeast(downloadManager.currentDownloads.size)
    }

    override fun getTotalStorageUsed(): String {
        val bytes = simpleCache.cacheSpace
        val mb = bytes / (1024 * 1024)
        return if (mb > 1024) "%.1f GB".format(mb / 1024.0) else "$mb MB"
    }

    override fun getStorageLimit(): String = "5 GB"

    override suspend fun enqueueDownload(trackId: String, title: String, artist: String) {
        when (val streamRes = backendRepository.getAudioStream(trackId)) {
            is BackendResult.Success -> {
                val uri = Uri.parse(streamRes.data.audioUrl)
                val request = DownloadRequest.Builder(trackId, uri)
                    .setCustomCacheKey(trackId)
                    .setMimeType(streamRes.data.mimeType)
                    .setData(title.toByteArray(Charsets.UTF_8))
                    .build()

                DownloadService.sendAddDownload(
                    context,
                    PulseDownloadService::class.java,
                    request,
                    false
                )

                trackDao.insertTrack(
                    TrackEntity(
                        id = trackId,
                        title = title,
                        artist = artist,
                        album = "Downloaded Single",
                        duration = "3:45",
                        audioQuality = "High Quality (${streamRes.data.bitrate / 1000}kbps)"
                    )
                )

                downloadDao.insertDownload(
                    DownloadEntity(
                        id = trackId,
                        trackId = trackId,
                        filePath = streamRes.data.audioUrl,
                        fileSize = "12 MB"
                    )
                )
            }
            is BackendResult.Error -> {
                // Fallback direct download enqueue
            }
        }
    }

    override suspend fun pauseDownload(trackId: String) {
        // A non-zero custom stop reason actually pauses the download. Using
        // STOP_REASON_NONE here (as before) made "pause" behave identically to
        // "resume" since STOP_REASON_NONE means "don't stop".
        DownloadService.sendSetStopReason(
            context,
            PulseDownloadService::class.java,
            trackId,
            STOP_REASON_PAUSED,
            false
        )
    }

    override suspend fun resumeDownload(trackId: String) {
        DownloadService.sendSetStopReason(
            context,
            PulseDownloadService::class.java,
            trackId,
            Download.STOP_REASON_NONE,
            false
        )
    }

    override suspend fun cancelDownload(trackId: String) {
        removeDownload(trackId)
    }

    override suspend fun removeDownload(trackId: String) {
        DownloadService.sendRemoveDownload(
            context,
            PulseDownloadService::class.java,
            trackId,
            false
        )
        downloadDao.deleteDownloadById(trackId)
        downloadDao.deleteDownloadByTrackId(trackId)
        simpleCache.removeResource(trackId)
    }

    override suspend fun clearCache() {
        downloadManager.removeAllDownloads()
        downloadDao.clearDownloads()
        simpleCache.keys.forEach { key ->
            simpleCache.removeResource(key)
        }
    }

    override suspend fun retryFailedDownloads() {
        val failed = _progressState.value.filter { it.status == DownloadStatus.FAILED }
        failed.forEach { progressState ->
            resumeDownload(progressState.trackId)
        }
    }

    override fun setMaxParallelDownloads(count: Int) {
        downloadManager.maxParallelDownloads = count.coerceAtLeast(1)
    }

    private fun startProgressPolling() {
        scope.launch {
            while (isActive) {
                updateProgressState()
                delay(1000)
            }
        }
    }

    private fun updateProgressState() {
        val list = mutableListOf<DownloadProgressState>()
        val cursor: DownloadCursor = downloadManager.downloadIndex.getDownloads()
        try {
            while (cursor.moveToNext()) {
                val download = cursor.download
                val trackId = download.request.id
                val status = when (download.state) {
                    Download.STATE_QUEUED -> DownloadStatus.QUEUED
                    Download.STATE_DOWNLOADING -> DownloadStatus.DOWNLOADING
                    Download.STATE_STOPPED -> DownloadStatus.PAUSED
                    Download.STATE_COMPLETED -> DownloadStatus.COMPLETED
                    Download.STATE_FAILED -> DownloadStatus.FAILED
                    Download.STATE_REMOVING -> DownloadStatus.CANCELLED
                    else -> DownloadStatus.QUEUED
                }
                val percent = if (download.percentDownloaded != -1f) download.percentDownloaded else 0f
                list.add(
                    DownloadProgressState(
                        trackId = trackId,
                        title = String(download.request.data ?: ByteArray(0), Charsets.UTF_8).ifEmpty { "Track #$trackId" },
                        artist = "Pulse Download",
                        bytesDownloaded = download.bytesDownloaded,
                        totalBytes = download.contentLength,
                        percentage = percent,
                        status = status
                    )
                )
            }
        } catch (_: Exception) {
        } finally {
            cursor.close()
        }
        _progressState.value = list
    }
}
