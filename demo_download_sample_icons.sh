#!/bin/bash

# Demo script to download some sample icons for testing
# This demonstrates the workflow with actual downloadable icons

echo "🎨 Demo: Downloading sample icons for Corporate Theme testing..."

# Create downloads directory
mkdir -p downloads/freepik_icons

echo "📥 Downloading sample icons from free icon sources..."

# Download some free icons for demonstration
# Using free icons from various sources for testing

# Face/user related icons
curl -L -o downloads/freepik_icons/face_overlay.png "https://cdn-icons-png.flaticon.com/512/1077/1077114.png" 2>/dev/null || echo "⚠️  Could not download face icon"
curl -L -o downloads/freepik_icons/scan_face.png "https://cdn-icons-png.flaticon.com/512/2919/2919906.png" 2>/dev/null || echo "⚠️  Could not download scan face icon"

# Document icons
curl -L -o downloads/freepik_icons/passport_icon.png "https://cdn-icons-png.flaticon.com/512/2534/2534204.png" 2>/dev/null || echo "⚠️  Could not download passport icon"
curl -L -o downloads/freepik_icons/id_card_icon.png "https://cdn-icons-png.flaticon.com/512/2534/2534203.png" 2>/dev/null || echo "⚠️  Could not download ID card icon"
curl -L -o downloads/freepik_icons/document_scan.png "https://cdn-icons-png.flaticon.com/512/2534/2534195.png" 2>/dev/null || echo "⚠️  Could not download document scan icon"

# UI control icons
curl -L -o downloads/freepik_icons/back_arrow.png "https://cdn-icons-png.flaticon.com/512/271/271220.png" 2>/dev/null || echo "⚠️  Could not download back arrow icon"
curl -L -o downloads/freepik_icons/camera.png "https://cdn-icons-png.flaticon.com/512/685/685655.png" 2>/dev/null || echo "⚠️  Could not download camera icon"

# Status icons
curl -L -o downloads/freepik_icons/success_checkmark.png "https://cdn-icons-png.flaticon.com/512/190/190411.png" 2>/dev/null || echo "⚠️  Could not download success icon"
curl -L -o downloads/freepik_icons/error_x.png "https://cdn-icons-png.flaticon.com/512/753/753345.png" 2>/dev/null || echo "⚠️  Could not download error icon"
curl -L -o downloads/freepik_icons/warning.png "https://cdn-icons-png.flaticon.com/512/595/595067.png" 2>/dev/null || echo "⚠️  Could not download warning icon"

# Brand assets
curl -L -o downloads/freepik_icons/company_logo.png "https://cdn-icons-png.flaticon.com/512/2534/2534176.png" 2>/dev/null || echo "⚠️  Could not download logo icon"
curl -L -o downloads/freepik_icons/brand_identity.png "https://cdn-icons-png.flaticon.com/512/2534/2534177.png" 2>/dev/null || echo "⚠️  Could not download brand icon"

# Document overlays (using frame-like icons)
curl -L -o downloads/freepik_icons/passport_frame.png "https://cdn-icons-png.flaticon.com/512/2534/2534204.png" 2>/dev/null || echo "⚠️  Could not download passport frame"
curl -L -o downloads/freepik_icons/id_card_front_frame.png "https://cdn-icons-png.flaticon.com/512/2534/2534203.png" 2>/dev/null || echo "⚠️  Could not download ID front frame"
curl -L -o downloads/freepik_icons/id_card_back_frame.png "https://cdn-icons-png.flaticon.com/512/2534/2534203.png" 2>/dev/null || echo "⚠️  Could not download ID back frame"

echo ""
echo "✅ Sample icon download complete!"
echo ""
echo "📊 Downloaded files:"
ls -la downloads/freepik_icons/*.png 2>/dev/null | wc -l | xargs echo "   PNG files:"
echo ""
echo "📝 Next steps:"
echo "1. Run ./rename_corporate_icons.sh to copy and rename them"
echo "2. Build and test the app with the new icons"
echo ""
echo "⚠️  Note: These are sample icons for testing. For production, use:"
echo "   - Freepik Special Lineal icons from: https://www.freepik.com/author/freepik/icons/special-lineal_7"
echo "   - Make sure you have proper licensing for commercial use"
