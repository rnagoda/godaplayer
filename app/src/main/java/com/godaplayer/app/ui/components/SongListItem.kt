package com.godaplayer.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.godaplayer.app.ui.theme.GodaColors

@Composable
fun SongListItem(
    title: String,
    artist: String?,
    duration: String,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false,
    position: Int? = null,
    showDragHandle: Boolean = false,
    onClick: () -> Unit = {},
    onMenuClick: (() -> Unit)? = null
) {
    val backgroundColor = if (isPlaying) {
        GodaColors.TertiaryBackground
    } else {
        GodaColors.Transparent
    }

    val titleColor = if (isPlaying) {
        GodaColors.PrimaryAccent
    } else {
        GodaColors.PrimaryText
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showDragHandle) {
            Icon(
                imageVector = Icons.Default.DragHandle,
                contentDescription = "Drag to reorder",
                tint = GodaColors.SecondaryText,
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 8.dp)
            )
        }

        if (position != null) {
            Text(
                text = position.toString().padStart(2),
                style = MaterialTheme.typography.bodySmall,
                color = GodaColors.SecondaryText,
                modifier = Modifier.padding(end = 12.dp)
            )
        }

        // Play indicator
        Text(
            text = if (isPlaying) "▶" else "♪",
            style = MaterialTheme.typography.bodyLarge,
            color = if (isPlaying) GodaColors.PrimaryAccent else GodaColors.SecondaryText,
            modifier = Modifier.padding(end = 12.dp)
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = titleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (artist != null) {
                Text(
                    text = artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = GodaColors.SecondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Text(
            text = duration,
            style = MaterialTheme.typography.bodySmall,
            color = GodaColors.SecondaryText,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        if (onMenuClick != null) {
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = GodaColors.SecondaryText
                )
            }
        }
    }
}

@Composable
fun FileListItem(
    name: String,
    isDirectory: Boolean,
    modifier: Modifier = Modifier,
    duration: String? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isDirectory) "📁" else "♪",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(end = 12.dp)
        )

        Text(
            text = if (isDirectory) "$name/" else name,
            style = MaterialTheme.typography.bodyLarge,
            color = GodaColors.PrimaryText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        if (duration != null) {
            Text(
                text = duration,
                style = MaterialTheme.typography.bodySmall,
                color = GodaColors.SecondaryText
            )
        }

        if (isDirectory) {
            Text(
                text = "▶",
                style = MaterialTheme.typography.bodyLarge,
                color = GodaColors.SecondaryText,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}
