package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.example.ui.components.PulseCard
import com.example.ui.components.PulseListItem
import com.example.ui.components.PulseScreenScaffold
import com.example.ui.components.PulseSectionHeader
import com.example.ui.components.pulseRotaryScroll
import com.example.ui.navigation.Screen
import com.example.ui.theme.PulseIconSizes
import com.example.ui.theme.PulsePadding
import com.example.ui.theme.PulseSpacing

@Composable
fun HomeScreen(
    onNavigate: (Screen) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val listState = rememberScalingLazyListState()
    val isOffline by viewModel.isOffline.collectAsState()
    val playerState by viewModel.playerUiState.collectAsState()
    val feedState by viewModel.homeFeed.collectAsState()
    val recentlyPlayed by viewModel.recentlyPlayed.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val downloads by viewModel.downloads.collectAsState()

    PulseScreenScaffold(scrollState = listState, modifier = modifier) {
    ScalingLazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().pulseRotaryScroll(listState),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PulsePadding.ScreenContent,
        verticalArrangement = Arrangement.spacedBy(PulseSpacing.sm)
    ) {
        // Brand Header
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = PulseSpacing.xs)
            ) {
                Box(
                    modifier = Modifier
                        .size(PulseIconSizes.md)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.width(4.dp).height(12.dp).clip(CircleShape).background(Color.Black))
                }
                Spacer(modifier = Modifier.width(PulseSpacing.sm))
                Text(
                    text = "PULSE",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        // Offline Banner
        if (isOffline) {
            item {
                PulseCard(onClick = {}) {
                    Text(
                        text = "OFFLINE MODE",
                        color = Color(0xFFFFB74D),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "Playing saved downloads & library",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Now Playing Card
        item {
            PulseCard(
                onClick = { onNavigate(Screen.Player) }
            ) {
                Text(
                    text = if (playerState.isPlaying) "NOW PLAYING" else "CONTINUE LISTENING",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Text(
                    text = playerState.title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = playerState.artist,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }

        // Quick Navigation Section
        item {
            PulseListItem(
                label = "Search",
                icon = Icons.Default.Search,
                onClick = { onNavigate(Screen.Search) }
            )
        }

        item {
            PulseListItem(
                label = "Recently Played",
                secondaryLabel = "${recentlyPlayed.size} tracks",
                icon = Icons.Default.History,
                onClick = { onNavigate(Screen.RecentlyPlayed) }
            )
        }

        item {
            PulseListItem(
                label = "Favorites",
                secondaryLabel = "${favorites.size} saved",
                icon = Icons.Default.Favorite,
                onClick = { onNavigate(Screen.Favorites) }
            )
        }

        item {
            PulseListItem(
                label = "Downloads",
                secondaryLabel = "${downloads.size} offline",
                icon = Icons.Default.Download,
                onClick = { onNavigate(Screen.Downloads) }
            )
        }

        // Recommended For You
        if (!isOffline && feedState.recommended.isNotEmpty()) {
            item { PulseSectionHeader(title = "Recommended For You") }
            items(feedState.recommended.take(3), key = { it.id }) { item ->
                PulseListItem(
                    label = item.title,
                    secondaryLabel = item.artist,
                    icon = Icons.Default.PlayArrow,
                    onClick = { viewModel.playTrack(item) }
                )
            }
        }

        // Trending
        if (!isOffline && feedState.trending.isNotEmpty()) {
            item { PulseSectionHeader(title = "Trending Now") }
            items(feedState.trending.take(3), key = { it.id }) { item ->
                PulseListItem(
                    label = item.title,
                    secondaryLabel = item.artist,
                    icon = Icons.Default.MusicNote,
                    onClick = { viewModel.playTrack(item) }
                )
            }
        }

        // Popular Albums
        if (!isOffline && feedState.popularAlbums.isNotEmpty()) {
            item { PulseSectionHeader(title = "Popular Albums") }
            items(feedState.popularAlbums, key = { it.id }) { album ->
                PulseListItem(
                    label = album.title,
                    secondaryLabel = album.artist,
                    icon = Icons.Default.Album,
                    onClick = { onNavigate(Screen.Album(album.id)) }
                )
            }
        }

        // Popular Artists
        if (!isOffline && feedState.popularArtists.isNotEmpty()) {
            item { PulseSectionHeader(title = "Popular Artists") }
            items(feedState.popularArtists, key = { it.id }) { artist ->
                PulseListItem(
                    label = artist.name,
                    icon = Icons.Default.Person,
                    onClick = { onNavigate(Screen.Artist(artist.id)) }
                )
            }
        }

        // Settings
        item {
            PulseListItem(
                label = "Settings",
                icon = Icons.Default.Settings,
                onClick = { onNavigate(Screen.Settings) }
            )
        }
    }
    }
}
