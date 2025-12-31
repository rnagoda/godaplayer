package com.godaplayer.app.ui.screens.browse

import android.content.Context
import android.media.MediaMetadataRetriever
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.godaplayer.app.domain.model.FileItem
import com.godaplayer.app.domain.model.Song
import com.godaplayer.app.domain.repository.SongRepository
import com.godaplayer.app.player.PlaybackController
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

data class QuickAccessLocation(
    val name: String,
    val path: String,
    val icon: String = "📁"
)

data class BrowseUiState(
    val currentPath: String = "",
    val files: List<FileItem> = emptyList(),
    val isLoading: Boolean = false,
    val hasPermission: Boolean = false,
    val canNavigateUp: Boolean = false,
    val showQuickAccess: Boolean = true,
    val quickAccessLocations: List<QuickAccessLocation> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class BrowseViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val playbackController: PlaybackController,
    private val songRepository: SongRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BrowseUiState())
    val uiState: StateFlow<BrowseUiState> = _uiState.asStateFlow()

    private val supportedExtensions = setOf("mp3", "flac", "aac", "m4a", "ogg", "wav")

    // Storage root - allows navigation to parent directories
    private val storageRoot = "/storage/emulated/0"

    // Quick access locations
    private val quickAccessLocations = listOfNotNull(
        QuickAccessLocation("Internal Storage", storageRoot, "📱"),
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
            .takeIf { it.exists() }
            ?.let { QuickAccessLocation("Music", it.absolutePath, "🎵") },
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            .takeIf { it.exists() }
            ?.let { QuickAccessLocation("Downloads", it.absolutePath, "📥") },
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS)
            .takeIf { it.exists() }
            ?.let { QuickAccessLocation("Podcasts", it.absolutePath, "🎙") },
        // Check for common audio folders
        File("$storageRoot/Audio").takeIf { it.exists() }
            ?.let { QuickAccessLocation("Audio", it.absolutePath, "🔊") },
        File("$storageRoot/Ringtones").takeIf { it.exists() }
            ?.let { QuickAccessLocation("Ringtones", it.absolutePath, "🔔") },
    )

    init {
        _uiState.update {
            it.copy(
                quickAccessLocations = quickAccessLocations,
                showQuickAccess = true
            )
        }
    }

    fun onPermissionResult(granted: Boolean) {
        _uiState.update { it.copy(hasPermission = granted) }
        if (granted && _uiState.value.showQuickAccess) {
            // Stay on quick access screen, don't auto-navigate
        }
    }

    fun navigateToFolder(path: String) {
        _uiState.update { it.copy(showQuickAccess = false) }
        loadDirectory(path)
    }

    fun navigateUp() {
        val currentPath = _uiState.value.currentPath
        val parentPath = File(currentPath).parent

        if (parentPath == null || currentPath == storageRoot || currentPath == "/storage") {
            // Go back to quick access
            _uiState.update { it.copy(showQuickAccess = true, currentPath = "", files = emptyList()) }
        } else {
            loadDirectory(parentPath)
        }
    }

    fun showQuickAccess() {
        _uiState.update { it.copy(showQuickAccess = true, currentPath = "", files = emptyList()) }
    }

    private fun loadDirectory(path: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val files = withContext(Dispatchers.IO) {
                    val directory = File(path)
                    if (!directory.exists() || !directory.isDirectory) {
                        return@withContext emptyList()
                    }

                    directory.listFiles()
                        ?.filter { file ->
                            // Show all directories and audio files
                            // Filter out hidden files (starting with .)
                            !file.name.startsWith(".") && (file.isDirectory || isAudioFile(file))
                        }
                        ?.map { file ->
                            if (file.isDirectory) {
                                FileItem(
                                    path = file.absolutePath,
                                    name = file.name,
                                    isDirectory = true
                                )
                            } else {
                                createAudioFileItem(file)
                            }
                        }
                        ?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
                        ?: emptyList()
                }

                _uiState.update {
                    it.copy(
                        currentPath = path,
                        files = files,
                        isLoading = false,
                        showQuickAccess = false,
                        canNavigateUp = true // Always allow navigating up (will go to quick access at root)
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load directory"
                    )
                }
            }
        }
    }

    private fun isAudioFile(file: File): Boolean {
        val extension = file.extension.lowercase()
        return extension in supportedExtensions
    }

    private fun createAudioFileItem(file: File): FileItem {
        var durationMs: Long? = null
        var artist: String? = null
        var title: String? = null

        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(file.absolutePath)
            durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
            artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            retriever.release()
        } catch (e: Exception) {
            // Ignore metadata extraction errors
        }

        return FileItem(
            path = file.absolutePath,
            name = file.name,
            isDirectory = false,
            size = file.length(),
            durationMs = durationMs,
            artist = artist,
            title = title
        )
    }

    fun playFile(fileItem: FileItem) {
        if (fileItem.isDirectory) return

        viewModelScope.launch {
            // Get all audio files in current directory to create a queue
            val audioFiles = _uiState.value.files
                .filter { !it.isDirectory }

            // Ensure all songs are in the database and get their IDs
            val songs = audioFiles.map { file ->
                getOrCreateSong(file)
            }

            val startIndex = songs.indexOfFirst { it.filePath == fileItem.path }
                .coerceAtLeast(0)

            playbackController.playQueue(songs, startIndex)
        }
    }

    private suspend fun getOrCreateSong(fileItem: FileItem): Song {
        // Check if song already exists in database
        val existingSong = songRepository.getSongByFilePath(fileItem.path)
        if (existingSong != null) {
            return existingSong
        }

        // Create new song and insert into database
        val newSong = Song(
            filePath = fileItem.path,
            fileName = fileItem.name,
            title = fileItem.title,
            artist = fileItem.artist,
            durationMs = fileItem.durationMs ?: 0,
            fileSize = fileItem.size ?: 0
        )

        val id = songRepository.insertSong(newSong)
        return newSong.copy(id = id)
    }
}
