package com.trishit.horizonic.presentation.main

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.trishit.horizonic.R
import kotlin.random.Random

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onNavigateToSettings: () -> Unit
) {
    val isActive by viewModel.isSessionActive.collectAsStateWithLifecycle()
    val remainingSeconds by viewModel.remainingSeconds.collectAsStateWithLifecycle()
    val totalDuration by viewModel.playbackDuration.collectAsStateWithLifecycle()

    val progress = remember(remainingSeconds, totalDuration) {
        if (totalDuration <= 0) 1f 
        else remainingSeconds.toFloat() / totalDuration.toFloat()
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground

    // Generate random orb configurations when the session starts
    val orbConfigs = remember(isActive, primaryColor) {
        if (isActive) {
            List(Random.nextInt(4, 7)) {
                OrbConfig(
                    xPercent = Random.nextFloat(),
                    yPercent = Random.nextFloat(),
                    size = (120 + Random.nextInt(100)).dp,
                    color = primaryColor.copy(alpha = 0.1f + Random.nextFloat() * 0.15f)
                )
            }
        } else emptyList()
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight

        // Zen Orbs with Ripples at Random Positions
        if (isActive) {
            orbConfigs.forEach { config ->
                ZenOrb(
                    modifier = Modifier
                        .offset(
                            x = screenWidth * config.xPercent - (config.size / 2),
                            y = screenHeight * config.yPercent - (config.size / 2)
                        ),
                    orbSize = config.size,
                    color = config.color
                )
            }
        } else {
            // Static decorative orb when inactive
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = 60.dp, y = (-100).dp)
                    .size(250.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(primaryColor.copy(alpha = 0.1f), Color.Transparent)
                        )
                    )
            )
        }

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
                    color = onBackgroundColor.copy(alpha = 0.8f),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 3.sp
                )
                IconButton(onClick = onNavigateToSettings) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.settings_content_description),
                        tint = onBackgroundColor.copy(alpha = 0.6f),
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
                        viewModel.toggleSession()
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isActive) {
                    CircularWavyProgressIndicator(
                        modifier = Modifier.size(240.dp),
                        progress = { progress },
                        color = primaryColor.copy(alpha = 0.3f),
                        amplitude = { 1f }
                    )
                    
                    Text(
                        text = if (totalDuration <= 0) "∞" else "$remainingSeconds",
                        color = onSurfaceColor.copy(alpha = 0.6f),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.ExtraLight,
                        letterSpacing = if (totalDuration <= 0) 0.sp else 4.sp
                    )
                } else {
                    Text(
                        text = stringResource(R.string.begin),
                        color = onSurfaceColor.copy(alpha = 0.4f),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraLight,
                        letterSpacing = 6.sp
                    )
                }
            }
        }
    }
}

data class OrbConfig(
    val xPercent: Float,
    val yPercent: Float,
    val size: Dp,
    val color: Color
)

@Composable
fun ZenOrb(
    modifier: Modifier = Modifier,
    orbSize: Dp,
    color: Color
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Ripples - we use a very large size to ensure they can cover the screen
        val rippleSize = 1500.dp 
        ZenRipple(modifier = Modifier.size(rippleSize), delayMillis = 0, color = color)
        ZenRipple(modifier = Modifier.size(rippleSize), delayMillis = 2000, color = color)
        ZenRipple(modifier = Modifier.size(rippleSize), delayMillis = 4000, color = color)

        // The Orb itself
        Box(
            modifier = Modifier
                .size(orbSize)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(color, Color.Transparent)
                    )
                )
        )
    }
}

@Composable
fun ZenRipple(
    modifier: Modifier = Modifier,
    delayMillis: Int,
    color: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ripple")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, delayMillis = delayMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    Canvas(modifier = modifier) {
        val maxRadius = size.maxDimension / 2
        val currentRadius = maxRadius * progress
        val alpha = (1f - progress).coerceIn(0f, 1f) * 0.3f

        drawCircle(
            color = color.copy(alpha = alpha),
            radius = currentRadius,
            style = Stroke(width = 1.dp.toPx())
        )
    }
}