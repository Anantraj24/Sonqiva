package com.anant.sonqiva.ui.folders

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anant.sonqiva.data.model.FolderItem
import com.anant.sonqiva.data.model.PlaybackState
import com.anant.sonqiva.data.model.Song
import com.anant.sonqiva.ui.components.AtmosphericBackground
import com.anant.sonqiva.ui.components.FolderItemRow
import com.anant.sonqiva.ui.components.GlassCard
import com.anant.sonqiva.ui.components.SongRow
import com.anant.sonqiva.ui.theme.GlassBackground
import com.anant.sonqiva.ui.theme.OnSurface
import com.anant.sonqiva.ui.theme.OnSurfaceVariant
import com.anant.sonqiva.ui.theme.PrimaryAccent

@Composable
fun FoldersScreen(
    folders: List<FolderItem>,
    currentFolder: FolderItem?,
    playbackState: PlaybackState,
    onFolderClick: (FolderItem) -> Unit,
    onBackClick: () -> Unit,
    onSongClick: (Song) -> Unit,
    onPlayFolderClick: (FolderItem) -> Unit,
    onShuffleFolderClick: (FolderItem) -> Unit,
    modifier: Modifier = Modifier
) {
    AtmosphericBackground(modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header with Back if inside subfolder
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 20.dp, top = 20.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentFolder != null) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = OnSurface
                        )
                    }
                }

                Text(
                    text = currentFolder?.name ?: "Folders",
                    style = MaterialTheme.typography.displayMedium,
                    color = OnSurface,
                    modifier = Modifier.weight(1f)
                )
            }

            if (currentFolder != null) {
                // Play / Shuffle Folder buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlassCard(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onPlayFolderClick(currentFolder) },
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
                                text = "Play Folder",
                                style = MaterialTheme.typography.labelLarge,
                                color = PrimaryAccent
                            )
                        }
                    }

                    GlassCard(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onShuffleFolderClick(currentFolder) },
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

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                if (currentFolder == null) {
                    // Root folders list
                    items(folders, key = { it.path }) { folder ->
                        FolderItemRow(
                            folder = folder,
                            onClick = { onFolderClick(folder) }
                        )
                    }
                } else {
                    // Subfolders if any
                    items(currentFolder.subFolders, key = { it.path }) { subFolder ->
                        FolderItemRow(
                            folder = subFolder,
                            onClick = { onFolderClick(subFolder) }
                        )
                    }

                    // Songs inside current folder
                    if (currentFolder.songs.isNotEmpty()) {
                        item {
                            Text(
                                text = "Songs in folder (${currentFolder.songs.size})",
                                style = MaterialTheme.typography.titleSmall,
                                color = OnSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        items(currentFolder.songs, key = { it.id }) { song ->
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
