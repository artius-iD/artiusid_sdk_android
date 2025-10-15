#!/bin/bash

# ArtiusID Android SDK Repository Cleanup Script
# Removes all redundant, unused, and temporary files to create a clean SDK repository

set -e

echo "🧹 ArtiusID Android SDK Repository Cleanup"
echo "=========================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_status() {
    echo -e "${GREEN}✅${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}⚠️${NC} $1"
}

print_error() {
    echo -e "${RED}❌${NC} $1"
}

print_info() {
    echo -e "${BLUE}ℹ️${NC} $1"
}

# Check if we're in the correct directory
if [ ! -f "artiusid-sdk/build.gradle" ] || [ ! -f "settings.gradle" ]; then
    print_error "Invalid Android project structure. Please run from project root."
    exit 1
fi

print_info "Starting cleanup of redundant and unused files..."

# 1. Remove internal documentation files (root level)
# NOTE: HILT_INTEGRATION_GUIDE.md, README_HILT_SETUP.md, hilt_diagnostic_script.gradle, and setup_hilt.sh are PRESERVED for customers
print_info "🗑️  Removing internal documentation files..."
declare -a DOC_FILES=(
    "DEPLOYMENT_COMPARISON.md"
    "DEPLOYMENT_GUIDE.md"
    "Image_Override_Punch_List.md"
    "Image_Override_System_Documentation.md"
    "SDK_DISTRIBUTION_SECURITY.md"
    "SDK_Icon_Theming_Documentation.txt"
    "SDK_SECURITY_GUIDE.md"
    "README.md"
)

for file in "${DOC_FILES[@]}"; do
    if [ -f "$file" ]; then
        echo "  Removing: $file"
        rm -f "$file"
    fi
done

# 2. Remove temporary and generated files
print_info "🗑️  Removing temporary and generated files..."
declare -a TEMP_FILES=(
    "clear-combo-5-394488.mp3"
    "create-customer-sample-distribution.sh"
    "ArtiusID-SDK-Customer-Sample-*.tar.gz"
)

for pattern in "${TEMP_FILES[@]}"; do
    for file in $pattern; do
        if [ -f "$file" ]; then
            echo "  Removing: $file"
            rm -f "$file"
        fi
    done
done

# 3. Remove temporary directories
print_info "🗑️  Removing temporary directories..."
declare -a TEMP_DIRS=(
    "customer-sample-app"
    "customer-sample-distribution"
    "customer-test-app"
)

for dir in "${TEMP_DIRS[@]}"; do
    if [ -d "$dir" ]; then
        echo "  Removing directory: $dir"
        rm -rf "$dir"
    fi
done

# 4. Remove external project (trinet-android-app)
print_info "🗑️  Removing external project..."
if [ -d "~/Documents/trinet-android-app" ]; then
    echo "  Removing: ~/Documents/trinet-android-app"
    rm -rf "~/Documents/trinet-android-app"
fi

# 5. Clean up old/redundant scripts
print_info "🗑️  Removing old and cleanup scripts..."
declare -a OLD_SCRIPTS=(
    "artiusid-sdk/scripts/cleanup-github-repo.sh"
    "artiusid-sdk/scripts/cleanup-github-repo-final.sh"
    "artiusid-sdk/scripts/cleanup-sample-source.sh"
    "artiusid-sdk/scripts/emergency-source-cleanup.sh"
    "artiusid-sdk/scripts/publish-android-github-improved.sh"
    "artiusid-sdk/scripts/publish-android-github-minimal.sh"
)

for script in "${OLD_SCRIPTS[@]}"; do
    if [ -f "$script" ]; then
        echo "  Removing: $script"
        rm -f "$script"
    fi
done

# 6. Remove build artifacts and cache directories
print_info "🗑️  Removing build artifacts and cache..."
declare -a BUILD_DIRS=(
    ".gradle"
    "build"
    "artiusid-sdk/build"
    "sample-app/build"
)

for dir in "${BUILD_DIRS[@]}"; do
    if [ -d "$dir" ]; then
        echo "  Removing build directory: $dir"
        rm -rf "$dir"
    fi
done

# 7. Remove local configuration files
print_info "🗑️  Removing local configuration files..."
declare -a LOCAL_FILES=(
    "local.properties"
    "gradle.properties.bak"
)

for file in "${LOCAL_FILES[@]}"; do
    if [ -f "$file" ]; then
        echo "  Removing: $file"
        rm -f "$file"
    fi
done

# 8. Remove sample-app temporary files
print_info "🗑️  Cleaning up sample-app temporary files..."
declare -a SAMPLE_TEMP_FILES=(
    "sample-app/create-customer-distribution.sh"
    "sample-app/dictionary.txt"
    "sample-app/proguard-rules-customer.pro"
    "sample-app/proguard-rules-obfuscated.pro"
)

for file in "${SAMPLE_TEMP_FILES[@]}"; do
    if [ -f "$file" ]; then
        echo "  Removing: $file"
        rm -f "$file"
    fi
done

# 9. Clean up any remaining .bak files
print_info "🗑️  Removing backup files..."
find . -name "*.bak" -type f -delete 2>/dev/null || true
find . -name "*.tmp" -type f -delete 2>/dev/null || true

# 10. Remove empty directories
print_info "🗑️  Removing empty directories..."
find . -type d -empty -delete 2>/dev/null || true

print_status "Cleanup completed successfully!"

echo ""
print_info "📋 Remaining essential files:"
echo ""
echo "📁 Core SDK Module:"
echo "   ✅ artiusid-sdk/ - Complete SDK implementation"
echo "   ✅ sample-app/ - Sample application for testing"
echo ""
echo "📁 Project Configuration:"
echo "   ✅ build.gradle - Root build configuration"
echo "   ✅ settings.gradle - Project structure"
echo "   ✅ gradle.properties - Build properties"
echo "   ✅ gradle/ - Gradle wrapper"
echo ""
echo "📁 Scripts & Tools (Essential Only):"
echo "   ✅ artiusid-sdk/scripts/version-manager.sh"
echo "   ✅ artiusid-sdk/scripts/publish-android-github-essential.sh"
echo "   ✅ setup_hilt.sh - Automated HILT configuration"
echo "   ✅ hilt_diagnostic_script.gradle - HILT troubleshooting"
echo ""
echo "📁 Customer Documentation:"
echo "   ✅ HILT_INTEGRATION_GUIDE.md - Complete HILT setup guide"
echo "   ✅ README_HILT_SETUP.md - Quick HILT reference"
echo ""

# Count remaining files
REMAINING_FILES=$(find . -type f | grep -v "/.git/" | wc -l | tr -d ' ')
print_status "Repository now contains $REMAINING_FILES essential files"

echo ""
print_info "🎯 Next steps:"
echo "   1. Review remaining files"
echo "   2. Test SDK build: ./gradlew :artiusid-sdk:assembleRelease"
echo "   3. Test sample app: ./gradlew :sample-app:assembleDebug"
echo "   4. Commit cleaned repository"

echo ""
print_status "🎉 SDK repository cleanup complete!"
