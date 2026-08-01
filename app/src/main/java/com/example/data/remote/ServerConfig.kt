package com.example.data.remote

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Holds the user-configured pulse-server URL. Updated by SettingsRepositoryImpl
 * whenever the user changes "Server URL" in Settings, and read by
 * [ServerUrlInterceptor] on every request so the app always talks to the current
 * Tailscale address without rebuilding Retrofit.
 */
@Singleton
class ServerConfig @Inject constructor() {

    @Volatile
    var baseUrl: String = ""
        private set

    /** Normalizes (trims, strips trailing slash) and stores the base URL. */
    fun update(url: String) {
        baseUrl = url.trim().trimEnd('/')
    }

    fun isConfigured(): Boolean = baseUrl.isNotBlank()
}

/**
 * Rewrites the request URL's scheme/host/port to the configured pulse-server URL.
 * Retrofit is built once with a placeholder base URL; this interceptor makes the
 * real destination dynamic.
 */
@Singleton
class ServerUrlInterceptor @Inject constructor(
    private val serverConfig: ServerConfig
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val base = serverConfig.baseUrl
        if (base.isBlank()) {
            throw IOException("Pulse server not configured — set Server URL in Settings")
        }
        val target = base.toHttpUrlOrNull()
            ?: throw IOException("Invalid server URL: $base")

        val original = chain.request()
        val rewritten = original.url.newBuilder()
            .scheme(target.scheme)
            .host(target.host)
            .port(target.port)
            .build()

        return chain.proceed(original.newBuilder().url(rewritten).build())
    }
}
