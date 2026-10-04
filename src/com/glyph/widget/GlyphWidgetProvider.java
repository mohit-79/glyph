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
    public static final String ACTION_CYCLE_THEME = "com.glyph.widget.ACTION_CYCLE_THEME";
    public static final String ACTION_CYCLE_CLOCK_STYLE = "com.glyph.widget.ACTION_CYCLE_CLOCK_STYLE";
    public static final String ACTION_CYCLE_CAL_STYLE = "com.glyph.widget.ACTION_CYCLE_CAL_STYLE";
    public static final String ACTION_INERT = "com.glyph.widget.ACTION_INERT";
    public static final String EXTRA_WIDGET_TYPE = "widget_type";

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
        if (action == null) return;

        if (ACTION_CYCLE_THEME.equals(action)) {
            String type = intent.getStringExtra(EXTRA_WIDGET_TYPE);
            if (type == null) type = GlyphPrefs.WIDGET_CLOCK_CALENDAR;
            GlyphPrefs prefs = new GlyphPrefs(context);
            prefs.cycleNextTheme(type);
            updateAllWidgets(context);
        } else if (ACTION_CYCLE_CLOCK_STYLE.equals(action)) {
            String type = intent.getStringExtra(EXTRA_WIDGET_TYPE);
            if (type == null) type = GlyphPrefs.WIDGET_CLOCK_CALENDAR;
            GlyphPrefs prefs = new GlyphPrefs(context);
            prefs.cycleNextClockStyle(type);
            updateAllWidgets(context);
        } else if (ACTION_CYCLE_CAL_STYLE.equals(action)) {
            String type = intent.getStringExtra(EXTRA_WIDGET_TYPE);
            if (type == null) type = GlyphPrefs.WIDGET_CLOCK_CALENDAR;
            GlyphPrefs prefs = new GlyphPrefs(context);
            prefs.cycleNextCalendarStyle(type);
            updateAllWidgets(context);
        } else if (ACTION_INERT.equals(action)) {
            // Inert tap: explicitly do nothing
        } else if (ACTION_UPDATE_GLYPH.equals(action) ||
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
            GlyphPrefs prefs = new GlyphPrefs(context);

            int piFlag = PendingIntent.FLAG_UPDATE_CURRENT | (android.os.Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0);

            // Fallback PendingIntent on widget_root: opens WidgetConfigActivity
            Intent clickIntent = new Intent(context, WidgetConfigActivity.class);
            clickIntent.putExtra(WidgetConfigActivity.EXTRA_WIDGET_TYPE, GlyphPrefs.WIDGET_CLOCK_CALENDAR);
            clickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, clickIntent, piFlag);
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent);

            // Configure Left Touch Area (Clock)
            setupTouchAction(context, views, R.id.widget_click_clock, appWidgetId * 10 + 1,
                    prefs.getClockClickAction(GlyphPrefs.WIDGET_CLOCK_CALENDAR),
                    true, prefs, GlyphPrefs.WIDGET_CLOCK_CALENDAR, piFlag);

            // Configure Right Touch Area (Calendar)
            setupTouchAction(context, views, R.id.widget_click_calendar, appWidgetId * 10 + 2,
                    prefs.getCalendarClickAction(GlyphPrefs.WIDGET_CLOCK_CALENDAR),
                    false, prefs, GlyphPrefs.WIDGET_CLOCK_CALENDAR, piFlag);

            // Query launcher options to get current cell dimensions
            Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
            int minWidthDp = (options != null) ? options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 250) : 250;
            int minHeightDp = (options != null) ? options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 110) : 110;

            // Render at 2.5x density for ultra-sharp canvas reproduction
            int targetWidth = Math.max((int) (minWidthDp * 2.5f), 720);
            int targetHeight = Math.max((int) (minHeightDp * 2.5f), 320);

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

    private static void setupTouchAction(Context context, RemoteViews views, int viewId, int requestCode,
                                         int actionType, boolean isClock, GlyphPrefs prefs, String widgetType, int piFlag) {
        try {
            PendingIntent pi = null;
            switch (actionType) {
                case GlyphPrefs.CLICK_ACTION_INERT: {
                    Intent inertIntent = new Intent(context, GlyphWidgetProvider.class);
                    inertIntent.setAction(ACTION_INERT);
                    pi = PendingIntent.getBroadcast(context, requestCode, inertIntent, piFlag);
                    break;
                }
                case GlyphPrefs.CLICK_ACTION_DEFAULT_APP: {
                    Intent appIntent = isClock ? getDefaultClockIntent(context) : getDefaultCalendarIntent(context);
                    pi = PendingIntent.getActivity(context, requestCode, appIntent, piFlag);
                    break;
                }
                case GlyphPrefs.CLICK_ACTION_OPEN_STUDIO: {
                    Intent studioIntent = new Intent(context, WidgetConfigActivity.class);
                    studioIntent.putExtra(WidgetConfigActivity.EXTRA_WIDGET_TYPE, widgetType);
                    studioIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    pi = PendingIntent.getActivity(context, requestCode, studioIntent, piFlag);
                    break;
                }
                case GlyphPrefs.CLICK_ACTION_CUSTOM_APP: {
                    String customPkg = isClock ? prefs.getClockCustomAppPackage(widgetType) : prefs.getCalendarCustomAppPackage(widgetType);
                    Intent customIntent = null;
                    if (customPkg != null && !customPkg.isEmpty()) {
                        customIntent = context.getPackageManager().getLaunchIntentForPackage(customPkg);
                    }
                    if (customIntent != null) {
                        customIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    } else {
                        customIntent = isClock ? getDefaultClockIntent(context) : getDefaultCalendarIntent(context);
                    }
                    pi = PendingIntent.getActivity(context, requestCode, customIntent, piFlag);
                    break;
                }
                case GlyphPrefs.CLICK_ACTION_CYCLE_THEME: {
                    Intent themeIntent = new Intent(context, GlyphWidgetProvider.class);
                    themeIntent.setAction(ACTION_CYCLE_THEME);
                    themeIntent.putExtra(EXTRA_WIDGET_TYPE, widgetType);
                    pi = PendingIntent.getBroadcast(context, requestCode, themeIntent, piFlag);
                    break;
                }
                case GlyphPrefs.CLICK_ACTION_CYCLE_STYLE: {
                    Intent styleIntent = new Intent(context, GlyphWidgetProvider.class);
                    styleIntent.setAction(isClock ? ACTION_CYCLE_CLOCK_STYLE : ACTION_CYCLE_CAL_STYLE);
                    styleIntent.putExtra(EXTRA_WIDGET_TYPE, widgetType);
                    pi = PendingIntent.getBroadcast(context, requestCode, styleIntent, piFlag);
                    break;
                }
                default: {
                    Intent defaultIntent = isClock ? getDefaultClockIntent(context) : getDefaultCalendarIntent(context);
                    pi = PendingIntent.getActivity(context, requestCode, defaultIntent, piFlag);
                    break;
                }
            }
            if (pi != null) {
                views.setOnClickPendingIntent(viewId, pi);
            }
        } catch (Throwable t) {
            android.util.Log.e("GlyphWidgetProvider", "Failed to setup touch action for view " + viewId, t);
        }
    }

    public static Intent getDefaultClockIntent(Context context) {
        try {
            Intent alarmIntent = new Intent(android.provider.AlarmClock.ACTION_SHOW_ALARMS);
            alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (alarmIntent.resolveActivity(context.getPackageManager()) != null) {
                return alarmIntent;
            }
        } catch (Throwable ignored) {}

        try {
            Intent dockIntent = new Intent(Intent.ACTION_MAIN);
            dockIntent.addCategory(Intent.CATEGORY_DESK_DOCK);
            dockIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (dockIntent.resolveActivity(context.getPackageManager()) != null) {
                return dockIntent;
            }
        } catch (Throwable ignored) {}

        String[] knownClockPackages = {
                "com.google.android.deskclock",
                "com.android.deskclock",
                "com.sec.android.app.clockpackage",
                "com.miui.clock",
                "com.coloros.alarmclock",
                "com.oppo.alarmclock",
                "com.oneplus.deskclock",
                "com.huawei.android.alarmclock",
                "com.asus.deskclock"
        };
        for (String pkg : knownClockPackages) {
            try {
                Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(pkg);
                if (launchIntent != null) {
                    launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    return launchIntent;
                }
            } catch (Throwable ignored) {}
        }

        Intent fallback = new Intent(context, WidgetConfigActivity.class);
        fallback.putExtra(WidgetConfigActivity.EXTRA_WIDGET_TYPE, GlyphPrefs.WIDGET_CLOCK_CALENDAR);
        fallback.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return fallback;
    }

    public static Intent getDefaultCalendarIntent(Context context) {
        try {
            android.net.Uri.Builder builder = android.provider.CalendarContract.CONTENT_URI.buildUpon();
            builder.appendPath("time");
            android.content.ContentUris.appendId(builder, System.currentTimeMillis());
            Intent calIntent = new Intent(Intent.ACTION_VIEW, builder.build());
            calIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (calIntent.resolveActivity(context.getPackageManager()) != null) {
                return calIntent;
            }
        } catch (Throwable ignored) {}

        String[] knownCalendarPackages = {
                "com.google.android.calendar",
                "com.android.calendar",
                "com.samsung.android.calendar",
                "com.miui.calendar",
                "com.coloros.calendar",
                "com.oneplus.calendar",
                "com.huawei.calendar"
        };
        for (String pkg : knownCalendarPackages) {
            try {
                Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(pkg);
                if (launchIntent != null) {
                    launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    return launchIntent;
                }
            } catch (Throwable ignored) {}
        }

        Intent fallback = new Intent(context, WidgetConfigActivity.class);
        fallback.putExtra(WidgetConfigActivity.EXTRA_WIDGET_TYPE, GlyphPrefs.WIDGET_CLOCK_CALENDAR);
        fallback.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return fallback;
    }
}
