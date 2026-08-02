package com.example.ui.screens.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
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
fun PlaylistScreen(
    viewModel: PlaylistViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberScalingLazyListState()

    PulseScreenScaffold(scrollState = listState, modifier = modifier) {
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        when (val state = uiState) {
            is PlaylistUiState.Loading -> {
                PulseLoadingIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is PlaylistUiState.Error -> {
                PulseEmptyState(
                    message = state.message,
                    icon = Icons.Default.MusicNote,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is PlaylistUiState.Success -> {
                ScalingLazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().pulseRotaryScroll(listState),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    contentPadding = PulsePadding.ScreenContent,
                    verticalArrangement = Arrangement.spacedBy(PulseSpacing.sm)
                ) {
                    item {
                        PulseSectionHeader(
                            title = state.playlist.title,
                            subtitle = state.playlist.author ?: "Pulse Playlist"
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(PulseSpacing.xs)
                        ) {
                            PulseButton(
                                label = "Play",
                                icon = Icons.Default.PlayArrow,
                                onClick = viewModel::playPlaylist,
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

                    item {
                        PulseSecondaryButton(
                            label = "Download All",
                            icon = Icons.Default.Download,
                            onClick = viewModel::downloadPlaylist,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (state.playlist.tracks.isNotEmpty()) {
                        item { PulseSectionHeader(title = "Tracks (${state.playlist.tracks.size})") }
                        items(state.playlist.tracks, key = { it.id }) { track ->
                            PulseListItem(
                                label = track.title,
                                secondaryLabel = "${track.artist} • ${track.duration}",
                                icon = Icons.Default.MusicNote,
                                onClick = { viewModel.playTrack(track.id) }
                            )
                        }
                    }
                }
            }
        }
    }
    }
}
