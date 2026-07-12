import React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { Text, View } from 'react-native';

import { HomeScreen } from '../screens/HomeScreen';
import { SearchScreen } from '../screens/SearchScreen';
import { LibraryScreen } from '../screens/LibraryScreen';
import { LikedSongsScreen } from '../screens/LikedSongsScreen';
import { NowPlayingScreen } from '../screens/NowPlayingScreen';
import { MiniPlayer } from '../components/MiniPlayer';
import { Colors, FontSize } from '../theme';
import { usePlayerStore } from '../store/playerStore';

const Stack = createNativeStackNavigator();
const Tab = createBottomTabNavigator();

const TabIcon: React.FC<{ label: string; icon: string; focused: boolean }> = ({
  label,
  icon,
  focused,
}) => (
  <View style={{ alignItems: 'center', gap: 2 }}>
    <Text style={{ fontSize: 20, color: focused ? Colors.green : Colors.textMuted }}>
      {icon}
    </Text>
    <Text
      style={{
        fontSize: FontSize.xs,
        fontWeight: focused ? 'bold' : 'normal',
        color: focused ? Colors.white : Colors.textMuted,
      }}>
      {label}
    </Text>
  </View>
);

const Tabs: React.FC = () => (
  <Tab.Navigator
    screenOptions={{
      headerShown: false,
      tabBarShowLabel: false,
      tabBarStyle: {
        backgroundColor: Colors.black,
        borderTopColor: Colors.border,
        height: 56,
        paddingBottom: 8,
      },
    }}>
    <Tab.Screen
      name="HomeTab"
      component={HomeScreen}
      options={{
        tabBarIcon: ({ focused }) => (
          <TabIcon label="Home" icon="⌂" focused={focused} />
        ),
      }}
    />
    <Tab.Screen
      name="SearchTab"
      component={SearchScreen}
      options={{
        tabBarIcon: ({ focused }) => (
          <TabIcon label="Search" icon="🔍" focused={focused} />
        ),
      }}
    />
    <Tab.Screen
      name="LibraryTab"
      component={LibraryScreen}
      options={{
        tabBarIcon: ({ focused }) => (
          <TabIcon label="Library" icon="📚" focused={focused} />
        ),
      }}
    />
  </Tab.Navigator>
);

export const RootNavigator: React.FC = () => {
  const { currentTrack } = usePlayerStore();

  return (
    <NavigationContainer>
      <View style={{ flex: 1, backgroundColor: Colors.black }}>
        <Stack.Navigator screenOptions={{ headerShown: false, animation: 'slide_from_right' }}>
          <Stack.Screen name="Tabs" component={Tabs} />
          <Stack.Screen
            name="LikedSongs"
            component={LikedSongsScreen}
            options={{ presentation: 'modal', animation: 'slide_from_bottom' }}
          />
          <Stack.Screen
            name="NowPlaying"
            component={NowPlayingScreen}
            options={{
              presentation: 'modal',
              animation: 'slide_from_bottom',
            }}
          />
        </Stack.Navigator>

        {/* Mini player overlay when a track is loaded */}
        {currentTrack && <MiniPlayer />}
      </View>
    </NavigationContainer>
  );
};
