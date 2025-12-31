package com.godaplayer.app.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.godaplayer.app.domain.model.Playlist
import com.godaplayer.app.domain.model.Song
import com.godaplayer.app.domain.repository.PlaylistRepository
import com.godaplayer.app.domain.repository.SongRepository
import com.godaplayer.app.domain.repository.SongSortOrder
import com.godaplayer.app.domain.usecase.file.ScanFilesUseCase
import com.godaplayer.app.domain.usecase.file.ScanProgress
import com.godaplayer.app.domain.usecase.playlist.AddSongsToPlaylistUseCase
import com.godaplayer.app.domain.usecase.playlist.CreatePlaylistUseCase
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

data class LibraryUiState(
    val songs: List<Song> = emptyList(),
    val songCount: Int = 0,
    val totalDurationMs: Long = 0,
    val sortOrder: SongSortOrder = SongSortOrder.TITLE,
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val scanProgress: ScanProgress = ScanProgress()
)

data class SongMenuState(
    val selectedSong: Song? = null,
    val showContextMenu: Boolean = false,
    val showAddToPlaylistDialog: Boolean = false,
    val showSongInfoSheet: Boolean = false,
    val showCreatePlaylistDialog: Boolean = false,
    val allPlaylists: List<Playlist> = emptyList(),
    val existingPlaylistIds: List<Long> = emptyList(),
    val songPlaylists: List<Playlist> = emptyList()
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val playlistRepository: PlaylistRepository,
    private val scanFilesUseCase: ScanFilesUseCase,
    private val playbackController: PlaybackController,
    private val addSongsToPlaylistUseCase: AddSongsToPlaylistUseCase,
    private val createPlaylistUseCase: CreatePlaylistUseCase
) : ViewModel() {

    private val _sortOrder = MutableStateFlow(SongSortOrder.TITLE)
    private val _searchQuery = MutableStateFlow("")
    private val _isSearching = MutableStateFlow(false)
    private val _menuState = MutableStateFlow(SongMenuState())

    val uiState: StateFlow<LibraryUiState> = combine(
        songRepository.getAllSongs(SongSortOrder.TITLE),
        songRepository.getSongCount(),
        songRepository.getTotalDuration(),
        _sortOrder,
        _searchQuery,
        _isSearching,
        scanFilesUseCase.scanProgress
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val allSongs = values[0] as List<Song>
        val songCount = values[1] as Int
        val totalDuration = values[2] as Long
        val sortOrder = values[3] as SongSortOrder
        val searchQuery = values[4] as String
        val isSearching = values[5] as Boolean
        val scanProgress = values[6] as ScanProgress

        val filteredSongs = if (searchQuery.isNotEmpty()) {
            allSongs.filter { song ->
                song.displayTitle.contains(searchQuery, ignoreCase = true) ||
                song.artist?.contains(searchQuery, ignoreCase = true) == true ||
                song.album?.contains(searchQuery, ignoreCase = true) == true
            }
        } else {
            allSongs
        }

        val sortedSongs = when (sortOrder) {
            SongSortOrder.TITLE -> filteredSongs.sortedBy { it.displayTitle.lowercase() }
            SongSortOrder.ARTIST -> filteredSongs.sortedWith(
                compareBy({ it.artist?.lowercase() ?: "" }, { it.album?.lowercase() ?: "" }, { it.trackNumber ?: 0 })
            )
            SongSortOrder.DATE_ADDED -> filteredSongs.sortedByDescending { it.dateAdded }
            SongSortOrder.DURATION -> filteredSongs.sortedBy { it.durationMs }
            SongSortOrder.FILE_NAME -> filteredSongs.sortedBy { it.fileName.lowercase() }
        }

        LibraryUiState(
            songs = sortedSongs,
            songCount = songCount,
            totalDurationMs = totalDuration,
            sortOrder = sortOrder,
            searchQuery = searchQuery,
            isSearching = isSearching,
            scanProgress = scanProgress
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LibraryUiState()
    )

    val menuState: StateFlow<SongMenuState> = _menuState.asStateFlow()

    fun setSortOrder(sortOrder: SongSortOrder) {
        _sortOrder.value = sortOrder
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleSearch() {
        _isSearching.value = !_isSearching.value
        if (!_isSearching.value) {
            _searchQuery.value = ""
        }
    }

    fun playSong(song: Song, allSongs: List<Song>) {
        val startIndex = allSongs.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
        playbackController.playQueue(allSongs, startIndex)
    }

    fun scanLibrary() {
        viewModelScope.launch {
            scanFilesUseCase.scanAllFolders()
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
            val playlistId = createPlaylistUseCase(name, description)
            addSongsToPlaylistUseCase(playlistId, listOf(song.id))
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
            playlistIds.forEach { playlistId ->
                addSongsToPlaylistUseCase(playlistId, listOf(song.id))
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
}
