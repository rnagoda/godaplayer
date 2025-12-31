package com.godaplayer.app.data.mapper

import com.godaplayer.app.data.local.database.entity.ScanFolderEntity
import com.godaplayer.app.domain.model.ScanFolder

fun ScanFolderEntity.toDomain(): ScanFolder = ScanFolder(
    id = id,
    path = path,
    enabled = enabled,
    lastScanned = lastScanned
)

fun ScanFolder.toEntity(): ScanFolderEntity = ScanFolderEntity(
    id = id,
    path = path,
    enabled = enabled,
    lastScanned = lastScanned
)

fun List<ScanFolderEntity>.toDomain(): List<ScanFolder> = map { it.toDomain() }
