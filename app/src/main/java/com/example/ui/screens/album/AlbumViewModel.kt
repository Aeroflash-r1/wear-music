package com.example.ui.screens.album

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.AlbumDetails
import com.example.domain.model.BackendResult
import com.example.domain.model.Track
import com.example.domain.model.userMessage
import com.example.domain.repository.AlbumRepository
import com.example.domain.repository.DownloadsRepository
import com.example.domain.repository.LibraryRepository
import com.example.domain.repository.PlaybackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AlbumUiState {
    object Loading : AlbumUiState
    data class Success(val album: AlbumDetails, val isFavorite: Boolean) : AlbumUiState
    data class Error(val message: String) : AlbumUiState
}

@HiltViewModel
class AlbumViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val albumRepository: AlbumRepository,
    private val libraryRepository: LibraryRepository,
    private val playbackRepository: PlaybackRepository,
    private val downloadsRepository: DownloadsRepository
) : ViewModel() {

    private val albumId: String = savedStateHandle["albumId"] ?: "default"

    private val _uiState = MutableStateFlow<AlbumUiState>(AlbumUiState.Loading)
    val uiState: StateFlow<AlbumUiState> = _uiState.asStateFlow()

    init {
        loadAlbum()
    }

    fun loadAlbum() {
        viewModelScope.launch {
            _uiState.value = AlbumUiState.Loading
            when (val res = albumRepository.getAlbumDetails(albumId)) {
                is BackendResult.Success -> {
                    val isFav = libraryRepository.isFavorite(albumId)
                    _uiState.value = AlbumUiState.Success(res.data, isFav)
                }
                is BackendResult.Error -> {
                    _uiState.value = AlbumUiState.Error(res.error.userMessage())
                }
            }
        }
    }

    fun toggleFavorite() {
        val state = _uiState.value as? AlbumUiState.Success ?: return
        viewModelScope.launch {
            libraryRepository.toggleFavoriteAlbum(state.album)
            val isFav = libraryRepository.isFavorite(albumId)
            _uiState.value = state.copy(isFavorite = isFav)
        }
    }

    fun playAlbum() {
        val state = _uiState.value as? AlbumUiState.Success ?: return
        val tracks = state.album.tracks.map {
            Track(it.id, it.title, state.album.artist, it.duration)
        }
        if (tracks.isNotEmpty()) {
            playbackRepository.playQueue(tracks, 0)
        }
    }

    fun playTrack(trackId: String) {
        val state = _uiState.value as? AlbumUiState.Success ?: return
        val track = state.album.tracks.firstOrNull { it.id == trackId } ?: return
        playbackRepository.playQueue(
            listOf(Track(track.id, track.title, state.album.artist, track.duration)),
            0
        )
    }

    fun downloadAlbum() {
        val state = _uiState.value as? AlbumUiState.Success ?: return
        viewModelScope.launch {
            state.album.tracks.forEach { tr ->
                downloadsRepository.enqueueDownload(tr.id, tr.title, state.album.artist)
            }
        }
    }
}
