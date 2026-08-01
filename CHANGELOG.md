# Changelog

## Milestone 18: Production Integration & App Finalization
- **Production Clean-up**: Replaced every remaining TODO, mock, fake, and placeholder implementation with production code.
- **Full Screen Connectivity**: Connected all 12 screens (Home, Search, Player, Queue, Favorites, Downloads, Recently Played, Albums, Artists, Playlists, Settings, About) to reactive StateFlow repository streams.
- **Dedicated About Screen**: Added production `AboutScreen` displaying Wear OS version, ExoPlayer Media3 audio engine, Room database persistence, WorkManager background sync, and dual Piped/Invidious backend failover.
- **End-to-End State Synchronization**: Unified state flows across Room local database, Media3 ExoPlayer playback engine, Media3 DownloadManager, and WorkManager SyncManager.
- **Production Readiness**: Validated zero mock data, zero placeholder UI, stable builds, rotary/haptic UI feedback, and optimized Wear OS performance.

## Milestone 17: Offline Reliability, Background Sync & Wear OS Optimization
- **WorkManager Synchronization**: Implemented `SyncWorker`, `SyncManager`, and `SyncRepositoryImpl` supporting background sync with exponential backoff and network/charging constraints.
- **Offline Manager & Network Monitor**: Created `NetworkMonitor` and `OfflineManager` tracking connection states (Wi-Fi, Cellular, Disconnected) and supporting forced offline toggle.
- **Offline Feed Experience**: Updated `HomeScreen` to display an offline status banner and prioritize downloads, queue, favorites, and local library when offline.
- **Download Reliability**: Added retry for failed downloads, parallel download limits (1 to 3), and cleanup of corrupted or incomplete downloads.
- **Backend Failover & Blacklisting**: Enhanced `BackendProvider` with 1-minute blacklisting failover between Piped and Invidious instances with exponential backoff.
- **Sync & Offline Settings**: Added Synchronization, Offline & Network State, and Download Reliability controls in `SettingsScreen`.

## Milestone 16: Content Discovery, Recommendations & Library Experience
- **Backend-Driven Home Feed**: Implemented dynamic Home feed with independent section loading for Recently Played, Favorites, Downloads, Recommended For You, Trending, Popular Albums, and Popular Artists.
- **Repository Expansion**: Implemented `RecommendationRepository`, `ArtistRepository`, `AlbumRepository`, `PlaylistRepository`, and `LibraryRepository` with Hilt binding in `RepositoryModule`.
- **Artist, Album & Playlist Screens**: Created dedicated screens with track playback, favorite toggling, and full album/playlist downloading.
- **Typed Favorites**: Updated `FavoriteEntity` and `FavoriteDao` to manage typed favorites for songs, albums, artists, and playlists.
- **Enhanced Search**: Added 300ms query debouncing, filter category selection, and immediate playback execution.
- **Navigation Routes**: Configured `artist/{artistId}`, `album/{albumId}`, `playlist/{playlistId}` in `Screen` and `PulseNavHost`.

## Milestone 1: Navigation Architecture
- Defined type-safe `Screen` sealed class representing route names.
- Extracted `HomeScreen` into a dedicated composable from `MainActivity`.
- Configured a Wear OS `SwipeDismissableNavHost` utilizing `rememberSwipeDismissableNavController`.
- Added placeholder screens utilizing `ScalingLazyColumn` and simple placeholder UI elements for:
  - Search
  - Player
  - Queue
  - Recently Played
  - Favorites
  - Downloads
  - Settings
  - About
## Milestone 2: Design System
- Created centralized `PulseDesignTokens.kt` to manage Spacing, Radius, IconSizes, Padding, Touch, Elevation, and Animations parameters cleanly.
- Implemented `PulseHaptics` and `rememberPulseHapticFeedback()` helper to abstract out standard UI haptic feedbacks from actual components.
- Developed the foundation for Reusable Components in `PulseComponents.kt`:
  - `PulseCard`, `PulseListItem`, `PulseSectionHeader`, `PulsePrimaryButton`, `PulseSecondaryButton`, `PulseIconButton`, `PulseLoadingIndicator`, `PulseEmptyState`, `PulseDivider`.
- Utilized Wear OS Material 3 elements exclusively for all styling.

## Milestone 3: Home Screen
- Integrated the new design system components into the Home Screen.
- Replaced manual custom compositions with `PulseCard` for the "Now Playing" feature and `PulseListItem` for navigation entries.
- Retained smooth navigation structure via `onNavigate(Screen)`.

## Milestone 4: Search Screen
- Constructed `SearchScreen.kt` using `ScalingLazyColumn`.
- Devised state-driven Search UI architecture in `SearchViewModel.kt`.
- Handled mock UI states for `Recent Searches`, `Loading`, `Results`, and `No Results`.
- Implemented robust `PulseSearchInput` encapsulating a `BasicTextField` with search and clear icons.
- Updated `PulseListItem` in design system to seamlessly support long presses utilizing standard foundation `combinedClickable`.
- Implemented a Wear OS optimized context-menu style `Dialog` resolving long click interactions for mock actions.

## Milestone 5: Now Playing (Player UI)
- Delivered `PlayerScreen.kt` with a premium layout dedicated exclusively to Wear OS round displays.
- Connected `PlayerViewModel.kt` storing interactive, mock `PlayerUiState`.
- Composed main playback sections: central circular progress indicator combined with animating Play/Pause action, surrounding previous/next tracks, and supportive shuffle/repeat/queue controls.
- Improved `PulseIconButton` by instantiating `Crossfade` animation directly upon icon alteration.
- Incorporated placeholder expandable lists for granular Audio Quality, Playback Device, and Song Information controls.

## Milestone 6: Queue, Favorites & Recently Played
- Developed `QueueScreen`, `FavoritesScreen`, and `RecentlyPlayedScreen` maintaining layout consistency.
- Initialized isolated state-managers (`QueueViewModel`, `FavoritesViewModel`, `RecentlyPlayedViewModel`).
- Extracted shared properties into a universal `Track` data model supporting consistent rendering semantics.
- Incorporated placeholder interaction layers surfacing responsive empty states via `PulseEmptyState` and actionable popup interfaces using dialogs upon item long-click.
 
## Milestone 7: Downloads
- Created `DownloadedTrack` model extending the capabilities of offline representation.
- Designed `DownloadsScreen.kt` using structured headers emphasizing aggregate storage capacity and total item limits.
- Established `DownloadsViewModel.kt` to bind the presentation layer with simulated offline content.
- Ensured a polished zero-data presentation via `PulseEmptyState` leading to a call-to-action button for initiating catalog search.
- Bound interactive long-click dialogs resolving mock user behaviors for Play, Remove Download, and View Song Information intents.

## Milestone 8: Settings
- Developed exhaustive `SettingsScreen.kt` enumerating configuration categories across Playback, Downloads, Cache, Backend, Appearance, and About segments.
- Appended `PulseToggleItem` composing a seamless toggle component via explicit verified/unverified vectors within `PulseComponents.kt`.
- Stored volatile preference states using `SettingsViewModel.kt` handling property toggles, modal cache confirmations, and Easter egg multi-tap sequences unlocking hidden development capabilities.

## Milestone 9: UX Polish & Wear OS Optimization
- Implemented `pulseRotaryScroll` extension using Wear OS official `rotaryScrollable` APIs and `FocusRequester` auto-focus handling for smooth physical crown and bezel scrolling.
- Applied rotary scrolling support across all main screens (`HomeScreen`, `SearchScreen`, `PlayerScreen`, `QueueScreen`, `FavoritesScreen`, `RecentlyPlayedScreen`, `DownloadsScreen`, `SettingsScreen`) and dialog overlay lists.
- Audited accessibility content descriptions across icons, playback controls, and toggles to ensure full screen reader support.
- Reinforced tactile haptic feedback (`PulseHaptics`) across all touch targets and long-press actions.
- Standardized UI spacing, typography, component styling, and animation specs across the entire application for 60 FPS performance.

## Milestone 10: Architecture Review & Refactoring
- Established clean package structure separating domain models (`com.example.domain.model`) and domain repository interfaces (`com.example.domain.repository`).
- Implemented repository singletons (`TrackRepositoryImpl`, `PlayerRepositoryImpl`, `DownloadsRepositoryImpl`, `SettingsRepositoryImpl`) under `com.example.data.repository`.
- Wired dependency injection using Hilt (`RepositoryModule.kt`) providing clean abstraction bindings.
- Refactored all ViewModels (`SearchViewModel`, `PlayerViewModel`, `QueueViewModel`, `FavoritesViewModel`, `RecentlyPlayedViewModel`, `DownloadsViewModel`, `SettingsViewModel`) to inject repositories and expose reactive `StateFlow` states using `SharingStarted.WhileSubscribed(5000)`.
- Enhanced Jetpack Compose rendering performance by enforcing explicit item keys (`key = { it.id }`) in `ScalingLazyColumn` blocks.
- Prepared codebase architecture for seamless upcoming integrations with Room database, Media3 playback engine, and remote APIs.

## Milestone 11: Room Database Integration
- Built Room Database (`PulseDatabase`) targeting lightweight Wear OS local storage.
- Created entities: `TrackEntity`, `FavoriteEntity`, `HistoryEntity`, `QueueEntity`, and `DownloadEntity`.
- Implemented DAOs: `TrackDao`, `FavoriteDao`, `HistoryDao`, `QueueDao`, and `DownloadDao` with `suspend` write operations and reactive `Flow` query streams.
- Configured `DatabaseModule` in Hilt to provide singleton database and DAO instances with automated background initial data seeding.
- Connected repository layer to Room DAOs, making Favorites, Queue, History, and Downloads operate directly against local database persistence.

## Milestone 12: Media3 Playback Engine & Audio Offload
- Added AndroidX Media3 dependencies (`media3-exoplayer` & `media3-session`) to version catalog and app build configuration.
- Declared `PulsePlaybackService` extending `MediaSessionService` in `AndroidManifest.xml` with `mediaPlayback` foreground service type and required permissions (`FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MEDIA_PLAYBACK`, `WAKE_LOCK`).
- Developed `PulsePlayerManager` wrapping ExoPlayer configured with Wear OS AudioAttributes (`USAGE_MEDIA`, `AUDIO_CONTENT_TYPE_MUSIC`) and `C.WAKE_MODE_LOCAL` for battery-efficient audio offload playback.
- Created `PlaybackRepository` interface and `Media3PlaybackRepositoryImpl` for domain abstraction and Hilt dependency injection.
- Refactored `PlayerViewModel` to expose real-time `StateFlow<PlayerUiState>` driven by ExoPlayer position updates and state transitions.

## Milestone 13: Backend Integration
- Created Retrofit, OkHttp, and Moshi network layer in Hilt `NetworkModule` with 10MB HTTP disk cache, connection pooling, 15s timeouts, GZIP compression, User-Agent header (`PulseWearOS/1.0`), Accept-Language header, and debug logging interceptor.
- Implemented `BackendError` sealed class (`Network`, `Timeout`, `Parsing`, `Unauthorized`, `BackendUnavailable`, `RateLimited`, `Unknown`) and `BackendResult<T>` sealed result wrapper.
- Built `BackendRepository` interface and `BackendRepositoryImpl` supporting `search`, `getTrack`, `getAudioStream`, `getRecommendations`, `getPlaylist`, `getAlbum`, `getArtist`, and `getTrending`.
- Created `BackendProvider` with multi-instance automatic failover supporting Piped, Invidious, and InnerTube endpoints with exponential backoff and server blacklisting.
- Built `MemoryCache` for thread-safe in-memory caching of API responses and stream URLs with TTL expiration.
- Implemented `NetworkMonitor` observing real-time Wear OS network states (`Online`, `Offline`, `Reconnecting`).
- Connected `TrackRepositoryImpl` and `Media3PlaybackRepositoryImpl` to `BackendRepository` for real backend search results and streaming audio playback via ExoPlayer.

## Milestone 14: Download Engine & Offline Playback
- Added AndroidX Media3 download dependencies (`media3-exoplayer-workmanager` & `media3-datasource`) to version catalog and app build.
- Created `PulseDownloadService` extending Media3 `DownloadService` with Wear OS `dataSync` foreground service type, `FOREGROUND_SERVICE_DATA_SYNC` permissions, and ongoing download notifications.
- Created `DownloadModule` in Hilt providing singleton `SimpleCache` (500MB LRU Cache), `CacheDataSource.Factory`, and Media3 `DownloadManager`.
- Configured `PulsePlayerManager` with `CacheDataSource.Factory` for single shared cache between ExoPlayer streaming playback and offline downloads.
- Built production-grade `DownloadsRepositoryImpl` handling `enqueueDownload`, `pauseDownload`, `resumeDownload`, `cancelDownload`, `removeDownload`, `clearCache`, and reactive `observeDownloadProgress` Flow.
- Persisted download states into Room `DownloadDao` and `TrackDao` with cursor mapping and progress polling.
- Updated `DownloadsViewModel` to expose real-time active download progress states and storage management actions.

## Milestone 15: Smart Playback Experience & Queue Engine
- Expanded `PulsePlayerManager` with queue management operations (`playQueue`, `addToQueue`, `playNext`, `removeFromQueue`, `moveQueueItem`, `clearQueue`) synchronized with ExoPlayer and Room `QueueDao`.
- Implemented automatic playback history persistence into Room `HistoryDao`, storing timestamps, track metadata, and position progress.
- Implemented smart resume position rules: resumes at exact position if playback position > 30s and remaining duration > 15s, otherwise restarts at 0s.
- Added variable playback speed controls (`0.75x`, `1.0x`, `1.25x`, `1.5x`, `2.0x`) applied dynamically to ExoPlayer.
- Built coroutine sleep timer with remaining countdown StateFlow and auto-pause triggers.
- Configured audio focus and becoming noisy handling for Bluetooth earbud disconnects.
- Extended `PlaybackRepository`, `Media3PlaybackRepositoryImpl`, and `PlayerViewModel` to expose single source of truth StateFlow for player controls, queue state, speed, and sleep timer.


