package com.godaplayer.app.player

import com.godaplayer.app.domain.model.RepeatMode
import com.godaplayer.app.domain.model.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QueueManager @Inject constructor() {

    private val _queue = MutableStateFlow<List<Song>>(emptyList())
    val queue: StateFlow<List<Song>> = _queue.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _shuffleEnabled = MutableStateFlow(false)
    val shuffleEnabled: StateFlow<Boolean> = _shuffleEnabled.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private var originalQueue: List<Song> = emptyList()

    val currentSong: Song?
        get() = _queue.value.getOrNull(_currentIndex.value)

    fun setQueue(songs: List<Song>, startIndex: Int = 0) {
        originalQueue = songs
        _queue.value = songs
        _currentIndex.value = startIndex.coerceIn(0, songs.size - 1)
    }

    fun updateCurrentIndex(index: Int) {
        _currentIndex.value = index
    }

    fun addToQueue(song: Song) {
        _queue.value = _queue.value + song
        originalQueue = originalQueue + song
    }

    fun addToQueueNext(song: Song) {
        val mutableQueue = _queue.value.toMutableList()
        val insertIndex = (_currentIndex.value + 1).coerceAtMost(mutableQueue.size)
        mutableQueue.add(insertIndex, song)
        _queue.value = mutableQueue

        val originalMutableQueue = originalQueue.toMutableList()
        originalMutableQueue.add(insertIndex, song)
        originalQueue = originalMutableQueue
    }

    fun removeFromQueue(index: Int) {
        if (index < 0 || index >= _queue.value.size) return

        val mutableQueue = _queue.value.toMutableList()
        mutableQueue.removeAt(index)
        _queue.value = mutableQueue

        // Adjust current index if needed
        if (index < _currentIndex.value) {
            _currentIndex.value = (_currentIndex.value - 1).coerceAtLeast(0)
        } else if (index == _currentIndex.value && _currentIndex.value >= mutableQueue.size) {
            _currentIndex.value = (mutableQueue.size - 1).coerceAtLeast(0)
        }
    }

    fun clearQueue() {
        _queue.value = emptyList()
        originalQueue = emptyList()
        _currentIndex.value = 0
    }

    fun reorder(fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex) return
        if (fromIndex < 0 || fromIndex >= _queue.value.size) return
        if (toIndex < 0 || toIndex >= _queue.value.size) return

        val mutableQueue = _queue.value.toMutableList()
        val item = mutableQueue.removeAt(fromIndex)
        mutableQueue.add(toIndex, item)
        _queue.value = mutableQueue

        // Update current index if affected
        val currentIdx = _currentIndex.value
        when {
            currentIdx == fromIndex -> _currentIndex.value = toIndex
            fromIndex < currentIdx && toIndex >= currentIdx -> _currentIndex.value = currentIdx - 1
            fromIndex > currentIdx && toIndex <= currentIdx -> _currentIndex.value = currentIdx + 1
        }
    }

    fun toggleShuffle(): Boolean {
        _shuffleEnabled.value = !_shuffleEnabled.value

        if (_shuffleEnabled.value) {
            // Shuffle the queue but keep current song in place
            val currentSong = currentSong
            val otherSongs = _queue.value.filterIndexed { index, _ -> index != _currentIndex.value }
            val shuffled = listOfNotNull(currentSong) + otherSongs.shuffled()
            _queue.value = shuffled
            _currentIndex.value = 0
        } else {
            // Restore original order
            val currentSongPath = currentSong?.filePath
            _queue.value = originalQueue
            _currentIndex.value = originalQueue.indexOfFirst { it.filePath == currentSongPath }
                .coerceAtLeast(0)
        }

        return _shuffleEnabled.value
    }

    fun cycleRepeatMode(): RepeatMode {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        return _repeatMode.value
    }

    fun getUpNext(count: Int = 3): List<Song> {
        val nextIndex = _currentIndex.value + 1
        return if (nextIndex < _queue.value.size) {
            _queue.value.subList(nextIndex, minOf(nextIndex + count, _queue.value.size))
        } else {
            emptyList()
        }
    }
}
