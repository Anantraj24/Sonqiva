package com.anant.sonqiva.ui.playlists

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anant.sonqiva.data.local.database.PlaylistEntity
import com.anant.sonqiva.data.model.PlaybackState
import com.anant.sonqiva.data.model.Song
import com.anant.sonqiva.ui.components.AtmosphericBackground
import com.anant.sonqiva.ui.components.GlassCard
import com.anant.sonqiva.ui.components.SongRow
import com.anant.sonqiva.ui.theme.ErrorColor
import com.anant.sonqiva.ui.theme.GlassBackground
import com.anant.sonqiva.ui.theme.OnSurface
import com.anant.sonqiva.ui.theme.OnSurfaceVariant
import com.anant.sonqiva.ui.theme.PrimaryAccent
import com.anant.sonqiva.ui.theme.SurfaceContainerHigh

@Composable
fun PlaylistDetailScreen(
    playlist: PlaylistEntity,
    songs: List<Song>,
    playbackState: PlaybackState,
    onBackClick: () -> Unit,
    onDeletePlaylistClick: () -> Unit,
    onSongClick: (Song) -> Unit,
    onPlayAllClick: () -> Unit,
    onShuffleAllClick: () -> Unit,
    onFavoriteToggle: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    AtmosphericBackground(modifier = modifier) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            // Top Bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = OnSurface
                        )
                    }
                    Text(
                        text = "Playlist",
                        style = MaterialTheme.typography.titleMedium,
                        color = OnSurface,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDeletePlaylistClick) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Playlist",
                            tint = ErrorColor
                        )
                    }
                }
            }

            // Hero Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                            contentDescription = null,
                            tint = PrimaryAccent,
                            modifier = Modifier.size(60.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = playlist.name,
                        style = MaterialTheme.typography.headlineLarge,
                        color = OnSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${songs.size} songs",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (songs.isNotEmpty()) {
                        // Play / Shuffle Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            GlassCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable(onClick = onPlayAllClick),
                                shape = RoundedCornerShape(14.dp),
                                backgroundColor = PrimaryAccent.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = PrimaryAccent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
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
                                shape = RoundedCornerShape(14.dp),
                                backgroundColor = GlassBackground
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shuffle,
                                        contentDescription = null,
                                        tint = OnSurface,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Shuffle",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = OnSurface
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            if (songs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "This playlist is empty.\nAdd tracks using the song options menu!",
                            style = MaterialTheme.typography.bodyLarge,
                            color = OnSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                items(songs, key = { it.id }) { song ->
                    SongRow(
                        song = song,
                        isPlaying = playbackState.isPlaying && playbackState.currentSong?.id == song.id,
                        isCurrentSong = playbackState.currentSong?.id == song.id,
                        onClick = { onSongClick(song) },
                        onFavoriteToggle = { onFavoriteToggle(song) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
