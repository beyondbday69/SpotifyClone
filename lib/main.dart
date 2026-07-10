import 'package:flutter/material.dart';

void main() {
  runApp(const OutifyCloneApp());
}

class OutifyCloneApp extends StatelessWidget {
  const OutifyCloneApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Outify Clone',
      theme: ThemeData(
        useMaterial3: true,
        colorScheme: ColorScheme.fromSeed(
          seedColor: const Color(0xFF6750A4),
          brightness: Brightness.dark,
        ),
      ),
      home: const MainScreen(),
    );
  }
}

class MainScreen extends StatefulWidget {
  const MainScreen({super.key});

  @override
  State<MainScreen> createState() => _MainScreenState();
}

class _MainScreenState extends State<MainScreen> {
  int _currentIndex = 0;
  
  final List<Widget> _screens = [
    const HomeScreen(),
    const SearchScreen(),
    const LibraryScreen(),
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Stack(
        children: [
          // Main content
          _screens[_currentIndex],
          
          // MiniPlayer & Floating Bottom Nav
          Positioned(
            left: 0,
            right: 0,
            bottom: 0,
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                const MiniPlayer(),
                const SizedBox(height: 8),
                FloatingBottomNav(
                  currentIndex: _currentIndex,
                  onTap: (index) {
                    setState(() {
                      _currentIndex = index;
                    });
                  },
                ),
                const SizedBox(height: 16), // Padding for bottom
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class FloatingBottomNav extends StatelessWidget {
  final int currentIndex;
  final Function(int) onTap;

  const FloatingBottomNav({
    super.key,
    required this.currentIndex,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final colorScheme = Theme.of(context).colorScheme;
    return Container(
      margin: const EdgeInsets.symmetric(horizontal: 48),
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      decoration: BoxDecoration(
        color: colorScheme.surfaceContainerHighest,
        borderRadius: BorderRadius.circular(32),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.2),
            blurRadius: 10,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceAround,
        children: [
          _NavItem(
            icon: Icons.home_outlined,
            activeIcon: Icons.home,
            label: 'Home',
            isActive: currentIndex == 0,
            onTap: () => onTap(0),
          ),
          _NavItem(
            icon: Icons.search_outlined,
            activeIcon: Icons.search,
            label: 'Search',
            isActive: currentIndex == 1,
            onTap: () => onTap(1),
          ),
          _NavItem(
            icon: Icons.library_music_outlined,
            activeIcon: Icons.library_music,
            label: 'Library',
            isActive: currentIndex == 2,
            onTap: () => onTap(2),
          ),
        ],
      ),
    );
  }
}

class _NavItem extends StatelessWidget {
  final IconData icon;
  final IconData activeIcon;
  final String label;
  final bool isActive;
  final VoidCallback onTap;

  const _NavItem({
    required this.icon,
    required this.activeIcon,
    required this.label,
    required this.isActive,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final colorScheme = Theme.of(context).colorScheme;
    return GestureDetector(
      onTap: onTap,
      behavior: HitTestBehavior.opaque,
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 200),
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
        decoration: BoxDecoration(
          color: isActive ? colorScheme.secondaryContainer : Colors.transparent,
          borderRadius: BorderRadius.circular(24),
        ),
        child: Icon(
          isActive ? activeIcon : icon,
          color: isActive ? colorScheme.onSecondaryContainer : colorScheme.onSurfaceVariant,
        ),
      ),
    );
  }
}

class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final colorScheme = Theme.of(context).colorScheme;
    return Scaffold(
      backgroundColor: colorScheme.surface,
      body: SafeArea(
        bottom: false,
        child: ListView(
          padding: const EdgeInsets.only(bottom: 150), // Space for nav + player
          children: [
            // Header
            Padding(
              padding: const EdgeInsets.fromLTRB(24, 16, 24, 16),
              child: Row(
                children: [
                  CircleAvatar(
                    backgroundColor: colorScheme.primaryContainer,
                    child: Icon(Icons.person, color: colorScheme.onPrimaryContainer),
                  ),
                  const SizedBox(width: 16),
                  Expanded(
                    child: Text(
                      'User',
                      style: Theme.of(context).textTheme.titleLarge?.copyWith(
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ),
                  IconButton(
                    icon: const Icon(Icons.settings_outlined),
                    onPressed: () {},
                  ),
                ],
              ),
            ),

            // Top Artists section
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 8),
              child: Text(
                'Top Artists',
                style: Theme.of(context).textTheme.titleMedium?.copyWith(
                  fontWeight: FontWeight.bold,
                ),
              ),
            ),
            SizedBox(
              height: 120,
              child: ListView.builder(
                scrollDirection: Axis.horizontal,
                padding: const EdgeInsets.symmetric(horizontal: 16),
                itemCount: 5,
                itemBuilder: (context, index) {
                  return Padding(
                    padding: const EdgeInsets.symmetric(horizontal: 8),
                    child: Column(
                      children: [
                        Container(
                          width: 80,
                          height: 80,
                          decoration: BoxDecoration(
                            color: colorScheme.surfaceVariant,
                            shape: BoxShape.circle,
                          ),
                          child: Icon(Icons.person_outline, size: 40, color: colorScheme.onSurfaceVariant),
                        ),
                        const SizedBox(height: 8),
                        Text('Artist ${index + 1}', style: Theme.of(context).textTheme.bodySmall),
                      ],
                    ),
                  );
                },
              ),
            ),

            // Connect Spotify Card
            Padding(
              padding: const EdgeInsets.all(24.0),
              child: Card(
                elevation: 0,
                color: colorScheme.surfaceContainerLow,
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(16),
                ),
                child: Padding(
                  padding: const EdgeInsets.all(24.0),
                  child: Column(
                    children: [
                      const Icon(Icons.link, size: 48),
                      const SizedBox(height: 16),
                      Text(
                        'Connect to Spotify',
                        style: Theme.of(context).textTheme.titleLarge?.copyWith(
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      const SizedBox(height: 8),
                      Text(
                        'Link your account to see personalized stats',
                        textAlign: TextAlign.center,
                        style: Theme.of(context).textTheme.bodyMedium,
                      ),
                      const SizedBox(height: 16),
                      FilledButton(
                        onPressed: () {},
                        child: const Text('Connect'),
                      ),
                    ],
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class SearchScreen extends StatelessWidget {
  const SearchScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final colorScheme = Theme.of(context).colorScheme;
    return Scaffold(
      backgroundColor: colorScheme.surface,
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(24.0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                'Search',
                style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                  fontWeight: FontWeight.bold,
                ),
              ),
              const SizedBox(height: 24),
              SearchBar(
                leading: const Icon(Icons.search),
                hintText: 'Songs, artists, or playlists',
                elevation: MaterialStateProperty.all(0),
                backgroundColor: MaterialStateProperty.all(colorScheme.surfaceVariant),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class LibraryScreen extends StatelessWidget {
  const LibraryScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Theme.of(context).colorScheme.surface,
      body: const Center(child: Text('Library')),
    );
  }
}

class MiniPlayer extends StatelessWidget {
  const MiniPlayer({super.key});

  @override
  Widget build(BuildContext context) {
    final colorScheme = Theme.of(context).colorScheme;
    return Container(
      margin: const EdgeInsets.symmetric(horizontal: 16),
      height: 64,
      decoration: BoxDecoration(
        color: colorScheme.surfaceContainerHigh,
        borderRadius: BorderRadius.circular(12),
      ),
      child: ClipRRect(
        borderRadius: BorderRadius.circular(12),
        child: Stack(
          children: [
            Row(
              children: [
                Container(
                  width: 48,
                  height: 48,
                  margin: const EdgeInsets.all(8),
                  decoration: BoxDecoration(
                    color: colorScheme.surfaceVariant,
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: Icon(Icons.music_note, color: colorScheme.onSurfaceVariant),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Text('Song Title', style: Theme.of(context).textTheme.titleSmall),
                      Text('Artist Name', style: Theme.of(context).textTheme.bodySmall),
                    ],
                  ),
                ),
                IconButton(
                  icon: const Icon(Icons.play_arrow),
                  onPressed: () {},
                ),
                IconButton(
                  icon: const Icon(Icons.skip_next),
                  onPressed: () {},
                ),
                const SizedBox(width: 8),
              ],
            ),
            // Progress Bar
            Positioned(
              left: 0,
              right: 0,
              bottom: 0,
              child: LinearProgressIndicator(
                value: 0.3,
                minHeight: 2,
                backgroundColor: Colors.transparent,
                valueColor: AlwaysStoppedAnimation<Color>(colorScheme.primary),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
