package com.godaplayer.app.ui.screens.equalizer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.godaplayer.app.data.local.preferences.UserPreferencesRepository
import com.godaplayer.app.domain.model.EqPreset
import com.godaplayer.app.domain.repository.EqPresetRepository
import com.godaplayer.app.player.EqualizerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EqualizerUiState(
    val isEnabled: Boolean = false,
    val isAvailable: Boolean = false,
    val presets: List<EqPreset> = emptyList(),
    val selectedPreset: EqPreset? = null,
    val isModified: Boolean = false,
    // Current values (in UI-friendly units)
    val band60Hz: Float = 0f,       // -12 to +12 dB
    val band250Hz: Float = 0f,
    val band1kHz: Float = 0f,
    val band4kHz: Float = 0f,
    val band16kHz: Float = 0f,
    val bassBoost: Float = 0f,      // 0 to 15 dB
    val virtualizer: Float = 0f     // 0 to 100%
)

data class EqualizerMenuState(
    val showPresetPicker: Boolean = false,
    val showSaveDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val presetToDelete: EqPreset? = null
)

@HiltViewModel
class EqualizerViewModel @Inject constructor(
    private val equalizerManager: EqualizerManager,
    private val eqPresetRepository: EqPresetRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _menuState = MutableStateFlow(EqualizerMenuState())
    val menuState: StateFlow<EqualizerMenuState> = _menuState.asStateFlow()

    private val _selectedPreset = MutableStateFlow<EqPreset?>(null)
    private val _isModified = MutableStateFlow(false)

    val uiState: StateFlow<EqualizerUiState> = combine(
        userPreferencesRepository.eqEnabled,
        equalizerManager.isAvailable,
        eqPresetRepository.getAllPresets(),
        equalizerManager.state,
        _selectedPreset,
        _isModified
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val enabled = values[0] as Boolean
        val available = values[1] as Boolean
        val presets = values[2] as List<EqPreset>
        val eqState = values[3] as com.godaplayer.app.player.EqualizerState
        val selectedPreset = values[4] as EqPreset?
        val isModified = values[5] as Boolean

        EqualizerUiState(
            isEnabled = enabled,
            isAvailable = available,
            presets = presets,
            selectedPreset = selectedPreset,
            isModified = isModified,
            band60Hz = eqState.band60Hz / 100f,
            band250Hz = eqState.band250Hz / 100f,
            band1kHz = eqState.band1kHz / 100f,
            band4kHz = eqState.band4kHz / 100f,
            band16kHz = eqState.band16kHz / 100f,
            bassBoost = (eqState.bassBoost / 1000f) * 15f,
            virtualizer = (eqState.virtualizer / 1000f) * 100f
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = EqualizerUiState()
    )

    init {
        // Load saved preset on startup
        viewModelScope.launch {
            val presetId = userPreferencesRepository.currentPresetId.first()
            if (presetId != null) {
                val preset = eqPresetRepository.getPresetById(presetId)
                if (preset != null) {
                    _selectedPreset.value = preset
                    applyPresetToManager(preset)
                }
            } else {
                // Default to Flat preset
                val flatPreset = eqPresetRepository.getPresetByName("Flat")
                if (flatPreset != null) {
                    _selectedPreset.value = flatPreset
                    applyPresetToManager(flatPreset)
                }
            }

            // Apply saved enabled state
            val enabled = userPreferencesRepository.eqEnabled.first()
            equalizerManager.setEnabled(enabled)
        }
    }

    fun setEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setEqEnabled(enabled)
            equalizerManager.setEnabled(enabled)
        }
    }

    fun setBand60Hz(db: Float) {
        equalizerManager.setBand60Hz((db * 100).toInt())
        markAsModified()
    }

    fun setBand250Hz(db: Float) {
        equalizerManager.setBand250Hz((db * 100).toInt())
        markAsModified()
    }

    fun setBand1kHz(db: Float) {
        equalizerManager.setBand1kHz((db * 100).toInt())
        markAsModified()
    }

    fun setBand4kHz(db: Float) {
        equalizerManager.setBand4kHz((db * 100).toInt())
        markAsModified()
    }

    fun setBand16kHz(db: Float) {
        equalizerManager.setBand16kHz((db * 100).toInt())
        markAsModified()
    }

    fun setBassBoost(db: Float) {
        val strength = ((db / 15f) * 1000).toInt().coerceIn(0, 1000)
        equalizerManager.setBassBoostStrength(strength)
        markAsModified()
    }

    fun setVirtualizer(percent: Float) {
        val strength = ((percent / 100f) * 1000).toInt().coerceIn(0, 1000)
        equalizerManager.setVirtualizerStrength(strength)
        markAsModified()
    }

    private fun markAsModified() {
        if (_selectedPreset.value != null && !_isModified.value) {
            _isModified.value = true
        }
    }

    fun selectPreset(preset: EqPreset) {
        viewModelScope.launch {
            _selectedPreset.value = preset
            _isModified.value = false
            applyPresetToManager(preset)
            userPreferencesRepository.setCurrentPresetId(preset.id)
        }
    }

    private fun applyPresetToManager(preset: EqPreset) {
        equalizerManager.applyPreset(
            band60Hz = preset.band60Hz,
            band250Hz = preset.band250Hz,
            band1kHz = preset.band1kHz,
            band4kHz = preset.band4kHz,
            band16kHz = preset.band16kHz,
            bassBoostStrength = preset.bassBoost,
            virtualizerStrength = preset.virtualizer
        )
    }

    fun resetToPreset() {
        val preset = _selectedPreset.value ?: return
        applyPresetToManager(preset)
        _isModified.value = false
    }

    fun reset() {
        equalizerManager.reset()
        _isModified.value = true
    }

    fun saveAsPreset(name: String) {
        viewModelScope.launch {
            val state = equalizerManager.state.value
            val newPreset = EqPreset(
                name = name,
                isCustom = true,
                band60Hz = state.band60Hz,
                band250Hz = state.band250Hz,
                band1kHz = state.band1kHz,
                band4kHz = state.band4kHz,
                band16kHz = state.band16kHz,
                bassBoost = state.bassBoost,
                virtualizer = state.virtualizer
            )
            val id = eqPresetRepository.insertPreset(newPreset)
            val savedPreset = eqPresetRepository.getPresetById(id)
            if (savedPreset != null) {
                _selectedPreset.value = savedPreset
                _isModified.value = false
                userPreferencesRepository.setCurrentPresetId(savedPreset.id)
            }
            dismissSaveDialog()
        }
    }

    fun deletePreset(preset: EqPreset) {
        if (!preset.isCustom) return // Can't delete built-in presets

        viewModelScope.launch {
            eqPresetRepository.deleteCustomPreset(preset.id)

            // If deleted preset was selected, switch to Flat
            if (_selectedPreset.value?.id == preset.id) {
                val flatPreset = eqPresetRepository.getPresetByName("Flat")
                if (flatPreset != null) {
                    selectPreset(flatPreset)
                }
            }
            dismissDeleteDialog()
        }
    }

    // Menu state management
    fun showPresetPicker() {
        _menuState.update { it.copy(showPresetPicker = true) }
    }

    fun dismissPresetPicker() {
        _menuState.update { it.copy(showPresetPicker = false) }
    }

    fun showSaveDialog() {
        _menuState.update { it.copy(showSaveDialog = true) }
    }

    fun dismissSaveDialog() {
        _menuState.update { it.copy(showSaveDialog = false) }
    }

    fun showDeleteDialog(preset: EqPreset) {
        _menuState.update { it.copy(showDeleteDialog = true, presetToDelete = preset) }
    }

    fun dismissDeleteDialog() {
        _menuState.update { it.copy(showDeleteDialog = false, presetToDelete = null) }
    }
}
