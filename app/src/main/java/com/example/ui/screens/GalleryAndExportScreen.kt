package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.VolumeOff
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Compare
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.FilterBAndW
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ExportStudioEngine
import com.example.data.FilterCatalog
import com.example.data.ImageFilterEngine
import com.example.model.AspectRatioOption
import com.example.model.EditRecipe
import com.example.model.ExportJobState
import com.example.model.ExportQualityPreset
import com.example.model.MediaItemModel
import com.example.model.PhotoFormat
import com.example.model.VideoCodecOption
import com.example.model.VideoFps
import com.example.model.VideoResolution
import com.example.ui.AppDestination
import com.example.ui.CameraUiState
import com.example.ui.GalleryTab
import com.example.ui.GodcamViewModel
import com.example.ui.components.LabeledCameraSlider
import com.example.ui.components.SegmentedPillSelector
import com.example.ui.theme.GodcamAmberGold
import com.example.ui.theme.GodcamForestActive
import com.example.ui.theme.GodcamMintAccent
import com.example.ui.theme.JetBrainsMonoFamily
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun GalleryAndExportScreen(
    uiState: CameraUiState,
    mediaItems: List<MediaItemModel>,
    viewModel: GodcamViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        when {
            uiState.showExportStudioSheet -> viewModel.closeExportStudio()
            uiState.activeViewerMedia != null -> viewModel.openMediaViewer(null)
            uiState.multiSelectMode -> viewModel.toggleMultiSelectMode()
            else -> viewModel.navigateTo(AppDestination.CAMERA)
        }
    }

    val context = LocalContext.current
    var showSearchBar by remember { mutableStateOf(false) }

    // Zero-permission Android Photo Picker for importing external media (PRD Section 3 & 10)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.importExternalMediaUri(uri.toString())
        }
    }

    val filteredItems = remember(mediaItems, uiState.selectedGalleryTab, uiState.gallerySearchQuery) {
        mediaItems.filter { item ->
            val tabMatch = when (uiState.selectedGalleryTab) {
                GalleryTab.PHOTOS -> true // Show all in main grid so 4K & 0:32 tiles match Screen 5 reference
                GalleryTab.VIDEOS -> item.isVideo
                GalleryTab.FAVORITES -> item.isFavorite
            }
            val q = uiState.gallerySearchQuery.trim()
            val queryMatch = q.isEmpty() ||
                item.title.contains(q, ignoreCase = true) ||
                item.formatLabel.contains(q, ignoreCase = true) ||
                item.cameraInfo.contains(q, ignoreCase = true)
            tabMatch && queryMatch
        }
    }

    // If user opened Full-Screen Media Viewer
    val viewerItem = uiState.activeViewerMedia
    if (viewerItem != null && !uiState.showExportStudioSheet) {
        FullScreenMediaViewer(
            item = viewerItem,
            viewModel = viewModel,
            onBack = { viewModel.openMediaViewer(null) },
            onOpenExport = { viewModel.openExportStudio(viewerItem) },
            onEditLook = {
                viewModel.selectPreset(FilterCatalog.getById(viewerItem.appliedPresetId))
                viewModel.navigateTo(AppDestination.FILTERS)
            }
        )
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp)
                .align(Alignment.TopCenter)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // 1. TOP GALLERY HEADER (Matches Screen 5: "Gallery"   Search   Import   More)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.navigateTo(AppDestination.CAMERA) },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("gallery_back_to_camera")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back to Camera",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Gallery",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                            )
                        },
                        modifier = Modifier.testTag("gallery_import_picker_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AddPhotoAlternate,
                            contentDescription = "Import Photo via Photo Picker",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(
                        onClick = { showSearchBar = !showSearchBar },
                        modifier = Modifier.testTag("gallery_search_button")
                    ) {
                        Icon(
                            imageVector = if (showSearchBar) Icons.Outlined.Close else Icons.Outlined.Search,
                            contentDescription = "Search Gallery",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(
                        onClick = {
                            if (uiState.showExportStudioSheet) {
                                viewModel.closeExportStudio()
                            } else {
                                val target = filteredItems.firstOrNull() ?: mediaItems.firstOrNull()
                                if (target != null) viewModel.openExportStudio(target)
                            }
                        },
                        modifier = Modifier.testTag("gallery_export_sheet_toggle")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription = "Open Export Settings",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            AnimatedVisibility(visible = showSearchBar) {
                OutlinedTextField(
                    value = uiState.gallerySearchQuery,
                    onValueChange = { viewModel.setGallerySearchQuery(it) },
                    placeholder = { Text("Search by title, format (RAW, 4K), or lens…") },
                    singleLine = true,
                    shape = RoundedCornerShape(50),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. SEGMENTED PILL TABS (Matches Screen 5: "Photos" | "Videos" | "Favorites")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SegmentedPillSelector(
                    options = GalleryTab.entries,
                    selectedOption = uiState.selectedGalleryTab,
                    labelProvider = { it.label },
                    onOptionSelected = { viewModel.selectGalleryTab(it) },
                    modifier = Modifier.fillMaxWidth(),
                    testTagPrefix = "gallery_tab"
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. 3-COLUMN ADAPTIVE MEDIA GRID (Matches Screen 5 thumbnail layout, 4K & 0:32 badges)
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(
                    bottom = if (uiState.showExportStudioSheet) 340.dp else 24.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("gallery_media_grid")
            ) {
                items(filteredItems, key = { it.id }) { item ->
                    GalleryThumbnailTile(
                        item = item,
                        isSelected = item.id in uiState.selectedMediaIds,
                        multiSelectMode = uiState.multiSelectMode,
                        onClick = {
                            if (uiState.multiSelectMode) {
                                viewModel.toggleMediaSelection(item.id)
                            } else {
                                viewModel.openMediaViewer(item)
                            }
                        },
                        onOpenExport = { viewModel.openExportStudio(item) }
                    )
                }
            }
        }

        // 4. EXPORT STUDIO DOCKED SHEET (Matches bottom half of Screen 5 of reference image!)
        AnimatedVisibility(
            visible = uiState.showExportStudioSheet,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            ExportStudioCardSheet(
                uiState = uiState,
                viewModel = viewModel,
                onShareCompleted = { item -> viewModel.shareMediaItem(context, item) }
            )
        }
    }
}

@Composable
private fun GalleryThumbnailTile(
    item: MediaItemModel,
    isSelected: Boolean,
    multiSelectMode: Boolean,
    onClick: () -> Unit,
    onOpenExport: () -> Unit
) {
    val context = LocalContext.current
    val thumbBitmap = remember(item.id, item.filePath, item.sampleSceneIndex) {
        ImageFilterEngine.loadBitmapForMediaItem(context, item, 360, 360)
    }
    val preset = remember(item.appliedPresetId) {
        FilterCatalog.getById(item.appliedPresetId)
    }
    val matrix = remember(preset, item.appliedIntensity) {
        ImageFilterEngine.buildComposeColorMatrix(
            preset,
            EditRecipe(presetId = preset.id, intensity = item.appliedIntensity)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("gallery_tile_${item.id}")
    ) {
        Image(
            bitmap = thumbBitmap.asImageBitmap(),
            contentDescription = item.title,
            contentScale = ContentScale.Crop,
            colorFilter = ColorFilter.colorMatrix(matrix),
            modifier = Modifier.fillMaxSize()
        )

        // Top-right "4K" or "RAW" badge (Matches Screen 5 Daisy tile!)
        if (item.formatLabel == "4K" || item.formatLabel == "RAW") {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xD9FCFBF7),
                contentColor = Color(0xFF1A2823),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
            ) {
                Text(
                    text = item.formatLabel,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                )
            }
        }

        // Bottom video duration labels ("0:32") matching Screen 5 Old Town Spires tile
        if (item.isVideo && item.durationSec > 0) {
            val durText = String.format(Locale.US, "0:%02d", item.durationSec.coerceAtMost(59))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color(0x550E1613))
                    .padding(horizontal = 7.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = durText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 10.sp,
                        color = Color.White
                    )
                )
                Text(
                    text = durText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                )
            }
        } else {
            // Subtle bottom-right quick Export button on thumbnail
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0x7714201C))
                    .clickable(onClick = onOpenExport)
            ) {
                Icon(
                    imageVector = Icons.Outlined.IosShare,
                    contentDescription = "Export ${item.title}",
                    tint = Color.White,
                    modifier = Modifier.size(13.dp)
                )
            }
        }

        if (multiSelectMode && isSelected) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = "Selected",
                tint = GodcamMintAccent,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp)
                    .size(22.dp)
            )
        }
    }
}

/**
 * Export Settings Sheet matching the bottom half of Screen 5 of the reference mockup
 * AND expandable for full PRD Section 11 Export Studio controls.
 */
@Composable
private fun ExportStudioCardSheet(
    uiState: CameraUiState,
    viewModel: GodcamViewModel,
    onShareCompleted: (MediaItemModel) -> Unit
) {
    val target = uiState.exportTargetMedia
    val cfg = uiState.exportConfig
    val estimatedBytes = remember(target, cfg) {
        if (target != null) ExportStudioEngine.estimateOutputBytes(target, cfg) else 8_400_000L
    }
    val estimatedMb = String.format(Locale.US, "%.1f MB", estimatedBytes / (1024f * 1024f))

    var showAdvancedStudio by remember { mutableStateOf(false) }
    var widthInput by remember(cfg.customWidth) { mutableStateOf(cfg.customWidth.toString()) }
    var heightInput by remember(cfg.customHeight) { mutableStateOf(cfg.customHeight.toString()) }

    val isUpscaling = target != null && (cfg.customWidth > target.width || cfg.customHeight > target.height)

    Surface(
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 640.dp)
            .shadow(18.dp, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            )
            .testTag("export_settings_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 18.dp)
        ) {
            // Header: "Export Settings" and "×" close button (Exact match to Screen 5)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Export Settings",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    if (target != null) {
                        Text(
                            text = "${target.title} • Est. $estimatedMb",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
                IconButton(
                    onClick = { viewModel.closeExportStudio() },
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("export_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Close Export Settings",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4 Clean Interactive Summary Rows matching Screen 5 of Reference Image
            ExportSummaryRow(
                label = "Resolution",
                value = "${cfg.videoResolution.shortLabel} (${cfg.customWidth}×${cfg.customHeight})",
                onClick = {
                    val resList = VideoResolution.entries.filter { it != VideoResolution.UHD_8K || uiState.capabilities.supports8K }
                    val next = resList[(resList.indexOf(cfg.videoResolution) + 1) % resList.size]
                    viewModel.updateExportConfig {
                        it.copy(
                            videoResolution = next,
                            customWidth = next.width,
                            customHeight = next.height
                        )
                    }
                },
                testTag = "export_row_resolution"
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

            ExportSummaryRow(
                label = "Frame Rate",
                value = cfg.videoFps.label,
                onClick = {
                    val fpsList = uiState.capabilities.validFpsFor(cfg.videoResolution, cfg.videoCodec)
                    val next = fpsList[(fpsList.indexOf(cfg.videoFps) + 1) % fpsList.size]
                    viewModel.updateExportConfig { it.copy(videoFps = next) }
                },
                testTag = "export_row_fps"
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

            ExportSummaryRow(
                label = "Format",
                value = if (target?.isVideo == true) cfg.videoCodec.shortName else "${cfg.videoCodec.shortName} / ${cfg.photoFormat.label}",
                onClick = {
                    val nextPhoto = PhotoFormat.entries[(cfg.photoFormat.ordinal + 1) % PhotoFormat.entries.size]
                    val nextCodec = if (cfg.videoCodec == VideoCodecOption.H264) VideoCodecOption.HEVC else VideoCodecOption.H264
                    viewModel.updateExportConfig {
                        it.copy(photoFormat = nextPhoto, videoCodec = nextCodec)
                    }
                },
                testTag = "export_row_format"
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

            ExportSummaryRow(
                label = "Quality",
                value = "${cfg.qualityPreset.label} (${cfg.bitrateMbps} Mbps)",
                onClick = {
                    val list = ExportQualityPreset.entries
                    val next = list[(cfg.qualityPreset.ordinal + 1) % list.size]
                    viewModel.selectExportQualityPreset(next)
                },
                testTag = "export_row_quality"
            )

            // Toggle for Full Custom Dimensions, Aspect Ratio Crop & Privacy Controls (PRD Section 11)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { showAdvancedStudio = !showAdvancedStudio },
                    modifier = Modifier.testTag("export_toggle_custom_studio")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Tune,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (showAdvancedStudio) "Hide Custom Dimensions & Metadata" else "Custom Dimensions, Crop & Privacy",
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Text(
                    text = cfg.aspectRatio.label,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = JetBrainsMonoFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            AnimatedVisibility(visible = showAdvancedStudio) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Preset Quality Tiers
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ExportQualityPreset.entries.forEach { preset ->
                            FilterChip(
                                selected = cfg.qualityPreset == preset,
                                onClick = { viewModel.selectExportQualityPreset(preset) },
                                label = { Text(preset.label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }

                    // Custom Numeric Width & Height + Aspect Lock + Reset
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = widthInput,
                            onValueChange = { txt ->
                                widthInput = txt.filter { it.isDigit() }
                                widthInput.toIntOrNull()?.let { w ->
                                    if (w >= 256) viewModel.setExportCustomDimensions(w, cfg.customHeight)
                                }
                            },
                            label = { Text("Width (px)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_custom_width_input")
                        )

                        IconButton(
                            onClick = {
                                viewModel.updateExportConfig { it.copy(lockAspectRatio = !it.lockAspectRatio) }
                            }
                        ) {
                            Icon(
                                imageVector = if (cfg.lockAspectRatio) Icons.Outlined.Lock else Icons.Outlined.LockOpen,
                                contentDescription = "Lock Aspect Ratio",
                                tint = if (cfg.lockAspectRatio) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        OutlinedTextField(
                            value = heightInput,
                            onValueChange = { txt ->
                                heightInput = txt.filter { it.isDigit() }
                                heightInput.toIntOrNull()?.let { h ->
                                    if (h >= 256) viewModel.setExportCustomDimensions(cfg.customWidth, h)
                                }
                            },
                            label = { Text("Height (px)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_custom_height_input")
                        )
                    }

                    // Honest Upscale Notice per PRD Section 11 ("4K/8K and quality honesty")
                    if (isUpscaling) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = GodcamAmberGold.copy(alpha = 0.22f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Quality Honesty: Target (${cfg.customWidth}×${cfg.customHeight}) exceeds source (${target?.width}×${target?.height}). Upscaling increases pixel dimensions but does not restore missing optical detail.",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    // Aspect Ratio Crop Selector
                    Text("Aspect Ratio & Cinematic Crop", style = MaterialTheme.typography.labelLarge)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AspectRatioOption.entries.forEach { ratio ->
                            FilterChip(
                                selected = cfg.aspectRatio == ratio,
                                onClick = {
                                    viewModel.updateExportConfig { it.copy(aspectRatio = ratio) }
                                },
                                label = { Text(ratio.label) }
                            )
                        }
                    }

                    // Photo Compression Quality Slider
                    LabeledCameraSlider(
                        label = "Compression Quality",
                        valueText = "${cfg.jpegQuality}%",
                        value = cfg.jpegQuality.toFloat(),
                        valueRange = 20f..100f,
                        onValueChange = { q ->
                            viewModel.updateExportConfig { it.copy(jpegQuality = q.roundToInt()) }
                        }
                    )

                    // Metadata & Audio Privacy Toggles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Preserve EXIF & Location Metadata", style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = if (cfg.preserveMetadata) "Keeps camera & lens EXIF tags" else "Strips location & device tags for privacy",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                        Switch(
                            checked = cfg.preserveMetadata,
                            onCheckedChange = { checked ->
                                viewModel.updateExportConfig { it.copy(preserveMetadata = checked) }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Export Job Progress / Completion Status & Primary Export Pill Button
            when (val jobState = uiState.exportJobState) {
                is ExportJobState.Running -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = jobState.stepDescription,
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                text = "${(jobState.progress * 100).roundToInt()}%",
                                style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFamily)
                            )
                        }
                        LinearProgressIndicator(
                            progress = { jobState.progress },
                            color = GodcamForestActive,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(50))
                        )
                        TextButton(
                            onClick = { viewModel.cancelExportJob() },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Cancel Export")
                        }
                    }
                }

                is ExportJobState.Completed -> {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Saved to Gallery (${jobState.width}×${jobState.height})",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "${jobState.formatLabel} • ${jobState.sizeBytes / 1024} KB • Original intact",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            if (target != null) {
                                Surface(
                                    onClick = { onShareCompleted(target) },
                                    shape = RoundedCornerShape(50),
                                    color = GodcamForestActive,
                                    contentColor = Color.White
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Share,
                                            contentDescription = "Share",
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text("Share", style = MaterialTheme.typography.labelLarge)
                                    }
                                }
                            }
                        }
                    }
                }

                is ExportJobState.Cancelled -> {
                    Text(
                        text = jobState.message,
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                is ExportJobState.Failed -> {
                    Text(
                        text = jobState.errorMessage,
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.error),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                ExportJobState.Idle -> Unit
            }

            // Primary Pill "Export" Button matching Screen 5 of reference image
            Surface(
                onClick = { viewModel.runExportJob() },
                shape = RoundedCornerShape(50),
                color = GodcamForestActive,
                contentColor = Color(0xFFFCFBF7),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("export_confirm_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "Export",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color(0xFFFCFBF7),
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun ExportSummaryRow(
    label: String,
    value: String,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 13.dp)
            .testTag(testTag),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall.copy(
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Full-Screen Photo & Video Media Viewer with Pinch/Double-Tap Zoom, Video Playback Controls,
 * Before/After Comparison, EXIF Details Dialog, Delete Confirmation, Share, and Export Studio launch.
 */
@Composable
private fun FullScreenMediaViewer(
    item: MediaItemModel,
    viewModel: GodcamViewModel,
    onBack: () -> Unit,
    onOpenExport: () -> Unit,
    onEditLook: () -> Unit
) {
    val context = LocalContext.current
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var comparingOriginal by remember { mutableStateOf(false) }
    var showDetailsDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    // Video player state when viewing a video item
    var isPlayingVideo by remember { mutableStateOf(false) }
    var videoProgressSec by remember { mutableFloatStateOf(0f) }
    var videoMuted by remember { mutableStateOf(false) }
    val totalVideoSec = item.durationSec.coerceAtLeast(15).toFloat()

    LaunchedEffect(isPlayingVideo) {
        while (isPlayingVideo) {
            delay(250)
            videoProgressSec = if (videoProgressSec + 0.25f >= totalVideoSec) 0f else videoProgressSec + 0.25f
        }
    }

    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 4f)
        offset = if (scale > 1f) offset + panChange else Offset.Zero
    }

    val fullBitmap = remember(item.id, item.filePath, item.sampleSceneIndex) {
        ImageFilterEngine.loadBitmapForMediaItem(context, item, 1280, 960)
    }
    val preset = remember(item.appliedPresetId) {
        FilterCatalog.getById(item.appliedPresetId)
    }
    val matrix = remember(preset, item.appliedIntensity, comparingOriginal) {
        if (comparingOriginal) {
            ImageFilterEngine.buildComposeColorMatrix(FilterCatalog.allPresets.first(), EditRecipe(intensity = 0f))
        } else {
            ImageFilterEngine.buildComposeColorMatrix(
                preset,
                EditRecipe(presetId = preset.id, intensity = item.appliedIntensity)
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101714))
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("media_viewer_screen"),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Viewer Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back to Gallery",
                    tint = Color.White
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium.copy(color = Color.White)
                )
                Text(
                    text = "${item.width}×${item.height} • ${preset.name}",
                    style = MaterialTheme.typography.labelSmall.copy(color = GodcamMintAccent)
                )
            }

            Row {
                IconButton(onClick = { viewModel.toggleMediaFavorite(item) }) {
                    Icon(
                        imageVector = if (item.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (item.isFavorite) GodcamAmberGold else Color.White
                    )
                }
                IconButton(onClick = { showDetailsDialog = true }) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Media Details",
                        tint = Color.White
                    )
                }
            }
        }

        // Center Zoomable Image / Video Player Surface
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .transformable(state = transformState)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            scale = if (scale > 1.2f) 1f else 2.2f
                            offset = Offset.Zero
                        }
                    )
                }
        ) {
            Image(
                bitmap = fullBitmap.asImageBitmap(),
                contentDescription = item.title,
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.colorMatrix(matrix),
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
            )

            // Video Player Overlay if item.isVideo
            if (item.isVideo) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color(0x99101714))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = { isPlayingVideo = !isPlayingVideo },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (isPlayingVideo) Icons.Outlined.Pause else Icons.Filled.PlayArrow,
                                contentDescription = if (isPlayingVideo) "Pause Video" else "Play Video",
                                tint = Color.White
                            )
                        }
                        Slider(
                            value = videoProgressSec,
                            onValueChange = { videoProgressSec = it },
                            valueRange = 0f..totalVideoSec,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                        )
                        Text(
                            text = String.format(Locale.US, "0:%02d / 0:%02d", videoProgressSec.toInt(), totalVideoSec.toInt()),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = JetBrainsMonoFamily,
                                color = Color.White
                            )
                        )
                        IconButton(
                            onClick = { videoMuted = !videoMuted },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (videoMuted) Icons.AutoMirrored.Outlined.VolumeOff else Icons.AutoMirrored.Outlined.VolumeUp,
                                contentDescription = "Mute or Unmute Video",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Bottom Viewer Action Dock: Apply Look, Compare, Share, Delete, Export
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF16201C))
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ViewerActionButton(
                icon = Icons.Outlined.FilterBAndW,
                label = "Apply Look",
                onClick = onEditLook
            )
            ViewerActionButton(
                icon = Icons.Outlined.Compare,
                label = if (comparingOriginal) "Original" else "Compare",
                onClick = { comparingOriginal = !comparingOriginal }
            )
            ViewerActionButton(
                icon = Icons.Outlined.Share,
                label = "Share",
                onClick = { viewModel.shareMediaItem(context, item) }
            )
            ViewerActionButton(
                icon = Icons.Outlined.DeleteOutline,
                label = "Delete",
                onClick = { showDeleteConfirm = true }
            )
            Surface(
                onClick = onOpenExport,
                shape = RoundedCornerShape(50),
                color = GodcamForestActive,
                contentColor = Color.White,
                modifier = Modifier.testTag("viewer_export_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.IosShare,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }

    // EXIF & Metadata Details Dialog
    if (showDetailsDialog) {
        val dateStr = remember(item.timestampMs) {
            SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.US).format(Date(item.timestampMs))
        }
        AlertDialog(
            onDismissRequest = { showDetailsDialog = false },
            title = { Text("Media & EXIF Metadata") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Title: ${item.title}", fontWeight = FontWeight.SemiBold)
                    Text("Captured: $dateStr")
                    Text("Dimensions: ${item.width} × ${item.height} px")
                    Text("Format: ${item.formatLabel}")
                    Text("File Size: ${String.format(Locale.US, "%.2f MB", item.sizeBytes / (1024f * 1024f))}")
                    if (item.isVideo) Text("Duration: ${item.durationSec} seconds")
                    Text("Applied Look: ${preset.name} (${(item.appliedIntensity * 100).roundToInt()}%)")
                    Text("Optical Telemetry: ${item.cameraInfo}")
                }
            },
            confirmButton = {
                TextButton(onClick = { showDetailsDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    // Delete Confirmation Dialog (PRD Section 10)
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete ${item.title}?") },
            text = { Text("This removes the file from GODCAM Gallery. Exported copies in system Pictures remain untouched.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        viewModel.deleteSingleMedia(item)
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ViewerActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.88f))
        )
    }
}
