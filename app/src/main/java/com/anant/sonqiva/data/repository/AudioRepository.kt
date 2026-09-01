package com.anant.sonqiva.data.repository

import com.anant.sonqiva.data.local.mediastore.MediaStoreAudioDataSource
import com.anant.sonqiva.data.model.Album
import com.anant.sonqiva.data.model.Artist
import com.anant.sonqiva.data.model.FolderItem
import com.anant.sonqiva.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File

class AudioRepository(private val mediaStoreDataSource: MediaStoreAudioDataSource) {

    fun getSongs(): Flow<List<Song>> = mediaStoreDataSource.scanAudioFiles()

    suspend fun getAlbums(songs: List<Song>): List<Album> = withContext(Dispatchers.Default) {
        songs.groupBy { it.albumId }
            .map { (albumId, albumSongs) ->
                val first = albumSongs.first()
                Album(
                    id = albumId,
                    title = first.album,
                    artist = first.artist,
                    artworkUri = first.albumArtUri,
                    songCount = albumSongs.size,
                    songs = albumSongs
                )
            }
            .sortedBy { it.title.lowercase() }
    }

    suspend fun getArtists(songs: List<Song>): List<Artist> = withContext(Dispatchers.Default) {
        songs.groupBy { it.artist.lowercase() }
            .entries
            .mapIndexed { index, entry ->
                val artistSongs = entry.value
                val first = artistSongs.first()
                val uniqueAlbums = artistSongs.map { it.albumId }.distinct().size
                Artist(
                    id = index.toLong(),
                    name = first.artist,
                    artworkUri = first.albumArtUri,
                    songCount = artistSongs.size,
                    albumCount = uniqueAlbums,
                    songs = artistSongs
                )
            }
            .sortedBy { it.name.lowercase() }
    }

    suspend fun getFolderHierarchy(songs: List<Song>): List<FolderItem> = withContext(Dispatchers.Default) {
        val foldersMap = mutableMapOf<String, MutableList<Song>>()

        songs.forEach { song ->
            if (song.folderPath.isNotEmpty()) {
                foldersMap.getOrPut(song.folderPath) { mutableListOf() }.add(song)
            }
        }

        foldersMap.map { (path, folderSongs) ->
            val folderName = File(path).name.ifBlank { "Storage" }
            FolderItem(
                name = folderName,
                path = path,
                parentPath = File(path).parent,
                songCount = folderSongs.size,
                songs = folderSongs
            )
        }.sortedBy { it.name.lowercase() }
    }
}
