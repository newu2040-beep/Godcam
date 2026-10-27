package com.example.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrixColorFilter
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.compose.ui.graphics.ColorMatrix
import com.example.model.AspectRatioOption
import com.example.model.ColorProfileOption
import com.example.model.EditRecipe
import com.example.model.FilterPreset
import com.example.model.MediaItemModel
import com.example.model.ProOpticalState
import com.example.model.WhiteBalancePreset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.random.Random

object ImageFilterEngine {

    /**
     * Pre-computed 160x160 organic film grain tile shader.
     * Replaces per-frame 10,000-rect loops with a single GPU/Canvas shader draw call,
     * eliminating frame drops during 1080p 30fps/60fps video recording and live filtering.
     */
    private val cachedGrainShader: BitmapShader by lazy {
        val tileSize = 160
        val bmp = Bitmap.createBitmap(tileSize, tileSize, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(tileSize * tileSize)
        val rng = Random(42)
        for (i in pixels.indices) {
            val roll = rng.nextInt(100)
            pixels[i] = when {
                roll < 22 -> {
                    val a = rng.nextInt(28, 120)
                    Color.argb(a, 250, 246, 238)
                }
                roll < 44 -> {
                    val a = rng.nextInt(28, 120)
                    Color.argb(a, 16, 22, 20)
                }
                else -> Color.TRANSPARENT
            }
        }
        bmp.setPixels(pixels, 0, tileSize, 0, 0, tileSize, tileSize)
        BitmapShader(bmp, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
    }

    private val grainShaderMatrix = Matrix()

    /**
     * Loads the actual saved photo or video frame from [MediaItemModel.filePath] or [MediaItemModel.uriString].
     */
    fun loadBitmapForMediaItem(
        context: Context,
        item: MediaItemModel?,
        targetWidth: Int = 640,
        targetHeight: Int = 640
    ): Bitmap? {
        if (item == null) return null

        if (item.filePath.isNotBlank()) {
            val file = File(item.filePath)
            if (file.exists() && file.length() > 0L) {
                runCatching {
                    val opts = BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }
                    BitmapFactory.decodeFile(file.absolutePath, opts)
                    if (opts.outWidth > 0 && opts.outHeight > 0) {
                        var sampleSize = 1
                        while (opts.outWidth / sampleSize > targetWidth * 2 ||
                            opts.outHeight / sampleSize > targetHeight * 2
                        ) {
                            sampleSize *= 2
                        }
                        val decodeOpts = BitmapFactory.Options().apply {
                            inSampleSize = sampleSize
                        }
                        BitmapFactory.decodeFile(file.absolutePath, decodeOpts)?.let { return it }
                    }
                }
            }
        }

        if (item.uriString.isNotBlank()) {
            runCatching {
                if (item.isVideo) {
                    val retriever = MediaMetadataRetriever()
                    if (item.uriString.startsWith("content://")) {
                        retriever.setDataSource(context, Uri.parse(item.uriString))
                    } else {
                        val path = Uri.parse(item.uriString).path ?: item.uriString
                        retriever.setDataSource(path)
                    }
                    val frame = retriever.getFrameAtTime(0)
                    retriever.release()
                    if (frame != null) return frame
                } else if (item.uriString.startsWith("content://")) {
                    val uri = Uri.parse(item.uriString)
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        BitmapFactory.decodeStream(stream)?.let { return it }
                    }
                }
            }
        }
        return null
    }

    /**
     * Builds a 4x5 Jetpack Compose [ColorMatrix] from a [FilterPreset], [EditRecipe], and [ProOpticalState].
     */
    fun buildComposeColorMatrix(
        preset: FilterPreset,
        recipe: EditRecipe = EditRecipe(presetId = preset.id),
        extraEv: Float = 0f,
        proOptical: ProOpticalState = ProOpticalState()
    ): ColorMatrix {
        return ColorMatrix(buildFloatMatrix(preset, recipe, extraEv, proOptical))
    }

    /**
     * Builds a native [android.graphics.ColorMatrix] for GPU RenderEffect and Canvas rendering.
     */
    fun buildAndroidColorMatrix(
        preset: FilterPreset,
        recipe: EditRecipe = EditRecipe(presetId = preset.id),
        extraEv: Float = 0f,
        proOptical: ProOpticalState = ProOpticalState()
    ): android.graphics.ColorMatrix {
        return android.graphics.ColorMatrix(buildFloatMatrix(preset, recipe, extraEv, proOptical))
    }

    /**
     * Builds a 20-float array for Android's [android.graphics.ColorMatrix] and Compose's [ColorMatrix].
     * Incorporates filter preset, fine-tune recipe, manual ISO/Shutter/EV response, White Balance Kelvin,
     * and LOG video profiles (G-Log, D-Log M, S-Log3, HLG HDR) for instant live viewfinder preview.
     */
    fun buildFloatMatrix(
        preset: FilterPreset,
        recipe: EditRecipe = EditRecipe(presetId = preset.id),
        extraEv: Float = 0f,
        proOptical: ProOpticalState = ProOpticalState()
    ): FloatArray {
        val t = if (preset.id == "original") 0f else recipe.intensity.coerceIn(0f, 1f)
        val combinedMatrix = android.graphics.ColorMatrix()

        // 1. Profile saturation & LOG curve characteristics
        val profileSatMultiplier = when (proOptical.colorProfile) {
            ColorProfileOption.STANDARD -> 1.0f
            ColorProfileOption.FLAT_NATURAL -> 0.72f
            ColorProfileOption.CINE_D_LOG -> 0.65f
            ColorProfileOption.S_LOG3 -> 0.58f
            ColorProfileOption.HLG_HDR -> 1.14f
        }
        val sat = (lerp(1f, preset.saturation, t) * profileSatMultiplier).coerceIn(0f, 2.2f)
        val satMatrix = android.graphics.ColorMatrix().apply {
            setSaturation(sat)
        }
        combinedMatrix.postConcat(satMatrix)

        // 2. White Balance Preset & Manual Kelvin (2500K..9500K) shift
        val wbWarmthShift = when (proOptical.whiteBalance) {
            WhiteBalancePreset.AUTO -> 0f
            WhiteBalancePreset.DAYLIGHT -> 0.04f
            WhiteBalancePreset.CLOUDY -> 0.18f
            WhiteBalancePreset.SHADE -> 0.28f
            WhiteBalancePreset.TUNGSTEN -> -0.28f
            WhiteBalancePreset.FLUORESCENT -> -0.14f
            WhiteBalancePreset.MANUAL_KELVIN -> ((proOptical.manualKelvin - 5500f) / 3800f).coerceIn(-0.8f, 0.9f)
        }
        val wbTintShift = if (proOptical.whiteBalance == WhiteBalancePreset.FLUORESCENT) 0.10f else 0f

        // 3. Manual ISO + Manual Shutter Speed optical exposure response
        val isoEvShift = if (!proOptical.isAutoIso) {
            val ratio = (proOptical.manualIso.coerceIn(50, 12800).toFloat() / 400f)
            (ln(ratio) / ln(2f)) * 0.42f
        } else 0f

        val shutterEvShift = if (!proOptical.isAutoShutter) {
            val ratio = (125f / proOptical.shutterDenominator.coerceIn(2, 8000).toFloat())
            (ln(ratio) / ln(2f)) * 0.40f
        } else 0f

        val totalEv = (extraEv + proOptical.exposureEv + isoEvShift + shutterEvShift).coerceIn(-3.5f, 3.5f)

        // 4. LOG Profile dynamic range compression (shadow lift + highlight roll-off)
        val profileContrastMul: Float
        val profileFadeLift: Float
        val profileHighlightScale: Float
        when (proOptical.colorProfile) {
            ColorProfileOption.STANDARD -> {
                profileContrastMul = 1.0f
                profileFadeLift = 0.0f
                profileHighlightScale = 1.0f
            }
            ColorProfileOption.FLAT_NATURAL -> {
                profileContrastMul = 0.78f
                profileFadeLift = 0.14f
                profileHighlightScale = 0.93f
            }
            ColorProfileOption.CINE_D_LOG -> {
                profileContrastMul = 0.72f
                profileFadeLift = 0.18f
                profileHighlightScale = 0.90f
            }
            ColorProfileOption.S_LOG3 -> {
                profileContrastMul = 0.66f
                profileFadeLift = 0.22f
                profileHighlightScale = 0.87f
            }
            ColorProfileOption.HLG_HDR -> {
                profileContrastMul = 1.08f
                profileFadeLift = 0.02f
                profileHighlightScale = 1.03f
            }
        }

        // 5. RGB Channel Gain + Warmth + Tint + Contrast + Fade + EV
        val totalWarmth = (preset.warmth * t) + recipe.warmthOffset + wbWarmthShift
        val rGain = (lerp(1f, preset.rScale, t) + (totalWarmth * 0.18f)) * profileHighlightScale
        val gGain = (lerp(1f, preset.gScale, t) - (preset.tint * t * 0.12f) + (wbTintShift * 0.08f)) * profileHighlightScale
        val bGain = (lerp(1f, preset.bScale, t) - (totalWarmth * 0.18f)) * profileHighlightScale

        val contrast = ((lerp(1f, preset.contrast, t) + recipe.contrastOffset) * profileContrastMul).coerceIn(0.48f, 1.85f)
        val fade = ((preset.fade * t) + recipe.fadeOffset + profileFadeLift).coerceIn(0f, 0.52f)
        val evOffset = totalEv * 22f

        val cScale = contrast * (1f - fade * 0.32f)
        val cOffset = 128f * (1f - cScale) + (fade * 48f) + evOffset

        val warmR = totalWarmth * 22f
        val warmG = totalWarmth * 5f
        val warmB = -totalWarmth * 24f

        val gradeArray = floatArrayOf(
            rGain * cScale, 0f, 0f, 0f, cOffset + warmR,
            0f, gGain * cScale, 0f, 0f, cOffset + warmG,
            0f, 0f, bGain * cScale, 0f, cOffset + warmB,
            0f, 0f, 0f, 1f, 0f
        )
        combinedMatrix.postConcat(android.graphics.ColorMatrix(gradeArray))

        return combinedMatrix.array
    }

    private fun lerp(start: Float, stop: Float, fraction: Float): Float {
        return start + (stop - start) * fraction
    }

    /**
     * Renders a filtered, cropped, and optionally resized [Bitmap] off the main thread.
     * Bakes the ColorMatrix, ProOpticalState (ISO/Shutter/Kelvin/LOG), optical vignette,
     * analog film grain, and hybrid zoom up to 100x directly into the output pixels.
     */
    suspend fun renderFilteredBitmap(
        source: Bitmap,
        preset: FilterPreset,
        recipe: EditRecipe,
        targetWidth: Int? = null,
        targetHeight: Int? = null,
        applyCrop: Boolean = false,
        extraEv: Float = 0f,
        proOptical: ProOpticalState = ProOpticalState(),
        extraDigitalZoom: Float = 1.0f
    ): Bitmap = withContext(Dispatchers.Default) {
        renderFilteredBitmapSync(
            source = source,
            preset = preset,
            recipe = recipe,
            targetWidth = targetWidth,
            targetHeight = targetHeight,
            applyCrop = applyCrop,
            extraEv = extraEv,
            proOptical = proOptical,
            extraDigitalZoom = extraDigitalZoom
        )
    }

    /**
     * Synchronous high-speed bitmap renderer for background video frame processing and photo pipelines.
     */
    fun renderFilteredBitmapSync(
        source: Bitmap,
        preset: FilterPreset,
        recipe: EditRecipe,
        targetWidth: Int? = null,
        targetHeight: Int? = null,
        applyCrop: Boolean = false,
        extraEv: Float = 0f,
        proOptical: ProOpticalState = ProOpticalState(),
        extraDigitalZoom: Float = 1.0f
    ): Bitmap {
        val cropped = if (applyCrop) {
            cropToAspectRatio(source, recipe.aspectRatio)
        } else {
            source
        }

        val finalW = (targetWidth ?: cropped.width).coerceAtLeast(16).let { if (it % 2 != 0) it + 1 else it }
        val finalH = (targetHeight ?: cropped.height).coerceAtLeast(16).let { if (it % 2 != 0) it + 1 else it }

        val output = Bitmap.createBitmap(finalW, finalH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        paint.colorFilter = ColorMatrixColorFilter(buildAndroidColorMatrix(preset, recipe, extraEv, proOptical))

        // Always center-crop source rect to match finalW:finalH AND apply extraDigitalZoom (up to 100x)
        val srcRatio = cropped.width.toFloat() / cropped.height.coerceAtLeast(1).toFloat()
        val dstRatio = finalW.toFloat() / finalH.coerceAtLeast(1).toFloat()
        val baseRect = if (abs(srcRatio - dstRatio) > 0.015f) {
            if (srcRatio > dstRatio) {
                val cropW = (cropped.height * dstRatio).roundToInt().coerceIn(1, cropped.width)
                val left = ((cropped.width - cropW) / 2).coerceAtLeast(0)
                android.graphics.Rect(left, 0, left + cropW, cropped.height)
            } else {
                val cropH = (cropped.width / dstRatio).roundToInt().coerceIn(1, cropped.height)
                val top = ((cropped.height - cropH) / 2).coerceAtLeast(0)
                android.graphics.Rect(0, top, cropped.width, top + cropH)
            }
        } else {
            android.graphics.Rect(0, 0, cropped.width, cropped.height)
        }

        val zoomFactor = extraDigitalZoom.coerceIn(1.0f, 100.0f)
        val srcRect = if (zoomFactor > 1.01f) {
            val zoomedW = (baseRect.width() / zoomFactor).roundToInt().coerceAtLeast(8)
            val zoomedH = (baseRect.height() / zoomFactor).roundToInt().coerceAtLeast(8)
            val cx = baseRect.centerX()
            val cy = baseRect.centerY()
            val left = (cx - zoomedW / 2).coerceIn(0, (cropped.width - zoomedW).coerceAtLeast(0))
            val top = (cy - zoomedH / 2).coerceIn(0, (cropped.height - zoomedH).coerceAtLeast(0))
            android.graphics.Rect(left, top, left + zoomedW, top + zoomedH)
        } else {
            baseRect
        }

        val dstRect = RectF(0f, 0f, finalW.toFloat(), finalH.toFloat())
        canvas.drawBitmap(cropped, srcRect, dstRect, paint)

        // Apply optical vignette if active
        val t = if (preset.id == "original") 0f else recipe.intensity.coerceIn(0f, 1f)
        val totalVignette = ((preset.vignette * t) + recipe.vignetteOffset).coerceIn(0f, 0.75f)
        if (totalVignette > 0.02f) {
            val radius = max(finalW, finalH) * 0.72f
            val vigPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = RadialGradient(
                    finalW / 2f,
                    finalH / 2f,
                    radius,
                    intArrayOf(
                        Color.TRANSPARENT,
                        Color.argb((totalVignette * 95).roundToInt().coerceIn(0, 200), 12, 18, 15),
                        Color.argb((totalVignette * 215).roundToInt().coerceIn(0, 240), 8, 12, 10)
                    ),
                    floatArrayOf(0.42f, 0.78f, 1.0f),
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(dstRect, vigPaint)
        }

        // Apply organic film grain via cached BitmapShader tile (single draw call)
        val isoGrainBoost = if (!proOptical.isAutoIso && proOptical.manualIso > 1600) {
            ((proOptical.manualIso - 1600f) / 16000f).coerceIn(0f, 0.18f)
        } else 0f
        val totalGrain = ((preset.grain * t) + recipe.grainOffset + isoGrainBoost).coerceIn(0f, 0.65f)
        if (totalGrain > 0.02f) {
            drawOrganicGrain(canvas, finalW, finalH, totalGrain)
        }

        if (cropped !== source) {
            cropped.recycle()
        }
        return output
    }

    private fun cropToAspectRatio(source: Bitmap, ratio: AspectRatioOption): Bitmap {
        val srcW = source.width.toFloat()
        val srcH = source.height.toFloat()
        val targetRatio = ratio.aspectValue
        val currentRatio = srcW / srcH

        if (abs(currentRatio - targetRatio) < 0.02f) return source

        val cropW: Int
        val cropH: Int
        if (currentRatio > targetRatio) {
            cropH = source.height
            cropW = (srcH * targetRatio).roundToInt().coerceIn(16, source.width)
        } else {
            cropW = source.width
            cropH = (srcW / targetRatio).roundToInt().coerceIn(16, source.height)
        }
        val x = ((source.width - cropW) / 2).coerceAtLeast(0)
        val y = ((source.height - cropH) / 2).coerceAtLeast(0)
        return Bitmap.createBitmap(source, x, y, cropW, cropH)
    }

    private fun drawOrganicGrain(canvas: Canvas, width: Int, height: Int, grainStrength: Float) {
        val scale = if (width > 1200) 1.6f else 1.0f
        grainShaderMatrix.reset()
        grainShaderMatrix.setScale(scale, scale)
        cachedGrainShader.setLocalMatrix(grainShaderMatrix)
        val grainPaint = Paint(Paint.FILTER_BITMAP_FLAG).apply {
            shader = cachedGrainShader
            alpha = (grainStrength * 145f).roundToInt().coerceIn(10, 115)
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), grainPaint)
    }

    /**
     * Computes a 32-bin normalized luminance histogram from a real camera frame [Bitmap].
     */
    fun computeLumaHistogram(bitmap: Bitmap?): List<Float> {
        if (bitmap == null || bitmap.isRecycled) {
            return List(32) { 0.05f }
        }
        val bins = IntArray(32)
        val stepX = (bitmap.width / 32).coerceAtLeast(1)
        val stepY = (bitmap.height / 32).coerceAtLeast(1)
        var x = 0
        while (x < bitmap.width) {
            var y = 0
            while (y < bitmap.height) {
                val pixel = bitmap.getPixel(x, y)
                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)
                val luma = ((r * 299 + g * 587 + b * 114) / 1000).coerceIn(0, 255)
                val bin = (luma * 31) / 255
                bins[bin]++
                y += stepY
            }
            x += stepX
        }
        val maxVal = (bins.maxOrNull() ?: 1).coerceAtLeast(1).toFloat()
        return bins.map { (it / maxVal).coerceIn(0.05f, 1f) }
    }

    /**
     * Generates a neutral optical color-calibration swatch card for filter thumbnail previews
     * only when no live camera frame or captured photo is available yet.
     */
    fun createNeutralCalibrationBitmap(width: Int = 320, height: Int = 240): Bitmap {
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        val w = width.toFloat()
        val h = height.toFloat()

        // Smooth natural sky-to-warm-earth gradient for accurate LUT/filter previewing
        p.shader = LinearGradient(
            0f, 0f, 0f, h,
            intArrayOf(
                Color.rgb(86, 134, 178),
                Color.rgb(218, 176, 142),
                Color.rgb(64, 108, 82),
                Color.rgb(28, 42, 36)
            ),
            floatArrayOf(0f, 0.45f, 0.75f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, w, h, p)
        p.shader = null

        // Color reference bars along bottom edge (Red, Amber, Green, Cyan, Blue, Skin, White)
        val swatches = intArrayOf(
            Color.rgb(204, 68, 58),
            Color.rgb(228, 168, 64),
            Color.rgb(78, 152, 98),
            Color.rgb(68, 156, 178),
            Color.rgb(216, 164, 138),
            Color.rgb(244, 240, 232)
        )
        val swW = w / swatches.size
        swatches.forEachIndexed { i, col ->
            p.color = col
            canvas.drawRect(i * swW, h * 0.82f, (i + 1) * swW, h, p)
        }
        return bmp
    }

    fun createCuratedSceneBitmap(sceneIndex: Int, width: Int = 640, height: Int = 480): Bitmap {
        return createNeutralCalibrationBitmap(width, height)
    }
}
