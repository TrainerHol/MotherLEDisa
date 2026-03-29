# MotherLEDisa

## What This Is

An Android app for deep customization of MELK-OT21 Bluetooth tower lights. Unlike existing apps that only offer preset modes, MotherLEDisa provides a timeline-based animation editor (After Effects-style) for creating custom light patterns, sound-reactive visualizations with custom color palettes, and multi-tower orchestration for room-scale lighting choreography.

## Core Value

Users can create and save their own custom LED animations with frame-by-frame control over color, position, and timing — not just pick from presets.

## Requirements

### Validated

(None yet — ship to validate)

### Active

- [ ] Connect to MELK-OT21 devices via Bluetooth LE
- [ ] Discover and list nearby compatible devices
- [ ] Control individual LED segments/positions on towers
- [ ] Timeline editor with keyframe animation (color over time)
- [ ] Pattern designer (sine waves, gradients, custom shapes)
- [ ] Sound-reactive mode using device's internal microphone
- [ ] Threshold controls for sound sensitivity
- [ ] Custom color palettes for sound triggers
- [ ] Multi-tower control with mirror mode (all same)
- [ ] Multi-tower offset mode (staggered timing across towers)
- [ ] Multi-tower cascade mode (relay — one finishes, next starts)
- [ ] Independent per-tower control
- [ ] User-defined tower ordering
- [ ] Save animations as presets
- [ ] Recall and apply saved presets

### Out of Scope

- iOS app — Android only for v1
- Phone microphone for sound reactivity — must use device's internal mic
- Cloud sync — local storage only for v1
- Social features/sharing — focus on personal use

## Context

**Hardware**: MELK-OT21 Bluetooth tower lights with addressable LED segments. User has 4 towers in living room, positioned near TV for audio reactivity.

**Reference Implementation**: [elk-led-controller](https://github.com/b1scoito/elk-led-controller) — reverse-engineered BLE protocol for ELK-BLEDOM compatible devices. Supports RGB control (0-255), brightness (0-100), built-in effects, and basic audio visualization. MELK devices are listed as compatible.

**Key Insight**: Existing apps (including the reference library) provide sound → preset mapping. User wants sound → custom animation mapping with full control over what triggers what.

**Physical Setup**: Tower lights have vertical LED segments that can display different colors at different heights. Patterns like sine waves affect segment brightness/color based on position. App needs to support position-based color mapping.

## Constraints

- **Platform**: Android (Kotlin) — native BLE required for low-latency control
- **Audio Source**: Device internal microphone only — phone mic is "useless" per user
- **Protocol**: Must work with MELK-OT21 BLE characteristics (reverse engineer or extend elk-led-controller approach)
- **Device Count**: Support unlimited towers, tested with 4

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Timeline UI modeled on After Effects | User specifically referenced AE-style keyframe editing | — Pending |
| Sound reactivity via device mic | Phone mic doesn't capture room audio effectively | — Pending |
| Cascade mode for multi-tower | User wants tower-to-tower relay animations | — Pending |

## Evolution

This document evolves at phase transitions and milestone boundaries.

**After each phase transition** (via `/gsd:transition`):
1. Requirements invalidated? → Move to Out of Scope with reason
2. Requirements validated? → Move to Validated with phase reference
3. New requirements emerged? → Add to Active
4. Decisions to log? → Add to Key Decisions
5. "What This Is" still accurate? → Update if drifted

**After each milestone** (via `/gsd:complete-milestone`):
1. Full review of all sections
2. Core Value check — still the right priority?
3. Audit Out of Scope — reasons still valid?
4. Update Context with current state

---
*Last updated: 2026-03-29 — Phase 4 (Multi-Tower Orchestration) complete. All 4 phases delivered.*
