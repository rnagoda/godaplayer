package com.godaplayer.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.godaplayer.app.domain.model.ScanFolder
import com.godaplayer.app.domain.repository.ScanFolderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScanFoldersUiState(
    val folders: List<ScanFolder> = emptyList(),
    val isLoading: Boolean = false
)

data class ScanFoldersDialogState(
    val showDeleteDialog: Boolean = false,
    val folderToDelete: ScanFolder? = null
)

@HiltViewModel
class ScanFoldersViewModel @Inject constructor(
    private val scanFolderRepository: ScanFolderRepository
) : ViewModel() {

    private val _dialogState = MutableStateFlow(ScanFoldersDialogState())
    val dialogState: StateFlow<ScanFoldersDialogState> = _dialogState.asStateFlow()

    val uiState: StateFlow<ScanFoldersUiState> = scanFolderRepository.getAllFolders()
        .map { folders -> ScanFoldersUiState(folders = folders) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ScanFoldersUiState(isLoading = true)
        )

    fun addFolder(path: String) {
        viewModelScope.launch {
            // Check if folder already exists
            val existingFolder = scanFolderRepository.getFolderByPath(path)
            if (existingFolder == null) {
                scanFolderRepository.addFolder(path)
            }
        }
    }

    fun toggleFolderEnabled(folder: ScanFolder) {
        viewModelScope.launch {
            scanFolderRepository.setFolderEnabled(folder.id, !folder.enabled)
        }
    }

    fun showDeleteDialog(folder: ScanFolder) {
        _dialogState.update { it.copy(showDeleteDialog = true, folderToDelete = folder) }
    }

    fun dismissDeleteDialog() {
        _dialogState.update { it.copy(showDeleteDialog = false, folderToDelete = null) }
    }

    fun confirmDeleteFolder() {
        val folder = _dialogState.value.folderToDelete ?: return
        viewModelScope.launch {
            scanFolderRepository.deleteFolder(folder.id)
            dismissDeleteDialog()
        }
    }
}
