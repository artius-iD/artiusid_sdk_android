#!/bin/bash

# Exit immediately if a command exits with a non-zero status.
set -e

echo "🔨 Building ArtiusID & OpenSSL XCFrameworks"
echo "============================================"

# Prompt for build type if not provided as an argument
BUILD_TYPE=${1:-}
if [ -z "$BUILD_TYPE" ]; then
    read -p "🛠️  Build type? (release/debug) [release]: " BUILD_TYPE
    BUILD_TYPE=${BUILD_TYPE:-release}
fi

if [[ "$BUILD_TYPE" != "release" && "$BUILD_TYPE" != "debug" ]]; then
    echo "❌ Invalid build type. Must be 'release' or 'debug'."
    exit 1
fi

if [ "$BUILD_TYPE" == "debug" ]; then
    CONFIGURATION="Debug"
else
    CONFIGURATION="Release"
fi

echo "✅ Selected build type: $CONFIGURATION"

# --- Configuration ---
FRAMEWORK_NAME="artiusid_sdk_ios"
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$SCRIPT_DIR/.."
PROJECT_NAME="$PROJECT_ROOT/artiusid-sdk-ios.xcodeproj"
WORKSPACE_NAME="$PROJECT_ROOT/artiusid-sdk-ios.xcworkspace"
SCHEME_NAME="artiusid-sdk-ios"
BUILD_DIR="$PROJECT_ROOT/build"
cd "$PROJECT_ROOT"  # Change to project root directory

# --- Clean Up ---
echo "🧹 Cleaning previous builds..."
rm -rf "${FRAMEWORK_NAME}.xcframework" "${FRAMEWORK_NAME}.xcframework.zip" "OpenSSL.xcframework" "OpenSSL.xcframework.zip" "$BUILD_DIR"
mkdir -p "$BUILD_DIR"
echo "[OK] Build directory created: $BUILD_DIR"

# --- Determine Project/Workspace ---
if [ -d "$WORKSPACE_NAME" ]; then
    echo "✅ Using workspace: $WORKSPACE_NAME"
    PROJECT_ARG="-workspace $WORKSPACE_NAME"
    PROJECT_DISPLAY=$(basename "$WORKSPACE_NAME")
else
    echo "⚠️ Workspace not found, using project: $(basename "$PROJECT_NAME")"
    PROJECT_ARG="-project $PROJECT_NAME"
    PROJECT_DISPLAY=$(basename "$PROJECT_NAME")
fi

# --- Resolve Package Dependencies ---
echo "📦 Resolving package dependencies..."
xcodebuild -resolvePackageDependencies $PROJECT_ARG -scheme "$SCHEME_NAME"

# --- Common Build Flags ---
# These flags ensure the framework is built for distribution with a stable module interface
COMMON_FLAGS="SKIP_INSTALL=NO BUILD_LIBRARY_FOR_DISTRIBUTION=YES ENABLE_LIBRARY_EVOLUTION=YES SWIFT_EMIT_MODULE_INTERFACE=YES CODE_SIGN_IDENTITY= CODE_SIGNING_REQUIRED=NO CODE_SIGNING_ALLOWED=NO"

# --- Archive for iOS Device ---
echo "📦 Archiving for iOS Device (arm64)..."
DEVICE_ARCHIVE_PATH="$BUILD_DIR/DerivedData-Device"
xcodebuild archive \
    $PROJECT_ARG \
    -scheme "$SCHEME_NAME" \
    -configuration "$CONFIGURATION" \
    -destination "generic/platform=iOS" \
    -archivePath "$BUILD_DIR/Device.xcarchive" \
    -derivedDataPath "$DEVICE_ARCHIVE_PATH" \
    $COMMON_FLAGS

echo "✅ iOS Device build completed"

# --- Archive for iOS Simulator ---
echo "📦 Archiving for iOS Simulator (x86_64, arm64)..."
SIMULATOR_ARCHIVE_PATH="$BUILD_DIR/DerivedData-Simulator"
xcodebuild archive \
    $PROJECT_ARG \
    -scheme "$SCHEME_NAME" \
    -configuration "$CONFIGURATION" \
    -destination "generic/platform=iOS Simulator" \
    -archivePath "$BUILD_DIR/Simulator.xcarchive" \
    -derivedDataPath "$SIMULATOR_ARCHIVE_PATH" \
    $COMMON_FLAGS

echo "✅ iOS Simulator build completed"

# --- Create ArtiusID XCFramework ---
echo "📦 Creating ${FRAMEWORK_NAME}.xcframework..."
xcodebuild -create-xcframework \
    -framework "$BUILD_DIR/Device.xcarchive/Products/Library/Frameworks/${FRAMEWORK_NAME}.framework" \
    -framework "$BUILD_DIR/Simulator.xcarchive/Products/Library/Frameworks/${FRAMEWORK_NAME}.framework" \
    -output "${FRAMEWORK_NAME}.xcframework"

echo "✅ ${FRAMEWORK_NAME}.xcframework created successfully"

# --- Create OpenSSL XCFramework ---
echo "📦 Creating OpenSSL.xcframework..."

IOS_OPENSSL=$(find "$DEVICE_ARCHIVE_PATH" -path "*/SourcePackages/checkouts/OpenSSL/Frameworks/iphoneos/OpenSSL.framework" -type d | head -1)
SIM_OPENSSL=$(find "$SIMULATOR_ARCHIVE_PATH" -path "*/SourcePackages/checkouts/OpenSSL/Frameworks/iphonesimulator/OpenSSL.framework" -type d | head -1)

if [ -z "$IOS_OPENSSL" ] || [ -z "$SIM_OPENSSL" ]; then
    echo "❌ Error: Could not find OpenSSL.framework in DerivedData. Ensure it's a dependency in your Xcode project."
    exit 1
fi

echo "🎯 Found iOS OpenSSL: $IOS_OPENSSL"
echo "🎯 Found Simulator OpenSSL: $SIM_OPENSSL"

xcodebuild -create-xcframework \
    -framework "$IOS_OPENSSL" \
    -framework "$SIM_OPENSSL" \
    -output "OpenSSL.xcframework"

echo "✅ OpenSSL.xcframework created successfully"

# --- Create ZIP Archives & Checksums ---
echo "📦 Creating ZIP archives and calculating checksums..."
zip -r "${FRAMEWORK_NAME}.xcframework.zip" "${FRAMEWORK_NAME}.xcframework" >/dev/null
zip -r "OpenSSL.xcframework.zip" "OpenSSL.xcframework" >/dev/null

ARTIUS_SIZE=$(du -h "${FRAMEWORK_NAME}.xcframework.zip" | cut -f1)
ARTIUS_CHECKSUM=$(shasum -a 256 "${FRAMEWORK_NAME}.xcframework.zip" | awk '{print $1}')
OPENSSL_SIZE=$(du -h "OpenSSL.xcframework.zip" | cut -f1)
OPENSSL_CHECKSUM=$(shasum -a 256 "OpenSSL.xcframework.zip" | awk '{print $1}')

# --- Move artifacts to build directory ---
mv "${FRAMEWORK_NAME}.xcframework.zip" "$BUILD_DIR/"
mv "OpenSSL.xcframework.zip" "$BUILD_DIR/"

echo ""
echo "🎉 Build completed successfully!"
echo ""
echo "--- ArtiusID SDK ---"
echo "📦 Framework: $BUILD_DIR/${FRAMEWORK_NAME}.xcframework.zip"
echo "📊 Size: $ARTIUS_SIZE"
echo "🔐 Checksum: $ARTIUS_CHECKSUM"
echo ""
echo "--- OpenSSL Dependency ---"
echo "📦 Framework: $BUILD_DIR/OpenSSL.xcframework.zip"
echo "📊 Size: $OPENSSL_SIZE"
echo "🔐 Checksum: $OPENSSL_CHECKSUM"
echo ""
echo "🚀 Run ./publish-github.sh to publish to GitHub"