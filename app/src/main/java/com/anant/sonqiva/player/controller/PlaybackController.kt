package com.anant.sonqiva.player.controller

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
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
    private var positionUpdateJob: Job? = null
    private var sleepTimerJob: Job? = null

    init {
        initializeController()
    }

    private fun initializeController() {
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
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updatePlaybackState()
                if (isPlaying) {
                    startPositionUpdates()
                } else {
                    stopPositionUpdates()
                }
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
        })
    }

    private fun startPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = scope.launch {
            while (isActive) {
                mediaController?.let { controller ->
                    if (controller.isPlaying) {
                        _playbackState.update {
                            it.copy(
                                currentPositionMs = controller.currentPosition.coerceAtLeast(0L),
                                durationMs = controller.duration.coerceAtLeast(0L)
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

    fun playSong(song: Song, queue: List<Song>) {
        val controller = mediaController ?: return
        currentQueue = queue

        val mediaItems = queue.map { qSong ->
            val metadata = MediaMetadata.Builder()
                .setTitle(qSong.title)
                .setArtist(qSong.artist)
                .setAlbumTitle(qSong.album)
                .setArtworkUri(qSong.albumArtUri)
                .build()

            val itemBuilder = MediaItem.Builder()
                .setMediaId(qSong.id.toString())
                .setMediaMetadata(metadata)

            if (qSong.mediaUri != null) {
                itemBuilder.setUri(qSong.mediaUri)
            }

            itemBuilder.build()
        }

        val targetIndex = queue.indexOfFirst { it.id == song.id }.coerceAtLeast(0)

        controller.setMediaItems(mediaItems, targetIndex, 0L)
        controller.prepare()
        controller.play()
    }

    fun playNext(song: Song) {
        val controller = mediaController ?: return
        val currentIdx = controller.currentMediaItemIndex
        val insertIdx = if (currentIdx >= 0) currentIdx + 1 else 0

        val metadata = MediaMetadata.Builder()
            .setTitle(song.title)
            .setArtist(song.artist)
            .setAlbumTitle(song.album)
            .setArtworkUri(song.albumArtUri)
            .build()

        val itemBuilder = MediaItem.Builder()
            .setMediaId(song.id.toString())
            .setMediaMetadata(metadata)

        if (song.mediaUri != null) {
            itemBuilder.setUri(song.mediaUri)
        }

        controller.addMediaItem(insertIdx, itemBuilder.build())

        val mutableQ = currentQueue.toMutableList()
        if (insertIdx in 0..mutableQ.size) {
            mutableQ.add(insertIdx, song)
        } else {
            mutableQ.add(song)
        }
        currentQueue = mutableQ
        _playbackState.update { it.copy(queue = currentQueue) }
    }

    fun addToQueue(song: Song) {
        val controller = mediaController ?: return

        val metadata = MediaMetadata.Builder()
            .setTitle(song.title)
            .setArtist(song.artist)
            .setAlbumTitle(song.album)
            .setArtworkUri(song.albumArtUri)
            .build()

        val itemBuilder = MediaItem.Builder()
            .setMediaId(song.id.toString())
            .setMediaMetadata(metadata)

        if (song.mediaUri != null) {
            itemBuilder.setUri(song.mediaUri)
        }

        controller.addMediaItem(itemBuilder.build())

        val mutableQ = currentQueue.toMutableList()
        mutableQ.add(song)
        currentQueue = mutableQ
        _playbackState.update { it.copy(queue = currentQueue) }
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
        if (minutes <= 0) return

        sleepTimerJob = scope.launch {
            delay(minutes * 60 * 1000L)
            mediaController?.pause()
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
    }

    private fun updatePlaybackState() {
        val controller = mediaController ?: return
        val currentMediaId = controller.currentMediaItem?.mediaId?.toLongOrNull()
        val currentSong = currentQueue.firstOrNull { it.id == currentMediaId }

        val repeatMode = when (controller.repeatMode) {
            Player.REPEAT_MODE_ALL -> RepeatMode.ALL
            Player.REPEAT_MODE_ONE -> RepeatMode.ONE
            else -> RepeatMode.OFF
        }

        _playbackState.update {
            it.copy(
                currentSong = currentSong ?: it.currentSong,
                isPlaying = controller.isPlaying,
                isBuffering = controller.playbackState == Player.STATE_BUFFERING,
                currentPositionMs = controller.currentPosition.coerceAtLeast(0L),
                durationMs = controller.duration.coerceAtLeast(0L),
                playbackSpeed = controller.playbackParameters.speed,
                isShuffleEnabled = controller.shuffleModeEnabled,
                repeatMode = repeatMode,
                queue = currentQueue,
                queueIndex = controller.currentMediaItemIndex
            )
        }
    }

    fun release() {
        stopPositionUpdates()
        sleepTimerJob?.cancel()
        controllerFuture?.let { MediaController.releaseFuture(it) }
        mediaController = null
    }
}
