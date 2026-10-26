package com.example.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrixColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.compose.ui.graphics.ColorMatrix
import com.example.model.AspectRatioOption
import com.example.model.EditRecipe
import com.example.model.FilterPreset
import com.example.model.MediaItemModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.random.Random

object ImageFilterEngine {

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
     * Builds a 4x5 Jetpack Compose [ColorMatrix] from a [FilterPreset] and [EditRecipe]
     * scaled by [EditRecipe.intensity] (0f..1f).
     */
    fun buildComposeColorMatrix(
        preset: FilterPreset,
        recipe: EditRecipe = EditRecipe(presetId = preset.id),
        extraEv: Float = 0f
    ): ColorMatrix {
        return ColorMatrix(buildFloatMatrix(preset, recipe, extraEv))
    }

    /**
     * Builds a native [android.graphics.ColorMatrix] for GPU RenderEffect and Canvas rendering.
     */
    fun buildAndroidColorMatrix(
        preset: FilterPreset,
        recipe: EditRecipe = EditRecipe(presetId = preset.id),
        extraEv: Float = 0f
    ): android.graphics.ColorMatrix {
        return android.graphics.ColorMatrix(buildFloatMatrix(preset, recipe, extraEv))
    }

    /**
     * Builds a 20-float array for Android's [android.graphics.ColorMatrix] and Compose's [ColorMatrix].
     */
    fun buildFloatMatrix(
        preset: FilterPreset,
        recipe: EditRecipe = EditRecipe(presetId = preset.id),
        extraEv: Float = 0f
    ): FloatArray {
        val t = if (preset.id == "original") 0f else recipe.intensity.coerceIn(0f, 1f)
        val combinedMatrix = android.graphics.ColorMatrix()

        // 1. Saturation
        val sat = lerp(1f, preset.saturation, t).coerceIn(0f, 2.2f)
        val satMatrix = android.graphics.ColorMatrix().apply {
            setSaturation(sat)
        }
        combinedMatrix.postConcat(satMatrix)

        // 2. RGB Channel Gain + Warmth + Tint + Contrast + Fade + EV
        val rGain = lerp(1f, preset.rScale, t) + (recipe.warmthOffset * 0.22f)
        val gGain = lerp(1f, preset.gScale, t) - (preset.tint * t * 0.12f)
        val bGain = lerp(1f, preset.bScale, t) - (recipe.warmthOffset * 0.22f)

        val contrast = (lerp(1f, preset.contrast, t) + recipe.contrastOffset).coerceIn(0.55f, 1.85f)
        val warmth = (preset.warmth * t) + recipe.warmthOffset
        val fade = ((preset.fade * t) + recipe.fadeOffset).coerceIn(0f, 0.45f)
        val evOffset = extraEv * 22f

        val cScale = contrast * (1f - fade * 0.32f)
        val cOffset = 128f * (1f - cScale) + (fade * 46f) + evOffset

        val warmR = warmth * 24f
        val warmG = warmth * 6f
        val warmB = -warmth * 26f

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
     * Bakes the ColorMatrix, optical vignette, and analog film grain directly into the output pixels.
     */
    suspend fun renderFilteredBitmap(
        source: Bitmap,
        preset: FilterPreset,
        recipe: EditRecipe,
        targetWidth: Int? = null,
        targetHeight: Int? = null,
        applyCrop: Boolean = false,
        extraEv: Float = 0f
    ): Bitmap = withContext(Dispatchers.Default) {
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
        paint.colorFilter = ColorMatrixColorFilter(buildAndroidColorMatrix(preset, recipe, extraEv))

        // Always center-crop source rect to match finalW:finalH so output never stretches
        val srcRatio = cropped.width.toFloat() / cropped.height.coerceAtLeast(1).toFloat()
        val dstRatio = finalW.toFloat() / finalH.coerceAtLeast(1).toFloat()
        val srcRect = if (abs(srcRatio - dstRatio) > 0.015f) {
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
            null
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

        // Apply organic film grain if active
        val totalGrain = ((preset.grain * t) + recipe.grainOffset).coerceIn(0f, 0.6f)
        if (totalGrain > 0.02f) {
            drawOrganicGrain(canvas, finalW, finalH, totalGrain)
        }

        if (cropped !== source) {
            cropped.recycle()
        }
        output
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
        val grainPaint = Paint()
        val rng = Random(42)
        val dotCount = ((width * height) / 130).coerceIn(600, 10000)
        val maxAlpha = (grainStrength * 82f).roundToInt().coerceIn(8, 72)
        for (i in 0 until dotCount) {
            val gx = rng.nextFloat() * width
            val gy = rng.nextFloat() * height
            val isLight = rng.nextBoolean()
            val alpha = rng.nextInt(4, maxAlpha + 1)
            grainPaint.color = if (isLight) {
                Color.argb(alpha, 250, 246, 238)
            } else {
                Color.argb(alpha, 18, 24, 22)
            }
            val size = if (width > 1000) 2.2f else 1.4f
            canvas.drawRect(gx, gy, gx + size, gy + size, grainPaint)
        }
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
