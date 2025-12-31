package com.godaplayer.app.ui.screens.playlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.godaplayer.app.domain.model.Playlist
import com.godaplayer.app.domain.repository.PlaylistRepository
import com.godaplayer.app.domain.usecase.playlist.CreatePlaylistUseCase
import com.godaplayer.app.domain.usecase.playlist.DeletePlaylistUseCase
import com.godaplayer.app.domain.usecase.playlist.RenamePlaylistUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlaylistsUiState(
    val playlists: List<Playlist> = emptyList(),
    val showCreateDialog: Boolean = false,
    val showRenameDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val selectedPlaylist: Playlist? = null
)

@HiltViewModel
class PlaylistsViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    private val createPlaylistUseCase: CreatePlaylistUseCase,
    private val renamePlaylistUseCase: RenamePlaylistUseCase,
    private val deletePlaylistUseCase: DeletePlaylistUseCase
) : ViewModel() {

    private val _showCreateDialog = MutableStateFlow(false)
    private val _showRenameDialog = MutableStateFlow(false)
    private val _showDeleteDialog = MutableStateFlow(false)
    private val _selectedPlaylist = MutableStateFlow<Playlist?>(null)

    val uiState: StateFlow<PlaylistsUiState> = combine(
        playlistRepository.getAllPlaylists(),
        _showCreateDialog,
        _showRenameDialog,
        _showDeleteDialog,
        _selectedPlaylist
    ) { playlists, showCreate, showRename, showDelete, selected ->
        PlaylistsUiState(
            playlists = playlists,
            showCreateDialog = showCreate,
            showRenameDialog = showRename,
            showDeleteDialog = showDelete,
            selectedPlaylist = selected
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PlaylistsUiState()
    )

    fun showCreateDialog() {
        _showCreateDialog.value = true
    }

    fun hideCreateDialog() {
        _showCreateDialog.value = false
    }

    fun showRenameDialog(playlist: Playlist) {
        _selectedPlaylist.value = playlist
        _showRenameDialog.value = true
    }

    fun hideRenameDialog() {
        _showRenameDialog.value = false
        _selectedPlaylist.value = null
    }

    fun showDeleteDialog(playlist: Playlist) {
        _selectedPlaylist.value = playlist
        _showDeleteDialog.value = true
    }

    fun hideDeleteDialog() {
        _showDeleteDialog.value = false
        _selectedPlaylist.value = null
    }

    fun createPlaylist(name: String, description: String?) {
        viewModelScope.launch {
            createPlaylistUseCase(name, description)
            hideCreateDialog()
        }
    }

    fun renamePlaylist(newName: String) {
        val playlist = _selectedPlaylist.value ?: return
        viewModelScope.launch {
            renamePlaylistUseCase(playlist.id, newName)
            hideRenameDialog()
        }
    }

    fun deletePlaylist() {
        val playlist = _selectedPlaylist.value ?: return
        viewModelScope.launch {
            deletePlaylistUseCase(playlist.id)
            hideDeleteDialog()
        }
    }
}
