package com.glyph.widget.compositor;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import java.util.Calendar;

/**
 * CalendarRenderer renders a mathematical 2D Canvas monthly calendar grid.
 * Supports 11 distinct artistic reference styles, independent position translation (X, Y),
 * and continuous scaling (50% - 180%) without XML layout restrictions or launcher clipping.
 */
public class CalendarRenderer {

    // 11 Distinct Artistic Calendar Reference Styles
    public static final int STYLE_PILL_RANGE = 0;
    public static final int STYLE_CIRCLE_BUBBLES = 1;
    public static final int STYLE_CLEAN_MONOSPACE = 2;
    public static final int STYLE_NEOMORPHIC_CARD = 3;
    public static final int STYLE_MODULAR_SQUARES = 4;
    public static final int STYLE_SUNKEN_DIAL = 5;
    public static final int STYLE_DOTTED_ACCENT = 6;
    public static final int STYLE_COMPACT_HEADERLESS = 7;
    public static final int STYLE_SEGMENTED_ROW = 8;
    public static final int STYLE_HIGH_CONTRAST_GRID = 9;
    public static final int STYLE_MODERN_SANS = 10;
    public static final int STYLE_COUNT = 11;

    public static final String[] STYLE_NAMES = {
            "Pill Range",
            "Circle Bubble Cluster",
            "Clean Monospace",
            "Neomorphic Frosted Card",
            "Modular Rounded Square Grid",
            "Inset Sunken Dial Hybrid",
            "Dotted Accent Calendar",
            "Compact Headerless",
            "Segmented Row Focus",
            "High-Contrast Dark Grid",
            "Modern Sans Grid"
    };

    public static final String[] STYLE_DESCRIPTIONS = {
            "Curved horizontal capsule pill highlighting the active week range",
            "Dual-state translucent circular bubble tiles with filled pop badge",
            "Minimalist high-fashion typographic grid with monospace alignment",
            "Soft frosted sub-card backing with recessed pill badges",
            "Modular rounded square tile matrix with uniform spacing",
            "Asymmetric layout with sunken circular month-progress dial gauge",
            "Micro-dot matrix indicators with concentric ring active badge",
            "Dense compact layout with expanded glanceable date numerals",
            "High-contrast week-row focus with dimmed context weeks",
            "Bold graphic badges with dual-ring active indicator",
            "Clean geometric sans typography with two-tone weekend accenting"
    };

    private static final String[] MONTH_NAMES = {
            "JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY", "JUNE",
            "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER"
    };

    private static final String[] DAY_HEADERS = {"S", "M", "T", "W", "T", "F", "S"};

    /**
     * Backward-compatible overload defaulting to Style 0 (Pill Range).
     */
    public static void drawCalendar(Canvas canvas, RectF pillRect, float scale,
                                    float offsetXdp, float offsetYdp, int scalePercent,
                                    int primaryColor, int secondaryColor) {
        drawCalendar(canvas, pillRect, scale, offsetXdp, offsetYdp, scalePercent,
                primaryColor, secondaryColor, STYLE_PILL_RANGE);
    }

    /**
     * Draws the selected artistic calendar style onto the 2D canvas.
     *
     * @param canvas Target canvas
     * @param pillRect Bounding box of the widget pill
     * @param scale Canvas DPI scale
     * @param offsetXdp Tuned X offset in dp (-120 to +120)
     * @param offsetYdp Tuned Y offset in dp (-80 to +80)
     * @param scalePercent Tuned scale percentage (50% to 180%)
     * @param primaryColor Primary calendar color (Header, current day badge)
     * @param secondaryColor Secondary calendar color (Day letters, date numbers)
     * @param styleIndex Selected style index (0 to 10)
     */
    public static void drawCalendar(Canvas canvas, RectF pillRect, float scale,
                                    float offsetXdp, float offsetYdp, int scalePercent,
                                    int primaryColor, int secondaryColor, int styleIndex) {
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

        int safeStyle = Math.max(0, Math.min(STYLE_COUNT - 1, styleIndex));

        switch (safeStyle) {
            case STYLE_PILL_RANGE:
                renderPillRange(canvas, scale, currentDay, currentMonth, currentYear,
                        firstDayOfWeek, totalDays, primaryColor, secondaryColor);
                break;
            case STYLE_CIRCLE_BUBBLES:
                renderCircleBubbles(canvas, scale, currentDay, currentMonth, currentYear,
                        firstDayOfWeek, totalDays, primaryColor, secondaryColor);
                break;
            case STYLE_CLEAN_MONOSPACE:
                renderCleanMonospace(canvas, scale, currentDay, currentMonth, currentYear,
                        firstDayOfWeek, totalDays, primaryColor, secondaryColor);
                break;
            case STYLE_NEOMORPHIC_CARD:
                renderNeomorphicCard(canvas, scale, currentDay, currentMonth, currentYear,
                        firstDayOfWeek, totalDays, primaryColor, secondaryColor);
                break;
            case STYLE_MODULAR_SQUARES:
                renderModularSquares(canvas, scale, currentDay, currentMonth, currentYear,
                        firstDayOfWeek, totalDays, primaryColor, secondaryColor);
                break;
            case STYLE_SUNKEN_DIAL:
                renderSunkenDial(canvas, scale, currentDay, currentMonth, currentYear,
                        firstDayOfWeek, totalDays, primaryColor, secondaryColor);
                break;
            case STYLE_DOTTED_ACCENT:
                renderDottedAccent(canvas, scale, currentDay, currentMonth, currentYear,
                        firstDayOfWeek, totalDays, primaryColor, secondaryColor);
                break;
            case STYLE_COMPACT_HEADERLESS:
                renderCompactHeaderless(canvas, scale, currentDay, currentMonth, currentYear,
                        firstDayOfWeek, totalDays, primaryColor, secondaryColor);
                break;
            case STYLE_SEGMENTED_ROW:
                renderSegmentedRow(canvas, scale, currentDay, currentMonth, currentYear,
                        firstDayOfWeek, totalDays, primaryColor, secondaryColor);
                break;
            case STYLE_HIGH_CONTRAST_GRID:
                renderHighContrastGrid(canvas, scale, currentDay, currentMonth, currentYear,
                        firstDayOfWeek, totalDays, primaryColor, secondaryColor);
                break;
            case STYLE_MODERN_SANS:
            default:
                renderModernSans(canvas, scale, currentDay, currentMonth, currentYear,
                        firstDayOfWeek, totalDays, primaryColor, secondaryColor);
                break;
        }

        canvas.restore();
    }

    // --- Style 0: Pill Range ---
    private static void renderPillRange(Canvas canvas, float scale, int currentDay, int currentMonth, int currentYear,
                                        int firstDayOfWeek, int totalDays, int primaryColor, int secondaryColor) {
        float colSpacing = 20f * scale;
        float rowSpacing = 15f * scale;
        float startGridY = -12f * scale;

        // Month Header
        Paint headerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        headerPaint.setColor(primaryColor);
        headerPaint.setTextSize(13f * scale);
        headerPaint.setTextAlign(Paint.Align.CENTER);
        headerPaint.setFakeBoldText(true);
        canvas.drawText(MONTH_NAMES[currentMonth] + " " + currentYear, 0f, -44f * scale, headerPaint);

        // Day Headers
        Paint dowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dowPaint.setColor(secondaryColor);
        dowPaint.setTextSize(9.5f * scale);
        dowPaint.setTextAlign(Paint.Align.CENTER);
        dowPaint.setFakeBoldText(true);
        for (int c = 0; c < 7; c++) {
            canvas.drawText(DAY_HEADERS[c], (c - 3) * colSpacing, -28f * scale, dowPaint);
        }

        // Horizontal pill capsule embracing the current week
        int activeSlot = firstDayOfWeek + (currentDay - 1);
        int activeRow = activeSlot / 7;
        int startCol = (activeRow == 0) ? firstDayOfWeek : 0;
        int lastSlotInMonth = firstDayOfWeek + totalDays - 1;
        int endCol = (activeRow == lastSlotInMonth / 7) ? (lastSlotInMonth % 7) : 6;

        float pillLeft = (startCol - 3) * colSpacing - (9.5f * scale);
        float pillRight = (endCol - 3) * colSpacing + (9.5f * scale);
        float pillTop = startGridY + (activeRow * rowSpacing) - (11f * scale);
        float pillBottom = startGridY + (activeRow * rowSpacing) + (5.5f * scale);

        RectF weekPill = new RectF(pillLeft, pillTop, pillRight, pillBottom);
        Paint pillBg = new Paint(Paint.ANTI_ALIAS_FLAG);
        pillBg.setStyle(Paint.Style.FILL);
        pillBg.setColor(Color.argb(38, Color.red(primaryColor), Color.green(primaryColor), Color.blue(primaryColor)));
        canvas.drawRoundRect(weekPill, 8f * scale, 8f * scale, pillBg);

        Paint pillStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        pillStroke.setStyle(Paint.Style.STROKE);
        pillStroke.setStrokeWidth(1f * scale);
        pillStroke.setColor(Color.argb(90, Color.red(primaryColor), Color.green(primaryColor), Color.blue(primaryColor)));
        canvas.drawRoundRect(weekPill, 8f * scale, 8f * scale, pillStroke);

        // Date Numbers
        Paint dayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dayPaint.setTextSize(10f * scale);
        dayPaint.setTextAlign(Paint.Align.CENTER);

        Paint todayBadge = new Paint(Paint.ANTI_ALIAS_FLAG);
        todayBadge.setStyle(Paint.Style.FILL);
        todayBadge.setColor(primaryColor);

        for (int day = 1; day <= totalDays; day++) {
            int slot = firstDayOfWeek + (day - 1);
            int col = slot % 7;
            int row = slot / 7;
            float cx = (col - 3) * colSpacing;
            float cy = startGridY + (row * rowSpacing);

            if (day == currentDay) {
                canvas.drawCircle(cx, cy - (3.5f * scale), 7.5f * scale, todayBadge);
                dayPaint.setColor(getContrastColor(primaryColor));
                dayPaint.setFakeBoldText(true);
            } else if (row == activeRow) {
                dayPaint.setColor(primaryColor);
                dayPaint.setFakeBoldText(true);
            } else {
                dayPaint.setColor(secondaryColor);
                dayPaint.setFakeBoldText(false);
            }
            canvas.drawText(String.valueOf(day), cx, cy, dayPaint);
        }
    }

    // --- Style 1: Circle Bubble Cluster ---
    private static void renderCircleBubbles(Canvas canvas, float scale, int currentDay, int currentMonth, int currentYear,
                                            int firstDayOfWeek, int totalDays, int primaryColor, int secondaryColor) {
        float colSpacing = 20f * scale;
        float rowSpacing = 15.5f * scale;
        float startGridY = -11f * scale;

        // Header with decorative bullet accents
        Paint headerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        headerPaint.setColor(primaryColor);
        headerPaint.setTextSize(12.5f * scale);
        headerPaint.setTextAlign(Paint.Align.CENTER);
        headerPaint.setFakeBoldText(true);
        canvas.drawText("• " + MONTH_NAMES[currentMonth] + " " + currentYear + " •", 0f, -44f * scale, headerPaint);

        // Day Headers
        Paint dowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dowPaint.setColor(secondaryColor);
        dowPaint.setTextSize(9f * scale);
        dowPaint.setTextAlign(Paint.Align.CENTER);
        dowPaint.setFakeBoldText(true);
        for (int c = 0; c < 7; c++) {
            canvas.drawText(DAY_HEADERS[c], (c - 3) * colSpacing, -28f * scale, dowPaint);
        }

        Paint bubbleBg = new Paint(Paint.ANTI_ALIAS_FLAG);
        bubbleBg.setStyle(Paint.Style.FILL);
        bubbleBg.setColor(Color.argb(25, Color.red(secondaryColor), Color.green(secondaryColor), Color.blue(secondaryColor)));

        Paint bubbleStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        bubbleStroke.setStyle(Paint.Style.STROKE);
        bubbleStroke.setStrokeWidth(1f * scale);
        bubbleStroke.setColor(Color.argb(50, Color.red(secondaryColor), Color.green(secondaryColor), Color.blue(secondaryColor)));

        Paint todayFill = new Paint(Paint.ANTI_ALIAS_FLAG);
        todayFill.setStyle(Paint.Style.FILL);
        todayFill.setColor(primaryColor);

        Paint dayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dayPaint.setTextSize(9.5f * scale);
        dayPaint.setTextAlign(Paint.Align.CENTER);

        for (int day = 1; day <= totalDays; day++) {
            int slot = firstDayOfWeek + (day - 1);
            int col = slot % 7;
            int row = slot / 7;
            float cx = (col - 3) * colSpacing;
            float cy = startGridY + (row * rowSpacing);
            float badgeCenterY = cy - (3.5f * scale);

            if (day == currentDay) {
                canvas.drawCircle(cx, badgeCenterY, 7.8f * scale, todayFill);
                dayPaint.setColor(getContrastColor(primaryColor));
                dayPaint.setFakeBoldText(true);
            } else {
                canvas.drawCircle(cx, badgeCenterY, 7.2f * scale, bubbleBg);
                if (col == 0 || col == 6) {
                    canvas.drawCircle(cx, badgeCenterY, 7.2f * scale, bubbleStroke);
                }
                dayPaint.setColor(secondaryColor);
                dayPaint.setFakeBoldText(false);
            }
            canvas.drawText(String.valueOf(day), cx, cy, dayPaint);
        }
    }

    // --- Style 2: Clean Monospace ---
    private static void renderCleanMonospace(Canvas canvas, float scale, int currentDay, int currentMonth, int currentYear,
                                             int firstDayOfWeek, int totalDays, int primaryColor, int secondaryColor) {
        float colSpacing = 20f * scale;
        float rowSpacing = 15f * scale;
        float startGridY = -10f * scale;

        // Tracked uppercase header
        Paint headerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        headerPaint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
        headerPaint.setColor(primaryColor);
        headerPaint.setTextSize(12f * scale);
        headerPaint.setTextAlign(Paint.Align.CENTER);

        String title = MONTH_NAMES[currentMonth] + " " + currentYear;
        canvas.drawText(title, 0f, -44f * scale, headerPaint);

        // Day Headers with monospace alignment
        Paint dowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dowPaint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
        dowPaint.setColor(secondaryColor);
        dowPaint.setTextSize(9.5f * scale);
        dowPaint.setTextAlign(Paint.Align.CENTER);

        for (int c = 0; c < 7; c++) {
            canvas.drawText(DAY_HEADERS[c], (c - 3) * colSpacing, -27f * scale, dowPaint);
        }

        // Sleek horizontal hairline rule
        Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.argb(70, Color.red(secondaryColor), Color.green(secondaryColor), Color.blue(secondaryColor)));
        linePaint.setStrokeWidth(1f * scale);
        canvas.drawLine(-68f * scale, -21f * scale, 68f * scale, -21f * scale, linePaint);

        // Date Numbers with monospace font
        Paint dayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dayPaint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL));
        dayPaint.setTextSize(10f * scale);
        dayPaint.setTextAlign(Paint.Align.CENTER);

        Paint bracketPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bracketPaint.setStyle(Paint.Style.STROKE);
        bracketPaint.setStrokeWidth(1.5f * scale);
        bracketPaint.setColor(primaryColor);

        for (int day = 1; day <= totalDays; day++) {
            int slot = firstDayOfWeek + (day - 1);
            int col = slot % 7;
            int row = slot / 7;
            float cx = (col - 3) * colSpacing;
            float cy = startGridY + (row * rowSpacing);

            if (day == currentDay) {
                // Minimalist geometric bracket rectangle
                RectF bracket = new RectF(cx - 8f * scale, cy - 11f * scale, cx + 8f * scale, cy + 4f * scale);
                canvas.drawRoundRect(bracket, 2f * scale, 2f * scale, bracketPaint);
                dayPaint.setColor(primaryColor);
                dayPaint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
            } else {
                dayPaint.setColor(secondaryColor);
                dayPaint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL));
            }
            canvas.drawText(String.valueOf(day), cx, cy, dayPaint);
        }
    }

    // --- Style 3: Neomorphic Frosted Card ---
    private static void renderNeomorphicCard(Canvas canvas, float scale, int currentDay, int currentMonth, int currentYear,
                                             int firstDayOfWeek, int totalDays, int primaryColor, int secondaryColor) {
        float colSpacing = 19.5f * scale;
        float rowSpacing = 14.5f * scale;
        float startGridY = -10f * scale;

        // Frosted sub-card background container
        RectF cardRect = new RectF(-74f * scale, -54f * scale, 74f * scale, 68f * scale);
        Paint cardBg = new Paint(Paint.ANTI_ALIAS_FLAG);
        cardBg.setStyle(Paint.Style.FILL);
        cardBg.setColor(Color.argb(32, Color.red(primaryColor), Color.green(primaryColor), Color.blue(primaryColor)));
        canvas.drawRoundRect(cardRect, 12f * scale, 12f * scale, cardBg);

        Paint cardBorder = new Paint(Paint.ANTI_ALIAS_FLAG);
        cardBorder.setStyle(Paint.Style.STROKE);
        cardBorder.setStrokeWidth(1f * scale);
        cardBorder.setColor(Color.argb(65, 255, 255, 255));
        canvas.drawRoundRect(cardRect, 12f * scale, 12f * scale, cardBorder);

        // Header Navigation Pill
        RectF headerPill = new RectF(-50f * scale, -47f * scale, 50f * scale, -31f * scale);
        Paint pillBg = new Paint(Paint.ANTI_ALIAS_FLAG);
        pillBg.setStyle(Paint.Style.FILL);
        pillBg.setColor(Color.argb(50, Color.red(primaryColor), Color.green(primaryColor), Color.blue(primaryColor)));
        canvas.drawRoundRect(headerPill, 8f * scale, 8f * scale, pillBg);

        Paint headerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        headerPaint.setColor(primaryColor);
        headerPaint.setTextSize(11f * scale);
        headerPaint.setTextAlign(Paint.Align.CENTER);
        headerPaint.setFakeBoldText(true);
        canvas.drawText("< " + MONTH_NAMES[currentMonth].substring(0, 3) + " " + currentYear + " >", 0f, -35.5f * scale, headerPaint);

        // Day Headers
        Paint dowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dowPaint.setColor(secondaryColor);
        dowPaint.setTextSize(9f * scale);
        dowPaint.setTextAlign(Paint.Align.CENTER);
        dowPaint.setFakeBoldText(true);
        for (int c = 0; c < 7; c++) {
            canvas.drawText(DAY_HEADERS[c], (c - 3) * colSpacing, -19f * scale, dowPaint);
        }

        // Date Numbers
        Paint dayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dayPaint.setTextSize(9.5f * scale);
        dayPaint.setTextAlign(Paint.Align.CENTER);

        Paint todayBadge = new Paint(Paint.ANTI_ALIAS_FLAG);
        todayBadge.setStyle(Paint.Style.FILL);
        todayBadge.setColor(primaryColor);

        for (int day = 1; day <= totalDays; day++) {
            int slot = firstDayOfWeek + (day - 1);
            int col = slot % 7;
            int row = slot / 7;
            float cx = (col - 3) * colSpacing;
            float cy = startGridY + (row * rowSpacing);

            if (day == currentDay) {
                RectF todayRect = new RectF(cx - 7.5f * scale, cy - 10.5f * scale, cx + 7.5f * scale, cy + 4.5f * scale);
                canvas.drawRoundRect(todayRect, 4f * scale, 4f * scale, todayBadge);
                dayPaint.setColor(getContrastColor(primaryColor));
                dayPaint.setFakeBoldText(true);
            } else {
                dayPaint.setColor(secondaryColor);
                dayPaint.setFakeBoldText(false);
            }
            canvas.drawText(String.valueOf(day), cx, cy, dayPaint);
        }
    }

    // --- Style 4: Modular Rounded Square Grid ---
    private static void renderModularSquares(Canvas canvas, float scale, int currentDay, int currentMonth, int currentYear,
                                             int firstDayOfWeek, int totalDays, int primaryColor, int secondaryColor) {
        float colSpacing = 20f * scale;
        float rowSpacing = 16f * scale;
        float startGridY = -11f * scale;
        float tileSize = 14.5f * scale;
        float corner = 3.5f * scale;

        // Header
        Paint headerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        headerPaint.setColor(primaryColor);
        headerPaint.setTextSize(13f * scale);
        headerPaint.setTextAlign(Paint.Align.CENTER);
        headerPaint.setFakeBoldText(true);
        canvas.drawText(MONTH_NAMES[currentMonth] + " " + currentYear, 0f, -44f * scale, headerPaint);

        // Day Headers
        Paint dowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dowPaint.setColor(secondaryColor);
        dowPaint.setTextSize(9f * scale);
        dowPaint.setTextAlign(Paint.Align.CENTER);
        dowPaint.setFakeBoldText(true);
        for (int c = 0; c < 7; c++) {
            canvas.drawText(DAY_HEADERS[c], (c - 3) * colSpacing, -28f * scale, dowPaint);
        }

        Paint tileBg = new Paint(Paint.ANTI_ALIAS_FLAG);
        tileBg.setStyle(Paint.Style.FILL);
        tileBg.setColor(Color.argb(22, Color.red(secondaryColor), Color.green(secondaryColor), Color.blue(secondaryColor)));

        Paint tileStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        tileStroke.setStyle(Paint.Style.STROKE);
        tileStroke.setStrokeWidth(0.8f * scale);
        tileStroke.setColor(Color.argb(45, Color.red(secondaryColor), Color.green(secondaryColor), Color.blue(secondaryColor)));

        Paint todayTile = new Paint(Paint.ANTI_ALIAS_FLAG);
        todayTile.setStyle(Paint.Style.FILL);
        todayTile.setColor(primaryColor);

        Paint dayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dayPaint.setTextSize(9.5f * scale);
        dayPaint.setTextAlign(Paint.Align.CENTER);

        for (int day = 1; day <= totalDays; day++) {
            int slot = firstDayOfWeek + (day - 1);
            int col = slot % 7;
            int row = slot / 7;
            float cx = (col - 3) * colSpacing;
            float cy = startGridY + (row * rowSpacing);
            float centerY = cy - (3.5f * scale);

            RectF tile = new RectF(cx - tileSize / 2, centerY - tileSize / 2, cx + tileSize / 2, centerY + tileSize / 2);

            if (day == currentDay) {
                canvas.drawRoundRect(tile, corner, corner, todayTile);
                dayPaint.setColor(getContrastColor(primaryColor));
                dayPaint.setFakeBoldText(true);
            } else {
                canvas.drawRoundRect(tile, corner, corner, tileBg);
                canvas.drawRoundRect(tile, corner, corner, tileStroke);
                dayPaint.setColor(secondaryColor);
                dayPaint.setFakeBoldText(false);
            }
            canvas.drawText(String.valueOf(day), cx, cy, dayPaint);
        }
    }

    // --- Style 5: Inset Sunken Dial Hybrid ---
    private static void renderSunkenDial(Canvas canvas, float scale, int currentDay, int currentMonth, int currentYear,
                                         int firstDayOfWeek, int totalDays, int primaryColor, int secondaryColor) {
        // Left Side: Sunken Dial / Month-Progress Gauge
        float dialCenterX = -46f * scale;
        float dialCenterY = 0f;
        float dialRadius = 26f * scale;

        // Dial outer rim track
        Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeWidth(3.5f * scale);
        trackPaint.setColor(Color.argb(45, Color.red(secondaryColor), Color.green(secondaryColor), Color.blue(secondaryColor)));
        canvas.drawCircle(dialCenterX, dialCenterY, dialRadius, trackPaint);

        // Progress Arc (Percentage of month elapsed)
        Paint arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeCap(Paint.Cap.ROUND);
        arcPaint.setStrokeWidth(3.5f * scale);
        arcPaint.setColor(primaryColor);

        float sweepAngle = 360f * ((float) currentDay / (float) totalDays);
        RectF arcBounds = new RectF(dialCenterX - dialRadius, dialCenterY - dialRadius,
                dialCenterX + dialRadius, dialCenterY + dialRadius);
        canvas.drawArc(arcBounds, -90f, sweepAngle, false, arcPaint);

        // Big Day Numerals in Center of Dial
        Paint bigDayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bigDayPaint.setColor(primaryColor);
        bigDayPaint.setTextSize(19f * scale);
        bigDayPaint.setTextAlign(Paint.Align.CENTER);
        bigDayPaint.setFakeBoldText(true);
        canvas.drawText(String.valueOf(currentDay), dialCenterX, dialCenterY + (3f * scale), bigDayPaint);

        // Month Subtitle under Dial Number
        Paint monthSubPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        monthSubPaint.setColor(secondaryColor);
        monthSubPaint.setTextSize(8f * scale);
        monthSubPaint.setTextAlign(Paint.Align.CENTER);
        monthSubPaint.setFakeBoldText(true);
        canvas.drawText(MONTH_NAMES[currentMonth].substring(0, 3), dialCenterX, dialCenterY + (13f * scale), monthSubPaint);

        // Right Side: Aligned Compact Calendar Grid
        float gridAnchorX = 26f * scale;
        float colSpacing = 13.5f * scale;
        float rowSpacing = 13.5f * scale;
        float startGridY = -22f * scale;

        // Mini Day Headers
        Paint dowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dowPaint.setColor(secondaryColor);
        dowPaint.setTextSize(8f * scale);
        dowPaint.setTextAlign(Paint.Align.CENTER);
        dowPaint.setFakeBoldText(true);
        for (int c = 0; c < 7; c++) {
            canvas.drawText(DAY_HEADERS[c], gridAnchorX + (c - 3) * colSpacing, startGridY - (10f * scale), dowPaint);
        }

        // Mini Date Numbers
        Paint miniDayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        miniDayPaint.setTextSize(8.5f * scale);
        miniDayPaint.setTextAlign(Paint.Align.CENTER);

        Paint miniTodayDot = new Paint(Paint.ANTI_ALIAS_FLAG);
        miniTodayDot.setStyle(Paint.Style.FILL);
        miniTodayDot.setColor(primaryColor);

        for (int day = 1; day <= totalDays; day++) {
            int slot = firstDayOfWeek + (day - 1);
            int col = slot % 7;
            int row = slot / 7;
            float cx = gridAnchorX + (col - 3) * colSpacing;
            float cy = startGridY + (row * rowSpacing);

            if (day == currentDay) {
                canvas.drawCircle(cx, cy - (3f * scale), 6f * scale, miniTodayDot);
                miniDayPaint.setColor(getContrastColor(primaryColor));
                miniDayPaint.setFakeBoldText(true);
            } else {
                miniDayPaint.setColor(secondaryColor);
                miniDayPaint.setFakeBoldText(false);
            }
            canvas.drawText(String.valueOf(day), cx, cy, miniDayPaint);
        }
    }

    // --- Style 6: Dotted Accent Calendar ---
    private static void renderDottedAccent(Canvas canvas, float scale, int currentDay, int currentMonth, int currentYear,
                                           int firstDayOfWeek, int totalDays, int primaryColor, int secondaryColor) {
        float colSpacing = 20f * scale;
        float rowSpacing = 15f * scale;
        float startGridY = -12f * scale;

        // Header with micro-dots
        Paint headerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        headerPaint.setColor(primaryColor);
        headerPaint.setTextSize(13f * scale);
        headerPaint.setTextAlign(Paint.Align.CENTER);
        headerPaint.setFakeBoldText(true);
        canvas.drawText("• " + MONTH_NAMES[currentMonth] + " " + currentYear + " •", 0f, -44f * scale, headerPaint);

        // Day Headers with inter-letter dot separators
        Paint dowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dowPaint.setColor(secondaryColor);
        dowPaint.setTextSize(9.5f * scale);
        dowPaint.setTextAlign(Paint.Align.CENTER);
        dowPaint.setFakeBoldText(true);
        for (int c = 0; c < 7; c++) {
            canvas.drawText(DAY_HEADERS[c], (c - 3) * colSpacing, -28f * scale, dowPaint);
        }

        Paint dayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dayPaint.setTextSize(10f * scale);
        dayPaint.setTextAlign(Paint.Align.CENTER);

        Paint ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setStrokeWidth(1.6f * scale);
        ringPaint.setColor(primaryColor);

        Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotPaint.setStyle(Paint.Style.FILL);

        for (int day = 1; day <= totalDays; day++) {
            int slot = firstDayOfWeek + (day - 1);
            int col = slot % 7;
            int row = slot / 7;
            float cx = (col - 3) * colSpacing;
            float cy = startGridY + (row * rowSpacing);

            if (day == currentDay) {
                // Outer ring indicator
                canvas.drawCircle(cx, cy - (3.5f * scale), 7.5f * scale, ringPaint);
                // Active dot underneath
                dotPaint.setColor(primaryColor);
                canvas.drawCircle(cx, cy + (4f * scale), 1.6f * scale, dotPaint);
                dayPaint.setColor(primaryColor);
                dayPaint.setFakeBoldText(true);
            } else {
                // Subtle dot under regular dates
                dotPaint.setColor(Color.argb(55, Color.red(secondaryColor), Color.green(secondaryColor), Color.blue(secondaryColor)));
                canvas.drawCircle(cx, cy + (4f * scale), 1.0f * scale, dotPaint);
                dayPaint.setColor(secondaryColor);
                dayPaint.setFakeBoldText(false);
            }
            canvas.drawText(String.valueOf(day), cx, cy, dayPaint);
        }
    }

    // --- Style 7: Compact Headerless ---
    private static void renderCompactHeaderless(Canvas canvas, float scale, int currentDay, int currentMonth, int currentYear,
                                                int firstDayOfWeek, int totalDays, int primaryColor, int secondaryColor) {
        float colSpacing = 20.5f * scale;
        float rowSpacing = 16f * scale;
        float startGridY = -20f * scale;

        // Sleek top status line: Left tag "CALENDAR", Right tag "MONTH 'YY"
        Paint statusPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        statusPaint.setColor(primaryColor);
        statusPaint.setTextSize(10f * scale);
        statusPaint.setFakeBoldText(true);

        statusPaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("CALENDAR", -65f * scale, -45f * scale, statusPaint);

        statusPaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(MONTH_NAMES[currentMonth] + " '" + (currentYear % 100), 65f * scale, -45f * scale, statusPaint);

        // Day Headers
        Paint dowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dowPaint.setColor(secondaryColor);
        dowPaint.setTextSize(9f * scale);
        dowPaint.setTextAlign(Paint.Align.CENTER);
        dowPaint.setFakeBoldText(true);
        for (int c = 0; c < 7; c++) {
            canvas.drawText(DAY_HEADERS[c], (c - 3) * colSpacing, -32f * scale, dowPaint);
        }

        // Expanded, bold glanceable date numbers
        Paint dayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dayPaint.setTextSize(11f * scale);
        dayPaint.setTextAlign(Paint.Align.CENTER);

        Paint todayBadge = new Paint(Paint.ANTI_ALIAS_FLAG);
        todayBadge.setStyle(Paint.Style.FILL);
        todayBadge.setColor(primaryColor);

        for (int day = 1; day <= totalDays; day++) {
            int slot = firstDayOfWeek + (day - 1);
            int col = slot % 7;
            int row = slot / 7;
            float cx = (col - 3) * colSpacing;
            float cy = startGridY + (row * rowSpacing);

            if (day == currentDay) {
                canvas.drawCircle(cx, cy - (3.8f * scale), 8.2f * scale, todayBadge);
                dayPaint.setColor(getContrastColor(primaryColor));
                dayPaint.setFakeBoldText(true);
            } else {
                dayPaint.setColor(secondaryColor);
                dayPaint.setFakeBoldText(false);
            }
            canvas.drawText(String.valueOf(day), cx, cy, dayPaint);
        }
    }

    // --- Style 8: Segmented Row Focus ---
    private static void renderSegmentedRow(Canvas canvas, float scale, int currentDay, int currentMonth, int currentYear,
                                           int firstDayOfWeek, int totalDays, int primaryColor, int secondaryColor) {
        float colSpacing = 20f * scale;
        float rowSpacing = 15f * scale;
        float startGridY = -12f * scale;

        // Header
        Paint headerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        headerPaint.setColor(primaryColor);
        headerPaint.setTextSize(13f * scale);
        headerPaint.setTextAlign(Paint.Align.CENTER);
        headerPaint.setFakeBoldText(true);
        canvas.drawText(MONTH_NAMES[currentMonth] + " " + currentYear, 0f, -44f * scale, headerPaint);

        // Day Headers
        Paint dowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dowPaint.setColor(secondaryColor);
        dowPaint.setTextSize(9.5f * scale);
        dowPaint.setTextAlign(Paint.Align.CENTER);
        dowPaint.setFakeBoldText(true);
        for (int c = 0; c < 7; c++) {
            canvas.drawText(DAY_HEADERS[c], (c - 3) * colSpacing, -28f * scale, dowPaint);
        }

        // Active week row container highlight
        int activeSlot = firstDayOfWeek + (currentDay - 1);
        int activeRow = activeSlot / 7;

        RectF focusRow = new RectF(-68f * scale, startGridY + (activeRow * rowSpacing) - (11f * scale),
                68f * scale, startGridY + (activeRow * rowSpacing) + (5.5f * scale));

        Paint focusBg = new Paint(Paint.ANTI_ALIAS_FLAG);
        focusBg.setStyle(Paint.Style.FILL);
        focusBg.setColor(Color.argb(40, Color.red(primaryColor), Color.green(primaryColor), Color.blue(primaryColor)));
        canvas.drawRoundRect(focusRow, 6f * scale, 6f * scale, focusBg);

        Paint focusStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        focusStroke.setStyle(Paint.Style.STROKE);
        focusStroke.setStrokeWidth(1.2f * scale);
        focusStroke.setColor(Color.argb(95, Color.red(primaryColor), Color.green(primaryColor), Color.blue(primaryColor)));
        canvas.drawRoundRect(focusRow, 6f * scale, 6f * scale, focusStroke);

        // Date Numbers: Current week has full contrast, other weeks are softly dimmed
        Paint dayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dayPaint.setTextSize(10f * scale);
        dayPaint.setTextAlign(Paint.Align.CENTER);

        Paint todayBadge = new Paint(Paint.ANTI_ALIAS_FLAG);
        todayBadge.setStyle(Paint.Style.FILL);
        todayBadge.setColor(primaryColor);

        for (int day = 1; day <= totalDays; day++) {
            int slot = firstDayOfWeek + (day - 1);
            int col = slot % 7;
            int row = slot / 7;
            float cx = (col - 3) * colSpacing;
            float cy = startGridY + (row * rowSpacing);

            if (day == currentDay) {
                canvas.drawCircle(cx, cy - (3.5f * scale), 7.5f * scale, todayBadge);
                dayPaint.setColor(getContrastColor(primaryColor));
                dayPaint.setFakeBoldText(true);
            } else if (row == activeRow) {
                dayPaint.setColor(primaryColor);
                dayPaint.setFakeBoldText(true);
            } else {
                dayPaint.setColor(Color.argb(120, Color.red(secondaryColor), Color.green(secondaryColor), Color.blue(secondaryColor)));
                dayPaint.setFakeBoldText(false);
            }
            canvas.drawText(String.valueOf(day), cx, cy, dayPaint);
        }
    }

    // --- Style 9: High-Contrast Dark Grid ---
    private static void renderHighContrastGrid(Canvas canvas, float scale, int currentDay, int currentMonth, int currentYear,
                                               int firstDayOfWeek, int totalDays, int primaryColor, int secondaryColor) {
        float colSpacing = 20f * scale;
        float rowSpacing = 15.5f * scale;
        float startGridY = -11f * scale;

        // Header
        Paint headerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        headerPaint.setColor(primaryColor);
        headerPaint.setTextSize(13f * scale);
        headerPaint.setTextAlign(Paint.Align.CENTER);
        headerPaint.setFakeBoldText(true);
        canvas.drawText(MONTH_NAMES[currentMonth] + " " + currentYear, 0f, -44f * scale, headerPaint);

        // Day Headers
        Paint dowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dowPaint.setColor(secondaryColor);
        dowPaint.setTextSize(9f * scale);
        dowPaint.setTextAlign(Paint.Align.CENTER);
        dowPaint.setFakeBoldText(true);
        for (int c = 0; c < 7; c++) {
            canvas.drawText(DAY_HEADERS[c], (c - 3) * colSpacing, -28f * scale, dowPaint);
        }

        Paint pastBg = new Paint(Paint.ANTI_ALIAS_FLAG);
        pastBg.setStyle(Paint.Style.FILL);
        pastBg.setColor(Color.argb(28, Color.red(secondaryColor), Color.green(secondaryColor), Color.blue(secondaryColor)));

        Paint futureStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        futureStroke.setStyle(Paint.Style.STROKE);
        futureStroke.setStrokeWidth(1f * scale);
        futureStroke.setColor(Color.argb(55, Color.red(secondaryColor), Color.green(secondaryColor), Color.blue(secondaryColor)));

        Paint todayOuterRing = new Paint(Paint.ANTI_ALIAS_FLAG);
        todayOuterRing.setStyle(Paint.Style.STROKE);
        todayOuterRing.setStrokeWidth(1.5f * scale);
        todayOuterRing.setColor(primaryColor);

        Paint todayFill = new Paint(Paint.ANTI_ALIAS_FLAG);
        todayFill.setStyle(Paint.Style.FILL);
        todayFill.setColor(primaryColor);

        Paint dayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dayPaint.setTextSize(9.5f * scale);
        dayPaint.setTextAlign(Paint.Align.CENTER);

        for (int day = 1; day <= totalDays; day++) {
            int slot = firstDayOfWeek + (day - 1);
            int col = slot % 7;
            int row = slot / 7;
            float cx = (col - 3) * colSpacing;
            float cy = startGridY + (row * rowSpacing);
            float centerY = cy - (3.5f * scale);

            if (day == currentDay) {
                canvas.drawCircle(cx, centerY, 9f * scale, todayOuterRing);
                canvas.drawCircle(cx, centerY, 6.8f * scale, todayFill);
                dayPaint.setColor(getContrastColor(primaryColor));
                dayPaint.setFakeBoldText(true);
            } else if (day < currentDay) {
                canvas.drawCircle(cx, centerY, 7.2f * scale, pastBg);
                dayPaint.setColor(secondaryColor);
                dayPaint.setFakeBoldText(false);
            } else {
                canvas.drawCircle(cx, centerY, 7.2f * scale, futureStroke);
                dayPaint.setColor(secondaryColor);
                dayPaint.setFakeBoldText(false);
            }
            canvas.drawText(String.valueOf(day), cx, cy, dayPaint);
        }
    }

    // --- Style 10: Modern Sans Grid ---
    private static void renderModernSans(Canvas canvas, float scale, int currentDay, int currentMonth, int currentYear,
                                         int firstDayOfWeek, int totalDays, int primaryColor, int secondaryColor) {
        float colSpacing = 20f * scale;
        float rowSpacing = 15f * scale;
        float startGridY = -12f * scale;

        // Two vertical tinted accent columns behind weekends (Sun = col 0, Sat = col 6)
        Paint weekendStrip = new Paint(Paint.ANTI_ALIAS_FLAG);
        weekendStrip.setStyle(Paint.Style.FILL);
        weekendStrip.setColor(Color.argb(22, Color.red(primaryColor), Color.green(primaryColor), Color.blue(primaryColor)));

        float col0X = (-3) * colSpacing;
        float col6X = (+3) * colSpacing;
        RectF stripSun = new RectF(col0X - (9.5f * scale), -36f * scale, col0X + (9.5f * scale), 64f * scale);
        RectF stripSat = new RectF(col6X - (9.5f * scale), -36f * scale, col6X + (9.5f * scale), 64f * scale);
        canvas.drawRoundRect(stripSun, 4f * scale, 4f * scale, weekendStrip);
        canvas.drawRoundRect(stripSat, 4f * scale, 4f * scale, weekendStrip);

        // Header
        Paint headerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        headerPaint.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        headerPaint.setColor(primaryColor);
        headerPaint.setTextSize(13f * scale);
        headerPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(MONTH_NAMES[currentMonth] + " " + currentYear, 0f, -44f * scale, headerPaint);

        // Day Headers
        Paint dowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dowPaint.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        dowPaint.setTextSize(9.5f * scale);
        dowPaint.setTextAlign(Paint.Align.CENTER);

        for (int c = 0; c < 7; c++) {
            if (c == 0 || c == 6) {
                dowPaint.setColor(primaryColor);
            } else {
                dowPaint.setColor(secondaryColor);
            }
            canvas.drawText(DAY_HEADERS[c], (c - 3) * colSpacing, -28f * scale, dowPaint);
        }

        // Date Numbers
        Paint dayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dayPaint.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        dayPaint.setTextSize(10f * scale);
        dayPaint.setTextAlign(Paint.Align.CENTER);

        Paint todayBadge = new Paint(Paint.ANTI_ALIAS_FLAG);
        todayBadge.setStyle(Paint.Style.FILL);
        todayBadge.setColor(primaryColor);

        for (int day = 1; day <= totalDays; day++) {
            int slot = firstDayOfWeek + (day - 1);
            int col = slot % 7;
            int row = slot / 7;
            float cx = (col - 3) * colSpacing;
            float cy = startGridY + (row * rowSpacing);

            if (day == currentDay) {
                canvas.drawCircle(cx, cy - (3.5f * scale), 7.5f * scale, todayBadge);
                dayPaint.setColor(getContrastColor(primaryColor));
                dayPaint.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
            } else if (col == 0 || col == 6) {
                dayPaint.setColor(primaryColor);
                dayPaint.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
            } else {
                dayPaint.setColor(secondaryColor);
                dayPaint.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
            }
            canvas.drawText(String.valueOf(day), cx, cy, dayPaint);
        }
    }

    /**
     * Determines optimal black or white contrast color for the active day badge.
     */
    public static int getContrastColor(int color) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        double yiq = ((r * 299) + (g * 587) + (b * 114)) / 1000.0;
        return (yiq >= 150) ? Color.parseColor("#0F172A") : Color.WHITE;
    }
}
