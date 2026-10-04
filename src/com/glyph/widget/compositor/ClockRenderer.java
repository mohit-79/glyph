package com.glyph.widget.compositor;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import java.util.Calendar;
import java.util.Locale;

/**
 * ClockRenderer renders a mathematical 2D Canvas digital clock.
 * Supports independent position translation (X, Y) and continuous scaling (50% - 250%)
 * without XML layout restrictions or launcher clipping.
 * Single-line guarantee: Time format renders as a single baseline, preventing multi-line digit wrapping.
 */
public class ClockRenderer {

    private static Typeface sBoldTypeface;

    private static Typeface getSafeBoldTypeface() {
        if (sBoldTypeface == null) {
            try {
                sBoldTypeface = Typeface.create("sans-serif", Typeface.BOLD);
            } catch (Throwable t) {
                try {
                    sBoldTypeface = Typeface.defaultFromStyle(Typeface.BOLD);
                } catch (Throwable t2) {
                    sBoldTypeface = Typeface.DEFAULT_BOLD;
                }
            }
            if (sBoldTypeface == null) {
                sBoldTypeface = Typeface.DEFAULT_BOLD;
            }
        }
        return sBoldTypeface;
    }

    /**
     * Backward-compatible overload with 12-hour format default.
     */
    public static void drawClock(Canvas canvas, RectF pillRect, float scale,
                                 float offsetXdp, float offsetYdp, int scalePercent,
                                 int primaryColor, int secondaryColor) {
        drawClock(canvas, pillRect, scale, offsetXdp, offsetYdp, scalePercent,
                primaryColor, secondaryColor, false);
    }

    /**
     * Draws the digital clock element onto the 2D canvas.
     *
     * @param canvas Target canvas
     * @param pillRect Bounding box of the widget pill
     * @param scale Canvas DPI scale
     * @param offsetXdp Tuned X offset in dp (-120 to +120)
     * @param offsetYdp Tuned Y offset in dp (-80 to +80)
     * @param scalePercent Tuned scale percentage (50% to 250%)
     * @param primaryColor Primary clock color (Hour/Minute digits)
     * @param secondaryColor Secondary clock color (Colon separator, AM/PM tag, accents)
     * @param is24HourFormat Whether to render in 24-hour format or 12-hour with AM/PM tag
     */
    public static void drawClock(Canvas canvas, RectF pillRect, float scale,
                                 float offsetXdp, float offsetYdp, int scalePercent,
                                 int primaryColor, int secondaryColor,
                                 boolean is24HourFormat) {
        if (canvas == null || pillRect == null) return;

        try {
            float zoom = Math.max(0.4f, Math.min(3.0f, scalePercent / 100f));
            float anchorX = pillRect.centerX() + (offsetXdp * scale);
            float anchorY = pillRect.centerY() + (offsetYdp * scale);

            canvas.save();
            canvas.translate(anchorX, anchorY);
            canvas.scale(zoom, zoom);

            Calendar now = Calendar.getInstance();
            int hour = is24HourFormat ? now.get(Calendar.HOUR_OF_DAY) : now.get(Calendar.HOUR);
            if (!is24HourFormat && hour == 0) hour = 12;
            int minute = now.get(Calendar.MINUTE);
            String amPm = is24HourFormat ? "" : (now.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM");

            String hourStr = String.format(Locale.US, "%02d", hour);
            String minStr = String.format(Locale.US, "%02d", minute);

            Typeface boldTf = getSafeBoldTypeface();

            // Digits Paint (Primary Color)
            Paint digitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            digitPaint.setColor(primaryColor);
            digitPaint.setTextSize(34f * scale);
            if (boldTf != null) {
                digitPaint.setTypeface(boldTf);
            }

            // Colon Separator Paint (Secondary Accent Color)
            Paint colonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            colonPaint.setColor(secondaryColor);
            colonPaint.setTextSize(32f * scale);
            if (boldTf != null) {
                colonPaint.setTypeface(boldTf);
            }

            // AM/PM Tag Paint
            Paint amPmPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            amPmPaint.setColor(secondaryColor);
            amPmPaint.setTextSize(10f * scale);
            if (boldTf != null) {
                amPmPaint.setTypeface(boldTf);
            }

            // Single-Line Metric Calculation: Measure exact character spans to center on anchor
            float hourWidth = digitPaint.measureText(hourStr);
            float colonWidth = colonPaint.measureText(" : ");
            float minWidth = digitPaint.measureText(minStr);
            float amPmWidth = amPm.isEmpty() ? 0f : amPmPaint.measureText(amPm) + (8f * scale);

            float totalClockWidth = hourWidth + colonWidth + minWidth + amPmWidth;
            float startX = -totalClockWidth / 2f;
            float baselineY = 11f * scale;

            // 1. Draw Hour Digits
            canvas.drawText(hourStr, startX, baselineY, digitPaint);
            float cursorX = startX + hourWidth;

            // 2. Draw Colon Separator
            canvas.drawText(" : ", cursorX, baselineY - (1.5f * scale), colonPaint);
            cursorX += colonWidth;

            // 3. Draw Minute Digits
            canvas.drawText(minStr, cursorX, baselineY, digitPaint);
            cursorX += minWidth;

            // 4. Draw AM / PM Pill Badge
            if (!amPm.isEmpty()) {
                float tagX = cursorX + (6f * scale);
                float tagY = baselineY - (12f * scale);

                RectF amPmPill = new RectF(
                        tagX - (3f * scale),
                        tagY - (10.5f * scale),
                        tagX + amPmPaint.measureText(amPm) + (3f * scale),
                        tagY + (3f * scale)
                );

                Paint tagBg = new Paint(Paint.ANTI_ALIAS_FLAG);
                tagBg.setStyle(Paint.Style.FILL);
                tagBg.setColor(Color.argb(38, Color.red(secondaryColor), Color.green(secondaryColor), Color.blue(secondaryColor)));
                canvas.drawRoundRect(amPmPill, 3.5f * scale, 3.5f * scale, tagBg);

                canvas.drawText(amPm, tagX, tagY, amPmPaint);
            }

            canvas.restore();
        } catch (Throwable t) {
            android.util.Log.e("ClockRenderer", "Failed to render clock", t);
        }
    }
}
