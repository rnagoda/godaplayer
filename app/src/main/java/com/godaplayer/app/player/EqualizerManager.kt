package com.godaplayer.app.player

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class EqualizerState(
    val isEnabled: Boolean = false,
    val band60Hz: Int = 0,      // -1200 to +1200 millibels
    val band250Hz: Int = 0,
    val band1kHz: Int = 0,
    val band4kHz: Int = 0,
    val band16kHz: Int = 0,
    val bassBoost: Int = 0,     // 0-1000
    val virtualizer: Int = 0    // 0-1000
)

@Singleton
class EqualizerManager @Inject constructor() {

    companion object {
        private const val TAG = "EqualizerManager"

        // Band indices for 5-band equalizer
        private const val BAND_60HZ = 0
        private const val BAND_250HZ = 1
        private const val BAND_1KHZ = 2
        private const val BAND_4KHZ = 3
        private const val BAND_16KHZ = 4

        // Millibel range for EQ bands
        const val MIN_LEVEL_MB = -1200
        const val MAX_LEVEL_MB = 1200

        // Strength range for effects
        const val MIN_STRENGTH = 0
        const val MAX_STRENGTH = 1000
    }

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var currentAudioSessionId: Int = 0

    private val _state = MutableStateFlow(EqualizerState())
    val state: StateFlow<EqualizerState> = _state.asStateFlow()

    private val _isAvailable = MutableStateFlow(false)
    val isAvailable: StateFlow<Boolean> = _isAvailable.asStateFlow()

    fun attachToAudioSession(audioSessionId: Int) {
        if (audioSessionId == 0 || audioSessionId == currentAudioSessionId) {
            return
        }

        Log.d(TAG, "Attaching to audio session: $audioSessionId")

        // Release existing effects
        release()

        currentAudioSessionId = audioSessionId

        try {
            // Initialize Equalizer
            equalizer = Equalizer(0, audioSessionId).apply {
                // Verify we have 5 bands
                if (numberOfBands.toInt() >= 5) {
                    Log.d(TAG, "Equalizer initialized with ${numberOfBands} bands")
                } else {
                    Log.w(TAG, "Equalizer has only ${numberOfBands} bands, expected 5")
                }
            }

            // Initialize Bass Boost
            bassBoost = BassBoost(0, audioSessionId).apply {
                if (strengthSupported) {
                    Log.d(TAG, "BassBoost initialized with strength support")
                } else {
                    Log.w(TAG, "BassBoost strength not supported on this device")
                }
            }

            // Initialize Virtualizer
            virtualizer = Virtualizer(0, audioSessionId).apply {
                if (strengthSupported) {
                    Log.d(TAG, "Virtualizer initialized with strength support")
                } else {
                    Log.w(TAG, "Virtualizer strength not supported on this device")
                }
            }

            _isAvailable.value = true

            // Apply current state to the new effects
            applyCurrentState()

        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize audio effects", e)
            _isAvailable.value = false
            release()
        }
    }

    fun setEnabled(enabled: Boolean) {
        _state.value = _state.value.copy(isEnabled = enabled)

        try {
            equalizer?.enabled = enabled
            bassBoost?.enabled = enabled
            virtualizer?.enabled = enabled
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set enabled state", e)
        }
    }

    fun setBandLevel(band: Int, levelMb: Int) {
        val clampedLevel = levelMb.coerceIn(MIN_LEVEL_MB, MAX_LEVEL_MB)

        _state.value = when (band) {
            BAND_60HZ -> _state.value.copy(band60Hz = clampedLevel)
            BAND_250HZ -> _state.value.copy(band250Hz = clampedLevel)
            BAND_1KHZ -> _state.value.copy(band1kHz = clampedLevel)
            BAND_4KHZ -> _state.value.copy(band4kHz = clampedLevel)
            BAND_16KHZ -> _state.value.copy(band16kHz = clampedLevel)
            else -> _state.value
        }

        try {
            equalizer?.setBandLevel(band.toShort(), clampedLevel.toShort())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set band $band level", e)
        }
    }

    fun setBand60Hz(levelMb: Int) = setBandLevel(BAND_60HZ, levelMb)
    fun setBand250Hz(levelMb: Int) = setBandLevel(BAND_250HZ, levelMb)
    fun setBand1kHz(levelMb: Int) = setBandLevel(BAND_1KHZ, levelMb)
    fun setBand4kHz(levelMb: Int) = setBandLevel(BAND_4KHZ, levelMb)
    fun setBand16kHz(levelMb: Int) = setBandLevel(BAND_16KHZ, levelMb)

    fun setBassBoostStrength(strength: Int) {
        val clampedStrength = strength.coerceIn(MIN_STRENGTH, MAX_STRENGTH)
        _state.value = _state.value.copy(bassBoost = clampedStrength)

        try {
            bassBoost?.setStrength(clampedStrength.toShort())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set bass boost strength", e)
        }
    }

    fun setVirtualizerStrength(strength: Int) {
        val clampedStrength = strength.coerceIn(MIN_STRENGTH, MAX_STRENGTH)
        _state.value = _state.value.copy(virtualizer = clampedStrength)

        try {
            virtualizer?.setStrength(clampedStrength.toShort())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set virtualizer strength", e)
        }
    }

    fun applyPreset(
        band60Hz: Int,
        band250Hz: Int,
        band1kHz: Int,
        band4kHz: Int,
        band16kHz: Int,
        bassBoostStrength: Int,
        virtualizerStrength: Int
    ) {
        setBand60Hz(band60Hz)
        setBand250Hz(band250Hz)
        setBand1kHz(band1kHz)
        setBand4kHz(band4kHz)
        setBand16kHz(band16kHz)
        setBassBoostStrength(bassBoostStrength)
        setVirtualizerStrength(virtualizerStrength)
    }

    fun reset() {
        applyPreset(0, 0, 0, 0, 0, 0, 0)
    }

    private fun applyCurrentState() {
        val currentState = _state.value

        try {
            equalizer?.apply {
                enabled = currentState.isEnabled
                setBandLevel(BAND_60HZ.toShort(), currentState.band60Hz.toShort())
                setBandLevel(BAND_250HZ.toShort(), currentState.band250Hz.toShort())
                setBandLevel(BAND_1KHZ.toShort(), currentState.band1kHz.toShort())
                setBandLevel(BAND_4KHZ.toShort(), currentState.band4kHz.toShort())
                setBandLevel(BAND_16KHZ.toShort(), currentState.band16kHz.toShort())
            }

            bassBoost?.apply {
                enabled = currentState.isEnabled
                setStrength(currentState.bassBoost.toShort())
            }

            virtualizer?.apply {
                enabled = currentState.isEnabled
                setStrength(currentState.virtualizer.toShort())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to apply current state", e)
        }
    }

    fun release() {
        Log.d(TAG, "Releasing audio effects")

        try {
            equalizer?.release()
            bassBoost?.release()
            virtualizer?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing audio effects", e)
        }

        equalizer = null
        bassBoost = null
        virtualizer = null
        currentAudioSessionId = 0
        _isAvailable.value = false
    }
}
