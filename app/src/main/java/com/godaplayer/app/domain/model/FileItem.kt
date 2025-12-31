package com.godaplayer.app.domain.model

data class FileItem(
    val path: String,
    val name: String,
    val isDirectory: Boolean,
    val size: Long = 0,
    val durationMs: Long? = null,
    val artist: String? = null,
    val title: String? = null
) {
    val displayName: String
        get() = if (isDirectory) "$name/" else name

    val formattedDuration: String?
        get() = durationMs?.let {
            val totalSeconds = it / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            "%d:%02d".format(minutes, seconds)
        }
}
