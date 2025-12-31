package com.godaplayer.app.ui.screens.playlists

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.godaplayer.app.domain.model.Song
import com.godaplayer.app.ui.components.PlaylistSongContextMenu
import com.godaplayer.app.ui.components.RetroButton
import com.godaplayer.app.ui.components.ScreenHeader
import com.godaplayer.app.ui.components.SongInfoSheet
import com.godaplayer.app.ui.theme.GodaColors

@Composable
fun PlaylistDetailScreen(
    playlistId: Long,
    onNavigateBack: () -> Unit,
    viewModel: PlaylistDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val menuState by viewModel.menuState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GodaColors.PrimaryBackground)
    ) {
        ScreenHeader(
            title = uiState.playlist?.name?.uppercase() ?: "PLAYLIST",
            onBackClick = onNavigateBack
        )

        if (uiState.isLoading) {
            LoadingContent()
        } else if (uiState.songs.isEmpty()) {
            EmptyPlaylistContent()
        } else {
            PlaylistContent(
                playlist = uiState.playlist,
                songs = uiState.songs,
                onPlayAll = { viewModel.playPlaylist() },
                onShuffle = viewModel::shufflePlaylist,
                onSongClick = viewModel::playSong,
                onSongMenuClick = viewModel::showContextMenu
            )
        }
    }

    // Context menu dialog
    if (menuState.showContextMenu && menuState.selectedSong != null) {
        PlaylistSongContextMenu(
            song = menuState.selectedSong!!,
            onDismiss = viewModel::dismissContextMenu,
            onAddToPlaylist = viewModel::showAddToPlaylistDialog,
            onPlayNext = viewModel::playNext,
            onAddToQueue = viewModel::addToQueue,
            onRemoveFromPlaylist = viewModel::removeSelectedFromPlaylist,
            onShowInfo = viewModel::showSongInfo
        )
    }

    // Add to playlist dialog
    if (menuState.showAddToPlaylistDialog) {
        AddToPlaylistDialog(
            playlists = menuState.allPlaylists,
            existingPlaylistIds = menuState.existingPlaylistIds,
            onDismiss = viewModel::dismissAddToPlaylistDialog,
            onAddToPlaylists = viewModel::addToPlaylists,
            onCreateNewPlaylist = viewModel::showCreatePlaylistDialog
        )
    }

    // Create playlist dialog
    if (menuState.showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            onDismiss = viewModel::dismissCreatePlaylistDialog,
            onCreate = viewModel::createPlaylistAndAddSong
        )
    }

    // Song info sheet
    if (menuState.showSongInfoSheet && menuState.selectedSong != null) {
        SongInfoSheet(
            song = menuState.selectedSong!!,
            playlists = menuState.songPlaylists,
            onDismiss = viewModel::dismissSongInfo
        )
    }

    // Delete song confirmation dialog
    if (uiState.showDeleteSongDialog && uiState.songToDelete != null) {
        RemoveSongDialog(
            songTitle = uiState.songToDelete!!.displayTitle,
            playlistName = uiState.playlist?.name ?: "playlist",
            onDismiss = viewModel::hideDeleteSongDialog,
            onConfirm = viewModel::removeSong
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
private fun EmptyPlaylistContent() {
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
            text = "PLAYLIST EMPTY",
            style = MaterialTheme.typography.headlineMedium,
            color = GodaColors.SecondaryText,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Add songs from your library or file browser",
            style = MaterialTheme.typography.bodyMedium,
            color = GodaColors.DisabledText,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PlaylistContent(
    playlist: com.godaplayer.app.domain.model.Playlist?,
    songs: List<Song>,
    onPlayAll: () -> Unit,
    onShuffle: () -> Unit,
    onSongClick: (Song) -> Unit,
    onSongMenuClick: (Song) -> Unit
) {
    Column {
        // Playlist info and controls
        PlaylistHeader(
            songCount = songs.size,
            totalDuration = playlist?.formattedDuration ?: "",
            onPlayAll = onPlayAll,
            onShuffle = onShuffle
        )

        // Songs list
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(songs, key = { _, song -> song.id }) { index, song ->
                PlaylistSongItem(
                    index = index + 1,
                    song = song,
                    onClick = { onSongClick(song) },
                    onMenuClick = { onSongMenuClick(song) }
                )
            }
        }
    }
}

@Composable
private fun PlaylistHeader(
    songCount: Int,
    totalDuration: String,
    onPlayAll: () -> Unit,
    onShuffle: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(GodaColors.SecondaryBackground)
            .padding(16.dp)
    ) {
        Text(
            text = "$songCount songs • $totalDuration",
            style = MaterialTheme.typography.bodyMedium,
            color = GodaColors.SecondaryText
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RetroButton(
                text = "PLAY ALL",
                onClick = onPlayAll,
                isPrimary = true,
                modifier = Modifier.weight(1f)
            )
            RetroButton(
                text = "SHUFFLE",
                onClick = onShuffle,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PlaylistSongItem(
    index: Int,
    song: Song,
    onClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Track number
        Text(
            text = String.format("%02d", index),
            style = MaterialTheme.typography.bodyMedium,
            color = GodaColors.SecondaryText,
            modifier = Modifier.width(32.dp)
        )

        // Song info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.displayTitle,
                style = MaterialTheme.typography.bodyLarge,
                color = GodaColors.PrimaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            song.artist?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = GodaColors.SecondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Duration
        Text(
            text = song.formattedDuration,
            style = MaterialTheme.typography.bodySmall,
            color = GodaColors.SecondaryText,
            modifier = Modifier.padding(end = 8.dp)
        )

        // Menu button
        Text(
            text = "⋮",
            style = MaterialTheme.typography.titleLarge,
            color = GodaColors.SecondaryText,
            modifier = Modifier
                .clickable(onClick = onMenuClick)
                .padding(8.dp)
        )
    }
}

@Composable
private fun RemoveSongDialog(
    songTitle: String,
    playlistName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = GodaColors.SecondaryBackground,
            modifier = Modifier.border(1.dp, GodaColors.BorderColor, RoundedCornerShape(4.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "REMOVE SONG",
                    style = MaterialTheme.typography.headlineSmall,
                    color = GodaColors.Error
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Remove \"$songTitle\" from $playlistName?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GodaColors.PrimaryText
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RetroButton(
                        text = "CANCEL",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    RetroButton(
                        text = "REMOVE",
                        onClick = onConfirm,
                        isPrimary = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
