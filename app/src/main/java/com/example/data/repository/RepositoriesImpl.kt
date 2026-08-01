package com.example.data.repository

import com.example.data.database.dao.FavoriteDao
import com.example.data.database.dao.HistoryDao
import com.example.data.database.dao.QueueDao
import com.example.data.database.dao.RecentSearchDao
import com.example.data.database.dao.TrackDao
import com.example.data.database.entity.RecentSearchEntity
import com.example.domain.model.BackendResult
import com.example.domain.model.SearchResult
import com.example.domain.model.Track
import com.example.domain.repository.BackendRepository
import com.example.domain.repository.TrackRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrackRepositoryImpl @Inject constructor(
    private val trackDao: TrackDao,
    private val favoriteDao: FavoriteDao,
    private val historyDao: HistoryDao,
    private val queueDao: QueueDao,
    private val recentSearchDao: RecentSearchDao,
    private val backendRepository: BackendRepository
) : TrackRepository {

    override fun getQueue(): Flow<List<Track>> = combine(
        queueDao.getQueue(),
        trackDao.getAllTracks()
    ) { queueEntities, trackEntities ->
        val trackMap = trackEntities.associateBy { it.id }
        queueEntities.mapNotNull { q ->
            trackMap[q.trackId]?.let { t ->
                Track(t.id, t.title, t.artist, t.duration)
            }
        }
    }

    override fun getFavorites(): Flow<List<Track>> = combine(
        favoriteDao.getAllFavorites(),
        trackDao.getAllTracks()
    ) { favEntities, trackEntities ->
        val trackMap = trackEntities.associateBy { it.id }
        favEntities.mapNotNull { f ->
            trackMap[f.trackId]?.let { t ->
                Track(t.id, t.title, t.artist, t.duration)
            }
        }
    }

    override fun getRecentlyPlayed(): Flow<List<Track>> = combine(
        historyDao.getHistory(),
        trackDao.getAllTracks()
    ) { histEntities, trackEntities ->
        val trackMap = trackEntities.associateBy { it.id }
        histEntities.mapNotNull { h ->
            trackMap[h.trackId]?.let { t ->
                Track(t.id, t.title, t.artist, t.duration)
            }
        }
    }

    override fun getRecentSearches(): Flow<List<String>> = recentSearchDao.getRecentSearches()

    override suspend fun search(query: String): List<SearchResult> {
        if (query.isNotBlank()) {
            recentSearchDao.insertSearch(RecentSearchEntity(query = query))
        }

        return when (val res = backendRepository.search(query)) {
            is BackendResult.Success -> res.data.map { item ->
                SearchResult(
                    id = item.id,
                    title = item.title,
                    artist = item.artist,
                    duration = item.duration,
                    type = item.type
                )
            }
            is BackendResult.Error -> {
                // Backend unreachable — fall back to whatever's genuinely in the
                // local library rather than presenting fabricated results.
                val allTracks = trackDao.getAllTracks().firstOrNull() ?: emptyList()
                val lower = query.lowercase()
                allTracks
                    .map { SearchResult(it.id, it.title, it.artist, it.duration) }
                    .filter { it.title.lowercase().contains(lower) || it.artist.lowercase().contains(lower) }
            }
        }
    }
}

// Note: PlayerRepository is implemented by Media3PlaybackRepositoryImpl (see
// Media3PlaybackRepositoryImpl.kt), which is what's actually bound in RepositoryModule.
// SettingsRepositoryImpl now lives in its own file, backed by DataStore — see
// SettingsRepositoryImpl.kt.
