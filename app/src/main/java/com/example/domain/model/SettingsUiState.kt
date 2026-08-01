package com.example.domain.model

import com.example.BuildConfig

data class SettingsUiState(
    val serverUrl: String = "",
    val audioQuality: String = "High",
    val audioOffload: Boolean = true,
    val gaplessPlayback: Boolean = true,
    val normalizeVolume: Boolean = false,

    val downloadQuality: String = "High",
    val autoDownload: Boolean = true,
    val downloadOverWifi: Boolean = true,

    val currentCacheSize: String = "0 MB",
    val cacheLimit: String = "500 MB",

    val dynamicColor: Boolean = true,
    val amoledDarkTheme: Boolean = true,
    val animationSpeed: String = "Normal",

    val pulseVersion: String = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
    val buildType: String = BuildConfig.BUILD_TYPE,
    val appSize: String = "—",

    val developerOptionsUnlocked: Boolean = false,
    val devClickCount: Int = 0,

    val showClearCacheDialog: Boolean = false
)
