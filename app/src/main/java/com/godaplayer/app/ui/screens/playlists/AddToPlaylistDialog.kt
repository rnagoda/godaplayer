package com.godaplayer.app.ui.screens.playlists

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.godaplayer.app.domain.model.Playlist
import com.godaplayer.app.ui.components.RetroButton
import com.godaplayer.app.ui.theme.GodaColors

@Composable
fun AddToPlaylistDialog(
    playlists: List<Playlist>,
    existingPlaylistIds: List<Long>,
    onDismiss: () -> Unit,
    onAddToPlaylists: (List<Long>) -> Unit,
    onCreateNewPlaylist: () -> Unit
) {
    var selectedIds by remember { mutableStateOf(setOf<Long>()) }

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
                    text = "ADD TO PLAYLIST",
                    style = MaterialTheme.typography.headlineSmall,
                    color = GodaColors.PrimaryAccent
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Create new playlist option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onCreateNewPlaylist)
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = GodaColors.PrimaryAccent,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Create new playlist",
                        style = MaterialTheme.typography.bodyLarge,
                        color = GodaColors.PrimaryAccent
                    )
                }

                HorizontalDivider(color = GodaColors.DividerColor)

                if (playlists.isEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "No playlists yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GodaColors.SecondaryText,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    ) {
                        items(playlists) { playlist ->
                            val isInPlaylist = playlist.id in existingPlaylistIds
                            val isSelected = playlist.id in selectedIds

                            PlaylistSelectionItem(
                                playlist = playlist,
                                isInPlaylist = isInPlaylist,
                                isSelected = isSelected,
                                onToggle = {
                                    if (!isInPlaylist) {
                                        selectedIds = if (isSelected) {
                                            selectedIds - playlist.id
                                        } else {
                                            selectedIds + playlist.id
                                        }
                                    }
                                }
                            )
                        }
                    }
                }

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
                        text = "ADD",
                        onClick = {
                            if (selectedIds.isNotEmpty()) {
                                onAddToPlaylists(selectedIds.toList())
                            }
                        },
                        isPrimary = true,
                        enabled = selectedIds.isNotEmpty(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaylistSelectionItem(
    playlist: Playlist,
    isInPlaylist: Boolean,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isInPlaylist, onClick = onToggle)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isInPlaylist || isSelected,
            onCheckedChange = { if (!isInPlaylist) onToggle() },
            enabled = !isInPlaylist,
            colors = CheckboxDefaults.colors(
                checkedColor = if (isInPlaylist) GodaColors.SecondaryText else GodaColors.PrimaryAccent,
                uncheckedColor = GodaColors.SecondaryText,
                checkmarkColor = GodaColors.PrimaryBackground,
                disabledCheckedColor = GodaColors.SecondaryText,
                disabledUncheckedColor = GodaColors.DisabledText
            )
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isInPlaylist) GodaColors.SecondaryText else GodaColors.PrimaryText
            )
            Text(
                text = "${playlist.songCount} songs",
                style = MaterialTheme.typography.bodySmall,
                color = GodaColors.SecondaryText
            )
        }

        if (isInPlaylist) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Already added",
                tint = GodaColors.SecondaryText,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
