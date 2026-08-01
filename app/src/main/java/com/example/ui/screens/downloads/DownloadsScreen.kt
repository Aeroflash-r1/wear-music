package com.example.ui.screens.downloads

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.example.ui.components.PulseEmptyState
import com.example.ui.components.PulseScreenScaffold
import com.example.ui.components.PulseListItem
import com.example.ui.components.PulseSecondaryButton
import com.example.ui.components.PulseSectionHeader
import com.example.ui.components.pulseRotaryScroll
import com.example.ui.models.DownloadedTrack
import com.example.ui.theme.PulsePadding
import com.example.ui.theme.PulseSpacing

@Composable
fun DownloadsScreen(
    viewModel: DownloadsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberScalingLazyListState()
    var selectedTrack by remember { mutableStateOf<DownloadedTrack?>(null) }
    
    PulseScreenScaffold(scrollState = listState, modifier = modifier) {
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        ScalingLazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().pulseRotaryScroll(listState),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PulsePadding.ScreenContent,
            verticalArrangement = Arrangement.spacedBy(PulseSpacing.sm)
        ) {
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().padding(bottom = PulseSpacing.sm)
                ) {
                    Text(
                        text = "Downloads",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(PulseSpacing.xs))
                    Text(
                        text = "${uiState.totalDownloads} songs • ${uiState.totalStorageUsed} / ${uiState.storageLimit}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            if (uiState.downloads.isEmpty()) {
                item {
                    PulseEmptyState(
                        message = "No downloaded songs",
                        icon = Icons.Default.Download,
                        modifier = Modifier.padding(top = PulseSpacing.lg)
                    )
                }
                item {
                    PulseSecondaryButton(
                        label = "Search Music",
                        onClick = { },
                        icon = Icons.Default.Search,
                        modifier = Modifier.padding(top = PulseSpacing.md)
                    )
                }
            } else {
                items(uiState.downloads, key = { it.id }) { track ->
                    PulseListItem(
                        label = track.title,
                        secondaryLabel = "${track.artist} • ${track.duration}\n${track.quality} • ${track.size}",
                        icon = Icons.Default.DownloadDone,
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
                                label = "Play",
                                icon = Icons.Default.PlayArrow,
                                onClick = { selectedTrack = null }
                            )
                        }
                        item {
                            PulseListItem(
                                label = "Remove Download",
                                icon = Icons.Default.DeleteOutline,
                                onClick = {
                                    viewModel.removeDownload(track.id)
                                    selectedTrack = null
                                }
                            )
                        }
                        item {
                            PulseListItem(
                                label = "Song Information",
                                icon = Icons.Default.Info,
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
