package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.model.AppThemeOption
import com.example.model.AspectRatioOption
import com.example.model.AudioRecordingFormat
import com.example.model.CompactLayoutMode
import com.example.model.GridMode
import com.example.model.MediaItemModel
import com.example.model.PhotoFormat
import com.example.model.PhotoMegapixelOption
import com.example.model.ShutterSoundStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File

private val Context.dataStore by preferencesDataStore(name = "godcam_preferences")

@Entity(tableName = "media_items")
data class MediaItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
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
) {
    fun toDomain() = MediaItemModel(
        id = id,
        title = title,
        filePath = filePath,
        uriString = uriString,
        isVideo = isVideo,
        timestampMs = timestampMs,
        width = width,
        height = height,
        durationSec = durationSec,
        formatLabel = formatLabel,
        sizeBytes = sizeBytes,
        appliedPresetId = appliedPresetId,
        appliedIntensity = appliedIntensity,
        isFavorite = isFavorite,
        cameraInfo = cameraInfo,
        sampleSceneIndex = sampleSceneIndex
    )
}

@Entity(tableName = "favorite_presets")
data class FavoritePresetEntity(
    @PrimaryKey val presetId: String,
    val addedAtMs: Long = System.currentTimeMillis()
)

@Dao
interface GodcamDao {
    @Query("SELECT * FROM media_items ORDER BY timestampMs DESC")
    fun observeAllMedia(): Flow<List<MediaItemEntity>>

    @Query("SELECT * FROM media_items ORDER BY timestampMs DESC")
    suspend fun getAllMediaOnce(): List<MediaItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(item: MediaItemEntity): Long

    @Query("UPDATE media_items SET isFavorite = :favorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, favorite: Boolean)

    @Query("UPDATE media_items SET appliedPresetId = :presetId, appliedIntensity = :intensity WHERE id = :id")
    suspend fun updateMediaLook(id: Long, presetId: String, intensity: Float)

    @Query("DELETE FROM media_items WHERE id = :id")
    suspend fun deleteMediaById(id: Long)

    @Query("DELETE FROM media_items WHERE filePath LIKE '%godcam_seed_%'")
    suspend fun purgeLegacySeededItems()

    @Query("SELECT * FROM favorite_presets")
    fun observeFavoritePresets(): Flow<List<FavoritePresetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavoritePreset(entity: FavoritePresetEntity)

    @Query("DELETE FROM favorite_presets WHERE presetId = :presetId")
    suspend fun removeFavoritePreset(presetId: String)
}

@Database(
    entities = [MediaItemEntity::class, FavoritePresetEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun godcamDao(): GodcamDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "godcam_studio.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
        }
    }
}

data class UserPreferencesState(
    val theme: AppThemeOption = AppThemeOption.CREAM_MINT,
    val compactLayoutMode: CompactLayoutMode = CompactLayoutMode.AUTO,
    val defaultGrid: GridMode = GridMode.THREE_BY_THREE,
    val defaultRatio: AspectRatioOption = AspectRatioOption.RATIO_4_3,
    val defaultPhotoFormat: PhotoFormat = PhotoFormat.JPEG,
    val defaultMegapixel: PhotoMegapixelOption = PhotoMegapixelOption.MP_50,
    val shutterSoundStyle: ShutterSoundStyle = ShutterSoundStyle.FUJI_MECHANICAL,
    val audioRecordingFormat: AudioRecordingFormat = AudioRecordingFormat.AAC_LC_STEREO_48K,
    val autoSaveToGallery: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val preserveLocationMetadata: Boolean = false
)

class GodcamRepository(
    private val context: Context,
    private val dao: GodcamDao
) {
    private object Keys {
        val THEME = stringPreferencesKey("theme_id")
        val COMPACT_MODE = stringPreferencesKey("compact_layout_mode")
        val GRID = stringPreferencesKey("grid_mode")
        val RATIO = stringPreferencesKey("default_ratio")
        val FORMAT = stringPreferencesKey("default_photo_format")
        val MEGAPIXEL = stringPreferencesKey("default_megapixel")
        val SHUTTER_SOUND = stringPreferencesKey("shutter_sound_style")
        val AUDIO_FORMAT = stringPreferencesKey("audio_recording_format")
        val AUTO_SAVE = booleanPreferencesKey("auto_save_to_gallery")
        val HAPTICS = booleanPreferencesKey("haptics_enabled")
        val LOCATION_META = booleanPreferencesKey("preserve_location_meta")
    }

    val mediaItems: Flow<List<MediaItemModel>> = dao.observeAllMedia().map { list ->
        list.map { it.toDomain() }
    }

    val favoritePresetIds: Flow<Set<String>> = dao.observeFavoritePresets().map { list ->
        list.map { it.presetId }.toSet()
    }

    val preferencesFlow: Flow<UserPreferencesState> = context.dataStore.data.map { prefs ->
        val themeId = prefs[Keys.THEME] ?: AppThemeOption.CREAM_MINT.id
        val theme = AppThemeOption.entries.firstOrNull { it.id == themeId } ?: AppThemeOption.CREAM_MINT
        val compactName = prefs[Keys.COMPACT_MODE] ?: CompactLayoutMode.AUTO.name
        val compactMode = CompactLayoutMode.entries.firstOrNull { it.name == compactName } ?: CompactLayoutMode.AUTO
        val gridName = prefs[Keys.GRID] ?: GridMode.THREE_BY_THREE.name
        val grid = GridMode.entries.firstOrNull { it.name == gridName } ?: GridMode.THREE_BY_THREE
        val ratioName = prefs[Keys.RATIO] ?: AspectRatioOption.RATIO_4_3.name
        val ratio = AspectRatioOption.entries.firstOrNull { it.name == ratioName } ?: AspectRatioOption.RATIO_4_3
        val formatName = prefs[Keys.FORMAT] ?: PhotoFormat.JPEG.name
        val format = PhotoFormat.entries.firstOrNull { it.name == formatName } ?: PhotoFormat.JPEG
        val mpName = prefs[Keys.MEGAPIXEL] ?: PhotoMegapixelOption.MP_50.name
        val megapixel = PhotoMegapixelOption.entries.firstOrNull { it.name == mpName } ?: PhotoMegapixelOption.MP_50
        val shutterName = prefs[Keys.SHUTTER_SOUND] ?: ShutterSoundStyle.FUJI_MECHANICAL.name
        val shutterStyle = ShutterSoundStyle.entries.firstOrNull { it.name == shutterName } ?: ShutterSoundStyle.FUJI_MECHANICAL
        val audioName = prefs[Keys.AUDIO_FORMAT] ?: AudioRecordingFormat.AAC_LC_STEREO_48K.name
        val audioFmt = AudioRecordingFormat.entries.firstOrNull { it.name == audioName } ?: AudioRecordingFormat.AAC_LC_STEREO_48K
        UserPreferencesState(
            theme = theme,
            compactLayoutMode = compactMode,
            defaultGrid = grid,
            defaultRatio = ratio,
            defaultPhotoFormat = format,
            defaultMegapixel = megapixel,
            shutterSoundStyle = shutterStyle,
            audioRecordingFormat = audioFmt,
            autoSaveToGallery = prefs[Keys.AUTO_SAVE] ?: true,
            hapticsEnabled = prefs[Keys.HAPTICS] ?: true,
            preserveLocationMetadata = prefs[Keys.LOCATION_META] ?: false
        )
    }

    suspend fun setTheme(theme: AppThemeOption) {
        context.dataStore.edit { it[Keys.THEME] = theme.id }
    }

    suspend fun setCompactLayoutMode(mode: CompactLayoutMode) {
        context.dataStore.edit { it[Keys.COMPACT_MODE] = mode.name }
    }

    suspend fun setGridMode(grid: GridMode) {
        context.dataStore.edit { it[Keys.GRID] = grid.name }
    }

    suspend fun setDefaultRatio(ratio: AspectRatioOption) {
        context.dataStore.edit { it[Keys.RATIO] = ratio.name }
    }

    suspend fun setDefaultPhotoFormat(format: PhotoFormat) {
        context.dataStore.edit { it[Keys.FORMAT] = format.name }
    }

    suspend fun setDefaultMegapixel(megapixel: PhotoMegapixelOption) {
        context.dataStore.edit { it[Keys.MEGAPIXEL] = megapixel.name }
    }

    suspend fun setShutterSoundStyle(style: ShutterSoundStyle) {
        context.dataStore.edit { it[Keys.SHUTTER_SOUND] = style.name }
    }

    suspend fun setAudioRecordingFormat(format: AudioRecordingFormat) {
        context.dataStore.edit { it[Keys.AUDIO_FORMAT] = format.name }
    }

    suspend fun setAutoSaveToGallery(enabled: Boolean) {
        context.dataStore.edit { it[Keys.AUTO_SAVE] = enabled }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.HAPTICS] = enabled }
    }

    suspend fun setPreserveLocation(enabled: Boolean) {
        context.dataStore.edit { it[Keys.LOCATION_META] = enabled }
    }

    suspend fun toggleFavoritePreset(presetId: String, currentlyFavorite: Boolean) {
        if (currentlyFavorite) {
            dao.removeFavoritePreset(presetId)
        } else {
            dao.addFavoritePreset(FavoritePresetEntity(presetId))
        }
    }

    suspend fun toggleMediaFavorite(id: Long, currentlyFavorite: Boolean) {
        dao.updateFavorite(id, !currentlyFavorite)
    }

    suspend fun updateMediaLook(id: Long, presetId: String, intensity: Float) {
        dao.updateMediaLook(id, presetId, intensity)
    }

    suspend fun deleteMediaItem(item: MediaItemModel) = withContext(Dispatchers.IO) {
        dao.deleteMediaById(item.id)
        if (item.filePath.isNotBlank()) {
            runCatching { File(item.filePath).delete() }
        }
    }

    suspend fun insertCapturedOrExportedMedia(
        title: String,
        filePath: String,
        uriString: String,
        isVideo: Boolean,
        width: Int,
        height: Int,
        durationSec: Int = 0,
        formatLabel: String = "JPEG",
        sizeBytes: Long,
        appliedPresetId: String = "original",
        appliedIntensity: Float = 1.0f,
        cameraInfo: String = "",
        sampleSceneIndex: Int = 0
    ): Long {
        return dao.insertMedia(
            MediaItemEntity(
                title = title,
                filePath = filePath,
                uriString = uriString,
                isVideo = isVideo,
                timestampMs = System.currentTimeMillis(),
                width = width,
                height = height,
                durationSec = durationSec,
                formatLabel = formatLabel,
                sizeBytes = sizeBytes,
                appliedPresetId = appliedPresetId,
                appliedIntensity = appliedIntensity,
                isFavorite = false,
                cameraInfo = cameraInfo,
                sampleSceneIndex = sampleSceneIndex
            )
        )
    }

    /**
     * Cleans up any legacy demo/seeded files and verifies that all items in Room DB
     * correspond to real existing files on disk.
     */
    suspend fun cleanAndVerifyRealLibrary() = withContext(Dispatchers.IO) {
        dao.purgeLegacySeededItems()
        val mediaDir = File(context.filesDir, "godcam_media")
        mediaDir.listFiles()?.forEach { file ->
            if (file.name.startsWith("godcam_seed_")) {
                runCatching { file.delete() }
            }
        }
        val existing = dao.getAllMediaOnce()
        for (entity in existing) {
            if (entity.filePath.isNotBlank() && !File(entity.filePath).exists() && !entity.uriString.startsWith("content://")) {
                dao.deleteMediaById(entity.id)
            }
        }
    }
}
