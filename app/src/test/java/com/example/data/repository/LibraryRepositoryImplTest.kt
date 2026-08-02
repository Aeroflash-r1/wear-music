package com.example.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.PulseDatabase
import com.example.data.database.entity.FavoriteEntity
import com.example.data.database.entity.TrackEntity
import com.example.domain.model.Track
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class LibraryRepositoryImplTest {

    private lateinit var db: PulseDatabase
    private lateinit var repository: LibraryRepositoryImpl

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, PulseDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = LibraryRepositoryImpl(
            favoriteDao = db.favoriteDao(),
            trackDao = db.trackDao()
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `getFavoriteTracks prefers real track data from track table`() = runTest {
        db.trackDao().insertTrack(
            TrackEntity(
                id = "t1", title = "Real Title", artist = "Real Artist",
                album = "Album", duration = "3:45", audioQuality = "High"
            )
        )
        db.favoriteDao().insertFavorite(
            FavoriteEntity(
                id = "fav1", trackId = "t1", type = "song",
                title = "Stale Title", subtitle = "Stale Artist", duration = "9:99"
            )
        )

        val favorites = repository.getFavoriteTracks().first()

        assertEquals("Real Title", favorites[0].title)
        assertEquals("Real Artist", favorites[0].artist)
        assertEquals("3:45", favorites[0].duration)
    }

    @Test
    fun `getFavoriteTracks falls back to stored favorite fields`() = runTest {
        db.favoriteDao().insertFavorite(
            FavoriteEntity(
                id = "fav1", trackId = "t1", type = "song",
                title = "Saved Title", subtitle = "Saved Artist", duration = "4:20"
            )
        )

        val favorites = repository.getFavoriteTracks().first()

        assertEquals("Saved Title", favorites[0].title)
        assertEquals("4:20", favorites[0].duration)
    }

    @Test
    fun `toggleFavoriteTrack stores duration and metadata`() = runTest {
        repository.toggleFavoriteTrack(Track("t1", "Song", "Artist", "3:45"))

        val favorite = db.favoriteDao().getFavoriteByTrackId("t1")
        assertNotNull(favorite)
        assertEquals("3:45", favorite?.duration)
        assertEquals("Song", favorite?.title)
    }

    @Test
    fun `toggleFavoriteTrack does not remove a different favorite type with the same id`() = runTest {
        db.favoriteDao().insertFavorite(
            FavoriteEntity(id = "album_same", trackId = "same", type = "album", title = "Album")
        )

        repository.toggleFavoriteTrack(Track("same", "Song", "Artist", "3:45"))
        repository.toggleFavoriteTrack(Track("same", "Song", "Artist", "3:45"))

        assertNotNull(db.favoriteDao().getFavorite("same", "album"))
        assertFalse(db.favoriteDao().getFavorite("same", "song") != null)
    }

    @Test
    fun `toggleFavoriteTrack removes when already favorited`() = runTest {
        repository.toggleFavoriteTrack(Track("t1", "Song", "Artist", "3:45"))
        assertTrue(repository.isFavorite("t1"))

        repository.toggleFavoriteTrack(Track("t1", "Song", "Artist", "3:45"))

        assertFalse(repository.isFavorite("t1"))
    }
}
