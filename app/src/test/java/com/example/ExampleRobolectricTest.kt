package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.FilterCatalog
import com.example.data.ImageFilterEngine
import com.example.model.EditRecipe
import com.example.model.HardwareCapabilities
import com.example.model.VideoCodecOption
import com.example.model.VideoFps
import com.example.model.VideoResolution
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read GODCAM app_name and verify 100 plus distinct film presets`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("GODCAM", appName)

        // Verify 160+ unique presets exist with unique IDs and names
        val presets = FilterCatalog.allPresets
        assertTrue("Expected at least 160 presets, got ${presets.size}", presets.size >= 160)
        assertEquals(presets.size, presets.map { it.id }.toSet().size)
        assertEquals(presets.size, presets.map { it.name }.toSet().size)

        // Verify 22 aspect ratios and non-stretching dimension calculation
        assertTrue(com.example.model.AspectRatioOption.entries.size >= 22)
        val portrait916 = com.example.model.AspectRatioOption.RATIO_9_16.computeDimensionsForLongEdge(1920)
        assertEquals(1080 to 1920, portrait916)
        val landscape169 = com.example.model.AspectRatioOption.RATIO_16_9.computeDimensionsForLongEdge(1920)
        assertEquals(1920 to 1080, landscape169)

        // Verify VideoDurationOption (Continuous vs preset/custom auto-stop)
        assertEquals(0, com.example.model.VideoDurationOption.CONTINUOUS.seconds)
        assertEquals(15, com.example.model.VideoDurationOption.SEC_15.seconds)
        assertEquals(-1, com.example.model.VideoDurationOption.CUSTOM.seconds)

        // Verify Macro Focus and Optical OIS Stabilization modes exist
        assertTrue(com.example.model.FocusModeOption.MACRO in com.example.model.FocusModeOption.entries)
        assertTrue(com.example.model.StabilizationMode.OPTICAL_OIS in com.example.model.StabilizationMode.entries)

        // Verify 26 themes including Pastel, Editorial, and Pro Dark themes
        assertEquals(26, com.example.model.AppThemeOption.entries.size)

        // Verify Megapixel Camera options (12 MP, 24 MP, 48 MP, 50 MP, 64 MP, 108 MP, 200 MP)
        val mp50Dims = com.example.model.PhotoMegapixelOption.MP_50.computeDimensions(com.example.model.AspectRatioOption.RATIO_4_3)
        assertEquals(8192 to 6144, mp50Dims)
        val mp48Dims = com.example.model.PhotoMegapixelOption.MP_48.computeDimensions(com.example.model.AspectRatioOption.RATIO_4_3)
        assertEquals(8000 to 6000, mp48Dims)
        val mp12Dims = com.example.model.PhotoMegapixelOption.MP_12.computeDimensions(com.example.model.AspectRatioOption.RATIO_4_3)
        assertEquals(4032 to 3024, mp12Dims)

        // Verify LOG Video profiles (G-Log, Cinema D-Log M, S-Log3)
        assertTrue(com.example.model.ColorProfileOption.FLAT_NATURAL.isLog)
        assertTrue(com.example.model.ColorProfileOption.CINE_D_LOG.isLog)
        assertTrue(com.example.model.ColorProfileOption.S_LOG3.isLog)

        // Verify ColorMatrix calculation for Fuji Pro 400H + ProOpticalState (ISO, Shutter, WB Kelvin, LOG)
        val fuji = FilterCatalog.getById("fuji_pro_400h")
        val proOptical = com.example.model.ProOpticalState(
            isAutoIso = false,
            manualIso = 800,
            isAutoShutter = false,
            shutterDenominator = 60,
            exposureEv = 0.5f,
            whiteBalance = com.example.model.WhiteBalancePreset.MANUAL_KELVIN,
            manualKelvin = 4200,
            colorProfile = com.example.model.ColorProfileOption.CINE_D_LOG
        )
        val matrix = ImageFilterEngine.buildFloatMatrix(
            preset = fuji,
            recipe = EditRecipe(presetId = fuji.id, intensity = 0.8f),
            extraEv = 0.5f,
            proOptical = proOptical
        )
        assertEquals(20, matrix.size)

        // Verify runtime capability matrix supports 100x zoom and filters valid FPS by resolution
        val caps = HardwareCapabilities()
        assertEquals(100.0f, caps.maxZoom)
        assertTrue(100.0f in caps.zoomStops)
        val fps4K = caps.validFpsFor(VideoResolution.UHD_4K, VideoCodecOption.H264)
        assertTrue(VideoFps.FPS_60 in fps4K)
        assertTrue(VideoFps.FPS_240 !in fps4K)
    }
}
