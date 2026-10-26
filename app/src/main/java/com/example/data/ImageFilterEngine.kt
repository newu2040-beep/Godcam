package com.example.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrixColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
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
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

object ImageFilterEngine {

    /**
     * Loads the actual saved photo/video poster bitmap from [MediaItemModel.filePath] or [MediaItemModel.uriString]
     * if available, falling back to the procedural scene bitmap.
     */
    fun loadBitmapForMediaItem(
        context: Context,
        item: MediaItemModel?,
        targetWidth: Int = 640,
        targetHeight: Int = 640
    ): Bitmap {
        if (item == null) {
            return createCuratedSceneBitmap(4, targetWidth, targetHeight)
        }
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
        if (item.uriString.startsWith("content://")) {
            runCatching {
                val uri = Uri.parse(item.uriString)
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)?.let { return it }
                }
            }
        }
        return createCuratedSceneBitmap(item.sampleSceneIndex, targetWidth, targetHeight)
    }

    /**
     * Builds a 4x5 Jetpack Compose [ColorMatrix] from a [FilterPreset] and [EditRecipe]
     * scaled by [EditRecipe.intensity] (0f..1f).
     */
    fun buildComposeColorMatrix(
        preset: FilterPreset,
        recipe: EditRecipe = EditRecipe(presetId = preset.id)
    ): ColorMatrix {
        val raw = buildFloatMatrix(preset, recipe)
        return ColorMatrix(raw)
    }

    /**
     * Builds a 20-float array for Android's [android.graphics.ColorMatrix] and Compose's [ColorMatrix].
     */
    fun buildFloatMatrix(
        preset: FilterPreset,
        recipe: EditRecipe = EditRecipe(presetId = preset.id)
    ): FloatArray {
        val t = recipe.intensity.coerceIn(0f, 1f)
        if (t <= 0.001f &&
            abs(recipe.warmthOffset) < 0.001f &&
            abs(recipe.contrastOffset) < 0.001f &&
            abs(recipe.fadeOffset) < 0.001f
        ) {
            return floatArrayOf(
                1f, 0f, 0f, 0f, 0f,
                0f, 1f, 0f, 0f, 0f,
                0f, 0f, 1f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
        }

        val rGain = lerp(1f, preset.rScale, t) + (recipe.warmthOffset * 0.18f)
        val gGain = lerp(1f, preset.gScale, t) - (recipe.PresetTint(preset, t) * 0.10f)
        val bGain = lerp(1f, preset.bScale, t) - (recipe.warmthOffset * 0.18f)

        val sat = lerp(1f, preset.saturation, t).coerceIn(0f, 2.0f)
        val contrast = (lerp(1f, preset.contrast, t) + recipe.contrastOffset).coerceIn(0.5f, 1.8f)
        val warmth = (preset.warmth * t) + recipe.warmthOffset
        val fade = ((preset.fade * t) + recipe.fadeOffset).coerceIn(0f, 0.45f)

        // Luminance weights for saturation matrix
        val lumR = 0.299f
        val lumG = 0.587f
        val lumB = 0.114f

        val sr = (1f - sat) * lumR
        val sg = (1f - sat) * lumG
        val sb = (1f - sat) * lumB

        val cScale = contrast * (1f - fade * 0.35f)
        val cOffset = 128f * (1f - cScale) + (fade * 42f)

        val warmR = warmth * 18f
        val warmG = warmth * 4f
        val warmB = -warmth * 20f

        val m00 = (sr + sat) * rGain * cScale
        val m01 = sg * rGain * cScale
        val m02 = sb * rGain * cScale
        val m04 = cOffset + warmR

        val m10 = sr * gGain * cScale
        val m11 = (sg + sat) * gGain * cScale
        val m12 = sb * gGain * cScale
        val m14 = cOffset + warmG

        val m20 = sr * bGain * cScale
        val m21 = sg * bGain * cScale
        val m22 = (sb + sat) * bGain * cScale
        val m24 = cOffset + warmB

        return floatArrayOf(
            m00, m01, m02, 0f, m04,
            m10, m11, m12, 0f, m14,
            m20, m21, m22, 0f, m24,
            0f, 0f, 0f, 1f, 0f
        )
    }

    private fun EditRecipe.PresetTint(preset: FilterPreset, t: Float): Float = preset.tint * t

    private fun lerp(start: Float, stop: Float, fraction: Float): Float {
        return start + (stop - start) * fraction
    }

    /**
     * Renders a filtered, cropped, and optionally resized [Bitmap] off the main thread.
     */
    suspend fun renderFilteredBitmap(
        source: Bitmap,
        preset: FilterPreset,
        recipe: EditRecipe,
        targetWidth: Int? = null,
        targetHeight: Int? = null,
        applyCrop: Boolean = false
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
        val matrixValues = buildFloatMatrix(preset, recipe)
        paint.colorFilter = ColorMatrixColorFilter(android.graphics.ColorMatrix(matrixValues))

        val dstRect = RectF(0f, 0f, finalW.toFloat(), finalH.toFloat())
        canvas.drawBitmap(cropped, null, dstRect, paint)

        // Apply optical vignette if active
        val t = recipe.intensity.coerceIn(0f, 1f)
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
                        Color.argb((totalVignette * 90).roundToInt().coerceIn(0, 200), 12, 18, 15),
                        Color.argb((totalVignette * 210).roundToInt().coerceIn(0, 235), 8, 12, 10)
                    ),
                    floatArrayOf(0.45f, 0.80f, 1.0f),
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
        val dotCount = ((width * height) / 140).coerceIn(600, 9000)
        val maxAlpha = (grainStrength * 78f).roundToInt().coerceIn(8, 68)
        for (i in 0 until dotCount) {
            val gx = rng.nextFloat() * width
            val gy = rng.nextFloat() * height
            val isLight = rng.nextBoolean()
            val alpha = rng.nextInt(4, maxAlpha + 1)
            grainPaint.color = if (isLight) {
                Color.argb(alpha, 250, 246, 238)
            } else {
                Color.argb(alpha, 20, 26, 24)
            }
            val size = if (width > 1000) 2.2f else 1.3f
            canvas.drawRect(gx, gy, gx + size, gy + size, grainPaint)
        }
    }

    /**
     * Computes a 32-bin normalized luminance histogram from a bitmap or returns a responsive sample curve.
     */
    fun computeLumaHistogram(bitmap: Bitmap?): List<Float> {
        if (bitmap == null || bitmap.isRecycled) {
            return List(32) { i ->
                val x = (i - 15f) / 8f
                (0.25f + 0.65f * kotlin.math.exp(-x * x * 0.5f)).coerceIn(0.08f, 1f)
            }
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
     * Generates rich, realistic, artistic sample photography bitmaps matching the 9 scenes
     * shown in the GODCAM reference design (Alpine Lake Island, Wild Daisy 4K, Sunset Horizon,
     * Historic Spire, Golden Hour Portrait, Misty Forest Path, Ginger Cat, Dusk Skyline, Emerald Cove).
     */
    fun createCuratedSceneBitmap(sceneIndex: Int, width: Int = 960, height: Int = 720): Bitmap {
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        val w = width.toFloat()
        val h = height.toFloat()

        when (sceneIndex % 9) {
            0 -> {
                // Scene 0: Alpine Lake & Island Church at Golden Dawn (Hero Filter Preview & Splash Scene)
                p.shader = LinearGradient(
                    0f, 0f, 0f, h * 0.56f,
                    intArrayOf(
                        Color.rgb(112, 148, 182),
                        Color.rgb(218, 178, 146),
                        Color.rgb(246, 204, 152)
                    ),
                    floatArrayOf(0f, 0.58f, 1f),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, w, h * 0.58f, p)

                // Sun glow
                p.shader = RadialGradient(
                    w * 0.72f, h * 0.34f, w * 0.35f,
                    intArrayOf(Color.argb(190, 255, 236, 190), Color.TRANSPARENT),
                    null, Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, w, h * 0.58f, p)

                // Distant Dolomite/Julian Alps mountain peaks
                p.shader = null
                p.color = Color.rgb(74, 98, 124)
                val farMountains = Path().apply {
                    moveTo(0f, h * 0.55f)
                    lineTo(w * 0.14f, h * 0.33f)
                    lineTo(w * 0.28f, h * 0.43f)
                    lineTo(w * 0.45f, h * 0.27f)
                    lineTo(w * 0.62f, h * 0.41f)
                    lineTo(w * 0.80f, h * 0.29f)
                    lineTo(w, h * 0.45f)
                    lineTo(w, h * 0.56f)
                    close()
                }
                canvas.drawPath(farMountains, p)

                // Mid-ground sunlit ridges
                p.color = Color.rgb(54, 76, 94)
                val midMountains = Path().apply {
                    moveTo(0f, h * 0.56f)
                    lineTo(w * 0.22f, h * 0.42f)
                    lineTo(w * 0.49f, h * 0.50f)
                    lineTo(w * 0.74f, h * 0.39f)
                    lineTo(w, h * 0.52f)
                    lineTo(w, h * 0.57f)
                    close()
                }
                canvas.drawPath(midMountains, p)

                // Mirror Alpine Lake water
                p.shader = LinearGradient(
                    0f, h * 0.55f, 0f, h,
                    intArrayOf(
                        Color.rgb(66, 108, 134),
                        Color.rgb(42, 82, 108),
                        Color.rgb(26, 54, 72)
                    ),
                    floatArrayOf(0f, 0.5f, 1f),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, h * 0.55f, w, h, p)

                // Golden water reflection shimmer
                p.shader = RadialGradient(
                    w * 0.55f, h * 0.68f, w * 0.32f,
                    intArrayOf(Color.argb(120, 244, 192, 134), Color.TRANSPARENT),
                    null, Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, h * 0.55f, w, h, p)

                // Lake Bled style island + spire in the center
                p.shader = null
                p.color = Color.rgb(38, 62, 48)
                canvas.drawOval(RectF(w * 0.34f, h * 0.52f, w * 0.66f, h * 0.61f), p)
                // White island chapel & warm terracotta roof
                p.color = Color.rgb(242, 235, 222)
                canvas.drawRect(w * 0.46f, h * 0.47f, w * 0.54f, h * 0.55f, p)
                canvas.drawRect(w * 0.485f, h * 0.38f, w * 0.508f, h * 0.55f, p)
                p.color = Color.rgb(176, 86, 62)
                val spire = Path().apply {
                    moveTo(w * 0.48f, h * 0.38f)
                    lineTo(w * 0.496f, h * 0.31f)
                    lineTo(w * 0.513f, h * 0.38f)
                    close()
                }
                canvas.drawPath(spire, p)

                // Island reflection in calm water
                p.color = Color.argb(95, 32, 48, 42)
                canvas.drawOval(RectF(w * 0.36f, h * 0.59f, w * 0.64f, h * 0.69f), p)
            }

            1 -> {
                // Scene 1: Wild Daisy Macro in Deep Botanical Meadow (4K badge item)
                p.shader = RadialGradient(
                    w * 0.5f, h * 0.5f, w * 0.7f,
                    intArrayOf(Color.rgb(54, 82, 56), Color.rgb(24, 42, 28)),
                    null, Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, w, h, p)
                p.shader = null

                // Soft bokeh circles in background
                p.color = Color.argb(45, 168, 206, 152)
                canvas.drawCircle(w * 0.22f, h * 0.26f, w * 0.11f, p)
                canvas.drawCircle(w * 0.78f, h * 0.72f, w * 0.14f, p)
                canvas.drawCircle(w * 0.80f, h * 0.24f, w * 0.08f, p)

                // Daisy petals
                val cx = w * 0.5f
                val cy = h * 0.5f
                val petalLen = min(w, h) * 0.28f
                val petalW = min(w, h) * 0.065f
                p.color = Color.rgb(248, 246, 238)
                for (i in 0 until 18) {
                    canvas.save()
                    canvas.rotate(i * 20f, cx, cy)
                    canvas.drawOval(
                        RectF(cx - petalW, cy - petalLen, cx + petalW, cy - petalW * 0.4f),
                        p
                    )
                    canvas.restore()
                }
                // Warm golden pollen center
                p.color = Color.rgb(226, 168, 48)
                canvas.drawCircle(cx, cy, min(w, h) * 0.095f, p)
                p.color = Color.rgb(188, 124, 24)
                canvas.drawCircle(cx, cy, min(w, h) * 0.065f, p)
            }

            2 -> {
                // Scene 2: Golden Ocean Sunset Horizon
                p.shader = LinearGradient(
                    0f, 0f, 0f, h * 0.58f,
                    intArrayOf(
                        Color.rgb(92, 118, 146),
                        Color.rgb(212, 146, 104),
                        Color.rgb(248, 188, 112)
                    ),
                    floatArrayOf(0f, 0.55f, 1f),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, w, h * 0.58f, p)
                p.shader = null
                p.color = Color.rgb(255, 234, 176)
                canvas.drawCircle(w * 0.52f, h * 0.49f, min(w, h) * 0.07f, p)

                p.shader = LinearGradient(
                    0f, h * 0.56f, 0f, h,
                    intArrayOf(Color.rgb(174, 122, 92), Color.rgb(48, 72, 92), Color.rgb(24, 38, 52)),
                    floatArrayOf(0f, 0.45f, 1f),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, h * 0.56f, w, h, p)
                p.shader = null
                p.color = Color.argb(170, 255, 218, 144)
                for (i in 0 until 8) {
                    val y = h * (0.59f + i * 0.042f)
                    val rw = w * (0.14f - i * 0.012f).coerceAtLeast(0.03f)
                    canvas.drawRoundRect(
                        RectF(w * 0.52f - rw, y, w * 0.52f + rw, y + 6f),
                        4f, 4f, p
                    )
                }
            }

            3 -> {
                // Scene 3: Historic Old Town Spires at Golden Hour (0:32 Video item)
                p.shader = LinearGradient(
                    0f, 0f, 0f, h,
                    intArrayOf(Color.rgb(154, 178, 198), Color.rgb(238, 188, 142), Color.rgb(88, 64, 52)),
                    floatArrayOf(0f, 0.6f, 1f),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, w, h, p)
                p.shader = null
                p.color = Color.rgb(62, 46, 38)
                canvas.drawRect(0f, h * 0.62f, w, h, p)
                // Gothic towers silhouette
                canvas.drawRect(w * 0.24f, h * 0.36f, w * 0.38f, h * 0.68f, p)
                canvas.drawRect(w * 0.52f, h * 0.28f, w * 0.66f, h * 0.68f, p)
                val spire1 = Path().apply {
                    moveTo(w * 0.23f, h * 0.36f)
                    lineTo(w * 0.31f, h * 0.16f)
                    lineTo(w * 0.39f, h * 0.36f)
                    close()
                }
                val spire2 = Path().apply {
                    moveTo(w * 0.51f, h * 0.28f)
                    lineTo(w * 0.59f, h * 0.10f)
                    lineTo(w * 0.67f, h * 0.28f)
                    close()
                }
                canvas.drawPath(spire1, p)
                canvas.drawPath(spire2, p)
            }

            4 -> {
                // Scene 4: Golden Hour Portrait on Hilltop Overlooking City (Camera Viewfinder Hero)
                p.shader = LinearGradient(
                    0f, 0f, 0f, h * 0.65f,
                    intArrayOf(
                        Color.rgb(156, 176, 194),
                        Color.rgb(224, 192, 158),
                        Color.rgb(242, 206, 164)
                    ),
                    floatArrayOf(0f, 0.55f, 1f),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, w, h, p)

                // Distant warm hills & village bokeh
                p.shader = null
                p.color = Color.rgb(118, 108, 104)
                canvas.drawRect(0f, h * 0.56f, w, h, p)
                p.color = Color.argb(120, 246, 212, 168)
                for (i in 0..12) {
                    canvas.drawCircle(w * (0.08f * i), h * (0.60f + (i % 3) * 0.04f), w * 0.025f, p)
                }

                // Artistic stylized portrait silhouette & warm knit sweater
                // Cream knit shoulder
                p.color = Color.rgb(232, 218, 198)
                canvas.drawOval(RectF(w * 0.22f, h * 0.58f, w * 0.86f, h * 1.12f), p)
                // Warm skin neck & face profile
                p.color = Color.rgb(218, 166, 134)
                canvas.drawRect(w * 0.44f, h * 0.44f, w * 0.58f, h * 0.64f, p)
                canvas.drawOval(RectF(w * 0.36f, h * 0.22f, w * 0.68f, h * 0.54f), p)
                // Deep espresso hair bun & strands
                p.color = Color.rgb(38, 28, 24)
                canvas.drawCircle(w * 0.30f, h * 0.34f, w * 0.09f, p)
                canvas.drawArc(
                    RectF(w * 0.33f, h * 0.20f, w * 0.69f, h * 0.52f),
                    150f, 190f, true, p
                )
                // Subtle golden rim light
                p.style = Paint.Style.STROKE
                p.strokeWidth = 5f
                p.color = Color.argb(160, 255, 232, 186)
                canvas.drawArc(
                    RectF(w * 0.36f, h * 0.22f, w * 0.68f, h * 0.54f),
                    -60f, 120f, false, p
                )
                p.style = Paint.Style.FILL
            }

            5 -> {
                // Scene 5: Sunbeams Through Misty Redwood Forest Path
                p.shader = LinearGradient(
                    0f, 0f, 0f, h,
                    intArrayOf(Color.rgb(68, 96, 68), Color.rgb(38, 58, 40), Color.rgb(28, 38, 26)),
                    null, Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, w, h, p)
                p.shader = null
                // Tree trunks
                p.color = Color.rgb(32, 26, 22)
                val trunks = listOf(0.12f, 0.26f, 0.74f, 0.88f)
                for (tx in trunks) {
                    canvas.drawRect(w * (tx - 0.04f), 0f, w * (tx + 0.04f), h * 0.85f, p)
                }
                // Forest trail
                p.color = Color.rgb(112, 96, 76)
                val trail = Path().apply {
                    moveTo(w * 0.45f, h * 0.58f)
                    lineTo(w * 0.55f, h * 0.58f)
                    lineTo(w * 0.76f, h)
                    lineTo(w * 0.24f, h)
                    close()
                }
                canvas.drawPath(trail, p)
            }

            6 -> {
                // Scene 6: Ginger Tabby Cat in Warm Window Light
                p.shader = LinearGradient(
                    0f, 0f, 0f, h,
                    intArrayOf(Color.rgb(122, 114, 102), Color.rgb(78, 70, 62)),
                    null, Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, w, h, p)
                p.shader = null
                // Cat body & head
                p.color = Color.rgb(214, 148, 88)
                canvas.drawOval(RectF(w * 0.24f, h * 0.54f, w * 0.76f, h * 1.05f), p)
                canvas.drawCircle(w * 0.5f, h * 0.46f, min(w, h) * 0.22f, p)
                // Ears
                val leftEar = Path().apply {
                    moveTo(w * 0.30f, h * 0.38f)
                    lineTo(w * 0.33f, h * 0.18f)
                    lineTo(w * 0.45f, h * 0.28f)
                    close()
                }
                val rightEar = Path().apply {
                    moveTo(w * 0.70f, h * 0.38f)
                    lineTo(w * 0.67f, h * 0.18f)
                    lineTo(w * 0.55f, h * 0.28f)
                    close()
                }
                canvas.drawPath(leftEar, p)
                canvas.drawPath(rightEar, p)
                // White muzzle & chest
                p.color = Color.rgb(246, 240, 230)
                canvas.drawOval(RectF(w * 0.38f, h * 0.48f, w * 0.62f, h * 0.64f), p)
                // Emerald cat eyes
                p.color = Color.rgb(76, 134, 96)
                canvas.drawCircle(w * 0.41f, h * 0.43f, min(w, h) * 0.035f, p)
                canvas.drawCircle(w * 0.59f, h * 0.43f, min(w, h) * 0.035f, p)
            }

            7 -> {
                // Scene 7: Metropolis River Skyline at Twilight (Pro Video Hero Scene)
                p.shader = LinearGradient(
                    0f, 0f, 0f, h * 0.62f,
                    intArrayOf(
                        Color.rgb(24, 38, 68),
                        Color.rgb(128, 68, 86),
                        Color.rgb(232, 128, 78)
                    ),
                    floatArrayOf(0f, 0.52f, 1f),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, w, h * 0.62f, p)
                p.shader = LinearGradient(
                    0f, h * 0.60f, 0f, h,
                    intArrayOf(Color.rgb(58, 52, 74), Color.rgb(22, 32, 48)),
                    null, Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, h * 0.60f, w, h, p)
                p.shader = null

                // Skyscraper silhouettes & iconic spire
                p.color = Color.rgb(22, 26, 38)
                val bldgs = listOf(
                    Triple(0.06f, 0.42f, 0.08f),
                    Triple(0.16f, 0.36f, 0.09f),
                    Triple(0.28f, 0.45f, 0.07f),
                    Triple(0.46f, 0.38f, 0.10f),
                    Triple(0.60f, 0.32f, 0.08f),
                    Triple(0.72f, 0.18f, 0.06f), // Tallest tower
                    Triple(0.82f, 0.40f, 0.09f)
                )
                for ((bx, topY, bw) in bldgs) {
                    canvas.drawRect(w * bx, h * topY, w * (bx + bw), h * 0.62f, p)
                }
                // Tall tower antenna
                canvas.drawRect(w * 0.745f, h * 0.08f, w * 0.755f, h * 0.18f, p)

                // City window lights & river reflections
                p.color = Color.argb(190, 255, 210, 128)
                val rng = Random(77)
                for (i in 0 until 75) {
                    val lx = rng.nextFloat() * w
                    val ly = h * (0.42f + rng.nextFloat() * 0.19f)
                    canvas.drawCircle(lx, ly, 2.5f, p)
                }
            }

            else -> {
                // Scene 8: Coastal Emerald Cove & Turquoise Surf
                p.shader = LinearGradient(
                    0f, 0f, 0f, h * 0.45f,
                    intArrayOf(Color.rgb(122, 168, 204), Color.rgb(206, 224, 236)),
                    null, Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, w, h * 0.45f, p)
                p.shader = LinearGradient(
                    0f, h * 0.45f, 0f, h,
                    intArrayOf(Color.rgb(38, 124, 142), Color.rgb(64, 164, 162), Color.rgb(218, 202, 174)),
                    floatArrayOf(0f, 0.65f, 1f),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, h * 0.45f, w, h, p)
                p.shader = null
                p.color = Color.rgb(54, 78, 58)
                val cliff = Path().apply {
                    moveTo(w * 0.55f, h * 0.45f)
                    lineTo(w * 0.78f, h * 0.28f)
                    lineTo(w, h * 0.34f)
                    lineTo(w, h * 0.68f)
                    close()
                }
                canvas.drawPath(cliff, p)
            }
        }
        return bmp
    }
}
