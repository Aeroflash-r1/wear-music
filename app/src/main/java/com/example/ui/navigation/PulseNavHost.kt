package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.about.AboutScreen

@Composable
fun PulseNavHost(
    modifier: Modifier = Modifier
) {
    val navController = rememberSwipeDismissableNavController()

    // AppScaffold keeps TimeText anchored in place during swipe-to-dismiss and other
    // in-app transitions, instead of it jumping/disappearing between screens.
    AppScaffold(modifier = modifier) {
        SwipeDismissableNavHost(
            navController = navController,
            startDestination = Screen.Home.route
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigate = { screen ->
                        navController.navigate(screen.route)
                    }
                )
            }
            composable(Screen.Search.route) {
                com.example.ui.screens.search.SearchScreen(
                    onNavigate = { screen -> navController.navigate(screen.route) }
                )
            }
            composable(Screen.Player.route) {
                com.example.ui.screens.player.PlayerScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToQueue = { navController.navigate(Screen.Queue.route) }
                )
            }
            composable(Screen.Queue.route) {
                com.example.ui.screens.queue.QueueScreen()
            }
            composable(Screen.RecentlyPlayed.route) {
                com.example.ui.screens.history.RecentlyPlayedScreen()
            }
            composable(Screen.Favorites.route) {
                com.example.ui.screens.favorites.FavoritesScreen()
            }
            composable(Screen.Downloads.route) {
                com.example.ui.screens.downloads.DownloadsScreen()
            }
            composable(Screen.Settings.route) {
                com.example.ui.screens.settings.SettingsScreen()
            }
            composable(Screen.About.route) {
                AboutScreen()
            }
            composable(Screen.Artist.routePattern) {
                com.example.ui.screens.artist.ArtistScreen(
                    onNavigateToAlbum = { albumId ->
                        navController.navigate(Screen.Album(albumId).route)
                    }
                )
            }
            composable(Screen.Album.routePattern) {
                com.example.ui.screens.album.AlbumScreen()
            }
            composable(Screen.Playlist.routePattern) {
                com.example.ui.screens.playlist.PlaylistScreen()
            }
        }
    }
}
