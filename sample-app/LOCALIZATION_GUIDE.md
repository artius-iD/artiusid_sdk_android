# 🌐 SDK Localization Guide

## Overview

The artius.iD SDK now supports comprehensive localization, allowing the sample app to customize **any text** displayed in the embedded SDK. This enables complete control over the user experience and branding.

## How It Works

1. **Sample App Defines Custom Strings**: Add string resources to `sample-app/src/main/res/values/strings.xml`
2. **Automatic Bridge**: The `SampleAppLocalization` class automatically detects and collects your custom strings
3. **SDK Integration**: Custom strings are passed to the SDK via `SDKConfiguration.localizationOverrides`
4. **Runtime Override**: The SDK's `LocalizationManager` uses your custom strings instead of defaults

## Quick Start

### 1. Add Custom Strings

Edit `sample-app/src/main/res/values/strings.xml` and add any strings you want to customize:

```xml
<resources>
    <!-- Custom welcome message -->
    <string name="welcome_to">Welcome to Our Secure Platform</string>
    
    <!-- Custom button text -->
    <string name="button_start_now">Begin Verification</string>
    
    <!-- Custom processing message -->
    <string name="verification_processing">Securely processing your information…</string>
    
    <!-- Custom success message -->
    <string name="verification_successful">Identity Verified Successfully!</string>
</resources>
```

### 2. That's It!

The system automatically:
- ✅ Detects your custom strings
- ✅ Passes them to the SDK
- ✅ Applies them throughout the verification flow

## Available Strings to Customize

You can customize **any** of these string categories:

### 🔘 Button Labels
- `button_verify_now`, `button_start_now`, `button_continue`, `button_try_again`, etc.

### 📱 Screen Titles & Subtitles
- `select_document_title`, `face_scan_intro_title`, `verification_steps_title`, etc.

### 📝 Instructions & Tips
- `tip_good_lighting`, `face_tip_no_glasses`, `nfc_scan_instruction`, etc.

### 📊 Status Messages
- `face_scan_searching`, `verification_processing`, `nfc_scan_reading`, etc.

### ✅ Success & Error Messages
- `verification_successful`, `error_document_validation`, `face_scan_complete`, etc.

### 🔐 Permission Messages
- `permission_camera_title`, `permission_camera_message`, etc.

## Complete String Reference

For a complete list of all available strings, see:
`artiusid-sdk/src/main/res/values/strings.xml`

## Examples

### Corporate Banking Theme
```xml
<string name="welcome_to">Welcome to SecureBank</string>
<string name="button_start_now">Begin Identity Verification</string>
<string name="verification_successful">Account Verification Complete</string>
<string name="select_document_title">Choose Your Government ID</string>
```

### Healthcare Application
```xml
<string name="welcome_to">Welcome to MedSecure</string>
<string name="button_start_now">Start Patient Verification</string>
<string name="face_scan_intro_title">Patient Identity Confirmation</string>
<string name="verification_successful">Patient Identity Confirmed</string>
```

### Fintech Application
```xml
<string name="welcome_to">Welcome to CryptoTrade</string>
<string name="button_start_now">Begin KYC Process</string>
<string name="verification_processing">Validating your credentials…</string>
<string name="verification_successful">KYC Verification Complete</string>
```

## Multi-Language Support

Create language-specific folders for international support:

```
sample-app/src/main/res/
├── values/strings.xml           (Default/English)
├── values-es/strings.xml        (Spanish)
├── values-fr/strings.xml        (French)
└── values-de/strings.xml        (German)
```

## Testing Your Customizations

1. **Add Custom Strings**: Edit `strings.xml` with your custom text
2. **Build & Install**: Run `./gradlew :sample-app:installDebug`
3. **Test Verification**: Launch verification and see your custom text
4. **Check Logs**: Look for `SampleAppLocalization` and `LocalizationManager` logs

## Debugging

### View Active Overrides
Check the logs for `SampleAppLocalization` to see which strings are being overridden:

```
✅ Override: welcome_to = Welcome to Our Secure Platform
✅ Override: button_start_now = Begin Verification
```

### Verify SDK Usage
Check the logs for `LocalizationManager` to see when overrides are applied:

```
✅ Using override for 'welcome_to': Welcome to Our Secure Platform
📚 Using SDK string for 'button_cancel': Cancel
```

## Advanced Usage

### Dynamic String Generation
```kotlin
// In your custom localization class
class CustomLocalization {
    fun getWelcomeMessage(userName: String): String {
        return "Welcome back, $userName!"
    }
}
```

### Conditional Strings
```xml
<!-- Different strings based on user type -->
<string name="welcome_to">Welcome, Premium Member</string>
<string name="verification_successful">Premium Verification Complete</string>
```

## Best Practices

1. **🎯 Be Consistent**: Use consistent terminology across your app
2. **📏 Consider Length**: Some UI elements have space constraints
3. **🌍 Think Global**: Consider how text will translate to other languages
4. **♿ Accessibility**: Ensure text is clear and accessible
5. **🧪 Test Thoroughly**: Test all verification flows with your custom text

## Troubleshooting

### String Not Appearing?
1. Check the string name matches exactly (case-sensitive)
2. Verify the string is defined in `strings.xml`
3. Check logs for `SampleAppLocalization` errors
4. Rebuild and reinstall the app

### Logs Not Showing?
1. Enable debug logging in the SDK configuration
2. Filter logcat for `SampleAppLocalization` and `LocalizationManager`
3. Check that the string override is being detected

## Support

For additional support or questions about localization:
- Check the SDK documentation
- Review the sample implementations in `strings.xml`
- Contact the artius.iD development team

---

**🎉 Congratulations!** You now have complete control over all text in the artius.iD SDK. Customize away!
