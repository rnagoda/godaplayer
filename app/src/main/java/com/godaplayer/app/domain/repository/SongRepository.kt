package com.godaplayer.app.domain.repository

import com.godaplayer.app.domain.model.Song
import kotlinx.coroutines.flow.Flow

enum class SongSortOrder {
    TITLE,
    ARTIST,
    DATE_ADDED,
    DURATION,
    FILE_NAME
}

interface SongRepository {
    fun getAllSongs(sortOrder: SongSortOrder = SongSortOrder.TITLE): Flow<List<Song>>
    fun getMostPlayedSongs(): Flow<List<Song>>
    fun getRecentlyPlayed(limit: Int = 50): Flow<List<Song>>
    fun searchSongs(query: String): Flow<List<Song>>
    fun getSongCount(): Flow<Int>
    fun getTotalDuration(): Flow<Long>

    suspend fun getSongById(id: Long): Song?
    suspend fun getSongByFilePath(filePath: String): Song?
    suspend fun insertSong(song: Song): Long
    suspend fun insertSongs(songs: List<Song>): List<Long>
    suspend fun updateSong(song: Song)
    suspend fun incrementPlayCount(songId: Long)
    suspend fun deleteSong(song: Song)
    suspend fun deleteSongByFilePath(filePath: String)
    suspend fun deleteOrphanedSongs(validPaths: List<String>)
    suspend fun deleteAllSongs()
    suspend fun clearPlayHistory()
}
