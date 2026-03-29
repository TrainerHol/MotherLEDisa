# Requirements: MotherLEDisa

**Defined:** 2025-03-25
**Core Value:** Users can create and save their own custom LED animations with frame-by-frame control over color, position, and timing — not just pick from presets.

## v1 Requirements

Requirements for initial release. Each maps to roadmap phases.

### BLE Connection

- [x] **BLE-01**: User can scan for nearby MELK/ELK/LEDBLE devices
- [x] **BLE-02**: User can connect to a discovered device
- [x] **BLE-03**: User can disconnect from a connected device
- [x] **BLE-04**: App auto-reconnects when connection drops unexpectedly
- [x] **BLE-05**: User can see list of connected devices
- [x] **BLE-06**: User can switch between multiple connected devices

### Basic Control

- [x] **CTRL-01**: User can turn tower on/off with single tap
- [x] **CTRL-02**: User can select any RGB color via color wheel
- [x] **CTRL-03**: User can adjust brightness via slider with real-time preview
- [x] **CTRL-04**: User can select from built-in hardware effects (fade, jump, blink, breathing)
- [x] **CTRL-05**: User can adjust effect speed

### Timeline Animation

- [x] **ANIM-01**: User can create animation with keyframes on a timeline
- [x] **ANIM-02**: User can set color for each keyframe
- [ ] **ANIM-03**: User can set position/segment for each keyframe (different heights = different colors)
- [ ] **ANIM-04**: User can drag keyframes to adjust timing
- [x] **ANIM-05**: User can preview animation in app before sending to device
- [x] **ANIM-06**: User can play animation on connected tower(s)
- [x] **ANIM-07**: User can pause and stop animation playback
- [x] **ANIM-08**: User can create patterns (sine waves, gradients) applied to segments

### Sound Reactive

- [x] **SOUND-01**: User can enable sound-reactive mode using device's internal microphone
- [x] **SOUND-02**: User can adjust sound threshold (sensitivity)
- [x] **SOUND-03**: User can assign color palette for sound triggers
- [ ] **SOUND-04**: User can trigger custom animations from sound (not just presets)

### Multi-Tower

- [ ] **MULTI-01**: User can control multiple towers simultaneously
- [ ] **MULTI-02**: User can enable mirror mode (all towers show same animation)
- [ ] **MULTI-03**: User can enable offset mode (staggered timing across towers)
- [ ] **MULTI-04**: User can enable cascade mode (when one tower finishes, next starts)
- [ ] **MULTI-05**: User can define tower ordering for offset/cascade modes
- [ ] **MULTI-06**: User can enable independent mode (each tower controlled separately)

### Preset Library

- [x] **PRESET-01**: User can save current animation as named preset
- [x] **PRESET-02**: User can view list of saved presets
- [x] **PRESET-03**: User can apply saved preset to connected tower(s)
- [x] **PRESET-04**: User can delete saved presets
- [x] **PRESET-05**: Presets persist across app restarts

### User Experience

- [x] **UX-01**: App has dedicated screen for device discovery and connection
- [x] **UX-02**: App has dedicated screen for basic controls (power, color, brightness)
- [x] **UX-03**: App has dedicated screen for timeline animation editor
- [ ] **UX-04**: App has dedicated screen for sound-reactive configuration
- [ ] **UX-05**: App has dedicated screen for multi-tower orchestration
- [x] **UX-06**: App has dedicated screen for preset library
- [x] **UX-07**: Navigation between screens is intuitive and consistent
- [x] **UX-08**: Real-time preview shows current tower state

## v2 Requirements

Deferred to future release. Tracked but not in current roadmap.

### Background Operation

- **BG-01**: App maintains BLE connections when backgrounded
- **BG-02**: Sound-reactive mode continues when screen is off

### Advanced Audio

- **AUDIO-01**: User can map frequency bands (bass/mids/highs) to different animation parameters
- **AUDIO-02**: User can see frequency spectrum visualization

### Scheduling

- **SCHED-01**: User can schedule presets to activate at specific times
- **SCHED-02**: User can schedule power on/off

## Out of Scope

Explicitly excluded. Documented to prevent scope creep.

| Feature | Reason |
|---------|--------|
| iOS version | Android-first; defer to v2 if successful |
| Cloud sync | Local-only for v1; no backend complexity |
| Social features/sharing | Not core use case; adds complexity |
| Smart home integration (HomeKit, Alexa) | Massive complexity for narrow use case |
| Phone microphone for sound | User stated phone mic is "useless" — device mic only |
| Account/login | No need for BLE-only app |
| Subscriptions/paywalls | User wants free app; no monetization gates |
| Ads | Focus on UX; no ad interruptions |

## Traceability

Which phases cover which requirements. Updated during roadmap creation.

| Requirement | Phase | Status |
|-------------|-------|--------|
| BLE-01 | Phase 1 | Complete |
| BLE-02 | Phase 1 | Complete |
| BLE-03 | Phase 1 | Complete |
| BLE-04 | Phase 1 | Complete |
| BLE-05 | Phase 1 | Complete |
| BLE-06 | Phase 1 | Complete |
| CTRL-01 | Phase 1 | Complete |
| CTRL-02 | Phase 1 | Complete |
| CTRL-03 | Phase 1 | Complete |
| CTRL-04 | Phase 1 | Complete |
| CTRL-05 | Phase 1 | Complete |
| ANIM-01 | Phase 2 | Complete |
| ANIM-02 | Phase 2 | Complete |
| ANIM-03 | Phase 2 | Pending |
| ANIM-04 | Phase 2 | Pending |
| ANIM-05 | Phase 2 | Complete |
| ANIM-06 | Phase 2 | Complete |
| ANIM-07 | Phase 2 | Complete |
| ANIM-08 | v2 | Deferred |
| SOUND-01 | Phase 3 | Complete |
| SOUND-02 | Phase 3 | Complete |
| SOUND-03 | Phase 3 | Complete |
| SOUND-04 | Phase 3 | Pending |
| MULTI-01 | Phase 4 | Pending |
| MULTI-02 | Phase 4 | Pending |
| MULTI-03 | Phase 4 | Pending |
| MULTI-04 | Phase 4 | Pending |
| MULTI-05 | Phase 4 | Pending |
| MULTI-06 | Phase 4 | Pending |
| PRESET-01 | Phase 2 | Complete |
| PRESET-02 | Phase 2 | Complete |
| PRESET-03 | Phase 2 | Complete |
| PRESET-04 | Phase 2 | Complete |
| PRESET-05 | Phase 2 | Complete |
| UX-01 | Phase 1 | Complete |
| UX-02 | Phase 1 | Complete |
| UX-03 | Phase 2 | Complete |
| UX-04 | Phase 3 | Pending |
| UX-05 | Phase 4 | Pending |
| UX-06 | Phase 2 | Complete |
| UX-07 | Phase 1 | Complete |
| UX-08 | Phase 1 | Complete |

**Coverage:**
- v1 requirements: 42 total
- Mapped to phases: 42
- Unmapped: 0

---
*Requirements defined: 2025-03-25*
*Last updated: 2025-03-25 after roadmap creation*
