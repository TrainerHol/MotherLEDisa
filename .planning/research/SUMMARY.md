# Project Research Summary

**Project:** MotherLEDisa - Android BLE LED Tower Light Controller
**Domain:** Android BLE hardware control with real-time audio processing and custom animation
**Researched:** 2026-03-25
**Confidence:** HIGH

## Executive Summary

MotherLEDisa is an Android app for controlling MELK-OT21 Bluetooth tower lights with timeline-based animation editing and sound-reactive features. The domain is well-understood: Android BLE development has mature tooling (Nordic BLE Library), the ELK-BLEDOM protocol is reverse-engineered and documented, and Jetpack Compose provides excellent Canvas APIs for custom timeline editors. The recommended approach is a clean MVVM architecture with a foreground service for BLE operations, Room for preset persistence, and TarsosDSP for audio FFT analysis.

The key architectural decision is building BLE operations with an operation queue from day one. Android BLE's callback-based API cannot handle concurrent operations, and attempting rapid-fire commands (which animation playback requires) causes GATT_ERROR 133 and silent failures. The Nordic Android-BLE-Library handles this internally, making it the clear choice over raw Android APIs or smaller libraries.

The primary risks are: (1) background BLE killed by Doze mode on Samsung/Xiaomi devices, mitigated with foreground service architecture; (2) audio latency in sound-reactive mode, requiring careful FFT buffer sizing; and (3) multi-tower clock drift during choreographed animations, requiring per-device latency measurement and compensated scheduling. None are showstoppers, but all require upfront architectural decisions rather than retrofits.

## Key Findings

### Recommended Stack

The stack is modern Android 2025: Kotlin 2.1.20, Jetpack Compose (BOM 2025.12.00), compileSdk 35, minSdk 29. The BLE layer uses Nordic Android-BLE-Library 2.11.0 with coroutines support. Audio processing uses TarsosDSP 2.4 for FFT analysis. State management follows MVVM with StateFlow, Hilt for DI, and Room 2.8.4 for persistence.

**Core technologies:**
- **Nordic BLE Library (2.11.0):** BLE connection and command dispatch - handles queuing, retries, and multi-device management
- **Jetpack Compose + Canvas:** UI framework and custom timeline editor - native animation APIs ideal for keyframe visualization
- **TarsosDSP (2.4):** Audio FFT analysis - pure Java, zero native dependencies, proven in music apps
- **Room (2.8.4):** Preset and animation persistence - official Android persistence with Flow support
- **Hilt (2.56):** Dependency injection - compile-time safety with KSP

### Expected Features

**Must have (table stakes):**
- BLE device discovery and connection with auto-reconnect
- Power on/off, color picker, brightness control
- Built-in effect presets with speed control
- Save/recall user presets (fix competitor's persistence bugs)
- Multiple device support with easy switching

**Should have (competitive differentiation):**
- Timeline-based animation editor (After Effects-style keyframes) - core differentiator
- Animation preview before sending to hardware
- Sound-reactive mode with custom animation triggers (not just preset mapping)
- Multi-tower orchestration: mirror, offset, cascade modes
- Per-segment addressable control (if protocol supports)

**Defer (v2+):**
- Import animation from image/video
- BPM auto-detection
- Cloud sync
- iOS version

### Architecture Approach

The architecture follows Clean Architecture with three layers: Presentation (Compose UI + ViewModels), Domain (Use Cases + Entities), and Data (BLE Service + Room + Audio Engine). The BLE layer runs in a foreground service for background reliability. Animation playback uses a frame-rate-limited interpolation loop (~60Hz for preview, ~30Hz for BLE dispatch). Multi-tower coordination is handled by a TowerCoordinator that supports Mirror, Offset, Cascade, and Independent modes.

**Major components:**
1. **BLE Connection Service** - foreground service managing device connections via Nordic library
2. **TowerCommandQueue** - serializes BLE operations per device, prevents GATT errors
3. **Animation Engine** - keyframe interpolation at 60Hz, dispatches to BLE at 30Hz
4. **Audio Capture Engine** - microphone input with FFT via TarsosDSP, outputs frequency bands
5. **Tower Coordinator** - multi-device orchestration with timing compensation

### Critical Pitfalls

1. **BLE operations without queuing** - Android BLE cannot handle concurrent operations; use Nordic library's built-in queue or implement explicit command queue. Build this into Phase 1 architecture.

2. **MELK device initialization missing** - MELK-OT21 devices require init commands after connection before accepting control commands. Detect device type from name prefix and apply device-specific initialization.

3. **Background BLE killed by Doze** - requires foreground service with `connectedDevice` type, battery optimization exemption request, and OEM-specific guidance (Samsung/Xiaomi). Critical for sound-reactive mode.

4. **Audio processing latency** - TarsosDSP on Kotlin is adequate but not optimal. Use 512-1024 sample FFT windows for 10-25ms latency. Accept 25-45ms total latency (audio + FFT + BLE).

5. **Multi-tower clock drift** - compensate with per-device latency measurement and pre-calculated send times. Use Choreographer-synced animation loops for consistent timing.

## Implications for Roadmap

Based on research, suggested phase structure:

### Phase 1: BLE Foundation
**Rationale:** Everything depends on reliable BLE connectivity. Build the hardest part (operations queue, device detection, foreground service) first.
**Delivers:** Scannable, connectable tower control with basic commands (power, color, brightness)
**Addresses:** BLE discovery, power control, color picker, brightness control from FEATURES.md
**Avoids:** Pitfalls 1 (queue), 2 (MELK init), 3 (background service), 11 (hardcoded names), 12 (permissions), 13 (UUID confusion)
**Stack:** Nordic BLE Library 2.11.0, Foreground Service, Hilt DI, basic Compose UI

### Phase 2: Persistence and Presets
**Rationale:** Users need to save settings immediately after basic control works. This also validates Room setup before complex animation storage.
**Delivers:** Save/load color configurations, device list persistence, basic preset system
**Uses:** Room 2.8.4, Kotlinx Serialization 1.10.0
**Implements:** Persistence Module from architecture
**Addresses:** Save/recall presets, multiple device support from FEATURES.md

### Phase 3: Timeline Animation Editor
**Rationale:** Core differentiator. Depends on Phase 1 (BLE dispatch) and Phase 2 (preset storage). This is the technically novel work.
**Delivers:** Keyframe-based animation creation, playback with preview, animation storage
**Uses:** Compose Canvas, Animatable APIs, Room for animation storage
**Implements:** Animation Module, Timeline Editor from architecture
**Avoids:** Pitfalls 7 (connection interval), 9 (write type), 14 (frame rate mismatch)
**Addresses:** Timeline editor, keyframe animation, animation preview from FEATURES.md

### Phase 4: Sound-Reactive Mode
**Rationale:** Complex feature that needs stable animation infrastructure. Defer until playback is solid.
**Delivers:** Microphone capture, FFT analysis, frequency-to-animation mapping
**Uses:** TarsosDSP, AudioRecord API
**Implements:** Audio Module from architecture
**Avoids:** Pitfalls 4 (audio latency), 10 (FFT size)
**Addresses:** Sound-reactive mode, threshold controls, custom color palettes from FEATURES.md

### Phase 5: Multi-Tower Orchestration
**Rationale:** Most complex coordination logic. Needs all prior phases working with single tower.
**Delivers:** Mirror mode, offset mode, cascade mode, tower ordering
**Uses:** TowerCoordinator from architecture
**Avoids:** Pitfall 5 (clock drift)
**Addresses:** Multi-tower modes, user-defined ordering from FEATURES.md

### Phase Ordering Rationale

- **BLE first** because every feature depends on device communication. Attempting animation or sound features on broken BLE is wasted work.
- **Persistence second** because preset saving is both high-value (competitor weakness) and validates storage patterns needed for animations.
- **Timeline third** because it is the core differentiator and requires extended development time. Deferring risks shipping without the key feature.
- **Sound-reactive fourth** because it depends on animation playback infrastructure and is complex enough to be its own phase.
- **Multi-tower last** because it requires all prior features working reliably with single tower first. Multi-device bugs are exponentially harder to debug.

### Research Flags

Phases likely needing deeper research during planning:
- **Phase 3 (Timeline Editor):** No off-the-shelf Compose timeline libraries exist. Requires custom Canvas implementation. Research After Effects UI patterns, Compose gesture handling, keyframe interpolation algorithms.
- **Phase 4 (Sound-Reactive):** Audio latency optimization is device-dependent. May need to evaluate NDK/Oboe if TarsosDSP latency is unacceptable. Research frequency-to-color mapping algorithms.
- **Phase 5 (Multi-Tower):** BLE time synchronization is a known hard problem. Research Nordic's guidance on multi-device coordination.

Phases with standard patterns (skip research-phase):
- **Phase 1 (BLE Foundation):** Nordic BLE Library is well-documented with extensive examples. ELK-BLEDOM protocol is fully reverse-engineered.
- **Phase 2 (Persistence):** Room is standard Android. Kotlinx Serialization is straightforward.

## Confidence Assessment

| Area | Confidence | Notes |
|------|------------|-------|
| Stack | HIGH | All versions verified against official Android docs (Dec 2025), Nordic releases, and stable library versions |
| Features | HIGH | Based on competitor analysis, user complaints, and established patterns in WLED/Govee ecosystem |
| Architecture | HIGH | MVVM + Clean Architecture is industry standard; BLE patterns from Punch Through guides |
| Pitfalls | HIGH | Verified across multiple authoritative sources; BLE pitfalls from Nordic DevZone, Punch Through, Android issue tracker |

**Overall confidence:** HIGH

### Gaps to Address

- **MELK-OT21 segment protocol:** Research confirms ELK-BLEDOM full-tower control works, but per-segment addressable control may require additional protocol reverse-engineering. Validate during Phase 1.
- **TarsosDSP Android availability:** Main repo shows JitPack distribution. Verify build configuration works during Phase 4 setup. May need to fork/build from source.
- **Oboe/NDK for audio:** Current recommendation is TarsosDSP (pure Java). If latency is unacceptable (>50ms perceived), Phase 4 may need NDK work. Flag for validation during implementation.
- **OEM-specific background restrictions:** Samsung/Xiaomi/Huawei have varying requirements. Build detection logic but validate with real devices.

## Sources

### Primary (HIGH confidence)
- [Android Developers - Compose BOM](https://developer.android.com/develop/ui/compose/bom) - BOM 2025.12.00 verification
- [Nordic Android-BLE-Library](https://github.com/NordicSemiconductor/Android-BLE-Library) - BLE Library 2.11.0
- [Punch Through - Android BLE Guide](https://punchthrough.com/android-ble-guide/) - BLE operation patterns
- [Punch Through - BLE Operation Queue](https://punchthrough.com/android-ble-operation-queue/) - Command queue pattern
- [Android NDK - Audio Latency](https://developer.android.com/ndk/guides/audio/audio-latency) - Audio latency guidance

### Secondary (MEDIUM confidence)
- [ELK-BLEDOM Protocol](https://github.com/FergusInLondon/ELK-BLEDOM/blob/master/PROTCOL.md) - 9-byte command format
- [elkbledom Home Assistant](https://github.com/dave-code-ruiz/elkbledom) - MELK initialization sequence
- [TarsosDSP](https://github.com/JorenSix/TarsosDSP) - Audio FFT library
- [elk-led-controller](https://github.com/b1scoito/elk-led-controller) - Reference protocol implementation

### Tertiary (LOW confidence)
- User reviews on Google Play (Elkotrol, HappyLighting) - Competitor weakness identification
- Stack Overflow threads on multi-device BLE - Community patterns for connection management

---
*Research completed: 2026-03-25*
*Ready for roadmap: yes*
