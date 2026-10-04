package com.glyph.widget.compositor;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import java.util.Calendar;
import java.util.Locale;

/**
 * ClockRenderer renders a mathematical 2D Canvas digital clock with 12 distinct
 * artistic styles matching reference aesthetics:
 *  0: Bold Capsule Sans (Default)
 *  1: Retro 7-Segment LED
 *  2: Mechanical Split-Flap
 *  3: Stacked 2x2 Gradient Pill
 *  4: Dot Matrix LED
 *  5: Superscript Seconds & Date
 *  6: Bold Athletic Block
 *  7: Whimsical Cartoon Bubble
 *  8: Modular Mosaic Block
 *  9: Sci-Fi Stencil Squircle
 * 10: Ultra-Condensed Tall Deco
 * 11: Minimalist Analog Dial Hybrid
 */
public class ClockRenderer {

    public static final int STYLE_BOLD_CAPSULE_SANS = 0;
    public static final int STYLE_RETRO_7_SEGMENT = 1;
    public static final int STYLE_MECHANICAL_SPLIT_FLAP = 2;
    public static final int STYLE_STACKED_2X2_PILL = 3;
    public static final int STYLE_DOT_MATRIX_LED = 4;
    public static final int STYLE_SUPERSCRIPT_SECONDS = 5;
    public static final int STYLE_ATHLETIC_BLOCK = 6;
    public static final int STYLE_CARTOON_BUBBLE = 7;
    public static final int STYLE_MODULAR_MOSAIC = 8;
    public static final int STYLE_SCIFI_SQUIRCLE = 9;
    public static final int STYLE_TALL_DECO = 10;
    public static final int STYLE_ANALOG_DIAL_HYBRID = 11;
    public static final int STYLE_COUNT = 12;

    public static final String[] STYLE_NAMES = {
            "Bold Capsule Sans",
            "Retro 7-Segment LED",
            "Mechanical Split-Flap",
            "Stacked 2x2 Gradient Pill",
            "Dot Matrix LED",
            "Superscript Seconds & Date",
            "Bold Athletic Block",
            "Whimsical Cartoon Bubble",
            "Modular Mosaic Block",
            "Sci-Fi Stencil Squircle",
            "Ultra-Condensed Tall Deco",
            "Analog Dial Hybrid"
    };

    public static final String[] STYLE_DESCRIPTIONS = {
            "Chunky rounded digital font with superscript AM/PM pill capsule badge",
            "Authentic electronic LED display with lit segments and faint ghost framing",
            "Split flip-board cards with center dividing seam and tactile card borders",
            "Vertical stacked layout with hours above minutes and bracket corner framing",
            "Fine-pitch 5x7 dot matrix array with unlit ghost LEDs and circular dot diodes",
            "Crisp architectural typography with live running seconds and date pill tag",
            "Heavy faceted athletic stencil block numerals with chamfered geometry",
            "Playful bulbous typography with inner bubble reflection highlight crescents",
            "Minimalist Nothing OS inspired block mosaic constructed from geometric tiles",
            "Cyberpunk HUD telemetry display with technical squircle frames and scanline seam",
            "Ultra-tall slender Bauhaus Art Deco numerals with stacked minute column",
            "Asymmetric hybrid featuring an authentic circular analog dial beside digital time"
    };

    private static final String[] MONTH_ABBR = {
            "JAN", "FEB", "MAR", "APR", "MAY", "JUN",
            "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"
    };

    private static final String[] DAY_ABBR = {"SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"};

    // 7-segment bitmasks: A, B, C, D, E, F, G
    private static final int[] SEGMENTS_7 = {
            0x3F, // 0: A B C D E F
            0x06, // 1: B C
            0x5B, // 2: A B G E D
            0x4F, // 3: A B G C D
            0x66, // 4: F G B C
            0x6D, // 5: A F G C D
            0x7D, // 6: A F E D C G
            0x07, // 7: A B C
            0x7F, // 8: A B C D E F G
            0x6F  // 9: A B C D F G
    };

    // 5x7 Dot Matrix patterns (column byte masks, LSB at top)
    private static final byte[][] DOT_MATRIX_5X7 = {
            {0x3E, 0x51, 0x49, 0x45, 0x3E}, // 0
            {0x00, 0x42, 0x7F, 0x40, 0x00}, // 1
            {0x42, 0x61, 0x51, 0x49, 0x46}, // 2
            {0x21, 0x41, 0x45, 0x4B, 0x31}, // 3
            {0x18, 0x14, 0x12, 0x7F, 0x10}, // 4
            {0x27, 0x45, 0x45, 0x45, 0x39}, // 5
            {0x3C, 0x4A, 0x49, 0x49, 0x30}, // 6
            {0x01, 0x71, 0x09, 0x05, 0x03}, // 7
            {0x36, 0x49, 0x49, 0x49, 0x36}, // 8
            {0x06, 0x49, 0x49, 0x29, 0x1E}  // 9
    };

    private static Typeface sBoldTypeface;
    private static Typeface sMonoTypeface;

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
            if (sBoldTypeface == null) sBoldTypeface = Typeface.DEFAULT_BOLD;
        }
        return sBoldTypeface;
    }

    private static Typeface getSafeMonoTypeface() {
        if (sMonoTypeface == null) {
            try {
                sMonoTypeface = Typeface.create("monospace", Typeface.BOLD);
            } catch (Throwable t) {
                try {
                    sMonoTypeface = Typeface.MONOSPACE;
                } catch (Throwable t2) {
                    sMonoTypeface = Typeface.DEFAULT_BOLD;
                }
            }
            if (sMonoTypeface == null) sMonoTypeface = Typeface.DEFAULT_BOLD;
        }
        return sMonoTypeface;
    }

    public static void drawClock(Canvas canvas, RectF pillRect, float scale,
                                 float offsetXdp, float offsetYdp, int scalePercent,
                                 int primaryColor, int secondaryColor) {
        drawClock(canvas, pillRect, scale, offsetXdp, offsetYdp, scalePercent,
                primaryColor, secondaryColor, false, STYLE_BOLD_CAPSULE_SANS);
    }

    public static void drawClock(Canvas canvas, RectF pillRect, float scale,
                                 float offsetXdp, float offsetYdp, int scalePercent,
                                 int primaryColor, int secondaryColor,
                                 boolean is24HourFormat) {
        drawClock(canvas, pillRect, scale, offsetXdp, offsetYdp, scalePercent,
                primaryColor, secondaryColor, is24HourFormat, STYLE_BOLD_CAPSULE_SANS);
    }

    /**
     * Main dispatch method rendering the selected clock style.
     */
    public static void drawClock(Canvas canvas, RectF pillRect, float scale,
                                 float offsetXdp, float offsetYdp, int scalePercent,
                                 int primaryColor, int secondaryColor,
                                 boolean is24HourFormat, int styleIndex) {
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
            int second = now.get(Calendar.SECOND);
            String amPm = is24HourFormat ? "" : (now.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM");

            int clampedStyle = Math.max(0, Math.min(STYLE_COUNT - 1, styleIndex));

            switch (clampedStyle) {
                case STYLE_RETRO_7_SEGMENT:
                    drawStyleRetro7Segment(canvas, scale, hour, minute, amPm, primaryColor, secondaryColor);
                    break;
                case STYLE_MECHANICAL_SPLIT_FLAP:
                    drawStyleSplitFlap(canvas, scale, hour, minute, amPm, primaryColor, secondaryColor);
                    break;
                case STYLE_STACKED_2X2_PILL:
                    drawStyleStacked2x2(canvas, scale, hour, minute, amPm, primaryColor, secondaryColor);
                    break;
                case STYLE_DOT_MATRIX_LED:
                    drawStyleDotMatrix(canvas, scale, hour, minute, amPm, primaryColor, secondaryColor);
                    break;
                case STYLE_SUPERSCRIPT_SECONDS:
                    drawStyleSuperscriptSeconds(canvas, scale, hour, minute, second, now, amPm, primaryColor, secondaryColor);
                    break;
                case STYLE_ATHLETIC_BLOCK:
                    drawStyleAthleticBlock(canvas, scale, hour, minute, amPm, primaryColor, secondaryColor);
                    break;
                case STYLE_CARTOON_BUBBLE:
                    drawStyleCartoonBubble(canvas, scale, hour, minute, amPm, primaryColor, secondaryColor);
                    break;
                case STYLE_MODULAR_MOSAIC:
                    drawStyleModularMosaic(canvas, scale, hour, minute, amPm, primaryColor, secondaryColor);
                    break;
                case STYLE_SCIFI_SQUIRCLE:
                    drawStyleSciFiSquircle(canvas, scale, hour, minute, amPm, primaryColor, secondaryColor);
                    break;
                case STYLE_TALL_DECO:
                    drawStyleTallDeco(canvas, scale, hour, minute, amPm, primaryColor, secondaryColor);
                    break;
                case STYLE_ANALOG_DIAL_HYBRID:
                    drawStyleAnalogDialHybrid(canvas, scale, hour, minute, amPm, primaryColor, secondaryColor);
                    break;
                case STYLE_BOLD_CAPSULE_SANS:
                default:
                    drawStyleBoldCapsuleSans(canvas, scale, hour, minute, amPm, primaryColor, secondaryColor);
                    break;
            }

            canvas.restore();
        } catch (Throwable t) {
            android.util.Log.e("ClockRenderer", "Failed to render clock style " + styleIndex, t);
        }
    }

    // --- Style 0: Bold Capsule Sans (Default) ---
    private static void drawStyleBoldCapsuleSans(Canvas canvas, float scale, int hour, int minute, String amPm,
                                                 int primaryColor, int secondaryColor) {
        String hourStr = String.format(Locale.US, "%02d", hour);
        String minStr = String.format(Locale.US, "%02d", minute);
        Typeface boldTf = getSafeBoldTypeface();

        Paint digitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        digitPaint.setColor(primaryColor);
        digitPaint.setTextSize(34f * scale);
        digitPaint.setTypeface(boldTf);

        Paint colonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        colonPaint.setColor(secondaryColor);
        colonPaint.setTextSize(32f * scale);
        colonPaint.setTypeface(boldTf);

        Paint amPmPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        amPmPaint.setColor(secondaryColor);
        amPmPaint.setTextSize(10f * scale);
        amPmPaint.setTypeface(boldTf);

        float hourWidth = digitPaint.measureText(hourStr);
        float colonWidth = colonPaint.measureText(" : ");
        float minWidth = digitPaint.measureText(minStr);
        float amPmWidth = amPm.isEmpty() ? 0f : amPmPaint.measureText(amPm) + (8f * scale);

        float totalClockWidth = hourWidth + colonWidth + minWidth + amPmWidth;
        float startX = -totalClockWidth / 2f;
        float baselineY = 11f * scale;

        canvas.drawText(hourStr, startX, baselineY, digitPaint);
        float cursorX = startX + hourWidth;

        canvas.drawText(" : ", cursorX, baselineY - (1.5f * scale), colonPaint);
        cursorX += colonWidth;

        canvas.drawText(minStr, cursorX, baselineY, digitPaint);
        cursorX += minWidth;

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
    }

    // --- Style 1: Retro 7-Segment LED ---
    private static void drawStyleRetro7Segment(Canvas canvas, float scale, int hour, int minute, String amPm,
                                               int primaryColor, int secondaryColor) {
        float digW = 16f * scale;
        float digH = 32f * scale;
        float segT = 2.8f * scale;
        float digitGap = 4f * scale;
        float colonW = 10f * scale;

        int h1 = hour / 10;
        int h2 = hour % 10;
        int m1 = minute / 10;
        int m2 = minute % 10;

        float totalW = (4 * digW) + (2 * digitGap) + colonW + (amPm.isEmpty() ? 0 : 20f * scale);
        float startX = -totalW / 2f;
        float startY = -digH / 2f;

        Paint litPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        litPaint.setStyle(Paint.Style.FILL);
        litPaint.setColor(primaryColor);

        Paint unlitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        unlitPaint.setStyle(Paint.Style.FILL);
        unlitPaint.setColor(Color.argb(25, Color.red(secondaryColor), Color.green(secondaryColor), Color.blue(secondaryColor)));

        // H1
        draw7Seg(canvas, startX, startY, digW, digH, segT, h1, litPaint, unlitPaint);
        // H2
        draw7Seg(canvas, startX + digW + digitGap, startY, digW, digH, segT, h2, litPaint, unlitPaint);

        // Colon Dots
        float colonX = startX + (2 * digW) + digitGap + (colonW / 2f);
        canvas.drawCircle(colonX, startY + (digH * 0.32f), 2.2f * scale, litPaint);
        canvas.drawCircle(colonX, startY + (digH * 0.68f), 2.2f * scale, litPaint);

        // M1
        float m1X = startX + (2 * digW) + digitGap + colonW;
        draw7Seg(canvas, m1X, startY, digW, digH, segT, m1, litPaint, unlitPaint);
        // M2
        draw7Seg(canvas, m1X + digW + digitGap, startY, digW, digH, segT, m2, litPaint, unlitPaint);

        // AM/PM LED Tag
        if (!amPm.isEmpty()) {
            Paint tagPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            tagPaint.setColor(secondaryColor);
            tagPaint.setTextSize(9f * scale);
            tagPaint.setTypeface(getSafeMonoTypeface());
            canvas.drawText(amPm, m1X + (2 * digW) + digitGap + (6f * scale), startY + (10f * scale), tagPaint);
        }
    }

    private static void draw7Seg(Canvas canvas, float x, float y, float w, float h, float t, int digit,
                                 Paint litPaint, Paint unlitPaint) {
        int mask = (digit >= 0 && digit <= 9) ? SEGMENTS_7[digit] : 0;
        float hHalf = h / 2f;

        // A (top)
        canvas.drawRect(x + t, y, x + w - t, y + t, ((mask & 0x01) != 0) ? litPaint : unlitPaint);
        // B (top-right)
        canvas.drawRect(x + w - t, y + t, x + w, y + hHalf - (t / 2f), ((mask & 0x02) != 0) ? litPaint : unlitPaint);
        // C (bottom-right)
        canvas.drawRect(x + w - t, y + hHalf + (t / 2f), x + w, y + h - t, ((mask & 0x04) != 0) ? litPaint : unlitPaint);
        // D (bottom)
        canvas.drawRect(x + t, y + h - t, x + w - t, y + h, ((mask & 0x08) != 0) ? litPaint : unlitPaint);
        // E (bottom-left)
        canvas.drawRect(x, y + hHalf + (t / 2f), x + t, y + h - t, ((mask & 0x10) != 0) ? litPaint : unlitPaint);
        // F (top-left)
        canvas.drawRect(x, y + t, x + t, y + hHalf - (t / 2f), ((mask & 0x20) != 0) ? litPaint : unlitPaint);
        // G (center)
        canvas.drawRect(x + t, y + hHalf - (t / 2f), x + w - t, y + hHalf + (t / 2f), ((mask & 0x40) != 0) ? litPaint : unlitPaint);
    }

    // --- Style 2: Mechanical Split-Flap ---
    private static void drawStyleSplitFlap(Canvas canvas, float scale, int hour, int minute, String amPm,
                                          int primaryColor, int secondaryColor) {
        float cardW = 20f * scale;
        float cardH = 34f * scale;
        float gap = 3f * scale;
        float colonW = 8f * scale;

        String hStr = String.format(Locale.US, "%02d", hour);
        String mStr = String.format(Locale.US, "%02d", minute);

        float totalW = (4 * cardW) + (2 * gap) + colonW + (amPm.isEmpty() ? 0 : 22f * scale);
        float startX = -totalW / 2f;
        float startY = -cardH / 2f;

        drawFlapCard(canvas, scale, startX, startY, cardW, cardH, hStr.charAt(0), primaryColor);
        drawFlapCard(canvas, scale, startX + cardW + gap, startY, cardW, cardH, hStr.charAt(1), primaryColor);

        // Center colon
        float colonX = startX + (2 * cardW) + gap + (colonW / 2f);
        Paint colonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        colonPaint.setColor(secondaryColor);
        canvas.drawCircle(colonX, startY + (cardH * 0.35f), 2f * scale, colonPaint);
        canvas.drawCircle(colonX, startY + (cardH * 0.65f), 2f * scale, colonPaint);

        float m1X = startX + (2 * cardW) + gap + colonW;
        drawFlapCard(canvas, scale, m1X, startY, cardW, cardH, mStr.charAt(0), primaryColor);
        drawFlapCard(canvas, scale, m1X + cardW + gap, startY, cardW, cardH, mStr.charAt(1), primaryColor);

        if (!amPm.isEmpty()) {
            float tagX = m1X + (2 * cardW) + gap + (5f * scale);
            drawFlapCard(canvas, scale, tagX, startY + (4f * scale), 16f * scale, 26f * scale, amPm.charAt(0), secondaryColor);
        }
    }

    private static void drawFlapCard(Canvas canvas, float scale, float x, float y, float w, float h, char ch, int textColor) {
        RectF cardRect = new RectF(x, y, x + w, y + h);

        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(Color.argb(220, 20, 24, 32));
        canvas.drawRoundRect(cardRect, 3f * scale, 3f * scale, bgPaint);

        Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(1f * scale);
        borderPaint.setColor(Color.argb(60, 255, 255, 255));
        canvas.drawRoundRect(cardRect, 3f * scale, 3f * scale, borderPaint);

        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(textColor);
        textPaint.setTextSize(h * 0.68f);
        textPaint.setTypeface(getSafeBoldTypeface());
        textPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(String.valueOf(ch), x + (w / 2f), y + (h * 0.72f), textPaint);

        // Center split seam
        Paint splitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        splitPaint.setColor(Color.argb(180, 10, 12, 16));
        splitPaint.setStrokeWidth(1.2f * scale);
        canvas.drawLine(x, y + (h / 2f), x + w, y + (h / 2f), splitPaint);

        Paint splitHighlight = new Paint(Paint.ANTI_ALIAS_FLAG);
        splitHighlight.setColor(Color.argb(40, 255, 255, 255));
        splitHighlight.setStrokeWidth(0.8f * scale);
        canvas.drawLine(x, y + (h / 2f) + (1f * scale), x + w, y + (h / 2f) + (1f * scale), splitHighlight);

        // Side hinge clips
        Paint pinPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pinPaint.setColor(Color.argb(220, 140, 150, 165));
        canvas.drawRect(x - (0.8f * scale), y + (h / 2f) - (2f * scale), x + (0.8f * scale), y + (h / 2f) + (2f * scale), pinPaint);
        canvas.drawRect(x + w - (0.8f * scale), y + (h / 2f) - (2f * scale), x + w + (0.8f * scale), y + (h / 2f) + (2f * scale), pinPaint);
    }

    // --- Style 3: Stacked 2x2 Gradient Pill ---
    private static void drawStyleStacked2x2(Canvas canvas, float scale, int hour, int minute, String amPm,
                                            int primaryColor, int secondaryColor) {
        String hourStr = String.format(Locale.US, "%02d", hour);
        String minStr = String.format(Locale.US, "%02d", minute);

        Paint hPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hPaint.setColor(primaryColor);
        hPaint.setTextSize(26f * scale);
        hPaint.setTypeface(getSafeBoldTypeface());
        hPaint.setTextAlign(Paint.Align.CENTER);

        Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(secondaryColor);
        mPaint.setTextSize(26f * scale);
        mPaint.setTypeface(getSafeBoldTypeface());
        mPaint.setTextAlign(Paint.Align.CENTER);

        float boxW = 54f * scale;
        float boxH = 58f * scale;
        RectF frame = new RectF(-boxW / 2f, -boxH / 2f, boxW / 2f, boxH / 2f);

        Paint framePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        framePaint.setStyle(Paint.Style.STROKE);
        framePaint.setColor(Color.argb(55, Color.red(secondaryColor), Color.green(secondaryColor), Color.blue(secondaryColor)));
        framePaint.setStrokeWidth(1.2f * scale);
        canvas.drawRoundRect(frame, 6f * scale, 6f * scale, framePaint);

        // Divider
        canvas.drawLine(-boxW * 0.4f, 0f, boxW * 0.4f, 0f, framePaint);

        // Hours & Minutes
        canvas.drawText(hourStr, 0f, -4f * scale, hPaint);
        canvas.drawText(minStr, 0f, 22f * scale, mPaint);

        if (!amPm.isEmpty()) {
            Paint amPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            amPaint.setColor(secondaryColor);
            amPaint.setTextSize(9f * scale);
            amPaint.setTypeface(getSafeBoldTypeface());
            canvas.drawText(amPm, (boxW / 2f) + (6f * scale), -boxH * 0.15f, amPaint);
        }
    }

    // --- Style 4: Dot Matrix LED ---
    private static void drawStyleDotMatrix(Canvas canvas, float scale, int hour, int minute, String amPm,
                                           int primaryColor, int secondaryColor) {
        float dotR = 1.3f * scale;
        float pitch = 3.4f * scale;
        float digW = 5 * pitch;
        float colonW = 3 * pitch;
        float digitGap = 2 * pitch;

        int h1 = hour / 10;
        int h2 = hour % 10;
        int m1 = minute / 10;
        int m2 = minute % 10;

        float totalW = (4 * digW) + (2 * digitGap) + colonW;
        float startX = -totalW / 2f;
        float startY = -3.5f * pitch;

        Paint litPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        litPaint.setColor(primaryColor);

        Paint unlitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        unlitPaint.setColor(Color.argb(20, Color.red(secondaryColor), Color.green(secondaryColor), Color.blue(secondaryColor)));

        // H1 & H2
        drawDotMatrixDigit(canvas, startX, startY, dotR, pitch, h1, litPaint, unlitPaint);
        drawDotMatrixDigit(canvas, startX + digW + digitGap, startY, dotR, pitch, h2, litPaint, unlitPaint);

        // Colon
        float colonX = startX + (2 * digW) + digitGap + (pitch * 1.5f);
        canvas.drawCircle(colonX, startY + (2 * pitch), dotR, litPaint);
        canvas.drawCircle(colonX, startY + (4 * pitch), dotR, litPaint);

        // M1 & M2
        float m1X = startX + (2 * digW) + digitGap + colonW;
        drawDotMatrixDigit(canvas, m1X, startY, dotR, pitch, m1, litPaint, unlitPaint);
        drawDotMatrixDigit(canvas, m1X + digW + digitGap, startY, dotR, pitch, m2, litPaint, unlitPaint);

        if (!amPm.isEmpty()) {
            Paint amPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            amPaint.setColor(secondaryColor);
            amPaint.setTextSize(9f * scale);
            amPaint.setTypeface(getSafeMonoTypeface());
            canvas.drawText(amPm, m1X + (2 * digW) + digitGap + (6f * scale), startY + (3 * pitch), amPaint);
        }
    }

    private static void drawDotMatrixDigit(Canvas canvas, float startX, float startY, float dotR, float pitch,
                                           int digit, Paint litPaint, Paint unlitPaint) {
        byte[] cols = (digit >= 0 && digit <= 9) ? DOT_MATRIX_5X7[digit] : DOT_MATRIX_5X7[0];
        for (int c = 0; c < 5; c++) {
            byte colVal = cols[c];
            for (int r = 0; r < 7; r++) {
                boolean lit = (colVal & (1 << r)) != 0;
                float cx = startX + (c * pitch);
                float cy = startY + (r * pitch);
                canvas.drawCircle(cx, cy, dotR, lit ? litPaint : unlitPaint);
            }
        }
    }

    // --- Style 5: Superscript Seconds & Clean Date ---
    private static void drawStyleSuperscriptSeconds(Canvas canvas, float scale, int hour, int minute, int second,
                                                    Calendar now, String amPm, int primaryColor, int secondaryColor) {
        String timeStr = String.format(Locale.US, "%02d:%02d", hour, minute);
        String secStr = String.format(Locale.US, ":%02d", second);
        String dateStr = DAY_ABBR[now.get(Calendar.DAY_OF_WEEK) - 1] + " " + String.format(Locale.US, "%02d", now.get(Calendar.DAY_OF_MONTH));

        Paint timePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        timePaint.setColor(primaryColor);
        timePaint.setTextSize(36f * scale);
        timePaint.setTypeface(getSafeBoldTypeface());

        Paint secPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        secPaint.setColor(secondaryColor);
        secPaint.setTextSize(14f * scale);
        secPaint.setTypeface(getSafeBoldTypeface());

        Paint datePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        datePaint.setColor(Color.argb(200, Color.red(secondaryColor), Color.green(secondaryColor), Color.blue(secondaryColor)));
        datePaint.setTextSize(10f * scale);
        datePaint.setTypeface(getSafeMonoTypeface());

        float timeW = timePaint.measureText(timeStr);
        float totalW = timeW + (40f * scale);
        float startX = -totalW / 2f;
        float baseTextY = 12f * scale;

        // Big Time
        canvas.drawText(timeStr, startX, baseTextY, timePaint);

        // Divider
        float divX = startX + timeW + (6f * scale);
        Paint divPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        divPaint.setColor(Color.argb(60, Color.red(secondaryColor), Color.green(secondaryColor), Color.blue(secondaryColor)));
        divPaint.setStrokeWidth(1.2f * scale);
        canvas.drawLine(divX, baseTextY - (28f * scale), divX, baseTextY, divPaint);

        // Seconds + AM/PM
        float metaX = divX + (6f * scale);
        canvas.drawText(secStr + (amPm.isEmpty() ? "" : " " + amPm), metaX, baseTextY - (15f * scale), secPaint);
        // Date
        canvas.drawText(dateStr, metaX, baseTextY - (2f * scale), datePaint);
    }

    // --- Style 6: Bold Athletic Block / Stencil ---
    private static void drawStyleAthleticBlock(Canvas canvas, float scale, int hour, int minute, String amPm,
                                               int primaryColor, int secondaryColor) {
        String clockStr = String.format(Locale.US, "%02d:%02d", hour, minute);

        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(primaryColor);
        textPaint.setTextSize(36f * scale);
        textPaint.setTypeface(getSafeMonoTypeface());
        textPaint.setTextAlign(Paint.Align.CENTER);

        float textW = textPaint.measureText(clockStr);
        float baselineY = 12f * scale;

        // Draw bold text
        canvas.drawText(clockStr, 0f, baselineY, textPaint);

        // Stencil horizontal cutout gap across the midline
        Paint gapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gapPaint.setColor(Color.argb(210, 16, 20, 28)); // Widget backing tone
        gapPaint.setStrokeWidth(1.8f * scale);
        canvas.drawLine(-textW * 0.52f, baselineY - (12f * scale), textW * 0.52f, baselineY - (12f * scale), gapPaint);

        // Athletic bracket ticks
        Paint bracketPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bracketPaint.setColor(secondaryColor);
        bracketPaint.setStyle(Paint.Style.STROKE);
        bracketPaint.setStrokeWidth(1.5f * scale);

        float bX1 = -textW * 0.56f;
        float bX2 = textW * 0.56f;
        float bY1 = baselineY - (28f * scale);
        float bY2 = baselineY + (4f * scale);

        canvas.drawLine(bX1, bY1, bX1 + (6f * scale), bY1, bracketPaint);
        canvas.drawLine(bX1, bY1, bX1, bY1 + (6f * scale), bracketPaint);

        canvas.drawLine(bX2, bY2, bX2 - (6f * scale), bY2, bracketPaint);
        canvas.drawLine(bX2, bY2, bX2, bY2 - (6f * scale), bracketPaint);

        if (!amPm.isEmpty()) {
            Paint amPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            amPaint.setColor(secondaryColor);
            amPaint.setTextSize(9f * scale);
            amPaint.setTypeface(getSafeMonoTypeface());
            canvas.drawText(amPm, bX2 + (4f * scale), bY1 + (8f * scale), amPaint);
        }
    }

    // --- Style 7: Whimsical Cartoon Bubble ---
    private static void drawStyleCartoonBubble(Canvas canvas, float scale, int hour, int minute, String amPm,
                                               int primaryColor, int secondaryColor) {
        String clockStr = String.format(Locale.US, "%02d:%02d", hour, minute);

        Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint.setColor(Color.argb(90, 0, 0, 0));
        shadowPaint.setTextSize(35f * scale);
        shadowPaint.setTypeface(getSafeBoldTypeface());
        shadowPaint.setTextAlign(Paint.Align.CENTER);

        Paint mainPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mainPaint.setColor(primaryColor);
        mainPaint.setTextSize(35f * scale);
        mainPaint.setTypeface(getSafeBoldTypeface());
        mainPaint.setTextAlign(Paint.Align.CENTER);

        float baselineY = 12f * scale;

        // Shadow
        canvas.drawText(clockStr, 1.5f * scale, baselineY + (2.5f * scale), shadowPaint);
        // Main bubble text
        canvas.drawText(clockStr, 0f, baselineY, mainPaint);

        // Bubble reflection glossy highlights
        float textW = mainPaint.measureText(clockStr);
        Paint highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlightPaint.setColor(Color.argb(120, 255, 255, 255));
        highlightPaint.setStyle(Paint.Style.STROKE);
        highlightPaint.setStrokeWidth(1.2f * scale);
        highlightPaint.setStrokeCap(Paint.Cap.ROUND);

        canvas.drawArc(new RectF(-textW * 0.42f, baselineY - (26f * scale), -textW * 0.28f, baselineY - (14f * scale)),
                200, 100, false, highlightPaint);
        canvas.drawArc(new RectF(textW * 0.18f, baselineY - (26f * scale), textW * 0.32f, baselineY - (14f * scale)),
                200, 100, false, highlightPaint);

        if (!amPm.isEmpty()) {
            Paint amPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            amPaint.setColor(secondaryColor);
            amPaint.setTextSize(10f * scale);
            amPaint.setTypeface(getSafeBoldTypeface());
            canvas.drawText(amPm, (textW / 2f) + (6f * scale), baselineY - (10f * scale), amPaint);
        }
    }

    // --- Style 8: Modular Mosaic Block ---
    private static void drawStyleModularMosaic(Canvas canvas, float scale, int hour, int minute, String amPm,
                                               int primaryColor, int secondaryColor) {
        String hStr = String.format(Locale.US, "%02d", hour);
        String mStr = String.format(Locale.US, "%02d", minute);

        Paint blockText = new Paint(Paint.ANTI_ALIAS_FLAG);
        blockText.setColor(primaryColor);
        blockText.setTextSize(32f * scale);
        blockText.setTypeface(getSafeMonoTypeface());
        blockText.setTextAlign(Paint.Align.CENTER);

        float textW = blockText.measureText(hStr + " " + mStr);
        float startX = -textW / 2f;
        float baselineY = 11f * scale;

        canvas.drawText(hStr, startX + (textW * 0.22f), baselineY, blockText);
        canvas.drawText(mStr, startX + (textW * 0.78f), baselineY, blockText);

        // Center mosaic block separator (3 small rounded blocks)
        Paint mosaicPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mosaicPaint.setColor(secondaryColor);
        float cX = 0f;
        float bSize = 3.5f * scale;
        canvas.drawRoundRect(new RectF(cX - bSize / 2f, baselineY - (22f * scale), cX + bSize / 2f, baselineY - (22f * scale) + bSize), 1f * scale, 1f * scale, mosaicPaint);
        canvas.drawRoundRect(new RectF(cX - bSize / 2f, baselineY - (13f * scale), cX + bSize / 2f, baselineY - (13f * scale) + bSize), 1f * scale, 1f * scale, mosaicPaint);
        canvas.drawRoundRect(new RectF(cX - bSize / 2f, baselineY - (4f * scale), cX + bSize / 2f, baselineY - (4f * scale) + bSize), 1f * scale, 1f * scale, mosaicPaint);

        if (!amPm.isEmpty()) {
            Paint amPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            amPaint.setColor(secondaryColor);
            amPaint.setTextSize(9f * scale);
            amPaint.setTypeface(getSafeMonoTypeface());
            canvas.drawText(amPm, (textW / 2f) + (4f * scale), baselineY - (10f * scale), amPaint);
        }
    }

    // --- Style 9: Sci-Fi Stencil Squircle ---
    private static void drawStyleSciFiSquircle(Canvas canvas, float scale, int hour, int minute, String amPm,
                                               int primaryColor, int secondaryColor) {
        String hStr = String.format(Locale.US, "%02d", hour);
        String mStr = String.format(Locale.US, "%02d", minute);

        float tileW = 38f * scale;
        float tileH = 34f * scale;
        float gap = 6f * scale;

        float totalW = (2 * tileW) + gap + (amPm.isEmpty() ? 0 : 20f * scale);
        float startX = -totalW / 2f;
        float startY = -tileH / 2f;

        // Telemetry header
        Paint techPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        techPaint.setColor(Color.argb(160, Color.red(secondaryColor), Color.green(secondaryColor), Color.blue(secondaryColor)));
        techPaint.setTextSize(7.5f * scale);
        techPaint.setTypeface(getSafeMonoTypeface());
        canvas.drawText("CHRONO // 01", startX, startY - (4f * scale), techPaint);

        // Hours Tile
        drawTechTile(canvas, scale, startX, startY, tileW, tileH, hStr, primaryColor, secondaryColor);
        // Minutes Tile
        drawTechTile(canvas, scale, startX + tileW + gap, startY, tileW, tileH, mStr, primaryColor, secondaryColor);

        if (!amPm.isEmpty()) {
            float tagX = startX + (2 * tileW) + gap + (6f * scale);
            canvas.drawText(amPm, tagX, startY + (tileH * 0.6f), techPaint);
        }
    }

    private static void drawTechTile(Canvas canvas, float scale, float x, float y, float w, float h, String text,
                                     int primaryColor, int secondaryColor) {
        RectF rect = new RectF(x, y, x + w, y + h);

        Paint bg = new Paint(Paint.ANTI_ALIAS_FLAG);
        bg.setColor(Color.argb(210, 15, 20, 28));
        canvas.drawRoundRect(rect, 4f * scale, 4f * scale, bg);

        Paint frame = new Paint(Paint.ANTI_ALIAS_FLAG);
        frame.setStyle(Paint.Style.STROKE);
        frame.setColor(Color.argb(70, Color.red(secondaryColor), Color.green(secondaryColor), Color.blue(secondaryColor)));
        frame.setStrokeWidth(1f * scale);
        canvas.drawRoundRect(rect, 4f * scale, 4f * scale, frame);

        // Corner bracket cuts
        Paint corner = new Paint(Paint.ANTI_ALIAS_FLAG);
        corner.setColor(secondaryColor);
        corner.setStrokeWidth(1.4f * scale);
        canvas.drawLine(x, y, x + (4f * scale), y, corner);
        canvas.drawLine(x, y, x, y + (4f * scale), corner);
        canvas.drawLine(x + w, y + h, x + w - (4f * scale), y + h, corner);
        canvas.drawLine(x + w, y + h, x + w, y + h - (4f * scale), corner);

        Paint tp = new Paint(Paint.ANTI_ALIAS_FLAG);
        tp.setColor(primaryColor);
        tp.setTextSize(22f * scale);
        tp.setTypeface(getSafeMonoTypeface());
        tp.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(text, x + (w / 2f), y + (h * 0.72f), tp);
    }

    // --- Style 10: Ultra-Condensed Tall Deco ---
    private static void drawStyleTallDeco(Canvas canvas, float scale, int hour, int minute, String amPm,
                                          int primaryColor, int secondaryColor) {
        String clockStr = String.format(Locale.US, "%02d:%02d", hour, minute);

        canvas.save();
        // Tall Art Deco stretch
        canvas.scale(0.72f, 1.45f);

        Paint decoPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        decoPaint.setColor(primaryColor);
        decoPaint.setTextSize(32f * scale);
        decoPaint.setTypeface(getSafeBoldTypeface());
        decoPaint.setTextAlign(Paint.Align.CENTER);

        float baselineY = 8f * scale;
        canvas.drawText(clockStr, 0f, baselineY, decoPaint);

        canvas.restore();

        if (!amPm.isEmpty()) {
            Paint amPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            amPaint.setColor(secondaryColor);
            amPaint.setTextSize(9f * scale);
            amPaint.setTypeface(getSafeBoldTypeface());
            canvas.drawText(amPm, (46f * scale), 8f * scale, amPaint);
        }
    }

    // --- Style 11: Minimalist Analog Dial Hybrid ---
    private static void drawStyleAnalogDialHybrid(Canvas canvas, float scale, int hour, int minute, String amPm,
                                                  int primaryColor, int secondaryColor) {
        float dialR = 19f * scale;
        float dialCenterX = -36f * scale;
        float dialCenterY = 0f;

        // 1. Dial Outer Rim
        Paint rimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rimPaint.setStyle(Paint.Style.STROKE);
        rimPaint.setColor(Color.argb(90, Color.red(secondaryColor), Color.green(secondaryColor), Color.blue(secondaryColor)));
        rimPaint.setStrokeWidth(1.2f * scale);
        canvas.drawCircle(dialCenterX, dialCenterY, dialR, rimPaint);

        // 2. 12 Tick Marks
        Paint tickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tickPaint.setColor(secondaryColor);
        for (int i = 0; i < 12; i++) {
            float angle = (float) Math.toRadians(i * 30.0);
            float len = (i % 3 == 0) ? (3.5f * scale) : (2f * scale);
            tickPaint.setStrokeWidth((i % 3 == 0) ? 1.5f * scale : 1f * scale);
            float x1 = dialCenterX + (float) Math.sin(angle) * (dialR - len);
            float y1 = dialCenterY - (float) Math.cos(angle) * (dialR - len);
            float x2 = dialCenterX + (float) Math.sin(angle) * dialR;
            float y2 = dialCenterY - (float) Math.cos(angle) * dialR;
            canvas.drawLine(x1, y1, x2, y2, tickPaint);
        }

        // 3. Hands
        float hourAngle = (float) Math.toRadians(((hour % 12) + (minute / 60f)) * 30.0);
        float minAngle = (float) Math.toRadians(minute * 6.0);

        // Hour Hand
        Paint hourHand = new Paint(Paint.ANTI_ALIAS_FLAG);
        hourHand.setColor(primaryColor);
        hourHand.setStrokeWidth(2.2f * scale);
        hourHand.setStrokeCap(Paint.Cap.ROUND);
        float hLen = dialR * 0.55f;
        canvas.drawLine(dialCenterX, dialCenterY, dialCenterX + (float) Math.sin(hourAngle) * hLen,
                dialCenterY - (float) Math.cos(hourAngle) * hLen, hourHand);

        // Minute Hand
        Paint minHand = new Paint(Paint.ANTI_ALIAS_FLAG);
        minHand.setColor(secondaryColor);
        minHand.setStrokeWidth(1.5f * scale);
        minHand.setStrokeCap(Paint.Cap.ROUND);
        float mLen = dialR * 0.8f;
        canvas.drawLine(dialCenterX, dialCenterY, dialCenterX + (float) Math.sin(minAngle) * mLen,
                dialCenterY - (float) Math.cos(minAngle) * mLen, minHand);

        // Center Pin
        Paint pin = new Paint(Paint.ANTI_ALIAS_FLAG);
        pin.setColor(primaryColor);
        canvas.drawCircle(dialCenterX, dialCenterY, 2f * scale, pin);

        // 4. Digital Time Display (Right)
        String timeStr = String.format(Locale.US, "%02d:%02d", hour, minute);
        Paint digPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        digPaint.setColor(primaryColor);
        digPaint.setTextSize(26f * scale);
        digPaint.setTypeface(getSafeBoldTypeface());

        float textStartX = 0f;
        float baseTextY = 9f * scale;
        canvas.drawText(timeStr, textStartX, baseTextY, digPaint);

        if (!amPm.isEmpty()) {
            Paint amPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            amPaint.setColor(secondaryColor);
            amPaint.setTextSize(9f * scale);
            amPaint.setTypeface(getSafeBoldTypeface());
            canvas.drawText(amPm, textStartX + digPaint.measureText(timeStr) + (4f * scale), baseTextY - (10f * scale), amPaint);
        }
    }
}
