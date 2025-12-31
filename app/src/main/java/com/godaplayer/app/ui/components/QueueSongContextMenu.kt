package com.godaplayer.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.godaplayer.app.domain.model.Song
import com.godaplayer.app.ui.theme.GodaColors

@Composable
fun QueueSongContextMenu(
    song: Song,
    onDismiss: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onRemoveFromQueue: () -> Unit,
    onShowInfo: () -> Unit
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
                    .padding(vertical = 8.dp)
            ) {
                // Song header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = song.displayTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = GodaColors.PrimaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (song.artist != null) {
                        Text(
                            text = song.artist,
                            style = MaterialTheme.typography.bodySmall,
                            color = GodaColors.SecondaryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                HorizontalDivider(color = GodaColors.DividerColor)

                // Menu items
                QueueContextMenuItem(
                    icon = Icons.Default.PlaylistAdd,
                    text = "Add to playlist",
                    onClick = {
                        onDismiss()
                        onAddToPlaylist()
                    }
                )

                QueueContextMenuItem(
                    icon = Icons.Default.Delete,
                    text = "Remove from queue",
                    textColor = GodaColors.Error,
                    onClick = {
                        onDismiss()
                        onRemoveFromQueue()
                    }
                )

                HorizontalDivider(
                    color = GodaColors.DividerColor,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                QueueContextMenuItem(
                    icon = Icons.Default.Info,
                    text = "Song info",
                    onClick = {
                        onDismiss()
                        onShowInfo()
                    }
                )
            }
        }
    }
}

@Composable
private fun QueueContextMenuItem(
    icon: ImageVector,
    text: String,
    textColor: androidx.compose.ui.graphics.Color = GodaColors.PrimaryText,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (textColor == GodaColors.Error) GodaColors.Error else GodaColors.SecondaryText,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = textColor
        )
    }
}
