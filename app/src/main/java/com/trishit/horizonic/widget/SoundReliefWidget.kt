package com.trishit.horizonic.widget

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.trishit.horizonic.MotionState
import com.trishit.horizonic.R
import com.trishit.horizonic.utils.SoundPlayer

class SoundReliefWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val ctx = LocalContext.current
            val isPlaying = SoundPlayer.playingState.collectAsState().value
            val remSec = SoundPlayer.remainingSeconds.collectAsState().value

            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(Color(0xFF1E1E1E))
                    .padding(12.dp)
                    .cornerRadius(16.dp)
            ) {
                Row(
                    modifier = GlanceModifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_volume_up),
                        contentDescription = "Volume Indicator",
                        modifier = GlanceModifier.size(32.dp).padding(end = 10.dp)
                    )

                    Column(modifier = GlanceModifier.defaultWeight()) {
                        Text(
                            text = "Horizonic",
                            style = TextStyle(
                                color = ColorProvider(Color.White, Color.Black),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = GlanceModifier.height(2.dp))
                        Text(
                            text = if (isPlaying) {
                                val minStr = if (remSec > 0) {
                                    "${remSec / 60}:${String.format("%02d", remSec % 60)}"
                                } else {
                                    "Playing"
                                }
                                ctx.getString(R.string.active_format, minStr)
                            } else {
                                ctx.getString(R.string.ready_100hz)
                            },
                            style = TextStyle(
                                color = ColorProvider(Color(0xFF94A3B8), Color(0xFF1F2937)),
                                fontSize = 11.sp
                            )
                        )
                    }

                    Box(
                        modifier = GlanceModifier
                            .background(Color(0xFF333333))
                            .cornerRadius(12.dp)
                            .clickable(actionRunCallback<TogglePlayActionCallback>())
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                provider = ImageProvider(
                                    if (isPlaying) R.drawable.ic_stop else R.drawable.ic_play_arrow
                                ),
                                contentDescription = "Toggle Button",
                                modifier = GlanceModifier.size(16.dp).padding(end = 4.dp)
                            )
                            Text(
                                text = if (isPlaying) ctx.getString(R.string.stop) else ctx.getString(R.string.start),
                                style = TextStyle(
                                    color = ColorProvider(Color.White, Color.Black),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
class TogglePlayActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val currentlyActive =
            SoundPlayer.playingState.value || MotionState.isOverlayActive.value

        if (currentlyActive) {
            SoundPlayer.stopPlaying()
            MotionState.isOverlayActive.value = false
        } else {
            SoundPlayer.startPlaying()
            MotionState.isOverlayActive.value = true
        }

        SoundReliefWidget().updateAll(context)
    }
}