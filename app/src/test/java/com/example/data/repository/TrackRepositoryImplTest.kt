package com.example.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.PulseDatabase
import com.example.data.database.entity.FavoriteEntity
import com.example.data.database.entity.TrackEntity
import com.example.domain.model.BackendError
import com.example.domain.model.BackendResult
import com.example.domain.model.AlbumDetails
import com.example.domain.model.ArtistDetails
import com.example.domain.model.AudioStreamInfo
import com.example.domain.model.PlaylistDetails
import com.example.domain.model.SearchResultItem
import com.example.domain.model.TrackDetails
import com.example.domain.repository.BackendRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class TrackRepositoryImplTest {

    private lateinit var db: PulseDatabase
    private lateinit var repository: TrackRepositoryImpl

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, PulseDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = TrackRepositoryImpl(
            trackDao = db.trackDao(),
            favoriteDao = db.favoriteDao(),
            historyDao = db.historyDao(),
            queueDao = db.queueDao(),
            recentSearchDao = db.recentSearchDao(),
            backendRepository = FakeBackendRepository()
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `getFavorites resolves real track data when track exists`() = runTest {
        db.trackDao().insertTrack(
            TrackEntity(
                id = "t1", title = "Song One", artist = "Artist One",
                album = "Album", duration = "3:45", audioQuality = "High"
            )
        )
        db.favoriteDao().insertFavorite(
            FavoriteEntity(
                id = "fav1", trackId = "t1", type = "song",
                title = "Song One", subtitle = "Artist One", duration = "3:45"
            )
        )

        val favorites = repository.getFavorites().first()

        assertEquals(1, favorites.size)
        assertEquals("t1", favorites[0].id)
        assertEquals("Song One", favorites[0].title)
        assertEquals("3:45", favorites[0].duration)
    }

    @Test
    fun `getFavorites falls back to stored favorite data when track missing`() = runTest {
        db.favoriteDao().insertFavorite(
            FavoriteEntity(
                id = "fav1", trackId = "t1", type = "song",
                title = "Saved Title", subtitle = "Saved Artist", duration = "4:20"
            )
        )

        val favorites = repository.getFavorites().first()

        assertEquals("Saved Title", favorites[0].title)
        assertEquals("Saved Artist", favorites[0].artist)
        assertEquals("4:20", favorites[0].duration)
    }

    @Test
    fun `getFavorites excludes non-song favorites`() = runTest {
        db.favoriteDao().insertFavorite(
            FavoriteEntity(id = "fav1", trackId = "a1", type = "album", title = "An Album")
        )

        assertTrue(repository.getFavorites().first().isEmpty())
    }

    @Test
    fun `search surfaces backend error instead of silently hiding it`() = runTest {
        db.trackDao().insertTrack(
            TrackEntity(
                id = "t1", title = "Hello World", artist = "Artist",
                album = "Album", duration = "3:45", audioQuality = "High"
            )
        )

        val result = repository.search("hello")

        // Backend failure must reach the UI (so the user knows the server/yt-dlp
        // failed) rather than being masked as a fake "No results".
        assertTrue(result is BackendResult.Error)
    }

    @Test
    fun `search records the query in recent searches`() = runTest {
        val result = repository.search("pulse")

        assertTrue(result is BackendResult.Error)
        val recent = db.recentSearchDao().getRecentSearches().first()
        assertEquals(listOf("pulse"), recent)
    }

    @Test
    fun `search passes the filter through to the backend`() = runTest {
        val backend = FakeBackendRepository().apply {
            searchResult = BackendResult.Success(
                listOf(SearchResultItem(id = "s1", title = "Song", artist = "Artist", duration = "3:00", type = "song"))
            )
        }
        repository = TrackRepositoryImpl(
            trackDao = db.trackDao(),
            favoriteDao = db.favoriteDao(),
            historyDao = db.historyDao(),
            queueDao = db.queueDao(),
            recentSearchDao = db.recentSearchDao(),
            backendRepository = backend
        )

        val result = repository.search("drake", "music_albums")

        assertTrue(result is BackendResult.Success)
        assertEquals("music_albums", backend.lastFilter)
        val items = (result as BackendResult.Success).data
        assertEquals(listOf("s1"), items.map { it.id })
        assertEquals("song", items[0].type)
    }

    private class FakeBackendRepository : BackendRepository {
        var searchResult: BackendResult<List<SearchResultItem>> =
            BackendResult.Error(BackendError.Network("offline"))
        var lastFilter: String? = null

        override suspend fun search(query: String, filter: String?, page: Int): BackendResult<List<SearchResultItem>> {
            lastFilter = filter
            return searchResult
        }

        override suspend fun getTrack(trackId: String): BackendResult<TrackDetails> =
            BackendResult.Error(BackendError.Network("offline"))

        override suspend fun getAudioStream(trackId: String): BackendResult<AudioStreamInfo> =
            BackendResult.Error(BackendError.Network("offline"))

        override suspend fun getRecommendations(trackId: String?): BackendResult<List<SearchResultItem>> =
            BackendResult.Error(BackendError.Network("offline"))

        override suspend fun getPlaylist(playlistId: String): BackendResult<PlaylistDetails> =
            BackendResult.Error(BackendError.Network("offline"))

        override suspend fun getAlbum(albumId: String): BackendResult<AlbumDetails> =
            BackendResult.Error(BackendError.Network("offline"))

        override suspend fun getArtist(artistId: String): BackendResult<ArtistDetails> =
            BackendResult.Error(BackendError.Network("offline"))

        override suspend fun getTrending(): BackendResult<List<SearchResultItem>> =
            BackendResult.Error(BackendError.Network("offline"))
    }
}
