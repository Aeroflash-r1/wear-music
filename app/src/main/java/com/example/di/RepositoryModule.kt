package com.example.di

import com.example.data.repository.DownloadsRepositoryImpl
import com.example.data.repository.Media3PlaybackRepositoryImpl
import com.example.data.repository.SettingsRepositoryImpl
import com.example.data.repository.TrackRepositoryImpl
import com.example.domain.repository.DownloadsRepository
import com.example.domain.repository.PlaybackRepository
import com.example.domain.repository.PlayerRepository
import com.example.domain.repository.SettingsRepository
import com.example.domain.repository.TrackRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTrackRepository(impl: TrackRepositoryImpl): TrackRepository

    @Binds
    @Singleton
    abstract fun bindPlayerRepository(impl: Media3PlaybackRepositoryImpl): PlayerRepository

    @Binds
    @Singleton
    abstract fun bindPlaybackRepository(impl: Media3PlaybackRepositoryImpl): PlaybackRepository

    @Binds
    @Singleton
    abstract fun bindDownloadsRepository(impl: DownloadsRepositoryImpl): DownloadsRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindRecommendationRepository(impl: com.example.data.repository.RecommendationRepositoryImpl): com.example.domain.repository.RecommendationRepository

    @Binds
    @Singleton
    abstract fun bindArtistRepository(impl: com.example.data.repository.ArtistRepositoryImpl): com.example.domain.repository.ArtistRepository

    @Binds
    @Singleton
    abstract fun bindAlbumRepository(impl: com.example.data.repository.AlbumRepositoryImpl): com.example.domain.repository.AlbumRepository

    @Binds
    @Singleton
    abstract fun bindPlaylistRepository(impl: com.example.data.repository.PlaylistRepositoryImpl): com.example.domain.repository.PlaylistRepository

    @Binds
    @Singleton
    abstract fun bindLibraryRepository(impl: com.example.data.repository.LibraryRepositoryImpl): com.example.domain.repository.LibraryRepository

    @Binds
    @Singleton
    abstract fun bindSyncRepository(impl: com.example.data.sync.SyncRepositoryImpl): com.example.domain.repository.SyncRepository

    @Binds
    @Singleton
    abstract fun bindOfflineRepository(impl: com.example.data.offline.OfflineManager): com.example.domain.repository.OfflineRepository
}
