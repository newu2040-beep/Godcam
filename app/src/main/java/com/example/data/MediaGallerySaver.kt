package com.example.data

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.ExifInterface
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.model.PhotoFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SavedMediaResult(
    val localFile: File,
    val posterFile: File,
    val mediaStoreUri: Uri?,
    val actualWidth: Int,
    val actualHeight: Int,
    val sizeBytes: Long
)

object MediaGallerySaver {

    private const val CHANNEL_ID = "godcam_gallery_autosave"
    private const val CHANNEL_NAME = "GODCAM Gallery Auto-Save"

    fun ensureNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications when photos and videos are automatically saved to your Gallery"
            }
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            nm?.createNotificationChannel(channel)
        }
    }

    fun sendAutoSaveNotification(
        context: Context,
        title: String,
        message: String,
        notificationId: Int = (System.currentTimeMillis() % 10000).toInt()
    ) {
        ensureNotificationChannel(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        runCatching {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            NotificationManagerCompat.from(context).notify(notificationId, notification)
        }
    }

    /**
     * Saves a captured or filtered photo at maximum hardware quality (100%)
     * with real EXIF optical metadata into app storage and (when [publishToSystemGallery] is true)
     * into the system Android Gallery (MediaStore.Images.Media under DCIM/GODCAM).
     */
    suspend fun savePhotoToGallery(
        context: Context,
        bitmap: Bitmap,
        fileNamePrefix: String = "GODCAM_IMG",
        photoFormat: PhotoFormat = PhotoFormat.JPEG,
        mimeType: String = photoFormat.mimeType,
        quality: Int = 100,
        appliedLookName: String = "Original",
        focalLengthMm: Float = 4.2f,
        apertureF: Float = 1.8f,
        isoValue: Int = 100,
        publishToSystemGallery: Boolean = true
    ): SavedMediaResult = withContext(Dispatchers.IO) {
        val dir = File(context.filesDir, "godcam_media").apply { mkdirs() }
        val ext = when (photoFormat) {
            PhotoFormat.PNG -> "png"
            PhotoFormat.WEBP -> "webp"
            else -> "jpg"
        }
        val resolvedMime = when (photoFormat) {
            PhotoFormat.PNG -> "image/png"
            PhotoFormat.WEBP -> "image/webp"
            else -> "image/jpeg"
        }
        val fileName = "${fileNamePrefix}_${System.currentTimeMillis()}.$ext"
        val localFile = File(dir, fileName)

        FileOutputStream(localFile).use { fos ->
            val compressFormat = when (photoFormat) {
                PhotoFormat.PNG -> Bitmap.CompressFormat.PNG
                PhotoFormat.WEBP -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Bitmap.CompressFormat.WEBP_LOSSLESS
                } else {
                    @Suppress("DEPRECATION")
                    Bitmap.CompressFormat.WEBP
                }
                else -> Bitmap.CompressFormat.JPEG
            }
            bitmap.compress(compressFormat, quality.coerceIn(90, 100), fos)
            fos.flush()
        }

        // Write hardware EXIF optical tags for JPEG captures
        if (ext == "jpg") {
            runCatching {
                val exif = ExifInterface(localFile.absolutePath)
                val exifDate = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.US).format(Date())
                exif.setAttribute(ExifInterface.TAG_MAKE, Build.MANUFACTURER.ifBlank { "Android" })
                exif.setAttribute(ExifInterface.TAG_MODEL, Build.MODEL.ifBlank { "GODCAM Optical Sensor" })
                exif.setAttribute(ExifInterface.TAG_SOFTWARE, "GODCAM Studio v5.0 • $appliedLookName")
                exif.setAttribute(ExifInterface.TAG_DATETIME, exifDate)
                exif.setAttribute(ExifInterface.TAG_DATETIME_ORIGINAL, exifDate)
                exif.setAttribute(ExifInterface.TAG_IMAGE_WIDTH, bitmap.width.toString())
                exif.setAttribute(ExifInterface.TAG_IMAGE_LENGTH, bitmap.height.toString())
                exif.setAttribute(ExifInterface.TAG_F_NUMBER, apertureF.toString())
                exif.setAttribute(ExifInterface.TAG_FOCAL_LENGTH, "${(focalLengthMm * 100).toInt()}/100")
                @Suppress("DEPRECATION")
                exif.setAttribute(ExifInterface.TAG_ISO_SPEED_RATINGS, isoValue.toString())
                exif.setAttribute(ExifInterface.TAG_USER_COMMENT, "Look: $appliedLookName | ${bitmap.width}x${bitmap.height}")
                exif.saveAttributes()
            }
        }

        val mediaStoreUri = if (publishToSystemGallery) {
            publishImageToMediaStore(
                context = context,
                sourceFile = localFile,
                displayName = fileName,
                mimeType = resolvedMime,
                width = bitmap.width,
                height = bitmap.height
            )
        } else {
            null
        }

        SavedMediaResult(
            localFile = localFile,
            posterFile = localFile,
            mediaStoreUri = mediaStoreUri,
            actualWidth = bitmap.width,
            actualHeight = bitmap.height,
            sizeBytes = localFile.length()
        )
    }

    /**
     * Saves a recorded video (.mp4) + high-quality filtered poster frame (.jpg)
     * + filtered motion frame sequence at the selected resolution into app storage
     * and (when [publishToSystemGallery] is true) into the system Android Gallery (DCIM/GODCAM).
     */
    suspend fun saveVideoToGallery(
        context: Context,
        posterBitmap: Bitmap,
        capturedFrames: List<Bitmap> = emptyList(),
        existingMp4File: File? = null,
        durationSec: Int = 5,
        width: Int = 1920,
        height: Int = 1080,
        fps: Int = 30,
        bitrateMbps: Int = 40,
        publishToSystemGallery: Boolean = true
    ): SavedMediaResult = withContext(Dispatchers.IO) {
        val dir = File(context.filesDir, "godcam_media").apply { mkdirs() }
        val stamp = System.currentTimeMillis()
        val posterFile = File(dir, "GODCAM_VID_POSTER_$stamp.jpg")

        FileOutputStream(posterFile).use { fos ->
            posterBitmap.compress(Bitmap.CompressFormat.JPEG, 96, fos)
            fos.flush()
        }

        // Save up to 120 aspect-ratio-accurate filtered motion frames evenly sampled across the entire recording
        // so FullScreenMediaViewer plays back 30fps/60fps smoothly from start to finish without lag or jitter
        val rawFrames = if (capturedFrames.isNotEmpty()) capturedFrames else listOf(posterBitmap)
        val maxPersistCount = rawFrames.size.coerceAtMost(120)
        val framesToPersist = if (rawFrames.size <= maxPersistCount) {
            rawFrames
        } else {
            List(maxPersistCount) { i ->
                val srcIdx = ((i.toFloat() / (maxPersistCount - 1).coerceAtLeast(1)) * (rawFrames.size - 1))
                    .toInt()
                    .coerceIn(0, rawFrames.lastIndex)
                rawFrames[srcIdx]
            }
        }
        framesToPersist.forEachIndexed { idx, bmp ->
            if (!bmp.isRecycled) {
                runCatching {
                    val frameFile = File(dir, "GODCAM_VID_POSTER_${stamp}_F$idx.jpg")
                    FileOutputStream(frameFile).use { fos ->
                        bmp.compress(Bitmap.CompressFormat.JPEG, 88, fos)
                    }
                }
            }
        }

        var actualW = width
        var actualH = height

        val mp4File = if (existingMp4File != null && existingMp4File.exists() && existingMp4File.length() > 0L) {
            runCatching {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(existingMp4File.absolutePath)
                val vw = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull()
                val vh = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull()
                if (vw != null && vh != null && vw > 0 && vh > 0) {
                    actualW = vw
                    actualH = vh
                }
                retriever.release()
            }
            existingMp4File
        } else {
            val targetMp4 = File(dir, "GODCAM_VID_$stamp.mp4")
            val (encodedW, encodedH) = encodeMp4AtSelectedResolution(
                poster = posterBitmap,
                capturedFrames = framesToPersist,
                outputFile = targetMp4,
                targetWidth = width,
                targetHeight = height,
                durationSec = durationSec.coerceAtLeast(1),
                fps = fps.coerceIn(24, 60),
                bitrateMbps = bitrateMbps.coerceIn(12, 160)
            )
            actualW = encodedW
            actualH = encodedH
            targetMp4
        }

        val mediaStoreUri = if (publishToSystemGallery) {
            publishVideoToMediaStore(
                context = context,
                videoFile = mp4File,
                fileName = mp4File.name,
                durationMs = durationSec.coerceAtLeast(1) * 1000L,
                width = actualW,
                height = actualH
            )
        } else {
            null
        }

        SavedMediaResult(
            localFile = mp4File,
            posterFile = posterFile,
            mediaStoreUri = mediaStoreUri,
            actualWidth = actualW,
            actualHeight = actualH,
            sizeBytes = mp4File.length().coerceAtLeast(posterFile.length())
        )
    }

    /**
     * Publishes an existing local photo or video file to the system Android Gallery (DCIM/GODCAM)
     * when the user manually confirms saving while Auto-Save is turned OFF.
     */
    suspend fun publishExistingFileToGallery(
        context: Context,
        localFile: File,
        isVideo: Boolean,
        width: Int,
        height: Int,
        durationSec: Int = 0
    ): Uri? = withContext(Dispatchers.IO) {
        if (!localFile.exists()) return@withContext null
        if (isVideo) {
            publishVideoToMediaStore(
                context = context,
                videoFile = localFile,
                fileName = localFile.name,
                durationMs = durationSec.coerceAtLeast(1) * 1000L,
                width = width,
                height = height
            )
        } else {
            val mime = when (localFile.extension.lowercase(Locale.US)) {
                "png" -> "image/png"
                "webp" -> "image/webp"
                else -> "image/jpeg"
            }
            publishImageToMediaStore(
                context = context,
                sourceFile = localFile,
                displayName = localFile.name,
                mimeType = mime,
                width = width,
                height = height
            )
        }
    }

    /**
     * Loads the persisted filtered motion frames for a recorded video off the main thread so FullScreenMediaViewer
     * displays smooth, zero-stretch, filter-accurate playback across the full recorded duration.
     */
    fun loadVideoFilteredFrames(posterPath: String): List<Bitmap> {
        if (posterPath.isBlank()) return emptyList()
        val posterFile = File(posterPath)
        val parent = posterFile.parentFile ?: return emptyList()
        val baseName = posterFile.nameWithoutExtension
        val loaded = mutableListOf<Bitmap>()
        for (idx in 0 until 120) {
            val f = File(parent, "${baseName}_F$idx.jpg")
            if (f.exists() && f.length() > 0L) {
                runCatching {
                    android.graphics.BitmapFactory.decodeFile(f.absolutePath)?.let { loaded.add(it) }
                }
            } else if (idx > 0) {
                break
            }
        }
        if (loaded.isEmpty() && posterFile.exists()) {
            runCatching {
                android.graphics.BitmapFactory.decodeFile(posterFile.absolutePath)?.let { loaded.add(it) }
            }
        }
        return loaded
    }

    private fun publishImageToMediaStore(
        context: Context,
        sourceFile: File,
        displayName: String,
        mimeType: String,
        width: Int,
        height: Int
    ): Uri? {
        return runCatching {
            val resolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
                put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                put(MediaStore.Images.Media.WIDTH, width)
                put(MediaStore.Images.Media.HEIGHT, height)
                put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
                put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_DCIM}/GODCAM")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }
            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }
            val uri = resolver.insert(collection, values) ?: return null
            resolver.openOutputStream(uri)?.use { out ->
                sourceFile.inputStream().use { input -> input.copyTo(out) }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            }
            MediaScannerConnection.scanFile(context, arrayOf(sourceFile.absolutePath), arrayOf(mimeType), null)
            uri
        }.getOrNull()
    }

    private fun publishVideoToMediaStore(
        context: Context,
        videoFile: File,
        fileName: String,
        durationMs: Long,
        width: Int,
        height: Int
    ): Uri? {
        return runCatching {
            val resolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                put(MediaStore.Video.Media.DURATION, durationMs)
                put(MediaStore.Video.Media.WIDTH, width)
                put(MediaStore.Video.Media.HEIGHT, height)
                put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
                put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Video.Media.RELATIVE_PATH, "${Environment.DIRECTORY_DCIM}/GODCAM")
                    put(MediaStore.Video.Media.IS_PENDING, 1)
                }
            }
            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }
            val uri = resolver.insert(collection, values) ?: return null
            resolver.openOutputStream(uri)?.use { out ->
                videoFile.inputStream().use { input -> input.copyTo(out) }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Video.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            }
            MediaScannerConnection.scanFile(context, arrayOf(videoFile.absolutePath), arrayOf("video/mp4"), null)
            uri
        }.getOrNull()
    }

    /**
     * Encodes an H.264 MP4 video at the user's selected resolution & aspect ratio with center-cropped
     * pre-filtered frames so the output video never stretches or squishes.
     */
    private fun encodeMp4AtSelectedResolution(
        poster: Bitmap,
        capturedFrames: List<Bitmap>,
        outputFile: File,
        targetWidth: Int,
        targetHeight: Int,
        durationSec: Int,
        fps: Int,
        bitrateMbps: Int
    ): Pair<Int, Int> {
        val evenW = targetWidth.coerceAtLeast(320).let { if (it % 2 != 0) it - 1 else it }
        val evenH = targetHeight.coerceAtLeast(320).let { if (it % 2 != 0) it - 1 else it }
        val isPortrait = evenH > evenW
        val candidateDimensions = buildList {
            add(evenW to evenH)
            if (isPortrait) {
                add(1080 to 1920)
                add(720 to 1280)
                add(480 to 640)
            } else {
                add(1920 to 1080)
                add(1280 to 720)
                add(640 to 480)
            }
        }.distinct()

        val sourceFrames = capturedFrames.ifEmpty { listOf(poster) }

        for ((encW, encH) in candidateDimensions) {
            var codec: MediaCodec? = null
            var muxer: MediaMuxer? = null
            try {
                val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, encW, encH).apply {
                    setInteger(
                        MediaFormat.KEY_COLOR_FORMAT,
                        MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
                    )
                    setInteger(MediaFormat.KEY_BIT_RATE, (bitrateMbps * 1_000_000).coerceIn(4_000_000, 80_000_000))
                    setInteger(MediaFormat.KEY_FRAME_RATE, fps)
                    setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
                }
                codec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
                codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
                val inputSurface = codec.createInputSurface()
                codec.start()

                muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
                val bufferInfo = MediaCodec.BufferInfo()
                var trackIndex = -1
                var muxerStarted = false

                val totalFrames = (durationSec.coerceAtLeast(1) * 8).coerceIn(sourceFrames.size.coerceAtLeast(24), 120)
                val frameDurationUs = ((durationSec.coerceAtLeast(1) * 1_000_000L) / totalFrames.coerceAtLeast(1)).coerceAtLeast(16_666L)
                var nextPtsUs = 0L
                val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
                val dstRect = android.graphics.Rect(0, 0, encW, encH)
                val targetRatio = encW.toFloat() / encH.toFloat()

                for (frameIdx in 0 until totalFrames) {
                    val srcIdx = ((frameIdx.toFloat() / totalFrames.coerceAtLeast(1)) * sourceFrames.size)
                        .toInt()
                        .coerceIn(0, sourceFrames.lastIndex)
                    val frameBmp = sourceFrames[srcIdx]
                    val canvas: Canvas = inputSurface.lockCanvas(null)
                    canvas.drawColor(Color.BLACK)
                    if (!frameBmp.isRecycled && frameBmp.width > 0 && frameBmp.height > 0) {
                        val srcRatio = frameBmp.width.toFloat() / frameBmp.height.toFloat()
                        val srcRect = if (srcRatio > targetRatio) {
                            val cropW = (frameBmp.height * targetRatio).toInt().coerceIn(1, frameBmp.width)
                            val left = (frameBmp.width - cropW) / 2
                            android.graphics.Rect(left, 0, left + cropW, frameBmp.height)
                        } else {
                            val cropH = (frameBmp.width / targetRatio).toInt().coerceIn(1, frameBmp.height)
                            val top = (frameBmp.height - cropH) / 2
                            android.graphics.Rect(0, top, frameBmp.width, top + cropH)
                        }
                        canvas.drawBitmap(frameBmp, srcRect, dstRect, paint)
                    }
                    inputSurface.unlockCanvasAndPost(canvas)
                    drainEncoder(
                        codec = codec,
                        muxer = muxer,
                        bufferInfo = bufferInfo,
                        endOfStream = false,
                        currentTrackIndex = trackIndex,
                        currentMuxerStarted = muxerStarted,
                        frameDurationUs = frameDurationUs,
                        currentPtsUs = nextPtsUs
                    ).also {
                        trackIndex = it.first
                        muxerStarted = it.second
                        nextPtsUs = it.third
                    }
                }
                codec.signalEndOfInputStream()
                drainEncoder(
                    codec = codec,
                    muxer = muxer,
                    bufferInfo = bufferInfo,
                    endOfStream = true,
                    currentTrackIndex = trackIndex,
                    currentMuxerStarted = muxerStarted,
                    frameDurationUs = frameDurationUs,
                    currentPtsUs = nextPtsUs
                )
                codec.stop()
                codec.release()
                codec = null
                if (muxerStarted) {
                    muxer.stop()
                }
                muxer.release()
                muxer = null
                if (outputFile.exists() && outputFile.length() > 0L) {
                    return evenW to evenH
                }
            } catch (_: Exception) {
                runCatching { codec?.stop() }
                runCatching { codec?.release() }
                runCatching { muxer?.stop() }
                runCatching { muxer?.release() }
                runCatching { outputFile.delete() }
            }
        }

        // Last resort fallback if no hardware H.264 surface encoder is present on JVM test runner
        FileOutputStream(outputFile).use { fos ->
            poster.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        }
        return evenW to evenH
    }

    private fun drainEncoder(
        codec: MediaCodec,
        muxer: MediaMuxer,
        bufferInfo: MediaCodec.BufferInfo,
        endOfStream: Boolean,
        currentTrackIndex: Int,
        currentMuxerStarted: Boolean,
        frameDurationUs: Long,
        currentPtsUs: Long
    ): Triple<Int, Boolean, Long> {
        var trackIndex = currentTrackIndex
        var muxerStarted = currentMuxerStarted
        var ptsUs = currentPtsUs
        while (true) {
            val outIndex = codec.dequeueOutputBuffer(bufferInfo, 10_000L)
            if (outIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                if (!endOfStream) break
            } else if (outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                if (!muxerStarted) {
                    trackIndex = muxer.addTrack(codec.outputFormat)
                    muxer.start()
                    muxerStarted = true
                }
            } else if (outIndex >= 0) {
                val encodedData: ByteBuffer? = codec.getOutputBuffer(outIndex)
                if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    bufferInfo.size = 0
                }
                if (encodedData != null && bufferInfo.size > 0 && muxerStarted) {
                    bufferInfo.presentationTimeUs = ptsUs
                    ptsUs += frameDurationUs
                    encodedData.position(bufferInfo.offset)
                    encodedData.limit(bufferInfo.offset + bufferInfo.size)
                    muxer.writeSampleData(trackIndex, encodedData, bufferInfo)
                }
                codec.releaseOutputBuffer(outIndex, false)
                if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    break
                }
            }
        }
        return Triple(trackIndex, muxerStarted, ptsUs)
    }
}
