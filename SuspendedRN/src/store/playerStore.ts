import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { Track, ytDlpApi, playbackApi, playbackEvents } from '../api';

export type RepeatMode = 'off' | 'one' | 'all';
export type ShuffleMode = 'off' | 'on';

interface PlayerState {
  // ─── playback ───
  currentTrack: Track | null;
  isPlaying: boolean;
  isBuffering: boolean;
  isFullScreen: boolean;
  duration: number;
  position: number;

  // ─── queue ───
  queue: Track[];
  shuffleMode: ShuffleMode;
  repeatMode: RepeatMode;

  // ─── library ───
  likedSongs: Track[];
  history: Track[];
  recentlyPlayed: Track[];
  likedIds: Set<string>;

  // ─── search ───
  recentSearches: string[];

  // ─── theme ───
  themeColor: string;

  // ─── actions ───
  playSong: (track: Track, queue?: Track[]) => Promise<void>;
  togglePlay: () => Promise<void>;
  pause: () => Promise<void>;
  resume: () => Promise<void>;
  seek: (positionMs: number) => Promise<void>;
  nextSong: () => Promise<void>;
  prevSong: () => Promise<void>;
  setFullScreen: (full: boolean) => void;
  toggleShuffle: () => void;
  cycleRepeat: () => void;
  setQueue: (tracks: Track[]) => void;
  addToQueue: (track: Track) => void;

  // ─── library actions ───
  toggleLike: (track: Track) => void;
  addToHistory: (track: Track) => void;

  // ─── search actions ───
  addRecentSearch: (query: string) => void;
  removeRecentSearch: (query: string) => void;
  clearRecentSearches: () => void;

  // ─── theme ───
  setThemeColor: (color: string) => void;
}

const loadLikedIds = (likedSongs: Track[]): Set<string> =>
  new Set(likedSongs.map(t => t.id));

export const usePlayerStore = create<PlayerState>()(
  persist(
    (set, get) => ({
      // ─── initial state ───
      currentTrack: null,
      isPlaying: false,
      isBuffering: false,
      isFullScreen: false,
      duration: 0,
      position: 0,

      queue: [],
      shuffleMode: 'off',
      repeatMode: 'off',

      likedSongs: [],
      history: [],
      recentlyPlayed: [],
      likedIds: new Set(),

      recentSearches: [],

      themeColor: '#1DB954',

      // ��── playback ───

      playSong: async (track: Track, newQueue?: Track[]) => {
        const { addToHistory } = get();
        addToHistory(track);

        set({
          currentTrack: track,
          isPlaying: true,
          isBuffering: true,
          queue: newQueue && newQueue.length > 0 ? newQueue : get().queue,
          isFullScreen: false,
        });

        // Resolve stream URL from yt-dlp
        const streamUrl = await ytDlpApi.resolveStreamUrl(track.id);
        if (!streamUrl) {
          set({ isBuffering: false, isPlaying: false });
          return;
        }

        const title = track.title || track.name || 'Unknown';
        const artist = track.artist || (track as any).artists?.primary?.[0]?.name || 'Unknown';
        const artwork = track.thumbnailUrl || (track as any).image?.[0]?.url || null;

        const started = await playbackApi.play(streamUrl, title, artist, artwork, track.id);
        if (!started) {
          set({ isBuffering: false, isPlaying: false });
        }
      },

      togglePlay: async () => {
        const { isPlaying } = get();
        if (isPlaying) {
          await playbackApi.pause();
          set({ isPlaying: false });
        } else {
          await playbackApi.resume();
          set({ isPlaying: true });
        }
      },

      pause: async () => {
        await playbackApi.pause();
        set({ isPlaying: false });
      },

      resume: async () => {
        await playbackApi.resume();
        set({ isPlaying: true });
      },

      seek: async (positionMs: number) => {
        await playbackApi.seek(positionMs);
        set({ position: positionMs });
      },

      nextSong: async () => {
        const { queue, currentTrack, shuffleMode, repeatMode } = get();
        if (!currentTrack || queue.length === 0) return;

        if (repeatMode === 'one') {
          await get().playSong(currentTrack);
          return;
        }

        const currentIndex = queue.findIndex(t => t.id === currentTrack.id);
        let nextIndex: number;

        if (shuffleMode === 'on') {
          nextIndex = Math.floor(Math.random() * queue.length);
          // Avoid same track on short queues
          if (queue.length > 1 && nextIndex === currentIndex) {
            nextIndex = (nextIndex + 1) % queue.length;
          }
        } else {
          nextIndex = currentIndex + 1;
          if (nextIndex >= queue.length) {
            if (repeatMode === 'all') {
              nextIndex = 0;
            } else {
              set({ isPlaying: false });
              return;
            }
          }
        }

        await get().playSong(queue[nextIndex]);
      },

      prevSong: async () => {
        const { queue, currentTrack, position } = get();
        if (!currentTrack || queue.length === 0) return;

        // Restart if > 3 seconds in
        if (position > 3000) {
          await playbackApi.seek(0);
          return;
        }

        const currentIndex = queue.findIndex(t => t.id === currentTrack.id);
        if (currentIndex > 0) {
          await get().playSong(queue[currentIndex - 1]);
        }
      },

      setFullScreen: (full: boolean) => set({ isFullScreen: full }),

      toggleShuffle: () =>
        set(state => ({
          shuffleMode: state.shuffleMode === 'off' ? 'on' : 'off',
        })),

      cycleRepeat: () =>
        set(state => ({
          repeatMode:
            state.repeatMode === 'off'
              ? 'all'
              : state.repeatMode === 'all'
              ? 'one'
              : 'off',
        })),

      setQueue: (tracks: Track[]) => set({ queue: tracks }),

      addToQueue: (track: Track) =>
        set(state => ({ queue: [...state.queue, track] })),

      // ─── library ───

      toggleLike: (track: Track) =>
        set(state => {
          const isLiked = state.likedIds.has(track.id);
          const newLiked = isLiked
            ? state.likedSongs.filter(t => t.id !== track.id)
            : [track, ...state.likedSongs];
          return {
            likedSongs: newLiked,
            likedIds: loadLikedIds(newLiked),
          };
        }),

      addToHistory: (track: Track) =>
        set(state => {
          const filtered = state.history.filter(t => t.id !== track.id);
          return {
            history: [track, ...filtered].slice(0, 50),
          };
        }),

      // ─── search ───

      addRecentSearch: (query: string) =>
        set(state => {
          const trimmed = query.trim().toLowerCase();
          const filtered = state.recentSearches.filter(s => s.toLowerCase() !== trimmed);
          return {
            recentSearches: [query, ...filtered].slice(0, 15),
          };
        }),

      removeRecentSearch: (query: string) =>
        set(state => ({
          recentSearches: state.recentSearches.filter(s => s !== query),
        })),

      clearRecentSearches: () => set({ recentSearches: [] }),

      // ─── theme ───

      setThemeColor: (color: string) => set({ themeColor: color }),
    }),
    {
      name: 'suspended-storage',
      storage: createJSONStorage(() => AsyncStorage),
      partialize: state => ({
        likedSongs: state.likedSongs,
        likedIds: Array.from(state.likedIds),
        history: state.history.slice(0, 20),
        recentSearches: state.recentSearches,
        themeColor: state.themeColor,
        shuffleMode: state.shuffleMode,
        repeatMode: state.repeatMode,
      }),
      merge: (persisted: any, current) => ({
        ...current,
        ...persisted,
        likedIds: new Set(persisted?.likedIds || []),
      }),
    },
  ),
);

// ─── Wire native event listeners ────────────────────────────

let listenersAttached = false;

export function attachPlaybackListeners() {
  if (listenersAttached) return;
  listenersAttached = true;

  playbackEvents.onPlaying(e => {
    usePlayerStore.setState({ isPlaying: e.isPlaying, isBuffering: false });
  });

  playbackEvents.onTick(e => {
    usePlayerStore.setState({
      position: e.position,
      duration: e.duration,
    });
  });

  playbackEvents.onBuffering(() => {
    usePlayerStore.setState({ isBuffering: true });
  });

  playbackEvents.onEnded(() => {
    usePlayerStore.getState().nextSong();
  });

  playbackEvents.onError(e => {
    console.warn('Playback error:', e.message);
    usePlayerStore.setState({ isPlaying: false, isBuffering: false });
  });

  playbackEvents.onTrackChanged(() => {
    usePlayerStore.setState({ isBuffering: false });
  });
}
