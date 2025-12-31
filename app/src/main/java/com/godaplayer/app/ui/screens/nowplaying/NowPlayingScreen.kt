package com.godaplayer.app.ui.screens.nowplaying

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.godaplayer.app.domain.model.RepeatMode
import com.godaplayer.app.ui.components.RetroProgressBar
import com.godaplayer.app.ui.components.RetroTextButton
import com.godaplayer.app.ui.components.ScreenHeader
import com.godaplayer.app.ui.components.SectionHeader
import com.godaplayer.app.ui.components.SongInfoSheet
import com.godaplayer.app.ui.components.SongListItem
import com.godaplayer.app.ui.components.formatDuration
import com.godaplayer.app.ui.screens.playlists.AddToPlaylistDialog
import com.godaplayer.app.ui.screens.playlists.CreatePlaylistDialog
import com.godaplayer.app.ui.theme.GodaColors

@Composable
fun NowPlayingScreen(
    onNavigateBack: () -> Unit,
    onNavigateToQueue: () -> Unit,
    onNavigateToEqualizer: () -> Unit,
    viewModel: NowPlayingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val menuState by viewModel.menuState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GodaColors.PrimaryBackground)
    ) {
        ScreenHeader(
            title = "NOW PLAYING",
            onBackClick = onNavigateBack,
            trailing = {
                Row {
                    IconButton(onClick = viewModel::showAddToPlaylistDialog) {
                        Icon(
                            imageVector = Icons.Default.PlaylistAdd,
                            contentDescription = "Add to Playlist",
                            tint = GodaColors.SecondaryText
                        )
                    }
                    IconButton(onClick = onNavigateToQueue) {
                        Icon(
                            imageVector = Icons.Default.QueueMusic,
                            contentDescription = "Queue",
                            tint = GodaColors.SecondaryText
                        )
                    }
                    IconButton(onClick = onNavigateToEqualizer) {
                        Icon(
                            imageVector = Icons.Default.Equalizer,
                            contentDescription = "Equalizer",
                            tint = GodaColors.SecondaryText
                        )
                    }
                }
            }
        )

        if (uiState.currentSong == null) {
            EmptyState()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                // Album art
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .background(GodaColors.TertiaryBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "♪",
                        style = MaterialTheme.typography.headlineLarge,
                        color = GodaColors.PrimaryAccent,
                        fontSize = MaterialTheme.typography.headlineLarge.fontSize * 4
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Song info
                Text(
                    text = uiState.currentSong?.displayTitle ?: "",
                    style = MaterialTheme.typography.headlineLarge,
                    color = GodaColors.PrimaryText,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                val artistAlbum = buildString {
                    uiState.currentSong?.artist?.let { append(it) }
                    uiState.currentSong?.album?.let {
                        if (isNotEmpty()) append(" — ")
                        append(it)
                    }
                }
                if (artistAlbum.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = artistAlbum,
                        style = MaterialTheme.typography.bodyMedium,
                        color = GodaColors.SecondaryText,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Progress
                RetroProgressBar(
                    progress = uiState.progress,
                    currentTime = formatDuration(uiState.currentPositionMs),
                    totalTime = formatDuration(uiState.durationMs),
                    onSeek = viewModel::seekToProgress
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = viewModel::toggleShuffle) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Shuffle",
                            tint = if (uiState.shuffleEnabled) GodaColors.PrimaryAccent else GodaColors.SecondaryText,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    IconButton(onClick = viewModel::skipToPrevious) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous",
                            tint = GodaColors.PrimaryText,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    IconButton(
                        onClick = viewModel::togglePlayPause,
                        modifier = Modifier.size(72.dp)
                    ) {
                        Icon(
                            imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                            tint = GodaColors.PrimaryAccent,
                            modifier = Modifier.size(56.dp)
                        )
                    }

                    IconButton(onClick = viewModel::skipToNext) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next",
                            tint = GodaColors.PrimaryText,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    IconButton(onClick = viewModel::cycleRepeatMode) {
                        Icon(
                            imageVector = when (uiState.repeatMode) {
                                RepeatMode.ONE -> Icons.Default.RepeatOne
                                else -> Icons.Default.Repeat
                            },
                            contentDescription = "Repeat",
                            tint = when (uiState.repeatMode) {
                                RepeatMode.OFF -> GodaColors.SecondaryText
                                else -> GodaColors.PrimaryAccent
                            },
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // EQ Button
                RetroTextButton(
                    text = "EQ",
                    onClick = onNavigateToEqualizer
                )

                // Up Next
                if (uiState.upNext.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    SectionHeader(title = "UP NEXT")
                    LazyColumn(
                        modifier = Modifier.weight(1f)
                    ) {
                        items(uiState.upNext) { song ->
                            SongListItem(
                                title = song.displayTitle,
                                artist = song.artist,
                                duration = song.formattedDuration
                            )
                        }
                    }
                }
            }
        }
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
    if (menuState.showSongInfoSheet && uiState.currentSong != null) {
        SongInfoSheet(
            song = uiState.currentSong!!,
            playlists = menuState.songPlaylists,
            onDismiss = viewModel::dismissSongInfo
        )
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No track selected",
            style = MaterialTheme.typography.headlineMedium,
            color = GodaColors.SecondaryText
        )
    }
}
