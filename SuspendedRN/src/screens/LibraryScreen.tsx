import React from 'react';
import {
  View,
  Text,
  ScrollView,
  TouchableOpacity,
  StyleSheet,
  Image,
} from 'react-native';
import { useNavigation } from '@react-navigation/native';
import { usePlayerStore } from '../store/playerStore';
import { Colors, Spacing, FontSize, BorderRadius } from '../theme';

export const LibraryScreen: React.FC = () => {
  const navigation = useNavigation<any>();
  const { history, likedSongs } = usePlayerStore();

  return (
    <ScrollView style={styles.container} showsVerticalScrollIndicator={false}>
      <Text style={styles.title}>Your Library</Text>

      {/* Quick access cards */}
      <TouchableOpacity
        style={styles.card}
        onPress={() => navigation.navigate('LikedSongs')}>
        <View style={[styles.cardIcon, { backgroundColor: '#5B21B6' }]}>
          <Text style={styles.cardEmoji}>♥</Text>
        </View>
        <View style={styles.cardInfo}>
          <Text style={styles.cardTitle}>Liked Songs</Text>
          <Text style={styles.cardSub}>{likedSongs.length} songs</Text>
        </View>
      </TouchableOpacity>

      <TouchableOpacity
        style={styles.card}
        onPress={() => navigation.navigate('History')}>
        <View style={[styles.cardIcon, { backgroundColor: Colors.surfaceLight }]}>
          <Text style={styles.cardEmoji}>⏱</Text>
        </View>
        <View style={styles.cardInfo}>
          <Text style={styles.cardTitle}>History</Text>
          <Text style={styles.cardSub}>{history.length} songs</Text>
        </View>
      </TouchableOpacity>

      <View style={{ height: 120 }} />
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.black, padding: Spacing.lg },
  title: { fontSize: FontSize.xxl, fontWeight: 'bold', color: Colors.white, marginBottom: Spacing.xl },
  card: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: Colors.surface,
    borderRadius: BorderRadius.md,
    padding: Spacing.md,
    marginBottom: Spacing.sm,
  },
  cardIcon: { width: 56, height: 56, borderRadius: BorderRadius.md, alignItems: 'center', justifyContent: 'center' },
  cardEmoji: { fontSize: 24, color: Colors.white },
  cardInfo: { flex: 1, marginLeft: Spacing.md },
  cardTitle: { fontSize: FontSize.md, fontWeight: 'bold', color: Colors.white },
  cardSub: { fontSize: FontSize.sm, color: Colors.textSecondary },
});
