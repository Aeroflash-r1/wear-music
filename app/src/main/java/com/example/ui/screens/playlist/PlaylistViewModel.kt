package com.example.ui.screens.playlist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.BackendResult
import com.example.domain.model.PlaylistDetails
import com.example.domain.model.Track
import com.example.domain.model.userMessage
import com.example.domain.repository.DownloadsRepository
import com.example.domain.repository.LibraryRepository
import com.example.domain.repository.PlaybackRepository
import com.example.domain.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface PlaylistUiState {
    object Loading : PlaylistUiState
    data class Success(val playlist: PlaylistDetails, val isFavorite: Boolean) : PlaylistUiState
    data class Error(val message: String) : PlaylistUiState
}

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val playlistRepository: PlaylistRepository,
    private val libraryRepository: LibraryRepository,
    private val playbackRepository: PlaybackRepository,
    private val downloadsRepository: DownloadsRepository
) : ViewModel() {

    private val playlistId: String = savedStateHandle["playlistId"] ?: "default"

    private val _uiState = MutableStateFlow<PlaylistUiState>(PlaylistUiState.Loading)
    val uiState: StateFlow<PlaylistUiState> = _uiState.asStateFlow()

    init {
        loadPlaylist()
    }

    fun loadPlaylist() {
        viewModelScope.launch {
            _uiState.value = PlaylistUiState.Loading
            when (val res = playlistRepository.getPlaylistDetails(playlistId)) {
                is BackendResult.Success -> {
                    val isFav = libraryRepository.isFavorite(playlistId)
                    _uiState.value = PlaylistUiState.Success(res.data, isFav)
                }
                is BackendResult.Error -> {
                    _uiState.value = PlaylistUiState.Error(res.error.userMessage())
                }
            }
        }
    }

    fun toggleFavorite() {
        val state = _uiState.value as? PlaylistUiState.Success ?: return
        viewModelScope.launch {
            libraryRepository.toggleFavoritePlaylist(state.playlist)
            val isFav = libraryRepository.isFavorite(playlistId)
            _uiState.value = state.copy(isFavorite = isFav)
        }
    }

    fun playTrack(trackId: String) {
        val state = _uiState.value as? PlaylistUiState.Success ?: return
        val track = state.playlist.tracks.firstOrNull { it.id == trackId } ?: return
        playbackRepository.playQueue(
            listOf(Track(track.id, track.title, track.artist, track.duration)),
            0
        )
    }

    fun playPlaylist() {
        val state = _uiState.value as? PlaylistUiState.Success ?: return
        val tracks = state.playlist.tracks.map {
            Track(it.id, it.title, it.artist, it.duration)
        }
        if (tracks.isNotEmpty()) {
            playbackRepository.playQueue(tracks, 0)
        }
    }

    fun downloadPlaylist() {
        val state = _uiState.value as? PlaylistUiState.Success ?: return
        viewModelScope.launch {
            state.playlist.tracks.forEach { tr ->
                downloadsRepository.enqueueDownload(tr.id, tr.title, tr.artist)
            }
        }
    }
}
