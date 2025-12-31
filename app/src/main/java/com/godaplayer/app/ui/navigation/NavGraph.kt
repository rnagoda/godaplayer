package com.godaplayer.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.godaplayer.app.ui.components.MiniPlayer
import com.godaplayer.app.ui.screens.browse.BrowseScreen
import com.godaplayer.app.ui.screens.equalizer.EqualizerScreen
import com.godaplayer.app.ui.screens.home.HomeScreen
import com.godaplayer.app.ui.screens.library.LibraryScreen
import com.godaplayer.app.ui.screens.nowplaying.NowPlayingScreen
import com.godaplayer.app.ui.screens.playlists.PlaylistDetailScreen
import com.godaplayer.app.ui.screens.playlists.PlaylistsScreen
import com.godaplayer.app.ui.screens.queue.QueueScreen
import com.godaplayer.app.ui.screens.settings.ScanFoldersScreen
import com.godaplayer.app.ui.screens.settings.SettingsScreen

@Composable
fun GodaPlayerNavHost(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Screens that show the bottom nav bar
    val bottomNavScreens = listOf(
        Screen.Home.route,
        Screen.Browse.route,
        Screen.Playlists.route,
        Screen.Settings.route,
        Screen.Library.route
    )
    val showBottomNav = currentRoute in bottomNavScreens

    // Screens that show the mini player
    val showMiniPlayer = showBottomNav && currentRoute != Screen.Home.route

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                Column {
                    if (showMiniPlayer) {
                        MiniPlayer(
                            onExpandClick = {
                                navController.navigate(Screen.NowPlaying.route)
                            }
                        )
                    }
                    BottomNavBar(
                        currentRoute = currentRoute,
                        onNavigate = { screen ->
                            navController.navigate(screen.route) {
                                popUpTo(Screen.Home.route) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        onNavigateToNowPlaying = {
                            navController.navigate(Screen.NowPlaying.route)
                        },
                        onNavigateToLibrary = {
                            navController.navigate(Screen.Library.route)
                        }
                    )
                }

                composable(Screen.Browse.route) {
                    BrowseScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.Playlists.route) {
                    PlaylistsScreen(
                        onNavigateToPlaylist = { playlistId ->
                            navController.navigate(Screen.PlaylistDetail.createRoute(playlistId))
                        }
                    )
                }

                composable(Screen.Settings.route) {
                    SettingsScreen(
                        onNavigateToEqualizer = {
                            navController.navigate(Screen.Equalizer.route)
                        },
                        onNavigateToScanFolders = {
                            navController.navigate(Screen.ScanFolders.route)
                        }
                    )
                }

                composable(Screen.NowPlaying.route) {
                    NowPlayingScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToQueue = {
                            navController.navigate(Screen.Queue.route)
                        },
                        onNavigateToEqualizer = {
                            navController.navigate(Screen.Equalizer.route)
                        }
                    )
                }

                composable(Screen.Queue.route) {
                    QueueScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.Equalizer.route) {
                    EqualizerScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.Library.route) {
                    LibraryScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.ScanFolders.route) {
                    ScanFoldersScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = Screen.PlaylistDetail.route,
                    arguments = listOf(
                        navArgument(Screen.PlaylistDetail.ARG_PLAYLIST_ID) {
                            type = NavType.LongType
                        }
                    )
                ) { backStackEntry ->
                    val playlistId = backStackEntry.arguments?.getLong(Screen.PlaylistDetail.ARG_PLAYLIST_ID) ?: 0L
                    PlaylistDetailScreen(
                        playlistId = playlistId,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
