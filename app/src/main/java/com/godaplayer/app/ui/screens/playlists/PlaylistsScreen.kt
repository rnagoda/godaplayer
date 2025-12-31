package com.godaplayer.app.ui.screens.playlists

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.godaplayer.app.domain.model.Playlist
import com.godaplayer.app.ui.components.RetroTextButton
import com.godaplayer.app.ui.components.ScreenHeader
import com.godaplayer.app.ui.theme.GodaColors

@Composable
fun PlaylistsScreen(
    onNavigateToPlaylist: (Long) -> Unit,
    viewModel: PlaylistsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GodaColors.PrimaryBackground)
    ) {
        ScreenHeader(
            title = "PLAYLISTS",
            trailing = {
                RetroTextButton(
                    text = "+ NEW",
                    onClick = viewModel::showCreateDialog
                )
            }
        )

        if (uiState.playlists.isEmpty()) {
            EmptyPlaylistsContent(
                onCreateClick = viewModel::showCreateDialog
            )
        } else {
            PlaylistListContent(
                playlists = uiState.playlists,
                onPlaylistClick = onNavigateToPlaylist,
                onRenameClick = viewModel::showRenameDialog,
                onDeleteClick = viewModel::showDeleteDialog
            )
        }
    }

    // Dialogs
    if (uiState.showCreateDialog) {
        CreatePlaylistDialog(
            onDismiss = viewModel::hideCreateDialog,
            onCreate = viewModel::createPlaylist
        )
    }

    if (uiState.showRenameDialog && uiState.selectedPlaylist != null) {
        RenamePlaylistDialog(
            currentName = uiState.selectedPlaylist!!.name,
            onDismiss = viewModel::hideRenameDialog,
            onRename = viewModel::renamePlaylist
        )
    }

    if (uiState.showDeleteDialog && uiState.selectedPlaylist != null) {
        DeletePlaylistDialog(
            playlistName = uiState.selectedPlaylist!!.name,
            onDismiss = viewModel::hideDeleteDialog,
            onConfirm = viewModel::deletePlaylist
        )
    }
}

@Composable
private fun EmptyPlaylistsContent(
    onCreateClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "♪",
            style = MaterialTheme.typography.headlineLarge,
            color = GodaColors.DisabledText,
            fontSize = MaterialTheme.typography.headlineLarge.fontSize * 3
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "NO PLAYLISTS YET",
            style = MaterialTheme.typography.headlineMedium,
            color = GodaColors.SecondaryText,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Create a playlist to organize your music",
            style = MaterialTheme.typography.bodyMedium,
            color = GodaColors.DisabledText,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        RetroTextButton(
            text = "CREATE PLAYLIST",
            onClick = onCreateClick
        )
    }
}

@Composable
private fun PlaylistListContent(
    playlists: List<Playlist>,
    onPlaylistClick: (Long) -> Unit,
    onRenameClick: (Playlist) -> Unit,
    onDeleteClick: (Playlist) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(playlists, key = { it.id }) { playlist ->
            PlaylistCard(
                playlist = playlist,
                onClick = { onPlaylistClick(playlist.id) },
                onRenameClick = { onRenameClick(playlist) },
                onDeleteClick = { onDeleteClick(playlist) }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlaylistCard(
    playlist: Playlist,
    onClick: () -> Unit,
    onRenameClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showMenu = true }
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = GodaColors.SecondaryBackground,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = GodaColors.PrimaryText
                )
                Text(
                    text = "${playlist.songCount} songs • ${playlist.formattedDuration}",
                    style = MaterialTheme.typography.bodySmall,
                    color = GodaColors.SecondaryText
                )
            }

            Text(
                text = "▶",
                style = MaterialTheme.typography.titleMedium,
                color = GodaColors.SecondaryText
            )

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(GodaColors.SecondaryBackground)
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Rename",
                            color = GodaColors.PrimaryText
                        )
                    },
                    onClick = {
                        showMenu = false
                        onRenameClick()
                    }
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Delete",
                            color = GodaColors.Error
                        )
                    },
                    onClick = {
                        showMenu = false
                        onDeleteClick()
                    }
                )
            }
        }
    }
}
