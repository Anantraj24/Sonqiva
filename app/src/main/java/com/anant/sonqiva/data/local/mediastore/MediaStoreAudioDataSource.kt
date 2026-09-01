package com.anant.sonqiva.data.local.mediastore

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.anant.sonqiva.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File

class MediaStoreAudioDataSource(private val context: Context) {

    fun scanAudioFiles(): Flow<List<Song>> = flow {
        val songsList = mutableListOf<Song>()

        val collectionUri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.MIME_TYPE
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= 10000"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        try {
            context.contentResolver.query(
                collectionUri,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
                val trackColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)

                var batchCount = 0

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn) ?: "Unknown Track"
                    val artist = cursor.getString(artistColumn) ?: "Unknown Artist"
                    val album = cursor.getString(albumColumn) ?: "Unknown Album"
                    val albumId = cursor.getLong(albumIdColumn)
                    val duration = cursor.getLong(durationColumn)
                    val dataPath = cursor.getString(dataColumn) ?: ""
                    val dateAdded = cursor.getLong(dateAddedColumn)
                    val track = cursor.getInt(trackColumn)
                    val size = cursor.getLong(sizeColumn)
                    val mimeType = cursor.getString(mimeTypeColumn) ?: "audio/mpeg"

                    val mediaUri = ContentUris.withAppendedId(collectionUri, id)
                    val albumArtUri = ContentUris.withAppendedId(
                        Uri.parse("content://media/external/audio/albumart"),
                        albumId
                    )

                    val parentFolder = if (dataPath.isNotEmpty()) {
                        try {
                            File(dataPath).parent ?: ""
                        } catch (e: Exception) {
                            ""
                        }
                    } else ""

                    val song = Song(
                        id = id,
                        mediaUri = mediaUri,
                        title = title.ifBlank { "Unknown Track" },
                        artist = if (artist.contains("<unknown>", ignoreCase = true)) "Unknown Artist" else artist,
                        album = if (album.contains("<unknown>", ignoreCase = true)) "Unknown Album" else album,
                        albumId = albumId,
                        albumArtUri = albumArtUri,
                        durationMs = duration,
                        folderPath = parentFolder,
                        dateAdded = dateAdded,
                        trackNumber = track,
                        sizeBytes = size,
                        mimeType = mimeType
                    )

                    songsList.add(song)
                    batchCount++

                    // Emit progressively in chunks of 50 songs so the UI is immediately interactive
                    if (batchCount % 50 == 0) {
                        emit(songsList.toList())
                    }
                }

                // Final emission
                emit(songsList.toList())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emit(songsList.toList())
        }
    }.flowOn(Dispatchers.IO)
}
