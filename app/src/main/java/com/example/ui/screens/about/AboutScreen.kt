package com.example.ui.screens.about

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.example.ui.components.PulseListItem
import com.example.ui.components.PulseScreenScaffold
import com.example.ui.components.PulseSectionHeader
import com.example.ui.components.pulseRotaryScroll

@Composable
fun AboutScreen(
    modifier: Modifier = Modifier
) {
    val listState = rememberScalingLazyListState()

    PulseScreenScaffold(scrollState = listState, modifier = modifier) {
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        ScalingLazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).pulseRotaryScroll(listState)
        ) {
            item { PulseSectionHeader("About Pulse") }

            item {
                PulseListItem(
                    label = "Pulse Wear OS",
                    secondaryLabel = "v1.0.0 Production",
                    icon = Icons.Default.MusicNote,
                    onClick = { }
                )
            }

            item {
                PulseListItem(
                    label = "Audio Engine",
                    secondaryLabel = "Media3 ExoPlayer",
                    icon = Icons.Default.Memory,
                    onClick = { }
                )
            }

            item {
                PulseListItem(
                    label = "Storage & Cache",
                    secondaryLabel = "Room DB Persistence",
                    icon = Icons.Default.Storage,
                    onClick = { }
                )
            }

            item {
                PulseListItem(
                    label = "Background Sync",
                    secondaryLabel = "WorkManager Auto Sync",
                    icon = Icons.Default.Sync,
                    onClick = { }
                )
            }

            item {
                PulseListItem(
                    label = "Backend",
                    secondaryLabel = "Self-hosted Ktor + yt-dlp server",
                    icon = Icons.Default.Info,
                    onClick = { }
                )
            }
        }
    }
    }
}
