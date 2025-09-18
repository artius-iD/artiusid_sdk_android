# Sample App Image Override Assets

This directory contains sample image override assets for demonstrating the artius.iD SDK image override functionality.

## Directory Structure

```
assets/
├── custom_images/          # File-based override examples
├── corporate/              # Corporate theme assets
├── modern/                 # Modern theme assets
├── custom/                 # Custom extended theme assets
└── README.md              # This file
```

## Asset Categories

### Face Scan Assets
- `face_overlay.png` - Face outline overlay for positioning guidance
- `face_up.gif` - Face positioning up animation
- `face_down.gif` - Face positioning down animation  
- `phone_up.gif` - Phone positioning up animation
- `phone_down.gif` - Phone positioning down animation

### Document Assets
- `passport_overlay.png` - Passport scan overlay
- `stateid_front_overlay.png` - State ID front scan overlay
- `stateid_back_overlay.png` - State ID back scan overlay
- `passport_animation.gif` - Passport scan animation
- `stateid_animation.gif` - State ID scan animation

### UI Icons
- `back_button.png` - Back navigation button
- `camera_button.png` - Camera capture button
- `scan_face.png` - Face scan step icon
- `doc_scan.png` - Document scan step icon
- `passport_icon.png` - Passport selection icon
- `stateid_icon.png` - State ID selection icon

### Status Icons
- `success.png` - Success/approval icon
- `failed.png` - Failure/error icon
- `error.png` - System error icon

### Brand Assets
- `logo.png` - Brand logo
- `brand_image.png` - Brand illustration

## Usage in Sample App

The sample app demonstrates different override configurations:

1. **Corporate Theme** - Professional styling with blue/grey colors
2. **Modern Theme** - Sleek, modern design with gradients
3. **URL-Based** - Loading images from web URLs (demo)
4. **File-Based** - Loading from local file system paths
5. **Custom Extended** - Using custom override extensibility

## Asset Requirements

- **Format**: PNG for static images, GIF for animations
- **Size**: Match or exceed default SDK asset dimensions
- **Quality**: High resolution for multi-density support
- **Transparency**: PNG with alpha channel support recommended

## Testing Override Assets

To test custom override assets:

1. Add your assets to the appropriate theme directory
2. Update the asset names in `SampleImageOverrides.kt`
3. Select the theme in the sample app
4. Launch verification to see your custom assets

## Production Considerations

- Use appropriate image compression
- Consider multi-density assets (hdpi, xhdpi, etc.)
- Test loading performance with large assets
- Implement proper error handling for missing assets
- Use HTTPS URLs for web-hosted assets
- Cache assets appropriately for offline usage
