package com.suspended.app.presentation.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset

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

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Search : Screen("search")
    object Library : Screen("library")
    object NowPlaying : Screen("now_playing")
    object PlaylistDetail : Screen("playlist/{playlistId}") {
        fun createRoute(playlistId: Long) = "playlist/$playlistId"
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppNavigation(
    navController: NavHostController,
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier
) {
    // Material 3 (Expressive) motion tokens, pulled from the app's
    // MotionScheme (see SuspendedTheme -> MotionScheme.expressive()).
    // Captured here in a composable context and reused as closures inside
    // the transition lambdas below, since those lambdas are not themselves
    // @Composable.
    val spatialSpec = MaterialTheme.motionScheme.defaultSpatialSpec<IntOffset>()
    val effectsSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier.padding(paddingValues),
        // M3 "fade through" for top-level destination swaps (Home/Search/Library)
        enterTransition = {
            fadeIn(animationSpec = effectsSpec)
        },
        exitTransition = {
            fadeOut(animationSpec = effectsSpec)
        },
        popEnterTransition = {
            fadeIn(animationSpec = effectsSpec)
        },
        popExitTransition = {
            fadeOut(animationSpec = effectsSpec)
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
            // Big Now Playing UI: M3 spatial slide-up on enter, slide-down on
            // exit (and the same on pop), driven by the MotionScheme's
            // spatial/effects specs rather than a hand-tuned easing curve.
            enterTransition = {
                slideInVertically(
                    initialOffsetY = { fullHeight -> fullHeight },
                    animationSpec = spatialSpec
                ) + fadeIn(animationSpec = effectsSpec)
            },
            exitTransition = {
                slideOutVertically(
                    targetOffsetY = { fullHeight -> fullHeight },
                    animationSpec = spatialSpec
                ) + fadeOut(animationSpec = effectsSpec)
            },
            popEnterTransition = {
                slideInVertically(
                    initialOffsetY = { fullHeight -> fullHeight },
                    animationSpec = spatialSpec
                ) + fadeIn(animationSpec = effectsSpec)
            },
            popExitTransition = {
                slideOutVertically(
                    targetOffsetY = { fullHeight -> fullHeight },
                    animationSpec = spatialSpec
                ) + fadeOut(animationSpec = effectsSpec)
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
            ),
            // Drill-in / drill-out: M3 shared-axis-style horizontal slide.
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = spatialSpec
                ) + fadeIn(animationSpec = effectsSpec)
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> -fullWidth / 4 },
                    animationSpec = spatialSpec
                ) + fadeOut(animationSpec = effectsSpec)
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> -fullWidth / 4 },
                    animationSpec = spatialSpec
                ) + fadeIn(animationSpec = effectsSpec)
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> fullWidth },
                    animationSpec = spatialSpec
                ) + fadeOut(animationSpec = effectsSpec)
            }
        ) {
            PlaylistDetailScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
