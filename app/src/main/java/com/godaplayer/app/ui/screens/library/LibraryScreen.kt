package com.godaplayer.app.ui.screens.library

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.godaplayer.app.domain.model.Song
import com.godaplayer.app.domain.repository.SongSortOrder
import com.godaplayer.app.ui.components.RetroTextButton
import com.godaplayer.app.ui.components.ScreenHeader
import com.godaplayer.app.ui.components.SongContextMenu
import com.godaplayer.app.ui.components.SongInfoSheet
import com.godaplayer.app.ui.components.SongListItem
import com.godaplayer.app.ui.screens.playlists.AddToPlaylistDialog
import com.godaplayer.app.ui.screens.playlists.CreatePlaylistDialog
import com.godaplayer.app.ui.theme.GodaColors

@Composable
fun LibraryScreen(
    onNavigateBack: () -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val menuState by viewModel.menuState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GodaColors.PrimaryBackground)
    ) {
        // Header with search and sort
        if (uiState.isSearching) {
            SearchHeader(
                query = uiState.searchQuery,
                onQueryChange = viewModel::setSearchQuery,
                onClose = viewModel::toggleSearch
            )
        } else {
            LibraryHeader(
                songCount = uiState.songCount,
                totalDurationMs = uiState.totalDurationMs,
                sortOrder = uiState.sortOrder,
                onSortChange = viewModel::setSortOrder,
                onSearchClick = viewModel::toggleSearch,
                onBackClick = onNavigateBack
            )
        }

        when {
            uiState.scanProgress.isScanning -> {
                ScanningContent(
                    currentFolder = uiState.scanProgress.currentFolder,
                    filesScanned = uiState.scanProgress.filesScanned,
                    newSongsAdded = uiState.scanProgress.newSongsAdded
                )
            }
            uiState.songs.isEmpty() && uiState.searchQuery.isEmpty() -> {
                EmptyLibraryContent(
                    onScanClick = viewModel::scanLibrary
                )
            }
            uiState.songs.isEmpty() && uiState.searchQuery.isNotEmpty() -> {
                NoResultsContent(query = uiState.searchQuery)
            }
            else -> {
                SongListContent(
                    songs = uiState.songs,
                    onSongClick = { song ->
                        viewModel.playSong(song, uiState.songs)
                    },
                    onMenuClick = { song ->
                        viewModel.showContextMenu(song)
                    }
                )
            }
        }
    }

    // Context menu dialog
    if (menuState.showContextMenu && menuState.selectedSong != null) {
        SongContextMenu(
            song = menuState.selectedSong!!,
            onDismiss = viewModel::dismissContextMenu,
            onAddToPlaylist = viewModel::showAddToPlaylistDialog,
            onPlayNext = viewModel::playNext,
            onAddToQueue = viewModel::addToQueue,
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
private fun LibraryHeader(
    songCount: Int,
    totalDurationMs: Long,
    sortOrder: SongSortOrder,
    onSortChange: (SongSortOrder) -> Unit,
    onSearchClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Column {
        ScreenHeader(
            title = "ALL SONGS",
            onBackClick = onBackClick,
            trailing = {
                Row {
                    IconButton(onClick = onSearchClick) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = GodaColors.PrimaryText
                        )
                    }
                    SortDropdown(
                        currentSort = sortOrder,
                        onSortChange = onSortChange
                    )
                }
            }
        )

        // Stats bar
        if (songCount > 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GodaColors.SecondaryBackground)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$songCount songs",
                    style = MaterialTheme.typography.bodySmall,
                    color = GodaColors.SecondaryText
                )
                Text(
                    text = formatTotalDuration(totalDurationMs),
                    style = MaterialTheme.typography.bodySmall,
                    color = GodaColors.SecondaryText
                )
            }
        }
    }
}

@Composable
private fun SortDropdown(
    currentSort: SongSortOrder,
    onSortChange: (SongSortOrder) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Default.Sort,
                contentDescription = "Sort",
                tint = GodaColors.PrimaryText
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(GodaColors.SecondaryBackground)
        ) {
            SongSortOrder.entries.forEach { sortOrder ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = sortOrder.displayName,
                            color = if (sortOrder == currentSort) GodaColors.PrimaryAccent else GodaColors.PrimaryText
                        )
                    },
                    onClick = {
                        onSortChange(sortOrder)
                        expanded = false
                    }
                )
            }
        }
    }
}

private val SongSortOrder.displayName: String
    get() = when (this) {
        SongSortOrder.TITLE -> "Title"
        SongSortOrder.ARTIST -> "Artist"
        SongSortOrder.DATE_ADDED -> "Date Added"
        SongSortOrder.DURATION -> "Duration"
        SongSortOrder.FILE_NAME -> "File Name"
    }

@Composable
private fun SearchHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(GodaColors.SecondaryBackground)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = GodaColors.SecondaryText,
            modifier = Modifier.padding(8.dp)
        )

        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = GodaColors.PrimaryText),
            cursorBrush = SolidColor(GodaColors.PrimaryAccent),
            decorationBox = { innerTextField ->
                Box {
                    if (query.isEmpty()) {
                        Text(
                            text = "Search songs...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = GodaColors.DisabledText
                        )
                    }
                    innerTextField()
                }
            }
        )

        IconButton(onClick = onClose) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close search",
                tint = GodaColors.PrimaryText
            )
        }
    }
}

@Composable
private fun SongListContent(
    songs: List<Song>,
    onSongClick: (Song) -> Unit,
    onMenuClick: (Song) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(songs, key = { it.id }) { song ->
            SongListItem(
                title = song.displayTitle,
                artist = song.artist,
                duration = song.formattedDuration,
                onClick = { onSongClick(song) },
                onMenuClick = { onMenuClick(song) }
            )
        }
    }
}

@Composable
private fun ScanningContent(
    currentFolder: String,
    filesScanned: Int,
    newSongsAdded: Int
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "SCANNING...",
            style = MaterialTheme.typography.headlineMedium,
            color = GodaColors.PrimaryAccent,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = currentFolder.substringAfterLast("/"),
            style = MaterialTheme.typography.bodyMedium,
            color = GodaColors.SecondaryText,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$filesScanned",
                    style = MaterialTheme.typography.headlineSmall,
                    color = GodaColors.PrimaryText
                )
                Text(
                    text = "files scanned",
                    style = MaterialTheme.typography.bodySmall,
                    color = GodaColors.SecondaryText
                )
            }

            Spacer(modifier = Modifier.width(48.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$newSongsAdded",
                    style = MaterialTheme.typography.headlineSmall,
                    color = GodaColors.PrimaryAccent
                )
                Text(
                    text = "new songs",
                    style = MaterialTheme.typography.bodySmall,
                    color = GodaColors.SecondaryText
                )
            }
        }
    }
}

@Composable
private fun EmptyLibraryContent(
    onScanClick: () -> Unit
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
            text = "LIBRARY EMPTY",
            style = MaterialTheme.typography.headlineMedium,
            color = GodaColors.SecondaryText,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Add folders in Settings, then scan your device to add music to your library",
            style = MaterialTheme.typography.bodyMedium,
            color = GodaColors.DisabledText,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        RetroTextButton(
            text = "SCAN NOW",
            onClick = onScanClick
        )
    }
}

@Composable
private fun NoResultsContent(query: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "NO RESULTS",
            style = MaterialTheme.typography.headlineMedium,
            color = GodaColors.SecondaryText,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "No songs matching \"$query\"",
            style = MaterialTheme.typography.bodyMedium,
            color = GodaColors.DisabledText,
            textAlign = TextAlign.Center
        )
    }
}

private fun formatTotalDuration(totalMs: Long): String {
    val totalMinutes = totalMs / 1000 / 60
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) {
        "${hours}h ${minutes}m"
    } else {
        "${minutes}m"
    }
}
