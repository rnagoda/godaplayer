package com.godaplayer.app.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

data class UserPreferences(
    val eqEnabled: Boolean = false,
    val currentPresetId: Long? = null,
    val gaplessPlayback: Boolean = true,
    val resumeOnStart: Boolean = true,
    val autoScan: Boolean = true,
    val showFileExtensions: Boolean = true
)

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val EQ_ENABLED = booleanPreferencesKey("eq_enabled")
        val CURRENT_PRESET_ID = longPreferencesKey("current_preset_id")
        val GAPLESS_PLAYBACK = booleanPreferencesKey("gapless_playback")
        val RESUME_ON_START = booleanPreferencesKey("resume_on_start")
        val AUTO_SCAN = booleanPreferencesKey("auto_scan")
        val SHOW_FILE_EXTENSIONS = booleanPreferencesKey("show_file_extensions")
    }

    val userPreferences: Flow<UserPreferences> = context.dataStore.data.map { preferences ->
        UserPreferences(
            eqEnabled = preferences[PreferencesKeys.EQ_ENABLED] ?: false,
            currentPresetId = preferences[PreferencesKeys.CURRENT_PRESET_ID],
            gaplessPlayback = preferences[PreferencesKeys.GAPLESS_PLAYBACK] ?: true,
            resumeOnStart = preferences[PreferencesKeys.RESUME_ON_START] ?: true,
            autoScan = preferences[PreferencesKeys.AUTO_SCAN] ?: true,
            showFileExtensions = preferences[PreferencesKeys.SHOW_FILE_EXTENSIONS] ?: true
        )
    }

    val eqEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.EQ_ENABLED] ?: false
    }

    val currentPresetId: Flow<Long?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.CURRENT_PRESET_ID]
    }

    val gaplessPlayback: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.GAPLESS_PLAYBACK] ?: true
    }

    val resumeOnStart: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.RESUME_ON_START] ?: true
    }

    val autoScan: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.AUTO_SCAN] ?: true
    }

    val showFileExtensions: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SHOW_FILE_EXTENSIONS] ?: true
    }

    suspend fun setEqEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.EQ_ENABLED] = enabled
        }
    }

    suspend fun setCurrentPresetId(presetId: Long?) {
        context.dataStore.edit { preferences ->
            if (presetId != null) {
                preferences[PreferencesKeys.CURRENT_PRESET_ID] = presetId
            } else {
                preferences.remove(PreferencesKeys.CURRENT_PRESET_ID)
            }
        }
    }

    suspend fun setGaplessPlayback(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.GAPLESS_PLAYBACK] = enabled
        }
    }

    suspend fun setResumeOnStart(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.RESUME_ON_START] = enabled
        }
    }

    suspend fun setAutoScan(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_SCAN] = enabled
        }
    }

    suspend fun setShowFileExtensions(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_FILE_EXTENSIONS] = enabled
        }
    }

    suspend fun updateEqSettings(enabled: Boolean, presetId: Long?) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.EQ_ENABLED] = enabled
            if (presetId != null) {
                preferences[PreferencesKeys.CURRENT_PRESET_ID] = presetId
            } else {
                preferences.remove(PreferencesKeys.CURRENT_PRESET_ID)
            }
        }
    }
}
