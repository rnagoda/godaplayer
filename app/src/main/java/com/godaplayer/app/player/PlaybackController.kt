package com.godaplayer.app.player

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.godaplayer.app.domain.model.RepeatMode
import com.godaplayer.app.domain.model.Song
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaybackController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val queueManager: QueueManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var mediaController: MediaController? = null
    private var progressJob: Job? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    val shuffleEnabled: StateFlow<Boolean> = queueManager.shuffleEnabled
    val repeatMode: StateFlow<RepeatMode> = queueManager.repeatMode

    init {
        // Watch both queue and currentIndex to update currentSong
        scope.launch {
            queueManager.queue.collect {
                _currentSong.value = queueManager.currentSong
            }
        }
        scope.launch {
            queueManager.currentIndex.collect {
                _currentSong.value = queueManager.currentSong
            }
        }
        // Connect to PlaybackService
        scope.launch {
            connect()
        }
    }

    private suspend fun connect() {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, PlaybackService::class.java)
        )

        mediaController = MediaController.Builder(context, sessionToken)
            .buildAsync()
            .await()

        mediaController?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                _isPlaying.value = playing
                if (playing) {
                    startProgressUpdates()
                } else {
                    stopProgressUpdates()
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                // Sync QueueManager's currentIndex with the player's current position
                val currentIndex = mediaController?.currentMediaItemIndex ?: 0
                queueManager.updateCurrentIndex(currentIndex)
                updateDuration()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    updateDuration()
                }
            }
        })

        // Sync initial state
        _isPlaying.value = mediaController?.isPlaying == true
        updateDuration()
        if (_isPlaying.value) {
            startProgressUpdates()
        }
    }

    private fun updateDuration() {
        val duration = mediaController?.duration ?: 0L
        _durationMs.value = if (duration > 0) duration else 0L
    }

    private fun startProgressUpdates() {
        stopProgressUpdates()
        progressJob = scope.launch {
            while (isActive) {
                val position = mediaController?.currentPosition ?: 0L
                val duration = mediaController?.duration ?: 0L
                _currentPositionMs.value = position
                _progress.value = if (duration > 0) position.toFloat() / duration else 0f
                delay(200)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
    }

    fun play() {
        mediaController?.play()
    }

    fun pause() {
        mediaController?.pause()
    }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            pause()
        } else {
            play()
        }
    }

    fun seekTo(positionMs: Long) {
        mediaController?.seekTo(positionMs)
        _currentPositionMs.value = positionMs
    }

    fun seekToProgress(progress: Float) {
        val position = (progress * (_durationMs.value)).toLong()
        seekTo(position)
    }

    fun skipToNext() {
        mediaController?.seekToNextMediaItem()
    }

    fun skipToPrevious() {
        // If we're more than 3 seconds in, restart current song
        if ((_currentPositionMs.value) > 3000) {
            seekTo(0)
        } else {
            mediaController?.seekToPreviousMediaItem()
        }
    }

    fun playSong(song: Song) {
        queueManager.setQueue(listOf(song), 0)
        val mediaItem = createMediaItem(song)
        mediaController?.setMediaItem(mediaItem)
        mediaController?.prepare()
        mediaController?.play()
    }

    fun playQueue(songs: List<Song>, startIndex: Int = 0) {
        queueManager.setQueue(songs, startIndex)
        val mediaItems = songs.map { createMediaItem(it) }
        mediaController?.setMediaItems(mediaItems, startIndex, 0L)
        mediaController?.prepare()
        mediaController?.play()
    }

    fun addToQueue(song: Song) {
        queueManager.addToQueue(song)
        mediaController?.addMediaItem(createMediaItem(song))
    }

    fun addToQueueNext(song: Song) {
        queueManager.addToQueueNext(song)
        val currentIndex = mediaController?.currentMediaItemIndex ?: 0
        mediaController?.addMediaItem(currentIndex + 1, createMediaItem(song))
    }

    fun removeFromQueue(index: Int) {
        queueManager.removeFromQueue(index)
        mediaController?.removeMediaItem(index)
    }

    fun clearQueue() {
        queueManager.clearQueue()
        mediaController?.clearMediaItems()
    }

    fun toggleShuffle(): Boolean {
        val enabled = queueManager.toggleShuffle()
        // Rebuild the queue in the player
        val queue = queueManager.queue.value
        val currentIndex = queueManager.currentIndex.value
        if (queue.isNotEmpty()) {
            val mediaItems = queue.map { createMediaItem(it) }
            val currentPosition = mediaController?.currentPosition ?: 0L
            mediaController?.setMediaItems(mediaItems, currentIndex, currentPosition)
        }
        return enabled
    }

    fun cycleRepeatMode(): RepeatMode {
        val mode = queueManager.cycleRepeatMode()
        mediaController?.repeatMode = when (mode) {
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
        }
        return mode
    }

    private fun createMediaItem(song: Song): MediaItem {
        return MediaItem.Builder()
            .setUri(song.filePath)
            .setMediaId(song.id.toString())
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(song.title ?: song.fileName)
                    .setArtist(song.artist)
                    .setAlbumTitle(song.album)
                    .build()
            )
            .build()
    }
}
