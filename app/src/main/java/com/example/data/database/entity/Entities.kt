package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val duration: String,
    val audioQuality: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val id: String,
    val trackId: String,
    val type: String = "song", // "song", "album", "artist", "playlist"
    val title: String = "",
    val subtitle: String = "",
    val thumbnailUrl: String? = null,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey val id: String,
    val trackId: String,
    val playedAt: Long = System.currentTimeMillis(),
    val lastPositionMs: Long = 0L
)

@Entity(tableName = "queue")
data class QueueEntity(
    @PrimaryKey val id: String,
    val trackId: String,
    val position: Int
)

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val id: String,
    val trackId: String,
    val filePath: String,
    val fileSize: String,
    val downloadDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "recent_searches")
data class RecentSearchEntity(
    @PrimaryKey val query: String,
    val searchedAt: Long = System.currentTimeMillis()
)
