package com.godaplayer.app.domain.usecase.playlist

import com.godaplayer.app.domain.repository.PlaylistRepository
import javax.inject.Inject

class RemoveSongFromPlaylistUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    suspend operator fun invoke(playlistId: Long, songId: Long) {
        playlistRepository.removeSongFromPlaylist(playlistId, songId)
    }
}
