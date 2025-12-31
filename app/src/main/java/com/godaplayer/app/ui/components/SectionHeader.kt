package com.godaplayer.app.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.godaplayer.app.ui.theme.GodaColors

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    count: Int? = null
) {
    val displayTitle = if (count != null) {
        "$title ($count)"
    } else {
        title
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "── $displayTitle ",
            style = MaterialTheme.typography.bodySmall,
            color = GodaColors.SecondaryText
        )
        Text(
            text = "─".repeat(40),
            style = MaterialTheme.typography.bodySmall,
            color = GodaColors.SecondaryText,
            maxLines = 1,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBackClick != null) {
            RetroTextButton(
                text = "◄",
                onClick = onBackClick
            )
        }
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.headlineLarge,
            color = GodaColors.PrimaryText,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = if (onBackClick != null) 8.dp else 0.dp)
        )
        trailing?.invoke()
    }
}
