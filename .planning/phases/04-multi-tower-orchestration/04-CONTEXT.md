# Phase 4: Multi-Tower Orchestration - Context

**Gathered:** 2026-03-28
**Status:** Ready for planning

<domain>
## Phase Boundary

Users can coordinate multiple connected towers with synchronized and choreographed animation modes: mirror (all same), offset (staggered timing), cascade (relay), and independent (per-tower control). This delivers room-scale lighting choreography.

</domain>

<decisions>
## Implementation Decisions

### Tower Ordering
- **D-01:** Drag-and-drop vertical list for tower ordering. Tap and hold to drag, familiar reorderable list pattern.
- **D-02:** Global default tower order saved in app preferences, with per-animation override option when saving presets.

### Mode Selection
- **D-03:** Horizontal segmented control for mode switching: Mirror | Offset | Cascade | Independent. Quick visual toggle.
- **D-04:** Mode selection is global across all towers when orchestrating — not per-tower setting.

### Independent Mode
- **D-05:** Dropdown per tower to select which animation/preset each tower plays. Compact, fits existing DevicePicker pattern.

### Offset Mode Timing
- **D-06:** Single delay slider (0-2000ms range) — same delay applied between each consecutive tower pair. Tower 1 starts, tower 2 starts after delay, tower 3 starts after another delay, etc.

### Cascade Mode
- **D-07:** Immediate handoff — tower 2 starts instantly when tower 1 completes its animation cycle. Clean relay effect with no overlap or gap.

### BLE Sync
- **D-08:** Accept BLE latency variance (~20-50ms between towers). No compensation logic — slight variance is acceptable for home use.

### Screen Layout
- **D-09:** Dedicated "Orchestrate" tab in bottom navigation for multi-tower settings.
- **D-10:** Tab order: Devices | Control | Orchestrate | Sound | Presets (5 tabs total).
- **D-11:** Orchestrate tab visible but disabled when <2 towers connected. Shows "Connect more towers" message.

### Claude's Discretion
- Exact slider range for offset delay
- Orchestrate tab icon choice
- Empty state design for <2 towers
- Tower preview visualization in orchestrate screen
- Whether to show live preview of offset/cascade timing

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Requirements
- `.planning/REQUIREMENTS.md` — MULTI-01 through MULTI-06, UX-05

### Phase Dependencies
- `.planning/phases/01-ble-foundation-basic-control/01-CONTEXT.md` — D-12 DevicePicker pattern, D-15 instant device switching, D-10 debounced commands
- `.planning/phases/02-timeline-animation-presets/02-CONTEXT.md` — D-12 30fps playback rate, D-15 preset data model
- `.planning/phases/03-sound-reactive-mode/03-CONTEXT.md` — D-03 fire-and-forget config model, tab navigation pattern

### Project Context
- `.planning/PROJECT.md` — Multi-tower cascade mode is a key decision, 4 towers in living room

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `TowerConnectionManager.kt` — Already has `*All()` methods (setColorAll, setBrightnessAll, etc.) for multi-tower control
- `DevicePicker.kt` — Dropdown with "All devices" option (null address pattern)
- `ConnectedTower` data class — Stores address, name, BleManager, commandQueue per tower
- `AnimationPlayer.kt` — Has `targetTower: ConnectedTower?` (null = all towers) pattern
- `NavGraph.kt` — 4-tab bottom navigation ready to extend

### Established Patterns
- StateFlow + MVVM for reactive state
- Type-safe Navigation Compose with @Serializable routes
- Single scrollable screen layout (Control, Sound patterns)
- 30fps debounced BLE commands (~33ms delay)
- Fire-and-forget command model

### Integration Points
- `Screen.kt` — Add `Orchestrate` route
- `NavGraph.kt` — Add 5th navigation tab
- `TowerConnectionManager.kt` — Add offset/cascade timing logic
- `AnimationPlayer.kt` — Extend for staggered playback
- `TowerConfigEntity.kt` — Store tower order preference

</code_context>

<specifics>
## Specific Ideas

- Cascade mode enables "tower-to-tower relay animations" — key differentiator mentioned in PROJECT.md
- User has 4 towers in living room — test with real 4-tower setup
- Offset mode creates "wave" effects across towers
- Mirror mode is simplest — same animation on all towers simultaneously

</specifics>

<deferred>
## Deferred Ideas

- Per-pair offset delays (more granular control) — evaluate after basic offset works
- Overlap/gap options for cascade mode — could add later if requested
- Visual room layout for tower positioning — complex UI, defer to v2
- BLE latency compensation — accept variance for now

None — discussion stayed within phase scope

</deferred>

---

*Phase: 04-multi-tower-orchestration*
*Context gathered: 2026-03-28*
