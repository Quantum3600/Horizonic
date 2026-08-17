package com.trishit.horizonic

import kotlinx.coroutines.flow.MutableStateFlow

object MotionState {
    val isOverlayActive: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val gyroSensitivity: MutableStateFlow<Float> = MutableStateFlow(1.5f)
    val particleColorTheme: MutableStateFlow<String> = MutableStateFlow("Calming Turquoise")
    val ambientSpeed: MutableStateFlow<Float> = MutableStateFlow(1.0f)
    val baseParticlesPerSide: MutableStateFlow<Int> = MutableStateFlow(4)
    val particleSize: MutableStateFlow<Float> = MutableStateFlow(6f)
    val isServiceRunning: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isAutoDetectEnabled: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val themeMode: MutableStateFlow<ThemeMode> = MutableStateFlow(ThemeMode.SYSTEM)
    val isStatusNotificationEnabled: MutableStateFlow<Boolean> = MutableStateFlow(true)
}

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}