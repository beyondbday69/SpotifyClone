import React from 'react';
import { View, Text, TouchableOpacity, StyleSheet, Image } from 'react-native';
import { usePlayerStore } from '../store/playerStore';
import { getImageUrl } from '../api';
import { Colors, Spacing, FontSize, BorderRadius } from '../theme';

export const MiniPlayer: React.FC = () => {
  const {
    currentTrack,
    isPlaying,
    isBuffering,
    duration,
    position,
    togglePlay,
    setFullScreen,
  } = usePlayerStore();

  if (!currentTrack) return null;

  const progress = duration > 0 ? position / duration : 0;

  return (
    <TouchableOpacity
      style={styles.container}
      activeOpacity={0.9}
      onPress={() => setFullScreen(true)}>
      <View style={styles.content}>
        <Image
          source={{ uri: getImageUrl(currentTrack) }}
          style={styles.art}
        />
        <View style={styles.info}>
          <Text style={styles.title} numberOfLines={1}>{currentTrack.title}</Text>
          <Text style={styles.artist} numberOfLines={1}>{currentTrack.artist}</Text>
        </View>
        <TouchableOpacity onPress={togglePlay} style={styles.playBtn}>
          <Text style={styles.playIcon}>
            {isBuffering ? '◌' : isPlaying ? '⏸' : '▶'}
          </Text>
        </TouchableOpacity>
      </View>

      {/* Progress bar */}
      <View style={styles.progressBar}>
        <View style={[styles.progressFill, { width: `${progress * 100}%` }]} />
      </View>
    </TouchableOpacity>
  );
};

const styles = StyleSheet.create({
  container: {
    position: 'absolute',
    bottom: 60,
    left: Spacing.md,
    right: Spacing.md,
    height: 64,
    backgroundColor: Colors.surfaceElevated,
    borderRadius: BorderRadius.md,
    overflow: 'hidden',
    borderWidth: 1,
    borderColor: Colors.border,
  },
  content: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: Spacing.sm,
    gap: Spacing.sm,
  },
  art: {
    width: 44,
    height: 44,
    borderRadius: BorderRadius.sm,
    backgroundColor: Colors.surface,
  },
  info: { flex: 1 },
  title: { color: Colors.white, fontSize: FontSize.base, fontWeight: '600' },
  artist: { color: Colors.textSecondary, fontSize: FontSize.xs },
  playBtn: { padding: 10 },
  playIcon: { color: Colors.white, fontSize: 20 },
  progressBar: {
    height: 2,
    backgroundColor: Colors.surfaceLight,
  },
  progressFill: {
    height: '100%',
    backgroundColor: Colors.white,
  },
});
