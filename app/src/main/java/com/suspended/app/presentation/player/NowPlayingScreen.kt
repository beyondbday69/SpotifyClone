package com.suspended.app.presentation.player

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.palette.graphics.Palette
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import com.suspended.app.playback.RepeatMode
import com.suspended.app.presentation.components.ExpressiveSlider
import com.suspended.app.presentation.theme.SpotifyBlack
import com.suspended.app.presentation.theme.SpotifyGreen
import com.suspended.app.presentation.theme.SpotifyLightGray
import com.suspended.app.presentation.theme.SpotifyWhite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    val context = LocalContext.current
    var dominantColor by remember { mutableStateOf(Color(0xFF3B3B3B)) }

    LaunchedEffect(track?.thumbnailUrl) {
        track?.thumbnailUrl?.let { url ->
            withContext(Dispatchers.IO) {
                try {
                    val loader = ImageLoader(context)
                    val request = ImageRequest.Builder(context)
                        .data(url)
                        .allowHardware(false)
                        .build()
                    val result = loader.execute(request)
                    if (result is SuccessResult) {
                        val bitmap = (result.image as? coil3.BitmapImage)?.bitmap
                        if (bitmap != null) {
                            Palette.from(bitmap).generate().dominantSwatch?.rgb?.let { rgb ->
                                dominantColor = Color(rgb)
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Ignore palette extraction errors
                }
            }
        }
    }

    val sharedTransitionScope = com.suspended.app.presentation.LocalSharedTransitionScope.current
    val animatedVisibilityScope = com.suspended.app.presentation.LocalNavAnimatedVisibilityScope.current

    val albumScale by animateFloatAsState(
        targetValue = if (isPlaying) 1.0f else 0.94f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f)
    )

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
                    colors = listOf(dominantColor, SpotifyBlack),
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .systemBarsPadding()
        ) {
            // Drag affordance handle
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(48.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(SpotifyWhite.copy(alpha = 0.4f))
                    .align(Alignment.CenterHorizontally)
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
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

            Box(
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
                    },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = track?.thumbnailUrl?.replace("hqdefault.jpg", "maxresdefault.jpg")?.replace(Regex("=w\\d+-h\\d+.*"), "=w1080-h1080"),
                    contentDescription = "Cover",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .androidx.compose.ui.graphics.graphicsLayer {
                            scaleX = albumScale
                            scaleY = albumScale
                        }
                        .shadow(16.dp, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                    Text(
                        text = track?.title ?: "",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        color = SpotifyWhite,
                        maxLines = 1,
                        modifier = Modifier.basicMarquee()
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
                
                // Previous button with pressed state background
                val prevInteraction = remember { MutableInteractionSource() }
                val prevPressed by prevInteraction.collectIsPressedAsState()
                IconButton(
                    onClick = { viewModel.skipPrevious() },
                    interactionSource = prevInteraction,
                    modifier = Modifier.background(
                        if (prevPressed) SpotifyWhite.copy(alpha = 0.2f) else Color.Transparent,
                        CircleShape
                    )
                ) {
                    Icon(Icons.Rounded.SkipPrevious, contentDescription = "Previous", tint = SpotifyWhite, modifier = Modifier.size(36.dp))
                }
                
                val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()
                val isLoading = playbackState == com.suspended.app.playback.PlaybackState.LOADING
                
                // Bouncing Play/Pause button
                val playInteraction = remember { MutableInteractionSource() }
                val playPressed by playInteraction.collectIsPressedAsState()
                val playScale by animateFloatAsState(
                    targetValue = if (playPressed) 0.85f else 1f,
                    animationSpec = spring(dampingRatio = 0.5f, stiffness = 500f)
                )
                
                Surface(
                    onClick = { viewModel.playPause() },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    interactionSource = playInteraction,
                    modifier = Modifier.size(84.dp).scale(playScale)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isLoading) {
                            androidx.compose.material3.LoadingIndicator(
                                modifier = Modifier.size(32.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(
                                imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                contentDescription = "Play/Pause",
                                modifier = Modifier.size(42.dp)
                            )
                        }
                    }
                }
                
                // Next button with pressed state background
                val nextInteraction = remember { MutableInteractionSource() }
                val nextPressed by nextInteraction.collectIsPressedAsState()
                IconButton(
                    onClick = { viewModel.skipNext() },
                    interactionSource = nextInteraction,
                    modifier = Modifier.background(
                        if (nextPressed) SpotifyWhite.copy(alpha = 0.2f) else Color.Transparent,
                        CircleShape
                    )
                ) {
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
