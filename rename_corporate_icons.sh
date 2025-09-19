#!/bin/bash

# Rename and copy Freepik icons to Corporate Theme naming scheme
# This script copies downloaded icons from downloads/freepik_icons/ to sample-app/src/main/assets/
# with the correct corporate naming convention

echo "🏢 Renaming Freepik icons to Corporate Theme naming scheme..."

# Check if downloads directory exists
if [ ! -d "downloads/freepik_icons" ]; then
    echo "❌ Error: downloads/freepik_icons/ directory not found!"
    echo "   Please run ./download_freepik_icons.sh first to download the icons"
    exit 1
fi

# Create assets directory if it doesn't exist
mkdir -p sample-app/src/main/assets/corporate

echo "📁 Copying and renaming icons..."

# Face scan icons
if [ -f "downloads/freepik_icons/face_overlay.png" ]; then
    cp downloads/freepik_icons/face_overlay.png sample-app/src/main/assets/corporate/corporate_face_overlay.png
    echo "✅ face_overlay.png → corporate_face_overlay.png"
else
    echo "⚠️  downloads/freepik_icons/face_overlay.png not found"
fi

if [ -f "downloads/freepik_icons/scan_face.png" ]; then
    cp downloads/freepik_icons/scan_face.png sample-app/src/main/assets/corporate/corporate_scan_face.png
    echo "✅ scan_face.png → corporate_scan_face.png"
else
    echo "⚠️  downloads/freepik_icons/scan_face.png not found"
fi

# Document icons
if [ -f "downloads/freepik_icons/passport_icon.png" ]; then
    cp downloads/freepik_icons/passport_icon.png sample-app/src/main/assets/corporate/corporate_passport_icon.png
    echo "✅ passport_icon.png → corporate_passport_icon.png"
else
    echo "⚠️  downloads/freepik_icons/passport_icon.png not found"
fi

if [ -f "downloads/freepik_icons/id_card_icon.png" ]; then
    cp downloads/freepik_icons/id_card_icon.png sample-app/src/main/assets/corporate/corporate_stateid_icon.png
    echo "✅ id_card_icon.png → corporate_stateid_icon.png"
else
    echo "⚠️  downloads/freepik_icons/id_card_icon.png not found"
fi

if [ -f "downloads/freepik_icons/document_scan.png" ]; then
    cp downloads/freepik_icons/document_scan.png sample-app/src/main/assets/corporate/corporate_doc_scan.png
    echo "✅ document_scan.png → corporate_doc_scan.png"
else
    echo "⚠️  downloads/freepik_icons/document_scan.png not found"
fi

# UI control icons
if [ -f "downloads/freepik_icons/back_arrow.png" ]; then
    cp downloads/freepik_icons/back_arrow.png sample-app/src/main/assets/corporate/corporate_back_button.png
    echo "✅ back_arrow.png → corporate_back_button.png"
else
    echo "⚠️  downloads/freepik_icons/back_arrow.png not found"
fi

if [ -f "downloads/freepik_icons/camera.png" ]; then
    cp downloads/freepik_icons/camera.png sample-app/src/main/assets/corporate/corporate_camera_button.png
    echo "✅ camera.png → corporate_camera_button.png"
else
    echo "⚠️  downloads/freepik_icons/camera.png not found"
fi

# Status icons
if [ -f "downloads/freepik_icons/success_checkmark.png" ]; then
    cp downloads/freepik_icons/success_checkmark.png sample-app/src/main/assets/corporate/corporate_success.png
    echo "✅ success_checkmark.png → corporate_success.png"
else
    echo "⚠️  downloads/freepik_icons/success_checkmark.png not found"
fi

if [ -f "downloads/freepik_icons/error_x.png" ]; then
    cp downloads/freepik_icons/error_x.png sample-app/src/main/assets/corporate/corporate_failed.png
    echo "✅ error_x.png → corporate_failed.png"
else
    echo "⚠️  downloads/freepik_icons/error_x.png not found"
fi

if [ -f "downloads/freepik_icons/warning.png" ]; then
    cp downloads/freepik_icons/warning.png sample-app/src/main/assets/corporate/corporate_error.png
    echo "✅ warning.png → corporate_error.png"
else
    echo "⚠️  downloads/freepik_icons/warning.png not found"
fi

# Brand assets
if [ -f "downloads/freepik_icons/company_logo.png" ]; then
    cp downloads/freepik_icons/company_logo.png sample-app/src/main/assets/corporate/corporate_logo.png
    echo "✅ company_logo.png → corporate_logo.png"
else
    echo "⚠️  downloads/freepik_icons/company_logo.png not found"
fi

if [ -f "downloads/freepik_icons/brand_identity.png" ]; then
    cp downloads/freepik_icons/brand_identity.png sample-app/src/main/assets/corporate/corporate_brand_image.png
    echo "✅ brand_identity.png → corporate_brand_image.png"
else
    echo "⚠️  downloads/freepik_icons/brand_identity.png not found"
fi

# Document overlays
if [ -f "downloads/freepik_icons/passport_frame.png" ]; then
    cp downloads/freepik_icons/passport_frame.png sample-app/src/main/assets/corporate/corporate_passport_overlay.png
    echo "✅ passport_frame.png → corporate_passport_overlay.png"
else
    echo "⚠️  downloads/freepik_icons/passport_frame.png not found"
fi

if [ -f "downloads/freepik_icons/id_card_front_frame.png" ]; then
    cp downloads/freepik_icons/id_card_front_frame.png sample-app/src/main/assets/corporate/corporate_stateid_front_overlay.png
    echo "✅ id_card_front_frame.png → corporate_stateid_front_overlay.png"
else
    echo "⚠️  downloads/freepik_icons/id_card_front_frame.png not found"
fi

if [ -f "downloads/freepik_icons/id_card_back_frame.png" ]; then
    cp downloads/freepik_icons/id_card_back_frame.png sample-app/src/main/assets/corporate/corporate_stateid_back_overlay.png
    echo "✅ id_card_back_frame.png → corporate_stateid_back_overlay.png"
else
    echo "⚠️  downloads/freepik_icons/id_card_back_frame.png not found"
fi

echo ""
echo "✅ Icon renaming complete!"
echo ""


echo "📊 Summary of corporate assets:"
ls -la sample-app/src/main/assets/corporate/corporate_*.png | wc -l | xargs echo "   PNG files:"
ls -la sample-app/src/main/assets/corporate/corporate_*.gif | wc -l | xargs echo "   GIF files:"
echo "   Location: sample-app/src/main/assets/corporate/ (host app assets only)"
echo ""
echo "📝 Next steps:"
echo "1. Build and install the app: ./gradlew :sample-app:installDebug"
echo "2. Select 'Corporate Theme' in the Image Override dropdown"
echo "3. Test the verification flow to see the new icons"
echo ""
echo "🎨 Corporate Theme is ready for testing!"
