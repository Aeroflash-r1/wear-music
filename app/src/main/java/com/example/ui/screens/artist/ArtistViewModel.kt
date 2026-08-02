package com.example.ui.screens.artist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.ArtistDetails
import com.example.domain.model.BackendResult
import com.example.domain.model.Track
import com.example.domain.model.userMessage
import com.example.domain.repository.ArtistRepository
import com.example.domain.repository.LibraryRepository
import com.example.domain.repository.PlaybackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ArtistUiState {
    object Loading : ArtistUiState
    data class Success(val artist: ArtistDetails, val isFavorite: Boolean) : ArtistUiState
    data class Error(val message: String) : ArtistUiState
}

@HiltViewModel
class ArtistViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val artistRepository: ArtistRepository,
    private val libraryRepository: LibraryRepository,
    private val playbackRepository: PlaybackRepository
) : ViewModel() {

    private val artistId: String = savedStateHandle["artistId"] ?: "default"

    private val _uiState = MutableStateFlow<ArtistUiState>(ArtistUiState.Loading)
    val uiState: StateFlow<ArtistUiState> = _uiState.asStateFlow()

    init {
        loadArtist()
    }

    fun loadArtist() {
        viewModelScope.launch {
            _uiState.value = ArtistUiState.Loading
            when (val res = artistRepository.getArtistDetails(artistId)) {
                is BackendResult.Success -> {
                    val isFav = libraryRepository.isFavorite(artistId)
                    _uiState.value = ArtistUiState.Success(res.data, isFav)
                }
                is BackendResult.Error -> {
                    _uiState.value = ArtistUiState.Error(res.error.userMessage())
                }
            }
        }
    }

    fun toggleFavorite() {
        val state = _uiState.value as? ArtistUiState.Success ?: return
        viewModelScope.launch {
            libraryRepository.toggleFavoriteArtist(state.artist)
            val isFav = libraryRepository.isFavorite(artistId)
            _uiState.value = state.copy(isFavorite = isFav)
        }
    }

    fun playTrack(trackId: String) {
        val state = _uiState.value as? ArtistUiState.Success ?: return
        val track = state.artist.topTracks.firstOrNull { it.id == trackId } ?: return
        playbackRepository.playQueue(
            listOf(Track(track.id, track.title, state.artist.name, track.duration)),
            0
        )
    }

    fun playAll() {
        val state = _uiState.value as? ArtistUiState.Success ?: return
        val tracks = state.artist.topTracks.map {
            Track(it.id, it.title, state.artist.name, it.duration)
        }
        if (tracks.isNotEmpty()) {
            playbackRepository.playQueue(tracks, 0)
        }
    }
}
