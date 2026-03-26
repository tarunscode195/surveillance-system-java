#!/bin/bash
# ============================================================
#  build_and_run.sh  –  Linux / macOS Build & Run Script
# ============================================================

echo "============================================="
echo "  Smart Surveillance System – Build Script  "
echo "============================================="
echo ""

# ── STEP 1: Configure your OpenCV path here ──────────────────
# Change this path to wherever OpenCV is installed on your system.
OPENCV_VERSION="4100"
OPENCV_DIR="/usr/local/share/java/opencv4"  # typical Linux path

# Common alternative paths (uncomment the one that matches your setup):
# OPENCV_DIR="$HOME/opencv/build/bin"     # built from source
# OPENCV_DIR="/opt/opencv/build/java"     # custom install

OPENCV_JAR="${OPENCV_DIR}/opencv-${OPENCV_VERSION}.jar"

# On Linux, native library is .so; on macOS it's .dylib
if [[ "$OSTYPE" == "darwin"* ]]; then
    NATIVE_LIB_EXT=".dylib"
else
    NATIVE_LIB_EXT=".so"
fi
OPENCV_NATIVE_DIR="${OPENCV_DIR}"

echo "[1] Checking OpenCV installation..."
if [ ! -f "$OPENCV_JAR" ]; then
    echo ""
    echo "ERROR: OpenCV JAR not found at: $OPENCV_JAR"
    echo ""
    echo "To install OpenCV on Ubuntu/Debian:"
    echo "  sudo apt update"
    echo "  sudo apt install libopencv-dev"
    echo "  # Then find jar: find / -name 'opencv*.jar' 2>/dev/null"
    echo ""
    echo "Or build from source:"
    echo "  https://docs.opencv.org/4.x/d7/d9f/tutorial_linux_install.html"
    exit 1
fi
echo "    Found: $OPENCV_JAR"
echo ""

# ── STEP 2: Create output and capture folders ─────────────────
mkdir -p out
mkdir -p captured_images
mkdir -p logs

# ── STEP 3: Compile all Java source files ─────────────────────
echo "[2] Compiling Java source files..."
javac -cp "$OPENCV_JAR" -d out src/*.java

if [ $? -ne 0 ]; then
    echo ""
    echo "ERROR: Compilation failed! Check the error messages above."
    exit 1
fi
echo "    Compilation successful!"
echo ""

# ── STEP 4: Run the application ───────────────────────────────
echo "[3] Starting Smart Surveillance System..."
echo ""
java -cp "out:$OPENCV_JAR" \
     -Djava.library.path="$OPENCV_NATIVE_DIR" \
     Main
