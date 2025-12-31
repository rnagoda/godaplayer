package com.godaplayer.app.domain.repository

import com.godaplayer.app.domain.model.EqPreset
import kotlinx.coroutines.flow.Flow

interface EqPresetRepository {
    fun getAllPresets(): Flow<List<EqPreset>>
    fun getBuiltInPresets(): Flow<List<EqPreset>>
    fun getCustomPresets(): Flow<List<EqPreset>>
    suspend fun getPresetById(id: Long): EqPreset?
    suspend fun getPresetByName(name: String): EqPreset?
    suspend fun insertPreset(preset: EqPreset): Long
    suspend fun updatePreset(preset: EqPreset)
    suspend fun deleteCustomPreset(presetId: Long)
    suspend fun seedBuiltInPresets()
}
