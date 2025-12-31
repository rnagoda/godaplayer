package com.godaplayer.app.domain.usecase.export

import com.godaplayer.app.domain.repository.PlaylistRepository
import java.io.OutputStream
import javax.inject.Inject

class ExportPlaylistM3UUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    suspend operator fun invoke(playlistId: Long, outputStream: OutputStream): Result<Unit> {
        return try {
            val playlist = playlistRepository.getPlaylistByIdOnce(playlistId)
                ?: return Result.failure(Exception("Playlist not found"))

            val songs = playlistRepository.getSongsForPlaylistOnce(playlistId)

            outputStream.bufferedWriter().use { writer ->
                // Write M3U header
                writer.write("#EXTM3U\n")
                writer.write("#PLAYLIST:${playlist.name}\n")

                // Write each song
                songs.forEach { song ->
                    val durationSeconds = (song.durationMs / 1000).toInt()
                    val displayName = if (!song.artist.isNullOrEmpty()) {
                        "${song.artist} - ${song.displayTitle}"
                    } else {
                        song.displayTitle
                    }

                    writer.write("#EXTINF:$durationSeconds,$displayName\n")
                    writer.write("${song.filePath}\n")
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun exportAllPlaylists(
        outputStreamProvider: suspend (playlistName: String) -> OutputStream?
    ): Result<ExportAllResult> {
        return try {
            var successCount = 0
            var failureCount = 0
            val failures = mutableListOf<String>()

            playlistRepository.getAllPlaylists().collect { playlists ->
                playlists.forEach { playlist ->
                    try {
                        val outputStream = outputStreamProvider(sanitizeFileName(playlist.name))
                        if (outputStream != null) {
                            invoke(playlist.id, outputStream).getOrThrow()
                            successCount++
                        } else {
                            failureCount++
                            failures.add(playlist.name)
                        }
                    } catch (e: Exception) {
                        failureCount++
                        failures.add(playlist.name)
                    }
                }
            }

            Result.success(ExportAllResult(successCount, failureCount, failures))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun sanitizeFileName(name: String): String {
        return name.replace(Regex("[^a-zA-Z0-9._-]"), "_")
    }
}

data class ExportAllResult(
    val successCount: Int,
    val failureCount: Int,
    val failedPlaylists: List<String>
)
