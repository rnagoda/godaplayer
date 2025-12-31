package com.godaplayer.app.domain.usecase.playlist

import com.godaplayer.app.domain.repository.PlaylistRepository
import javax.inject.Inject

class DeletePlaylistUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    suspend operator fun invoke(playlistId: Long) {
        playlistRepository.deletePlaylist(playlistId)
    }
}
