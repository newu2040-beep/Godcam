package com.example.data

import com.example.model.FilterFamily
import com.example.model.FilterPreset

object FilterCatalog {

    val allPresets: List<FilterPreset> by lazy { buildCatalog() }

    fun getById(id: String): FilterPreset {
        return allPresets.firstOrNull { it.id == id } ?: allPresets.first()
    }

    private fun buildCatalog(): List<FilterPreset> {
        val list = mutableListOf<FilterPreset>()

        // 0. Original Neutral Baseline
        list += FilterPreset(
            id = "original",
            name = "Original",
            family = FilterFamily.ALL,
            subtitle = "Unprocessed optical sensor baseline",
            rScale = 1f, gScale = 1f, bScale = 1f,
            saturation = 1f, contrast = 1f, warmth = 0f, fade = 0f
        )

        // 1. Fuji-inspired (11 presets) — Soft greens, airy portraits, cinematic tones
        list += listOf(
            FilterPreset("fuji_pro_400h", "Fuji Pro 400H", FilterFamily.FUJI, "Airy pastel highlights & signature jade greens", 0.98f, 1.05f, 1.03f, 0.94f, 0.96f, -0.05f, 0.08f, -0.06f, 0.06f, 0.05f),
            FilterPreset("fuji_velvia_50", "Velvia Vivid 50", FilterFamily.FUJI, "Deep landscape saturation & punchy micro-contrast", 1.06f, 1.04f, 1.08f, 1.38f, 1.18f, 0.04f, 0.0f, 0.03f, 0.03f, 0.12f),
            FilterPreset("fuji_astia_100f", "Astia Soft 100", FilterFamily.FUJI, "Subtle skin-tone gradation & muted shadows", 1.02f, 1.02f, 1.00f, 0.92f, 0.93f, 0.03f, 0.09f, 0.02f, 0.04f, 0.04f),
            FilterPreset("fuji_classic_chrome", "Classic Chrome", FilterFamily.FUJI, "Documentary muted palette with deep slate skies", 0.97f, 1.01f, 0.95f, 0.78f, 1.14f, -0.03f, 0.07f, -0.04f, 0.08f, 0.14f),
            FilterPreset("fuji_superia_400", "Superia X-TRA", FilterFamily.FUJI, "Everyday fourth-color-layer emerald & magenta balance", 1.01f, 1.06f, 0.99f, 1.08f, 1.06f, -0.02f, 0.05f, -0.08f, 0.12f, 0.10f),
            FilterPreset("fuji_provia_std", "Provia Standard", FilterFamily.FUJI, "True-to-life slide transparency clarity", 1.01f, 1.02f, 1.02f, 1.06f, 1.05f, 0.0f, 0.02f, 0.0f, 0.03f, 0.05f),
            FilterPreset("fuji_eterna_cinema", "Eterna Cinema", FilterFamily.FUJI, "Wide dynamic range look with understated highlights", 0.96f, 1.00f, 1.02f, 0.72f, 0.88f, -0.04f, 0.16f, -0.03f, 0.07f, 0.08f),
            FilterPreset("fuji_classic_neg", "Classic Neg", FilterFamily.FUJI, "High-contrast street tones with warm copper reds", 1.07f, 0.99f, 0.94f, 0.89f, 1.20f, 0.07f, 0.09f, 0.04f, 0.14f, 0.15f),
            FilterPreset("fuji_natura_1600", "Natura 1600", FilterFamily.FUJI, "Low-light ambient warmth & organic high-ISO grain", 1.04f, 1.03f, 0.95f, 0.91f, 0.95f, 0.11f, 0.13f, -0.02f, 0.25f, 0.12f),
            FilterPreset("fuji_nostalgic_neg", "Nostalgic Amber", FilterFamily.FUJI, "Amber highlights & lush shadow separation", 1.09f, 1.02f, 0.90f, 0.96f, 1.06f, 0.16f, 0.08f, 0.03f, 0.10f, 0.11f),
            FilterPreset("fuji_reala_ace", "Reala Ace", FilterFamily.FUJI, "Crisp neutral fidelity with delicate highlight roll-off", 1.00f, 1.03f, 1.01f, 0.95f, 1.08f, -0.01f, 0.04f, -0.02f, 0.05f, 0.06f)
        )

        // 2. Kodak-inspired (11 presets) — Warm highlights, nostalgic color, rich skin tones
        list += listOf(
            FilterPreset("kodak_gold_200", "Kodak Gold", FilterFamily.KODAK, "Golden-hour sunshine warmth & honey highlights", 1.10f, 1.04f, 0.89f, 1.12f, 1.07f, 0.22f, 0.06f, 0.04f, 0.10f, 0.10f),
            FilterPreset("kodak_portra_400", "Portra 400", FilterFamily.KODAK, "Velvet skin tones, warm midtones & creamy highlights", 1.06f, 1.02f, 0.95f, 0.96f, 0.97f, 0.13f, 0.08f, 0.03f, 0.08f, 0.07f),
            FilterPreset("kodak_portra_160", "Portra 160 Studio", FilterFamily.KODAK, "Ultra-fine grain with gentle portrait contrast", 1.04f, 1.01f, 0.97f, 0.90f, 0.94f, 0.09f, 0.09f, 0.02f, 0.04f, 0.05f),
            FilterPreset("kodak_portra_800", "Portra 800 Dusk", FilterFamily.KODAK, "Vibrant twilight saturation & luminous grain", 1.09f, 1.03f, 0.93f, 1.14f, 1.08f, 0.18f, 0.07f, 0.05f, 0.18f, 0.14f),
            FilterPreset("kodak_ektar_100", "Ektar 100", FilterFamily.KODAK, "Ultra-vivid travel color & deep primary reds/blues", 1.11f, 1.01f, 1.04f, 1.32f, 1.16f, 0.08f, 0.01f, 0.05f, 0.04f, 0.10f),
            FilterPreset("kodak_ultramax_400", "ColorPlus 200", FilterFamily.KODAK, "Nostalgic ochre tones & soft vintage contrast", 1.12f, 1.04f, 0.87f, 1.04f, 1.03f, 0.25f, 0.11f, 0.02f, 0.14f, 0.12f),
            FilterPreset("kodak_proimage_100", "ProImage 100", FilterFamily.KODAK, "Clean daylight warmth with balanced neutral shadows", 1.05f, 1.02f, 0.96f, 1.02f, 1.02f, 0.11f, 0.05f, 0.01f, 0.06f, 0.06f),
            FilterPreset("kodak_ektachrome_e100", "Ektachrome E100", FilterFamily.KODAK, "Crisp slide blues & clean neutral whites", 0.98f, 1.02f, 1.08f, 1.10f, 1.12f, -0.06f, 0.02f, 0.01f, 0.05f, 0.08f),
            FilterPreset("kodak_kodachrome_64", "Archive Chrome 64", FilterFamily.KODAK, "Iconic mid-century editorial warmth & deep blacks", 1.08f, 1.00f, 0.91f, 1.08f, 1.22f, 0.16f, 0.04f, 0.04f, 0.11f, 0.16f),
            FilterPreset("kodak_vision3_250d", "Vision3 250D", FilterFamily.KODAK, "Daylight motion-picture negative with halation warmth", 1.05f, 1.03f, 0.96f, 0.95f, 0.96f, 0.10f, 0.10f, 0.02f, 0.12f, 0.09f),
            FilterPreset("kodak_royal_gold", "Royal Supra", FilterFamily.KODAK, "Luminous golden skin & crisp autumn foliage", 1.11f, 1.05f, 0.90f, 1.16f, 1.09f, 0.20f, 0.04f, 0.03f, 0.09f, 0.10f)
        )

        // 3. Retro / Vintage (11 presets) — Faded color, analog warmth, optional edge effects
        list += listOf(
            FilterPreset("retro_agfa_vista", "Agfa Vista", FilterFamily.RETRO, "Punchycherry reds & warm vintage street character", 1.09f, 1.01f, 0.92f, 1.10f, 1.09f, 0.14f, 0.12f, 0.06f, 0.15f, 0.16f),
            FilterPreset("retro_classic", "Retro", FilterFamily.RETRO, "Lifted matte blacks & sun-faded 1970s postcard warmth", 1.08f, 1.02f, 0.86f, 0.82f, 0.90f, 0.24f, 0.24f, 0.03f, 0.20f, 0.24f),
            FilterPreset("retro_vintage", "Vintage", FilterFamily.RETRO, "Warm sepia-tinted highlights & soft corner vignette", 1.12f, 1.03f, 0.84f, 0.76f, 0.92f, 0.28f, 0.22f, 0.05f, 0.22f, 0.28f),
            FilterPreset("retro_1974_summer", "1974 Summer", FilterFamily.RETRO, "Golden haze, low contrast & analog dust texture", 1.14f, 1.05f, 0.82f, 0.85f, 0.88f, 0.30f, 0.26f, 0.02f, 0.24f, 0.22f),
            FilterPreset("retro_faded_memory", "Faded Memory", FilterFamily.RETRO, "Washed pastel tones with creamy shadow lift", 1.04f, 1.02f, 0.94f, 0.68f, 0.85f, 0.15f, 0.30f, 0.01f, 0.18f, 0.18f),
            FilterPreset("retro_optik_amber", "Optik Amber", FilterFamily.RETRO, "Light-leak inspired warm edge glow & rich bronze", 1.16f, 1.01f, 0.85f, 0.95f, 1.04f, 0.32f, 0.16f, 0.08f, 0.19f, 0.26f),
            FilterPreset("retro_thrift_35", "Thrift 35mm", FilterFamily.RETRO, "Expired film roll with olive shadows & warm grain", 1.03f, 1.07f, 0.88f, 0.84f, 0.94f, 0.18f, 0.20f, -0.07f, 0.28f, 0.25f),
            FilterPreset("retro_helio_warm", "Helios Swirl", FilterFamily.RETRO, "Soviet vintage glass warmth & strong edge falloff", 1.09f, 1.04f, 0.89f, 0.90f, 0.98f, 0.20f, 0.15f, 0.02f, 0.16f, 0.36f),
            FilterPreset("retro_vinyl_sleeve", "Vinyl Sleeve", FilterFamily.RETRO, "Muted mustard & teal mid-century record aesthetic", 1.06f, 1.05f, 0.91f, 0.80f, 1.02f, 0.17f, 0.18f, -0.04f, 0.21f, 0.20f),
            FilterPreset("retro_polar_sun", "Solarized Fade", FilterFamily.RETRO, "Soft highlight compression with warm desert dust", 1.11f, 1.02f, 0.88f, 0.78f, 0.87f, 0.25f, 0.28f, 0.04f, 0.22f, 0.20f),
            FilterPreset("retro_super8_frame", "Super 8 Reel", FilterFamily.RETRO, "Home-movie tungsten warmth & pronounced celluloid grain", 1.15f, 1.03f, 0.83f, 0.88f, 1.06f, 0.29f, 0.19f, 0.05f, 0.32f, 0.30f)
        )

        // 4. Cinematic (11 presets) — Muted palettes and filmic contrast
        list += listOf(
            FilterPreset("cine_still_800t", "CineStill", FilterFamily.CINEMATIC, "Tungsten motion-picture stock with cool shadows & halation", 0.95f, 1.02f, 1.12f, 0.96f, 1.12f, -0.14f, 0.08f, 0.04f, 0.14f, 0.16f),
            FilterPreset("cine_teal_orange", "Anamorphic Gold", FilterFamily.CINEMATIC, "Complementary cyan shadows & warm skin highlights", 1.07f, 1.01f, 1.04f, 1.05f, 1.16f, 0.08f, 0.06f, -0.04f, 0.09f, 0.18f),
            FilterPreset("cine_silver_screen", "Silver Nitrate", FilterFamily.CINEMATIC, "Bleach-bypass desaturated grit & metallic speculars", 0.98f, 1.00f, 1.02f, 0.54f, 1.28f, -0.04f, 0.05f, 0.0f, 0.18f, 0.22f),
            FilterPreset("cine_nordic_drama", "Fjord Slate", FilterFamily.CINEMATIC, "Cool Scandinavian overcast blues & deep charcoal", 0.92f, 0.99f, 1.09f, 0.74f, 1.10f, -0.18f, 0.11f, -0.03f, 0.10f, 0.16f),
            FilterPreset("cine_desert_dune", "Arrakis Ochre", FilterFamily.CINEMATIC, "Epic golden sand palette with controlled sky luminance", 1.12f, 1.04f, 0.86f, 0.88f, 1.14f, 0.24f, 0.09f, 0.02f, 0.11f, 0.18f),
            FilterPreset("cine_emerald_matrix", "Neo Emerald", FilterFamily.CINEMATIC, "Subtle cyber-green midtone shift & crisp black floor", 0.91f, 1.08f, 0.94f, 0.82f, 1.18f, -0.08f, 0.07f, -0.14f, 0.13f, 0.20f),
            FilterPreset("cine_auteur_warm", "Parisian New Wave", FilterFamily.CINEMATIC, "Warm bistrot tungsten with poetic shadow roll-off", 1.08f, 1.01f, 0.92f, 0.86f, 1.04f, 0.16f, 0.14f, 0.04f, 0.15f, 0.18f),
            FilterPreset("cine_rainy_kowloon", "Kowloon Express", FilterFamily.CINEMATIC, "Lush emerald-amber neon atmosphere & step-printed mood", 1.04f, 1.07f, 0.92f, 1.10f, 1.15f, 0.09f, 0.08f, -0.09f, 0.17f, 0.24f),
            FilterPreset("cine_500t_stage", "Studio 500T", FilterFamily.CINEMATIC, "Balanced soundstage color with smooth highlight latitude", 0.97f, 1.01f, 1.06f, 0.90f, 0.96f, -0.07f, 0.12f, 0.01f, 0.11f, 0.12f),
            FilterPreset("cine_grand_hotel", "Symmetry Pastel", FilterFamily.CINEMATIC, "Whimsical storybook rose, cream & mint palette", 1.08f, 1.03f, 1.01f, 1.08f, 0.94f, 0.12f, 0.13f, 0.08f, 0.06f, 0.08f),
            FilterPreset("cine_midnight_express", "Nocturne 35", FilterFamily.CINEMATIC, "Deep sapphire shadows & clean streetlamp highlights", 0.91f, 0.97f, 1.14f, 0.85f, 1.16f, -0.19f, 0.06f, 0.02f, 0.16f, 0.22f)
        )

        // 5. Monochrome (11 presets) — Classic B&W, silver, noir, high contrast
        list += listOf(
            FilterPreset("bw_classic", "B&W", FilterFamily.MONOCHROME, "Balanced panchromatic black & white with full tonal range", 1.0f, 1.0f, 1.0f, 0.0f, 1.10f, 0.0f, 0.03f, 0.0f, 0.10f, 0.10f),
            FilterPreset("bw_tri_x_400", "Tri-X 400", FilterFamily.MONOCHROME, "Classic photojournalism grit, deep blacks & silver grain", 1.02f, 1.02f, 1.02f, 0.0f, 1.28f, 0.0f, 0.04f, 0.0f, 0.26f, 0.18f),
            FilterPreset("bw_ilford_hp5", "HP5 Plus", FilterFamily.MONOCHROME, "Wide latitude documentary grey scale with soft shadows", 1.0f, 1.0f, 1.0f, 0.0f, 1.04f, 0.0f, 0.12f, 0.0f, 0.18f, 0.10f),
            FilterPreset("bw_acros_100", "Acros Fine 100", FilterFamily.MONOCHROME, "Ultra-smooth architectural monochrome & deep sky rendering", 0.99f, 1.01f, 1.01f, 0.0f, 1.18f, -0.02f, 0.01f, 0.0f, 0.05f, 0.08f),
            FilterPreset("bw_noir_detective", "Cinema Noir", FilterFamily.MONOCHROME, "Crushed shadows, blinding key light & dramatic vignette", 1.04f, 1.04f, 1.04f, 0.0f, 1.42f, 0.0f, 0.0f, 0.0f, 0.22f, 0.34f),
            FilterPreset("bw_selenium_plate", "Selenium Tone", FilterFamily.MONOCHROME, "Cool gallery fiber-print silver with subtle steel tint", 0.96f, 0.99f, 1.05f, 0.08f, 1.15f, -0.08f, 0.05f, 0.0f, 0.09f, 0.12f),
            FilterPreset("bw_warm_platinum", "Platinum Palladium", FilterFamily.MONOCHROME, "Warm museum darkroom print with rich paper whites", 1.06f, 1.02f, 0.93f, 0.10f, 1.08f, 0.12f, 0.08f, 0.0f, 0.08f, 0.10f),
            FilterPreset("bw_red_filter", "Red Filter Sky", FilterFamily.MONOCHROME, "Dramatic dark skies & luminous cumulus clouds", 1.12f, 0.95f, 0.88f, 0.0f, 1.32f, 0.0f, 0.02f, 0.0f, 0.14f, 0.16f),
            FilterPreset("bw_yellow_portrait", "Yellow Filter Skin", FilterFamily.MONOCHROME, "Flattering monochrome skin luminance & natural separation", 1.05f, 1.02f, 0.92f, 0.0f, 1.12f, 0.02f, 0.06f, 0.0f, 0.10f, 0.09f),
            FilterPreset("bw_pushed_3200", "Delta 3200 Push", FilterFamily.MONOCHROME, "Concert & street night monochrome with bold grain structure", 1.03f, 1.03f, 1.03f, 0.0f, 1.24f, 0.0f, 0.10f, 0.0f, 0.38f, 0.20f),
            FilterPreset("bw_chalk_matte", "Silky Matte B&W", FilterFamily.MONOCHROME, "Low-contrast charcoal sketch feel with lifted blacks", 0.98f, 0.98f, 0.98f, 0.0f, 0.88f, 0.0f, 0.24f, 0.0f, 0.12f, 0.08f)
        )

        // 6. Instant-film inspired (11 presets) — Soft contrast, warm whites, nostalgic rendering
        list += listOf(
            FilterPreset("inst_sx70_blend", "SX-70 Classic", FilterFamily.INSTANT, "Creamy instant dye diffusion with warm ivory highlights", 1.08f, 1.03f, 0.91f, 0.86f, 0.92f, 0.18f, 0.18f, 0.03f, 0.12f, 0.18f),
            FilterPreset("inst_600_cool", "Instant 600", FilterFamily.INSTANT, "Nostalgic cyan-green shadows & soft flash highlights", 0.97f, 1.05f, 1.04f, 0.88f, 0.95f, -0.04f, 0.16f, -0.06f, 0.11f, 0.15f),
            FilterPreset("inst_mini_daylight", "Mini Daylight", FilterFamily.INSTANT, "Bright airy instant prints with rosy cheek warmth", 1.05f, 1.02f, 1.00f, 0.96f, 0.94f, 0.08f, 0.12f, 0.05f, 0.07f, 0.10f),
            FilterPreset("inst_peel_apart_100", "FP-100C Peel", FilterFamily.INSTANT, "Legendary pack-film fidelity with lush greens & creams", 1.02f, 1.06f, 0.98f, 1.02f, 1.03f, 0.05f, 0.09f, -0.03f, 0.08f, 0.12f),
            FilterPreset("inst_spectra_wide", "Spectra Wide", FilterFamily.INSTANT, "Road-trip wide instant look with faded sky blues", 1.03f, 1.03f, 1.06f, 0.84f, 0.91f, 0.04f, 0.20f, -0.02f, 0.14f, 0.16f),
            FilterPreset("inst_expired_2004", "Expired Pack '04", FilterFamily.INSTANT, "Warm magenta-amber chemical shift & dreamy softness", 1.13f, 0.98f, 0.92f, 0.78f, 0.89f, 0.24f, 0.22f, 0.12f, 0.18f, 0.22f),
            FilterPreset("inst_party_flash", "Direct Flash 90s", FilterFamily.INSTANT, "Punchy foreground clarity with warm background falloff", 1.06f, 1.04f, 0.96f, 1.04f, 1.12f, 0.09f, 0.07f, 0.02f, 0.13f, 0.28f),
            FilterPreset("inst_ivory_frame", "Warm Ivory", FilterFamily.INSTANT, "Soft gallery instant tone with gentle highlight compression", 1.07f, 1.04f, 0.93f, 0.82f, 0.90f, 0.15f, 0.17f, 0.01f, 0.09f, 0.12f),
            FilterPreset("inst_mint_pocket", "Pocket Mint", FilterFamily.INSTANT, "GODCAM signature cream-mint instant emulsion", 0.98f, 1.07f, 1.01f, 0.90f, 0.96f, -0.02f, 0.14f, -0.08f, 0.08f, 0.10f),
            FilterPreset("inst_sepia_transfer", "Emulsion Lift", FilterFamily.INSTANT, "Hand-transferred watercolor warmth & organic vignette", 1.14f, 1.04f, 0.85f, 0.65f, 0.92f, 0.28f, 0.21f, 0.04f, 0.19f, 0.25f),
            FilterPreset("inst_mono_instant", "Instant Silver", FilterFamily.INSTANT, "Coaterless B&W instant print with warm creamy highlights", 1.05f, 1.02f, 0.95f, 0.08f, 1.06f, 0.10f, 0.15f, 0.0f, 0.14f, 0.16f)
        )

        // 7. Nature (11 presets) — Foliage, landscape, ocean and sky
        list += listOf(
            FilterPreset("nat_alpine_lake", "Alpine Lake", FilterFamily.NATURE, "Crystalline glacier blues & sunlit mountain warmth", 1.02f, 1.05f, 1.08f, 1.16f, 1.09f, 0.03f, 0.02f, -0.03f, 0.04f, 0.08f),
            FilterPreset("nat_redwood_mist", "Redwood Mist", FilterFamily.NATURE, "Deep moss greens, bark browns & atmospheric fog lift", 0.99f, 1.08f, 0.95f, 1.04f, 0.98f, 0.05f, 0.11f, -0.07f, 0.07f, 0.14f),
            FilterPreset("nat_pacific_swell", "Pacific Swell", FilterFamily.NATURE, "Rich aquamarine water & crisp sea-foam highlights", 0.94f, 1.06f, 1.11f, 1.14f, 1.07f, -0.10f, 0.04f, -0.05f, 0.05f, 0.09f),
            FilterPreset("nat_golden_meadow", "Golden Meadow", FilterFamily.NATURE, "Late-afternoon pastoral gold & warm chlorophyll greens", 1.10f, 1.07f, 0.89f, 1.12f, 1.05f, 0.21f, 0.05f, -0.02f, 0.06f, 0.08f),
            FilterPreset("nat_canyon_ochre", "Red Rock Canyon", FilterFamily.NATURE, "Sandstone terracotta vibrancy against deep cobalt skies", 1.13f, 1.01f, 0.96f, 1.24f, 1.14f, 0.18f, 0.02f, 0.05f, 0.05f, 0.10f),
            FilterPreset("nat_nordic_pine", "Nordic Pine", FilterFamily.NATURE, "Subdued evergreen foliage & quiet overcast tranquility", 0.95f, 1.04f, 0.98f, 0.78f, 1.04f, -0.06f, 0.10f, -0.06f, 0.08f, 0.15f),
            FilterPreset("nat_tropical_lagoon", "Emerald Lagoon", FilterFamily.NATURE, "Lush botanical greens & turquoise reef clarity", 0.96f, 1.10f, 1.06f, 1.22f, 1.08f, -0.02f, 0.02f, -0.08f, 0.03f, 0.06f),
            FilterPreset("nat_autumn_maple", "Kyoto Maple", FilterFamily.NATURE, "Crimson & amber foliage with delicate shadow detail", 1.12f, 1.00f, 0.91f, 1.18f, 1.08f, 0.19f, 0.04f, 0.06f, 0.06f, 0.10f),
            FilterPreset("nat_glacier_blue", "Glacier Ice", FilterFamily.NATURE, "Pure high-altitude snow whites & cerulean crevasse shadows", 0.93f, 1.01f, 1.12f, 1.05f, 1.11f, -0.15f, 0.01f, 0.0f, 0.04f, 0.07f),
            FilterPreset("nat_savanna_sun", "Savanna Horizon", FilterFamily.NATURE, "Warm dry-season grasses & dramatic sunset gradation", 1.11f, 1.04f, 0.87f, 1.08f, 1.10f, 0.23f, 0.06f, 0.03f, 0.08f, 0.12f),
            FilterPreset("nat_botanical_dew", "Botanical Dew", FilterFamily.NATURE, "Macro petal delicacy with soft sage-mint greens", 0.98f, 1.06f, 1.00f, 0.96f, 0.95f, 0.01f, 0.08f, -0.05f, 0.04f, 0.07f)
        )

        // 8. Portrait (11 presets) — Skin-friendly color and controlled highlights
        list += listOf(
            FilterPreset("port_golden_hour", "Golden Portrait", FilterFamily.PORTRAIT, "Luminous skin warmth, gentle contrast & airy hair-light", 1.06f, 1.02f, 0.94f, 0.96f, 0.95f, 0.14f, 0.07f, 0.03f, 0.05f, 0.07f),
            FilterPreset("port_editorial_cream", "Editorial Cream", FilterFamily.PORTRAIT, "Magazine-grade neutral skin tones & warm cream backdrops", 1.03f, 1.01f, 0.97f, 0.90f, 0.97f, 0.08f, 0.08f, 0.02f, 0.04f, 0.06f),
            FilterPreset("port_peach_silk", "Peach Silk", FilterFamily.PORTRAIT, "Soft rose-peach complexion & smooth highlight roll-off", 1.07f, 1.01f, 0.98f, 0.94f, 0.92f, 0.10f, 0.10f, 0.06f, 0.04f, 0.05f),
            FilterPreset("port_window_light", "North Window", FilterFamily.PORTRAIT, "Vermeer-inspired natural directional light & quiet shadows", 1.01f, 1.02f, 1.02f, 0.88f, 1.04f, 0.02f, 0.06f, 0.0f, 0.06f, 0.12f),
            FilterPreset("port_melanin_glow", "Rich Bronze", FilterFamily.PORTRAIT, "Deep skin-tone luminance without ashy shadow cast", 1.08f, 1.03f, 0.93f, 1.04f, 1.02f, 0.17f, 0.04f, 0.03f, 0.05f, 0.08f),
            FilterPreset("port_porcelain_air", "Airy Daylight", FilterFamily.PORTRAIT, "Bright open shadows & delicate cool-neutral highlights", 1.00f, 1.03f, 1.04f, 0.92f, 0.91f, -0.02f, 0.11f, 0.01f, 0.03f, 0.04f),
            FilterPreset("port_cafe_warmth", "Café Terrace", FilterFamily.PORTRAIT, "Cozy afternoon espresso tones & flattering midtone contrast", 1.08f, 1.02f, 0.91f, 0.92f, 1.01f, 0.18f, 0.09f, 0.03f, 0.08f, 0.11f),
            FilterPreset("port_studio_strobe", "Beauty Dish", FilterFamily.PORTRAIT, "Crisp catchlights, clean whites & polished micro-contrast", 1.02f, 1.02f, 1.02f, 0.96f, 1.10f, 0.02f, 0.02f, 0.01f, 0.03f, 0.08f),
            FilterPreset("port_velvet_rose", "Velvet Rose", FilterFamily.PORTRAIT, "Romantic warm magenta-gold undertones & dreamy edges", 1.09f, 1.00f, 0.96f, 0.95f, 0.94f, 0.15f, 0.10f, 0.08f, 0.06f, 0.14f),
            FilterPreset("port_monochrome_soul", "Soul B&W Portrait", FilterFamily.PORTRAIT, "Intimate portrait monochrome with smooth skin transitions", 1.04f, 1.02f, 0.95f, 0.0f, 1.08f, 0.04f, 0.07f, 0.0f, 0.09f, 0.14f),
            FilterPreset("port_linen_minimal", "Natural Linen", FilterFamily.PORTRAIT, "Organic oatmeal neutrals & honest unforced skin color", 1.03f, 1.02f, 0.96f, 0.86f, 0.95f, 0.07f, 0.09f, 0.0f, 0.05f, 0.06f)
        )

        // 9. Night / Neon (10 presets) — City glow and restrained saturation
        list += listOf(
            FilterPreset("night_tokyo_neon", "Tokyo Neon", FilterFamily.NIGHT, "Electric cyan & magenta signage with restrained shadow noise", 0.96f, 1.03f, 1.14f, 1.18f, 1.16f, -0.10f, 0.04f, 0.08f, 0.10f, 0.18f),
            FilterPreset("night_sodium_vapor", "Sodium Street", FilterFamily.NIGHT, "Warm amber urban streetlights & deep midnight blacks", 1.14f, 1.02f, 0.86f, 0.96f, 1.14f, 0.24f, 0.05f, 0.03f, 0.12f, 0.20f),
            FilterPreset("night_blue_hour", "Blue Hour Dusk", FilterFamily.NIGHT, "Velvet twilight cobalt skies & glowing window warmth", 0.93f, 1.00f, 1.15f, 1.10f, 1.09f, -0.16f, 0.04f, 0.02f, 0.07f, 0.14f),
            FilterPreset("night_cyber_rain", "Wet Asphalt", FilterFamily.NIGHT, "Specular street reflections in teal, violet & coral", 0.98f, 1.04f, 1.12f, 1.14f, 1.20f, -0.06f, 0.03f, 0.06f, 0.11f, 0.22f),
            FilterPreset("night_jazz_club", "Velvet Lounge", FilterFamily.NIGHT, "Dim candlelit burgundy & brass low-light warmth", 1.12f, 0.98f, 0.90f, 0.90f, 1.08f, 0.20f, 0.10f, 0.09f, 0.16f, 0.26f),
            FilterPreset("night_astro_clear", "Starlight Clear", FilterFamily.NIGHT, "High micro-contrast night sky with suppressed light pollution", 0.92f, 0.98f, 1.10f, 0.92f, 1.22f, -0.14f, 0.0f, -0.02f, 0.05f, 0.10f),
            FilterPreset("night_diner_window", "Late Diner", FilterFamily.NIGHT, "Hopper-inspired emerald interior fluorescent & warm skin", 0.96f, 1.09f, 0.97f, 0.98f, 1.12f, 0.02f, 0.07f, -0.10f, 0.13f, 0.18f),
            FilterPreset("night_harbor_fog", "Harbor Beacon", FilterFamily.NIGHT, "Misty maritime slate with soft halation around lights", 0.94f, 1.02f, 1.08f, 0.78f, 0.96f, -0.10f, 0.15f, -0.03f, 0.12f, 0.16f),
            FilterPreset("night_violet_skyline", "Metropolis Dusk", FilterFamily.NIGHT, "Sunset-to-night gradient in plum,rose & indigo", 1.06f, 0.97f, 1.11f, 1.12f, 1.12f, 0.02f, 0.05f, 0.12f, 0.08f, 0.15f),
            FilterPreset("night_low_iso_clean", "Clean Night NR", FilterFamily.NIGHT, "Balanced low-light clarity with neutral white balance", 0.99f, 1.01f, 1.04f, 0.94f, 1.06f, -0.04f, 0.03f, 0.0f, 0.02f, 0.08f)
        )

        // 10. Creative (10 presets) — Duotone, cross-process-inspired experiments
        list += listOf(
            FilterPreset("crea_xpro_emerald", "Cross-Process E6", FilterFamily.CREATIVE, "Slide film processed in C-41: emerald greens & yellow highs", 1.06f, 1.12f, 0.86f, 1.22f, 1.24f, 0.15f, 0.05f, -0.14f, 0.14f, 0.20f),
            FilterPreset("crea_aerochrome_ir", "Infrared Aero", FilterFamily.CREATIVE, "Surreal coral-pink foliage & dramatic cyan skies", 1.22f, 0.86f, 1.04f, 1.26f, 1.16f, 0.12f, 0.03f, 0.24f, 0.10f, 0.14f),
            FilterPreset("crea_cyanotype_print", "Prussian Cyanotype", FilterFamily.CREATIVE, "19th-century botanical blueprint duotone", 0.78f, 0.98f, 1.24f, 0.35f, 1.14f, -0.32f, 0.08f, -0.05f, 0.09f, 0.16f),
            FilterPreset("crea_lomo_purple", "Violet Shift", FilterFamily.CREATIVE, "Experimental color-shifted negative in amethyst & teal", 1.10f, 0.90f, 1.16f, 1.15f, 1.12f, -0.04f, 0.06f, 0.22f, 0.15f, 0.20f),
            FilterPreset("crea_redscale_sun", "Redscale XR", FilterFamily.CREATIVE, "Reverse-wound film look in fiery copper, red & gold", 1.28f, 0.92f, 0.72f, 1.10f, 1.14f, 0.42f, 0.08f, 0.10f, 0.18f, 0.24f),
            FilterPreset("crea_mint_duotone", "GODCAM Mint Duo", FilterFamily.CREATIVE, "Editorial forest-charcoal shadows & warm cream-mint highs", 0.94f, 1.09f, 1.01f, 0.48f, 1.10f, -0.04f, 0.12f, -0.12f, 0.07f, 0.12f),
            FilterPreset("crea_bleach_copper", "Copper Bleach", FilterFamily.CREATIVE, "Silver retention with warm metallic bronze highlights", 1.10f, 1.01f, 0.88f, 0.42f, 1.30f, 0.20f, 0.04f, 0.02f, 0.16f, 0.22f),
            FilterPreset("crea_pastel_dream", "Cotton Candy", FilterFamily.CREATIVE, "Dreamy lavender-rose highlights & lifted powder blues", 1.07f, 1.01f, 1.10f, 0.92f, 0.86f, 0.04f, 0.22f, 0.14f, 0.05f, 0.08f),
            FilterPreset("crea_split_sage_gold", "Sage & Terracotta", FilterFamily.CREATIVE, "Architectural split-tone in muted sage and warm clay", 1.08f, 1.05f, 0.91f, 0.84f, 1.08f, 0.16f, 0.10f, -0.06f, 0.09f, 0.14f),
            FilterPreset("crea_halide_high_key", "High-Key Studio", FilterFamily.CREATIVE, "Luminous gallery overexposure with crisp dark accents", 1.04f, 1.04f, 1.04f, 0.75f, 0.92f, 0.05f, 0.26f, 0.0f, 0.06f, 0.04f)
        )

        return list
    }
}
