package com.anant.sonqiva.data.model

import android.net.Uri

data class Album(
    val id: Long,
    val title: String,
    val artist: String,
    val artworkUri: Uri? = null,
    val songCount: Int = 0,
    val year: Int = 0,
    val songs: List<Song> = emptyList()
)

data class Artist(
    val id: Long,
    val name: String,
    val artworkUri: Uri? = null,
    val songCount: Int = 0,
    val albumCount: Int = 0,
    val songs: List<Song> = emptyList()
)

data class Genre(
    val id: Long,
    val name: String,
    val songCount: Int = 0,
    val songs: List<Song> = emptyList()
)

data class FolderItem(
    val name: String,
    val path: String,
    val parentPath: String? = null,
    val songCount: Int = 0,
    val subFolderCount: Int = 0,
    val songs: List<Song> = emptyList(),
    val subFolders: List<FolderItem> = emptyList()
)

data class Playlist(
    val id: Long,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val songCount: Int = 0,
    val coverArtUri: Uri? = null,
    val songs: List<Song> = emptyList()
)

enum class RepeatMode {
    OFF,
    ALL,
    ONE
}

data class PlaybackState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val isShuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val queue: List<Song> = emptyList(),
    val queueIndex: Int = -1,
    val isFavorite: Boolean = false
) {
    val progress: Float
        get() = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f

    val formattedCurrentPosition: String
        get() {
            val totalSeconds = currentPositionMs / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format("%d:%02d", minutes, seconds)
        }

    val formattedDuration: String
        get() {
            val totalSeconds = durationMs / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format("%d:%02d", minutes, seconds)
        }
}
