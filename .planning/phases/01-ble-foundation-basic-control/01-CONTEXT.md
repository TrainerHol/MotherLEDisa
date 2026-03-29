# Phase 1: BLE Foundation & Basic Control - Context

**Gathered:** 2025-03-25
**Status:** Ready for planning

<domain>
## Phase Boundary

Users can discover, connect to, and control individual LED towers with basic commands (power, color, brightness, built-in effects). This establishes the BLE foundation that all later phases build upon.

</domain>

<decisions>
## Implementation Decisions

### Device Discovery UX
- **D-01:** Continuous scanning while on device list screen. No pull-to-refresh or timeout — always showing live nearby devices.
- **D-02:** List items show: device name (e.g., MELK-12AB34), signal strength bars, connected/available badge.
- **D-03:** Single tap on device connects immediately with loading indicator.
- **D-04:** Empty state shows simple text: "No devices found. Tap to scan again." — no illustrations.
- **D-05:** Known devices (previously connected) appear in same list with "Last connected" badge, merged with scan results.

### Control Layout
- **D-06:** Single scrollable screen for all controls: preview at top, then power toggle, color picker, brightness slider, effects section below.
- **D-07:** Real-time preview is a vertical tower visualization showing actual segment colors, updating live.
- **D-08:** Color picker is circular wheel for hue with saturation/brightness slider.
- **D-09:** Power toggle is large, prominent on/off button at top of control area with clear state indication.
- **D-10:** Brightness slider sends commands continuously as user drags, debounced to ~30fps for real-time feedback.
- **D-11:** Row of 8-10 quick-access preset color swatches below color wheel (red, orange, yellow, green, cyan, blue, purple, white).
- **D-12:** Device picker chip/dropdown at top when multiple devices connected. "All" option applies controls to all devices.

### Connection Behavior
- **D-13:** Auto-reconnect is persistent: keep trying to reconnect whenever device is visible. No retry limit.
- **D-14:** Foreground service is minimal — small persistent notification showing connection count. Background operation is not a priority; app is primarily active-use.
- **D-15:** Switching devices via picker is instant. Both devices remain connected; only control focus changes.
- **D-16:** Users can rename devices (long-press or edit icon) to set custom display names like "Desk Tower".
- **D-17:** BLE permission handling shows in-context explanation dialog explaining why Bluetooth/Location needed, with "Grant" button to settings.

### Built-in Effects
- **D-18:** Full hardware effects menu exposed (~20+ effects from ELK-BLEDOM protocol), not just core 4.
- **D-19:** Effects displayed as scrollable vertical list with effect name + small icon per row. Tap to activate.
- **D-20:** Speed slider appears below effects list when any effect is active. Universal speed control.
- **D-21:** Color wheel modifies base color used by active effect (e.g., change from red breathing to blue breathing). Full customization, preparing for Phase 2 timeline editing.
- **D-22:** Effects grouped by category (Fades, Jumps, Multi-color, etc.) with section headers in the list.

### Claude's Discretion
- Connection timeout duration
- Exact signal strength thresholds for bar display
- Effect icon/preview design
- Landscape orientation support (defer if not critical)
- Animation transitions between screens

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### BLE Protocol & Architecture
- `.planning/research/ARCHITECTURE.md` — Component boundaries, data flow, BLE module design, command queue pattern
- `.planning/research/PITFALLS.md` — Critical BLE pitfalls (operation queuing, MELK init sequence, background stability)
- `.planning/research/STACK.md` — Nordic BLE Library choice, ELK-BLEDOM protocol UUIDs and command format

### Requirements
- `.planning/REQUIREMENTS.md` — BLE-01 through BLE-06, CTRL-01 through CTRL-05, UX-01, UX-02, UX-07, UX-08

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- None yet — greenfield project

### Established Patterns
- Nordic Android-BLE-Library 2.11.0 chosen for BLE (handles queuing, MTU, connection management)
- Jetpack Compose with Material 3 for UI
- MVVM + Clean Architecture per ARCHITECTURE.md
- StateFlow for reactive state management

### Integration Points
- BLE module is foundational — no dependencies, but Animation module (Phase 2) will depend on it
- Room database for storing device configs (name, MAC address)

</code_context>

<specifics>
## Specific Ideas

- Color wheel interaction should feel responsive — continuous updates, not commit-on-release
- Device renaming supports multi-tower scenarios (user can distinguish "Desk Tower" from "Corner Tower")
- Full effects menu now, because Phase 2 timeline will allow chaining effects: "5-color gradient + breathe -> blue breathing after 3 seconds" — the foundation needs all effects accessible
- Preview tower visualization should match physical tower segment layout

</specifics>

<deferred>
## Deferred Ideas

- Timeline-based effect sequencing (mentioned during effects discussion) — Phase 2
- Advanced background operation and battery optimization handling — if needed later
- Landscape orientation — evaluate after portrait is working

None — discussion stayed within phase scope

</deferred>

---

*Phase: 01-ble-foundation-basic-control*
*Context gathered: 2025-03-25*
