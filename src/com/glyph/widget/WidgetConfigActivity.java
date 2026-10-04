package com.glyph.widget;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.glyph.widget.compositor.CalendarRenderer;
import com.glyph.widget.compositor.WallpaperHelper;
import com.glyph.widget.compositor.WidgetCanvas;
import java.util.ArrayList;
import java.util.List;

/**
 * WidgetConfigActivity provides the dedicated per-widget customizer window.
 * Supports 27 curated color themes, Withering Glass, Frosted Glass (with optical blur),
 * border customizer (thickness & sRGB gamut), 4-side margin controls, and real-time previews.
 */
public class WidgetConfigActivity extends Activity {

    public static final String EXTRA_WIDGET_TYPE = "extra_widget_type";
    private static final int REQUEST_PICK_WALLPAPER = 1001;
    private static final int REQUEST_STORAGE_PERMISSION = 1002;

    private String widgetType = GlyphPrefs.WIDGET_CLOCK_CALENDAR;
    private GlyphPrefs prefs;

    private ImageView previewCanvas;
    private TextView textConfigWidgetTitle;
    private Button btnBackToHub;

    private Spinner spinnerThemes;
    private Button btnPrevTheme;
    private Button btnNextTheme;
    private Button btnSelectWitheringGlass;
    private Button btnSelectFrostedGlass;
    private TextView textThemeSpecs;

    private View cardFrostedIntensity;
    private View layoutBlurControls;
    private View layoutWitheringControls;
    private SeekBar seekBlurIntensity;
    private TextView textBlurIntensityVal;
    private SeekBar seekFrostingIntensity;
    private TextView textFrostingIntensityVal;

    private TextView textWallpaperStatus;
    private Button btnSyncWallpaper;
    private Button btnPickWallpaper;
    private Spinner spinnerWallpaperPosition;

    // Border Customizer fields
    private SeekBar seekBorderThickness;
    private TextView textBorderThicknessVal;
    private View viewBorderColorPreview;
    private TextView textBorderColorHex;
    private TextView textBorderColorStatus;
    private Button btnResetBorderColor;
    private LinearLayout layoutBorderQuickPalette;
    private SeekBar seekHue;
    private TextView textHueVal;
    private SeekBar seekSaturation;
    private TextView textSaturationVal;
    private SeekBar seekValue;
    private TextView textValueVal;
    private boolean isUpdatingBorderFromCode = false;

    // Calendar Studio (Styles & Transform) fields
    private Spinner spinnerCalStyle;
    private TextView textCalStyleCounter;
    private TextView textCalStyleDesc;
    private Button btnPrevCalStyle;
    private Button btnNextCalStyle;
    private SeekBar seekCalX;
    private TextView textCalXVal;
    private SeekBar seekCalY;
    private TextView textCalYVal;
    private SeekBar seekCalScale;
    private TextView textCalScaleVal;
    private Button btnResetCalTransform;

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

    private boolean isInitializingTheme = true;

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
        initThemes();
        initGlassControls();
        initBorderControls();
        initCalendarStudioControls();
        initControls();
        refreshPreview();
    }

    private void bindViews() {
        textConfigWidgetTitle = (TextView) findViewById(R.id.text_config_widget_title);
        btnBackToHub = (Button) findViewById(R.id.btn_back_to_hub);
        previewCanvas = (ImageView) findViewById(R.id.preview_canvas);

        spinnerThemes = (Spinner) findViewById(R.id.spinner_themes);
        btnPrevTheme = (Button) findViewById(R.id.btn_prev_theme);
        btnNextTheme = (Button) findViewById(R.id.btn_next_theme);
        btnSelectWitheringGlass = (Button) findViewById(R.id.btn_select_withering_glass);
        btnSelectFrostedGlass = (Button) findViewById(R.id.btn_select_frosted_glass);
        textThemeSpecs = (TextView) findViewById(R.id.text_theme_specs);

        cardFrostedIntensity = findViewById(R.id.card_frosted_intensity);
        layoutBlurControls = findViewById(R.id.layout_blur_controls);
        layoutWitheringControls = findViewById(R.id.layout_withering_controls);
        seekBlurIntensity = (SeekBar) findViewById(R.id.seek_blur_intensity);
        textBlurIntensityVal = (TextView) findViewById(R.id.text_blur_intensity_val);
        seekFrostingIntensity = (SeekBar) findViewById(R.id.seek_frosting_intensity);
        textFrostingIntensityVal = (TextView) findViewById(R.id.text_frosting_intensity_val);

        textWallpaperStatus = (TextView) findViewById(R.id.text_wallpaper_status);
        btnSyncWallpaper = (Button) findViewById(R.id.btn_sync_wallpaper);
        btnPickWallpaper = (Button) findViewById(R.id.btn_pick_wallpaper);
        spinnerWallpaperPosition = (Spinner) findViewById(R.id.spinner_wallpaper_position);

        seekBorderThickness = (SeekBar) findViewById(R.id.seek_border_thickness);
        textBorderThicknessVal = (TextView) findViewById(R.id.text_border_thickness_val);
        viewBorderColorPreview = findViewById(R.id.view_border_color_preview);
        textBorderColorHex = (TextView) findViewById(R.id.text_border_color_hex);
        textBorderColorStatus = (TextView) findViewById(R.id.text_border_color_status);
        btnResetBorderColor = (Button) findViewById(R.id.btn_reset_border_color);
        layoutBorderQuickPalette = (LinearLayout) findViewById(R.id.layout_border_quick_palette);
        seekHue = (SeekBar) findViewById(R.id.seek_hue);
        textHueVal = (TextView) findViewById(R.id.text_hue_val);
        seekSaturation = (SeekBar) findViewById(R.id.seek_saturation);
        textSaturationVal = (TextView) findViewById(R.id.text_saturation_val);
        seekValue = (SeekBar) findViewById(R.id.seek_value);
        textValueVal = (TextView) findViewById(R.id.text_value_val);

        spinnerCalStyle = (Spinner) findViewById(R.id.spinner_cal_style);
        textCalStyleCounter = (TextView) findViewById(R.id.text_cal_style_counter);
        textCalStyleDesc = (TextView) findViewById(R.id.text_cal_style_desc);
        btnPrevCalStyle = (Button) findViewById(R.id.btn_prev_cal_style);
        btnNextCalStyle = (Button) findViewById(R.id.btn_next_cal_style);

        seekCalX = (SeekBar) findViewById(R.id.seek_cal_x);
        textCalXVal = (TextView) findViewById(R.id.text_cal_x_val);
        seekCalY = (SeekBar) findViewById(R.id.seek_cal_y);
        textCalYVal = (TextView) findViewById(R.id.text_cal_y_val);
        seekCalScale = (SeekBar) findViewById(R.id.seek_cal_scale);
        textCalScaleVal = (TextView) findViewById(R.id.text_cal_scale_val);
        btnResetCalTransform = (Button) findViewById(R.id.btn_reset_cal_transform);

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

    private void initThemes() {
        final List<GlyphTheme.ThemeDef> themes = GlyphTheme.getAllThemes();
        List<String> themeTitles = new ArrayList<String>();
        for (int i = 0; i < themes.size(); i++) {
            themeTitles.add((i + 1) + ". " + themes.get(i).name);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                themeTitles
        );
        spinnerThemes.setAdapter(adapter);

        int currentIdx = GlyphTheme.getThemeIndexById(prefs.getThemeId(widgetType));
        spinnerThemes.setSelection(currentIdx);
        updateThemeSpecsDisplay(themes.get(currentIdx));
        updateGlassCardVisibility(themes.get(currentIdx).id);

        spinnerThemes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isInitializingTheme) {
                    isInitializingTheme = false;
                    return;
                }
                GlyphTheme.ThemeDef selected = themes.get(position);
                prefs.setThemeId(widgetType, selected.id);
                updateThemeSpecsDisplay(selected);
                updateGlassCardVisibility(selected.id);
                updateBorderCustomizerUI();
                refreshPreview();
                GlyphWidgetProvider.updateAllWidgets(WidgetConfigActivity.this);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnPrevTheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int cur = spinnerThemes.getSelectedItemPosition();
                int next = (cur > 0) ? cur - 1 : themes.size() - 1;
                spinnerThemes.setSelection(next);
            }
        });

        btnNextTheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int cur = spinnerThemes.getSelectedItemPosition();
                int next = (cur < themes.size() - 1) ? cur + 1 : 0;
                spinnerThemes.setSelection(next);
            }
        });

        btnSelectWitheringGlass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int witheringIdx = GlyphTheme.getThemeIndexById("withering_glass");
                spinnerThemes.setSelection(witheringIdx);
            }
        });

        btnSelectFrostedGlass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int frostedIdx = GlyphTheme.getThemeIndexById("frosted_glass");
                spinnerThemes.setSelection(frostedIdx);
            }
        });
    }

    private void updateGlassCardVisibility(String themeId) {
        boolean isFrosted = GlyphTheme.isFrosted(themeId);
        boolean isWithering = GlyphTheme.isWithering(themeId);

        if (isFrosted) {
            cardFrostedIntensity.setVisibility(View.VISIBLE);
            layoutBlurControls.setVisibility(View.VISIBLE);
            layoutWitheringControls.setVisibility(View.GONE);
        } else if (isWithering) {
            cardFrostedIntensity.setVisibility(View.VISIBLE);
            layoutBlurControls.setVisibility(View.GONE);
            layoutWitheringControls.setVisibility(View.VISIBLE);
        } else {
            // Completely hide the glass card for all regular themes 1-25
            cardFrostedIntensity.setVisibility(View.GONE);
        }
    }

    private void initGlassControls() {
        int currentBlur = prefs.getBlurIntensity(widgetType);
        seekBlurIntensity.setProgress(currentBlur);
        textBlurIntensityVal.setText(currentBlur + " px");

        seekBlurIntensity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int clamped = Math.max(1, progress);
                textBlurIntensityVal.setText(clamped + " px");
                prefs.setBlurIntensity(widgetType, clamped);
                refreshPreview();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                GlyphWidgetProvider.updateAllWidgets(WidgetConfigActivity.this);
            }
        });

        int currentIntensity = prefs.getFrostingIntensity(widgetType);
        seekFrostingIntensity.setProgress(currentIntensity);
        textFrostingIntensityVal.setText(currentIntensity + " %");

        seekFrostingIntensity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int clamped = Math.max(5, progress);
                textFrostingIntensityVal.setText(clamped + " %");
                prefs.setFrostingIntensity(widgetType, clamped);
                refreshPreview();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                GlyphWidgetProvider.updateAllWidgets(WidgetConfigActivity.this);
            }
        });

        updateWallpaperStatus();

        ArrayAdapter<String> posAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                WallpaperHelper.POSITION_NAMES
        );
        spinnerWallpaperPosition.setAdapter(posAdapter);
        int currentPos = Math.max(0, Math.min(WallpaperHelper.POSITION_NAMES.length - 1, prefs.getWallpaperPosition(widgetType)));
        spinnerWallpaperPosition.setSelection(currentPos);

        spinnerWallpaperPosition.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (prefs.getWallpaperPosition(widgetType) != position) {
                    prefs.setWallpaperPosition(widgetType, position);
                    refreshPreview();
                    GlyphWidgetProvider.updateAllWidgets(WidgetConfigActivity.this);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnSyncWallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSyncWallpaper();
            }
        });

        btnPickWallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select Home Screen Wallpaper"), REQUEST_PICK_WALLPAPER);
            }
        });
    }

    private void updateWallpaperStatus() {
        if (textWallpaperStatus == null) return;
        if (WallpaperHelper.hasCachedWallpaper(this)) {
            textWallpaperStatus.setText("Status: Wallpaper synced (Optical blur active)");
            textWallpaperStatus.setTextColor(getResources().getColor(R.color.status_green));
        } else {
            textWallpaperStatus.setText("Status: Not synced (Using frosted fallback)");
            textWallpaperStatus.setTextColor(getResources().getColor(R.color.text_secondary));
        }
    }

    private void handleSyncWallpaper() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (checkSelfPermission("android.permission.READ_MEDIA_IMAGES") != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{"android.permission.READ_MEDIA_IMAGES"}, REQUEST_STORAGE_PERMISSION);
                return;
            }
        } else if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{"android.permission.READ_EXTERNAL_STORAGE"}, REQUEST_STORAGE_PERMISSION);
                return;
            }
        }

        performWallpaperSync();
    }

    private void performWallpaperSync() {
        boolean success = WallpaperHelper.syncSystemWallpaper(this);
        if (success) {
            updateWallpaperStatus();
            refreshPreview();
            GlyphWidgetProvider.updateAllWidgets(this);
            Toast.makeText(this, "Wallpaper synced successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Could not auto-detect system wallpaper. Tap 'Pick from Gallery' to select it.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                performWallpaperSync();
            } else {
                Toast.makeText(this, "Permission denied. Tap 'Pick from Gallery' instead.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_WALLPAPER && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            boolean success = WallpaperHelper.saveWallpaperFromUri(this, imageUri);
            if (success) {
                updateWallpaperStatus();
                refreshPreview();
                GlyphWidgetProvider.updateAllWidgets(this);
                Toast.makeText(this, "Wallpaper loaded successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to load wallpaper image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initBorderControls() {
        int thickness = prefs.getBorderThickness(widgetType);
        seekBorderThickness.setProgress(thickness);
        updateBorderThicknessDisplay(thickness);

        seekBorderThickness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateBorderThicknessDisplay(progress);
                prefs.setBorderThickness(widgetType, progress);
                updateThemeSpecsDisplay(prefs.getTheme(widgetType));
                refreshPreview();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                GlyphWidgetProvider.updateAllWidgets(WidgetConfigActivity.this);
            }
        });

        // Setup Rainbow spectrum background for Hue SeekBar
        GradientDrawable rainbow = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{
                        0xFFFF0000, 0xFFFFFF00, 0xFF00FF00,
                        0xFF00FFFF, 0xFF0000FF, 0xFFFF00FF, 0xFFFF0000
                }
        );
        rainbow.setCornerRadius(8f);
        seekHue.setBackground(rainbow);
        seekHue.setPadding(16, 12, 16, 12);

        // Populate Quick Palette
        final int[] quickColors = {
                0xFFFFFFFF, // Pure White
                0xFF94A3B8, // Silver Slate
                0xFF334155, // Charcoal
                0xFFEF4444, // Red
                0xFFF59E0B, // Amber
                0xFF10B981, // Emerald
                0xFF38BDF8, // Cyan Blue
                0xFF8B5CF6  // Violet
        };

        layoutBorderQuickPalette.removeAllViews();
        float density = getResources().getDisplayMetrics().density;
        int sizePx = (int) (32 * density);
        int marginPx = (int) (8 * density);

        for (final int color : quickColors) {
            View dot = new View(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(sizePx, sizePx);
            lp.setMargins(0, 0, marginPx, 0);
            dot.setLayoutParams(lp);

            GradientDrawable gd = new GradientDrawable();
            gd.setShape(GradientDrawable.OVAL);
            gd.setColor(color);
            gd.setStroke(2, 0xFF475569);
            dot.setBackground(gd);

            dot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    prefs.setBorderColor(widgetType, color);
                    updateBorderCustomizerUI();
                    updateThemeSpecsDisplay(prefs.getTheme(widgetType));
                    refreshPreview();
                    GlyphWidgetProvider.updateAllWidgets(WidgetConfigActivity.this);
                }
            });

            layoutBorderQuickPalette.addView(dot);
        }

        // HSV SeekBars Listeners
        SeekBar.OnSeekBarChangeListener hsvListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (isUpdatingBorderFromCode) return;

                float hue = seekHue.getProgress();
                float sat = seekSaturation.getProgress() / 100f;
                float val = seekValue.getProgress() / 100f;

                textHueVal.setText(Math.round(hue) + "°");
                textSaturationVal.setText(Math.round(sat * 100f) + "%");
                textValueVal.setText(Math.round(val * 100f) + "%");

                int color = Color.HSVToColor(new float[]{hue, sat, val});
                prefs.setBorderColor(widgetType, color);

                updateBorderSwatchOnly(color, true);
                updateThemeSpecsDisplay(prefs.getTheme(widgetType));
                refreshPreview();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                GlyphWidgetProvider.updateAllWidgets(WidgetConfigActivity.this);
            }
        };

        seekHue.setOnSeekBarChangeListener(hsvListener);
        seekSaturation.setOnSeekBarChangeListener(hsvListener);
        seekValue.setOnSeekBarChangeListener(hsvListener);

        btnResetBorderColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prefs.resetBorderColor(widgetType);
                updateBorderCustomizerUI();
                updateThemeSpecsDisplay(prefs.getTheme(widgetType));
                refreshPreview();
                GlyphWidgetProvider.updateAllWidgets(WidgetConfigActivity.this);
                Toast.makeText(WidgetConfigActivity.this, "Reset border color to theme default", Toast.LENGTH_SHORT).show();
            }
        });

        updateBorderCustomizerUI();
    }

    private void updateBorderThicknessDisplay(int thickness) {
        if (thickness == 0) {
            textBorderThicknessVal.setText("0 dp (Borderless)");
        } else if (thickness == 3) {
            textBorderThicknessVal.setText("3 dp (Default)");
        } else if (thickness >= 12) {
            textBorderThicknessVal.setText(thickness + " dp (Very Thick)");
        } else {
            textBorderThicknessVal.setText(thickness + " dp");
        }
    }

    private void updateBorderSwatchOnly(int color, boolean isCustom) {
        GradientDrawable swatch = new GradientDrawable();
        swatch.setShape(GradientDrawable.OVAL);
        swatch.setColor(color);
        swatch.setStroke(2, 0xFF64748B);
        viewBorderColorPreview.setBackground(swatch);

        textBorderColorHex.setText(String.format("#%06X", (0xFFFFFF & color)));
        if (isCustom) {
            textBorderColorStatus.setText("Custom Override");
            textBorderColorStatus.setTextColor(getResources().getColor(R.color.accent_blue));
            btnResetBorderColor.setVisibility(View.VISIBLE);
        } else {
            textBorderColorStatus.setText("Theme Default");
            textBorderColorStatus.setTextColor(getResources().getColor(R.color.text_secondary));
            btnResetBorderColor.setVisibility(View.GONE);
        }
    }

    private void updateBorderCustomizerUI() {
        if (seekHue == null) return;
        int activeColor = prefs.getBorderColor(widgetType);
        boolean isCustom = prefs.hasCustomBorderColor(widgetType);

        updateBorderSwatchOnly(activeColor, isCustom);

        float[] hsv = new float[3];
        Color.colorToHSV(activeColor, hsv);

        isUpdatingBorderFromCode = true;
        seekHue.setProgress(Math.round(hsv[0]));
        textHueVal.setText(Math.round(hsv[0]) + "°");

        seekSaturation.setProgress(Math.round(hsv[1] * 100f));
        textSaturationVal.setText(Math.round(hsv[1] * 100f) + "%");

        seekValue.setProgress(Math.round(hsv[2] * 100f));
        textValueVal.setText(Math.round(hsv[2] * 100f) + "%");
        isUpdatingBorderFromCode = false;
    }

    private void initCalendarStudioControls() {
        // 1. Calendar Style Selector
        List<String> styleLabels = new ArrayList<String>();
        for (int i = 0; i < CalendarRenderer.STYLE_COUNT; i++) {
            styleLabels.add((i + 1) + ". " + CalendarRenderer.STYLE_NAMES[i]);
        }

        ArrayAdapter<String> styleAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                styleLabels
        );
        spinnerCalStyle.setAdapter(styleAdapter);

        int currentStyle = Math.max(0, Math.min(CalendarRenderer.STYLE_COUNT - 1, prefs.getCalendarStyle(widgetType)));
        spinnerCalStyle.setSelection(currentStyle);
        updateCalendarStyleDisplay(currentStyle);

        spinnerCalStyle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (prefs.getCalendarStyle(widgetType) != position) {
                    prefs.setCalendarStyle(widgetType, position);
                    updateCalendarStyleDisplay(position);
                    refreshPreview();
                    GlyphWidgetProvider.updateAllWidgets(WidgetConfigActivity.this);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnPrevCalStyle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int cur = spinnerCalStyle.getSelectedItemPosition();
                int prev = (cur > 0) ? cur - 1 : CalendarRenderer.STYLE_COUNT - 1;
                spinnerCalStyle.setSelection(prev);
            }
        });

        btnNextCalStyle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int cur = spinnerCalStyle.getSelectedItemPosition();
                int next = (cur < CalendarRenderer.STYLE_COUNT - 1) ? cur + 1 : 0;
                spinnerCalStyle.setSelection(next);
            }
        });

        // 2. Continuous Translation & Scale Zoom Controls
        int calX = prefs.getCalendarX(widgetType);
        int calY = prefs.getCalendarY(widgetType);
        int calScale = prefs.getCalendarScale(widgetType);

        seekCalX.setProgress(calX + 120);
        updateCalXDisplay(calX);

        seekCalY.setProgress(calY + 80);
        updateCalYDisplay(calY);

        seekCalScale.setProgress(calScale - 50);
        textCalScaleVal.setText(calScale + "%");

        seekCalX.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int val = progress - 120;
                updateCalXDisplay(val);
                prefs.setCalendarX(widgetType, val);
                refreshPreview();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                GlyphWidgetProvider.updateAllWidgets(WidgetConfigActivity.this);
            }
        });

        seekCalY.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int val = progress - 80;
                updateCalYDisplay(val);
                prefs.setCalendarY(widgetType, val);
                refreshPreview();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                GlyphWidgetProvider.updateAllWidgets(WidgetConfigActivity.this);
            }
        });

        seekCalScale.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int val = progress + 50;
                textCalScaleVal.setText(val + "%");
                prefs.setCalendarScale(widgetType, val);
                refreshPreview();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                GlyphWidgetProvider.updateAllWidgets(WidgetConfigActivity.this);
            }
        });

        btnResetCalTransform.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prefs.resetCalendarTransform(widgetType);
                int calX = prefs.getCalendarX(widgetType);
                int calY = prefs.getCalendarY(widgetType);
                int calScale = prefs.getCalendarScale(widgetType);
                seekCalX.setProgress(calX + 120);
                updateCalXDisplay(calX);
                seekCalY.setProgress(calY + 80);
                updateCalYDisplay(calY);
                seekCalScale.setProgress(calScale - 50);
                textCalScaleVal.setText(calScale + "%");
                refreshPreview();
                GlyphWidgetProvider.updateAllWidgets(WidgetConfigActivity.this);
                Toast.makeText(WidgetConfigActivity.this, "Reset calendar position & scale", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCalendarStyleDisplay(int index) {
        if (textCalStyleCounter != null) {
            textCalStyleCounter.setText((index + 1) + " / " + CalendarRenderer.STYLE_COUNT);
        }
        if (textCalStyleDesc != null) {
            textCalStyleDesc.setText(CalendarRenderer.STYLE_DESCRIPTIONS[index]);
        }
    }

    private void updateCalXDisplay(int x) {
        if (x == 0) {
            textCalXVal.setText("0 dp (Center)");
        } else if (x > 0) {
            textCalXVal.setText("+" + x + " dp (Right)");
        } else {
            textCalXVal.setText(x + " dp (Left)");
        }
    }

    private void updateCalYDisplay(int y) {
        if (y == 0) {
            textCalYVal.setText("0 dp (Center)");
        } else if (y > 0) {
            textCalYVal.setText("+" + y + " dp (Down)");
        } else {
            textCalYVal.setText(y + " dp (Up)");
        }
    }

    private void updateThemeSpecsDisplay(GlyphTheme.ThemeDef theme) {
        int borderColor = prefs.getBorderColor(widgetType);
        int borderThickness = prefs.getBorderThickness(widgetType);
        boolean customBorder = prefs.hasCustomBorderColor(widgetType);
        int calColor1 = prefs.getCalendarColor1(widgetType);
        int calColor2 = prefs.getCalendarColor2(widgetType);
        int clockColor1 = prefs.getClockColor1(widgetType);
        int clockColor2 = prefs.getClockColor2(widgetType);

        String bgDesc;
        if (GlyphTheme.isFrosted(theme.id)) {
            bgDesc = "100% Transparent + Optical Blur (" + prefs.getBlurIntensity(widgetType) + "px, No Tint)";
        } else if (GlyphTheme.isWithering(theme.id)) {
            bgDesc = "Withering Smoked Glass (" + prefs.getFrostingIntensity(widgetType) + "%)";
        } else {
            bgDesc = String.format("#%06X", (0xFFFFFF & theme.backgroundColor));
        }

        String borderDesc = String.format("#%06X", (0xFFFFFF & borderColor))
                + " (" + borderThickness + "dp" + (customBorder ? ", Custom" : ", Default") + ")";

        String specs = "Active Theme: " + theme.name + "\n"
                + "• Background: " + bgDesc + "\n"
                + "• Border (1): " + borderDesc + "\n"
                + "• Calendar Colors (2): "
                + String.format("#%06X", (0xFFFFFF & calColor1)) + " / "
                + String.format("#%06X", (0xFFFFFF & calColor2)) + "\n"
                + "• Clock Colors (2): "
                + String.format("#%06X", (0xFFFFFF & clockColor1)) + " / "
                + String.format("#%06X", (0xFFFFFF & clockColor2));
        textThemeSpecs.setText(specs);
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
                Toast.makeText(WidgetConfigActivity.this, "Reset margins for " + textConfigWidgetTitle.getText(), Toast.LENGTH_SHORT).show();
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
        Bitmap previewBitmap = WidgetCanvas.renderPreview(this, 720, 360, prefs, widgetType);
        previewCanvas.setImageBitmap(previewBitmap);
    }
}
