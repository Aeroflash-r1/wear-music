package com.example.ui.screens.queue

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.itemsIndexed
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.MaterialTheme
import com.example.ui.components.PulseEmptyState
import com.example.ui.components.PulseListItem
import com.example.ui.components.PulseScreenScaffold
import com.example.ui.components.PulseSecondaryButton
import com.example.ui.components.PulseSectionHeader
import com.example.ui.components.pulseRotaryScroll
import com.example.ui.models.Track
import com.example.ui.theme.PulsePadding
import com.example.ui.theme.PulseSpacing

@Composable
fun QueueScreen(
    viewModel: QueueViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberScalingLazyListState()
    var selectedTrack by remember { mutableStateOf<Track?>(null) }
    var selectedIndex by remember { mutableStateOf(-1) }

    PulseScreenScaffold(scrollState = listState, modifier = modifier) {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            ScalingLazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().pulseRotaryScroll(listState),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PulsePadding.ScreenContent,
                verticalArrangement = Arrangement.spacedBy(PulseSpacing.sm)
            ) {
                item { PulseSectionHeader("Up Next") }
                if (uiState.upcoming.isEmpty()) {
                    item {
                        PulseEmptyState(
                            message = "Your queue is empty",
                            icon = Icons.Default.MusicNote,
                            modifier = Modifier.padding(top = PulseSpacing.lg)
                        )
                    }
                } else {
                    itemsIndexed(uiState.upcoming, key = { _, track -> track.id }) { index, track ->
                        PulseListItem(
                            label = track.title,
                            secondaryLabel = "${track.artist} • ${track.duration}",
                            icon = Icons.Default.MusicNote,
                            onClick = { viewModel.play(track) },
                            onLongClick = {
                                selectedIndex = index
                                selectedTrack = track
                            }
                        )
                    }
                }
            }

            selectedTrack?.let { track ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    val dialogListState = rememberScalingLazyListState()
                    ScalingLazyColumn(
                        state = dialogListState,
                        modifier = Modifier.fillMaxSize().pulseRotaryScroll(dialogListState),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        contentPadding = PulsePadding.ScreenContent,
                        verticalArrangement = Arrangement.spacedBy(PulseSpacing.sm)
                    ) {
                        item { PulseSectionHeader(title = track.title) }
                        item {
                            PulseListItem(
                                label = "Play Next",
                                icon = Icons.Default.PlayArrow,
                                onClick = {
                                    viewModel.playNext(track)
                                    selectedTrack = null
                                }
                            )
                        }
                        item {
                            PulseListItem(
                                label = "Remove from Queue",
                                icon = Icons.Default.RemoveCircleOutline,
                                onClick = {
                                    if (selectedIndex >= 0) viewModel.remove(selectedIndex)
                                    selectedTrack = null
                                    selectedIndex = -1
                                }
                            )
                        }
                        item {
                            PulseListItem(
                                label = "Favorite",
                                icon = Icons.Default.Favorite,
                                onClick = {
                                    viewModel.favorite(track)
                                    selectedTrack = null
                                }
                            )
                        }
                        item {
                            PulseListItem(
                                label = "Download",
                                icon = Icons.Default.Download,
                                onClick = {
                                    viewModel.download(track)
                                    selectedTrack = null
                                }
                            )
                        }
                        item {
                            PulseSecondaryButton(
                                label = "Cancel",
                                onClick = {
                                    selectedTrack = null
                                    selectedIndex = -1
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
