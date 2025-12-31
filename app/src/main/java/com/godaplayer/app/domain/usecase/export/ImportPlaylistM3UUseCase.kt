package com.godaplayer.app.domain.usecase.export

import com.godaplayer.app.domain.repository.PlaylistRepository
import com.godaplayer.app.domain.repository.SongRepository
import java.io.InputStream
import javax.inject.Inject

class ImportPlaylistM3UUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    private val songRepository: SongRepository
) {
    suspend operator fun invoke(inputStream: InputStream): Result<ImportResult> {
        return try {
            var playlistName = "Imported Playlist"
            val filePaths = mutableListOf<String>()
            val unmatchedPaths = mutableListOf<String>()

            inputStream.bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    val trimmedLine = line.trim()
                    when {
                        trimmedLine.isEmpty() -> { /* Skip empty lines */ }
                        trimmedLine.startsWith("#EXTM3U") -> { /* Header, skip */ }
                        trimmedLine.startsWith("#PLAYLIST:") -> {
                            playlistName = trimmedLine.substringAfter("#PLAYLIST:").trim()
                        }
                        trimmedLine.startsWith("#EXTINF:") -> { /* Extended info, skip */ }
                        trimmedLine.startsWith("#") -> { /* Other comments, skip */ }
                        else -> {
                            // This is a file path
                            filePaths.add(trimmedLine)
                        }
                    }
                }
            }

            if (filePaths.isEmpty()) {
                return Result.failure(Exception("No tracks found in playlist file"))
            }

            // Create the playlist
            val playlistId = playlistRepository.createPlaylist(playlistName)

            // Match file paths to songs in our library
            val matchedSongIds = mutableListOf<Long>()

            filePaths.forEach { path ->
                val song = songRepository.getSongByFilePath(path)
                if (song != null) {
                    matchedSongIds.add(song.id)
                } else {
                    unmatchedPaths.add(path)
                }
            }

            // Add matched songs to playlist
            if (matchedSongIds.isNotEmpty()) {
                playlistRepository.addSongsToPlaylist(playlistId, matchedSongIds)
            }

            Result.success(
                ImportResult(
                    playlistId = playlistId,
                    playlistName = playlistName,
                    totalTracks = filePaths.size,
                    matchedTracks = matchedSongIds.size,
                    unmatchedPaths = unmatchedPaths
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class ImportResult(
    val playlistId: Long,
    val playlistName: String,
    val totalTracks: Int,
    val matchedTracks: Int,
    val unmatchedPaths: List<String>
) {
    val unmatchedCount: Int get() = unmatchedPaths.size
    val successRate: Float get() = if (totalTracks > 0) matchedTracks.toFloat() / totalTracks else 0f
}
