package com.trishit.horizonic.presentation.settings

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.trishit.horizonic.R
import com.trishit.horizonic.ThemeMode
import com.trishit.horizonic.presentation.components.WheelPicker
import com.trishit.horizonic.presentation.main.MainViewModel
import kotlinx.coroutines.launch
import androidx.core.net.toUri

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val playDuration by viewModel.playbackDuration.collectAsStateWithLifecycle()
    val isServiceRunning by viewModel.isServiceRunning.collectAsStateWithLifecycle()
    val isAutoDetectEnabled by viewModel.isAutoDetectEnabled.collectAsStateWithLifecycle()
    val isHeadsetConnected by viewModel.isHeadsetConnected.collectAsStateWithLifecycle()
    val baseParticlesPerSide by viewModel.baseParticlesPerSide.collectAsStateWithLifecycle()
    val particleSize by viewModel.particleSize.collectAsStateWithLifecycle()
    val gyroSensitivity by viewModel.gyroSensitivity.collectAsStateWithLifecycle()
    val particleColorTheme by viewModel.particleColorTheme.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    var isNotificationEnabled by remember { mutableStateOf(true) }

    var showDurationSheet by remember { mutableStateOf(false) }
    val sheetState = rememberBottomSheetState(SheetValue.Hidden)
    val scope = rememberCoroutineScope()
    var tempDuration by remember(playDuration) { mutableIntStateOf(playDuration) }

    val scrollState = rememberScrollState()

    // Check headphone status periodically when on this screen
    LaunchedEffect(Unit) {
        viewModel.checkHeadphoneConnection(context)
    }
    if (showDurationSheet) {
        ModalBottomSheet(
            onDismissRequest = { showDurationSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.playback_duration_picker_title),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                WheelPicker(
                    items = (40..120).toList(),
                    initialValue = if (playDuration in 40..120) playDuration else 60,
                    onValueChange = { tempDuration = it }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        viewModel.setDuration(tempDuration)
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            showDurationSheet = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(stringResource(R.string.ok), fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // 0. Appearance Group
            SettingsGroup(title = stringResource(R.string.appearance)) {
                SegmentedListItem(
                    shapes = ListItemDefaults.segmentedShapes(0, 1),
                    colors = ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    content = {
                        Column {
                            Text(
                                text = stringResource(R.string.theme),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
                            ) {
                                ThemeMode.entries.forEachIndexed { index, mode ->
                                    val isSelected = themeMode == mode
                                    ToggleButton(
                                        checked = isSelected,
                                        onCheckedChange = { viewModel.setThemeMode(mode) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp),
                                        shapes = when (index) {
                                            0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                            1 -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                                            2 -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                            else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                                        }
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            val themeIcon = when (mode) {
                                                ThemeMode.SYSTEM -> Icons.Default.Smartphone
                                                ThemeMode.LIGHT -> Icons.Default.LightMode
                                                ThemeMode.DARK -> Icons.Default.DarkMode
                                            }
                                            Icon(
                                                imageVector = themeIcon,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(Modifier.width(ToggleButtonDefaults.IconSpacing))
                                            Text(
                                                text = mode.name.lowercase()
                                                    .replaceFirstChar { it.uppercase() },
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                )
            }

            // 1. Playback Group
            SettingsGroup(title = stringResource(R.string.playback)) {
                // Playback Duration
                SegmentedListItem(
                    shapes = ListItemDefaults.segmentedShapes(0, 3),
                    colors = ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    onClick = { showDurationSheet = true },
                    content = {
                        Text(stringResource(R.string.duration_seconds))
                    },
                    trailingContent = {
                        Text(
                            text = if (playDuration == -1) "∞" else "$playDuration s",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                )

                // Headphones Status
                SegmentedListItem(
                    shapes = ListItemDefaults.segmentedShapes(1, 3),
                    colors = ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    content = {
                        Text(stringResource(R.string.check_for_headphones))
                    },
                    trailingContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isHeadsetConnected) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isHeadsetConnected) Color(0xFF00FFCC) else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isHeadsetConnected) stringResource(R.string.connected) else stringResource(R.string.required),
                                color = if (isHeadsetConnected) Color(0xFF00FFCC) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                fontSize = 14.sp
                            )
                        }
                    }
                )

                // Notifications
                SegmentedListItem(
                    shapes = ListItemDefaults.segmentedShapes(2, 3),
                    colors = ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    content = {
                        Text(stringResource(R.string.status_notification))
                    },
                    trailingContent = {
                        Switch(
                            checked = isNotificationEnabled,
                            onCheckedChange = { isNotificationEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                )
            }

            // 2. Motion & Permissions Group
            SettingsGroup(title = stringResource(R.string.motion_and_service)) {
                SegmentedListItem(
                    shapes = ListItemDefaults.segmentedShapes(0, 3),
                    colors = ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    content = {
                        Column {
                            Text(stringResource(R.string.auto_detect_motion))
                            Text(
                                stringResource(R.string.auto_detect_desc),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                fontSize = 12.sp
                            )
                        }
                    },
                    trailingContent = {
                        Switch(
                            checked = isAutoDetectEnabled,
                            onCheckedChange = { viewModel.toggleAutoDetect(context) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                )

                SegmentedListItem(
                    shapes = ListItemDefaults.segmentedShapes(1, 3),
                    colors = ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    content = {
                        Column {
                            Text(stringResource(R.string.accessibility_service))
                            Text(
                                text = if (isServiceRunning) stringResource(R.string.running) else stringResource(R.string.stopped),
                                color = if (isServiceRunning) Color(0xFF00FFCC) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                fontSize = 12.sp
                            )
                        }
                    },
                    trailingContent = {
                        Button(
                            onClick = {
                                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(stringResource(R.string.setup), fontSize = 12.sp)
                        }
                    }
                )

                SegmentedListItem(
                    shapes = ListItemDefaults.segmentedShapes(2, 3),
                    colors = ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    content = {
                        Text(stringResource(R.string.battery_optimization_settings))
                    },
                    leadingContent = {
                        Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                    },
                    onClick = { requestBatteryExemption(context) }
                )
            }

            // 3. Visual Motion Cues Group
            val visualSegmentsCount = if (isServiceRunning) 5 else 7
            var visualIndex = 0
            SettingsGroup(title = stringResource(R.string.visual_motion_cues)) {
                SegmentedListItem(
                    shapes = ListItemDefaults.segmentedShapes(visualIndex++, visualSegmentsCount),
                    colors = ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    content = {
                        Text(
                            text = stringResource(R.string.visual_motion_cues_desc),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                )

                if (!isServiceRunning) {
                    SegmentedListItem(
                        shapes = ListItemDefaults.segmentedShapes(visualIndex++, visualSegmentsCount),
                        content = {
                            Column {
                                Text(
                                    stringResource(R.string.setup_required),
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    stringResource(R.string.enable_overlay_to_see),
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontSize = 13.sp
                                )
                            }
                        },
                        leadingContent = {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        colors = ListItemDefaults.segmentedColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    )

                    SegmentedListItem(
                        shapes = ListItemDefaults.segmentedShapes(visualIndex++, visualSegmentsCount),
                        colors = ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        content = {
                            Column {
                                Text(
                                    stringResource(R.string.how_to_enable_overlay),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                val steps = listOf(
                                    stringResource(R.string.step_1),
                                    stringResource(R.string.step_2),
                                    stringResource(R.string.step_3)
                                )
                                steps.forEach { step ->
                                    Text(
                                        text = step,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                        fontSize = 13.sp,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        }
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    shape = RoundedCornerShape(24.dp)
                                ) {
                                    Text(stringResource(R.string.enable_overlay), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    )
                }

                // Sensitivity
                SegmentedListItem(
                    shapes = ListItemDefaults.segmentedShapes(visualIndex++, visualSegmentsCount),
                    colors = ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    content = {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(stringResource(R.string.motion_sensitivity))
                                Text(text = String.format("%.1f", gyroSensitivity), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 12.sp)
                            }
                            Slider(
                                value = gyroSensitivity,
                                onValueChange = { viewModel.setGyroSensitivity(it) },
                                valueRange = 0.5f..10.0f,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }
                    }
                )

                // Dot Size
                SegmentedListItem(
                    shapes = ListItemDefaults.segmentedShapes(visualIndex++, visualSegmentsCount),
                    colors = ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    content = {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(stringResource(R.string.dot_size))
                                Text(text = "${particleSize.toInt()} px", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 12.sp)
                            }
                            Slider(
                                value = particleSize,
                                onValueChange = { viewModel.setParticleSize(it) },
                                valueRange = 2f..20f,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }
                    }
                )

                // Dots Per Side
                SegmentedListItem(
                    shapes = ListItemDefaults.segmentedShapes(visualIndex++, visualSegmentsCount),
                    colors = ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    content = {
                        Column {
                            Text(
                                text = stringResource(R.string.dots_per_side),
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
                            ) {
                                (3..5).forEachIndexed { index, count ->
                                    val isSelected = baseParticlesPerSide == count
                                    ToggleButton(
                                        checked = isSelected,
                                        onCheckedChange = { viewModel.setBaseParticlesPerSide(count) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp),
                                        shapes = when (index) {
                                            0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                            1 -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                                            2 -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                            else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                                        }
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = count.toString(),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                )

                // Color Theme
                SegmentedListItem(
                    shapes = ListItemDefaults.segmentedShapes(visualIndex++, visualSegmentsCount),
                    colors = ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    content = {
                        Column {
                            Text(stringResource(R.string.color_theme), modifier = Modifier.padding(bottom = 12.dp))
                            val themes = listOf("Calming Turquoise", "Deep Ocean", "Soft Lavender", "Sunset Amber")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
                            ) {
                                themes.forEachIndexed { index, theme ->
                                    val isSelected = particleColorTheme == theme
                                    ToggleButton(
                                        checked = isSelected,
                                        onCheckedChange = { viewModel.setParticleColorTheme(theme) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp),
                                        shapes = when (index) {
                                            0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                            1 -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                                            2 -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                                            3 -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                            else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                                        }
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = theme.split(" ").last(),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                )
            }

            // 4. Info/Background Text
            SettingsGroup(title = stringResource(R.string.background)) {
                SegmentedListItem(
                    shapes = ListItemDefaults.segmentedShapes(0, 1),
                    colors = ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    content = {
                        Text(
                            text = stringResource(R.string.background_desc),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                )
            }

            // 5. Vestibular Harmony
            SettingsGroup(title = stringResource(R.string.vestibular_harmony)) {
                SegmentedListItem(
                    shapes = ListItemDefaults.segmentedShapes(0, 1),
                    colors = ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    content = {
                        Column {
                            Text(
                                stringResource(R.string.vestibular_harmony),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.vestibular_harmony_desc),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    },
                    leadingContent = {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 8.dp)
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
            content = content
        )
    }
}

@SuppressLint("BatteryLife")
fun requestBatteryExemption(context: Context) {
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    if (!powerManager.isIgnoringBatteryOptimizations(context.packageName)) {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = "package:${context.packageName}".toUri()
        }
        context.startActivity(intent)
    }
}