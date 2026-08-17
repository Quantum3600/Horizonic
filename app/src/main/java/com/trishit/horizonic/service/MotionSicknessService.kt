package com.trishit.horizonic.service

import android.accessibilityservice.AccessibilityService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.PixelFormat
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import com.trishit.horizonic.MotionState
import com.trishit.horizonic.R
import com.trishit.horizonic.utils.SoundPlayer
import com.trishit.horizonic.utils.updateSoundGlanceWidgets
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class MotionSicknessService : AccessibilityService(), SensorEventListener {
    companion object {
        const val ACTIVE_SESSION_CHANNEL_ID = "active_session_channel"
        const val ACTIVE_SESSION_NOTIFICATION_ID = 201
    }

    private var windowManager: WindowManager? = null
    private var sensorManager: SensorManager? = null
    private var overlayView: ParticleOverlay? = null
    private var gyroSensor: Sensor? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        gyroSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        createActiveSessionNotificationChannel()
        observeStatusNotificationState()

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

    private fun observeStatusNotificationState() {
        serviceScope.launch {
            combine(
                MotionState.isOverlayActive,
                MotionState.isStatusNotificationEnabled
            ) { isOverlayActive, isStatusNotificationEnabled ->
                isOverlayActive && isStatusNotificationEnabled
            }.collectLatest { shouldShowNotification ->
                if (shouldShowNotification) {
                    showActiveNotification()
                } else {
                    cancelActiveNotification()
                }
            }
        }
    }

    private fun showActiveNotification() {
        val stopIntent = Intent(this, MotionNotificationReceiver::class.java).apply {
            action = MotionNotificationReceiver.ACTION_STOP_SESSION
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = androidx.core.app.NotificationCompat.Builder(this, ACTIVE_SESSION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(getString(R.string.active_session_title))
            .setContentText(getString(R.string.active_session_desc))
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(android.R.drawable.ic_media_pause, getString(R.string.stop), stopPendingIntent)
            .build()

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(ACTIVE_SESSION_NOTIFICATION_ID, notification)
    }

    private fun cancelActiveNotification() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(ACTIVE_SESSION_NOTIFICATION_ID)
    }

    private fun createActiveSessionNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            ACTIVE_SESSION_CHANNEL_ID,
            getString(R.string.active_session_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.active_session_channel_desc)
        }
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        MotionState.isServiceRunning.value = false
        cancelActiveNotification()
        serviceScope.cancel()
        hideOverlay()
        unregisterGyro()
        super.onDestroy()
    }
}
