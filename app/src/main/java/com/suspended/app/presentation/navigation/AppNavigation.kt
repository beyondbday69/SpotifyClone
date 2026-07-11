package com.suspended.app.presentation.navigation

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.suspended.app.presentation.home.HomeScreen
import com.suspended.app.presentation.library.LibraryScreen
import com.suspended.app.presentation.player.NowPlayingScreen
import com.suspended.app.presentation.playlist.PlaylistDetailScreen
import com.suspended.app.presentation.search.SearchScreen

// Matches CSS cubic-bezier(0.16, 1, 0.3, 1) "ease-out-expo" from ref design
val ExpoOutEasing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
const val PLAYER_TRANSITION_MS = 500

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Search : Screen("search")
    object Library : Screen("library")
    object NowPlaying : Screen("now_playing")
    object PlaylistDetail : Screen("playlist/{playlistId}") {
        fun createRoute(playlistId: Long) = "playlist/$playlistId"
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier.padding(paddingValues),
        enterTransition = {
            fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(300))
        }
    ) {
        composable(route = Screen.Home.route) {
            HomeScreen(
                onNavigateToPlaylist = { playlistId ->
                    navController.navigate(Screen.PlaylistDetail.createRoute(playlistId))
                },
                onNavigateToNowPlaying = {
                    navController.navigate(Screen.NowPlaying.route)
                }
            )
        }

        composable(route = Screen.Search.route) {
            SearchScreen()
        }

        composable(route = Screen.Library.route) {
            LibraryScreen(
                onNavigateToPlaylist = { playlistId ->
                    navController.navigate(Screen.PlaylistDetail.createRoute(playlistId))
                }
            )
        }

        composable(
            route = Screen.NowPlaying.route,
            // Slide-up + fade-in, expo-out 500ms — matches ref html #full-player enter
            enterTransition = {
                slideInVertically(
                    initialOffsetY = { fullHeight -> fullHeight },
                    animationSpec = tween(durationMillis = PLAYER_TRANSITION_MS, easing = ExpoOutEasing)
                ) + fadeIn(
                    animationSpec = tween(durationMillis = PLAYER_TRANSITION_MS, easing = ExpoOutEasing)
                )
            },
            // Slide-down + fade-out, expo-out 500ms — matches ref html #full-player exit
            exitTransition = {
                slideOutVertically(
                    targetOffsetY = { fullHeight -> fullHeight },
                    animationSpec = tween(durationMillis = PLAYER_TRANSITION_MS, easing = ExpoOutEasing)
                ) + fadeOut(
                    animationSpec = tween(durationMillis = PLAYER_TRANSITION_MS, easing = ExpoOutEasing)
                )
            },
            popEnterTransition = {
                slideInVertically(
                    initialOffsetY = { fullHeight -> fullHeight },
                    animationSpec = tween(durationMillis = PLAYER_TRANSITION_MS, easing = ExpoOutEasing)
                ) + fadeIn(
                    animationSpec = tween(durationMillis = PLAYER_TRANSITION_MS, easing = ExpoOutEasing)
                )
            },
            popExitTransition = {
                slideOutVertically(
                    targetOffsetY = { fullHeight -> fullHeight },
                    animationSpec = tween(durationMillis = PLAYER_TRANSITION_MS, easing = ExpoOutEasing)
                ) + fadeOut(
                    animationSpec = tween(durationMillis = PLAYER_TRANSITION_MS, easing = ExpoOutEasing)
                )
            }
        ) {
            NowPlayingScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.PlaylistDetail.route,
            arguments = listOf(
                navArgument("playlistId") {
                    type = NavType.LongType
                }
            )
        ) {
            PlaylistDetailScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
