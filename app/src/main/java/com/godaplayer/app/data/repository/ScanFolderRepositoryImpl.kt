package com.godaplayer.app.data.repository

import com.godaplayer.app.data.local.database.dao.ScanFolderDao
import com.godaplayer.app.data.local.database.entity.ScanFolderEntity
import com.godaplayer.app.data.mapper.toDomain
import com.godaplayer.app.domain.model.ScanFolder
import com.godaplayer.app.domain.repository.ScanFolderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScanFolderRepositoryImpl @Inject constructor(
    private val scanFolderDao: ScanFolderDao
) : ScanFolderRepository {

    override fun getAllFolders(): Flow<List<ScanFolder>> {
        return scanFolderDao.getAllFolders().map { it.toDomain() }
    }

    override fun getEnabledFolders(): Flow<List<ScanFolder>> {
        return scanFolderDao.getEnabledFolders().map { it.toDomain() }
    }

    override suspend fun getEnabledFoldersOnce(): List<ScanFolder> {
        return scanFolderDao.getEnabledFoldersOnce().toDomain()
    }

    override suspend fun getFolderById(id: Long): ScanFolder? {
        return scanFolderDao.getFolderById(id)?.toDomain()
    }

    override suspend fun getFolderByPath(path: String): ScanFolder? {
        return scanFolderDao.getFolderByPath(path)?.toDomain()
    }

    override suspend fun addFolder(path: String): Long {
        return scanFolderDao.insertFolder(ScanFolderEntity(path = path))
    }

    override suspend fun setFolderEnabled(folderId: Long, enabled: Boolean) {
        scanFolderDao.setFolderEnabled(folderId, enabled)
    }

    override suspend fun updateLastScanned(folderId: Long) {
        scanFolderDao.updateLastScanned(folderId)
    }

    override suspend fun deleteFolder(folderId: Long) {
        scanFolderDao.deleteFolderById(folderId)
    }
}
