# GodaPlayer User Guide

A step-by-step guide to using GodaPlayer, the retro terminal-style audio player for Android.

---

## Table of Contents

1. [First-Time Setup](#first-time-setup)
2. [Adding Music to Your Library](#adding-music-to-your-library)
3. [Browsing and Playing Music](#browsing-and-playing-music)
4. [Creating and Managing Playlists](#creating-and-managing-playlists)
5. [Using the Equalizer](#using-the-equalizer)
6. [Exporting and Importing Playlists](#exporting-and-importing-playlists)
7. [Settings Overview](#settings-overview)

---

## First-Time Setup

### Granting Permissions

When you first launch GodaPlayer, you'll be prompted to grant permissions:

1. **Audio File Access** - Required to read music files from your device
   - Android 13+: "Allow access to music and audio"
   - Android 12 and below: "Allow access to files"

2. **Notifications** (Android 13+) - Required for playback controls in the notification shade

Tap "Allow" for each permission request. Without audio access, the app cannot find or play your music files.

### Initial Library Scan

After granting permissions:

1. Navigate to **Settings** (gear icon in the bottom navigation)
2. Tap **Manage Scan Folders**
3. Tap **[ ADD FOLDER ]** and select a folder containing your music
4. Return to Settings and tap **Rescan Library** to scan for music

The app will find all audio files in your selected folders and extract their metadata (title, artist, album, etc.).

---

## Adding Music to Your Library

### Setting Up Scan Folders

GodaPlayer scans specific folders for music files. To configure:

1. Go to **Settings** > **Manage Scan Folders**
2. Tap **[ ADD FOLDER ]** to add a new folder
3. Navigate to and select your music directory
4. The folder appears in the list with a toggle to enable/disable scanning

### Supported Audio Formats

- MP3 (.mp3)
- FLAC (.flac)
- AAC (.aac)
- M4A (.m4a)
- OGG Vorbis (.ogg)
- WAV (.wav)

### Rescanning Your Library

To update your library after adding new music:

1. Go to **Settings**
2. Tap **Rescan Library**
3. Wait for the scan to complete

The scanner will:
- Find new audio files
- Extract metadata from new files
- Remove entries for deleted files

---

## Browsing and Playing Music

### File Browser

The **Browse** tab lets you navigate your device's file system:

1. Tap folders to navigate into them
2. Tap the back arrow or use the breadcrumb path to go up
3. Audio files display with their duration
4. Tap any audio file to start playback

### Library View

The **Library** tab shows all scanned music:

1. Songs are listed alphabetically with letter sections
2. Use the search bar to find specific songs
3. Sort options: Title, Artist, Date Added, Duration, Filename

### Now Playing Screen

Tap the mini player at the bottom to open the full Now Playing screen:

```
┌─────────────────────────────┐
│                             │
│      [ Album Art ]          │
│                             │
├─────────────────────────────┤
│  Song Title                 │
│  Artist Name                │
│                             │
│  ═══════●════════════  3:45 │
│  1:23                       │
│                             │
│    ◁◁    ▶/❚❚    ▷▷        │
│                             │
│  🔀 Shuffle    🔁 Repeat    │
└─────────────────────────────┘
```

**Controls:**
- **Play/Pause** - Center button
- **Previous** - Double-tap to restart, single tap during first 3 seconds goes to previous
- **Next** - Skip to next track
- **Progress Bar** - Tap or drag to seek
- **Shuffle** - Randomize queue order
- **Repeat** - Off → Repeat All → Repeat One

### Queue Management

Tap the queue icon to view and manage the play queue:

- Drag handles to reorder tracks
- Swipe to remove tracks
- Tap **[ SAVE AS PLAYLIST ]** to save the current queue

---

## Creating and Managing Playlists

### Creating a New Playlist

**Method 1: From Playlists Screen**
1. Go to **Playlists** tab
2. Tap **[ NEW PLAYLIST ]**
3. Enter a name and optional description
4. Tap **Create**

**Method 2: From Queue**
1. Build a queue by playing songs
2. Open the queue view
3. Tap **[ SAVE AS PLAYLIST ]**
4. Enter a name

### Adding Songs to Playlists

1. Long-press on any song (in Library, Browse, or another playlist)
2. Select **Add to Playlist**
3. Choose one or more playlists
4. Tap **Add**

### Editing Playlists

From a playlist's detail screen:

- **Reorder** - Drag songs using the handle on the right
- **Remove** - Swipe a song left or right
- **Rename** - Tap the menu icon > Rename
- **Delete** - Tap the menu icon > Delete Playlist

### Viewing Song Info

Long-press any song and select **Song Info** to see:
- File path and size
- Duration and format
- Metadata (title, artist, album, year, genre)
- Which playlists contain this song

---

## Using the Equalizer

GodaPlayer includes a 5-band equalizer with bass boost and virtualizer effects.

### Accessing the Equalizer

1. Open the **Now Playing** screen
2. Tap the **EQ** button
3. Toggle the **ENABLED** switch to activate effects

### Adjusting Bands

The five frequency bands:
- **60 Hz** - Sub-bass (deep bass)
- **230 Hz** - Bass (warmth)
- **910 Hz** - Midrange (vocals, instruments)
- **3.6 kHz** - Upper mids (presence, clarity)
- **14 kHz** - Treble (brightness, air)

Drag sliders up to boost or down to cut each frequency.

### Using Presets

GodaPlayer includes 12 built-in presets:

| Preset | Best For |
|--------|----------|
| Flat | Neutral, no coloration |
| Bass Boost | Bass-heavy genres |
| Bass Reducer | Reducing muddiness |
| Treble Boost | Adding brightness |
| Treble Reducer | Reducing harshness |
| Vocal Boost | Podcasts, vocals |
| Rock | Guitar-driven music |
| Pop | Modern pop music |
| Jazz | Acoustic instruments |
| Classical | Orchestral music |
| Hip Hop | Bass and beats |
| Electronic | EDM, synth music |

To select a preset:
1. Tap **[ SELECT PRESET ]**
2. Choose from the list
3. Settings apply immediately

### Bass Boost & Virtualizer

Below the EQ bands:
- **Bass Boost** - Enhances low frequencies beyond EQ
- **Virtualizer** - Creates a wider stereo image

### Saving Custom Presets

After adjusting to your liking:
1. Tap **[ SAVE PRESET ]**
2. Enter a name
3. Your preset appears in the preset list

---

## Exporting and Importing Playlists

GodaPlayer uses the M3U format for playlist portability.

### Exporting Playlists

**Export All Playlists:**
1. Go to **Settings**
2. Tap **Export Playlists**
3. Select a destination folder
4. All playlists export as .m3u files

The exported files can be:
- Imported on another device with GodaPlayer
- Used with other music players that support M3U
- Backed up to cloud storage

### Importing Playlists

1. Go to **Settings**
2. Tap **Import Playlist**
3. Select an .m3u file
4. The app creates a new playlist and matches tracks

**Note:** Import matches tracks by file path. If your music is in a different location on the new device, some tracks may not match. The import dialog shows how many tracks were successfully matched.

### M3U Format

Exported files use extended M3U format:
```
#EXTM3U
#PLAYLIST:My Playlist Name
#EXTINF:245,Artist Name - Song Title
/storage/emulated/0/Music/song.mp3
```

---

## Settings Overview

### Playback Settings

| Setting | Description |
|---------|-------------|
| **Gapless Playback** | Eliminates silence between tracks (great for live albums) |
| **Resume on Start** | Remember position and resume when app opens |

### Library Settings

| Setting | Description |
|---------|-------------|
| **Auto-Scan** | Automatically scan for new music on app launch |
| **Show File Extensions** | Display .mp3, .flac, etc. in file names |
| **Manage Scan Folders** | Configure which folders to scan |
| **Rescan Library** | Manually trigger a full library scan |

### Data Management

| Setting | Description |
|---------|-------------|
| **Clear Play History** | Reset play counts and last played timestamps |
| **Export Playlists** | Export all playlists to M3U files |
| **Import Playlist** | Import an M3U file as a new playlist |

---

## Tips & Tricks

1. **Quick Queue Building** - In the file browser, tap songs to add them to the queue without navigating away

2. **Batch Operations** - Long-press to select multiple songs, then add them all to a playlist at once

3. **Keyboard Shortcuts** - If using a Bluetooth keyboard:
   - Space: Play/Pause
   - Arrow keys: Seek/Navigate

4. **Notification Controls** - When playing in the background, use notification controls for play/pause, next, and previous

5. **Headphone Controls** - GodaPlayer responds to headphone button presses:
   - Single press: Play/Pause
   - Double press: Next track
   - Triple press: Previous track

---

## Troubleshooting

### Music Not Appearing in Library

1. Check that the folder is added in **Settings** > **Manage Scan Folders**
2. Ensure the folder toggle is enabled
3. Tap **Rescan Library** in Settings
4. Verify files are in a supported format

### Playback Issues

1. Try a different audio file to rule out file corruption
2. Check that audio permissions are granted
3. Restart the app

### Equalizer Not Working

1. Ensure the EQ is toggled ON
2. Some Bluetooth devices have their own DSP that may conflict
3. Effects apply to the current playback session

---

## Support

For bug reports and feature requests, please visit:
https://github.com/rnagoda/godaplayer/issues
