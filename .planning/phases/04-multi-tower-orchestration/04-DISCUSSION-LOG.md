# Phase 4: Multi-Tower Orchestration - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-03-28
**Phase:** 04-multi-tower-orchestration
**Areas discussed:** Tower ordering UI, Mode switching UX, Timing controls, Screen layout

---

## Tower Ordering UI

| Option | Description | Selected |
|--------|-------------|----------|
| Drag-and-drop list | Vertical reorderable list — tap and drag to rearrange. Simple, familiar pattern. | ✓ |
| Numbered badges | Tap tower to assign number (1, 2, 3...). Good for quick reordering without dragging. | |
| Visual room layout | Top-down room view where users position tower icons spatially. More immersive but complex. | |

**User's choice:** Drag-and-drop list
**Notes:** Simple and familiar pattern

| Option | Description | Selected |
|--------|-------------|----------|
| Global order | One tower order used across all animations — simpler, set once and forget. | |
| Per-animation | Each preset/animation can have its own tower order — more flexible for different effects. | |
| Both | Global default order, with per-animation override option. | ✓ |

**User's choice:** Both
**Notes:** Global default with per-animation override capability

---

## Mode Switching UX

| Option | Description | Selected |
|--------|-------------|----------|
| Segmented control | Horizontal toggle bar with Mirror | Offset | Cascade | Independent. Quick visual switching. | ✓ |
| Dropdown menu | Single dropdown selector — compact, but requires tap to see options. | |
| Card selection | Tappable cards with mode name + brief description. More discoverable for new users. | |

**User's choice:** Segmented control
**Notes:** Quick visual mode switching

| Option | Description | Selected |
|--------|-------------|----------|
| Dropdown per tower | Each tower gets a dropdown to pick its animation/preset. Compact, fits existing DevicePicker pattern. | ✓ |
| Tower cards | Expandable cards showing tower name + current animation with inline controls. More visual. | |
| Tab per tower | Swipeable tabs — each tower gets its own control screen. Good for deep customization. | |

**User's choice:** Dropdown per tower
**Notes:** For independent mode per-tower control

---

## Timing Controls

| Option | Description | Selected |
|--------|-------------|----------|
| Single delay slider | One slider (e.g., 0-2000ms) — same delay between each pair of towers. | ✓ |
| Per-pair delay | Individual delay sliders between each tower pair — more control, more complexity. | |
| Percentage-based | Slider as % of animation duration (e.g., 25% offset = tower 2 starts at 25%, tower 3 at 50%). | |

**User's choice:** Single delay slider
**Notes:** Same delay between consecutive tower pairs

| Option | Description | Selected |
|--------|-------------|----------|
| Immediate handoff | Tower 2 starts instantly when tower 1 completes. Clean relay effect. | ✓ |
| Overlap option | Optional overlap (tower 2 can start before tower 1 finishes) for smooth transitions. | |
| Gap option | Optional gap/pause between towers for dramatic effect (darkness between). | |

**User's choice:** Immediate handoff
**Notes:** Clean relay effect for cascade mode

| Option | Description | Selected |
|--------|-------------|----------|
| Accept variance | Don't try to compensate — slight variance is acceptable for home use. | ✓ |
| Show warning | Warn user that perfect sync isn't possible, but proceed normally. | |
| Stagger commands | Pre-delay commands to faster-responding towers to improve perceived sync (complex). | |

**User's choice:** Accept variance
**Notes:** BLE latency (~20-50ms) variance acceptable for home use

---

## Screen Layout

| Option | Description | Selected |
|--------|-------------|----------|
| Dedicated tab | New 'Multi' or 'Orchestrate' tab in bottom nav — clear separation, but adds 5th tab. | ✓ |
| Under Control | Expand Control screen with orchestration section when multiple towers connected. | |
| Under Presets | Orchestration is part of preset configuration — set mode when applying to multiple towers. | |

**User's choice:** Dedicated tab
**Notes:** Clear separation for multi-tower features

| Option | Description | Selected |
|--------|-------------|----------|
| After Control | Devices | Control | Orchestrate | Sound | Presets — orchestration relates to control. | ✓ |
| After Sound | Devices | Control | Sound | Orchestrate | Presets — keeps presets last. | |
| Replace Presets | Move Presets into Control or Orchestrate, keep to 4 tabs total. | |

**User's choice:** After Control
**Notes:** Orchestration relates to control functionality

| Option | Description | Selected |
|--------|-------------|----------|
| Always visible | Tab always shown — user can set up orchestration before connecting more towers. | |
| Hidden until 2+ | Tab only appears when 2+ towers connected — cleaner nav for single-tower users. | |
| Visible but disabled | Tab visible but grayed out with 'Connect more towers' message when <2 towers. | ✓ |

**User's choice:** Visible but disabled
**Notes:** Shows capability exists but requires more towers

---

## Claude's Discretion

- Exact slider range for offset delay
- Orchestrate tab icon choice
- Empty state design for <2 towers
- Tower preview visualization in orchestrate screen
- Whether to show live preview of offset/cascade timing

## Deferred Ideas

- Per-pair offset delays (more granular control)
- Overlap/gap options for cascade mode
- Visual room layout for tower positioning
- BLE latency compensation
