package com.suspended.app.presentation.player

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.suspended.app.playback.RepeatMode
import com.suspended.app.presentation.components.formatMillis


// Dark theme colors for Now Playing - bouncy motion physics
private val NowPlayingBackground = Color(0xFF0D0D0D)
private val NowPlayingSurface = Color(0xFF181818)
private val NowPlayingAccentGreen = Color(0xFF1DB954)
private val NowPlayingPlayButton = Color(0xFF1DB954)
private val NowPlayingTextPrimary = Color(0xFFFFFFFF)
private val NowPlayingTextSecondary = Color(0xFFB3B3B3)
private val NowPlayingTrackBackground = Color(0xFF282828)
private val NowPlayingTrackActive = Color(0xFF1DB954)

// Bouncy spring animation specs for smooth, lively transitions
private val bouncySpring = spring<Float>(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessLow
)

private val smoothSpring = spring<Float>(
    dampingRatio = Spring.DampingRatioLowBouncy,
    stiffness = Spring.StiffnessMedium
)

private val snappySpring = spring<Float>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = Spring.StiffnessHigh
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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
    val repeatMode by viewModel.repeatMode.collectAsStateWithLifecycle()
    val volume by viewModel.volume.collectAsStateWithLifecycle()
    val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()
    val isLoading = playbackState == com.suspended.app.playback.PlaybackState.LOADING

    if (track == null) {
        Box(modifier = Modifier.fillMaxSize().background(NowPlayingBackground))
        return
    }

    // Bouncy album scale animation - pulses with music
    val albumScale by animateFloatAsState(
        targetValue = if (isPlaying) 1.0f else 0.92f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    // Play button bouncy scale
    val playButtonScale by animateFloatAsState(
        targetValue = if (isPlaying) 1.0f else 0.9f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )

    // Album rotation for fun bouncy effect
    val albumRotation by animateFloatAsState(
        targetValue = if (isPlaying) 0f else -2f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessVeryLow
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NowPlayingBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .systemBarsPadding()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = NowPlayingTextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Now playing",
                    style = MaterialTheme.typography.headlineSmall,
                    color = NowPlayingTextPrimary
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Album cover with bouncy animation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .graphicsLayer {
                        scaleX = albumScale
                        scaleY = albumScale
                        rotationZ = albumRotation
                    }
                    .clip(RoundedCornerShape(28.dp))
                    .background(NowPlayingSurface)
            ) {
                AsyncImage(
                    model = track?.thumbnailUrl?.replace("hqdefault.jpg", "maxresdefault.jpg")?.replace(Regex("=w\\d+-h\\d+.*"), "=w1080-h1080"),
                    contentDescription = "Cover",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = track?.title ?: "",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Normal),
                color = NowPlayingTextPrimary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth().basicMarquee()
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = track?.artist ?: "",
                style = MaterialTheme.typography.bodyLarge,
                color = NowPlayingTextSecondary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            NowPlayingProgressSlider(
                progress = progress,
                currentPosition = currentPosition,
                duration = duration,
                onSeek = { viewModel.seekTo(it) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.toggleShuffle() }) {
                    Icon(
                        Icons.Rounded.Shuffle,
                        contentDescription = "Shuffle",
                        tint = NowPlayingAccentGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }
                IconButton(onClick = { viewModel.skipPrevious() }) {
                    Icon(
                        Icons.Rounded.SkipPrevious,
                        contentDescription = "Previous",
                        tint = NowPlayingTextPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Bouncy play button
                Surface(
                    onClick = { viewModel.playPause() },
                    shape = CircleShape,
                    color = NowPlayingPlayButton,
                    contentColor = Color.Black,
                    modifier = Modifier
                        .size(76.dp)
                        .graphicsLayer {
                            scaleX = playButtonScale
                            scaleY = playButtonScale
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isLoading) {
                            LoadingIndicator(
                                modifier = Modifier.size(28.dp),
                                color = Color.Black
                            )
                        } else {
                            Icon(
                                imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                contentDescription = "Play/Pause",
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }
                }

                IconButton(onClick = { viewModel.skipNext() }) {
                    Icon(
                        Icons.Rounded.SkipNext,
                        contentDescription = "Next",
                        tint = NowPlayingTextPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }
                IconButton(onClick = { viewModel.toggleRepeat() }) {
                    Icon(
                        imageVector = if (repeatMode == RepeatMode.ONE) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat,
                        contentDescription = "Repeat",
                        tint = NowPlayingAccentGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            NowPlayingVolumeBar(
                volume = volume,
                onVolumeChange = { viewModel.setVolume(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NowPlayingProgressSlider(
    progress: Float,
    currentPosition: Long,
    duration: Long,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableStateOf(0f) }

    val displayProgress = if (isDragging) dragProgress else progress

    // Bouncy thumb scale when dragging
    val thumbScale by animateFloatAsState(
        targetValue = if (isDragging) 1.3f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        )
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Slider(
            value = displayProgress,
            onValueChange = {
                isDragging = true
                dragProgress = it
            },
            onValueChangeFinished = {
                isDragging = false
                onSeek(dragProgress)
            },
            colors = SliderDefaults.colors(
                thumbColor = NowPlayingAccentGreen,
                activeTrackColor = NowPlayingAccentGreen,
                inactiveTrackColor = NowPlayingTrackBackground
            ),
            track = { sliderState ->
                SliderDefaults.Track(
                    sliderState = sliderState,
                    colors = SliderDefaults.colors(
                        activeTrackColor = NowPlayingAccentGreen,
                        inactiveTrackColor = NowPlayingTrackBackground
                    ),
                    modifier = Modifier.height(8.dp) // Thick slider track
                )
            },
            thumb = {
                // Custom bouncy thumb
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .graphicsLayer {
                            scaleX = thumbScale
                            scaleY = thumbScale
                        }
                        .clip(CircleShape)
                        .background(NowPlayingAccentGreen)
                )
            },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val displayPos = if (isDragging) (dragProgress * duration).toLong() else currentPosition
            Text(
                text = formatMillis(displayPos),
                style = MaterialTheme.typography.labelSmall,
                color = NowPlayingTextSecondary
            )
            Text(
                text = formatMillis(duration),
                style = MaterialTheme.typography.labelSmall,
                color = NowPlayingTextSecondary
            )
        }
    }
}

@Composable
private fun NowPlayingVolumeBar(
    volume: Float,
    onVolumeChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var barWidthPx by remember { mutableStateOf(0) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(50))
            .background(NowPlayingTrackBackground)
            .onSizeChanged { barWidthPx = it.width }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    if (barWidthPx > 0) {
                        onVolumeChange((offset.x / barWidthPx).coerceIn(0f, 1f))
                    }
                }
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, _ ->
                    if (barWidthPx > 0) {
                        onVolumeChange((change.position.x / barWidthPx).coerceIn(0f, 1f))
                    }
                }
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction = volume.coerceIn(0.14f, 1f))
                .clip(RoundedCornerShape(50))
                .background(NowPlayingAccentGreen),
            contentAlignment = Alignment.CenterEnd
        ) {
            Icon(
                imageVector = Icons.Rounded.VolumeUp,
                contentDescription = "Volume",
                tint = Color.Black,
                modifier = Modifier.padding(end = 20.dp)
            )
        }
    }
}
