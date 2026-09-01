package com.anant.sonqiva.data.model

import android.net.Uri

object MockData {
    val sampleSongs = listOf(
        Song(
            id = 1L,
            mediaUri = Uri.parse("file:///music/song1.mp3"),
            title = "Midnight Echoes",
            artist = "Aether",
            album = "Cosmic Horizons",
            albumId = 101L,
            durationMs = 214000L,
            folderPath = "/storage/emulated/0/Music/Chill",
            isFavorite = true
        ),
        Song(
            id = 2L,
            mediaUri = Uri.parse("file:///music/song2.mp3"),
            title = "Violet Waves",
            artist = "Solaris",
            album = "Nebula Dream",
            albumId = 102L,
            durationMs = 185000L,
            folderPath = "/storage/emulated/0/Music/Electronic",
            isFavorite = false
        ),
        Song(
            id = 3L,
            mediaUri = Uri.parse("file:///music/song3.mp3"),
            title = "Atmosphere",
            artist = "Komorebi",
            album = "Deep State",
            albumId = 103L,
            durationMs = 248000L,
            folderPath = "/storage/emulated/0/Music/Chill",
            isFavorite = true
        ),
        Song(
            id = 4L,
            mediaUri = Uri.parse("file:///music/song4.mp3"),
            title = "Starlight Serenade",
            artist = "Luna Nova",
            album = "Silent Galaxy",
            albumId = 104L,
            durationMs = 205000L,
            folderPath = "/storage/emulated/0/Music/Acoustic",
            isFavorite = false
        )
    )

    val sampleAlbums = listOf(
        Album(
            id = 101L,
            title = "Cosmic Horizons",
            artist = "Aether",
            songCount = 8,
            year = 2024
        ),
        Album(
            id = 102L,
            title = "Nebula Dream",
            artist = "Solaris",
            songCount = 12,
            year = 2023
        ),
        Album(
            id = 103L,
            title = "Deep State",
            artist = "Komorebi",
            songCount = 6,
            year = 2024
        )
    )

    val sampleArtists = listOf(
        Artist(
            id = 201L,
            name = "Aether",
            songCount = 8,
            albumCount = 1
        ),
        Artist(
            id = 202L,
            name = "Solaris",
            songCount = 12,
            albumCount = 2
        ),
        Artist(
            id = 203L,
            name = "Komorebi",
            songCount = 6,
            albumCount = 1
        )
    )

    val sampleFolders = listOf(
        FolderItem(
            name = "Music",
            path = "/storage/emulated/0/Music",
            songCount = 26,
            subFolderCount = 3,
            subFolders = listOf(
                FolderItem(
                    name = "Chill",
                    path = "/storage/emulated/0/Music/Chill",
                    songCount = 14,
                    songs = sampleSongs.filter { it.folderPath.endsWith("Chill") }
                ),
                FolderItem(
                    name = "Electronic",
                    path = "/storage/emulated/0/Music/Electronic",
                    songCount = 12,
                    songs = sampleSongs.filter { it.folderPath.endsWith("Electronic") }
                )
            )
        ),
        FolderItem(
            name = "Downloads",
            path = "/storage/emulated/0/Download",
            songCount = 8
        )
    )
}
