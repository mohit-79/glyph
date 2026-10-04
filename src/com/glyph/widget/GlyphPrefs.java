package com.glyph.widget;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * GlyphPrefs manages persistent configuration namespaced per widget type.
 * Ensures each widget (Clock & Calendar, Weather, Music, etc.) controls
 * its margins, themes, and dimensions completely independently.
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

    public GlyphTheme.ThemeDef getTheme(String widgetType) {
        return GlyphTheme.getThemeById(getThemeId(widgetType));
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
