package com.anant.sonqiva.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.anant.sonqiva.data.model.Album
import com.anant.sonqiva.data.model.Artist
import com.anant.sonqiva.data.model.FolderItem
import com.anant.sonqiva.data.model.PlaybackState
import com.anant.sonqiva.data.model.Song
import com.anant.sonqiva.ui.components.MiniPlayer
import com.anant.sonqiva.ui.folders.FoldersScreen
import com.anant.sonqiva.ui.home.HomeScreen
import com.anant.sonqiva.ui.library.LibraryScreen
import com.anant.sonqiva.ui.player.FullPlayerScreen
import com.anant.sonqiva.ui.search.SearchScreen
import com.anant.sonqiva.ui.settings.SettingsScreen
import com.anant.sonqiva.ui.theme.BackgroundDark

@Composable
fun SonqivaAppShell(
    songs: List<Song>,
    albums: List<Album>,
    artists: List<Artist>,
    folders: List<FolderItem>,
    currentFolder: FolderItem?,
    playbackState: PlaybackState,
    onSongClick: (Song) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onArtistClick: (Artist) -> Unit,
    onFolderClick: (FolderItem) -> Unit,
    onFolderBackClick: () -> Unit,
    onPlayAllClick: () -> Unit,
    onShuffleAllClick: () -> Unit,
    onPlayFolderClick: (FolderItem) -> Unit,
    onShuffleFolderClick: (FolderItem) -> Unit,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onSeekClick: (Long) -> Unit,
    onShuffleToggle: () -> Unit,
    onRepeatToggle: () -> Unit,
    onFavoriteToggle: (Song) -> Unit,
    onSpeedChange: (Float) -> Unit,
    onQueueItemClick: (Int) -> Unit,
    onRescanLibraryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    var isFullPlayerExpanded by remember { mutableStateOf(false) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isBottomBarVisible = currentRoute in Screen.bottomNavItems.map { it.route }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                if (isBottomBarVisible) {
                    SonqivaBottomNavBar(navController = navController)
                }
            },
            containerColor = BackgroundDark
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                NavHost(
                    navController = navController,
                    startDestination = Screen.Home.route,
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable(Screen.Home.route) {
                        HomeScreen(
                            songs = songs,
                            albums = albums,
                            playbackState = playbackState,
                            onSongClick = onSongClick,
                            onAlbumClick = { album ->
                                onAlbumClick(album)
                            },
                            onShuffleAllClick = onShuffleAllClick,
                            onFavoritesClick = {
                                navController.navigate(Screen.Library.route)
                            },
                            onSearchClick = {
                                navController.navigate(Screen.Search.route)
                            },
                            onFavoriteToggle = onFavoriteToggle
                        )
                    }

                    composable(Screen.Library.route) {
                        LibraryScreen(
                            songs = songs,
                            albums = albums,
                            artists = artists,
                            playbackState = playbackState,
                            onSongClick = onSongClick,
                            onAlbumClick = onAlbumClick,
                            onArtistClick = onArtistClick,
                            onPlayAllClick = onPlayAllClick,
                            onShuffleAllClick = onShuffleAllClick,
                            onFavoriteToggle = onFavoriteToggle
                        )
                    }

                    composable(Screen.Folders.route) {
                        FoldersScreen(
                            folders = folders,
                            currentFolder = currentFolder,
                            playbackState = playbackState,
                            onFolderClick = onFolderClick,
                            onBackClick = onFolderBackClick,
                            onSongClick = onSongClick,
                            onPlayFolderClick = onPlayFolderClick,
                            onShuffleFolderClick = onShuffleFolderClick
                        )
                    }

                    composable(Screen.Search.route) {
                        SearchScreen(
                            allSongs = songs,
                            playbackState = playbackState,
                            onSongClick = onSongClick
                        )
                    }

                    composable(Screen.Settings.route) {
                        SettingsScreen(
                            onRescanLibraryClick = onRescanLibraryClick
                        )
                    }
                }

                // Persistent Floating Mini-Player positioned right above the BottomNavBar
                if (playbackState.currentSong != null && !isFullPlayerExpanded) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp)
                    ) {
                        MiniPlayer(
                            playbackState = playbackState,
                            onPlayPauseClick = onPlayPauseClick,
                            onNextClick = onNextClick,
                            onExpandClick = { isFullPlayerExpanded = true }
                        )
                    }
                }
            }
        }

        // Full Player Modal Screen
        AnimatedVisibility(
            visible = isFullPlayerExpanded && playbackState.currentSong != null,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            FullPlayerScreen(
                playbackState = playbackState,
                onCollapse = { isFullPlayerExpanded = false },
                onPlayPause = onPlayPauseClick,
                onNext = onNextClick,
                onPrevious = onPreviousClick,
                onSeek = onSeekClick,
                onShuffleToggle = onShuffleToggle,
                onRepeatToggle = onRepeatToggle,
                onFavoriteToggle = {
                    playbackState.currentSong?.let { onFavoriteToggle(it) }
                },
                onSpeedChange = onSpeedChange,
                onQueueItemClick = onQueueItemClick
            )
        }
    }
}
