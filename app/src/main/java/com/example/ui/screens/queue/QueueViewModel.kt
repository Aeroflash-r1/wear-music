package com.example.ui.screens.queue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Track
import com.example.domain.repository.DownloadsRepository
import com.example.domain.repository.LibraryRepository
import com.example.domain.repository.PlaybackRepository
import com.example.domain.repository.TrackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QueueUiState(
    val upcoming: List<Track> = emptyList()
)

@HiltViewModel
class QueueViewModel @Inject constructor(
    private val trackRepository: TrackRepository,
    private val playbackRepository: PlaybackRepository,
    private val libraryRepository: LibraryRepository,
    private val downloadsRepository: DownloadsRepository
) : ViewModel() {

    val uiState: StateFlow<QueueUiState> = trackRepository.getQueue()
        .map { QueueUiState(upcoming = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = QueueUiState()
        )

    fun play(track: Track) = playbackRepository.playQueue(listOf(track), 0)

    fun playNext(track: Track) = playbackRepository.playNext(track)

    fun remove(index: Int) = playbackRepository.removeFromQueue(index)

    fun favorite(track: Track) {
        viewModelScope.launch { libraryRepository.toggleFavoriteTrack(track) }
    }

    fun download(track: Track) {
        viewModelScope.launch {
            downloadsRepository.enqueueDownload(track.id, track.title, track.artist)
        }
    }
}
