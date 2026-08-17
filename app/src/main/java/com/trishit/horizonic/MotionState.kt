package com.trishit.horizonic

import kotlinx.coroutines.flow.MutableStateFlow

object MotionState {
    val isOverlayActive: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val gyroSensitivity: MutableStateFlow<Float> = MutableStateFlow(2.5f)
    val particleColorTheme: MutableStateFlow<String> = MutableStateFlow("Calming Turquoise")
    val ambientSpeed: MutableStateFlow<Float> = MutableStateFlow(1.0f)
    val particleCount: MutableStateFlow<Int> = MutableStateFlow(6)
    val isServiceRunning: MutableStateFlow<Boolean> = MutableStateFlow(false)
}