package com.anant.sonqiva.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.anant.sonqiva.data.local.database.PlaylistEntity
import com.anant.sonqiva.data.model.Album
import com.anant.sonqiva.data.model.Artist
import com.anant.sonqiva.data.model.PlaybackState
import com.anant.sonqiva.data.model.Song
import com.anant.sonqiva.ui.components.AlbumCard
import com.anant.sonqiva.ui.components.ArtistItem
import com.anant.sonqiva.ui.components.AtmosphericBackground
import com.anant.sonqiva.ui.components.GlassCard
import com.anant.sonqiva.ui.components.SongRow
import com.anant.sonqiva.ui.theme.GlassBackground
import com.anant.sonqiva.ui.theme.OnSurface
import com.anant.sonqiva.ui.theme.OnSurfaceVariant
import com.anant.sonqiva.ui.theme.PrimaryAccent
import com.anant.sonqiva.ui.theme.SurfaceContainerHigh
import com.anant.sonqiva.ui.theme.SurfaceDark

@Composable
fun LibraryScreen(
    songs: List<Song>,
    albums: List<Album>,
    artists: List<Artist>,
    playlists: List<PlaylistEntity>,
    playbackState: PlaybackState,
    onSongClick: (Song) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onArtistClick: (Artist) -> Unit,
    onPlaylistClick: (PlaylistEntity) -> Unit,
    onCreatePlaylistClick: () -> Unit,
    onPlayAllClick: () -> Unit,
    onShuffleAllClick: () -> Unit,
    onFavoriteToggle: (Song) -> Unit,
    onSongMoreClick: ((Song) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        "Songs (${songs.size})",
        "Albums (${albums.size})",
        "Artists (${artists.size})",
        "Playlists (${playlists.size})"
    )

    AtmosphericBackground(modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Screen Title
            Text(
                text = "Your Library",
                style = MaterialTheme.typography.displayMedium,
                color = OnSurface,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 8.dp)
            )

            // Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = SurfaceDark.copy(alpha = 0.6f),
                contentColor = PrimaryAccent,
                edgePadding = 20.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = PrimaryAccent
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (selectedTabIndex == index) PrimaryAccent else OnSurfaceVariant
                            )
                        }
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> {
                    // Songs Tab
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 12.dp, bottom = 120.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                GlassCard(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable(onClick = onPlayAllClick),
                                    shape = RoundedCornerShape(12.dp),
                                    backgroundColor = PrimaryAccent.copy(alpha = 0.15f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = PrimaryAccent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Play All",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = PrimaryAccent
                                        )
                                    }
                                }

                                GlassCard(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable(onClick = onShuffleAllClick),
                                    shape = RoundedCornerShape(12.dp),
                                    backgroundColor = GlassBackground
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Shuffle,
                                            contentDescription = null,
                                            tint = OnSurface,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Shuffle",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = OnSurface
                                        )
                                    }
                                }
                            }
                        }

                        items(songs, key = { it.id }) { song ->
                            SongRow(
                                song = song,
                                isPlaying = playbackState.isPlaying && playbackState.currentSong?.id == song.id,
                                isCurrentSong = playbackState.currentSong?.id == song.id,
                                onClick = { onSongClick(song) },
                                onFavoriteToggle = { onFavoriteToggle(song) },
                                onMoreClick = { onSongMoreClick?.invoke(song) },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                1 -> {
                    // Albums Grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(albums, key = { it.id }) { album ->
                            AlbumCard(
                                album = album,
                                onClick = { onAlbumClick(album) }
                            )
                        }
                    }
                }

                2 -> {
                    // Artists Grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(artists, key = { it.id }) { artist ->
                            ArtistItem(
                                artist = artist,
                                onClick = { onArtistClick(artist) }
                            )
                        }
                    }
                }

                3 -> {
                    // Playlists Tab
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        // Create Playlist Action Card
                        item {
                            GlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(onClick = onCreatePlaylistClick)
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(14.dp),
                                backgroundColor = PrimaryAccent.copy(alpha = 0.12f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "New Playlist",
                                        tint = PrimaryAccent,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        text = "Create New Playlist",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = PrimaryAccent
                                    )
                                }
                            }
                        }

                        items(playlists, key = { it.id }) { playlist ->
                            GlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onPlaylistClick(playlist) }
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(14.dp),
                                backgroundColor = GlassBackground
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(SurfaceContainerHigh),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                                            contentDescription = null,
                                            tint = PrimaryAccent,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = playlist.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = OnSurface
                                        )
                                        Text(
                                            text = "Custom Playlist",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = OnSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
