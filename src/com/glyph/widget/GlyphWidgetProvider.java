package com.glyph.widget;

import android.app.AlarmManager;
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
        scheduleNextMinuteAlarm(context);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        scheduleNextMinuteAlarm(context);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        cancelMinuteAlarm(context);
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
        String action = intent != null ? intent.getAction() : null;
        if (ACTION_UPDATE_GLYPH.equals(action) ||
                Intent.ACTION_TIME_CHANGED.equals(action) ||
                Intent.ACTION_TIMEZONE_CHANGED.equals(action) ||
                Intent.ACTION_DATE_CHANGED.equals(action) ||
                Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            updateAllWidgets(context);
        }
    }

    /**
     * Renders and updates a specific Clock & Calendar widget instance using its isolated preferences.
     */
    public static void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        if (context == null || appWidgetManager == null) return;
        try {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_clock_calendar);

            // PendingIntent to launch WidgetConfigActivity directly into this widget's isolated settings
            Intent clickIntent = new Intent(context, WidgetConfigActivity.class);
            clickIntent.putExtra(WidgetConfigActivity.EXTRA_WIDGET_TYPE, GlyphPrefs.WIDGET_CLOCK_CALENDAR);
            clickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context, appWidgetId, clickIntent,
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
            Bitmap bitmap = WidgetCanvas.renderWidget(context, targetWidth, targetHeight, prefs, GlyphPrefs.WIDGET_CLOCK_CALENDAR);

            if (bitmap != null) {
                views.setImageViewBitmap(R.id.widget_canvas_view, bitmap);
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
        } catch (Throwable t) {
            android.util.Log.e("GlyphWidgetProvider", "Failed to update widget " + appWidgetId, t);
        }
    }

    /**
     * Broadcasts refresh to all active placed widget instances.
     */
    public static void updateAllWidgets(Context context) {
        if (context == null) return;
        try {
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            if (manager == null) return;
            ComponentName component = new ComponentName(context, GlyphWidgetProvider.class);
            int[] ids = manager.getAppWidgetIds(component);
            if (ids != null && ids.length > 0) {
                for (int id : ids) {
                    try {
                        updateWidget(context, manager, id);
                    } catch (Throwable t) {
                        android.util.Log.e("GlyphWidgetProvider", "Error updating widget instance", t);
                    }
                }
                scheduleNextMinuteAlarm(context);
            }
        } catch (Throwable t) {
            android.util.Log.e("GlyphWidgetProvider", "Failed to update all widgets", t);
        }
    }

    private static boolean canScheduleExactAlarms(AlarmManager am) {
        if (android.os.Build.VERSION.SDK_INT >= 31 && am != null) {
            try {
                java.lang.reflect.Method method = AlarmManager.class.getMethod("canScheduleExactAlarms");
                Object result = method.invoke(am);
                if (result instanceof Boolean) {
                    return (Boolean) result;
                }
            } catch (Throwable ignored) {}
        }
        return android.os.Build.VERSION.SDK_INT < 31;
    }

    public static void scheduleNextMinuteAlarm(Context context) {
        if (context == null) return;
        try {
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (am == null) return;
            Intent intent = new Intent(context, GlyphWidgetProvider.class);
            intent.setAction(ACTION_UPDATE_GLYPH);
            PendingIntent pi = PendingIntent.getBroadcast(
                    context, 999, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | (android.os.Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0)
            );
            long now = System.currentTimeMillis();
            long nextMinute = now + (60000 - (now % 60000));
            if (android.os.Build.VERSION.SDK_INT >= 31) {
                if (canScheduleExactAlarms(am)) {
                    am.setExactAndAllowWhileIdle(AlarmManager.RTC, nextMinute, pi);
                } else {
                    am.set(AlarmManager.RTC, nextMinute, pi);
                }
            } else if (android.os.Build.VERSION.SDK_INT >= 23) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC, nextMinute, pi);
            } else if (android.os.Build.VERSION.SDK_INT >= 19) {
                am.setExact(AlarmManager.RTC, nextMinute, pi);
            } else {
                am.set(AlarmManager.RTC, nextMinute, pi);
            }
        } catch (Throwable t) {
            android.util.Log.e("GlyphWidgetProvider", "Failed to schedule minute alarm", t);
        }
    }

    public static void cancelMinuteAlarm(Context context) {
        if (context == null) return;
        try {
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (am == null) return;
            Intent intent = new Intent(context, GlyphWidgetProvider.class);
            intent.setAction(ACTION_UPDATE_GLYPH);
            PendingIntent pi = PendingIntent.getBroadcast(
                    context, 999, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | (android.os.Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0)
            );
            am.cancel(pi);
        } catch (Throwable t) {
            android.util.Log.e("GlyphWidgetProvider", "Failed to cancel minute alarm", t);
        }
    }
}
