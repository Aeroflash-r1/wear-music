package com.example.di

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.offline.DownloadManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import java.util.concurrent.Executors
import javax.inject.Singleton

@OptIn(UnstableApi::class)
@Module
@InstallIn(SingletonComponent::class)
object DownloadModule {

    @Provides
    @Singleton
    fun provideSimpleCache(
        @ApplicationContext context: Context
    ): SimpleCache {
        val cacheDir = File(context.cacheDir, "media_cache")
        val evictor = LeastRecentlyUsedCacheEvictor(500 * 1024 * 1024) // 500MB LRU Cache
        val databaseProvider = StandaloneDatabaseProvider(context)
        return SimpleCache(cacheDir, evictor, databaseProvider)
    }

    @Provides
    @Singleton
    fun provideCacheDataSourceFactory(
        cache: SimpleCache
    ): CacheDataSource.Factory {
        val upstreamFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("PulseWearOS/1.0")
            .setAllowCrossProtocolRedirects(true)

        return CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(upstreamFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }

    @Provides
    @Singleton
    fun provideDownloadManager(
        @ApplicationContext context: Context,
        cache: SimpleCache,
        cacheDataSourceFactory: CacheDataSource.Factory
    ): DownloadManager {
        val databaseProvider = StandaloneDatabaseProvider(context)
        val downloadExecutor = Executors.newFixedThreadPool(2)
        return DownloadManager(
            context,
            databaseProvider,
            cache,
            cacheDataSourceFactory,
            downloadExecutor
        ).apply {
            maxParallelDownloads = 2
        }
    }
}
