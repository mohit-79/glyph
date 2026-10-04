package com.glyph.widget.compositor;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import com.glyph.widget.GlyphPrefs;
import com.glyph.widget.GlyphTheme;

/**
 * WidgetCanvas renders 2D Canvas bitmaps for both launcher RemoteViews
 * and in-app real-time previews. Supports:
 * - Withering Glass: Liquid translucent gradient shader with specular rim
 * - Frosted Glass: Transparent widget with optical blur intensity (FastBlur)
 * - 25 Standard Color Themes
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
        int borderColor = prefs.getBorderColor(widgetType);
        int calColor1 = prefs.getCalendarColor1(widgetType);
        int calColor2 = prefs.getCalendarColor2(widgetType);
        int clockColor1 = prefs.getClockColor1(widgetType);
        int clockColor2 = prefs.getClockColor2(widgetType);

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

        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setStyle(Paint.Style.FILL);

        Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(3.5f * scale);

        boolean isWithering = GlyphTheme.isWithering(theme.id);
        boolean isFrosted = GlyphTheme.isFrosted(theme.id);

        if (isFrosted) {
            // Transparent widget with optical blur intensity
            int blurRadius = Math.max(1, Math.min(50, prefs.getBlurIntensity(widgetType)));
            int frostingPct = prefs.getFrostingIntensity(widgetType);
            float ratio = Math.max(0.05f, Math.min(0.95f, frostingPct / 100f));

            // Try sampling and blurring system wallpaper for authentic frosted glass
            Bitmap blurredWp = getBlurredWallpaper(context, width, height, pillRect, blurRadius);
            if (blurredWp != null) {
                Path pillPath = new Path();
                pillPath.addRoundRect(pillRect, cornerRadius, cornerRadius, Path.Direction.CW);
                canvas.save();
                canvas.clipPath(pillPath);
                canvas.drawBitmap(blurredWp, pillRect.left, pillRect.top, null);
                canvas.restore();
            }

            // Translucent glass tint over the blurred background
            int tintAlpha = Math.max(10, Math.min(180, (int) (ratio * 160f)));
            int topAlpha = Math.min(255, (int) (tintAlpha * 1.35f));
            int bottomAlpha = Math.max(5, (int) (tintAlpha * 0.65f));

            bgPaint.setShader(new LinearGradient(
                    pillRect.centerX(), pillRect.top,
                    pillRect.centerX(), pillRect.bottom,
                    Color.argb(topAlpha, 255, 255, 255),
                    Color.argb(bottomAlpha, 220, 230, 245),
                    Shader.TileMode.CLAMP));

            int rimAlpha = Math.min(255, (int) (ratio * 160f) + 60);
            borderPaint.setColor(Color.argb(rimAlpha, 255, 255, 255));

        } else if (isWithering) {
            // Liquid translucent gradient glass
            int intensity = prefs.getFrostingIntensity(widgetType);
            float ratio = Math.max(0.1f, Math.min(1.0f, intensity / 100f));
            int baseAlpha = (int) (ratio * 190f);
            int topAlpha = Math.min(255, (int) (baseAlpha * 1.35f));
            int bottomAlpha = Math.max(15, (int) (baseAlpha * 0.70f));

            bgPaint.setShader(new LinearGradient(
                    pillRect.centerX(), pillRect.top,
                    pillRect.centerX(), pillRect.bottom,
                    Color.argb(topAlpha, 255, 255, 255),
                    Color.argb(bottomAlpha, 210, 225, 245),
                    Shader.TileMode.CLAMP));

            int rimAlpha = Math.min(255, (int) (ratio * 160f) + 75);
            borderPaint.setColor(Color.argb(rimAlpha, 255, 255, 255));
        } else {
            bgPaint.setColor(theme.backgroundColor);
            borderPaint.setColor(borderColor);
        }

        canvas.drawRoundRect(pillRect, cornerRadius, cornerRadius, bgPaint);
        canvas.drawRoundRect(pillRect, cornerRadius, cornerRadius, borderPaint);

        float centerX = pillRect.centerX();
        float centerY = pillRect.centerY();

        // Theme Title & Primary Display
        Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setColor(clockColor1);
        titlePaint.setTextSize(26f * scale);
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setFakeBoldText(true);

        String displayTitle;
        if (isFrosted) {
            displayTitle = "FROSTED GLASS (BLUR " + prefs.getBlurIntensity(widgetType) + ")";
        } else if (isWithering) {
            displayTitle = "WITHERING GLASS " + prefs.getFrostingIntensity(widgetType) + "%";
        } else {
            displayTitle = theme.name.toUpperCase();
        }
        canvas.drawText(displayTitle, centerX, centerY - (18f * scale), titlePaint);

        // Stored Clock Colors indicator
        Paint clockIndicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        clockIndicatorPaint.setColor(clockColor2);
        clockIndicatorPaint.setTextSize(13f * scale);
        clockIndicatorPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Clock Colors: Pri / Sec", centerX, centerY + (6f * scale), clockIndicatorPaint);

        // Stored Calendar Colors indicator
        Paint calIndicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        calIndicatorPaint.setColor(calColor2);
        calIndicatorPaint.setTextSize(13f * scale);
        calIndicatorPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Calendar Colors: Pri / Sec", centerX, centerY + (26f * scale), calIndicatorPaint);

        // Color Swatches Bar inside the pill
        float swatchY = centerY + (42f * scale);
        float swatchRadius = 6f * scale;
        float spacing = 22f * scale;
        float startX = centerX - (spacing * 1.5f);

        drawSwatch(canvas, startX, swatchY, swatchRadius, calColor1, scale);
        drawSwatch(canvas, startX + spacing, swatchY, swatchRadius, calColor2, scale);
        drawSwatch(canvas, startX + (spacing * 2), swatchY, swatchRadius, clockColor1, scale);
        drawSwatch(canvas, startX + (spacing * 3), swatchY, swatchRadius, clockColor2, scale);

        return bitmap;
    }

    private static Bitmap getBlurredWallpaper(Context context, int width, int height, RectF pillRect, int blurRadius) {
        if (context == null) return null;
        try {
            WallpaperManager wm = WallpaperManager.getInstance(context);
            Drawable d = wm.getDrawable();
            if (d instanceof BitmapDrawable) {
                Bitmap wp = ((BitmapDrawable) d).getBitmap();
                if (wp != null && !wp.isRecycled()) {
                    int pLeft = (int) Math.max(0, pillRect.left);
                    int pTop = (int) Math.max(0, pillRect.top);
                    int pW = (int) Math.max(20, pillRect.width());
                    int pH = (int) Math.max(20, pillRect.height());

                    // Scale wallpaper to match aspect ratio
                    Bitmap scaled = Bitmap.createScaledBitmap(wp, width, height, true);
                    if (pLeft + pW <= scaled.getWidth() && pTop + pH <= scaled.getHeight()) {
                        Bitmap crop = Bitmap.createBitmap(scaled, pLeft, pTop, pW, pH);
                        return FastBlur.stackBlur(crop, Math.max(1, Math.min(50, blurRadius)));
                    }
                }
            }
        } catch (Throwable ignored) {
        }
        return null;
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
     * Renders the interactive preview bitmap with a dashed cell boundary,
     * wallpaper background pattern, and optical blur shader for Frosted Glass.
     */
    public static Bitmap renderPreview(int width, int height, GlyphPrefs prefs, String widgetType) {
        if (width <= 0) width = 720;
        if (height <= 0) height = 360;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        float scale = width / 360f;

        GlyphTheme.ThemeDef theme = prefs.getTheme(widgetType);
        int borderColor = prefs.getBorderColor(widgetType);
        int calColor1 = prefs.getCalendarColor1(widgetType);
        int calColor2 = prefs.getCalendarColor2(widgetType);
        int clockColor1 = prefs.getClockColor1(widgetType);
        int clockColor2 = prefs.getClockColor2(widgetType);

        // Preview background simulating home screen wallpaper with vibrant stripes & shapes
        Paint bgCanvasPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgCanvasPaint.setColor(Color.parseColor("#0C111D"));
        canvas.drawRect(0, 0, width, height, bgCanvasPaint);

        // Colorful background elements so optical blur is immediately striking
        Paint stripePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        stripePaint.setStrokeWidth(14f * scale);
        for (int x = -100; x < width + 100; x += (int) (48f * scale)) {
            stripePaint.setColor((x % 96 == 0) ? Color.parseColor("#1D3354") : Color.parseColor("#132238"));
            canvas.drawLine(x, 0, x + (height / 2f), height, stripePaint);
        }

        // Circular background accent
        Paint accentCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        accentCirclePaint.setColor(Color.parseColor("#2E1065"));
        canvas.drawCircle(width * 0.75f, height * 0.4f, 90f * scale, accentCirclePaint);

        // Faint outer bounds representing home screen cell boundary
        Paint cellBoundaryPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cellBoundaryPaint.setColor(Color.parseColor("#475569"));
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

        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setStyle(Paint.Style.FILL);

        Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(3f * scale);

        boolean isWithering = GlyphTheme.isWithering(theme.id);
        boolean isFrosted = GlyphTheme.isFrosted(theme.id);

        if (isFrosted) {
            // Extract the background behind the pill and apply real optical StackBlur
            int blurRadius = Math.max(1, Math.min(50, prefs.getBlurIntensity(widgetType)));
            int pL = (int) pillRect.left;
            int pT = (int) pillRect.top;
            int pW = (int) pillRect.width();
            int pH = (int) pillRect.height();

            if (pL >= 0 && pT >= 0 && pL + pW <= width && pT + pH <= height) {
                Bitmap subBackdrop = Bitmap.createBitmap(bitmap, pL, pT, pW, pH);
                Bitmap blurredBackdrop = FastBlur.stackBlur(subBackdrop, blurRadius);

                Path pillPath = new Path();
                pillPath.addRoundRect(pillRect, cornerRadius, cornerRadius, Path.Direction.CW);
                canvas.save();
                canvas.clipPath(pillPath);
                canvas.drawBitmap(blurredBackdrop, pillRect.left, pillRect.top, null);
                canvas.restore();
            }

            // Transparent glass tint overlay
            int frostingPct = prefs.getFrostingIntensity(widgetType);
            float ratio = Math.max(0.05f, Math.min(0.95f, frostingPct / 100f));
            int tintAlpha = Math.max(10, Math.min(180, (int) (ratio * 140f)));

            bgPaint.setShader(new LinearGradient(
                    pillRect.centerX(), pillRect.top,
                    pillRect.centerX(), pillRect.bottom,
                    Color.argb(Math.min(255, (int) (tintAlpha * 1.3f)), 255, 255, 255),
                    Color.argb(Math.max(5, (int) (tintAlpha * 0.6f)), 220, 235, 255),
                    Shader.TileMode.CLAMP));

            int rimAlpha = Math.min(255, (int) (ratio * 150f) + 70);
            borderPaint.setColor(Color.argb(rimAlpha, 255, 255, 255));

        } else if (isWithering) {
            int intensity = prefs.getFrostingIntensity(widgetType);
            float ratio = Math.max(0.1f, Math.min(1.0f, intensity / 100f));
            int baseAlpha = (int) (ratio * 190f);
            int topAlpha = Math.min(255, (int) (baseAlpha * 1.35f));
            int bottomAlpha = Math.max(15, (int) (baseAlpha * 0.70f));

            bgPaint.setShader(new LinearGradient(
                    pillRect.centerX(), pillRect.top,
                    pillRect.centerX(), pillRect.bottom,
                    Color.argb(topAlpha, 255, 255, 255),
                    Color.argb(bottomAlpha, 210, 225, 245),
                    Shader.TileMode.CLAMP));

            int rimAlpha = Math.min(255, (int) (ratio * 160f) + 75);
            borderPaint.setColor(Color.argb(rimAlpha, 255, 255, 255));
        } else {
            bgPaint.setColor(theme.backgroundColor);
            borderPaint.setColor(borderColor);
        }

        canvas.drawRoundRect(pillRect, cornerRadius, cornerRadius, bgPaint);
        canvas.drawRoundRect(pillRect, cornerRadius, cornerRadius, borderPaint);

        float centerX = pillRect.centerX();
        float centerY = pillRect.centerY();

        // Theme name label
        Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setColor(clockColor1);
        titlePaint.setTextSize(24f * scale);
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setFakeBoldText(true);

        String previewTitle;
        if (isFrosted) {
            previewTitle = "FROSTED (BLUR " + prefs.getBlurIntensity(widgetType) + " • " + prefs.getFrostingIntensity(widgetType) + "%)";
        } else if (isWithering) {
            previewTitle = "WITHERING GLASS (" + prefs.getFrostingIntensity(widgetType) + "%)";
        } else {
            previewTitle = theme.name;
        }
        canvas.drawText(previewTitle, centerX, centerY - (14f * scale), titlePaint);

        // Stored color values display
        Paint subPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        subPaint.setColor(clockColor2);
        subPaint.setTextSize(12f * scale);
        subPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Stored: Border (1) • Cal (2) • Clock (2)", centerX, centerY + (8f * scale), subPaint);

        // Color Swatches
        float swatchY = centerY + (28f * scale);
        float swatchRadius = 6f * scale;
        float spacing = 22f * scale;
        float startX = centerX - (spacing * 1.5f);

        drawSwatch(canvas, startX, swatchY, swatchRadius, calColor1, scale);
        drawSwatch(canvas, startX + spacing, swatchY, swatchRadius, calColor2, scale);
        drawSwatch(canvas, startX + (spacing * 2), swatchY, swatchRadius, clockColor1, scale);
        drawSwatch(canvas, startX + (spacing * 3), swatchY, swatchRadius, clockColor2, scale);

        return bitmap;
    }

    public static Bitmap renderPreview(int width, int height, GlyphPrefs prefs) {
        return renderPreview(width, height, prefs, GlyphPrefs.WIDGET_CLOCK_CALENDAR);
    }
}
