package com.example.data.repository

import com.example.data.database.dao.FavoriteDao
import com.example.data.database.dao.TrackDao
import com.example.data.database.entity.FavoriteEntity
import com.example.data.database.entity.TrackEntity
import com.example.domain.model.AlbumDetails
import com.example.domain.model.ArtistDetails
import com.example.domain.model.BackendResult
import com.example.domain.model.PlaylistDetails
import com.example.domain.model.SearchResultItem
import com.example.domain.model.Track
import com.example.domain.repository.AlbumRepository
import com.example.domain.repository.ArtistRepository
import com.example.domain.repository.BackendRepository
import com.example.domain.repository.LibraryRepository
import com.example.domain.repository.PlaylistRepository
import com.example.domain.repository.RecommendationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecommendationRepositoryImpl @Inject constructor(
    private val backendRepository: BackendRepository
) : RecommendationRepository {
    override suspend fun getRecommendations(trackId: String?): BackendResult<List<SearchResultItem>> {
        return backendRepository.getRecommendations(trackId)
    }

    override suspend fun getTrending(): BackendResult<List<SearchResultItem>> {
        return backendRepository.getTrending()
    }
}

@Singleton
class ArtistRepositoryImpl @Inject constructor(
    private val backendRepository: BackendRepository
) : ArtistRepository {
    override suspend fun getArtistDetails(artistId: String): BackendResult<ArtistDetails> {
        return backendRepository.getArtist(artistId)
    }
}

@Singleton
class AlbumRepositoryImpl @Inject constructor(
    private val backendRepository: BackendRepository
) : AlbumRepository {
    override suspend fun getAlbumDetails(albumId: String): BackendResult<AlbumDetails> {
        return backendRepository.getAlbum(albumId)
    }
}

@Singleton
class PlaylistRepositoryImpl @Inject constructor(
    private val backendRepository: BackendRepository
) : PlaylistRepository {
    override suspend fun getPlaylistDetails(playlistId: String): BackendResult<PlaylistDetails> {
        return backendRepository.getPlaylist(playlistId)
    }
}

@Singleton
class LibraryRepositoryImpl @Inject constructor(
    private val favoriteDao: FavoriteDao,
    private val trackDao: TrackDao
) : LibraryRepository {

    override fun getFavoriteTracks(): Flow<List<Track>> = combine(
        favoriteDao.getFavoritesByType("song"),
        trackDao.getAllTracks()
    ) { favorites, tracks ->
        val trackMap = tracks.associateBy { it.id }
        favorites.map { fav ->
            val t = trackMap[fav.trackId]
            Track(
                id = fav.trackId,
                title = t?.title ?: fav.title.ifEmpty { "Track ${fav.trackId}" },
                artist = t?.artist ?: fav.subtitle.ifEmpty { "Artist" },
                duration = t?.duration ?: fav.duration
            )
        }
    }

    override fun getFavoriteAlbums(): Flow<List<AlbumDetails>> {
        return favoriteDao.getFavoritesByType("album").map { favorites ->
            favorites.map { fav ->
                AlbumDetails(
                    id = fav.trackId,
                    title = fav.title,
                    artist = fav.subtitle,
                    thumbnailUrl = fav.thumbnailUrl
                )
            }
        }
    }

    override fun getFavoriteArtists(): Flow<List<ArtistDetails>> {
        return favoriteDao.getFavoritesByType("artist").map { favorites ->
            favorites.map { fav ->
                ArtistDetails(
                    id = fav.trackId,
                    name = fav.title,
                    thumbnailUrl = fav.thumbnailUrl
                )
            }
        }
    }

    override fun getFavoritePlaylists(): Flow<List<PlaylistDetails>> {
        return favoriteDao.getFavoritesByType("playlist").map { favorites ->
            favorites.map { fav ->
                PlaylistDetails(
                    id = fav.trackId,
                    title = fav.title,
                    author = fav.subtitle,
                    thumbnailUrl = fav.thumbnailUrl
                )
            }
        }
    }

    override suspend fun toggleFavoriteTrack(track: Track) {
        val existing = favoriteDao.getFavorite(track.id, "song")
        if (existing != null) {
            favoriteDao.deleteFavorite(track.id, "song")
        } else {
            favoriteDao.insertFavorite(
                FavoriteEntity(
                    id = "fav_${track.id}",
                    trackId = track.id,
                    type = "song",
                    title = track.title,
                    subtitle = track.artist,
                    duration = track.duration
                )
            )
        }
    }

    override suspend fun toggleFavoriteAlbum(album: AlbumDetails) {
        val existing = favoriteDao.getFavorite(album.id, "album")
        if (existing != null) {
            favoriteDao.deleteFavorite(album.id, "album")
        } else {
            favoriteDao.insertFavorite(
                FavoriteEntity(
                    id = "fav_${album.id}",
                    trackId = album.id,
                    type = "album",
                    title = album.title,
                    subtitle = album.artist,
                    thumbnailUrl = album.thumbnailUrl
                )
            )
        }
    }

    override suspend fun toggleFavoriteArtist(artist: ArtistDetails) {
        val existing = favoriteDao.getFavorite(artist.id, "artist")
        if (existing != null) {
            favoriteDao.deleteFavorite(artist.id, "artist")
        } else {
            favoriteDao.insertFavorite(
                FavoriteEntity(
                    id = "fav_${artist.id}",
                    trackId = artist.id,
                    type = "artist",
                    title = artist.name,
                    thumbnailUrl = artist.thumbnailUrl
                )
            )
        }
    }

    override suspend fun toggleFavoritePlaylist(playlist: PlaylistDetails) {
        val existing = favoriteDao.getFavorite(playlist.id, "playlist")
        if (existing != null) {
            favoriteDao.deleteFavorite(playlist.id, "playlist")
        } else {
            favoriteDao.insertFavorite(
                FavoriteEntity(
                    id = "fav_${playlist.id}",
                    trackId = playlist.id,
                    type = "playlist",
                    title = playlist.title,
                    subtitle = playlist.author ?: "",
                    thumbnailUrl = playlist.thumbnailUrl
                )
            )
        }
    }

    override suspend fun isFavorite(id: String): Boolean {
        return favoriteDao.getFavoriteByTrackId(id) != null
    }
}
