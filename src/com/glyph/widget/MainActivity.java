package com.glyph.widget;

import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

/**
 * MainActivity serves as the central Multi-Widget Hub for Glyph.
 * Features Nothing OS dot-matrix typography, status bar theming,
 * and developer support actions (UPI, Email, GitHub).
 */
public class MainActivity extends Activity {

    private static final String UPI_ID = "mohitharjani79@oksbi";
    private static final String CONTACT_EMAIL = "mohitharjani79@gmail.com";
    private static final String GITHUB_URL = "https://github.com/mohit-79/glyph";

    private TextView textAppTitle;
    private TextView textClockCalendarCount;
    private Button btnCustomizeClockCalendar;
    private Button btnRefreshClockCalendar;
    private ImageButton btnCoffee;
    private TextView btnQuickCoffee;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applySystemBarTheme();
        setContentView(R.layout.activity_main);

        bindViews();
        applyCustomFont();
        setupActions();
        updatePlacedCount();
    }

    @Override
    protected void onResume() {
        super.onResume();
        applySystemBarTheme();
        updatePlacedCount();
    }

    private void applySystemBarTheme() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(0xFF0D0F14);
            window.setNavigationBarColor(0xFF0D0F14);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                View decor = window.getDecorView();
                int flags = decor.getSystemUiVisibility();
                flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                decor.setSystemUiVisibility(flags);
            }
        }
    }

    private void bindViews() {
        textAppTitle = (TextView) findViewById(R.id.text_app_title);
        textClockCalendarCount = (TextView) findViewById(R.id.text_clock_calendar_count);
        btnCustomizeClockCalendar = (Button) findViewById(R.id.btn_customize_clock_calendar);
        btnRefreshClockCalendar = (Button) findViewById(R.id.btn_refresh_clock_calendar);
        btnCoffee = (ImageButton) findViewById(R.id.btn_coffee);
        btnQuickCoffee = (TextView) findViewById(R.id.btn_quick_coffee);
    }

    private void applyCustomFont() {
        try {
            Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/ndot55.otf");
            if (tf != null && textAppTitle != null) {
                textAppTitle.setTypeface(tf);
            }
        } catch (Exception ignored) {
            // Graceful fallback to default system font
        }
    }

    private void setupActions() {
        if (btnCustomizeClockCalendar != null) {
            btnCustomizeClockCalendar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, WidgetConfigActivity.class);
                    intent.putExtra(WidgetConfigActivity.EXTRA_WIDGET_TYPE, GlyphPrefs.WIDGET_CLOCK_CALENDAR);
                    startActivity(intent);
                }
            });
        }

        if (btnRefreshClockCalendar != null) {
            btnRefreshClockCalendar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    GlyphWidgetProvider.updateAllWidgets(MainActivity.this);
                    updatePlacedCount();
                    Toast.makeText(MainActivity.this, "Clock & Calendar refreshed on Home Screen", Toast.LENGTH_SHORT).show();
                }
            });
        }

        View.OnClickListener coffeeClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCoffeeDialog();
            }
        };

        if (btnCoffee != null) {
            btnCoffee.setOnClickListener(coffeeClickListener);
        }
        if (btnQuickCoffee != null) {
            btnQuickCoffee.setOnClickListener(coffeeClickListener);
        }
    }

    private void showCoffeeDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_coffee, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        final AlertDialog dialog = builder.create();

        // Bind dialog elements
        TextView dialogTitle = (TextView) dialogView.findViewById(R.id.dialog_title);
        try {
            Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/ndot55.otf");
            if (tf != null && dialogTitle != null) {
                dialogTitle.setTypeface(tf);
            }
        } catch (Exception ignored) {}

        Button btnCopyUpi = (Button) dialogView.findViewById(R.id.btn_copy_upi);
        Button btnPayUpi = (Button) dialogView.findViewById(R.id.btn_pay_upi);
        Button btnCopyEmail = (Button) dialogView.findViewById(R.id.btn_copy_email);
        Button btnSendEmail = (Button) dialogView.findViewById(R.id.btn_send_email);
        Button btnStarGithub = (Button) dialogView.findViewById(R.id.btn_star_github);
        Button btnCloseDialog = (Button) dialogView.findViewById(R.id.btn_close_dialog);

        if (btnCopyUpi != null) {
            btnCopyUpi.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    copyToClipboard("Glyph UPI ID", UPI_ID);
                    Toast.makeText(MainActivity.this, "UPI ID copied: " + UPI_ID, Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnPayUpi != null) {
            btnPayUpi.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Uri uri = Uri.parse("upi://pay?pa=" + UPI_ID + "&pn=Mohit%20Harjani&cu=INR");
                        Intent upiIntent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(Intent.createChooser(upiIntent, "Pay via UPI App"));
                    } catch (Exception e) {
                        copyToClipboard("Glyph UPI ID", UPI_ID);
                        Toast.makeText(MainActivity.this, "No UPI app found. UPI ID copied to clipboard: " + UPI_ID, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        if (btnCopyEmail != null) {
            btnCopyEmail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    copyToClipboard("Glyph Contact Email", CONTACT_EMAIL);
                    Toast.makeText(MainActivity.this, "Email copied: " + CONTACT_EMAIL, Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnSendEmail != null) {
            btnSendEmail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                        emailIntent.setData(Uri.parse("mailto:" + CONTACT_EMAIL));
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Glyph Widget Studio Feedback");
                        startActivity(Intent.createChooser(emailIntent, "Send Email"));
                    } catch (Exception e) {
                        copyToClipboard("Glyph Contact Email", CONTACT_EMAIL);
                        Toast.makeText(MainActivity.this, "Email copied: " + CONTACT_EMAIL, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        if (btnStarGithub != null) {
            btnStarGithub.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_URL));
                        startActivity(webIntent);
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "Unable to open browser: " + GITHUB_URL, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        if (btnCloseDialog != null) {
            btnCloseDialog.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
        }

        dialog.show();
    }

    private void copyToClipboard(String label, String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            ClipData clip = ClipData.newPlainText(label, text);
            clipboard.setPrimaryClip(clip);
        }
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
