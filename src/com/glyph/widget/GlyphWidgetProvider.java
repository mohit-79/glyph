package com.glyph.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.widget.RemoteViews;

/**
 * GlyphWidgetProvider manages the lifecycle, initial rendering, and updates
 * for the Clock & Calendar widget on the Android home screen launcher.
 */
public class GlyphWidgetProvider extends AppWidgetProvider {

    public static final String ACTION_UPDATE_GLYPH = "com.glyph.widget.ACTION_UPDATE_GLYPH";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                                          int appWidgetId, Bundle newOptions) {
        updateWidget(context, appWidgetManager, appWidgetId);
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (ACTION_UPDATE_GLYPH.equals(intent.getAction())) {
            updateAllWidgets(context);
        }
    }

    /**
     * Updates an individual widget instance with a high-resolution 2D Canvas bitmap.
     */
    public static void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_clock_calendar);

        // PendingIntent to launch MainActivity on tap
        Intent clickIntent = new Intent(context, MainActivity.class);
        clickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | (android.os.Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0)
        );
        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent);

        // Render clean initial placeholder bitmap using 2D Canvas
        int width = 720;
        int height = 360;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Background pill
        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(Color.parseColor("#12151C"));
        bgPaint.setStyle(Paint.Style.FILL);

        Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setColor(Color.parseColor("#272E3B"));
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(4f);

        RectF pillRect = new RectF(16f, 16f, width - 16f, height - 16f);
        float radius = 54f;
        canvas.drawRoundRect(pillRect, radius, radius, bgPaint);
        canvas.drawRoundRect(pillRect, radius, radius, borderPaint);

        // Center title & status
        Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setColor(Color.parseColor("#F8FAFC"));
        titlePaint.setTextSize(44f);
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setFakeBoldText(true);
        canvas.drawText("GLYPH", width / 2f, (height / 2f) - 10f, titlePaint);

        Paint subPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        subPaint.setColor(Color.parseColor("#3B82F6"));
        subPaint.setTextSize(22f);
        subPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Clock & Calendar Widget • Active", width / 2f, (height / 2f) + 36f, subPaint);

        // Set rendered bitmap to RemoteViews
        views.setImageViewBitmap(R.id.widget_canvas_view, bitmap);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    /**
     * Helper to broadcast refresh to all active placed widget instances.
     */
    public static void updateAllWidgets(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName component = new ComponentName(context, GlyphWidgetProvider.class);
        int[] ids = manager.getAppWidgetIds(component);
        if (ids != null && ids.length > 0) {
            for (int id : ids) {
                updateWidget(context, manager, id);
            }
        }
    }
}
