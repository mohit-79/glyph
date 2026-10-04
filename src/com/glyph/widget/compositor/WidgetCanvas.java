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
 * - Frosted Glass: 100% transparent widget with optical blur and ZERO white tint
 * - Withering Glass: Weathered smoked obsidian glass with icy specular border
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

        // Outer transparent background ensures unoccupied margins show home screen wallpaper
        canvas.drawColor(Color.TRANSPARENT);

        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setStyle(Paint.Style.FILL);

        float borderThicknessDp = prefs.getBorderThickness(widgetType);
        float borderStrokeWidth = borderThicknessDp * scale;
        boolean hasBorder = borderStrokeWidth > 0.05f;

        Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(borderStrokeWidth);

        boolean isWithering = GlyphTheme.isWithering(theme.id);
        boolean isFrosted = GlyphTheme.isFrosted(theme.id);
        boolean hasCustomBorder = prefs.hasCustomBorderColor(widgetType);

        if (isFrosted) {
            // Pure optical blur with ZERO white tint
            int blurRadius = Math.max(1, Math.min(50, prefs.getBlurIntensity(widgetType)));
            int wpPos = prefs.getWallpaperPosition(widgetType);

            // Pull coordinate-aligned wallpaper crop and optically blur
            Bitmap blurredWp = WallpaperHelper.cropAndBlurForWidget(context, width, height, pillRect, blurRadius, wpPos);
            if (blurredWp != null) {
                Path pillPath = new Path();
                pillPath.addRoundRect(pillRect, cornerRadius, cornerRadius, Path.Direction.CW);
                canvas.save();
                canvas.clipPath(pillPath);
                canvas.drawBitmap(blurredWp, pillRect.left, pillRect.top, null);
                canvas.restore();

                // Zero white tint: background is completely transparent
                bgPaint.setColor(Color.TRANSPARENT);
            } else {
                // Frosted Glass Diffusion fallback when wallpaper is not yet synced
                bgPaint.setShader(new LinearGradient(
                        pillRect.left, pillRect.top,
                        pillRect.right, pillRect.bottom,
                        Color.argb(34, 255, 255, 255),
                        Color.argb(12, 255, 255, 255),
                        Shader.TileMode.CLAMP));
            }

            if (hasCustomBorder) {
                borderPaint.setColor(borderColor);
            } else {
                borderPaint.setColor(blurredWp != null ? Color.parseColor("#44FFFFFF") : Color.argb(90, 255, 255, 255));
            }

        } else if (isWithering) {
            // Weathered smoked obsidian glass with dark translucency
            int intensity = prefs.getFrostingIntensity(widgetType);
            float ratio = Math.max(0.1f, Math.min(1.0f, intensity / 100f));
            int alphaTop = Math.min(220, (int) (ratio * 140f) + 30);
            int alphaBottom = Math.min(240, (int) (ratio * 200f) + 50);

            bgPaint.setShader(new LinearGradient(
                    pillRect.centerX(), pillRect.top,
                    pillRect.centerX(), pillRect.bottom,
                    Color.argb(alphaTop, 18, 24, 34),
                    Color.argb(alphaBottom, 10, 14, 20),
                    Shader.TileMode.CLAMP));

            if (hasCustomBorder) {
                borderPaint.setColor(borderColor);
            } else {
                int rimAlpha = Math.min(255, (int) (ratio * 160f) + 60);
                borderPaint.setColor(Color.argb(rimAlpha, 56, 189, 248));
            }

        } else {
            bgPaint.setColor(theme.backgroundColor);
            borderPaint.setColor(borderColor);
        }

        canvas.drawRoundRect(pillRect, cornerRadius, cornerRadius, bgPaint);
        if (hasBorder) {
            canvas.drawRoundRect(pillRect, cornerRadius, cornerRadius, borderPaint);
        }

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
            displayTitle = "FROSTED GLASS (BLUR " + prefs.getBlurIntensity(widgetType) + "PX)";
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
     * Renders the interactive preview bitmap with wallpaper backdrop and true optical blur.
     */
    public static Bitmap renderPreview(Context context, int width, int height, GlyphPrefs prefs, String widgetType) {
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

        Bitmap cachedWp = (context != null) ? WallpaperHelper.getCachedWallpaper(context) : null;
        if (cachedWp != null && !cachedWp.isRecycled()) {
            int wpPos = prefs.getWallpaperPosition(widgetType);
            float yStartRatio, yEndRatio;
            switch (wpPos) {
                case WallpaperHelper.POSITION_TOP: yStartRatio = 0.07f; yEndRatio = 0.28f; break;
                case WallpaperHelper.POSITION_UPPER_CENTER: yStartRatio = 0.22f; yEndRatio = 0.43f; break;
                case WallpaperHelper.POSITION_CENTER: yStartRatio = 0.37f; yEndRatio = 0.58f; break;
                case WallpaperHelper.POSITION_LOWER_CENTER: yStartRatio = 0.52f; yEndRatio = 0.73f; break;
                case WallpaperHelper.POSITION_BOTTOM: yStartRatio = 0.67f; yEndRatio = 0.88f; break;
                default: yStartRatio = 0.07f; yEndRatio = 0.28f; break;
            }
            float xStartRatio = 0.04f;
            float xEndRatio = 0.96f;
            int cropX = (int) (cachedWp.getWidth() * xStartRatio);
            int cropY = (int) (cachedWp.getHeight() * yStartRatio);
            int cropW = (int) (cachedWp.getWidth() * (xEndRatio - xStartRatio));
            int cropH = (int) (cachedWp.getHeight() * (yEndRatio - yStartRatio));

            cropX = Math.max(0, Math.min(cropX, cachedWp.getWidth() - 10));
            cropY = Math.max(0, Math.min(cropY, cachedWp.getHeight() - 10));
            cropW = Math.max(10, Math.min(cropW, cachedWp.getWidth() - cropX));
            cropH = Math.max(10, Math.min(cropH, cachedWp.getHeight() - cropY));

            Bitmap slice = Bitmap.createBitmap(cachedWp, cropX, cropY, cropW, cropH);
            Bitmap scaledSlice = Bitmap.createScaledBitmap(slice, width, height, true);
            canvas.drawBitmap(scaledSlice, 0, 0, null);
        } else {
            // Preview background with colorful shapes & stripes representing wallpaper
            Paint bgCanvasPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            bgCanvasPaint.setColor(Color.parseColor("#0C111D"));
            canvas.drawRect(0, 0, width, height, bgCanvasPaint);

            // Striking diagonal stripes
            Paint stripePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            stripePaint.setStrokeWidth(16f * scale);
            for (int x = -100; x < width + 100; x += (int) (48f * scale)) {
                stripePaint.setColor((x % 96 == 0) ? Color.parseColor("#1D3354") : Color.parseColor("#142338"));
                canvas.drawLine(x, 0, x + (height / 2f), height, stripePaint);
            }

            // Circular background accents so blur diffusion is immediately obvious
            Paint accentCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            accentCirclePaint.setColor(Color.parseColor("#4C1D95"));
            canvas.drawCircle(width * 0.75f, height * 0.4f, 85f * scale, accentCirclePaint);

            Paint coralCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            coralCirclePaint.setColor(Color.parseColor("#BE185D"));
            canvas.drawCircle(width * 0.25f, height * 0.65f, 65f * scale, coralCirclePaint);
        }

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

        float borderThicknessDp = prefs.getBorderThickness(widgetType);
        float borderStrokeWidth = borderThicknessDp * scale;
        boolean hasBorder = borderStrokeWidth > 0.05f;

        Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(borderStrokeWidth);

        boolean isWithering = GlyphTheme.isWithering(theme.id);
        boolean isFrosted = GlyphTheme.isFrosted(theme.id);
        boolean hasCustomBorder = prefs.hasCustomBorderColor(widgetType);

        if (isFrosted) {
            // Extract the background behind the pill and apply optical blur with ZERO white tint
            int blurRadius = Math.max(1, Math.min(50, prefs.getBlurIntensity(widgetType)));
            int pL = (int) pillRect.left;
            int pT = (int) pillRect.top;
            int pW = (int) pillRect.width();
            int pH = (int) pillRect.height();

            if (pL >= 0 && pT >= 0 && pL + pW <= width && pT + pH <= height) {
                Bitmap subBackdrop = Bitmap.createBitmap(bitmap, pL, pT, pW, pH);
                Bitmap blurredBackdrop = FastBlur.blurFast(subBackdrop, blurRadius);

                Path pillPath = new Path();
                pillPath.addRoundRect(pillRect, cornerRadius, cornerRadius, Path.Direction.CW);
                canvas.save();
                canvas.clipPath(pillPath);
                canvas.drawBitmap(blurredBackdrop, pillRect.left, pillRect.top, null);
                canvas.restore();
            }

            // Zero white tint overlay: completely transparent inside
            bgPaint.setColor(Color.TRANSPARENT);

            if (hasCustomBorder) {
                borderPaint.setColor(borderColor);
            } else {
                borderPaint.setColor(Color.parseColor("#44FFFFFF"));
            }

        } else if (isWithering) {
            int intensity = prefs.getFrostingIntensity(widgetType);
            float ratio = Math.max(0.1f, Math.min(1.0f, intensity / 100f));
            int alphaTop = Math.min(220, (int) (ratio * 140f) + 30);
            int alphaBottom = Math.min(240, (int) (ratio * 200f) + 50);

            bgPaint.setShader(new LinearGradient(
                    pillRect.centerX(), pillRect.top,
                    pillRect.centerX(), pillRect.bottom,
                    Color.argb(alphaTop, 18, 24, 34),
                    Color.argb(alphaBottom, 10, 14, 20),
                    Shader.TileMode.CLAMP));

            if (hasCustomBorder) {
                borderPaint.setColor(borderColor);
            } else {
                int rimAlpha = Math.min(255, (int) (ratio * 160f) + 60);
                borderPaint.setColor(Color.argb(rimAlpha, 56, 189, 248));
            }

        } else {
            bgPaint.setColor(theme.backgroundColor);
            borderPaint.setColor(borderColor);
        }

        canvas.drawRoundRect(pillRect, cornerRadius, cornerRadius, bgPaint);
        if (hasBorder) {
            canvas.drawRoundRect(pillRect, cornerRadius, cornerRadius, borderPaint);
        }

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
            previewTitle = "FROSTED GLASS (BLUR " + prefs.getBlurIntensity(widgetType) + "PX)";
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

    public static Bitmap renderPreview(int width, int height, GlyphPrefs prefs, String widgetType) {
        return renderPreview(null, width, height, prefs, widgetType);
    }

    public static Bitmap renderPreview(int width, int height, GlyphPrefs prefs) {
        return renderPreview(null, width, height, prefs, GlyphPrefs.WIDGET_CLOCK_CALENDAR);
    }
}
