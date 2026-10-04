# Glyph: Artistic Home Screen Widgets

Glyph is an Android home screen widget app engineered from the ground up with a pure 2D Canvas Compositor engine. Instead of relying on rigid, clunky XML View hierarchies that suffer from multi-line text wrapping or clipping during resize, Glyph renders complete widgets (background pills, borders, calendar grids, and clock digits) onto high-resolution 2D bitmaps and transfers them directly to the launcher via RemoteViews.

This repository contains the complete source code, architecture, and standalone build toolchain for Glyph (`com.glyph.widget`).

---

## Architecture & Project Structure

The project is structured for high modularity, per-widget isolated state persistence, and direct compilation via Android build tools (AAPT, javac, D8, apksigner) without Gradle overhead:

```
glyph/
├── AndroidManifest.xml                  # Application blueprint, permissions, and component contracts
├── README.md                            # Project documentation and architectural guide
├── build.sh                             # Compilation, dexing, packaging, and signing pipeline
├── debug.keystore                       # Development signing keystore
├── res/                                 # Packaged application resources
│   ├── drawable/                        # Vector assets and background card drawables
│   │   ├── bg_card.xml                  # Rounded card container style
│   │   ├── ic_launcher.xml              # Vector launcher icon
│   │   └── widget_preview.xml           # Launcher widget preview asset
│   ├── layout/                          # UI layout hierarchies
│   │   ├── activity_main.xml            # Multi-widget hub and selector dashboard
│   │   ├── activity_widget_config.xml   # Dedicated per-widget live customization studio
│   │   └── widget_clock_calendar.xml    # RemoteViews container for launcher instances
│   ├── values/                          # Scalar values
│   │   ├── colors.xml                   # Semantic color definitions
│   │   └── strings.xml                  # Application strings and labels
│   └── xml/                             # System metadata descriptors
│       └── glyph_clock_calendar_widget_info.xml # AppWidgetProviderInfo configuration
└── src/                                 # Java source code
    └── com/glyph/widget/
        ├── MainActivity.java            # Multi-widget hub launching isolated customization studios
        ├── WidgetConfigActivity.java    # Interactive customizer with live canvas preview
        ├── GlyphWidgetProvider.java     # BroadcastReceiver managing widget lifecycle and updates
        ├── GlyphPrefs.java              # Namespaced SharedPreferences state persistence engine
        ├── GlyphTheme.java              # Catalog of 27 curated color palettes and theme matrices
        └── compositor/                  # Pure 2D Canvas rendering engine
            ├── WidgetCanvas.java        # High-DPI bitmap compositor and pill geometry engine
            ├── CalendarRenderer.java    # Dynamic live-date calendar with 2D transform matrix
            ├── ClockRenderer.java       # Single-line live digital clock with 2D transform matrix
            ├── FastBlur.java            # Dual-pass optical StackBlur algorithm implementation
            └── WallpaperHelper.java     # System wallpaper sampler, gallery picker, and coordinate slicer
```

---

## Core Engine Components

### 1. 2D Canvas Compositor Engine (`WidgetCanvas.java`)
Standard Android widget layouts run in the launcher's process via `RemoteViews`, which severely limits layout flexibility and introduces graphical artifacts during resizing. Glyph solves this by compositing the entire widget surface directly onto an in-memory `android.graphics.Bitmap` at device DPI:
* Computes rounded pill bounds with sub-pixel floating-point geometry.
* Evaluates unoccupied space offsets derived from 4 independent margin tuners.
* Renders borders, backgrounds, and element layers with anti-aliasing.
* Transfers the finalized bitmap to `RemoteViews.setImageViewBitmap()`.

### 2. Isolated Multi-Widget Persistence (`GlyphPrefs.java`)
All widget settings are namespaced by widget type (e.g. `clock_calendar`), ensuring independent configuration for each widget family without crosstalk:
* 4-side independent margins (`margin_left`, `margin_right`, `margin_top`, `margin_bottom`).
* Corner radius styling.
* Active theme index (0 to 26).
* Optical blur intensity and screen placement.
* Border thickness and custom sRGB border color override.
* Calendar continuous horizontal offset (X), vertical offset (Y), and scale zoom.

### 3. Optical Blur & Wallpaper Engine (`WallpaperHelper.java`, `FastBlur.java`)
Because Android security sandboxing prevents widgets from capturing home screen pixels directly, Glyph provides true optical blur through coordinate-aligned sampling:
* **System Wallpaper Auto-Sync**: Queries `WallpaperManager` with runtime storage permissions.
* **Gallery Photo Picker Fallback**: Fallback picker for devices with dynamic or live wallpapers (such as MIUI Super Wallpapers).
* **Coordinate Ratio Slicing**: Slices the underlying wallpaper into 5 vertical bands (`Top`, `Upper Center`, `Center`, `Lower Center`, `Bottom`) to accurately align background imagery.
* **StackBlur Pipeline**: High-speed, dual-pass box-blur approximation with adjustable radius (1 to 25px) and zero white tint for genuine frosted transparency.
* **Frosted Fallback**: When wallpaper is unsynced, renders a delicate frosted diffusion shader so widgets remain readable.

### 4. Live 2D Calendar Engine (`CalendarRenderer.java`)
Draws an authentic, real-time monthly calendar directly onto the 2D canvas with mathematical precision and support for 11 distinct artistic reference styles:
* Calculates the active month, total days, first-day-of-week offset, and current day index.
* **11 Distinct Artistic Styles**:
  1. **Pill Range**: Curved horizontal capsule pill embracing the active week range with dynamic range bounds.
  2. **Circle Bubble Cluster**: Dual-state translucent circular bubble tiles with filled pop badge and weekend outlines.
  3. **Clean Monospace**: Minimalist high-fashion typographic grid with monospace alignment and horizontal hairline rule.
  4. **Neomorphic Frosted Card**: Soft frosted sub-card backing with recessed header pill and elevated date badges.
  5. **Modular Rounded Square Grid**: Individual rounded square tile matrix with uniform spacing and subtle border strokes.
  6. **Inset Sunken Dial Hybrid**: Asymmetric layout with a sunken circular month-progress dial gauge on the left and a compact calendar grid on the right.
  7. **Dotted Accent Calendar**: Micro-dot matrix indicators under each date with concentric ring active badge.
  8. **Compact Headerless**: Dense glanceable layout with expanded bold date numerals and clean dual-tag status line.
  9. **Segmented Row Focus**: High-contrast week-row focus container with dimmed context weeks for rapid temporal awareness.
  10. **High-Contrast Dark Grid**: Graphic circular badges distinguishing past, future, and active dates with dual-ring badges.
  11. **Modern Sans Grid**: Clean geometric sans typography with two vertical tinted accent columns behind weekend days.
* **Continuous Transformations**:
  * Horizontal Position (X): -120dp to +120dp
  * Vertical Position (Y): -80dp to +80dp
  * Calendar Scale (Zoom): 50% to 180%
* In-app interactive style selector with previous/next quick-cycling buttons and live descriptions.
* One-tap reset button to restore default positioning and scale.

### 5. Live 2D Clock Engine (`ClockRenderer.java`)
Renders a live digital clock directly onto the 2D canvas with sub-pixel alignment, single-line text measurement guarantee, and continuous 2D transform controls:
* **Single-Line Guarantee**: Accurately computes horizontal text metrics (`Paint.measureText`) for digits, colon separator, and AM/PM tag, drawing them along a shared baseline to strictly avoid multi-line digit wrapping.
* **12h / 24h Toggle**: In-studio button switching between 12-hour format (with capsule AM/PM badge) and 24-hour military format.
* **Superscript AM/PM Capsule Badge**: In 12-hour mode, renders AM/PM indicator inside a stylish rounded capsule badge beside the minute digits.
* **Continuous Transformations**:
  * Horizontal Position (X): -120dp to +120dp
  * Vertical Position (Y): -80dp to +80dp
  * Clock Scale (Zoom): 50% to 250%
* **One-Tap Reset**: Restores default clock coordinates and scale without affecting calendar transforms.
* **Perpetual Minute Tick Sync**: Employs `AlarmManager.setExactAndAllowWhileIdle` (`ACTION_UPDATE_GLYPH`) to wake and tick widgets precisely on the minute rollover, combined with system broadcast listeners (`TIME_SET`, `TIMEZONE_CHANGED`, `DATE_CHANGED`, `BOOT_COMPLETED`), reflection-based API 31+ permission checks, and inexact fallback.
* **Crash Resilience & Defensive Guards**: Safe cached Typeface resolution avoiding null font metrics across custom ROMs, wrapped canvas drawing routines, and guarded widget update broadcasts ensuring the customizer never crashes or restarts during slider manipulation.

### 6. Two-Tone Calendar sRGB Gamut Customizer
Provides independent full-spectrum color customization for the two functional calendar tones:
* **Tone 1 (Primary Accent)**: Month/Year header, active day badge, capsule pill bounds, and progress gauge arcs.
* **Tone 2 (Secondary Tone)**: Day-of-week headers, secondary date numerals, grid dividers, and dial tracks.
* **Dual-Tab UI**: Tabbed selector with live circular swatches, dynamic hex codes, and "Theme Default" vs "Custom Override" indicators.
* **Full sRGB Gamut**: Rainbow Hue spectrum bar (0 to 360 degrees), Saturation slider (0% to 100%), and Brightness/Value slider (0% to 100%) for exact hue tuning.
* **8-Color Quick Palette**: Fast 1-tap presets for White, Slate, Charcoal, Red, Amber, Emerald, Cyan, and Violet.
* **Selective Resets**: "Reset Active Tone" button and "Reset Both Tones" button to independently restore active theme defaults.

### 7. Border Studio & Full sRGB Color Gamut Picker
* **Thickness Control**: Continuous slider from `0dp` (completely borderless) to `16dp` (ultra thick).
* **Full sRGB Gamut**: Rainbow Hue spectrum bar (0 to 360 degrees), Saturation slider (0% to 100%), and Brightness/Value slider (0% to 100%) providing access to all 16.7 million colors.
* **8-Color Quick Palette**: Quick swatches for White, Slate, Charcoal, Red, Amber, Emerald, Cyan, and Violet.
* **Theme Default Reset**: Instant reset button reverting to the active theme's default border palette.

### 8. Curated Theme Catalog (`GlyphTheme.java`)
Contains 27 curated themes with coordinated colors for backgrounds, borders, and typography:
1. Obsidian Dark
2. Porcelain Light
3. Emerald Mint
4. Cyber Neon
5. Nordic Lavender
6. Brushed Titanium
7. Amber Gold
8. Crimson Velvet
9. Deep Ocean
10. Matcha Latte
11. Terracotta Sunset
12. Midnight Purple
13. Monochrome Slate
14. Solar Dawn
15. Nordic Frost
16. Charcoal Minimal
17. Desert Dune
18. Rose Quartz
19. Cobalt Blue
20. Olive Drab
21. Graphite Orange
22. Pastel Mint
23. Steel Blue
24. Wine Berry
25. Mocha Espresso
26. Withering Glass (Dark smoked obsidian glass with soft translucency)
27. Frosted Glass (100% transparent widget with pure optical blur and zero white tint)

---

## Build Pipeline & Toolchain

The project features a standalone bash build script (`build.sh`) designed for fast compilation in Termux or any standard Linux environment without requiring Gradle:

```
res/ + AndroidManifest.xml
       │
       ▼ (aapt package -m -J)
  R.java + resources.ap_
       │
       ▼ (javac -cp android.jar)
  .class Bytecode
       │
       ▼ (d8 / r8)
  classes.dex
       │
       ▼ (aapt add)
  glyph-unsigned.apk
       │
       ▼ (apksigner)
  Signed Glyph.apk
```

### Building the APK
Run the build script from the repository root:
```bash
./build.sh
```
The output APK is compiled, dexed, aligned, and signed with `debug.keystore`, producing `Glyph.apk`.

---

## Roadmap & Commit History

* [x] **Commit 1**: Architecture & File Structure Documentation
* [x] **Commit 2**: Basic Installable App & Build Toolchain
* [x] **Commit 3**: Multi-Widget Entry Architecture (Clock & Calendar Provider)
* [x] **Commit 4**: Widget Background Pill & 4-Side Independent Margin Controls
* [x] **Commit 4.1**: Multi-Widget Listing Hub & Per-Widget Isolated Preferences
* [x] **Commit 5**: 25+ Curated Color Themes with Dual Text & Border Defaults
* [x] **Commit 5.1**: Withering Glass & Transparent Frosted Glass Separation
* [x] **Commit 5.2**: Wallpaper-Aligned Optical Blur Engine & Dynamic Glass Controls
* [x] **Commit 7**: Border Customizer (0 to 16dp Thickness, sRGB HSV Spectrum & Quick Palette)
* [x] **Commit 8**: Dynamic 2D Calendar Foundation with Live Dates & Continuous (X, Y, Scale) Transforms
* [x] **Commit 9**: Multiple Artistic Calendar Styles (11 Distinct Aesthetic Variants)
* [x] **Commit 10**: Two-Tone Calendar sRGB Gamut Customizer
* [x] **Commit 11**: Movable & Resizable Test Clock Foundation
* [ ] **Commit 12**: Multiple Artistic Clock Styles (12 Distinct Aesthetic Variants)
* [ ] **Commit 13**: Clock Two-Tone sRGB Gamut Customizer
* [ ] **Commit 14**: In-App UI/UX Aesthetic Redesign
