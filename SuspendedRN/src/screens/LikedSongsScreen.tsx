import React from 'react';
import {
  View,
  Text,
  FlatList,
  TouchableOpacity,
  StyleSheet,
  Image,
} from 'react-native';
import { usePlayerStore } from '../store/playerStore';
import { Track } from '../api';
import { Colors, Spacing, FontSize, BorderRadius } from '../theme';

export const LikedSongsScreen: React.FC = () => {
  const { likedSongs, playSong, toggleLike } = usePlayerStore();

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.title}>Liked Songs</Text>
        <Text style={styles.count}>{likedSongs.length} songs</Text>
      </View>

      {likedSongs.length === 0 ? (
        <View style={styles.empty}>
          <Text style={styles.emptyIcon}>♥</Text>
          <Text style={styles.emptyText}>No liked songs yet</Text>
          <Text style={styles.emptySub}>Tap the + button on any song to add it here</Text>
        </View>
      ) : (
        <FlatList
          data={likedSongs}
          keyExtractor={item => item.id}
          renderItem={({ item }) => (
            <TouchableOpacity style={styles.trackRow} onPress={() => playSong(item, likedSongs)}>
              <Image source={{ uri: item.thumbnailUrl }} style={styles.trackImage} />
              <View style={styles.trackInfo}>
                <Text style={styles.trackTitle} numberOfLines={1}>{item.title}</Text>
                <Text style={styles.trackArtist} numberOfLines={1}>{item.artist}</Text>
              </View>
              <TouchableOpacity onPress={() => toggleLike(item)}>
                <Text style={styles.removeBtn}>✕</Text>
              </TouchableOpacity>
            </TouchableOpacity>
          )}
          ListFooterComponent={<View style={{ height: 120 }} />}
        />
      )}
    </View>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.black, padding: Spacing.lg },
  header: { marginBottom: Spacing.xl },
  title: { fontSize: FontSize.xxl, fontWeight: 'bold', color: Colors.white },
  count: { fontSize: FontSize.sm, color: Colors.textSecondary, marginTop: Spacing.xs },
  empty: { flex: 1, alignItems: 'center', justifyContent: 'center' },
  emptyIcon: { fontSize: 64, color: Colors.textMuted, marginBottom: Spacing.lg },
  emptyText: { fontSize: FontSize.lg, fontWeight: 'bold', color: Colors.white },
  emptySub: { fontSize: FontSize.sm, color: Colors.textSecondary, textAlign: 'center', marginTop: Spacing.sm },
  trackRow: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: Spacing.sm,
    gap: Spacing.md,
  },
  trackImage: { width: 48, height: 48, borderRadius: BorderRadius.sm },
  trackInfo: { flex: 1 },
  trackTitle: { color: Colors.white, fontSize: FontSize.base, fontWeight: '600' },
  trackArtist: { color: Colors.textSecondary, fontSize: FontSize.sm },
  removeBtn: { fontSize: FontSize.md, color: Colors.textMuted, paddingHorizontal: Spacing.md },
});
