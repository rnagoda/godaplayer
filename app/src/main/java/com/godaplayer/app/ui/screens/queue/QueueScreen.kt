package com.godaplayer.app.ui.screens.queue

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.godaplayer.app.ui.components.QueueSongContextMenu
import com.godaplayer.app.ui.components.RetroTextButton
import com.godaplayer.app.ui.components.ScreenHeader
import com.godaplayer.app.ui.components.SectionHeader
import com.godaplayer.app.ui.components.SongInfoSheet
import com.godaplayer.app.ui.components.SongListItem
import com.godaplayer.app.ui.screens.playlists.AddToPlaylistDialog
import com.godaplayer.app.ui.screens.playlists.CreatePlaylistDialog
import com.godaplayer.app.ui.theme.GodaColors

@Composable
fun QueueScreen(
    onNavigateBack: () -> Unit,
    viewModel: QueueViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val menuState by viewModel.menuState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GodaColors.PrimaryBackground)
    ) {
        ScreenHeader(
            title = "PLAY QUEUE",
            onBackClick = onNavigateBack,
            trailing = {
                if (uiState.queue.isNotEmpty()) {
                    RetroTextButton(
                        text = "CLEAR",
                        onClick = viewModel::clearQueue
                    )
                }
            }
        )

        if (uiState.currentSong != null) {
            SectionHeader(title = "NOW PLAYING")
            SongListItem(
                title = uiState.currentSong!!.displayTitle,
                artist = uiState.currentSong!!.artist,
                duration = uiState.currentSong!!.formattedDuration,
                isPlaying = true,
                onMenuClick = {
                    viewModel.showContextMenu(uiState.currentSong!!, uiState.currentIndex)
                }
            )
        }

        val upNextSongs = uiState.queue.drop(uiState.currentIndex + 1)
        if (upNextSongs.isNotEmpty()) {
            SectionHeader(title = "UP NEXT", count = upNextSongs.size)

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 16.dp)
            ) {
                itemsIndexed(upNextSongs) { index, song ->
                    val actualIndex = uiState.currentIndex + 1 + index
                    SongListItem(
                        title = song.displayTitle,
                        artist = song.artist,
                        duration = song.formattedDuration,
                        showDragHandle = true,
                        onClick = { viewModel.playAtIndex(actualIndex) },
                        onMenuClick = { viewModel.showContextMenu(song, actualIndex) }
                    )
                }
            }
        }

        // Save as playlist button
        if (uiState.queue.isNotEmpty()) {
            RetroTextButton(
                text = "SAVE AS PLAYLIST",
                onClick = viewModel::showSaveQueueDialog,
                modifier = Modifier.padding(16.dp)
            )
        }
    }

    // Context menu dialog
    if (menuState.showContextMenu && menuState.selectedSong != null) {
        QueueSongContextMenu(
            song = menuState.selectedSong!!,
            onDismiss = viewModel::dismissContextMenu,
            onAddToPlaylist = viewModel::showAddToPlaylistDialog,
            onRemoveFromQueue = viewModel::removeSelectedFromQueue,
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

    // Create playlist dialog (from add to playlist flow)
    if (menuState.showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            onDismiss = viewModel::dismissCreatePlaylistDialog,
            onCreate = viewModel::createPlaylistAndAddSong
        )
    }

    // Save queue as playlist dialog
    if (menuState.showSaveQueueDialog) {
        CreatePlaylistDialog(
            onDismiss = viewModel::dismissSaveQueueDialog,
            onCreate = viewModel::saveQueueAsPlaylist,
            title = "SAVE QUEUE AS PLAYLIST"
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
}
