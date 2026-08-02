package com.example.ui.screens.artist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.example.ui.components.PulseButton
import com.example.ui.components.PulseScreenScaffold
import com.example.ui.components.PulseEmptyState
import com.example.ui.components.PulseListItem
import com.example.ui.components.PulseLoadingIndicator
import com.example.ui.components.PulseSecondaryButton
import com.example.ui.components.PulseSectionHeader
import com.example.ui.components.pulseRotaryScroll
import com.example.ui.theme.PulsePadding
import com.example.ui.theme.PulseSpacing

@Composable
fun ArtistScreen(
    onNavigateToAlbum: (String) -> Unit,
    viewModel: ArtistViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberScalingLazyListState()

    PulseScreenScaffold(scrollState = listState, modifier = modifier) {
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        when (val state = uiState) {
            is ArtistUiState.Loading -> {
                PulseLoadingIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is ArtistUiState.Error -> {
                PulseEmptyState(
                    message = state.message,
                    icon = Icons.Default.MusicNote,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is ArtistUiState.Success -> {
                ScalingLazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().pulseRotaryScroll(listState),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    contentPadding = PulsePadding.ScreenContent,
                    verticalArrangement = Arrangement.spacedBy(PulseSpacing.sm)
                ) {
                    item {
                        PulseSectionHeader(title = state.artist.name)
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(PulseSpacing.xs)
                        ) {
                            PulseButton(
                                label = "Play All",
                                icon = Icons.Default.PlayArrow,
                                onClick = viewModel::playAll,
                                modifier = Modifier.weight(1f)
                            )
                            PulseSecondaryButton(
                                label = if (state.isFavorite) "Favorited" else "Favorite",
                                icon = if (state.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                onClick = viewModel::toggleFavorite,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    if (state.artist.topTracks.isNotEmpty()) {
                        item { PulseSectionHeader(title = "Top Tracks") }
                        items(state.artist.topTracks, key = { it.id }) { track ->
                            PulseListItem(
                                label = track.title,
                                secondaryLabel = track.duration,
                                icon = Icons.Default.MusicNote,
                                onClick = { viewModel.playTrack(track.id) }
                            )
                        }
                    }

                    if (state.artist.albums.isNotEmpty()) {
                        item { PulseSectionHeader(title = "Albums") }
                        items(state.artist.albums, key = { it.id }) { album ->
                            PulseListItem(
                                label = album.title,
                                secondaryLabel = album.artist,
                                icon = Icons.Default.Album,
                                onClick = { onNavigateToAlbum(album.id) }
                            )
                        }
                    }
                }
            }
        }
    }
    }
}
