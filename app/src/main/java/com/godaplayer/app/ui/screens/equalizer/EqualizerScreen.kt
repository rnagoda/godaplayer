package com.godaplayer.app.ui.screens.equalizer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.godaplayer.app.domain.model.EqPreset
import com.godaplayer.app.ui.components.RetroButton
import com.godaplayer.app.ui.components.RetroTextButton
import com.godaplayer.app.ui.components.ScreenHeader
import com.godaplayer.app.ui.components.SectionHeader
import com.godaplayer.app.ui.theme.GodaColors

@Composable
fun EqualizerScreen(
    onNavigateBack: () -> Unit,
    viewModel: EqualizerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val menuState by viewModel.menuState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GodaColors.PrimaryBackground)
    ) {
        ScreenHeader(
            title = "EQUALIZER",
            onBackClick = onNavigateBack,
            trailing = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (uiState.isEnabled) "ON" else "OFF",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (uiState.isEnabled) GodaColors.PrimaryAccent else GodaColors.SecondaryText
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = uiState.isEnabled,
                        onCheckedChange = viewModel::setEnabled,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = GodaColors.PrimaryAccent,
                            checkedTrackColor = GodaColors.SecondaryAccent,
                            uncheckedThumbColor = GodaColors.SecondaryText,
                            uncheckedTrackColor = GodaColors.TertiaryBackground
                        )
                    )
                }
            }
        )

        // Preset selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PRESET:",
                style = MaterialTheme.typography.bodyMedium,
                color = GodaColors.SecondaryText
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                val presetName = buildString {
                    append(uiState.selectedPreset?.name ?: "Flat")
                    if (uiState.isModified) append(" *")
                }
                RetroTextButton(
                    text = "$presetName ▼",
                    onClick = viewModel::showPresetPicker
                )
            }
        }

        SectionHeader(title = "FREQUENCY BANDS")

        // EQ Bands
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            EqBandSlider(
                label = "60Hz",
                value = uiState.band60Hz,
                onValueChange = viewModel::setBand60Hz,
                enabled = uiState.isEnabled
            )
            EqBandSlider(
                label = "250Hz",
                value = uiState.band250Hz,
                onValueChange = viewModel::setBand250Hz,
                enabled = uiState.isEnabled
            )
            EqBandSlider(
                label = "1kHz",
                value = uiState.band1kHz,
                onValueChange = viewModel::setBand1kHz,
                enabled = uiState.isEnabled
            )
            EqBandSlider(
                label = "4kHz",
                value = uiState.band4kHz,
                onValueChange = viewModel::setBand4kHz,
                enabled = uiState.isEnabled
            )
            EqBandSlider(
                label = "16kHz",
                value = uiState.band16kHz,
                onValueChange = viewModel::setBand16kHz,
                enabled = uiState.isEnabled
            )
        }

        SectionHeader(title = "BASS BOOST")

        EffectSlider(
            value = uiState.bassBoost,
            onValueChange = viewModel::setBassBoost,
            enabled = uiState.isEnabled,
            valueRange = 0f..15f,
            label = "${uiState.bassBoost.toInt()} dB"
        )

        SectionHeader(title = "VIRTUALIZER")

        EffectSlider(
            value = uiState.virtualizer,
            onValueChange = viewModel::setVirtualizer,
            enabled = uiState.isEnabled,
            valueRange = 0f..100f,
            label = "${uiState.virtualizer.toInt()}%"
        )

        Spacer(modifier = Modifier.weight(1f))

        // Action buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            RetroTextButton(
                text = "RESET",
                onClick = {
                    if (uiState.isModified) {
                        viewModel.resetToPreset()
                    } else {
                        viewModel.reset()
                    }
                }
            )
            RetroTextButton(
                text = "SAVE AS PRESET",
                onClick = viewModel::showSaveDialog
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Preset picker dialog
    if (menuState.showPresetPicker) {
        PresetPickerDialog(
            presets = uiState.presets,
            selectedPreset = uiState.selectedPreset,
            onSelectPreset = {
                viewModel.selectPreset(it)
                viewModel.dismissPresetPicker()
            },
            onDeletePreset = viewModel::showDeleteDialog,
            onDismiss = viewModel::dismissPresetPicker
        )
    }

    // Save preset dialog
    if (menuState.showSaveDialog) {
        SavePresetDialog(
            onSave = viewModel::saveAsPreset,
            onDismiss = viewModel::dismissSaveDialog
        )
    }

    // Delete preset confirmation dialog
    if (menuState.showDeleteDialog && menuState.presetToDelete != null) {
        DeletePresetDialog(
            presetName = menuState.presetToDelete!!.name,
            onConfirm = { viewModel.deletePreset(menuState.presetToDelete!!) },
            onDismiss = viewModel::dismissDeleteDialog
        )
    }
}

@Composable
private fun EqBandSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    enabled: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = if (enabled) GodaColors.SecondaryText else GodaColors.DisabledText,
            modifier = Modifier.width(56.dp)
        )

        Slider(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            valueRange = -12f..12f,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = GodaColors.PrimaryAccent,
                activeTrackColor = GodaColors.PrimaryAccent,
                inactiveTrackColor = GodaColors.TertiaryBackground,
                disabledThumbColor = GodaColors.DisabledText,
                disabledActiveTrackColor = GodaColors.DisabledText,
                disabledInactiveTrackColor = GodaColors.TertiaryBackground
            )
        )

        Text(
            text = "${if (value >= 0) "+" else ""}${value.toInt()}",
            style = MaterialTheme.typography.bodySmall,
            color = if (enabled) GodaColors.SecondaryText else GodaColors.DisabledText,
            modifier = Modifier.width(40.dp)
        )
    }
}

@Composable
private fun EffectSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    enabled: Boolean,
    valueRange: ClosedFloatingPointRange<Float>,
    label: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Slider(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            valueRange = valueRange,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = GodaColors.PrimaryAccent,
                activeTrackColor = GodaColors.PrimaryAccent,
                inactiveTrackColor = GodaColors.TertiaryBackground,
                disabledThumbColor = GodaColors.DisabledText,
                disabledActiveTrackColor = GodaColors.DisabledText,
                disabledInactiveTrackColor = GodaColors.TertiaryBackground
            )
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = if (enabled) GodaColors.SecondaryText else GodaColors.DisabledText,
            modifier = Modifier.width(56.dp)
        )
    }
}

@Composable
private fun PresetPickerDialog(
    presets: List<EqPreset>,
    selectedPreset: EqPreset?,
    onSelectPreset: (EqPreset) -> Unit,
    onDeletePreset: (EqPreset) -> Unit,
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
                    .padding(vertical = 16.dp)
            ) {
                Text(
                    text = "SELECT PRESET",
                    style = MaterialTheme.typography.headlineSmall,
                    color = GodaColors.PrimaryText,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                HorizontalDivider(color = GodaColors.DividerColor)

                val builtInPresets = presets.filter { !it.isCustom }
                val customPresets = presets.filter { it.isCustom }

                LazyColumn(
                    modifier = Modifier.height(300.dp)
                ) {
                    if (builtInPresets.isNotEmpty()) {
                        item {
                            Text(
                                text = "BUILT-IN",
                                style = MaterialTheme.typography.labelSmall,
                                color = GodaColors.SecondaryText,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        items(builtInPresets) { preset ->
                            PresetItem(
                                preset = preset,
                                isSelected = selectedPreset?.id == preset.id,
                                onClick = { onSelectPreset(preset) },
                                onDelete = null
                            )
                        }
                    }

                    if (customPresets.isNotEmpty()) {
                        item {
                            HorizontalDivider(
                                color = GodaColors.DividerColor,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            Text(
                                text = "CUSTOM",
                                style = MaterialTheme.typography.labelSmall,
                                color = GodaColors.SecondaryText,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        items(customPresets) { preset ->
                            PresetItem(
                                preset = preset,
                                isSelected = selectedPreset?.id == preset.id,
                                onClick = { onSelectPreset(preset) },
                                onDelete = { onDeletePreset(preset) }
                            )
                        }
                    }
                }

                HorizontalDivider(color = GodaColors.DividerColor)

                RetroTextButton(
                    text = "CANCEL",
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun PresetItem(
    preset: EqPreset,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDelete: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = GodaColors.PrimaryAccent,
                    modifier = Modifier.padding(end = 12.dp)
                )
            } else {
                Spacer(modifier = Modifier.width(36.dp))
            }
            Text(
                text = preset.name,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected) GodaColors.PrimaryAccent else GodaColors.PrimaryText
            )
        }

        if (onDelete != null) {
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = GodaColors.Error
                )
            }
        }
    }
}

@Composable
private fun SavePresetDialog(
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }

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
                    text = "SAVE PRESET",
                    style = MaterialTheme.typography.headlineSmall,
                    color = GodaColors.PrimaryText
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Preset name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GodaColors.PrimaryAccent,
                        unfocusedBorderColor = GodaColors.BorderColor,
                        focusedLabelColor = GodaColors.PrimaryAccent,
                        unfocusedLabelColor = GodaColors.SecondaryText,
                        cursorColor = GodaColors.PrimaryAccent,
                        focusedTextColor = GodaColors.PrimaryText,
                        unfocusedTextColor = GodaColors.PrimaryText
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (name.isNotBlank()) {
                                onSave(name.trim())
                            }
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    RetroButton(
                        text = "CANCEL",
                        onClick = onDismiss,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    RetroButton(
                        text = "SAVE",
                        onClick = {
                            if (name.isNotBlank()) {
                                onSave(name.trim())
                            }
                        },
                        isPrimary = true
                    )
                }
            }
        }
    }
}

@Composable
private fun DeletePresetDialog(
    presetName: String,
    onConfirm: () -> Unit,
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
            ) {
                Text(
                    text = "DELETE PRESET",
                    style = MaterialTheme.typography.headlineSmall,
                    color = GodaColors.Error
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Delete \"$presetName\"? This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GodaColors.PrimaryText
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
