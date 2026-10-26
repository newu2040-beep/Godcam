package com.example.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.ColorMatrixColorFilter
import android.graphics.RenderEffect
import android.os.Build
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.MicOff
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Settings
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
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
    val coroutineScope = rememberCoroutineScope()

    var showMoreToolsSheet by remember { mutableStateOf(false) }
    var showRatioDialogSheet by remember { mutableStateOf(false) }
    var showExposurePopup by remember { mutableStateOf(false) }

    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }
    var boundCameraRef by remember { mutableStateOf<Camera?>(null) }
    var imageCaptureRef by remember { mutableStateOf<ImageCapture?>(null) }
    var videoCaptureRef by remember { mutableStateOf<VideoCapture<Recorder>?>(null) }
    var activeRecordingRef by remember { mutableStateOf<Recording?>(null) }
    var activeRecordingFile by remember { mutableStateOf<File?>(null) }
    var liveHistogramBins by remember { mutableStateOf(List(24) { 0f }) }

    val activePreset = remember(uiState.activeRecipe.presetId) {
        FilterCatalog.getById(uiState.activeRecipe.presetId)
    }

    // Real-time Android ColorMatrix for GPU RenderEffect on the live PreviewView
    val liveAndroidColorMatrix = remember(activePreset, uiState.activeRecipe, uiState.exposureEv) {
        ImageFilterEngine.buildAndroidColorMatrix(
            preset = activePreset,
            recipe = uiState.activeRecipe,
            extraEv = uiState.exposureEv
        )
    }

    val recentThumbBitmap = remember(recentMedia?.id, recentMedia?.filePath, recentMedia?.uriString) {
        ImageFilterEngine.loadBitmapForMediaItem(context, recentMedia, 240, 240)
    }

    // Periodically sample the real live camera frame for the real-time Luminance Histogram & Filters screen preview
    LaunchedEffect(uiState.cameraPermissionGranted, previewViewRef, uiState.showHistogram, uiState.captureMode) {
        while (uiState.cameraPermissionGranted) {
            delay(450)
            val bmp = runCatching { previewViewRef?.bitmap }.getOrNull()
            if (bmp != null) {
                viewModel.updateLatestCameraFrame(bmp)
                if (uiState.showHistogram || uiState.captureMode == CaptureMode.PRO_PHOTO || uiState.captureMode == CaptureMode.PRO_VIDEO) {
                    liveHistogramBins = ImageFilterEngine.computeLumaHistogram(bmp)
                }
            }
        }
    }

    // Bind real CameraX Preview + ImageCapture + VideoCapture<Recorder> at the user's selected resolution
    DisposableEffect(
        uiState.cameraPermissionGranted,
        uiState.isFrontCamera,
        uiState.videoResolution,
        previewViewRef
    ) {
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

                    val imageCapture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build()

                    // Map selected VideoResolution to CameraX QualitySelector
                    val targetQuality = when (uiState.videoResolution) {
                        VideoResolution.SD_480P -> Quality.SD
                        VideoResolution.HD_720P -> Quality.HD
                        VideoResolution.FHD_1080P -> Quality.FHD
                        VideoResolution.UHD_4K, VideoResolution.DCI_4K, VideoResolution.UHD_8K -> Quality.UHD
                    }
                    val qualitySelector = QualitySelector.from(
                        targetQuality,
                        FallbackStrategy.lowerQualityOrHigherThan(targetQuality)
                    )
                    val recorder = Recorder.Builder()
                        .setQualitySelector(qualitySelector)
                        .build()
                    val videoCapture = VideoCapture.withOutput(recorder)

                    val selector = if (uiState.isFrontCamera) {
                        CameraSelector.DEFAULT_FRONT_CAMERA
                    } else {
                        CameraSelector.DEFAULT_BACK_CAMERA
                    }

                    provider.unbindAll()

                    val camera = try {
                        provider.bindToLifecycle(
                            lifecycleOwner,
                            selector,
                            preview,
                            imageCapture,
                            videoCapture
                        )
                    } catch (_: Exception) {
                        // Fallback if device hardware level only supports 2 simultaneous surfaces
                        try {
                            if (uiState.captureMode.isVideo) {
                                provider.bindToLifecycle(lifecycleOwner, selector, preview, videoCapture)
                            } else {
                                provider.bindToLifecycle(lifecycleOwner, selector, preview, imageCapture)
                            }
                        } catch (_: Exception) {
                            provider.bindToLifecycle(lifecycleOwner, selector, preview)
                        }
                    }

                    boundCameraRef = camera
                    imageCaptureRef = imageCapture
                    videoCaptureRef = videoCapture

                    val maxZ = camera.cameraInfo.zoomState.value?.maxZoomRatio ?: uiState.capabilities.maxZoom
                    val minZ = camera.cameraInfo.zoomState.value?.minZoomRatio ?: uiState.capabilities.minZoom
                    camera.cameraControl.enableTorch(uiState.flashMode == FlashModeOption.TORCH)
                    camera.cameraControl.setZoomRatio(uiState.activeZoom.coerceIn(minZ, maxZ))
                }
            }, executor)
        }
        onDispose { }
    }

    // Dynamically update Torch, Optical Zoom, and Exposure Compensation on the bound CameraX instance
    LaunchedEffect(uiState.flashMode, uiState.activeZoom, uiState.exposureEv, boundCameraRef) {
        val cam = boundCameraRef ?: return@LaunchedEffect
        runCatching {
            cam.cameraControl.enableTorch(uiState.flashMode == FlashModeOption.TORCH)
            val zoomState = cam.cameraInfo.zoomState.value
            val minZ = zoomState?.minZoomRatio ?: 1f
            val maxZ = zoomState?.maxZoomRatio ?: 8f
            cam.cameraControl.setZoomRatio(uiState.activeZoom.coerceIn(minZ, maxZ))

            val evState = cam.cameraInfo.exposureState
            if (evState.isExposureCompensationSupported) {
                val step = evState.exposureCompensationStep.toFloat().coerceAtLeast(0.1f)
                val index = (uiState.exposureEv / step).roundToInt()
                    .coerceIn(evState.exposureCompensationRange.lower, evState.exposureCompensationRange.upper)
                cam.cameraControl.setExposureCompensationIndex(index)
            }
        }
    }

    // Helper to trigger photo capture (with optional timer) and bake the active filter into the saved photo
    val triggerPhotoCapture: () -> Unit = {
        val captureNow: () -> Unit = {
            val liveBmp = runCatching { previewViewRef?.bitmap }.getOrNull()
            if (liveBmp != null) {
                viewModel.processAndSaveCapturedPhoto(liveBmp)
            } else {
                val imgCap = imageCaptureRef
                if (imgCap != null) {
                    imgCap.takePicture(
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageCapturedCallback() {
                            override fun onCaptureSuccess(image: ImageProxy) {
                                val decoded = runCatching { image.toBitmap() }.getOrNull()
                                image.close()
                                viewModel.processAndSaveCapturedPhoto(decoded)
                            }

                            override fun onError(exception: ImageCaptureException) {
                                viewModel.processAndSaveCapturedPhoto(runCatching { previewViewRef?.bitmap }.getOrNull())
                            }
                        }
                    )
                } else {
                    viewModel.processAndSaveCapturedPhoto(null)
                }
            }
        }

        if (uiState.timerOption.seconds > 0) {
            coroutineScope.launch {
                val finished = viewModel.runCountdownTimerIfNeeded()
                if (finished) captureNow()
            }
        } else {
            captureNow()
        }
    }

    // Helper to start/stop real video recording at the user's selected resolution + active filter
    @SuppressLint("MissingPermission")
    val triggerVideoToggle: () -> Unit = {
        if (uiState.isRecordingVideo) {
            val currentRec = activeRecordingRef
            val recordedFile = activeRecordingFile
            val currentFrame = runCatching { previewViewRef?.bitmap }.getOrNull()
            activeRecordingRef = null
            if (currentRec != null) {
                runCatching { currentRec.stop() }
                // Finalize callback will save, or if stop doesn't fire immediately, safeguard save:
                coroutineScope.launch {
                    delay(350)
                    if (uiState.isRecordingVideo) {
                        viewModel.finalizeAndAutoSaveVideo(recordedFile, currentFrame)
                    }
                }
            } else {
                viewModel.finalizeAndAutoSaveVideo(recordedFile, currentFrame)
            }
        } else {
            val vidCap = videoCaptureRef
            val mediaDir = File(context.filesDir, "godcam_media").apply { mkdirs() }
            val rawMp4File = File(
                mediaDir,
                "GODCAM_RAW_${uiState.videoResolution.shortLabel}_${System.currentTimeMillis()}.mp4"
            )
            activeRecordingFile = rawMp4File
            viewModel.startVideoRecordingTimer()

            if (vidCap != null && uiState.cameraPermissionGranted) {
                runCatching {
                    val outputOptions = FileOutputOptions.Builder(rawMp4File).build()
                    var pending = vidCap.output.prepareRecording(context, outputOptions)
                    if (uiState.micPermissionGranted && !uiState.audioMuted) {
                        pending = pending.withAudioEnabled()
                    }
                    val snapshotBeforeStop = runCatching { previewViewRef?.bitmap }.getOrNull()
                    activeRecordingRef = pending.start(ContextCompat.getMainExecutor(context)) { event ->
                        if (event is VideoRecordEvent.Finalize) {
                            activeRecordingRef = null
                            val latestBmp = runCatching { previewViewRef?.bitmap }.getOrNull() ?: snapshotBeforeStop
                            // If a film filter is active in Video mode, pass null for existingMp4File so
                            // MediaGallerySaver encodes the MP4 with the filter baked in at the exact selected resolution!
                            // If Original (no filter) is active and CameraX succeeded without error, use the CameraX MP4.
                            val usableCameraXMp4 = if (
                                !event.hasError() &&
                                rawMp4File.exists() &&
                                rawMp4File.length() > 1024L &&
                                uiState.activeRecipe.presetId == "original"
                            ) {
                                rawMp4File
                            } else {
                                null
                            }
                            viewModel.finalizeAndAutoSaveVideo(
                                existingMp4File = usableCameraXMp4,
                                livePreviewBitmap = latestBmp
                            )
                        }
                    }
                }.onFailure {
                    activeRecordingRef = null
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
            // 1. BOLD MINIMALIST TOP CAMERA CONTROL BAR
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

            // 2. HERO VIEWFINDER CARD (Live CameraX + Real-Time GPU Filter Matrix + Hardware Telemetry)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFF0E1512))
                    .border(
                        width = 1.2.dp,
                        color = if (uiState.isRecordingVideo) GodcamRecordRed else MaterialTheme.colorScheme.outline.copy(alpha = 0.65f),
                        shape = RoundedCornerShape(22.dp)
                    )
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { offset ->
                                val nx = (offset.x / size.width.coerceAtLeast(1)).coerceIn(0.08f, 0.92f)
                                val ny = (offset.y / size.height.coerceAtLeast(1)).coerceIn(0.08f, 0.92f)
                                viewModel.onTapToFocus(nx, ny)
                                showExposurePopup = true
                                // Trigger real CameraX FocusMeteringAction on the tapped point
                                val view = previewViewRef
                                val cam = boundCameraRef
                                if (view != null && cam != null) {
                                    runCatching {
                                        val point = view.meteringPointFactory.createPoint(offset.x, offset.y)
                                        val action = FocusMeteringAction.Builder(point).build()
                                        cam.cameraControl.startFocusAndMetering(action)
                                    }
                                }
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
                if (uiState.cameraPermissionGranted) {
                    AndroidView(
                        factory = { ctx ->
                            PreviewView(ctx).apply {
                                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                                scaleType = PreviewView.ScaleType.FILL_CENTER
                                previewViewRef = this
                            }
                        },
                        update = { view ->
                            // Apply the active film filter + EV to the live CameraX viewfinder in BOTH Photo and Video modes
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                val isNeutral = uiState.activeRecipe.presetId == "original" &&
                                    uiState.activeRecipe.warmthOffset == 0f &&
                                    uiState.activeRecipe.contrastOffset == 0f &&
                                    uiState.activeRecipe.fadeOffset == 0f &&
                                    uiState.exposureEv == 0f
                                if (isNeutral) {
                                    view.setRenderEffect(null)
                                } else {
                                    view.setRenderEffect(
                                        RenderEffect.createColorFilterEffect(
                                            ColorMatrixColorFilter(liveAndroidColorMatrix)
                                        )
                                    )
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Real-time Vignette & Film Atmosphere overlay when an analog look is active
                    val vigStrength = ((activePreset.vignette + uiState.activeRecipe.vignetteOffset) * uiState.activeRecipe.intensity)
                        .coerceIn(0f, 0.65f)
                    if (vigStrength > 0.02f) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val radius = size.maxDimension * 0.65f
                            drawRect(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = vigStrength * 0.78f)
                                    ),
                                    center = Offset(size.width / 2f, size.height / 2f),
                                    radius = radius
                                )
                            )
                        }
                    }
                } else {
                    // Minimalist Hardware Standby State when camera permission is not yet granted
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Memory,
                            contentDescription = null,
                            tint = GodcamMintAccent,
                            modifier = Modifier.size(42.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "OPTICAL SENSOR STANDBY",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = JetBrainsMonoFamily,
                                color = Color(0xFFFCFBF7),
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.4.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Lens #${uiState.capabilities.activeCameraId} • ${uiState.capabilities.focalLengthMm}mm f/${uiState.capabilities.apertureF} • ${uiState.capabilities.hardwareLevelLabel}",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = JetBrainsMonoFamily,
                                color = GodcamMintAccent
                            )
                        )
                    }
                }

                // Grid, Golden Ratio, Real-Time Accelerometer Horizon Level, Cinematic Guide & Focus Reticle
                ViewfinderCompositionOverlays(
                    gridMode = uiState.gridMode,
                    aspectRatio = uiState.aspectRatio,
                    focusPoint = uiState.focusPointNormalized,
                    aeAfLocked = uiState.aeAfLocked,
                    showFocusPeaking = uiState.showFocusPeaking,
                    showZebraStripes = uiState.showZebraStripes,
                    deviceTiltDegrees = uiState.deviceTiltDegrees
                )

                // Top-left Viewfinder Badges (Format + Active Film Filter Badge for Photo & Video + AE/AF Lock)
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
                            color = Color(0xBB101915),
                            contentColor = Color(0xFFFCFBF7),
                            modifier = Modifier.border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(50))
                        ) {
                            Text(
                                text = uiState.photoFormat.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = JetBrainsMonoFamily,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.6.sp
                                ),
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp)
                            )
                        }
                    }

                    // Active Filter Chip in Viewfinder (Works in BOTH Photo & Video modes!)
                    Surface(
                        onClick = { viewModel.toggleLiveFilterStrip() },
                        shape = RoundedCornerShape(50),
                        color = if (uiState.activeRecipe.presetId != "original") GodcamForestActive.copy(alpha = 0.92f) else Color(0xBB101915),
                        contentColor = if (uiState.activeRecipe.presetId != "original") Color(0xFFFCFBF7) else GodcamMintAccent,
                        modifier = Modifier
                            .border(1.dp, GodcamMintAccent.copy(alpha = 0.4f), RoundedCornerShape(50))
                            .testTag("viewfinder_active_filter_chip")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.FilterBAndW,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (uiState.activeRecipe.presetId == "original") {
                                    "Look: Original"
                                } else {
                                    "${activePreset.name} ${(uiState.activeRecipe.intensity * 100).roundToInt()}%"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    if (uiState.aeAfLocked) {
                        Surface(
                            onClick = { viewModel.toggleAeAfLock() },
                            shape = RoundedCornerShape(50),
                            color = GodcamAmberGold,
                            contentColor = Color(0xFF141E1A)
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
                                    text = "LOCK",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }

                // Top-Center Video / Pro Telemetry Pill ("4K · 60fps · 100Mbps")
                if (uiState.captureMode.isVideo || isProPhotoStyle) {
                    val telemetryText = if (uiState.captureMode.isVideo) {
                        val recPrefix = if (uiState.isRecordingVideo) {
                            val mins = uiState.recordingElapsedSec / 60
                            val secs = uiState.recordingElapsedSec % 60
                            String.format(Locale.US, "REC %02d:%02d • ", mins, secs)
                        } else ""
                        "$recPrefix${uiState.videoResolution.shortLabel} (${uiState.videoResolution.width}×${uiState.videoResolution.height}) · ${uiState.videoFps.fps}fps"
                    } else {
                        val isoVal = if (uiState.isAutoIso) "ISO Auto" else "ISO ${uiState.manualIso}"
                        val shutVal = if (uiState.isAutoShutter) "Auto S" else "1/${uiState.shutterDenominator}s"
                        "$isoVal · $shutVal · ${uiState.whiteBalance.label}"
                    }

                    Surface(
                        onClick = {
                            if (uiState.isRecordingVideo) {
                                triggerVideoToggle()
                            }
                        },
                        shape = RoundedCornerShape(50),
                        color = if (uiState.isRecordingVideo) GodcamRecordRed else Color(0xCC101915),
                        contentColor = Color(0xFFFCFBF7),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 44.dp)
                    ) {
                        Text(
                            text = if (uiState.isRecordingVideo) "$telemetryText • STOP" else telemetryText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = JetBrainsMonoFamily,
                                color = Color(0xFFFCFBF7),
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
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
                            color = Color(0xCC101915),
                            contentColor = GodcamAmberGold
                        ) {
                            Text(
                                text = "${uiState.aspectRatio.label} GUIDE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = JetBrainsMonoFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                    if (uiState.showHistogram || isProPhotoStyle || isProVideoStyle) {
                        LiveHistogramOverlay(bins = liveHistogramBins)
                    }
                }

                // Real-time Hardware Telemetry Strip above the Zoom pill (Zero fake data — live Camera2, SensorManager & BatteryManager)
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0x990E1613),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 12.dp, bottom = 52.dp)
                ) {
                    val caps = uiState.capabilities
                    val freeGb = String.format(Locale.US, "%.1fGB", caps.availableStorageMb / 1024f)
                    val tiltStr = String.format(Locale.US, "%+.1f°", uiState.deviceTiltDegrees)
                    Text(
                        text = "ID:${caps.activeCameraId} ${caps.focalLengthMm}mm f/${caps.apertureF} • $tiltStr • ${caps.batteryTempCelsius}°C • $freeGb",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 9.5.sp,
                            color = Color(0xFFDCE7E1),
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                // Self-Timer Full-Viewfinder Countdown Overlay
                if (uiState.countdownRemainingSec > 0) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x880D1512))
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${uiState.countdownRemainingSec}",
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontSize = 74.sp,
                                    fontFamily = JetBrainsMonoFamily,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "Self-Timer Active",
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.88f))
                            )
                        }
                    }
                }

                // Contextual Permission Banner if any core permission is missing
                if (!uiState.allCorePermissionsGranted) {
                    Surface(
                        onClick = {
                            onRequestPermissions()
                            viewModel.setShowPermissionsDialog(true)
                        },
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xDD14201C),
                        contentColor = Color(0xFFFCFBF7),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 24.dp)
                            .border(1.dp, GodcamMintAccent.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .testTag("enable_live_camera_pill")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AutoAwesome,
                                contentDescription = null,
                                tint = GodcamMintAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Column(modifier = Modifier.weight(1f, fill = false)) {
                                Text(
                                    text = "Grant Camera, Mic & Gallery Access",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        color = Color(0xFFFCFBF7),
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "Tap to enable real-time sensor & auto-save to DCIM/GODCAM",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFD0DDD6))
                                )
                            }
                        }
                    }
                }

                // Bottom of Viewfinder: Hardware-Validated Optical Zoom Stops & EV Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xBB141E1A),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .height(36.dp)
                            .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(50))
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
                                        .background(if (isSelected) GodcamForestActive else Color.Transparent)
                                        .then(
                                            if (isSelected) Modifier.border(1.dp, GodcamMintAccent, CircleShape)
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
                                            color = if (isSelected) Color(0xFFFCFBF7) else Color(0xFFE6ECE9)
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Bottom-Right EV Compensation Quick Toggle inside Viewfinder
                    Surface(
                        onClick = { showExposurePopup = !showExposurePopup },
                        shape = CircleShape,
                        color = if (showExposurePopup || uiState.exposureEv != 0f) GodcamForestActive else Color(0xBB141E1A),
                        contentColor = Color(0xFFFCFBF7),
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(34.dp)
                            .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape)
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
                if (uiState.shutterFlashVisible) {
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
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontWeight = FontWeight.Medium
                                ),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = if (msg.contains("Gallery", ignoreCase = true)) "Open Gallery" else "Dismiss",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }

            // Quick Inline Live Filter Strip (Works in BOTH Photo AND Video/Pro Video modes!)
            AnimatedVisibility(visible = uiState.showLiveFilterStrip) {
                LiveFilterSelectorRibbon(
                    uiState = uiState,
                    onSelectPreset = { viewModel.selectPreset(it) },
                    onOpenFullLibrary = { viewModel.navigateTo(AppDestination.FILTERS) }
                )
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

            // 3. MODE SELECTOR PILL ("Photo | Video" and "Video | Pro Video")
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.captureMode == CaptureMode.VIDEO || uiState.captureMode == CaptureMode.PRO_VIDEO) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            onClick = { viewModel.setCaptureMode(CaptureMode.PHOTO) },
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .height(36.dp)
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(50))
                                .testTag("return_to_photo_mode_pill")
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 14.dp)
                            ) {
                                Text(
                                    text = "Photo",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                        SegmentedPillSelector(
                            options = listOf(CaptureMode.VIDEO, CaptureMode.PRO_VIDEO),
                            selectedOption = uiState.captureMode,
                            labelProvider = { it.label },
                            onOptionSelected = { viewModel.setCaptureMode(it) },
                            testTagPrefix = "mode_pill"
                        )
                    }
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

            // 4. PRO VIDEO / PRO PHOTO SLIDERS DECK
            AnimatedVisibility(visible = isProVideoStyle || isProPhotoStyle) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .border(1.2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f), RoundedCornerShape(18.dp))
                ) {
                    if (isProVideoStyle) {
                        ProVideoSlidersDeck(uiState = uiState, viewModel = viewModel)
                    } else {
                        ProPhotoSlidersDeck(uiState = uiState, viewModel = viewModel)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 5. PRIMARY SHUTTER ROW (Recent Real Capture Thumbnail • Tactile Shutter • Lens Switch)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Recent Media Circular Thumbnail -> Opens Gallery (No fake placeholder thumbnail!)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.8.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        .clickable { viewModel.navigateTo(AppDestination.GALLERY) }
                        .testTag("recent_media_thumbnail")
                ) {
                    if (recentThumbBitmap != null) {
                        Image(
                            bitmap = recentThumbBitmap.asImageBitmap(),
                            contentDescription = "Open Gallery",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Collections,
                            contentDescription = "Open Gallery",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Large Tactile Shutter / Record Control
                GodcamShutterButton(
                    isVideoMode = uiState.captureMode.isVideo,
                    isRecording = uiState.isRecordingVideo,
                    onClick = {
                        if (uiState.captureMode.isVideo) {
                            triggerVideoToggle()
                        } else {
                            triggerPhotoCapture()
                        }
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

            // 6. BOTTOM QUICK TOOL DOCK (Includes Filter in BOTH Photo and Video / Pro Video modes!)
            if (isProVideoStyle) {
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
                        badge = if (uiState.activeRecipe.presetId != "original") activePreset.name.take(5) else null,
                        isActive = uiState.activeRecipe.presetId != "original" || uiState.showLiveFilterStrip,
                        onClick = { viewModel.toggleLiveFilterStrip() },
                        testTag = "dock_filter"
                    )
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
                        badge = if (uiState.activeRecipe.presetId != "original") activePreset.name.take(6) else null,
                        isActive = uiState.activeRecipe.presetId != "original",
                        onClick = { viewModel.navigateTo(AppDestination.FILTERS) },
                        testTag = "dock_filter"
                    )
                    if (uiState.captureMode.isVideo) {
                        QuickToolItem(
                            icon = Icons.Outlined.HighQuality,
                            label = "Res",
                            badge = uiState.videoResolution.shortLabel,
                            isActive = true,
                            onClick = {
                                val list = uiState.capabilities.supportedVideoResolutions
                                val next = list[(list.indexOf(uiState.videoResolution) + 1) % list.size]
                                viewModel.setVideoResolution(next)
                            },
                            testTag = "dock_resolution"
                        )
                    } else {
                        QuickToolItem(
                            icon = Icons.Outlined.AspectRatio,
                            label = "Ratio",
                            badge = uiState.aspectRatio.label,
                            onClick = { showRatioDialogSheet = true },
                            testTag = "dock_ratio"
                        )
                    }
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
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
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

    // Pro Controls & Real-Time Hardware Inspector Sheet ("More" / "•••")
    if (showMoreToolsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMoreToolsSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            ProAndAdvancedControlsSheet(
                uiState = uiState,
                viewModel = viewModel,
                onOpenFullFilters = {
                    showMoreToolsSheet = false
                    viewModel.navigateTo(AppDestination.FILTERS)
                }
            )
        }
    }
}

/**
 * Horizontal Live Filter Selector Strip usable in both Photo and Video / Pro Video modes.
 */
@Composable
private fun LiveFilterSelectorRibbon(
    uiState: CameraUiState,
    onSelectPreset: (com.example.model.FilterPreset) -> Unit,
    onOpenFullLibrary: () -> Unit
) {
    val quickPresets = remember {
        val ids = listOf(
            "original",
            "fuji_pro_400h",
            "kodak_gold_200",
            "kodak_portra_400",
            "cine_still_800t",
            "retro_agfa_vista",
            "bw_classic",
            "fuji_velvia_50",
            "retro_classic",
            "night_cyber_emerald"
        )
        ids.mapNotNull { id -> FilterCatalog.allPresets.firstOrNull { it.id == id } }
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f), RoundedCornerShape(16.dp))
            .testTag("live_filter_ribbon")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(end = 8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(quickPresets, key = { it.id }) { preset ->
                    val selected = uiState.activeRecipe.presetId == preset.id
                    Surface(
                        onClick = { onSelectPreset(preset) },
                        shape = RoundedCornerShape(50),
                        color = if (selected) GodcamForestActive else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (selected) Color(0xFFFCFBF7) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .height(32.dp)
                            .border(
                                width = 1.dp,
                                color = if (selected) GodcamMintAccent else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(50)
                            )
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        ) {
                            Text(
                                text = preset.name,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        }
                    }
                }
            }

            Surface(
                onClick = onOpenFullLibrary,
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.height(32.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    Text(
                        text = "All 108",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )
                }
            }
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
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        } else {
            CircularIconControl(
                icon = Icons.Outlined.MoreHoriz,
                contentDescription = "Pro & Manual Controls",
                onClick = onOpenMoreSheet,
                testTag = "top_more_button"
            )

            // Center Capture Profile Pill: cycles resolution or toggles Pro Mode
            Surface(
                onClick = onToggleProMode,
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                modifier = Modifier
                    .height(36.dp)
                    .border(1.2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), RoundedCornerShape(50))
                    .testTag("top_profile_pill")
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    val badgeText = if (uiState.captureMode == CaptureMode.NIGHT) {
                        "NIGHT • LOW LIGHT"
                    } else {
                        "${uiState.videoResolution.shortLabel} | ${uiState.videoFps.fps}FPS"
                    }
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = JetBrainsMonoFamily,
                            fontWeight = FontWeight.Bold,
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
        // 1. Resolution Slider
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

        // 2. Frame Rate Slider
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

        // 3. Bitrate Slider
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
            .padding(horizontal = 8.dp, vertical = 4.dp)
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
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
            )
        )
    }
}

@Composable
private fun ProAndAdvancedControlsSheet(
    uiState: CameraUiState,
    viewModel: GodcamViewModel,
    onOpenFullFilters: () -> Unit
) {
    val caps = uiState.capabilities
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Pro Controls & Hardware Telemetry",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Lens #${caps.activeCameraId} (${caps.hardwareLevelLabel}) • ${caps.focalLengthMm}mm f/${caps.apertureF} • Free: ${caps.availableStorageMb / 1024} GB",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = JetBrainsMonoFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
            Surface(
                onClick = onOpenFullFilters,
                shape = RoundedCornerShape(50),
                color = GodcamForestActive,
                contentColor = Color(0xFFFCFBF7)
            ) {
                Text(
                    text = "108 Filters",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                )
            }
        }

        // 1. Capture Mode Selector Chips (Photo, Video, Pro Photo, Pro Video, Night)
        Text("Capture Mode", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
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

        // 2. Hardware-Supported Video Resolutions
        Text(
            text = "Hardware-Supported Video Resolutions (${caps.supportedVideoResolutions.size} verified)",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            caps.supportedVideoResolutions.forEach { res ->
                val selected = uiState.videoResolution == res
                FilterChip(
                    selected = selected,
                    onClick = { viewModel.setVideoResolution(res) },
                    label = { Text(res.fullLabel) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }

        // 3. Photo Format (RAW / JPEG / HEIF)
        Text("Photo Capture Format", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
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

        // 4. White Balance Presets & Kelvin
        Text("White Balance (${uiState.whiteBalance.label} • ${uiState.manualKelvin}K)", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
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

        // 5. Manual Focus Slider (where supported)
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

        // 6. Video Stabilization & Audio Controls
        Text("Video Stabilization & Audio", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
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

        // 7. Viewfinder Assist Overlays
        Text("Viewfinder Assist Overlays (Not burned into capture)", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
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
