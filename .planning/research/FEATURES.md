# Feature Landscape

**Domain:** Android BLE LED tower light controller app
**Researched:** 2025-03-25
**Confidence:** HIGH (based on competitor analysis, user complaints, and established patterns in WLED/Govee ecosystem)

## Table Stakes

Features users expect from any LED controller app. Missing these = product feels incomplete or broken.

| Feature | Why Expected | Complexity | Notes |
|---------|--------------|------------|-------|
| BLE device discovery and connection | Users expect "just works" pairing without manual setup | Medium | Must handle ELK-BLEDOM's no-security BLE protocol; auto-reconnect essential |
| Power on/off control | Most basic function; IR remotes do this | Low | Single tap, not buried in menus |
| Color picker (wheel or palette) | All competitor apps have this; 16M colors standard | Low | Visual color wheel preferred over sliders |
| Brightness control | Universal expectation; users adjust for time of day | Low | Slider with real-time preview |
| Built-in effect presets | ELK-BLEDOM hardware has built-in effects; users expect access | Low | Jump, fade, blink, breathing - these are hardware-native |
| Effect speed control | Competitor apps include this; tied to hardware capability | Low | Slider for hardware effect speed |
| Save/recall user presets | Users want to save their setup without reconfiguring | Medium | Persistent storage; survives app restart (common complaint in Elkotrol) |
| Offline operation | BLE-only apps must work without internet | Low | No account required, no cloud dependency |
| Multiple device support | Users often have 2+ strips/towers | Medium | Device list, easy switching |
| Scheduling/timers | "Turn off at midnight" is common use case | Medium | Elkotrol users complain timers don't work reliably |

## Differentiators

Features that set MotherLEDisa apart. Not expected, but highly valued when present.

| Feature | Value Proposition | Complexity | Notes |
|---------|-------------------|------------|-------|
| **Timeline-based animation editor** | "After Effects for LEDs" - frame-by-frame control over color/position/timing. No competitor offers this. | High | Core differentiator per PROJECT.md. Keyframe interpolation, layer-based editing |
| **Addressable segment control** | Different colors at different heights on tower. WLED does this, but not consumer BLE apps. | High | Must reverse-engineer per-segment protocol if available; fallback to full-tower color |
| **Sound-reactive with custom animations** | Existing apps: sound -> preset. MotherLEDisa: sound -> user-created animation with custom triggers | High | Use device mic, not phone mic. Map frequency bands to animation parameters |
| **Multi-tower orchestration modes** | Mirror (all same), offset (staggered timing), cascade (relay) | High | Requires precise timing synchronization across BLE connections |
| **Pattern designer** | Sine waves, gradients, custom shapes applied to tower segments | Medium | Mathematical pattern generation, not just static colors |
| **Animation preview** | See animation before sending to hardware | Medium | Reduces trial-and-error; software simulation of tower |
| **Import from image/video** | Extract color palette from photo (Govee has this) | Medium | Nice-to-have; Govee's AI features popular |
| **User-defined tower ordering** | Specify which tower is "first" in cascade/offset modes | Low | Simple configuration, high value for multi-tower users |
| **BPM sync mode** | Auto-detect music tempo, sync animation playback to beat | Medium | LedFx and professional tools have this |
| **Frequency band mapping** | Map bass/mids/highs to different animation parameters | Medium | Beyond simple "pulse on sound" |

## Anti-Features

Features to explicitly NOT build. These actively harm the product.

| Anti-Feature | Why Avoid | What to Do Instead |
|--------------|-----------|-------------------|
| Subscription/paywall for basic features | #1 user complaint in LED app reviews; users paying $70/year for what a $5 IR remote does | Free app, no subscriptions. Monetize via hardware or donations if needed |
| Account requirement | Forces unnecessary friction; BLE apps don't need accounts | Local-only storage, no signup |
| Cloud dependency | Adds latency, privacy concerns, breaks when servers down | BLE-direct control only |
| Aggressive upsells/gates before use | Users hate being asked to rate app, upgrade, or survey before they can use it | App opens to device list, ready to use |
| Ads that block core functionality | Banner ads at bottom acceptable; interstitials blocking control are not | No ads in control interface |
| "Smart home ecosystem" lock-in | Matter/HomeKit/Alexa integration adds massive complexity for narrow use case | Out of scope per PROJECT.md; focus on direct control |
| Phone microphone for sound reactivity | User explicitly stated phone mic is "useless" for room audio | Use device's internal microphone only |
| iOS version in v1 | Splitting focus dilutes quality; Android first | Defer to v2 if successful |
| Social features/sharing | Not the use case; adds backend complexity | Local presets only |
| Complex onboarding wizard | Users want to connect and control immediately | Scan -> connect -> control in 3 taps |

## Feature Dependencies

```
BLE Connection
    |
    +-- Power Control
    +-- Color Control --> Color Picker
    +-- Brightness Control
    +-- Effect Presets --> Effect Speed
    +-- Segment Control (if protocol supports)
    |       |
    |       +-- Pattern Designer
    |       +-- Timeline Editor --> Keyframe System
    |                                   |
    +-- Multiple Devices                +-- Animation Preview
    |       |
    |       +-- Multi-Tower Orchestration
    |               |
    |               +-- Mirror Mode
    |               +-- Offset Mode
    |               +-- Cascade Mode
    |               +-- User-defined Ordering
    |
    +-- Preset System
    |       |
    |       +-- Save/Recall
    |       +-- Scheduling/Timers
    |
    +-- Sound Reactivity (requires device mic)
            |
            +-- Threshold Controls
            +-- Frequency Band Mapping
            +-- BPM Detection
            +-- Custom Animation Triggers
```

## MVP Recommendation

**Phase 1 - Table Stakes (Must Ship):**
1. BLE discovery and connection (ELK-BLEDOM/MELK protocol)
2. Power, color, brightness control
3. Built-in effect presets + speed control
4. Save/recall user presets (fix the persistence bug competitors have)
5. Multiple device support with easy switching

**Phase 2 - Core Differentiators:**
1. Timeline-based animation editor (minimal viable: 1 tower, basic keyframes)
2. Animation preview before sending
3. Segment control (if protocol allows, otherwise full-tower animations)

**Phase 3 - Sound Reactivity:**
1. Device microphone integration
2. Threshold controls
3. Sound -> custom animation mapping (the killer feature)
4. Frequency band visualization

**Phase 4 - Multi-Tower Orchestration:**
1. Multi-tower simultaneous control
2. Mirror mode
3. Offset/cascade modes
4. User-defined tower ordering

**Defer to Later:**
- Import from image/video (nice-to-have)
- BPM auto-detection (complex audio analysis)
- Cloud sync (v2 if users request)
- Additional platforms (iOS, desktop)

## Competitive Landscape Summary

| App | Strengths | Weaknesses | Gap MotherLEDisa Fills |
|-----|-----------|------------|------------------------|
| **Elkotrol** | Simple, reliable, free, ad-supported | No custom animations, settings don't persist, no multi-color patterns | Full animation editor, persistent settings |
| **HappyLighting** | Music sync built-in | Only preset-based, no custom control | Custom sound-reactive animations |
| **Magic Home Pro** | Smart home integration | Requires WiFi, complex setup | BLE simplicity, offline-first |
| **Govee Home** | AI features, GIF import, multi-layer editing | Locked to Govee hardware, expensive ecosystem | Hardware-agnostic, open protocol |
| **WLED** | Segments, presets, effects, 250 presets | Requires ESP8266/32 hardware, not for consumer BLE strips | Works with existing MELK hardware |
| **LedFx** | Professional music visualization | Desktop-only, requires network setup | Mobile-native, BLE-direct |

## Sources

**LED Controller Apps:**
- [Elkotrol on Google Play](https://play.google.com/store/apps/details?id=com.leet.elkotrol&hl=en)
- [XDA Forums - Elkotrol Discussion](https://xdaforums.com/t/app-elkotrol-elk-bledom-bluetooth-led-strip-control-app.4597905/)
- [LED BLE on Softonic](https://led-ble.en.softonic.com/android)

**Feature Research:**
- [Ankeral - LED Strip Light App Guide 2025](https://www.ankeral.com/led-strip-light-app-smart-control-guide/)
- [Govee Home App](https://us.govee.com/pages/govee-home-app)
- [WLED Project - Segments](https://kno.wled.ge/features/segments/)
- [WLED Project - Presets](https://kno.wled.ge/features/presets/)

**Sound Reactive:**
- [LedFx - LED Music Visualiser](https://www.ledfx.app/)
- [Audio Reactive LED Strip (GitHub)](https://github.com/scottlawsonbc/audio-reactive-led-strip)

**User Complaints:**
- [JustUseApp - LED Light Controller Reviews](https://justuseapp.com/en/app/1587679090/led-light-controller-remote/reviews)
- [Best Control Apps for ELK-BLEDOM](https://elk-bledom.com/best-control-apps-for-elk-bledom-led-strips/)

**UX Best Practices:**
- [LED Professional - UI/UX for IoT Lighting Controls](https://www.led-professional.com/resources-1/articles/enhancing-the-led-experience-ui-ux-as-success-factors-for-iot-based-lighting-controls-1)

**Reference Implementation:**
- [elk-led-controller (GitHub)](https://github.com/b1scoito/elk-led-controller)
