# Technology Stack

**Project:** MotherLEDisa - Android BLE LED Tower Light Controller
**Researched:** 2026-03-25
**Overall Confidence:** HIGH

## Executive Summary

This stack is built for an Android app that controls MELK-OT21 Bluetooth tower lights with timeline-based animation editing. The architecture prioritizes:

1. **Low-latency BLE communication** - Nordic's battle-tested BLE library with Kotlin coroutines
2. **Fluid UI for animation editing** - Jetpack Compose with Canvas for custom timeline/keyframe editor
3. **Real-time audio processing** - TarsosDSP for FFT-based sound reactivity
4. **Modern Android architecture** - MVVM with StateFlow, Hilt DI, Room persistence

The ELK-BLEDOM protocol is well-documented and simple (9-byte commands), making the BLE layer straightforward once the connection is established.

---

## Recommended Stack

### Build Configuration

| Component | Version | Rationale |
|-----------|---------|-----------|
| compileSdk | 35 | Current Google Play requirement (API 35 required by Aug 2025) |
| targetSdk | 35 | Matches Play Store requirements for new app submissions |
| minSdk | 29 (Android 10) | BLE improvements, modern permission model, broad device coverage |
| Kotlin | 2.1.20 | Stable, well-tested with Compose; 2.2+ has breaking changes |
| AGP | 8.8.0+ | Latest stable supporting API 35; 8.9.1 if using latest androidx.core |
| Gradle | 8.11.1 | Compatible with AGP 8.8/8.9 |
| Java Target | 17 | Required by Nordic BLE Library 2.7+ |

**Confidence:** HIGH - Verified via official Android Developer documentation

---

### Core Framework

| Technology | Version | Purpose | Rationale |
|------------|---------|---------|-----------|
| Jetpack Compose | BOM 2025.12.00 | UI Framework | December '25 release is stable with 1.10 core modules; pausable composition provides performance parity with Views; native animation APIs for timeline editor |
| Material 3 | 1.4.0 (via BOM) | Design System | Included in BOM; modern theming, adaptive layouts |
| Navigation Compose | 2.9.7 | Screen Navigation | Type-safe routes (stable since 2.8.0); compile-time safety with @Serializable routes |

**Confidence:** HIGH - Verified via Android Developers Blog (December 2025 release notes)

```kotlin
// build.gradle.kts
dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2025.12.00")
    implementation(composeBom)

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.navigation:navigation-compose:2.9.7")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
```

---

### Bluetooth Low Energy

| Technology | Version | Purpose | Rationale |
|------------|---------|---------|-----------|
| Nordic Android-BLE-Library | 2.11.0 | BLE Connection & Communication | Industry standard; handles connection retries, queuing, MTU negotiation, error recovery. Used by thousands of production apps. Kotlin coroutines support via `.suspend()` and `.asFlow()` |
| Nordic Android Scanner Compat | 1.6.0 | BLE Device Discovery | Recommended companion to BLE Library; backports scanning features to older Android versions |

**Why Nordic over alternatives:**
- **BLESSED-Kotlin** - Good library but smaller community; Nordic has better documentation and enterprise support
- **Kable (Kotlin Multiplatform)** - Overkill for Android-only app; adds KMP complexity we don't need
- **Raw Android BLE APIs** - Callback hell, manual queue management, many edge cases to handle

**Confidence:** HIGH - Nordic BLE Library verified via GitHub (2.11.0 released Sept 2025)

```kotlin
dependencies {
    implementation("no.nordicsemi.android:ble:2.11.0")
    implementation("no.nordicsemi.android:ble-ktx:2.11.0")  // Kotlin coroutines, Flow
    implementation("no.nordicsemi.android.support.v18:scanner:1.6.0")
}
```

---

### ELK-BLEDOM Protocol Details

The MELK-OT21 uses the ELK-BLEDOM protocol, which is well-documented:

| Constant | Value | Notes |
|----------|-------|-------|
| Service UUID | `0000fff0-0000-1000-8000-00805f9b34fb` | Primary service |
| Characteristic UUID | `0000fff3-0000-1000-8000-00805f9b34fb` | Write commands here |
| Command Start Byte | `0x7E` | All commands start with this |
| Command End Byte | `0xEF` | All commands end with this |
| Command Length | 9 bytes | Fixed length |

**Command Examples:**
```
Power ON:  7e 04 04 f0 00 01 ff 00 ef
Power OFF: 7e 04 04 00 00 00 ff 00 ef
Red:       7e 07 05 03 ff 00 00 10 ef
Green:     7e 07 05 03 00 ff 00 10 ef
Blue:      7e 07 05 03 00 00 ff 10 ef
```

**Confidence:** HIGH - Verified via multiple GitHub implementations (FergusInLondon/ELK-BLEDOM, dave-code-ruiz/elkbledom)

---

### Audio Processing (Sound Reactivity)

| Technology | Version | Purpose | Rationale |
|------------|---------|---------|-----------|
| TarsosDSP | 2.4 | FFT & Audio Analysis | Pure Java, no native dependencies, out-of-box Android support. Includes FFT, onset detection, pitch detection. Proven in thousands of music apps |
| Android AudioRecord | Native | Microphone Capture | Standard Android API for raw audio capture |

**Why TarsosDSP:**
- Zero external dependencies (critical for Android)
- Real-time FFT with spectral peak extraction
- Supports sampling rates up to device maximum
- Active development, academic backing

**What NOT to use:**
- **Superpowered SDK** - Commercial, overkill for this use case
- **Oboe** - C++ library for low-latency audio OUTPUT, not analysis
- **Processing/Minim** - Desktop-focused, Android support is hacky

**Confidence:** MEDIUM - TarsosDSP verified via GitHub; version 2.4 last release was 2022 but library is stable and maintained

```kotlin
dependencies {
    implementation("be.tarsos.dsp:core:2.4")
    implementation("be.tarsos.dsp:android:2.4")
}
```

**Note:** TarsosDSP is available via JitPack, not Maven Central:
```kotlin
repositories {
    maven { url = uri("https://jitpack.io") }
}
dependencies {
    implementation("com.github.AtsushiSaito:TarsosDSP-Android:1.0.0")
    // OR build from source for latest
}
```

---

### State Management & Architecture

| Technology | Version | Purpose | Rationale |
|------------|---------|---------|-----------|
| ViewModel | 2.9.0 (lifecycle) | Screen State | Survives config changes; SavedStateHandle for process death |
| StateFlow | kotlinx.coroutines 1.9.0 | Reactive State | Hot stream, Compose-native, better than LiveData |
| Hilt | 2.56 | Dependency Injection | Official Android DI; KSP support for faster builds |
| AndroidX Hilt | 1.2.0 | Compose Integration | hiltViewModel() for ViewModel injection |

**Architecture Pattern:** MVVM with Unidirectional Data Flow
- State flows DOWN from ViewModel to Composables
- Events flow UP from Composables to ViewModel
- Single source of truth per screen

**Confidence:** HIGH - Verified via official Android documentation

```kotlin
// build.gradle.kts (project level)
plugins {
    id("com.google.dagger.hilt.android") version "2.56" apply false
    id("com.google.devtools.ksp") version "2.1.20-1.0.29" apply false
}

// build.gradle.kts (app level)
plugins {
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

dependencies {
    implementation("com.google.dagger:hilt-android:2.56")
    ksp("com.google.dagger:hilt-compiler:2.56")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
}
```

---

### Database (Preset Storage)

| Technology | Version | Purpose | Rationale |
|------------|---------|---------|-----------|
| Room | 2.8.4 | Local Database | Official Android persistence; KSP for code generation; Kotlin coroutines with room-ktx |

**Schema includes:**
- Animation presets (name, keyframes, metadata)
- Device profiles (MAC address, name, tower position)
- User preferences

**Confidence:** HIGH - Verified via official Android Room documentation (2.8.4 released Nov 2025)

```kotlin
dependencies {
    val roomVersion = "2.8.4"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
}
```

---

### Data Serialization

| Technology | Version | Purpose | Rationale |
|------------|---------|---------|-----------|
| Kotlinx Serialization | 1.10.0 | JSON Encoding/Decoding | Native Kotlin, no reflection, compile-time safety. Use for animation preset export/import |

**Confidence:** HIGH - Verified via Kotlin documentation

```kotlin
plugins {
    kotlin("plugin.serialization") version "2.1.20"
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0")
}
```

---

### Timeline Animation Editor (Custom UI)

No off-the-shelf library exists for After Effects-style timeline editors in Compose. Build custom using:

| Component | Compose API | Purpose |
|-----------|-------------|---------|
| Timeline Track | `Canvas` + `Modifier.horizontalScroll` | Draw keyframes, playhead, time ruler |
| Keyframe Dragging | `Modifier.pointerInput` + `detectDragGestures` | Move keyframes |
| Zoom/Pan | `transformable` modifier | Pinch to zoom timeline |
| Animations | `Animatable` + `keyframes` spec | Preview animations |
| Color Picker | Material 3 or custom Canvas | Keyframe color values |

**Reference libraries for inspiration (not dependencies):**
- [JetLime](https://github.com/pushpalroy/JetLime) - Timeline view concepts
- [composeCanvas](https://github.com/nameisjayant/composeCanvas) - Custom Compose drawing examples

**Confidence:** MEDIUM - No existing library means custom implementation required; Compose Canvas APIs well-documented

---

### Supporting Libraries

| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| Coil | 3.0.4 | Image Loading | Preset thumbnails (if needed) |
| Timber | 5.0.1 | Logging | Debug logging with tags |
| LeakCanary | 2.14 | Memory Leak Detection | Debug builds only |

```kotlin
dependencies {
    implementation("io.coil-kt.coil3:coil-compose:3.0.4")
    implementation("com.jakewharton.timber:timber:5.0.1")
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.14")
}
```

---

## Alternatives Considered

| Category | Recommended | Alternative | Why Not |
|----------|-------------|-------------|---------|
| BLE Library | Nordic BLE | Kable | Kable adds KMP complexity; we're Android-only |
| BLE Library | Nordic BLE | BLESSED-Kotlin | Smaller community, less documentation |
| UI Framework | Jetpack Compose | XML Views | Views are legacy; Compose is the future |
| Audio FFT | TarsosDSP | Oboe | Oboe is for audio OUTPUT, not analysis |
| State | StateFlow | LiveData | StateFlow has better Compose integration |
| DI | Hilt | Koin | Hilt has official Android support, compile-time safety |
| Database | Room | SQLDelight | Room has better Android tooling integration |
| JSON | Kotlinx Serialization | Moshi/Gson | Native Kotlin, no reflection |

---

## What NOT To Use

| Technology | Reason |
|------------|--------|
| **RxJava/RxKotlin** | Coroutines are the modern Android standard; RxJava adds complexity |
| **KAPT** | Use KSP instead; KAPT is deprecated, slower builds |
| **LiveData** | StateFlow is preferred for Compose; LiveData requires observeAsState() bridge |
| **DataStore** | Room is better for structured animation data; DataStore is for key-value |
| **Proto DataStore** | Overkill for this use case |
| **Jetpack Navigation (XML)** | Use Navigation Compose with type-safe routes |
| **Fragment** | Compose doesn't need Fragments for navigation |
| **View Binding** | Not applicable to Compose |

---

## Complete build.gradle.kts (App Module)

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.motherledisa"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.motherledisa"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2025.12.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.9.7")

    // Lifecycle & ViewModel
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.0")

    // Hilt DI
    implementation("com.google.dagger:hilt-android:2.56")
    ksp("com.google.dagger:hilt-compiler:2.56")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Room Database
    val roomVersion = "2.8.4"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // BLE (Nordic)
    implementation("no.nordicsemi.android:ble:2.11.0")
    implementation("no.nordicsemi.android:ble-ktx:2.11.0")
    implementation("no.nordicsemi.android.support.v18:scanner:1.6.0")

    // Audio Processing
    // Note: TarsosDSP requires JitPack repository
    implementation("com.github.AtsushiSaito:TarsosDSP-Android:1.0.0")

    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    // Utilities
    implementation("com.jakewharton.timber:timber:5.0.1")
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.14")
}
```

---

## Required Android Permissions

```xml
<manifest>
    <!-- BLE Permissions -->
    <uses-permission android:name="android.permission.BLUETOOTH" android:maxSdkVersion="30"/>
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" android:maxSdkVersion="30"/>
    <uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
    <uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />

    <!-- Audio Permission (for sound reactivity) -->
    <uses-permission android:name="android.permission.RECORD_AUDIO" />

    <!-- Feature declarations -->
    <uses-feature android:name="android.hardware.bluetooth_le" android:required="true"/>
</manifest>
```

---

## Sources

### Official Documentation
- [Android Compose BOM](https://developer.android.com/develop/ui/compose/bom) - BOM version and usage
- [Android Room](https://developer.android.com/jetpack/androidx/releases/room) - Room 2.8.4 release
- [Android Navigation](https://developer.android.com/jetpack/androidx/releases/navigation) - Navigation 2.9.7
- [Jetpack Compose December '25 Release](https://android-developers.googleblog.com/2025/12/whats-new-in-jetpack-compose-december.html) - Compose 1.10

### BLE Libraries
- [Nordic Android-BLE-Library](https://github.com/NordicSemiconductor/Android-BLE-Library) - BLE Library 2.11.0
- [Nordic Kotlin-BLE-Library](https://github.com/NordicSemiconductor/Kotlin-BLE-Library) - Next-gen (still in development)
- [Android BLE Guide (Punch Through)](https://punchthrough.com/android-ble-guide/) - BLE best practices

### ELK-BLEDOM Protocol
- [FergusInLondon/ELK-BLEDOM Protocol](https://github.com/FergusInLondon/ELK-BLEDOM/blob/master/PROTCOL.md) - Protocol specification
- [dave-code-ruiz/elkbledom](https://github.com/dave-code-ruiz/elkbledom) - Home Assistant integration

### Audio Processing
- [TarsosDSP](https://github.com/JorenSix/TarsosDSP) - Audio processing framework
- [TarsosDSP Android](https://github.com/AnandaAp/TarsosDSPAndroid) - Kotlin Android port

### State Management
- [StateFlow and SharedFlow](https://developer.android.com/kotlin/flow/stateflow-and-sharedflow) - Official docs
- [Compose State Management Best Practices](https://medium.com/@hiren6997/seamless-state-management-in-jetpack-compose-the-developers-guide-to-building-bulletproof-android-e862c3de4ed3)

### Hilt DI
- [Hilt Setup 2025](https://dev.to/abdul_rehman_2050/how-to-properly-setup-hilt-in-android-jetpack-compose-project-in-2025-o56) - KSP migration guide
- [Hilt Official](https://developer.android.com/training/dependency-injection/hilt-android)
