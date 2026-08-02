package com.example.ui.screens.favorites

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

data class FavoritesUiState(
    val favorites: List<Track> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val trackRepository: TrackRepository,
    private val playbackRepository: PlaybackRepository,
    private val libraryRepository: LibraryRepository,
    private val downloadsRepository: DownloadsRepository
) : ViewModel() {

    val uiState: StateFlow<FavoritesUiState> = trackRepository.getFavorites()
        .map { FavoritesUiState(favorites = it, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FavoritesUiState()
        )

    fun play(track: Track) = playbackRepository.playQueue(listOf(track), 0)

    fun remove(track: Track) {
        viewModelScope.launch { libraryRepository.toggleFavoriteTrack(track) }
    }

    fun download(track: Track) {
        viewModelScope.launch {
            downloadsRepository.enqueueDownload(track.id, track.title, track.artist)
        }
    }
}
