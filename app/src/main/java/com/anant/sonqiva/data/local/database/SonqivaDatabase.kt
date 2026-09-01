package com.anant.sonqiva.data.local.database

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey
    val songId: Long,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "history")
data class PlaybackHistoryEntity(
    @PrimaryKey
    val songId: Long,
    val lastPlayedTimestamp: Long = System.currentTimeMillis(),
    val playCount: Int = 1,
    val lastPositionMs: Long = 0L
)

@Dao
interface FavoriteDao {
    @Query("SELECT songId FROM favorites ORDER BY addedAt DESC")
    fun getAllFavoriteSongIds(): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE songId = :songId")
    fun deleteFavorite(songId: Long): Int
}

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPlaylist(playlist: PlaylistEntity): Long

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    fun deletePlaylist(playlistId: Long): Int
}

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history ORDER BY lastPlayedTimestamp DESC LIMIT 50")
    fun getRecentHistory(): Flow<List<PlaybackHistoryEntity>>

    @Query("SELECT * FROM history ORDER BY lastPlayedTimestamp DESC LIMIT 1")
    fun getLastPlayed(): PlaybackHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdateHistory(history: PlaybackHistoryEntity)
}

@Database(
    entities = [FavoriteEntity::class, PlaylistEntity::class, PlaybackHistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SonqivaDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun historyDao(): HistoryDao
}
