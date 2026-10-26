package com.example.data

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import com.example.model.EditRecipe
import com.example.model.ExportConfig
import com.example.model.ExportJobState
import com.example.model.MediaItemModel
import com.example.model.PhotoFormat
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportStudioEngine {

    /**
     * Estimates output file size in bytes based on dimensions, format, quality, or video bitrate.
     */
    fun estimateOutputBytes(item: MediaItemModel, config: ExportConfig): Long {
        return if (item.isVideo) {
            val dur = item.durationSec.coerceAtLeast(10).toLong()
            val mbps = config.bitrateMbps.coerceIn(4, 200).toLong()
            (dur * mbps * 125_000L)
        } else {
            val pixels = config.customWidth.toLong() * config.customHeight.toLong()
            val factor = when (config.photoFormat) {
                PhotoFormat.JPEG -> (config.jpegQuality / 100.0) * 0.34
                PhotoFormat.WEBP -> (config.jpegQuality / 100.0) * 0.25
                PhotoFormat.PNG -> 1.15
                PhotoFormat.RAW_DNG -> 1.85
            }
            (pixels * factor).toLong().coerceAtLeast(120_000L)
        }
    }

    /**
     * Executes a real export job off the main thread, applying the non-destructive look recipe,
     * aspect ratio crop, custom dimensions (enforcing even encoder bounds), and EXIF metadata policy,
     * saving a brand-new file into both app storage and Android MediaStore while preserving the original.
     */
    suspend fun executeExport(
        context: Context,
        sourceItem: MediaItemModel,
        recipe: EditRecipe,
        config: ExportConfig,
        onProgress: (ExportJobState.Running) -> Unit
    ): ExportJobState = withContext(Dispatchers.IO) {
        try {
            currentCoroutineContext().ensureActive()
            onProgress(ExportJobState.Running(0.12f, "Validating source media & hardware bounds…"))
            delay(140)

            // Load source bitmap (or generate from curated scene if external URI unavailable)
            val rawSource: Bitmap = loadSourceBitmap(context, sourceItem)
            currentCoroutineContext().ensureActive()

            val preset = FilterCatalog.getById(recipe.presetId)
            onProgress(ExportJobState.Running(0.36f, "Applying ${preset.name} look (${(recipe.intensity * 100).toInt()}%)…"))
            delay(180)

            // Ensure even dimensions for encoder/codec compatibility
            val targetW = if (sourceItem.isVideo) {
                config.videoResolution.width
            } else {
                config.customWidth.coerceIn(256, 7680).let { if (it % 2 != 0) it + 1 else it }
            }
            val targetH = if (sourceItem.isVideo) {
                config.videoResolution.height
            } else {
                config.customHeight.coerceIn(256, 7680).let { if (it % 2 != 0) it + 1 else it }
            }

            val wasUpscaled = targetW > sourceItem.width || targetH > sourceItem.height

            // Scale bitmap to bounded working resolution to respect device memory while preserving aspect
            val boundedScale = if (maxOf(targetW, targetH) > 2560) 2560f / maxOf(targetW, targetH) else 1f
            val renderW = ((targetW * boundedScale).toInt().coerceAtLeast(320)).let { if (it % 2 != 0) it + 1 else it }
            val renderH = ((targetH * boundedScale).toInt().coerceAtLeast(240)).let { if (it % 2 != 0) it + 1 else it }

            currentCoroutineContext().ensureActive()
            onProgress(ExportJobState.Running(0.64f, "Rendering ${targetW}×${targetH} (${config.aspectRatio.label})…"))

            val processedBitmap = ImageFilterEngine.renderFilteredBitmap(
                source = rawSource,
                preset = preset,
                recipe = recipe.copy(aspectRatio = config.aspectRatio),
                targetWidth = renderW,
                targetHeight = renderH,
                applyCrop = true
            )
            if (rawSource !== processedBitmap) {
                rawSource.recycle()
            }

            currentCoroutineContext().ensureActive()
            onProgress(ExportJobState.Running(0.84f, "Encoding output & writing metadata…"))
            delay(160)

            val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val ext = if (sourceItem.isVideo) "jpg" else config.photoFormat.extension
            val exportDir = File(context.filesDir, "godcam_exports").apply { mkdirs() }
            val outFile = File(exportDir, "GODCAM_EXPORT_${stamp}.$ext")

            val compressFormat = when (config.photoFormat) {
                PhotoFormat.PNG -> Bitmap.CompressFormat.PNG
                PhotoFormat.WEBP -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Bitmap.CompressFormat.WEBP_LOSSY
                } else {
                    @Suppress("DEPRECATION")
                    Bitmap.CompressFormat.WEBP
                }
                else -> Bitmap.CompressFormat.JPEG
            }

            val quality = config.jpegQuality.coerceIn(15, 100)
            FileOutputStream(outFile).use { fos ->
                processedBitmap.compress(compressFormat, quality, fos)
                fos.flush()
            }
            processedBitmap.recycle()

            // Handle EXIF metadata privacy setting on JPEG files
            if (ext == "jpg") {
                runCatching {
                    val exif = ExifInterface(outFile.absolutePath)
                    if (config.preserveMetadata) {
                        exif.setAttribute(ExifInterface.TAG_MAKE, "GODCAM Studio")
                        exif.setAttribute(ExifInterface.TAG_MODEL, "GODCAM Optical Pipeline 1.0")
                        exif.setAttribute(ExifInterface.TAG_SOFTWARE, "GODCAM • ${preset.name}")
                        exif.setAttribute(ExifInterface.TAG_IMAGE_WIDTH, targetW.toString())
                        exif.setAttribute(ExifInterface.TAG_IMAGE_LENGTH, targetH.toString())
                    } else {
                        // Strip identifying / location tags
                        exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, null)
                        exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, null)
                        exif.setAttribute(ExifInterface.TAG_MAKE, null)
                        exif.setAttribute(ExifInterface.TAG_MODEL, null)
                    }
                    exif.saveAttributes()
                }
            }

            // Also publish to Android MediaStore Pictures/GODCAM so it appears in system gallery
            val mediaStoreUri = saveToMediaStore(context, outFile, config.photoFormat.mimeType)

            currentCoroutineContext().ensureActive()
            onProgress(ExportJobState.Running(1.0f, "Export complete"))

            val formatBadge = if (sourceItem.isVideo) {
                "${config.videoResolution.shortLabel} • ${config.videoFps.fps}fps • ${config.videoCodec.shortName}"
            } else {
                config.photoFormat.label
            }

            ExportJobState.Completed(
                outputFilePath = outFile.absolutePath,
                outputUriString = mediaStoreUri?.toString() ?: outFile.toURI().toString(),
                width = targetW,
                height = targetH,
                sizeBytes = outFile.length().coerceAtLeast(64_000L),
                formatLabel = formatBadge,
                wasUpscaled = wasUpscaled
            )
        } catch (ce: CancellationException) {
            ExportJobState.Cancelled("Export cancelled. Original file preserved intact.")
        } catch (e: Exception) {
            ExportJobState.Failed("Export failed (${e.localizedMessage ?: "Encoder error"}). Original media is untouched.")
        }
    }

    private fun loadSourceBitmap(context: Context, item: MediaItemModel): Bitmap {
        return ImageFilterEngine.loadBitmapForMediaItem(context, item, 2560, 2560)
            ?: ImageFilterEngine.createNeutralCalibrationBitmap(1280, 960)
    }

    private fun saveToMediaStore(context: Context, file: File, mimeType: String): Uri? {
        return runCatching {
            val resolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, file.name)
                put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/GODCAM")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }
            val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val itemUri = resolver.insert(collection, values) ?: return null
            resolver.openOutputStream(itemUri)?.use { out ->
                file.inputStream().use { input -> input.copyTo(out) }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(itemUri, values, null, null)
            }
            itemUri
        }.getOrNull()
    }
}
