package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
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
import com.example.data.ShutterSoundEngine
import com.example.data.UserPreferencesState
import com.example.model.AppThemeOption
import com.example.model.AspectRatioOption
import com.example.model.AudioRecordingFormat
import com.example.model.CaptureMode
import com.example.model.ColorProfileOption
import com.example.model.CompactLayoutMode
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
import com.example.model.PhotoMegapixelOption
import com.example.model.ProOpticalState
import com.example.model.ShutterSoundStyle
import com.example.model.StabilizationMode
import android.media.AudioManager
import com.example.model.TimerOption
import com.example.model.VideoCodecOption
import com.example.model.VideoDurationOption
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
import kotlin.math.atan2
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
    val cameraPermissionGranted: Boolean = false,
    val micPermissionGranted: Boolean = false,
    val notificationsPermissionGranted: Boolean = false,
    val showPermissionsDialog: Boolean = false,
    val captureMode: CaptureMode = CaptureMode.PHOTO,
    val isFrontCamera: Boolean = false,
    val capabilities: HardwareCapabilities = HardwareCapabilities(),
    val deviceTiltDegrees: Float = 0.0f,
    val activeZoom: Float = 1.0f,
    val flashMode: FlashModeOption = FlashModeOption.OFF,
    val gridMode: GridMode = GridMode.THREE_BY_THREE,
    val timerOption: TimerOption = TimerOption.OFF,
    val countdownRemainingSec: Int = 0,
    val aspectRatio: AspectRatioOption = AspectRatioOption.RATIO_4_3,
    val photoFormat: PhotoFormat = PhotoFormat.JPEG,
    val photoMegapixel: PhotoMegapixelOption = PhotoMegapixelOption.MP_50,
    val shutterSoundStyle: ShutterSoundStyle = ShutterSoundStyle.FUJI_MECHANICAL,
    val audioRecordingFormat: AudioRecordingFormat = AudioRecordingFormat.AAC_LC_STEREO_48K,
    val autoSaveToGallery: Boolean = true,
    // Manual / Pro Photo & Video settings
    val isAutoIso: Boolean = true,
    val manualIso: Int = 100,
    val isAutoShutter: Boolean = true,
    val shutterDenominator: Int = 250, // e.g., 1/250s
    val exposureEv: Float = 0.0f,
    val whiteBalance: WhiteBalancePreset = WhiteBalancePreset.AUTO,
    val manualKelvin: Int = 5500,
    val focusMode: FocusModeOption = FocusModeOption.CONTINUOUS_AF,
    val manualFocusDistance: Float = 0.0f,
    val macroModeEnabled: Boolean = false,
    val showZoomSlider: Boolean = false,
    val meteringMode: MeteringModeOption = MeteringModeOption.MATRIX,
    val aeAfLocked: Boolean = false,
    val showHistogram: Boolean = false,
    val showFocusPeaking: Boolean = false,
    val showZebraStripes: Boolean = false,
    val nightModeActive: Boolean = false,
    val showLiveFilterStrip: Boolean = false,
    val compactLayoutMode: CompactLayoutMode = CompactLayoutMode.AUTO,
    // Video & Pro Video settings
    val videoResolution: VideoResolution = VideoResolution.FHD_1080P,
    val useCustomVideoSize: Boolean = false,
    val customVideoWidth: Int = 1920,
    val customVideoHeight: Int = 1080,
    val videoFps: VideoFps = VideoFps.FPS_30,
    val videoBitrateMbps: Int = 40,
    val videoCodec: VideoCodecOption = VideoCodecOption.H264,
    val stabilizationMode: StabilizationMode = StabilizationMode.STANDARD,
    val colorProfile: ColorProfileOption = ColorProfileOption.STANDARD,
    val videoDurationOption: VideoDurationOption = VideoDurationOption.CONTINUOUS,
    val customVideoDurationSec: Int = 15,
    val allowBackgroundMusic: Boolean = true,
    val audioMuted: Boolean = false,
    val isRecordingVideo: Boolean = false,
    val isRecordingPaused: Boolean = false,
    val recordingElapsedSec: Int = 0,
    // Filter & Look state
    val activeRecipe: EditRecipe = EditRecipe(presetId = "fuji_pro_400h", intensity = 1.0f),
    val selectedFilterFamily: FilterFamily = FilterFamily.ALL,
    val filterSearchQuery: String = "",
    val recentPresetIds: List<String> = listOf("fuji_pro_400h", "kodak_gold_200", "kodak_portra_400", "cine_still_800t", "bw_tri_x_400"),
    val isComparingOriginal: Boolean = false,
    // Capture & UI feedback
    val isCapturing: Boolean = false,
    val shutterFlashVisible: Boolean = false,
    val focusPointNormalized: Pair<Float, Float>? = null,
    val statusBannerMessage: String? = null,
    val pendingUnsavedMedia: MediaItemModel? = null,
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

    val isVideoFamilyMode: Boolean
        get() = captureMode == CaptureMode.VIDEO || captureMode == CaptureMode.PRO_VIDEO

    fun toProOpticalState(): ProOpticalState {
        return ProOpticalState(
            isAutoIso = isAutoIso,
            manualIso = manualIso,
            isAutoShutter = isAutoShutter,
            shutterDenominator = shutterDenominator,
            exposureEv = exposureEv,
            whiteBalance = whiteBalance,
            manualKelvin = manualKelvin,
            colorProfile = colorProfile,
            zoomRatio = activeZoom,
            hardwareOpticalMaxZoom = capabilities.hardwareOpticalMaxZoom
        )
    }

    fun effectiveVideoDimensions(): Pair<Int, Int> {
        return if (useCustomVideoSize) {
            val w = customVideoWidth.coerceIn(320, 3840).let { if (it % 2 != 0) it + 1 else it }
            val h = customVideoHeight.coerceIn(320, 3840).let { if (it % 2 != 0) it + 1 else it }
            w to h
        } else {
            aspectRatio.computeDimensionsForLongEdge(videoResolution.longEdge)
        }
    }

    fun effectiveTargetVideoDurationSec(): Int {
        return when (videoDurationOption) {
            VideoDurationOption.CONTINUOUS -> 0
            VideoDurationOption.CUSTOM -> customVideoDurationSec.coerceIn(1, 3600)
            else -> videoDurationOption.seconds
        }
    }
}

class GodcamViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val database = AppDatabase.getInstance(application)
    private val repository = GodcamRepository(application, database.godcamDao())
    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    // Live camera frame cached from PreviewView so FiltersScreen & Histogram operate on real camera pixels
    private val _latestCameraFrame = MutableStateFlow<Bitmap?>(null)
    val latestCameraFrame: StateFlow<Bitmap?> = _latestCameraFrame.asStateFlow()

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
    private var lastNonSilentShutterStyle: ShutterSoundStyle = ShutterSoundStyle.FUJI_MECHANICAL

    init {
        MediaGallerySaver.ensureNotificationChannel(application)
        // Register real accelerometer sensor for live horizon level telemetry
        sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let { accel ->
            sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_UI)
        }

        viewModelScope.launch {
            repository.cleanAndVerifyRealLibrary()
            refreshCapabilities(isFront = false)
        }
        viewModelScope.launch {
            repository.preferencesFlow.collect { prefs ->
                if (prefs.shutterSoundStyle != ShutterSoundStyle.OFF) {
                    lastNonSilentShutterStyle = prefs.shutterSoundStyle
                }
                _uiState.update { state ->
                    val validAudio = if (prefs.audioRecordingFormat in state.capabilities.supportedAudioFormats) {
                        prefs.audioRecordingFormat
                    } else {
                        state.capabilities.supportedAudioFormats.firstOrNull() ?: AudioRecordingFormat.AAC_LC_STEREO_48K
                    }
                    val validMp = if (prefs.defaultMegapixel in state.capabilities.supportedMegapixels) {
                        prefs.defaultMegapixel
                    } else {
                        state.capabilities.supportedMegapixels.lastOrNull() ?: PhotoMegapixelOption.MP_50
                    }
                    state.copy(
                        gridMode = prefs.defaultGrid,
                        aspectRatio = prefs.defaultRatio,
                        photoFormat = prefs.defaultPhotoFormat,
                        photoMegapixel = validMp,
                        shutterSoundStyle = prefs.shutterSoundStyle,
                        audioRecordingFormat = validAudio,
                        audioMuted = validAudio == AudioRecordingFormat.MUTED,
                        compactLayoutMode = prefs.compactLayoutMode,
                        autoSaveToGallery = prefs.autoSaveToGallery
                    )
                }
            }
        }
        // Periodic real-time hardware telemetry refresh (battery temp, thermal state, storage)
        viewModelScope.launch {
            while (true) {
                delay(6000)
                refreshCapabilities(_uiState.value.isFrontCamera)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val angleDeg = Math.toDegrees(atan2(x.toDouble(), y.toDouble())).toFloat()
            val smoothed = ((angleDeg * 10f).roundToInt() / 10f).coerceIn(-90f, 90f)
            _uiState.update { it.copy(deviceTiltDegrees = smoothed) }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    fun updateLatestCameraFrame(bitmap: Bitmap?) {
        if (bitmap != null && !bitmap.isRecycled) {
            _latestCameraFrame.value = bitmap
        }
    }

    fun refreshCapabilities(isFront: Boolean = _uiState.value.isFrontCamera) {
        val caps = CameraCapabilityManager.discoverCapabilities(getApplication(), isFront)
        _uiState.update { current ->
            val supportedRes = caps.supportedVideoResolutions
            val resolvedRes = if (current.videoResolution in supportedRes) {
                current.videoResolution
            } else {
                supportedRes.lastOrNull() ?: VideoResolution.FHD_1080P
            }
            val validFpsList = caps.validFpsFor(resolvedRes, current.videoCodec)
            val resolvedFps = if (current.videoFps in validFpsList) {
                current.videoFps
            } else {
                validFpsList.lastOrNull() ?: VideoFps.FPS_30
            }
            val maxBitrate = caps.maxBitrateMbpsFor(resolvedRes, current.videoCodec)
            val resolvedBitrate = current.videoBitrateMbps.coerceIn(8, maxBitrate)
            val resolvedFlash = if (!caps.hasFlashUnit) FlashModeOption.OFF else current.flashMode
            val resolvedFormat = if (current.photoFormat !in caps.supportedPhotoFormats) {
                caps.supportedPhotoFormats.firstOrNull() ?: PhotoFormat.JPEG
            } else {
                current.photoFormat
            }
            val resolvedAudio = if (current.audioRecordingFormat in caps.supportedAudioFormats) {
                current.audioRecordingFormat
            } else {
                caps.supportedAudioFormats.firstOrNull() ?: AudioRecordingFormat.AAC_LC_STEREO_48K
            }
            val resolvedMp = if (current.photoMegapixel in caps.supportedMegapixels) {
                current.photoMegapixel
            } else {
                caps.supportedMegapixels.lastOrNull() ?: PhotoMegapixelOption.MP_50
            }
            current.copy(
                capabilities = caps,
                videoResolution = resolvedRes,
                videoFps = resolvedFps,
                videoBitrateMbps = resolvedBitrate,
                flashMode = resolvedFlash,
                photoFormat = resolvedFormat,
                photoMegapixel = resolvedMp,
                audioRecordingFormat = resolvedAudio,
                audioMuted = resolvedAudio == AudioRecordingFormat.MUTED
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

    fun toggleLiveFilterStrip() {
        triggerHaptic()
        _uiState.update { it.copy(showLiveFilterStrip = !it.showLiveFilterStrip) }
    }

    fun refreshHardwareTelemetryNow() {
        refreshCapabilities()
        showBanner("Hardware telemetry refreshed from Camera2, MediaCodec, AudioManager & BatteryManager")
    }

    suspend fun runCountdownTimerIfNeeded(): Boolean {
        val timerSec = _uiState.value.timerOption.seconds
        if (timerSec <= 0) return true
        if (_uiState.value.countdownRemainingSec > 0) {
            timerJob?.cancel()
            _uiState.update { it.copy(countdownRemainingSec = 0, statusBannerMessage = "Self-Timer Cancelled") }
            return false
        }
        for (sec in timerSec downTo 1) {
            _uiState.update { it.copy(countdownRemainingSec = sec) }
            triggerHaptic()
            ShutterSoundEngine.playTimerCountdownTick(
                style = _uiState.value.shutterSoundStyle,
                isFinalSecond = (sec == 1)
            )
            delay(1000)
        }
        _uiState.update { it.copy(countdownRemainingSec = 0) }
        return true
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
        if (_uiState.value.isRecordingVideo && destination != AppDestination.CAMERA) {
            finalizeAndAutoSaveVideo(existingMp4File = null, livePreviewBitmap = _latestCameraFrame.value)
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
        if (_uiState.value.isRecordingVideo && mode != _uiState.value.captureMode) {
            finalizeAndAutoSaveVideo(existingMp4File = null, livePreviewBitmap = _latestCameraFrame.value)
        }
        triggerHaptic()
        _uiState.update { state ->
            state.copy(
                captureMode = mode,
                nightModeActive = mode == CaptureMode.NIGHT
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
        val supportedRes = caps.supportedVideoResolutions
        _uiState.update { state ->
            val resolvedRes = if (state.videoResolution in supportedRes) state.videoResolution else supportedRes.lastOrNull() ?: VideoResolution.FHD_1080P
            val resetFlash = if (!caps.hasFlashUnit) FlashModeOption.OFF else state.flashMode
            state.copy(
                isFrontCamera = nextFront,
                capabilities = caps,
                videoResolution = resolvedRes,
                flashMode = resetFlash,
                activeZoom = 1.0f,
                statusBannerMessage = "Lens ID ${caps.activeCameraId} (${caps.hardwareLevelLabel}) • Max ${resolvedRes.shortLabel}"
            )
        }
    }

    fun setZoom(zoom: Float) {
        triggerHaptic()
        val caps = _uiState.value.capabilities
        val clamped = zoom.coerceIn(caps.minZoom, caps.maxZoom)
        _uiState.update { it.copy(activeZoom = clamped) }
    }

    fun cycleFlashMode() {
        val caps = _uiState.value.capabilities
        if (!caps.hasFlashUnit) {
            showBanner("Hardware Flash/Torch is not supported on Lens ID ${caps.activeCameraId}.")
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

    fun setPhotoFormat(format: PhotoFormat) {
        triggerHaptic()
        val caps = _uiState.value.capabilities
        if (format == PhotoFormat.RAW_DNG && !caps.supportsRawCapture) {
            showBanner("Hardware RAW/DNG sensor readout is not supported on Lens ID ${caps.activeCameraId}. Using JPEG.")
            return
        }
        _uiState.update { it.copy(photoFormat = format, statusBannerMessage = "Capture Format: ${format.label}") }
        viewModelScope.launch { repository.setDefaultPhotoFormat(format) }
    }

    fun setPhotoMegapixel(option: PhotoMegapixelOption) {
        triggerHaptic()
        val supported = _uiState.value.capabilities.supportedMegapixels
        val resolved = if (option in supported) option else supported.lastOrNull() ?: PhotoMegapixelOption.MP_50
        val (outW, outH) = resolved.computeDimensions(_uiState.value.aspectRatio)
        _uiState.update {
            it.copy(
                photoMegapixel = resolved,
                statusBannerMessage = "Sensor Resolution: ${resolved.label} (${outW}×${outH} • ${resolved.description})"
            )
        }
        viewModelScope.launch { repository.setDefaultMegapixel(resolved) }
    }

    fun cyclePhotoMegapixel() {
        val supported = _uiState.value.capabilities.supportedMegapixels.ifEmpty {
            listOf(PhotoMegapixelOption.MP_12, PhotoMegapixelOption.MP_24, PhotoMegapixelOption.MP_48, PhotoMegapixelOption.MP_50)
        }
        val currentIdx = supported.indexOf(_uiState.value.photoMegapixel).coerceAtLeast(0)
        val next = supported[(currentIdx + 1) % supported.size]
        setPhotoMegapixel(next)
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
            _uiState.update {
                it.copy(
                    focusMode = FocusModeOption.CONTINUOUS_AF,
                    macroModeEnabled = false
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    focusMode = FocusModeOption.MANUAL,
                    macroModeEnabled = distance >= 0.85f,
                    manualFocusDistance = distance.coerceIn(0f, 1f)
                )
            }
        }
    }

    fun toggleMacroMode() {
        val next = !_uiState.value.macroModeEnabled
        setMacroMode(next)
    }

    fun setMacroMode(enabled: Boolean) {
        triggerHaptic()
        _uiState.update { state ->
            val nextZoom = if (enabled && state.activeZoom < 1.2f) {
                1.4f.coerceIn(state.capabilities.minZoom, state.capabilities.maxZoom)
            } else if (!enabled && kotlin.math.abs(state.activeZoom - 1.4f) < 0.08f) {
                1.0f
            } else {
                state.activeZoom
            }
            state.copy(
                macroModeEnabled = enabled,
                focusMode = if (enabled) FocusModeOption.MACRO else FocusModeOption.CONTINUOUS_AF,
                manualFocusDistance = if (enabled) 0.92f else 0.0f,
                activeZoom = nextZoom,
                statusBannerMessage = if (enabled) {
                    "Macro Close-Up Mode Active • Optical Close Focus (${state.capabilities.minFocusDistanceDiopters.roundToInt()}D)"
                } else {
                    "Macro Mode Off • Standard Continuous AF-C"
                }
            )
        }
    }

    fun toggleZoomSlider() {
        triggerHaptic()
        _uiState.update { it.copy(showZoomSlider = !it.showZoomSlider) }
    }

    fun setShowZoomSlider(show: Boolean) {
        _uiState.update { it.copy(showZoomSlider = show) }
    }

    fun setMeteringMode(mode: MeteringModeOption) {
        triggerHaptic()
        _uiState.update { it.copy(meteringMode = mode, statusBannerMessage = "Metering: ${mode.label}") }
    }

    fun setColorProfile(profile: ColorProfileOption) {
        triggerHaptic()
        _uiState.update {
            it.copy(
                colorProfile = profile,
                statusBannerMessage = "Video Color Profile: ${profile.label} — ${profile.description}"
            )
        }
    }

    fun toggleLogVideoProfile() {
        triggerHaptic()
        val current = _uiState.value.colorProfile
        val next = when (current) {
            ColorProfileOption.STANDARD -> ColorProfileOption.FLAT_NATURAL
            ColorProfileOption.FLAT_NATURAL -> ColorProfileOption.CINE_D_LOG
            ColorProfileOption.CINE_D_LOG -> ColorProfileOption.S_LOG3
            ColorProfileOption.S_LOG3 -> ColorProfileOption.HLG_HDR
            ColorProfileOption.HLG_HDR -> ColorProfileOption.STANDARD
        }
        setColorProfile(next)
    }

    fun setVideoDurationOption(option: VideoDurationOption) {
        triggerHaptic()
        _uiState.update { state ->
            val msg = when (option) {
                VideoDurationOption.CONTINUOUS -> "Video Duration: Continuous (Record until stopped)"
                VideoDurationOption.CUSTOM -> "Video Duration: Custom (${state.customVideoDurationSec}s auto-stop & save)"
                else -> "Video Duration: ${option.label} (Auto-stops & saves)"
            }
            state.copy(
                videoDurationOption = option,
                statusBannerMessage = msg
            )
        }
    }

    fun setCustomVideoDurationSec(seconds: Int) {
        val safeSec = seconds.coerceIn(1, 3600)
        _uiState.update {
            it.copy(
                videoDurationOption = VideoDurationOption.CUSTOM,
                customVideoDurationSec = safeSec,
                statusBannerMessage = "Custom Video Duration: ${safeSec}s (Auto-stop & save)"
            )
        }
    }

    fun setAllowBackgroundMusic(allow: Boolean) {
        triggerHaptic()
        if (allow) {
            runCatching {
                val am = getApplication<Application>().getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                am?.mode = AudioManager.MODE_NORMAL
            }
        }
        _uiState.update {
            it.copy(
                allowBackgroundMusic = allow,
                statusBannerMessage = if (allow) {
                    "Music Playback While Recording: Enabled (Background music continues playing)"
                } else {
                    "Music Playback While Recording: Disabled"
                }
            )
        }
    }

    fun toggleVideoPauseResume() {
        if (!_uiState.value.isRecordingVideo) return
        triggerHaptic()
        val nextPaused = !_uiState.value.isRecordingPaused
        _uiState.update {
            it.copy(
                isRecordingPaused = nextPaused,
                statusBannerMessage = if (nextPaused) {
                    "Recording Paused at ${it.recordingElapsedSec}s — Tap Resume to continue"
                } else {
                    "Recording Resumed"
                }
            )
        }
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
        if (res !in caps.supportedVideoResolutions) {
            val highest = caps.supportedVideoResolutions.lastOrNull() ?: VideoResolution.FHD_1080P
            showBanner("${res.fullLabel} is not supported by Lens ID ${caps.activeCameraId}. Highest hardware-supported resolution is ${highest.fullLabel}.")
            return
        }
        triggerHaptic()
        _uiState.update { state ->
            val validFps = caps.validFpsFor(res, state.videoCodec)
            val resolvedFps = if (state.videoFps in validFps) state.videoFps else validFps.last()
            val maxBitrate = caps.maxBitrateMbpsFor(res, state.videoCodec)
            val resolvedBitrate = state.videoBitrateMbps.coerceAtMost(maxBitrate)
            val (outW, outH) = state.aspectRatio.computeDimensionsForLongEdge(res.longEdge)
            val note = if (resolvedFps != state.videoFps) {
                "${res.shortLabel} (${outW}×${outH}) • Frame rate matched to ${resolvedFps.label}"
            } else {
                "Video Size: ${res.shortLabel} (${outW}×${outH} • ${state.aspectRatio.label})"
            }
            state.copy(
                videoResolution = res,
                useCustomVideoSize = false,
                customVideoWidth = outW,
                customVideoHeight = outH,
                videoFps = resolvedFps,
                videoBitrateMbps = resolvedBitrate,
                statusBannerMessage = note
            )
        }
    }

    fun setUseCustomVideoSize(enabled: Boolean) {
        triggerHaptic()
        _uiState.update { state ->
            val (defaultW, defaultH) = state.aspectRatio.computeDimensionsForLongEdge(state.videoResolution.longEdge)
            val w = if (enabled) state.customVideoWidth else defaultW
            val h = if (enabled) state.customVideoHeight else defaultH
            state.copy(
                useCustomVideoSize = enabled,
                customVideoWidth = w,
                customVideoHeight = h,
                statusBannerMessage = if (enabled) {
                    "Custom Video Size: ${w}×${h} px"
                } else {
                    "Standard Video Size: ${defaultW}×${defaultH} (${state.aspectRatio.label})"
                }
            )
        }
    }

    fun setCustomVideoDimensions(width: Int, height: Int) {
        val safeW = width.coerceIn(320, 3840).let { if (it % 2 != 0) it + 1 else it }
        val safeH = height.coerceIn(320, 3840).let { if (it % 2 != 0) it + 1 else it }
        _uiState.update { state ->
            state.copy(
                useCustomVideoSize = true,
                customVideoWidth = safeW,
                customVideoHeight = safeH,
                statusBannerMessage = "Custom Video Size: ${safeW}×${safeH} px"
            )
        }
    }

    fun setVideoFps(fps: VideoFps) {
        val state = _uiState.value
        val validFps = state.capabilities.validFpsFor(state.videoResolution, state.videoCodec)
        if (fps !in validFps) {
            showBanner("${fps.label} is not supported at ${state.videoResolution.shortLabel} by this hardware sensor.")
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
            showBanner("${codec.label} hardware encoder unavailable. Using MP4 / H.264.")
            return
        }
        triggerHaptic()
        _uiState.update { it.copy(videoCodec = codec, statusBannerMessage = "Format: ${codec.label}") }
    }

    fun setStabilization(mode: StabilizationMode) {
        triggerHaptic()
        _uiState.update {
            it.copy(
                stabilizationMode = mode,
                statusBannerMessage = "${mode.label}: ${mode.description}"
            )
        }
    }

    fun setShutterSoundStyle(style: ShutterSoundStyle, playPreview: Boolean = true) {
        triggerHaptic()
        if (style != ShutterSoundStyle.OFF) {
            lastNonSilentShutterStyle = style
        }
        _uiState.update {
            it.copy(
                shutterSoundStyle = style,
                statusBannerMessage = "Shutter Sound: ${style.label} — ${style.description}"
            )
        }
        if (playPreview && style != ShutterSoundStyle.OFF) {
            ShutterSoundEngine.playShutterSound(style)
        }
        viewModelScope.launch {
            repository.setShutterSoundStyle(style)
        }
    }

    fun toggleShutterSoundOnOff() {
        val current = _uiState.value.shutterSoundStyle
        val next = if (current == ShutterSoundStyle.OFF) {
            lastNonSilentShutterStyle.takeIf { it != ShutterSoundStyle.OFF } ?: ShutterSoundStyle.FUJI_MECHANICAL
        } else {
            ShutterSoundStyle.OFF
        }
        setShutterSoundStyle(next, playPreview = next != ShutterSoundStyle.OFF)
    }

    fun setAudioRecordingFormat(format: AudioRecordingFormat) {
        val supported = _uiState.value.capabilities.supportedAudioFormats
        if (format !in supported) {
            showBanner("${format.label} is not supported by this device's hardware audio encoder.")
            return
        }
        triggerHaptic()
        _uiState.update {
            it.copy(
                audioRecordingFormat = format,
                audioMuted = (format == AudioRecordingFormat.MUTED),
                statusBannerMessage = "Video Audio Format: ${format.label} (${format.specsLabel})"
            )
        }
        viewModelScope.launch {
            repository.setAudioRecordingFormat(format)
        }
    }

    fun toggleAudioMute() {
        val current = _uiState.value
        if (current.audioMuted) {
            val firstActive = current.capabilities.supportedAudioFormats
                .firstOrNull { it != AudioRecordingFormat.MUTED }
                ?: AudioRecordingFormat.AAC_LC_STEREO_48K
            setAudioRecordingFormat(firstActive)
        } else {
            setAudioRecordingFormat(AudioRecordingFormat.MUTED)
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
                recentPresetIds = recents,
                statusBannerMessage = "Look: ${preset.name} (${(state.activeRecipe.intensity * 100).roundToInt()}%)"
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
     * Saves the currently previewed look to the System Gallery (DCIM/GODCAM) and in-app Gallery at 100% quality.
     */
    fun applyFilterAndSave(previewBitmap: Bitmap? = null) {
        triggerHaptic()
        val state = _uiState.value
        val activeMedia = state.activeViewerMedia
        val preset = FilterCatalog.getById(state.activeRecipe.presetId)

        if (activeMedia == null && previewBitmap == null && _latestCameraFrame.value == null) {
            _uiState.update {
                it.copy(
                    destination = AppDestination.CAMERA,
                    statusBannerMessage = "Active Look set to ${preset.name} (${(state.activeRecipe.intensity * 100).roundToInt()}%)"
                )
            }
            return
        }

        viewModelScope.launch {
            val src = previewBitmap
                ?: ImageFilterEngine.loadBitmapForMediaItem(getApplication(), activeMedia, 2560, 1920)
                ?: _latestCameraFrame.value

            if (src == null) {
                _uiState.update {
                    it.copy(
                        destination = AppDestination.CAMERA,
                        statusBannerMessage = "Active Look set to ${preset.name}"
                    )
                }
                return@launch
            }

            // If editing a recorded Video in FiltersScreen, update its look AND re-render its filtered frames
            if (activeMedia != null && activeMedia.isVideo) {
                val renderedPoster = ImageFilterEngine.renderFilteredBitmap(
                    source = src,
                    preset = preset,
                    recipe = state.activeRecipe,
                    targetWidth = activeMedia.width.coerceAtLeast(1280),
                    targetHeight = activeMedia.height.coerceAtLeast(720),
                    applyCrop = false,
                    extraEv = state.exposureEv
                )
                val savedVideo = MediaGallerySaver.saveVideoToGallery(
                    context = getApplication(),
                    posterBitmap = renderedPoster,
                    capturedFrames = listOf(renderedPoster),
                    existingMp4File = null,
                    durationSec = activeMedia.durationSec.coerceAtLeast(3),
                    width = activeMedia.width.coerceAtLeast(1280),
                    height = activeMedia.height.coerceAtLeast(720),
                    fps = state.videoFps.fps,
                    bitrateMbps = state.videoBitrateMbps
                )
                renderedPoster.recycle()

                val title = "${activeMedia.title} • ${preset.name}"
                repository.insertCapturedOrExportedMedia(
                    title = title,
                    filePath = savedVideo.posterFile.absolutePath,
                    uriString = savedVideo.localFile.absolutePath,
                    isVideo = true,
                    width = savedVideo.actualWidth,
                    height = savedVideo.actualHeight,
                    durationSec = activeMedia.durationSec.coerceAtLeast(3),
                    formatLabel = activeMedia.formatLabel,
                    sizeBytes = savedVideo.sizeBytes,
                    appliedPresetId = preset.id,
                    appliedIntensity = state.activeRecipe.intensity,
                    cameraInfo = "${savedVideo.actualWidth}×${savedVideo.actualHeight} • ${state.videoFps.fps}fps • ${preset.name}"
                )
                _uiState.update {
                    it.copy(
                        destination = AppDestination.GALLERY,
                        statusBannerMessage = "Saved Filtered Video ($title) to Gallery"
                    )
                }
                return@launch
            }

            val rendered = ImageFilterEngine.renderFilteredBitmap(
                source = src,
                preset = preset,
                recipe = state.activeRecipe,
                applyCrop = false,
                extraEv = state.exposureEv
            )

            val savedResult = MediaGallerySaver.savePhotoToGallery(
                context = getApplication(),
                bitmap = rendered,
                fileNamePrefix = "GODCAM_LOOK_${preset.id.uppercase(Locale.US)}",
                photoFormat = state.photoFormat,
                quality = 100,
                appliedLookName = preset.name,
                focalLengthMm = state.capabilities.focalLengthMm,
                apertureF = state.capabilities.apertureF,
                isoValue = if (state.isAutoIso) 100 else state.manualIso
            )
            rendered.recycle()

            val baseTitle = activeMedia?.title ?: "GODCAM_Capture"
            val title = "$baseTitle • ${preset.name}"
            repository.insertCapturedOrExportedMedia(
                title = title,
                filePath = savedResult.localFile.absolutePath,
                uriString = savedResult.mediaStoreUri?.toString() ?: savedResult.localFile.toURI().toString(),
                isVideo = false,
                width = savedResult.actualWidth,
                height = savedResult.actualHeight,
                formatLabel = activeMedia?.formatLabel ?: state.photoFormat.label,
                sizeBytes = savedResult.sizeBytes,
                appliedPresetId = preset.id,
                appliedIntensity = state.activeRecipe.intensity,
                cameraInfo = "Filter: ${preset.name} (${(state.activeRecipe.intensity * 100).roundToInt()}%)"
            )

            MediaGallerySaver.sendAutoSaveNotification(
                context = getApplication(),
                title = "Filtered Photo Saved to Gallery",
                message = "$title (${savedResult.actualWidth}×${savedResult.actualHeight}) saved to DCIM/GODCAM."
            )

            _uiState.update {
                it.copy(
                    destination = if (activeMedia != null) AppDestination.GALLERY else AppDestination.CAMERA,
                    statusBannerMessage = "Saved $title (${savedResult.actualWidth}×${savedResult.actualHeight}) to Gallery"
                )
            }
        }
    }

    /**
     * Processes a captured raw photo [Bitmap] from CameraX ImageCapture (MAXIMIZE_QUALITY) / PreviewView,
     * plays the active Fuji / Digital Camera shutter sound, applies the active FilterPreset + EditRecipe +
     * ProOpticalState (ISO, Shutter, EV, White Balance Kelvin, 100x Zoom) at the selected Megapixel resolution
     * (12MP, 24MP, 48MP, 50MP, etc.), and either auto-saves to Gallery or holds for review if Auto-Save is OFF.
     */
    fun processAndSaveCapturedPhoto(rawBitmap: Bitmap?) {
        if (_uiState.value.isCapturing) return
        val state = _uiState.value
        triggerHaptic()
        ShutterSoundEngine.playShutterSound(state.shutterSoundStyle)

        val sourceBitmap = rawBitmap ?: _latestCameraFrame.value
        if (sourceBitmap == null) {
            showBanner("Camera sensor is initializing. Grant Camera permission and wait for live feed.")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isCapturing = true, shutterFlashVisible = true) }
            if (state.nightModeActive) {
                _uiState.update { it.copy(statusBannerMessage = "Night Mode: Stacking high-dynamic-range frame…") }
                delay(220)
            } else {
                delay(70)
            }
            _uiState.update { it.copy(shutterFlashVisible = false) }

            val preset = FilterCatalog.getById(state.activeRecipe.presetId)
            val proOptical = state.toProOpticalState()
            val extraDigitalZoom = (state.activeZoom / state.capabilities.hardwareOpticalMaxZoom.coerceAtLeast(1f)).coerceAtLeast(1f)

            // Nominal megapixel dimensions (e.g., 8192x6144 for 50MP, 8000x6000 for 48MP, 4032x3024 for 12MP)
            val (nominalMpW, nominalMpH) = state.photoMegapixel.computeDimensions(state.aspectRatio)
            // Safe in-memory bitmap allocation cap to prevent OOM while preserving exact aspect ratio & high quality
            val maxAllocEdge = when (state.photoMegapixel.megapixels) {
                12 -> 4032
                24 -> 4320
                else -> 4608
            }
            val ratioVal = state.aspectRatio.aspectValue
            val (highResW, highResH) = if (ratioVal >= 1f) {
                val w = minOf(nominalMpW, maxAllocEdge)
                val h = (w / ratioVal).roundToInt().coerceAtLeast(480)
                w to h
            } else {
                val h = minOf(nominalMpH, maxAllocEdge)
                val w = (h * ratioVal).roundToInt().coerceAtLeast(480)
                w to h
            }

            val filtered = withContext(Dispatchers.Default) {
                ImageFilterEngine.renderFilteredBitmap(
                    source = sourceBitmap,
                    preset = preset,
                    recipe = state.activeRecipe.copy(aspectRatio = state.aspectRatio),
                    targetWidth = highResW,
                    targetHeight = highResH,
                    applyCrop = true,
                    extraEv = state.exposureEv,
                    proOptical = proOptical,
                    extraDigitalZoom = extraDigitalZoom
                )
            }

            val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val savedResult = MediaGallerySaver.savePhotoToGallery(
                context = getApplication(),
                bitmap = filtered,
                fileNamePrefix = "GODCAM_${state.photoMegapixel.shortBadge}_$stamp",
                photoFormat = state.photoFormat,
                mimeType = state.photoFormat.mimeType,
                quality = 100,
                appliedLookName = "${preset.name} • ${state.photoMegapixel.shortBadge}",
                focalLengthMm = state.capabilities.focalLengthMm,
                apertureF = state.capabilities.apertureF,
                isoValue = if (state.isAutoIso) 100 else state.manualIso,
                publishToSystemGallery = state.autoSaveToGallery
            )
            filtered.recycle()

            val isoText = if (state.isAutoIso) "ISO Auto" else "ISO ${state.manualIso}"
            val shutterText = if (state.isAutoShutter) "Auto S" else "1/${state.shutterDenominator}s"
            val wbText = if (state.whiteBalance == WhiteBalancePreset.MANUAL_KELVIN) "${state.manualKelvin}K" else state.whiteBalance.label
            val zoomText = if (state.activeZoom >= 10f) "${state.activeZoom.roundToInt()}×" else "${(state.activeZoom * 10f).roundToInt() / 10f}×"
            val title = "GODCAM_${state.photoMegapixel.shortBadge}_${stamp.takeLast(6)}"
            val formatBadge = "${state.photoFormat.label} • ${state.photoMegapixel.shortBadge}"
            val cameraInfo = "${state.photoMegapixel.label} (${nominalMpW}×${nominalMpH}) • $zoomText • $isoText • $shutterText • $wbText • ${preset.name}"

            if (state.autoSaveToGallery) {
                repository.insertCapturedOrExportedMedia(
                    title = title,
                    filePath = savedResult.localFile.absolutePath,
                    uriString = savedResult.mediaStoreUri?.toString() ?: savedResult.localFile.toURI().toString(),
                    isVideo = false,
                    width = nominalMpW,
                    height = nominalMpH,
                    durationSec = 0,
                    formatLabel = formatBadge,
                    sizeBytes = savedResult.sizeBytes,
                    appliedPresetId = preset.id,
                    appliedIntensity = state.activeRecipe.intensity,
                    cameraInfo = cameraInfo
                )

                MediaGallerySaver.sendAutoSaveNotification(
                    context = getApplication(),
                    title = "${state.photoMegapixel.label} Photo Saved",
                    message = "$title (${nominalMpW}×${nominalMpH} ${state.photoFormat.label} • ${preset.name}) saved to DCIM/GODCAM."
                )

                _uiState.update {
                    it.copy(
                        isCapturing = false,
                        pendingUnsavedMedia = null,
                        selectedGalleryTab = GalleryTab.PHOTOS,
                        statusBannerMessage = "Saved ${state.photoMegapixel.label} Photo (${nominalMpW}×${nominalMpH} ${state.photoFormat.label} • $zoomText • ${preset.name})"
                    )
                }
            } else {
                val pendingItem = MediaItemModel(
                    id = System.currentTimeMillis(),
                    title = title,
                    filePath = savedResult.localFile.absolutePath,
                    uriString = savedResult.localFile.toURI().toString(),
                    isVideo = false,
                    width = nominalMpW,
                    height = nominalMpH,
                    durationSec = 0,
                    formatLabel = formatBadge,
                    sizeBytes = savedResult.sizeBytes,
                    timestampMs = System.currentTimeMillis(),
                    isFavorite = false,
                    appliedPresetId = preset.id,
                    appliedIntensity = state.activeRecipe.intensity,
                    cameraInfo = cameraInfo
                )
                _uiState.update {
                    it.copy(
                        isCapturing = false,
                        pendingUnsavedMedia = pendingItem,
                        statusBannerMessage = "Auto-Save OFF • Captured ${state.photoMegapixel.label} Photo — Tap 'Save' below to keep"
                    )
                }
            }
        }
    }

    fun startVideoRecordingTimer() {
        triggerHaptic()
        val state = _uiState.value
        if (state.allowBackgroundMusic) {
            runCatching {
                val am = getApplication<Application>().getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                am?.mode = AudioManager.MODE_NORMAL
            }
        }
        ShutterSoundEngine.playVideoRecordTone(state.shutterSoundStyle, isStarting = true)
        val preset = FilterCatalog.getById(state.activeRecipe.presetId)
        val (vidW, vidH) = state.effectiveVideoDimensions()
        val targetDur = state.effectiveTargetVideoDurationSec()
        val durTag = if (targetDur > 0) "Auto-Stop ${targetDur}s" else "Continuous"
        val logTag = if (state.colorProfile.isLog) " · ${state.colorProfile.shortBadge}" else ""
        _uiState.update {
            it.copy(
                isRecordingVideo = true,
                isRecordingPaused = false,
                recordingElapsedSec = 0,
                statusBannerMessage = "REC ${vidW}×${vidH} ${it.videoFps.label}$logTag · $durTag · ${preset.name}"
            )
        }
        recordingJob?.cancel()
        recordingJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val current = _uiState.value
                if (!current.isRecordingVideo) break
                if (!current.isRecordingPaused) {
                    val nextSec = current.recordingElapsedSec + 1
                    _uiState.update { it.copy(recordingElapsedSec = nextSec) }
                }
            }
        }
    }

    fun finalizeAndAutoSaveVideo(
        existingMp4File: File? = null,
        livePreviewBitmap: Bitmap? = null,
        capturedFrames: List<Bitmap> = emptyList()
    ) {
        val state = _uiState.value
        recordingJob?.cancel()
        ShutterSoundEngine.playVideoRecordTone(state.shutterSoundStyle, isStarting = false)
        val duration = state.recordingElapsedSec.coerceAtLeast(1)
        _uiState.update {
            it.copy(
                isRecordingVideo = false,
                isRecordingPaused = false,
                recordingElapsedSec = 0,
                isCapturing = true
            )
        }

        viewModelScope.launch {
            val preset = FilterCatalog.getById(state.activeRecipe.presetId)
            val (videoW, videoH) = state.effectiveVideoDimensions()
            val rawFrame = livePreviewBitmap
                ?: capturedFrames.lastOrNull()
                ?: _latestCameraFrame.value
                ?: ImageFilterEngine.createNeutralCalibrationBitmap(videoW, videoH)

            val recipeWithRatio = state.activeRecipe.copy(aspectRatio = state.aspectRatio)
            val proOptical = state.toProOpticalState()
            val extraDigitalZoom = (state.activeZoom / state.capabilities.hardwareOpticalMaxZoom.coerceAtLeast(1f)).coerceAtLeast(1f)

            // Perform all bitmap filtering off the main thread on Dispatchers.Default so UI stays 60fps smooth
            val (filteredPoster, filteredMotionFrames) = withContext(Dispatchers.Default) {
                val poster = ImageFilterEngine.renderFilteredBitmapSync(
                    source = rawFrame,
                    preset = preset,
                    recipe = recipeWithRatio,
                    targetWidth = videoW,
                    targetHeight = videoH,
                    applyCrop = !state.useCustomVideoSize,
                    extraEv = state.exposureEv,
                    proOptical = proOptical,
                    extraDigitalZoom = extraDigitalZoom
                )

                val scaleDown = if (maxOf(videoW, videoH) > 720) 720f / maxOf(videoW, videoH).toFloat() else 1f
                val frameTargetW = ((videoW * scaleDown).roundToInt().coerceAtLeast(320)).let { if (it % 2 != 0) it + 1 else it }
                val frameTargetH = ((videoH * scaleDown).roundToInt().coerceAtLeast(320)).let { if (it % 2 != 0) it + 1 else it }

                val sourceList = if (capturedFrames.isNotEmpty()) capturedFrames else listOf(rawFrame)
                val maxToRender = sourceList.size.coerceAtMost(120)
                val sampledSources = if (sourceList.size <= maxToRender) {
                    sourceList
                } else {
                    List(maxToRender) { i ->
                        val idx = ((i.toFloat() / (maxToRender - 1).coerceAtLeast(1)) * (sourceList.size - 1))
                            .toInt()
                            .coerceIn(0, sourceList.lastIndex)
                        sourceList[idx]
                    }
                }

                val motionFrames = sampledSources.mapNotNull { frame ->
                    runCatching {
                        ImageFilterEngine.renderFilteredBitmapSync(
                            source = frame,
                            preset = preset,
                            recipe = recipeWithRatio,
                            targetWidth = frameTargetW,
                            targetHeight = frameTargetH,
                            applyCrop = !state.useCustomVideoSize,
                            extraEv = state.exposureEv,
                            proOptical = proOptical,
                            extraDigitalZoom = extraDigitalZoom
                        )
                    }.getOrNull()
                }
                poster to motionFrames
            }

            val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())

            val savedVideo = MediaGallerySaver.saveVideoToGallery(
                context = getApplication(),
                posterBitmap = filteredPoster,
                capturedFrames = filteredMotionFrames,
                existingMp4File = existingMp4File,
                durationSec = duration,
                width = videoW,
                height = videoH,
                fps = state.videoFps.fps,
                bitrateMbps = state.videoBitrateMbps,
                publishToSystemGallery = state.autoSaveToGallery
            )
            filteredPoster.recycle()
            filteredMotionFrames.forEach { runCatching { it.recycle() } }
            capturedFrames.forEach { frame ->
                if (frame !== rawFrame && frame !== _latestCameraFrame.value) {
                    runCatching { frame.recycle() }
                }
            }

            val audioTag = state.audioRecordingFormat.label
            val logBadge = if (state.colorProfile.isLog) " • ${state.colorProfile.shortBadge}" else ""
            val title = "GODCAM_VID_${stamp.takeLast(6)}"
            val baseBadge = if (state.useCustomVideoSize) "CUSTOM" else state.videoResolution.shortLabel
            val badgeLabel = "$baseBadge • ${state.videoFps.fps}fps$logBadge"
            val cameraInfo = "${savedVideo.actualWidth}×${savedVideo.actualHeight} • ${duration}s • ${state.aspectRatio.label} • ${state.videoFps.fps}fps • ${state.colorProfile.shortBadge} • ${state.stabilizationMode.label} • $audioTag • ${preset.name}"

            if (state.autoSaveToGallery) {
                repository.insertCapturedOrExportedMedia(
                    title = title,
                    filePath = savedVideo.posterFile.absolutePath,
                    uriString = savedVideo.localFile.absolutePath,
                    isVideo = true,
                    width = savedVideo.actualWidth,
                    height = savedVideo.actualHeight,
                    durationSec = duration,
                    formatLabel = badgeLabel,
                    sizeBytes = savedVideo.sizeBytes,
                    appliedPresetId = preset.id,
                    appliedIntensity = state.activeRecipe.intensity,
                    cameraInfo = cameraInfo
                )

                MediaGallerySaver.sendAutoSaveNotification(
                    context = getApplication(),
                    title = "High-Quality Video Saved",
                    message = "$title (${savedVideo.actualWidth}×${savedVideo.actualHeight} • ${duration}s • ${state.videoFps.fps}fps$logBadge • ${preset.name}) saved to DCIM/GODCAM."
                )

                _uiState.update {
                    it.copy(
                        isCapturing = false,
                        pendingUnsavedMedia = null,
                        selectedGalleryTab = GalleryTab.VIDEOS,
                        statusBannerMessage = "Saved ${duration}s Video (${savedVideo.actualWidth}×${savedVideo.actualHeight} ${state.videoFps.fps}fps$logBadge • ${preset.name})"
                    )
                }
            } else {
                val pendingVideo = MediaItemModel(
                    id = System.currentTimeMillis(),
                    title = title,
                    filePath = savedVideo.posterFile.absolutePath,
                    uriString = savedVideo.localFile.absolutePath,
                    isVideo = true,
                    width = savedVideo.actualWidth,
                    height = savedVideo.actualHeight,
                    durationSec = duration,
                    formatLabel = badgeLabel,
                    sizeBytes = savedVideo.sizeBytes,
                    timestampMs = System.currentTimeMillis(),
                    isFavorite = false,
                    appliedPresetId = preset.id,
                    appliedIntensity = state.activeRecipe.intensity,
                    cameraInfo = cameraInfo
                )
                _uiState.update {
                    it.copy(
                        isCapturing = false,
                        pendingUnsavedMedia = pendingVideo,
                        statusBannerMessage = "Auto-Save OFF • Recorded ${duration}s Video — Tap 'Save' below to keep"
                    )
                }
            }
        }
    }

    fun savePendingCaptureToGallery() {
        val pending = _uiState.value.pendingUnsavedMedia ?: return
        triggerHaptic()
        viewModelScope.launch {
            val fileToPublish = if (pending.isVideo) File(pending.uriString) else File(pending.filePath)
            val publishedUri = MediaGallerySaver.publishExistingFileToGallery(
                context = getApplication(),
                localFile = fileToPublish,
                isVideo = pending.isVideo,
                width = pending.width,
                height = pending.height,
                durationSec = pending.durationSec
            )
            repository.insertCapturedOrExportedMedia(
                title = pending.title,
                filePath = pending.filePath,
                uriString = if (pending.isVideo) pending.uriString else (publishedUri?.toString() ?: pending.uriString),
                isVideo = pending.isVideo,
                width = pending.width,
                height = pending.height,
                durationSec = pending.durationSec,
                formatLabel = pending.formatLabel,
                sizeBytes = pending.sizeBytes,
                appliedPresetId = pending.appliedPresetId,
                appliedIntensity = pending.appliedIntensity,
                cameraInfo = pending.cameraInfo
            )
            _uiState.update {
                it.copy(
                    pendingUnsavedMedia = null,
                    selectedGalleryTab = if (pending.isVideo) GalleryTab.VIDEOS else GalleryTab.PHOTOS,
                    statusBannerMessage = "Saved ${pending.title} (${pending.width}×${pending.height}) to Gallery"
                )
            }
        }
    }

    fun discardPendingCapture() {
        val pending = _uiState.value.pendingUnsavedMedia ?: return
        triggerHaptic()
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { File(pending.filePath).delete() }
            if (pending.isVideo) {
                runCatching { File(pending.uriString).delete() }
            }
        }
        _uiState.update {
            it.copy(
                pendingUnsavedMedia = null,
                statusBannerMessage = "Discarded unsaved capture (${pending.title})"
            )
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
            }
            if (decodedBitmap == null) {
                showBanner("Unable to decode selected media file.")
                return@launch
            }

            val preset = FilterCatalog.getById(_uiState.value.activeRecipe.presetId)
            val filtered = ImageFilterEngine.renderFilteredBitmap(
                source = decodedBitmap,
                preset = preset,
                recipe = _uiState.value.activeRecipe,
                applyCrop = false
            )
            decodedBitmap.recycle()

            val saved = MediaGallerySaver.savePhotoToGallery(
                context = context,
                bitmap = filtered,
                fileNamePrefix = "GODCAM_IMPORT_$stamp"
            )
            filtered.recycle()

            repository.insertCapturedOrExportedMedia(
                title = "Imported_$stamp",
                filePath = saved.localFile.absolutePath,
                uriString = saved.mediaStoreUri?.toString() ?: uriString,
                isVideo = false,
                width = saved.actualWidth,
                height = saved.actualHeight,
                formatLabel = "JPEG",
                sizeBytes = saved.sizeBytes,
                appliedPresetId = preset.id,
                appliedIntensity = _uiState.value.activeRecipe.intensity,
                cameraInfo = "Imported via Photo Picker • ${preset.name}"
            )
            MediaGallerySaver.sendAutoSaveNotification(
                context = context,
                title = "Media Imported to Gallery",
                message = "Imported_$stamp (${saved.actualWidth}×${saved.actualHeight}) saved to Gallery."
            )
            showBanner("Imported & saved photo (${saved.actualWidth}×${saved.actualHeight}) into Gallery")
        }
    }

    // Export Studio Actions (PRD Section 11)
    fun openExportStudio(item: MediaItemModel) {
        triggerHaptic()
        val itemRatio = if (item.height > 0) item.width.toFloat() / item.height.toFloat() else 1.333f
        val matchedAspect = AspectRatioOption.entries.minByOrNull {
            kotlin.math.abs(it.aspectValue - itemRatio)
        } ?: AspectRatioOption.RATIO_4_3
        _uiState.update { state ->
            state.copy(
                showExportStudioSheet = true,
                exportTargetMedia = item,
                exportJobState = ExportJobState.Idle,
                exportConfig = state.exportConfig.copy(
                    customWidth = item.width.coerceAtLeast(320),
                    customHeight = item.height.coerceAtLeast(320),
                    aspectRatio = matchedAspect,
                    useCustomVideoSize = false,
                    videoResolution = state.videoResolution,
                    videoFps = state.videoFps,
                    bitrateMbps = state.videoBitrateMbps,
                    photoFormat = state.photoFormat
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
        val origW = target?.width ?: 1920
        val origH = target?.height ?: 1080
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
                    appliedPresetId = recipeForExport.presetId,
                    appliedIntensity = recipeForExport.intensity,
                    cameraInfo = "Exported ${result.width}×${result.height} • $presetName"
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

    fun setCompactLayoutMode(mode: CompactLayoutMode) {
        triggerHaptic()
        _uiState.update {
            it.copy(
                compactLayoutMode = mode,
                statusBannerMessage = "UI Density: ${mode.label} (${mode.description})"
            )
        }
        viewModelScope.launch { repository.setCompactLayoutMode(mode) }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setHapticsEnabled(enabled) }
    }

    fun setPreserveLocation(enabled: Boolean) {
        viewModelScope.launch { repository.setPreserveLocation(enabled) }
    }

    fun setAutoSaveToGallery(enabled: Boolean) {
        triggerHaptic()
        _uiState.update {
            it.copy(
                autoSaveToGallery = enabled,
                statusBannerMessage = if (enabled) {
                    "Auto-Save Photos & Videos: ON (Saves automatically to Gallery)"
                } else {
                    "Auto-Save Photos & Videos: OFF (Review & confirm before saving)"
                }
            )
        }
        viewModelScope.launch { repository.setAutoSaveToGallery(enabled) }
    }

    fun showBanner(message: String?) {
        _uiState.update { it.copy(statusBannerMessage = message) }
    }

    fun triggerHaptic() {
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
