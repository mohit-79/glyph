#!/data/data/com.termux/files/usr/bin/bash
set -e

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_DIR"

echo "=== Building Glyph APK ==="

# Paths and Directories
BUILD_DIR="$PROJECT_DIR/build"
GEN_DIR="$BUILD_DIR/gen"
OBJ_DIR="$BUILD_DIR/obj"
ANDROID_JAR="$PROJECT_DIR/lib/android.jar"
KEYSTORE="$PROJECT_DIR/debug.keystore"
DOWNLOADS_DIR="/data/data/com.termux/files/home/storage/downloads"

rm -rf "$BUILD_DIR"
mkdir -p "$GEN_DIR" "$OBJ_DIR"

# 1. Compile resources with aapt
echo "[1/5] Compiling resources with aapt..."
aapt package -f -m \
    -J "$GEN_DIR" \
    -M "$PROJECT_DIR/AndroidManifest.xml" \
    -S "$PROJECT_DIR/res" \
    -I "$ANDROID_JAR"

# 2. Compile Java sources
echo "[2/5] Compiling Java sources with javac..."
JAVA_FILES=$(find "$GEN_DIR" "$PROJECT_DIR/src" -name "*.java")
javac -source 1.8 -target 1.8 \
    -bootclasspath "$ANDROID_JAR" \
    -cp "$ANDROID_JAR" \
    -d "$OBJ_DIR" \
    $JAVA_FILES

# 3. Dex bytecode with d8
echo "[3/5] Converting bytecode to classes.dex with d8..."
CLASS_FILES=$(find "$OBJ_DIR" -name "*.class")
d8 --lib "$ANDROID_JAR" --output "$BUILD_DIR" $CLASS_FILES

# 4. Package APK with aapt
echo "[4/5] Packaging APK..."
aapt package -f \
    -M "$PROJECT_DIR/AndroidManifest.xml" \
    -S "$PROJECT_DIR/res" \
    -I "$ANDROID_JAR" \
    -F "$BUILD_DIR/glyph-unsigned.apk"

# Add classes.dex into APK
cd "$BUILD_DIR"
aapt add glyph-unsigned.apk classes.dex
cd "$PROJECT_DIR"

# 5. Sign APK with apksigner
echo "[5/5] Signing APK with apksigner..."
apksigner sign \
    --ks "$KEYSTORE" \
    --ks-pass pass:android \
    --key-pass pass:android \
    --ks-key-alias androiddebugkey \
    --out "$PROJECT_DIR/Glyph.apk" \
    "$BUILD_DIR/glyph-unsigned.apk"

# Verify signature
apksigner verify "$PROJECT_DIR/Glyph.apk"

# Copy to device Downloads directory
if [ -d "$DOWNLOADS_DIR" ]; then
    cp "$PROJECT_DIR/Glyph.apk" "$DOWNLOADS_DIR/Glyph.apk"
    echo "Copied APK to $DOWNLOADS_DIR/Glyph.apk"
fi

echo "=== Build Succeeded! ==="
ls -lh "$PROJECT_DIR/Glyph.apk"
