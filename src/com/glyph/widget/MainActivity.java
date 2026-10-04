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
 * MainActivity provides the real-time customization workspace for Glyph.
 * Features 4-side independent unoccupied margin sliders and live canvas preview.
 */
public class MainActivity extends Activity {

    private GlyphPrefs prefs;
    private ImageView previewCanvas;

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
        setContentView(R.layout.activity_main);

        prefs = new GlyphPrefs(this);
        bindViews();
        initControls();
        refreshPreview();
    }

    private void bindViews() {
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
    }

    private void initControls() {
        // Initialize values from prefs
        seekMarginLeft.setProgress(prefs.getMarginLeft());
        textMarginLeftVal.setText(prefs.getMarginLeft() + " dp");

        seekMarginTop.setProgress(prefs.getMarginTop());
        textMarginTopVal.setText(prefs.getMarginTop() + " dp");

        seekMarginRight.setProgress(prefs.getMarginRight());
        textMarginRightVal.setText(prefs.getMarginRight() + " dp");

        seekMarginBottom.setProgress(prefs.getMarginBottom());
        textMarginBottomVal.setText(prefs.getMarginBottom() + " dp");

        seekCornerRadius.setProgress(prefs.getCornerRadius());
        textCornerRadiusVal.setText(prefs.getCornerRadius() + " dp");

        // Listeners for real-time updates
        setupSeekBar(seekMarginLeft, textMarginLeftVal, new ValueSetter() {
            @Override
            public void set(int val) {
                prefs.setMarginLeft(val);
            }
        });

        setupSeekBar(seekMarginTop, textMarginTopVal, new ValueSetter() {
            @Override
            public void set(int val) {
                prefs.setMarginTop(val);
            }
        });

        setupSeekBar(seekMarginRight, textMarginRightVal, new ValueSetter() {
            @Override
            public void set(int val) {
                prefs.setMarginRight(val);
            }
        });

        setupSeekBar(seekMarginBottom, textMarginBottomVal, new ValueSetter() {
            @Override
            public void set(int val) {
                prefs.setMarginBottom(val);
            }
        });

        setupSeekBar(seekCornerRadius, textCornerRadiusVal, new ValueSetter() {
            @Override
            public void set(int val) {
                prefs.setCornerRadius(val);
            }
        });

        btnResetMargins.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prefs.resetMargins();
                initControls();
                refreshPreview();
                GlyphWidgetProvider.updateAllWidgets(MainActivity.this);
                Toast.makeText(MainActivity.this, "Margins reset to defaults", Toast.LENGTH_SHORT).show();
            }
        });

        btnApplyWidget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GlyphWidgetProvider.updateAllWidgets(MainActivity.this);
                Toast.makeText(MainActivity.this, "Applied to Home Screen Widget", Toast.LENGTH_SHORT).show();
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
                GlyphWidgetProvider.updateAllWidgets(MainActivity.this);
            }
        });
    }

    private void refreshPreview() {
        Bitmap previewBitmap = WidgetCanvas.renderPreview(720, 360, prefs);
        previewCanvas.setImageBitmap(previewBitmap);
    }
}
