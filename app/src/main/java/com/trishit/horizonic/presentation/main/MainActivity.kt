package com.trishit.horizonic.presentation.main

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.trishit.horizonic.MotionState
import com.trishit.horizonic.ThemeMode
import com.trishit.horizonic.presentation.settings.SettingsScreen
import com.trishit.horizonic.ui.theme.HorizonicTheme
import com.trishit.horizonic.utils.SoundPlayer
import com.trishit.horizonic.utils.checkIsAccessibilityServiceEnabled
import com.trishit.horizonic.utils.updateSoundGlanceWidgets
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

sealed interface Route {
    data object Main : Route
    data object Settings : Route
}

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    private val headsetReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_HEADSET_PLUG) {
                val state = intent.getIntExtra("state", 0)
                viewModel.setHeadsetConnected(state > 0)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        SoundPlayer.onStateChanged = {
            updateSoundGlanceWidgets(applicationContext)
        }

        // Initial check
        viewModel.checkHeadphoneConnection(this)

        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val context = LocalContext.current
            
            DisposableEffect(Unit) {
                val filter = IntentFilter(Intent.ACTION_HEADSET_PLUG)
                registerReceiver(headsetReceiver, filter)
                onDispose {
                    unregisterReceiver(headsetReceiver)
                }
            }

            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                // Handle permission result if needed
            }

            LaunchedEffect(Unit) {
                // Post-notification permission for Android 13+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                // Check Accessibility Service
                if (!checkIsAccessibilityServiceEnabled(context)) {
                    viewModel.setPermissionDialogVisible(true)
                }
            }

            HorizonicTheme(darkTheme = darkTheme) {
                // Navigation 3 uses a user-owned back stack defined as a SnapshotStateList
                val backStack = remember { mutableStateListOf<Route>(Route.Main) }

                // Periodic Accessibility Service Check
                LaunchedEffect(Unit) {
                    while (true) {
                        val isRunning = checkIsAccessibilityServiceEnabled(applicationContext)
                        MotionState.isServiceRunning.value = isRunning
                        delay(1000.milliseconds)
                    }
                }

                NavDisplay(
                    backStack = backStack,
                    onBack = { backStack.removeLastOrNull() },
                    transitionSpec = {
                        slideInHorizontally(initialOffsetX = { it }) togetherWith
                            slideOutHorizontally(targetOffsetX = { -it })
                    },
                    popTransitionSpec = {
                        slideInHorizontally(initialOffsetX = { -it }) togetherWith
                            slideOutHorizontally(targetOffsetX = { it })
                    },
                    predictivePopTransitionSpec = {
                        slideInHorizontally(initialOffsetX = { -it }) togetherWith
                            slideOutHorizontally(targetOffsetX = { it })
                    },
                    entryProvider = { key ->
                        when (key) {
                            Route.Main -> NavEntry(key) {
                                MainScreen(
                                    viewModel = viewModel,
                                    onNavigateToSettings = { backStack.add(Route.Settings) }
                                )
                            }
                            Route.Settings -> NavEntry(key) {
                                SettingsScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { backStack.removeLastOrNull() }
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}
