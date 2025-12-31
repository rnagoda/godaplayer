package com.godaplayer.app.domain.usecase.playlist

import com.godaplayer.app.domain.repository.PlaylistRepository
import javax.inject.Inject

class CreatePlaylistUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    suspend operator fun invoke(name: String, description: String? = null): Long {
        return playlistRepository.createPlaylist(name.trim(), description?.trim())
    }
}
