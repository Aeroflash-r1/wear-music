package com.example.data.repository

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import java.io.File
import java.util.Locale
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.remote.ServerConfig
import com.example.domain.model.SettingsUiState
import com.example.domain.repository.DownloadsRepository
import com.example.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore by preferencesDataStore(name = "pulse_settings")

private object PrefKeys {
    val SERVER_URL = stringPreferencesKey("server_url")
    val AUDIO_OFFLOAD = booleanPreferencesKey("audio_offload")
    val GAPLESS_PLAYBACK = booleanPreferencesKey("gapless_playback")
    val NORMALIZE_VOLUME = booleanPreferencesKey("normalize_volume")
    val AUTO_DOWNLOAD = booleanPreferencesKey("auto_download")
    val DOWNLOAD_OVER_WIFI = booleanPreferencesKey("download_over_wifi")
    val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
    val AMOLED_DARK_THEME = booleanPreferencesKey("amoled_dark_theme")
    val DEV_OPTIONS_UNLOCKED = booleanPreferencesKey("dev_options_unlocked")
}

/**
 * Settings are split into two groups:
 *  - Persisted toggles (audio/download/appearance prefs) -> backed by DataStore, survive restarts.
 *  - Transient UI state (dev-unlock click counter, dialog visibility, cache size readout) ->
 *    kept in memory only, since these are per-session UI state, not user preferences.
 */
@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val downloadsRepository: DownloadsRepository,
    private val serverConfig: ServerConfig
) : SettingsRepository {

    private val scope = CoroutineScope(Dispatchers.IO)

    /** Real on-disk size of the installed APK, computed once at startup. */
    private val appSize: String = runCatching {
        val info = context.packageManager.getApplicationInfo(context.packageName, 0)
        val bytes = File(info.sourceDir).length()
        if (bytes > 0) String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0)) else "—"
    }.getOrDefault("—")

    private data class TransientState(
        val devClickCount: Int = 0,
        val showClearCacheDialog: Boolean = false
    )

    private val _transientState = MutableStateFlow(TransientState())

    override val serverUrl: Flow<String> = context.settingsDataStore.data.map { prefs ->
        (prefs[PrefKeys.SERVER_URL] ?: "").trim().trimEnd('/')
    }

    init {
        // Push the persisted server URL into ServerConfig so the Retrofit interceptor
        // can rewrite requests before the user ever opens Settings.
        scope.launch {
            val url = context.settingsDataStore.data.first()[PrefKeys.SERVER_URL] ?: ""
            serverConfig.update(url)
        }
    }

    override suspend fun setServerUrl(url: String) {
        context.settingsDataStore.edit { prefs -> prefs[PrefKeys.SERVER_URL] = url }
        serverConfig.update(url)
    }

    private val persistedFlow: Flow<SettingsUiState> = context.settingsDataStore.data.map { prefs ->
        SettingsUiState(
            serverUrl = prefs[PrefKeys.SERVER_URL] ?: "",
            audioOffload = prefs[PrefKeys.AUDIO_OFFLOAD] ?: true,
            gaplessPlayback = prefs[PrefKeys.GAPLESS_PLAYBACK] ?: true,
            normalizeVolume = prefs[PrefKeys.NORMALIZE_VOLUME] ?: false,
            autoDownload = prefs[PrefKeys.AUTO_DOWNLOAD] ?: true,
            downloadOverWifi = prefs[PrefKeys.DOWNLOAD_OVER_WIFI] ?: true,
            dynamicColor = prefs[PrefKeys.DYNAMIC_COLOR] ?: true,
            amoledDarkTheme = prefs[PrefKeys.AMOLED_DARK_THEME] ?: true,
            developerOptionsUnlocked = prefs[PrefKeys.DEV_OPTIONS_UNLOCKED] ?: false,
            appSize = appSize
        )
    }

    override val settingsState: Flow<SettingsUiState> = combine(
        persistedFlow,
        downloadsRepository.getDownloads(),
        _transientState
    ) { persisted, _, transient ->
        persisted.copy(
            devClickCount = transient.devClickCount,
            showClearCacheDialog = transient.showClearCacheDialog,
            currentCacheSize = downloadsRepository.getTotalStorageUsed()
        )
    }

    private fun toggleBoolean(key: Preferences.Key<Boolean>, default: Boolean) {
        scope.launch {
            context.settingsDataStore.edit { prefs ->
                prefs[key] = !(prefs[key] ?: default)
            }
        }
    }

    override fun toggleAudioOffload() = toggleBoolean(PrefKeys.AUDIO_OFFLOAD, true)
    override fun toggleGaplessPlayback() = toggleBoolean(PrefKeys.GAPLESS_PLAYBACK, true)
    override fun toggleNormalizeVolume() = toggleBoolean(PrefKeys.NORMALIZE_VOLUME, false)
    override fun toggleAutoDownload() = toggleBoolean(PrefKeys.AUTO_DOWNLOAD, true)
    override fun toggleDownloadOverWifi() = toggleBoolean(PrefKeys.DOWNLOAD_OVER_WIFI, true)
    override fun toggleDynamicColor() = toggleBoolean(PrefKeys.DYNAMIC_COLOR, true)
    override fun toggleAmoledDarkTheme() = toggleBoolean(PrefKeys.AMOLED_DARK_THEME, true)

    override fun onVersionClicked() {
        scope.launch {
            val alreadyUnlocked = context.settingsDataStore.data.first()[PrefKeys.DEV_OPTIONS_UNLOCKED] ?: false
            if (alreadyUnlocked) return@launch

            val nextCount = _transientState.value.devClickCount + 1
            if (nextCount >= 7) {
                context.settingsDataStore.edit { prefs -> prefs[PrefKeys.DEV_OPTIONS_UNLOCKED] = true }
            }
            _transientState.value = _transientState.value.copy(devClickCount = nextCount)
        }
    }

    override fun showClearCacheDialog() {
        _transientState.value = _transientState.value.copy(showClearCacheDialog = true)
    }

    override fun hideClearCacheDialog() {
        _transientState.value = _transientState.value.copy(showClearCacheDialog = false)
    }

    override fun clearCache() {
        scope.launch {
            downloadsRepository.clearCache()
            _transientState.value = _transientState.value.copy(showClearCacheDialog = false)
        }
    }
}
