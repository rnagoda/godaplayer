package com.godaplayer.app.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.godaplayer.app.data.local.database.entity.PlaylistSongEntity
import com.godaplayer.app.data.local.database.entity.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistSongDao {

    @Query("""
        SELECT s.* FROM songs s
        INNER JOIN playlist_songs ps ON s.id = ps.song_id
        WHERE ps.playlist_id = :playlistId
        ORDER BY ps.position ASC
    """)
    fun getSongsForPlaylist(playlistId: Long): Flow<List<SongEntity>>

    @Query("""
        SELECT s.* FROM songs s
        INNER JOIN playlist_songs ps ON s.id = ps.song_id
        WHERE ps.playlist_id = :playlistId
        ORDER BY ps.position ASC
    """)
    suspend fun getSongsForPlaylistOnce(playlistId: Long): List<SongEntity>

    @Query("""
        SELECT p.id FROM playlists p
        INNER JOIN playlist_songs ps ON p.id = ps.playlist_id
        WHERE ps.song_id = :songId
    """)
    fun getPlaylistsContainingSong(songId: Long): Flow<List<Long>>

    @Query("SELECT COUNT(*) FROM playlist_songs WHERE playlist_id = :playlistId AND song_id = :songId")
    suspend fun isSongInPlaylist(playlistId: Long, songId: Long): Int

    @Query("SELECT COALESCE(MAX(position), -1) + 1 FROM playlist_songs WHERE playlist_id = :playlistId")
    suspend fun getNextPosition(playlistId: Long): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPlaylistSong(playlistSong: PlaylistSongEntity): Long

    @Transaction
    suspend fun addSongToPlaylist(playlistId: Long, songId: Long): Boolean {
        if (isSongInPlaylist(playlistId, songId) > 0) {
            return false // Song already in playlist
        }
        val position = getNextPosition(playlistId)
        insertPlaylistSong(PlaylistSongEntity(
            playlistId = playlistId,
            songId = songId,
            position = position
        ))
        return true
    }

    @Transaction
    suspend fun addSongsToPlaylist(playlistId: Long, songIds: List<Long>): Int {
        var addedCount = 0
        var position = getNextPosition(playlistId)
        for (songId in songIds) {
            if (isSongInPlaylist(playlistId, songId) == 0) {
                insertPlaylistSong(PlaylistSongEntity(
                    playlistId = playlistId,
                    songId = songId,
                    position = position++
                ))
                addedCount++
            }
        }
        return addedCount
    }

    @Query("DELETE FROM playlist_songs WHERE playlist_id = :playlistId AND song_id = :songId")
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)

    @Query("DELETE FROM playlist_songs WHERE playlist_id = :playlistId")
    suspend fun clearPlaylist(playlistId: Long)

    @Transaction
    suspend fun reorderPlaylist(playlistId: Long, fromPosition: Int, toPosition: Int) {
        if (fromPosition == toPosition) return

        // Get the song being moved
        val movingSong = getSongAtPosition(playlistId, fromPosition) ?: return

        if (fromPosition < toPosition) {
            // Moving down: shift songs between from+1 and to up by 1
            shiftPositionsUp(playlistId, fromPosition + 1, toPosition)
        } else {
            // Moving up: shift songs between to and from-1 down by 1
            shiftPositionsDown(playlistId, toPosition, fromPosition - 1)
        }

        // Update the moved song's position
        updatePosition(playlistId, movingSong, toPosition)
    }

    @Query("SELECT song_id FROM playlist_songs WHERE playlist_id = :playlistId AND position = :position")
    suspend fun getSongAtPosition(playlistId: Long, position: Int): Long?

    @Query("""
        UPDATE playlist_songs
        SET position = position - 1
        WHERE playlist_id = :playlistId AND position BETWEEN :fromPos AND :toPos
    """)
    suspend fun shiftPositionsUp(playlistId: Long, fromPos: Int, toPos: Int)

    @Query("""
        UPDATE playlist_songs
        SET position = position + 1
        WHERE playlist_id = :playlistId AND position BETWEEN :fromPos AND :toPos
    """)
    suspend fun shiftPositionsDown(playlistId: Long, fromPos: Int, toPos: Int)

    @Query("UPDATE playlist_songs SET position = :newPosition WHERE playlist_id = :playlistId AND song_id = :songId")
    suspend fun updatePosition(playlistId: Long, songId: Long, newPosition: Int)
}
