package com.example.ui.screens

import android.graphics.Bitmap
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.outlined.AspectRatio
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Cameraswitch
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.Exposure
import androidx.compose.material.icons.outlined.FilterBAndW
import androidx.compose.material.icons.outlined.FlashAuto
import androidx.compose.material.icons.outlined.FlashOff
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.HighQuality
import androidx.compose.material.icons.outlined.Iso
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.MicOff
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Nightlight
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShutterSpeed
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.FilterCatalog
import com.example.data.ImageFilterEngine
import com.example.model.AspectRatioOption
import com.example.model.CaptureMode
import com.example.model.FlashModeOption
import com.example.model.MediaItemModel
import com.example.model.PhotoFormat
import com.example.model.StabilizationMode
import com.example.model.TimerOption
import com.example.model.VideoCodecOption
import com.example.model.VideoFps
import com.example.model.VideoResolution
import com.example.model.WhiteBalancePreset
import com.example.ui.AppDestination
import com.example.ui.CameraUiState
import com.example.ui.GodcamViewModel
import com.example.ui.components.CircularIconControl
import com.example.ui.components.GodcamShutterButton
import com.example.ui.components.LabeledCameraSlider
import com.example.ui.components.LiveHistogramOverlay
import com.example.ui.components.SegmentedPillSelector
import com.example.ui.components.ViewfinderCompositionOverlays
import com.example.ui.theme.GodcamAmberGold
import com.example.ui.theme.GodcamForestActive
import com.example.ui.theme.GodcamMintAccent
import com.example.ui.theme.GodcamRecordRed
import com.example.ui.theme.JetBrainsMonoFamily
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    uiState: CameraUiState,
    recentMedia: MediaItemModel?,
    viewModel: GodcamViewModel,
    onRequestPermissions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var showMoreToolsSheet by remember { mutableStateOf(false) }
    var showRatioDialogSheet by remember { mutableStateOf(false) }
    var showExposurePopup by remember { mutableStateOf(false) }
    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }

    val activePreset = remember(uiState.activeRecipe.presetId) {
        FilterCatalog.getById(uiState.activeRecipe.presetId)
    }
    val activeColorMatrix = remember(activePreset, uiState.activeRecipe) {
        ImageFilterEngine.buildComposeColorMatrix(activePreset, uiState.activeRecipe)
    }
    val sceneBitmap = remember(uiState.viewfinderSceneIndex) {
        ImageFilterEngine.createCuratedSceneBitmap(uiState.viewfinderSceneIndex, 1080, 1350)
    }
    val recentThumbBitmap = remember(recentMedia?.id, recentMedia?.filePath, recentMedia?.sampleSceneIndex) {
        ImageFilterEngine.loadBitmapForMediaItem(context, recentMedia, 240, 240)
    }
    val histogramBins = remember(sceneBitmap, uiState.exposureEv) {
        ImageFilterEngine.computeLumaHistogram(sceneBitmap)
    }

    // Bind CameraX Preview when permission is granted
    DisposableEffect(uiState.cameraPermissionGranted, uiState.isFrontCamera, previewViewRef) {
        val view = previewViewRef
        if (uiState.cameraPermissionGranted && view != null) {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            val executor = ContextCompat.getMainExecutor(context)
            cameraProviderFuture.addListener({
                runCatching {
                    val provider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = view.surfaceProvider
                    }
                    val selector = if (uiState.isFrontCamera) {
                        CameraSelector.DEFAULT_FRONT_CAMERA
                    } else {
                        CameraSelector.DEFAULT_BACK_CAMERA
                    }
                    provider.unbindAll()
                    val camera = provider.bindToLifecycle(lifecycleOwner, selector, preview)
                    camera.cameraControl.enableTorch(uiState.flashMode == FlashModeOption.TORCH)
                    camera.cameraControl.setZoomRatio(uiState.activeZoom.coerceIn(1f, 5f))
                }
            }, executor)
        }
        onDispose { }
    }

    // Update Torch & Zoom dynamically on active CameraX session
    LaunchedEffect(uiState.flashMode, uiState.activeZoom) {
        if (uiState.cameraPermissionGranted) {
            runCatching {
                val provider = ProcessCameraProvider.getInstance(context).get()
                val selector = if (uiState.isFrontCamera) {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                } else {
                    CameraSelector.DEFAULT_BACK_CAMERA
                }
                if (provider.hasCamera(selector)) {
                    val preview = Preview.Builder().build().also {
                        previewViewRef?.surfaceProvider?.let { sp -> it.surfaceProvider = sp }
                    }
                    val cam = provider.bindToLifecycle(lifecycleOwner, selector, preview)
                    cam.cameraControl.enableTorch(uiState.flashMode == FlashModeOption.TORCH)
                    cam.cameraControl.setZoomRatio(uiState.activeZoom.coerceIn(1f, 5f))
                }
            }
        }
    }

    val isProVideoStyle = uiState.captureMode == CaptureMode.PRO_VIDEO
    val isProPhotoStyle = uiState.captureMode == CaptureMode.PRO_PHOTO

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        val isExpandedWidth = maxWidth >= 680.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = if (isExpandedWidth) 640.dp else 560.dp)
                .align(Alignment.Center)
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. TOP CAMERA CONTROL BAR (Matches Screens 2 & 4 of reference image)
            CameraTopBar(
                uiState = uiState,
                onOpenMoreSheet = { showMoreToolsSheet = true },
                onToggleProMode = {
                    val next = when (uiState.captureMode) {
                        CaptureMode.PHOTO -> CaptureMode.PRO_PHOTO
                        CaptureMode.PRO_PHOTO -> CaptureMode.PHOTO
                        CaptureMode.VIDEO -> CaptureMode.PRO_VIDEO
                        CaptureMode.PRO_VIDEO -> CaptureMode.VIDEO
                        CaptureMode.NIGHT -> CaptureMode.PHOTO
                    }
                    viewModel.setCaptureMode(next)
                },
                onCycleFlash = { viewModel.cycleFlashMode() },
                onSwitchLens = { viewModel.switchCameraLens() },
                onOpenSettings = { viewModel.navigateTo(AppDestination.SETTINGS) }
            )

            Spacer(modifier = Modifier.height(6.dp))

            // 2. HERO VIEWFINDER CARD (Live CameraX + Real-time Filter Matrix + Composition Overlays)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFF141D1A))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f),
                        shape = RoundedCornerShape(22.dp)
                    )
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { offset ->
                                val nx = (offset.x / size.width.coerceAtLeast(1)).coerceIn(0.08f, 0.92f)
                                val ny = (offset.y / size.height.coerceAtLeast(1)).coerceIn(0.08f, 0.92f)
                                viewModel.onTapToFocus(nx, ny)
                                showExposurePopup = true
                            },
                            onDoubleTap = {
                                viewModel.switchCameraLens()
                            },
                            onLongPress = {
                                viewModel.toggleAeAfLock()
                            }
                        )
                    }
                    .testTag("camera_viewfinder")
            ) {
                // Optical Scene & Live CameraX Preview Layer
                Image(
                    bitmap = sceneBitmap.asImageBitmap(),
                    contentDescription = "Live Optical Viewfinder Preview",
                    contentScale = ContentScale.Crop,
                    colorFilter = ColorFilter.colorMatrix(activeColorMatrix),
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(uiState.activeZoom.coerceIn(0.85f, 2.5f))
                )

                if (uiState.cameraPermissionGranted) {
                    AndroidView(
                        factory = { ctx ->
                            PreviewView(ctx).apply {
                                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                                scaleType = PreviewView.ScaleType.FILL_CENTER
                                alpha = 0.96f
                                previewViewRef = this
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Grid, Golden Ratio, Level, Cinematic Framing & Focus Reticle Overlay
                ViewfinderCompositionOverlays(
                    gridMode = uiState.gridMode,
                    aspectRatio = uiState.aspectRatio,
                    focusPoint = uiState.focusPointNormalized,
                    aeAfLocked = uiState.aeAfLocked,
                    showFocusPeaking = uiState.showFocusPeaking,
                    showZebraStripes = uiState.showZebraStripes
                )

                // Top-left Viewfinder Badges (RAW / NIGHT / Active Look / AE-AF Lock)
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!uiState.captureMode.isVideo) {
                        Surface(
                            onClick = {
                                val nextFmt = if (uiState.photoFormat == PhotoFormat.RAW_DNG) {
                                    PhotoFormat.JPEG
                                } else {
                                    PhotoFormat.RAW_DNG
                                }
                                viewModel.setPhotoFormat(nextFmt)
                            },
                            shape = RoundedCornerShape(50),
                            color = Color(0x991C2824),
                            contentColor = Color(0xFFFCFBF7)
                        ) {
                            Text(
                                text = uiState.photoFormat.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.6.sp
                                ),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }
                    }

                    if (uiState.activeRecipe.presetId != "original") {
                        Surface(
                            onClick = { viewModel.navigateTo(AppDestination.FILTERS) },
                            shape = RoundedCornerShape(50),
                            color = Color(0x991C2824),
                            contentColor = GodcamMintAccent
                        ) {
                            Text(
                                text = "${activePreset.name} • ${(uiState.activeRecipe.intensity * 100).roundToInt()}%",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    if (uiState.aeAfLocked) {
                        Surface(
                            onClick = { viewModel.toggleAeAfLock() },
                            shape = RoundedCornerShape(50),
                            color = GodcamAmberGold.copy(alpha = 0.90f),
                            contentColor = Color(0xFF1A2420)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Lock,
                                    contentDescription = "AE/AF Locked",
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "AE/AF LOCK",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }

                // Top-Center Video / Pro Telemetry Pill (Matches Screen 4: "4K · 60fps · 100Mbps")
                if (uiState.captureMode.isVideo || isProPhotoStyle) {
                    val telemetryText = if (uiState.captureMode.isVideo) {
                        val recPrefix = if (uiState.isRecordingVideo) {
                            val mins = uiState.recordingElapsedSec / 60
                            val secs = uiState.recordingElapsedSec % 60
                            String.format(Locale.US, "REC %02d:%02d • ", mins, secs)
                        } else ""
                        "$recPrefix${uiState.videoResolution.shortLabel} · ${uiState.videoFps.fps}fps · ${uiState.videoBitrateMbps}Mbps"
                    } else {
                        val isoVal = if (uiState.isAutoIso) "ISO Auto" else "ISO ${uiState.manualIso}"
                        val shutVal = if (uiState.isAutoShutter) "1/250s" else "1/${uiState.shutterDenominator}s"
                        "$isoVal · $shutVal · ${uiState.whiteBalance.label}"
                    }

                    Surface(
                        onClick = {
                            if (uiState.isRecordingVideo) {
                                viewModel.finalizeAndAutoSaveVideo { previewViewRef?.bitmap }
                            }
                        },
                        shape = RoundedCornerShape(50),
                        color = if (uiState.isRecordingVideo) GodcamRecordRed.copy(alpha = 0.92f) else Color(0xAA16221E),
                        contentColor = Color(0xFFFCFBF7),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 12.dp)
                    ) {
                        Text(
                            text = if (uiState.isRecordingVideo) "$telemetryText • Tap to Save" else telemetryText,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = JetBrainsMonoFamily,
                                color = Color(0xFFFCFBF7)
                            ),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }

                // Top-right Live Histogram or Aspect Ratio Guide Badge
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                ) {
                    if (uiState.aspectRatio.isCinematicGuide) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color(0xAA16221E),
                            contentColor = GodcamAmberGold
                        ) {
                            Text(
                                text = "${uiState.aspectRatio.label} Guide",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                    if (uiState.showHistogram || isProPhotoStyle) {
                        LiveHistogramOverlay(bins = histogramBins)
                    }
                }

                // Self-Timer Full-Viewfinder Countdown Overlay
                if (uiState.countdownRemainingSec > 0) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x66101815))
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${uiState.countdownRemainingSec}",
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontSize = 72.sp,
                                    fontFamily = JetBrainsMonoFamily,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "Tap shutter to cancel",
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.85f))
                            )
                        }
                    }
                }

                // Contextual Permission Pill Banner (Opens full Permissions & Gallery Access dialog + system prompt)
                if (!uiState.allCorePermissionsGranted) {
                    Surface(
                        onClick = {
                            onRequestPermissions()
                            viewModel.setShowPermissionsDialog(true)
                        },
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xCC182621),
                        contentColor = Color(0xFFFCFBF7),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 24.dp)
                            .testTag("enable_live_camera_pill")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AutoAwesome,
                                contentDescription = null,
                                tint = GodcamMintAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Column(modifier = Modifier.weight(1f, fill = false)) {
                                Text(
                                    text = "Allow Camera, Mic, Notifications & Gallery",
                                    style = MaterialTheme.typography.labelLarge.copy(color = Color(0xFFFCFBF7))
                                )
                                Text(
                                    text = "Tap to grant permissions • Captures auto-save to Gallery",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFD0DDD6))
                                )
                            }
                        }
                    }
                }

                // Bottom of Viewfinder: Zoom Stops Pill ("0.5  1x  2  3") & EV/Scene Switcher (Matches Screen 2 of reference image)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    // Optical Zoom Selector Pill centered
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0x99242E2A),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .height(36.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            uiState.capabilities.zoomStops.forEach { stop ->
                                val isSelected = kotlin.math.abs(uiState.activeZoom - stop) < 0.15f
                                val label = if (isSelected) {
                                    if (stop == stop.toInt().toFloat()) "${stop.toInt()}x" else "${stop}x"
                                } else {
                                    if (stop == stop.toInt().toFloat()) "${stop.toInt()}" else "$stop"
                                }
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) Color(0xFF233930) else Color.Transparent)
                                        .then(
                                            if (isSelected) Modifier.border(1.dp, GodcamMintAccent.copy(alpha = 0.7f), CircleShape)
                                            else Modifier
                                        )
                                        .clickable { viewModel.setZoom(stop) }
                                        .testTag("zoom_stop_$stop")
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = JetBrainsMonoFamily,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) GodcamMintAccent else Color(0xFFE6ECE9)
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Bottom-Right EV / Scene Quick Toggle inside Viewfinder (Matches Screen 2 icon)
                    Surface(
                        onClick = { showExposurePopup = !showExposurePopup },
                        shape = CircleShape,
                        color = if (showExposurePopup || uiState.exposureEv != 0f) GodcamForestActive else Color(0x99242E2A),
                        contentColor = Color(0xFFFCFBF7),
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(34.dp)
                            .testTag("viewfinder_ev_toggle")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Exposure,
                                contentDescription = "Exposure Compensation",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Shutter Flash Feedback Overlay
                androidx.compose.animation.AnimatedVisibility(
                    visible = uiState.shutterFlashVisible,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xCCFCFBF7))
                    )
                }
            }

            // Status / Capability / Auto-Save Feedback Toast Banner
            AnimatedVisibility(visible = uiState.statusBannerMessage != null) {
                uiState.statusBannerMessage?.let { msg ->
                    Surface(
                        onClick = {
                            if (msg.contains("Gallery", ignoreCase = true)) {
                                viewModel.navigateTo(AppDestination.GALLERY)
                            } else {
                                viewModel.showBanner(null)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                ),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = if (msg.contains("Gallery", ignoreCase = true)) "View Gallery" else "Dismiss",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }

            // Quick Inline Exposure (EV) Slider when tapped inside viewfinder
            AnimatedVisibility(visible = showExposurePopup && !isProPhotoStyle && !isProVideoStyle) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                ) {
                    Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                        LabeledCameraSlider(
                            label = "Exposure Compensation (EV)",
                            valueText = "${if (uiState.exposureEv > 0) "+" else ""}${uiState.exposureEv} EV",
                            value = uiState.exposureEv,
                            valueRange = uiState.capabilities.evRange,
                            onValueChange = { viewModel.setExposureEv(it) },
                            onReset = { viewModel.setExposureEv(0f) },
                            testTag = "quick_ev_slider"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 3. MODE SELECTOR PILL (Matches Screen 2 "Photo | Video" and Screen 4 "Video | Pro Video")
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.captureMode == CaptureMode.VIDEO || uiState.captureMode == CaptureMode.PRO_VIDEO) {
                    SegmentedPillSelector(
                        options = listOf(CaptureMode.VIDEO, CaptureMode.PRO_VIDEO),
                        selectedOption = uiState.captureMode,
                        labelProvider = { it.label },
                        onOptionSelected = { viewModel.setCaptureMode(it) },
                        testTagPrefix = "mode_pill"
                    )
                } else {
                    SegmentedPillSelector(
                        options = listOf(CaptureMode.PHOTO, CaptureMode.VIDEO),
                        selectedOption = if (uiState.captureMode.isVideo) CaptureMode.VIDEO else CaptureMode.PHOTO,
                        labelProvider = { it.label },
                        onOptionSelected = { viewModel.setCaptureMode(it) },
                        testTagPrefix = "mode_pill"
                    )
                }
            }

            // 4. PRO VIDEO / PRO PHOTO SLIDERS DECK (Matches Screen 4 of reference image)
            AnimatedVisibility(visible = isProVideoStyle || isProPhotoStyle) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
                ) {
                    if (isProVideoStyle) {
                        ProVideoSlidersDeck(uiState = uiState, viewModel = viewModel)
                    } else {
                        ProPhotoSlidersDeck(uiState = uiState, viewModel = viewModel)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 5. PRIMARY SHUTTER ROW (Recent Thumbnail • Large Tactile Shutter • Lens Switch)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Recent Media Circular Thumbnail -> Opens Gallery
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        .clickable { viewModel.navigateTo(AppDestination.GALLERY) }
                        .testTag("recent_media_thumbnail")
                ) {
                    Image(
                        bitmap = recentThumbBitmap.asImageBitmap(),
                        contentDescription = "Open Gallery",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Large Tactile Shutter / Record Control
                GodcamShutterButton(
                    isVideoMode = uiState.captureMode.isVideo,
                    isRecording = uiState.isRecordingVideo,
                    onClick = {
                        viewModel.onPrimaryShutterPressed(
                            livePreviewBitmapProvider = { previewViewRef?.bitmap }
                        )
                    }
                )

                // Front / Rear Lens Switch Button
                CircularIconControl(
                    icon = Icons.Outlined.Cameraswitch,
                    contentDescription = "Switch Front/Rear Camera",
                    onClick = { viewModel.switchCameraLens() },
                    size = 46.dp,
                    testTag = "switch_camera_button"
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 6. BOTTOM QUICK TOOL DOCK (Matches Screen 2: Filter, Ratio, Grid, Timer, More; Screen 4: Resolution, FPS, Bitrate, Format, More)
            if (isProVideoStyle) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 2.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    QuickToolItem(
                        icon = Icons.Outlined.HighQuality,
                        label = "Resolution",
                        badge = uiState.videoResolution.shortLabel,
                        onClick = {
                            val list = uiState.capabilities.supportedVideoResolutions
                            val next = list[(list.indexOf(uiState.videoResolution) + 1) % list.size]
                            viewModel.setVideoResolution(next)
                        },
                        testTag = "dock_resolution"
                    )
                    QuickToolItem(
                        icon = Icons.Outlined.Speed,
                        label = "FPS",
                        badge = "${uiState.videoFps.fps}",
                        onClick = {
                            val valid = uiState.capabilities.validFpsFor(uiState.videoResolution, uiState.videoCodec)
                            val next = valid[(valid.indexOf(uiState.videoFps) + 1) % valid.size]
                            viewModel.setVideoFps(next)
                        },
                        testTag = "dock_fps"
                    )
                    QuickToolItem(
                        icon = Icons.Outlined.Tune,
                        label = "Bitrate",
                        badge = "${uiState.videoBitrateMbps}M",
                        onClick = {
                            val tiers = listOf(24, 50, 100, 140)
                            val idx = tiers.indexOfFirst { it >= uiState.videoBitrateMbps }
                            val next = tiers[(idx + 1) % tiers.size]
                            viewModel.setVideoBitrateMbps(next)
                        },
                        testTag = "dock_bitrate"
                    )
                    QuickToolItem(
                        icon = Icons.Outlined.Videocam,
                        label = "Format",
                        badge = uiState.videoCodec.shortName,
                        onClick = {
                            val next = if (uiState.videoCodec == VideoCodecOption.H264) {
                                VideoCodecOption.HEVC
                            } else {
                                VideoCodecOption.H264
                            }
                            viewModel.setVideoCodec(next)
                        },
                        testTag = "dock_format"
                    )
                    QuickToolItem(
                        icon = Icons.Outlined.MoreHoriz,
                        label = "More",
                        onClick = { showMoreToolsSheet = true },
                        testTag = "dock_more"
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 2.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    QuickToolItem(
                        icon = Icons.Outlined.FilterBAndW,
                        label = "Filter",
                        isActive = uiState.activeRecipe.presetId != "original",
                        onClick = { viewModel.navigateTo(AppDestination.FILTERS) },
                        testTag = "dock_filter"
                    )
                    QuickToolItem(
                        icon = Icons.Outlined.AspectRatio,
                        label = "Ratio",
                        badge = uiState.aspectRatio.label,
                        onClick = { showRatioDialogSheet = true },
                        testTag = "dock_ratio"
                    )
                    QuickToolItem(
                        icon = Icons.Outlined.GridOn,
                        label = "Grid",
                        badge = if (uiState.gridMode.name != "OFF") uiState.gridMode.label else null,
                        isActive = uiState.gridMode.name != "OFF",
                        onClick = { viewModel.cycleGridMode() },
                        testTag = "dock_grid"
                    )
                    QuickToolItem(
                        icon = Icons.Outlined.Timer,
                        label = "Timer",
                        badge = if (uiState.timerOption != TimerOption.OFF) uiState.timerOption.label else null,
                        isActive = uiState.timerOption != TimerOption.OFF,
                        onClick = { viewModel.cycleTimerOption() },
                        testTag = "dock_timer"
                    )
                    QuickToolItem(
                        icon = Icons.Outlined.MoreHoriz,
                        label = "More",
                        isActive = isProPhotoStyle || uiState.nightModeActive,
                        onClick = { showMoreToolsSheet = true },
                        testTag = "dock_more"
                    )
                }
            }
        }
    }

    // Aspect Ratio & Cinematic Framing Bottom Sheet
    if (showRatioDialogSheet) {
        ModalBottomSheet(
            onDismissRequest = { showRatioDialogSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .navigationBarsPadding()
            ) {
                Text(
                    text = "Aspect Ratio & Framing Guides",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Native sensor ratios capture full-frame; cinematic ratios apply a non-destructive viewfinder guide and export crop.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Spacer(modifier = Modifier.height(14.dp))

                AspectRatioOption.entries.forEach { ratio ->
                    val selected = ratio == uiState.aspectRatio
                    Surface(
                        onClick = {
                            viewModel.setAspectRatio(ratio)
                            showRatioDialogSheet = false
                        },
                        shape = RoundedCornerShape(14.dp),
                        color = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = ratio.label,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                Text(
                                    text = ratio.description,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (ratio.isCinematicGuide) GodcamAmberGold.copy(alpha = 0.25f)
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (ratio.isCinematicGuide) "Guide / Crop" else "Sensor / Crop",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    // Pro Controls & Advanced Capture Tools Sheet ("More" / "•••")
    if (showMoreToolsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMoreToolsSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            ProAndAdvancedControlsSheet(
                uiState = uiState,
                viewModel = viewModel,
                onDismiss = { showMoreToolsSheet = false }
            )
        }
    }
}

@Composable
private fun CameraTopBar(
    uiState: CameraUiState,
    onOpenMoreSheet: () -> Unit,
    onToggleProMode: () -> Unit,
    onCycleFlash: () -> Unit,
    onSwitchLens: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val flashIcon = when (uiState.flashMode) {
        FlashModeOption.OFF -> Icons.Outlined.FlashOff
        FlashModeOption.AUTO -> Icons.Outlined.FlashAuto
        FlashModeOption.ON -> Icons.Outlined.FlashOn
        FlashModeOption.TORCH -> Icons.Outlined.Bolt
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (uiState.captureMode.isPro) {
            // Matches Screen 4: Green "Pro Video" / "Pro Photo" pill on top-left
            Surface(
                onClick = onToggleProMode,
                shape = RoundedCornerShape(50),
                color = GodcamForestActive,
                contentColor = Color(0xFFFCFBF7),
                modifier = Modifier
                    .height(38.dp)
                    .testTag("top_pro_mode_pill")
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(horizontal = 18.dp)
                ) {
                    Text(
                        text = uiState.captureMode.label,
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = Color(0xFFFCFBF7),
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        } else {
            // Matches Screen 2: Circular "•••" button on top-left
            CircularIconControl(
                icon = Icons.Outlined.MoreHoriz,
                contentDescription = "Pro & Manual Controls",
                onClick = onOpenMoreSheet,
                testTag = "top_more_button"
            )

            // Center Capture Profile Pill: "4K | 30" (Matches Screen 2 of reference image)
            Surface(
                onClick = onToggleProMode,
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                modifier = Modifier
                    .height(36.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(50))
                    .testTag("top_profile_pill")
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    val badgeText = if (uiState.captureMode == CaptureMode.NIGHT) {
                        "NIGHT • MULTI-FRAME"
                    } else {
                        "${uiState.videoResolution.shortLabel} | ${uiState.videoFps.fps}"
                    }
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = JetBrainsMonoFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
        }

        // Right Controls: Flash/Torch, (Camera Switch in Pro mode), Settings
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularIconControl(
                icon = flashIcon,
                contentDescription = "Flash Mode: ${uiState.flashMode.label}",
                isSelected = uiState.flashMode != FlashModeOption.OFF,
                onClick = onCycleFlash,
                testTag = "top_flash_button"
            )
            if (uiState.captureMode.isPro) {
                CircularIconControl(
                    icon = Icons.Outlined.Cameraswitch,
                    contentDescription = "Switch Lens",
                    onClick = onSwitchLens,
                    testTag = "top_lens_switch_button"
                )
            }
            CircularIconControl(
                icon = Icons.Outlined.Settings,
                contentDescription = "Open Settings",
                onClick = onOpenSettings,
                testTag = "top_settings_button"
            )
        }
    }
}

@Composable
private fun ProVideoSlidersDeck(
    uiState: CameraUiState,
    viewModel: GodcamViewModel
) {
    val resList = uiState.capabilities.supportedVideoResolutions
    val resIdx = resList.indexOf(uiState.videoResolution).coerceAtLeast(0).toFloat()

    val validFpsList = uiState.capabilities.validFpsFor(uiState.videoResolution, uiState.videoCodec)
    val fpsIdx = validFpsList.indexOf(uiState.videoFps).coerceAtLeast(0).toFloat()

    val maxBitrate = uiState.capabilities.maxBitrateMbpsFor(uiState.videoResolution, uiState.videoCodec).toFloat()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // 1. Resolution Slider ("4K (3840×2160)")
        LabeledCameraSlider(
            label = "Resolution",
            valueText = uiState.videoResolution.fullLabel,
            value = resIdx,
            valueRange = 0f..(resList.size - 1).coerceAtLeast(1).toFloat(),
            steps = (resList.size - 2).coerceAtLeast(0),
            onValueChange = { v ->
                val chosen = resList[v.roundToInt().coerceIn(0, resList.lastIndex)]
                if (chosen != uiState.videoResolution) viewModel.setVideoResolution(chosen)
            },
            testTag = "pro_video_resolution_slider"
        )

        // 2. Frame Rate Slider ("60 fps")
        LabeledCameraSlider(
            label = "Frame Rate",
            valueText = uiState.videoFps.label,
            value = fpsIdx,
            valueRange = 0f..(validFpsList.size - 1).coerceAtLeast(1).toFloat(),
            steps = (validFpsList.size - 2).coerceAtLeast(0),
            onValueChange = { v ->
                val chosen = validFpsList[v.roundToInt().coerceIn(0, validFpsList.lastIndex)]
                if (chosen != uiState.videoFps) viewModel.setVideoFps(chosen)
            },
            testTag = "pro_video_fps_slider"
        )

        // 3. Bitrate Slider ("100 Mbps")
        LabeledCameraSlider(
            label = "Bitrate",
            valueText = "${uiState.videoBitrateMbps} Mbps",
            value = uiState.videoBitrateMbps.toFloat(),
            valueRange = 12f..maxBitrate,
            onValueChange = { v ->
                viewModel.setVideoBitrateMbps(v.roundToInt())
            },
            testTag = "pro_video_bitrate_slider"
        )
    }
}

@Composable
private fun ProPhotoSlidersDeck(
    uiState: CameraUiState,
    viewModel: GodcamViewModel
) {
    val isoRange = uiState.capabilities.isoRange
    val shutterChoices = listOf(15, 30, 60, 125, 250, 500, 1000, 2000, 4000)
    val shutterIdx = shutterChoices.indexOfFirst { it >= uiState.shutterDenominator }.let { if (it < 0) 4 else it }.toFloat()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        LabeledCameraSlider(
            label = "ISO Sensitivity",
            valueText = if (uiState.isAutoIso) "Auto (${uiState.manualIso})" else "ISO ${uiState.manualIso}",
            value = uiState.manualIso.toFloat(),
            valueRange = isoRange.first.toFloat()..isoRange.last.toFloat(),
            onValueChange = { viewModel.setManualIso(it.roundToInt()) },
            onReset = { viewModel.setManualIso(null) },
            testTag = "pro_photo_iso_slider"
        )

        LabeledCameraSlider(
            label = "Shutter Speed",
            valueText = if (uiState.isAutoShutter) "Auto (1/250s)" else "1/${uiState.shutterDenominator}s",
            value = shutterIdx,
            valueRange = 0f..(shutterChoices.size - 1).toFloat(),
            steps = shutterChoices.size - 2,
            onValueChange = { idx ->
                val denom = shutterChoices[idx.roundToInt().coerceIn(0, shutterChoices.lastIndex)]
                viewModel.setShutterDenominator(denom)
            },
            onReset = { viewModel.setShutterDenominator(null) },
            testTag = "pro_photo_shutter_slider"
        )

        LabeledCameraSlider(
            label = "Exposure (EV)",
            valueText = "${if (uiState.exposureEv > 0) "+" else ""}${uiState.exposureEv} EV",
            value = uiState.exposureEv,
            valueRange = uiState.capabilities.evRange,
            onValueChange = { viewModel.setExposureEv(it) },
            onReset = { viewModel.setExposureEv(0f) },
            testTag = "pro_photo_ev_slider"
        )
    }
}

@Composable
private fun QuickToolItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: String? = null,
    isActive: Boolean = false,
    testTag: String = "quick_tool"
) {
    val tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .testTag(testTag)
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(22.dp)
            )
            if (badge != null) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(start = 14.dp)
                ) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = tint,
                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium
            )
        )
    }
}

@Composable
private fun ProAndAdvancedControlsSheet(
    uiState: CameraUiState,
    viewModel: GodcamViewModel,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Camera & Pro Studio Controls",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Hardware-validated settings • Storage Free: ${uiState.capabilities.availableStorageMb / 1024} GB",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
            Surface(
                onClick = { viewModel.cycleViewfinderScene() },
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    text = "Cycle Scene",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        // 1. Capture Mode Selector Chips (Photo, Video, Pro Photo, Pro Video, Night)
        Text("Capture Mode", style = MaterialTheme.typography.titleSmall)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CaptureMode.entries.forEach { mode ->
                val selected = uiState.captureMode == mode
                FilterChip(
                    selected = selected,
                    onClick = { viewModel.setCaptureMode(mode) },
                    label = { Text(mode.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }

        // 2. Photo Format (RAW / JPEG / HEIF)
        Text("Photo Capture Format", style = MaterialTheme.typography.titleSmall)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PhotoFormat.entries.forEach { fmt ->
                val selected = uiState.photoFormat == fmt
                FilterChip(
                    selected = selected,
                    onClick = { viewModel.setPhotoFormat(fmt) },
                    label = { Text(fmt.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }

        // 3. White Balance Presets & Kelvin
        Text("White Balance (${uiState.whiteBalance.label} • ${uiState.manualKelvin}K)", style = MaterialTheme.typography.titleSmall)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WhiteBalancePreset.entries.forEach { wb ->
                FilterChip(
                    selected = uiState.whiteBalance == wb,
                    onClick = { viewModel.setWhiteBalance(wb) },
                    label = { Text(wb.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
        if (uiState.whiteBalance == WhiteBalancePreset.MANUAL_KELVIN) {
            LabeledCameraSlider(
                label = "Color Temperature",
                valueText = "${uiState.manualKelvin}K",
                value = uiState.manualKelvin.toFloat(),
                valueRange = 2500f..9000f,
                onValueChange = { viewModel.setWhiteBalance(WhiteBalancePreset.MANUAL_KELVIN, it.roundToInt()) },
                onReset = { viewModel.setWhiteBalance(WhiteBalancePreset.AUTO) }
            )
        }

        // 4. Manual Focus Slider (where supported)
        LabeledCameraSlider(
            label = "Optical Focus Distance",
            valueText = if (uiState.focusMode.name.startsWith("MANUAL")) {
                "${(uiState.manualFocusDistance * 100).roundToInt()}% Macro"
            } else "Auto AF-C",
            value = uiState.manualFocusDistance,
            valueRange = 0f..1f,
            onValueChange = { viewModel.setManualFocusDistance(it) },
            onReset = { viewModel.setManualFocusDistance(null) }
        )

        // 5. Video Stabilization & Audio Controls
        Text("Video Stabilization & Audio", style = MaterialTheme.typography.titleSmall)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StabilizationMode.entries.forEach { stab ->
                FilterChip(
                    selected = uiState.stabilizationMode == stab,
                    onClick = { viewModel.setStabilization(stab) },
                    label = { Text(stab.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
            FilterChip(
                selected = !uiState.audioMuted,
                onClick = { viewModel.toggleAudioMute() },
                leadingIcon = {
                    Icon(
                        imageVector = if (uiState.audioMuted) Icons.Outlined.MicOff else Icons.Outlined.Mic,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                label = { Text(if (uiState.audioMuted) "Audio Muted" else "Stereo Mic On") }
            )
        }

        // 6. Viewfinder Assist Overlays (Histogram, Focus Peaking, Zebra, AE/AF Lock)
        Text("Viewfinder Assist Overlays (Not burned into capture)", style = MaterialTheme.typography.titleSmall)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = uiState.showHistogram,
                onClick = { viewModel.toggleHistogram() },
                label = { Text("Live Histogram") }
            )
            FilterChip(
                selected = uiState.showFocusPeaking,
                onClick = { viewModel.toggleFocusPeaking() },
                label = { Text("Focus Peaking") }
            )
            FilterChip(
                selected = uiState.showZebraStripes,
                onClick = { viewModel.toggleZebraStripes() },
                label = { Text("Zebra Clipping") }
            )
            FilterChip(
                selected = uiState.aeAfLocked,
                onClick = { viewModel.toggleAeAfLock() },
                label = { Text("AE/AF Lock") }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}
