package com.example.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.SearchResult
import com.example.domain.model.Track
import com.example.domain.repository.BackendRepository
import com.example.domain.repository.DownloadsRepository
import com.example.domain.repository.LibraryRepository
import com.example.domain.repository.PlaybackRepository
import com.example.domain.repository.TrackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SearchUiState {
    object RecentSearches : SearchUiState
    object Loading : SearchUiState
    data class Results(
        val items: List<SearchResult>,
        val selectedFilter: String = "All"
    ) : SearchUiState
    object NoResults : SearchUiState
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val trackRepository: TrackRepository,
    private val backendRepository: BackendRepository,
    private val playbackRepository: PlaybackRepository,
    private val libraryRepository: LibraryRepository,
    private val downloadsRepository: DownloadsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.RecentSearches)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _selectedFilter = MutableStateFlow("All")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    private var searchJob: Job? = null

    val recentSearches: StateFlow<List<String>> = trackRepository.getRecentSearches()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun onQueryChanged(newQuery: String) {
        _query.value = newQuery
        searchJob?.cancel()
        if (newQuery.isBlank()) {
            _uiState.value = SearchUiState.RecentSearches
        } else {
            searchJob = viewModelScope.launch {
                delay(300) // Debounce 300ms
                performSearch(newQuery, _selectedFilter.value)
            }
        }
    }

    fun onFilterSelected(filter: String) {
        _selectedFilter.value = filter
        if (_query.value.isNotBlank()) {
            searchJob?.cancel()
            searchJob = viewModelScope.launch {
                performSearch(_query.value, filter)
            }
        }
    }

    fun onClearQuery() {
        searchJob?.cancel()
        _query.value = ""
        _uiState.value = SearchUiState.RecentSearches
    }

    private suspend fun performSearch(query: String, filter: String) {
        _uiState.value = SearchUiState.Loading
        val results = trackRepository.search(query)
        if (results.isEmpty()) {
            _uiState.value = SearchUiState.NoResults
        } else {
            _uiState.value = SearchUiState.Results(results, filter)
        }
    }

    fun onRecentSearchClicked(query: String) {
        onQueryChanged(query)
    }

    fun playResult(result: SearchResult) {
        val track = Track(result.id, result.title, result.artist, result.duration)
        playbackRepository.playQueue(listOf(track), 0)
    }

    fun addToQueue(result: SearchResult) {
        val track = Track(result.id, result.title, result.artist, result.duration)
        playbackRepository.addToQueue(track)
    }

    fun toggleFavorite(result: SearchResult) {
        viewModelScope.launch {
            val track = Track(result.id, result.title, result.artist, result.duration)
            libraryRepository.toggleFavoriteTrack(track)
        }
    }

    fun download(result: SearchResult) {
        viewModelScope.launch {
            downloadsRepository.enqueueDownload(result.id, result.title, result.artist)
        }
    }
}
