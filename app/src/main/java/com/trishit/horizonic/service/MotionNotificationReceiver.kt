package com.trishit.horizonic.service

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.trishit.horizonic.MotionState
import com.trishit.horizonic.utils.SoundPlayer
import com.trishit.horizonic.utils.updateSoundGlanceWidgets

class MotionNotificationReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_ENABLE_SESSION = "com.trishit.horizonic.ACTION_ENABLE_SESSION"
        const val ACTION_STOP_SESSION = "com.trishit.horizonic.ACTION_STOP_SESSION"
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_ENABLE_SESSION -> {
                MotionState.isOverlayActive.value = true
                SoundPlayer.startPlaying()
                updateSoundGlanceWidgets(context)

                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.cancel(MotionDetectionService.ALERT_ID)
            }

            ACTION_STOP_SESSION -> {
                MotionState.isOverlayActive.value = false
                SoundPlayer.stopPlaying()
                updateSoundGlanceWidgets(context)

                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.cancel(MotionSicknessService.ACTIVE_SESSION_NOTIFICATION_ID)
            }
        }
    }
}