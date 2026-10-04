package com.glyph.widget.compositor;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import java.util.Calendar;

/**
 * CalendarRenderer renders a mathematical 2D Canvas monthly calendar grid.
 * Supports independent position translation (X, Y) and scaling (50% - 180%)
 * without XML layout restrictions or launcher clipping.
 */
public class CalendarRenderer {

    private static final String[] MONTH_NAMES = {
            "JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY", "JUNE",
            "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER"
    };

    private static final String[] DAY_HEADERS = {"S", "M", "T", "W", "T", "F", "S"};

    /**
     * Draws the calendar element onto the canvas inside the pill container.
     *
     * @param canvas Target canvas
     * @param pillRect Bounding box of the widget pill
     * @param scale Canvas DPI scale
     * @param offsetXdp Tuned X offset in dp (-120 to +120)
     * @param offsetYdp Tuned Y offset in dp (-80 to +80)
     * @param scalePercent Tuned scale percentage (50% to 180%)
     * @param primaryColor Primary calendar color (Header, current day badge)
     * @param secondaryColor Secondary calendar color (Day letters, date numbers)
     */
    public static void drawCalendar(Canvas canvas, RectF pillRect, float scale,
                                    float offsetXdp, float offsetYdp, int scalePercent,
                                    int primaryColor, int secondaryColor) {
        if (canvas == null || pillRect == null) return;

        float zoom = Math.max(0.4f, Math.min(2.0f, scalePercent / 100f));
        float anchorX = pillRect.centerX() + (offsetXdp * scale);
        float anchorY = pillRect.centerY() + (offsetYdp * scale);

        canvas.save();
        canvas.translate(anchorX, anchorY);
        canvas.scale(zoom, zoom);

        // Date calculation for current live month
        Calendar today = Calendar.getInstance();
        int currentDay = today.get(Calendar.DAY_OF_MONTH);
        int currentMonth = today.get(Calendar.MONTH);
        int currentYear = today.get(Calendar.YEAR);

        Calendar monthCal = Calendar.getInstance();
        monthCal.set(Calendar.YEAR, currentYear);
        monthCal.set(Calendar.MONTH, currentMonth);
        monthCal.set(Calendar.DAY_OF_MONTH, 1);

        int firstDayOfWeek = monthCal.get(Calendar.DAY_OF_WEEK) - 1; // 0 = Sunday .. 6 = Saturday
        int totalDays = monthCal.getActualMaximum(Calendar.DAY_OF_MONTH);

        // 1. Month & Year Header
        Paint headerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        headerPaint.setColor(primaryColor);
        headerPaint.setTextSize(13f * scale);
        headerPaint.setTextAlign(Paint.Align.CENTER);
        headerPaint.setFakeBoldText(true);

        String monthHeader = MONTH_NAMES[currentMonth] + " " + currentYear;
        canvas.drawText(monthHeader, 0f, -44f * scale, headerPaint);

        // 2. Day-of-Week Initial Row
        Paint dowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dowPaint.setColor(secondaryColor);
        dowPaint.setTextSize(9.5f * scale);
        dowPaint.setTextAlign(Paint.Align.CENTER);
        dowPaint.setFakeBoldText(true);

        float colSpacing = 20f * scale;
        for (int c = 0; c < 7; c++) {
            float cx = (c - 3) * colSpacing;
            canvas.drawText(DAY_HEADERS[c], cx, -28f * scale, dowPaint);
        }

        // 3. Date Grid Numbers
        Paint dayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dayPaint.setTextSize(10f * scale);
        dayPaint.setTextAlign(Paint.Align.CENTER);

        Paint todayBadgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        todayBadgePaint.setStyle(Paint.Style.FILL);
        todayBadgePaint.setColor(primaryColor);

        float rowSpacing = 15f * scale;
        float startGridY = -12f * scale;

        for (int day = 1; day <= totalDays; day++) {
            int slot = firstDayOfWeek + (day - 1);
            int col = slot % 7;
            int row = slot / 7;

            float cx = (col - 3) * colSpacing;
            float cy = startGridY + (row * rowSpacing);

            if (day == currentDay) {
                // Circular active badge for today
                float badgeRadius = 7.5f * scale;
                canvas.drawCircle(cx, cy - (3.5f * scale), badgeRadius, todayBadgePaint);

                // High contrast number inside the active badge
                dayPaint.setColor(getContrastColor(primaryColor));
                dayPaint.setFakeBoldText(true);
            } else {
                dayPaint.setColor(secondaryColor);
                dayPaint.setFakeBoldText(false);
            }

            canvas.drawText(String.valueOf(day), cx, cy, dayPaint);
        }

        canvas.restore();
    }

    /**
     * Determines optimal black or white contrast color for the active day badge.
     */
    private static int getContrastColor(int color) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        double yiq = ((r * 299) + (g * 587) + (b * 114)) / 1000.0;
        return (yiq >= 150) ? Color.parseColor("#0F172A") : Color.WHITE;
    }
}
