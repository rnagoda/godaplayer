package com.godaplayer.app.data.mapper

import com.godaplayer.app.data.local.database.entity.EqPresetEntity
import com.godaplayer.app.domain.model.EqPreset

fun EqPresetEntity.toDomain(): EqPreset {
    return EqPreset(
        id = id,
        name = name,
        isCustom = isCustom,
        band60Hz = band60Hz,
        band250Hz = band250Hz,
        band1kHz = band1kHz,
        band4kHz = band4kHz,
        band16kHz = band16kHz,
        bassBoost = bassBoost,
        virtualizer = virtualizer,
        createdAt = createdAt
    )
}

fun EqPreset.toEntity(): EqPresetEntity {
    return EqPresetEntity(
        id = id,
        name = name,
        isCustom = isCustom,
        band60Hz = band60Hz,
        band250Hz = band250Hz,
        band1kHz = band1kHz,
        band4kHz = band4kHz,
        band16kHz = band16kHz,
        bassBoost = bassBoost,
        virtualizer = virtualizer,
        createdAt = createdAt
    )
}
