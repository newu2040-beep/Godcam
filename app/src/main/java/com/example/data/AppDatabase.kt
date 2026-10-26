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
import com.example.model.GridMode
import com.example.model.MediaItemModel
import com.example.model.PhotoFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

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
    val sizeBytes: Long = 2_400_000L,
    val appliedPresetId: String = "original",
    val appliedIntensity: Float = 1.0f,
    val isFavorite: Boolean = false,
    val cameraInfo: String = "24mm f/1.8 • ISO 100 • 1/250s",
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

    @Query("SELECT COUNT(*) FROM media_items")
    suspend fun countMedia(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(item: MediaItemEntity): Long

    @Query("UPDATE media_items SET isFavorite = :favorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, favorite: Boolean)

    @Query("UPDATE media_items SET appliedPresetId = :presetId, appliedIntensity = :intensity WHERE id = :id")
    suspend fun updateMediaLook(id: Long, presetId: String, intensity: Float)

    @Query("DELETE FROM media_items WHERE id = :id")
    suspend fun deleteMediaById(id: Long)

    @Query("SELECT * FROM favorite_presets")
    fun observeFavoritePresets(): Flow<List<FavoritePresetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavoritePreset(entity: FavoritePresetEntity)

    @Query("DELETE FROM favorite_presets WHERE presetId = :presetId")
    suspend fun removeFavoritePreset(presetId: String)
}

@Database(
    entities = [MediaItemEntity::class, FavoritePresetEntity::class],
    version = 1,
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
    val defaultGrid: GridMode = GridMode.THREE_BY_THREE,
    val defaultRatio: AspectRatioOption = AspectRatioOption.RATIO_4_3,
    val defaultPhotoFormat: PhotoFormat = PhotoFormat.RAW_DNG,
    val hapticsEnabled: Boolean = true,
    val preserveLocationMetadata: Boolean = false
)

class GodcamRepository(
    private val context: Context,
    private val dao: GodcamDao
) {
    private object Keys {
        val THEME = stringPreferencesKey("theme_id")
        val GRID = stringPreferencesKey("grid_mode")
        val RATIO = stringPreferencesKey("default_ratio")
        val FORMAT = stringPreferencesKey("default_photo_format")
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
        val gridName = prefs[Keys.GRID] ?: GridMode.THREE_BY_THREE.name
        val grid = GridMode.entries.firstOrNull { it.name == gridName } ?: GridMode.THREE_BY_THREE
        val ratioName = prefs[Keys.RATIO] ?: AspectRatioOption.RATIO_4_3.name
        val ratio = AspectRatioOption.entries.firstOrNull { it.name == ratioName } ?: AspectRatioOption.RATIO_4_3
        val formatName = prefs[Keys.FORMAT] ?: PhotoFormat.RAW_DNG.name
        val format = PhotoFormat.entries.firstOrNull { it.name == formatName } ?: PhotoFormat.RAW_DNG
        UserPreferencesState(
            theme = theme,
            defaultGrid = grid,
            defaultRatio = ratio,
            defaultPhotoFormat = format,
            hapticsEnabled = prefs[Keys.HAPTICS] ?: true,
            preserveLocationMetadata = prefs[Keys.LOCATION_META] ?: false
        )
    }

    suspend fun setTheme(theme: AppThemeOption) {
        context.dataStore.edit { it[Keys.THEME] = theme.id }
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
        cameraInfo: String = "24mm f/1.8 • ISO 100 • 1/250s",
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
     * Ensures that on first launch, the built-in gallery has the 9 curated real files
     * shown in the GODCAM reference design so the user can immediately test Filters,
     * Gallery Viewer, Before/After comparison, and Export Studio offline.
     */
    suspend fun ensureSeededGalleryIfEmpty() = withContext(Dispatchers.IO) {
        if (dao.countMedia() > 0) return@withContext

        // Star top signature presets by default
        listOf("fuji_pro_400h", "kodak_gold_200", "kodak_portra_400", "cine_still_800t", "bw_tri_x_400")
            .forEach { dao.addFavoritePreset(FavoritePresetEntity(it)) }

        val dir = File(context.filesDir, "godcam_media").apply { mkdirs() }
        val now = System.currentTimeMillis()

        data class SeedSpec(
            val index: Int,
            val title: String,
            val isVideo: Boolean,
            val w: Int,
            val h: Int,
            val duration: Int,
            val format: String,
            val presetId: String,
            val fav: Boolean,
            val info: String
        )

        val seeds = listOf(
            SeedSpec(0, "Alpine Lake Island", false, 3840, 2880, 0, "RAW", "fuji_pro_400h", true, "24mm f/1.8 • ISO 64 • 1/320s"),
            SeedSpec(1, "Botanical Daisy 4K", true, 3840, 2160, 18, "4K", "fuji_velvia_50", true, "4K 60fps • 100Mbps • HEVC"),
            SeedSpec(2, "Pacific Golden Horizon", false, 4032, 3024, 0, "JPEG", "kodak_gold_200", false, "35mm f/2.0 • ISO 100 • 1/1000s"),
            SeedSpec(3, "Old Town Spires", true, 3840, 2160, 32, "MP4", "kodak_portra_400", false, "4K 30fps • 85Mbps • 1/60s"),
            SeedSpec(4, "Golden Hour Portrait", false, 3024, 4032, 0, "RAW", "port_golden_hour", true, "50mm f/1.4 • ISO 80 • 1/500s"),
            SeedSpec(5, "Misty Redwood Trail", false, 3840, 2880, 0, "JPEG", "nat_redwood_mist", false, "28mm f/2.2 • ISO 200 • 1/125s"),
            SeedSpec(6, "Studio Ginger Portrait", false, 3024, 3024, 0, "JPEG", "kodak_portra_160", true, "50mm f/1.8 • ISO 160 • 1/200s"),
            SeedSpec(7, "Harbor Skyline Dusk", true, 3840, 2160, 24, "4K", "cine_still_800t", true, "4K 60fps • 100Mbps • Pro Video"),
            SeedSpec(8, "Emerald Coastal Cove", false, 3840, 2880, 0, "JPEG", "nat_pacific_swell", false, "24mm f/2.8 • ISO 50 • 1/640s")
        )

        seeds.forEachIndexed { idx, spec ->
            val file = File(dir, "godcam_seed_${spec.index}.jpg")
            if (!file.exists()) {
                val bmp = ImageFilterEngine.createCuratedSceneBitmap(spec.index, 960, 720)
                FileOutputStream(file).use { out ->
                    bmp.compress(android.graphics.Bitmap.CompressFormat.JPEG, 92, out)
                }
                bmp.recycle()
            }
            dao.insertMedia(
                MediaItemEntity(
                    title = spec.title,
                    filePath = file.absolutePath,
                    uriString = file.toURI().toString(),
                    isVideo = spec.isVideo,
                    timestampMs = now - (idx * 3_600_000L),
                    width = spec.w,
                    height = spec.h,
                    durationSec = spec.duration,
                    formatLabel = spec.format,
                    sizeBytes = file.length().coerceAtLeast(1_850_000L + idx * 320_000L),
                    appliedPresetId = spec.presetId,
                    appliedIntensity = 1.0f,
                    isFavorite = spec.fav,
                    cameraInfo = spec.info,
                    sampleSceneIndex = spec.index
                )
            )
        }
    }
}
