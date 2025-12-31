package com.godaplayer.app.player

import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@AndroidEntryPoint
class PlaybackService : MediaSessionService() {

    @Inject
    lateinit var queueManager: QueueManager

    @Inject
    lateinit var equalizerManager: EqualizerManager

    private var mediaSession: MediaSession? = null
    private var player: ExoPlayer? = null

    companion object {
        private val _audioSessionId = MutableStateFlow(0)
        val audioSessionId: StateFlow<Int> = _audioSessionId.asStateFlow()
    }

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                true // handleAudioFocus
            )
            .setHandleAudioBecomingNoisy(true)
            .build()
            .apply {
                addListener(PlayerListener())
            }

        // Expose and attach audio session for equalizer
        val sessionId = player?.audioSessionId ?: 0
        _audioSessionId.value = sessionId
        if (sessionId != 0) {
            equalizerManager.attachToAudioSession(sessionId)
        }

        mediaSession = MediaSession.Builder(this, player!!)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player != null && !player.playWhenReady) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        equalizerManager.release()
        _audioSessionId.value = 0
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        player = null
        super.onDestroy()
    }

    private inner class PlayerListener : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            mediaItem?.let {
                val index = player?.currentMediaItemIndex ?: 0
                queueManager.updateCurrentIndex(index)
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            // Could be used to update play count when song completes
        }
    }
}
