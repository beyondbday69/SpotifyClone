import React, { useEffect, useRef } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  Image,
  Dimensions,
  PanResponder,
} from 'react-native';
import { usePlayerStore } from '../store/playerStore';
import { getImageUrl } from '../api';
import { Colors, Spacing, FontSize, BorderRadius } from '../theme';

const { width: SCREEN_WIDTH } = Dimensions.get('window');
const ART_SIZE = SCREEN_WIDTH - 80;

export const NowPlayingScreen: React.FC = () => {
  const {
    currentTrack,
    isPlaying,
    isBuffering,
    duration,
    position,
    togglePlay,
    nextSong,
    prevSong,
    seek,
    setFullScreen,
    likedSongs,
    toggleLike,
  } = usePlayerStore();

  const isLiked = currentTrack ? likedSongs.some(s => s.id === currentTrack.id) : false;

  const formatTime = (ms: number): string => {
    if (!ms || ms <= 0) return '0:00';
    const totalSec = Math.floor(ms / 1000);
    const mins = Math.floor(totalSec / 60);
    const secs = totalSec % 60;
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  };

  const progress = duration > 0 ? position / duration : 0;

  if (!currentTrack) return null;

  return (
    <View style={styles.container}>
      {/* Header */}
      <View style={styles.header}>
        <TouchableOpacity onPress={() => setFullScreen(false)} style={styles.headerBtn}>
          <Text style={styles.headerBtnText}>▼</Text>
        </TouchableOpacity>
        <Text style={styles.headerLabel}>NOW PLAYING</Text>
        <View style={{ width: 40 }} />
      </View>

      {/* Artwork */}
      <View style={styles.artContainer}>
        <Image
          source={{ uri: getImageUrl(currentTrack) }}
          style={styles.art}
          resizeMode="cover"
        />
      </View>

      {/* Track Info */}
      <View style={styles.info}>
        <Text style={styles.title} numberOfLines={1}>{currentTrack.title}</Text>
        <Text style={styles.artist} numberOfLines={1}>{currentTrack.artist}</Text>
      </View>

      {/* Like */}
      <TouchableOpacity onPress={() => toggleLike(currentTrack)} style={styles.likeBtn}>
        <Text style={[styles.likeIcon, isLiked && styles.likeActive]}>
          {isLiked ? '♥' : '♡'}
        </Text>
      </TouchableOpacity>

      {/* Seek bar */}
      <View style={styles.seekContainer}>
        <View style={styles.seekBar}>
          <View style={[styles.seekFill, { width: `${progress * 100}%` }]} />
        </View>
        <View style={styles.timeRow}>
          <Text style={styles.time}>{formatTime(position)}</Text>
          <Text style={styles.time}>{formatTime(duration)}</Text>
        </View>
      </View>

      {/* Controls */}
      <View style={styles.controls}>
        <TouchableOpacity onPress={prevSong} style={styles.ctrlBtn}>
          <Text style={styles.ctrlIcon}>⏮</Text>
        </TouchableOpacity>

        <TouchableOpacity onPress={togglePlay} style={styles.playBtn}>
          <Text style={styles.playIcon}>
            {isBuffering ? '◌' : isPlaying ? '⏸' : '▶'}
          </Text>
        </TouchableOpacity>

        <TouchableOpacity onPress={nextSong} style={styles.ctrlBtn}>
          <Text style={styles.ctrlIcon}>⏭</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: Colors.background,
    paddingTop: 50,
    paddingBottom: 34,
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: Spacing.lg,
    height: 44,
  },
  headerBtn: { padding: 8 },
  headerBtnText: { color: Colors.white, fontSize: FontSize.xl },
  headerLabel: {
    color: Colors.textSecondary,
    fontSize: FontSize.xs,
    fontWeight: 'bold',
    letterSpacing: 1.5,
  },
  artContainer: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: Spacing.xl,
  },
  art: {
    width: ART_SIZE,
    height: ART_SIZE,
    borderRadius: BorderRadius.lg,
    backgroundColor: Colors.surface,
  },
  info: {
    paddingHorizontal: Spacing.xl,
    marginBottom: Spacing.md,
  },
  title: { color: Colors.white, fontSize: FontSize.xl, fontWeight: 'bold' },
  artist: { color: Colors.textSecondary, fontSize: FontSize.md, marginTop: 4 },
  likeBtn: { paddingHorizontal: Spacing.xl, marginBottom: Spacing.md },
  likeIcon: { fontSize: 24, color: Colors.textMuted },
  likeActive: { color: Colors.green },
  seekContainer: { paddingHorizontal: Spacing.xl, marginBottom: Spacing.lg },
  seekBar: {
    height: 4,
    backgroundColor: Colors.surfaceLight,
    borderRadius: 2,
    overflow: 'hidden',
  },
  seekFill: { height: '100%', backgroundColor: Colors.white, borderRadius: 2 },
  timeRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginTop: Spacing.xs,
  },
  time: { color: Colors.textMuted, fontSize: FontSize.xs },
  controls: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 40,
  },
  ctrlBtn: { padding: 12 },
  ctrlIcon: { color: Colors.white, fontSize: 28 },
  playBtn: {
    width: 64,
    height: 64,
    borderRadius: 32,
    backgroundColor: Colors.white,
    alignItems: 'center',
    justifyContent: 'center',
  },
  playIcon: { fontSize: 28, color: Colors.black },
});
