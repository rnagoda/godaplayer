package com.godaplayer.app.ui.screens.queue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.godaplayer.app.domain.model.Playlist
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

data class QueueUiState(
    val queue: List<Song> = emptyList(),
    val currentIndex: Int = 0,
    val currentSong: Song? = null
)

data class QueueMenuState(
    val selectedSong: Song? = null,
    val selectedIndex: Int = -1,
    val showContextMenu: Boolean = false,
    val showAddToPlaylistDialog: Boolean = false,
    val showCreatePlaylistDialog: Boolean = false,
    val showSaveQueueDialog: Boolean = false,
    val showSongInfoSheet: Boolean = false,
    val allPlaylists: List<Playlist> = emptyList(),
    val existingPlaylistIds: List<Long> = emptyList(),
    val songPlaylists: List<Playlist> = emptyList()
)

@HiltViewModel
class QueueViewModel @Inject constructor(
    private val playbackController: PlaybackController,
    private val queueManager: QueueManager,
    private val playlistRepository: PlaylistRepository,
    private val addSongsToPlaylistUseCase: AddSongsToPlaylistUseCase,
    private val createPlaylistUseCase: CreatePlaylistUseCase
) : ViewModel() {

    private val _menuState = MutableStateFlow(QueueMenuState())
    val menuState: StateFlow<QueueMenuState> = _menuState.asStateFlow()

    val uiState: StateFlow<QueueUiState> = combine(
        queueManager.queue,
        queueManager.currentIndex,
        playbackController.currentSong
    ) { queue, currentIndex, currentSong ->
        QueueUiState(
            queue = queue,
            currentIndex = currentIndex,
            currentSong = currentSong
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = QueueUiState()
    )

    fun clearQueue() {
        playbackController.clearQueue()
    }

    fun removeFromQueue(index: Int) {
        playbackController.removeFromQueue(index)
    }

    fun playAtIndex(index: Int) {
        val currentIndex = queueManager.currentIndex.value
        if (index > currentIndex) {
            repeat(index - currentIndex) {
                playbackController.skipToNext()
            }
        }
    }

    fun reorderQueue(fromIndex: Int, toIndex: Int) {
        queueManager.reorder(fromIndex, toIndex)
    }

    // Context menu functions
    fun showContextMenu(song: Song, index: Int) {
        _menuState.update { it.copy(selectedSong = song, selectedIndex = index, showContextMenu = true) }
    }

    fun dismissContextMenu() {
        _menuState.update { it.copy(showContextMenu = false) }
    }

    fun showAddToPlaylistDialog() {
        viewModelScope.launch {
            val song = _menuState.value.selectedSong ?: return@launch
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
            val song = _menuState.value.selectedSong ?: return@launch
            val playlistId = createPlaylistUseCase(name, description)
            addSongsToPlaylistUseCase(playlistId, listOf(song.id))
            _menuState.update {
                it.copy(
                    showCreatePlaylistDialog = false,
                    selectedSong = null,
                    selectedIndex = -1
                )
            }
        }
    }

    fun addToPlaylists(playlistIds: List<Long>) {
        viewModelScope.launch {
            val song = _menuState.value.selectedSong ?: return@launch
            playlistIds.forEach { playlistId ->
                addSongsToPlaylistUseCase(playlistId, listOf(song.id))
            }
            _menuState.update {
                it.copy(
                    showAddToPlaylistDialog = false,
                    selectedSong = null,
                    selectedIndex = -1
                )
            }
        }
    }

    fun showSongInfo() {
        viewModelScope.launch {
            val song = _menuState.value.selectedSong ?: return@launch
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
        _menuState.update { it.copy(showSongInfoSheet = false, selectedSong = null, selectedIndex = -1) }
    }

    fun removeSelectedFromQueue() {
        val index = _menuState.value.selectedIndex
        if (index >= 0) {
            playbackController.removeFromQueue(index)
        }
        _menuState.update { it.copy(selectedSong = null, selectedIndex = -1) }
    }

    // Save queue as playlist
    fun showSaveQueueDialog() {
        _menuState.update { it.copy(showSaveQueueDialog = true) }
    }

    fun dismissSaveQueueDialog() {
        _menuState.update { it.copy(showSaveQueueDialog = false) }
    }

    fun saveQueueAsPlaylist(name: String, description: String?) {
        viewModelScope.launch {
            val queue = uiState.value.queue
            if (queue.isEmpty()) return@launch

            val playlistId = createPlaylistUseCase(name, description)
            val songIds = queue.map { it.id }
            addSongsToPlaylistUseCase(playlistId, songIds)
            _menuState.update { it.copy(showSaveQueueDialog = false) }
        }
    }
}
