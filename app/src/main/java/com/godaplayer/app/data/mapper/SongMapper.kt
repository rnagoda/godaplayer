package com.godaplayer.app.data.mapper

import com.godaplayer.app.data.local.database.entity.SongEntity
import com.godaplayer.app.domain.model.Song

fun SongEntity.toDomain(): Song = Song(
    id = id,
    filePath = filePath,
    fileName = fileName,
    title = title,
    artist = artist,
    album = album,
    durationMs = durationMs,
    fileSize = fileSize,
    dateAdded = dateAdded,
    lastPlayed = lastPlayed,
    playCount = playCount,
    trackNumber = trackNumber,
    year = year,
    genre = genre
)

fun Song.toEntity(): SongEntity = SongEntity(
    id = id,
    filePath = filePath,
    fileName = fileName,
    title = title,
    artist = artist,
    album = album,
    durationMs = durationMs,
    fileSize = fileSize,
    dateAdded = dateAdded,
    lastPlayed = lastPlayed,
    playCount = playCount,
    trackNumber = trackNumber,
    year = year,
    genre = genre
)

fun List<SongEntity>.toDomain(): List<Song> = map { it.toDomain() }
fun List<Song>.toEntity(): List<SongEntity> = map { it.toEntity() }
