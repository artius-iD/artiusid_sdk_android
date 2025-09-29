#!/bin/bash

# ArtiusID SDK Publish-Only Script
# Publishes an existing XCFramework without building
# Use this after building the framework manually in Xcode

set -e

echo "[PUBLISH] ArtiusID SDK Publisher (Build-Free)"
echo "[TIME] Started at: $(date)"
echo ""

SDK_VERSION=${SDK_VERSION:-"1.0.50"}
BUILD_DIR="build"
FRAMEWORK_NAME="ArtiusIDiOSSDK.xcframework"
ZIP_NAME="ArtiusIDiOSSDK.xcframework.zip"

# Check if framework exists
if [ ! -d "$BUILD_DIR/$FRAMEWORK_NAME" ]; then
    echo "[ERROR] Framework not found: $BUILD_DIR/$FRAMEWORK_NAME"
    echo "[INFO] Please build the framework first using one of these methods:"
    echo "  1. Build in Xcode: Product → Archive"
    echo "  2. Copy existing framework to $BUILD_DIR/"
    echo "  3. Run build-framework.sh successfully"
    exit 1
fi

echo "[FOUND] Using existing framework: $BUILD_DIR/$FRAMEWORK_NAME"

# Create ZIP
echo "[ZIP] Creating ZIP for distribution..."
cd "$BUILD_DIR"
zip -r "$ZIP_NAME" "$FRAMEWORK_NAME" > /dev/null 2>&1
cd ..
echo "[OK] ZIP file created: $BUILD_DIR/$ZIP_NAME"

# Compute checksum
echo "[HASH] Computing checksum..."
CHECKSUM=$(swift package compute-checksum "$BUILD_DIR/$ZIP_NAME")
echo "[OK] Checksum computed: $CHECKSUM"

# Calculate file sizes
FRAMEWORK_SIZE=$(du -h "$BUILD_DIR/$FRAMEWORK_NAME" | cut -f1)
ZIP_SIZE=$(du -h "$BUILD_DIR/$ZIP_NAME" | cut -f1)

echo ""
echo "[DONE] PUBLISH PREPARATION COMPLETED!"
echo "[TIME] Finished at: $(date)"
echo ""
echo "[OK] Framework ready for distribution!"
echo "[LOC] Framework: ./build/$FRAMEWORK_NAME ($FRAMEWORK_SIZE)"
echo "[PKG] ZIP: ./build/$ZIP_NAME ($ZIP_SIZE)"
echo "[HASH] Checksum: $CHECKSUM"
echo ""
echo "[NOTE] Copy this for Package.swift:"
echo ".binaryTarget("
echo "    name: \"ArtiusIDiOSSDK\","
echo "    url: \"https://github.com/artiusID/sdk/releases/download/v$SDK_VERSION/$ZIP_NAME\","
echo "    checksum: \"$CHECKSUM\""
echo ")"
echo ""
echo "[NEXT] Run publish-github.sh to upload to GitHub"
