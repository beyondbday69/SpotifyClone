import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  TextInput,
  ScrollView,
  TouchableOpacity,
  StyleSheet,
  Image,
  ActivityIndicator,
  FlatList,
} from 'react-native';
import { useRoute } from '@react-navigation/native';
import { usePlayerStore } from '../store/playerStore';
import { ytDlpApi, Track } from '../api';
import { Colors, Spacing, FontSize, BorderRadius } from '../theme';

export const SearchScreen: React.FC = () => {
  const route = useRoute<any>();
  const { playSong, recentSearches, addRecentSearch, removeRecentSearch, clearRecentSearches } =
    usePlayerStore();
  const [query, setQuery] = useState(route.params?.query || '');
  const [results, setResults] = useState<Track[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (route.params?.query) {
      setQuery(route.params.query);
      doSearch(route.params.query);
    }
  }, [route.params?.query]);

  const doSearch = async (q: string) => {
    if (!q.trim()) return;
    setLoading(true);
    addRecentSearch(q.trim());
    const tracks = await ytDlpApi.search(q, 20);
    setResults(tracks);
    setLoading(false);
  };

  const handleSelect = (track: Track) => {
    playSong(track, results);
  };

  return (
    <View style={styles.container}>
      <TextInput
        style={styles.input}
        placeholder="Search songs..."
        placeholderTextColor={Colors.textMuted}
        value={query}
        onChangeText={setQuery}
        onSubmitEditing={() => doSearch(query)}
        returnKeyType="search"
        autoFocus
      />

      {loading && <ActivityIndicator color={Colors.green} size="large" style={{ marginTop: 30 }} />}

      {!loading && results.length === 0 && recentSearches.length > 0 && (
        <View style={styles.recentSection}>
          <View style={styles.recentHeader}>
            <Text style={styles.sectionTitle}>Recent</Text>
            <TouchableOpacity onPress={clearRecentSearches}>
              <Text style={styles.clearBtn}>Clear all</Text>
            </TouchableOpacity>
          </View>
          {recentSearches.map((s, i) => (
            <View key={`${s}-${i}`} style={styles.recentRow}>
              <TouchableOpacity style={styles.recentItem} onPress={() => { setQuery(s); doSearch(s); }}>
                <Text style={styles.recentText}>{s}</Text>
              </TouchableOpacity>
              <TouchableOpacity onPress={() => removeRecentSearch(s)}>
                <Text style={styles.removeBtn}>✕</Text>
              </TouchableOpacity>
            </View>
          ))}
        </View>
      )}

      <FlatList
        data={results}
        keyExtractor={item => item.id}
        renderItem={({ item, index }) => (
          <TouchableOpacity style={styles.trackRow} onPress={() => handleSelect(item)}>
            <Image source={{ uri: item.thumbnailUrl }} style={styles.trackImage} />
            <View style={styles.trackInfo}>
              <Text style={styles.trackTitle} numberOfLines={1}>{item.title}</Text>
              <Text style={styles.trackArtist} numberOfLines={1}>{item.artist}</Text>
            </View>
            <Text style={styles.trackDuration}>{formatDuration(item.duration)}</Text>
          </TouchableOpacity>
        )}
        ListFooterComponent={<View style={{ height: 120 }} />}
        contentContainerStyle={{ paddingBottom: 20 }}
      />
    </View>
  );
};

function formatDuration(seconds: number): string {
  if (!seconds || seconds <= 0) return '';
  const mins = Math.floor(seconds / 60);
  const secs = Math.floor(seconds % 60);
  return `${mins}:${secs.toString().padStart(2, '0')}`;
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.black, padding: Spacing.lg },
  input: {
    backgroundColor: Colors.surfaceLight,
    borderRadius: BorderRadius.md,
    paddingHorizontal: Spacing.lg,
    paddingVertical: Spacing.md,
    color: Colors.white,
    fontSize: FontSize.md,
    marginBottom: Spacing.lg,
  },
  recentSection: { marginTop: Spacing.md },
  recentHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: Spacing.md },
  sectionTitle: { fontSize: FontSize.lg, fontWeight: 'bold', color: Colors.white },
  clearBtn: { fontSize: FontSize.sm, color: Colors.green },
  recentRow: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', paddingVertical: Spacing.sm },
  recentItem: { flex: 1 },
  recentText: { fontSize: FontSize.base, color: Colors.textSecondary },
  removeBtn: { fontSize: FontSize.md, color: Colors.textMuted, paddingHorizontal: Spacing.md },
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
  trackDuration: { color: Colors.textMuted, fontSize: FontSize.sm },
});
