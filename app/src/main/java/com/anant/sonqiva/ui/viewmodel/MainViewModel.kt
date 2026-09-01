package com.anant.sonqiva.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.anant.sonqiva.data.local.database.FavoriteEntity
import com.anant.sonqiva.data.local.database.PlaybackHistoryEntity
import com.anant.sonqiva.data.local.database.SonqivaDatabase
import com.anant.sonqiva.data.local.datastore.UserPreferencesRepository
import com.anant.sonqiva.data.local.mediastore.MediaStoreAudioDataSource
import com.anant.sonqiva.data.model.Album
import com.anant.sonqiva.data.model.Artist
import com.anant.sonqiva.data.model.FolderItem
import com.anant.sonqiva.data.model.PlaybackState
import com.anant.sonqiva.data.model.Song
import com.anant.sonqiva.data.repository.AudioRepository
import com.anant.sonqiva.player.controller.PlaybackController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database: SonqivaDatabase = Room.databaseBuilder(
        application,
        SonqivaDatabase::class.java,
        "sonqiva_database"
    ).fallbackToDestructiveMigration().build()

    private val mediaStoreDataSource = MediaStoreAudioDataSource(application)
    private val audioRepository = AudioRepository(mediaStoreDataSource)
    private val preferencesRepository = UserPreferencesRepository(application)
    val playbackController = PlaybackController(application)

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    private val _albums = MutableStateFlow<List<Album>>(emptyList())
    val albums: StateFlow<List<Album>> = _albums.asStateFlow()

    private val _artists = MutableStateFlow<List<Artist>>(emptyList())
    val artists: StateFlow<List<Artist>> = _artists.asStateFlow()

    private val _folders = MutableStateFlow<List<FolderItem>>(emptyList())
    val folders: StateFlow<List<FolderItem>> = _folders.asStateFlow()

    private val _currentFolder = MutableStateFlow<FolderItem?>(null)
    val currentFolder: StateFlow<FolderItem?> = _currentFolder.asStateFlow()

    val playbackState: StateFlow<PlaybackState> = playbackController.playbackState

    val favoriteIds: StateFlow<List<Long>> = database.favoriteDao().getAllFavoriteSongIds()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val lowMemoryMode: StateFlow<Boolean> = preferencesRepository.lowMemoryModeFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    init {
        observeFavoritesAndSongs()
        observeStoredPreferences()
    }

    fun loadAudioLibrary() {
        viewModelScope.launch {
            audioRepository.getSongs().collect { scannedSongs ->
                _songs.value = scannedSongs
                _albums.value = audioRepository.getAlbums(scannedSongs)
                _artists.value = audioRepository.getArtists(scannedSongs)
                _folders.value = audioRepository.getFolderHierarchy(scannedSongs)
            }
        }
    }

    private fun observeFavoritesAndSongs() {
        viewModelScope.launch {
            combine(_songs, favoriteIds) { songList, favIds ->
                val favSet = favIds.toSet()
                songList.map { it.copy(isFavorite = favSet.contains(it.id)) }
            }.collect { updatedSongs ->
                _songs.value = updatedSongs
            }
        }
    }

    private fun observeStoredPreferences() {
        viewModelScope.launch {
            preferencesRepository.playbackSpeedFlow.collect { savedSpeed ->
                playbackController.setPlaybackSpeed(savedSpeed)
            }
        }
    }

    fun playSong(song: Song, queue: List<Song> = _songs.value) {
        playbackController.playSong(song, queue)
        recordHistory(song)
    }

    fun playNext(song: Song) {
        playbackController.playNext(song)
    }

    fun addToQueue(song: Song) {
        playbackController.addToQueue(song)
    }

    fun playPause() {
        playbackController.playPause()
    }

    fun seekTo(positionMs: Long) {
        playbackController.seekTo(positionMs)
    }

    fun skipToNext() {
        playbackController.skipToNext()
        playbackState.value.currentSong?.let { recordHistory(it) }
    }

    fun skipToPrevious() {
        playbackController.skipToPrevious()
        playbackState.value.currentSong?.let { recordHistory(it) }
    }

    fun skipToQueueItem(index: Int) {
        playbackController.skipToQueueItem(index)
    }

    fun toggleShuffle() {
        playbackController.toggleShuffle()
    }

    fun toggleRepeat() {
        playbackController.toggleRepeat()
    }

    fun setPlaybackSpeed(speed: Float) {
        playbackController.setPlaybackSpeed(speed)
        viewModelScope.launch {
            preferencesRepository.setPlaybackSpeed(speed)
        }
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (song.isFavorite) {
                    database.favoriteDao().deleteFavorite(song.id)
                } else {
                    database.favoriteDao().insertFavorite(FavoriteEntity(songId = song.id))
                }
            }
        }
    }

    fun setLowMemoryMode(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setLowMemoryMode(enabled)
        }
    }

    fun selectFolder(folder: FolderItem) {
        _currentFolder.value = folder
    }

    fun clearSelectedFolder() {
        _currentFolder.value = null
    }

    private fun recordHistory(song: Song) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                database.historyDao().insertOrUpdateHistory(
                    PlaybackHistoryEntity(
                        songId = song.id,
                        lastPlayedTimestamp = System.currentTimeMillis(),
                        lastPositionMs = 0L
                    )
                )
            }
            preferencesRepository.saveLastPlaybackState(song.id, 0L)
        }
    }

    override fun onCleared() {
        playbackController.release()
        super.onCleared()
    }
}
