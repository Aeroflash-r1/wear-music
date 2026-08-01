package com.example.ui.screens.player

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.example.ui.components.PulseIconButton
import com.example.ui.components.PulseListItem
import com.example.ui.components.PulseScreenScaffold
import com.example.ui.components.PulseSectionHeader
import com.example.ui.components.pulseRotaryScroll
import com.example.ui.theme.PulseAnimations
import com.example.ui.theme.PulseIconSizes
import com.example.ui.theme.PulsePadding
import com.example.ui.theme.PulseSpacing
import com.example.utils.rememberPulseHapticFeedback

@Composable
fun PlayerScreen(
    onNavigateBack: () -> Unit,
    onNavigateToQueue: () -> Unit = {},
    viewModel: PlayerViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberScalingLazyListState()
    val haptic = rememberPulseHapticFeedback()

    PulseScreenScaffold(scrollState = listState, modifier = modifier) {
    ScalingLazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).pulseRotaryScroll(listState),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PulsePadding.ScreenContent,
        verticalArrangement = Arrangement.spacedBy(PulseSpacing.md)
    ) {
        // Top section: Back Button, Title, Artist
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(horizontal = PulseSpacing.md)
            ) {
                PulseIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    onClick = onNavigateBack,
                    contentDescription = "Navigate back",
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(PulseSpacing.sm))
                Text(
                    text = uiState.title.ifBlank { "Nothing Playing" },
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = uiState.artist.ifBlank { "Search for something to play" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Center section: Circular Progress & Play/Pause
        item {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(100.dp).padding(PulseSpacing.sm)
            ) {
                val progress by animateFloatAsState(
                    targetValue = if (uiState.duration > 0) uiState.currentPosition.toFloat() / uiState.duration.toFloat() else 0f,
                    animationSpec = PulseAnimations.standardTween(),
                    label = "progress"
                )
                
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 4.dp
                )
                
                PulseIconButton(
                    icon = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    onClick = {
                        haptic.performClick()
                        viewModel.togglePlayPause()
                    },
                    contentDescription = if (uiState.isPlaying) "Pause track" else "Play track",
                    modifier = Modifier.size(64.dp)
                )
            }
        }

        // Below Center: Previous, Next
        item {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                PulseIconButton(
                    icon = Icons.Default.SkipPrevious,
                    onClick = {
                        haptic.performClick()
                        viewModel.previousTrack()
                    },
                    contentDescription = "Skip to previous track",
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.width(PulseSpacing.xl))
                PulseIconButton(
                    icon = Icons.Default.SkipNext,
                    onClick = {
                        haptic.performClick()
                        viewModel.nextTrack()
                    },
                    contentDescription = "Skip to next track",
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        // Bottom Controls: Shuffle, Repeat, Queue
        item {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                PulseIconButton(
                    icon = Icons.Default.Shuffle,
                    onClick = {
                        haptic.performClick()
                        viewModel.toggleShuffle()
                    },
                    contentDescription = "Toggle shuffle",
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(PulseSpacing.md))
                PulseIconButton(
                    icon = when (uiState.repeatMode) {
                        1 -> Icons.Default.Repeat
                        2 -> Icons.Default.RepeatOne
                        else -> Icons.Default.Repeat
                    },
                    onClick = {
                        haptic.performClick()
                        viewModel.toggleRepeat()
                    },
                    contentDescription = "Toggle repeat mode",
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(PulseSpacing.md))
                PulseIconButton(
                    icon = Icons.AutoMirrored.Filled.QueueMusic,
                    onClick = {
                        haptic.performClick()
                        onNavigateToQueue()
                    },
                    contentDescription = "View queue",
                    modifier = Modifier.size(40.dp)
                )
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(PulseSpacing.md))
        }

        // Playback Options
        item { PulseSectionHeader(title = "Playback Options") }
        item {
            PulseListItem(
                label = "Audio Quality",
                icon = Icons.Default.MusicNote,
                secondaryLabel = "High",
                onClick = { haptic.performClick() }
            )
        }
        item {
            PulseListItem(
                label = "Playback Device",
                icon = Icons.Default.Speaker,
                secondaryLabel = android.os.Build.MODEL ?: "This Watch",
                onClick = { haptic.performClick() }
            )
        }
        item {
            PulseListItem(
                label = "Song Information",
                icon = Icons.Default.Info,
                onClick = { haptic.performClick() }
            )
        }
    }
    }
}
