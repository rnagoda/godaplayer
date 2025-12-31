package com.godaplayer.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import com.godaplayer.app.ui.theme.GodaColors

@Composable
fun RetroProgressBar(
    progress: Float,
    currentTime: String,
    totalTime: String,
    modifier: Modifier = Modifier,
    onSeek: ((Float) -> Unit)? = null
) {
    var width by remember { mutableFloatStateOf(0f) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = currentTime,
            style = MaterialTheme.typography.bodySmall,
            color = GodaColors.SecondaryText
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
                .height(16.dp)
                .onSizeChanged { width = it.width.toFloat() }
                .let { mod ->
                    if (onSeek != null) {
                        mod.pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val position = event.changes.firstOrNull()?.position ?: Offset.Zero
                                    if (event.changes.any { it.pressed }) {
                                        val newProgress = (position.x / width).coerceIn(0f, 1f)
                                        onSeek(newProgress)
                                    }
                                }
                            }
                        }
                    } else {
                        mod
                    }
                }
        ) {
            // Background track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .align(Alignment.Center)
                    .background(GodaColors.TertiaryBackground)
            )

            // Progress fill
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .height(4.dp)
                    .align(Alignment.CenterStart)
                    .background(GodaColors.PrimaryAccent)
            )
        }

        Text(
            text = totalTime,
            style = MaterialTheme.typography.bodySmall,
            color = GodaColors.SecondaryText
        )
    }
}

@Composable
fun AsciiProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    width: Int = 30
) {
    val filledCount = (progress * width).toInt().coerceIn(0, width)
    val emptyCount = width - filledCount

    Text(
        text = "▓".repeat(filledCount) + "░".repeat(emptyCount),
        style = MaterialTheme.typography.bodySmall,
        color = GodaColors.PrimaryAccent,
        modifier = modifier
    )
}

fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
