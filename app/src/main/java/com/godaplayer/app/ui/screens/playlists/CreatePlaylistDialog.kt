package com.godaplayer.app.ui.screens.playlists

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.godaplayer.app.ui.components.RetroButton
import com.godaplayer.app.ui.theme.GodaColors

@Composable
fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, description: String?) -> Unit,
    initialName: String = "",
    title: String = "NEW PLAYLIST"
) {
    var name by remember { mutableStateOf(initialName) }
    var description by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

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
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = GodaColors.PrimaryAccent
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Name field
                Text(
                    text = "NAME",
                    style = MaterialTheme.typography.labelSmall,
                    color = GodaColors.SecondaryText
                )
                Spacer(modifier = Modifier.height(8.dp))
                BasicTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GodaColors.PrimaryBackground, RoundedCornerShape(4.dp))
                        .border(1.dp, GodaColors.BorderColor, RoundedCornerShape(4.dp))
                        .padding(12.dp)
                        .focusRequester(focusRequester),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = GodaColors.PrimaryText),
                    cursorBrush = SolidColor(GodaColors.PrimaryAccent),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        if (name.isEmpty()) {
                            Text(
                                text = "Enter playlist name...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = GodaColors.DisabledText
                            )
                        }
                        innerTextField()
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Description field (optional)
                Text(
                    text = "DESCRIPTION (OPTIONAL)",
                    style = MaterialTheme.typography.labelSmall,
                    color = GodaColors.SecondaryText
                )
                Spacer(modifier = Modifier.height(8.dp))
                BasicTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(GodaColors.PrimaryBackground, RoundedCornerShape(4.dp))
                        .border(1.dp, GodaColors.BorderColor, RoundedCornerShape(4.dp))
                        .padding(12.dp),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = GodaColors.PrimaryText),
                    cursorBrush = SolidColor(GodaColors.PrimaryAccent),
                    decorationBox = { innerTextField ->
                        if (description.isEmpty()) {
                            Text(
                                text = "Add a description...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = GodaColors.DisabledText
                            )
                        }
                        innerTextField()
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Buttons
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
                        text = "CREATE",
                        onClick = {
                            if (name.isNotBlank()) {
                                onCreate(name, description.ifBlank { null })
                            }
                        },
                        isPrimary = true,
                        enabled = name.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun RenamePlaylistDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onRename: (newName: String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

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
                    text = "RENAME PLAYLIST",
                    style = MaterialTheme.typography.headlineSmall,
                    color = GodaColors.PrimaryAccent
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "NAME",
                    style = MaterialTheme.typography.labelSmall,
                    color = GodaColors.SecondaryText
                )
                Spacer(modifier = Modifier.height(8.dp))
                BasicTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GodaColors.PrimaryBackground, RoundedCornerShape(4.dp))
                        .border(1.dp, GodaColors.BorderColor, RoundedCornerShape(4.dp))
                        .padding(12.dp)
                        .focusRequester(focusRequester),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = GodaColors.PrimaryText),
                    cursorBrush = SolidColor(GodaColors.PrimaryAccent),
                    singleLine = true
                )

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
                        text = "RENAME",
                        onClick = {
                            if (name.isNotBlank()) {
                                onRename(name)
                            }
                        },
                        isPrimary = true,
                        enabled = name.isNotBlank() && name != currentName,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun DeletePlaylistDialog(
    playlistName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
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
            ) {
                Text(
                    text = "DELETE PLAYLIST",
                    style = MaterialTheme.typography.headlineSmall,
                    color = GodaColors.Error
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Are you sure you want to delete \"$playlistName\"?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GodaColors.PrimaryText
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "This action cannot be undone. The songs will not be deleted from your device.",
                    style = MaterialTheme.typography.bodySmall,
                    color = GodaColors.SecondaryText
                )

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
                        text = "DELETE",
                        onClick = onConfirm,
                        isPrimary = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
