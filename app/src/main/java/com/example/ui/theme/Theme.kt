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

        AppThemeOption.PASTEL_MATCHA -> lightColorScheme(
            primary = PastelMatchaActive,
            onPrimary = Color.White,
            primaryContainer = PastelMatchaAccent,
            onPrimaryContainer = PastelMatchaText,
            secondary = PastelMatchaAccent,
            onSecondary = PastelMatchaText,
            secondaryContainer = Color(0xFFDCECD6),
            onSecondaryContainer = PastelMatchaText,
            background = PastelMatchaBg,
            onBackground = PastelMatchaText,
            surface = PastelMatchaSurface,
            onSurface = PastelMatchaText,
            surfaceVariant = Color(0xFFDCECD6),
            onSurfaceVariant = Color(0xFF34523A),
            outline = PastelMatchaBorder,
            error = GodcamRecordRed
        )

        AppThemeOption.PASTEL_PEACH -> lightColorScheme(
            primary = PastelPeachActive,
            onPrimary = Color.White,
            primaryContainer = PastelPeachAccent,
            onPrimaryContainer = PastelPeachText,
            secondary = PastelPeachAccent,
            onSecondary = PastelPeachText,
            secondaryContainer = Color(0xFFFADDD1),
            onSecondaryContainer = PastelPeachText,
            background = PastelPeachBg,
            onBackground = PastelPeachText,
            surface = PastelPeachSurface,
            onSurface = PastelPeachText,
            surfaceVariant = Color(0xFFFADDD1),
            onSurfaceVariant = Color(0xFF5E3A2F),
            outline = PastelPeachBorder,
            error = GodcamRecordRed
        )

        AppThemeOption.PASTEL_SAKURA -> lightColorScheme(
            primary = PastelSakuraActive,
            onPrimary = Color.White,
            primaryContainer = PastelSakuraAccent,
            onPrimaryContainer = PastelSakuraText,
            secondary = PastelSakuraAccent,
            onSecondary = PastelSakuraText,
            secondaryContainer = Color(0xFFFAD9E5),
            onSecondaryContainer = PastelSakuraText,
            background = PastelSakuraBg,
            onBackground = PastelSakuraText,
            surface = PastelSakuraSurface,
            onSurface = PastelSakuraText,
            surfaceVariant = Color(0xFFFAD9E5),
            onSurfaceVariant = Color(0xFF5C3444),
            outline = PastelSakuraBorder,
            error = GodcamRecordRed
        )

        AppThemeOption.PASTEL_BUTTER -> lightColorScheme(
            primary = PastelButterActive,
            onPrimary = Color.White,
            primaryContainer = PastelButterAccent,
            onPrimaryContainer = PastelButterText,
            secondary = PastelButterAccent,
            onSecondary = PastelButterText,
            secondaryContainer = Color(0xFFF7EBC4),
            onSecondaryContainer = PastelButterText,
            background = PastelButterBg,
            onBackground = PastelButterText,
            surface = PastelButterSurface,
            onSurface = PastelButterText,
            surfaceVariant = Color(0xFFF7EBC4),
            onSurfaceVariant = Color(0xFF57481C),
            outline = PastelButterBorder,
            error = GodcamRecordRed
        )

        AppThemeOption.PASTEL_SKY -> lightColorScheme(
            primary = PastelSkyActive,
            onPrimary = Color.White,
            primaryContainer = PastelSkyAccent,
            onPrimaryContainer = PastelSkyText,
            secondary = PastelSkyAccent,
            onSecondary = PastelSkyText,
            secondaryContainer = Color(0xFFD6E7FA),
            onSecondaryContainer = PastelSkyText,
            background = PastelSkyBg,
            onBackground = PastelSkyText,
            surface = PastelSkySurface,
            onSurface = PastelSkyText,
            surfaceVariant = Color(0xFFD6E7FA),
            onSurfaceVariant = Color(0xFF2D4966),
            outline = PastelSkyBorder,
            error = GodcamRecordRed
        )

        AppThemeOption.PASTEL_LILAC -> lightColorScheme(
            primary = PastelLilacActive,
            onPrimary = Color.White,
            primaryContainer = PastelLilacAccent,
            onPrimaryContainer = PastelLilacText,
            secondary = PastelLilacAccent,
            onSecondary = PastelLilacText,
            secondaryContainer = Color(0xFFE5D9FA),
            onSecondaryContainer = PastelLilacText,
            background = PastelLilacBg,
            onBackground = PastelLilacText,
            surface = PastelLilacSurface,
            onSurface = PastelLilacText,
            surfaceVariant = Color(0xFFE5D9FA),
            onSurfaceVariant = Color(0xFF473566),
            outline = PastelLilacBorder,
            error = GodcamRecordRed
        )

        AppThemeOption.PASTEL_PISTACHIO -> lightColorScheme(
            primary = PastelPistachioActive,
            onPrimary = Color.White,
            primaryContainer = PastelPistachioAccent,
            onPrimaryContainer = PastelPistachioText,
            secondary = PastelPistachioAccent,
            onSecondary = PastelPistachioText,
            secondaryContainer = Color(0xFFD4EFE0),
            onSecondaryContainer = PastelPistachioText,
            background = PastelPistachioBg,
            onBackground = PastelPistachioText,
            surface = PastelPistachioSurface,
            onSurface = PastelPistachioText,
            surfaceVariant = Color(0xFFD4EFE0),
            onSurfaceVariant = Color(0xFF2C523E),
            outline = PastelPistachioBorder,
            error = GodcamRecordRed
        )

        AppThemeOption.PAPER_SAGE -> lightColorScheme(
            primary = PaperSageActive,
            onPrimary = Color.White,
            primaryContainer = PaperSageAccent,
            onPrimaryContainer = PaperSageText,
            secondary = PaperSageAccent,
            onSecondary = PaperSageText,
            secondaryContainer = Color(0xFFE4EAE6),
            onSecondaryContainer = PaperSageText,
            background = PaperSageBg,
            onBackground = PaperSageText,
            surface = PaperSageSurface,
            onSurface = PaperSageText,
            surfaceVariant = Color(0xFFE4EAE6),
            onSurfaceVariant = Color(0xFF42544D),
            outline = Color(0xFFC2CEC8),
            error = GodcamRecordRed
        )

        AppThemeOption.OCEAN -> lightColorScheme(
            primary = OceanActive,
            onPrimary = Color.White,
            primaryContainer = OceanAccent,
            onPrimaryContainer = OceanText,
            secondary = OceanAccent,
            onSecondary = OceanText,
            secondaryContainer = Color(0xFFD9E7EC),
            onSecondaryContainer = OceanText,
            background = OceanBg,
            onBackground = OceanText,
            surface = OceanSurface,
            onSurface = OceanText,
            surfaceVariant = Color(0xFFD9E7EC),
            onSurfaceVariant = Color(0xFF3E5963),
            outline = Color(0xFFB8CDD4),
            error = GodcamRecordRed
        )

        AppThemeOption.LAVENDER_MIST -> lightColorScheme(
            primary = LavenderActive,
            onPrimary = Color.White,
            primaryContainer = LavenderAccent,
            onPrimaryContainer = LavenderText,
            secondary = LavenderAccent,
            onSecondary = LavenderText,
            secondaryContainer = Color(0xFFE2DDEC),
            onSecondaryContainer = LavenderText,
            background = LavenderBg,
            onBackground = LavenderText,
            surface = LavenderSurface,
            onSurface = LavenderText,
            surfaceVariant = Color(0xFFE2DDEC),
            onSurfaceVariant = Color(0xFF4E4661),
            outline = Color(0xFFC7C0D6),
            error = GodcamRecordRed
        )

        AppThemeOption.SUNSET_SAND -> lightColorScheme(
            primary = SunsetActive,
            onPrimary = Color.White,
            primaryContainer = SunsetAccent,
            onPrimaryContainer = SunsetText,
            secondary = SunsetAccent,
            onSecondary = SunsetText,
            secondaryContainer = Color(0xFFEADBC9),
            onSecondaryContainer = SunsetText,
            background = SunsetBg,
            onBackground = SunsetText,
            surface = SunsetSurface,
            onSurface = SunsetText,
            surfaceVariant = Color(0xFFEADBC9),
            onSurfaceVariant = Color(0xFF5C453B),
            outline = Color(0xFFD4C0AE),
            error = GodcamRecordRed
        )

        AppThemeOption.GRAPHITE -> darkColorScheme(
            primary = GraphiteAccent,
            onPrimary = Color(0xFF0E1D17),
            primaryContainer = GraphiteActive,
            onPrimaryContainer = Color.White,
            secondary = GraphiteAccent,
            onSecondary = Color(0xFF0E1D17),
            secondaryContainer = Color(0xFF2B3531),
            onSecondaryContainer = GraphiteText,
            background = GraphiteBg,
            onBackground = GraphiteText,
            surface = GraphiteSurface,
            onSurface = GraphiteText,
            surfaceVariant = Color(0xFF2B3531),
            onSurfaceVariant = GraphiteSubtleText,
            outline = GraphiteBorder,
            error = GodcamRecordRed
        )

        AppThemeOption.MIDNIGHT -> darkColorScheme(
            primary = MidnightAccent,
            onPrimary = Color(0xFF091510),
            primaryContainer = MidnightActive,
            onPrimaryContainer = Color.White,
            secondary = MidnightAccent,
            onSecondary = Color(0xFF091510),
            secondaryContainer = Color(0xFF1F2925),
            onSecondaryContainer = MidnightText,
            background = MidnightBg,
            onBackground = MidnightText,
            surface = MidnightSurface,
            onSurface = MidnightText,
            surfaceVariant = Color(0xFF1F2925),
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
