package com.suspended.app.presentation.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.suspended.app.playback.RepeatMode
import com.suspended.app.presentation.components.ExpressiveSlider
import com.suspended.app.presentation.theme.SpotifyBlack
import com.suspended.app.presentation.theme.SpotifyGreen
import com.suspended.app.presentation.theme.SpotifyLightGray
import com.suspended.app.presentation.theme.SpotifyWhite
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi

import androidx.compose.animation.ExperimentalSharedTransitionApi

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun NowPlayingScreen(
    onNavigateBack: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val track by viewModel.currentTrack.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val progress by viewModel.progress.collectAsStateWithLifecycle()
    val currentPosition by viewModel.currentPosition.collectAsStateWithLifecycle()
    val duration by viewModel.duration.collectAsStateWithLifecycle()
    val isShuffled by viewModel.isShuffled.collectAsStateWithLifecycle()
    val repeatMode by viewModel.repeatMode.collectAsStateWithLifecycle()

    if (track == null) {
        Box(modifier = Modifier.fillMaxSize().background(SpotifyBlack))
        return
    }

    val sharedTransitionScope = com.suspended.app.presentation.LocalSharedTransitionScope.current
    val animatedVisibilityScope = com.suspended.app.presentation.LocalNavAnimatedVisibilityScope.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .run {
                if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                    with(sharedTransitionScope) {
                        sharedBounds(
                            sharedContentState = rememberSharedContentState(key = "player_bounds"),
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                    }
                } else {
                    this
                }
            }
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF3B3B3B), SpotifyBlack),
                    startY = 0f,
                    endY = 1500f
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .systemBarsPadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = "Minimize", tint = SpotifyWhite)
                }
                Text(
                    text = track?.albumName ?: "Now Playing",
                    style = MaterialTheme.typography.labelMedium,
                    color = SpotifyWhite
                )
                IconButton(onClick = { /* More options */ }) {
                    Icon(Icons.Rounded.MoreVert, contentDescription = "More", tint = SpotifyWhite)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            AsyncImage(
                model = track?.thumbnailUrl,
                contentDescription = "Cover",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .run {
                        if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                            with(sharedTransitionScope) {
                                sharedElement(
                                    sharedContentState = rememberSharedContentState(key = "album_art"),
                                    animatedVisibilityScope = animatedVisibilityScope
                                )
                            }
                        } else {
                            this
                        }
                    }
                    .clip(RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track?.title ?: "",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = SpotifyWhite,
                        maxLines = 1
                    )
                    Text(
                        text = track?.artist ?: "",
                        style = MaterialTheme.typography.bodyLarge,
                        color = SpotifyLightGray,
                        maxLines = 1
                    )
                }
                IconButton(onClick = { track?.let { viewModel.addToLibrary(it) } }) {
                    Icon(
                        imageVector = if (track?.isDownloaded == true) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (track?.isDownloaded == true) SpotifyGreen else SpotifyWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            ExpressiveSlider(
                progress = progress,
                currentPosition = currentPosition,
                duration = duration,
                onSeek = { viewModel.seekTo(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.toggleShuffle() }) {
                    Icon(
                        Icons.Rounded.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (isShuffled) SpotifyGreen else SpotifyWhite
                    )
                }
                IconButton(onClick = { viewModel.skipPrevious() }) {
                    Icon(Icons.Rounded.SkipPrevious, contentDescription = "Previous", tint = SpotifyWhite, modifier = Modifier.size(36.dp))
                }
                val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()
                val isLoading = playbackState == com.suspended.app.playback.PlaybackState.LOADING
                
                FloatingActionButton(
                    onClick = { viewModel.playPause() },
                    shape = MaterialTheme.shapes.extraLarge,
                    containerColor = SpotifyGreen,
                    contentColor = SpotifyBlack,
                    modifier = Modifier.size(72.dp)
                ) {
                    if (isLoading) {
                        androidx.compose.material3.LoadingIndicator(
                            modifier = Modifier.size(32.dp),
                            color = SpotifyBlack
                        )
                    } else {
                        Icon(
                            imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = "Play/Pause",
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
                IconButton(onClick = { viewModel.skipNext() }) {
                    Icon(Icons.Rounded.SkipNext, contentDescription = "Next", tint = SpotifyWhite, modifier = Modifier.size(36.dp))
                }
                IconButton(onClick = { viewModel.toggleRepeat() }) {
                    Icon(
                        imageVector = if (repeatMode == RepeatMode.ONE) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat,
                        contentDescription = "Repeat",
                        tint = if (repeatMode != RepeatMode.OFF) SpotifyGreen else SpotifyWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Content streamed for personal use only",
                style = MaterialTheme.typography.labelSmall,
                color = SpotifyLightGray.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}
