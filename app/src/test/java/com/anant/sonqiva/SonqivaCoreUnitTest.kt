package com.anant.sonqiva

import com.anant.sonqiva.data.local.database.PlaylistEntity
import com.anant.sonqiva.data.local.database.PlaylistSongEntity
import com.anant.sonqiva.data.model.Album
import com.anant.sonqiva.data.model.AlbumSortOrder
import com.anant.sonqiva.data.model.Artist
import com.anant.sonqiva.data.model.PlaybackState
import com.anant.sonqiva.data.model.RepeatMode
import com.anant.sonqiva.data.model.Song
import com.anant.sonqiva.data.model.SongSortOrder
import com.anant.sonqiva.ui.navigation.Screen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
    fun testSleepTimerFormatting() {
        val inactiveState = PlaybackState()
        assertNull(inactiveState.formattedSleepTimer)

        val activeState = PlaybackState(sleepTimerRemainingSeconds = 1795) // 29m 55s
        assertEquals("29:55", activeState.formattedSleepTimer)

        val endingState = PlaybackState(sleepTimerRemainingSeconds = 5)
        assertEquals("0:05", endingState.formattedSleepTimer)
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
        assertEquals("playlist_detail/5", Screen.PlaylistDetail.createRoute(5L))
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

    @Test
    fun testPlaylistEntities() {
        val playlist = PlaylistEntity(id = 10L, name = "Workout Beats", createdAt = 12345678L)
        assertEquals(10L, playlist.id)
        assertEquals("Workout Beats", playlist.name)

        val playlistSong = PlaylistSongEntity(playlistId = 10L, songId = 42L, addedAt = 12345678L)
        assertEquals(10L, playlistSong.playlistId)
        assertEquals(42L, playlistSong.songId)
    }

    @Test
    fun testSongSortingLogic() {
        val s1 = Song(id = 1L, title = "Zeta", artist = "Astro", album = "A", albumId = 1L, durationMs = 100000L, dateAdded = 100L)
        val s2 = Song(id = 2L, title = "Alpha", artist = "Bravo", album = "A", albumId = 1L, durationMs = 300000L, dateAdded = 500L)
        val s3 = Song(id = 3L, title = "Beta", artist = "Charlie", album = "A", albumId = 1L, durationMs = 200000L, dateAdded = 300L)
        val list = listOf(s1, s2, s3)

        val sortedTitleAsc = list.sortedBy { it.title.lowercase() }
        assertEquals("Alpha", sortedTitleAsc[0].title)
        assertEquals("Beta", sortedTitleAsc[1].title)
        assertEquals("Zeta", sortedTitleAsc[2].title)

        val sortedDateDesc = list.sortedByDescending { it.dateAdded }
        assertEquals("Alpha", sortedDateDesc[0].title) // 500
        assertEquals("Beta", sortedDateDesc[1].title)  // 300
        assertEquals("Zeta", sortedDateDesc[2].title)  // 100

        val sortedDurationDesc = list.sortedByDescending { it.durationMs }
        assertEquals("Alpha", sortedDurationDesc[0].title) // 300000
        assertEquals("Beta", sortedDurationDesc[1].title)     // 200000
        assertEquals("Zeta", sortedDurationDesc[2].title)     // 100000
    }

    @Test
    fun testMultiCategorySearchFiltering() {
        val songs = listOf(
            Song(id = 1L, title = "Starlight", artist = "Muse", album = "Black Holes", albumId = 1L),
            Song(id = 2L, title = "Supermassive Black Hole", artist = "Muse", album = "Black Holes", albumId = 1L),
            Song(id = 3L, title = "Solaris", artist = "Atmosphere", album = "Southsiders", albumId = 2L)
        )
        val query = "Muse"

        val matching = songs.filter {
            it.title.contains(query, ignoreCase = true) ||
            it.artist.contains(query, ignoreCase = true) ||
            it.album.contains(query, ignoreCase = true)
        }
        assertEquals(2, matching.size)
        assertTrue(matching.any { it.title == "Starlight" })
        assertTrue(matching.any { it.title == "Supermassive Black Hole" })
    }
}
