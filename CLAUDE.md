<!-- GSD:project-start source:PROJECT.md -->
## Project

**MotherLEDisa**

An Android app for deep customization of MELK-OT21 Bluetooth tower lights. Unlike existing apps that only offer preset modes, MotherLEDisa provides a timeline-based animation editor (After Effects-style) for creating custom light patterns, sound-reactive visualizations with custom color palettes, and multi-tower orchestration for room-scale lighting choreography.

**Core Value:** Users can create and save their own custom LED animations with frame-by-frame control over color, position, and timing — not just pick from presets.

### Constraints

- **Platform**: Android (Kotlin) — native BLE required for low-latency control
- **Audio Source**: Device internal microphone only — phone mic is "useless" per user
- **Protocol**: Must work with MELK-OT21 BLE characteristics (reverse engineer or extend elk-led-controller approach)
- **Device Count**: Support unlimited towers, tested with 4
<!-- GSD:project-end -->

<!-- GSD:stack-start source:research/STACK.md -->
## Technology Stack

## Executive Summary
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
### Core Framework
| Technology | Version | Purpose | Rationale |
|------------|---------|---------|-----------|
| Jetpack Compose | BOM 2025.12.00 | UI Framework | December '25 release is stable with 1.10 core modules; pausable composition provides performance parity with Views; native animation APIs for timeline editor |
| Material 3 | 1.4.0 (via BOM) | Design System | Included in BOM; modern theming, adaptive layouts |
| Navigation Compose | 2.9.7 | Screen Navigation | Type-safe routes (stable since 2.8.0); compile-time safety with @Serializable routes |
### Bluetooth Low Energy
| Technology | Version | Purpose | Rationale |
|------------|---------|---------|-----------|
| Nordic Android-BLE-Library | 2.11.0 | BLE Connection & Communication | Industry standard; handles connection retries, queuing, MTU negotiation, error recovery. Used by thousands of production apps. Kotlin coroutines support via `.suspend()` and `.asFlow()` |
| Nordic Android Scanner Compat | 1.6.0 | BLE Device Discovery | Recommended companion to BLE Library; backports scanning features to older Android versions |
- **BLESSED-Kotlin** - Good library but smaller community; Nordic has better documentation and enterprise support
- **Kable (Kotlin Multiplatform)** - Overkill for Android-only app; adds KMP complexity we don't need
- **Raw Android BLE APIs** - Callback hell, manual queue management, many edge cases to handle
### ELK-BLEDOM Protocol Details
| Constant | Value | Notes |
|----------|-------|-------|
| Service UUID | `0000fff0-0000-1000-8000-00805f9b34fb` | Primary service |
| Characteristic UUID | `0000fff3-0000-1000-8000-00805f9b34fb` | Write commands here |
| Command Start Byte | `0x7E` | All commands start with this |
| Command End Byte | `0xEF` | All commands end with this |
| Command Length | 9 bytes | Fixed length |
### Audio Processing (Sound Reactivity)
| Technology | Version | Purpose | Rationale |
|------------|---------|---------|-----------|
| TarsosDSP | 2.4 | FFT & Audio Analysis | Pure Java, no native dependencies, out-of-box Android support. Includes FFT, onset detection, pitch detection. Proven in thousands of music apps |
| Android AudioRecord | Native | Microphone Capture | Standard Android API for raw audio capture |
- Zero external dependencies (critical for Android)
- Real-time FFT with spectral peak extraction
- Supports sampling rates up to device maximum
- Active development, academic backing
- **Superpowered SDK** - Commercial, overkill for this use case
- **Oboe** - C++ library for low-latency audio OUTPUT, not analysis
- **Processing/Minim** - Desktop-focused, Android support is hacky
### State Management & Architecture
| Technology | Version | Purpose | Rationale |
|------------|---------|---------|-----------|
| ViewModel | 2.9.0 (lifecycle) | Screen State | Survives config changes; SavedStateHandle for process death |
| StateFlow | kotlinx.coroutines 1.9.0 | Reactive State | Hot stream, Compose-native, better than LiveData |
| Hilt | 2.56 | Dependency Injection | Official Android DI; KSP support for faster builds |
| AndroidX Hilt | 1.2.0 | Compose Integration | hiltViewModel() for ViewModel injection |
- State flows DOWN from ViewModel to Composables
- Events flow UP from Composables to ViewModel
- Single source of truth per screen
### Database (Preset Storage)
| Technology | Version | Purpose | Rationale |
|------------|---------|---------|-----------|
| Room | 2.8.4 | Local Database | Official Android persistence; KSP for code generation; Kotlin coroutines with room-ktx |
- Animation presets (name, keyframes, metadata)
- Device profiles (MAC address, name, tower position)
- User preferences
### Data Serialization
| Technology | Version | Purpose | Rationale |
|------------|---------|---------|-----------|
| Kotlinx Serialization | 1.10.0 | JSON Encoding/Decoding | Native Kotlin, no reflection, compile-time safety. Use for animation preset export/import |
### Timeline Animation Editor (Custom UI)
| Component | Compose API | Purpose |
|-----------|-------------|---------|
| Timeline Track | `Canvas` + `Modifier.horizontalScroll` | Draw keyframes, playhead, time ruler |
| Keyframe Dragging | `Modifier.pointerInput` + `detectDragGestures` | Move keyframes |
| Zoom/Pan | `transformable` modifier | Pinch to zoom timeline |
| Animations | `Animatable` + `keyframes` spec | Preview animations |
| Color Picker | Material 3 or custom Canvas | Keyframe color values |
- [JetLime](https://github.com/pushpalroy/JetLime) - Timeline view concepts
- [composeCanvas](https://github.com/nameisjayant/composeCanvas) - Custom Compose drawing examples
### Supporting Libraries
| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| Coil | 3.0.4 | Image Loading | Preset thumbnails (if needed) |
| Timber | 5.0.1 | Logging | Debug logging with tags |
| LeakCanary | 2.14 | Memory Leak Detection | Debug builds only |
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
## Complete build.gradle.kts (App Module)
## Required Android Permissions
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
<!-- GSD:stack-end -->

<!-- GSD:conventions-start source:CONVENTIONS.md -->
## Conventions

Conventions not yet established. Will populate as patterns emerge during development.
<!-- GSD:conventions-end -->

<!-- GSD:architecture-start source:ARCHITECTURE.md -->
## Architecture

Architecture not yet mapped. Follow existing patterns found in the codebase.
<!-- GSD:architecture-end -->

<!-- GSD:workflow-start source:GSD defaults -->
## GSD Workflow Enforcement

Before using Edit, Write, or other file-changing tools, start work through a GSD command so planning artifacts and execution context stay in sync.

Use these entry points:
- `/gsd:quick` for small fixes, doc updates, and ad-hoc tasks
- `/gsd:debug` for investigation and bug fixing
- `/gsd:execute-phase` for planned phase work

Do not make direct repo edits outside a GSD workflow unless the user explicitly asks to bypass it.
<!-- GSD:workflow-end -->



<!-- GSD:profile-start -->
## Developer Profile

> Profile not yet configured. Run `/gsd:profile-user` to generate your developer profile.
> This section is managed by `generate-claude-profile` -- do not edit manually.
<!-- GSD:profile-end -->
