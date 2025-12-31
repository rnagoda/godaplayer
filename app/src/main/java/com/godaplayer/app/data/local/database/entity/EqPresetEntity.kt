package com.godaplayer.app.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "eq_presets")
data class EqPresetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "is_custom")
    val isCustom: Boolean = true,

    // 5-band EQ values (-12 to +12 dB, stored as millibels)
    @ColumnInfo(name = "band_60hz")
    val band60Hz: Int = 0,

    @ColumnInfo(name = "band_250hz")
    val band250Hz: Int = 0,

    @ColumnInfo(name = "band_1khz")
    val band1kHz: Int = 0,

    @ColumnInfo(name = "band_4khz")
    val band4kHz: Int = 0,

    @ColumnInfo(name = "band_16khz")
    val band16kHz: Int = 0,

    // Bass boost (0-1000, mapped to 0-15 dB)
    @ColumnInfo(name = "bass_boost")
    val bassBoost: Int = 0,

    // Virtualizer (0-1000, mapped to 0-100%)
    @ColumnInfo(name = "virtualizer")
    val virtualizer: Int = 0,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
