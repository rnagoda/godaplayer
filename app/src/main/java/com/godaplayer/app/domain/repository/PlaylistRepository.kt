package com.godaplayer.app.domain.repository

import com.godaplayer.app.domain.model.Playlist
import com.godaplayer.app.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {
    fun getAllPlaylists(): Flow<List<Playlist>>
    fun getPlaylistById(id: Long): Flow<Playlist?>
    fun getSongsForPlaylist(playlistId: Long): Flow<List<Song>>
    fun getPlaylistsContainingSong(songId: Long): Flow<List<Long>>
    fun searchPlaylists(query: String): Flow<List<Playlist>>

    suspend fun getPlaylistByIdOnce(id: Long): Playlist?
    suspend fun getSongsForPlaylistOnce(playlistId: Long): List<Song>
    suspend fun createPlaylist(name: String, description: String? = null): Long
    suspend fun updatePlaylist(playlist: Playlist)
    suspend fun renamePlaylist(playlistId: Long, name: String)
    suspend fun deletePlaylist(playlistId: Long)
    suspend fun addSongToPlaylist(playlistId: Long, songId: Long): Boolean
    suspend fun addSongsToPlaylist(playlistId: Long, songIds: List<Long>): Int
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)
    suspend fun clearPlaylist(playlistId: Long)
    suspend fun reorderPlaylist(playlistId: Long, fromPosition: Int, toPosition: Int)
    suspend fun duplicatePlaylist(playlistId: Long, newName: String): Long
    suspend fun updateLastPlayed(playlistId: Long)
}
