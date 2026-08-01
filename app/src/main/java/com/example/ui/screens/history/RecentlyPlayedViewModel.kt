package com.example.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Track
import com.example.domain.repository.TrackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class RecentlyPlayedUiState(
    val history: List<Track> = emptyList()
)

@HiltViewModel
class RecentlyPlayedViewModel @Inject constructor(
    private val trackRepository: TrackRepository
) : ViewModel() {

    val uiState: StateFlow<RecentlyPlayedUiState> = trackRepository.getRecentlyPlayed()
        .map { RecentlyPlayedUiState(history = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = RecentlyPlayedUiState()
        )
}
