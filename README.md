# Artius.iD Android SDK

Identity verification, biometric authentication and session binding for Android apps, distributed as an Android library (AAR).

| | |
|---|---|
| **Latest release** | [1.4.0](https://github.com/artius-iD/artiusid_sdk_android/releases/tag/v1.4.0) (September 21, 2026) |
| **Download** | [`artiusid-sdk-1.4.0.aar`](https://github.com/artius-iD/artiusid_sdk_android/releases/download/v1.4.0/artiusid-sdk-1.4.0.aar) |
| **Platform** | Android 7.0 (API 24) or later. Compiled against API 34. |
| **Toolchain** | Kotlin 1.9.10, Jetpack Compose (compiler 1.5.3, BOM 2023.10.01), Hilt 2.48 with KSP, JDK 17 |
| **iOS SDK** | [artius-iD/sdk](https://github.com/artius-iD/sdk) |

## Features

- **Enrollment.** Guided face capture and government ID capture (photo ID or passport, including the passport chip over NFC). When a document image can't be read, the SDK walks the user through a retry.
- **Biometric authentication.** Returning users confirm their identity with the device's biometric check.
- **Session binding (patent pending).** Users confirm browser sign-ins on their enrolled phone, so a stolen password or session token isn't enough on its own.
- **Approval requests.** Users approve or decline requests that your backend sends to their phone.
- **Organization sign-in.** Enrollment can be tied to your organization's own login, such as Okta or another OIDC provider.
- **Real-time session status.** A WebSocket heartbeat reports binding-session state changes (active, locked, closed) to your app as they happen, with automatic reconnect.
- **Mutual TLS.** The SDK registers client certificates for the device and uses them for its service calls, with a separate certificate for the real-time gateway.
- **Branding.** You can set your own colors, fonts, logo, text and language.

## What's new in 1.4.0

- Real-time binding-session status over a WebSocket connection. `ArtiusIDSDK.connectBindingWebSocket(...)` opens a heartbeat channel and reports session-status changes through `bindingSessionStatusFlow` and `onBindingSessionStatusChange`, with automatic reconnect configurable via `BindingWebSocketConfig`. `disconnectBindingWebSocket()` and `stopBindingWebSocket()` close it.
- Separate client certificates for the REST API and the WebSocket gateway (`CertificatePurpose`, `ensureCertificateRegistered(context, purpose)`). Existing REST behavior is unchanged.
- Brings the Android binding-session API in line with the iOS SDK.

## What's new in 1.3.2

- Fixed the binding-session gateway URL when the SDK is configured with URL templates: it could resolve to a non-existent host, so binding responses failed to connect. Enrollment and verification were unaffected.

## What's new in 1.3.1

- Reduced the SDK's public API surface. The presence monitor's internal classes are no longer part of the public API. The host-facing binding, enrollment and presence APIs are unchanged.

## What's new in 1.3.0

- Session binding for host apps: receive requests through `AppNotificationState` and answer them with `ArtiusIDSDK.sendBindingResponse`.
- `AppNotificationState`, `BindingResultData`, `ThirdPartyLoginResult` and `VerificationStateManager` keep their names in the minified AAR, so apps can call them.
- Includes the fixes from releases 1.2.56 through 1.2.63.

See [CHANGELOG.md](CHANGELOG.md) for earlier releases.

## Installation

### 1. Add the AAR

Download [`artiusid-sdk-1.4.0.aar`](https://github.com/artius-iD/artiusid_sdk_android/releases/download/v1.4.0/artiusid-sdk-1.4.0.aar) and copy it to `app/libs/`:

```bash
curl -L -o app/libs/artiusid-sdk-1.4.0.aar \
  https://github.com/artius-iD/artiusid_sdk_android/releases/download/v1.4.0/artiusid-sdk-1.4.0.aar
```

### 2. Configure Gradle

The SDK is compiled with the Compose compiler and BOM listed above. Use the same versions in your app. Mismatched versions crash at runtime (see [Troubleshooting](#troubleshooting)).

`app/build.gradle.kts`:

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")       // 1.9.10
    id("com.google.dagger.hilt.android")     // 2.48
    id("com.google.devtools.ksp")            // 1.9.10-1.0.13
    id("com.google.gms.google-services")     // for Firebase Cloud Messaging
}

android {
    compileSdk = 34
    defaultConfig {
        minSdk = 24
        targetSdk = 34
    }
    buildFeatures { compose = true }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.3" }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    packaging {
        resources {
            excludes += setOf("/META-INF/{AL2.0,LGPL2.1}", "META-INF/versions/9/OSGI-INF/MANIFEST.MF")
            pickFirsts += setOf("META-INF/DEPENDENCIES", "META-INF/LICENSE*", "META-INF/NOTICE*")
        }
    }
}
```

An AAR carries no dependency metadata, so declare the libraries the SDK uses:

```kotlin
dependencies {
    implementation(files("libs/artiusid-sdk-1.4.0.aar"))

    val camerax = "1.4.2"
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation("androidx.biometric:biometric:1.1.0")
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material3:material3-window-size-class")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.runtime:runtime-livedata")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.navigation:navigation-compose:2.7.6")
    implementation("androidx.camera:camera-core:$camerax")
    implementation("androidx.camera:camera-camera2:$camerax")
    implementation("androidx.camera:camera-lifecycle:$camerax")
    implementation("androidx.camera:camera-view:$camerax")
    implementation("androidx.camera:camera-extensions:$camerax")
    implementation("com.microsoft.onnxruntime:onnxruntime-android:1.23.2")
    implementation("com.google.mlkit:face-detection:16.1.7")
    implementation("com.google.mlkit:text-recognition:16.0.1")
    implementation("com.google.mlkit:barcode-scanning:17.3.0")
    implementation("com.google.mlkit:object-detection:17.0.2")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("androidx.exifinterface:exifinterface:1.3.7")
    implementation("io.coil-kt:coil-compose:2.4.0")
    implementation("io.coil-kt:coil-gif:2.4.0")
    implementation("io.coil-kt:coil-base:2.4.0")
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("com.google.zxing:core:3.5.2")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.32.0")
    implementation("com.google.dagger:hilt-android:2.48")
    ksp("com.google.dagger:hilt-android-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    implementation("io.insert-koin:koin-android:3.5.0")
    implementation("io.insert-koin:koin-androidx-compose:3.5.0")
    implementation(platform("com.google.firebase:firebase-bom:32.7.2"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-messaging:23.4.1")
    implementation("org.jmrtd:jmrtd:0.7.34")
    implementation("net.sf.scuba:scuba-sc-android:0.0.23")
    implementation("edu.ucar:jj2000:5.2")
    implementation("com.github.mhshams:jnbis:1.1.0")
    implementation("com.madgag.spongycastle:core:1.58.0.0")
    implementation("com.madgag.spongycastle:prov:1.58.0.0")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("com.google.code.gson:gson:2.10.1")
}
```

If you use the Groovy DSL, [SDK_DEPENDENCY_REQUIREMENTS.md](SDK_DEPENDENCY_REQUIREMENTS.md) explains each group of dependencies.

### 3. Minified release builds

The AAR's consumer rules keep the SDK's public API. If your release build runs R8, also add these lines to your `proguard-rules.pro` for classes that the SDK's libraries reference but Android doesn't include:

```proguard
-dontwarn java.applet.**
-dontwarn java.awt.**
-dontwarn javax.naming.**
-dontwarn java.lang.management.**
-dontwarn com.google.api.client.**
-dontwarn org.joda.time.**
```

## Setup

### 1. Get credentials

For each environment you use, Artius.iD issues a client ID, a client group ID and the service domains. To request sandbox access, use the [Artius.iD developer portal](https://developer.artiusid.ai).

### 2. Make your app a Hilt app

The SDK's screens use Hilt, so your `Application` class must be annotated. See the [Hilt integration guide](HILT_INTEGRATION_GUIDE.md) for details.

```kotlin
@HiltAndroidApp
class MyApp : Application()
```

### 3. Initialize the SDK

Initialize once, from `Application.onCreate()` or before you start any SDK flow:

```kotlin
val configuration = SDKConfiguration(
    apiKey = "my-app",                          // any non-empty identifier for your app
    environment = Environment.SANDBOX,
    urlTemplate = "https://#env#.#domain#",
    mobileDomain = "mobile.artiusid.ai",
    registrationUrlTemplate = "https://#env#.#domain#",
    registrationDomain = "registration.artiusid.ai",
    clientId = CLIENT_ID,                       // issued by Artius.iD
    clientGroupId = CLIENT_GROUP_ID,            // issued by Artius.iD
    handleFirebaseNotifications = false,        // your app owns Firebase (see Push notifications)
    hostAppPackageName = packageName,
)

val theme = EnhancedSDKThemeConfiguration.artiusIDDefault().withBrandName("Acme")

ArtiusIDSDK.initializeWithEnhancedTheme(applicationContext, configuration, theme)
```

The SDK replaces `#env#` with the environment's prefix and `#domain#` with the domain you pass. The sandbox values above resolve to `https://sandbox.mobile.artiusid.ai` and `https://sandbox.registration.artiusid.ai`. For every other environment, pass the templates and domains that Artius.iD gives you. Always pass all four values explicitly instead of relying on the SDK's defaults.

Other `SDKConfiguration` options:

| Option | Default | Purpose |
|---|---|---|
| `isThirdPartyLoginEnabled` | `true` | Requires an organization sign-in before enrollment (see [Organization sign-in](#organization-sign-in)). |
| `thirdPartyLoginUrl` | `null` | Your organization sign-in endpoint, as provided by Artius.iD. |
| `includeOktaIDInVerificationPayload` | `true` | Includes the user's Okta ID in the enrollment request. |
| `enableLogging` | `false` | Turns on SDK logging. Leave it off in release builds. |
| `localizationOverrides` | empty | Replaces SDK strings by key. |

Enrollment runs as binding enrollment by default. To run identity verification only, call `ArtiusIDSDK.setVerificationOperationMode(OperationMode.VERIFY)`.

### 4. Push notifications

Your app configures Firebase Cloud Messaging with its own `google-services.json`, then passes the token and Artius.iD requests to the SDK:

```kotlin
class ArtiusIDMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        ArtiusIDSDK.updateFcmToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        val approvalTitle = data["approvalTitle"]
        val sessionId = data["sessionId"]
        when {
            !approvalTitle.isNullOrEmpty() -> AppNotificationState.handleApprovalNotification(
                requestId = data["requestId"]?.toIntOrNull(),
                title = approvalTitle,
                description = data["approvalDescription"],
            )
            !sessionId.isNullOrEmpty() -> AppNotificationState.handleBindingNotification(
                sessionId = sessionId,
                title = data["bindingTitle"],
                description = data["bindingDescription"],
            )
            else -> Unit // not an Artius.iD request
        }
    }
}
```

Register the service in `AndroidManifest.xml`:

```xml
<service
    android:name=".ArtiusIDMessagingService"
    android:exported="false">
    <intent-filter>
        <action android:name="com.google.firebase.MESSAGING_EVENT" />
    </intent-filter>
</service>
```

When the app is in the background, show a notification that opens your activity. The activity then displays the pending request, as shown in [Approval requests and session binding](#approval-requests-and-session-binding).

## Enrollment

```kotlin
ArtiusIDSDK.startVerification(activity, object : VerificationCallback {
    override fun onVerificationSuccess(result: VerificationResult) {
        if (result.requiresRecapture) {
            // The user left a document retry. Offer to start again.
        } else {
            Log.i("Enroll", "Enrolled account ${result.accountNumber}")
        }
    }

    override fun onVerificationError(error: SDKError) {
        Log.w("Enroll", "Enrollment failed: ${error.code} ${error.message}")
    }

    override fun onVerificationCancelled() = Unit
})
```

Check `requiresRecapture` first. The SDK already offers the user a retry for unreadable document images. You only receive this result if the user leaves that retry.

`VerificationResult` also carries `fullName`, `firstName`, `lastName`, `verificationScore`, `faceMatchScore`, `documentStatus` and `errorMessage`.

## Returning-user authentication

```kotlin
ArtiusIDSDK.startAuthentication(activity, object : AuthenticationCallback {
    override fun onAuthenticationSuccess(result: AuthenticationResult) {
        Log.i("Auth", "Authenticated: ${result.message}")
    }

    override fun onAuthenticationError(error: SDKError) {
        Log.w("Auth", "Authentication failed: ${error.message}")
    }

    override fun onAuthenticationCancelled() = Unit
})
```

## Organization sign-in

To sign the user in with your organization's credentials and register the device's push token for that user, call:

```kotlin
suspend fun signIn(context: Context, loginId: String, password: String): String {
    ArtiusIDSDK.setThirdPartyLoginUrl(LOGIN_URL)   // provided by Artius.iD
    val result = ArtiusIDSDK.validateCredentialsAndRegisterFCM(context, loginId, password)
    check(result.isSuccessful) { result.errorMessage ?: "Sign-in failed" }
    return result.loginId ?: loginId
}
```

Call it after the SDK has a push token. To use your own identity provider behind the SDK's sign-in screen, install a handler:

```kotlin
ArtiusIDSDK.setThirdPartyLoginHandler { loginId, password, environment ->
    val accepted = myIdentityProvider.verify(loginId, password)
    ThirdPartyLoginResult(
        isSuccessful = accepted,
        loginId = if (accepted) loginId else null,
        errorMessage = if (accepted) null else "Invalid credentials",
    )
}
```

## Approval requests and session binding

`AppNotificationState` holds the pending request as `StateFlow`s: `notificationType`, `notificationTitle`, `notificationDescription`, `requestId` and `sessionId`.

1. When a request arrives, open your screen.
2. Call `markNotificationConsumed()` so the same request doesn't open again.
3. Call `reset()` when the user finishes or backs out.

```kotlin
@Composable
fun ArtiusIDRequestHost() {
    val type by AppNotificationState.notificationType.collectAsState()
    var showing by remember { mutableStateOf<AppNotificationState.NotificationType?>(null) }

    LaunchedEffect(type) {
        if (type != AppNotificationState.NotificationType.DEFAULT) {
            showing = type
            AppNotificationState.markNotificationConsumed()
        }
    }

    val done = {
        showing = null
        AppNotificationState.reset()
    }
    when (showing) {
        AppNotificationState.NotificationType.APPROVAL -> ApprovalScreen(onDone = done)
        AppNotificationState.NotificationType.BINDING -> BindingScreen(onDone = done)
        else -> Unit
    }
}
```

**Approvals.** Show the request's title and description, then send the user's answer:

```kotlin
val result = ArtiusIDSDK.sendApprovalResponse(context, if (approved) "Approved" else "Deny")
```

**Session binding.** In 1.3.0, your app provides the binding screen and submits the user's decision with `ArtiusIDSDK.sendBindingResponse`. Artius.iD provides the screen specification, the browser-side setup and the additional session-binding permissions with your integration package. The binding screen needs an enrolled device (see [Enrollment](#enrollment)).

## Branding and language

```kotlin
val theme = EnhancedSDKThemeConfiguration(
    brandName = "Acme",
    brandLogoResourceName = "acme_logo",       // a drawable in your app
    colorScheme = SDKColorScheme(
        primaryColorHex = "#1B6EF3",
        secondaryColorHex = "#FF8A00",
    ),
)
ArtiusIDSDK.initializeWithEnhancedTheme(applicationContext, configuration, theme)

ArtiusIDSDK.setLanguage(context, "es")
```

`EnhancedSDKThemeConfiguration` also accepts `typography`, `iconTheme`, `textContent`, `componentStyling`, `layoutConfig` and `animationConfig`. See [THEMING_GUIDE.md](THEMING_GUIDE.md).

## Troubleshooting

| Symptom | Fix |
|---|---|
| `NoSuchMethodError` from `androidx.compose.ui.semantics` (for example `performImeAction$default`) when an SDK screen opens | Use Compose BOM `2023.10.01` and `kotlinCompilerExtensionVersion = "1.5.3"`. |
| `Hilt Activity must be attached to an @HiltAndroidApp Application` | Annotate your `Application` class with `@HiltAndroidApp`. |
| R8 reports missing classes such as `java.awt.*` or `javax.naming.*` | Add the `-dontwarn` rules from [Minified release builds](#3-minified-release-builds). |
| `Unresolved reference` to an SDK function that this README doesn't describe | The release AAR renames internal SDK code. Use the API documented here. |
| Certificate registration fails after switching environments | Clear the app's data (`adb shell pm clear <your.package>`) and start again. |
| Camera or NFC screens don't work on an emulator | Enrollment needs a physical device. |
| No push token | Add your `google-services.json` and apply the `com.google.gms.google-services` plugin. |

For more Hilt help, run `./setup_hilt.sh` or see [README_HILT_SETUP.md](README_HILT_SETUP.md).

## Repository contents

| Path | Contents |
|---|---|
| [Releases](https://github.com/artius-iD/artiusid_sdk_android/releases) | The SDK AAR for each version |
| [HILT_INTEGRATION_GUIDE.md](HILT_INTEGRATION_GUIDE.md), [README_HILT_SETUP.md](README_HILT_SETUP.md) | Hilt setup |
| [SDK_DEPENDENCY_REQUIREMENTS.md](SDK_DEPENDENCY_REQUIREMENTS.md) | Dependency notes |
| [THEMING_GUIDE.md](THEMING_GUIDE.md) | Theme options |
| [CHANGELOG.md](CHANGELOG.md) | Release history |
| `sample-app/` | An example integration written for the 1.2 releases |

## Support

Request credentials and integration help through the [Artius.iD developer portal](https://developer.artiusid.ai). Company information is at [artiusid.ai](https://www.artiusid.ai).

## License

Copyright © 2024–2026 Artius.iD, Inc. All rights reserved. Your license agreement with Artius.iD governs use of this SDK.
