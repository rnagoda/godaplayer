package com.godaplayer.app.data.repository

import com.godaplayer.app.data.local.database.dao.PlaylistDao
import com.godaplayer.app.data.local.database.dao.PlaylistSongDao
import com.godaplayer.app.data.local.database.entity.PlaylistEntity
import com.godaplayer.app.data.mapper.toDomain
import com.godaplayer.app.data.mapper.toEntity
import com.godaplayer.app.domain.model.Playlist
import com.godaplayer.app.domain.model.Song
import com.godaplayer.app.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistRepositoryImpl @Inject constructor(
    private val playlistDao: PlaylistDao,
    private val playlistSongDao: PlaylistSongDao
) : PlaylistRepository {

    override fun getAllPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getAllPlaylistsWithSongCount().map { it.toDomain() }
    }

    override fun getPlaylistById(id: Long): Flow<Playlist?> {
        return playlistDao.getPlaylistByIdFlow(id).map { entity ->
            entity?.toDomain()
        }
    }

    override fun getSongsForPlaylist(playlistId: Long): Flow<List<Song>> {
        return playlistSongDao.getSongsForPlaylist(playlistId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getPlaylistsContainingSong(songId: Long): Flow<List<Long>> {
        return playlistSongDao.getPlaylistsContainingSong(songId)
    }

    override fun searchPlaylists(query: String): Flow<List<Playlist>> {
        return playlistDao.searchPlaylists(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getPlaylistByIdOnce(id: Long): Playlist? {
        return playlistDao.getPlaylistById(id)?.toDomain()
    }

    override suspend fun getSongsForPlaylistOnce(playlistId: Long): List<Song> {
        return playlistSongDao.getSongsForPlaylistOnce(playlistId).map { it.toDomain() }
    }

    override suspend fun createPlaylist(name: String, description: String?): Long {
        return playlistDao.insertPlaylist(
            PlaylistEntity(
                name = name,
                description = description
            )
        )
    }

    override suspend fun updatePlaylist(playlist: Playlist) {
        playlistDao.updatePlaylist(playlist.toEntity())
    }

    override suspend fun renamePlaylist(playlistId: Long, name: String) {
        playlistDao.renamePlaylist(playlistId, name)
    }

    override suspend fun deletePlaylist(playlistId: Long) {
        playlistDao.deletePlaylistById(playlistId)
    }

    override suspend fun addSongToPlaylist(playlistId: Long, songId: Long): Boolean {
        return playlistSongDao.addSongToPlaylist(playlistId, songId)
    }

    override suspend fun addSongsToPlaylist(playlistId: Long, songIds: List<Long>): Int {
        return playlistSongDao.addSongsToPlaylist(playlistId, songIds)
    }

    override suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        playlistSongDao.removeSongFromPlaylist(playlistId, songId)
    }

    override suspend fun clearPlaylist(playlistId: Long) {
        playlistSongDao.clearPlaylist(playlistId)
    }

    override suspend fun reorderPlaylist(playlistId: Long, fromPosition: Int, toPosition: Int) {
        playlistSongDao.reorderPlaylist(playlistId, fromPosition, toPosition)
    }

    override suspend fun duplicatePlaylist(playlistId: Long, newName: String): Long {
        val originalPlaylist = playlistDao.getPlaylistById(playlistId) ?: return -1
        val songs = playlistSongDao.getSongsForPlaylistOnce(playlistId)

        val newPlaylistId = playlistDao.insertPlaylist(
            PlaylistEntity(
                name = newName,
                description = originalPlaylist.description
            )
        )

        if (songs.isNotEmpty()) {
            playlistSongDao.addSongsToPlaylist(newPlaylistId, songs.map { it.id })
        }

        return newPlaylistId
    }

    override suspend fun updateLastPlayed(playlistId: Long) {
        playlistDao.updateLastPlayed(playlistId)
    }
}
