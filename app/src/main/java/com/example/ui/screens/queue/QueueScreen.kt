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
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.MaterialTheme
import com.example.ui.components.PulseEmptyState
import com.example.ui.components.PulseScreenScaffold
import com.example.ui.components.PulseListItem
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
                items(uiState.upcoming, key = { it.id }) { track ->
                    PulseListItem(
                        label = track.title,
                        secondaryLabel = "${track.artist} • ${track.duration}",
                        icon = Icons.Default.MusicNote,
                        onClick = { },
                        onLongClick = { selectedTrack = track }
                    )
                }
            }
        }
        
        if (selectedTrack != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                val dialogListState = rememberScalingLazyListState()
                selectedTrack?.let { track ->
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
                                onClick = { selectedTrack = null }
                            )
                        }
                        item {
                            PulseListItem(
                                label = "Remove from Queue",
                                icon = Icons.Default.RemoveCircleOutline,
                                onClick = { selectedTrack = null }
                            )
                        }
                        item {
                            PulseListItem(
                                label = "Favorite",
                                icon = Icons.Default.Favorite,
                                onClick = { selectedTrack = null }
                            )
                        }
                        item {
                            PulseListItem(
                                label = "Download",
                                icon = Icons.Default.Download,
                                onClick = { selectedTrack = null }
                            )
                        }
                        item {
                            PulseSecondaryButton(
                                label = "Cancel",
                                onClick = { selectedTrack = null }
                            )
                        }
                    }
                }
            }
        }
    }
    }
}
