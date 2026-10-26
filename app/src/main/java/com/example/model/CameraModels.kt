package com.example.model

import androidx.compose.runtime.Immutable

enum class CaptureMode(val label: String, val isVideo: Boolean, val isPro: Boolean) {
    PHOTO("Photo", isVideo = false, isPro = false),
    VIDEO("Video", isVideo = true, isPro = false),
    PRO_PHOTO("Pro Photo", isVideo = false, isPro = true),
    PRO_VIDEO("Pro Video", isVideo = true, isPro = true),
    NIGHT("Night", isVideo = false, isPro = false)
}

enum class AspectRatioOption(
    val label: String,
    val widthRatio: Float,
    val heightRatio: Float,
    val isCinematicGuide: Boolean = false,
    val description: String
) {
    RATIO_4_3("4:3", 4f, 3f, false, "Full Sensor Native (Photo default)"),
    RATIO_3_2("3:2", 3f, 2f, false, "Classic 35mm Film Frame (Crop)"),
    RATIO_16_9("16:9", 16f, 9f, false, "Widescreen HD/4K Native"),
    RATIO_1_1("1:1", 1f, 1f, false, "Square Medium-Format Crop"),
    RATIO_4_5("4:5", 4f, 5f, false, "Editorial Portrait Crop"),
    RATIO_9_16("9:16", 9f, 16f, false, "Vertical Full-Frame Story"),
    GUIDE_2_00("2.00:1", 2.0f, 1f, true, "Univisium 2:1 Viewfinder Guide / Export Crop"),
    GUIDE_2_20("2.20:1", 2.2f, 1f, true, "70mm Todd-AO Viewfinder Guide / Export Crop"),
    GUIDE_2_35("2.35:1", 2.35f, 1f, true, "Anamorphic Scope Guide / Export Crop"),
    GUIDE_2_39("2.39:1", 2.39f, 1f, true, "Modern DCI Scope Guide / Export Crop");

    val aspectValue: Float get() = widthRatio / heightRatio
}

enum class GridMode(val label: String) {
    OFF("Off"),
    THREE_BY_THREE("3×3"),
    FOUR_BY_FOUR("4×4"),
    GOLDEN_RATIO("Golden"),
    LEVEL("Level")
}

enum class FlashModeOption(val label: String) {
    OFF("Off"),
    AUTO("Auto"),
    ON("On"),
    TORCH("Torch")
}

enum class TimerOption(val seconds: Int, val label: String) {
    OFF(0, "Off"),
    THREE_SEC(3, "3s"),
    FIVE_SEC(5, "5s"),
    TEN_SEC(10, "10s")
}

enum class PhotoFormat(val label: String, val extension: String, val mimeType: String) {
    JPEG("JPEG", "jpg", "image/jpeg"),
    PNG("PNG", "png", "image/png"),
    WEBP("WebP", "webp", "image/webp"),
    RAW_DNG("RAW", "dng", "image/x-adobe-dng")
}

enum class VideoResolution(
    val shortLabel: String,
    val fullLabel: String,
    val width: Int,
    val height: Int,
    val isUltraHigh: Boolean = false
) {
    SD_480P("480p", "480p (720×480)", 720, 480, false),
    HD_720P("720p", "720p (1280×720)", 1280, 720, false),
    FHD_1080P("1080p", "1080p (1920×1080)", 1920, 1080, false),
    UHD_4K("4K", "4K (3840×2160)", 3840, 2160, true),
    DCI_4K("DCI 4K", "DCI 4K (4096×2160)", 4096, 2160, true),
    UHD_8K("8K", "8K (7680×4320)", 7680, 4320, true)
}

enum class VideoFps(val fps: Int, val label: String) {
    FPS_24(24, "24 fps"),
    FPS_25(25, "25 fps"),
    FPS_30(30, "30 fps"),
    FPS_50(50, "50 fps"),
    FPS_60(60, "60 fps"),
    FPS_120(120, "120 fps"),
    FPS_240(240, "240 fps")
}

enum class VideoCodecOption(val label: String, val mimeType: String, val shortName: String) {
    H264("MP4 / H.264", "video/avc", "MP4"),
    HEVC("HEVC / H.265", "video/hevc", "HEVC")
}

enum class WhiteBalancePreset(val label: String, val kelvin: Int) {
    AUTO("Auto", 5500),
    DAYLIGHT("Daylight", 5600),
    CLOUDY("Cloudy", 6500),
    SHADE("Shade", 7500),
    TUNGSTEN("Tungsten", 3200),
    FLUORESCENT("Fluorescent", 4000),
    MANUAL_KELVIN("Kelvin", 5200)
}

enum class FocusModeOption(val label: String) {
    CONTINUOUS_AF("AF-C"),
    TAP_AF("Tap AF"),
    LOCKED_AF("AF Lock"),
    MANUAL("MF")
}

enum class MeteringModeOption(val label: String) {
    MATRIX("Matrix"),
    CENTER("Center"),
    SPOT("Spot")
}

enum class StabilizationMode(val label: String, val description: String) {
    STANDARD("Standard", "Balanced optical/electronic stabilization"),
    CINEMA_LOCK("Locked", "Maximum horizon stabilization"),
    OFF("Off", "Native unstabilized sensor readout")
}

enum class ColorProfileOption(val label: String, val description: String) {
    STANDARD("Rec.709 Standard", "Accurate contrast and natural saturation"),
    FLAT_NATURAL("Natural Flat", "Reduced contrast & soft roll-off for post grading")
}

enum class ExportQualityPreset(
    val label: String,
    val purpose: String,
    val behavior: String,
    val jpegQuality: Int,
    val bitrateMbps: Int
) {
    EFFICIENT("Efficient", "Smaller shareable files", "Lower bitrate/quality within sensible limits", 75, 12),
    BALANCED("Balanced", "Everyday use", "Good quality and moderate size", 88, 28),
    HIGH("High", "Best practical quality", "Higher quality and larger output", 95, 65),
    MAXIMUM("Maximum / Original", "Preservation", "Original stream or highest valid output", 100, 100),
    CUSTOM("Custom", "Power users", "Manual dimensions, codec, FPS and bitrate", 92, 45)
}

enum class AppThemeOption(
    val id: String,
    val title: String,
    val direction: String,
    val bgHex: Long,
    val accentHex: Long,
    val primaryTextHex: Long,
    val isDark: Boolean = false
) {
    CREAM_MINT(
        "cream_mint",
        "Cream Mint",
        "Warm cream, subtle mint, deep green-black (Default)",
        0xFFF7F3E9,
        0xFF8EB8A5,
        0xFF20332D,
        isDark = false
    ),
    PAPER_SAGE(
        "paper_sage",
        "Paper Sage",
        "Paper white and muted sage",
        0xFFFAF9F5,
        0xFF7B9A8B,
        0xFF1E2D27,
        isDark = false
    ),
    OCEAN(
        "ocean",
        "Ocean",
        "Pale blue and restrained teal",
        0xFFF0F5F7,
        0xFF6B9EAD,
        0xFF1A2E35,
        isDark = false
    ),
    LAVENDER_MIST(
        "lavender_mist",
        "Lavender Mist",
        "Quiet lavender and neutral gray",
        0xFFF4F2F7,
        0xFF988EB8,
        0xFF282334,
        isDark = false
    ),
    SUNSET_SAND(
        "sunset_sand",
        "Sunset Sand",
        "Warm sand and muted terracotta",
        0xFFF8F1E7,
        0xFFC98A70,
        0xFF33231D,
        isDark = false
    ),
    GRAPHITE(
        "graphite",
        "Graphite",
        "Dark graphite with mint accents",
        0xFF1D2220,
        0xFF8EB8A5,
        0xFFF2F5F3,
        isDark = true
    ),
    MIDNIGHT(
        "midnight",
        "Midnight",
        "Deep dark surfaces and accessible contrast",
        0xFF101513,
        0xFF7AB39B,
        0xFFF7F9F8,
        isDark = true
    ),
    SYSTEM(
        "system",
        "System",
        "Follow Android light/dark preference",
        0xFFF7F3E9,
        0xFF8EB8A5,
        0xFF20332D,
        isDark = false
    )
}

enum class FilterFamily(val displayName: String, val shortChip: String, val lookDirection: String) {
    ALL("All Looks", "All", "Complete catalog of 108 film-inspired presets"),
    FAVORITES("Favorites", "Saved", "Your starred presets for instant access"),
    FUJI("Fuji-Inspired", "Fuji", "Soft greens, airy portraits, cinematic tones"),
    KODAK("Kodak-Inspired", "Kodak", "Warm highlights, nostalgic color, rich skin tones"),
    RETRO("Retro / Vintage", "Retro", "Faded color, analog warmth, optional edge effects"),
    CINEMATIC("Cinematic", "Film", "Muted palettes and filmic contrast"),
    MONOCHROME("Monochrome", "B&W", "Classic B&W, silver, noir, high contrast"),
    INSTANT("Instant-Film", "Instant", "Soft contrast, warm whites, nostalgic rendering"),
    NATURE("Nature", "Nature", "Foliage, landscape, ocean and sky"),
    PORTRAIT("Portrait", "Portrait", "Skin-friendly color and controlled highlights"),
    NIGHT("Night / Neon", "Night", "City glow and restrained saturation"),
    CREATIVE("Creative", "Creative", "Duotone, cross-process-inspired experiments")
}

@Immutable
data class FilterPreset(
    val id: String,
    val name: String,
    val family: FilterFamily,
    val subtitle: String,
    val rScale: Float = 1f,
    val gScale: Float = 1f,
    val bScale: Float = 1f,
    val saturation: Float = 1f,
    val contrast: Float = 1f,
    val warmth: Float = 0f,     // -1f to +1f
    val fade: Float = 0f,       // 0f to 0.45f
    val tint: Float = 0f,       // -1f to +1f (green-magenta)
    val grain: Float = 0f,      // 0f to 0.5f
    val vignette: Float = 0f,   // 0f to 0.6f
    val accentColorHex: Long = 0xFF4D7766
)

@Immutable
data class EditRecipe(
    val presetId: String = "original",
    val intensity: Float = 1.0f, // 0f..1f
    val warmthOffset: Float = 0f, // -0.5f..0.5f
    val contrastOffset: Float = 0f, // -0.4f..0.4f
    val fadeOffset: Float = 0f, // 0f..0.4f
    val grainOffset: Float = 0f, // 0f..0.4f
    val vignetteOffset: Float = 0f, // 0f..0.5f
    val aspectRatio: AspectRatioOption = AspectRatioOption.RATIO_4_3
)

@Immutable
data class HardwareCapabilities(
    val activeCameraId: String = "0",
    val hardwareLevelLabel: String = "LIMITED",
    val focalLengthMm: Float = 4.2f,
    val apertureF: Float = 1.8f,
    val maxPhotoWidth: Int = 4032,
    val maxPhotoHeight: Int = 3024,
    val hasBackCamera: Boolean = true,
    val hasFrontCamera: Boolean = true,
    val hasFlashUnit: Boolean = false,
    val supportsRawCapture: Boolean = false,
    val supportsManualSensor: Boolean = false,
    val supportsManualFocus: Boolean = false,
    val supportsOpticalStabilization: Boolean = false,
    val isoRange: IntRange = 100..1600,
    val exposureTimeRangeNs: LongRange = 1_000_000L..500_000_000L,
    val evRange: ClosedFloatingPointRange<Float> = -2.0f..2.0f,
    val zoomStops: List<Float> = listOf(1.0f, 2.0f),
    val minZoom: Float = 1.0f,
    val maxZoom: Float = 4.0f,
    val supportedVideoResolutions: List<VideoResolution> = listOf(
        VideoResolution.HD_720P,
        VideoResolution.FHD_1080P
    ),
    val supportedFpsChoices: List<VideoFps> = listOf(
        VideoFps.FPS_24,
        VideoFps.FPS_30,
        VideoFps.FPS_60
    ),
    val supportedCodecs: List<VideoCodecOption> = listOf(
        VideoCodecOption.H264
    ),
    val encoderNames: List<String> = emptyList(),
    val supports8K: Boolean = false,
    val availableStorageMb: Long = 0L,
    val totalStorageMb: Long = 0L,
    val batteryTempCelsius: Float = 0f,
    val batteryPercent: Int = 100,
    val thermalStatusLabel: String = "Active"
) {
    fun validFpsFor(resolution: VideoResolution, codec: VideoCodecOption): List<VideoFps> {
        val base = supportedFpsChoices.ifEmpty { listOf(VideoFps.FPS_24, VideoFps.FPS_30) }
        return when (resolution) {
            VideoResolution.UHD_4K, VideoResolution.DCI_4K, VideoResolution.UHD_8K ->
                base.filter { it.fps <= 60 }.ifEmpty { listOf(VideoFps.FPS_30) }
            else -> base
        }
    }

    fun maxBitrateMbpsFor(resolution: VideoResolution, codec: VideoCodecOption): Int {
        return when (resolution) {
            VideoResolution.SD_480P -> 16
            VideoResolution.HD_720P -> 40
            VideoResolution.FHD_1080P -> 80
            VideoResolution.UHD_4K, VideoResolution.DCI_4K -> if (codec == VideoCodecOption.HEVC) 140 else 100
            VideoResolution.UHD_8K -> 180
        }
    }
}

@Immutable
data class MediaItemModel(
    val id: Long,
    val title: String,
    val filePath: String,
    val uriString: String,
    val isVideo: Boolean,
    val timestampMs: Long,
    val width: Int,
    val height: Int,
    val durationSec: Int = 0,
    val formatLabel: String = "JPEG",
    val sizeBytes: Long = 0L,
    val appliedPresetId: String = "original",
    val appliedIntensity: Float = 1.0f,
    val isFavorite: Boolean = false,
    val cameraInfo: String = "",
    val sampleSceneIndex: Int = 0
)

@Immutable
data class ExportConfig(
    val qualityPreset: ExportQualityPreset = ExportQualityPreset.HIGH,
    val customWidth: Int = 1920,
    val customHeight: Int = 1080,
    val lockAspectRatio: Boolean = true,
    val aspectRatio: AspectRatioOption = AspectRatioOption.RATIO_4_3,
    val photoFormat: PhotoFormat = PhotoFormat.JPEG,
    val jpegQuality: Int = 95,
    val videoResolution: VideoResolution = VideoResolution.FHD_1080P,
    val videoFps: VideoFps = VideoFps.FPS_30,
    val videoCodec: VideoCodecOption = VideoCodecOption.H264,
    val bitrateMbps: Int = 50,
    val keepAudio: Boolean = true,
    val preserveMetadata: Boolean = true
)

sealed interface ExportJobState {
    data object Idle : ExportJobState
    data class Running(val progress: Float, val stepDescription: String) : ExportJobState
    data class Completed(
        val outputFilePath: String,
        val outputUriString: String,
        val width: Int,
        val height: Int,
        val sizeBytes: Long,
        val formatLabel: String,
        val wasUpscaled: Boolean
    ) : ExportJobState
    data class Cancelled(val message: String) : ExportJobState
    data class Failed(val errorMessage: String) : ExportJobState
}
