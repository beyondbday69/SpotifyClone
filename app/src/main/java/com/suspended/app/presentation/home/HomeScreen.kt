package com.suspended.app.presentation.home

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.suspended.app.presentation.components.ShelfItem
import com.suspended.app.presentation.components.ShelfSection
import com.suspended.app.presentation.theme.SpotifyBlack
import com.suspended.app.presentation.theme.SpotifyWhite
import coil3.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun HomeScreen(
    onNavigateToPlaylist: (Long) -> Unit,
    onNavigateToNowPlaying: () -> Unit,
    onNavigateToFeatured: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF2B2B2B), SpotifyBlack),
                    startY = 0f,
                    endY = 1000f
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = state.greeting,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = SpotifyWhite
                    )
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Rounded.Settings, contentDescription = "Settings", tint = SpotifyWhite)
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = true,
                        onClick = { },
                        label = { Text("Music") },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = SpotifyWhite,
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = SpotifyBlack
                        ),
                        shape = CircleShape
                    )
                    FilterChip(
                        selected = false,
                        onClick = onNavigateToFeatured,
                        label = { Text("Featured") },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = SpotifyWhite
                        ),
                        shape = CircleShape
                    )
                }
            }

            if (state.recentlyPlayed.isNotEmpty()) {
                item {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp)
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        userScrollEnabled = false
                    ) {
                        items(state.recentlyPlayed.take(6), key = { it.id }) { track ->
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(4.dp),
                                onClick = { viewModel.playTrack(track) },
                                modifier = Modifier.height(56.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    AsyncImage(
                                        model = track.thumbnailUrl,
                                        contentDescription = null,
                                        modifier = Modifier.size(56.dp)
                                    )
                                    Text(
                                        text = track.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = SpotifyWhite,
                                        maxLines = 2,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    ShelfSection(
                        title = "Recently Played",
                        items = state.recentlyPlayed,
                        key = { it.id }
                    ) { track ->
                        ShelfItem(
                            title = track.title,
                            subtitle = track.artist,
                            imageUrl = track.thumbnailUrl,
                            onClick = { viewModel.playTrack(track) }
                        )
                    }
                }
            }

            if (state.libraryTracks.isNotEmpty()) {
                item {
                    ShelfSection(
                        title = "Made for You",
                        items = state.libraryTracks.take(10),
                        key = { it.id }
                    ) { track ->
                        ShelfItem(
                            title = track.title,
                            subtitle = track.artist,
                            imageUrl = track.thumbnailUrl,
                            onClick = { viewModel.playTrack(track) }
                        )
                    }
                }
            }

            if (state.playlists.isNotEmpty()) {
                item {
                    ShelfSection(
                        title = "Your Playlists",
                        items = state.playlists,
                        key = { it.id }
                    ) { playlist ->
                        ShelfItem(
                            title = playlist.name,
                            subtitle = "Playlist",
                            imageUrl = playlist.coverUrl,
                            onClick = { onNavigateToPlaylist(playlist.id) }
                        )
                    }
                }
            }
        }
    }
}