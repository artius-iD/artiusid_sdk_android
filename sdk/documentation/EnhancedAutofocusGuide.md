# Enhanced Autofocus for Android Camera

This document explains the enhanced autofocus implementation that mirrors the iOS functionality.

## Overview

The enhanced autofocus system provides intelligent, adaptive focusing capabilities for document and barcode scanning, similar to the iOS implementation. It includes:

1. **Smart Autofocus Configuration** - Starts with continuous autofocus
2. **Focus Stability Monitor** - Monitors focus stability over time  
3. **Adaptive Focus Lock** - Locks focus when stable for better document capture
4. **Document-based Focus Adjustment** - Adjusts focus point based on detected document position
5. **Throttling** - Prevents excessive focus adjustments

## Key Components

### EnhancedCameraManager
Main camera manager that handles enhanced autofocus functionality:
- `configureEnhancedAutofocus()` - Sets up Camera2 interop for advanced autofocus
- `adjustFocusForDocument()` - Adjusts focus based on document bounds
- `initializeFocusMonitoring()` - Sets up focus stability monitoring
- `scheduleAdaptiveFocusLock()` - Schedules focus lock when stable

### FocusStabilityMonitor
Monitors camera focus state and determines when focus becomes stable:
- Checks focus stability every 200ms (similar to iOS timer interval)
- Requires 5 consecutive stable readings before considering focus stable
- Automatically stops monitoring after focus is deemed stable

### EnhancedDocumentCameraPreview
Composable that integrates enhanced autofocus with document scanning:
- Uses `EnhancedCameraManager` instead of standard CameraX
- Processes camera frames and adjusts focus based on detected document bounds
- Provides focus stability feedback to the UI

## Usage

### Basic Usage
Replace existing `DocumentCameraPreview` with `EnhancedDocumentCameraPreview`:

```kotlin
// Old way
DocumentCameraPreview(
    modifier = Modifier.fillMaxSize(),
    viewModel = viewModel
)

// New way with enhanced autofocus
EnhancedDocumentCameraPreview(
    modifier = Modifier.fillMaxSize(),
    viewModel = viewModel
)
```

### ViewModel Integration
The `DocumentScanViewModel` has been extended with methods that return document bounds:

```kotlin
// Enhanced methods that return document bounds for autofocus
fun processDocumentImageWithBounds(bitmap: Bitmap): Rect?
fun processBackScanImageWithBounds(bitmap: Bitmap): Rect?
```

### Focus Stability Monitoring
You can monitor focus stability state:

```kotlin
val enhancedCameraManager = EnhancedCameraManager(context, lifecycleOwner)

// Collect focus stability state
enhancedCameraManager.isFocusStable.collect { isStable ->
    // Update UI or perform actions based on focus stability
    if (isStable) {
        // Focus is stable - good time to capture
    }
}
```

## Key Features

### Document-Based Autofocus
- Automatically detects document bounds from ML Kit processing
- Adjusts focus point to center of detected document
- Only adjusts if document has moved significantly (> 5% of frame)
- Throttles adjustments to prevent excessive focusing

### Focus Stability Detection
- Monitors camera focus state changes
- Waits for 5 consecutive stable readings (1 second total)
- Automatically locks focus when stable for optimal capture

### Camera2 Interop Integration
- Uses Camera2Interop for advanced autofocus control
- Configures continuous picture autofocus mode
- Sets auto exposure with flash support
- Handles focus point and region configuration

## Configuration

### Focus Update Throttling
```kotlin
private val focusUpdateThrottleMs = 500L // Throttle focus updates
```

### Stability Requirements
```kotlin
private val requiredStabilityCount = 5 // 5 consecutive stable readings
```

### Monitoring Interval
```kotlin
delay(200) // Check every 200ms (similar to iOS timer)
```

## Comparison with iOS Implementation

| Feature | iOS Implementation | Android Implementation |
|---------|-------------------|----------------------|
| Focus Stability Monitor | `FocusStabilityMonitor` class | `FocusStabilityMonitor` class |
| Continuous Autofocus | `AVCaptureDevice.FocusMode.continuousAutoFocus` | `CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE` |
| Focus Point Adjustment | `device.focusPointOfInterest` | `FocusMeteringAction` with `MeteringPoint` |
| Focus Lock | `device.focusMode = .locked` | Camera zoom lock (Camera2 limitation) |
| Document Bounds Detection | `adjustFocusForDocument(CGRect)` | `adjustFocusForDocument(Rect, Int, Int)` |
| Throttling | 500ms throttle | 500ms throttle |
| Stability Count | 5 consecutive readings | 5 consecutive readings |
| Monitor Interval | 200ms timer | 200ms coroutine delay |

## Testing

To test the enhanced autofocus:

1. **Document Detection Test**: Place a document in frame and observe focus adjustment to document center
2. **Stability Test**: Keep document still and verify focus locks after ~1 second
3. **Movement Test**: Move document and verify focus readjusts to new position
4. **Throttling Test**: Rapidly move document and verify focus doesn't adjust excessively

## Troubleshooting

### Focus Not Adjusting
- Verify document bounds are being detected (`DocumentScanResult.documentBounds`)
- Check that Camera2Interop configuration succeeded
- Ensure camera permissions are granted

### Excessive Focus Adjustments
- Verify throttling is working (500ms minimum between adjustments)
- Check that document movement threshold is appropriate (5% of frame)

### Focus Never Locks
- Check `FocusStabilityMonitor` is running
- Verify camera state changes are being detected
- Ensure coroutines are not being cancelled prematurely

## Files Modified

1. `EnhancedCameraManager.kt` - Main enhanced camera manager
2. `EnhancedCameraPreview.kt` - Enhanced camera preview components
3. `DocumentScanViewModel.kt` - Added bounds-returning methods
4. `DocumentScanScreen.kt` - Updated to use enhanced preview
5. `DocumentScanResult.kt` - Added document bounds field
6. `BarcodeScanResult.kt` - Added success/error fields

This implementation provides iOS-like autofocus behavior for improved document and barcode scanning on Android.