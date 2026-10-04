package com.glyph.widget.compositor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
import com.glyph.widget.GlyphPrefs;
import com.glyph.widget.GlyphTheme;

/**
 * WidgetCanvas renders 2D Canvas bitmaps for both launcher RemoteViews
 * and in-app real-time previews, strictly enforcing per-widget margins and themes.
 */
public class WidgetCanvas {

    /**
     * Renders the production widget bitmap for the home screen launcher for a given widget type.
     */
    public static Bitmap renderWidget(Context context, int width, int height, GlyphPrefs prefs, String widgetType) {
        if (width <= 0) width = 720;
        if (height <= 0) height = 360;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        float scale = width / 360f;

        GlyphTheme.ThemeDef theme = prefs.getTheme(widgetType);

        float marginLeft = prefs.getMarginLeft(widgetType) * scale;
        float marginTop = prefs.getMarginTop(widgetType) * scale;
        float marginRight = prefs.getMarginRight(widgetType) * scale;
        float marginBottom = prefs.getMarginBottom(widgetType) * scale;
        float cornerRadius = prefs.getCornerRadius(widgetType) * scale;

        float pillLeft = Math.max(0, marginLeft);
        float pillTop = Math.max(0, marginTop);
        float pillRight = Math.min(width, width - marginRight);
        float pillBottom = Math.min(height, height - marginBottom);

        if (pillRight <= pillLeft + 40f) pillRight = pillLeft + 40f;
        if (pillBottom <= pillTop + 40f) pillBottom = pillTop + 40f;

        RectF pillRect = new RectF(pillLeft, pillTop, pillRight, pillBottom);

        // Outer transparent background
        canvas.drawColor(Color.TRANSPARENT);

        // Fill background pill with theme background color
        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(theme.backgroundColor);
        bgPaint.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(pillRect, cornerRadius, cornerRadius, bgPaint);

        // Draw border with theme border color
        Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setColor(theme.borderColor);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(3.5f * scale);
        canvas.drawRoundRect(pillRect, cornerRadius, cornerRadius, borderPaint);

        float centerX = pillRect.centerX();
        float centerY = pillRect.centerY();

        // Theme Title & Primary Display
        Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setColor(theme.clockTextPrimary);
        titlePaint.setTextSize(26f * scale);
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setFakeBoldText(true);
        canvas.drawText(theme.name.toUpperCase(), centerX, centerY - (18f * scale), titlePaint);

        // Stored Clock Colors indicator
        Paint clockIndicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        clockIndicatorPaint.setColor(theme.clockTextSecondary);
        clockIndicatorPaint.setTextSize(13f * scale);
        clockIndicatorPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Clock Colors: Pri / Sec", centerX, centerY + (6f * scale), clockIndicatorPaint);

        // Stored Calendar Colors indicator
        Paint calIndicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        calIndicatorPaint.setColor(theme.calendarTextSecondary);
        calIndicatorPaint.setTextSize(13f * scale);
        calIndicatorPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Calendar Colors: Pri / Sec", centerX, centerY + (26f * scale), calIndicatorPaint);

        // Color Swatches Bar inside the pill
        float swatchY = centerY + (42f * scale);
        float swatchRadius = 6f * scale;
        float spacing = 22f * scale;
        float startX = centerX - (spacing * 1.5f);

        drawSwatch(canvas, startX, swatchY, swatchRadius, theme.calendarTextPrimary, scale);
        drawSwatch(canvas, startX + spacing, swatchY, swatchRadius, theme.calendarTextSecondary, scale);
        drawSwatch(canvas, startX + (spacing * 2), swatchY, swatchRadius, theme.clockTextPrimary, scale);
        drawSwatch(canvas, startX + (spacing * 3), swatchY, swatchRadius, theme.clockTextSecondary, scale);

        return bitmap;
    }

    private static void drawSwatch(Canvas canvas, float cx, float cy, float radius, int color, float scale) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setStyle(Paint.Style.FILL);
        p.setColor(color);
        canvas.drawCircle(cx, cy, radius, p);

        Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        stroke.setStyle(Paint.Style.STROKE);
        stroke.setStrokeWidth(1.2f * scale);
        stroke.setColor(Color.WHITE);
        canvas.drawCircle(cx, cy, radius, stroke);
    }

    public static Bitmap renderWidget(Context context, int width, int height, GlyphPrefs prefs) {
        return renderWidget(context, width, height, prefs, GlyphPrefs.WIDGET_CLOCK_CALENDAR);
    }

    /**
     * Renders the interactive preview bitmap with a dashed cell boundary
     * and the active theme's colors.
     */
    public static Bitmap renderPreview(int width, int height, GlyphPrefs prefs, String widgetType) {
        if (width <= 0) width = 720;
        if (height <= 0) height = 360;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        float scale = width / 360f;

        GlyphTheme.ThemeDef theme = prefs.getTheme(widgetType);

        // Faint outer bounds representing home screen cell boundary
        Paint cellBoundaryPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cellBoundaryPaint.setColor(Color.parseColor("#2A3142"));
        cellBoundaryPaint.setStyle(Paint.Style.STROKE);
        cellBoundaryPaint.setStrokeWidth(1.5f * scale);
        cellBoundaryPaint.setPathEffect(new DashPathEffect(new float[]{10f * scale, 8f * scale}, 0));

        RectF cellRect = new RectF(4f * scale, 4f * scale, width - (4f * scale), height - (4f * scale));
        canvas.drawRoundRect(cellRect, 16f * scale, 16f * scale, cellBoundaryPaint);

        // Inner pill bounds
        float marginLeft = prefs.getMarginLeft(widgetType) * scale;
        float marginTop = prefs.getMarginTop(widgetType) * scale;
        float marginRight = prefs.getMarginRight(widgetType) * scale;
        float marginBottom = prefs.getMarginBottom(widgetType) * scale;
        float cornerRadius = prefs.getCornerRadius(widgetType) * scale;

        float pillLeft = Math.max(0, marginLeft);
        float pillTop = Math.max(0, marginTop);
        float pillRight = Math.min(width, width - marginRight);
        float pillBottom = Math.min(height, height - marginBottom);

        if (pillRight <= pillLeft + 40f) pillRight = pillLeft + 40f;
        if (pillBottom <= pillTop + 40f) pillBottom = pillTop + 40f;

        RectF pillRect = new RectF(pillLeft, pillTop, pillRight, pillBottom);

        // Fill pill
        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(theme.backgroundColor);
        bgPaint.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(pillRect, cornerRadius, cornerRadius, bgPaint);

        // Pill border
        Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setColor(theme.borderColor);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(3f * scale);
        canvas.drawRoundRect(pillRect, cornerRadius, cornerRadius, borderPaint);

        float centerX = pillRect.centerX();
        float centerY = pillRect.centerY();

        // Theme name label
        Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setColor(theme.clockTextPrimary);
        titlePaint.setTextSize(24f * scale);
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setFakeBoldText(true);
        canvas.drawText(theme.name, centerX, centerY - (14f * scale), titlePaint);

        // Stored color values display
        Paint subPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        subPaint.setColor(theme.clockTextSecondary);
        subPaint.setTextSize(12f * scale);
        subPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Stored: Border (1) • Cal (2) • Clock (2)", centerX, centerY + (8f * scale), subPaint);

        // Color Swatches
        float swatchY = centerY + (28f * scale);
        float swatchRadius = 6f * scale;
        float spacing = 22f * scale;
        float startX = centerX - (spacing * 1.5f);

        drawSwatch(canvas, startX, swatchY, swatchRadius, theme.calendarTextPrimary, scale);
        drawSwatch(canvas, startX + spacing, swatchY, swatchRadius, theme.calendarTextSecondary, scale);
        drawSwatch(canvas, startX + (spacing * 2), swatchY, swatchRadius, theme.clockTextPrimary, scale);
        drawSwatch(canvas, startX + (spacing * 3), swatchY, swatchRadius, theme.clockTextSecondary, scale);

        return bitmap;
    }

    public static Bitmap renderPreview(int width, int height, GlyphPrefs prefs) {
        return renderPreview(width, height, prefs, GlyphPrefs.WIDGET_CLOCK_CALENDAR);
    }
}
