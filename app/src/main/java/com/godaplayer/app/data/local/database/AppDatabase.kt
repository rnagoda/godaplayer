package com.godaplayer.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.godaplayer.app.data.local.database.dao.EqPresetDao
import com.godaplayer.app.data.local.database.dao.PlaylistDao
import com.godaplayer.app.data.local.database.dao.PlaylistSongDao
import com.godaplayer.app.data.local.database.dao.ScanFolderDao
import com.godaplayer.app.data.local.database.dao.SongDao
import com.godaplayer.app.data.local.database.entity.EqPresetEntity
import com.godaplayer.app.data.local.database.entity.PlaylistEntity
import com.godaplayer.app.data.local.database.entity.PlaylistSongEntity
import com.godaplayer.app.data.local.database.entity.ScanFolderEntity
import com.godaplayer.app.data.local.database.entity.SongEntity

@Database(
    entities = [
        SongEntity::class,
        PlaylistEntity::class,
        PlaylistSongEntity::class,
        ScanFolderEntity::class,
        EqPresetEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun playlistSongDao(): PlaylistSongDao
    abstract fun scanFolderDao(): ScanFolderDao
    abstract fun eqPresetDao(): EqPresetDao

    companion object {
        const val DATABASE_NAME = "godaplayer_db"
    }
}
