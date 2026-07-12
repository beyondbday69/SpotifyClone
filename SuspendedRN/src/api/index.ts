import { NativeModules, NativeEventEmitter, Platform } from 'react-native';

/**
 * Type-safe wrapper around the Kotlin Bridge module.
 *
 * This replaces the old Vite app's api.ts which called:
 *   - /api/search?q=...
 *   - /api/stream-proxy/{id}
 *   - /api/download/{id}
 *
 * Now all calls go through Chaquopy → yt-dlp in the same APK.
 */

const { YtDlp } = NativeModules;
const { Playback } = NativeModules;

if (!YtDlp) {
  console.warn('YtDlp native module not linked. Ensure the module is registered.');
}
if (!Playback) {
  console.warn('Playback native module not linked. Ensure the module is registered.');
}

const YtDlpEmitter = YtDlp ? new NativeEventEmitter(YtDlp) : null;
const PlaybackEmitter = Playback ? new NativeEventEmitter(Playback) : null;

export interface Track {
  id: string;
  title: string;
  artist: string;
  artistId: string;
  duration: number;
  thumbnailUrl: string;
  albumName: string;
  localPath?: string;
  isDownloaded?: boolean;
  isInLibrary?: boolean;
  lastPlayedAt?: number;
  isLiked?: boolean;
  likedAt?: number;
  streamUrl?: string;
}

// ─── yt-dlp operations ──────────────────────────────────────

export const ytDlpApi = {
  /**
   * Search YouTube for tracks via the embedded yt-dlp bridge.
   * @param query - search query
   * @param maxResults - max results (default 15)
   */
  search: async (query: string, maxResults: number = 15): Promise<Track[]> => {
    try {
      const raw: Track[] = await YtDlp.search(query, maxResults);
      return raw;
    } catch (e: any) {
      console.error('ytDlpApi.search failed:', e.message);
      return [];
    }
  },

  /**
   * Resolve a direct audio stream URL for a video ID.
   * Falls back to the cache if available.
   */
  resolveStreamUrl: async (videoId: string): Promise<string | null> => {
    try {
      // Check cache first
      const cached: string = await YtDlp.getCachedStreamUrl(videoId);
      if (cached && cached.length > 0) return cached;

      // Resolve fresh
      const url: string = await YtDlp.resolveStreamUrl(videoId);
      if (url && url.length > 0) {
        await YtDlp.cacheStreamUrl(videoId, url);
        return url;
      }
      return null;
    } catch (e: any) {
      console.error('ytDlpApi.resolveStreamUrl failed:', e.message);
      return null;
    }
  },

  /**
   * Download audio to local storage via yt-dlp.
   * Returns the file path on success.
   */
  download: async (videoId: string): Promise<string | null> => {
    try {
      const path: string = await YtDlp.download(videoId, '');
      return path || null;
    } catch (e: any) {
      console.error('ytDlpApi.download failed:', e.message);
      return null;
    }
  },

  /**
   * Delete a downloaded audio file.
   */
  deleteVideo: async (videoId: string): Promise<boolean> => {
    try {
      return await YtDlp.deleteVideo(videoId);
    } catch (e: any) {
      console.error('ytDlpApi.deleteVideo failed:', e.message);
      return false;
    }
  },
};

// ─── playback control ───────────────────────────────────────

export interface PlaybackState {
  isPlaying: boolean;
  position: number;
  duration: number;
  mediaId: string;
}

export const playbackApi = {
  play: async (
    streamUrl: string,
    title: string,
    artist: string,
    artworkUrl: string | null,
    mediaId: string,
  ): Promise<boolean> => {
    try {
      return await Playback.play(streamUrl, title, artist, artworkUrl, mediaId);
    } catch (e: any) {
      console.error('playbackApi.play failed:', e.message);
      return false;
    }
  },

  pause: async (): Promise<boolean> => {
    try {
      return await Playback.pause();
    } catch (e: any) {
      return false;
    }
  },

  resume: async (): Promise<boolean> => {
    try {
      return await Playback.resume();
    } catch (e: any) {
      return false;
    }
  },

  togglePlay: async (): Promise<boolean> => {
    try {
      return await Playback.togglePlay();
    } catch (e: any) {
      return false;
    }
  },

  seek: async (positionMs: number): Promise<boolean> => {
    try {
      return await Playback.seek(positionMs);
    } catch (e: any) {
      return false;
    }
  },

  stop: async (): Promise<void> => {
    try {
      await Playback.stop();
    } catch (_) {}
  },

  getState: async (): Promise<PlaybackState> => {
    try {
      return await Playback.getState();
    } catch (e: any) {
      return { isPlaying: false, position: 0, duration: 0, mediaId: '' };
    }
  },
};

// ─── event listeners ────────────────────────────────────────

export const playbackEvents = {
  onTrackChanged: (cb: (e: any) => void) =>
    PlaybackEmitter?.addListener('PlaybackTrackChanged', cb),

  onPlaying: (cb: (e: { isPlaying: boolean }) => void) =>
    PlaybackEmitter?.addListener('PlaybackPlaying', cb),

  onTick: (cb: (e: { position: number; duration: number; percent: number }) => void) =>
    PlaybackEmitter?.addListener('PlaybackTick', cb),

  onEnded: (cb: (e: any) => void) =>
    PlaybackEmitter?.addListener('PlaybackEnded', cb),

  onBuffering: (cb: (e: any) => void) =>
    PlaybackEmitter?.addListener('PlaybackBuffering', cb),

  onError: (cb: (e: { message: string }) => void) =>
    PlaybackEmitter?.addListener('PlaybackError', cb),

  removeAll: () => {
    PlaybackEmitter?.removeAllListeners('PlaybackTrackChanged');
    PlaybackEmitter?.removeAllListeners('PlaybackPlaying');
    PlaybackEmitter?.removeAllListeners('PlaybackTick');
    PlaybackEmitter?.removeAllListeners('PlaybackEnded');
    PlaybackEmitter?.removeAllListeners('PlaybackBuffering');
    PlaybackEmitter?.removeAllListeners('PlaybackError');
  },
};

// ─── image helper (from source Vite api.ts) ─────────────────

export const getImageUrl = (track: Track): string => {
  if (track.thumbnailUrl && track.thumbnailUrl.length > 0) return track.thumbnailUrl;
  return 'https://picsum.photos/500/500';
};
