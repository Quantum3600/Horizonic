package com.trishit.horizonic.presentation.main

import android.app.Application
import android.content.Context
import android.content.Intent
import android.media.AudioDeviceInfo
import android.media.AudioManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.trishit.horizonic.MotionState
import com.trishit.horizonic.data.repository.DataStoreManager
import com.trishit.horizonic.service.MotionDetectionService
import com.trishit.horizonic.utils.SoundPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStoreManager = DataStoreManager(application)

    val isSessionActive: StateFlow<Boolean> = combine(
        SoundPlayer.playingState,
        MotionState.isOverlayActive
    ) { isPlaying, isOverlayActive ->
        isPlaying || isOverlayActive
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val playbackDuration = SoundPlayer.currentDurationSeconds
    val remainingSeconds = SoundPlayer.remainingSeconds
    val isServiceRunning = MotionState.isServiceRunning
    val isAutoDetectEnabled = MotionState.isAutoDetectEnabled
    val baseParticlesPerSide = MotionState.baseParticlesPerSide
    val particleSize = MotionState.particleSize
    val gyroSensitivity = MotionState.gyroSensitivity
    val particleColorTheme = MotionState.particleColorTheme
    val themeMode = MotionState.themeMode

    private val _isHeadsetConnected = MutableStateFlow(false)
    val isHeadsetConnected: StateFlow<Boolean> = _isHeadsetConnected

    init {
        viewModelScope.launch {
            dataStoreManager.gyroSensitivity.collectLatest { MotionState.gyroSensitivity.value = it }
        }
        viewModelScope.launch {
            dataStoreManager.particleColorTheme.collectLatest { MotionState.particleColorTheme.value = it }
        }
        viewModelScope.launch {
            dataStoreManager.baseParticlesPerSide.collectLatest { MotionState.baseParticlesPerSide.value = it }
        }
        viewModelScope.launch {
            dataStoreManager.particleSize.collectLatest { MotionState.particleSize.value = it }
        }
        viewModelScope.launch {
            dataStoreManager.isAutoDetectEnabled.collectLatest { MotionState.isAutoDetectEnabled.value = it }
        }
        viewModelScope.launch {
            dataStoreManager.themeMode.collectLatest { MotionState.themeMode.value = it }
        }
        viewModelScope.launch {
            dataStoreManager.playbackDuration.collectLatest { SoundPlayer.currentDurationSeconds.value = it }
        }
    }

    fun toggleSession() {
        val currentlyActive = isSessionActive.value
        if (currentlyActive) {
            SoundPlayer.stopPlaying()
            MotionState.isOverlayActive.value = false
        } else {
            SoundPlayer.startPlaying()
            MotionState.isOverlayActive.value = true
        }
    }

    fun setThemeMode(mode: com.trishit.horizonic.ThemeMode) {
        viewModelScope.launch { dataStoreManager.saveThemeMode(mode) }
    }

    fun setDuration(seconds: Int) {
        viewModelScope.launch { dataStoreManager.savePlaybackDuration(seconds) }
    }

    fun setBaseParticlesPerSide(count: Int) {
        viewModelScope.launch { dataStoreManager.saveBaseParticlesPerSide(count) }
    }

    fun setParticleSize(size: Float) {
        viewModelScope.launch { dataStoreManager.saveParticleSize(size) }
    }

    fun setGyroSensitivity(sensitivity: Float) {
        viewModelScope.launch { dataStoreManager.saveGyroSensitivity(sensitivity) }
    }

    fun setParticleColorTheme(theme: String) {
        viewModelScope.launch { dataStoreManager.saveParticleColorTheme(theme) }
    }

    fun checkHeadphoneConnection(context: Context) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        val connected = devices.any {
            it.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES || it.type == AudioDeviceInfo.TYPE_WIRED_HEADSET || it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP
        }
        _isHeadsetConnected.value = connected
    }

    fun toggleAutoDetect(context: Context) {
        val newState = !isAutoDetectEnabled.value
        viewModelScope.launch { dataStoreManager.saveAutoDetectEnabled(newState) }

        val intent = Intent(context, MotionDetectionService::class.java)
        if (newState) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } else {
            context.stopService(intent)
        }
    }
}
