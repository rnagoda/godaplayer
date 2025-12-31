package com.godaplayer.app.ui.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.godaplayer.app.domain.model.Song
import com.godaplayer.app.player.PlaybackController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class MiniPlayerUiState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val progress: Float = 0f
)

@HiltViewModel
class MiniPlayerViewModel @Inject constructor(
    private val playbackController: PlaybackController
) : ViewModel() {

    val uiState: StateFlow<MiniPlayerUiState> = combine(
        playbackController.currentSong,
        playbackController.isPlaying,
        playbackController.progress
    ) { song, isPlaying, progress ->
        MiniPlayerUiState(
            currentSong = song,
            isPlaying = isPlaying,
            progress = progress
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MiniPlayerUiState()
    )

    fun togglePlayPause() {
        playbackController.togglePlayPause()
    }

    fun skipToNext() {
        playbackController.skipToNext()
    }
}
