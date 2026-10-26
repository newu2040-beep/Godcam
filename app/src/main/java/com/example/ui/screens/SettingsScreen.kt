package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.UserPreferencesState
import com.example.model.AppThemeOption
import com.example.model.GridMode
import com.example.model.PhotoFormat
import com.example.model.ShutterSoundStyle
import com.example.ui.AppDestination
import com.example.ui.CameraUiState
import com.example.ui.GodcamViewModel
import com.example.ui.components.GodcamLensEmblem
import com.example.ui.theme.JetBrainsMonoFamily
import java.util.Locale

@Composable
fun SettingsScreen(
    uiState: CameraUiState,
    preferences: UserPreferencesState,
    viewModel: GodcamViewModel,
    onRequestPermissions: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(AppDestination.CAMERA)
    }

    val caps = uiState.capabilities

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp)
                .align(Alignment.TopCenter)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.navigateTo(AppDestination.CAMERA) },
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back to Camera",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Text(
                        text = "Settings & Hardware",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Surface(
                    onClick = { viewModel.navigateTo(AppDestination.SPLASH_HERO) },
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.testTag("settings_view_splash_button")
                ) {
                    Text(
                        text = "Hero Cover",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    )
                }
            }

            // 1. Real-Time Hardware Capability & Telemetry Matrix (Camera2, MediaCodecList, BatteryManager, PowerManager, StatFs)
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Memory,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Real-Time Hardware Telemetry",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        IconButton(
                            onClick = { viewModel.refreshHardwareTelemetryNow() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Refresh,
                                contentDescription = "Refresh Hardware Telemetry",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Text(
                        text = "Queried directly from Android Camera2 CameraCharacteristics, StreamConfigurationMap, MediaCodecList, BatteryManager, PowerManager, and StatFs.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    CapabilityStatRow("Active Camera Sensor ID", "Camera #${caps.activeCameraId} (${caps.hardwareLevelLabel})")
                    CapabilityStatRow("Optical Focal Length & Aperture", "${caps.focalLengthMm}mm • f/${caps.apertureF}")
                    CapabilityStatRow("Max Sensor Still Resolution", "${caps.maxPhotoWidth} × ${caps.maxPhotoHeight} px")
                    CapabilityStatRow("Optical Zoom Ratio Range", "${caps.minZoom}x – ${caps.maxZoom}x")
                    CapabilityStatRow("Optical / Digital Stabilization", if (caps.supportsOpticalStabilization) "OIS + EIS Supported" else "EIS Digital Supported")
                    CapabilityStatRow("Supported Video Resolutions", caps.supportedVideoResolutions.joinToString { it.shortLabel })
                    CapabilityStatRow("Supported Target FPS Ranges", caps.supportedFpsChoices.joinToString { "${it.fps}fps" })
                    CapabilityStatRow("Hardware Video Codecs", caps.supportedCodecs.joinToString { it.shortName })
                    if (caps.encoderNames.isNotEmpty()) {
                        CapabilityStatRow("MediaCodec Encoders", caps.encoderNames.joinToString())
                    }
                    CapabilityStatRow("Native Audio Sample Rate", "${caps.nativeAudioSampleRateHz} Hz")
                    CapabilityStatRow("Supported Audio Formats", caps.supportedAudioFormats.joinToString { it.shortBadge })
                    if (caps.audioEncoderNames.isNotEmpty()) {
                        CapabilityStatRow("Hardware Audio Encoders", caps.audioEncoderNames.joinToString())
                    }
                    CapabilityStatRow("ISO Sensitivity Range", "ISO ${caps.isoRange.first} – ${caps.isoRange.last}")
                    CapabilityStatRow("Exposure Compensation Range", "${caps.evRange.start} EV to +${caps.evRange.endInclusive} EV")
                    CapabilityStatRow("RAW (DNG) Sensor Support", if (caps.supportsRawCapture) "Supported" else "JPEG / PNG / WebP Only")
                    CapabilityStatRow("8K Native Hardware Support", if (caps.supports8K) "Supported" else "Not Supported")
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
                    CapabilityStatRow(
                        label = "Real-Time Accelerometer Tilt",
                        value = String.format(Locale.US, "%+.1f°", uiState.deviceTiltDegrees)
                    )
                    CapabilityStatRow(
                        label = "Battery Level & Temperature",
                        value = "${caps.batteryPercent}% • ${caps.batteryTempCelsius}°C"
                    )
                    CapabilityStatRow("PowerManager Thermal Status", caps.thermalStatusLabel)
                    CapabilityStatRow(
                        label = "Device Storage (StatFs)",
                        value = String.format(
                            Locale.US,
                            "%.1f GB free / %.1f GB total",
                            caps.availableStorageMb / 1024f,
                            caps.totalStorageMb / 1024f
                        )
                    )
                }
            }

            // 2. Themes & Personalization (8 Accessible Circular Swatches)
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Themes & Personalization",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Active: ${preferences.theme.title} — ${preferences.theme.direction}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        AppThemeOption.entries.forEach { themeOpt ->
                            val selected = preferences.theme == themeOpt
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable { viewModel.selectTheme(themeOpt) }
                                    .padding(vertical = 4.dp)
                                    .testTag("theme_swatch_${themeOpt.id}")
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(Color(themeOpt.bgHex))
                                        .border(
                                            width = if (selected) 3.dp else 1.5.dp,
                                            color = if (selected) MaterialTheme.colorScheme.primary else Color(themeOpt.accentHex),
                                            shape = CircleShape
                                        )
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(Color(themeOpt.accentHex))
                                    ) {
                                        if (selected) {
                                            Icon(
                                                imageVector = Icons.Filled.Check,
                                                contentDescription = "Selected Theme",
                                                tint = Color(themeOpt.primaryTextHex),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = themeOpt.title,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // 3. Capture Defaults & Haptics
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.CameraAlt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Default Capture Preferences",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Text("Default Composition Grid", style = MaterialTheme.typography.labelLarge)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GridMode.entries.forEach { grid ->
                            FilterChip(
                                selected = uiState.gridMode == grid,
                                onClick = { viewModel.cycleGridMode() },
                                label = { Text(grid.label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }

                    Text("Default Photo Format (Hardware Supported)", style = MaterialTheme.typography.labelLarge)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        caps.supportedPhotoFormats.forEach { fmt ->
                            FilterChip(
                                selected = uiState.photoFormat == fmt,
                                onClick = { viewModel.setPhotoFormat(fmt) },
                                label = { Text(fmt.label) }
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Fuji & Digital Camera Shutter Sound",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "${uiState.shutterSoundStyle.label} — ${uiState.shutterSoundStyle.description}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                        Switch(
                            checked = uiState.shutterSoundStyle != ShutterSoundStyle.OFF,
                            onCheckedChange = { viewModel.toggleShutterSoundOnOff() },
                            modifier = Modifier.testTag("settings_shutter_sound_switch")
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ShutterSoundStyle.entries.forEach { style ->
                            FilterChip(
                                selected = uiState.shutterSoundStyle == style,
                                onClick = { viewModel.setShutterSoundStyle(style) },
                                label = { Text(style.shortBadge) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.testTag("settings_shutter_${style.name}")
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                    Text("Default Video Audio Recording Format (Hardware Verified)", style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = "${uiState.audioRecordingFormat.label} (${uiState.audioRecordingFormat.specsLabel})",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        caps.supportedAudioFormats.forEach { audioFmt ->
                            FilterChip(
                                selected = uiState.audioRecordingFormat == audioFmt,
                                onClick = { viewModel.setAudioRecordingFormat(audioFmt) },
                                label = { Text("${audioFmt.label} (${audioFmt.shortBadge})") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.testTag("settings_audio_${audioFmt.name}")
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Tactile Shutter & Dial Haptics", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Text(
                                text = "Subtle vibration feedback on shutter, mode switch, and lens stops",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                        Switch(
                            checked = preferences.hapticsEnabled,
                            onCheckedChange = { viewModel.setHapticsEnabled(it) },
                            modifier = Modifier.testTag("settings_haptics_switch")
                        )
                    }
                }
            }

            // 4. Privacy & Contextual Permissions
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.PrivacyTip,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Privacy & Contextual Permissions",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Text(
                        text = "• Offline-First: No account, cloud upload, or internet is required. All 108 film looks, camera controls, and Export Studio run 100% locally on your device.\n• Zero-Permission Imports: External photos use the Android Photo Picker without broad storage access.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Include Geotag / Location Metadata", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Text(
                                text = "Opt-in only; removable anytime in Export Studio",
                                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                        Switch(
                            checked = preferences.preserveLocationMetadata,
                            onCheckedChange = { viewModel.setPreserveLocation(it) }
                        )
                    }

                    CapabilityStatRow(
                        label = "Camera Permission",
                        value = if (uiState.cameraPermissionGranted) "Granted" else "Tap Below to Grant"
                    )
                    CapabilityStatRow(
                        label = "Microphone Permission",
                        value = if (uiState.micPermissionGranted) "Granted" else "Tap Below to Grant"
                    )
                    CapabilityStatRow(
                        label = "Notifications Permission",
                        value = if (uiState.notificationsPermissionGranted) "Granted" else "Tap Below to Grant"
                    )
                    CapabilityStatRow(
                        label = "Gallery Auto-Save (DCIM/GODCAM)",
                        value = "Active (Photos & MP4 Videos)"
                    )

                    Surface(
                        onClick = {
                            onRequestPermissions()
                            viewModel.setShowPermissionsDialog(true)
                        },
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("settings_grant_all_permissions_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = if (uiState.allCorePermissionsGranted) {
                                    "Review Permissions & Gallery Auto-Save"
                                } else {
                                    "Allow All Permissions (Camera, Mic, Notifications & Gallery)"
                                },
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }

            // Footer Brand Attribution
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                GodcamLensEmblem(size = 48.dp)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "GODCAM • Version 1.0",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Capture More Than Moments.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
        }
    }
}

@Composable
private fun CapabilityStatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium.copy(
                fontFamily = JetBrainsMonoFamily,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        )
    }
}
