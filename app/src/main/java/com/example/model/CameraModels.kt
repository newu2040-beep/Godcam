package com.example.model

import androidx.compose.runtime.Immutable
import kotlin.math.roundToInt

enum class CaptureMode(val label: String, val isVideo: Boolean, val isPro: Boolean) {
    PHOTO("Photo", isVideo = false, isPro = false),
    VIDEO("Video", isVideo = true, isPro = false),
    PRO_PHOTO("Pro Photo", isVideo = false, isPro = true),
    PRO_VIDEO("Pro Video", isVideo = true, isPro = true),
    NIGHT("Night", isVideo = false, isPro = false)
}

enum class CompactLayoutMode(val label: String, val shortLabel: String, val description: String) {
    AUTO("Auto Adaptive", "Auto", "Automatically scales controls for small and large displays"),
    COMPACT("Compact Mode", "Compact", "Dense, one-handed pro layout with maximum viewfinder area"),
    COMFORTABLE("Spacious Mode", "Spacious", "Larger controls and generous spacing for big screens")
}

enum class AspectRatioOption(
    val label: String,
    val widthRatio: Float,
    val heightRatio: Float,
    val isCinematicGuide: Boolean = false,
    val description: String
) {
    RATIO_4_3("4:3", 4f, 3f, false, "Full Sensor Native (Photo default)"),
    RATIO_3_4("3:4", 3f, 4f, false, "Vertical Full Sensor Portrait"),
    RATIO_16_9("16:9", 16f, 9f, false, "Widescreen HD / 4K Native"),
    RATIO_9_16("9:16", 9f, 16f, false, "Vertical Full-Frame Cinema / Story"),
    RATIO_1_1("1:1", 1f, 1f, false, "Square Medium-Format 6×6"),
    RATIO_3_2("3:2", 3f, 2f, false, "Classic 35mm Film Frame"),
    RATIO_2_3("2:3", 2f, 3f, false, "Vertical 35mm Portrait Frame"),
    RATIO_4_5("4:5", 4f, 5f, false, "Editorial Portrait 4×5"),
    RATIO_5_4("5:4", 5f, 4f, false, "Large-Format 5×4 Landscape"),
    RATIO_6_7("6:7", 6f, 7f, false, "Medium-Format 6×7 Portrait"),
    RATIO_7_6("7:6", 7f, 6f, false, "Medium-Format 7×6 Landscape"),
    RATIO_18_9("18:9", 18f, 9f, false, "Modern Mobile Full-Display 2:1"),
    RATIO_21_9("21:9", 21f, 9f, true, "Ultra-Wide Cinema 21:9"),
    RATIO_1_43("1.43:1", 1.43f, 1f, false, "IMAX 70mm Full Frame 1.43:1"),
    RATIO_1_618("1.618:1", 1.618f, 1f, false, "Golden Ratio Harmonic Frame"),
    GUIDE_1_85("1.85:1", 1.85f, 1f, true, "Academy Flat Theatrical Widescreen"),
    GUIDE_2_00("2.00:1", 2.0f, 1f, true, "Univisium 2:1 Widescreen Crop"),
    GUIDE_2_20("2.20:1", 2.2f, 1f, true, "70mm Todd-AO Widescreen Crop"),
    GUIDE_2_35("2.35:1", 2.35f, 1f, true, "Anamorphic Scope 2.35:1 Crop"),
    GUIDE_2_39("2.39:1", 2.39f, 1f, true, "Modern DCI Scope 2.39:1 Crop"),
    GUIDE_2_76("2.76:1", 2.76f, 1f, true, "Ultra Panavision 70mm 2.76:1 Crop"),
    XPAN_65_24("65:24", 65f, 24f, true, "Panoramic XPan 65×24mm Format");

    val aspectValue: Float get() = widthRatio / heightRatio

    fun computeDimensionsForLongEdge(longEdge: Int): Pair<Int, Int> {
        val safeLong = longEdge.coerceIn(320, 7680).let { if (it % 2 != 0) it + 1 else it }
        val ratio = aspectValue
        return if (ratio >= 1f) {
            val w = safeLong
            val h = ((w / ratio).roundToInt().coerceAtLeast(240)).let { if (it % 2 != 0) it + 1 else it }
            w to h
        } else {
            val h = safeLong
            val w = ((h * ratio).roundToInt().coerceAtLeast(240)).let { if (it % 2 != 0) it + 1 else it }
            w to h
        }
    }
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

enum class VideoDurationOption(
    val label: String,
    val shortBadge: String,
    val seconds: Int,
    val description: String
) {
    CONTINUOUS("Continuous (Unlimited)", "∞", 0, "Records continuously until you tap Stop"),
    SEC_3("3 Seconds", "3s", 3, "Automatically stops and saves after 3s"),
    SEC_5("5 Seconds", "5s", 5, "Automatically stops and saves after 5s"),
    SEC_10("10 Seconds", "10s", 10, "Automatically stops and saves after 10s"),
    SEC_15("15 Seconds", "15s", 15, "Automatically stops and saves after 15s"),
    SEC_30("30 Seconds", "30s", 30, "Automatically stops and saves after 30s"),
    SEC_60("60 Seconds", "60s", 60, "Automatically stops and saves after 60s"),
    CUSTOM("Custom Duration", "Custom", -1, "Set any custom recording duration in seconds")
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
    UHD_8K("8K", "8K (7680×4320)", 7680, 4320, true);

    val longEdge: Int get() = maxOf(width, height)
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
    MACRO("Macro"),
    LOCKED_AF("AF Lock"),
    MANUAL("MF")
}

enum class MeteringModeOption(val label: String) {
    MATRIX("Matrix"),
    CENTER("Center"),
    SPOT("Spot")
}

enum class StabilizationMode(val label: String, val description: String) {
    STANDARD("Standard EIS", "Hardware electronic video stabilization"),
    OPTICAL_OIS("Optical OIS", "Hardware lens optical stabilization"),
    CINEMA_LOCK("Cinema Lock", "Maximum optical + electronic horizon lock"),
    OFF("Off", "Native unstabilized sensor readout")
}

enum class ColorProfileOption(val label: String, val description: String) {
    STANDARD("Rec.709 Standard", "Accurate contrast and natural saturation"),
    FLAT_NATURAL("Natural Flat / Log", "Wide dynamic range flat curve for color grading")
}

enum class ShutterSoundStyle(
    val label: String,
    val shortBadge: String,
    val description: String
) {
    FUJI_MECHANICAL(
        "Fuji X Mechanical",
        "Fuji X",
        "Crisp Fujifilm X-Series dual-curtain focal-plane click"
    ),
    FUJI_LEAF(
        "Fuji X100 Leaf",
        "X100 Leaf",
        "Whisper-quiet Fujifilm X100 optical leaf shutter snick"
    ),
    LEICA_RANGEFINDER(
        "Rangefinder M",
        "Rangefinder",
        "Dampened brass cloth-curtain rangefinder click"
    ),
    CLASSIC_SLR(
        "Classic Film SLR",
        "35mm SLR",
        "Tactile 35mm mirror slap + mechanical curtain release"
    ),
    DIGITAL_PRO(
        "Digital Studio",
        "Digital",
        "Clean mirrorless studio confirmation chirp"
    ),
    OFF(
        "Off (Silent)",
        "Silent",
        "Mute all camera shutter and video start/stop sounds"
    )
}

enum class AudioRecordingFormat(
    val label: String,
    val specsLabel: String,
    val shortBadge: String,
    val mimeType: String,
    val sampleRateHz: Int,
    val channelCount: Int,
    val bitrateBps: Int
) {
    AAC_LC_STEREO_48K(
        "AAC-LC Stereo (48kHz)",
        "48 kHz • 320 kbps • 2ch Stereo AAC-LC",
        "AAC 48k",
        "audio/mp4a-latm",
        48000,
        2,
        320_000
    ),
    HE_AAC_STEREO_48K(
        "HE-AAC Studio (48kHz)",
        "48 kHz • 192 kbps • High-Efficiency Stereo",
        "HE-AAC",
        "audio/mp4a-latm",
        48000,
        2,
        192_000
    ),
    PCM_LPCM_48K(
        "Uncompressed LPCM (16-Bit)",
        "48 kHz • 1536 kbps • Linear PCM Master",
        "LPCM 48k",
        "audio/raw",
        48000,
        2,
        1_536_000
    ),
    OPUS_STUDIO_48K(
        "Opus Low-Latency (48kHz)",
        "48 kHz • 256 kbps • Wideband Opus",
        "Opus 48k",
        "audio/opus",
        48000,
        2,
        256_000
    ),
    FLAC_LOSSLESS_48K(
        "FLAC Lossless (48kHz)",
        "48 kHz • Lossless Archival Stereo",
        "FLAC",
        "audio/flac",
        48000,
        2,
        768_000
    ),
    AAC_MONO_VOICE_44K(
        "Mono Voice (44.1kHz)",
        "44.1 kHz • 128 kbps • 1ch Vocal Focus",
        "Mono 44.1k",
        "audio/mp4a-latm",
        44100,
        1,
        128_000
    ),
    MUTED(
        "Muted (Silent Track)",
        "No microphone audio recorded",
        "Muted",
        "none",
        0,
        0,
        0
    )
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
    CUSTOM("Custom", "Power users", "Manual dimensions, codec, FPS and bitrate", 96, 48)
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
        "Warm cream, bold mint, deep forest ink (Default)",
        0xFFF6F1E6,
        0xFF72AE95,
        0xFF14241E,
        isDark = false
    ),
    PASTEL_MATCHA(
        "pastel_matcha",
        "Pastel Matcha",
        "Creamy Kyoto matcha pastel with high-contrast forest ink",
        0xFFF1F7ED,
        0xFF7EC084,
        0xFF122616,
        isDark = false
    ),
    PASTEL_PEACH(
        "pastel_peach",
        "Pastel Peach",
        "Soft apricot-peach pastel with bold espresso contrast",
        0xFFFFF3EC,
        0xFFF09C7E,
        0xFF2B150E,
        isDark = false
    ),
    PASTEL_SAKURA(
        "pastel_sakura",
        "Pastel Sakura",
        "Blossom pink pastel with deep plum-charcoal contrast",
        0xFFFFF1F5,
        0xFFE68EAD,
        0xFF2B101B,
        isDark = false
    ),
    PASTEL_BUTTER(
        "pastel_butter",
        "Pastel Butter",
        "Sunlit buttercup pastel with deep umber typography",
        0xFFFFFAE6,
        0xFFE2BA4C,
        0xFF281F06,
        isDark = false
    ),
    PASTEL_SKY(
        "pastel_sky",
        "Pastel Sky",
        "Airy powder blue pastel with crisp midnight-navy ink",
        0xFFEDF5FF,
        0xFF79ADE6,
        0xFF0E2036,
        isDark = false
    ),
    PASTEL_LILAC(
        "pastel_lilac",
        "Pastel Lilac",
        "Dreamy wisteria lilac pastel with bold indigo contrast",
        0xFFF5EEFF,
        0xFFAC8BE6,
        0xFF1F1135,
        isDark = false
    ),
    PASTEL_PISTACHIO(
        "pastel_pistachio",
        "Pastel Pistachio",
        "Artisanal pistachio gelato with deep obsidian-green ink",
        0xFFECF8F1,
        0xFF6EC498,
        0xFF0E261A,
        isDark = false
    ),
    PAPER_SAGE(
        "paper_sage",
        "Paper Sage",
        "Crisp gallery paper and high-contrast sage",
        0xFFFAF9F5,
        0xFF688E7C,
        0xFF14221D,
        isDark = false
    ),
    OCEAN(
        "ocean",
        "Ocean Mist",
        "Nordic seafoam blue and deep slate teal",
        0xFFEEF4F7,
        0xFF5A93A4,
        0xFF12242B,
        isDark = false
    ),
    LAVENDER_MIST(
        "lavender_mist",
        "Lavender Mist",
        "Muted botanical lavender and deep slate",
        0xFFF3F0F7,
        0xFF8C80B0,
        0xFF1E192B,
        isDark = false
    ),
    SUNSET_SAND(
        "sunset_sand",
        "Sunset Sand",
        "Warm desert sand and bold terracotta",
        0xFFF8EFE4,
        0xFFC47D60,
        0xFF291A14,
        isDark = false
    ),
    GRAPHITE(
        "graphite",
        "Graphite Pro",
        "High-contrast dark graphite with luminous mint accents",
        0xFF161B19,
        0xFF8EB8A5,
        0xFFF5F8F6,
        isDark = true
    ),
    MIDNIGHT(
        "midnight",
        "Midnight OLED",
        "Deep obsidian dark surfaces with sharp emerald contrast",
        0xFF0B0F0E,
        0xFF7AB39B,
        0xFFFAFCFB,
        isDark = true
    ),
    SYSTEM(
        "system",
        "System Auto",
        "Follow Android device light/dark preference",
        0xFFF6F1E6,
        0xFF72AE95,
        0xFF14241E,
        isDark = false
    )
}

enum class FilterFamily(val displayName: String, val shortChip: String, val lookDirection: String) {
    ALL("All Looks", "All", "Complete catalog of 165+ film, analog, and pastel presets"),
    FAVORITES("Favorites", "Saved", "Your starred presets for instant access"),
    PASTEL("Pastel & Airy", "Pastel", "Soft pastel tones, creamy highlights, dreamy editorial color"),
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
    val supportsMacroFocus: Boolean = true,
    val minFocusDistanceDiopters: Float = 10.0f,
    val supportsOpticalStabilization: Boolean = false,
    val supportedStabilizationModes: List<StabilizationMode> = listOf(
        StabilizationMode.STANDARD,
        StabilizationMode.OFF
    ),
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
    val supportedPhotoFormats: List<PhotoFormat> = listOf(
        PhotoFormat.JPEG,
        PhotoFormat.PNG,
        PhotoFormat.WEBP
    ),
    val supportedAudioFormats: List<AudioRecordingFormat> = listOf(
        AudioRecordingFormat.AAC_LC_STEREO_48K,
        AudioRecordingFormat.PCM_LPCM_48K,
        AudioRecordingFormat.AAC_MONO_VOICE_44K,
        AudioRecordingFormat.MUTED
    ),
    val encoderNames: List<String> = emptyList(),
    val audioEncoderNames: List<String> = emptyList(),
    val nativeAudioSampleRateHz: Int = 48000,
    val supports8K: Boolean = false,
    val availableStorageMb: Long = 0L,
    val totalStorageMb: Long = 0L,
    val batteryTempCelsius: Float = 0f,
    val batteryPercent: Int = 100,
    val thermalStatusLabel: String = "Nominal"
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
    val useCustomVideoSize: Boolean = false,
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
