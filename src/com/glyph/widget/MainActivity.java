package com.glyph.widget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * MainActivity serves as the central Multi-Widget Hub for Glyph.
 * Lists all available widgets and routes to dedicated, isolated customizers.
 */
public class MainActivity extends Activity {

    private TextView textClockCalendarCount;
    private Button btnCustomizeClockCalendar;
    private Button btnRefreshClockCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindViews();
        setupActions();
        updatePlacedCount();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updatePlacedCount();
    }

    private void bindViews() {
        textClockCalendarCount = (TextView) findViewById(R.id.text_clock_calendar_count);
        btnCustomizeClockCalendar = (Button) findViewById(R.id.btn_customize_clock_calendar);
        btnRefreshClockCalendar = (Button) findViewById(R.id.btn_refresh_clock_calendar);
    }

    private void setupActions() {
        btnCustomizeClockCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, WidgetConfigActivity.class);
                intent.putExtra(WidgetConfigActivity.EXTRA_WIDGET_TYPE, GlyphPrefs.WIDGET_CLOCK_CALENDAR);
                startActivity(intent);
            }
        });

        btnRefreshClockCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GlyphWidgetProvider.updateAllWidgets(MainActivity.this);
                updatePlacedCount();
                Toast.makeText(MainActivity.this, "Clock & Calendar refreshed on Home Screen", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePlacedCount() {
        try {
            AppWidgetManager manager = AppWidgetManager.getInstance(this);
            ComponentName component = new ComponentName(this, GlyphWidgetProvider.class);
            int[] ids = manager.getAppWidgetIds(component);
            int count = (ids != null) ? ids.length : 0;
            if (count > 0) {
                textClockCalendarCount.setText("Placed on home screen: " + count + " active instance(s)");
            } else {
                textClockCalendarCount.setText("Not placed on home screen yet (Long-press home screen to add)");
            }
        } catch (Exception e) {
            textClockCalendarCount.setText("Placed on home screen: Ready for placement");
        }
    }
}
