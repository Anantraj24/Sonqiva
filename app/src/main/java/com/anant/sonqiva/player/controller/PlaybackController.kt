package com.anant.sonqiva.player.controller

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.anant.sonqiva.data.model.PlaybackState
import com.anant.sonqiva.data.model.RepeatMode
import com.anant.sonqiva.data.model.Song
import com.anant.sonqiva.player.service.SonqivaMediaSessionService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PlaybackController(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private var currentQueue: List<Song> = emptyList()
    private var knownSongs: Map<Long, Song> = emptyMap()
    private var positionUpdateJob: Job? = null
    private var sleepTimerJob: Job? = null

    init {
        initializeController()
    }

    private fun initializeController() {
        try {
            val intent = Intent(context, SonqivaMediaSessionService::class.java)
            context.startService(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val sessionToken = SessionToken(
            context,
            ComponentName(context, SonqivaMediaSessionService::class.java)
        )

        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            try {
                mediaController = controllerFuture?.get()
                setupPlayerListener()
                updatePlaybackState()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, MoreExecutors.directExecutor())
    }

    private fun setupPlayerListener() {
        mediaController?.addListener(object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) {
                updatePlaybackState()
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updatePlaybackState()
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                updatePlaybackState()
            }

            override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
                updatePlaybackState()
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                updatePlaybackState()
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                updatePlaybackState()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                updatePlaybackState()
            }

            override fun onTimelineChanged(timeline: Timeline, reason: Int) {
                val controller = mediaController ?: return
                if (controller.mediaItemCount > 0) {
                    val rebuiltQueue = mutableListOf<Song>()
                    for (i in 0 until controller.mediaItemCount) {
                        val item = controller.getMediaItemAt(i)
                        val song = resolveSong(item) ?: mediaItemToSong(item)
                        rebuiltQueue.add(song)
                    }
                    currentQueue = rebuiltQueue
                }
                updatePlaybackState()
            }
        })
    }

    private fun startPositionUpdates() {
        if (positionUpdateJob?.isActive == true) return
        positionUpdateJob = scope.launch {
            while (isActive) {
                mediaController?.let { controller ->
                    if (controller.isPlaying) {
                        _playbackState.update {
                            it.copy(
                                currentPositionMs = controller.currentPosition.coerceAtLeast(0L),
                                durationMs = if (controller.duration > 0) controller.duration else it.durationMs
                            )
                        }
                    }
                }
                delay(400L)
            }
        }
    }

    private fun stopPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = null
    }

    fun onSongsUpdated(songs: List<Song>) {
        knownSongs = songs.associateBy { it.id }
        updatePlaybackState()
    }

    private fun songToMediaItem(song: Song): MediaItem {
        val extras = Bundle().apply {
            putLong("albumId", song.albumId)
            putLong("durationMs", song.durationMs)
            putString("folderPath", song.folderPath)
            putString("relativePath", song.relativePath)
            putLong("dateAdded", song.dateAdded)
            putInt("trackNumber", song.trackNumber)
            putLong("sizeBytes", song.sizeBytes)
            putString("mimeType", song.mimeType)
            putBoolean("isFavorite", song.isFavorite)
            putInt("playCount", song.playCount)
        }

        val metadata = MediaMetadata.Builder()
            .setTitle(song.title)
            .setArtist(song.artist)
            .setAlbumTitle(song.album)
            .setArtworkUri(song.albumArtUri)
            .setExtras(extras)
            .build()

        val builder = MediaItem.Builder()
            .setMediaId(song.id.toString())
            .setMediaMetadata(metadata)

        song.mediaUri?.let { uri ->
            builder.setUri(uri)
        }

        return builder.build()
    }

    private fun mediaItemToSong(mediaItem: MediaItem): Song {
        val id = mediaItem.mediaId.toLongOrNull() ?: 0L
        val metadata = mediaItem.mediaMetadata
        val title = metadata.title?.toString()?.ifBlank { "Unknown Track" } ?: "Unknown Track"
        val artist = metadata.artist?.toString()?.ifBlank { "Unknown Artist" } ?: "Unknown Artist"
        val album = metadata.albumTitle?.toString()?.ifBlank { "Unknown Album" } ?: "Unknown Album"
        val artworkUri = metadata.artworkUri
        val mediaUri = mediaItem.requestMetadata.mediaUri ?: mediaItem.localConfiguration?.uri

        val extras = metadata.extras
        val albumId = extras?.getLong("albumId", 0L) ?: 0L
        val durationMs = extras?.getLong("durationMs", 0L) ?: 0L
        val folderPath = extras?.getString("folderPath", "") ?: ""
        val relativePath = extras?.getString("relativePath", "") ?: ""
        val dateAdded = extras?.getLong("dateAdded", 0L) ?: 0L
        val trackNumber = extras?.getInt("trackNumber", 0) ?: 0
        val sizeBytes = extras?.getLong("sizeBytes", 0L) ?: 0L
        val mimeType = extras?.getString("mimeType", "audio/mpeg") ?: "audio/mpeg"
        val isFavorite = extras?.getBoolean("isFavorite", false) ?: false
        val playCount = extras?.getInt("playCount", 0) ?: 0

        return Song(
            id = id,
            mediaUri = mediaUri,
            title = title,
            artist = artist,
            album = album,
            albumId = albumId,
            albumArtUri = artworkUri,
            durationMs = durationMs,
            folderPath = folderPath,
            relativePath = relativePath,
            dateAdded = dateAdded,
            trackNumber = trackNumber,
            sizeBytes = sizeBytes,
            mimeType = mimeType,
            isFavorite = isFavorite,
            playCount = playCount
        )
    }

    private fun resolveSong(mediaItem: MediaItem?): Song? {
        if (mediaItem == null) return null
        val id = mediaItem.mediaId.toLongOrNull() ?: return mediaItemToSong(mediaItem)
        return currentQueue.firstOrNull { it.id == id }
            ?: knownSongs[id]
            ?: mediaItemToSong(mediaItem)
    }

    fun playSong(song: Song, queue: List<Song>) {
        val controller = mediaController ?: return
        currentQueue = queue

        val mediaItems = queue.map { songToMediaItem(it) }
        val targetIndex = queue.indexOfFirst { it.id == song.id }.coerceAtLeast(0)

        controller.setMediaItems(mediaItems, targetIndex, 0L)
        controller.prepare()
        controller.play()
        updatePlaybackState()
    }

    fun playNext(song: Song) {
        val controller = mediaController ?: return
        val currentIdx = controller.currentMediaItemIndex
        val insertIdx = if (currentIdx >= 0) currentIdx + 1 else 0

        val mediaItem = songToMediaItem(song)
        controller.addMediaItem(insertIdx, mediaItem)

        val mutableQ = currentQueue.toMutableList()
        if (insertIdx in 0..mutableQ.size) {
            mutableQ.add(insertIdx, song)
        } else {
            mutableQ.add(song)
        }
        currentQueue = mutableQ
        updatePlaybackState()
    }

    fun addToQueue(song: Song) {
        val controller = mediaController ?: return
        val mediaItem = songToMediaItem(song)
        controller.addMediaItem(mediaItem)

        val mutableQ = currentQueue.toMutableList()
        mutableQ.add(song)
        currentQueue = mutableQ
        updatePlaybackState()
    }

    fun playPause() {
        val controller = mediaController ?: return
        if (controller.isPlaying) {
            controller.pause()
        } else {
            controller.play()
        }
    }

    fun seekTo(positionMs: Long) {
        mediaController?.seekTo(positionMs)
        _playbackState.update { it.copy(currentPositionMs = positionMs) }
    }

    fun skipToNext() {
        mediaController?.let {
            if (it.hasNextMediaItem()) {
                it.seekToNextMediaItem()
            }
        }
    }

    fun skipToPrevious() {
        mediaController?.let {
            if (it.hasPreviousMediaItem()) {
                it.seekToPreviousMediaItem()
            }
        }
    }

    fun skipToQueueItem(index: Int) {
        mediaController?.let { controller ->
            if (index in 0 until controller.mediaItemCount) {
                controller.seekToDefaultPosition(index)
                controller.play()
            }
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        mediaController?.setPlaybackSpeed(speed)
        _playbackState.update { it.copy(playbackSpeed = speed) }
    }

    fun toggleShuffle() {
        mediaController?.let { controller ->
            val newShuffle = !controller.shuffleModeEnabled
            controller.shuffleModeEnabled = newShuffle
            _playbackState.update { it.copy(isShuffleEnabled = newShuffle) }
        }
    }

    fun toggleRepeat() {
        mediaController?.let { controller ->
            val nextMode = when (controller.repeatMode) {
                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_OFF
                else -> Player.REPEAT_MODE_OFF
            }
            controller.repeatMode = nextMode

            val appRepeatMode = when (nextMode) {
                Player.REPEAT_MODE_ALL -> RepeatMode.ALL
                Player.REPEAT_MODE_ONE -> RepeatMode.ONE
                else -> RepeatMode.OFF
            }
            _playbackState.update { it.copy(repeatMode = appRepeatMode) }
        }
    }

    fun startSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        if (minutes <= 0) {
            cancelSleepTimer()
            return
        }

        var remainingSeconds = minutes * 60
        _playbackState.update { it.copy(sleepTimerRemainingSeconds = remainingSeconds) }

        sleepTimerJob = scope.launch {
            while (remainingSeconds > 0) {
                delay(1000L)
                remainingSeconds -= 1
                _playbackState.update { it.copy(sleepTimerRemainingSeconds = remainingSeconds) }
            }
            mediaController?.pause()
            _playbackState.update { it.copy(sleepTimerRemainingSeconds = null) }
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        _playbackState.update { it.copy(sleepTimerRemainingSeconds = null) }
    }

    private fun updatePlaybackState() {
        val controller = mediaController ?: return
        val currentItem = controller.currentMediaItem
        val currentSong = resolveSong(currentItem)

        if (currentQueue.isEmpty() && controller.mediaItemCount > 0) {
            val rebuiltQueue = mutableListOf<Song>()
            for (i in 0 until controller.mediaItemCount) {
                val item = controller.getMediaItemAt(i)
                val song = resolveSong(item) ?: mediaItemToSong(item)
                rebuiltQueue.add(song)
            }
            currentQueue = rebuiltQueue
        } else if (currentQueue.isNotEmpty() && knownSongs.isNotEmpty()) {
            currentQueue = currentQueue.map { qSong ->
                knownSongs[qSong.id]?.copy(isFavorite = knownSongs[qSong.id]?.isFavorite ?: qSong.isFavorite) ?: qSong
            }
        }

        val repeatMode = when (controller.repeatMode) {
            Player.REPEAT_MODE_ALL -> RepeatMode.ALL
            Player.REPEAT_MODE_ONE -> RepeatMode.ONE
            else -> RepeatMode.OFF
        }

        val isPlaying = controller.isPlaying

        _playbackState.update {
            it.copy(
                currentSong = currentSong ?: it.currentSong,
                isPlaying = isPlaying,
                isBuffering = controller.playbackState == Player.STATE_BUFFERING,
                currentPositionMs = controller.currentPosition.coerceAtLeast(0L),
                durationMs = if (controller.duration > 0) controller.duration else (currentSong?.durationMs ?: it.durationMs),
                playbackSpeed = controller.playbackParameters.speed,
                isShuffleEnabled = controller.shuffleModeEnabled,
                repeatMode = repeatMode,
                queue = currentQueue,
                queueIndex = controller.currentMediaItemIndex,
                isFavorite = currentSong?.isFavorite ?: it.isFavorite
            )
        }

        if (isPlaying) {
            startPositionUpdates()
        } else {
            stopPositionUpdates()
        }
    }

    fun release() {
        stopPositionUpdates()
        sleepTimerJob?.cancel()
        controllerFuture?.let { MediaController.releaseFuture(it) }
        mediaController = null
    }
}
