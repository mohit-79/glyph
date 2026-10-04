package com.glyph.widget.compositor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
import com.glyph.widget.GlyphPrefs;

/**
 * WidgetCanvas renders the 2D Canvas bitmap for both launcher RemoteViews
 * and in-app real-time previews, strictly enforcing 4-side unoccupied margins.
 */
public class WidgetCanvas {

    /**
     * Renders the production widget bitmap for the home screen launcher.
     */
    public static Bitmap renderWidget(Context context, int width, int height, GlyphPrefs prefs) {
        if (width <= 0) width = 720;
        if (height <= 0) height = 360;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Density scale factor for high-resolution rendering
        float scale = width / 360f;

        float marginLeft = prefs.getMarginLeft() * scale;
        float marginTop = prefs.getMarginTop() * scale;
        float marginRight = prefs.getMarginRight() * scale;
        float marginBottom = prefs.getMarginBottom() * scale;
        float cornerRadius = prefs.getCornerRadius() * scale;

        // Bounding box for the inner pill based on 4-side margins
        float pillLeft = Math.max(0, marginLeft);
        float pillTop = Math.max(0, marginTop);
        float pillRight = Math.min(width, width - marginRight);
        float pillBottom = Math.min(height, height - marginBottom);

        // Prevent negative or inverted dimensions
        if (pillRight <= pillLeft + 40f) {
            pillRight = pillLeft + 40f;
        }
        if (pillBottom <= pillTop + 40f) {
            pillBottom = pillTop + 40f;
        }

        RectF pillRect = new RectF(pillLeft, pillTop, pillRight, pillBottom);

        // Outer transparent background (ensures unoccupied margins are transparent)
        canvas.drawColor(Color.TRANSPARENT);

        // Background pill
        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(Color.parseColor("#12151C"));
        bgPaint.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(pillRect, cornerRadius, cornerRadius, bgPaint);

        // Border
        Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setColor(Color.parseColor("#272E3B"));
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(3f * scale);
        canvas.drawRoundRect(pillRect, cornerRadius, cornerRadius, borderPaint);

        // Center typography indicator
        Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setColor(Color.parseColor("#F8FAFC"));
        titlePaint.setTextSize(36f * scale);
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setFakeBoldText(true);

        float centerX = pillRect.centerX();
        float centerY = pillRect.centerY();
        canvas.drawText("GLYPH", centerX, centerY - (8f * scale), titlePaint);

        Paint subPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        subPaint.setColor(Color.parseColor("#3B82F6"));
        subPaint.setTextSize(16f * scale);
        subPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Pill Margin: L" + prefs.getMarginLeft() + " T" + prefs.getMarginTop()
                + " R" + prefs.getMarginRight() + " B" + prefs.getMarginBottom(),
                centerX, centerY + (24f * scale), subPaint);

        return bitmap;
    }

    /**
     * Renders the interactive preview bitmap with a dashed bounding box
     * to visualize the unoccupied launcher space in the app settings screen.
     */
    public static Bitmap renderPreview(int width, int height, GlyphPrefs prefs) {
        if (width <= 0) width = 720;
        if (height <= 0) height = 360;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        float scale = width / 360f;

        // Faint outer bounds representing home screen cell boundary
        Paint cellBoundaryPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cellBoundaryPaint.setColor(Color.parseColor("#2A3142"));
        cellBoundaryPaint.setStyle(Paint.Style.STROKE);
        cellBoundaryPaint.setStrokeWidth(1.5f * scale);
        cellBoundaryPaint.setPathEffect(new DashPathEffect(new float[]{10f * scale, 8f * scale}, 0));

        RectF cellRect = new RectF(4f * scale, 4f * scale, width - (4f * scale), height - (4f * scale));
        canvas.drawRoundRect(cellRect, 16f * scale, 16f * scale, cellBoundaryPaint);

        // Inner pill bounds
        float marginLeft = prefs.getMarginLeft() * scale;
        float marginTop = prefs.getMarginTop() * scale;
        float marginRight = prefs.getMarginRight() * scale;
        float marginBottom = prefs.getMarginBottom() * scale;
        float cornerRadius = prefs.getCornerRadius() * scale;

        float pillLeft = Math.max(0, marginLeft);
        float pillTop = Math.max(0, marginTop);
        float pillRight = Math.min(width, width - marginRight);
        float pillBottom = Math.min(height, height - marginBottom);

        if (pillRight <= pillLeft + 40f) pillRight = pillLeft + 40f;
        if (pillBottom <= pillTop + 40f) pillBottom = pillTop + 40f;

        RectF pillRect = new RectF(pillLeft, pillTop, pillRight, pillBottom);

        // Fill pill
        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(Color.parseColor("#12151C"));
        bgPaint.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(pillRect, cornerRadius, cornerRadius, bgPaint);

        // Pill border
        Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setColor(Color.parseColor("#3B82F6"));
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(2.5f * scale);
        canvas.drawRoundRect(pillRect, cornerRadius, cornerRadius, borderPaint);

        // Center preview labels
        Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setColor(Color.parseColor("#F8FAFC"));
        titlePaint.setTextSize(32f * scale);
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setFakeBoldText(true);

        float centerX = pillRect.centerX();
        float centerY = pillRect.centerY();
        canvas.drawText("GLYPH PILL", centerX, centerY - (6f * scale), titlePaint);

        Paint subPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        subPaint.setColor(Color.parseColor("#94A3B8"));
        subPaint.setTextSize(14f * scale);
        subPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Unoccupied Margin Tuner", centerX, centerY + (20f * scale), subPaint);

        return bitmap;
    }
}
