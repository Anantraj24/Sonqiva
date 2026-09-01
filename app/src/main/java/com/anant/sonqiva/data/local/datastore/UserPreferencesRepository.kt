package com.anant.sonqiva.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sonqiva_preferences")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val LOW_MEMORY_MODE = booleanPreferencesKey("low_memory_mode")
        val PLAYBACK_SPEED = floatPreferencesKey("playback_speed")
        val AUTO_RESUME = booleanPreferencesKey("auto_resume")
        val LAST_PLAYED_SONG_ID = longPreferencesKey("last_played_song_id")
        val LAST_PLAYED_POSITION = longPreferencesKey("last_played_position")
    }

    val lowMemoryModeFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.LOW_MEMORY_MODE] ?: false
    }

    val playbackSpeedFlow: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.PLAYBACK_SPEED] ?: 1.0f
    }

    val autoResumeFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.AUTO_RESUME] ?: true
    }

    suspend fun setLowMemoryMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LOW_MEMORY_MODE] = enabled
        }
    }

    suspend fun setPlaybackSpeed(speed: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PLAYBACK_SPEED] = speed
        }
    }

    suspend fun setAutoResume(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_RESUME] = enabled
        }
    }

    suspend fun saveLastPlaybackState(songId: Long, positionMs: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_PLAYED_SONG_ID] = songId
            preferences[PreferencesKeys.LAST_PLAYED_POSITION] = positionMs
        }
    }
}
