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
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
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

@OptIn(androidx.compose.animation.ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = staticCompositionLocalOf<SharedTransitionScope?> { null }
val LocalNavAnimatedVisibilityScope = staticCompositionLocalOf<AnimatedVisibilityScope?> { null }

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
            val themeViewModel: com.suspended.app.presentation.theme.ThemeViewModel = hiltViewModel()
            val themeState by themeViewModel.appTheme.collectAsStateWithLifecycle()
            
            SuspendedTheme(appTheme = themeState) {
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

    val playbackState by playerViewModel.playbackState.collectAsStateWithLifecycle()
    val errorMessage by playerViewModel.errorMessage.collectAsStateWithLifecycle()

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
    
    val snackbarHostState = androidx.compose.runtime.remember { androidx.compose.material3.SnackbarHostState() }
    
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(
                message = errorMessage!!,
                duration = androidx.compose.material3.SnackbarDuration.Short
            )
            playerViewModel.clearError()
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

    @OptIn(ExperimentalSharedTransitionApi::class, androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)
    SharedTransitionLayout {
        CompositionLocalProvider(LocalSharedTransitionScope provides this) {
            val scrollBehavior = androidx.compose.material3.FloatingToolbarDefaults.exitAlwaysScrollBehavior(
                exitDirection = androidx.compose.material3.FloatingToolbarExitDirection.Bottom
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior)
            ) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = SpotifyBlack,
                    snackbarHost = { androidx.compose.material3.SnackbarHost(hostState = snackbarHostState) }
                ) { paddingValues ->
                    // Calculate extra padding needed for overlays
                    val toolbarHeight = if (showBottomBar) 80.dp else 0.dp
                    val playerHeight = if (currentTrack != null) 80.dp else 0.dp
                    val adjustedPadding = PaddingValues(
                        top = paddingValues.calculateTopPadding(),
                        bottom = paddingValues.calculateBottomPadding() + toolbarHeight + playerHeight,
                        start = paddingValues.calculateLeftPadding(LayoutDirection.Ltr),
                        end = paddingValues.calculateRightPadding(LayoutDirection.Ltr)
                    )

                    AppNavigation(
                        navController = navController,
                        paddingValues = adjustedPadding,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Overlay for Player and Toolbar
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                        .padding(horizontal = 8.dp)
                        .zIndex(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnimatedVisibility(
                        visible = currentTrack != null,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                    ) {
                        CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this) {
                            MiniPlayer(
                                track = currentTrack,
                                isPlaying = isPlaying,
                                isLoading = playbackState == com.suspended.app.playback.PlaybackState.LOADING,
                                progress = progress,
                                onPlayPause = { playerViewModel.playPause() },
                                onSkipNext = { playerViewModel.skipNext() },
                                onSkipPrevious = { playerViewModel.skipPrevious() },
                                onDismiss = { playerViewModel.stop() },
                                onClick = {
                                    navController.navigate(Screen.NowPlaying.route) {
                                        launchSingleTop = true
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    if (showBottomBar) {
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        com.suspended.app.presentation.components.AppFloatingToolbar(
                            items = bottomNavItems,
                            currentRoute = currentRoute,
                            onItemClick = { item ->
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
                            scrollBehavior = scrollBehavior
                        )
                    }
                }
            }
        }
    }
}
