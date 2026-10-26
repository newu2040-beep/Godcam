package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.model.AppThemeOption

private fun buildSchemeFor(option: AppThemeOption, systemDark: Boolean): ColorScheme {
    val effective = if (option == AppThemeOption.SYSTEM) {
        if (systemDark) AppThemeOption.GRAPHITE else AppThemeOption.CREAM_MINT
    } else {
        option
    }

    return when (effective) {
        AppThemeOption.CREAM_MINT, AppThemeOption.SYSTEM -> lightColorScheme(
            primary = GodcamForestActive,
            onPrimary = Color(0xFFFCFBF7),
            primaryContainer = GodcamMintAccent,
            onPrimaryContainer = GodcamPrimaryText,
            secondary = GodcamMintAccent,
            onSecondary = GodcamPrimaryText,
            secondaryContainer = GodcamPillSurface,
            onSecondaryContainer = GodcamPrimaryText,
            background = GodcamCreamBackground,
            onBackground = GodcamPrimaryText,
            surface = GodcamSurfaceCard,
            onSurface = GodcamPrimaryText,
            surfaceVariant = GodcamPillSurface,
            onSurfaceVariant = GodcamSecondaryText,
            outline = GodcamBorderSage,
            error = GodcamRecordRed,
            onError = Color.White
        )

        AppThemeOption.PAPER_SAGE -> lightColorScheme(
            primary = PaperSageActive,
            onPrimary = Color.White,
            primaryContainer = PaperSageAccent,
            onPrimaryContainer = PaperSageText,
            secondary = PaperSageAccent,
            onSecondary = PaperSageText,
            secondaryContainer = Color(0xFFECEFE9),
            onSecondaryContainer = PaperSageText,
            background = PaperSageBg,
            onBackground = PaperSageText,
            surface = PaperSageSurface,
            onSurface = PaperSageText,
            surfaceVariant = Color(0xFFECEFE9),
            onSurfaceVariant = Color(0xFF5A6962),
            outline = Color(0xFFD5DDD8),
            error = GodcamRecordRed
        )

        AppThemeOption.OCEAN -> lightColorScheme(
            primary = OceanActive,
            onPrimary = Color.White,
            primaryContainer = OceanAccent,
            onPrimaryContainer = OceanText,
            secondary = OceanAccent,
            onSecondary = OceanText,
            secondaryContainer = Color(0xFFE1ECEF),
            onSecondaryContainer = OceanText,
            background = OceanBg,
            onBackground = OceanText,
            surface = OceanSurface,
            onSurface = OceanText,
            surfaceVariant = Color(0xFFE1ECEF),
            onSurfaceVariant = Color(0xFF526B73),
            outline = Color(0xFFCBDCE0),
            error = GodcamRecordRed
        )

        AppThemeOption.LAVENDER_MIST -> lightColorScheme(
            primary = LavenderActive,
            onPrimary = Color.White,
            primaryContainer = LavenderAccent,
            onPrimaryContainer = LavenderText,
            secondary = LavenderAccent,
            onSecondary = LavenderText,
            secondaryContainer = Color(0xFFE8E4F0),
            onSecondaryContainer = LavenderText,
            background = LavenderBg,
            onBackground = LavenderText,
            surface = LavenderSurface,
            onSurface = LavenderText,
            surfaceVariant = Color(0xFFE8E4F0),
            onSurfaceVariant = Color(0xFF635C73),
            outline = Color(0xFFD7D1E3),
            error = GodcamRecordRed
        )

        AppThemeOption.SUNSET_SAND -> lightColorScheme(
            primary = SunsetActive,
            onPrimary = Color.White,
            primaryContainer = SunsetAccent,
            onPrimaryContainer = SunsetText,
            secondary = SunsetAccent,
            onSecondary = SunsetText,
            secondaryContainer = Color(0xFFEDE2D3),
            onSecondaryContainer = SunsetText,
            background = SunsetBg,
            onBackground = SunsetText,
            surface = SunsetSurface,
            onSurface = SunsetText,
            surfaceVariant = Color(0xFFEDE2D3),
            onSurfaceVariant = Color(0xFF705A50),
            outline = Color(0xFFDFCFC0),
            error = GodcamRecordRed
        )

        AppThemeOption.GRAPHITE -> darkColorScheme(
            primary = GraphiteAccent,
            onPrimary = Color(0xFF14241E),
            primaryContainer = GraphiteActive,
            onPrimaryContainer = Color.White,
            secondary = GraphiteAccent,
            onSecondary = Color(0xFF14241E),
            secondaryContainer = Color(0xFF2D3632),
            onSecondaryContainer = GraphiteText,
            background = GraphiteBg,
            onBackground = GraphiteText,
            surface = GraphiteSurface,
            onSurface = GraphiteText,
            surfaceVariant = Color(0xFF2D3632),
            onSurfaceVariant = GraphiteSubtleText,
            outline = GraphiteBorder,
            error = GodcamRecordRed
        )

        AppThemeOption.MIDNIGHT -> darkColorScheme(
            primary = MidnightAccent,
            onPrimary = Color(0xFF0E1D17),
            primaryContainer = MidnightActive,
            onPrimaryContainer = Color.White,
            secondary = MidnightAccent,
            onSecondary = Color(0xFF0E1D17),
            secondaryContainer = Color(0xFF212B27),
            onSecondaryContainer = MidnightText,
            background = MidnightBg,
            onBackground = MidnightText,
            surface = MidnightSurface,
            onSurface = MidnightText,
            surfaceVariant = Color(0xFF212B27),
            onSurfaceVariant = MidnightSubtleText,
            outline = MidnightBorder,
            error = GodcamRecordRed
        )
    }
}

@Composable
fun GodcamTheme(
    themeOption: AppThemeOption = AppThemeOption.CREAM_MINT,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val colorScheme = buildSchemeFor(themeOption, systemDark)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
