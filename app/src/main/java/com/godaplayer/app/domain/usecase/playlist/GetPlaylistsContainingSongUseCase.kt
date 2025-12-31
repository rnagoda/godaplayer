package com.godaplayer.app.domain.usecase.playlist

import com.godaplayer.app.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPlaylistsContainingSongUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    operator fun invoke(songId: Long): Flow<List<Long>> {
        return playlistRepository.getPlaylistsContainingSong(songId)
    }
}
