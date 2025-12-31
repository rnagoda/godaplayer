package com.godaplayer.app.domain.model

import com.godaplayer.app.ui.components.formatDuration

data class Song(
    val id: Long = 0,
    val filePath: String,
    val fileName: String,
    val title: String? = null,
    val artist: String? = null,
    val album: String? = null,
    val durationMs: Long = 0,
    val fileSize: Long = 0,
    val dateAdded: Long = System.currentTimeMillis(),
    val lastPlayed: Long? = null,
    val playCount: Int = 0,
    val trackNumber: Int? = null,
    val year: Int? = null,
    val genre: String? = null
) {
    val displayTitle: String
        get() = title ?: fileName

    val displayArtist: String
        get() = artist ?: "Unknown Artist"

    val formattedDuration: String
        get() = formatDuration(durationMs)
}
