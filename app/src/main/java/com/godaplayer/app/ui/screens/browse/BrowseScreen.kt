package com.godaplayer.app.ui.screens.browse

import android.Manifest
import android.os.Build
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.godaplayer.app.ui.components.FileListItem
import com.godaplayer.app.ui.components.RetroButton
import com.godaplayer.app.ui.components.ScreenHeader
import com.godaplayer.app.ui.components.SectionHeader
import com.godaplayer.app.ui.theme.GodaColors

@Composable
fun BrowseScreen(
    onNavigateBack: () -> Unit,
    viewModel: BrowseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onPermissionResult(isGranted)
    }

    LaunchedEffect(Unit) {
        if (!uiState.hasPermission) {
            permissionLauncher.launch(permission)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GodaColors.PrimaryBackground)
    ) {
        ScreenHeader(
            title = "BROWSE FILES",
            onBackClick = if (!uiState.showQuickAccess && uiState.canNavigateUp) {
                { viewModel.navigateUp() }
            } else null
        )

        // Current path (only show when not in quick access)
        if (!uiState.showQuickAccess && uiState.currentPath.isNotEmpty()) {
            Text(
                text = uiState.currentPath,
                style = MaterialTheme.typography.bodySmall,
                color = GodaColors.SecondaryText,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GodaColors.SecondaryBackground)
                    .clickable { viewModel.showQuickAccess() }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        when {
            !uiState.hasPermission -> {
                PermissionRequiredContent(
                    onRequestPermission = { permissionLauncher.launch(permission) }
                )
            }
            uiState.showQuickAccess -> {
                QuickAccessContent(
                    locations = uiState.quickAccessLocations,
                    onLocationClick = { viewModel.navigateToFolder(it.path) }
                )
            }
            uiState.isLoading -> {
                LoadingContent()
            }
            uiState.files.isEmpty() -> {
                EmptyFolderContent(
                    onGoBack = { viewModel.navigateUp() }
                )
            }
            else -> {
                FileListContent(
                    uiState = uiState,
                    onFileClick = { file ->
                        if (file.isDirectory) {
                            viewModel.navigateToFolder(file.path)
                        } else {
                            viewModel.playFile(file)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun QuickAccessContent(
    locations: List<QuickAccessLocation>,
    onLocationClick: (QuickAccessLocation) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        SectionHeader(title = "QUICK ACCESS")

        LazyColumn {
            items(locations) { location ->
                QuickAccessItem(
                    location = location,
                    onClick = { onLocationClick(location) }
                )
            }
        }
    }
}

@Composable
private fun QuickAccessItem(
    location: QuickAccessLocation,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = location.icon,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(end = 16.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = location.name,
                style = MaterialTheme.typography.bodyLarge,
                color = GodaColors.PrimaryText
            )
            Text(
                text = location.path,
                style = MaterialTheme.typography.bodySmall,
                color = GodaColors.SecondaryText
            )
        }

        Text(
            text = "▶",
            style = MaterialTheme.typography.bodyLarge,
            color = GodaColors.SecondaryText
        )
    }
}

@Composable
private fun PermissionRequiredContent(
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "STORAGE ACCESS REQUIRED",
            style = MaterialTheme.typography.headlineMedium,
            color = GodaColors.PrimaryText,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "GodaPlayer needs permission to access your music files.",
            style = MaterialTheme.typography.bodyMedium,
            color = GodaColors.SecondaryText,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = """
                This app:
                  • Reads audio files from your device
                  • Never uploads or shares your files
                  • Works completely offline
            """.trimIndent(),
            style = MaterialTheme.typography.bodySmall,
            color = GodaColors.SecondaryText,
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.height(24.dp))

        RetroButton(
            text = "GRANT ACCESS",
            onClick = onRequestPermission,
            isPrimary = true
        )
    }
}

@Composable
private fun LoadingContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Loading...",
            style = MaterialTheme.typography.bodyMedium,
            color = GodaColors.SecondaryText
        )
    }
}

@Composable
private fun EmptyFolderContent(
    onGoBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "📁",
            style = MaterialTheme.typography.headlineLarge,
            fontSize = MaterialTheme.typography.headlineLarge.fontSize * 2
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "NO AUDIO FILES",
            style = MaterialTheme.typography.headlineMedium,
            color = GodaColors.SecondaryText,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "This folder contains no audio files",
            style = MaterialTheme.typography.bodyMedium,
            color = GodaColors.DisabledText,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        RetroButton(
            text = "GO BACK",
            onClick = onGoBack
        )
    }
}

@Composable
private fun FileListContent(
    uiState: BrowseUiState,
    onFileClick: (com.godaplayer.app.domain.model.FileItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        // Directories first
        val directories = uiState.files.filter { it.isDirectory }
        val audioFiles = uiState.files.filter { !it.isDirectory }

        items(directories) { file ->
            FileListItem(
                name = file.name,
                isDirectory = true,
                onClick = { onFileClick(file) }
            )
        }

        if (directories.isNotEmpty() && audioFiles.isNotEmpty()) {
            item {
                HorizontalDivider(
                    color = GodaColors.DividerColor,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        items(audioFiles) { file ->
            FileListItem(
                name = file.name,
                isDirectory = false,
                duration = file.formattedDuration,
                onClick = { onFileClick(file) }
            )
        }
    }
}
