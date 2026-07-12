package com.suspended.app.presentation.featured

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.suspended.app.presentation.theme.SpotifyBlack
import com.suspended.app.presentation.theme.SpotifyWhite

data class FeaturedPlaylist(
    val id: Long,
    val name: String,
    val coverUrl: String,
    val description: String
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun FeaturedScreen(
    onNavigateBack: () -> Unit,
    onPlaylistClick: (Long) -> Unit
) {
    var expandedPlaylist by remember { mutableStateOf<FeaturedPlaylist?>(null) }

    val samplePlaylists = listOf(
        FeaturedPlaylist(1, "Discover Weekly", "https://i.scdn.co/image/ab67706f000000027ea4d505212b9de1f72c5112", "Your weekly mixtape of fresh music"),
        FeaturedPlaylist(2, "Release Radar", "https://i.scdn.co/image/ab67706f000000027ea4d505212b9de1f72c5112", "Catch all the latest music from artists you follow"),
        FeaturedPlaylist(3, "Daily Mix 1", "https://i.scdn.co/image/ab67706f000000027ea4d505212b9de1f72c5112", "Radiohead, Tame Impala, The Strokes and more"),
        FeaturedPlaylist(4, "Daily Mix 2", "https://i.scdn.co/image/ab67706f000000027ea4d505212b9de1f72c5112", "Arctic Monkeys, The Black Keys, Cage the Elephant"),
        FeaturedPlaylist(5, "Chill Hits", "https://i.scdn.co/image/ab67706f000000027ea4d505212b9de1f72c5112", "Kick back to the best new and recent chill hits"),
        FeaturedPlaylist(6, "Deep Focus", "https://i.scdn.co/image/ab67706f000000027ea4d505212b9de1f72c5112", "Keep calm and focus with ambient and post-rock music")
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpotifyBlack)
    ) {
        AnimatedContent(
            targetState = expandedPlaylist,
            transitionSpec = {
                if (targetState != null) {
                    // Container transform: scale up + fade in
                    (scaleIn(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ) + fadeIn()).togetherWith(
                        slideOutVertically(
                            targetOffsetY = { -it / 3 },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        ) + fadeOut()
                    )
                } else {
                    // Reverse transform: scale down + fade out
                    (slideInVertically(
                        initialOffsetY = { -it / 3 },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    ) + fadeIn()).togetherWith(
                        scaleOut(
                            targetScale = 0.85f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        ) + fadeOut()
                    )
                }
            }
        ) { playlist ->
            if (playlist != null) {
                // Expanded playlist detail view
                PlaylistDetailView(
                    playlist = playlist,
                    onBack = { expandedPlaylist = null },
                    onPlay = { onPlaylistClick(playlist.id) }
                )
            } else {
                // Grid view
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Top app bar
                    TopAppBar(
                        title = {
                            Text(
                                "Featured Playlists",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
                                Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = SpotifyWhite,
                            navigationIconContentColor = SpotifyWhite
                        )
                    )

                    // Playlist grid with container transform
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        items(samplePlaylists, key = { it.id }) { playlist ->
                            PlaylistGridItem(
                                playlist = playlist,
                                onClick = { expandedPlaylist = playlist }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaylistGridItem(
    playlist: FeaturedPlaylist,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column {
            AsyncImage(
                model = playlist.coverUrl,
                contentDescription = playlist.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = SpotifyWhite,
                    maxLines = 1
                )
                Text(
                    text = playlist.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun PlaylistDetailView(
    playlist: FeaturedPlaylist,
    onBack: () -> Unit,
    onPlay: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        SpotifyBlack
                    )
                )
            )
    ) {
        // Header with back button
        TopAppBar(
            title = { Text(playlist.name) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = SpotifyWhite,
                navigationIconContentColor = SpotifyWhite
            )
        )

        // Large playlist image
        AsyncImage(
            model = playlist.coverUrl,
            contentDescription = playlist.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Playlist info
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = SpotifyWhite
            )
            Text(
                text = playlist.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Play button
        Button(
            onClick = onPlay,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "Play",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}