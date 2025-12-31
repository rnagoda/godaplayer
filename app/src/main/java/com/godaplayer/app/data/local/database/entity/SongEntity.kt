package com.godaplayer.app.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "songs",
    indices = [Index(value = ["file_path"], unique = true)]
)
data class SongEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "file_path")
    val filePath: String,

    @ColumnInfo(name = "file_name")
    val fileName: String,

    @ColumnInfo(name = "title")
    val title: String? = null,

    @ColumnInfo(name = "artist")
    val artist: String? = null,

    @ColumnInfo(name = "album")
    val album: String? = null,

    @ColumnInfo(name = "duration_ms")
    val durationMs: Long = 0,

    @ColumnInfo(name = "file_size")
    val fileSize: Long = 0,

    @ColumnInfo(name = "date_added")
    val dateAdded: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "last_played")
    val lastPlayed: Long? = null,

    @ColumnInfo(name = "play_count")
    val playCount: Int = 0,

    @ColumnInfo(name = "track_number")
    val trackNumber: Int? = null,

    @ColumnInfo(name = "year")
    val year: Int? = null,

    @ColumnInfo(name = "genre")
    val genre: String? = null
)
