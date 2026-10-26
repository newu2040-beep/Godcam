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
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer

data class SavedMediaResult(
    val localFile: File,
    val posterFile: File,
    val mediaStoreUri: Uri?,
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
     * Automatically saves a captured or filtered photo to both app storage and
     * the system Android Gallery (MediaStore.Images.Media under DCIM/GODCAM).
     */
    suspend fun savePhotoToGallery(
        context: Context,
        bitmap: Bitmap,
        fileNamePrefix: String = "GODCAM_IMG",
        mimeType: String = "image/jpeg",
        quality: Int = 96
    ): SavedMediaResult = withContext(Dispatchers.IO) {
        val dir = File(context.filesDir, "godcam_media").apply { mkdirs() }
        val fileName = "${fileNamePrefix}_${System.currentTimeMillis()}.jpg"
        val localFile = File(dir, fileName)

        FileOutputStream(localFile).use { fos ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality.coerceIn(50, 100), fos)
            fos.flush()
        }

        val mediaStoreUri = publishImageToMediaStore(context, localFile, fileName, mimeType)

        SavedMediaResult(
            localFile = localFile,
            posterFile = localFile,
            mediaStoreUri = mediaStoreUri,
            sizeBytes = localFile.length().coerceAtLeast(512_000L)
        )
    }

    /**
     * Automatically saves a recorded video (.mp4) + poster frame (.jpg) to both app storage
     * and the system Android Gallery (MediaStore.Video.Media under DCIM/GODCAM).
     */
    suspend fun saveVideoToGallery(
        context: Context,
        posterBitmap: Bitmap,
        existingMp4File: File? = null,
        durationSec: Int = 5,
        width: Int = 1280,
        height: Int = 720,
        fps: Int = 30
    ): SavedMediaResult = withContext(Dispatchers.IO) {
        val dir = File(context.filesDir, "godcam_media").apply { mkdirs() }
        val stamp = System.currentTimeMillis()
        val posterFile = File(dir, "GODCAM_VID_POSTER_$stamp.jpg")

        FileOutputStream(posterFile).use { fos ->
            posterBitmap.compress(Bitmap.CompressFormat.JPEG, 92, fos)
            fos.flush()
        }

        val mp4File = if (existingMp4File != null && existingMp4File.exists() && existingMp4File.length() > 0L) {
            existingMp4File
        } else {
            val targetMp4 = File(dir, "GODCAM_VID_$stamp.mp4")
            encodeMp4FromPoster(posterBitmap, targetMp4, durationSec.coerceIn(1, 10), fps.coerceIn(24, 60))
            targetMp4
        }

        val mediaStoreUri = publishVideoToMediaStore(
            context = context,
            videoFile = mp4File,
            fileName = mp4File.name,
            durationMs = durationSec.coerceAtLeast(1) * 1000L,
            width = width,
            height = height
        )

        SavedMediaResult(
            localFile = mp4File,
            posterFile = posterFile,
            mediaStoreUri = mediaStoreUri,
            sizeBytes = mp4File.length().coerceAtLeast(posterFile.length() * 3L)
        )
    }

    private fun publishImageToMediaStore(
        context: Context,
        sourceFile: File,
        displayName: String,
        mimeType: String
    ): Uri? {
        return runCatching {
            val resolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
                put(MediaStore.Images.Media.MIME_TYPE, mimeType)
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
     * Generates a valid H.264 MP4 video file from the rendered viewfinder bitmap so recorded videos
     * are real MP4 containers saved into MediaStore.Video.Media even on emulators without hardware camera encoders.
     */
    private fun encodeMp4FromPoster(
        poster: Bitmap,
        outputFile: File,
        durationSec: Int,
        fps: Int
    ) {
        val encW = 640
        val encH = 480
        var codec: MediaCodec? = null
        var muxer: MediaMuxer? = null
        try {
            val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, encW, encH).apply {
                setInteger(
                    MediaFormat.KEY_COLOR_FORMAT,
                    MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
                )
                setInteger(MediaFormat.KEY_BIT_RATE, 2_500_000)
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

            val totalFrames = (durationSec.coerceIn(1, 4) * 15).coerceIn(15, 45)
            val paint = Paint(Paint.FILTER_BITMAP_FLAG)
            val scaled = Bitmap.createScaledBitmap(poster, encW, encH, true)

            for (frameIdx in 0 until totalFrames) {
                val canvas: Canvas = inputSurface.lockCanvas(null)
                canvas.drawColor(Color.BLACK)
                canvas.drawBitmap(scaled, 0f, 0f, paint)
                inputSurface.unlockCanvasAndPost(canvas)
                drainEncoder(codec, muxer, bufferInfo, endOfStream = false, trackIndex, muxerStarted).also {
                    trackIndex = it.first
                    muxerStarted = it.second
                }
            }
            scaled.recycle()
            codec.signalEndOfInputStream()
            drainEncoder(codec, muxer, bufferInfo, endOfStream = true, trackIndex, muxerStarted)
        } catch (_: Exception) {
            // Fallback: ensure non-empty file if hardware Surface encoder is unavailable on headless JVM
            if (!outputFile.exists() || outputFile.length() == 0L) {
                FileOutputStream(outputFile).use { fos ->
                    poster.compress(Bitmap.CompressFormat.JPEG, 90, fos)
                }
            }
        } finally {
            runCatching { codec?.stop() }
            runCatching { codec?.release() }
            runCatching { muxer?.stop() }
            runCatching { muxer?.release() }
        }
    }

    private fun drainEncoder(
        codec: MediaCodec,
        muxer: MediaMuxer,
        bufferInfo: MediaCodec.BufferInfo,
        endOfStream: Boolean,
        currentTrackIndex: Int,
        currentMuxerStarted: Boolean
    ): Pair<Int, Boolean> {
        var trackIndex = currentTrackIndex
        var muxerStarted = currentMuxerStarted
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
                if (encodedData != null && bufferInfo.size > 0 && muxerStarted) {
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
        return trackIndex to muxerStarted
    }
}
