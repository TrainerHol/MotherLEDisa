# Phase 3: Sound-Reactive Mode - Context

**Gathered:** 2026-03-28
**Status:** Ready for planning

<domain>
## Phase Boundary

Users can configure the tower's built-in sound-reactive mode with custom sensitivity settings and color palettes. The tower handles audio detection autonomously via its internal microphone — the app only sends configuration; it does not need to stay connected during sound-reactive operation.

**Critical constraint:** MELK towers are autonomous. Settings persist on the tower even after app disconnects. The app configures the tower, then the tower runs independently.

</domain>

<decisions>
## Implementation Decisions

### Audio Architecture
- **D-01:** Tower has internal microphone and handles audio detection in hardware — app does NOT capture audio from phone mic
- **D-02:** App sends configuration (effect, sensitivity, palette) to tower via BLE; tower runs sound-reactive mode autonomously
- **D-03:** App does not need to stay connected during sound-reactive operation — "fire and forget" configuration model
- **D-04:** If protocol research shows tower CANNOT store custom animations, Phase 3 scope is limited to configuring built-in sound-reactive effects with custom palette/sensitivity. Custom animation playback stays in Phase 2 (manual trigger, app-connected).

### Sound-to-Animation Mapping
- **D-05:** User can define color palette for sound mode in two ways:
  - Pick from a saved animation preset (extract its color palette)
  - Build a custom palette via dedicated picker (2-5 colors for sound mode)
- **D-06:** Research needed: Does ELK-BLEDOM protocol support uploading custom animations to tower memory for autonomous sound-triggered playback? If yes, enable. If no, fallback to D-04.

### Sensitivity Controls
- **D-07:** Claude's discretion based on protocol research — expose whatever sensitivity controls the ELK-BLEDOM protocol supports (threshold slider, gain, or both)

### Sound Effects Selection
- **D-08:** Claude's discretion based on research — expose all sound-reactive effects the protocol supports, or curate if some are redundant/low-quality

### Audio Visualization
- **D-09:** Show live audio visualization (VU meter) only if the tower sends audio level data via BLE notifications — skip if protocol doesn't support bidirectional audio data

### Screen Layout
- **D-10:** Claude's discretion — follow Phase 1/2 patterns (single scrollable screen recommended: preview at top, effect selector, sensitivity slider, palette picker)

### Navigation
- **D-11:** Claude's discretion — integrate cleanly with current bottom nav layout. Check existing screens (Devices, Control, Editor, Presets) and add Sound screen where it fits best UX-wise.

### Claude's Discretion
- Exact sensitivity UI (single slider vs. threshold + gain)
- Which built-in sound effects to expose
- VU meter implementation (if protocol supports audio data)
- Screen layout and navigation placement
- Palette picker UI (how many colors, swatch design)

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### ELK-BLEDOM Protocol
- `.planning/research/STACK.md` — Protocol UUIDs, command format, sound-reactive command bytes (research needed)
- External: [FergusInLondon/ELK-BLEDOM Protocol](https://github.com/FergusInLondon/ELK-BLEDOM/blob/master/PROTCOL.md) — Sound mode commands
- External: [dave-code-ruiz/elkbledom](https://github.com/dave-code-ruiz/elkbledom) — Home Assistant implementation for reference

### Requirements
- `.planning/REQUIREMENTS.md` — SOUND-01 through SOUND-04, UX-04

### Phase Dependencies
- `.planning/phases/01-ble-foundation-basic-control/01-CONTEXT.md` — BLE patterns, command queue, debouncing
- `.planning/phases/02-timeline-animation-presets/02-CONTEXT.md` — Animation/preset data model for palette extraction

### Project Constraints
- `.planning/PROJECT.md` — Constraint: "Audio Source: Device internal microphone only — phone mic is 'useless' per user"

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `MelkProtocol.kt` — Extend with sound-reactive effect commands
- `TowerCommandQueue.kt` — BLE command scheduling for setting sound mode
- `ControlViewModel.kt` — Pattern for settings screens
- `EffectsSection.kt` — Effect selection UI, adapt for sound effects
- `ColorPickerSection.kt` — HSV color picker with swatches, reuse for palette picker
- `AnimationRepository.kt` — Access saved presets for palette extraction
- `DevicePicker.kt` — Multi-tower targeting

### Established Patterns
- StateFlow + MVVM for reactive state
- Single scrollable screen layout (Control screen pattern)
- Fire-and-forget BLE commands (tower retains settings)
- Type-safe Navigation Compose routes

### Integration Points
- `Screen.kt` — Add `SoundReactive` route
- `NavGraph.kt` — Add navigation to sound screen
- Bottom nav — Add "Sound" tab (placement TBD)
- `MelkProtocol.kt` — Add sound mode commands based on protocol research

</code_context>

<specifics>
## Specific Ideas

- "The main appeal of this app is being able to customize the sound-reacting effects" — this is THE differentiator vs. preset-only apps
- Tower autonomy is key: user configures, then walks away. App doesn't need to run for sound reactivity to work.
- If protocol allows custom animation upload → full custom sound-triggered animations
- If protocol is limited → best-in-class configuration of built-in effects with custom palettes

</specifics>

<deferred>
## Deferred Ideas

- App-side audio capture (phone mic): Only if tower-native is impossible AND user requests it — per PROJECT.md constraint, phone mic is considered "useless"
- Frequency band mapping (bass/mids/highs to different colors): Requires app-side FFT, deferred to v2 (AUDIO-01, AUDIO-02)
- Beat detection with BPM sync: Would require app-side processing, deferred
- Multi-tower cascade sound reactions: Phase 4 (multi-tower orchestration)

</deferred>

---

*Phase: 03-sound-reactive-mode*
*Context gathered: 2026-03-28*
