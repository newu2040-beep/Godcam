package com.example.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Compare
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.FilterCatalog
import com.example.data.ImageFilterEngine
import com.example.model.EditRecipe
import com.example.model.FilterFamily
import com.example.model.FilterPreset
import com.example.ui.AppDestination
import com.example.ui.CameraUiState
import com.example.ui.GodcamViewModel
import com.example.ui.components.LabeledCameraSlider
import com.example.ui.theme.GodcamAmberGold
import com.example.ui.theme.GodcamForestActive
import com.example.ui.theme.JetBrainsMonoFamily
import kotlin.math.roundToInt

@Composable
fun FiltersScreen(
    uiState: CameraUiState,
    favoritePresetIds: Set<String>,
    viewModel: GodcamViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val latestCameraFrame by viewModel.latestCameraFrame.collectAsStateWithLifecycle()

    BackHandler {
        if (uiState.activeViewerMedia != null) {
            viewModel.navigateTo(AppDestination.GALLERY)
        } else {
            viewModel.navigateTo(AppDestination.CAMERA)
        }
    }

    var showSearchBar by remember { mutableStateOf(false) }
    var showFineAdjustments by remember { mutableStateOf(false) }

    // Use real captured/gallery image or real live camera sensor frame; fallback to optical color-calibration chart
    val heroBitmap = remember(uiState.activeViewerMedia?.id, uiState.activeViewerMedia?.filePath, latestCameraFrame) {
        ImageFilterEngine.loadBitmapForMediaItem(context, uiState.activeViewerMedia, 1080, 720)
            ?: latestCameraFrame
            ?: ImageFilterEngine.createNeutralCalibrationBitmap(960, 640)
    }

    val thumbBitmap = remember(heroBitmap) {
        runCatching {
            Bitmap.createScaledBitmap(heroBitmap, 260, 195, true)
        }.getOrElse {
            ImageFilterEngine.createNeutralCalibrationBitmap(260, 195)
        }
    }

    val activePreset = remember(uiState.activeRecipe.presetId) {
        FilterCatalog.getById(uiState.activeRecipe.presetId)
    }
    val heroColorMatrix = remember(activePreset, uiState.activeRecipe, uiState.isComparingOriginal, uiState.exposureEv) {
        if (uiState.isComparingOriginal) {
            ImageFilterEngine.buildComposeColorMatrix(FilterCatalog.allPresets.first(), EditRecipe(intensity = 0f))
        } else {
            ImageFilterEngine.buildComposeColorMatrix(activePreset, uiState.activeRecipe, uiState.exposureEv)
        }
    }

    val filteredPresets = remember(
        uiState.selectedFilterFamily,
        uiState.filterSearchQuery,
        favoritePresetIds
    ) {
        FilterCatalog.allPresets.filter { preset ->
            val matchesFamily = when (uiState.selectedFilterFamily) {
                FilterFamily.ALL -> true
                FilterFamily.FAVORITES -> preset.id in favoritePresetIds
                else -> preset.family == uiState.selectedFilterFamily
            }
            val q = uiState.filterSearchQuery.trim()
            val matchesQuery = q.isEmpty() ||
                preset.name.contains(q, ignoreCase = true) ||
                preset.subtitle.contains(q, ignoreCase = true) ||
                preset.family.displayName.contains(q, ignoreCase = true)
            matchesFamily && matchesQuery
        }
    }

    val orderedPresets = remember(filteredPresets, uiState.selectedFilterFamily, uiState.filterSearchQuery) {
        if (uiState.selectedFilterFamily == FilterFamily.ALL && uiState.filterSearchQuery.isBlank()) {
            val priorityIds = listOf(
                "original",
                "fuji_pro_400h",
                "kodak_gold_200",
                "retro_agfa_vista",
                "kodak_portra_400",
                "retro_classic",
                "cine_still_800t",
                "retro_vintage",
                "bw_classic"
            )
            val top = priorityIds.mapNotNull { id -> filteredPresets.firstOrNull { it.id == id } }
            val rest = filteredPresets.filter { it.id !in priorityIds }
            top + rest
        } else {
            filteredPresets
        }
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
                .align(Alignment.Center)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // 1. BOLD MINIMALIST TOP HEADER ("<"  "Filters"  "Apply / Save")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (uiState.activeViewerMedia != null) {
                            viewModel.navigateTo(AppDestination.GALLERY)
                        } else {
                            viewModel.navigateTo(AppDestination.CAMERA)
                        }
                    },
                    modifier = Modifier.testTag("filters_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Filters",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = if (uiState.captureMode.isVideo) "ACTIVE FOR PHOTO & VIDEO" else "108 ANALOG & FILM LOOKS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 9.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.8.sp
                        )
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = { showSearchBar = !showSearchBar },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("filters_search_toggle")
                    ) {
                        Icon(
                            imageVector = if (showSearchBar) Icons.Outlined.Close else Icons.Outlined.Search,
                            contentDescription = "Search 108 Presets",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Surface(
                        onClick = {
                            if (uiState.activeViewerMedia != null) {
                                viewModel.applyFilterAndSave(heroBitmap)
                            } else {
                                // Apply filter to live Photo / Video viewfinder and return to Camera
                                viewModel.navigateTo(AppDestination.CAMERA)
                            }
                        },
                        shape = RoundedCornerShape(50),
                        color = GodcamForestActive,
                        contentColor = Color(0xFFFCFBF7),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("filters_save_button")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 18.dp)
                        ) {
                            Text(
                                text = if (uiState.activeViewerMedia != null) "Save" else "Apply",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = Color(0xFFFCFBF7),
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }

            // Optional Search Field
            AnimatedVisibility(visible = showSearchBar) {
                OutlinedTextField(
                    value = uiState.filterSearchQuery,
                    onValueChange = { viewModel.setFilterSearchQuery(it) },
                    placeholder = { Text("Search 108 looks (e.g. Fuji, Portra, Noir, Neon)…") },
                    singleLine = true,
                    shape = RoundedCornerShape(50),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                        .testTag("filters_search_input")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 2. LARGE REAL-TIME HERO PREVIEW CARD
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f), RoundedCornerShape(18.dp))
                    .testTag("filters_hero_preview")
            ) {
                Image(
                    bitmap = heroBitmap.asImageBitmap(),
                    contentDescription = "Live Filter Preview: ${activePreset.name}",
                    contentScale = ContentScale.Crop,
                    colorFilter = ColorFilter.colorMatrix(heroColorMatrix),
                    modifier = Modifier.fillMaxSize()
                )

                // Top-left Active Look Badge
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xBB101915),
                    contentColor = Color(0xFFFCFBF7),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                ) {
                    Text(
                        text = if (uiState.isComparingOriginal) {
                            "BEFORE • Original Sensor"
                        } else {
                            "${activePreset.name} • ${(uiState.activeRecipe.intensity * 100).roundToInt()}%"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (uiState.isComparingOriginal) GodcamAmberGold else Color(0xFFFCFBF7),
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }

                // Bottom-left & Bottom-right Hero Actions: Compare Original + Fine Tune Recipe
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = { viewModel.setComparingOriginal(!uiState.isComparingOriginal) },
                        shape = RoundedCornerShape(50),
                        color = if (uiState.isComparingOriginal) GodcamAmberGold else Color(0xBB101915),
                        contentColor = if (uiState.isComparingOriginal) Color(0xFF141E1A) else Color(0xFFFCFBF7),
                        modifier = Modifier.testTag("filters_compare_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Compare,
                                contentDescription = "Compare Before and After",
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (uiState.isComparingOriginal) "Showing Original" else "Before / After",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    Surface(
                        onClick = { showFineAdjustments = !showFineAdjustments },
                        shape = RoundedCornerShape(50),
                        color = if (showFineAdjustments) GodcamForestActive else Color(0xBB101915),
                        contentColor = Color(0xFFFCFBF7),
                        modifier = Modifier.testTag("filters_adjust_toggle")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Tune,
                                contentDescription = "Adjust Intensity & Grain",
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Adjust (${(uiState.activeRecipe.intensity * 100).roundToInt()}%)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }

            // Expandable Fine-Tuning Panel (Intensity 0-100%, Warmth, Contrast, Grain)
            AnimatedVisibility(visible = showFineAdjustments) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .border(1.2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        LabeledCameraSlider(
                            label = "Look Intensity",
                            valueText = "${(uiState.activeRecipe.intensity * 100).roundToInt()}%",
                            value = uiState.activeRecipe.intensity,
                            valueRange = 0f..1f,
                            onValueChange = { viewModel.setFilterIntensity(it) },
                            onReset = { viewModel.resetRecipeAdjustments() },
                            testTag = "filter_intensity_slider"
                        )
                        LabeledCameraSlider(
                            label = "Warmth Shift",
                            valueText = "${(uiState.activeRecipe.warmthOffset * 100).roundToInt()}",
                            value = uiState.activeRecipe.warmthOffset,
                            valueRange = -0.4f..0.4f,
                            onValueChange = { viewModel.updateRecipeAdjustments(warmth = it) }
                        )
                        LabeledCameraSlider(
                            label = "Analog Grain & Halation",
                            valueText = "${(uiState.activeRecipe.grainOffset * 100).roundToInt()}%",
                            value = uiState.activeRecipe.grainOffset,
                            valueRange = 0f..0.4f,
                            onValueChange = { viewModel.updateRecipeAdjustments(grain = it) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. CATEGORY FILTER CHIPS ROW (All | Fuji | Kodak | Retro | Film | B&W | Instant | Nature | Portrait | Night | Creative)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterFamily.entries.forEach { family ->
                    val selected = uiState.selectedFilterFamily == family
                    val chipBg by animateColorAsState(
                        targetValue = if (selected) GodcamForestActive else MaterialTheme.colorScheme.surface,
                        label = "chipBg"
                    )
                    val chipText by animateColorAsState(
                        targetValue = if (selected) Color(0xFFFCFBF7) else MaterialTheme.colorScheme.onSurfaceVariant,
                        label = "chipText"
                    )

                    Surface(
                        onClick = { viewModel.selectFilterFamily(family) },
                        shape = RoundedCornerShape(50),
                        color = chipBg,
                        modifier = Modifier
                            .height(34.dp)
                            .border(
                                width = 1.2.dp,
                                color = if (selected) GodcamForestActive else MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(50)
                            )
                            .testTag("filter_family_${family.name.lowercase()}")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = family.shortChip,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = chipText,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4. 3-COLUMN RESPONSIVE PRESET GRID (108 Distinct Presets)
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("filters_preset_grid")
            ) {
                items(orderedPresets, key = { it.id }) { preset ->
                    FilterPresetCard(
                        preset = preset,
                        thumbBitmap = thumbBitmap,
                        isSelected = preset.id == uiState.activeRecipe.presetId,
                        isFavorite = preset.id in favoritePresetIds,
                        onSelect = { viewModel.selectPreset(preset) },
                        onToggleFavorite = { viewModel.toggleFavoritePreset(preset.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterPresetCard(
    preset: FilterPreset,
    thumbBitmap: Bitmap,
    isSelected: Boolean,
    isFavorite: Boolean,
    onSelect: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val matrix = remember(preset) {
        ImageFilterEngine.buildComposeColorMatrix(preset)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .testTag("preset_card_${preset.id}")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.16f)
                .then(
                    if (isSelected) {
                        Modifier
                            .border(2.5.dp, GodcamForestActive, RoundedCornerShape(14.dp))
                            .padding(3.dp)
                    } else {
                        Modifier.border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                    }
                )
                .clip(RoundedCornerShape(11.dp))
        ) {
            Image(
                bitmap = thumbBitmap.asImageBitmap(),
                contentDescription = "${preset.name}: ${preset.subtitle}",
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.colorMatrix(matrix),
                modifier = Modifier.fillMaxSize()
            )

            // Subtle favorite star button in top-right of thumbnail
            if (preset.id != "original") {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(5.dp)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Color(0x88101815))
                        .clickable(onClick = onToggleFavorite)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                        contentDescription = if (isFavorite) "Remove Favorite" else "Favorite Preset",
                        tint = if (isFavorite) GodcamAmberGold else Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = preset.name,
            style = MaterialTheme.typography.labelLarge.copy(
                fontSize = 12.sp,
                color = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}
