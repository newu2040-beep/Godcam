package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AspectRatioOption
import com.example.model.GridMode
import com.example.ui.theme.GodcamAmberGold
import com.example.ui.theme.GodcamForestActive
import com.example.ui.theme.GodcamMintAccent
import com.example.ui.theme.GodcamRecordRed
import com.example.ui.theme.JetBrainsMonoFamily
import kotlin.math.abs

/**
 * Iconic circular GODCAM 3D Lens Emblem.
 */
@Composable
fun GodcamLensEmblem(
    size: Dp = 112.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val r = this.size.minDimension / 2f
        val center = Offset(this.size.width / 2f, this.size.height / 2f)

        // Outer soft cream-mint metallic housing
        drawCircle(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFFEFF4F0), Color(0xFF7BA492)),
                start = Offset(0f, 0f),
                end = Offset(this.size.width, this.size.height)
            ),
            radius = r,
            center = center
        )
        // Subtle bevel ring
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFDCE7E1), Color(0xFF699683)),
                center = center,
                radius = r * 0.92f
            ),
            radius = r * 0.90f,
            center = center
        )
        // Deep forest optical barrel
        drawCircle(
            color = Color(0xFF1F3830),
            radius = r * 0.66f,
            center = center
        )
        // Multi-coated inner glass
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF2E5447), Color(0xFF11201B), Color(0xFF0A1410)),
                center = Offset(center.x - r * 0.15f, center.y - r * 0.15f),
                radius = r * 0.56f
            ),
            radius = r * 0.54f,
            center = center
        )
        // Aperture core
        drawCircle(
            color = Color(0xFF070F0C),
            radius = r * 0.28f,
            center = center
        )
        // Specular optical reflection dot (top-left)
        drawCircle(
            color = Color(0xFFFCFBF7).copy(alpha = 0.92f),
            radius = r * 0.13f,
            center = Offset(center.x - r * 0.18f, center.y - r * 0.18f)
        )
        // Secondary subtle emerald reflection dot (bottom-right)
        drawCircle(
            color = GodcamMintAccent.copy(alpha = 0.45f),
            radius = r * 0.06f,
            center = Offset(center.x + r * 0.18f, center.y + r * 0.18f)
        )
        // Rangefinder amber indicator dot on top-right housing
        drawCircle(
            color = GodcamAmberGold,
            radius = r * 0.095f,
            center = Offset(center.x + r * 0.58f, center.y - r * 0.58f)
        )
    }
}

/**
 * Bold Minimalist GODCAM Studio Cover — zero fake images, featuring architectural rangefinder marks
 * and precision typography.
 */
@Composable
fun GodcamHeroSplashOverlay(
    onEnterCamera: () -> Unit,
    onOpenGallery: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF7F3E9))
            .testTag("hero_splash_screen")
    ) {
        // Minimalist architectural rangefinder grid lines
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val gridStroke = 1.dp.toPx()
            val ruleColor = Color(0xFF20332D).copy(alpha = 0.07f)

            drawLine(ruleColor, Offset(w * 0.12f, 0f), Offset(w * 0.12f, h), gridStroke)
            drawLine(ruleColor, Offset(w * 0.88f, 0f), Offset(w * 0.88f, h), gridStroke)
            drawLine(ruleColor, Offset(0f, h * 0.22f), Offset(w, h * 0.22f), gridStroke)
            drawLine(ruleColor, Offset(0f, h * 0.72f), Offset(w, h * 0.72f), gridStroke)

            // Precision optical ring at center
            drawCircle(
                color = Color(0xFF20332D).copy(alpha = 0.08f),
                radius = w * 0.36f,
                center = Offset(w / 2f, h * 0.42f),
                style = Stroke(width = 1.dp.toPx())
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 42.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Minimalist Spec Tag
            Surface(
                shape = RoundedCornerShape(50),
                color = Color(0xFF20332D).copy(alpha = 0.08f)
            ) {
                Text(
                    text = "OPTICAL STUDIO  •  108 ANALOG LOOKS  •  CAMERA2 PRO",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = JetBrainsMonoFamily,
                        letterSpacing = 1.2.sp,
                        color = Color(0xFF20332D),
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                GodcamLensEmblem(size = 124.dp)
                Spacer(modifier = Modifier.height(28.dp))
                Text(
                    text = "G O D C A M",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 34.sp,
                        letterSpacing = 6.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF182823)
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Capture More Than Moments",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color(0xFF4E5E57),
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.4.sp
                    )
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        onClick = onEnterCamera,
                        shape = RoundedCornerShape(50),
                        color = GodcamForestActive,
                        contentColor = Color(0xFFFCFBF7),
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .testTag("hero_enter_camera_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "Open Camera",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                    Surface(
                        onClick = onOpenGallery,
                        shape = RoundedCornerShape(50),
                        color = Color(0xFFFCFBF7),
                        contentColor = Color(0xFF182823),
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .border(1.5.dp, Color(0xFF182823).copy(alpha = 0.2f), RoundedCornerShape(50))
                            .testTag("hero_open_gallery_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "Gallery & Looks",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "REAL-TIME HARDWARE TELEMETRY  •  ZERO SIMULATION",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = JetBrainsMonoFamily,
                        color = Color(0xFF56655E),
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}

/**
 * Circular icon control with minimum 48dp touch target, crisp border, and active state.
 */
@Composable
fun CircularIconControl(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    size: Dp = 40.dp,
    testTag: String? = null
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
        label = "circleBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        label = "circleFg"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .minimumInteractiveComponentSize()
            .size(size)
            .clip(CircleShape)
            .background(bgColor)
            .border(1.2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f), CircleShape)
            .clickable(onClick = onClick)
            .let { if (testTag != null) it.testTag(testTag) else it }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = contentColor,
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * Bold Minimalist Segmented Pill Toggle (Photo | Video, Video | Pro Video, Photos | Videos | Favorites).
 */
@Composable
fun <T> SegmentedPillSelector(
    options: List<T>,
    selectedOption: T,
    labelProvider: (T) -> String,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    testTagPrefix: String = "pill"
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.92f),
        modifier = modifier
            .height(40.dp)
            .border(1.2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f), RoundedCornerShape(50))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(3.dp)
        ) {
            options.forEach { option ->
                val selected = option == selectedOption
                val pillBg by animateColorAsState(
                    targetValue = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    label = "pillBg"
                )
                val pillText by animateColorAsState(
                    targetValue = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    label = "pillText"
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(34.dp)
                        .clip(RoundedCornerShape(50))
                        .background(pillBg)
                        .clickable { onOptionSelected(option) }
                        .padding(horizontal = 18.dp)
                        .testTag("${testTagPrefix}_${labelProvider(option).lowercase().replace(" ", "_")}")
                ) {
                    Text(
                        text = labelProvider(option),
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = pillText,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        )
                    )
                }
            }
        }
    }
}

/**
 * Tactile GODCAM Shutter / Video Record Button.
 */
@Composable
fun GodcamShutterButton(
    isVideoMode: Boolean,
    isRecording: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isRecording) 0.92f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "shutterScale"
    )

    val outerRingColor = if (isVideoMode) {
        Color(0xFFD5D2C8)
    } else {
        GodcamMintAccent.copy(alpha = 0.6f)
    }

    val innerFillColor = if (isVideoMode) {
        GodcamRecordRed
    } else {
        GodcamForestActive
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(76.dp)
            .clip(CircleShape)
            .background(outerRingColor)
            .border(3.dp, Color(0xFFFCFBF7), CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .testTag("primary_shutter_button")
    ) {
        Box(
            modifier = Modifier
                .size(if (isRecording) 32.dp else (60.dp * scale))
                .clip(if (isRecording) RoundedCornerShape(9.dp) else CircleShape)
                .background(innerFillColor)
                .border(
                    width = 2.dp,
                    color = Color(0xFFFCFBF7).copy(alpha = 0.9f),
                    shape = if (isRecording) RoundedCornerShape(9.dp) else CircleShape
                )
        )
    }
}

/**
 * Labeled Slider with right-aligned tabular value + unit and optional one-tap Reset.
 */
@Composable
fun LabeledCameraSlider(
    label: String,
    valueText: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    steps: Int = 0,
    onReset: (() -> Unit)? = null,
    testTag: String = "slider"
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = valueText,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = JetBrainsMonoFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                if (onReset != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = onReset,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "Reset $label",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
        Slider(
            value = value.coerceIn(valueRange.start, valueRange.endInclusive),
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .testTag(testTag)
        )
    }
}

/**
 * Viewfinder Grid, Golden Ratio, Real-time Accelerometer Horizon Level,
 * Cinematic Letterbox Guide, Focus Peaking, and Tap-to-Focus Reticle Canvas Overlay.
 */
@Composable
fun ViewfinderCompositionOverlays(
    gridMode: GridMode,
    aspectRatio: AspectRatioOption,
    focusPoint: Pair<Float, Float>?,
    aeAfLocked: Boolean,
    showFocusPeaking: Boolean,
    showZebraStripes: Boolean,
    deviceTiltDegrees: Float = 0f,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val lineColor = Color.White.copy(alpha = 0.38f)
        val strokeWidth = 1.dp.toPx()

        // 1. Grid / Composition lines
        when (gridMode) {
            GridMode.OFF -> Unit
            GridMode.THREE_BY_THREE -> {
                for (i in 1..2) {
                    val x = w * (i / 3f)
                    val y = h * (i / 3f)
                    drawLine(lineColor, Offset(x, 0f), Offset(x, h), strokeWidth)
                    drawLine(lineColor, Offset(0f, y), Offset(w, y), strokeWidth)
                }
            }
            GridMode.FOUR_BY_FOUR -> {
                for (i in 1..3) {
                    val x = w * (i / 4f)
                    val y = h * (i / 4f)
                    drawLine(lineColor, Offset(x, 0f), Offset(x, h), strokeWidth)
                    drawLine(lineColor, Offset(0f, y), Offset(w, y), strokeWidth)
                }
            }
            GridMode.GOLDEN_RATIO -> {
                val phi1 = 0.382f
                val phi2 = 0.618f
                for (ratio in listOf(phi1, phi2)) {
                    drawLine(lineColor, Offset(w * ratio, 0f), Offset(w * ratio, h), strokeWidth)
                    drawLine(lineColor, Offset(0f, h * ratio), Offset(w, h * ratio), strokeWidth)
                }
            }
            GridMode.LEVEL -> {
                for (i in 1..2) {
                    val x = w * (i / 3f)
                    val y = h * (i / 3f)
                    drawLine(lineColor, Offset(x, 0f), Offset(x, h), strokeWidth)
                    drawLine(lineColor, Offset(0f, y), Offset(w, y), strokeWidth)
                }
                // Real-time Accelerometer Horizon Level Bar in center
                val cy = h / 2f
                val cx = w / 2f
                val isLevel = abs(deviceTiltDegrees) <= 1.5f
                val levelColor = if (isLevel) GodcamMintAccent else GodcamAmberGold

                // Fixed reference ticks
                drawLine(
                    color = Color.White.copy(alpha = 0.55f),
                    start = Offset(cx - 64.dp.toPx(), cy),
                    end = Offset(cx - 46.dp.toPx(), cy),
                    strokeWidth = 1.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.55f),
                    start = Offset(cx + 46.dp.toPx(), cy),
                    end = Offset(cx + 64.dp.toPx(), cy),
                    strokeWidth = 1.5.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Rotating live accelerometer horizon bar
                rotate(degrees = -deviceTiltDegrees.coerceIn(-45f, 45f), pivot = Offset(cx, cy)) {
                    drawLine(
                        color = levelColor,
                        start = Offset(cx - 42.dp.toPx(), cy),
                        end = Offset(cx + 42.dp.toPx(), cy),
                        strokeWidth = 2.4.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
                drawCircle(
                    color = levelColor,
                    radius = 4.dp.toPx(),
                    center = Offset(cx, cy),
                    style = Stroke(width = 1.6.dp.toPx())
                )
            }
        }

        // 2. Cinematic Framing Guides (2.00:1, 2.20:1, 2.35:1, 2.39:1)
        if (aspectRatio.isCinematicGuide) {
            val guideHeight = (w / aspectRatio.aspectValue).coerceAtMost(h * 0.85f)
            val barHeight = ((h - guideHeight) / 2f).coerceAtLeast(0f)
            val maskColor = Color.Black.copy(alpha = 0.52f)
            drawRect(maskColor, Offset(0f, 0f), Size(w, barHeight))
            drawRect(maskColor, Offset(0f, h - barHeight), Size(w, barHeight))
            val guideLine = GodcamAmberGold.copy(alpha = 0.8f)
            drawLine(guideLine, Offset(0f, barHeight), Offset(w, barHeight), 1.2.dp.toPx())
            drawLine(guideLine, Offset(0f, h - barHeight), Offset(w, h - barHeight), 1.2.dp.toPx())
        }

        // 3. Focus Peaking assist highlight
        if (showFocusPeaking) {
            val peakColor = Color(0xFF4FFFB0).copy(alpha = 0.65f)
            drawRoundRect(
                color = peakColor,
                topLeft = Offset(w * 0.28f, h * 0.24f),
                size = Size(w * 0.44f, h * 0.48f),
                cornerRadius = CornerRadius(24f, 24f),
                style = Stroke(width = 1.5.dp.toPx())
            )
        }

        // 4. Zebra Exposure Clipping Stripes assist
        if (showZebraStripes) {
            val zebraColor = Color.White.copy(alpha = 0.32f)
            var zx = w * 0.6f
            while (zx < w * 0.9f) {
                drawLine(
                    color = zebraColor,
                    start = Offset(zx, h * 0.08f),
                    end = Offset(zx - 28f, h * 0.22f),
                    strokeWidth = 2f
                )
                zx += 12f
            }
        }

        // 5. Tap-to-Focus & AE/AF Lock Reticle
        focusPoint?.let { (nx, ny) ->
            val fx = (nx * w).coerceIn(36f, w - 36f)
            val fy = (ny * h).coerceIn(36f, h - 36f)
            val boxRadius = 28.dp.toPx()
            val reticleColor = if (aeAfLocked) GodcamAmberGold else GodcamMintAccent
            drawRoundRect(
                color = reticleColor,
                topLeft = Offset(fx - boxRadius, fy - boxRadius),
                size = Size(boxRadius * 2f, boxRadius * 2f),
                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                style = Stroke(width = 2.dp.toPx())
            )
            drawCircle(
                color = reticleColor,
                radius = 3.dp.toPx(),
                center = Offset(fx, fy)
            )
        }
    }
}

/**
 * Real-time Luminance Histogram mini-overlay for Pro Photo & Pro Video modes.
 */
@Composable
fun LiveHistogramOverlay(
    bins: List<Float>,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xBB101A16),
        modifier = modifier
            .size(width = 98.dp, height = 46.dp)
            .border(1.dp, Color.White.copy(alpha = 0.24f), RoundedCornerShape(10.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(6.dp)) {
            if (bins.isEmpty()) return@Canvas
            val w = size.width
            val h = size.height
            val step = w / (bins.size - 1).coerceAtLeast(1)
            val path = Path().apply {
                moveTo(0f, h)
                bins.forEachIndexed { idx, v ->
                    lineTo(idx * step, h * (1f - v.coerceIn(0f, 1f)))
                }
                lineTo(w, h)
                close()
            }
            drawPath(
                path = path,
                color = GodcamMintAccent.copy(alpha = 0.72f)
            )
        }
    }
}
