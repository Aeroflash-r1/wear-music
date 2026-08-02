package com.example.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.domain.model.SearchResult
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.example.ui.components.PulseEmptyState
import com.example.ui.components.PulseListItem
import com.example.ui.components.PulseLoadingIndicator
import com.example.ui.components.PulseScreenScaffold
import com.example.ui.components.PulseSecondaryButton
import com.example.ui.components.PulseSectionHeader
import com.example.ui.components.pulseRotaryScroll
import com.example.ui.navigation.Screen
import com.example.ui.theme.PulseIconSizes
import com.example.ui.theme.PulsePadding
import com.example.ui.theme.PulseRadius
import com.example.ui.theme.PulseSpacing

private val searchFilters = listOf("All", "Albums", "Artists")

private fun iconForType(type: String) = when (type) {
    "album" -> Icons.Default.Album
    "artist" -> Icons.Default.Person
    "playlist" -> Icons.AutoMirrored.Filled.QueueMusic
    else -> Icons.Default.MusicNote
}

@Composable
private fun SearchFilterChips(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(PulseSpacing.xs)
    ) {
        searchFilters.forEach { filter ->
            val isSelected = filter == selectedFilter
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(PulseRadius.full))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceContainer
                    )
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        RoundedCornerShape(PulseRadius.full)
                    )
                    .clickable { onFilterSelected(filter) }
                    .padding(vertical = PulseSpacing.sm),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = filter,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun SearchScreen(
    onNavigate: (Screen) -> Unit = {},
    viewModel: SearchViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val query by viewModel.query.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState()
    val listState = rememberScalingLazyListState()
    
    var selectedResultForDialog by remember { mutableStateOf<SearchResult?>(null) }

    fun openResult(result: SearchResult) {
        when (result.type) {
            "album" -> onNavigate(Screen.Album(result.id))
            "artist" -> onNavigate(Screen.Artist(result.id))
            "playlist" -> onNavigate(Screen.Playlist(result.id))
            else -> viewModel.playResult(result)
        }
    }
    
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
                PulseSearchInput(
                    query = query,
                    onQueryChanged = viewModel::onQueryChanged,
                    onClear = viewModel::onClearQuery
                )
            }

            item {
                SearchFilterChips(
                    selectedFilter = selectedFilter,
                    onFilterSelected = viewModel::onFilterSelected
                )
            }

            when (val state = uiState) {
                is SearchUiState.RecentSearches -> {
                    if (recentSearches.isNotEmpty()) {
                        item { PulseSectionHeader(title = "Recent Searches") }
                        items(recentSearches, key = { it }) { recent ->
                            PulseListItem(
                                label = recent,
                                icon = Icons.Default.History,
                                onClick = { viewModel.onRecentSearchClicked(recent) }
                            )
                        }
                    }
                }
                is SearchUiState.Loading -> {
                    item { PulseLoadingIndicator(modifier = Modifier.padding(top = PulseSpacing.lg)) }
                }
                is SearchUiState.NoResults -> {
                    item {
                        PulseEmptyState(
                            message = "No results found for '$query'",
                            icon = Icons.Default.Search,
                            modifier = Modifier.padding(top = PulseSpacing.lg)
                        )
                    }
                }
                is SearchUiState.Error -> {
                    item {
                        PulseEmptyState(
                            message = state.message,
                            icon = Icons.Default.Warning,
                            modifier = Modifier.padding(top = PulseSpacing.lg)
                        )
                    }
                }
                is SearchUiState.Results -> {
                    item { PulseSectionHeader(title = "Results") }
                    items(state.items, key = { "${it.type}:${it.id}" }) { result ->
                        PulseListItem(
                            label = result.title,
                            secondaryLabel = "${result.artist} • ${result.duration}",
                            icon = iconForType(result.type),
                            onClick = { openResult(result) },
                            onLongClick = {
                                if (result.type == "song") selectedResultForDialog = result
                            }
                        )
                    }
                }
            }
        }
        
        // Options Dialog overlay
        if (selectedResultForDialog != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                val dialogListState = rememberScalingLazyListState()
                selectedResultForDialog?.let { result ->
                    ScalingLazyColumn(
                        state = dialogListState,
                        modifier = Modifier.fillMaxSize().pulseRotaryScroll(dialogListState),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        contentPadding = PulsePadding.ScreenContent,
                        verticalArrangement = Arrangement.spacedBy(PulseSpacing.sm)
                    ) {
                        item { PulseSectionHeader(title = result.title) }
                        item {
                            PulseListItem(
                                label = "Play",
                                icon = Icons.Default.PlayArrow,
                                onClick = {
                                    viewModel.playResult(result)
                                    selectedResultForDialog = null
                                }
                            )
                        }
                        item {
                            PulseListItem(
                                label = "Add to Queue",
                                icon = Icons.AutoMirrored.Filled.QueueMusic,
                                onClick = {
                                    viewModel.addToQueue(result)
                                    selectedResultForDialog = null
                                }
                            )
                        }
                        item {
                            PulseListItem(
                                label = "Favorite",
                                icon = Icons.Default.Favorite,
                                onClick = {
                                    viewModel.toggleFavorite(result)
                                    selectedResultForDialog = null
                                }
                            )
                        }
                        item {
                            PulseListItem(
                                label = "Download",
                                icon = Icons.Default.Download,
                                onClick = {
                                    viewModel.download(result)
                                    selectedResultForDialog = null
                                }
                            )
                        }
                        item {
                            PulseSecondaryButton(
                                label = "Cancel",
                                onClick = { selectedResultForDialog = null }
                            )
                        }
                    }
                }
            }
        }
    }
    }
}

@Composable
fun PulseSearchInput(
    query: String,
    onQueryChanged: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(PulseRadius.full))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(PulseRadius.full))
            .padding(horizontal = PulseSpacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(PulseIconSizes.md)
        )
        Spacer(modifier = Modifier.width(PulseSpacing.sm))
        BasicTextField(
            value = query,
            onValueChange = onQueryChanged,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
            modifier = Modifier.weight(1f),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            decorationBox = { innerTextField ->
                if (query.isEmpty()) {
                    Text("Search...", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                }
                innerTextField()
            }
        )
        if (query.isNotEmpty()) {
            Spacer(modifier = Modifier.width(PulseSpacing.sm))
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Clear",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(PulseIconSizes.md)
                    .clickable { onClear() }
            )
        }
    }
}
