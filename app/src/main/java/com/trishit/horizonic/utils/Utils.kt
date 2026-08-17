package com.trishit.horizonic.utils

import android.content.Context
import android.provider.Settings
import androidx.glance.appwidget.updateAll
import com.trishit.horizonic.service.MotionSicknessService
import com.trishit.horizonic.widget.SoundReliefWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.jvm.java

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

fun updateSoundGlanceWidgets(context: Context) {
    CoroutineScope(Dispatchers.Default).launch {
        try {
            SoundReliefWidget().updateAll(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}