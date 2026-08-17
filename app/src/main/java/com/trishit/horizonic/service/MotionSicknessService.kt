package com.trishit.horizonic.service

import android.accessibilityservice.AccessibilityService
import android.graphics.PixelFormat
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import com.trishit.horizonic.MotionState
import com.trishit.horizonic.utils.SoundPlayer
import com.trishit.horizonic.utils.updateSoundGlanceWidgets

class MotionSicknessService : AccessibilityService(), SensorEventListener {
    private var windowManager: WindowManager? = null
    private var sensorManager: SensorManager? = null
    private var overlayView: ParticleOverlay? = null
    private var gyroSensor: Sensor? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        gyroSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        SoundPlayer.onStateChanged = {
            updateSoundGlanceWidgets(applicationContext)
        }
    }
    override fun onServiceConnected() {
        super.onServiceConnected()
        MotionState.isServiceRunning.value = true
        showOverlay()
        registerGyro()
    }
    private fun showOverlay() {
        if (overlayView == null) {
            overlayView = ParticleOverlay(this)
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                        WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                PixelFormat.TRANSLUCENT
            )
            try {
                windowManager?.addView(overlayView, params)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun hideOverlay() {
        overlayView?.let {
            try {
                windowManager?.removeView(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            overlayView = null
        }
    }

    private fun registerGyro() {
        gyroSensor?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    private fun unregisterGyro() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_GYROSCOPE) return

        overlayView?.updateGyroData(
            gx = event.values[0],
            gy = event.values[1],
            gz = event.values[2]
        )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    override fun onDestroy() {
        MotionState.isServiceRunning.value = false
        hideOverlay()
        unregisterGyro()
        super.onDestroy()
    }
}
