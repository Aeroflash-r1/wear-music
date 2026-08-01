package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Search : Screen("search")
    object Player : Screen("player")
    object Queue : Screen("queue")
    object RecentlyPlayed : Screen("recently_played")
    object Favorites : Screen("favorites")
    object Downloads : Screen("downloads")
    object Settings : Screen("settings")
    object About : Screen("about")

    class Artist(val id: String) : Screen("artist/$id") {
        companion object { const val routePattern = "artist/{artistId}" }
    }
    class Album(val id: String) : Screen("album/$id") {
        companion object { const val routePattern = "album/{albumId}" }
    }
    class Playlist(val id: String) : Screen("playlist/$id") {
        companion object { const val routePattern = "playlist/{playlistId}" }
    }
}
