package com.suspended.app.presentation.components

import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.suspended.app.domain.model.Track
import com.suspended.app.presentation.theme.SpotifyWhite
import kotlinx.coroutines.launch
import kotlin.math.abs

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.animation.ExperimentalSharedTransitionApi

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun MiniPlayer(
    track: Track?,
    isPlaying: Boolean,
    isLoading: Boolean = false,
    progress: Float,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onDismiss: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Bouncy spring animation for smooth slide-in/out
    val slideSpec = spring<IntOffset>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )

    AnimatedVisibility(
        visible = track != null,
        enter = slideInVertically(slideSpec) { it },
        exit = slideOutVertically(slideSpec) { it }
    ) {
        if (track != null) {

            // Drag offset as an Animatable with bouncy spring back
            val offsetX = remember { Animatable(0f) }
            val offsetY = remember { Animatable(0f) }
            val coroutineScope = rememberCoroutineScope()
            
            // Bouncy spring for snap back
            val snapBackSpec = spring<Float>(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMedium
            )

            Surface(
                modifier = modifier
                    .fillMaxWidth(0.96f) // Slightly inset to float
                    .height(64.dp)
                    .offset { IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt()) }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragEnd = {
                                val endX = offsetX.value
                                val endY = offsetY.value
                                when {
                                    endY > 100f -> onDismiss()
                                    endX > 150f -> onSkipPrevious()
                                    endX < -150f -> onSkipNext()
                                }
                                // Bouncy spring back to rest
                                coroutineScope.launch { offsetX.animateTo(0f, snapBackSpec) }
                                coroutineScope.launch { offsetY.animateTo(0f, snapBackSpec) }
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                coroutineScope.launch {
                                    if (abs(dragAmount.x) > abs(dragAmount.y)) {
                                        offsetX.snapTo(offsetX.value + dragAmount.x * 0.5f) // Damped translation
                                    } else if (dragAmount.y > 0) { // Only allow swiping down
                                        offsetY.snapTo(offsetY.value + dragAmount.y * 0.5f)
                                    }
                                }
                            }
                        )
                    },
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 4.dp,
                shadowElevation = 12.dp,
                onClick = onClick
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                            strokeWidth = 3.dp // Thicker progress ring
                        )
                        AsyncImage(
                            model = track.thumbnailUrl,
                            contentDescription = "Cover",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                        )
                    }
                    
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp)
                    ) {
                        Text(
                            text = track.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = SpotifyWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = track.artist,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    if (isLoading) {
                        androidx.compose.material3.LoadingIndicator(
                            modifier = Modifier.size(28.dp).padding(4.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        IconButton(onClick = onPlayPause) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                contentDescription = "Play/Pause",
                                tint = SpotifyWhite,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    
                    IconButton(onClick = onSkipNext) {
                        Icon(
                            imageVector = Icons.Rounded.SkipNext,
                            contentDescription = "Skip Next",
                            tint = SpotifyWhite,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}
