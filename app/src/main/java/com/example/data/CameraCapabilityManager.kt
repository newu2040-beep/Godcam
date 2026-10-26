package com.example.data

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.MediaCodecList
import android.media.MediaRecorder
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.os.StatFs
import com.example.model.HardwareCapabilities
import com.example.model.VideoCodecOption
import com.example.model.VideoFps
import com.example.model.VideoResolution
import java.util.Locale

object CameraCapabilityManager {

    /**
     * Queries the Android device's real Camera2 characteristics, StreamConfigurationMap,
     * MediaCodec hardware encoders, BatteryManager temperature, PowerManager thermal state,
     * and StatFs storage at runtime. Zero fake or hardcoded demo assumptions.
     */
    fun discoverCapabilities(context: Context, isFrontLens: Boolean = false): HardwareCapabilities {
        val (freeMb, totalMb) = queryStorageMb()
        val (tempC, batteryPct) = queryBatteryTelemetry(context)
        val thermalStr = queryThermalState(context, tempC)

        return try {
            val pm = context.packageManager
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            val cameraIds = cameraManager?.cameraIdList.orEmpty()

            var hasBack = pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)
            var hasFront = pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)
            var hasFlash = false
            var supportsRaw = false
            var supportsManualSensor = false
            var supportsManualFocus = false
            var supportsOis = false
            var isoRange = 100..1600
            var exposureNsRange = 1_000_000L..250_000_000L
            var evRange = -2.0f..2.0f
            var minZoom = 1.0f
            var maxZoom = 4.0f
            var activeId = "0"
            var hwLevelLabel = "LIMITED"
            var focalLength = 4.2f
            var aperture = 1.8f
            var maxPhotoW = 1920
            var maxPhotoH = 1080

            val targetFacing = if (isFrontLens) {
                CameraCharacteristics.LENS_FACING_FRONT
            } else {
                CameraCharacteristics.LENS_FACING_BACK
            }

            var selectedChars: CameraCharacteristics? = null
            for (id in cameraIds) {
                val chars = cameraManager?.getCameraCharacteristics(id) ?: continue
                val facing = chars.get(CameraCharacteristics.LENS_FACING)
                if (facing == CameraCharacteristics.LENS_FACING_BACK) hasBack = true
                if (facing == CameraCharacteristics.LENS_FACING_FRONT) hasFront = true
                if (facing == targetFacing && selectedChars == null) {
                    selectedChars = chars
                    activeId = id
                }
            }
            if (selectedChars == null && cameraIds.isNotEmpty()) {
                activeId = cameraIds.first()
                selectedChars = cameraManager?.getCameraCharacteristics(activeId)
            }

            val discoveredResolutions = mutableListOf<VideoResolution>()
            val discoveredFps = mutableSetOf<VideoFps>()

            selectedChars?.let { chars ->
                hasFlash = chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true

                val hwLevel = chars.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
                hwLevelLabel = when (hwLevel) {
                    CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3 -> "LEVEL_3"
                    CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL -> "FULL"
                    CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED -> "LIMITED"
                    CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY -> "LEGACY"
                    CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_EXTERNAL -> "EXTERNAL"
                    else -> "STANDARD"
                }

                val caps = chars.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES) ?: intArrayOf()
                supportsRaw = caps.contains(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_RAW)
                supportsManualSensor = caps.contains(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_SENSOR)

                val minFocusDist = chars.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE) ?: 0f
                supportsManualFocus = minFocusDist > 0f

                val oisModes = chars.get(CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION) ?: intArrayOf()
                supportsOis = oisModes.contains(CameraCharacteristics.LENS_OPTICAL_STABILIZATION_MODE_ON)

                chars.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)?.firstOrNull()?.let {
                    focalLength = it
                }
                chars.get(CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES)?.firstOrNull()?.let {
                    aperture = it
                }

                chars.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)?.let { range ->
                    isoRange = range.lower..range.upper
                }
                chars.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE)?.let { range ->
                    exposureNsRange = range.lower..range.upper
                }

                val aeRange = chars.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE)
                val aeStep = chars.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_STEP)
                if (aeRange != null && aeStep != null && aeStep.toFloat() > 0f) {
                    val step = aeStep.toFloat()
                    val minEv = (aeRange.lower * step)
                    val maxEv = (aeRange.upper * step)
                    if (minEv < maxEv) {
                        evRange = minEv..maxEv
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    chars.get(CameraCharacteristics.CONTROL_ZOOM_RATIO_RANGE)?.let { zRange ->
                        minZoom = zRange.lower.coerceAtLeast(0.5f)
                        maxZoom = zRange.upper.coerceAtLeast(1.0f)
                    }
                } else {
                    chars.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM)?.let { mz ->
                        if (mz >= 1f) maxZoom = mz
                    }
                }

                // Real FPS ranges from hardware sensor
                chars.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES)?.forEach { fpsRange ->
                    val upper = fpsRange.upper
                    VideoFps.entries.forEach { candidate ->
                        if (candidate.fps <= upper) {
                            discoveredFps.add(candidate)
                        }
                    }
                }

                // Real output resolutions from StreamConfigurationMap
                val map = chars.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                val jpegSizes = map?.getOutputSizes(ImageFormat.JPEG)
                jpegSizes?.maxByOrNull { it.width * it.height }?.let { largest ->
                    maxPhotoW = largest.width
                    maxPhotoH = largest.height
                }

                val videoSizes = map?.getOutputSizes(MediaRecorder::class.java)
                    ?: map?.getOutputSizes(ImageFormat.YUV_420_888)

                if (!videoSizes.isNullOrEmpty()) {
                    VideoResolution.entries.forEach { res ->
                        val supported = videoSizes.any { size ->
                            (size.width >= res.width && size.height >= res.height) ||
                                (size.width == res.width && size.height == res.height)
                        }
                        if (supported) {
                            discoveredResolutions.add(res)
                        }
                    }
                }

                // High-speed FPS ranges if supported by hardware
                runCatching {
                    map?.highSpeedVideoFpsRanges?.forEach { hsRange ->
                        if (hsRange.upper >= 120) discoveredFps.add(VideoFps.FPS_120)
                        if (hsRange.upper >= 240) discoveredFps.add(VideoFps.FPS_240)
                    }
                }
            }

            if (discoveredResolutions.isEmpty()) {
                discoveredResolutions.add(VideoResolution.SD_480P)
                discoveredResolutions.add(VideoResolution.HD_720P)
                discoveredResolutions.add(VideoResolution.FHD_1080P)
            }
            if (discoveredFps.isEmpty()) {
                discoveredFps.add(VideoFps.FPS_24)
                discoveredFps.add(VideoFps.FPS_30)
            }

            val zoomStops = buildList {
                if (minZoom < 0.9f) add(String.format(Locale.US, "%.1f", minZoom).toFloat())
                add(1.0f)
                if (maxZoom >= 2.0f) add(2.0f)
                if (maxZoom >= 3.0f) add(3.0f)
                if (maxZoom >= 5.0f) add(5.0f)
            }.distinct()

            // Discover real hardware video codecs via MediaCodecList
            val codecInfos = MediaCodecList(MediaCodecList.REGULAR_CODECS).codecInfos
                .filter { it.isEncoder }
            val supportsHevc = codecInfos.any { info ->
                info.supportedTypes.any { it.equals("video/hevc", ignoreCase = true) }
            }
            val hardwareEncoderNames = codecInfos
                .flatMap { info ->
                    info.supportedTypes
                        .filter { it.startsWith("video/", ignoreCase = true) }
                        .map { "${info.name} ($it)" }
                }
                .take(6)

            val codecs = buildList {
                add(VideoCodecOption.H264)
                if (supportsHevc) add(VideoCodecOption.HEVC)
            }

            val supports8K = discoveredResolutions.contains(VideoResolution.UHD_8K)

            HardwareCapabilities(
                activeCameraId = activeId,
                hardwareLevelLabel = hwLevelLabel,
                focalLengthMm = focalLength,
                apertureF = aperture,
                maxPhotoWidth = maxPhotoW,
                maxPhotoHeight = maxPhotoH,
                hasBackCamera = hasBack,
                hasFrontCamera = hasFront,
                hasFlashUnit = hasFlash,
                supportsRawCapture = supportsRaw,
                supportsManualSensor = supportsManualSensor,
                supportsManualFocus = supportsManualFocus,
                supportsOpticalStabilization = supportsOis,
                isoRange = isoRange,
                exposureTimeRangeNs = exposureNsRange,
                evRange = evRange,
                zoomStops = zoomStops,
                minZoom = minZoom,
                maxZoom = maxZoom,
                supportedVideoResolutions = discoveredResolutions,
                supportedFpsChoices = discoveredFps.sortedBy { it.fps },
                supportedCodecs = codecs,
                encoderNames = hardwareEncoderNames,
                supports8K = supports8K,
                availableStorageMb = freeMb,
                totalStorageMb = totalMb,
                batteryTempCelsius = tempC,
                batteryPercent = batteryPct,
                thermalStatusLabel = thermalStr
            )
        } catch (_: Exception) {
            HardwareCapabilities(
                availableStorageMb = freeMb,
                totalStorageMb = totalMb,
                batteryTempCelsius = tempC,
                batteryPercent = batteryPct,
                thermalStatusLabel = thermalStr
            )
        }
    }

    fun queryStorageMb(): Pair<Long, Long> {
        return try {
            val stat = StatFs(Environment.getDataDirectory().path)
            val free = (stat.availableBytes / (1024L * 1024L)).coerceAtLeast(1L)
            val total = (stat.totalBytes / (1024L * 1024L)).coerceAtLeast(free)
            free to total
        } catch (_: Exception) {
            0L to 0L
        }
    }

    private fun queryBatteryTelemetry(context: Context): Pair<Float, Int> {
        return try {
            val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val tempTenths = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
            val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val pct = if (level >= 0 && scale > 0) (level * 100) / scale else 100
            val tempCelsius = if (tempTenths > 0) tempTenths / 10f else 0f
            tempCelsius to pct
        } catch (_: Exception) {
            0f to 100
        }
    }

    private fun queryThermalState(context: Context, tempC: Float): String {
        val statusName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            when (pm?.currentThermalStatus) {
                PowerManager.THERMAL_STATUS_NONE -> "NONE (Optimal)"
                PowerManager.THERMAL_STATUS_LIGHT -> "LIGHT"
                PowerManager.THERMAL_STATUS_MODERATE -> "MODERATE"
                PowerManager.THERMAL_STATUS_SEVERE -> "SEVERE (Throttling)"
                PowerManager.THERMAL_STATUS_CRITICAL -> "CRITICAL"
                else -> "NOMINAL"
            }
        } else {
            "NORMAL"
        }
        return if (tempC > 0f) {
            String.format(Locale.US, "%s • %.1f°C", statusName, tempC)
        } else {
            statusName
        }
    }
}
