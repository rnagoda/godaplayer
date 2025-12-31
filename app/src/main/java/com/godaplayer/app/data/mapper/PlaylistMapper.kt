package com.godaplayer.app.data.mapper

import com.godaplayer.app.data.local.database.dao.PlaylistWithSongCount
import com.godaplayer.app.data.local.database.entity.PlaylistEntity
import com.godaplayer.app.domain.model.Playlist

fun PlaylistEntity.toDomain(songCount: Int = 0, totalDurationMs: Long = 0): Playlist = Playlist(
    id = id,
    name = name,
    description = description,
    createdAt = createdAt,
    updatedAt = updatedAt,
    lastPlayed = lastPlayed,
    songCount = songCount,
    totalDurationMs = totalDurationMs
)

fun PlaylistWithSongCount.toDomain(): Playlist = Playlist(
    id = id,
    name = name,
    description = description,
    createdAt = createdAt,
    updatedAt = updatedAt,
    lastPlayed = lastPlayed,
    songCount = songCount,
    totalDurationMs = totalDurationMs
)

fun Playlist.toEntity(): PlaylistEntity = PlaylistEntity(
    id = id,
    name = name,
    description = description,
    createdAt = createdAt,
    updatedAt = updatedAt,
    lastPlayed = lastPlayed
)

fun List<PlaylistWithSongCount>.toDomain(): List<Playlist> = map { it.toDomain() }
