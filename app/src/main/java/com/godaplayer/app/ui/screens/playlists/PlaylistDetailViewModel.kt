package com.godaplayer.app.ui.screens.playlists

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.godaplayer.app.domain.model.Playlist
import com.godaplayer.app.domain.model.Song
import com.godaplayer.app.domain.repository.PlaylistRepository
import com.godaplayer.app.domain.usecase.playlist.AddSongsToPlaylistUseCase
import com.godaplayer.app.domain.usecase.playlist.CreatePlaylistUseCase
import com.godaplayer.app.domain.usecase.playlist.RemoveSongFromPlaylistUseCase
import com.godaplayer.app.domain.usecase.playlist.ReorderPlaylistUseCase
import com.godaplayer.app.player.PlaybackController
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

data class PlaylistDetailUiState(
    val playlist: Playlist? = null,
    val songs: List<Song> = emptyList(),
    val isLoading: Boolean = true,
    val showDeleteSongDialog: Boolean = false,
    val songToDelete: Song? = null
)

data class PlaylistDetailMenuState(
    val selectedSong: Song? = null,
    val showContextMenu: Boolean = false,
    val showAddToPlaylistDialog: Boolean = false,
    val showCreatePlaylistDialog: Boolean = false,
    val showSongInfoSheet: Boolean = false,
    val allPlaylists: List<Playlist> = emptyList(),
    val existingPlaylistIds: List<Long> = emptyList(),
    val songPlaylists: List<Playlist> = emptyList()
)

@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val playlistRepository: PlaylistRepository,
    private val removeSongFromPlaylistUseCase: RemoveSongFromPlaylistUseCase,
    private val reorderPlaylistUseCase: ReorderPlaylistUseCase,
    private val playbackController: PlaybackController,
    private val addSongsToPlaylistUseCase: AddSongsToPlaylistUseCase,
    private val createPlaylistUseCase: CreatePlaylistUseCase
) : ViewModel() {

    private val playlistId: Long = savedStateHandle.get<Long>("playlistId") ?: 0L

    private val _showDeleteSongDialog = MutableStateFlow(false)
    private val _songToDelete = MutableStateFlow<Song?>(null)
    private val _menuState = MutableStateFlow(PlaylistDetailMenuState())

    private val playlistFlow = playlistRepository.getPlaylistById(playlistId)
    private val songsFlow = playlistRepository.getSongsForPlaylist(playlistId)

    val uiState: StateFlow<PlaylistDetailUiState> = combine(
        playlistFlow,
        songsFlow,
        _showDeleteSongDialog,
        _songToDelete
    ) { playlist, songs, showDialog, songToDelete ->
        PlaylistDetailUiState(
            playlist = playlist,
            songs = songs,
            isLoading = false,
            showDeleteSongDialog = showDialog,
            songToDelete = songToDelete
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PlaylistDetailUiState()
    )

    val menuState: StateFlow<PlaylistDetailMenuState> = _menuState.asStateFlow()

    fun playPlaylist(startIndex: Int = 0) {
        val songs = uiState.value.songs
        if (songs.isNotEmpty()) {
            playbackController.playQueue(songs, startIndex)
            viewModelScope.launch {
                playlistRepository.updateLastPlayed(playlistId)
            }
        }
    }

    fun playSong(song: Song) {
        val songs = uiState.value.songs
        val index = songs.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
        playPlaylist(index)
    }

    fun shufflePlaylist() {
        val songs = uiState.value.songs
        if (songs.isNotEmpty()) {
            val shuffled = songs.shuffled()
            playbackController.playQueue(shuffled, 0)
            viewModelScope.launch {
                playlistRepository.updateLastPlayed(playlistId)
            }
        }
    }

    fun showDeleteSongDialog(song: Song) {
        _songToDelete.value = song
        _showDeleteSongDialog.value = true
    }

    fun hideDeleteSongDialog() {
        _showDeleteSongDialog.value = false
        _songToDelete.value = null
    }

    fun removeSong() {
        val song = _songToDelete.value ?: return
        viewModelScope.launch {
            removeSongFromPlaylistUseCase(playlistId, song.id)
            hideDeleteSongDialog()
        }
    }

    fun reorderSongs(fromIndex: Int, toIndex: Int) {
        viewModelScope.launch {
            reorderPlaylistUseCase(playlistId, fromIndex, toIndex)
        }
    }

    // Context menu functions
    fun showContextMenu(song: Song) {
        _menuState.update { it.copy(selectedSong = song, showContextMenu = true) }
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
            val newPlaylistId = createPlaylistUseCase(name, description)
            addSongsToPlaylistUseCase(newPlaylistId, listOf(song.id))
            _menuState.update {
                it.copy(
                    showCreatePlaylistDialog = false,
                    selectedSong = null
                )
            }
        }
    }

    fun addToPlaylists(playlistIds: List<Long>) {
        viewModelScope.launch {
            val song = _menuState.value.selectedSong ?: return@launch
            playlistIds.forEach { targetPlaylistId ->
                addSongsToPlaylistUseCase(targetPlaylistId, listOf(song.id))
            }
            _menuState.update {
                it.copy(
                    showAddToPlaylistDialog = false,
                    selectedSong = null
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
        _menuState.update { it.copy(showSongInfoSheet = false, selectedSong = null) }
    }

    fun playNext() {
        val song = _menuState.value.selectedSong ?: return
        playbackController.addToQueueNext(song)
        _menuState.update { it.copy(selectedSong = null) }
    }

    fun addToQueue() {
        val song = _menuState.value.selectedSong ?: return
        playbackController.addToQueue(song)
        _menuState.update { it.copy(selectedSong = null) }
    }

    fun removeSelectedFromPlaylist() {
        val song = _menuState.value.selectedSong ?: return
        showDeleteSongDialog(song)
        _menuState.update { it.copy(selectedSong = null) }
    }
}
