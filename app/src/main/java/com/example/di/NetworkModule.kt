package com.example.di

import android.content.Context
import com.example.BuildConfig
import com.example.data.remote.ServerConfig
import com.example.data.remote.ServerUrlInterceptor
import com.example.data.remote.api.PulseApi
import com.example.data.repository.BackendRepositoryImpl
import com.example.domain.repository.BackendRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.ConnectionPool
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    @Provides
    @Singleton
    fun provideMoshiConverterFactory(moshi: Moshi): MoshiConverterFactory {
        return MoshiConverterFactory.create(moshi)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        @ApplicationContext context: Context
    ): OkHttpClient {
        val cacheDir = File(context.cacheDir, "http_cache")
        val cache = Cache(cacheDir, 10L * 1024L * 1024L) // 10 MB Cache

        val userAgentInterceptor = Interceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "PulseWearOS/1.0 (Android; WearOS)")
                .header("Accept-Language", "en-US,en;q=0.9")
                .build()
            chain.proceed(request)
        }

        val builder = OkHttpClient.Builder()
            .cache(cache)
            .connectionPool(ConnectionPool(5, 5, TimeUnit.MINUTES))
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(userAgentInterceptor)

        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            builder.addInterceptor(loggingInterceptor)
        }

        return builder.build()
    }

    @Provides
    @Singleton
    fun provideServerConfig(): ServerConfig = ServerConfig()

    @Provides
    @Singleton
    fun provideServerUrlInterceptor(serverConfig: ServerConfig): ServerUrlInterceptor {
        return ServerUrlInterceptor(serverConfig)
    }

    @Provides
    @Singleton
    fun providePulseApi(
        okHttpClient: OkHttpClient,
        moshiConverterFactory: MoshiConverterFactory,
        serverUrlInterceptor: ServerUrlInterceptor
    ): PulseApi {
        // yt-dlp on the server resolves each search result, which can take 10-40s,
        // so the server-bound client needs a much longer read timeout than the
        // default 15s client. Connect timeout stays short for fast failure.
        val serverClient = okHttpClient.newBuilder()
            .addInterceptor(serverUrlInterceptor)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl("http://pulse.local/") // placeholder; host rewritten by interceptor
            .client(serverClient)
            .addConverterFactory(moshiConverterFactory)
            .build()
            .create(PulseApi::class.java)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindBackendRepository(
        impl: BackendRepositoryImpl
    ): BackendRepository
}
