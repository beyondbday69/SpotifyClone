# SpotifyClone Project — Feb 2025

## Current context
- Repo: `/root/SpotifyClone` (GitHub: beyondbday69/SpotifyClone)
- Working branch: **`spotrn`** (branched from `ui-improvement`)
- Original app is a **Kotlin Android app** with Python+yt-dlp embedded via **Chaquopy** (`com.chaquo.python.Python`).
- Goal: build a **React Native Android port** with same architecture — RN UI ↔ Kotlin native module ↔ Chaquopy running yt-dlp in-process. No VPS, no Termux.

## Architecture (COMPLETED - Phase 1 - BUILDING ON CI ✅)
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
  ��── playback/PlaybackModule.kt     (NativeModules.Playback: play/pause/seek/stop)
  └── playback/PlaybackService.kt    (foreground MediaSessionService)
  └── playback/PlaybackPackage.kt

Chaquopy
  └── android/app/src/main/python/ytdlp_bridge.py (from rn-backend/)
  └── Gradle: chaquopy { pip install yt-dlp==2026.7.4 }
```

## CI Status
- GitHub Actions: `.github/workflows/build-spotrn.yml` → **BUILDING SUCCESSFULLY** ✅
- APK artifact: `suspendedrn-debug` (uploaded on each push to spotrn)
- Key fixes needed to build:
  - CLI downgraded to 14.1.0 (RN 0.76 compatible)
  - react-native-screens pinned to 3.35.x (not 4.x which needs RN 0.84)
  - Autolink disabled (we only use our own native modules)
  - `--legacy-peer-deps` for npm install
  - Groovy list syntax for ndk abiFilters (not Kotlin's `listOf()`)

## User behavior
- Responds late (sometimes hours)
- Gets confused by "VPS" / server talk — wants everything local on Android
- Will ask "huh" or "what" when unclear
- Doesn't want to install Termux or manage external servers
- Their current Kotlin app already hosts yt-dlp independently on the device via Chaquopy
- Wants React Native port with same local yt-dlp approach

## What's done
- [x] Branch `spotrn` created
- [x] Chaquopy Gradle plugin added to root + app build.gradle
- [x] ytdlp_bridge.py + requirements.txt copied into android/app/src/main/python/
- [x] MainApplication.kt — initializes Python.start(AndroidPlatform(this))
- [x] YtDlpModule.kt — NativeModule exposing search/resolve/download/cacheStreamUrl/getCachedStreamUrl
- [x] BridgePackage.kt — registers YtDlpModule
- [x] PlaybackController.kt — ExoPlayer singleton, emits events to JS via RCTDeviceEventEmitter
- [x] PlaybackModule.kt — NativeModule exposing play/pause/resume/togglePlay/seek/stop/getState
- [x] PlaybackService.kt — foreground MediaSessionService for background playback
- [x] PlaybackPackage.kt — registers PlaybackModule
- [x] AndroidManifest.xml — INTERNET, FOREGROUND_SERVICE, MEDIA_PLAYBACK, POST_NOTIFICATIONS, WAKE_LOCK
- [x] package.json — all deps with correct versions
- [x] src/api/index.ts — JS API client wrapping both native modules
- [x] src/store/playerStore.ts — Zustand store (ported from Vite playerStore, no Firebase/social)
- [x] src/theme/index.ts — Spotify-inspired dark theme colors
- [x] src/screens/ — HomeScreen, SearchScreen, LibraryScreen, LikedSongsScreen, NowPlayingScreen
- [x] src/components/MiniPlayer.tsx — floating mini player bar
- [x] src/navigation/RootNavigator.tsx — bottom tabs + stack (modal for NowPlaying, LikedSongs)
- [x] App.tsx — root entry point
- [x] CI workflow → BUILDING SUCCESSFULLY ✅

## TODO next
- [ ] Download APK artifact from GitHub Actions and test on device
- [ ] Test search → resolve → play flow end-to-end
- [ ] Wire up MiniPlayer tap → navigation.navigate('NowPlaying')
- [ ] Add notification controls integration (MediaSessionMetadata)
- [ ] Port remaining screens from source (AlbumDetails, ArtistDetails, PlaylistDetails) if needed
- [ ] Add a settings screen for streaming quality / theme color
- [ ] Handle the case where autolink is disabled but react-native-screens still needs native linking
