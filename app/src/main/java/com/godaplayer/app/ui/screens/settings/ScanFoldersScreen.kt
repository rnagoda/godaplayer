package com.godaplayer.app.ui.screens.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import androidx.hilt.navigation.compose.hiltViewModel
import com.godaplayer.app.domain.model.ScanFolder
import com.godaplayer.app.ui.components.ScreenHeader
import com.godaplayer.app.ui.theme.GodaColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ScanFoldersScreen(
    onNavigateBack: () -> Unit,
    viewModel: ScanFoldersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val dialogState by viewModel.dialogState.collectAsState()
    val context = LocalContext.current

    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let {
            // Take persistent permission
            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, takeFlags)

            // Get the actual path
            val documentFile = DocumentFile.fromTreeUri(context, uri)
            val path = documentFile?.uri?.toString() ?: uri.toString()
            viewModel.addFolder(path)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GodaColors.PrimaryBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            ScreenHeader(
                title = "SCAN FOLDERS",
                onBackClick = onNavigateBack
            )

            if (uiState.folders.isEmpty() && !uiState.isLoading) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No folders configured",
                            style = MaterialTheme.typography.titleMedium,
                            color = GodaColors.SecondaryText
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap + to add a folder to scan for music files",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GodaColors.SecondaryText
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = uiState.folders,
                        key = { it.id }
                    ) { folder ->
                        ScanFolderItem(
                            folder = folder,
                            onToggleEnabled = { viewModel.toggleFolderEnabled(folder) },
                            onDelete = { viewModel.showDeleteDialog(folder) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp)) // Space for FAB
                    }
                }
            }
        }

        // FAB for adding folders
        FloatingActionButton(
            onClick = { folderPickerLauncher.launch(null) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = GodaColors.PrimaryAccent,
            contentColor = GodaColors.PrimaryBackground
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add folder"
            )
        }
    }

    // Delete confirmation dialog
    if (dialogState.showDeleteDialog && dialogState.folderToDelete != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteDialog() },
            containerColor = GodaColors.SecondaryBackground,
            titleContentColor = GodaColors.PrimaryText,
            textContentColor = GodaColors.SecondaryText,
            title = {
                Text(
                    text = "Remove Folder",
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Text(
                    text = "Remove \"${dialogState.folderToDelete?.displayName}\" from scan folders? Songs from this folder will remain in your library until the next scan.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDeleteFolder() }) {
                    Text(
                        text = "Remove",
                        color = GodaColors.Error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteDialog() }) {
                    Text(
                        text = "Cancel",
                        color = GodaColors.SecondaryText
                    )
                }
            }
        )
    }
}

@Composable
private fun ScanFolderItem(
    folder: ScanFolder,
    onToggleEnabled: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggleEnabled)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = folder.displayName,
                style = MaterialTheme.typography.bodyLarge,
                color = if (folder.enabled) GodaColors.PrimaryText else GodaColors.SecondaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = formatPath(folder.path),
                style = MaterialTheme.typography.bodySmall,
                color = GodaColors.SecondaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            folder.lastScanned?.let { timestamp ->
                Text(
                    text = "Last scanned: ${formatTimestamp(timestamp)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = GodaColors.SecondaryText
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = folder.enabled,
                onCheckedChange = { onToggleEnabled() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = GodaColors.PrimaryAccent,
                    checkedTrackColor = GodaColors.SecondaryAccent,
                    uncheckedThumbColor = GodaColors.SecondaryText,
                    uncheckedTrackColor = GodaColors.TertiaryBackground
                )
            )
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete folder",
                    tint = GodaColors.SecondaryText
                )
            }
        }
    }
}

private fun formatPath(path: String): String {
    // Try to make content:// URIs more readable
    return if (path.startsWith("content://")) {
        path.substringAfter("tree/")
            .replace("%3A", ":")
            .replace("%2F", "/")
    } else {
        path
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val formatter = SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault())
    return formatter.format(Date(timestamp))
}
