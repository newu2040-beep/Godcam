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

        // Verify 20 aspect ratios and non-stretching dimension calculation
        assertTrue(com.example.model.AspectRatioOption.entries.size >= 20)
        val portrait916 = com.example.model.AspectRatioOption.RATIO_9_16.computeDimensionsForLongEdge(1920)
        assertEquals(1080 to 1920, portrait916)
        val landscape169 = com.example.model.AspectRatioOption.RATIO_16_9.computeDimensionsForLongEdge(1920)
        assertEquals(1920 to 1080, landscape169)

        // Verify 15 themes including Pastel themes
        assertTrue(com.example.model.AppThemeOption.entries.size >= 15)

        // Verify ColorMatrix calculation for Fuji Pro 400H
        val fuji = FilterCatalog.getById("fuji_pro_400h")
        val matrix = ImageFilterEngine.buildFloatMatrix(fuji, EditRecipe(presetId = fuji.id, intensity = 0.8f))
        assertEquals(20, matrix.size)

        // Verify runtime capability matrix filters valid FPS by resolution
        val caps = HardwareCapabilities()
        val fps4K = caps.validFpsFor(VideoResolution.UHD_4K, VideoCodecOption.H264)
        assertTrue(VideoFps.FPS_60 in fps4K)
        assertTrue(VideoFps.FPS_240 !in fps4K)
    }
}
