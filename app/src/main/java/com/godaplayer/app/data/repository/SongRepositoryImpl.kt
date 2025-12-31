package com.godaplayer.app.data.repository

import com.godaplayer.app.data.local.database.dao.SongDao
import com.godaplayer.app.data.mapper.toDomain
import com.godaplayer.app.data.mapper.toEntity
import com.godaplayer.app.domain.model.Song
import com.godaplayer.app.domain.repository.SongRepository
import com.godaplayer.app.domain.repository.SongSortOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SongRepositoryImpl @Inject constructor(
    private val songDao: SongDao
) : SongRepository {

    override fun getAllSongs(sortOrder: SongSortOrder): Flow<List<Song>> {
        return when (sortOrder) {
            SongSortOrder.TITLE -> songDao.getAllSongs()
            SongSortOrder.ARTIST -> songDao.getAllSongsByArtist()
            SongSortOrder.DATE_ADDED -> songDao.getAllSongsByDateAdded()
            SongSortOrder.DURATION -> songDao.getAllSongsByDuration()
            SongSortOrder.FILE_NAME -> songDao.getAllSongsByFileName()
        }.map { it.toDomain() }
    }

    override fun getMostPlayedSongs(): Flow<List<Song>> {
        return songDao.getMostPlayedSongs().map { it.toDomain() }
    }

    override fun getRecentlyPlayed(limit: Int): Flow<List<Song>> {
        return songDao.getRecentlyPlayed(limit).map { it.toDomain() }
    }

    override fun searchSongs(query: String): Flow<List<Song>> {
        return songDao.searchSongs(query).map { it.toDomain() }
    }

    override fun getSongCount(): Flow<Int> {
        return songDao.getSongCount()
    }

    override fun getTotalDuration(): Flow<Long> {
        return songDao.getTotalDuration().map { it ?: 0L }
    }

    override suspend fun getSongById(id: Long): Song? {
        return songDao.getSongById(id)?.toDomain()
    }

    override suspend fun getSongByFilePath(filePath: String): Song? {
        return songDao.getSongByFilePath(filePath)?.toDomain()
    }

    override suspend fun insertSong(song: Song): Long {
        return songDao.insertSong(song.toEntity())
    }

    override suspend fun insertSongs(songs: List<Song>): List<Long> {
        return songDao.insertSongs(songs.map { it.toEntity() })
    }

    override suspend fun updateSong(song: Song) {
        songDao.updateSong(song.toEntity())
    }

    override suspend fun incrementPlayCount(songId: Long) {
        songDao.incrementPlayCount(songId)
    }

    override suspend fun deleteSong(song: Song) {
        songDao.deleteSong(song.toEntity())
    }

    override suspend fun deleteSongByFilePath(filePath: String) {
        songDao.deleteSongByFilePath(filePath)
    }

    override suspend fun deleteOrphanedSongs(validPaths: List<String>) {
        songDao.deleteOrphanedSongs(validPaths)
    }

    override suspend fun deleteAllSongs() {
        songDao.deleteAllSongs()
    }

    override suspend fun clearPlayHistory() {
        songDao.clearPlayHistory()
    }
}
