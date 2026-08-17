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
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_ENABLE_SESSION) {

            // 1. Start the Session
            MotionState.isOverlayActive.value = true
            SoundPlayer.startPlaying()

            // 2. Update Widgets & Tiles
            updateSoundGlanceWidgets(context)

            // 3. Dismiss the Alert Notification
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.cancel(MotionDetectionService.ALERT_ID)
        }
    }
}