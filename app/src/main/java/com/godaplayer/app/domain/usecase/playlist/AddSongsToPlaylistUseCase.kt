package com.godaplayer.app.domain.usecase.playlist

import com.godaplayer.app.domain.repository.PlaylistRepository
import javax.inject.Inject

class AddSongsToPlaylistUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    suspend operator fun invoke(playlistId: Long, songIds: List<Long>): Int {
        return playlistRepository.addSongsToPlaylist(playlistId, songIds)
    }

    suspend fun addSingle(playlistId: Long, songId: Long): Boolean {
        return playlistRepository.addSongToPlaylist(playlistId, songId)
    }
}
