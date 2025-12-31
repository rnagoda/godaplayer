package com.godaplayer.app.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Browse : Screen("browse")
    data object Playlists : Screen("playlists")
    data object Settings : Screen("settings")
    data object NowPlaying : Screen("now_playing")
    data object Queue : Screen("queue")
    data object Equalizer : Screen("equalizer")
    data object Library : Screen("library")
    data object ScanFolders : Screen("scan_folders")

    data object PlaylistDetail : Screen("playlist/{playlistId}") {
        fun createRoute(playlistId: Long) = "playlist/$playlistId"
        const val ARG_PLAYLIST_ID = "playlistId"
    }

    data object SongInfo : Screen("song_info/{songId}") {
        fun createRoute(songId: Long) = "song_info/$songId"
        const val ARG_SONG_ID = "songId"
    }
}
