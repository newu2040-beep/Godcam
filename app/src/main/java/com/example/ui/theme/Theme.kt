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

        AppThemeOption.PASTEL_CORAL -> lightColorScheme(
            primary = PastelCoralActive,
            onPrimary = Color.White,
            primaryContainer = PastelCoralAccent,
            onPrimaryContainer = PastelCoralText,
            secondary = PastelCoralAccent,
            onSecondary = PastelCoralText,
            secondaryContainer = Color(0xFFFCDAD5),
            onSecondaryContainer = PastelCoralText,
            background = PastelCoralBg,
            onBackground = PastelCoralText,
            surface = PastelCoralSurface,
            onSurface = PastelCoralText,
            surfaceVariant = Color(0xFFFCDAD5),
            onSurfaceVariant = Color(0xFF61312B),
            outline = PastelCoralBorder,
            error = GodcamRecordRed
        )

        AppThemeOption.PASTEL_MINT_GELATO -> lightColorScheme(
            primary = PastelMintGelatoActive,
            onPrimary = Color.White,
            primaryContainer = PastelMintGelatoAccent,
            onPrimaryContainer = PastelMintGelatoText,
            secondary = PastelMintGelatoAccent,
            onSecondary = PastelMintGelatoText,
            secondaryContainer = Color(0xFFD0F2E4),
            onSecondaryContainer = PastelMintGelatoText,
            background = PastelMintGelatoBg,
            onBackground = PastelMintGelatoText,
            surface = PastelMintGelatoSurface,
            onSurface = PastelMintGelatoText,
            surfaceVariant = Color(0xFFD0F2E4),
            onSurfaceVariant = Color(0xFF255442),
            outline = PastelMintGelatoBorder,
            error = GodcamRecordRed
        )

        AppThemeOption.PASTEL_ROSEWATER -> lightColorScheme(
            primary = PastelRosewaterActive,
            onPrimary = Color.White,
            primaryContainer = PastelRosewaterAccent,
            onPrimaryContainer = PastelRosewaterText,
            secondary = PastelRosewaterAccent,
            onSecondary = PastelRosewaterText,
            secondaryContainer = Color(0xFFFAD7E0),
            onSecondaryContainer = PastelRosewaterText,
            background = PastelRosewaterBg,
            onBackground = PastelRosewaterText,
            surface = PastelRosewaterSurface,
            onSurface = PastelRosewaterText,
            surfaceVariant = Color(0xFFFAD7E0),
            onSurfaceVariant = Color(0xFF5E303C),
            outline = PastelRosewaterBorder,
            error = GodcamRecordRed
        )

        AppThemeOption.PASTEL_HONEYDEW -> lightColorScheme(
            primary = PastelHoneydewActive,
            onPrimary = Color.White,
            primaryContainer = PastelHoneydewAccent,
            onPrimaryContainer = PastelHoneydewText,
            secondary = PastelHoneydewAccent,
            onSecondary = PastelHoneydewText,
            secondaryContainer = Color(0xFFDDF2D3),
            onSecondaryContainer = PastelHoneydewText,
            background = PastelHoneydewBg,
            onBackground = PastelHoneydewText,
            surface = PastelHoneydewSurface,
            onSurface = PastelHoneydewText,
            surfaceVariant = Color(0xFFDDF2D3),
            onSurfaceVariant = Color(0xFF39542B),
            outline = PastelHoneydewBorder,
            error = GodcamRecordRed
        )

        AppThemeOption.PASTEL_PERIWINKLE -> lightColorScheme(
            primary = PastelPeriwinkleActive,
            onPrimary = Color.White,
            primaryContainer = PastelPeriwinkleAccent,
            onPrimaryContainer = PastelPeriwinkleText,
            secondary = PastelPeriwinkleAccent,
            onSecondary = PastelPeriwinkleText,
            secondaryContainer = Color(0xFFD8DFFA),
            onSecondaryContainer = PastelPeriwinkleText,
            background = PastelPeriwinkleBg,
            onBackground = PastelPeriwinkleText,
            surface = PastelPeriwinkleSurface,
            onSurface = PastelPeriwinkleText,
            surfaceVariant = Color(0xFFD8DFFA),
            onSurfaceVariant = Color(0xFF333D66),
            outline = PastelPeriwinkleBorder,
            error = GodcamRecordRed
        )

        AppThemeOption.PASTEL_CHAMOMILE -> lightColorScheme(
            primary = PastelChamomileActive,
            onPrimary = Color.White,
            primaryContainer = PastelChamomileAccent,
            onPrimaryContainer = PastelChamomileText,
            secondary = PastelChamomileAccent,
            onSecondary = PastelChamomileText,
            secondaryContainer = Color(0xFFF7ECC6),
            onSecondaryContainer = PastelChamomileText,
            background = PastelChamomileBg,
            onBackground = PastelChamomileText,
            surface = PastelChamomileSurface,
            onSurface = PastelChamomileText,
            surfaceVariant = Color(0xFFF7ECC6),
            onSurfaceVariant = Color(0xFF594B1D),
            outline = PastelChamomileBorder,
            error = GodcamRecordRed
        )

        AppThemeOption.FUJI_SILVER -> lightColorScheme(
            primary = FujiSilverActive,
            onPrimary = Color.White,
            primaryContainer = FujiSilverAccent,
            onPrimaryContainer = Color.White,
            secondary = FujiSilverAccent,
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFDCDEDB),
            onSecondaryContainer = FujiSilverText,
            background = FujiSilverBg,
            onBackground = FujiSilverText,
            surface = FujiSilverSurface,
            onSurface = FujiSilverText,
            surfaceVariant = Color(0xFFDCDEDB),
            onSurfaceVariant = Color(0xFF454B4F),
            outline = FujiSilverBorder,
            error = GodcamRecordRed
        )

        AppThemeOption.NORDIC_SNOW -> lightColorScheme(
            primary = NordicSnowActive,
            onPrimary = Color.White,
            primaryContainer = NordicSnowAccent,
            onPrimaryContainer = NordicSnowText,
            secondary = NordicSnowAccent,
            onSecondary = NordicSnowText,
            secondaryContainer = Color(0xFFDCE6F0),
            onSecondaryContainer = NordicSnowText,
            background = NordicSnowBg,
            onBackground = NordicSnowText,
            surface = NordicSnowSurface,
            onSurface = NordicSnowText,
            surfaceVariant = Color(0xFFDCE6F0),
            onSurfaceVariant = Color(0xFF395066),
            outline = NordicSnowBorder,
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

        AppThemeOption.LEICA_BRASS -> darkColorScheme(
            primary = LeicaBrassAccent,
            onPrimary = Color(0xFF1F1705),
            primaryContainer = LeicaBrassActive,
            onPrimaryContainer = LeicaBrassText,
            secondary = LeicaBrassAccent,
            onSecondary = Color(0xFF1F1705),
            secondaryContainer = Color(0xFF2B271F),
            onSecondaryContainer = LeicaBrassText,
            background = LeicaBrassBg,
            onBackground = LeicaBrassText,
            surface = LeicaBrassSurface,
            onSurface = LeicaBrassText,
            surfaceVariant = Color(0xFF2B271F),
            onSurfaceVariant = LeicaBrassSubtle,
            outline = LeicaBrassBorder,
            error = GodcamRecordRed
        )

        AppThemeOption.TOKYO_NEON -> darkColorScheme(
            primary = TokyoNeonAccent,
            onPrimary = Color(0xFF051F17),
            primaryContainer = TokyoNeonActive,
            onPrimaryContainer = TokyoNeonText,
            secondary = TokyoNeonAccent,
            onSecondary = Color(0xFF051F17),
            secondaryContainer = Color(0xFF1E2742),
            onSecondaryContainer = TokyoNeonText,
            background = TokyoNeonBg,
            onBackground = TokyoNeonText,
            surface = TokyoNeonSurface,
            onSurface = TokyoNeonText,
            surfaceVariant = Color(0xFF1E2742),
            onSurfaceVariant = TokyoNeonSubtle,
            outline = TokyoNeonBorder,
            error = GodcamRecordRed
        )

        AppThemeOption.ESPRESSO_CLUB -> darkColorScheme(
            primary = EspressoClubAccent,
            onPrimary = Color(0xFF241409),
            primaryContainer = EspressoClubActive,
            onPrimaryContainer = EspressoClubText,
            secondary = EspressoClubAccent,
            onSecondary = Color(0xFF241409),
            secondaryContainer = Color(0xFF332721),
            onSecondaryContainer = EspressoClubText,
            background = EspressoClubBg,
            onBackground = EspressoClubText,
            surface = EspressoClubSurface,
            onSurface = EspressoClubText,
            surfaceVariant = Color(0xFF332721),
            onSurfaceVariant = EspressoClubSubtle,
            outline = EspressoClubBorder,
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
