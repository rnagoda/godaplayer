package com.godaplayer.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.godaplayer.app.ui.theme.GodaColors

@Composable
fun RetroButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isPrimary: Boolean = false
) {
    val backgroundColor = if (isPrimary) {
        GodaColors.PrimaryAccent
    } else {
        GodaColors.Transparent
    }

    val textColor = when {
        !enabled -> GodaColors.DisabledText
        isPrimary -> GodaColors.PrimaryBackground
        else -> GodaColors.PrimaryAccent
    }

    val borderColor = when {
        !enabled -> GodaColors.DisabledText
        else -> GodaColors.PrimaryAccent
    }

    Surface(
        modifier = modifier
            .clickable(enabled = enabled, onClick = onClick),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor),
        shape = MaterialTheme.shapes.extraSmall
    ) {
        Text(
            text = "[ $text ]",
            style = MaterialTheme.typography.labelLarge,
            color = textColor,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun RetroTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val textColor = if (enabled) GodaColors.PrimaryAccent else GodaColors.DisabledText

    Text(
        text = "[ $text ]",
        style = MaterialTheme.typography.labelLarge,
        color = textColor,
        modifier = modifier
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}
