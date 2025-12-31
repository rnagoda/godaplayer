package com.godaplayer.app.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.godaplayer.app.data.local.database.AppDatabase
import com.godaplayer.app.data.local.database.dao.EqPresetDao
import com.godaplayer.app.data.local.database.dao.PlaylistDao
import com.godaplayer.app.data.local.database.dao.PlaylistSongDao
import com.godaplayer.app.data.local.database.dao.ScanFolderDao
import com.godaplayer.app.data.local.database.dao.SongDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .addCallback(object : RoomDatabase.Callback() {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    // Seed built-in EQ presets if they don't exist
                    seedBuiltInPresetsIfEmpty(db)
                }
            })
            .build()
    }

    private fun seedBuiltInPresetsIfEmpty(db: SupportSQLiteDatabase) {
        // Check if built-in presets already exist
        val cursor = db.query("SELECT COUNT(*) FROM eq_presets WHERE is_custom = 0")
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()

        if (count > 0) return // Already seeded

        val currentTime = System.currentTimeMillis()
        val presets = listOf(
            // name, band60Hz, band250Hz, band1kHz, band4kHz, band16kHz, bassBoost, virtualizer
            arrayOf("Flat", 0, 0, 0, 0, 0, 0, 0),
            arrayOf("Bass Boost", 600, 400, 0, 0, 0, 533, 0),
            arrayOf("Bass Reducer", -600, -400, 0, 0, 0, 0, 0),
            arrayOf("Treble Boost", 0, 0, 0, 400, 600, 0, 0),
            arrayOf("Treble Reducer", 0, 0, 0, -400, -600, 0, 0),
            arrayOf("Vocal Boost", -200, 0, 400, 200, 0, 0, 0),
            arrayOf("Rock", 400, 200, -200, 200, 400, 267, 200),
            arrayOf("Pop", -200, 200, 400, 200, -200, 133, 300),
            arrayOf("Jazz", 300, 0, 200, 300, 400, 133, 250),
            arrayOf("Classical", 0, 0, 0, -200, -400, 0, 400),
            arrayOf("Hip Hop", 600, 400, 0, 200, 300, 667, 300),
            arrayOf("Electronic", 400, 200, 0, 300, 500, 400, 500)
        )

        presets.forEach { preset ->
            db.execSQL(
                """
                INSERT INTO eq_presets (name, is_custom, band_60hz, band_250hz, band_1khz, band_4khz, band_16khz, bass_boost, virtualizer, created_at)
                VALUES (?, 0, ?, ?, ?, ?, ?, ?, ?, ?)
                """.trimIndent(),
                arrayOf(preset[0], preset[1], preset[2], preset[3], preset[4], preset[5], preset[6], preset[7], currentTime)
            )
        }
    }

    @Provides
    fun provideSongDao(database: AppDatabase): SongDao = database.songDao()

    @Provides
    fun providePlaylistDao(database: AppDatabase): PlaylistDao = database.playlistDao()

    @Provides
    fun providePlaylistSongDao(database: AppDatabase): PlaylistSongDao = database.playlistSongDao()

    @Provides
    fun provideScanFolderDao(database: AppDatabase): ScanFolderDao = database.scanFolderDao()

    @Provides
    fun provideEqPresetDao(database: AppDatabase): EqPresetDao = database.eqPresetDao()
}
