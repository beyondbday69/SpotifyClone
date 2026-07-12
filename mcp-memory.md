# SpotifyClone Project — Feb 2025

## Current context
- Repo: `/root/SpotifyClone` (GitHub: beyondbday69/SpotifyClone)
- Working branch: **`spotrn`** (branched from `ui-improvement`)
- Original app is a **Kotlin Android app** with Python+yt-dlp embedded via **Chaquopy** (`com.chaquo.python.Python`).
- Goal: build a **React Native Android port** with same architecture — RN UI ↔ Kotlin native module ↔ Chaquopy running yt-dlp in-process. No VPS, no Termux.

## Architecture (COMPLETED - Phase 1)
```
React Native (JS)
  └── src/api/index.ts              (calls NativeModules.YtDlp / .Playback)
  └── src/store/playerStore.ts      (Zustand, same as web source)
  └── src/screens/{Home,Search,Library,LikedSongs,NowPlaying}Screen.tsx
  └── src/components/MiniPlayer.tsx
  └── src/navigation/RootNavigator.tsx
  └── src/theme/index.ts

Android native (Kotlin)
  └── MainApplication.kt            (inits Chaquopy + registers packages)
  └── bridge/YtDlpModule.kt         (NativeModules.YtDlp: search/resolve/download/cache)
  └── bridge/BridgePackage.kt
  └── playback/PlaybackController.kt (ExoPlayer + MediaSession singleton, events → JS)
  └── playback/PlaybackModule.kt     (NativeModules.Playback: play/pause/seek/stop)
  └── playback/PlaybackService.kt    (foreground MediaSessionService)
  └── playback/PlaybackPackage.kt

Chaquopy
  └── android/app/src/main/python/ytdlp_bridge.py (from rn-backend/)
  └── Gradle: chaquopy { pip install yt-dlp==2026.7.4 }
```

## User decisions
- Android only
- yt-dlp runs **inside** the Android process via Chaquopy (same as Kotlin app)
- Local storage on device only (AsyncStorage for Zustand persist)
- Drop Firebase/social/chat from the source Vite app
- Keep: Home, Search, Library, LikedSongs, Player, Settings
- Branch name: **`spotrn`**

## User behavior
- Responds late (sometimes hours)
- Gets confused by "VPS" / server talk — wants everything local on Android
- Will ask "huh" or "what" when unclear
- Doesn't want to install Termux or manage external servers
- Their current Kotlin app already hosts yt-dlp independently on the device via Chaquopy
- Wants React Native port with same local yt-dlp approach

## Constraints to remember
- The existing Kotlin app is at `app/` — NOT deleting it yet (user may still want it)
- Chaquopy 16.0.0 + Python 3.11 embedded in APK (~50-80 MB APK size)
- Min SDK: 26 (Chaquopy requirement)
- The source web app uses Zustand + HTMLAudioElement → swapped to native ExoPlayer
- Firebase/social/chat removed from this port

## What's done
- [x] Branch `spotrn` created
- [x] Chaquopy Gradle plugin added to root + app build.gradle
- [x] ytdlp_bridge.py + requirements.txt copied into `SuspendedRN/android/app/src/main/python/`
- [x] MainApplication.kt — initializes Python.start(AndroidPlatform(this))
- [x] YtDlpModule.kt — NativeModule exposing search/resolve/download/cacheStreamUrl/getCachedStreamUrl
- [x] BridgePackage.kt — registers YtDlpModule
- [x] PlaybackController.kt — ExoPlayer singleton, emits events to JS via RCTDeviceEventEmitter
- [x] PlaybackModule.kt — NativeModule exposing play/pause/resume/togglePlay/seek/stop/getState
- [x] PlaybackService.kt — foreground MediaSessionService for background playback
- [x] PlaybackPackage.kt — registers PlaybackModule
- [x] AndroidManifest.xml — INTERNET, FOREGROUND_SERVICE, MEDIA_PLAYBACK, POST_NOTIFICATIONS, WAKE_LOCK
- [x] package.json — added @react-navigation/*, zustand, async-storage, react-native-screens, safe-area-context
- [x] src/api/index.ts — JS API client wrapping both native modules
- [x] src/store/playerStore.ts — Zustand store (ported from Vite playerStore, no Firebase/social)
- [x] src/theme/index.ts — Spotify-inspired dark theme colors
- [x] src/screens/ — HomeScreen, SearchScreen, LibraryScreen, LikedSongsScreen, NowPlayingScreen
- [x] src/components/MiniPlayer.tsx — floating mini player bar
- [x] src/navigation/RootNavigator.tsx — bottom tabs + stack (modal for NowPlaying, LikedSongs)
- [x] App.tsx — root entry point

## TODO next
- [ ] Install npm packages (`cd SuspendedRN && npm install`)
- [ ] Try build (`cd SuspendedRN/android && ./gradlew assembleDebug`)
- [ ] Fix any Chaquopy / Kotlin compile errors
- [ ] Add yt-dlp_bridge.py postprocessor support for ffmpeg (download needs it)
- [ ] Wire up the NowPlaying screen as a modal triggered from MiniPlayer tap → navigation.navigate('NowPlaying')
- [ ] Test search → resolve → play flow end-to-end
- [ ] Add notification controls integration (MediaSessionMetadata)
- [ ] Port remaining screens from source (AlbumDetails, ArtistDetails, PlaylistDetails) if needed
- [ ] Consider adding a settings screen for streaming quality / theme color
