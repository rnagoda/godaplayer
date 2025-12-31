package com.godaplayer.app.ui.screens.settings

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.godaplayer.app.data.local.preferences.UserPreferencesRepository
import com.godaplayer.app.domain.repository.PlaylistRepository
import com.godaplayer.app.domain.repository.SongRepository
import com.godaplayer.app.domain.usecase.export.ExportPlaylistM3UUseCase
import com.godaplayer.app.domain.usecase.export.ImportPlaylistM3UUseCase
import com.godaplayer.app.domain.usecase.file.ScanFilesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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

data class SettingsUiState(
    val gaplessPlayback: Boolean = true,
    val resumeOnStart: Boolean = true,
    val autoScan: Boolean = true,
    val showFileExtensions: Boolean = true,
    val isScanning: Boolean = false,
    val isExporting: Boolean = false,
    val isImporting: Boolean = false
)

data class SettingsDialogState(
    val showClearHistoryDialog: Boolean = false,
    val showExportResultDialog: Boolean = false,
    val showImportResultDialog: Boolean = false,
    val exportResult: ExportResultData? = null,
    val importResult: ImportResultData? = null
)

data class ExportResultData(
    val success: Boolean,
    val message: String,
    val playlistCount: Int = 0
)

data class ImportResultData(
    val success: Boolean,
    val message: String,
    val playlistName: String = "",
    val matchedTracks: Int = 0,
    val totalTracks: Int = 0
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val songRepository: SongRepository,
    private val playlistRepository: PlaylistRepository,
    private val scanFilesUseCase: ScanFilesUseCase,
    private val exportPlaylistM3UUseCase: ExportPlaylistM3UUseCase,
    private val importPlaylistM3UUseCase: ImportPlaylistM3UUseCase
) : ViewModel() {

    private val _dialogState = MutableStateFlow(SettingsDialogState())
    val dialogState: StateFlow<SettingsDialogState> = _dialogState.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    private val _isExporting = MutableStateFlow(false)
    private val _isImporting = MutableStateFlow(false)

    val uiState: StateFlow<SettingsUiState> = combine(
        userPreferencesRepository.gaplessPlayback,
        userPreferencesRepository.resumeOnStart,
        userPreferencesRepository.autoScan,
        userPreferencesRepository.showFileExtensions,
        _isScanning,
        _isExporting,
        _isImporting
    ) { values ->
        SettingsUiState(
            gaplessPlayback = values[0] as Boolean,
            resumeOnStart = values[1] as Boolean,
            autoScan = values[2] as Boolean,
            showFileExtensions = values[3] as Boolean,
            isScanning = values[4] as Boolean,
            isExporting = values[5] as Boolean,
            isImporting = values[6] as Boolean
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun setGaplessPlayback(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setGaplessPlayback(enabled)
        }
    }

    fun setResumeOnStart(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setResumeOnStart(enabled)
        }
    }

    fun setAutoScan(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setAutoScan(enabled)
        }
    }

    fun setShowFileExtensions(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setShowFileExtensions(enabled)
        }
    }

    fun triggerRescan() {
        viewModelScope.launch {
            _isScanning.value = true
            try {
                scanFilesUseCase.scanAllFolders()
            } finally {
                _isScanning.value = false
            }
        }
    }

    fun showClearHistoryDialog() {
        _dialogState.update { it.copy(showClearHistoryDialog = true) }
    }

    fun dismissClearHistoryDialog() {
        _dialogState.update { it.copy(showClearHistoryDialog = false) }
    }

    fun clearPlayHistory() {
        viewModelScope.launch {
            songRepository.clearPlayHistory()
            dismissClearHistoryDialog()
        }
    }

    fun exportAllPlaylists(directoryUri: Uri) {
        viewModelScope.launch {
            _isExporting.value = true
            try {
                val docFile = DocumentFile.fromTreeUri(context, directoryUri)
                if (docFile == null || !docFile.canWrite()) {
                    _dialogState.update {
                        it.copy(
                            showExportResultDialog = true,
                            exportResult = ExportResultData(
                                success = false,
                                message = "Cannot write to selected directory"
                            )
                        )
                    }
                    return@launch
                }

                val playlists = playlistRepository.getAllPlaylists().first()
                var successCount = 0
                val failures = mutableListOf<String>()

                playlists.forEach { playlist ->
                    try {
                        val fileName = sanitizeFileName(playlist.name) + ".m3u"
                        val existingFile = docFile.findFile(fileName)
                        existingFile?.delete()

                        val newFile = docFile.createFile("audio/x-mpegurl", fileName)
                        if (newFile != null) {
                            val outputStream = context.contentResolver.openOutputStream(newFile.uri)
                            if (outputStream != null) {
                                exportPlaylistM3UUseCase(playlist.id, outputStream).getOrThrow()
                                successCount++
                            } else {
                                failures.add(playlist.name)
                            }
                        } else {
                            failures.add(playlist.name)
                        }
                    } catch (e: Exception) {
                        failures.add(playlist.name)
                    }
                }

                val message = if (failures.isEmpty()) {
                    "Successfully exported $successCount playlists"
                } else {
                    "Exported $successCount playlists. Failed: ${failures.joinToString(", ")}"
                }

                _dialogState.update {
                    it.copy(
                        showExportResultDialog = true,
                        exportResult = ExportResultData(
                            success = failures.isEmpty(),
                            message = message,
                            playlistCount = successCount
                        )
                    )
                }
            } catch (e: Exception) {
                _dialogState.update {
                    it.copy(
                        showExportResultDialog = true,
                        exportResult = ExportResultData(
                            success = false,
                            message = "Export failed: ${e.message}"
                        )
                    )
                }
            } finally {
                _isExporting.value = false
            }
        }
    }

    fun importPlaylist(fileUri: Uri) {
        viewModelScope.launch {
            _isImporting.value = true
            try {
                val inputStream = context.contentResolver.openInputStream(fileUri)
                if (inputStream == null) {
                    _dialogState.update {
                        it.copy(
                            showImportResultDialog = true,
                            importResult = ImportResultData(
                                success = false,
                                message = "Could not open file"
                            )
                        )
                    }
                    return@launch
                }

                val result = importPlaylistM3UUseCase(inputStream).getOrThrow()

                val message = if (result.unmatchedCount == 0) {
                    "Imported \"${result.playlistName}\" with ${result.matchedTracks} tracks"
                } else {
                    "Imported \"${result.playlistName}\": ${result.matchedTracks}/${result.totalTracks} tracks matched"
                }

                _dialogState.update {
                    it.copy(
                        showImportResultDialog = true,
                        importResult = ImportResultData(
                            success = true,
                            message = message,
                            playlistName = result.playlistName,
                            matchedTracks = result.matchedTracks,
                            totalTracks = result.totalTracks
                        )
                    )
                }
            } catch (e: Exception) {
                _dialogState.update {
                    it.copy(
                        showImportResultDialog = true,
                        importResult = ImportResultData(
                            success = false,
                            message = "Import failed: ${e.message}"
                        )
                    )
                }
            } finally {
                _isImporting.value = false
            }
        }
    }

    fun dismissExportResultDialog() {
        _dialogState.update { it.copy(showExportResultDialog = false, exportResult = null) }
    }

    fun dismissImportResultDialog() {
        _dialogState.update { it.copy(showImportResultDialog = false, importResult = null) }
    }

    private fun sanitizeFileName(name: String): String {
        return name.replace(Regex("[^a-zA-Z0-9._\\- ]"), "_")
    }
}
