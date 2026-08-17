package com.trishit.horizonic.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.glance.appwidget.updateAll
import com.trishit.horizonic.service.MotionSicknessService
import com.trishit.horizonic.widget.SoundReliefWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.core.net.toUri

fun checkIsAccessibilityServiceEnabled(context: Context): Boolean {
    return try {
        val expectedId = "${context.packageName}/${MotionSicknessService::class.java.name}"
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        enabledServices.split(":").any { it.trim().equals(expectedId, ignoreCase = true) }
    } catch (e: Exception) {
        false
    }
}

fun openAccessibilitySettings(context: Context) {
    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    context.startActivity(intent)
}

fun checkOverlayPermission(context: Context): Boolean {
    return Settings.canDrawOverlays(context)
}

fun openOverlaySettings(context: Context) {
    val intent = Intent(
        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
        "package:${context.packageName}".toUri()
    ).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    context.startActivity(intent)
}

fun updateSoundGlanceWidgets(context: Context) {
    CoroutineScope(Dispatchers.Default).launch {
        try {
            SoundReliefWidget().updateAll(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}