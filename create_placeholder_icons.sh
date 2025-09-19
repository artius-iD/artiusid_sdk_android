#!/bin/bash

# Create placeholder icons for Corporate Theme testing
# These are simple colored squares that will serve as placeholders until real Freepik icons are downloaded

echo "🎨 Creating placeholder icons for Corporate Theme..."

# Create assets directory if it doesn't exist
mkdir -p sample-app/src/main/assets

# Function to create a simple colored PNG icon using ImageMagick (if available)
create_icon() {
    local filename=$1
    local color=$2
    local size=${3:-64}
    local text=$4
    
    if command -v convert &> /dev/null; then
        # Use ImageMagick to create a simple icon
        convert -size ${size}x${size} xc:"$color" \
                -gravity center -pointsize 12 -fill white \
                -annotate +0+0 "$text" \
                "sample-app/src/main/assets/$filename"
        echo "✅ Created $filename"
    else
        # Create a simple text file as placeholder if ImageMagick is not available
        echo "Placeholder icon: $text ($color)" > "sample-app/src/main/assets/$filename.txt"
        echo "⚠️  Created text placeholder for $filename (ImageMagick not available)"
    fi
}

# Create corporate-themed placeholder icons
echo "Creating face scan icons..."
create_icon "corporate_face_overlay.png" "#2E86AB" 128 "FACE"
create_icon "corporate_scan_face.png" "#2E86AB" 64 "SCAN"

echo "Creating document icons..."
create_icon "corporate_passport_icon.png" "#A23B72" 64 "PASS"
create_icon "corporate_stateid_icon.png" "#A23B72" 64 "ID"
create_icon "corporate_doc_scan.png" "#A23B72" 64 "DOC"

echo "Creating UI control icons..."
create_icon "corporate_back_button.png" "#F18F01" 48 "←"
create_icon "corporate_camera_button.png" "#F18F01" 64 "📷"

echo "Creating status icons..."
create_icon "corporate_success.png" "#C73E1D" 64 "✓"
create_icon "corporate_failed.png" "#C73E1D" 64 "✗"
create_icon "corporate_error.png" "#C73E1D" 64 "⚠"

echo "Creating brand assets..."
create_icon "corporate_logo.png" "#2E86AB" 96 "LOGO"
create_icon "corporate_brand_image.png" "#2E86AB" 128 "BRAND"

echo "Creating document overlays..."
create_icon "corporate_passport_overlay.png" "#A23B72" 256 "PASSPORT\nOVERLAY"
create_icon "corporate_stateid_front_overlay.png" "#A23B72" 256 "ID FRONT\nOVERLAY"
create_icon "corporate_stateid_back_overlay.png" "#A23B72" 256 "ID BACK\nOVERLAY"

echo ""
echo "✅ Placeholder icon creation complete!"
echo ""
echo "📝 Color scheme used:"
echo "  🔵 Blue (#2E86AB) - Face scan and brand assets"
echo "  🟣 Purple (#A23B72) - Document icons and overlays"
echo "  🟠 Orange (#F18F01) - UI controls"
echo "  🔴 Red (#C73E1D) - Status icons"
echo ""
echo "🎨 These placeholder icons will be used until you replace them with actual Freepik Special Lineal icons"
echo "📱 Build and install the app to test the Corporate Theme with these placeholders"
