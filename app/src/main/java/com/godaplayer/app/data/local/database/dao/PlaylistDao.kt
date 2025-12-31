package com.godaplayer.app.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.godaplayer.app.data.local.database.entity.PlaylistEntity
import com.godaplayer.app.data.local.database.entity.SongEntity
import kotlinx.coroutines.flow.Flow

data class PlaylistWithSongCount(
    val id: Long,
    val name: String,
    val description: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val lastPlayed: Long?,
    val songCount: Int,
    val totalDurationMs: Long
)

@Dao
interface PlaylistDao {

    @Query("SELECT * FROM playlists ORDER BY name COLLATE NOCASE ASC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Query("""
        SELECT
            p.id,
            p.name,
            p.description,
            p.created_at as createdAt,
            p.updated_at as updatedAt,
            p.last_played as lastPlayed,
            COUNT(ps.song_id) as songCount,
            COALESCE(SUM(s.duration_ms), 0) as totalDurationMs
        FROM playlists p
        LEFT JOIN playlist_songs ps ON p.id = ps.playlist_id
        LEFT JOIN songs s ON ps.song_id = s.id
        GROUP BY p.id
        ORDER BY p.name COLLATE NOCASE ASC
    """)
    fun getAllPlaylistsWithSongCount(): Flow<List<PlaylistWithSongCount>>

    @Query("SELECT * FROM playlists WHERE id = :id")
    suspend fun getPlaylistById(id: Long): PlaylistEntity?

    @Query("SELECT * FROM playlists WHERE id = :id")
    fun getPlaylistByIdFlow(id: Long): Flow<PlaylistEntity?>

    @Query("""
        SELECT * FROM playlists
        WHERE name LIKE '%' || :query || '%'
        OR description LIKE '%' || :query || '%'
        ORDER BY name COLLATE NOCASE ASC
    """)
    fun searchPlaylists(query: String): Flow<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)

    @Query("UPDATE playlists SET name = :name, updated_at = :updatedAt WHERE id = :playlistId")
    suspend fun renamePlaylist(playlistId: Long, name: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE playlists SET last_played = :timestamp, updated_at = :timestamp WHERE id = :playlistId")
    suspend fun updateLastPlayed(playlistId: Long, timestamp: Long = System.currentTimeMillis())

    @Delete
    suspend fun deletePlaylist(playlist: PlaylistEntity)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylistById(playlistId: Long)
}
