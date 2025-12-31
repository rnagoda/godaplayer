package com.godaplayer.app.domain.usecase.playlist

import com.godaplayer.app.domain.repository.PlaylistRepository
import javax.inject.Inject

class RenamePlaylistUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    suspend operator fun invoke(playlistId: Long, newName: String) {
        playlistRepository.renamePlaylist(playlistId, newName.trim())
    }
}
