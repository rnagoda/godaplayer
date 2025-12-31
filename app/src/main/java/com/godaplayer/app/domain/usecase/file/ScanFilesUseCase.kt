package com.godaplayer.app.domain.usecase.file

import android.media.MediaMetadataRetriever
import com.godaplayer.app.domain.model.ScanFolder
import com.godaplayer.app.domain.model.Song
import com.godaplayer.app.domain.repository.ScanFolderRepository
import com.godaplayer.app.domain.repository.SongRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

data class ScanProgress(
    val isScanning: Boolean = false,
    val currentFolder: String = "",
    val filesScanned: Int = 0,
    val filesFound: Int = 0,
    val newSongsAdded: Int = 0
)

@Singleton
class ScanFilesUseCase @Inject constructor(
    private val songRepository: SongRepository,
    private val scanFolderRepository: ScanFolderRepository
) {
    private val supportedExtensions = setOf("mp3", "flac", "aac", "m4a", "ogg", "wav")

    private val _scanProgress = MutableStateFlow(ScanProgress())
    val scanProgress: Flow<ScanProgress> = _scanProgress.asStateFlow()

    suspend fun scanAllFolders(): ScanResult = withContext(Dispatchers.IO) {
        val folders = scanFolderRepository.getEnabledFoldersOnce()
        if (folders.isEmpty()) {
            return@withContext ScanResult(0, 0, 0)
        }

        _scanProgress.value = ScanProgress(isScanning = true)

        var totalFilesScanned = 0
        var totalFilesFound = 0
        var totalNewSongs = 0
        val allValidPaths = mutableListOf<String>()

        for (folder in folders) {
            _scanProgress.value = _scanProgress.value.copy(currentFolder = folder.path)

            val result = scanFolder(folder)
            totalFilesScanned += result.filesScanned
            totalFilesFound += result.filesFound
            totalNewSongs += result.newSongsAdded
            allValidPaths.addAll(result.validPaths)

            // Update last scanned time
            scanFolderRepository.updateLastScanned(folder.id)
        }

        // Remove songs that no longer exist
        if (allValidPaths.isNotEmpty()) {
            songRepository.deleteOrphanedSongs(allValidPaths)
        }

        _scanProgress.value = ScanProgress(isScanning = false)

        ScanResult(totalFilesScanned, totalFilesFound, totalNewSongs)
    }

    suspend fun scanSingleFolder(folderPath: String): ScanResult = withContext(Dispatchers.IO) {
        _scanProgress.value = ScanProgress(isScanning = true, currentFolder = folderPath)

        val folder = ScanFolder(path = folderPath)
        val result = scanFolder(folder)

        _scanProgress.value = ScanProgress(isScanning = false)

        ScanResult(result.filesScanned, result.filesFound, result.newSongsAdded)
    }

    private suspend fun scanFolder(folder: ScanFolder): FolderScanResult {
        val directory = File(folder.path)
        if (!directory.exists() || !directory.isDirectory) {
            return FolderScanResult(0, 0, 0, emptyList())
        }

        val audioFiles = mutableListOf<File>()
        findAudioFiles(directory, audioFiles)

        var filesScanned = 0
        var newSongsAdded = 0
        val validPaths = mutableListOf<String>()

        for (file in audioFiles) {
            filesScanned++
            validPaths.add(file.absolutePath)

            _scanProgress.value = _scanProgress.value.copy(
                filesScanned = _scanProgress.value.filesScanned + 1,
                filesFound = _scanProgress.value.filesFound + 1
            )

            // Check if song already exists
            val existingSong = songRepository.getSongByFilePath(file.absolutePath)
            if (existingSong == null) {
                val song = extractMetadata(file)
                songRepository.insertSong(song)
                newSongsAdded++
                _scanProgress.value = _scanProgress.value.copy(
                    newSongsAdded = _scanProgress.value.newSongsAdded + 1
                )
            }
        }

        return FolderScanResult(filesScanned, audioFiles.size, newSongsAdded, validPaths)
    }

    private fun findAudioFiles(directory: File, results: MutableList<File>) {
        val files = directory.listFiles() ?: return

        for (file in files) {
            if (file.isDirectory && !file.name.startsWith(".")) {
                findAudioFiles(file, results)
            } else if (isAudioFile(file)) {
                results.add(file)
            }
        }
    }

    private fun isAudioFile(file: File): Boolean {
        return file.extension.lowercase() in supportedExtensions
    }

    private fun extractMetadata(file: File): Song {
        var title: String? = null
        var artist: String? = null
        var album: String? = null
        var durationMs: Long = 0
        var trackNumber: Int? = null
        var year: Int? = null
        var genre: String? = null

        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(file.absolutePath)

            title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
            durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull() ?: 0
            trackNumber = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)
                ?.split("/")?.firstOrNull()?.toIntOrNull()
            year = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)
                ?.toIntOrNull()
            genre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)

            retriever.release()
        } catch (e: Exception) {
            // Ignore metadata extraction errors
        }

        return Song(
            filePath = file.absolutePath,
            fileName = file.name,
            title = title,
            artist = artist,
            album = album,
            durationMs = durationMs,
            fileSize = file.length(),
            trackNumber = trackNumber,
            year = year,
            genre = genre
        )
    }

    data class ScanResult(
        val filesScanned: Int,
        val filesFound: Int,
        val newSongsAdded: Int
    )

    private data class FolderScanResult(
        val filesScanned: Int,
        val filesFound: Int,
        val newSongsAdded: Int,
        val validPaths: List<String>
    )
}
