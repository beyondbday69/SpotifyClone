package com.suspended.app.presentation

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.suspended.app.domain.usecase.ResolveStreamUrlUseCase
import com.suspended.app.playback.PlaybackController
import com.suspended.app.playback.PlaybackService
import com.suspended.app.presentation.components.MiniPlayer
import com.suspended.app.presentation.navigation.AppNavigation
import com.suspended.app.presentation.navigation.Screen
import com.suspended.app.presentation.player.PlayerViewModel
import com.suspended.app.presentation.theme.SpotifyBlack
import com.suspended.app.presentation.theme.SpotifyGreen
import com.suspended.app.presentation.theme.SuspendedTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val screen: Screen
)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var playbackController: PlaybackController

    @Inject
    lateinit var resolveStreamUrlUseCase: ResolveStreamUrlUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        playbackController.onTrackNeedsResolve = { track ->
            val result = resolveStreamUrlUseCase(track.id)
            result.getOrNull()
        }

        setContent {
            SuspendedTheme {
                MainScreen()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        playbackController.onTrackNeedsResolve = null
    }
}

@Composable
private fun MainScreen() {
    val navController = rememberNavController()
    val playerViewModel: PlayerViewModel = hiltViewModel()

    val currentTrack by playerViewModel.currentTrack.collectAsStateWithLifecycle()
    val isPlaying by playerViewModel.isPlaying.collectAsStateWithLifecycle()
    val progress by playerViewModel.progress.collectAsStateWithLifecycle()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var serviceStarted by rememberSaveable { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { /* Permission result handled by system */ }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying && !serviceStarted) {
            val context = navController.context
            val intent = Intent(context, PlaybackService::class.java)
            context.startService(intent)
            serviceStarted = true
        }
    }

    val bottomNavItems = listOf(
        BottomNavItem(label = "Home", icon = Icons.Rounded.Home, screen = Screen.Home),
        BottomNavItem(label = "Search", icon = Icons.Rounded.Search, screen = Screen.Search),
        BottomNavItem(label = "Library", icon = Icons.Rounded.LibraryMusic, screen = Screen.Library)
    )

    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Search.route,
        Screen.Library.route,
        Screen.PlaylistDetail.route
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = SpotifyBlack,
        bottomBar = {
            if (showBottomBar) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    AnimatedVisibility(
                        visible = currentTrack != null,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                    ) {
                        MiniPlayer(
                            track = currentTrack,
                            isPlaying = isPlaying,
                            progress = progress,
                            onPlayPause = { playerViewModel.playPause() },
                            onSkipNext = { playerViewModel.skipNext() },
                            onClick = {
                                navController.navigate(Screen.NowPlaying.route) {
                                    launchSingleTop = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    NavigationBar(
                        containerColor = SpotifyBlack,
                        contentColor = Color.White
                    ) {
                        bottomNavItems.forEach { item ->
                            val isSelected = currentRoute == item.screen.route

                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    if (currentRoute != item.screen.route) {
                                        navController.navigate(item.screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label
                                    )
                                },
                                label = {
                                    Text(text = item.label)
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = SpotifyGreen,
                                    selectedTextColor = SpotifyGreen,
                                    unselectedIconColor = Color.White.copy(alpha = 0.7f),
                                    unselectedTextColor = Color.White.copy(alpha = 0.7f),
                                    indicatorColor = SpotifyGreen.copy(alpha = 0.15f)
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        AppNavigation(
            navController = navController,
            paddingValues = paddingValues,
            modifier = Modifier.fillMaxSize()
        )
    }
}
