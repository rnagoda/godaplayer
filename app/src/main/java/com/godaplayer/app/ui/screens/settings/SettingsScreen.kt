package com.godaplayer.app.ui.screens.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.godaplayer.app.ui.components.RetroTextButton
import com.godaplayer.app.ui.components.ScreenHeader
import com.godaplayer.app.ui.components.SectionHeader
import com.godaplayer.app.ui.theme.GodaColors

@Composable
fun SettingsScreen(
    onNavigateToEqualizer: () -> Unit,
    onNavigateToScanFolders: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val dialogState by viewModel.dialogState.collectAsState()

    // Export folder picker
    val exportFolderPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let { viewModel.exportAllPlaylists(it) }
    }

    // Import file picker
    val importFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.importPlaylist(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GodaColors.PrimaryBackground)
    ) {
        ScreenHeader(title = "SETTINGS")

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item { SectionHeader(title = "PLAYBACK") }

            item {
                SettingsToggle(
                    title = "Gapless Playback",
                    checked = uiState.gaplessPlayback,
                    onCheckedChange = { viewModel.setGaplessPlayback(it) }
                )
            }

            item {
                SettingsToggle(
                    title = "Resume on App Start",
                    checked = uiState.resumeOnStart,
                    onCheckedChange = { viewModel.setResumeOnStart(it) }
                )
            }

            item { SectionHeader(title = "AUDIO") }

            item {
                SettingsNavItem(
                    title = "Equalizer",
                    onClick = onNavigateToEqualizer
                )
            }

            item { SectionHeader(title = "LIBRARY") }

            item {
                SettingsToggle(
                    title = "Auto-scan for new files",
                    checked = uiState.autoScan,
                    onCheckedChange = { viewModel.setAutoScan(it) }
                )
            }

            item {
                SettingsNavItem(
                    title = "Scan Folders",
                    onClick = onNavigateToScanFolders
                )
            }

            item {
                SettingsButtonWithLoading(
                    title = "Rescan Library Now",
                    buttonText = "SCAN",
                    isLoading = uiState.isScanning,
                    onClick = { viewModel.triggerRescan() }
                )
            }

            item { SectionHeader(title = "DISPLAY") }

            item {
                SettingsToggle(
                    title = "Show File Extensions",
                    checked = uiState.showFileExtensions,
                    onCheckedChange = { viewModel.setShowFileExtensions(it) }
                )
            }

            item { SectionHeader(title = "DATA") }

            item {
                SettingsButtonWithLoading(
                    title = "Export All Playlists",
                    buttonText = "EXPORT",
                    isLoading = uiState.isExporting,
                    onClick = { exportFolderPicker.launch(null) }
                )
            }

            item {
                SettingsButtonWithLoading(
                    title = "Import Playlists",
                    buttonText = "IMPORT",
                    isLoading = uiState.isImporting,
                    onClick = {
                        importFilePicker.launch(arrayOf(
                            "audio/x-mpegurl",
                            "audio/mpegurl",
                            "application/x-mpegurl",
                            "*/*" // Fallback for .m3u files
                        ))
                    }
                )
            }

            item {
                SettingsButton(
                    title = "Clear Play History",
                    buttonText = "CLEAR",
                    onClick = { viewModel.showClearHistoryDialog() }
                )
            }

            item { SectionHeader(title = "ABOUT") }

            item {
                SettingsInfo(
                    title = "Version",
                    value = "1.0.0"
                )
            }

            item {
                SettingsNavItem(
                    title = "Licenses",
                    onClick = { /* TODO */ }
                )
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }

    // Clear History Confirmation Dialog
    if (dialogState.showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissClearHistoryDialog() },
            containerColor = GodaColors.SecondaryBackground,
            titleContentColor = GodaColors.PrimaryText,
            textContentColor = GodaColors.SecondaryText,
            title = {
                Text(
                    text = "Clear Play History",
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Text(
                    text = "This will reset play counts and recently played history for all songs. This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.clearPlayHistory() }) {
                    Text(
                        text = "Clear",
                        color = GodaColors.Error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissClearHistoryDialog() }) {
                    Text(
                        text = "Cancel",
                        color = GodaColors.SecondaryText
                    )
                }
            }
        )
    }

    // Export Result Dialog
    if (dialogState.showExportResultDialog && dialogState.exportResult != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissExportResultDialog() },
            containerColor = GodaColors.SecondaryBackground,
            titleContentColor = GodaColors.PrimaryText,
            textContentColor = GodaColors.SecondaryText,
            title = {
                Text(
                    text = if (dialogState.exportResult!!.success) "Export Complete" else "Export Failed",
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Text(
                    text = dialogState.exportResult!!.message,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissExportResultDialog() }) {
                    Text(
                        text = "OK",
                        color = GodaColors.PrimaryAccent
                    )
                }
            }
        )
    }

    // Import Result Dialog
    if (dialogState.showImportResultDialog && dialogState.importResult != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissImportResultDialog() },
            containerColor = GodaColors.SecondaryBackground,
            titleContentColor = GodaColors.PrimaryText,
            textContentColor = GodaColors.SecondaryText,
            title = {
                Text(
                    text = if (dialogState.importResult!!.success) "Import Complete" else "Import Failed",
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Text(
                    text = dialogState.importResult!!.message,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissImportResultDialog() }) {
                    Text(
                        text = "OK",
                        color = GodaColors.PrimaryAccent
                    )
                }
            }
        )
    }
}

@Composable
fun SettingsToggle(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = GodaColors.PrimaryText
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = GodaColors.PrimaryAccent,
                checkedTrackColor = GodaColors.SecondaryAccent,
                uncheckedThumbColor = GodaColors.SecondaryText,
                uncheckedTrackColor = GodaColors.TertiaryBackground
            )
        )
    }
}

@Composable
fun SettingsNavItem(
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = GodaColors.PrimaryText
        )
        Text(
            text = "▶",
            style = MaterialTheme.typography.bodyLarge,
            color = GodaColors.SecondaryText
        )
    }
}

@Composable
fun SettingsButton(
    title: String,
    buttonText: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = GodaColors.PrimaryText
        )
        RetroTextButton(
            text = buttonText,
            onClick = onClick
        )
    }
}

@Composable
fun SettingsButtonWithLoading(
    title: String,
    buttonText: String,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = GodaColors.PrimaryText
        )
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = GodaColors.PrimaryAccent,
                strokeWidth = 2.dp
            )
        } else {
            RetroTextButton(
                text = buttonText,
                onClick = onClick
            )
        }
    }
}

@Composable
fun SettingsInfo(
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = GodaColors.PrimaryText
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = GodaColors.SecondaryText
        )
    }
}
