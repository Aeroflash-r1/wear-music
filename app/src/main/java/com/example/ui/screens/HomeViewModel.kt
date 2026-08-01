package com.example.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.AlbumDetails
import com.example.domain.model.ArtistDetails
import com.example.domain.model.BackendResult
import com.example.domain.model.DownloadedTrack
import com.example.domain.model.SearchResultItem
import com.example.domain.model.Track
import com.example.domain.repository.BackendRepository
import com.example.domain.repository.DownloadsRepository
import com.example.domain.repository.LibraryRepository
import com.example.domain.repository.OfflineRepository
import com.example.domain.repository.PlaybackRepository
import com.example.domain.repository.RecommendationRepository
import com.example.domain.repository.TrackRepository
import com.example.ui.screens.player.PlayerUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeFeedState(
    val recommended: List<SearchResultItem> = emptyList(),
    val trending: List<SearchResultItem> = emptyList(),
    val quickMixes: List<SearchResultItem> = emptyList(),
    val popularAlbums: List<AlbumDetails> = emptyList(),
    val popularArtists: List<ArtistDetails> = emptyList()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val recommendationRepository: RecommendationRepository,
    private val backendRepository: BackendRepository,
    private val trackRepository: TrackRepository,
    private val downloadsRepository: DownloadsRepository,
    private val libraryRepository: LibraryRepository,
    private val playbackRepository: PlaybackRepository,
    private val offlineRepository: OfflineRepository
) : ViewModel() {

    val isOffline: StateFlow<Boolean> = offlineRepository.isOffline()
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val playerUiState: StateFlow<PlayerUiState> = playbackRepository.playerUiState
        .stateIn(viewModelScope, SharingStarted.Lazily, PlayerUiState())

    val recentlyPlayed: StateFlow<List<Track>> = trackRepository.getRecentlyPlayed()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val favorites: StateFlow<List<Track>> = libraryRepository.getFavoriteTracks()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val downloads: StateFlow<List<DownloadedTrack>> = downloadsRepository.getDownloads()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _homeFeed = MutableStateFlow(HomeFeedState())
    val homeFeed: StateFlow<HomeFeedState> = _homeFeed.asStateFlow()

    init {
        loadFeedSections()
    }

    fun loadFeedSections() {
        viewModelScope.launch {
            launch {
                val recs = recommendationRepository.getRecommendations()
                if (recs is BackendResult.Success) {
                    _homeFeed.value = _homeFeed.value.copy(recommended = recs.data)
                }
            }
            launch {
                val tr = recommendationRepository.getTrending()
                if (tr is BackendResult.Success) {
                    _homeFeed.value = _homeFeed.value.copy(trending = tr.data)
                    // Seed "Popular Albums"/"Popular Artists" from a real artist name in
                    // the actual trending results — Piped/Invidious don't expose a
                    // dedicated "popular albums/artists" browse endpoint, so this is the
                    // most honest real-data stand-in (as opposed to fabricating entries).
                    val seedArtist = tr.data.firstOrNull { it.artist.isNotBlank() }?.artist
                    if (seedArtist != null) {
                        loadPopularAlbumsAndArtists(seedArtist)
                    }
                }
            }
            launch {
                val mixes = recommendationRepository.getQuickMixes()
                if (mixes is BackendResult.Success) {
                    _homeFeed.value = _homeFeed.value.copy(quickMixes = mixes.data)
                }
            }
        }
    }

    private fun loadPopularAlbumsAndArtists(seedArtist: String) {
        viewModelScope.launch {
            launch {
                val albumResults = backendRepository.search(seedArtist, filter = "music_albums")
                if (albumResults is BackendResult.Success) {
                    val albums = albumResults.data.map { item ->
                        AlbumDetails(id = item.id, title = item.title, artist = item.artist, thumbnailUrl = item.thumbnailUrl)
                    }
                    _homeFeed.value = _homeFeed.value.copy(popularAlbums = albums)
                }
            }
            launch {
                val artistResults = backendRepository.search(seedArtist, filter = "channels")
                if (artistResults is BackendResult.Success) {
                    val artists = artistResults.data.map { item ->
                        ArtistDetails(id = item.id, name = item.title, thumbnailUrl = item.thumbnailUrl)
                    }
                    _homeFeed.value = _homeFeed.value.copy(popularArtists = artists)
                }
            }
        }
    }

    fun playTrack(trackItem: SearchResultItem) {
        val track = Track(trackItem.id, trackItem.title, trackItem.artist, trackItem.duration)
        playbackRepository.playQueue(listOf(track), 0)
    }

    fun playTrackModel(track: Track) {
        playbackRepository.playQueue(listOf(track), 0)
    }
}
