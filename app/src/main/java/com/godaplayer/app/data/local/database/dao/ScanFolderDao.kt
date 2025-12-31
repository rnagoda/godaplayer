package com.godaplayer.app.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.godaplayer.app.data.local.database.entity.ScanFolderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanFolderDao {

    @Query("SELECT * FROM scan_folders ORDER BY path ASC")
    fun getAllFolders(): Flow<List<ScanFolderEntity>>

    @Query("SELECT * FROM scan_folders WHERE enabled = 1 ORDER BY path ASC")
    fun getEnabledFolders(): Flow<List<ScanFolderEntity>>

    @Query("SELECT * FROM scan_folders WHERE enabled = 1")
    suspend fun getEnabledFoldersOnce(): List<ScanFolderEntity>

    @Query("SELECT * FROM scan_folders WHERE id = :id")
    suspend fun getFolderById(id: Long): ScanFolderEntity?

    @Query("SELECT * FROM scan_folders WHERE path = :path")
    suspend fun getFolderByPath(path: String): ScanFolderEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFolder(folder: ScanFolderEntity): Long

    @Update
    suspend fun updateFolder(folder: ScanFolderEntity)

    @Query("UPDATE scan_folders SET enabled = :enabled WHERE id = :folderId")
    suspend fun setFolderEnabled(folderId: Long, enabled: Boolean)

    @Query("UPDATE scan_folders SET last_scanned = :timestamp WHERE id = :folderId")
    suspend fun updateLastScanned(folderId: Long, timestamp: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteFolder(folder: ScanFolderEntity)

    @Query("DELETE FROM scan_folders WHERE id = :folderId")
    suspend fun deleteFolderById(folderId: Long)
}
