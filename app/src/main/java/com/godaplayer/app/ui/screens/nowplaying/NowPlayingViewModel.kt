package com.godaplayer.app.ui.screens.nowplaying

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.godaplayer.app.domain.model.Playlist
import com.godaplayer.app.domain.model.RepeatMode
import com.godaplayer.app.domain.model.Song
import com.godaplayer.app.domain.repository.PlaylistRepository
import com.godaplayer.app.domain.usecase.playlist.AddSongsToPlaylistUseCase
import com.godaplayer.app.domain.usecase.playlist.CreatePlaylistUseCase
import com.godaplayer.app.player.PlaybackController
import com.godaplayer.app.player.QueueManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NowPlayingUiState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val progress: Float = 0f,
    val currentPositionMs: Long = 0,
    val durationMs: Long = 0,
    val shuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val upNext: List<Song> = emptyList()
)

data class NowPlayingMenuState(
    val showAddToPlaylistDialog: Boolean = false,
    val showCreatePlaylistDialog: Boolean = false,
    val showSongInfoSheet: Boolean = false,
    val allPlaylists: List<Playlist> = emptyList(),
    val existingPlaylistIds: List<Long> = emptyList(),
    val songPlaylists: List<Playlist> = emptyList()
)

@HiltViewModel
class NowPlayingViewModel @Inject constructor(
    private val playbackController: PlaybackController,
    private val queueManager: QueueManager,
    private val playlistRepository: PlaylistRepository,
    private val addSongsToPlaylistUseCase: AddSongsToPlaylistUseCase,
    private val createPlaylistUseCase: CreatePlaylistUseCase
) : ViewModel() {

    private val _menuState = MutableStateFlow(NowPlayingMenuState())
    val menuState: StateFlow<NowPlayingMenuState> = _menuState.asStateFlow()

    val uiState: StateFlow<NowPlayingUiState> = combine(
        playbackController.currentSong,
        playbackController.isPlaying,
        playbackController.progress,
        playbackController.currentPositionMs,
        playbackController.durationMs,
        playbackController.shuffleEnabled,
        playbackController.repeatMode,
        queueManager.queue,
        queueManager.currentIndex
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val queue = values[7] as List<Song>
        val currentIndex = values[8] as Int
        val upNext = if (currentIndex + 1 < queue.size) {
            queue.subList(currentIndex + 1, minOf(currentIndex + 3, queue.size))
        } else {
            emptyList()
        }

        NowPlayingUiState(
            currentSong = values[0] as Song?,
            isPlaying = values[1] as Boolean,
            progress = values[2] as Float,
            currentPositionMs = values[3] as Long,
            durationMs = values[4] as Long,
            shuffleEnabled = values[5] as Boolean,
            repeatMode = values[6] as RepeatMode,
            upNext = upNext
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NowPlayingUiState()
    )

    fun togglePlayPause() {
        playbackController.togglePlayPause()
    }

    fun skipToNext() {
        playbackController.skipToNext()
    }

    fun skipToPrevious() {
        playbackController.skipToPrevious()
    }

    fun toggleShuffle() {
        playbackController.toggleShuffle()
    }

    fun cycleRepeatMode() {
        playbackController.cycleRepeatMode()
    }

    fun seekToProgress(progress: Float) {
        playbackController.seekToProgress(progress)
    }

    // Playlist functions for current song
    fun showAddToPlaylistDialog() {
        viewModelScope.launch {
            val song = uiState.value.currentSong ?: return@launch
            if (song.id == 0L) {
                // Song not in database - can't add to playlist
                return@launch
            }
            val playlists = playlistRepository.getAllPlaylists().first()
            val existingIds = playlistRepository.getPlaylistsContainingSong(song.id).first()
            _menuState.update {
                it.copy(
                    showAddToPlaylistDialog = true,
                    allPlaylists = playlists,
                    existingPlaylistIds = existingIds
                )
            }
        }
    }

    fun dismissAddToPlaylistDialog() {
        _menuState.update { it.copy(showAddToPlaylistDialog = false) }
    }

    fun showCreatePlaylistDialog() {
        _menuState.update { it.copy(showCreatePlaylistDialog = true, showAddToPlaylistDialog = false) }
    }

    fun dismissCreatePlaylistDialog() {
        _menuState.update { it.copy(showCreatePlaylistDialog = false) }
    }

    fun createPlaylistAndAddSong(name: String, description: String?) {
        viewModelScope.launch {
            val song = uiState.value.currentSong ?: return@launch
            if (song.id == 0L) return@launch
            val playlistId = createPlaylistUseCase(name, description)
            addSongsToPlaylistUseCase(playlistId, listOf(song.id))
            _menuState.update { it.copy(showCreatePlaylistDialog = false) }
        }
    }

    fun addToPlaylists(playlistIds: List<Long>) {
        viewModelScope.launch {
            val song = uiState.value.currentSong ?: return@launch
            if (song.id == 0L) return@launch
            playlistIds.forEach { playlistId ->
                addSongsToPlaylistUseCase(playlistId, listOf(song.id))
            }
            _menuState.update { it.copy(showAddToPlaylistDialog = false) }
        }
    }

    fun showSongInfo() {
        viewModelScope.launch {
            val song = uiState.value.currentSong ?: return@launch
            if (song.id == 0L) {
                _menuState.update { it.copy(showSongInfoSheet = true, songPlaylists = emptyList()) }
                return@launch
            }
            val playlistIds = playlistRepository.getPlaylistsContainingSong(song.id).first()
            val allPlaylists = playlistRepository.getAllPlaylists().first()
            val songPlaylists = allPlaylists.filter { it.id in playlistIds }
            _menuState.update {
                it.copy(
                    showSongInfoSheet = true,
                    songPlaylists = songPlaylists
                )
            }
        }
    }

    fun dismissSongInfo() {
        _menuState.update { it.copy(showSongInfoSheet = false) }
    }
}
