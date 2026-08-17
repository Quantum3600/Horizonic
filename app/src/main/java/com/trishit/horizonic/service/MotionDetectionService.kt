package com.trishit.horizonic.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.trishit.horizonic.MotionState
import com.trishit.horizonic.R
import kotlin.math.sqrt

class MotionDetectionService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    private val threshold = 2.5f // m/s^2 threshold for vehicular motion
    private var continuousMotionCount = 0
    private val requiredMotionTicks = 5 // Requires 5 consecutive ticks over threshold
    private var lastNotificationTime = 0L
    private val cooldownMs = 60_000L // Don't spam notifications (1 minute cooldown)

    companion object {
        const val CHANNEL_ID = "motion_monitoring_channel"
        const val ALERT_CHANNEL_ID = "motion_alert_channel"
        const val FOREGROUND_ID = 101
        const val ALERT_ID = 102
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()

        val notification = createMonitoringNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                FOREGROUND_ID,
                notification,
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(FOREGROUND_ID, notification)
        }

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_LINEAR_ACCELERATION) return

        // Check if the session is already active. If yes, no need to detect/notify
        if (MotionState.isOverlayActive.value) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        // Calculate magnitude of acceleration vector
        val magnitude = sqrt((x * x + y * y + z * z).toDouble()).toFloat()

        if (magnitude > threshold) {
            continuousMotionCount++
            if (continuousMotionCount >= requiredMotionTicks) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastNotificationTime > cooldownMs) {
                    sendMotionAlertNotification()
                    lastNotificationTime = currentTime
                }
                continuousMotionCount = 0 // Reset after sending
            }
        } else {
            // Reset counter if motion stops
            continuousMotionCount = 0
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun sendMotionAlertNotification() {
        val intent = Intent(this, MotionNotificationReceiver::class.java).apply {
            action = MotionNotificationReceiver.ACTION_ENABLE_SESSION
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, ALERT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(getString(R.string.motion_detected))
            .setContentText(getString(R.string.traveling_prompt))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(android.R.drawable.ic_media_play, getString(R.string.start_session), pendingIntent)
            .build()

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(ALERT_ID, notification)
    }

    private fun createMonitoringNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(getString(R.string.monitoring_title))
            .setContentText(getString(R.string.monitoring_desc))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            val monitorChannel = NotificationChannel(
                CHANNEL_ID, getString(R.string.motion_monitoring_channel), NotificationManager.IMPORTANCE_LOW
            ).apply { description = getString(R.string.motion_monitoring_channel_desc) }

            val alertChannel = NotificationChannel(
                ALERT_CHANNEL_ID, getString(R.string.motion_alerts_channel), NotificationManager.IMPORTANCE_HIGH
            ).apply { description = getString(R.string.motion_alerts_channel_desc) }

            manager.createNotificationChannel(monitorChannel)
            manager.createNotificationChannel(alertChannel)
        }
    }

    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}