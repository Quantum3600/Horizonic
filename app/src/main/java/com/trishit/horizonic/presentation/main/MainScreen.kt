package com.trishit.horizonic.presentation.main

import android.R.attr.fontWeight
import android.R.attr.text
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.trishit.horizonic.R
import com.trishit.horizonic.utils.checkOverlayPermission
import androidx.compose.ui.tooling.preview.Preview
import com.trishit.horizonic.ui.theme.HorizonicTheme
import kotlin.math.abs
import kotlin.random.Random

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val isActive by viewModel.isSessionActive.collectAsStateWithLifecycle()
    val remainingSeconds by viewModel.remainingSeconds.collectAsStateWithLifecycle()
    val totalDuration by viewModel.playbackDuration.collectAsStateWithLifecycle()
    val showPermissionDialog by viewModel.showPermissionDialog.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.updatePermissionStatus(context)
    }

    MainScreenContent(
        isActive = isActive,
        remainingSeconds = remainingSeconds,
        totalDuration = totalDuration,
        showPermissionDialog = showPermissionDialog,
        onNavigateToSettings = onNavigateToSettings,
        onToggleSession = { viewModel.toggleSession(it) },
        onDismissPermissionDialog = { viewModel.setPermissionDialogVisible(false) }
    )
}

@Composable
fun MainScreenContent(
    isActive: Boolean,
    remainingSeconds: Int,
    totalDuration: Int,
    showPermissionDialog: Boolean,
    onNavigateToSettings: () -> Unit,
    onToggleSession: (() -> Unit) -> Unit,
    onDismissPermissionDialog: () -> Unit
) {
    val context = LocalContext.current
    val progress = remember(remainingSeconds, totalDuration) {
        if (totalDuration <= 0) 1f 
        else remainingSeconds.toFloat() / totalDuration.toFloat()
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground
    val headphonesRequiredMsg = stringResource(R.string.headphones_required_toast)

    // Fluid speed control
    val targetSpeedFactor by animateFloatAsState(
        targetValue = if (isActive) 2.5f else 0.4f,
        animationSpec = tween(1000),
        label = "speedFactor"
    )

    var timePhase by remember { mutableFloatStateOf(0f) }
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            var lastTimeMillis = withFrameMillis { it }
            while (true) {
                withFrameMillis { frameTimeMillis ->
                    val deltaTime = (frameTimeMillis - lastTimeMillis) / 1000f
                    timePhase += deltaTime * targetSpeedFactor
                    lastTimeMillis = frameTimeMillis
                }
            }
        }
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = onDismissPermissionDialog,
            title = { Text(stringResource(R.string.accessibility_permission_title)) },
            text = { Text(stringResource(R.string.accessibility_permission_msg)) },
            confirmButton = {
                TextButton(
                    onClick = onNavigateToSettings
                ) {
                    Text(stringResource(R.string.setup))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissPermissionDialog) {
                    Text(stringResource(R.string.not_now))
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Ambient Background Wireframe Ripples
        BackgroundRipples(
            modifier = Modifier.fillMaxSize(),
            timePhase = timePhase,
            color = primaryColor,
            count = 8 // Reduced count
        )

        // No more ZenOrb cores - switched to pure wireframe ripples

        val statusBarPadding = WindowInsets.statusBars.asPaddingValues()

        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = statusBarPadding.calculateTopPadding() + 16.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.horizonic_title),
                    color = onBackgroundColor.copy(),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
                IconButton(onClick = onNavigateToSettings) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.settings_content_description),
                        tint = onBackgroundColor.copy(),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Tap to Start Area
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null 
                    ) {
                        onToggleSession {
                            Toast.makeText(
                                context,
                                headphonesRequiredMsg,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isActive) {
                    CircularWavyProgressIndicator(
                        modifier = Modifier.size(320.dp),
                        progress = { 1f - progress },
                        color = primaryColor,
                        stroke = Stroke(width = 20f),
                        amplitude = { 8f },
                        wavelength = 60.dp
                    )
                    
                    Text(
                        text = if (totalDuration <= 0) "∞" else "${remainingSeconds}s",
                        color = onSurfaceColor,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                    )
                } else {
                    Text(
                        text = stringResource(R.string.begin),
                        color = onSurfaceColor.copy(),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}


@Composable
fun BackgroundRipples(
    modifier: Modifier = Modifier,
    timePhase: Float,
    color: Color,
    count: Int = 15
) {
    // Large pool of random screen-wide positions
    val randomPositions = remember {
        List(100) { 
            Offset(Random.nextFloat(), Random.nextFloat())
        }
    }
    
    // Stable phase offsets for each ripple stream
    val phaseOffsets = remember {
        List(count) { Random.nextFloat() }
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val maxRippleRadius = maxOf(w, h) * 0.4f

        repeat(count) { i ->
            val progressValue = timePhase * 0.15f + phaseOffsets[i]
            val cycleCount = kotlin.math.floor(progressValue).toInt()
            val phase = progressValue % 1f
            
            // Pick a new position from the pool every time a new cycle starts
            val pos = randomPositions[abs(cycleCount + i * 7) % randomPositions.size]
            val centerOffset = Offset(pos.x * w, pos.y * h)
            
            val radius = maxRippleRadius * phase
            val alpha = (1f - phase).coerceIn(0f, 1f) * 0.45f
            
            if (radius > 0f) {
                drawCircle(
                    color = color.copy(alpha = alpha),
                    radius = radius,
                    center = centerOffset,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }
        }
    }
}

// Data models and helper functions can remain below

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    HorizonicTheme {
        MainScreenContent(
            isActive = false,
            remainingSeconds = 60,
            totalDuration = 60,
            showPermissionDialog = false,
            onNavigateToSettings = {},
            onToggleSession = {},
            onDismissPermissionDialog = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenActivePreview() {
    HorizonicTheme {
        MainScreenContent(
            isActive = true,
            remainingSeconds = 45,
            totalDuration = 60,
            showPermissionDialog = false,
            onNavigateToSettings = {},
            onToggleSession = {},
            onDismissPermissionDialog = {}
        )
    }
}
