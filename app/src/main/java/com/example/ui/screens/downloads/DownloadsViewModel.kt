package com.example.ui.screens.downloads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.DownloadProgressState
import com.example.domain.model.DownloadedTrack
import com.example.domain.repository.DownloadsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DownloadsUiState(
    val downloads: List<DownloadedTrack> = emptyList(),
    val progressList: List<DownloadProgressState> = emptyList(),
    val totalDownloads: Int = 0,
    val totalStorageUsed: String = "0 MB",
    val storageLimit: String = "5 GB"
)

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val downloadsRepository: DownloadsRepository
) : ViewModel() {

    val uiState: StateFlow<DownloadsUiState> = combine(
        downloadsRepository.getDownloads(),
        downloadsRepository.observeDownloadProgress()
    ) { downloads, progress ->
        DownloadsUiState(
            downloads = downloads,
            progressList = progress,
            totalDownloads = downloadsRepository.getTotalDownloads(),
            totalStorageUsed = downloadsRepository.getTotalStorageUsed(),
            storageLimit = downloadsRepository.getStorageLimit()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DownloadsUiState()
    )

    fun pauseDownload(trackId: String) {
        viewModelScope.launch {
            downloadsRepository.pauseDownload(trackId)
        }
    }

    fun resumeDownload(trackId: String) {
        viewModelScope.launch {
            downloadsRepository.resumeDownload(trackId)
        }
    }

    fun cancelDownload(trackId: String) {
        viewModelScope.launch {
            downloadsRepository.cancelDownload(trackId)
        }
    }

    fun removeDownload(trackId: String) {
        viewModelScope.launch {
            downloadsRepository.removeDownload(trackId)
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            downloadsRepository.clearCache()
        }
    }
}

