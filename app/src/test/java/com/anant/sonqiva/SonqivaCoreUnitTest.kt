package com.anant.sonqiva

import com.anant.sonqiva.data.model.Album
import com.anant.sonqiva.data.model.Artist
import com.anant.sonqiva.data.model.PlaybackState
import com.anant.sonqiva.data.model.RepeatMode
import com.anant.sonqiva.data.model.Song
import com.anant.sonqiva.ui.navigation.Screen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SonqivaCoreUnitTest {

    @Test
    fun testSongDurationFormatting() {
        val songShort = Song(
            id = 1L,
            title = "Test Track",
            artist = "Artist",
            album = "Album",
            albumId = 1L,
            durationMs = 65000L // 1 min 5 sec
        )
        assertEquals("1:05", songShort.formattedDuration)

        val songLong = Song(
            id = 2L,
            title = "Long Track",
            artist = "Artist",
            album = "Album",
            albumId = 1L,
            durationMs = 3725000L // 1 hour 2 min 5 sec
        )
        assertEquals("1:02:05", songLong.formattedDuration)
    }

    @Test
    fun testPlaybackStateProgressCalculation() {
        val state = PlaybackState(
            currentPositionMs = 50000L,
            durationMs = 100000L
        )
        assertEquals(0.5f, state.progress, 0.001f)
        assertEquals("0:50", state.formattedCurrentPosition)
        assertEquals("1:40", state.formattedDuration)
    }

    @Test
    fun testNavigationRoutes() {
        assertEquals("home", Screen.Home.route)
        assertEquals("library", Screen.Library.route)
        assertEquals("folders", Screen.Folders.route)
        assertEquals("search", Screen.Search.route)
        assertEquals("settings", Screen.Settings.route)
        assertEquals("album_detail/42", Screen.AlbumDetail.createRoute(42L))
        assertEquals("artist_detail/10", Screen.ArtistDetail.createRoute(10L))
    }

    @Test
    fun testRepeatModeCycle() {
        var mode = RepeatMode.OFF
        mode = when (mode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        assertEquals(RepeatMode.ALL, mode)

        mode = when (mode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        assertEquals(RepeatMode.ONE, mode)

        mode = when (mode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        assertEquals(RepeatMode.OFF, mode)
    }

    @Test
    fun testAlbumAndArtistModel() {
        val album = Album(
            id = 101L,
            title = "Atmosphere",
            artist = "Solaris",
            songCount = 10,
            year = 2024
        )
        assertEquals("Atmosphere", album.title)
        assertEquals(10, album.songCount)

        val artist = Artist(
            id = 201L,
            name = "Solaris",
            songCount = 20,
            albumCount = 2
        )
        assertEquals("Solaris", artist.name)
        assertEquals(2, artist.albumCount)
    }
}
