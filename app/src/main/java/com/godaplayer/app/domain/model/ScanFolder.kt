package com.godaplayer.app.domain.model

data class ScanFolder(
    val id: Long = 0,
    val path: String,
    val enabled: Boolean = true,
    val lastScanned: Long? = null
) {
    val displayName: String
        get() = path.substringAfterLast("/")
}
