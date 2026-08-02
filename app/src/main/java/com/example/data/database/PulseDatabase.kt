package com.example.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.data.database.dao.DownloadDao
import com.example.data.database.dao.FavoriteDao
import com.example.data.database.dao.HistoryDao
import com.example.data.database.dao.QueueDao
import com.example.data.database.dao.RecentSearchDao
import com.example.data.database.dao.TrackDao
import com.example.data.database.entity.DownloadEntity
import com.example.data.database.entity.FavoriteEntity
import com.example.data.database.entity.HistoryEntity
import com.example.data.database.entity.QueueEntity
import com.example.data.database.entity.RecentSearchEntity
import com.example.data.database.entity.TrackEntity

@Database(
    entities = [
        TrackEntity::class,
        FavoriteEntity::class,
        HistoryEntity::class,
        QueueEntity::class,
        DownloadEntity::class,
        RecentSearchEntity::class
    ],
    // History trimming changes runtime behavior only; no schema migration is needed.
    version = 3,
    exportSchema = false
)
abstract class PulseDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun historyDao(): HistoryDao
    abstract fun queueDao(): QueueDao
    abstract fun downloadDao(): DownloadDao
    abstract fun recentSearchDao(): RecentSearchDao
}
