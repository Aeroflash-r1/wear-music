package com.example.service

import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PulsePlaybackService : MediaSessionService() {

    @Inject
    lateinit var playerManager: PulsePlayerManager

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val player = playerManager.exoPlayer
        mediaSession = MediaSession.Builder(this, player).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        // playerManager and its ExoPlayer are an app-scoped Hilt singleton shared with
        // every ViewModel that controls playback directly (not just this service) — they
        // must outlive this specific Service instance. Only release the MediaSession
        // wrapper here; never call playerManager.release(), or the *next* time this
        // service is started (very common on Wear OS, which aggressively kills services
        // under memory pressure) every playback call will throw
        // "ExoPlayer has already been released" since ExoPlayer can't be reused once
        // released.
        mediaSession?.run {
            release()
            mediaSession = null
        }
        playerManager.onServiceDestroyed()
        super.onDestroy()
    }
}
