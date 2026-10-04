package com.glyph.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.RemoteViews;
import com.glyph.widget.compositor.WidgetCanvas;

/**
 * GlyphWidgetProvider manages the lifecycle, dynamic resizing, and 2D Canvas
 * rendering for the Clock & Calendar widget.
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
     * Renders and updates a specific widget instance using the current user margins and dimensions.
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

        // Query launcher options to get current cell dimensions
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
        int minWidthDp = (options != null) ? options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 250) : 250;
        int minHeightDp = (options != null) ? options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 110) : 110;

        // Render at 2.5x density for ultra-sharp canvas reproduction
        int targetWidth = Math.max((int) (minWidthDp * 2.5f), 720);
        int targetHeight = Math.max((int) (minHeightDp * 2.5f), 320);

        GlyphPrefs prefs = new GlyphPrefs(context);
        Bitmap bitmap = WidgetCanvas.renderWidget(context, targetWidth, targetHeight, prefs);

        views.setImageViewBitmap(R.id.widget_canvas_view, bitmap);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    /**
     * Broadcasts refresh to all active placed widget instances.
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
