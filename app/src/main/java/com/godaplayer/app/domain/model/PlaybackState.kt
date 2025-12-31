package com.godaplayer.app.domain.model

enum class RepeatMode {
    OFF,
    ALL,
    ONE
}

enum class PlaybackState {
    IDLE,
    BUFFERING,
    PLAYING,
    PAUSED,
    ERROR
}
