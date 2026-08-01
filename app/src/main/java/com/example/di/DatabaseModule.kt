package com.example.di

import android.content.Context
import androidx.room.Room
import com.example.data.database.PulseDatabase
import com.example.data.database.dao.DownloadDao
import com.example.data.database.dao.FavoriteDao
import com.example.data.database.dao.HistoryDao
import com.example.data.database.dao.QueueDao
import com.example.data.database.dao.RecentSearchDao
import com.example.data.database.dao.TrackDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun providePulseDatabase(
        @ApplicationContext context: Context
    ): PulseDatabase {
        // No seed data here on purpose: the app should start with a genuinely
        // empty library and fill up from real backend/search/download activity,
        // rather than showing a hardcoded demo catalog on first launch.
        return Room.databaseBuilder(
            context,
            PulseDatabase::class.java,
            "pulse_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideTrackDao(database: PulseDatabase): TrackDao = database.trackDao()

    @Provides
    fun provideFavoriteDao(database: PulseDatabase): FavoriteDao = database.favoriteDao()

    @Provides
    fun provideHistoryDao(database: PulseDatabase): HistoryDao = database.historyDao()

    @Provides
    fun provideQueueDao(database: PulseDatabase): QueueDao = database.queueDao()

    @Provides
    fun provideDownloadDao(database: PulseDatabase): DownloadDao = database.downloadDao()

    @Provides
    fun provideRecentSearchDao(database: PulseDatabase): RecentSearchDao = database.recentSearchDao()
}
