package com.godaplayer.app.data.repository

import com.godaplayer.app.data.local.database.dao.EqPresetDao
import com.godaplayer.app.data.local.database.entity.EqPresetEntity
import com.godaplayer.app.data.mapper.toDomain
import com.godaplayer.app.data.mapper.toEntity
import com.godaplayer.app.domain.model.EqPreset
import com.godaplayer.app.domain.repository.EqPresetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EqPresetRepositoryImpl @Inject constructor(
    private val eqPresetDao: EqPresetDao
) : EqPresetRepository {

    override fun getAllPresets(): Flow<List<EqPreset>> {
        return eqPresetDao.getAllPresets().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getBuiltInPresets(): Flow<List<EqPreset>> {
        return eqPresetDao.getBuiltInPresets().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getCustomPresets(): Flow<List<EqPreset>> {
        return eqPresetDao.getCustomPresets().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getPresetById(id: Long): EqPreset? {
        return eqPresetDao.getPresetById(id)?.toDomain()
    }

    override suspend fun getPresetByName(name: String): EqPreset? {
        return eqPresetDao.getPresetByName(name)?.toDomain()
    }

    override suspend fun insertPreset(preset: EqPreset): Long {
        return eqPresetDao.insertPreset(preset.toEntity())
    }

    override suspend fun updatePreset(preset: EqPreset) {
        eqPresetDao.updatePreset(preset.toEntity())
    }

    override suspend fun deleteCustomPreset(presetId: Long) {
        eqPresetDao.deleteCustomPreset(presetId)
    }

    override suspend fun seedBuiltInPresets() {
        val count = eqPresetDao.getBuiltInPresetCount()
        if (count > 0) return // Already seeded

        val builtInPresets = listOf(
            // Flat - neutral settings
            EqPresetEntity(
                name = "Flat",
                isCustom = false,
                band60Hz = 0, band250Hz = 0, band1kHz = 0, band4kHz = 0, band16kHz = 0,
                bassBoost = 0, virtualizer = 0
            ),
            // Bass Boost - enhanced low frequencies
            EqPresetEntity(
                name = "Bass Boost",
                isCustom = false,
                band60Hz = 600, band250Hz = 400, band1kHz = 0, band4kHz = 0, band16kHz = 0,
                bassBoost = 533, virtualizer = 0
            ),
            // Bass Reducer - reduced low frequencies
            EqPresetEntity(
                name = "Bass Reducer",
                isCustom = false,
                band60Hz = -600, band250Hz = -400, band1kHz = 0, band4kHz = 0, band16kHz = 0,
                bassBoost = 0, virtualizer = 0
            ),
            // Treble Boost - enhanced high frequencies
            EqPresetEntity(
                name = "Treble Boost",
                isCustom = false,
                band60Hz = 0, band250Hz = 0, band1kHz = 0, band4kHz = 400, band16kHz = 600,
                bassBoost = 0, virtualizer = 0
            ),
            // Treble Reducer - reduced high frequencies
            EqPresetEntity(
                name = "Treble Reducer",
                isCustom = false,
                band60Hz = 0, band250Hz = 0, band1kHz = 0, band4kHz = -400, band16kHz = -600,
                bassBoost = 0, virtualizer = 0
            ),
            // Vocal Boost - enhanced mid frequencies for vocals
            EqPresetEntity(
                name = "Vocal Boost",
                isCustom = false,
                band60Hz = -200, band250Hz = 0, band1kHz = 400, band4kHz = 200, band16kHz = 0,
                bassBoost = 0, virtualizer = 0
            ),
            // Rock - classic rock sound
            EqPresetEntity(
                name = "Rock",
                isCustom = false,
                band60Hz = 400, band250Hz = 200, band1kHz = -200, band4kHz = 200, band16kHz = 400,
                bassBoost = 267, virtualizer = 200
            ),
            // Pop - balanced for pop music
            EqPresetEntity(
                name = "Pop",
                isCustom = false,
                band60Hz = -200, band250Hz = 200, band1kHz = 400, band4kHz = 200, band16kHz = -200,
                bassBoost = 133, virtualizer = 300
            ),
            // Jazz - warm and spacious
            EqPresetEntity(
                name = "Jazz",
                isCustom = false,
                band60Hz = 300, band250Hz = 0, band1kHz = 200, band4kHz = 300, band16kHz = 400,
                bassBoost = 133, virtualizer = 250
            ),
            // Classical - natural and clear
            EqPresetEntity(
                name = "Classical",
                isCustom = false,
                band60Hz = 0, band250Hz = 0, band1kHz = 0, band4kHz = -200, band16kHz = -400,
                bassBoost = 0, virtualizer = 400
            ),
            // Hip Hop - deep bass and punchy
            EqPresetEntity(
                name = "Hip Hop",
                isCustom = false,
                band60Hz = 600, band250Hz = 400, band1kHz = 0, band4kHz = 200, band16kHz = 300,
                bassBoost = 667, virtualizer = 300
            ),
            // Electronic - powerful bass and crisp highs
            EqPresetEntity(
                name = "Electronic",
                isCustom = false,
                band60Hz = 400, band250Hz = 200, band1kHz = 0, band4kHz = 300, band16kHz = 500,
                bassBoost = 400, virtualizer = 500
            )
        )

        eqPresetDao.insertPresets(builtInPresets)
    }
}
