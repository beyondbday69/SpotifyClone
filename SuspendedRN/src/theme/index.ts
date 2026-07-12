/**
 * Spotify-inspired dark theme. Ported from the Vite app's Tailwind classes.
 * All colors use hex values; RN StyleSheet doesn't support CSS variables.
 */
import { usePlayerStore } from '../store/playerStore';

export const Colors = {
  // Core
  black: '#000000',
  white: '#FFFFFF',
  background: '#121212',
  surface: '#181818',
  surfaceLight: '#282828',
  surfaceElevated: '#2A2A2A',

  // Text
  textPrimary: '#FFFFFF',
  textSecondary: '#B3B3B3',
  textMuted: '#535353',
  textInvert: '#000000',

  // Accent
  green: '#1DB954',
  greenLight: '#1ED760',
  greenDark: '#1AA34A',

  // Semantic
  error: '#E91E63',
  success: '#1DB954',
  warning: '#FFA726',
  info: '#2979FF',

  // Overlay
  overlay: 'rgba(0, 0, 0, 0.6)',
  overlayLight: 'rgba(0, 0, 0, 0.3)',

  // Borders
  border: 'rgba(255, 255, 255, 0.1)',
  borderLight: 'rgba(255, 255, 255, 0.2)',
} as const;

export const Spacing = {
  xs: 4,
  sm: 8,
  md: 12,
  lg: 16,
  xl: 24,
  xxl: 32,
  xxxl: 48,
} as const;

export const FontSize = {
  xs: 10,
  sm: 12,
  base: 14,
  md: 16,
  lg: 18,
  xl: 24,
  xxl: 32,
  hero: 48,
} as const;

export const BorderRadius = {
  sm: 4,
  md: 8,
  lg: 12,
  xl: 16,
  xxl: 24,
  full: 9999,
} as const;

export const useThemeColor = () => {
  return usePlayerStore(s => s.themeColor);
};
