package com.godaplayer.app.domain.repository

import com.godaplayer.app.domain.model.ScanFolder
import kotlinx.coroutines.flow.Flow

interface ScanFolderRepository {
    fun getAllFolders(): Flow<List<ScanFolder>>
    fun getEnabledFolders(): Flow<List<ScanFolder>>

    suspend fun getEnabledFoldersOnce(): List<ScanFolder>
    suspend fun getFolderById(id: Long): ScanFolder?
    suspend fun getFolderByPath(path: String): ScanFolder?
    suspend fun addFolder(path: String): Long
    suspend fun setFolderEnabled(folderId: Long, enabled: Boolean)
    suspend fun updateLastScanned(folderId: Long)
    suspend fun deleteFolder(folderId: Long)
}
