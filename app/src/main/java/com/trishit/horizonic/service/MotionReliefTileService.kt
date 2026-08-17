package com.trishit.horizonic.service

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.trishit.horizonic.MotionState
import com.trishit.horizonic.R
import com.trishit.horizonic.utils.SoundPlayer
import com.trishit.horizonic.utils.checkIsAccessibilityServiceEnabled
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MotionReliefTileService : TileService() {
    private var scope: CoroutineScope? = null
    private var flowJob: Job? = null

    override fun onStartListening() {
        super.onStartListening()
        scope = CoroutineScope(Dispatchers.Main)
        flowJob = scope?.launch {
            MotionState.isOverlayActive.collectLatest {
                updateTileState()
            }
        }
        updateTileState()
    }

    override fun onStopListening() {
        flowJob?.cancel()
        scope?.cancel()
        flowJob = null
        scope = null
        super.onStopListening()
    }

    @SuppressLint("StartActivityAndCollapseDeprecated")
    override fun onClick() {
        super.onClick()
        val context = applicationContext
        val isServiceRunning = checkIsAccessibilityServiceEnabled(context)

        if (!isServiceRunning) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                val pendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
                )
                startActivityAndCollapse(pendingIntent)
            } else {
                startActivityAndCollapse(intent)
            }
        } else {
            val isActive = MotionState.isOverlayActive.value
            if (isActive) {
                MotionState.isOverlayActive.value = false
                SoundPlayer.stopPlaying()
            } else {
                MotionState.isOverlayActive.value = true
                SoundPlayer.startPlaying()
            }
            updateTileState()
        }
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        val isServiceRunning = checkIsAccessibilityServiceEnabled(applicationContext)

        if (!isServiceRunning) {
            tile.state = Tile.STATE_INACTIVE
            tile.label = getString(R.string.app_name)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.subtitle = getString(R.string.setup_overlay)
            }
        } else {
            val isActive = MotionState.isOverlayActive.value
            tile.state = if (isActive) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            tile.label = getString(R.string.app_name)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.subtitle = if (isActive) getString(R.string.session_active) else getString(R.string.paused)
            }
        }
        tile.updateTile()
    }
}
