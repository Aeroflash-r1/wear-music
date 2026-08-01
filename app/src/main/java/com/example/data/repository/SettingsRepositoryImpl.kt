package com.example.data.repository

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.repository.SettingsRepository
import com.example.ui.screens.settings.SettingsUiState
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
    @ApplicationContext private val context: Context
) : SettingsRepository {

    private val scope = CoroutineScope(Dispatchers.IO)

    private data class TransientState(
        val devClickCount: Int = 0,
        val showClearCacheDialog: Boolean = false,
        val currentCacheSize: String = "0 MB"
    )

    private val _transientState = MutableStateFlow(TransientState())

    private val persistedFlow: Flow<SettingsUiState> = context.settingsDataStore.data.map { prefs ->
        SettingsUiState(
            audioOffload = prefs[PrefKeys.AUDIO_OFFLOAD] ?: true,
            gaplessPlayback = prefs[PrefKeys.GAPLESS_PLAYBACK] ?: true,
            normalizeVolume = prefs[PrefKeys.NORMALIZE_VOLUME] ?: false,
            autoDownload = prefs[PrefKeys.AUTO_DOWNLOAD] ?: true,
            downloadOverWifi = prefs[PrefKeys.DOWNLOAD_OVER_WIFI] ?: true,
            dynamicColor = prefs[PrefKeys.DYNAMIC_COLOR] ?: true,
            amoledDarkTheme = prefs[PrefKeys.AMOLED_DARK_THEME] ?: true,
            developerOptionsUnlocked = prefs[PrefKeys.DEV_OPTIONS_UNLOCKED] ?: false
        )
    }

    override val settingsState: Flow<SettingsUiState> = combine(
        persistedFlow,
        _transientState
    ) { persisted, transient ->
        persisted.copy(
            devClickCount = transient.devClickCount,
            showClearCacheDialog = transient.showClearCacheDialog,
            currentCacheSize = transient.currentCacheSize
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
        _transientState.value = _transientState.value.copy(
            currentCacheSize = "0 MB",
            showClearCacheDialog = false
        )
    }
}
