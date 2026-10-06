# Glyph: Artistic Home Screen Widgets

Glyph is an Android home screen widget app engineered from the ground up with a pure 2D Canvas Compositor engine. Instead of relying on rigid, clunky XML View hierarchies that suffer from multi-line text wrapping or clipping during resize, Glyph renders complete widgets (background pills, borders, calendar grids, and clock digits) onto high-resolution 2D bitmaps and transfers them directly to the launcher via RemoteViews.
<!-- <img width="1080" height="2400" alt="1791315176167" src="https://github.com/user-attachments/assets/0ab07a93-ed46-4969-b7f8-98c355c36016" /> -->
<img width="1080" height="2400" alt="1791315176199" src="https://github.com/user-attachments/assets/8552827b-4511-4a65-ba0e-e48642f67c0f" />
<img width="1080" height="2400" alt="1791315176190" src="https://github.com/user-attachments/assets/66683fd8-72bc-4083-be7e-5b3ae8eb3cbf" />
<img width="1080" height="2400" alt="1791315176181" src="https://github.com/user-attachments/assets/d6cdef23-dd05-4934-9889-be3c2ea09e7e" />
<img width="1080" height="2400" alt="1791315176175" src="https://github.com/user-attachments/assets/47bf0d4c-fd7a-4146-881e-6c7bf54574ed" />
<img width="1080" height="2400" alt="1791315176167" src="https://github.com/user-attachments/assets/38122570-8724-4c36-b554-9dda145b1cf0" />
<img width="1080" height="2400" alt="1791315176160" src="https://github.com/user-attachments/assets/c7e08d79-7408-41a2-9218-d78f3d1a2be1" />
<img width="1080" height="2400" alt="1791315176153" src="https://github.com/user-attachments/assets/b6a30952-f70f-4570-8cae-8336b196df52" />
<img width="1080" height="2400" alt="1791315176147" src="https://github.com/user-attachments/assets/4c45dfd9-9c1d-432e-ab97-c090ae4cee99" />
<img width="1080" height="2400" alt="1791315176141" src="https://github.com/user-attachments/assets/c09c0ef2-6e74-4ed5-9be6-1af1522020b3" />
<img width="1080" height="2400" alt="1791315176133" src="https://github.com/user-attachments/assets/b1241c03-08a6-4b0a-82fa-a416cea1528c" />
<img width="1080" height="2400" alt="1791315176127" src="https://github.com/user-attachments/assets/f01a6d13-358f-41fb-af05-c4cca01a2085" />
<img width="1080" height="2400" alt="1791315176120" src="https://github.com/user-attachments/assets/75b86264-b495-40ca-b5c3-6a285bb74b62" />
<img width="1080" height="2400" alt="1791315176113" src="https://github.com/user-attachments/assets/b4286d3f-b514-42af-ad0b-67145da98e55" />
<img width="1080" height="2400" alt="1791315176101" src="https://github.com/user-attachments/assets/67e3557f-ee92-429b-9a89-a5cca24c077e" />



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
├── assets/                              # Packaged application assets
│   └── fonts/                           # Typographic font families
│       └── ndot55.otf                   # Authentic Nothing OS dot-matrix typeface
├── res/                                 # Packaged application resources
│   ├── drawable/                        # Vector assets and background card drawables
│   │   ├── bg_card.xml                  # Rounded card container style
│   │   ├── bg_badge.xml                 # Rounded status badge style
│   │   ├── btn_icon_bg.xml              # Circular button background style
│   │   ├── ic_coffee.xml                # Buy me a coffee vector icon
│   │   ├── teddy_cartoon.png            # Authentic Mr. Bean Animated Series Teddy asset
│   │   └── widget_preview.xml           # Launcher widget preview asset
│   ├── layout/                          # UI layout hierarchies
│   │   ├── activity_main.xml            # Nothing OS styled multi-widget hub & coffee modal
│   │   ├── activity_widget_config.xml   # Categorized 5-section dropdown customization studio
│   │   ├── dialog_coffee.xml            # Buy me a coffee modal dialog (UPI, Contact, GitHub)
│   │   └── widget_clock_calendar.xml    # RemoteViews container for launcher instances
│   ├── mipmap-*/                        # High-resolution launcher icons across all screen densities
│   │   └── ic_launcher.png              # Mr. Bean Teddy launcher icon (mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi)
│   ├── values/                          # Scalar values
│   │   ├── colors.xml                   # Semantic color definitions
│   │   ├── strings.xml                  # Application strings and labels
│   │   └── styles.xml                   # Seamless edge-to-edge dark theme with status bar coloring
│   └── xml/                             # System metadata descriptors
│       └── glyph_clock_calendar_widget_info.xml # AppWidgetProviderInfo configuration
└── src/                                 # Java source code
    └── com/glyph/widget/
        ├── MainActivity.java            # Multi-widget hub launching isolated customization studios
        ├── WidgetConfigActivity.java    # Interactive customizer with live canvas preview & accordion categories
        ├── GlyphWidgetProvider.java     # BroadcastReceiver managing widget lifecycle and updates
        ├── GlyphPrefs.java              # Namespaced SharedPreferences state persistence engine
        ├── GlyphTheme.java              # Catalog of 27 curated color palettes and theme matrices
        └── compositor/                  # Pure 2D Canvas rendering engine
            ├── WidgetCanvas.java        # High-DPI bitmap compositor and pill geometry engine
            ├── CalendarRenderer.java    # Dynamic live-date calendar with 2D transform matrix
            ├── ClockRenderer.java       # Single-Line live digital clock with 2D transform matrix
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
* **12 Distinct Artistic Clock Styles**:
  1. **Bold Capsule Sans**: Chunky rounded digital font with superscript AM/PM pill capsule badge.
  2. **Retro 7-Segment LED**: Authentic electronic LED display with lit segments and faint unlit ghost framing.
  3. **Mechanical Split-Flap**: Split flip-board cards with center dividing seam and tactile card borders.
  4. **Stacked 2x2 Gradient Pill**: Vertical stacked layout with hours above minutes and bracket corner framing.
  5. **Dot Matrix LED**: Fine-pitch 5x7 dot matrix array with unlit ghost LEDs and circular dot diodes.
  6. **Superscript Seconds & Date**: Crisp architectural typography with live running seconds and date pill tag.
  7. **Bold Athletic Block**: Heavy faceted athletic stencil block numerals with chamfered geometry.
  8. **Whimsical Cartoon Bubble**: Playful bulbous typography with inner bubble reflection highlight crescents.
  9. **Modular Mosaic Block**: Minimalist Nothing OS inspired block mosaic constructed from geometric tiles.
  10. **Sci-Fi Stencil Squircle**: Cyberpunk HUD telemetry display with technical squircle frames and scanline seam.
  11. **Ultra-Condensed Tall Deco**: Ultra-tall slender Bauhaus Art Deco numerals with stacked minute column.
  12. **Analog Dial Hybrid**: Asymmetric hybrid featuring an authentic circular analog dial beside digital time.
* **In-App Style Studio**: Interactive spinner, style counter (e.g. `1 / 12`), previous/next quick cycling buttons, and live style descriptions.
* **Crash Resilience & Defensive Guards**: Safe cached Typeface resolution avoiding null font metrics across custom ROMs, wrapped canvas drawing routines, and guarded widget update broadcasts ensuring the customizer never crashes or restarts during slider manipulation.

### 6. Two-Tone Calendar sRGB Gamut Customizer
Provides independent full-spectrum color customization for the two functional calendar tones:
* **Tone 1 (Primary Accent)**: Month/Year header, active day badge, capsule pill bounds, and progress gauge arcs.
* **Tone 2 (Secondary Tone)**: Day-of-week headers, secondary date numerals, grid dividers, and dial tracks.
* **Dual-Tab UI**: Tabbed selector with live circular swatches, dynamic hex codes, and "Theme Default" vs "Custom Override" indicators.
* **Full sRGB Gamut**: Rainbow Hue spectrum bar (0 to 360 degrees), Saturation slider (0% to 100%), and Brightness/Value slider (0% to 100%) for exact hue tuning.
* **8-Color Quick Palette**: Fast 1-tap presets for White, Slate, Charcoal, Red, Amber, Emerald, Cyan, and Violet.
* **Selective Resets**: "Reset Active Tone" button and "Reset Both Tones" button to independently restore active theme defaults.

### 7. Two-Tone Clock sRGB Gamut Customizer
Provides independent full-spectrum color customization for the two functional clock tones:
* **Tone 1 (Primary Digits)**: Primary hour and minute digits across all 12 artistic clock styles.
* **Tone 2 (Secondary / Accent)**: Colon separator, superscript AM/PM pill capsule badge, live seconds indicators, date pills, dial ticks, and border accents.
* **Dual-Tab UI**: Tabbed selector (`Tone 1` vs `Tone 2`) with live circular preview swatches, dynamic hex codes, and real-time "Theme Default" vs "Custom Override" indicators.
* **Full sRGB Gamut**: Rainbow Hue spectrum bar (0 to 360 degrees), Saturation slider (0% to 100%), and Brightness/Value slider (0% to 100%) providing all 16.7 million sRGB colors.
* **8-Color Quick Palette**: Fast 1-tap presets for White, Slate, Charcoal, Red, Amber, Emerald, Cyan, and Violet.
* **Selective Resets**: "Reset Active Tone" button and "Reset Both Tones" button to independently restore active theme defaults without affecting positions or scales.

### 8. Border Studio & Full sRGB Color Gamut Picker
* **Thickness Control**: Continuous slider from `0dp` (completely borderless) to `16dp` (ultra thick).
* **Full sRGB Gamut**: Rainbow Hue spectrum bar (0 to 360 degrees), Saturation slider (0% to 100%), and Brightness/Value slider (0% to 100%) providing access to all 16.7 million colors.
* **8-Color Quick Palette**: Quick swatches for White, Slate, Charcoal, Red, Amber, Emerald, Cyan, and Violet.
* **Theme Default Reset**: Instant reset button reverting to the active theme's default border palette.

### 9. Widget Tap Gestures & Click Action Studio
Provides independent touch gesture behaviors for the Clock (left half) and Calendar (right half) regions:
* **Dual-Zone Touch Overlay**: Horizontal touch partitioning splitting the widget surface into dedicated Clock and Calendar touch zones with fallback background handling.
* **6 Configurable Behaviors for Clock & Calendar**:
  1. **Open Default App**: Launches the system Clock / Alarm app (for Clock) or system Calendar (for Calendar) with manufacturer fallback resolution across Google, Samsung, Xiaomi MIUI/HyperOS, OnePlus, and Oppo.
  2. **Open Glyph Studio**: Instantly opens the widget configurator studio.
  3. **Open Any Installed App**: Allows selecting any launchable app installed on the device via an alphabetical in-studio app chooser dialog.
  4. **Cycle Through Themes**: 1-tap live theme cycler stepping through all 27 themes with instant canvas recomposition on the home screen.
  5. **Cycle Style (Dynamic Animation)**: 1-tap dynamic style switcher cycling through all 12 artistic clock styles or 11 calendar styles directly on the widget.
  6. **Inert (Do Nothing)**: Intercepts touch events and performs no action, completely inert.

### 10. Curated Theme Catalog (`GlyphTheme.java`)
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

### 11. Mr. Bean Teddy Logo, Dropdown Accordion Studio, Developer Support & Status Bar Theming
* **Authentic Mr. Bean Cartoon Teddy Logo**: High-fidelity launcher icon across all screen densities (mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi) featuring the iconic Teddy bear from the Mr. Bean Animated Series, rendered on launcher home screens and within the app header.
* **Categorized Dropdown Accordion UI**: Organizes the extensive customizer studio into 5 clean, collapsible category cards with dynamic "OPEN" / "EXPAND" indicator badges:
  1. Themes & Glass Engine (27 presets, optical blur, opacity, specular highlight)
  2. Clock Studio & Gamut (Position, scale, 12h/24h toggle, two-tone hour & minute sRGB gamut)
  3. Calendar Studio & Gamut (Position, scale, 11 artistic styles, two-tone day & month sRGB gamut)
  4. Frame, Margins & Borders (Border thickness, sRGB gamut, 4-side margins, corner radius)
  5. Widget Tap Gestures (Clock & calendar touch behaviors: apps, themes, animations, inert)
* **Nothing OS Dot-Matrix Typography**: The entry page header prominently displays "GLYPH" in the authentic Nothing OS dot-matrix typeface (`ndot55.otf`) loaded dynamically from application assets.
* **Buy Me a Coffee & Developer Support Modal**: Dedicated coffee cup action on the top right of the entry page opening an interactive modal supporting:
  - UPI ID (`mohitharjani79@oksbi`) with 1-tap clipboard copy and direct UPI app payment intent.
  - Developer contact (`mohitharjani79@gmail.com`) with 1-tap email launch and clipboard copy.
  - Open source repository (`https://github.com/mohit-79/glyph`) with direct browser launch to star on GitHub.
* **Seamless Status & System Bar Theming**: Programmatic window flag configuration (`FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS`) and dark status/navigation bar coloring (`#0D0F14`) eliminating color mismatches between system notification bars and the application surface across all Android versions and OEM skins.

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
* [x] **Commit 12**: Multiple Artistic Clock Styles (12 Distinct Aesthetic Variants)
* [x] **Commit 13**: Clock Two-Tone sRGB Gamut Customizer
* [x] **Commit 15**: Widget Click Actions for Clock & Calendar (Inert, Open App, Cycle Themes, Style Animation)
* [x] **Commit 16**: Mr. Bean Cartoon Teddy Logo, Categorized Dropdown Settings UI, Coffee Support, Nothing OS Typography & Status Bar Theming
