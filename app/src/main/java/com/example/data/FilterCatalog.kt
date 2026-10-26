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

        // 1. Pastel & Airy (18 NEW presets) — Soft pastel tones, creamy highlights, dreamy editorial color
        list += listOf(
            FilterPreset("pastel_matcha_latte", "Matcha Cream", FilterFamily.PASTEL, "Velvety Kyoto green-tea highlights & creamy matte shadows", 0.96f, 1.07f, 0.99f, 0.88f, 0.91f, 0.03f, 0.16f, -0.07f, 0.04f, 0.05f),
            FilterPreset("pastel_peach_sorbet", "Peach Sorbet", FilterFamily.PASTEL, "Soft apricot-blush highlights & luminous skin tones", 1.08f, 1.02f, 0.95f, 0.92f, 0.90f, 0.14f, 0.17f, 0.05f, 0.04f, 0.06f),
            FilterPreset("pastel_sakura_petal", "Sakura Blossom", FilterFamily.PASTEL, "Spring cherry-blossom pink tint & airy white balance", 1.07f, 1.00f, 1.04f, 0.90f, 0.89f, 0.06f, 0.19f, 0.09f, 0.03f, 0.05f),
            FilterPreset("pastel_butter_croissant", "Buttercream", FilterFamily.PASTEL, "Warm French-bakery yellow pastel & soft shadow roll-off", 1.09f, 1.06f, 0.90f, 0.86f, 0.92f, 0.19f, 0.18f, 0.01f, 0.05f, 0.06f),
            FilterPreset("pastel_powder_sky", "Powder Sky", FilterFamily.PASTEL, "Serene baby-blue skies & lifted porcelain shadows", 0.95f, 1.02f, 1.09f, 0.89f, 0.91f, -0.09f, 0.16f, -0.02f, 0.03f, 0.05f),
            FilterPreset("pastel_wisteria_haze", "Wisteria Haze", FilterFamily.PASTEL, "Dreamy lilac-violet midtones & soft silver highlights", 1.04f, 0.99f, 1.09f, 0.87f, 0.90f, -0.03f, 0.18f, 0.10f, 0.04f, 0.06f),
            FilterPreset("pastel_pistachio_gelato", "Pistachio Gelato", FilterFamily.PASTEL, "Italian pastel mint & warm vanilla cream highlights", 0.97f, 1.08f, 1.01f, 0.91f, 0.92f, 0.02f, 0.15f, -0.08f, 0.04f, 0.05f),
            FilterPreset("pastel_rose_milk", "Rose Milk Tea", FilterFamily.PASTEL, "Muted dusty-rose warmth & silky low-contrast shadows", 1.08f, 1.01f, 0.98f, 0.84f, 0.88f, 0.11f, 0.20f, 0.07f, 0.05f, 0.07f),
            FilterPreset("pastel_lemon_meringue", "Lemon Meringue", FilterFamily.PASTEL, "Sun-kissed citrus pastel with crisp white highlights", 1.07f, 1.07f, 0.91f, 0.93f, 0.94f, 0.16f, 0.14f, -0.02f, 0.04f, 0.05f),
            FilterPreset("pastel_periwinkle_cloud", "Periwinkle Cloud", FilterFamily.PASTEL, "Cool lavender-blue twilight pastel & feather-soft contrast", 0.97f, 1.01f, 1.11f, 0.88f, 0.89f, -0.08f, 0.19f, 0.06f, 0.03f, 0.05f),
            FilterPreset("pastel_mint_macaron", "Mint Macaron", FilterFamily.PASTEL, "Parisian patisserie seafoam & warm almond midtones", 0.95f, 1.07f, 1.04f, 0.90f, 0.93f, -0.03f, 0.15f, -0.06f, 0.04f, 0.06f),
            FilterPreset("pastel_coral_blush", "Coral Blush", FilterFamily.PASTEL, "Warm flamingo coral highlights & airy beach shadows", 1.10f, 1.01f, 0.94f, 0.95f, 0.93f, 0.15f, 0.15f, 0.06f, 0.04f, 0.06f),
            FilterPreset("pastel_oat_milk", "Oat Milk Minimal", FilterFamily.PASTEL, "Clean Scandinavian beige-cream neutral pastel", 1.04f, 1.03f, 0.96f, 0.78f, 0.92f, 0.09f, 0.17f, 0.01f, 0.04f, 0.04f),
            FilterPreset("pastel_sea_glass", "Sea Glass", FilterFamily.PASTEL, "Frosted aqua-emerald coastal pastel with soft grain", 0.94f, 1.06f, 1.06f, 0.86f, 0.91f, -0.06f, 0.17f, -0.05f, 0.06f, 0.06f),
            FilterPreset("pastel_vanilla_sky", "Vanilla Bean", FilterFamily.PASTEL, "Luminous ivory highlights & gentle warm-gray shadows", 1.05f, 1.04f, 0.95f, 0.82f, 0.90f, 0.12f, 0.18f, 0.02f, 0.04f, 0.05f),
            FilterPreset("pastel_lilac_sorbet", "Lilac Sorbet", FilterFamily.PASTEL, "Pastel orchid highlights & cool porcelain shadows", 1.05f, 0.98f, 1.08f, 0.91f, 0.91f, -0.01f, 0.17f, 0.11f, 0.04f, 0.05f),
            FilterPreset("pastel_honeydew_mist", "Honeydew Mist", FilterFamily.PASTEL, "Fresh melon-green pastel with airy highlight roll-off", 0.98f, 1.08f, 0.97f, 0.89f, 0.91f, 0.04f, 0.16f, -0.07f, 0.04f, 0.05f),
            FilterPreset("pastel_chamomile_sun", "Chamomile Tea", FilterFamily.PASTEL, "Soothing golden-ivory pastel & soft botanical contrast", 1.08f, 1.05f, 0.92f, 0.85f, 0.91f, 0.17f, 0.16f, 0.01f, 0.05f, 0.06f)
        )

        // 2. Fuji-inspired (15 presets: 11 classic + 4 new) — Soft greens, airy portraits, cinematic tones
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
            FilterPreset("fuji_reala_ace", "Reala Ace", FilterFamily.FUJI, "Crisp neutral fidelity with delicate highlight roll-off", 1.00f, 1.03f, 1.01f, 0.95f, 1.08f, -0.01f, 0.04f, -0.02f, 0.05f, 0.06f),
            FilterPreset("fuji_eterna_bleach", "Eterna Bleach Bypass", FilterFamily.FUJI, "High-contrast silver-retained cinema look with muted saturation", 0.97f, 1.00f, 0.98f, 0.58f, 1.26f, -0.03f, 0.06f, -0.02f, 0.14f, 0.16f),
            FilterPreset("fuji_pro_160ns", "Pro 160NS Studio", FilterFamily.FUJI, "Daylight portrait stock with ultra-smooth pastel skin tones", 1.01f, 1.04f, 1.02f, 0.90f, 0.93f, -0.01f, 0.10f, -0.03f, 0.04f, 0.05f),
            FilterPreset("fuji_superia_1600", "Superia Venus 800", FilterFamily.FUJI, "Vibrant twilight street color & emerald shadow fidelity", 1.03f, 1.07f, 1.00f, 1.12f, 1.09f, 0.02f, 0.07f, -0.06f, 0.19f, 0.13f),
            FilterPreset("fuji_sensia_100", "Sensia Slide 100", FilterFamily.FUJI, "Warm consumer slide transparency with soft highlight glow", 1.04f, 1.03f, 0.98f, 1.06f, 1.07f, 0.08f, 0.04f, 0.01f, 0.05f, 0.07f)
        )

        // 3. Kodak-inspired (15 presets: 11 classic + 4 new) — Warm highlights, nostalgic color, rich skin tones
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
            FilterPreset("kodak_royal_gold", "Royal Supra", FilterFamily.KODAK, "Luminous golden skin & crisp autumn foliage", 1.11f, 1.05f, 0.90f, 1.16f, 1.09f, 0.20f, 0.04f, 0.03f, 0.09f, 0.10f),
            FilterPreset("kodak_ultramax_vivid", "UltraMax 400", FilterFamily.KODAK, "Punchy everyday consumer film with bold primary yellows & blues", 1.09f, 1.03f, 0.96f, 1.20f, 1.11f, 0.14f, 0.05f, 0.03f, 0.13f, 0.10f),
            FilterPreset("kodak_vision3_50d", "Vision3 50D", FilterFamily.KODAK, "Ultra-fine daylight cinema stock with wide highlight latitude", 1.04f, 1.02f, 0.98f, 0.98f, 0.99f, 0.07f, 0.08f, 0.01f, 0.05f, 0.06f),
            FilterPreset("kodak_vericolor_iii", "Vericolor III", FilterFamily.KODAK, "Classic wedding & studio portrait negative with creamy whites", 1.06f, 1.03f, 0.95f, 0.88f, 0.92f, 0.12f, 0.13f, 0.02f, 0.07f, 0.08f),
            FilterPreset("kodak_kodachrome_25", "Kodachrome 25", FilterFamily.KODAK, "Archival slide sharpness with rich crimson reds & cobalt skies", 1.10f, 1.01f, 0.94f, 1.14f, 1.20f, 0.13f, 0.02f, 0.03f, 0.06f, 0.12f)
        )

        // 4. Retro / Vintage (15 presets: 11 classic + 4 new) — Faded color, analog warmth, optional edge effects
        list += listOf(
            FilterPreset("retro_agfa_vista", "Agfa Vista", FilterFamily.RETRO, "Punchy cherry reds & warm vintage street character", 1.09f, 1.01f, 0.92f, 1.10f, 1.09f, 0.14f, 0.12f, 0.06f, 0.15f, 0.16f),
            FilterPreset("retro_classic", "Retro", FilterFamily.RETRO, "Lifted matte blacks & sun-faded 1970s postcard warmth", 1.08f, 1.02f, 0.86f, 0.82f, 0.90f, 0.24f, 0.24f, 0.03f, 0.20f, 0.24f),
            FilterPreset("retro_vintage", "Vintage", FilterFamily.RETRO, "Warm sepia-tinted highlights & soft corner vignette", 1.12f, 1.03f, 0.84f, 0.76f, 0.92f, 0.28f, 0.22f, 0.05f, 0.22f, 0.28f),
            FilterPreset("retro_1974_summer", "1974 Summer", FilterFamily.RETRO, "Golden haze, low contrast & analog dust texture", 1.14f, 1.05f, 0.82f, 0.85f, 0.88f, 0.30f, 0.26f, 0.02f, 0.24f, 0.22f),
            FilterPreset("retro_faded_memory", "Faded Memory", FilterFamily.RETRO, "Washed pastel tones with creamy shadow lift", 1.04f, 1.02f, 0.94f, 0.68f, 0.85f, 0.15f, 0.30f, 0.01f, 0.18f, 0.18f),
            FilterPreset("retro_optik_amber", "Optik Amber", FilterFamily.RETRO, "Light-leak inspired warm edge glow & rich bronze", 1.16f, 1.01f, 0.85f, 0.95f, 1.04f, 0.32f, 0.16f, 0.08f, 0.19f, 0.26f),
            FilterPreset("retro_thrift_35", "Thrift 35mm", FilterFamily.RETRO, "Expired film roll with olive shadows & warm grain", 1.03f, 1.07f, 0.88f, 0.84f, 0.94f, 0.18f, 0.20f, -0.07f, 0.28f, 0.25f),
            FilterPreset("retro_helio_warm", "Helios Swirl", FilterFamily.RETRO, "Soviet vintage glass warmth & strong edge falloff", 1.09f, 1.04f, 0.89f, 0.90f, 0.98f, 0.20f, 0.15f, 0.02f, 0.16f, 0.36f),
            FilterPreset("retro_vinyl_sleeve", "Vinyl Sleeve", FilterFamily.RETRO, "Muted mustard & teal mid-century record aesthetic", 1.06f, 1.05f, 0.91f, 0.80f, 1.02f, 0.17f, 0.18f, -0.04f, 0.21f, 0.20f),
            FilterPreset("retro_polar_sun", "Solarized Fade", FilterFamily.RETRO, "Soft highlight compression with warm desert dust", 1.11f, 1.02f, 0.88f, 0.78f, 0.87f, 0.25f, 0.28f, 0.04f, 0.22f, 0.20f),
            FilterPreset("retro_super8_frame", "Super 8 Reel", FilterFamily.RETRO, "Home-movie tungsten warmth & pronounced celluloid grain", 1.15f, 1.03f, 0.83f, 0.88f, 1.06f, 0.29f, 0.19f, 0.05f, 0.32f, 0.30f),
            FilterPreset("retro_orwo_nc19", "ORWO Chrome", FilterFamily.RETRO, "East-German vintage cinema stock with muted olive-ochre tones", 1.05f, 1.06f, 0.87f, 0.79f, 0.96f, 0.16f, 0.21f, -0.05f, 0.23f, 0.22f),
            FilterPreset("retro_konica_centuria", "Konica Centuria", FilterFamily.RETRO, "Early-2000s nostalgic cyan skies & warm amber skin", 1.03f, 1.04f, 1.06f, 0.96f, 1.02f, 0.04f, 0.14f, -0.03f, 0.16f, 0.15f),
            FilterPreset("retro_perutz_primera", "Perutz Primera", FilterFamily.RETRO, "European holiday slide warmth & soft halation fade", 1.10f, 1.03f, 0.89f, 0.92f, 0.95f, 0.21f, 0.17f, 0.03f, 0.17f, 0.18f),
            FilterPreset("retro_110_pocket", "110 Pocket Cam", FilterFamily.RETRO, "Lo-fi miniature cartridge grain with warm matte shadows", 1.11f, 1.02f, 0.86f, 0.86f, 0.91f, 0.23f, 0.25f, 0.04f, 0.34f, 0.28f)
        )

        // 5. Cinematic (15 presets: 11 classic + 4 new) — Muted palettes and filmic contrast
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
            FilterPreset("cine_midnight_express", "Nocturne 35", FilterFamily.CINEMATIC, "Deep sapphire shadows & clean streetlamp highlights", 0.91f, 0.97f, 1.14f, 0.85f, 1.16f, -0.19f, 0.06f, 0.02f, 0.16f, 0.22f),
            FilterPreset("cine_still_50d", "CineStill 50D", FilterFamily.CINEMATIC, "Daylight cinema stock with warm halation & smooth highlights", 1.06f, 1.03f, 0.97f, 0.98f, 1.04f, 0.11f, 0.09f, 0.02f, 0.08f, 0.12f),
            FilterPreset("cine_technicolor_iv", "Technicolor IV", FilterFamily.CINEMATIC, "Three-strip golden-age Hollywood primary saturation", 1.12f, 1.04f, 1.05f, 1.28f, 1.18f, 0.10f, 0.03f, 0.04f, 0.09f, 0.16f),
            FilterPreset("cine_blade_amber", "Wallace 2049", FilterFamily.CINEMATIC, "Monolithic amber fog & deep dystopian shadow contrast", 1.18f, 1.02f, 0.78f, 0.92f, 1.22f, 0.34f, 0.08f, 0.04f, 0.14f, 0.24f),
            FilterPreset("cine_roma_65mm", "65mm Panavision", FilterFamily.CINEMATIC, "Large-format cinema clarity with creamy highlight roll-off", 1.02f, 1.03f, 1.01f, 0.94f, 1.06f, 0.04f, 0.08f, -0.01f, 0.05f, 0.10f)
        )

        // 6. Monochrome (15 presets: 11 classic + 4 new) — Classic B&W, silver, noir, high contrast
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
            FilterPreset("bw_chalk_matte", "Silky Matte B&W", FilterFamily.MONOCHROME, "Low-contrast charcoal sketch feel with lifted blacks", 0.98f, 0.98f, 0.98f, 0.0f, 0.88f, 0.0f, 0.24f, 0.0f, 0.12f, 0.08f),
            FilterPreset("bw_tmax_400", "T-Max 400", FilterFamily.MONOCHROME, "Tabular-grain crispness with punchy midtone separation", 1.01f, 1.01f, 1.01f, 0.0f, 1.22f, 0.0f, 0.03f, 0.0f, 0.14f, 0.12f),
            FilterPreset("bw_pan_f_50", "Pan F Plus 50", FilterFamily.MONOCHROME, "Fine-art studio monochrome with deep velvet blacks", 1.02f, 1.02f, 1.02f, 0.0f, 1.30f, 0.0f, 0.01f, 0.0f, 0.04f, 0.10f),
            FilterPreset("bw_double_x_5222", "Eastman Double-X", FilterFamily.MONOCHROME, "Legendary 35mm motion-picture B&W stock", 1.02f, 1.01f, 1.00f, 0.0f, 1.20f, 0.02f, 0.07f, 0.0f, 0.21f, 0.16f),
            FilterPreset("bw_wet_collodion", "Tintype 1860", FilterFamily.MONOCHROME, "Antique wet-plate collodion silver with heavy edge vignette", 1.06f, 1.01f, 0.92f, 0.06f, 1.26f, 0.10f, 0.11f, 0.0f, 0.28f, 0.42f)
        )

        // 7. Instant-film inspired (15 presets: 11 classic + 4 new) — Soft contrast, warm whites, nostalgic rendering
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
            FilterPreset("inst_mono_instant", "Instant Silver", FilterFamily.INSTANT, "Coaterless B&W instant print with warm creamy highlights", 1.05f, 1.02f, 0.95f, 0.08f, 1.06f, 0.10f, 0.15f, 0.0f, 0.14f, 0.16f),
            FilterPreset("inst_fp3000b", "FP-3000B Night", FilterFamily.INSTANT, "High-speed peel-apart monochrome instant with rich silver highlights", 1.03f, 1.03f, 1.01f, 0.04f, 1.24f, 0.04f, 0.12f, 0.0f, 0.24f, 0.20f),
            FilterPreset("inst_square_pastel", "Instax Square Soft", FilterFamily.INSTANT, "Pastel instant film with bright skin & soft shadow lift", 1.04f, 1.03f, 1.02f, 0.91f, 0.91f, 0.06f, 0.17f, 0.03f, 0.06f, 0.10f),
            FilterPreset("inst_golden_polar", "Sun 660 Autofocus", FilterFamily.INSTANT, "80s sonar instant camera warmth with amber shadow tint", 1.11f, 1.04f, 0.88f, 0.89f, 0.95f, 0.21f, 0.18f, 0.03f, 0.14f, 0.19f),
            FilterPreset("inst_cyan_cool_pack", "Cold Stored 600", FilterFamily.INSTANT, "Fridge-kept instant pack with crisp teal-blue shadows", 0.95f, 1.05f, 1.09f, 0.92f, 0.97f, -0.10f, 0.14f, -0.04f, 0.10f, 0.14f)
        )

        // 8. Nature (15 presets: 11 classic + 4 new) — Foliage, landscape, ocean and sky
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
            FilterPreset("nat_botanical_dew", "Botanical Dew", FilterFamily.NATURE, "Macro petal delicacy with soft sage-mint greens", 0.98f, 1.06f, 1.00f, 0.96f, 0.95f, 0.01f, 0.08f, -0.05f, 0.04f, 0.07f),
            FilterPreset("nat_highland_heather", "Scottish Highland", FilterFamily.NATURE, "Misty peat-moss greens & muted heather purple skies", 0.98f, 1.04f, 1.02f, 0.86f, 1.06f, -0.02f, 0.09f, 0.03f, 0.08f, 0.14f),
            FilterPreset("nat_bamboo_grove", "Arashiyama Bamboo", FilterFamily.NATURE, "Zen jade stalks & filtered canopy light", 0.94f, 1.11f, 0.97f, 1.06f, 1.02f, -0.04f, 0.07f, -0.10f, 0.05f, 0.10f),
            FilterPreset("nat_amalfi_citrus", "Amalfi Coast", FilterFamily.NATURE, "Sun-drenched Mediterranean azure water & lemon warmth", 1.06f, 1.05f, 1.08f, 1.24f, 1.10f, 0.10f, 0.03f, -0.02f, 0.04f, 0.07f),
            FilterPreset("nat_dolomite_dawn", "Dolomite Alpenglow", FilterFamily.NATURE, "Rose-gold mountain peaks & cool valley blue shadows", 1.09f, 1.00f, 1.05f, 1.12f, 1.09f, 0.08f, 0.05f, 0.07f, 0.05f, 0.09f)
        )

        // 9. Portrait (15 presets: 11 classic + 4 new) — Skin-friendly color and controlled highlights
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
            FilterPreset("port_linen_minimal", "Natural Linen", FilterFamily.PORTRAIT, "Organic oatmeal neutrals & honest unforced skin color", 1.03f, 1.02f, 0.96f, 0.86f, 0.95f, 0.07f, 0.09f, 0.0f, 0.05f, 0.06f),
            FilterPreset("port_milan_runway", "Milan Editorial", FilterFamily.PORTRAIT, "Sculpted cheekbones, warm sand skin & bold shadow contrast", 1.05f, 1.01f, 0.95f, 0.88f, 1.12f, 0.10f, 0.05f, 0.02f, 0.06f, 0.12f),
            FilterPreset("port_seoul_glow", "Seoul Glass Skin", FilterFamily.PORTRAIT, "Dewy luminous highlights & clean rose-ivory complexion", 1.04f, 1.02f, 1.03f, 0.93f, 0.92f, 0.04f, 0.12f, 0.05f, 0.02f, 0.04f),
            FilterPreset("port_sunlit_amber", "Sunlit Backlight", FilterFamily.PORTRAIT, "Warm rim-light flare & lifted shadow detail on faces", 1.09f, 1.04f, 0.92f, 0.97f, 0.93f, 0.19f, 0.13f, 0.03f, 0.06f, 0.08f),
            FilterPreset("port_atelier_softbox", "Atelier Softbox", FilterFamily.PORTRAIT, "Medium-format studio portrait neutrality & fine tonal roll-off", 1.02f, 1.02f, 1.00f, 0.91f, 1.02f, 0.04f, 0.06f, 0.01f, 0.03f, 0.07f)
        )

        // 10. Night / Neon (14 presets: 10 classic + 4 new) — City glow and restrained saturation
        list += listOf(
            FilterPreset("night_tokyo_neon", "Tokyo Neon", FilterFamily.NIGHT, "Electric cyan & magenta signage with restrained shadow noise", 0.96f, 1.03f, 1.14f, 1.18f, 1.16f, -0.10f, 0.04f, 0.08f, 0.10f, 0.18f),
            FilterPreset("night_sodium_vapor", "Sodium Street", FilterFamily.NIGHT, "Warm amber urban streetlights & deep midnight blacks", 1.14f, 1.02f, 0.86f, 0.96f, 1.14f, 0.24f, 0.05f, 0.03f, 0.12f, 0.20f),
            FilterPreset("night_blue_hour", "Blue Hour Dusk", FilterFamily.NIGHT, "Velvet twilight cobalt skies & glowing window warmth", 0.93f, 1.00f, 1.15f, 1.10f, 1.09f, -0.16f, 0.04f, 0.02f, 0.07f, 0.14f),
            FilterPreset("night_cyber_rain", "Wet Asphalt", FilterFamily.NIGHT, "Specular street reflections in teal, violet & coral", 0.98f, 1.04f, 1.12f, 1.14f, 1.20f, -0.06f, 0.03f, 0.06f, 0.11f, 0.22f),
            FilterPreset("night_jazz_club", "Velvet Lounge", FilterFamily.NIGHT, "Dim candlelit burgundy & brass low-light warmth", 1.12f, 0.98f, 0.90f, 0.90f, 1.08f, 0.20f, 0.10f, 0.09f, 0.16f, 0.26f),
            FilterPreset("night_astro_clear", "Starlight Clear", FilterFamily.NIGHT, "High micro-contrast night sky with suppressed light pollution", 0.92f, 0.98f, 1.10f, 0.92f, 1.22f, -0.14f, 0.0f, -0.02f, 0.05f, 0.10f),
            FilterPreset("night_diner_window", "Late Diner", FilterFamily.NIGHT, "Hopper-inspired emerald interior fluorescent & warm skin", 0.96f, 1.09f, 0.97f, 0.98f, 1.12f, 0.02f, 0.07f, -0.10f, 0.13f, 0.18f),
            FilterPreset("night_harbor_fog", "Harbor Beacon", FilterFamily.NIGHT, "Misty maritime slate with soft halation around lights", 0.94f, 1.02f, 1.08f, 0.78f, 0.96f, -0.10f, 0.15f, -0.03f, 0.12f, 0.16f),
            FilterPreset("night_violet_skyline", "Metropolis Dusk", FilterFamily.NIGHT, "Sunset-to-night gradient in plum, rose & indigo", 1.06f, 0.97f, 1.11f, 1.12f, 1.12f, 0.02f, 0.05f, 0.12f, 0.08f, 0.15f),
            FilterPreset("night_low_iso_clean", "Clean Night NR", FilterFamily.NIGHT, "Balanced low-light clarity with neutral white balance", 0.99f, 1.01f, 1.04f, 0.94f, 1.06f, -0.04f, 0.03f, 0.0f, 0.02f, 0.08f),
            FilterPreset("night_shibuya_crossing", "Shibuya Crossing", FilterFamily.NIGHT, "Crisp neon billboards with deep teal shadows & ruby taillights", 0.97f, 1.04f, 1.13f, 1.20f, 1.18f, -0.08f, 0.04f, 0.05f, 0.11f, 0.18f),
            FilterPreset("night_aurora_borealis", "Aurora Borealis", FilterFamily.NIGHT, "Luminous emerald night sky & deep arctic indigo", 0.90f, 1.12f, 1.08f, 1.16f, 1.15f, -0.12f, 0.03f, -0.09f, 0.06f, 0.14f),
            FilterPreset("night_rooftop_amber", "Rooftop Bar", FilterFamily.NIGHT, "Warm Edison bulb glow against dark city horizon", 1.13f, 1.03f, 0.89f, 1.02f, 1.10f, 0.22f, 0.07f, 0.04f, 0.12f, 0.18f),
            FilterPreset("night_subway_fluoro", "Subway Platform", FilterFamily.NIGHT, "Underground cool-green fluorescent tubes & gritty contrast", 0.93f, 1.10f, 0.99f, 0.86f, 1.16f, -0.07f, 0.08f, -0.12f, 0.16f, 0.20f)
        )

        // 11. Creative (28 presets: 10 classic + 18 new) — Duotone, cross-process-inspired experiments
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
            FilterPreset("crea_halide_high_key", "High-Key Studio", FilterFamily.CREATIVE, "Luminous gallery overexposure with crisp dark accents", 1.04f, 1.04f, 1.04f, 0.75f, 0.92f, 0.05f, 0.26f, 0.0f, 0.06f, 0.04f),
            FilterPreset("crea_lomo_turquoise", "Turquoise Shift", FilterFamily.CREATIVE, "Experimental color-shifted emulsion turning skies warm gold & skin aqua", 0.88f, 1.10f, 1.18f, 1.18f, 1.12f, -0.18f, 0.06f, -0.08f, 0.14f, 0.18f),
            FilterPreset("crea_risograph_coral", "Risograph Coral & Teal", FilterFamily.CREATIVE, "Soy-ink Japanese printmaking duotone with tactile grain", 1.14f, 1.02f, 1.04f, 0.92f, 1.16f, 0.14f, 0.12f, 0.08f, 0.24f, 0.12f),
            FilterPreset("crea_dichroic_prism", "Dichroic Prism", FilterFamily.CREATIVE, "Iridescent magenta-cyan spectral split & luminous contrast", 1.08f, 0.96f, 1.14f, 1.24f, 1.14f, -0.04f, 0.05f, 0.18f, 0.08f, 0.15f),
            FilterPreset("crea_amber_monolith", "Amber Monolith", FilterFamily.CREATIVE, "Pure monochromatic golden-amber darkroom safelight tone", 1.26f, 1.04f, 0.68f, 0.45f, 1.20f, 0.38f, 0.06f, 0.04f, 0.15f, 0.22f),
            FilterPreset("crea_cobalt_velvet", "Cobalt Velvet", FilterFamily.CREATIVE, "Deep Klein-blue shadows with warm parchment highlights", 0.90f, 0.98f, 1.22f, 0.88f, 1.18f, -0.20f, 0.09f, 0.04f, 0.10f, 0.18f),
            FilterPreset("crea_acid_lime", "Slide X-Pro Lime", FilterFamily.CREATIVE, "90s skate-video cross-processed chartreuse & deep blacks", 1.02f, 1.16f, 0.84f, 1.26f, 1.26f, 0.12f, 0.03f, -0.18f, 0.16f, 0.24f),
            FilterPreset("crea_rose_quartz_duo", "Rose Quartz Duotone", FilterFamily.CREATIVE, "Soft blush highlights paired with deep charcoal-plum shadows", 1.12f, 0.96f, 1.04f, 0.52f, 1.12f, 0.10f, 0.14f, 0.16f, 0.08f, 0.14f),
            FilterPreset("crea_bauhaus_primary", "Bauhaus Primary", FilterFamily.CREATIVE, "Bold modernist primary reds, yellows & blues with crisp contrast", 1.10f, 1.04f, 1.08f, 1.34f, 1.22f, 0.06f, 0.02f, 0.02f, 0.06f, 0.10f),
            FilterPreset("crea_matcha_sepia", "Matcha Split-Tone", FilterFamily.CREATIVE, "Warm cream highlights rolling into botanical moss shadows", 1.02f, 1.09f, 0.93f, 0.68f, 1.06f, 0.08f, 0.14f, -0.10f, 0.09f, 0.12f),
            FilterPreset("crea_ultraviolet_haze", "Ultraviolet Dream", FilterFamily.CREATIVE, "Surreal electric violet tint with soft highlight bloom", 1.06f, 0.92f, 1.20f, 1.12f, 1.06f, -0.08f, 0.11f, 0.22f, 0.09f, 0.16f),
            FilterPreset("crea_terracotta_mono", "Sienna Clay Mono", FilterFamily.CREATIVE, "Renaissance red-chalk drawing warmth & matte paper texture", 1.20f, 0.98f, 0.82f, 0.36f, 1.14f, 0.30f, 0.12f, 0.08f, 0.16f, 0.18f),
            FilterPreset("crea_arctic_infrared", "Blue Infrared", FilterFamily.CREATIVE, "False-color aero foliage in golden yellow & deep navy skies", 1.08f, 1.08f, 0.92f, 1.20f, 1.18f, 0.14f, 0.04f, -0.08f, 0.10f, 0.14f),
            FilterPreset("crea_silver_gelatin_warm", "Warm Silver Gelatin", FilterFamily.CREATIVE, "Museum darkroom print with subtle olive-gold shadow tone", 1.05f, 1.04f, 0.92f, 0.16f, 1.16f, 0.11f, 0.07f, -0.03f, 0.11f, 0.14f),
            FilterPreset("crea_neon_peach_teal", "Miami 1986", FilterFamily.CREATIVE, "Pastel Art-Deco flamingo coral & ocean turquoise", 1.09f, 1.04f, 1.08f, 1.18f, 1.04f, 0.06f, 0.11f, 0.04f, 0.07f, 0.10f),
            FilterPreset("crea_espresso_matte", "Espresso Matte", FilterFamily.CREATIVE, "Rich roasted coffee shadows & warm parchment highlights", 1.09f, 1.02f, 0.89f, 0.62f, 1.08f, 0.22f, 0.19f, 0.03f, 0.13f, 0.18f),
            FilterPreset("crea_jade_dynasty", "Imperial Jade", FilterFamily.CREATIVE, "Celadon porcelain greens & antique gold highlights", 0.96f, 1.10f, 0.98f, 0.82f, 1.10f, 0.04f, 0.10f, -0.11f, 0.08f, 0.14f),
            FilterPreset("crea_lavender_noir", "Lavender Noir", FilterFamily.CREATIVE, "High-contrast twilight shadows with misty lilac highlights", 1.03f, 0.96f, 1.12f, 0.46f, 1.24f, -0.06f, 0.08f, 0.14f, 0.14f, 0.22f),
            FilterPreset("crea_solaris_gold", "Solaris Halo", FilterFamily.CREATIVE, "Radiant golden-amber bloom & poetic low-contrast shadows", 1.15f, 1.06f, 0.85f, 0.94f, 0.94f, 0.28f, 0.18f, 0.03f, 0.12f, 0.16f)
        )

        return list
    }
}
