package com.godaplayer.app.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.godaplayer.app.data.local.database.entity.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {

    @Query("SELECT * FROM songs ORDER BY title COLLATE NOCASE ASC")
    fun getAllSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs ORDER BY title COLLATE NOCASE ASC")
    suspend fun getAllSongsOnce(): List<SongEntity>

    @Query("SELECT * FROM songs ORDER BY artist COLLATE NOCASE ASC, album COLLATE NOCASE ASC, track_number ASC")
    fun getAllSongsByArtist(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs ORDER BY date_added DESC")
    fun getAllSongsByDateAdded(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs ORDER BY duration_ms ASC")
    fun getAllSongsByDuration(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs ORDER BY file_name COLLATE NOCASE ASC")
    fun getAllSongsByFileName(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs ORDER BY play_count DESC")
    fun getMostPlayedSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE last_played IS NOT NULL ORDER BY last_played DESC LIMIT :limit")
    fun getRecentlyPlayed(limit: Int = 50): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE id = :id")
    suspend fun getSongById(id: Long): SongEntity?

    @Query("SELECT * FROM songs WHERE file_path = :filePath")
    suspend fun getSongByFilePath(filePath: String): SongEntity?

    @Query("""
        SELECT * FROM songs
        WHERE title LIKE '%' || :query || '%'
        OR artist LIKE '%' || :query || '%'
        OR album LIKE '%' || :query || '%'
        OR file_name LIKE '%' || :query || '%'
        ORDER BY title COLLATE NOCASE ASC
    """)
    fun searchSongs(query: String): Flow<List<SongEntity>>

    @Query("SELECT COUNT(*) FROM songs")
    fun getSongCount(): Flow<Int>

    @Query("SELECT SUM(duration_ms) FROM songs")
    fun getTotalDuration(): Flow<Long?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: SongEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<SongEntity>): List<Long>

    @Update
    suspend fun updateSong(song: SongEntity)

    @Query("UPDATE songs SET play_count = play_count + 1, last_played = :timestamp WHERE id = :songId")
    suspend fun incrementPlayCount(songId: Long, timestamp: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteSong(song: SongEntity)

    @Query("DELETE FROM songs WHERE file_path = :filePath")
    suspend fun deleteSongByFilePath(filePath: String)

    @Query("DELETE FROM songs WHERE file_path NOT IN (:validPaths)")
    suspend fun deleteOrphanedSongs(validPaths: List<String>)

    @Query("DELETE FROM songs")
    suspend fun deleteAllSongs()

    @Query("UPDATE songs SET play_count = 0, last_played = NULL")
    suspend fun clearPlayHistory()
}
