package com.anant.sonqiva

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.anant.sonqiva.data.model.FolderItem
import com.anant.sonqiva.data.model.MockData
import com.anant.sonqiva.data.model.PlaybackState
import com.anant.sonqiva.data.model.RepeatMode
import com.anant.sonqiva.data.model.Song
import com.anant.sonqiva.ui.navigation.SonqivaAppShell
import com.anant.sonqiva.ui.theme.SonqivaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SonqivaTheme {
                var songs by remember { mutableStateOf(MockData.sampleSongs) }
                val albums = remember { MockData.sampleAlbums }
                val artists = remember { MockData.sampleArtists }
                val folders = remember { MockData.sampleFolders }
                var currentFolder by remember { mutableStateOf<FolderItem?>(null) }

                var playbackState by remember {
                    mutableStateOf(
                        PlaybackState(
                            currentSong = MockData.sampleSongs.first(),
                            isPlaying = false,
                            currentPositionMs = 45000L,
                            durationMs = MockData.sampleSongs.first().durationMs,
                            queue = MockData.sampleSongs,
                            queueIndex = 0,
                            isFavorite = true
                        )
                    )
                }

                SonqivaAppShell(
                    songs = songs,
                    albums = albums,
                    artists = artists,
                    folders = folders,
                    currentFolder = currentFolder,
                    playbackState = playbackState,
                    onSongClick = { song ->
                        val index = songs.indexOfFirst { it.id == song.id }
                        playbackState = playbackState.copy(
                            currentSong = song,
                            isPlaying = true,
                            currentPositionMs = 0L,
                            durationMs = song.durationMs,
                            queue = songs,
                            queueIndex = if (index >= 0) index else 0,
                            isFavorite = song.isFavorite
                        )
                    },
                    onAlbumClick = { /* Will open album detail */ },
                    onArtistClick = { /* Will open artist detail */ },
                    onFolderClick = { folder ->
                        currentFolder = folder
                    },
                    onFolderBackClick = {
                        currentFolder = null
                    },
                    onPlayAllClick = {
                        songs.firstOrNull()?.let { firstSong ->
                            playbackState = playbackState.copy(
                                currentSong = firstSong,
                                isPlaying = true,
                                currentPositionMs = 0L,
                                durationMs = firstSong.durationMs,
                                queue = songs,
                                queueIndex = 0
                            )
                        }
                    },
                    onShuffleAllClick = {
                        val shuffled = songs.shuffled()
                        shuffled.firstOrNull()?.let { firstSong ->
                            playbackState = playbackState.copy(
                                currentSong = firstSong,
                                isPlaying = true,
                                currentPositionMs = 0L,
                                durationMs = firstSong.durationMs,
                                queue = shuffled,
                                queueIndex = 0,
                                isShuffleEnabled = true
                            )
                        }
                    },
                    onPlayFolderClick = { folder ->
                        folder.songs.firstOrNull()?.let { firstSong ->
                            playbackState = playbackState.copy(
                                currentSong = firstSong,
                                isPlaying = true,
                                currentPositionMs = 0L,
                                durationMs = firstSong.durationMs,
                                queue = folder.songs,
                                queueIndex = 0
                            )
                        }
                    },
                    onShuffleFolderClick = { folder ->
                        val shuffled = folder.songs.shuffled()
                        shuffled.firstOrNull()?.let { firstSong ->
                            playbackState = playbackState.copy(
                                currentSong = firstSong,
                                isPlaying = true,
                                currentPositionMs = 0L,
                                durationMs = firstSong.durationMs,
                                queue = shuffled,
                                queueIndex = 0,
                                isShuffleEnabled = true
                            )
                        }
                    },
                    onPlayPauseClick = {
                        playbackState = playbackState.copy(isPlaying = !playbackState.isPlaying)
                    },
                    onNextClick = {
                        if (playbackState.queue.isNotEmpty()) {
                            val nextIndex = (playbackState.queueIndex + 1) % playbackState.queue.size
                            val nextSong = playbackState.queue[nextIndex]
                            playbackState = playbackState.copy(
                                currentSong = nextSong,
                                queueIndex = nextIndex,
                                currentPositionMs = 0L,
                                durationMs = nextSong.durationMs,
                                isPlaying = true
                            )
                        }
                    },
                    onPreviousClick = {
                        if (playbackState.queue.isNotEmpty()) {
                            val prevIndex = if (playbackState.queueIndex > 0) playbackState.queueIndex - 1 else playbackState.queue.size - 1
                            val prevSong = playbackState.queue[prevIndex]
                            playbackState = playbackState.copy(
                                currentSong = prevSong,
                                queueIndex = prevIndex,
                                currentPositionMs = 0L,
                                durationMs = prevSong.durationMs,
                                isPlaying = true
                            )
                        }
                    },
                    onSeekClick = { newPos ->
                        playbackState = playbackState.copy(currentPositionMs = newPos)
                    },
                    onShuffleToggle = {
                        playbackState = playbackState.copy(isShuffleEnabled = !playbackState.isShuffleEnabled)
                    },
                    onRepeatToggle = {
                        val nextRepeat = when (playbackState.repeatMode) {
                            RepeatMode.OFF -> RepeatMode.ALL
                            RepeatMode.ALL -> RepeatMode.ONE
                            RepeatMode.ONE -> RepeatMode.OFF
                        }
                        playbackState = playbackState.copy(repeatMode = nextRepeat)
                    },
                    onFavoriteToggle = { song ->
                        val updatedSongs = songs.map {
                            if (it.id == song.id) it.copy(isFavorite = !it.isFavorite) else it
                        }
                        songs = updatedSongs
                        if (playbackState.currentSong?.id == song.id) {
                            playbackState = playbackState.copy(isFavorite = !playbackState.isFavorite)
                        }
                    },
                    onSpeedChange = { newSpeed ->
                        playbackState = playbackState.copy(playbackSpeed = newSpeed)
                    },
                    onQueueItemClick = { index ->
                        if (index in playbackState.queue.indices) {
                            val song = playbackState.queue[index]
                            playbackState = playbackState.copy(
                                currentSong = song,
                                queueIndex = index,
                                currentPositionMs = 0L,
                                durationMs = song.durationMs,
                                isPlaying = true
                            )
                        }
                    },
                    onRescanLibraryClick = {
                        // Will trigger MediaStore sync in Phase 4
                    }
                )
            }
        }
    }
}