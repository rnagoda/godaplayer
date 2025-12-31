# GodaPlayer

A native Android audio player with a retro terminal aesthetic. Built with Kotlin, Jetpack Compose, and Media3/ExoPlayer.

## Features

- **File Browser** - Navigate your device's folders and play audio files directly
- **Library Management** - Scan folders for music with automatic metadata extraction
- **Playlist Support** - Create, edit, and manage playlists with drag-drop reordering
- **5-Band Equalizer** - Customize your sound with EQ, bass boost, and virtualizer
- **12 Built-in Presets** - Flat, Rock, Pop, Jazz, Classical, Hip Hop, and more
- **Background Playback** - Continue listening with notification controls
- **M3U Export/Import** - Share playlists across devices and apps
- **Retro Terminal UI** - Monospace fonts and a dark terminal-inspired theme

## Screenshots

*Coming soon*

## Requirements

- Android 8.0 (API 26) or higher
- Storage permission for accessing audio files

## Building

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK with API 35

### Build Steps

1. Clone the repository:
   ```bash
   git clone https://github.com/rnagoda/godaplayer.git
   cd godaplayer
   ```

2. Open in Android Studio or build from command line:
   ```bash
   ./gradlew assembleDebug
   ```

3. Install the APK:
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

## Architecture

GodaPlayer follows MVVM architecture with clean architecture principles:

```
com.godaplayer.app/
├── data/           # Data layer
│   ├── local/      # Room database, DAOs, DataStore preferences
│   ├── mapper/     # Entity to domain model mappers
│   └── repository/ # Repository implementations
├── domain/         # Domain layer
│   ├── model/      # Domain models (Song, Playlist, etc.)
│   ├── repository/ # Repository interfaces
│   └── usecase/    # Business logic use cases
├── player/         # Media playback
│   ├── PlaybackService.kt    # MediaSessionService
│   ├── PlaybackController.kt # UI to service bridge
│   ├── QueueManager.kt       # Queue state management
│   └── EqualizerManager.kt   # Audio effects
├── ui/             # Presentation layer
│   ├── theme/      # Colors, typography, theme
│   ├── components/ # Reusable UI components
│   ├── screens/    # Screen composables and ViewModels
│   └── navigation/ # Navigation graph
└── di/             # Hilt dependency injection modules
```

## Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Kotlin 2.0 |
| UI | Jetpack Compose |
| Architecture | MVVM + Clean Architecture |
| Dependency Injection | Hilt |
| Database | Room |
| Preferences | DataStore |
| Media Playback | Media3 ExoPlayer |
| Audio Effects | Android AudioFX (Equalizer, BassBoost, Virtualizer) |

## Permissions

The app requires the following permissions:

- `READ_MEDIA_AUDIO` (Android 13+) / `READ_EXTERNAL_STORAGE` (Android 12 and below) - Access audio files
- `FOREGROUND_SERVICE` - Background playback
- `POST_NOTIFICATIONS` - Playback notification controls

## License

*License to be determined*

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.
