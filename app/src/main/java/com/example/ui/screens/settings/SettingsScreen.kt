package com.example.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.Api
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.ToggleOff
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import com.example.ui.components.PulseListItem
import com.example.ui.components.PulseScreenScaffold
import com.example.ui.components.PulseSecondaryButton
import com.example.ui.components.PulseSectionHeader
import com.example.ui.components.pulseRotaryScroll
import com.example.ui.screens.search.PulseSearchInput
import com.example.ui.theme.PulsePadding
import com.example.ui.theme.PulseSpacing

@Composable
fun PulseToggleItem(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    secondaryLabel: String? = null
) {
    PulseListItem(
        label = label,
        onClick = { onCheckedChange(!checked) },
        modifier = modifier,
        icon = icon,
        secondaryLabel = secondaryLabel,
        trailingContent = {
            Icon(
                imageVector = if (checked) Icons.Default.ToggleOn else Icons.Default.ToggleOff,
                contentDescription = if (checked) "Enabled" else "Disabled",
                tint = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val syncState by viewModel.syncUiState.collectAsState()
    val connectionTest by viewModel.connectionTest.collectAsState()
    val forceOfflineMode by viewModel.forceOfflineMode.collectAsState()
    val listState = rememberScalingLazyListState()

    var editingServerUrl by remember { mutableStateOf(false) }
    var serverUrlDraft by remember { mutableStateOf("") }

    val lastSyncStr = if (syncState.lastSyncTimeMs > 0) {
        val diffSec = (System.currentTimeMillis() - syncState.lastSyncTimeMs) / 1000
        when {
            diffSec < 60 -> "Just now"
            diffSec < 3600 -> "${diffSec / 60}m ago"
            else -> "${diffSec / 3600}h ago"
        }
    } else "Never"

    PulseScreenScaffold(scrollState = listState, modifier = modifier) {
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        ScalingLazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().pulseRotaryScroll(listState),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PulsePadding.ScreenContent,
            verticalArrangement = Arrangement.spacedBy(PulseSpacing.sm)
        ) {
            item { PulseSectionHeader("Settings") }

            // Synchronization
            item { PulseSectionHeader("Synchronization") }
            item {
                PulseListItem(
                    label = "Sync Now",
                    secondaryLabel = "Status: ${syncState.status.name}",
                    icon = Icons.Default.Sync,
                    onClick = { viewModel.triggerSync() }
                )
            }
            item {
                PulseListItem(
                    label = "Last Sync",
                    secondaryLabel = lastSyncStr,
                    icon = Icons.Default.Sync,
                    onClick = { }
                )
            }
            item {
                PulseToggleItem(
                    label = "Auto Sync",
                    checked = syncState.autoSyncEnabled,
                    onCheckedChange = { viewModel.toggleAutoSync(it) },
                    icon = Icons.Default.Sync
                )
            }
            item {
                PulseToggleItem(
                    label = "Sync on Wi-Fi Only",
                    checked = syncState.syncOnWifiOnly,
                    onCheckedChange = { viewModel.toggleSyncOnWifiOnly(it) },
                    icon = Icons.Default.Wifi
                )
            }

            // Offline & Network
            item { PulseSectionHeader("Offline & Network") }
            item {
                PulseToggleItem(
                    label = "Force Offline Mode",
                    checked = forceOfflineMode,
                    onCheckedChange = { viewModel.toggleForceOfflineMode(it) },
                    icon = Icons.Default.NetworkCheck
                )
            }
            item {
                PulseListItem(
                    label = "Network State",
                    secondaryLabel = syncState.networkState.name.replace("CONNECTED_", ""),
                    icon = Icons.Default.Wifi,
                    onClick = { }
                )
            }
            item {
                PulseListItem(
                    label = "Active Backend",
                    secondaryLabel = syncState.activeBackend,
                    icon = Icons.Default.Api,
                    onClick = { }
                )
            }

            // Playback
            item { PulseSectionHeader("Playback") }
            item {
                PulseListItem(
                    label = "Audio Quality",
                    secondaryLabel = uiState.audioQuality,
                    icon = Icons.Default.GraphicEq,
                    onClick = { }
                )
            }
            item {
                PulseToggleItem(
                    label = "Audio Offload",
                    checked = uiState.audioOffload,
                    onCheckedChange = { viewModel.toggleAudioOffload() },
                    icon = Icons.Default.Memory
                )
            }
            item {
                PulseToggleItem(
                    label = "Gapless Playback",
                    checked = uiState.gaplessPlayback,
                    onCheckedChange = { viewModel.toggleGaplessPlayback() },
                    icon = Icons.Default.Animation
                )
            }
            item {
                PulseToggleItem(
                    label = "Normalize Volume",
                    checked = uiState.normalizeVolume,
                    onCheckedChange = { viewModel.toggleNormalizeVolume() },
                    icon = Icons.Default.GraphicEq
                )
            }

            // Downloads
            item { PulseSectionHeader("Downloads") }
            item {
                PulseListItem(
                    label = "Retry Failed Downloads",
                    secondaryLabel = "${syncState.failedDownloadsCount} failed",
                    icon = Icons.Default.Download,
                    onClick = { viewModel.retryFailedDownloads() }
                )
            }
            item {
                PulseListItem(
                    label = "Max Concurrent Downloads",
                    secondaryLabel = "${syncState.maxConcurrentDownloads}",
                    icon = Icons.Default.Download,
                    onClick = {
                        val next = if (syncState.maxConcurrentDownloads >= 3) 1 else syncState.maxConcurrentDownloads + 1
                        viewModel.setMaxConcurrentDownloads(next)
                    }
                )
            }
            item {
                PulseListItem(
                    label = "Download Quality",
                    secondaryLabel = uiState.downloadQuality,
                    icon = Icons.Default.Download,
                    onClick = { }
                )
            }
            item {
                PulseToggleItem(
                    label = "Auto Download",
                    checked = uiState.autoDownload,
                    onCheckedChange = { viewModel.toggleAutoDownload() },
                    icon = Icons.Default.Sync
                )
            }
            item {
                PulseToggleItem(
                    label = "Download over Wi-Fi",
                    checked = uiState.downloadOverWifi,
                    onCheckedChange = { viewModel.toggleDownloadOverWifi() },
                    icon = Icons.Default.Wifi
                )
            }

            // Cache
            item { PulseSectionHeader("Cache") }
            item {
                PulseListItem(
                    label = "Current Cache Size",
                    secondaryLabel = uiState.currentCacheSize,
                    icon = Icons.Default.Storage,
                    onClick = { }
                )
            }
            item {
                PulseListItem(
                    label = "Cache Limit",
                    secondaryLabel = uiState.cacheLimit,
                    icon = Icons.Default.SdStorage,
                    onClick = { }
                )
            }
            item {
                PulseListItem(
                    label = "Clear Cache",
                    icon = Icons.Default.Delete,
                    onClick = { viewModel.showClearCacheDialog() }
                )
            }

            // Backend
            item { PulseSectionHeader("Backend") }
            item {
                PulseListItem(
                    label = "Server URL",
                    secondaryLabel = uiState.serverUrl.ifBlank { "Not configured" },
                    icon = Icons.Default.Api,
                    onClick = {
                        serverUrlDraft = uiState.serverUrl
                        editingServerUrl = true
                    }
                )
            }
            item {
                PulseListItem(
                    label = "Connection Test",
                    secondaryLabel = connectionTest ?: "Tap to test",
                    icon = Icons.Default.CheckCircle,
                    onClick = { viewModel.testConnection() }
                )
            }
            item {
                PulseListItem(
                    label = "Backend Status",
                    secondaryLabel = if (syncState.isOffline) "Offline" else if (uiState.serverUrl.isBlank()) "Not configured" else "Connected",
                    icon = Icons.Default.Api,
                    onClick = { }
                )
            }

            // Appearance
            item { PulseSectionHeader("Appearance") }
            item {
                PulseToggleItem(
                    label = "Dynamic Color",
                    checked = uiState.dynamicColor,
                    onCheckedChange = { viewModel.toggleDynamicColor() },
                    icon = Icons.Default.ColorLens
                )
            }
            item {
                PulseToggleItem(
                    label = "AMOLED Dark Theme",
                    checked = uiState.amoledDarkTheme,
                    onCheckedChange = { viewModel.toggleAmoledDarkTheme() },
                    icon = Icons.Default.DarkMode
                )
            }
            item {
                PulseListItem(
                    label = "Animation Speed",
                    secondaryLabel = uiState.animationSpeed,
                    icon = Icons.Default.Animation,
                    onClick = { }
                )
            }

            // About
            item { PulseSectionHeader("About") }
            item {
                PulseListItem(
                    label = "Pulse Version",
                    secondaryLabel = uiState.pulseVersion,
                    icon = Icons.Default.Info,
                    onClick = { viewModel.onVersionClicked() }
                )
            }
            item {
                PulseListItem(
                    label = "Build Type",
                    secondaryLabel = uiState.buildType,
                    icon = Icons.Default.Settings,
                    onClick = { }
                )
            }
            item {
                PulseListItem(
                    label = "App Size",
                    secondaryLabel = uiState.appSize,
                    icon = Icons.Default.Storage,
                    onClick = { }
                )
            }
            item {
                PulseListItem(
                    label = "Open Source Licenses",
                    icon = Icons.Default.Code,
                    onClick = { }
                )
            }

            // Developer Options
            if (uiState.developerOptionsUnlocked) {
                item { PulseSectionHeader("Developer Options") }
                item {
                    PulseListItem(
                        label = "Force Crash",
                        icon = Icons.Default.Settings,
                        onClick = { }
                    )
                }
                item {
                    PulseListItem(
                        label = "Debug Logs",
                        icon = Icons.Default.Settings,
                        onClick = { }
                    )
                }
            }
        }

        if (editingServerUrl) {
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
                    item { PulseSectionHeader(title = "Server URL") }
                    item {
                        PulseSearchInput(
                            query = serverUrlDraft,
                            onQueryChanged = { serverUrlDraft = it },
                            onClear = { serverUrlDraft = "" }
                        )
                    }
                    item {
                        PulseListItem(
                            label = "Save",
                            icon = Icons.Default.Check,
                            onClick = {
                                viewModel.setServerUrl(serverUrlDraft)
                                editingServerUrl = false
                            }
                        )
                    }
                    item {
                        PulseSecondaryButton(
                            label = "Cancel",
                            onClick = { editingServerUrl = false }
                        )
                    }
                }
            }
        }

        if (uiState.showClearCacheDialog) {
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
                    item { PulseSectionHeader(title = "Clear Cache?") }
                    item {
                        PulseListItem(
                            label = "Confirm",
                            icon = Icons.Default.Check,
                            onClick = { viewModel.clearCache() }
                        )
                    }
                    item {
                        PulseSecondaryButton(
                            label = "Cancel",
                            onClick = { viewModel.hideClearCacheDialog() }
                        )
                    }
                }
            }
        }
    }
    }
}
