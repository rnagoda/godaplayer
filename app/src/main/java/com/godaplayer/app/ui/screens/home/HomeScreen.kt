package com.godaplayer.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
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
import com.godaplayer.app.ui.components.QueueSongContextMenu
import com.godaplayer.app.ui.components.RetroProgressBar
import com.godaplayer.app.ui.components.SectionHeader
import com.godaplayer.app.ui.components.SongInfoSheet
import com.godaplayer.app.ui.components.SongListItem
import com.godaplayer.app.ui.components.formatDuration
import com.godaplayer.app.ui.screens.playlists.AddToPlaylistDialog
import com.godaplayer.app.ui.screens.playlists.CreatePlaylistDialog
import com.godaplayer.app.ui.theme.GodaColors

@Composable
fun HomeScreen(
    onNavigateToNowPlaying: () -> Unit,
    onNavigateToLibrary: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val menuState by viewModel.menuState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GodaColors.PrimaryBackground)
    ) {
        // Now Playing section (main focus of home screen)
        if (uiState.currentSong != null) {
            NowPlayingSection(
                uiState = uiState,
                onPlayPauseClick = viewModel::togglePlayPause,
                onPreviousClick = viewModel::skipToPrevious,
                onNextClick = viewModel::skipToNext,
                onShuffleClick = viewModel::toggleShuffle,
                onRepeatClick = viewModel::cycleRepeatMode,
                onSeek = viewModel::seekToProgress,
                onExpandClick = onNavigateToNowPlaying,
                modifier = Modifier.weight(1f)
            )
        } else {
            EmptyStateSection(
                onBrowseClick = onNavigateToLibrary,
                modifier = Modifier.weight(1f)
            )
        }

        // Up Next section
        if (uiState.upNext.isNotEmpty()) {
            SectionHeader(title = "UP NEXT", count = uiState.upNext.size)
            LazyColumn(
                modifier = Modifier.height(180.dp)
            ) {
                itemsIndexed(uiState.upNext) { index, song ->
                    val actualIndex = uiState.currentIndex + 1 + index
                    SongListItem(
                        title = song.displayTitle,
                        artist = song.artist,
                        duration = song.formattedDuration,
                        onClick = { /* Play this song */ },
                        onMenuClick = { viewModel.showContextMenu(song, actualIndex) }
                    )
                }
            }
        }
    }

    // Context menu dialog
    if (menuState.showContextMenu && menuState.selectedSong != null) {
        QueueSongContextMenu(
            song = menuState.selectedSong!!,
            onDismiss = viewModel::dismissContextMenu,
            onAddToPlaylist = viewModel::showAddToPlaylistDialog,
            onRemoveFromQueue = viewModel::removeFromQueue,
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
}

@Composable
private fun NowPlayingSection(
    uiState: HomeUiState,
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onShuffleClick: () -> Unit,
    onRepeatClick: () -> Unit,
    onSeek: (Float) -> Unit,
    onExpandClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onExpandClick)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Album art placeholder
        Box(
            modifier = Modifier
                .size(200.dp)
                .aspectRatio(1f)
                .background(GodaColors.TertiaryBackground),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "♪",
                style = MaterialTheme.typography.headlineLarge,
                color = GodaColors.PrimaryAccent,
                fontSize = MaterialTheme.typography.headlineLarge.fontSize * 3
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Song title
        Text(
            text = uiState.currentSong?.displayTitle ?: "",
            style = MaterialTheme.typography.headlineLarge,
            color = GodaColors.PrimaryText,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        // Artist - Album
        val artistAlbum = buildString {
            uiState.currentSong?.artist?.let { append(it) }
            uiState.currentSong?.album?.let {
                if (isNotEmpty()) append(" — ")
                append(it)
            }
        }
        if (artistAlbum.isNotEmpty()) {
            Text(
                text = artistAlbum,
                style = MaterialTheme.typography.bodyMedium,
                color = GodaColors.SecondaryText,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Progress bar
        RetroProgressBar(
            progress = uiState.progress,
            currentTime = formatDuration(uiState.currentPositionMs),
            totalTime = formatDuration(uiState.durationMs),
            onSeek = onSeek
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shuffle
            IconButton(onClick = onShuffleClick) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = "Shuffle",
                    tint = if (uiState.shuffleEnabled) GodaColors.PrimaryAccent else GodaColors.SecondaryText,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Previous
            IconButton(onClick = onPreviousClick) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous",
                    tint = GodaColors.PrimaryText,
                    modifier = Modifier.size(36.dp)
                )
            }

            // Play/Pause
            IconButton(
                onClick = onPlayPauseClick,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                    tint = GodaColors.PrimaryAccent,
                    modifier = Modifier.size(48.dp)
                )
            }

            // Next
            IconButton(onClick = onNextClick) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next",
                    tint = GodaColors.PrimaryText,
                    modifier = Modifier.size(36.dp)
                )
            }

            // Repeat
            IconButton(onClick = onRepeatClick) {
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
    }
}

@Composable
private fun EmptyStateSection(
    onBrowseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "♪",
            style = MaterialTheme.typography.headlineLarge,
            color = GodaColors.DisabledText,
            fontSize = MaterialTheme.typography.headlineLarge.fontSize * 4
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "NO MUSIC PLAYING",
            style = MaterialTheme.typography.headlineMedium,
            color = GodaColors.SecondaryText,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Browse your files or library to start playing",
            style = MaterialTheme.typography.bodyMedium,
            color = GodaColors.DisabledText,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "[ BROWSE FILES ]",
            style = MaterialTheme.typography.labelLarge,
            color = GodaColors.PrimaryAccent,
            modifier = Modifier.clickable(onClick = onBrowseClick)
        )
    }
}
