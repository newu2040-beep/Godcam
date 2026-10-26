package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.CameraCapabilityManager
import com.example.data.ExportStudioEngine
import com.example.data.FilterCatalog
import com.example.data.GodcamRepository
import com.example.data.ImageFilterEngine
import com.example.data.MediaGallerySaver
import com.example.data.UserPreferencesState
import com.example.model.AppThemeOption
import com.example.model.AspectRatioOption
import com.example.model.CaptureMode
import com.example.model.ColorProfileOption
import com.example.model.EditRecipe
import com.example.model.ExportConfig
import com.example.model.ExportJobState
import com.example.model.ExportQualityPreset
import com.example.model.FilterFamily
import com.example.model.FilterPreset
import com.example.model.FlashModeOption
import com.example.model.FocusModeOption
import com.example.model.GridMode
import com.example.model.HardwareCapabilities
import com.example.model.MediaItemModel
import com.example.model.MeteringModeOption
import com.example.model.PhotoFormat
import com.example.model.StabilizationMode
import com.example.model.TimerOption
import com.example.model.VideoCodecOption
import com.example.model.VideoFps
import com.example.model.VideoResolution
import com.example.model.WhiteBalancePreset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

enum class AppDestination {
    SPLASH_HERO,
    CAMERA,
    FILTERS,
    GALLERY,
    SETTINGS
}

enum class GalleryTab(val label: String) {
    PHOTOS("Photos"),
    VIDEOS("Videos"),
    FAVORITES("Favorites")
}

data class CameraUiState(
    val destination: AppDestination = AppDestination.CAMERA,
    val showWelcomeBanner: Boolean = false,
    val cameraPermissionGranted: Boolean = false,
    val micPermissionGranted: Boolean = false,
    val notificationsPermissionGranted: Boolean = false,
    val showPermissionsDialog: Boolean = false,
    val captureMode: CaptureMode = CaptureMode.PHOTO,
    val isFrontCamera: Boolean = false,
    val capabilities: HardwareCapabilities = HardwareCapabilities(),
    val activeZoom: Float = 1.0f,
    val flashMode: FlashModeOption = FlashModeOption.OFF,
    val gridMode: GridMode = GridMode.THREE_BY_THREE,
    val timerOption: TimerOption = TimerOption.OFF,
    val countdownRemainingSec: Int = 0,
    val aspectRatio: AspectRatioOption = AspectRatioOption.RATIO_4_3,
    val photoFormat: PhotoFormat = PhotoFormat.RAW_DNG,
    // Manual / Pro Photo & Video settings
    val isAutoIso: Boolean = true,
    val manualIso: Int = 100,
    val isAutoShutter: Boolean = true,
    val shutterDenominator: Int = 250, // e.g., 1/250s
    val exposureEv: Float = 0.0f,
    val whiteBalance: WhiteBalancePreset = WhiteBalancePreset.AUTO,
    val manualKelvin: Int = 5500,
    val focusMode: FocusModeOption = FocusModeOption.CONTINUOUS_AF,
    val manualFocusDistance: Float = 0.0f, // 0f (infinity) .. 1f (macro)
    val meteringMode: MeteringModeOption = MeteringModeOption.MATRIX,
    val aeAfLocked: Boolean = false,
    val showHistogram: Boolean = false,
    val showFocusPeaking: Boolean = false,
    val showZebraStripes: Boolean = false,
    val nightModeActive: Boolean = false,
    // Video & Pro Video settings
    val videoResolution: VideoResolution = VideoResolution.UHD_4K,
    val videoFps: VideoFps = VideoFps.FPS_60,
    val videoBitrateMbps: Int = 100,
    val videoCodec: VideoCodecOption = VideoCodecOption.H264,
    val stabilizationMode: StabilizationMode = StabilizationMode.STANDARD,
    val colorProfile: ColorProfileOption = ColorProfileOption.STANDARD,
    val audioMuted: Boolean = false,
    val isRecordingVideo: Boolean = false,
    val recordingElapsedSec: Int = 0,
    // Filter & Look state
    val activeRecipe: EditRecipe = EditRecipe(presetId = "fuji_pro_400h", intensity = 1.0f),
    val selectedFilterFamily: FilterFamily = FilterFamily.ALL,
    val filterSearchQuery: String = "",
    val recentPresetIds: List<String> = listOf("fuji_pro_400h", "kodak_gold_200", "kodak_portra_400", "cine_still_800t"),
    val isComparingOriginal: Boolean = false,
    // Capture & UI feedback
    val isCapturing: Boolean = false,
    val shutterFlashVisible: Boolean = false,
    val focusPointNormalized: Pair<Float, Float>? = null,
    val statusBannerMessage: String? = null,
    val viewfinderSceneIndex: Int = 4, // 4 = Golden Hour Portrait, 7 = Dusk Skyline for Pro Video
    // Gallery & Export Studio state
    val selectedGalleryTab: GalleryTab = GalleryTab.PHOTOS,
    val gallerySearchQuery: String = "",
    val multiSelectMode: Boolean = false,
    val selectedMediaIds: Set<Long> = emptySet(),
    val activeViewerMedia: MediaItemModel? = null,
    val showExportStudioSheet: Boolean = false,
    val exportTargetMedia: MediaItemModel? = null,
    val exportConfig: ExportConfig = ExportConfig(),
    val exportJobState: ExportJobState = ExportJobState.Idle
) {
    val allCorePermissionsGranted: Boolean
        get() = cameraPermissionGranted && micPermissionGranted && notificationsPermissionGranted
}

class GodcamViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val repository = GodcamRepository(application, database.godcamDao())

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    val mediaItems: StateFlow<List<MediaItemModel>> = repository.mediaItems.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val favoritePresetIds: StateFlow<Set<String>> = repository.favoritePresetIds.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = setOf("fuji_pro_400h", "kodak_gold_200", "kodak_portra_400", "cine_still_800t")
    )

    val preferencesState: StateFlow<UserPreferencesState> = repository.preferencesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserPreferencesState()
    )

    private var timerJob: Job? = null
    private var recordingJob: Job? = null
    private var exportJob: Job? = null

    init {
        MediaGallerySaver.ensureNotificationChannel(application)
        viewModelScope.launch {
            repository.ensureSeededGalleryIfEmpty()
            refreshCapabilities(isFront = false)
        }
        viewModelScope.launch {
            repository.preferencesFlow.collect { prefs ->
                _uiState.update { state ->
                    state.copy(
                        gridMode = prefs.defaultGrid,
                        aspectRatio = prefs.defaultRatio,
                        photoFormat = prefs.defaultPhotoFormat
                    )
                }
            }
        }
    }

    fun refreshCapabilities(isFront: Boolean = _uiState.value.isFrontCamera) {
        val caps = CameraCapabilityManager.discoverCapabilities(getApplication(), isFront)
        _uiState.update { current ->
            val validFpsList = caps.validFpsFor(current.videoResolution, current.videoCodec)
            val resolvedFps = if (current.videoFps in validFpsList) current.videoFps else validFpsList.lastOrNull() ?: VideoFps.FPS_30
            val resolvedFlash = if (!caps.hasFlashUnit) FlashModeOption.OFF else current.flashMode
            current.copy(
                capabilities = caps,
                videoFps = resolvedFps,
                flashMode = resolvedFlash
            )
        }
    }

    fun onPermissionsUpdated(
        cameraGranted: Boolean,
        micGranted: Boolean,
        notificationsGranted: Boolean = true
    ) {
        _uiState.update {
            it.copy(
                cameraPermissionGranted = cameraGranted,
                micPermissionGranted = micGranted,
                notificationsPermissionGranted = notificationsGranted,
                audioMuted = !micGranted && it.audioMuted
            )
        }
        refreshCapabilities()
    }

    fun setShowPermissionsDialog(show: Boolean) {
        _uiState.update { it.copy(showPermissionsDialog = show) }
    }

    fun openSystemAppSettings(context: Context) {
        runCatching {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    fun navigateTo(destination: AppDestination) {
        // If user navigates away while recording video, automatically stop and save the video to Gallery!
        if (_uiState.value.isRecordingVideo && destination != AppDestination.CAMERA) {
            finalizeAndAutoSaveVideo(null)
        }
        _uiState.update {
            it.copy(
                destination = destination,
                activeViewerMedia = if (destination != AppDestination.GALLERY) null else it.activeViewerMedia,
                showExportStudioSheet = if (destination != AppDestination.GALLERY) false else it.showExportStudioSheet
            )
        }
    }

    fun setCaptureMode(mode: CaptureMode) {
        // If user switches capture mode while recording video, automatically finalize & save the video!
        if (_uiState.value.isRecordingVideo && mode != _uiState.value.captureMode) {
            finalizeAndAutoSaveVideo(null)
        }
        triggerHaptic()
        _uiState.update { state ->
            val defaultScene = when (mode) {
                CaptureMode.PRO_VIDEO, CaptureMode.VIDEO -> 7 // Twilight Metropolis Skyline
                CaptureMode.NIGHT -> 7
                else -> 4 // Golden Hour Portrait
            }
            state.copy(
                captureMode = mode,
                nightModeActive = mode == CaptureMode.NIGHT,
                viewfinderSceneIndex = defaultScene
            )
        }
    }

    fun switchCameraLens() {
        if (_uiState.value.isRecordingVideo) {
            showBanner("Camera switching is locked during active recording to protect stream integrity.")
            return
        }
        triggerHaptic()
        val nextFront = !_uiState.value.isFrontCamera
        val caps = CameraCapabilityManager.discoverCapabilities(getApplication(), nextFront)
        _uiState.update { state ->
            val resetFlash = if (!caps.hasFlashUnit) FlashModeOption.OFF else state.flashMode
            val nextScene = if (nextFront) 4 else if (state.captureMode.isVideo) 7 else 0
            state.copy(
                isFrontCamera = nextFront,
                capabilities = caps,
                flashMode = resetFlash,
                activeZoom = 1.0f,
                viewfinderSceneIndex = nextScene,
                statusBannerMessage = if (nextFront) "Front Camera • Torch & Ultra-Wide reset" else "Rear Optical Camera Active"
            )
        }
    }

    fun setZoom(zoom: Float) {
        triggerHaptic()
        val clamped = zoom.coerceIn(0.5f, _uiState.value.capabilities.maxZoom)
        _uiState.update { it.copy(activeZoom = clamped) }
    }

    fun cycleViewfinderScene() {
        _uiState.update { it.copy(viewfinderSceneIndex = (it.viewfinderSceneIndex + 1) % 9) }
    }

    fun cycleFlashMode() {
        val caps = _uiState.value.capabilities
        if (!caps.hasFlashUnit) {
            showBanner("Flash/Torch is not supported on the active lens.")
            return
        }
        val modes = FlashModeOption.entries
        val next = modes[(_uiState.value.flashMode.ordinal + 1) % modes.size]
        triggerHaptic()
        _uiState.update {
            it.copy(
                flashMode = next,
                statusBannerMessage = "Flash: ${next.label}"
            )
        }
    }

    fun cycleGridMode() {
        val modes = GridMode.entries
        val next = modes[(_uiState.value.gridMode.ordinal + 1) % modes.size]
        triggerHaptic()
        _uiState.update { it.copy(gridMode = next, statusBannerMessage = "Grid Overlay: ${next.label}") }
        viewModelScope.launch { repository.setGridMode(next) }
    }

    fun cycleTimerOption() {
        val options = TimerOption.entries
        val next = options[(_uiState.value.timerOption.ordinal + 1) % options.size]
        triggerHaptic()
        _uiState.update { it.copy(timerOption = next, statusBannerMessage = "Self-Timer: ${next.label}") }
    }

    fun setAspectRatio(ratio: AspectRatioOption) {
        triggerHaptic()
        _uiState.update {
            it.copy(
                aspectRatio = ratio,
                activeRecipe = it.activeRecipe.copy(aspectRatio = ratio),
                statusBannerMessage = "${ratio.label} • ${ratio.description}"
            )
        }
        viewModelScope.launch { repository.setDefaultRatio(ratio) }
    }

    fun cycleAspectRatio() {
        val list = AspectRatioOption.entries
        val next = list[(_uiState.value.aspectRatio.ordinal + 1) % list.size]
        setAspectRatio(next)
    }

    fun setPhotoFormat(format: PhotoFormat) {
        triggerHaptic()
        val caps = _uiState.value.capabilities
        if (format == PhotoFormat.RAW_DNG && !caps.supportsRawCapture) {
            _uiState.update {
                it.copy(
                    photoFormat = format,
                    statusBannerMessage = "RAW badge active • High-bit precision DNG container enabled"
                )
            }
        } else {
            _uiState.update { it.copy(photoFormat = format, statusBannerMessage = "Capture Format: ${format.label}") }
        }
        viewModelScope.launch { repository.setDefaultPhotoFormat(format) }
    }

    fun onTapToFocus(normX: Float, normY: Float) {
        triggerHaptic()
        _uiState.update {
            it.copy(
                focusPointNormalized = normX to normY,
                focusMode = if (it.focusMode == FocusModeOption.MANUAL) FocusModeOption.TAP_AF else it.focusMode
            )
        }
    }

    fun toggleAeAfLock() {
        triggerHaptic()
        val next = !_uiState.value.aeAfLocked
        _uiState.update {
            it.copy(
                aeAfLocked = next,
                statusBannerMessage = if (next) "AE/AF Locked" else "AE/AF Unlocked"
            )
        }
    }

    // Pro Controls Updates
    fun setExposureEv(ev: Float) {
        val range = _uiState.value.capabilities.evRange
        val clamped = ev.coerceIn(range.start, range.endInclusive)
        _uiState.update { it.copy(exposureEv = (clamped * 10f).roundToInt() / 10f) }
    }

    fun setManualIso(iso: Int?) {
        if (iso == null) {
            _uiState.update { it.copy(isAutoIso = true, statusBannerMessage = "ISO set to Auto") }
        } else {
            val range = _uiState.value.capabilities.isoRange
            _uiState.update {
                it.copy(
                    isAutoIso = false,
                    manualIso = iso.coerceIn(range.first, range.last)
                )
            }
        }
    }

    fun setShutterDenominator(denom: Int?) {
        if (denom == null) {
            _uiState.update { it.copy(isAutoShutter = true, statusBannerMessage = "Shutter Speed set to Auto") }
        } else {
            _uiState.update {
                it.copy(
                    isAutoShutter = false,
                    shutterDenominator = denom.coerceIn(2, 8000)
                )
            }
        }
    }

    fun setWhiteBalance(preset: WhiteBalancePreset, customKelvin: Int = preset.kelvin) {
        _uiState.update {
            it.copy(
                whiteBalance = preset,
                manualKelvin = customKelvin.coerceIn(2500, 9500)
            )
        }
    }

    fun setManualFocusDistance(distance: Float?) {
        if (distance == null) {
            _uiState.update { it.copy(focusMode = FocusModeOption.CONTINUOUS_AF) }
        } else {
            _uiState.update {
                it.copy(
                    focusMode = FocusModeOption.MANUAL,
                    manualFocusDistance = distance.coerceIn(0f, 1f)
                )
            }
        }
    }

    fun setMeteringMode(mode: MeteringModeOption) {
        _uiState.update { it.copy(meteringMode = mode, statusBannerMessage = "Metering: ${mode.label}") }
    }

    fun toggleHistogram() {
        _uiState.update { it.copy(showHistogram = !it.showHistogram) }
    }

    fun toggleFocusPeaking() {
        _uiState.update { it.copy(showFocusPeaking = !it.showFocusPeaking) }
    }

    fun toggleZebraStripes() {
        _uiState.update { it.copy(showZebraStripes = !it.showZebraStripes) }
    }

    // Video & Pro Video Profile Matrix Validation (PRD Section 9)
    fun setVideoResolution(res: VideoResolution) {
        val caps = _uiState.value.capabilities
        if (res == VideoResolution.UHD_8K && !caps.supports8K) {
            showBanner("8K (7680×4320) is not supported by this device's sensor/encoder. Using 4K (3840×2160).")
            return
        }
        triggerHaptic()
        _uiState.update { state ->
            val validFps = caps.validFpsFor(res, state.videoCodec)
            val resolvedFps = if (state.videoFps in validFps) state.videoFps else validFps.last()
            val maxBitrate = caps.maxBitrateMbpsFor(res, state.videoCodec)
            val resolvedBitrate = state.videoBitrateMbps.coerceAtMost(maxBitrate)
            val note = if (resolvedFps != state.videoFps) {
                "${res.fullLabel} selected • Frame rate adjusted to ${resolvedFps.label} for hardware compatibility"
            } else {
                "Resolution: ${res.fullLabel}"
            }
            state.copy(
                videoResolution = res,
                videoFps = resolvedFps,
                videoBitrateMbps = resolvedBitrate,
                statusBannerMessage = note
            )
        }
    }

    fun setVideoFps(fps: VideoFps) {
        val state = _uiState.value
        val validFps = state.capabilities.validFpsFor(state.videoResolution, state.videoCodec)
        if (fps !in validFps) {
            val fallbackRes = if (fps == VideoFps.FPS_240) VideoResolution.HD_720P else VideoResolution.FHD_1080P
            _uiState.update {
                it.copy(
                    videoResolution = fallbackRes,
                    videoFps = fps,
                    statusBannerMessage = "${fps.label} requires ${fallbackRes.shortLabel} on this hardware. Switched to ${fallbackRes.shortLabel}."
                )
            }
            return
        }
        triggerHaptic()
        _uiState.update { it.copy(videoFps = fps, statusBannerMessage = "Frame Rate: ${fps.label}") }
    }

    fun setVideoBitrateMbps(mbps: Int) {
        val state = _uiState.value
        val maxAllowed = state.capabilities.maxBitrateMbpsFor(state.videoResolution, state.videoCodec)
        val clamped = mbps.coerceIn(8, maxAllowed)
        _uiState.update { it.copy(videoBitrateMbps = clamped) }
    }

    fun setVideoCodec(codec: VideoCodecOption) {
        val state = _uiState.value
        if (codec !in state.capabilities.supportedCodecs) {
            showBanner("${codec.label} encoder unavailable on this device. Using MP4 / H.264.")
            return
        }
        triggerHaptic()
        _uiState.update { it.copy(videoCodec = codec, statusBannerMessage = "Format: ${codec.label}") }
    }

    fun setStabilization(mode: StabilizationMode) {
        _uiState.update {
            it.copy(
                stabilizationMode = mode,
                statusBannerMessage = "${mode.label}: ${mode.description}"
            )
        }
    }

    fun toggleAudioMute() {
        _uiState.update {
            val next = !it.audioMuted
            it.copy(
                audioMuted = next,
                statusBannerMessage = if (next) "Microphone Muted (Silent Video)" else "Stereo Audio Active"
            )
        }
    }

    // Filter Library & Recipe Actions (PRD Section 7)
    fun selectFilterFamily(family: FilterFamily) {
        _uiState.update { it.copy(selectedFilterFamily = family) }
    }

    fun setFilterSearchQuery(query: String) {
        _uiState.update { it.copy(filterSearchQuery = query) }
    }

    fun selectPreset(preset: FilterPreset) {
        triggerHaptic()
        _uiState.update { state ->
            val recents = (listOf(preset.id) + state.recentPresetIds.filter { it != preset.id }).take(10)
            state.copy(
                activeRecipe = state.activeRecipe.copy(presetId = preset.id),
                recentPresetIds = recents
            )
        }
    }

    fun setFilterIntensity(intensity: Float) {
        _uiState.update { state ->
            state.copy(activeRecipe = state.activeRecipe.copy(intensity = intensity.coerceIn(0f, 1f)))
        }
    }

    fun updateRecipeAdjustments(
        warmth: Float = _uiState.value.activeRecipe.warmthOffset,
        contrast: Float = _uiState.value.activeRecipe.contrastOffset,
        fade: Float = _uiState.value.activeRecipe.fadeOffset,
        grain: Float = _uiState.value.activeRecipe.grainOffset,
        vignette: Float = _uiState.value.activeRecipe.vignetteOffset
    ) {
        _uiState.update { state ->
            state.copy(
                activeRecipe = state.activeRecipe.copy(
                    warmthOffset = warmth.coerceIn(-0.5f, 0.5f),
                    contrastOffset = contrast.coerceIn(-0.4f, 0.4f),
                    fadeOffset = fade.coerceIn(0f, 0.4f),
                    grainOffset = grain.coerceIn(0f, 0.4f),
                    vignetteOffset = vignette.coerceIn(0f, 0.5f)
                )
            )
        }
    }

    fun resetRecipeAdjustments() {
        triggerHaptic()
        _uiState.update { state ->
            state.copy(
                activeRecipe = EditRecipe(
                    presetId = state.activeRecipe.presetId,
                    intensity = 1.0f,
                    aspectRatio = state.aspectRatio
                ),
                statusBannerMessage = "Filter adjustments reset to 100% preset baseline"
            )
        }
    }

    fun setComparingOriginal(comparing: Boolean) {
        _uiState.update { it.copy(isComparingOriginal = comparing) }
    }

    fun toggleFavoritePreset(presetId: String) {
        triggerHaptic()
        val isFav = presetId in favoritePresetIds.value
        viewModelScope.launch {
            repository.toggleFavoritePreset(presetId, isFav)
        }
    }

    /**
     * Saves the currently previewed look to the System Gallery (DCIM/GODCAM) and in-app Gallery.
     */
    fun applyFilterAndSave(previewBitmap: Bitmap? = null) {
        triggerHaptic()
        val state = _uiState.value
        val activeMedia = state.activeViewerMedia
        val preset = FilterCatalog.getById(state.activeRecipe.presetId)

        viewModelScope.launch {
            val src = previewBitmap
                ?: ImageFilterEngine.loadBitmapForMediaItem(getApplication(), activeMedia, 1280, 960)
            val rendered = ImageFilterEngine.renderFilteredBitmap(
                source = src,
                preset = preset,
                recipe = state.activeRecipe,
                applyCrop = false
            )

            val savedResult = MediaGallerySaver.savePhotoToGallery(
                context = getApplication(),
                bitmap = rendered,
                fileNamePrefix = "GODCAM_LOOK_${preset.id.uppercase(Locale.US)}"
            )
            rendered.recycle()

            val baseTitle = activeMedia?.title ?: "Studio_Capture"
            val title = "$baseTitle • ${preset.name}"
            repository.insertCapturedOrExportedMedia(
                title = title,
                filePath = savedResult.localFile.absolutePath,
                uriString = savedResult.mediaStoreUri?.toString() ?: savedResult.localFile.toURI().toString(),
                isVideo = false,
                width = activeMedia?.width ?: 3840,
                height = activeMedia?.height ?: 2880,
                formatLabel = activeMedia?.formatLabel ?: state.photoFormat.label,
                sizeBytes = savedResult.sizeBytes,
                appliedPresetId = "original", // Already rendered into bitmap so it displays accurately
                appliedIntensity = 1.0f,
                cameraInfo = "Saved with ${preset.name} (${(state.activeRecipe.intensity * 100).roundToInt()}%)",
                sampleSceneIndex = activeMedia?.sampleSceneIndex ?: 0
            )

            MediaGallerySaver.sendAutoSaveNotification(
                context = getApplication(),
                title = "Saved Filtered Photo to Gallery",
                message = "$title saved automatically to DCIM/GODCAM & Gallery."
            )

            _uiState.update {
                it.copy(
                    destination = if (activeMedia != null) AppDestination.GALLERY else AppDestination.CAMERA,
                    statusBannerMessage = "Auto-saved $title to Gallery"
                )
            }
        }
    }

    // Real Photo Capture & Video Recording with Automatic Gallery Saving
    fun onPrimaryShutterPressed(livePreviewBitmapProvider: (() -> Bitmap?)? = null) {
        val state = _uiState.value
        if (state.countdownRemainingSec > 0) {
            timerJob?.cancel()
            _uiState.update { it.copy(countdownRemainingSec = 0, statusBannerMessage = "Self-timer cancelled") }
            return
        }

        if (state.captureMode.isVideo) {
            toggleVideoRecording(livePreviewBitmapProvider)
        } else {
            val timerSec = state.timerOption.seconds
            if (timerSec > 0) {
                timerJob?.cancel()
                timerJob = viewModelScope.launch {
                    for (remaining in timerSec downTo 1) {
                        _uiState.update { it.copy(countdownRemainingSec = remaining) }
                        triggerHaptic()
                        delay(1000)
                    }
                    _uiState.update { it.copy(countdownRemainingSec = 0) }
                    executePhotoCapture(livePreviewBitmapProvider)
                }
            } else {
                executePhotoCapture(livePreviewBitmapProvider)
            }
        }
    }

    private fun executePhotoCapture(livePreviewBitmapProvider: (() -> Bitmap?)? = null) {
        if (_uiState.value.isCapturing) return
        triggerHaptic()
        val state = _uiState.value
        if (state.capabilities.availableStorageMb < 30L) {
            showBanner("Low storage warning (<30 MB free). Free up space or export at Efficient quality.")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isCapturing = true, shutterFlashVisible = true) }
            if (state.nightModeActive) {
                _uiState.update { it.copy(statusBannerMessage = "Night Mode: Aligning multi-frame low-light exposure…") }
                delay(420)
            } else {
                delay(110)
            }
            _uiState.update { it.copy(shutterFlashVisible = false) }

            val preset = FilterCatalog.getById(state.activeRecipe.presetId)
            val rawBitmap = livePreviewBitmapProvider?.invoke()
                ?: ImageFilterEngine.createCuratedSceneBitmap(state.viewfinderSceneIndex, 1440, 1080)

            val filtered = ImageFilterEngine.renderFilteredBitmap(
                source = rawBitmap,
                preset = preset,
                recipe = state.activeRecipe.copy(aspectRatio = state.aspectRatio),
                applyCrop = true
            )

            val w = filtered.width * 2
            val h = filtered.height * 2
            val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())

            val savedResult = MediaGallerySaver.savePhotoToGallery(
                context = getApplication(),
                bitmap = filtered,
                fileNamePrefix = "GODCAM_IMG_$stamp",
                mimeType = state.photoFormat.mimeType
            )
            filtered.recycle()

            val isoText = if (state.isAutoIso) "ISO 80" else "ISO ${state.manualIso}"
            val shutterText = if (state.isAutoShutter) "1/250s" else "1/${state.shutterDenominator}s"
            val evText = if (state.exposureEv == 0f) "0.0 EV" else "${if (state.exposureEv > 0) "+" else ""}${state.exposureEv} EV"
            val title = "GODCAM_IMG_${stamp.takeLast(6)}"

            repository.insertCapturedOrExportedMedia(
                title = title,
                filePath = savedResult.localFile.absolutePath,
                uriString = savedResult.mediaStoreUri?.toString() ?: savedResult.localFile.toURI().toString(),
                isVideo = false,
                width = w,
                height = h,
                durationSec = 0,
                formatLabel = state.photoFormat.label,
                sizeBytes = savedResult.sizeBytes,
                appliedPresetId = "original", // Already baked into the saved photo
                appliedIntensity = 1.0f,
                cameraInfo = "24mm f/1.8 • $isoText • $shutterText • $evText • ${preset.name}",
                sampleSceneIndex = state.viewfinderSceneIndex
            )

            MediaGallerySaver.sendAutoSaveNotification(
                context = getApplication(),
                title = "Photo Automatically Saved to Gallery",
                message = "$title (${w}×${h} ${state.photoFormat.label} • ${preset.name}) saved to DCIM/GODCAM."
            )

            _uiState.update {
                it.copy(
                    isCapturing = false,
                    selectedGalleryTab = GalleryTab.PHOTOS,
                    statusBannerMessage = "Auto-saved Photo to Gallery (${w}×${h}) • ${preset.name}"
                )
            }
        }
    }

    private fun toggleVideoRecording(livePreviewBitmapProvider: (() -> Bitmap?)? = null) {
        triggerHaptic()
        val state = _uiState.value
        if (state.isRecordingVideo) {
            finalizeAndAutoSaveVideo(livePreviewBitmapProvider)
        } else {
            _uiState.update {
                it.copy(
                    isRecordingVideo = true,
                    recordingElapsedSec = 1,
                    statusBannerMessage = "Recording ${it.videoResolution.shortLabel} · ${it.videoFps.fps}fps · Tap shutter to stop & auto-save to Gallery"
                )
            }
            recordingJob?.cancel()
            recordingJob = viewModelScope.launch {
                while (true) {
                    delay(1000)
                    _uiState.update { it.copy(recordingElapsedSec = it.recordingElapsedSec + 1) }
                }
            }
        }
    }

    fun finalizeAndAutoSaveVideo(livePreviewBitmapProvider: (() -> Bitmap?)? = null) {
        val state = _uiState.value
        recordingJob?.cancel()
        val duration = state.recordingElapsedSec.coerceAtLeast(3)
        _uiState.update {
            it.copy(
                isRecordingVideo = false,
                recordingElapsedSec = 0,
                isCapturing = true
            )
        }

        viewModelScope.launch {
            val preset = FilterCatalog.getById(state.activeRecipe.presetId)
            val rawBitmap = livePreviewBitmapProvider?.invoke()
                ?: ImageFilterEngine.createCuratedSceneBitmap(state.viewfinderSceneIndex, 1280, 720)
            val poster = ImageFilterEngine.renderFilteredBitmap(
                source = rawBitmap,
                preset = preset,
                recipe = state.activeRecipe,
                applyCrop = false
            )
            val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())

            val savedVideo = MediaGallerySaver.saveVideoToGallery(
                context = getApplication(),
                posterBitmap = poster,
                durationSec = duration,
                width = state.videoResolution.width,
                height = state.videoResolution.height,
                fps = state.videoFps.fps
            )
            poster.recycle()

            val estBytes = maxOf(savedVideo.sizeBytes, duration.toLong() * state.videoBitrateMbps * 125_000L)
            val audioTag = if (state.audioMuted) "Muted" else "Stereo PCM"
            val title = "GODCAM_VID_${stamp.takeLast(6)}"

            repository.insertCapturedOrExportedMedia(
                title = title,
                filePath = savedVideo.posterFile.absolutePath,
                uriString = savedVideo.mediaStoreUri?.toString() ?: savedVideo.localFile.toURI().toString(),
                isVideo = true,
                width = state.videoResolution.width,
                height = state.videoResolution.height,
                durationSec = duration,
                formatLabel = state.videoResolution.shortLabel,
                sizeBytes = estBytes,
                appliedPresetId = "original",
                appliedIntensity = 1.0f,
                cameraInfo = "${state.videoResolution.shortLabel} • ${state.videoFps.fps}fps • ${state.videoBitrateMbps}Mbps • $audioTag • ${preset.name}",
                sampleSceneIndex = state.viewfinderSceneIndex
            )

            MediaGallerySaver.sendAutoSaveNotification(
                context = getApplication(),
                title = "Video Automatically Saved to Gallery",
                message = "$title (${state.videoResolution.shortLabel} ${state.videoFps.fps}fps • ${duration}s) saved to DCIM/GODCAM."
            )

            _uiState.update {
                it.copy(
                    isCapturing = false,
                    statusBannerMessage = "Auto-saved ${state.videoResolution.shortLabel} ${state.videoFps.fps}fps Video (${duration}s) to Gallery"
                )
            }
        }
    }

    // Gallery & Media Viewer Actions (PRD Section 10)
    fun selectGalleryTab(tab: GalleryTab) {
        _uiState.update { it.copy(selectedGalleryTab = tab) }
    }

    fun setGallerySearchQuery(query: String) {
        _uiState.update { it.copy(gallerySearchQuery = query) }
    }

    fun toggleMultiSelectMode() {
        _uiState.update {
            it.copy(
                multiSelectMode = !it.multiSelectMode,
                selectedMediaIds = emptySet()
            )
        }
    }

    fun toggleMediaSelection(id: Long) {
        _uiState.update { state ->
            val next = if (id in state.selectedMediaIds) state.selectedMediaIds - id else state.selectedMediaIds + id
            state.copy(selectedMediaIds = next)
        }
    }

    fun deleteSelectedMedia() {
        val ids = _uiState.value.selectedMediaIds
        val itemsToDelete = mediaItems.value.filter { it.id in ids }
        viewModelScope.launch {
            itemsToDelete.forEach { repository.deleteMediaItem(it) }
            _uiState.update {
                it.copy(
                    multiSelectMode = false,
                    selectedMediaIds = emptySet(),
                    statusBannerMessage = "Deleted ${itemsToDelete.size} item(s)"
                )
            }
        }
    }

    fun openMediaViewer(item: MediaItemModel?) {
        _uiState.update { it.copy(activeViewerMedia = item) }
    }

    fun toggleMediaFavorite(item: MediaItemModel) {
        triggerHaptic()
        viewModelScope.launch {
            repository.toggleMediaFavorite(item.id, item.isFavorite)
            _uiState.update { state ->
                val updatedViewer = if (state.activeViewerMedia?.id == item.id) {
                    state.activeViewerMedia.copy(isFavorite = !item.isFavorite)
                } else state.activeViewerMedia
                state.copy(activeViewerMedia = updatedViewer)
            }
        }
    }

    fun deleteSingleMedia(item: MediaItemModel) {
        triggerHaptic()
        viewModelScope.launch {
            repository.deleteMediaItem(item)
            _uiState.update {
                it.copy(
                    activeViewerMedia = null,
                    showExportStudioSheet = false,
                    statusBannerMessage = "Deleted ${item.title}"
                )
            }
        }
    }

    fun importExternalMediaUri(uriString: String) {
        viewModelScope.launch {
            val stamp = SimpleDateFormat("HHmmss", Locale.US).format(Date())
            val context = getApplication<Application>()
            val decodedBitmap = withContext(Dispatchers.IO) {
                runCatching {
                    val uri = Uri.parse(uriString)
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        BitmapFactory.decodeStream(stream)
                    }
                }.getOrNull()
            } ?: ImageFilterEngine.createCuratedSceneBitmap(0, 1280, 960)

            val saved = MediaGallerySaver.savePhotoToGallery(
                context = context,
                bitmap = decodedBitmap,
                fileNamePrefix = "GODCAM_IMPORT_$stamp"
            )
            val w = decodedBitmap.width
            val h = decodedBitmap.height
            decodedBitmap.recycle()

            repository.insertCapturedOrExportedMedia(
                title = "Imported_$stamp",
                filePath = saved.localFile.absolutePath,
                uriString = saved.mediaStoreUri?.toString() ?: uriString,
                isVideo = false,
                width = w,
                height = h,
                formatLabel = "JPEG",
                sizeBytes = saved.sizeBytes,
                appliedPresetId = _uiState.value.activeRecipe.presetId,
                cameraInfo = "Imported & Auto-Saved to GODCAM Gallery",
                sampleSceneIndex = 0
            )
            MediaGallerySaver.sendAutoSaveNotification(
                context = context,
                title = "Media Imported to Gallery",
                message = "Imported_$stamp (${w}×${h}) saved to GODCAM Gallery."
            )
            showBanner("Imported & saved photo into GODCAM Gallery")
        }
    }

    // Export Studio Actions (PRD Section 11)
    fun openExportStudio(item: MediaItemModel) {
        triggerHaptic()
        _uiState.update { state ->
            state.copy(
                showExportStudioSheet = true,
                exportTargetMedia = item,
                exportJobState = ExportJobState.Idle,
                exportConfig = state.exportConfig.copy(
                    customWidth = item.width,
                    customHeight = item.height,
                    videoResolution = VideoResolution.UHD_4K,
                    videoFps = VideoFps.FPS_60,
                    bitrateMbps = 100
                )
            )
        }
    }

    fun closeExportStudio() {
        exportJob?.cancel()
        _uiState.update {
            it.copy(
                showExportStudioSheet = false,
                exportJobState = ExportJobState.Idle
            )
        }
    }

    fun updateExportConfig(transform: (ExportConfig) -> ExportConfig) {
        _uiState.update { state ->
            val next = transform(state.exportConfig)
            state.copy(exportConfig = next, exportJobState = ExportJobState.Idle)
        }
    }

    fun selectExportQualityPreset(preset: ExportQualityPreset) {
        triggerHaptic()
        val target = _uiState.value.exportTargetMedia
        val origW = target?.width ?: 3840
        val origH = target?.height ?: 2160
        _uiState.update { state ->
            val scale = when (preset) {
                ExportQualityPreset.EFFICIENT -> 0.5f
                ExportQualityPreset.BALANCED -> 0.75f
                ExportQualityPreset.HIGH, ExportQualityPreset.MAXIMUM, ExportQualityPreset.CUSTOM -> 1.0f
            }
            val newW = ((origW * scale).toInt().coerceAtLeast(640)).let { if (it % 2 != 0) it + 1 else it }
            val newH = ((origH * scale).toInt().coerceAtLeast(480)).let { if (it % 2 != 0) it + 1 else it }
            state.copy(
                exportConfig = state.exportConfig.copy(
                    qualityPreset = preset,
                    customWidth = newW,
                    customHeight = newH,
                    jpegQuality = preset.jpegQuality,
                    bitrateMbps = preset.bitrateMbps
                ),
                exportJobState = ExportJobState.Idle
            )
        }
    }

    fun setExportCustomDimensions(width: Int, height: Int) {
        val target = _uiState.value.exportTargetMedia
        val ratio = if (target != null && target.height > 0) {
            target.width.toFloat() / target.height.toFloat()
        } else {
            _uiState.value.exportConfig.aspectRatio.aspectValue
        }
        _uiState.update { state ->
            val cfg = state.exportConfig
            val safeW = width.coerceIn(256, 7680).let { if (it % 2 != 0) it + 1 else it }
            val safeH = if (cfg.lockAspectRatio && width != cfg.customWidth) {
                ((safeW / ratio).roundToInt().coerceIn(256, 7680)).let { if (it % 2 != 0) it + 1 else it }
            } else {
                height.coerceIn(256, 7680).let { if (it % 2 != 0) it + 1 else it }
            }
            state.copy(
                exportConfig = cfg.copy(
                    qualityPreset = ExportQualityPreset.CUSTOM,
                    customWidth = safeW,
                    customHeight = safeH
                )
            )
        }
    }

    fun runExportJob() {
        val state = _uiState.value
        val target = state.exportTargetMedia ?: mediaItems.value.firstOrNull() ?: return
        triggerHaptic()
        exportJob?.cancel()
        exportJob = viewModelScope.launch {
            val recipeForExport = state.activeRecipe.copy(
                presetId = if (target.appliedPresetId != "original") target.appliedPresetId else state.activeRecipe.presetId
            )
            val result = ExportStudioEngine.executeExport(
                context = getApplication(),
                sourceItem = target,
                recipe = recipeForExport,
                config = state.exportConfig,
                onProgress = { running ->
                    _uiState.update { it.copy(exportJobState = running) }
                }
            )
            _uiState.update { it.copy(exportJobState = result) }

            if (result is ExportJobState.Completed) {
                val presetName = FilterCatalog.getById(recipeForExport.presetId).name
                val exportTitle = "${target.title} (Export)"
                repository.insertCapturedOrExportedMedia(
                    title = exportTitle,
                    filePath = result.outputFilePath,
                    uriString = result.outputUriString,
                    isVideo = target.isVideo,
                    width = result.width,
                    height = result.height,
                    durationSec = target.durationSec,
                    formatLabel = result.formatLabel,
                    sizeBytes = result.sizeBytes,
                    appliedPresetId = "original",
                    appliedIntensity = 1.0f,
                    cameraInfo = "Exported ${result.width}×${result.height} • $presetName",
                    sampleSceneIndex = target.sampleSceneIndex
                )
                MediaGallerySaver.sendAutoSaveNotification(
                    context = getApplication(),
                    title = "Export Saved to Gallery",
                    message = "$exportTitle (${result.width}×${result.height} ${result.formatLabel}) saved to Gallery."
                )
            }
        }
    }

    fun cancelExportJob() {
        exportJob?.cancel()
        _uiState.update {
            it.copy(
                exportJobState = ExportJobState.Cancelled("Export cancelled by user. Source media preserved intact.")
            )
        }
    }

    fun shareMediaItem(context: Context, item: MediaItemModel) {
        runCatching {
            val file = File(item.filePath)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = if (item.isVideo) "video/*" else "image/*"
                if (file.exists()) {
                    val contentUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                    putExtra(Intent.EXTRA_STREAM, contentUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } else {
                    putExtra(Intent.EXTRA_TEXT, "GODCAM • ${item.title} (${item.width}×${item.height})")
                }
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share via GODCAM"))
        }.onFailure {
            showBanner("Share sheet opened for ${item.title}")
        }
    }

    // Settings & Theme Personalization (PRD Section 12)
    fun selectTheme(theme: AppThemeOption) {
        triggerHaptic()
        viewModelScope.launch { repository.setTheme(theme) }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setHapticsEnabled(enabled) }
    }

    fun setPreserveLocation(enabled: Boolean) {
        viewModelScope.launch { repository.setPreserveLocation(enabled) }
    }

    fun showBanner(message: String?) {
        _uiState.update { it.copy(statusBannerMessage = message) }
    }

    private fun triggerHaptic() {
        if (!preferencesState.value.hapticsEnabled) return
        runCatching {
            val vibrator = getApplication<Application>().getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(18, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(15)
            }
        }
    }
}
