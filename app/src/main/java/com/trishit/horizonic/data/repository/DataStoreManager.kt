package com.trishit.horizonic.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.trishit.horizonic.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class DataStoreManager(private val context: Context) {

    companion object {
        val GYRO_SENSITIVITY = floatPreferencesKey("gyro_sensitivity")
        val PARTICLE_COLOR_THEME = stringPreferencesKey("particle_color_theme")
        val BASE_PARTICLES_PER_SIDE = intPreferencesKey("base_particles_per_side")
        val PARTICLE_SIZE = floatPreferencesKey("particle_size")
        val IS_AUTO_DETECT_ENABLED = booleanPreferencesKey("is_auto_detect_enabled")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val PLAYBACK_DURATION = intPreferencesKey("playback_duration")
    }

    val gyroSensitivity: Flow<Float> = context.dataStore.data.map { it[GYRO_SENSITIVITY] ?: 2.5f }
    val particleColorTheme: Flow<String> = context.dataStore.data.map { it[PARTICLE_COLOR_THEME] ?: "Calming Turquoise" }
    val baseParticlesPerSide: Flow<Int> = context.dataStore.data.map { it[BASE_PARTICLES_PER_SIDE] ?: 4 }
    val particleSize: Flow<Float> = context.dataStore.data.map { it[PARTICLE_SIZE] ?: 6f }
    val isAutoDetectEnabled: Flow<Boolean> = context.dataStore.data.map { it[IS_AUTO_DETECT_ENABLED] ?: false }
    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { 
        ThemeMode.valueOf(it[THEME_MODE] ?: ThemeMode.SYSTEM.name)
    }
    val playbackDuration: Flow<Int> = context.dataStore.data.map { it[PLAYBACK_DURATION] ?: 60 }

    suspend fun saveGyroSensitivity(value: Float) {
        context.dataStore.edit { it[GYRO_SENSITIVITY] = value }
    }

    suspend fun saveParticleColorTheme(value: String) {
        context.dataStore.edit { it[PARTICLE_COLOR_THEME] = value }
    }

    suspend fun saveBaseParticlesPerSide(value: Int) {
        context.dataStore.edit { it[BASE_PARTICLES_PER_SIDE] = value }
    }

    suspend fun saveParticleSize(value: Float) {
        context.dataStore.edit { it[PARTICLE_SIZE] = value }
    }

    suspend fun saveAutoDetectEnabled(value: Boolean) {
        context.dataStore.edit { it[IS_AUTO_DETECT_ENABLED] = value }
    }

    suspend fun saveThemeMode(value: ThemeMode) {
        context.dataStore.edit { it[THEME_MODE] = value.name }
    }

    suspend fun savePlaybackDuration(value: Int) {
        context.dataStore.edit { it[PLAYBACK_DURATION] = value }
    }
}
