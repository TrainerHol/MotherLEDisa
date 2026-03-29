# Phase 2: Timeline Animation & Presets - Context

**Gathered:** 2026-03-28
**Status:** Ready for planning

<domain>
## Phase Boundary

Users can create custom keyframe animations on a timeline and save them as reusable presets. This delivers the core creative tooling that differentiates MotherLEDisa from preset-only apps.

</domain>

<decisions>
## Implementation Decisions

### Timeline Editor Design
- **D-01:** Horizontal timeline layout (After Effects-style) — time flows left-to-right, segment tracks stacked vertically
- **D-02:** Diamond markers on tracks for keyframes, colored by the keyframe's color value
- **D-03:** Long-press on track to add new keyframe — brings up context menu with "Add keyframe" option
- **D-04:** Playhead is draggable vertical line spanning all tracks — drag to scrub with live preview
- **D-05:** One track per tower segment (5 segments = 5 horizontal tracks) — clearest visualization for customization

### Keyframe Properties
- **D-06:** Each keyframe controls: Color (RGB), Segment/position, Brightness, and optional Effect trigger
- **D-07:** User chooses interpolation mode per keyframe — smooth blend (HSV) vs step/hold
- **D-08:** HSV interpolation for smooth color transitions (red→orange→yellow→green flow)

### Animation Playback
- **D-09:** Fixed tower preview above timeline — always visible during editing
- **D-10:** Play button triggers both in-app preview AND device simultaneously for instant feedback
- **D-11:** Full loop options: loop count (once, 2x, 3x, infinite) plus ping-pong mode (A→B→A)
- **D-12:** Transport controls: Play, Pause, Stop — consistent with Phase 1 debounced command pattern (~30fps)

### Preset Library (not discussed — sensible defaults)
- **D-13:** Grid layout with animation name and visual thumbnail
- **D-14:** Single tap to preview, long-press for options (rename, delete, duplicate)
- **D-15:** Presets stored in Room database with JSON-serialized keyframe data
- **D-16:** "Apply" sends preset animation to currently selected device(s)

### Claude's Discretion
- Time ruler scale and zoom/pan gestures
- Keyframe selection UI (multi-select, lasso, etc.)
- Copy/paste keyframe functionality
- Easing curves beyond linear/step
- Preset thumbnail generation approach
- Animation duration limits (if any)

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Phase 1 Foundation
- `.planning/phases/01-ble-foundation-basic-control/01-CONTEXT.md` — D-07 preview, D-10 debouncing, D-12 device picker patterns
- `.planning/research/STACK.md` — TarsosDSP for audio, Compose Canvas for custom drawing

### Requirements
- `.planning/REQUIREMENTS.md` — ANIM-01 through ANIM-08 (timeline, keyframes, patterns), PRESET-01 through PRESET-05 (save, list, apply, delete, persist), UX-03 (timeline screen), UX-06 (preset screen)

### Project Context
- `.planning/PROJECT.md` — Core value: "frame-by-frame control over color, position, and timing"

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `ColorPickerSection.kt` — HSV color wheel with preset swatches (D-08, D-11 from Phase 1)
- `TowerPreviewCanvas.kt` — Tower visualization showing segment colors (reuse for timeline preview)
- `ControlViewModel.kt` — Debounced BLE updates pattern (~30fps brightness, 50ms color)
- `DevicePicker.kt` — Multi-device selection chip for targeting animations
- `Effect.kt` + `AllEffects` — Hardware effect definitions for effect trigger keyframes

### Established Patterns
- StateFlow + MVVM architecture for reactive state
- Type-safe Navigation Compose with @Serializable routes
- Room database with KSP code generation (AppDatabase ready for animation tables)
- Kotlinx Serialization for JSON (preset export/import)
- Compose Canvas for custom drawing (use for timeline tracks)

### Integration Points
- `Screen.kt` — Add new `AnimationEditor` and `PresetLibrary` routes
- `AppDatabase.kt` — Add `AnimationEntity` and `PresetEntity` tables
- `MelkProtocol.kt` — Extend with segment-specific commands if protocol supports
- `TowerCommandQueue.kt` — Reuse for animation playback command scheduling

</code_context>

<specifics>
## Specific Ideas

- "After Effects-style keyframe editing" — user specifically referenced AE timeline interaction model
- Segment tracks allow "different colors at different heights" — critical differentiator vs flat LED strips
- Effect triggers in timeline enable "5-color gradient + breathe → blue breathing after 3 seconds" — builds on Phase 1 full effects menu
- Scrubbing playhead should update both preview AND device — instant feedback loop

</specifics>

<deferred>
## Deferred Ideas

- Sound-reactive animation triggers — Phase 3
- Multi-tower choreography (offset/cascade timing) — Phase 4
- Pattern generators (sine waves, gradients) — could be separate tool or Phase 2 extension
- Advanced easing curves (bezier) — evaluate after basic interpolation works

None — discussion stayed within phase scope

</deferred>

---

*Phase: 02-timeline-animation-presets*
*Context gathered: 2026-03-28*
