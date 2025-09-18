#!/bin/bash

# Create demo override assets with visible differences
echo "Creating demo override assets..."

# Create a simple text file that can be used to verify override loading
echo "CORPORATE THEME ACTIVE" > sample-app/src/main/assets/corporate_demo.txt

# Copy more assets for demonstration
cp artiusid-sdk/src/main/res/raw/phone_up.gif sample-app/src/main/assets/corporate_phone_up.gif
cp artiusid-sdk/src/main/res/raw/phone_down.gif sample-app/src/main/assets/corporate_phone_down.gif

# Copy some status icons
cp artiusid-sdk/src/main/res/drawable/img_success.png sample-app/src/main/assets/corporate_success.png
cp artiusid-sdk/src/main/res/drawable/img_failed.png sample-app/src/main/assets/corporate_failed.png

# Copy document overlays
cp artiusid-sdk/src/main/res/drawable/passport_overlay.png sample-app/src/main/assets/corporate_passport_overlay.png
cp artiusid-sdk/src/main/res/drawable/state_id_front_overlay.png sample-app/src/main/assets/corporate_stateid_front_overlay.png

# Copy animations
cp artiusid-sdk/src/main/res/drawable/stateid_animation.gif sample-app/src/main/assets/corporate_stateid_animation.gif

echo "Demo assets created successfully!"
echo "Assets created:"
ls -la sample-app/src/main/assets/corporate_*
