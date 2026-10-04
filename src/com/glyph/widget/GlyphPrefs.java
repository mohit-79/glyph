package com.glyph.widget;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * GlyphPrefs manages persistent configuration for widget margins, dimensions,
 * themes, colors, and element coordinates.
 */
public class GlyphPrefs {

    private static final String PREF_NAME = "glyph_widget_prefs";

    // 4-side independent margin keys (in dp)
    private static final String KEY_MARGIN_LEFT = "margin_left";
    private static final String KEY_MARGIN_TOP = "margin_top";
    private static final String KEY_MARGIN_RIGHT = "margin_right";
    private static final String KEY_MARGIN_BOTTOM = "margin_bottom";
    private static final String KEY_CORNER_RADIUS = "corner_radius";

    // Defaults
    public static final int DEFAULT_MARGIN_LEFT = 16;
    public static final int DEFAULT_MARGIN_TOP = 16;
    public static final int DEFAULT_MARGIN_RIGHT = 16;
    public static final int DEFAULT_MARGIN_BOTTOM = 16;
    public static final int DEFAULT_CORNER_RADIUS = 36;

    private final SharedPreferences prefs;

    public GlyphPrefs(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public int getMarginLeft() {
        return prefs.getInt(KEY_MARGIN_LEFT, DEFAULT_MARGIN_LEFT);
    }

    public void setMarginLeft(int value) {
        prefs.edit().putInt(KEY_MARGIN_LEFT, value).apply();
    }

    public int getMarginTop() {
        return prefs.getInt(KEY_MARGIN_TOP, DEFAULT_MARGIN_TOP);
    }

    public void setMarginTop(int value) {
        prefs.edit().putInt(KEY_MARGIN_TOP, value).apply();
    }

    public int getMarginRight() {
        return prefs.getInt(KEY_MARGIN_RIGHT, DEFAULT_MARGIN_RIGHT);
    }

    public void setMarginRight(int value) {
        prefs.edit().putInt(KEY_MARGIN_RIGHT, value).apply();
    }

    public int getMarginBottom() {
        return prefs.getInt(KEY_MARGIN_BOTTOM, DEFAULT_MARGIN_BOTTOM);
    }

    public void setMarginBottom(int value) {
        prefs.edit().putInt(KEY_MARGIN_BOTTOM, value).apply();
    }

    public int getCornerRadius() {
        return prefs.getInt(KEY_CORNER_RADIUS, DEFAULT_CORNER_RADIUS);
    }

    public void setCornerRadius(int value) {
        prefs.edit().putInt(KEY_CORNER_RADIUS, value).apply();
    }

    public void resetMargins() {
        prefs.edit()
                .putInt(KEY_MARGIN_LEFT, DEFAULT_MARGIN_LEFT)
                .putInt(KEY_MARGIN_TOP, DEFAULT_MARGIN_TOP)
                .putInt(KEY_MARGIN_RIGHT, DEFAULT_MARGIN_RIGHT)
                .putInt(KEY_MARGIN_BOTTOM, DEFAULT_MARGIN_BOTTOM)
                .putInt(KEY_CORNER_RADIUS, DEFAULT_CORNER_RADIUS)
                .apply();
    }
}
