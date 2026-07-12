import React, { useEffect, useState } from 'react';
import {
  View,
  Text,
  ScrollView,
  TouchableOpacity,
  StyleSheet,
  Image,
  ActivityIndicator,
} from 'react-native';
import { useNavigation } from '@react-navigation/native';
import { usePlayerStore, attachPlaybackListeners } from '../store/playerStore';
import { ytDlpApi, Track } from '../api';
import { Colors, Spacing, FontSize, BorderRadius } from '../theme';

const QUICK_PICKS = [
  { query: 'Taylor Swift', label: 'Taylor Swift' },
  { query: 'Drake', label: 'Drake' },
  { query: 'The Weeknd', label: 'The Weeknd' },
  { query: 'BTS', label: 'BTS' },
  { query: 'Ed Sheeran', label: 'Ed Sheeran' },
  { query: 'Arijit Singh', label: 'Arijit Singh' },
];

const GREETINGS = ['Good evening', 'Good night', 'Good morning', 'Good afternoon'];

const getGreeting = (): string => {
  const h = new Date().getHours();
  if (h < 12) return GREETINGS[2];
  if (h < 17) return GREETINGS[3];
  if (h < 21) return GREETINGS[0];
  return GREETINGS[1];
};

export const HomeScreen: React.FC = () => {
  const navigation = useNavigation<any>();
  const { history, playSong } = usePlayerStore();
  const [trendingTracks, setTrendingTracks] = useState<Track[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    attachPlaybackListeners();
    (async () => {
      const tracks = await ytDlpApi.search('popular music 2024', 10);
      setTrendingTracks(tracks);
      setLoading(false);
    })();
  }, []);

  const handleQuickPick = (query: string) => {
    navigation.navigate('Search', { query });
  };

  return (
    <ScrollView style={styles.container} showsVerticalScrollIndicator={false}>
      <Text style={styles.greeting}>{getGreeting()}</Text>

      {/* Quick picks grid */}
      <View style={styles.quickGrid}>
        {QUICK_PICKS.map(p => (
          <TouchableOpacity
            key={p.query}
            style={styles.quickCard}
            onPress={() => handleQuickPick(p.query)}>
            <Text style={styles.quickLabel} numberOfLines={1}>{p.label}</Text>
          </TouchableOpacity>
        ))}
      </View>

      {/* Recently played */}
      {history.length > 0 && (
        <Section title="Recently Played">
          <ScrollView horizontal showsHorizontalScrollIndicator={false}>
            {history.slice(0, 10).map(t => (
              <TouchableOpacity
                key={t.id}
                style={styles.card}
                onPress={() => playSong(t, history.slice(0, 10))}>
                <Image source={{ uri: t.thumbnailUrl }} style={styles.cardImage} />
                <Text style={styles.cardTitle} numberOfLines={1}>{t.title}</Text>
                <Text style={styles.cardSub} numberOfLines={1}>{t.artist}</Text>
              </TouchableOpacity>
            ))}
          </ScrollView>
        </Section>
      )}

      {/* Trending */}
      <Section title="Trending">
        {loading ? (
          <ActivityIndicator color={Colors.green} size="small" style={{ paddingVertical: 20 }} />
        ) : (
          <ScrollView horizontal showsHorizontalScrollIndicator={false}>
            {trendingTracks.map(t => (
              <TouchableOpacity
                key={t.id}
                style={styles.card}
                onPress={() => playSong(t, trendingTracks)}>
                <Image source={{ uri: t.thumbnailUrl }} style={styles.cardImage} />
                <Text style={styles.cardTitle} numberOfLines={1}>{t.title}</Text>
                <Text style={styles.cardSub} numberOfLines={1}>{t.artist}</Text>
              </TouchableOpacity>
            ))}
          </ScrollView>
        )}
      </Section>

      <View style={{ height: 120 }} />
    </ScrollView>
  );
};

const Section: React.FC<{ title: string; children: React.ReactNode }> = ({ title, children }) => (
  <View style={styles.section}>
    <Text style={styles.sectionTitle}>{title}</Text>
    {children}
  </View>
);

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.black, padding: Spacing.lg },
  greeting: { fontSize: FontSize.xxl, fontWeight: 'bold', color: Colors.white, marginBottom: Spacing.lg },
  quickGrid: { flexDirection: 'row', flexWrap: 'wrap', gap: Spacing.sm, marginBottom: Spacing.xl },
  quickCard: {
    width: '48%',
    backgroundColor: Colors.surfaceLight,
    borderRadius: BorderRadius.md,
    padding: Spacing.md,
    flexDirection: 'row',
    alignItems: 'center',
  },
  quickLabel: { color: Colors.white, fontSize: FontSize.base, fontWeight: 'bold' },
  section: { marginBottom: Spacing.xl },
  sectionTitle: { fontSize: FontSize.xl, fontWeight: 'bold', color: Colors.white, marginBottom: Spacing.md },
  card: { width: 150, marginRight: Spacing.md },
  cardImage: { width: 150, height: 150, borderRadius: BorderRadius.md, marginBottom: Spacing.xs },
  cardTitle: { fontSize: FontSize.sm, color: Colors.white, fontWeight: '600' },
  cardSub: { fontSize: FontSize.xs, color: Colors.textSecondary },
});
