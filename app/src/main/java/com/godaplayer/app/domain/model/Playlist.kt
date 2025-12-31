package com.godaplayer.app.domain.model

data class Playlist(
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastPlayed: Long? = null,
    val songCount: Int = 0,
    val totalDurationMs: Long = 0
) {
    val formattedDuration: String
        get() {
            val totalMinutes = totalDurationMs / 1000 / 60
            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60
            return if (hours > 0) {
                "${hours}h ${minutes}m"
            } else {
                "${minutes}m"
            }
        }
}

data class PlaylistWithSongs(
    val playlist: Playlist,
    val songs: List<Song>
)
