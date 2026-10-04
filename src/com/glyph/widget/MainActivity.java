package com.glyph.widget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * MainActivity provides the multi-widget management hub for Glyph.
 */
public class MainActivity extends Activity {

    private TextView textWidgetPlacedCount;
    private Button btnRefreshWidget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textWidgetPlacedCount = (TextView) findViewById(R.id.text_widget_placed_count);
        btnRefreshWidget = (Button) findViewById(R.id.btn_refresh_widget);

        btnRefreshWidget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GlyphWidgetProvider.updateAllWidgets(MainActivity.this);
                updatePlacedCount();
                Toast.makeText(MainActivity.this, "Widget refreshed on Home Screen", Toast.LENGTH_SHORT).show();
            }
        });

        updatePlacedCount();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updatePlacedCount();
    }

    private void updatePlacedCount() {
        try {
            AppWidgetManager manager = AppWidgetManager.getInstance(this);
            ComponentName component = new ComponentName(this, GlyphWidgetProvider.class);
            int[] ids = manager.getAppWidgetIds(component);
            int count = (ids != null) ? ids.length : 0;
            if (count > 0) {
                textWidgetPlacedCount.setText("Placed on home screen: " + count + " active instance(s)");
            } else {
                textWidgetPlacedCount.setText("Not placed on home screen yet (Long-press home screen to add)");
            }
        } catch (Exception e) {
            textWidgetPlacedCount.setText("Placed on home screen: Ready for placement");
        }
    }
}
