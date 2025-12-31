package com.godaplayer.app.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.godaplayer.app.data.local.database.entity.EqPresetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EqPresetDao {

    @Query("SELECT * FROM eq_presets ORDER BY is_custom ASC, name COLLATE NOCASE ASC")
    fun getAllPresets(): Flow<List<EqPresetEntity>>

    @Query("SELECT * FROM eq_presets WHERE is_custom = 0 ORDER BY name COLLATE NOCASE ASC")
    fun getBuiltInPresets(): Flow<List<EqPresetEntity>>

    @Query("SELECT * FROM eq_presets WHERE is_custom = 1 ORDER BY name COLLATE NOCASE ASC")
    fun getCustomPresets(): Flow<List<EqPresetEntity>>

    @Query("SELECT * FROM eq_presets WHERE id = :id")
    suspend fun getPresetById(id: Long): EqPresetEntity?

    @Query("SELECT * FROM eq_presets WHERE name = :name")
    suspend fun getPresetByName(name: String): EqPresetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: EqPresetEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPresets(presets: List<EqPresetEntity>)

    @Update
    suspend fun updatePreset(preset: EqPresetEntity)

    @Delete
    suspend fun deletePreset(preset: EqPresetEntity)

    @Query("DELETE FROM eq_presets WHERE id = :presetId AND is_custom = 1")
    suspend fun deleteCustomPreset(presetId: Long)

    @Query("SELECT COUNT(*) FROM eq_presets WHERE is_custom = 0")
    suspend fun getBuiltInPresetCount(): Int
}
