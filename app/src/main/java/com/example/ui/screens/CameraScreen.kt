package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.ColorMatrixColorFilter
import android.graphics.RenderEffect
import android.hardware.camera2.CaptureRequest
import android.os.Build
import androidx.camera.camera2.interop.Camera2CameraControl
import androidx.camera.camera2.interop.CaptureRequestOptions
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AspectRatio
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Cameraswitch
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.Compress
import androidx.compose.material.icons.outlined.Expand
import androidx.compose.material.icons.outlined.Exposure
import androidx.compose.material.icons.outlined.FilterBAndW
import androidx.compose.material.icons.outlined.FilterVintage
import androidx.compose.material.icons.outlined.FlashAuto
import androidx.compose.material.icons.outlined.FlashOff
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.HighQuality
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.ZoomIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import com.example.model.ColorProfileOption
import com.example.model.CompactLayoutMode
import com.example.model.FlashModeOption
import com.example.model.FocusModeOption
import com.example.model.MediaItemModel
import com.example.model.MeteringModeOption
import com.example.model.PhotoMegapixelOption
import com.example.model.StabilizationMode
import com.example.model.TimerOption
import com.example.model.VideoDurationOption
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

@androidx.annotation.OptIn(ExperimentalCamera2Interop::class)
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
    val recordedMotionFrames = remember { mutableStateListOf<Bitmap>() }
    var liveHistogramBins by remember { mutableStateOf(List(24) { 0f }) }

    val activePreset = remember(uiState.activeRecipe.presetId) {
        FilterCatalog.getById(uiState.activeRecipe.presetId)
    }

    val proOpticalState = remember(
        uiState.isAutoIso,
        uiState.manualIso,
        uiState.isAutoShutter,
        uiState.shutterDenominator,
        uiState.exposureEv,
        uiState.whiteBalance,
        uiState.manualKelvin,
        uiState.colorProfile,
        uiState.activeZoom,
        uiState.capabilities.hardwareOpticalMaxZoom
    ) {
        uiState.toProOpticalState()
    }

    // Real-time Android ColorMatrix for GPU RenderEffect on the live PreviewView (includes Filter + Pro ISO/Shutter/EV/WB/Kelvin + LOG Video Curve)
    val liveAndroidColorMatrix = remember(activePreset, uiState.activeRecipe, uiState.exposureEv, proOpticalState) {
        ImageFilterEngine.buildAndroidColorMatrix(
            preset = activePreset,
            recipe = uiState.activeRecipe,
            extraEv = uiState.exposureEv,
            proOptical = proOpticalState
        )
    }

    val recentThumbBitmap = remember(recentMedia?.id, recentMedia?.filePath, recentMedia?.uriString) {
        ImageFilterEngine.loadBitmapForMediaItem(context, recentMedia, 320, 320)
    }

    // Helper to downscale a captured frame on a background thread so video recording is 60fps with zero UI lag
    suspend fun downscaleMotionBitmap(raw: Bitmap, maxEdge: Int = 720): Bitmap = withContext(Dispatchers.Default) {
        val w = raw.width.coerceAtLeast(1)
        val h = raw.height.coerceAtLeast(1)
        val longest = max(w, h)
        if (longest <= maxEdge) {
            raw
        } else {
            val scale = maxEdge.toFloat() / longest.toFloat()
            val nw = (w * scale).roundToInt().coerceAtLeast(2)
            val nh = (h * scale).roundToInt().coerceAtLeast(2)
            Bitmap.createScaledBitmap(raw, nw, nh, true)
        }
    }

    // Helper to stop and save active video recording
    val stopAndSaveActiveVideo: () -> Unit = {
        if (uiState.isRecordingVideo) {
            val currentFrame = runCatching { previewViewRef?.bitmap }.getOrNull()
            val framesSnapshot = recordedMotionFrames.toList()
            recordedMotionFrames.clear()
            viewModel.finalizeAndAutoSaveVideo(
                existingMp4File = null,
                livePreviewBitmap = currentFrame,
                capturedFrames = framesSnapshot
            )
        }
    }

    //Helper to toggle start/stop real video recording at the user's selected resolution + active filter
    val triggerVideoToggle: () -> Unit = {
        if (uiState.isRecordingVideo) {
            stopAndSaveActiveVideo()
        } else {
            recordedMotionFrames.clear()
            viewModel.startVideoRecordingTimer()
        }
    }

    // Automatically stop and save video when a preset or custom video duration is reached
    LaunchedEffect(
        uiState.isRecordingVideo,
        uiState.recordingElapsedSec,
        uiState.videoDurationOption,
        uiState.customVideoDurationSec
    ) {
        if (uiState.isRecordingVideo) {
            val targetSec = uiState.effectiveTargetVideoDurationSec()
            if (targetSec > 0 && uiState.recordingElapsedSec >= targetSec) {
                stopAndSaveActiveVideo()
            }
        }
    }

    // Periodically sample the real live camera frame for the real-time Luminance Histogram & Filters screen preview
    LaunchedEffect(
        uiState.cameraPermissionGranted,
        previewViewRef,
        uiState.showHistogram,
        uiState.captureMode,
        uiState.isRecordingVideo
    ) {
        while (uiState.cameraPermissionGranted) {
            delay(if (uiState.isRecordingVideo) 900L else 480L)
            if (!uiState.isRecordingVideo || uiState.showHistogram || uiState.captureMode.isPro) {
                val bmp = runCatching { previewViewRef?.bitmap }.getOrNull()
                if (bmp != null) {
                    val small = downscaleMotionBitmap(bmp, 360)
                    viewModel.updateLatestCameraFrame(small)
                    if (uiState.showHistogram || uiState.captureMode.isPro) {
                        liveHistogramBins = withContext(Dispatchers.Default) {
                            ImageFilterEngine.computeLumaHistogram(small)
                        }
                    }
                }
            }
        }
    }

    // Continuous, lag-free 1080p 30fps/60fps motion frame sampling off the main UI thread (respects Pause state)
    LaunchedEffect(uiState.isRecordingVideo, previewViewRef) {
        if (uiState.isRecordingVideo) {
            recordedMotionFrames.clear()
            while (uiState.isRecordingVideo) {
                if (!uiState.isRecordingPaused) {
                    val rawFrame = runCatching { previewViewRef?.bitmap }.getOrNull()
                    if (rawFrame != null) {
                        val compactFrame = downscaleMotionBitmap(rawFrame, 640)
                        if (recordedMotionFrames.size >= 160) {
                            for (i in 79 downTo 1 step 2) {
                                recordedMotionFrames.removeAt(i)
                            }
                        }
                        recordedMotionFrames.add(compactFrame)
                    }
                }
                delay(115L)
            }
        }
    }

    // Bind real CameraX Preview + high-quality ImageCapture
    DisposableEffect(
        uiState.cameraPermissionGranted,
        uiState.isFrontCamera,
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
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                        .setJpegQuality(100)
                        .build()

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
                            imageCapture
                        )
                    } catch (_: Exception) {
                        provider.bindToLifecycle(lifecycleOwner, selector, preview)
                    }

                    boundCameraRef = camera
                    imageCaptureRef = imageCapture

                    val maxZ = camera.cameraInfo.zoomState.value?.maxZoomRatio ?: uiState.capabilities.maxZoom
                    val minZ = camera.cameraInfo.zoomState.value?.minZoomRatio ?: uiState.capabilities.minZoom
                    camera.cameraControl.enableTorch(uiState.flashMode == FlashModeOption.TORCH)
                    camera.cameraControl.setZoomRatio(uiState.activeZoom.coerceIn(minZ, maxZ))
                }
            }, executor)
        }
        onDispose { }
    }

    // Dynamically update Torch, Optical Zoom, Exposure Compensation, Hardware Stabilization, Macro Mode & Pro Camera2 controls
    LaunchedEffect(
        uiState.flashMode,
        uiState.activeZoom,
        uiState.exposureEv,
        uiState.isAutoIso,
        uiState.manualIso,
        uiState.isAutoShutter,
        uiState.shutterDenominator,
        uiState.whiteBalance,
        uiState.manualKelvin,
        uiState.stabilizationMode,
        uiState.macroModeEnabled,
        uiState.focusMode,
        uiState.manualFocusDistance,
        uiState.videoFps,
        uiState.colorProfile,
        uiState.captureMode,
        boundCameraRef
    ) {
        val cam = boundCameraRef ?: return@LaunchedEffect
        runCatching {
            cam.cameraControl.enableTorch(uiState.flashMode == FlashModeOption.TORCH)
            val zoomState = cam.cameraInfo.zoomState.value
            val minZ = zoomState?.minZoomRatio ?: 1f
            val maxZ = zoomState?.maxZoomRatio ?: uiState.capabilities.hardwareOpticalMaxZoom
            cam.cameraControl.setZoomRatio(uiState.activeZoom.coerceIn(minZ, maxZ))

            val evState = cam.cameraInfo.exposureState
            if (evState.isExposureCompensationSupported) {
                val step = evState.exposureCompensationStep.toFloat().coerceAtLeast(0.1f)
                val index = (uiState.exposureEv / step).roundToInt()
                    .coerceIn(evState.exposureCompensationRange.lower, evState.exposureCompensationRange.upper)
                cam.cameraControl.setExposureCompensationIndex(index)
            }

            // Apply Hardware Video Stabilization, Optical OIS, White Balance, FPS range & Pro Manual controls via Camera2CameraControl
            val camera2Control = Camera2CameraControl.from(cam.cameraControl)
            val optionsBuilder = CaptureRequestOptions.Builder()

            // 1. Hardware Stabilization (Digital EIS + Optical OIS)
            when (uiState.stabilizationMode) {
                StabilizationMode.OFF -> {
                    optionsBuilder.setCaptureRequestOption(
                        CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE,
                        CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_OFF
                    )
                }
                StabilizationMode.OPTICAL_OIS -> {
                    optionsBuilder.setCaptureRequestOption(
                        CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE,
                        CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_ON
                    )
                    optionsBuilder.setCaptureRequestOption(
                        CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE,
                        CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_OFF
                    )
                }
                StabilizationMode.STANDARD, StabilizationMode.CINEMA_LOCK -> {
                    optionsBuilder.setCaptureRequestOption(
                        CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE,
                        CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_ON
                    )
                    if (uiState.capabilities.supportsOpticalStabilization) {
                        optionsBuilder.setCaptureRequestOption(
                            CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE,
                            CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_ON
                        )
                    }
                }
            }

            // 2. Focus Mode: Macro close-up mode, Manual Focus distance, or Continuous AF
            when {
                uiState.macroModeEnabled || uiState.focusMode == FocusModeOption.MACRO -> {
                    optionsBuilder.setCaptureRequestOption(
                        CaptureRequest.CONTROL_AF_MODE,
                        CaptureRequest.CONTROL_AF_MODE_MACRO
                    )
                }
                uiState.focusMode == FocusModeOption.MANUAL && uiState.capabilities.supportsManualFocus -> {
                    optionsBuilder.setCaptureRequestOption(
                        CaptureRequest.CONTROL_AF_MODE,
                        CaptureRequest.CONTROL_AF_MODE_OFF
                    )
                    val maxDiopters = uiState.capabilities.minFocusDistanceDiopters.coerceAtLeast(2f)
                    optionsBuilder.setCaptureRequestOption(
                        CaptureRequest.LENS_FOCUS_DISTANCE,
                        (uiState.manualFocusDistance * maxDiopters).coerceIn(0f, maxDiopters)
                    )
                }
                else -> {
                    val afMode = if (uiState.captureMode.isVideo) {
                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO
                    } else {
                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                    }
                    optionsBuilder.setCaptureRequestOption(CaptureRequest.CONTROL_AF_MODE, afMode)
                }
            }

            // 3. White Balance Hardware Mode
            val awbMode = when (uiState.whiteBalance) {
                WhiteBalancePreset.AUTO, WhiteBalancePreset.MANUAL_KELVIN -> CaptureRequest.CONTROL_AWB_MODE_AUTO
                WhiteBalancePreset.DAYLIGHT -> CaptureRequest.CONTROL_AWB_MODE_DAYLIGHT
                WhiteBalancePreset.CLOUDY -> CaptureRequest.CONTROL_AWB_MODE_CLOUDY_DAYLIGHT
                WhiteBalancePreset.SHADE -> CaptureRequest.CONTROL_AWB_MODE_SHADE
                WhiteBalancePreset.TUNGSTEN -> CaptureRequest.CONTROL_AWB_MODE_INCANDESCENT
                WhiteBalancePreset.FLUORESCENT -> CaptureRequest.CONTROL_AWB_MODE_FLUORESCENT
            }
            optionsBuilder.setCaptureRequestOption(CaptureRequest.CONTROL_AWB_MODE, awbMode)

            // 4. Smooth 30fps / 60fps Hardware Sensor Target Frame Rate
            if (uiState.captureMode.isVideo) {
                val targetFps = uiState.videoFps.fps.coerceIn(24, 60)
                optionsBuilder.setCaptureRequestOption(
                    CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE,
                    android.util.Range(targetFps, targetFps)
                )
            }

            // 5. Pro Manual ISO & Shutter Speed when supported by hardware sensor
            if (uiState.capabilities.supportsManualSensor && (!uiState.isAutoIso || !uiState.isAutoShutter)) {
                optionsBuilder.setCaptureRequestOption(
                    CaptureRequest.SENSOR_SENSITIVITY,
                    uiState.manualIso.coerceIn(uiState.capabilities.isoRange.first, uiState.capabilities.isoRange.last)
                )
                if (!uiState.isAutoShutter) {
                    val exposureNs = (1_000_000_000L / uiState.shutterDenominator.coerceIn(1, 8000).toLong())
                    optionsBuilder.setCaptureRequestOption(CaptureRequest.SENSOR_EXPOSURE_TIME, exposureNs)
                }
            }

            camera2Control.setCaptureRequestOptions(optionsBuilder.build())
        }
    }

    // Trigger high-resolution hardware ImageCapture first
    val triggerPhotoCapture: () -> Unit = {
        val captureNow: () -> Unit = {
            val imgCap = imageCaptureRef
            if (imgCap != null && uiState.cameraPermissionGranted) {
                imgCap.takePicture(
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(image: ImageProxy) {
                            val decoded = runCatching { image.toBitmap() }.getOrNull()
                            image.close()
                            val fallback = decoded ?: runCatching { previewViewRef?.bitmap }.getOrNull()
                            viewModel.processAndSaveCapturedPhoto(fallback)
                        }

                        override fun onError(exception: ImageCaptureException) {
                            viewModel.processAndSaveCapturedPhoto(runCatching { previewViewRef?.bitmap }.getOrNull())
                        }
                    }
                )
            } else {
                viewModel.processAndSaveCapturedPhoto(runCatching { previewViewRef?.bitmap }.getOrNull())
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
        val isCompact = when (uiState.compactLayoutMode) {
            CompactLayoutMode.COMPACT -> true
            CompactLayoutMode.COMFORTABLE -> false
            CompactLayoutMode.AUTO -> maxHeight < 730.dp || maxWidth < 385.dp
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = if (isExpandedWidth) 660.dp else 560.dp)
                .align(Alignment.Center)
                .padding(
                    horizontal = if (isCompact) 8.dp else 14.dp,
                    vertical = if (isCompact) 2.dp else 6.dp
                ),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. APPLE-INSPIRED CLEAN, BOLD MINIMALIST TOP BAR (With Megapixel / LOG Quick Pill & Top Video Pause/Resume!)
            CameraTopBar(
                uiState = uiState,
                isCompact = isCompact,
                onOpenRatioSheet = { showRatioDialogSheet = true },
                onCycleMegapixel = { viewModel.cyclePhotoMegapixel() },
                onToggleLogProfile = { viewModel.toggleLogVideoProfile() },
                onToggleMacro = { viewModel.toggleMacroMode() },
                onToggleCompactMode = {
                    val nextMode = if (isCompact) CompactLayoutMode.COMFORTABLE else CompactLayoutMode.COMPACT
                    viewModel.setCompactLayoutMode(nextMode)
                },
                onCycleFlash = { viewModel.cycleFlashMode() },
                onTogglePauseVideo = { viewModel.toggleVideoPauseResume() },
                onStopVideo = { stopAndSaveActiveVideo() },
                onOpenSettings = { viewModel.navigateTo(AppDestination.SETTINGS) }
            )

            Spacer(modifier = Modifier.height(if (isCompact) 3.dp else 6.dp))

            // 2. UNCLUTTERED APPLE-STYLE HERO VIEWFINDER CARD (Supports live Pro optical preview & 100x hybrid zoom)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(if (isCompact) 16.dp else 24.dp))
                    .background(Color(0xFF0A100E))
                    .border(
                        width = if (uiState.isRecordingVideo) 2.2.dp else 1.5.dp,
                        color = when {
                            uiState.isRecordingVideo && uiState.isRecordingPaused -> GodcamAmberGold
                            uiState.isRecordingVideo -> GodcamRecordRed
                            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.75f)
                        },
                        shape = RoundedCornerShape(if (isCompact) 16.dp else 24.dp)
                    )
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { offset ->
                                val nx = (offset.x / size.width.coerceAtLeast(1)).coerceIn(0.08f, 0.92f)
                                val ny = (offset.y / size.height.coerceAtLeast(1)).coerceIn(0.08f, 0.92f)
                                viewModel.onTapToFocus(nx, ny)
                                showExposurePopup = true
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
                    val hardwareMaxZ = (boundCameraRef?.cameraInfo?.zoomState?.value?.maxZoomRatio
                        ?: uiState.capabilities.hardwareOpticalMaxZoom).coerceAtLeast(1f)
                    val extraPreviewZoom = (uiState.activeZoom / hardwareMaxZ).coerceIn(1f, 25f)

                    AndroidView(
                        factory = { ctx ->
                            PreviewView(ctx).apply {
                                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                                scaleType = PreviewView.ScaleType.FILL_CENTER
                                previewViewRef = this
                            }
                        },
                        update = { view ->
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                val isNeutral = uiState.activeRecipe.presetId == "original" &&
                                    uiState.activeRecipe.warmthOffset == 0f &&
                                    uiState.activeRecipe.contrastOffset == 0f &&
                                    uiState.activeRecipe.fadeOffset == 0f &&
                                    uiState.exposureEv == 0f &&
                                    uiState.isAutoIso &&
                                    uiState.isAutoShutter &&
                                    uiState.whiteBalance == WhiteBalancePreset.AUTO &&
                                    uiState.colorProfile == ColorProfileOption.STANDARD
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
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = extraPreviewZoom,
                                scaleY = extraPreviewZoom
                            )
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
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Memory,
                            contentDescription = null,
                            tint = GodcamMintAccent,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "OPTICAL SENSOR STANDBY",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = JetBrainsMonoFamily,
                                color = Color(0xFFFCFBF7),
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.4.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Lens #${uiState.capabilities.activeCameraId} • ${uiState.capabilities.focalLengthMm}mm f/${uiState.capabilities.apertureF} • ${uiState.capabilities.hardwareLevelLabel}",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = JetBrainsMonoFamily,
                                color = GodcamMintAccent
                            )
                        )
                    }
                }

                // Grid, Golden Ratio, Real-Time Accelerometer Horizon Level, Aspect Ratio Framing Mask & Focus Reticle
                ViewfinderCompositionOverlays(
                    gridMode = uiState.gridMode,
                    aspectRatio = uiState.aspectRatio,
                    focusPoint = uiState.focusPointNormalized,
                    aeAfLocked = uiState.aeAfLocked,
                    showFocusPeaking = uiState.showFocusPeaking,
                    showZebraStripes = uiState.showZebraStripes,
                    deviceTiltDegrees = uiState.deviceTiltDegrees
                )

                // Subtle Top-Center Live Pro Telemetry / Lock / Macro / LOG Pill
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isProPhotoStyle || isProVideoStyle || uiState.colorProfile.isLog) {
                        val isoTag = if (uiState.isAutoIso) "ISO A" else "ISO ${uiState.manualIso}"
                        val shutTag = if (uiState.isAutoShutter) "S Auto" else "1/${uiState.shutterDenominator}s"
                        val wbTag = if (uiState.whiteBalance == WhiteBalancePreset.MANUAL_KELVIN) "${uiState.manualKelvin}K" else uiState.whiteBalance.label
                        val modeTag = if (uiState.captureMode.isVideo) uiState.colorProfile.shortBadge else uiState.photoMegapixel.shortBadge
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color(0xCC101915),
                            contentColor = GodcamMintAccent,
                            modifier = Modifier.border(1.dp, GodcamMintAccent.copy(alpha = 0.45f), RoundedCornerShape(50))
                        ) {
                            Text(
                                text = "$modeTag · $isoTag · $shutTag · $wbTag",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = JetBrainsMonoFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
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
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Lock,
                                    contentDescription = "AE/AF Locked",
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "AE/AF LOCK",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold)
                                )
                            }
                        }
                    }

                    if (uiState.macroModeEnabled) {
                        Surface(
                            onClick = { viewModel.toggleMacroMode() },
                            shape = RoundedCornerShape(50),
                            color = GodcamAmberGold,
                            contentColor = Color(0xFF141E1A)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.FilterVintage,
                                    contentDescription = "Macro Active",
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "MACRO",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold)
                                )
                            }
                        }
                    }
                }

                // Top-Right Live Histogram (When enabled in Settings or in Pro modes)
                if (uiState.showHistogram || isProPhotoStyle || isProVideoStyle) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                    ) {
                        LiveHistogramOverlay(bins = liveHistogramBins)
                    }
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

                // Bottom of Viewfinder: Custom Precision Zoom Slider + Optical Zoom Stops + Macro & EV Toggles
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 12.dp, vertical = if (isCompact) 6.dp else 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Custom Continuous Zoom Slider (Apple-style smooth dial bar)
                    AnimatedVisibility(
                        visible = uiState.showZoomSlider,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xCC101915),
                            contentColor = Color(0xFFFCFBF7),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(16.dp))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = if (uiState.activeZoom >= 10f) {
                                        "${uiState.activeZoom.roundToInt()}x"
                                    } else {
                                        String.format(Locale.US, "%.1fx", uiState.activeZoom)
                                    },
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontFamily = JetBrainsMonoFamily,
                                        color = GodcamAmberGold,
                                        fontWeight = FontWeight.ExtraBold
                                    ),
                                    modifier = Modifier.width(44.dp)
                                )
                                Slider(
                                    value = uiState.activeZoom.coerceIn(
                                        uiState.capabilities.minZoom,
                                        uiState.capabilities.maxZoom
                                    ),
                                    onValueChange = { viewModel.setZoom(it) },
                                    valueRange = uiState.capabilities.minZoom..uiState.capabilities.maxZoom.coerceAtLeast(100f),
                                    colors = SliderDefaults.colors(
                                        thumbColor = GodcamAmberGold,
                                        activeTrackColor = GodcamMintAccent,
                                        inactiveTrackColor = Color.White.copy(alpha = 0.25f)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(28.dp)
                                        .testTag("custom_zoom_slider")
                                )
                                Surface(
                                    onClick = { viewModel.setZoom(1.0f) },
                                    shape = RoundedCornerShape(50),
                                    color = Color.White.copy(alpha = 0.14f)
                                ) {
                                    Text(
                                        text = "1x",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = JetBrainsMonoFamily,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                    )
                                }
                                Surface(
                                    onClick = { viewModel.setZoom(100.0f) },
                                    shape = RoundedCornerShape(50),
                                    color = if (uiState.activeZoom >= 95f) GodcamAmberGold else Color.White.copy(alpha = 0.14f)
                                ) {
                                    Text(
                                        text = "100x",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = JetBrainsMonoFamily,
                                            color = if (uiState.activeZoom >= 95f) Color(0xFF141E1A) else GodcamAmberGold,
                                            fontWeight = FontWeight.ExtraBold
                                        ),
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }

                    Box(modifier = Modifier.fillMaxWidth()) {
                        // Left: Macro Close-Up Flower Quick Toggle + Zoom Slider Toggle
                        Row(
                            modifier = Modifier.align(Alignment.CenterStart),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                onClick = { viewModel.toggleMacroMode() },
                                shape = CircleShape,
                                color = if (uiState.macroModeEnabled) GodcamAmberGold else Color(0xBB141E1A),
                                contentColor = if (uiState.macroModeEnabled) Color(0xFF141E1A) else Color(0xFFFCFBF7),
                                modifier = Modifier
                                    .size(if (isCompact) 30.dp else 34.dp)
                                    .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape)
                                    .testTag("viewfinder_macro_toggle")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.FilterVintage,
                                        contentDescription = "Macro Close-Up Mode",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Surface(
                                onClick = { viewModel.toggleZoomSlider() },
                                shape = RoundedCornerShape(50),
                                color = if (uiState.showZoomSlider) GodcamForestActive else Color(0xBB141E1A),
                                contentColor = Color(0xFFFCFBF7),
                                modifier = Modifier
                                    .height(if (isCompact) 30.dp else 34.dp)
                                    .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(50))
                                    .testTag("zoom_slider_toggle_pill")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 9.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ZoomIn,
                                        contentDescription = "Toggle Custom Zoom Slider",
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = if (uiState.activeZoom >= 10f) {
                                            "${uiState.activeZoom.roundToInt()}x"
                                        } else {
                                            String.format(Locale.US, "%.1fx", uiState.activeZoom)
                                        },
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = JetBrainsMonoFamily,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }

                        // Center: Optical & Super-Res Zoom Stops up to 100x
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color(0xBB141E1A),
                            modifier = Modifier
                                .align(Alignment.Center)
                                .height(if (isCompact) 32.dp else 36.dp)
                                .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(50))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp)
                            ) {
                                uiState.capabilities.zoomStops.forEach { stop ->
                                    val isSelected = abs(uiState.activeZoom - stop) < (if (stop >= 10f) 1.5f else 0.15f)
                                    val label = if (isSelected) {
                                        if (stop == stop.toInt().toFloat()) "${stop.toInt()}x" else "${stop}x"
                                    } else {
                                        if (stop == stop.toInt().toFloat()) "${stop.toInt()}" else "$stop"
                                    }
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .height(if (isCompact) 24.dp else 28.dp)
                                            .widthIn(min = if (isCompact) 24.dp else 28.dp)
                                            .clip(RoundedCornerShape(50))
                                            .background(if (isSelected) GodcamForestActive else Color.Transparent)
                                            .then(
                                                if (isSelected) Modifier.border(1.dp, GodcamMintAccent, RoundedCornerShape(50))
                                                else Modifier
                                            )
                                            .clickable {
                                                viewModel.setZoom(stop)
                                                viewModel.setShowZoomSlider(true)
                                            }
                                            .padding(horizontal = if (stop >= 10f) 5.dp else 3.dp)
                                            .testTag("zoom_stop_$stop")
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontFamily = JetBrainsMonoFamily,
                                                fontSize = if (isCompact || stop >= 100f) 9.5.sp else 10.5.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) Color(0xFFFCFBF7) else Color(0xFFE6ECE9)
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // Right: EV Compensation Quick Toggle inside Viewfinder
                        Surface(
                            onClick = { showExposurePopup = !showExposurePopup },
                            shape = CircleShape,
                            color = if (showExposurePopup || uiState.exposureEv != 0f) GodcamForestActive else Color(0xBB141E1A),
                            contentColor = Color(0xFFFCFBF7),
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .size(if (isCompact) 30.dp else 34.dp)
                                .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape)
                                .testTag("viewfinder_ev_toggle")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.Exposure,
                                    contentDescription = "Exposure Compensation",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
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

            // Pending Unsaved Capture Review Bar (Appears when Auto-Save is turned OFF in Settings)
            AnimatedVisibility(visible = uiState.pendingUnsavedMedia != null) {
                uiState.pendingUnsavedMedia?.let { pending ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(14.dp))
                            .testTag("pending_capture_review_bar")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Auto-Save OFF • ${pending.title}",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${pending.width}×${pending.height} • ${pending.formatLabel}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = JetBrainsMonoFamily,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                    )
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(
                                    onClick = { viewModel.discardPendingCapture() },
                                    shape = RoundedCornerShape(50),
                                    color = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.testTag("discard_pending_capture_button")
                                ) {
                                    Text(
                                        text = "Discard",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                                Surface(
                                    onClick = { viewModel.savePendingCaptureToGallery() },
                                    shape = RoundedCornerShape(50),
                                    color = GodcamForestActive,
                                    contentColor = Color(0xFFFCFBF7),
                                    modifier = Modifier.testTag("save_pending_capture_button")
                                ) {
                                    Text(
                                        text = "Save to Gallery",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Status / Capability / Auto-Save Feedback Toast Banner
            AnimatedVisibility(visible = uiState.statusBannerMessage != null && uiState.pendingUnsavedMedia == null) {
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
                            .padding(top = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
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
            AnimatedVisibility(
                visible = uiState.showLiveFilterStrip,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                LiveFilterSelectorRibbon(
                    uiState = uiState,
                    onSelectPreset = { viewModel.selectPreset(it) },
                    onOpenFullLibrary = { viewModel.navigateTo(AppDestination.FILTERS) }
                )
            }

            // Quick Inline Exposure (EV) Slider when tapped inside viewfinder
            AnimatedVisibility(
                visible = showExposurePopup && !isProPhotoStyle && !isProVideoStyle,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                ) {
                    Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
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

            Spacer(modifier = Modifier.height(if (isCompact) 3.dp else 6.dp))

            // 3. APPLE-INSPIRED 4-MODE SELECTOR PILL ("Photo | Pro Photo | Video | Pro Video")
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                val primaryModes = remember {
                    listOf(
                        CaptureMode.PHOTO,
                        CaptureMode.PRO_PHOTO,
                        CaptureMode.VIDEO,
                        CaptureMode.PRO_VIDEO
                    )
                }
                val activePrimaryMode = if (uiState.captureMode == CaptureMode.NIGHT) {
                    CaptureMode.PRO_PHOTO
                } else {
                    uiState.captureMode
                }

                SegmentedPillSelector(
                    options = primaryModes,
                    selectedOption = activePrimaryMode,
                    labelProvider = { it.label },
                    onOptionSelected = { viewModel.setCaptureMode(it) },
                    isCompact = isCompact,
                    testTagPrefix = "mode_pill"
                )
            }

            // 4. PRO PHOTO & PRO VIDEO PROFESSIONAL CONTROLS DECK
            AnimatedVisibility(
                visible = isProVideoStyle || isProPhotoStyle,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Surface(
                    shape = RoundedCornerShape(if (isCompact) 14.dp else 18.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = if (isCompact) 3.dp else 6.dp)
                        .border(
                            1.5.dp,
                            MaterialTheme.colorScheme.outline,
                            RoundedCornerShape(if (isCompact) 14.dp else 18.dp)
                        )
                ) {
                    if (isProVideoStyle) {
                        ProVideoSlidersDeck(
                            uiState = uiState,
                            isCompact = isCompact,
                            viewModel = viewModel
                        )
                    } else {
                        ProPhotoSlidersDeck(
                            uiState = uiState,
                            isCompact = isCompact,
                            viewModel = viewModel
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(if (isCompact) 3.dp else 6.dp))

            // 5. PRIMARY SHUTTER ROW (Recent Real Capture Thumbnail • Tactile Shutter • Lens Switch)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = if (isCompact) 10.dp else 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(if (isCompact) 40.dp else 48.dp)
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
                            modifier = Modifier.size(if (isCompact) 18.dp else 20.dp)
                        )
                    }
                }

                GodcamShutterButton(
                    isVideoMode = uiState.captureMode.isVideo,
                    isRecording = uiState.isRecordingVideo,
                    isCompact = isCompact,
                    isPaused = uiState.isRecordingPaused,
                    onClick = {
                        if (uiState.captureMode.isVideo) {
                            triggerVideoToggle()
                        } else {
                            triggerPhotoCapture()
                        }
                    }
                )

                CircularIconControl(
                    icon = Icons.Outlined.Cameraswitch,
                    contentDescription = "Switch Front/Rear Camera",
                    onClick = { viewModel.switchCameraLens() },
                    size = if (isCompact) 40.dp else 46.dp,
                    testTag = "switch_camera_button"
                )
            }

            Spacer(modifier = Modifier.height(if (isCompact) 2.dp else 6.dp))

            // 6. BOTTOM QUICK TOOL DOCK (Clean, responsive, supports Compact Mode)
            if (isProVideoStyle) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = if (isCompact) 0.dp else 2.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    QuickToolItem(
                        icon = Icons.Outlined.FilterBAndW,
                        label = "Filter",
                        badge = if (uiState.activeRecipe.presetId != "original") activePreset.name.take(5) else null,
                        isActive = uiState.activeRecipe.presetId != "original" || uiState.showLiveFilterStrip,
                        isCompact = isCompact,
                        onClick = { viewModel.toggleLiveFilterStrip() },
                        testTag = "dock_filter"
                    )
                    QuickToolItem(
                        icon = Icons.Outlined.AspectRatio,
                        label = "Ratio/Size",
                        badge = if (uiState.useCustomVideoSize) "CUST" else uiState.aspectRatio.label,
                        isActive = uiState.useCustomVideoSize,
                        isCompact = isCompact,
                        onClick = { showRatioDialogSheet = true },
                        testTag = "dock_ratio"
                    )
                    QuickToolItem(
                        icon = Icons.Outlined.HighQuality,
                        label = "Res",
                        badge = uiState.videoResolution.shortLabel,
                        isCompact = isCompact,
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
                        isCompact = isCompact,
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
                        isCompact = isCompact,
                        onClick = {
                            val tiers = listOf(24, 50, 100, 140)
                            val idx = tiers.indexOfFirst { it >= uiState.videoBitrateMbps }
                            val next = tiers[(idx + 1) % tiers.size]
                            viewModel.setVideoBitrateMbps(next)
                        },
                        testTag = "dock_bitrate"
                    )
                    QuickToolItem(
                        icon = Icons.Outlined.MoreHoriz,
                        label = "More",
                        isCompact = isCompact,
                        onClick = { showMoreToolsSheet = true },
                        testTag = "dock_more"
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = if (isCompact) 0.dp else 2.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    QuickToolItem(
                        icon = Icons.Outlined.FilterBAndW,
                        label = "Filter",
                        badge = if (uiState.activeRecipe.presetId != "original") activePreset.name.take(6) else null,
                        isActive = uiState.activeRecipe.presetId != "original",
                        isCompact = isCompact,
                        onClick = { viewModel.navigateTo(AppDestination.FILTERS) },
                        testTag = "dock_filter"
                    )
                    QuickToolItem(
                        icon = Icons.Outlined.AspectRatio,
                        label = if (uiState.captureMode.isVideo) "Ratio/Size" else "Ratio",
                        badge = if (uiState.captureMode.isVideo && uiState.useCustomVideoSize) "CUST" else uiState.aspectRatio.label,
                        isActive = uiState.captureMode.isVideo && uiState.useCustomVideoSize,
                        isCompact = isCompact,
                        onClick = { showRatioDialogSheet = true },
                        testTag = "dock_ratio"
                    )
                    if (uiState.captureMode.isVideo) {
                        QuickToolItem(
                            icon = Icons.Outlined.Timer,
                            label = "Length",
                            badge = if (uiState.videoDurationOption == VideoDurationOption.CUSTOM) {
                                "${uiState.customVideoDurationSec}s"
                            } else {
                                uiState.videoDurationOption.shortBadge
                            },
                            isActive = uiState.videoDurationOption != VideoDurationOption.CONTINUOUS,
                            isCompact = isCompact,
                            onClick = {
                                val opts = VideoDurationOption.entries
                                val next = opts[(opts.indexOf(uiState.videoDurationOption) + 1) % opts.size]
                                viewModel.setVideoDurationOption(next)
                            },
                            testTag = "dock_video_duration"
                        )
                    } else {
                        QuickToolItem(
                            icon = Icons.Outlined.GridOn,
                            label = "Grid",
                            badge = if (uiState.gridMode.name != "OFF") uiState.gridMode.label else null,
                            isActive = uiState.gridMode.name != "OFF",
                            isCompact = isCompact,
                            onClick = { viewModel.cycleGridMode() },
                            testTag = "dock_grid"
                        )
                    }
                    QuickToolItem(
                        icon = Icons.Outlined.Timer,
                        label = "Timer",
                        badge = if (uiState.timerOption != TimerOption.OFF) uiState.timerOption.label else null,
                        isActive = uiState.timerOption != TimerOption.OFF,
                        isCompact = isCompact,
                        onClick = { viewModel.cycleTimerOption() },
                        testTag = "dock_timer"
                    )
                    QuickToolItem(
                        icon = Icons.Outlined.MoreHoriz,
                        label = "More",
                        isActive = isProPhotoStyle || uiState.nightModeActive || uiState.macroModeEnabled,
                        isCompact = isCompact,
                        onClick = { showMoreToolsSheet = true },
                        testTag = "dock_more"
                    )
                }
            }
        }
    }

    // All 22 Aspect Ratios & Custom Video Size Bottom Sheet (Shared for Photo & Video)
    if (showRatioDialogSheet) {
        ModalBottomSheet(
            onDismissRequest = { showRatioDialogSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            var customWInput by remember(uiState.customVideoWidth) { mutableStateOf(uiState.customVideoWidth.toString()) }
            var customHInput by remember(uiState.customVideoHeight) { mutableStateOf(uiState.customVideoHeight.toString()) }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (uiState.captureMode.isVideo) {
                        "Video Aspect Ratio & Custom Size (${AspectRatioOption.entries.size} Ratios)"
                    } else {
                        "Photo & Video Aspect Ratios (${AspectRatioOption.entries.size} Ratios)"
                    },
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                )
                Text(
                    text = "All ${AspectRatioOption.entries.size} optical, social, medium-format, IMAX, Golden Ratio, and cinema widescreen ratios center-crop accurately without stretching.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                // Custom Video Size Section
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Custom Video Size (Width × Height px)",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold)
                                )
                                Text(
                                    text = "Active Video Output: ${uiState.effectiveVideoDimensions().first} × ${uiState.effectiveVideoDimensions().second} px",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = JetBrainsMonoFamily,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Switch(
                                checked = uiState.useCustomVideoSize,
                                onCheckedChange = { viewModel.setUseCustomVideoSize(it) },
                                modifier = Modifier.testTag("custom_video_size_switch")
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val quickSizes = listOf(
                                "1080×1920 (9:16)" to (1080 to 1920),
                                "1920×1080 (16:9)" to (1920 to 1080),
                                "1080×1080 (1:1)" to (1080 to 1080),
                                "1080×1350 (4:5)" to (1080 to 1350),
                                "1440×1080 (4:3)" to (1440 to 1080),
                                "1080×1440 (3:4)" to (1080 to 1440),
                                "1920×804 (2.39:1)" to (1920 to 804)
                            )
                            quickSizes.forEach { (label, pair) ->
                                val (qw, qh) = pair
                                FilterChip(
                                    selected = uiState.useCustomVideoSize && uiState.customVideoWidth == qw && uiState.customVideoHeight == qh,
                                    onClick = {
                                        customWInput = qw.toString()
                                        customHInput = qh.toString()
                                        viewModel.setCustomVideoDimensions(qw, qh)
                                    },
                                    label = { Text(label) }
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = customWInput,
                                onValueChange = { txt ->
                                    customWInput = txt.filter { it.isDigit() }
                                    customWInput.toIntOrNull()?.let { w ->
                                        if (w >= 320) viewModel.setCustomVideoDimensions(w, uiState.customVideoHeight)
                                    }
                                },
                                label = { Text("Width (px)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("custom_video_width_input")
                            )
                            OutlinedTextField(
                                value = customHInput,
                                onValueChange = { txt ->
                                    customHInput = txt.filter { it.isDigit() }
                                    customHInput.toIntOrNull()?.let { h ->
                                        if (h >= 320) viewModel.setCustomVideoDimensions(uiState.customVideoWidth, h)
                                    }
                                },
                                label = { Text("Height (px)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("custom_video_height_input")
                            )
                        }
                    }
                }

                Text(
                    text = "All ${AspectRatioOption.entries.size} Photo & Video Aspect Ratios",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                )

                AspectRatioOption.entries.forEach { ratio ->
                    val selected = !uiState.useCustomVideoSize && ratio == uiState.aspectRatio
                    val (rw, rh) = ratio.computeDimensionsForLongEdge(uiState.videoResolution.longEdge)
                    val (pw, ph) = ratio.computeDimensionsForLongEdge(
                        max(uiState.capabilities.maxPhotoWidth, uiState.capabilities.maxPhotoHeight)
                    )
                    Surface(
                        onClick = {
                            viewModel.setUseCustomVideoSize(false)
                            viewModel.setAspectRatio(ratio)
                            showRatioDialogSheet = false
                        },
                        shape = RoundedCornerShape(14.dp),
                        color = if (selected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (selected) 2.dp else 1.dp,
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(14.dp)
                            )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 11.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = ratio.label,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                )
                                Text(
                                    text = "${ratio.description} • Photo ${pw}×${ph} • Video ${rw}×${rh}",
                                    style = MaterialTheme.typography.labelMedium.copy(
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
                                    text = if (ratio.isCinematicGuide) "Cinema Crop" else "Photo & Video",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
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
            .padding(top = 4.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f), RoundedCornerShape(16.dp))
            .testTag("live_filter_ribbon")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
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
                            .height(30.dp)
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
                modifier = Modifier.height(30.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    Text(
                        text = "All ${FilterCatalog.allPresets.size}",
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

/**
 * Apple-Inspired Clean Minimalist Top Bar:
 * - Removes the red-circled top-left "..." button.
 * - When recording video, transforms into a high-visibility Recording Bar with a prominent PAUSE / RESUME button and timer at the top!
 */
@Composable
private fun CameraTopBar(
    uiState: CameraUiState,
    isCompact: Boolean,
    onOpenRatioSheet: () -> Unit,
    onCycleMegapixel: () -> Unit,
    onToggleLogProfile: () -> Unit,
    onToggleMacro: () -> Unit,
    onToggleCompactMode: () -> Unit,
    onCycleFlash: () -> Unit,
    onTogglePauseVideo: () -> Unit,
    onStopVideo: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val flashIcon = when (uiState.flashMode) {
        FlashModeOption.OFF -> Icons.Outlined.FlashOff
        FlashModeOption.AUTO -> Icons.Outlined.FlashAuto
        FlashModeOption.ON -> Icons.Outlined.FlashOn
        FlashModeOption.TORCH -> Icons.Outlined.Bolt
    }
    val iconBtnSize = if (isCompact) 34.dp else 38.dp
    val pillHeight = if (isCompact) 32.dp else 36.dp

    if (uiState.isRecordingVideo) {
        // Active Video Recording Top Bar with Pause / Resume Button & Custom Duration Readout
        val mins = uiState.recordingElapsedSec / 60
        val secs = uiState.recordingElapsedSec % 60
        val targetSec = uiState.effectiveTargetVideoDurationSec()
        val targetSuffix = if (targetSec > 0) {
            String.format(Locale.US, " / %02d:%02d", targetSec / 60, targetSec % 60)
        } else {
            ""
        }
        val statusPrefix = if (uiState.isRecordingPaused) "PAUSED" else "REC"

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Live Recording Timer Pill
            Surface(
                shape = RoundedCornerShape(50),
                color = if (uiState.isRecordingPaused) GodcamAmberGold else GodcamRecordRed,
                contentColor = if (uiState.isRecordingPaused) Color(0xFF141E1A) else Color(0xFFFCFBF7),
                modifier = Modifier
                    .height(pillHeight)
                    .testTag("top_recording_timer_pill")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp)
                ) {
                    Text(
                        text = String.format(Locale.US, "%s %02d:%02d%s", statusPrefix, mins, secs, targetSuffix),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = JetBrainsMonoFamily,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                }
            }

            // Right: Prominent Top Pause / Resume Button + Stop Button
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = onTogglePauseVideo,
                    shape = RoundedCornerShape(50),
                    color = if (uiState.isRecordingPaused) GodcamForestActive else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (uiState.isRecordingPaused) Color(0xFFFCFBF7) else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .height(pillHeight)
                        .border(1.4.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(50))
                        .testTag("video_pause_resume_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        Icon(
                            imageVector = if (uiState.isRecordingPaused) Icons.Outlined.PlayArrow else Icons.Outlined.Pause,
                            contentDescription = if (uiState.isRecordingPaused) "Resume Video" else "Pause Video",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (uiState.isRecordingPaused) "Resume" else "Pause",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold)
                        )
                    }
                }

                Surface(
                    onClick = onStopVideo,
                    shape = RoundedCornerShape(50),
                    color = GodcamRecordRed,
                    contentColor = Color(0xFFFCFBF7),
                    modifier = Modifier
                        .height(pillHeight)
                        .testTag("video_top_stop_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Stop,
                            contentDescription = "Stop & Save Video",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Save",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold)
                        )
                    }
                }
            }
        }
    } else {
        // Standard Clean Apple-Inspired Top Bar (Format/Ratio Pill + Megapixel/LOG Quick Pill + Hardware Controls)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Clean Format & Ratio Spec Pill (Tapping opens Aspect Ratio & Size Sheet)
                Surface(
                    onClick = onOpenRatioSheet,
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .height(pillHeight)
                        .border(1.4.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(50))
                        .testTag("top_profile_pill")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(horizontal = if (isCompact) 10.dp else 12.dp)
                    ) {
                        val durSuffix = if (uiState.captureMode.isVideo && uiState.videoDurationOption != VideoDurationOption.CONTINUOUS) {
                            " · ${uiState.effectiveTargetVideoDurationSec()}s"
                        } else {
                            ""
                        }
                        val badgeText = when {
                            uiState.captureMode == CaptureMode.NIGHT -> "NIGHT · ${uiState.aspectRatio.label}"
                            uiState.captureMode.isVideo && uiState.useCustomVideoSize -> {
                                "${uiState.customVideoWidth}×${uiState.customVideoHeight} · ${uiState.videoFps.fps}fps$durSuffix"
                            }
                            uiState.captureMode.isVideo -> {
                                "${uiState.videoResolution.shortLabel} · ${uiState.aspectRatio.label} · ${uiState.videoFps.fps}fps$durSuffix"
                            }
                            else -> {
                                "${uiState.photoFormat.label} · ${uiState.aspectRatio.label}"
                            }
                        }
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = JetBrainsMonoFamily,
                                fontSize = if (isCompact) 10.5.sp else 11.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }

                // Quick Hardware Megapixel (12MP/24MP/48MP/50MP) or LOG Video Profile Pill
                if (uiState.captureMode.isVideo) {
                    val isLogActive = uiState.colorProfile.isLog
                    Surface(
                        onClick = onToggleLogProfile,
                        shape = RoundedCornerShape(50),
                        color = if (isLogActive) GodcamAmberGold else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isLogActive) Color(0xFF141E1A) else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .height(pillHeight)
                            .border(
                                1.4.dp,
                                if (isLogActive) GodcamAmberGold else MaterialTheme.colorScheme.outline,
                                RoundedCornerShape(50)
                            )
                            .testTag("top_log_video_pill")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = if (isCompact) 9.dp else 11.dp)
                        ) {
                            Text(
                                text = if (isLogActive) uiState.colorProfile.shortBadge else "LOG",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = JetBrainsMonoFamily,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                        }
                    }
                } else {
                    val isHighMp = uiState.photoMegapixel.megapixels >= 48
                    Surface(
                        onClick = onCycleMegapixel,
                        shape = RoundedCornerShape(50),
                        color = if (isHighMp) GodcamForestActive else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isHighMp) Color(0xFFFCFBF7) else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .height(pillHeight)
                            .border(
                                1.4.dp,
                                if (isHighMp) GodcamMintAccent else MaterialTheme.colorScheme.outline,
                                RoundedCornerShape(50)
                            )
                            .testTag("top_megapixel_pill")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = if (isCompact) 9.dp else 11.dp)
                        ) {
                            Text(
                                text = uiState.photoMegapixel.shortBadge,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = JetBrainsMonoFamily,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                        }
                    }
                }
            }

            // Right: Macro Toggle, Compact Mode Toggle, Flash/Torch (if supported), Settings
            Row(
                horizontalArrangement = Arrangement.spacedBy(if (isCompact) 4.dp else 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularIconControl(
                    icon = Icons.Outlined.FilterVintage,
                    contentDescription = "Macro Close-Up Mode",
                    isSelected = uiState.macroModeEnabled,
                    onClick = onToggleMacro,
                    size = iconBtnSize,
                    testTag = "top_macro_button"
                )
                CircularIconControl(
                    icon = if (isCompact) Icons.Outlined.Expand else Icons.Outlined.Compress,
                    contentDescription = if (isCompact) "Switch to Comfortable Layout" else "Switch to Compact Layout",
                    isSelected = isCompact,
                    onClick = onToggleCompactMode,
                    size = iconBtnSize,
                    testTag = "top_compact_mode_button"
                )
                if (uiState.capabilities.hasFlashUnit) {
                    CircularIconControl(
                        icon = flashIcon,
                        contentDescription = "Flash Mode: ${uiState.flashMode.label}",
                        isSelected = uiState.flashMode != FlashModeOption.OFF,
                        onClick = onCycleFlash,
                        size = iconBtnSize,
                        testTag = "top_flash_button"
                    )
                }
                CircularIconControl(
                    icon = Icons.Outlined.Settings,
                    contentDescription = "Open Settings",
                    onClick = onOpenSettings,
                    size = iconBtnSize,
                    testTag = "top_settings_button"
                )
            }
        }
    }
}

/**
 * Pro Video Mode Deck:
 * Includes Resolution, FPS, Bitrate, plus interactive tabs for Video Duration Timer,
 * Hardware Stabilization, Manual Exposure/Focus, and Audio/Background Music.
 */
@Composable
private fun ProVideoSlidersDeck(
    uiState: CameraUiState,
    isCompact: Boolean,
    viewModel: GodcamViewModel
) {
    var selectedSection by remember { mutableIntStateOf(0) }
    val sections = remember { listOf("Format", "Exposure", "Stabilize & Time", "Audio & Log") }

    val resList = uiState.capabilities.supportedVideoResolutions
    val resIdx = resList.indexOf(uiState.videoResolution).coerceAtLeast(0).toFloat()

    val validFpsList = uiState.capabilities.validFpsFor(uiState.videoResolution, uiState.videoCodec)
    val fpsIdx = validFpsList.indexOf(uiState.videoFps).coerceAtLeast(0).toFloat()

    val maxBitrate = uiState.capabilities.maxBitrateMbpsFor(uiState.videoResolution, uiState.videoCodec).toFloat()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
            .padding(horizontal = if (isCompact) 12.dp else 16.dp, vertical = if (isCompact) 6.dp else 10.dp),
        verticalArrangement = Arrangement.spacedBy(if (isCompact) 2.dp else 4.dp)
    ) {
        // Section Selector Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            sections.forEachIndexed { idx, title ->
                val active = selectedSection == idx
                Surface(
                    onClick = { selectedSection = idx },
                    shape = RoundedCornerShape(50),
                    color = if (active) GodcamForestActive else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (active) Color(0xFFFCFBF7) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.height(26.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(horizontal = 11.dp)
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (active) FontWeight.ExtraBold else FontWeight.SemiBold
                            )
                        )
                    }
                }
            }
        }

        when (selectedSection) {
            0 -> {
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
                if (!isCompact) {
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

            1 -> {
                // Pro Video Exposure (EV, ISO, Shutter, Manual Focus) — All update Live Viewfinder immediately!
                val shutterChoices = listOf(24, 30, 48, 50, 60, 100, 120, 250, 500, 1000, 2000, 4000)
                val shutterIdx = shutterChoices.indexOfFirst { it >= uiState.shutterDenominator }.let { if (it < 0) 4 else it }.toFloat()

                LabeledCameraSlider(
                    label = "Exposure (EV)",
                    valueText = "${if (uiState.exposureEv > 0) "+" else ""}${uiState.exposureEv} EV",
                    value = uiState.exposureEv,
                    valueRange = uiState.capabilities.evRange,
                    onValueChange = { viewModel.setExposureEv(it) },
                    onReset = { viewModel.setExposureEv(0f) },
                    testTag = "pro_video_ev_slider"
                )
                LabeledCameraSlider(
                    label = "ISO Sensitivity",
                    valueText = if (uiState.isAutoIso) "Auto (${uiState.manualIso})" else "ISO ${uiState.manualIso}",
                    value = uiState.manualIso.toFloat(),
                    valueRange = uiState.capabilities.isoRange.first.toFloat()..uiState.capabilities.isoRange.last.toFloat(),
                    onValueChange = { viewModel.setManualIso(it.roundToInt()) },
                    onReset = { viewModel.setManualIso(null) },
                    testTag = "pro_video_iso_slider"
                )
                LabeledCameraSlider(
                    label = "Cinema Shutter",
                    valueText = if (uiState.isAutoShutter) "Auto (180° Rule)" else "1/${uiState.shutterDenominator}s",
                    value = shutterIdx,
                    valueRange = 0f..(shutterChoices.size - 1).toFloat(),
                    steps = shutterChoices.size - 2,
                    onValueChange = { idx ->
                        val denom = shutterChoices[idx.roundToInt().coerceIn(0, shutterChoices.lastIndex)]
                        viewModel.setShutterDenominator(denom)
                    },
                    onReset = { viewModel.setShutterDenominator(null) },
                    testTag = "pro_video_shutter_slider"
                )
                if (uiState.capabilities.supportsManualFocus && !isCompact) {
                    LabeledCameraSlider(
                        label = "Cinema Manual Focus",
                        valueText = if (uiState.focusMode == FocusModeOption.MANUAL) {
                            "${(uiState.manualFocusDistance * 100).roundToInt()}% Macro"
                        } else if (uiState.macroModeEnabled) "Macro AF" else "Continuous AF",
                        value = uiState.manualFocusDistance,
                        valueRange = 0f..1f,
                        onValueChange = { viewModel.setManualFocusDistance(it) },
                        onReset = { viewModel.setManualFocusDistance(null) }
                    )
                }
            }

            2 -> {
                // Hardware Stabilization + Custom Video Duration Timer
                Text(
                    text = "Hardware Stabilization",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    uiState.capabilities.supportedStabilizationModes.forEach { stab ->
                        FilterChip(
                            selected = uiState.stabilizationMode == stab,
                            onClick = { viewModel.setStabilization(stab) },
                            label = { Text(stab.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GodcamForestActive,
                                selectedLabelColor = Color(0xFFFCFBF7)
                            )
                        )
                    }
                }

                Text(
                    text = "Auto-Stop Video Duration (${if (uiState.videoDurationOption == VideoDurationOption.CONTINUOUS) "Continuous" else "${uiState.effectiveTargetVideoDurationSec()}s"})",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    VideoDurationOption.entries.forEach { opt ->
                        FilterChip(
                            selected = uiState.videoDurationOption == opt,
                            onClick = { viewModel.setVideoDurationOption(opt) },
                            label = { Text(opt.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GodcamForestActive,
                                selectedLabelColor = Color(0xFFFCFBF7)
                            )
                        )
                    }
                }
                if (uiState.videoDurationOption == VideoDurationOption.CUSTOM) {
                    LabeledCameraSlider(
                        label = "Custom Duration",
                        valueText = "${uiState.customVideoDurationSec} sec",
                        value = uiState.customVideoDurationSec.toFloat(),
                        valueRange = 2f..300f,
                        onValueChange = { viewModel.setCustomVideoDurationSec(it.roundToInt()) },
                        testTag = "custom_video_duration_slider"
                    )
                }
            }

            3 -> {
                // LOG Video Profiles (G-Log, D-Log M, S-Log3, HLG) + Audio Format + Allow Background Music
                Text(
                    text = "LOG & HDR Video Color Profile (${uiState.colorProfile.label})",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ColorProfileOption.entries.forEach { profile ->
                        FilterChip(
                            selected = uiState.colorProfile == profile,
                            onClick = { viewModel.setColorProfile(profile) },
                            label = { Text(profile.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (profile.isLog) GodcamAmberGold else GodcamForestActive,
                                selectedLabelColor = if (profile.isLog) Color(0xFF141E1A) else Color(0xFFFCFBF7)
                            )
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    uiState.capabilities.supportedAudioFormats.forEach { audioFmt ->
                        FilterChip(
                            selected = uiState.audioRecordingFormat == audioFmt,
                            onClick = { viewModel.setAudioRecordingFormat(audioFmt) },
                            label = { Text(audioFmt.shortBadge) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GodcamForestActive,
                                selectedLabelColor = Color(0xFFFCFBF7)
                            )
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.MusicNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Allow Music Playback While Recording",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Switch(
                        checked = uiState.allowBackgroundMusic,
                        onCheckedChange = { viewModel.setAllowBackgroundMusic(it) },
                        modifier = Modifier.testTag("pro_video_music_switch")
                    )
                }
            }
        }
    }
}

/**
 * Pro Photo Mode Deck:
 * Provides full manual ISO, Shutter Speed, Exposure EV, Focus / Macro, White Balance / Kelvin,
 * Hardware Sensor Megapixels (12MP, 24MP, 48MP, 50MP), Metering Mode, Photo Format (JPEG/PNG/WebP/RAW), and Assists.
 */
@Composable
private fun ProPhotoSlidersDeck(
    uiState: CameraUiState,
    isCompact: Boolean,
    viewModel: GodcamViewModel
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = remember { listOf("Exposure", "Focus & WB", "MP & Format") }

    val isoRange = uiState.capabilities.isoRange
    val shutterChoices = listOf(1, 2, 4, 8, 15, 30, 60, 125, 250, 500, 1000, 2000, 4000, 8000)
    val shutterIdx = shutterChoices.indexOfFirst { it >= uiState.shutterDenominator }.let { if (it < 0) 8 else it }.toFloat()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
            .padding(horizontal = if (isCompact) 12.dp else 16.dp, vertical = if (isCompact) 6.dp else 10.dp),
        verticalArrangement = Arrangement.spacedBy(if (isCompact) 2.dp else 4.dp)
    ) {
        // Pro Photo Parameter Section Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            tabs.forEachIndexed { idx, label ->
                val active = selectedTab == idx
                Surface(
                    onClick = { selectedTab = idx },
                    shape = RoundedCornerShape(50),
                    color = if (active) GodcamForestActive else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (active) Color(0xFFFCFBF7) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.height(26.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(horizontal = 11.dp)
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (active) FontWeight.ExtraBold else FontWeight.SemiBold
                            )
                        )
                    }
                }
            }
        }

        when (selectedTab) {
            0 -> {
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
                    valueText = if (uiState.isAutoShutter) {
                        "Auto (1/250s)"
                    } else if (uiState.shutterDenominator == 1) {
                        "1.0s"
                    } else {
                        "1/${uiState.shutterDenominator}s"
                    },
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

                if (!isCompact) {
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

            1 -> {
                // Focus Mode (AF-C, Macro, Manual Focus) + White Balance & Kelvin Slider
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = uiState.macroModeEnabled,
                        onClick = { viewModel.toggleMacroMode() },
                        label = { Text("Macro Close-Up") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GodcamAmberGold,
                            selectedLabelColor = Color(0xFF141E1A)
                        )
                    )
                    WhiteBalancePreset.entries.forEach { wb ->
                        FilterChip(
                            selected = uiState.whiteBalance == wb,
                            onClick = { viewModel.setWhiteBalance(wb) },
                            label = { Text(wb.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GodcamForestActive,
                                selectedLabelColor = Color(0xFFFCFBF7)
                            )
                        )
                    }
                }

                LabeledCameraSlider(
                    label = "Color Temp (Kelvin)",
                    valueText = "${uiState.manualKelvin}K",
                    value = uiState.manualKelvin.toFloat(),
                    valueRange = 2500f..9000f,
                    onValueChange = { viewModel.setWhiteBalance(WhiteBalancePreset.MANUAL_KELVIN, it.roundToInt()) },
                    onReset = { viewModel.setWhiteBalance(WhiteBalancePreset.AUTO) },
                    testTag = "pro_photo_kelvin_slider"
                )

                if (uiState.capabilities.supportsManualFocus && !isCompact) {
                    LabeledCameraSlider(
                        label = "Manual Focus Distance",
                        valueText = if (uiState.focusMode == FocusModeOption.MANUAL) {
                            "${(uiState.manualFocusDistance * 100).roundToInt()}% Macro"
                        } else "Auto AF-C",
                        value = uiState.manualFocusDistance,
                        valueRange = 0f..1f,
                        onValueChange = { viewModel.setManualFocusDistance(it) },
                        onReset = { viewModel.setManualFocusDistance(null) },
                        testTag = "pro_photo_focus_slider"
                    )
                }
            }

            2 -> {
                // Hardware Sensor Megapixels (12MP, 24MP, 48MP, 50MP, etc.) + Photo Formats + Metering + Assists
                val (mpW, mpH) = uiState.photoMegapixel.computeDimensions(uiState.aspectRatio)
                Text(
                    text = "Sensor Megapixel Resolution (${uiState.photoMegapixel.label} • ${mpW}×${mpH})",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    uiState.capabilities.supportedMegapixels.forEach { mp ->
                        FilterChip(
                            selected = uiState.photoMegapixel == mp,
                            onClick = { viewModel.setPhotoMegapixel(mp) },
                            label = { Text(mp.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GodcamForestActive,
                                selectedLabelColor = Color(0xFFFCFBF7)
                            )
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    uiState.capabilities.supportedPhotoFormats.forEach { fmt ->
                        FilterChip(
                            selected = uiState.photoFormat == fmt,
                            onClick = { viewModel.setPhotoFormat(fmt) },
                            label = { Text(fmt.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GodcamForestActive,
                                selectedLabelColor = Color(0xFFFCFBF7)
                            )
                        )
                    }
                    MeteringModeOption.entries.forEach { meter ->
                        FilterChip(
                            selected = uiState.meteringMode == meter,
                            onClick = { viewModel.setMeteringMode(meter) },
                            label = { Text(meter.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GodcamForestActive,
                                selectedLabelColor = Color(0xFFFCFBF7)
                            )
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
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
            }
        }
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
    isCompact: Boolean = false,
    testTag: String = "quick_tool"
) {
    val tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = if (isCompact) 6.dp else 8.dp, vertical = if (isCompact) 2.dp else 4.dp)
            .testTag(testTag)
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(if (isCompact) 19.dp else 22.dp)
            )
            if (badge != null) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(start = 12.dp)
                ) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = if (isCompact) 7.5.sp else 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(if (isCompact) 1.dp else 2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = if (isCompact) 10.sp else 11.sp,
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
    var customDurInput by remember(uiState.customVideoDurationSec) {
        mutableStateOf(uiState.customVideoDurationSec.toString())
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Pro Photo & Video Studio",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                )
                Text(
                    text = "Lens #${caps.activeCameraId} (${caps.hardwareLevelLabel}) • ${caps.focalLengthMm}mm f/${caps.apertureF}",
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
                    text = "${FilterCatalog.allPresets.size} Filters",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                )
            }
        }

        // 1. Capture Mode Selector Chips (Photo, Pro Photo, Video, Pro Video, Night)
        Text("Capture Mode", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold))
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

        // 2. Macro Mode & Custom 100x Zoom Slider Quick Toggles
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = uiState.macroModeEnabled,
                onClick = { viewModel.toggleMacroMode() },
                label = { Text("Macro Close-Up Mode") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = GodcamAmberGold,
                    selectedLabelColor = Color(0xFF141E1A)
                )
            )
            FilterChip(
                selected = uiState.showZoomSlider,
                onClick = { viewModel.toggleZoomSlider() },
                label = { Text("100× Zoom Slider (${String.format(Locale.US, "%.1fx", uiState.activeZoom)})") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }

        // 2b. Sensor Megapixel Resolution (12MP, 24MP, 48MP, 50MP, etc.)
        val (sheetMpW, sheetMpH) = uiState.photoMegapixel.computeDimensions(uiState.aspectRatio)
        Text(
            text = "Sensor Megapixel Resolution (${uiState.photoMegapixel.label} • ${sheetMpW}×${sheetMpH})",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            caps.supportedMegapixels.forEach { mp ->
                FilterChip(
                    selected = uiState.photoMegapixel == mp,
                    onClick = { viewModel.setPhotoMegapixel(mp) },
                    label = { Text(mp.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }

        // 2c. LOG & HDR Video Color Profile
        Text(
            text = "LOG & HDR Video Color Profile (${uiState.colorProfile.label})",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ColorProfileOption.entries.forEach { profile ->
                FilterChip(
                    selected = uiState.colorProfile == profile,
                    onClick = { viewModel.setColorProfile(profile) },
                    label = { Text(profile.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = if (profile.isLog) GodcamAmberGold else MaterialTheme.colorScheme.primary,
                        selectedLabelColor = if (profile.isLog) Color(0xFF141E1A) else MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))

        // 3. Video Duration Limit (Continuous vs Auto-Stop & Save)
        Text(
            text = "Video Recording Duration (${if (uiState.videoDurationOption == VideoDurationOption.CONTINUOUS) "Continuous Unlimited" else "Auto-Stop & Save at ${uiState.effectiveTargetVideoDurationSec()}s"})",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            VideoDurationOption.entries.forEach { dur ->
                FilterChip(
                    selected = uiState.videoDurationOption == dur,
                    onClick = { viewModel.setVideoDurationOption(dur) },
                    label = { Text(dur.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
        if (uiState.videoDurationOption == VideoDurationOption.CUSTOM) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = customDurInput,
                    onValueChange = { txt ->
                        customDurInput = txt.filter { it.isDigit() }
                        customDurInput.toIntOrNull()?.let { sec ->
                            if (sec >= 2) viewModel.setCustomVideoDurationSec(sec)
                        }
                    },
                    label = { Text("Custom Duration (Seconds)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sheet_custom_duration_input")
                )
            }
        }

        // 4. Hardware-Supported Video Stabilization & Background Music
        Text("Hardware Video Stabilization", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            caps.supportedStabilizationModes.forEach { stab ->
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
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Allow Music Playback While Recording Video",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold)
                )
                Text(
                    text = "Keeps Spotify / Apple Music / background audio playing uninterrupted during video capture",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
            Switch(
                checked = uiState.allowBackgroundMusic,
                onCheckedChange = { viewModel.setAllowBackgroundMusic(it) },
                modifier = Modifier.testTag("sheet_allow_music_switch")
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))

        // 5. White Balance Presets & Kelvin
        Text(
            text = "White Balance (${uiState.whiteBalance.label} • ${uiState.manualKelvin}K)",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold)
        )
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

        // 6. Manual Focus Slider (when supported by hardware lens)
        if (caps.supportsManualFocus) {
            LabeledCameraSlider(
                label = "Optical Focus Distance",
                valueText = if (uiState.focusMode.name.startsWith("MANUAL")) {
                    "${(uiState.manualFocusDistance * 100).roundToInt()}% Macro"
                } else if (uiState.macroModeEnabled) {
                    "Macro AF"
                } else "Auto AF-C",
                value = uiState.manualFocusDistance,
                valueRange = 0f..1f,
                onValueChange = { viewModel.setManualFocusDistance(it) },
                onReset = { viewModel.setManualFocusDistance(null) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}
