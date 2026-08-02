package com.example.service

import android.content.Context
import android.content.Intent
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.example.data.database.dao.HistoryDao
import com.example.data.database.dao.QueueDao
import com.example.data.database.dao.TrackDao
import com.example.data.database.entity.HistoryEntity
import com.example.data.database.entity.QueueEntity
import com.example.data.database.entity.TrackEntity
import com.example.domain.model.BackendResult
import com.example.domain.model.PlayerUiState
import com.example.domain.model.Track
import com.example.domain.repository.BackendRepository
import com.example.utils.DurationParser
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(UnstableApi::class)
@Singleton
class PulsePlayerManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cacheDataSourceFactory: CacheDataSource.Factory,
    private val historyDao: HistoryDao,
    private val queueDao: QueueDao,
    private val trackDao: TrackDao,
    private val backendRepository: BackendRepository
) {
    private val scope = CoroutineScope(Dispatchers.Main)
    private var positionUpdateJob: Job? = null
    private var sleepTimerJob: Job? = null
    private var currentPlaybackJob: Job? = null
    private var playbackRequestId = 0L

    private val currentQueue = mutableListOf<Track>()
    private var currentQueueIndex = 0
    @Volatile
    private var serviceStarted = false

    /**
     * Starts PulsePlaybackService so a MediaSession exists and the process gets
     * foreground-service protection. Guarded by a flag to avoid an IPC call to
     * ActivityManager on every single track change, but the flag is reset by
     * onServiceDestroyed() below if the OS ever actually kills the service (which
     * happens under memory pressure on Wear OS), so it can always come back.
     */
    private fun ensureServiceStarted() {
        if (serviceStarted) return
        serviceStarted = true
        runCatching {
            ContextCompat.startForegroundService(
                context,
                Intent(context, PulsePlaybackService::class.java)
            )
        }.onFailure {
            // Playback initiated while the app is backgrounded may not be allowed
            // to start a foreground service. Keep the player usable and retry on the
            // next user-initiated action rather than crashing the app.
            serviceStarted = false
        }
    }

    /** Called by PulsePlaybackService.onDestroy() so we know to restart it next time. */
    fun onServiceDestroyed() {
        serviceStarted = false
    }

    val exoPlayer: ExoPlayer by lazy {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        val mediaSourceFactory = DefaultMediaSourceFactory(context)
            .setDataSourceFactory(cacheDataSourceFactory)

        ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .setAudioAttributes(audioAttributes, true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .setHandleAudioBecomingNoisy(true)
            .build().apply {
                addListener(playerListener)
            }
    }

    private val _uiState = MutableStateFlow(
        PlayerUiState(
            title = "",
            artist = "",
            duration = 0L,
            currentPosition = 0L,
            isPlaying = false,
            shuffleEnabled = false,
            repeatMode = 0,
            buffering = false,
            playbackSpeed = 1.0f,
            sleepTimerRemainingMs = 0L,
            currentTrackId = "",
            queue = emptyList(),
            queueIndex = 0
        )
    )
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    init {
        restorePersistedQueue()
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _uiState.value = _uiState.value.copy(isPlaying = isPlaying)
            if (isPlaying) {
                startPositionUpdates()
                recordHistory(_uiState.value.currentTrackId)
            } else {
                stopPositionUpdates()
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            val isBuffering = playbackState == Player.STATE_BUFFERING
            val duration = if (exoPlayer.duration != C.TIME_UNSET && exoPlayer.duration > 0) exoPlayer.duration else _uiState.value.duration
            _uiState.value = _uiState.value.copy(
                buffering = isBuffering,
                duration = duration
            )
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            mediaItem?.mediaMetadata?.let { metadata ->
                val trackId = mediaItem.mediaId.ifEmpty { _uiState.value.currentTrackId }
                _uiState.value = _uiState.value.copy(
                    title = metadata.title?.toString() ?: _uiState.value.title,
                    artist = metadata.artist?.toString() ?: _uiState.value.artist,
                    duration = if (exoPlayer.duration != C.TIME_UNSET && exoPlayer.duration > 0) exoPlayer.duration else _uiState.value.duration,
                    currentPosition = 0L,
                    currentTrackId = trackId
                )
            }
        }
    }

    fun playStream(trackId: String, streamUrl: String, title: String, artist: String) {
        ensureServiceStarted()
        val metadata = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .build()

        val item = MediaItem.Builder()
            .setMediaId(trackId)
            .setUri(streamUrl)
            .setMediaMetadata(metadata)
            .build()

        _uiState.value = _uiState.value.copy(
            title = title,
            artist = artist,
            currentTrackId = trackId,
            isPlaying = true,
            buffering = true,
            currentPosition = 0L
        )

        exoPlayer.setMediaItem(item)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
        exoPlayer.play()

    }

    fun play() {
        // Nothing has ever been loaded (no track selected yet, empty queue) — there's
        // genuinely nothing to play, so don't fake it with a placeholder media item.
        if (exoPlayer.mediaItemCount == 0) return
        ensureServiceStarted()
        exoPlayer.playWhenReady = true
        exoPlayer.play()
    }

    fun pause() {
        exoPlayer.pause()
    }

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) {
            pause()
        } else {
            play()
        }
    }

    fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
        _uiState.value = _uiState.value.copy(currentPosition = positionMs)
    }

    fun nextTrack() {
        if (currentQueue.isNotEmpty() && currentQueueIndex + 1 < currentQueue.size) {
            currentQueueIndex++
            val nextTrack = currentQueue[currentQueueIndex]
            playTrackFromQueue(nextTrack)
        } else if (exoPlayer.hasNextMediaItem()) {
            exoPlayer.seekToNextMediaItem()
        }
        // else: end of queue reached, nothing more to play — leave current track as-is.
    }

    fun previousTrack() {
        if (currentQueue.isNotEmpty() && currentQueueIndex - 1 >= 0) {
            currentQueueIndex--
            val prevTrack = currentQueue[currentQueueIndex]
            playTrackFromQueue(prevTrack)
        } else if (exoPlayer.hasPreviousMediaItem()) {
            exoPlayer.seekToPreviousMediaItem()
        } else {
            seekTo(0L)
        }
    }

    fun toggleShuffle() {
        val newShuffle = !_uiState.value.shuffleEnabled
        exoPlayer.shuffleModeEnabled = newShuffle
        _uiState.value = _uiState.value.copy(shuffleEnabled = newShuffle)
    }

    fun toggleRepeat() {
        val nextMode = (_uiState.value.repeatMode + 1) % 3
        val exoRepeatMode = when (nextMode) {
            1 -> Player.REPEAT_MODE_ALL
            2 -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        exoPlayer.repeatMode = exoRepeatMode
        _uiState.value = _uiState.value.copy(repeatMode = nextMode)
    }

    fun setPlaybackSpeed(speed: Float) {
        exoPlayer.playbackParameters = PlaybackParameters(speed)
        _uiState.value = _uiState.value.copy(playbackSpeed = speed)
    }

    fun setSleepTimer(minutes: Int) {
        cancelSleepTimer()
        if (minutes <= 0) return

        val timerMs = minutes * 60 * 1000L
        _uiState.value = _uiState.value.copy(sleepTimerRemainingMs = timerMs)

        sleepTimerJob = scope.launch(Dispatchers.IO) {
            var remaining = timerMs
            while (isActive && remaining > 0) {
                delay(1000)
                remaining -= 1000
                _uiState.value = _uiState.value.copy(sleepTimerRemainingMs = remaining.coerceAtLeast(0L))
            }
            if (remaining <= 0) {
                scope.launch(Dispatchers.Main) {
                    pause()
                    _uiState.value = _uiState.value.copy(sleepTimerRemainingMs = 0L)
                }
            }
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        _uiState.value = _uiState.value.copy(sleepTimerRemainingMs = 0L)
    }

    fun playQueue(tracks: List<Track>, startIndex: Int = 0) {
        currentPlaybackJob?.cancel()
        currentQueue.clear()
        currentQueue.addAll(tracks)
        persistTracks(tracks)
        currentQueueIndex = startIndex.coerceIn(0, (tracks.size - 1).coerceAtLeast(0))

        _uiState.value = _uiState.value.copy(
            queue = currentQueue.toList(),
            queueIndex = currentQueueIndex
        )

        persistQueue()

        if (currentQueue.isNotEmpty()) {
            val track = currentQueue[currentQueueIndex]
            playTrackFromQueue(track)
        }
    }

    fun addToQueue(track: Track) {
        currentQueue.add(track)
        persistTracks(listOf(track))
        _uiState.value = _uiState.value.copy(queue = currentQueue.toList())
        persistQueue()
    }

    fun playNext(track: Track) {
        val insertIndex = (currentQueueIndex + 1).coerceAtMost(currentQueue.size)
        persistTracks(listOf(track))
        currentQueue.add(insertIndex, track)
        _uiState.value = _uiState.value.copy(queue = currentQueue.toList())
        persistQueue()
    }

    fun removeFromQueue(index: Int) {
        if (index in currentQueue.indices) {
            currentQueue.removeAt(index)
            if (index < currentQueueIndex) {
                currentQueueIndex--
            } else if (currentQueueIndex >= currentQueue.size) {
                currentQueueIndex = (currentQueue.size - 1).coerceAtLeast(0)
            }
            _uiState.value = _uiState.value.copy(
                queue = currentQueue.toList(),
                queueIndex = currentQueueIndex
            )
            persistQueue()
        }
    }

    fun moveQueueItem(fromIndex: Int, toIndex: Int) {
        if (fromIndex in currentQueue.indices && toIndex in currentQueue.indices) {
            val item = currentQueue.removeAt(fromIndex)
            currentQueue.add(toIndex, item)
            _uiState.value = _uiState.value.copy(queue = currentQueue.toList())
            persistQueue()
        }
    }

    fun clearQueue() {
        currentQueue.clear()
        currentQueueIndex = 0
        _uiState.value = _uiState.value.copy(
            queue = emptyList(),
            queueIndex = 0
        )
        persistQueue()
    }

    fun stop() {
        exoPlayer.stop()
        stopPositionUpdates()
        _uiState.value = _uiState.value.copy(isPlaying = false, currentPosition = 0L)
    }

    private fun playTrackFromQueue(track: Track) {
        currentPlaybackJob?.cancel()
        val requestId = ++playbackRequestId

        // Resume only when the selected track is the same track that was previously
        // active; never carry one track's position into another track.
        val previousTrackId = _uiState.value.currentTrackId
        val totalDuration = _uiState.value.duration
        val savedPos = if (previousTrackId == track.id) _uiState.value.currentPosition else 0L
        val startPos = if (savedPos > 30000L && (totalDuration - savedPos) > 15000L) savedPos else 0L

        // Update UI immediately so the Player screen reflects the new track while we
        // resolve the actual audio stream in the background.
        _uiState.value = _uiState.value.copy(
            title = track.title,
            artist = track.artist,
            duration = DurationParser.toMillis(track.duration),
            currentTrackId = track.id,
            isPlaying = true,
            buffering = true,
            currentPosition = startPos
        )

        currentPlaybackJob = scope.launch {
            val streamUrl = when (val result = backendRepository.getAudioStream(track.id)) {
                is BackendResult.Success -> result.data.audioUrl
                is BackendResult.Error -> null
            }

            if (requestId != playbackRequestId || _uiState.value.currentTrackId != track.id) return@launch
            if (streamUrl == null) {
                // Couldn't resolve a playable stream — stop buffering and bail out
                // rather than silently pretending playback started.
                _uiState.value = _uiState.value.copy(buffering = false, isPlaying = false)
                return@launch
            }

            // Start the media service only once there is an actual stream to play;
            // starting it while a network lookup is still buffering can violate the
            // Android foreground-service startup deadline.
            ensureServiceStarted()

            val metadata = MediaMetadata.Builder()
                .setTitle(track.title)
                .setArtist(track.artist)
                .build()

            val item = MediaItem.Builder()
                .setMediaId(track.id)
                .setUri(streamUrl)
                .setMediaMetadata(metadata)
                .build()

            exoPlayer.setMediaItem(item, startPos)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
            exoPlayer.play()

        }
    }

    private fun recordHistory(trackId: String) {
        if (trackId.isEmpty()) return
        // ExoPlayer is main-thread confined. Capture its position before switching
        // to IO for the Room write.
        val position = exoPlayer.currentPosition.coerceAtLeast(0L)
        scope.launch(Dispatchers.IO) {
            try {
                historyDao.insertHistory(
                    HistoryEntity(
                        // Stable primary key makes history an MRU-style list rather
                        // than one unbounded row per transition event.
                        id = trackId,
                        trackId = trackId,
                        playedAt = System.currentTimeMillis(),
                        lastPositionMs = position
                    )
                )
                historyDao.trimHistory()
            } catch (_: Exception) {}
        }
    }

    private fun persistTracks(tracks: List<Track>) {
        val entities = tracks.distinctBy { it.id }.map { track ->
            TrackEntity(
                id = track.id,
                title = track.title,
                artist = track.artist,
                album = "",
                duration = track.duration,
                audioQuality = "Unknown"
            )
        }
        scope.launch(Dispatchers.IO) {
            runCatching { trackDao.insertTracks(entities) }
        }
    }

    private fun persistQueue() {
        val snapshot = currentQueue.toList()
        scope.launch(Dispatchers.IO) {
            try {
                queueDao.clearQueue()
                val entities = snapshot.mapIndexed { idx, track ->
                    QueueEntity(id = "${track.id}_$idx", trackId = track.id, position = idx)
                }
                queueDao.insertQueueItems(entities)
            } catch (_: Exception) {}
        }
    }

    private fun restorePersistedQueue() {
        scope.launch {
            val persisted = withContext(Dispatchers.IO) {
                val tracks = trackDao.getAllTracks().first().associateBy { it.id }
                queueDao.getQueue().first().mapNotNull { item ->
                    tracks[item.trackId]?.let { track ->
                        Track(track.id, track.title, track.artist, track.duration)
                    }
                }
            }
            if (currentQueue.isEmpty() && persisted.isNotEmpty()) {
                currentQueue.addAll(persisted)
                _uiState.value = _uiState.value.copy(queue = persisted)
            }
        }
    }

    private fun startPositionUpdates() {
        stopPositionUpdates()
        positionUpdateJob = scope.launch {
            while (isActive) {
                if (exoPlayer.isPlaying) {
                    val pos = exoPlayer.currentPosition
                    if (pos >= 0) {
                        _uiState.value = _uiState.value.copy(currentPosition = pos)
                    }
                }
                delay(1000)
            }
        }
    }

    private fun stopPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = null
    }

    fun release() {
        currentPlaybackJob?.cancel()
        stopPositionUpdates()
        cancelSleepTimer()
        exoPlayer.removeListener(playerListener)
        exoPlayer.release()
    }
}
