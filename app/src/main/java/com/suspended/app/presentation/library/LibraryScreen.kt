package com.suspended.app.presentation.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.suspended.app.presentation.components.TrackListItem
import com.suspended.app.presentation.theme.SpotifyBlack
import com.suspended.app.presentation.theme.SpotifyWhite
import com.suspended.app.presentation.components.ShelfItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onNavigateToPlaylist: (Long) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel(),
    themeViewModel: com.suspended.app.presentation.theme.ThemeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showThemeSheet by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpotifyBlack)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Your Library",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = SpotifyWhite
            )
            Row {
                IconButton(onClick = { showThemeSheet = true }) {
                    Icon(Icons.Rounded.Settings, contentDescription = "Settings", tint = SpotifyWhite)
                }
                IconButton(onClick = { viewModel.showCreatePlaylistDialog(true) }) {
                    Icon(Icons.Rounded.Add, contentDescription = "Add Playlist", tint = SpotifyWhite)
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            val filters = listOf(
                LibraryFilter.PLAYLISTS,
                LibraryFilter.ARTISTS,
                LibraryFilter.DOWNLOADED,
                LibraryFilter.LIKED
            )
            SingleChoiceSegmentedButtonRow {
                filters.forEachIndexed { index, filter ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = filters.size
                        ),
                        selected = state.selectedFilter == filter,
                        onClick = { viewModel.setFilter(filter) },
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = MaterialTheme.colorScheme.primary,
                            activeContentColor = SpotifyBlack,
                            activeBorderColor = MaterialTheme.colorScheme.primary,
                            inactiveContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            inactiveContentColor = SpotifyWhite,
                            inactiveBorderColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        label = { Text(filter.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }
        }


        LazyColumn(
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            when (state.selectedFilter) {
                LibraryFilter.PLAYLISTS -> {
                    items(state.playlists, key = { it.id }) { playlist ->
                        TrackListItem(
                            track = com.suspended.app.domain.model.Track(
                                id = playlist.id.toString(),
                                title = playlist.name,
                                artist = "${playlist.tracks.size} tracks",
                                thumbnailUrl = playlist.coverUrl
                            ),
                            onClick = { onNavigateToPlaylist(playlist.id) }
                        )
                    }
                }
                LibraryFilter.TRACKS -> {
                    items(state.tracks, key = { it.id }) { track ->
                        TrackListItem(
                            track = track,
                            isLiked = state.likedTrackIds.contains(track.id),
                            onLikeClick = { viewModel.toggleLike(track) },
                            onClick = { viewModel.playTrack(track) }
                        )
                    }
                }
                LibraryFilter.ARTISTS -> {
                    items(state.artists, key = { it.id }) { artist ->
                        TrackListItem(
                            track = com.suspended.app.domain.model.Track(
                                id = artist.id,
                                title = artist.name,
                                artist = "${artist.trackCount} tracks",
                                thumbnailUrl = artist.thumbnailUrl
                            ),
                            onClick = { }
                        )
                    }
                }
                LibraryFilter.DOWNLOADED -> {
                    items(state.downloadedTracks, key = { it.id }) { track ->
                        TrackListItem(
                            track = track,
                            isLiked = state.likedTrackIds.contains(track.id),
                            onLikeClick = { viewModel.toggleLike(track) },
                            onClick = { viewModel.playTrack(track) }
                        )
                    }
                }
                LibraryFilter.LIKED -> {
                    if (state.likedSongs.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Songs you like will appear here.\nTap the heart on any track to save it.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    } else {
                        items(state.likedSongs, key = { it.id }) { track ->
                            TrackListItem(
                                track = track,
                                isLiked = true,
                                onLikeClick = { viewModel.toggleLike(track) },
                                onClick = { viewModel.playTrack(track) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (state.showCreatePlaylistDialog) {
        var playlistName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { viewModel.showCreatePlaylistDialog(false) },
            title = { Text("New Playlist") },
            text = {
                OutlinedTextField(
                    value = playlistName,
                    onValueChange = { playlistName = it },
                    label = { Text("Playlist Name") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (playlistName.isNotBlank()) {
                        viewModel.createPlaylist(playlistName)
                    }
                }) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showCreatePlaylistDialog(false) }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showThemeSheet) {
        val currentTheme by themeViewModel.appTheme.collectAsStateWithLifecycle()
        ModalBottomSheet(
            onDismissRequest = { showThemeSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "App Theme",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    com.suspended.app.presentation.theme.AppTheme.values().forEach { themeOption ->
                        FilterChip(
                            selected = currentTheme == themeOption,
                            onClick = { themeViewModel.setTheme(themeOption) },
                            label = { 
                                Text(
                                    text = themeOption.name.lowercase().replace("_", " ").replaceFirstChar { it.uppercase() }
                                ) 
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = CircleShape
                        )
                    }
                }
            }
        }
    }
}
