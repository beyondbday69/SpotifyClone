package com.suspended.app.presentation.search

import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.suspended.app.presentation.components.CategoryCard
import com.suspended.app.presentation.components.TrackListItem
import com.suspended.app.presentation.theme.SpotifyBlack
import com.suspended.app.presentation.theme.SpotifyWhite

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    playerViewModel: com.suspended.app.presentation.player.PlayerViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val currentTrack by playerViewModel.currentTrack.collectAsStateWithLifecycle()
    val playbackState by playerViewModel.playbackState.collectAsStateWithLifecycle()
    val isPlaying by playerViewModel.isPlaying.collectAsStateWithLifecycle()

    var active by remember { mutableStateOf(false) }

    val categories = listOf(
        "Pop" to listOf(Color(0xFFFF4081), Color(0xFFFF80AB)),
        "Hip-Hop" to listOf(Color(0xFFFF9800), Color(0xFFFFB74D)),
        "Rock" to listOf(Color(0xFFE53935), Color(0xFFEF5350)),
        "Electronic" to listOf(Color(0xFF00B0FF), Color(0xFF40C4FF)),
        "R&B" to listOf(Color(0xFF9C27B0), Color(0xFFBA68C8)),
        "Jazz" to listOf(Color(0xFF3F51B5), Color(0xFF7986CB)),
        "Classical" to listOf(Color(0xFF795548), Color(0xFFA1887F)),
        "Country" to listOf(Color(0xFF4CAF50), Color(0xFF81C784)),
        "Indie" to listOf(Color(0xFF009688), Color(0xFF4DB6AC)),
        "Metal" to listOf(Color(0xFF607D8B), Color(0xFF90A4AE)),
        "Latin" to listOf(Color(0xFFFFEB3B), Color(0xFFFFF176)),
        "K-Pop" to listOf(Color(0xFFE91E63), Color(0xFFF06292))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpotifyBlack)
    ) {
        SearchBar(
            query = state.query,
            onQueryChange = viewModel::onQueryChange,
            onSearch = { },
            active = active,
            onActiveChange = { active = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (active) 0.dp else 16.dp),
            placeholder = { Text("What do you want to listen to?") },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            colors = SearchBarDefaults.colors(
                containerColor = if (active) SpotifyBlack else MaterialTheme.colorScheme.surfaceVariant,
                dividerColor = Color.Transparent, // ← remove the line under the search bar
                inputFieldColors = SearchBarDefaults.inputFieldColors(
                    focusedTextColor = SpotifyWhite,
                    unfocusedTextColor = SpotifyWhite
                )
            )
        ) {
            if (state.isSearching) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .fillMaxHeight(0.6f),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator(
                        modifier = Modifier.size(64.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else if (state.query.isNotBlank() && state.results.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No results found", color = SpotifyWhite, style = MaterialTheme.typography.titleMedium)
                }
            } else if (state.query.isNotBlank()) {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    items(state.results, key = { it.id }) { track ->
                        TrackListItem(
                            track = track,
                            isPlaying = currentTrack?.id == track.id && isPlaying,
                            isLoading = currentTrack?.id == track.id && playbackState == com.suspended.app.playback.PlaybackState.LOADING,
                            isLiked = state.likedTrackIds.contains(track.id),
                            onLikeClick = { viewModel.toggleLike(track) },
                            onClick = { viewModel.playTrack(track) },
                            modifier = Modifier.animateItem(
                                fadeInSpec = spring(),
                                placementSpec = spring()
                            )
                        )
                    }
                }
            }
        }

        if (!active) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(categories) { (title, colors) ->
                    CategoryCard(
                        title = title,
                        gradientColors = colors,
                        onClick = { 
                            active = true
                            viewModel.onQueryChange(title)
                        }
                    )
                }
            }
        }
    }
}
