package com.glyph.widget;

import android.graphics.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * GlyphTheme defines 26 curated artistic themes.
 * Each theme defines:
 * - Background color
 * - 1 Border color
 * - 2 Calendar text colors (Primary & Secondary)
 * - 2 Clock text colors (Primary & Accent)
 */
public class GlyphTheme {

    public static class ThemeDef {
        public final String id;
        public final String name;
        public final int backgroundColor;
        public final int borderColor;
        public final int calendarTextPrimary;
        public final int calendarTextSecondary;
        public final int clockTextPrimary;
        public final int clockTextSecondary;

        public ThemeDef(String id, String name, String bg, String border,
                        String calPrimary, String calSecondary,
                        String clockPrimary, String clockSecondary) {
            this.id = id;
            this.name = name;
            this.backgroundColor = Color.parseColor(bg);
            this.borderColor = Color.parseColor(border);
            this.calendarTextPrimary = Color.parseColor(calPrimary);
            this.calendarTextSecondary = Color.parseColor(calSecondary);
            this.clockTextPrimary = Color.parseColor(clockPrimary);
            this.clockTextSecondary = Color.parseColor(clockSecondary);
        }
    }

    private static final List<ThemeDef> THEMES = new ArrayList<ThemeDef>();

    static {
        // 1. Obsidian Dark
        THEMES.add(new ThemeDef("obsidian", "Obsidian Dark",
                "#101216", "#262C36", "#F8FAFC", "#94A3B8", "#FFFFFF", "#60A5FA"));

        // 2. Porcelain Light
        THEMES.add(new ThemeDef("porcelain", "Porcelain Light",
                "#F3F4F6", "#D1D5DB", "#111827", "#6B7280", "#0F172A", "#3B82F6"));

        // 3. Emerald Mint
        THEMES.add(new ThemeDef("emerald", "Emerald Mint",
                "#06281E", "#0F5132", "#A7F3D0", "#34D399", "#ECFDF5", "#10B981"));

        // 4. Cyber Neon
        THEMES.add(new ThemeDef("cyber_neon", "Cyber Neon",
                "#130924", "#7928CA", "#F43F5E", "#38BDF8", "#00F5FF", "#FF007F"));

        // 5. Nordic Lavender
        THEMES.add(new ThemeDef("nordic_lavender", "Nordic Lavender",
                "#1E1B2E", "#4338CA", "#E0E7FF", "#A5B4FC", "#F5F3FF", "#C084FC"));

        // 6. Brushed Titanium
        THEMES.add(new ThemeDef("brushed_titanium", "Brushed Titanium",
                "#1E232A", "#475569", "#F1F5F9", "#94A3B8", "#CBD5E1", "#38BDF8"));

        // 7. Amber Espresso
        THEMES.add(new ThemeDef("amber_espresso", "Amber Espresso",
                "#1C130E", "#78350F", "#FEF3C7", "#FBBF24", "#FDE68A", "#D97706"));

        // 8. Crimson Velvet
        THEMES.add(new ThemeDef("crimson_velvet", "Crimson Velvet",
                "#25090F", "#881337", "#FFE4E6", "#FB7185", "#FFF1F2", "#E11D48"));

        // 9. Deep Ocean
        THEMES.add(new ThemeDef("deep_ocean", "Deep Ocean",
                "#0B1B2B", "#1E3A8A", "#E0F2FE", "#38BDF8", "#F0F9FF", "#0284C7"));

        // 10. Matcha Sage
        THEMES.add(new ThemeDef("matcha_sage", "Matcha Sage",
                "#182216", "#3F6212", "#ECFCCB", "#A3E635", "#F7FEE7", "#65A30D"));

        // 11. Terracotta Sunset
        THEMES.add(new ThemeDef("terracotta", "Terracotta Sunset",
                "#2B140E", "#9A3412", "#FFEDD5", "#FB923C", "#FFF7ED", "#EA580C"));

        // 12. Midnight Plum
        THEMES.add(new ThemeDef("midnight_plum", "Midnight Plum",
                "#1F0D24", "#701A75", "#FAE8FF", "#E879F9", "#FDF4FF", "#C026D3"));

        // 13. Monochrome Zinc
        THEMES.add(new ThemeDef("monochrome_zinc", "Monochrome Zinc",
                "#18181B", "#3F3F46", "#FAFAFA", "#A1A1AA", "#FFFFFF", "#71717A"));

        // 14. Solar Dawn
        THEMES.add(new ThemeDef("solar_dawn", "Solar Dawn",
                "#161B2E", "#1D4ED8", "#FEF08A", "#FACC15", "#FFFFFF", "#F59E0B"));

        // 15. Glacial Frost
        THEMES.add(new ThemeDef("glacial_frost", "Glacial Frost",
                "#0F2027", "#203A43", "#E0F7FA", "#80DEEA", "#FFFFFF", "#00E5FF"));

        // 16. Sand Dune
        THEMES.add(new ThemeDef("sand_dune", "Sand Dune",
                "#241D17", "#574133", "#F5EBE6", "#D4B8A5", "#FAF6F3", "#C28E6F"));

        // 17. Rose Quartz
        THEMES.add(new ThemeDef("rose_quartz", "Rose Quartz",
                "#241219", "#63233B", "#FCE7F3", "#F472B6", "#FDF2F8", "#DB2777"));

        // 18. Cobalt Midnight
        THEMES.add(new ThemeDef("cobalt_midnight", "Cobalt Midnight",
                "#0A1128", "#1C3782", "#DBEAFE", "#60A5FA", "#EFF6FF", "#2563EB"));

        // 19. Tactical Olive
        THEMES.add(new ThemeDef("tactical_olive", "Tactical Olive",
                "#191E13", "#3A4726", "#E8EDDF", "#A3B18A", "#F4F6F0", "#588157"));

        // 20. Graphite Coral
        THEMES.add(new ThemeDef("graphite_coral", "Graphite Coral",
                "#1A1D20", "#32383E", "#FFFFFF", "#9CA3AF", "#FFFFFF", "#FF6B6B"));

        // 21. Pastel Pistachio
        THEMES.add(new ThemeDef("pastel_pistachio", "Pastel Pistachio",
                "#14221D", "#2C4C3E", "#D8F3DC", "#95D5B2", "#E8F8EE", "#52B788"));

        // 22. Steel Cerulean
        THEMES.add(new ThemeDef("steel_cerulean", "Steel Cerulean",
                "#111A24", "#283E56", "#E2E8F0", "#7DD3FC", "#F8FAFC", "#0284C7"));

        // 23. Wine Berry
        THEMES.add(new ThemeDef("wine_berry", "Wine Berry",
                "#210B18", "#5C1D45", "#F3E8FF", "#D8B4FE", "#FAF5FF", "#9333EA"));

        // 24. Warm Mocha
        THEMES.add(new ThemeDef("warm_mocha", "Warm Mocha",
                "#1C1815", "#483C32", "#F5EBE0", "#D5BDAF", "#FAF0E6", "#B08968"));

        // 25. Aurora Borealis
        THEMES.add(new ThemeDef("aurora", "Aurora Borealis",
                "#071E22", "#1D7874", "#EEF5DB", "#679267", "#FFFFFF", "#00F5D4"));

        // 26. Withering Glass (Obsidian Weathered Smoked Glass)
        THEMES.add(new ThemeDef("withering_glass", "Withering Glass",
                "#3310141D", "#4D38BDF8", "#F8FAFC", "#94A3B8", "#FFFFFF", "#38BDF8"));

        // 27. Frosted Glass (100% Transparent Widget with Pure Optical Blur & Zero White Tint)
        THEMES.add(new ThemeDef("frosted_glass", "Frosted Glass",
                "#00000000", "#40FFFFFF", "#FFFFFF", "#F1F5F9", "#FFFFFF", "#38BDF8"));
    }

    public static boolean isWithering(String themeId) {
        return "withering_glass".equals(themeId);
    }

    public static boolean isFrosted(String themeId) {
        return "frosted_glass".equals(themeId);
    }

    public static boolean isGlass(String themeId) {
        return isWithering(themeId) || isFrosted(themeId);
    }

    public static List<ThemeDef> getAllThemes() {
        return THEMES;
    }

    public static ThemeDef getTheme(int index) {
        if (index < 0 || index >= THEMES.size()) {
            return THEMES.get(0);
        }
        return THEMES.get(index);
    }

    public static ThemeDef getThemeById(String id) {
        if (id != null) {
            for (ThemeDef theme : THEMES) {
                if (theme.id.equals(id)) {
                    return theme;
                }
            }
        }
        return THEMES.get(0);
    }

    public static int getThemeIndexById(String id) {
        if (id != null) {
            for (int i = 0; i < THEMES.size(); i++) {
                if (THEMES.get(i).id.equals(id)) {
                    return i;
                }
            }
        }
        return 0;
    }
}
