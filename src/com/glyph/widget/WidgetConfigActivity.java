package com.glyph.widget;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.glyph.widget.compositor.WidgetCanvas;

/**
 * WidgetConfigActivity provides the dedicated per-widget customizer window.
 * Ensures margins and settings apply exclusively to the selected widget type.
 */
public class WidgetConfigActivity extends Activity {

    public static final String EXTRA_WIDGET_TYPE = "extra_widget_type";

    private String widgetType = GlyphPrefs.WIDGET_CLOCK_CALENDAR;
    private GlyphPrefs prefs;

    private ImageView previewCanvas;
    private TextView textConfigWidgetTitle;
    private Button btnBackToHub;

    private SeekBar seekMarginLeft;
    private SeekBar seekMarginTop;
    private SeekBar seekMarginRight;
    private SeekBar seekMarginBottom;
    private SeekBar seekCornerRadius;

    private TextView textMarginLeftVal;
    private TextView textMarginTopVal;
    private TextView textMarginRightVal;
    private TextView textMarginBottomVal;
    private TextView textCornerRadiusVal;

    private Button btnResetMargins;
    private Button btnApplyWidget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget_config);

        String passedType = getIntent().getStringExtra(EXTRA_WIDGET_TYPE);
        if (passedType != null && !passedType.isEmpty()) {
            this.widgetType = passedType;
        }

        prefs = new GlyphPrefs(this);
        bindViews();
        initControls();
        refreshPreview();
    }

    private void bindViews() {
        textConfigWidgetTitle = (TextView) findViewById(R.id.text_config_widget_title);
        btnBackToHub = (Button) findViewById(R.id.btn_back_to_hub);
        previewCanvas = (ImageView) findViewById(R.id.preview_canvas);

        seekMarginLeft = (SeekBar) findViewById(R.id.seek_margin_left);
        seekMarginTop = (SeekBar) findViewById(R.id.seek_margin_top);
        seekMarginRight = (SeekBar) findViewById(R.id.seek_margin_right);
        seekMarginBottom = (SeekBar) findViewById(R.id.seek_margin_bottom);
        seekCornerRadius = (SeekBar) findViewById(R.id.seek_corner_radius);

        textMarginLeftVal = (TextView) findViewById(R.id.text_margin_left_val);
        textMarginTopVal = (TextView) findViewById(R.id.text_margin_top_val);
        textMarginRightVal = (TextView) findViewById(R.id.text_margin_right_val);
        textMarginBottomVal = (TextView) findViewById(R.id.text_margin_bottom_val);
        textCornerRadiusVal = (TextView) findViewById(R.id.text_corner_radius_val);

        btnResetMargins = (Button) findViewById(R.id.btn_reset_margins);
        btnApplyWidget = (Button) findViewById(R.id.btn_apply_widget);

        if (GlyphPrefs.WIDGET_CLOCK_CALENDAR.equals(widgetType)) {
            textConfigWidgetTitle.setText("Clock & Calendar");
        } else {
            textConfigWidgetTitle.setText(widgetType);
        }

        btnBackToHub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initControls() {
        seekMarginLeft.setProgress(prefs.getMarginLeft(widgetType));
        textMarginLeftVal.setText(prefs.getMarginLeft(widgetType) + " dp");

        seekMarginTop.setProgress(prefs.getMarginTop(widgetType));
        textMarginTopVal.setText(prefs.getMarginTop(widgetType) + " dp");

        seekMarginRight.setProgress(prefs.getMarginRight(widgetType));
        textMarginRightVal.setText(prefs.getMarginRight(widgetType) + " dp");

        seekMarginBottom.setProgress(prefs.getMarginBottom(widgetType));
        textMarginBottomVal.setText(prefs.getMarginBottom(widgetType) + " dp");

        seekCornerRadius.setProgress(prefs.getCornerRadius(widgetType));
        textCornerRadiusVal.setText(prefs.getCornerRadius(widgetType) + " dp");

        setupSeekBar(seekMarginLeft, textMarginLeftVal, new ValueSetter() {
            @Override
            public void set(int val) {
                prefs.setMarginLeft(widgetType, val);
            }
        });

        setupSeekBar(seekMarginTop, textMarginTopVal, new ValueSetter() {
            @Override
            public void set(int val) {
                prefs.setMarginTop(widgetType, val);
            }
        });

        setupSeekBar(seekMarginRight, textMarginRightVal, new ValueSetter() {
            @Override
            public void set(int val) {
                prefs.setMarginRight(widgetType, val);
            }
        });

        setupSeekBar(seekMarginBottom, textMarginBottomVal, new ValueSetter() {
            @Override
            public void set(int val) {
                prefs.setMarginBottom(widgetType, val);
            }
        });

        setupSeekBar(seekCornerRadius, textCornerRadiusVal, new ValueSetter() {
            @Override
            public void set(int val) {
                prefs.setCornerRadius(widgetType, val);
            }
        });

        btnResetMargins.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prefs.resetMargins(widgetType);
                initControls();
                refreshPreview();
                GlyphWidgetProvider.updateAllWidgets(WidgetConfigActivity.this);
                Toast.makeText(WidgetConfigActivity.this, "Reset to defaults for " + textConfigWidgetTitle.getText(), Toast.LENGTH_SHORT).show();
            }
        });

        btnApplyWidget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GlyphWidgetProvider.updateAllWidgets(WidgetConfigActivity.this);
                Toast.makeText(WidgetConfigActivity.this, "Applied to " + textConfigWidgetTitle.getText() + " Widget", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private interface ValueSetter {
        void set(int val);
    }

    private void setupSeekBar(SeekBar seekBar, final TextView display, final ValueSetter setter) {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                display.setText(progress + " dp");
                setter.set(progress);
                refreshPreview();
            }

            @Override
            public void onStartTrackingTouch(SeekBar sb) {}

            @Override
            public void onStopTrackingTouch(SeekBar sb) {
                GlyphWidgetProvider.updateAllWidgets(WidgetConfigActivity.this);
            }
        });
    }

    private void refreshPreview() {
        Bitmap previewBitmap = WidgetCanvas.renderPreview(720, 360, prefs, widgetType);
        previewCanvas.setImageBitmap(previewBitmap);
    }
}
