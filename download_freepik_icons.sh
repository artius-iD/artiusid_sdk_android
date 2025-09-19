#!/bin/bash

# Download Freepik Special Lineal Icons for Corporate Theme
# Run this script to download icons from Freepik Special Lineal collection
# https://www.freepik.com/author/freepik/icons/special-lineal_7

echo "🎨 Downloading Freepik Special Lineal Icons for Corporate Theme..."

# Create assets directory if it doesn't exist
mkdir -p sample-app/src/main/assets

# Create downloads directory for temporary storage
mkdir -p downloads/freepik_icons

echo "📥 Step 1: Download icons to downloads/freepik_icons/ directory"
echo "   Replace the example URLs below with actual Freepik download links"
echo ""

# Icon URLs from Freepik Special Lineal collection (you'll need to replace these with actual URLs)
# These are example URLs - you'll need to get the actual download links from Freepik

# Face scan icons
echo "Downloading face scan icons..."
curl -L -o downloads/freepik_icons/face_overlay.png "https://example.freepik.com/face-scan-icon.png" || echo "⚠️  Replace with actual face overlay icon URL"
curl -L -o downloads/freepik_icons/scan_face.png "https://example.freepik.com/face-recognition-icon.png" || echo "⚠️  Replace with actual face recognition icon URL"

# Document icons
echo "Downloading document icons..."
curl -L -o downloads/freepik_icons/passport_icon.png "https://example.freepik.com/passport-icon.png" || echo "⚠️  Replace with actual passport icon URL"
curl -L -o downloads/freepik_icons/id_card_icon.png "https://example.freepik.com/id-card-icon.png" || echo "⚠️  Replace with actual ID card icon URL"
curl -L -o downloads/freepik_icons/document_scan.png "https://example.freepik.com/document-scan-icon.png" || echo "⚠️  Replace with actual document scan icon URL"

# UI control icons
echo "Downloading UI control icons..."
curl -L -o downloads/freepik_icons/back_arrow.png "https://example.freepik.com/back-arrow-icon.png" || echo "⚠️  Replace with actual back arrow icon URL"
curl -L -o downloads/freepik_icons/camera.png "https://example.freepik.com/camera-icon.png" || echo "⚠️  Replace with actual camera icon URL"

# Status icons
echo "Downloading status icons..."
curl -L -o downloads/freepik_icons/success_checkmark.png "https://example.freepik.com/success-checkmark-icon.png" || echo "⚠️  Replace with actual success icon URL"
curl -L -o downloads/freepik_icons/error_x.png "https://example.freepik.com/error-x-icon.png" || echo "⚠️  Replace with actual error icon URL"
curl -L -o downloads/freepik_icons/warning.png "https://example.freepik.com/warning-icon.png" || echo "⚠️  Replace with actual warning icon URL"

# Brand assets
echo "Downloading brand assets..."
curl -L -o downloads/freepik_icons/company_logo.png "https://example.freepik.com/company-logo-icon.png" || echo "⚠️  Replace with actual logo icon URL"
curl -L -o downloads/freepik_icons/brand_identity.png "https://example.freepik.com/brand-identity-icon.png" || echo "⚠️  Replace with actual brand icon URL"

# Document overlays (these might be more complex graphics)
echo "Downloading document overlays..."
curl -L -o downloads/freepik_icons/passport_frame.png "https://example.freepik.com/passport-frame-icon.png" || echo "⚠️  Replace with actual passport frame URL"
curl -L -o downloads/freepik_icons/id_card_front_frame.png "https://example.freepik.com/id-card-frame-icon.png" || echo "⚠️  Replace with actual ID front frame URL"
curl -L -o downloads/freepik_icons/id_card_back_frame.png "https://example.freepik.com/id-card-back-frame-icon.png" || echo "⚠️  Replace with actual ID back frame URL"

echo ""
echo "📝 Next steps:"
echo "1. Replace the example URLs above with actual Freepik download links"
echo "2. Make sure you have proper licensing for the icons"
echo "3. Run this script to download the icons to downloads/freepik_icons/"
echo "4. Run ./rename_corporate_icons.sh to copy and rename them to corporate naming scheme"
echo "5. Build and test the app with the new corporate theme"
echo ""
echo "🎨 Icons will be used in the Corporate Theme image override option"
