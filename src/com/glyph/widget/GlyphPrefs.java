package com.glyph.widget;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * GlyphPrefs manages persistent configuration namespaced per widget type.
 * Ensures each widget (Clock & Calendar, Weather, Music, etc.) controls
 * its margins, themes, dimensions, and colors completely independently.
 */
public class GlyphPrefs {

    private static final String PREF_NAME = "glyph_widget_prefs";

    // Known widget types
    public static final String WIDGET_CLOCK_CALENDAR = "clock_calendar";

    // Default values
    public static final int DEFAULT_MARGIN_LEFT = 16;
    public static final int DEFAULT_MARGIN_TOP = 16;
    public static final int DEFAULT_MARGIN_RIGHT = 16;
    public static final int DEFAULT_MARGIN_BOTTOM = 16;
    public static final int DEFAULT_CORNER_RADIUS = 36;
    public static final String DEFAULT_THEME_ID = "obsidian";
    public static final int DEFAULT_FROSTING_INTENSITY = 45;
    public static final int DEFAULT_BLUR_INTENSITY = 24;
    public static final int DEFAULT_WALLPAPER_POSITION = 0; // 0 = Top, 1 = Upper Center, 2 = Center, etc.
    public static final int DEFAULT_BORDER_THICKNESS = 3; // 0dp to 16dp
    public static final int DEFAULT_CALENDAR_X = 0;
    public static final int DEFAULT_CALENDAR_Y = 0;
    public static final int DEFAULT_CALENDAR_SCALE = 100;

    public static final int NO_OVERRIDE_COLOR = -1;

    private final SharedPreferences prefs;

    public GlyphPrefs(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // --- Per-Widget Namespaced Theme Selection ---

    public String getThemeId(String widgetType) {
        return prefs.getString(widgetType + "_theme_id", DEFAULT_THEME_ID);
    }

    public void setThemeId(String widgetType, String themeId) {
        prefs.edit().putString(widgetType + "_theme_id", themeId).apply();
    }

    public int getFrostingIntensity(String widgetType) {
        return prefs.getInt(widgetType + "_frosting_intensity", DEFAULT_FROSTING_INTENSITY);
    }

    public void setFrostingIntensity(String widgetType, int intensity) {
        prefs.edit().putInt(widgetType + "_frosting_intensity", intensity).apply();
    }

    public int getBlurIntensity(String widgetType) {
        return prefs.getInt(widgetType + "_blur_intensity", DEFAULT_BLUR_INTENSITY);
    }

    public void setBlurIntensity(String widgetType, int blur) {
        prefs.edit().putInt(widgetType + "_blur_intensity", blur).apply();
    }

    public int getWallpaperPosition(String widgetType) {
        return prefs.getInt(widgetType + "_wp_pos", DEFAULT_WALLPAPER_POSITION);
    }

    public void setWallpaperPosition(String widgetType, int position) {
        prefs.edit().putInt(widgetType + "_wp_pos", position).apply();
    }

    public GlyphTheme.ThemeDef getTheme(String widgetType) {
        return GlyphTheme.getThemeById(getThemeId(widgetType));
    }

    // --- Per-Widget Default & Custom Color Getters/Setters ---

    /**
     * Returns the effective border color: custom override if set, else active theme default.
     */
    public int getBorderColor(String widgetType) {
        int custom = prefs.getInt(widgetType + "_border_color", NO_OVERRIDE_COLOR);
        if (custom != NO_OVERRIDE_COLOR) {
            return custom;
        }
        return getTheme(widgetType).borderColor;
    }

    public void setBorderColor(String widgetType, int color) {
        prefs.edit().putInt(widgetType + "_border_color", color).apply();
    }

    public void resetBorderColor(String widgetType) {
        prefs.edit().remove(widgetType + "_border_color").apply();
    }

    public boolean hasCustomBorderColor(String widgetType) {
        return prefs.getInt(widgetType + "_border_color", NO_OVERRIDE_COLOR) != NO_OVERRIDE_COLOR;
    }

    public int getBorderThickness(String widgetType) {
        return prefs.getInt(widgetType + "_border_thickness", DEFAULT_BORDER_THICKNESS);
    }

    public void setBorderThickness(String widgetType, int thickness) {
        prefs.edit().putInt(widgetType + "_border_thickness", thickness).apply();
    }

    public void resetBorderThickness(String widgetType) {
        prefs.edit().remove(widgetType + "_border_thickness").apply();
    }

    /**
     * Returns the effective Calendar Color 1 (Primary): custom override if set, else theme default.
     */
    public int getCalendarColor1(String widgetType) {
        int custom = prefs.getInt(widgetType + "_cal_color_1", NO_OVERRIDE_COLOR);
        if (custom != NO_OVERRIDE_COLOR) {
            return custom;
        }
        return getTheme(widgetType).calendarTextPrimary;
    }

    public void setCalendarColor1(String widgetType, int color) {
        prefs.edit().putInt(widgetType + "_cal_color_1", color).apply();
    }

    public boolean hasCustomCalendarColor1(String widgetType) {
        return prefs.getInt(widgetType + "_cal_color_1", NO_OVERRIDE_COLOR) != NO_OVERRIDE_COLOR;
    }

    public void resetCalendarColor1(String widgetType) {
        prefs.edit().remove(widgetType + "_cal_color_1").apply();
    }

    /**
     * Returns the effective Calendar Color 2 (Secondary): custom override if set, else theme default.
     */
    public int getCalendarColor2(String widgetType) {
        int custom = prefs.getInt(widgetType + "_cal_color_2", NO_OVERRIDE_COLOR);
        if (custom != NO_OVERRIDE_COLOR) {
            return custom;
        }
        return getTheme(widgetType).calendarTextSecondary;
    }

    public void setCalendarColor2(String widgetType, int color) {
        prefs.edit().putInt(widgetType + "_cal_color_2", color).apply();
    }

    public boolean hasCustomCalendarColor2(String widgetType) {
        return prefs.getInt(widgetType + "_cal_color_2", NO_OVERRIDE_COLOR) != NO_OVERRIDE_COLOR;
    }

    public void resetCalendarColor2(String widgetType) {
        prefs.edit().remove(widgetType + "_cal_color_2").apply();
    }

    public void resetCalendarColors(String widgetType) {
        prefs.edit()
                .remove(widgetType + "_cal_color_1")
                .remove(widgetType + "_cal_color_2")
                .apply();
    }

    /**
     * Returns the effective Clock Color 1 (Primary): custom override if set, else theme default.
     */
    public int getClockColor1(String widgetType) {
        int custom = prefs.getInt(widgetType + "_clock_color_1", NO_OVERRIDE_COLOR);
        if (custom != NO_OVERRIDE_COLOR) {
            return custom;
        }
        return getTheme(widgetType).clockTextPrimary;
    }

    public void setClockColor1(String widgetType, int color) {
        prefs.edit().putInt(widgetType + "_clock_color_1", color).apply();
    }

    /**
     * Returns the effective Clock Color 2 (Secondary/Accent): custom override if set, else theme default.
     */
    public int getClockColor2(String widgetType) {
        int custom = prefs.getInt(widgetType + "_clock_color_2", NO_OVERRIDE_COLOR);
        if (custom != NO_OVERRIDE_COLOR) {
            return custom;
        }
        return getTheme(widgetType).clockTextSecondary;
    }

    public void setClockColor2(String widgetType, int color) {
        prefs.edit().putInt(widgetType + "_clock_color_2", color).apply();
    }

    public void resetClockColors(String widgetType) {
        prefs.edit()
                .remove(widgetType + "_clock_color_1")
                .remove(widgetType + "_clock_color_2")
                .apply();
    }

    // --- Per-Widget Namespaced Margin Controls ---

    public int getMarginLeft(String widgetType) {
        return prefs.getInt(widgetType + "_margin_left", DEFAULT_MARGIN_LEFT);
    }

    public void setMarginLeft(String widgetType, int value) {
        prefs.edit().putInt(widgetType + "_margin_left", value).apply();
    }

    public int getMarginTop(String widgetType) {
        return prefs.getInt(widgetType + "_margin_top", DEFAULT_MARGIN_TOP);
    }

    public void setMarginTop(String widgetType, int value) {
        prefs.edit().putInt(widgetType + "_margin_top", value).apply();
    }

    public int getMarginRight(String widgetType) {
        return prefs.getInt(widgetType + "_margin_right", DEFAULT_MARGIN_RIGHT);
    }

    public void setMarginRight(String widgetType, int value) {
        prefs.edit().putInt(widgetType + "_margin_right", value).apply();
    }

    public int getMarginBottom(String widgetType) {
        return prefs.getInt(widgetType + "_margin_bottom", DEFAULT_MARGIN_BOTTOM);
    }

    public void setMarginBottom(String widgetType, int value) {
        prefs.edit().putInt(widgetType + "_margin_bottom", value).apply();
    }

    public int getCornerRadius(String widgetType) {
        return prefs.getInt(widgetType + "_corner_radius", DEFAULT_CORNER_RADIUS);
    }

    public void setCornerRadius(String widgetType, int value) {
        prefs.edit().putInt(widgetType + "_corner_radius", value).apply();
    }

    public void resetMargins(String widgetType) {
        prefs.edit()
                .putInt(widgetType + "_margin_left", DEFAULT_MARGIN_LEFT)
                .putInt(widgetType + "_margin_top", DEFAULT_MARGIN_TOP)
                .putInt(widgetType + "_margin_right", DEFAULT_MARGIN_RIGHT)
                .putInt(widgetType + "_margin_bottom", DEFAULT_MARGIN_BOTTOM)
                .putInt(widgetType + "_corner_radius", DEFAULT_CORNER_RADIUS)
                .apply();
    }

    // --- Per-Widget Calendar Position & Scale Transform ---

    public int getCalendarX(String widgetType) {
        return prefs.getInt(widgetType + "_cal_x", DEFAULT_CALENDAR_X);
    }

    public void setCalendarX(String widgetType, int val) {
        prefs.edit().putInt(widgetType + "_cal_x", val).apply();
    }

    public int getCalendarY(String widgetType) {
        return prefs.getInt(widgetType + "_cal_y", DEFAULT_CALENDAR_Y);
    }

    public void setCalendarY(String widgetType, int val) {
        prefs.edit().putInt(widgetType + "_cal_y", val).apply();
    }

    public int getCalendarScale(String widgetType) {
        return prefs.getInt(widgetType + "_cal_scale", DEFAULT_CALENDAR_SCALE);
    }

    public void setCalendarScale(String widgetType, int val) {
        prefs.edit().putInt(widgetType + "_cal_scale", val).apply();
    }

    public static final int DEFAULT_CALENDAR_STYLE = 0;

    public int getCalendarStyle(String widgetType) {
        return prefs.getInt(widgetType + "_cal_style", DEFAULT_CALENDAR_STYLE);
    }

    public void setCalendarStyle(String widgetType, int style) {
        prefs.edit().putInt(widgetType + "_cal_style", style).apply();
    }

    public void resetCalendarStyle(String widgetType) {
        prefs.edit().remove(widgetType + "_cal_style").apply();
    }

    public void resetCalendarTransform(String widgetType) {
        prefs.edit()
                .putInt(widgetType + "_cal_x", DEFAULT_CALENDAR_X)
                .putInt(widgetType + "_cal_y", DEFAULT_CALENDAR_Y)
                .putInt(widgetType + "_cal_scale", DEFAULT_CALENDAR_SCALE)
                .apply();
    }


    // --- Backward Compatible Overloads (defaults to Clock & Calendar) ---
    public int getMarginLeft() { return getMarginLeft(WIDGET_CLOCK_CALENDAR); }
    public void setMarginLeft(int value) { setMarginLeft(WIDGET_CLOCK_CALENDAR, value); }

    public int getMarginTop() { return getMarginTop(WIDGET_CLOCK_CALENDAR); }
    public void setMarginTop(int value) { setMarginTop(WIDGET_CLOCK_CALENDAR, value); }

    public int getMarginRight() { return getMarginRight(WIDGET_CLOCK_CALENDAR); }
    public void setMarginRight(int value) { setMarginRight(WIDGET_CLOCK_CALENDAR, value); }

    public int getMarginBottom() { return getMarginBottom(WIDGET_CLOCK_CALENDAR); }
    public void setMarginBottom(int value) { setMarginBottom(WIDGET_CLOCK_CALENDAR, value); }

    public int getCornerRadius() { return getCornerRadius(WIDGET_CLOCK_CALENDAR); }
    public void setCornerRadius(int value) { setCornerRadius(WIDGET_CLOCK_CALENDAR, value); }

    public void resetMargins() { resetMargins(WIDGET_CLOCK_CALENDAR); }
}
