package com.godaplayer.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.godaplayer.app.domain.model.Playlist
import com.godaplayer.app.domain.model.Song
import com.godaplayer.app.ui.theme.GodaColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SongInfoSheet(
    song: Song,
    playlists: List<Playlist>,
    onDismiss: () -> Unit
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
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "SONG INFO",
                    style = MaterialTheme.typography.headlineSmall,
                    color = GodaColors.PrimaryAccent
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Basic info
                InfoSection(title = "TITLE") {
                    Text(
                        text = song.displayTitle,
                        style = MaterialTheme.typography.bodyLarge,
                        color = GodaColors.PrimaryText
                    )
                }

                if (song.artist != null) {
                    InfoSection(title = "ARTIST") {
                        Text(
                            text = song.artist,
                            style = MaterialTheme.typography.bodyLarge,
                            color = GodaColors.PrimaryText
                        )
                    }
                }

                if (song.album != null) {
                    InfoSection(title = "ALBUM") {
                        Text(
                            text = song.album,
                            style = MaterialTheme.typography.bodyLarge,
                            color = GodaColors.PrimaryText
                        )
                    }
                }

                InfoSection(title = "DURATION") {
                    Text(
                        text = song.formattedDuration,
                        style = MaterialTheme.typography.bodyLarge,
                        color = GodaColors.PrimaryText
                    )
                }

                if (song.genre != null) {
                    InfoSection(title = "GENRE") {
                        Text(
                            text = song.genre,
                            style = MaterialTheme.typography.bodyLarge,
                            color = GodaColors.PrimaryText
                        )
                    }
                }

                if (song.year != null) {
                    InfoSection(title = "YEAR") {
                        Text(
                            text = song.year.toString(),
                            style = MaterialTheme.typography.bodyLarge,
                            color = GodaColors.PrimaryText
                        )
                    }
                }

                if (song.trackNumber != null) {
                    InfoSection(title = "TRACK") {
                        Text(
                            text = song.trackNumber.toString(),
                            style = MaterialTheme.typography.bodyLarge,
                            color = GodaColors.PrimaryText
                        )
                    }
                }

                HorizontalDivider(
                    color = GodaColors.DividerColor,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                // File info
                InfoSection(title = "FILE NAME") {
                    Text(
                        text = song.fileName,
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                        color = GodaColors.PrimaryText
                    )
                }

                InfoSection(title = "FILE PATH") {
                    Text(
                        text = song.filePath,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = GodaColors.SecondaryText
                    )
                }

                InfoSection(title = "FILE SIZE") {
                    Text(
                        text = formatFileSize(song.fileSize),
                        style = MaterialTheme.typography.bodyLarge,
                        color = GodaColors.PrimaryText
                    )
                }

                HorizontalDivider(
                    color = GodaColors.DividerColor,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                // Stats
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "PLAY COUNT",
                            style = MaterialTheme.typography.labelSmall,
                            color = GodaColors.SecondaryText
                        )
                        Text(
                            text = song.playCount.toString(),
                            style = MaterialTheme.typography.bodyLarge,
                            color = GodaColors.PrimaryAccent
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "DATE ADDED",
                            style = MaterialTheme.typography.labelSmall,
                            color = GodaColors.SecondaryText
                        )
                        Text(
                            text = formatDate(song.dateAdded),
                            style = MaterialTheme.typography.bodyMedium,
                            color = GodaColors.PrimaryText
                        )
                    }
                }

                if (song.lastPlayed != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    InfoSection(title = "LAST PLAYED") {
                        Text(
                            text = formatDate(song.lastPlayed),
                            style = MaterialTheme.typography.bodyMedium,
                            color = GodaColors.PrimaryText
                        )
                    }
                }

                // Playlists containing this song
                if (playlists.isNotEmpty()) {
                    HorizontalDivider(
                        color = GodaColors.DividerColor,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )

                    Text(
                        text = "IN PLAYLISTS",
                        style = MaterialTheme.typography.labelSmall,
                        color = GodaColors.SecondaryText
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    playlists.forEach { playlist ->
                        Text(
                            text = "• ${playlist.name}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GodaColors.PrimaryText,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                RetroButton(
                    text = "CLOSE",
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun InfoSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = GodaColors.SecondaryText
        )
        Spacer(modifier = Modifier.height(2.dp))
        content()
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0))
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM d, yyyy", Locale.US)
    return sdf.format(Date(timestamp))
}
