package com.suspended.app.presentation.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.suspended.app.presentation.components.TrackListItem
import com.suspended.app.presentation.theme.SpotifyBlack
import com.suspended.app.presentation.theme.SpotifyGreen
import com.suspended.app.presentation.theme.SpotifyLightGray
import com.suspended.app.presentation.theme.SpotifyWhite
import com.suspended.app.presentation.components.formatMillis

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: PlaylistViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val playlist = state.playlist

    if (state.isLoading || playlist == null) {
        Box(modifier = Modifier.fillMaxSize().background(SpotifyBlack), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = SpotifyGreen)
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize().background(SpotifyBlack)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    AsyncImage(
                        model = playlist.coverUrl,
                        contentDescription = "Cover",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, SpotifyBlack),
                                    startY = 0f,
                                    endY = Float.POSITIVE_INFINITY
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            text = playlist.name,
                            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                            color = SpotifyWhite
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${playlist.tracks.size} tracks",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SpotifyLightGray
                        )
                    }
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.align(Alignment.TopStart).padding(top = 24.dp)
                    ) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = SpotifyWhite)
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FloatingActionButton(
                        onClick = { viewModel.playAll() },
                        shape = CircleShape,
                        containerColor = SpotifyGreen,
                        contentColor = SpotifyBlack
                    ) {
                        Icon(Icons.Rounded.PlayArrow, contentDescription = "Play All", modifier = Modifier.size(32.dp))
                    }
                }
            }

            itemsIndexed(playlist.tracks) { index, track ->
                TrackListItem(
                    track = track,
                    trackNumber = index + 1,
                    isPlaying = state.currentPlayingTrackId == track.id,
                    isLiked = state.likedTrackIds.contains(track.id),
                    onLikeClick = { viewModel.toggleLike(track) },
                    onClick = { viewModel.playTrack(track) },
                    trailingContent = {
                        Text(
                            text = formatMillis(track.duration * 1000),
                            style = MaterialTheme.typography.bodySmall,
                            color = SpotifyLightGray
                        )
                    }
                )
            }
        }
    }
}
