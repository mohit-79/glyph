package com.glyph.widget.compositor;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.DisplayMetrics;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * WallpaperHelper manages wallpaper capture, caching, and coordinate-aligned
 * optical blur sampling for home screen widgets.
 */
public class WallpaperHelper {

    public static final String WALLPAPER_CACHE_FILE = "glyph_wallpaper_cache.png";

    // 5 Screen Position Presets
    public static final int POSITION_TOP = 0;
    public static final int POSITION_UPPER_CENTER = 1;
    public static final int POSITION_CENTER = 2;
    public static final int POSITION_LOWER_CENTER = 3;
    public static final int POSITION_BOTTOM = 4;

    public static final String[] POSITION_NAMES = {
            "Top (Rows 1-2)",
            "Upper Center (Rows 2-3)",
            "Center (Rows 3-4)",
            "Lower Center (Rows 4-5)",
            "Bottom (Rows 5-6)"
    };

    /**
     * Checks if a user or system wallpaper is currently cached in private storage.
     */
    public static boolean hasCachedWallpaper(Context context) {
        if (context == null) return false;
        File file = new File(context.getFilesDir(), WALLPAPER_CACHE_FILE);
        return file.exists() && file.length() > 0;
    }

    /**
     * Retrieves the cached wallpaper bitmap. Returns null if not cached or decode fails.
     */
    public static Bitmap getCachedWallpaper(Context context) {
        if (context == null) return null;
        File file = new File(context.getFilesDir(), WALLPAPER_CACHE_FILE);
        if (!file.exists() || file.length() == 0) return null;

        try {
            // First decode bounds
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(file.getAbsolutePath(), opts);

            // Downsample if excessively large to protect memory
            opts.inSampleSize = 1;
            int maxDim = Math.max(opts.outWidth, opts.outHeight);
            if (maxDim > 2400) {
                opts.inSampleSize = 2;
            }
            opts.inJustDecodeBounds = false;
            return BitmapFactory.decodeFile(file.getAbsolutePath(), opts);
        } catch (Throwable t) {
            return null;
        }
    }

    /**
     * Saves a wallpaper bitmap into app private storage for instant widget access.
     */
    public static boolean saveWallpaperBitmap(Context context, Bitmap bitmap) {
        if (context == null || bitmap == null || bitmap.isRecycled()) return false;
        FileOutputStream fos = null;
        try {
            File file = new File(context.getFilesDir(), WALLPAPER_CACHE_FILE);

            // Scale down if larger than 1080x2400 to keep memory lean
            Bitmap toSave = bitmap;
            int maxW = 1080;
            int maxH = 2400;
            if (bitmap.getWidth() > maxW || bitmap.getHeight() > maxH) {
                float aspect = (float) bitmap.getWidth() / (float) bitmap.getHeight();
                int targetW = maxW;
                int targetH = (int) (maxW / aspect);
                if (targetH > maxH) {
                    targetH = maxH;
                    targetW = (int) (maxH * aspect);
                }
                toSave = Bitmap.createScaledBitmap(bitmap, Math.max(10, targetW), Math.max(10, targetH), true);
            }

            fos = new FileOutputStream(file);
            toSave.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            return true;
        } catch (Throwable t) {
            return false;
        } finally {
            if (fos != null) {
                try { fos.close(); } catch (Throwable ignored) {}
            }
        }
    }

    /**
     * Imports a user-selected wallpaper from a content URI.
     */
    public static boolean saveWallpaperFromUri(Context context, Uri uri) {
        if (context == null || uri == null) return false;
        InputStream is = null;
        try {
            is = context.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            if (bitmap != null) {
                return saveWallpaperBitmap(context, bitmap);
            }
        } catch (Throwable t) {
            return false;
        } finally {
            if (is != null) {
                try { is.close(); } catch (Throwable ignored) {}
            }
        }
        return false;
    }

    /**
     * Attempts to read and cache the system wallpaper from WallpaperManager.
     */
    public static boolean syncSystemWallpaper(Context context) {
        if (context == null) return false;
        try {
            WallpaperManager wm = WallpaperManager.getInstance(context);
            Drawable drawable = wm.getDrawable();
            if (drawable instanceof BitmapDrawable) {
                Bitmap bmp = ((BitmapDrawable) drawable).getBitmap();
                if (bmp != null && !bmp.isRecycled()) {
                    return saveWallpaperBitmap(context, bmp);
                }
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

    /**
     * Crops the wallpaper slice aligned with the widget's home screen coordinates,
     * extracts the pill region, and applies FastBlur optical blur.
     */
    public static Bitmap cropAndBlurForWidget(Context context, int widgetWidth, int widgetHeight,
                                             RectF pillRect, int blurRadius, int positionIndex) {
        if (context == null || widgetWidth <= 0 || widgetHeight <= 0 || pillRect == null) {
            return null;
        }

        Bitmap wallpaper = getCachedWallpaper(context);
        if (wallpaper == null) {
            // Attempt auto-sync if not yet cached
            if (syncSystemWallpaper(context)) {
                wallpaper = getCachedWallpaper(context);
            }
        }

        if (wallpaper == null || wallpaper.isRecycled()) {
            return null;
        }

        try {
            int wpW = wallpaper.getWidth();
            int wpH = wallpaper.getHeight();

            // Vertical boundaries based on home screen grid placement
            float yStartRatio;
            float yEndRatio;
            switch (positionIndex) {
                case POSITION_TOP:
                    yStartRatio = 0.07f;
                    yEndRatio = 0.28f;
                    break;
                case POSITION_UPPER_CENTER:
                    yStartRatio = 0.22f;
                    yEndRatio = 0.43f;
                    break;
                case POSITION_CENTER:
                    yStartRatio = 0.37f;
                    yEndRatio = 0.58f;
                    break;
                case POSITION_LOWER_CENTER:
                    yStartRatio = 0.52f;
                    yEndRatio = 0.73f;
                    break;
                case POSITION_BOTTOM:
                    yStartRatio = 0.67f;
                    yEndRatio = 0.88f;
                    break;
                default:
                    yStartRatio = 0.07f;
                    yEndRatio = 0.28f;
                    break;
            }

            // Horizontal span: widgets typically occupy ~92% of the home screen width
            float xStartRatio = 0.04f;
            float xEndRatio = 0.96f;

            int cropX = (int) (wpW * xStartRatio);
            int cropY = (int) (wpH * yStartRatio);
            int cropW = (int) (wpW * (xEndRatio - xStartRatio));
            int cropH = (int) (wpH * (yEndRatio - yStartRatio));

            cropX = Math.max(0, Math.min(cropX, wpW - 10));
            cropY = Math.max(0, Math.min(cropY, wpH - 10));
            cropW = Math.max(10, Math.min(cropW, wpW - cropX));
            cropH = Math.max(10, Math.min(cropH, wpH - cropY));

            Bitmap widgetSlice = Bitmap.createBitmap(wallpaper, cropX, cropY, cropW, cropH);
            Bitmap scaledWidget = Bitmap.createScaledBitmap(widgetSlice, widgetWidth, widgetHeight, true);

            // Now crop the inner pill inside the widget canvas
            int pL = (int) Math.max(0, pillRect.left);
            int pT = (int) Math.max(0, pillRect.top);
            int pW = (int) Math.max(10, Math.min(pillRect.width(), scaledWidget.getWidth() - pL));
            int pH = (int) Math.max(10, Math.min(pillRect.height(), scaledWidget.getHeight() - pT));

            if (pL + pW <= scaledWidget.getWidth() && pT + pH <= scaledWidget.getHeight()) {
                Bitmap pillSlice = Bitmap.createBitmap(scaledWidget, pL, pT, pW, pH);
                return FastBlur.blurFast(pillSlice, Math.max(1, Math.min(50, blurRadius)));
            }
        } catch (Throwable t) {
            return null;
        }

        return null;
    }
}
