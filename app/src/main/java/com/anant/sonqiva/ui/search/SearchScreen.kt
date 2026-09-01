package com.anant.sonqiva.ui.search

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
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
import com.anant.sonqiva.ui.theme.OnPrimary
import com.anant.sonqiva.ui.theme.OnSurface
import com.anant.sonqiva.ui.theme.OnSurfaceVariant
import com.anant.sonqiva.ui.theme.PrimaryAccent
import com.anant.sonqiva.ui.theme.PrimaryGradient

@Composable
fun SearchScreen(
    allSongs: List<Song>,
    allAlbums: List<Album> = emptyList(),
    allArtists: List<Artist> = emptyList(),
    playbackState: PlaybackState,
    onSongClick: (Song) -> Unit,
    onAlbumClick: ((Album) -> Unit)? = null,
    onArtistClick: ((Artist) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    var selectedFilterIndex by remember { mutableIntStateOf(0) }
    val filterTabs = listOf("All", "Songs", "Artists", "Albums")

    val matchingSongs = remember(query, allSongs) {
        if (query.isBlank()) emptyList()
        else allSongs.filter {
            it.title.contains(query, ignoreCase = true) ||
            it.artist.contains(query, ignoreCase = true) ||
            it.album.contains(query, ignoreCase = true)
        }
    }

    val matchingArtists = remember(query, allArtists) {
        if (query.isBlank()) emptyList()
        else allArtists.filter { it.name.contains(query, ignoreCase = true) }
    }

    val matchingAlbums = remember(query, allAlbums) {
        if (query.isBlank()) emptyList()
        else allAlbums.filter {
            it.title.contains(query, ignoreCase = true) ||
            it.artist.contains(query, ignoreCase = true)
        }
    }

    AtmosphericBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            Text(
                text = "Search",
                style = MaterialTheme.typography.displayMedium,
                color = OnSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Glassmorphic Search Bar
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                backgroundColor = GlassBackground.copy(alpha = 0.12f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = PrimaryAccent,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))

                    Box(modifier = Modifier.weight(1f)) {
                        if (query.isEmpty()) {
                            Text(
                                text = "Search tracks, artists, albums...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = OnSurfaceVariant
                            )
                        }
                        BasicTextField(
                            value = query,
                            onValueChange = { query = it },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = OnSurface),
                            cursorBrush = SolidColor(PrimaryAccent),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    if (query.isNotEmpty()) {
                        IconButton(
                            onClick = { query = "" },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = OnSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips Row
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filterTabs.size) { idx ->
                    val isSelected = selectedFilterIndex == idx
                    GlassCard(
                        modifier = Modifier.clickable { selectedFilterIndex = idx },
                        shape = RoundedCornerShape(16.dp),
                        backgroundColor = if (isSelected) PrimaryAccent.copy(alpha = 0.2f) else GlassBackground
                    ) {
                        Text(
                            text = filterTabs[idx],
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isSelected) PrimaryAccent else OnSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (query.isBlank()) {
                // Empty search greeting / quick tips
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(PrimaryAccent.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = PrimaryAccent,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Find Any Music Instantly",
                            style = MaterialTheme.typography.titleMedium,
                            color = OnSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Search across all offline songs, artists, and albums on your device.",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                val totalMatches = matchingSongs.size + matchingArtists.size + matchingAlbums.size
                if (totalMatches == 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No results found for \"$query\"",
                            style = MaterialTheme.typography.bodyLarge,
                            color = OnSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 120.dp)
                    ) {
                        // Matching Artists
                        if ((selectedFilterIndex == 0 || selectedFilterIndex == 2) && matchingArtists.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Artists (${matchingArtists.size})",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = PrimaryAccent,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }

                            item {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(bottom = 12.dp)
                                ) {
                                    items(matchingArtists, key = { it.id }) { artist ->
                                        ArtistItem(
                                            artist = artist,
                                            onClick = { onArtistClick?.invoke(artist) }
                                        )
                                    }
                                }
                            }
                        }

                        // Matching Albums
                        if ((selectedFilterIndex == 0 || selectedFilterIndex == 3) && matchingAlbums.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Albums (${matchingAlbums.size})",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = PrimaryAccent,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }

                            item {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.padding(bottom = 12.dp)
                                ) {
                                    items(matchingAlbums, key = { it.id }) { album ->
                                        AlbumCard(
                                            album = album,
                                            onClick = { onAlbumClick?.invoke(album) }
                                        )
                                    }
                                }
                            }
                        }

                        // Matching Songs
                        if ((selectedFilterIndex == 0 || selectedFilterIndex == 1) && matchingSongs.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Songs (${matchingSongs.size})",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = PrimaryAccent,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }

                            items(matchingSongs, key = { it.id }) { song ->
                                SongRow(
                                    song = song,
                                    isPlaying = playbackState.isPlaying && playbackState.currentSong?.id == song.id,
                                    isCurrentSong = playbackState.currentSong?.id == song.id,
                                    onClick = { onSongClick(song) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
