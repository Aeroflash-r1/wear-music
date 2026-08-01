package com.example.data.repository

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.remote.ServerConfig
import com.example.domain.model.DownloadProgressState
import com.example.domain.model.DownloadedTrack
import com.example.domain.repository.DownloadsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class SettingsRepositoryImplTest {

    private class FakeDownloadsRepository : DownloadsRepository {
        var storageUsed = "42 MB"
        var clearCacheCalled = false

        private val downloads = MutableStateFlow<List<DownloadedTrack>>(emptyList())
        private val progress = MutableStateFlow<List<DownloadProgressState>>(emptyList())

        override fun getDownloads(): Flow<List<DownloadedTrack>> = downloads
        override fun observeDownloadProgress(): Flow<List<DownloadProgressState>> = progress
        override fun getTotalDownloads(): Int = 0
        override fun getTotalStorageUsed(): String = storageUsed
        override fun getStorageLimit(): String = "500 MB"

        override suspend fun enqueueDownload(trackId: String, title: String, artist: String) = Unit
        override suspend fun pauseDownload(trackId: String) = Unit
        override suspend fun resumeDownload(trackId: String) = Unit
        override suspend fun cancelDownload(trackId: String) = Unit
        override suspend fun removeDownload(trackId: String) = Unit

        override suspend fun clearCache() {
            clearCacheCalled = true
            storageUsed = "0 MB"
        }

        override suspend fun retryFailedDownloads() = Unit
        override fun setMaxParallelDownloads(count: Int) = Unit
    }

    private fun context(): Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `settingsState reports real cache size from downloads repository`() = runBlocking {
        val downloads = FakeDownloadsRepository().apply { storageUsed = "123 MB" }
        val repository = SettingsRepositoryImpl(context(), downloads, ServerConfig())

        val state = repository.settingsState.first()

        assertEquals("123 MB", state.currentCacheSize)
        assertEquals("500 MB", state.cacheLimit)
    }

    @Test
    fun `clearCache delegates to downloads repository and hides dialog`() = runBlocking {
        val downloads = FakeDownloadsRepository()
        val repository = SettingsRepositoryImpl(context(), downloads, ServerConfig())

        repository.showClearCacheDialog()
        assertTrue(repository.settingsState.first().showClearCacheDialog)

        repository.clearCache()

        // clearCache() launches on the repository's internal IO scope; poll until the
        // dialog is dismissed and the downloads repository was actually cleared.
        withTimeout(5_000) {
            while (true) {
                val state = repository.settingsState.first()
                if (!state.showClearCacheDialog && downloads.clearCacheCalled) break
                delay(25)
            }
        }

        val finalState = repository.settingsState.first()
        assertTrue(downloads.clearCacheCalled)
        assertEquals("0 MB", finalState.currentCacheSize)
        assertFalse(finalState.showClearCacheDialog)
    }

    @Test
    fun `serverUrl persists and updates ServerConfig`() = runBlocking {
        val downloads = FakeDownloadsRepository()
        val serverConfig = ServerConfig()
        val repository = SettingsRepositoryImpl(context(), downloads, serverConfig)

        assertEquals("", repository.serverUrl.first())

        repository.setServerUrl("http://100.100.100.1:8080/")

        assertEquals("http://100.100.100.1:8080", repository.serverUrl.first())
        assertEquals("http://100.100.100.1:8080", serverConfig.baseUrl)
    }
}
