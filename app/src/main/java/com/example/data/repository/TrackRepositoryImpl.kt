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
        val        trackMap = trackEntities.associateBy { it.id }
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
        favEntities.filter { it.type == "song" }.map { f ->
            val t = trackMap[f.trackId]
            Track(
                id = f.trackId,
                title = t?.title ?: f.title.ifEmpty { "Track ${f.trackId}" },
                artist = t?.artist ?: f.subtitle.ifEmpty { "Artist" },
                duration = t?.duration ?: f.duration
            )
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

    override suspend fun search(query: String, filter: String?): BackendResult<List<SearchResult>> {
        if (query.isNotBlank()) {
            recentSearchDao.insertSearch(RecentSearchEntity(query = query))
        }

        return when (val res = backendRepository.search(query, filter)) {
            is BackendResult.Success -> BackendResult.Success(res.data.map { item ->
                SearchResult(
                    id = item.id,
                    title = item.title,
                    artist = item.artist,
                    duration = item.duration,
                    type = item.type
                )
            })
            is BackendResult.Error -> {
                // Surface the real backend error so the UI can tell the user the
                // server/yt-dlp failed instead of silently showing "No results".
                res
            }
        }
    }
}

