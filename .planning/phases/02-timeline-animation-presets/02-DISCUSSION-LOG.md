# Phase 2: Timeline Animation & Presets - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-03-28
**Phase:** 02-timeline-animation-presets
**Areas discussed:** Timeline editor design, Keyframe behavior, Animation playback

---

## Gray Area Selection

| Option | Description | Selected |
|--------|-------------|----------|
| Timeline editor design | Canvas layout, keyframe visualization, time ruler, playhead interaction, drag-to-edit gestures | ✓ |
| Keyframe behavior | What properties per keyframe, interpolation between keyframes, grouping | ✓ |
| Animation playback | Preview vs device playback, loop modes, timing sync, transport controls | ✓ |
| Preset library UI | List vs grid, thumbnails/previews, naming, organization, quick-apply | |

**User's choice:** Discuss timeline editor, keyframe behavior, and animation playback. Skip preset library UI (sensible defaults).

---

## Timeline editor design

### Question 1: Canvas Layout

| Option | Description | Selected |
|--------|-------------|----------|
| Horizontal timeline (AE-style) | Time flows left-to-right, segments/layers stacked vertically. Matches After Effects reference. | ✓ |
| Vertical timeline | Time flows top-to-bottom, segments side-by-side horizontally | |
| Circular/radial | Time as circular loop, useful for repeating animations | |

**User's choice:** Horizontal timeline (AE-style)

### Question 2: Keyframe Visualization

| Option | Description | Selected |
|--------|-------------|----------|
| Diamond markers on track | Classic AE-style diamonds on a horizontal track line, colored by keyframe value | ✓ |
| Colored blocks/bars | Each keyframe is a bar showing duration, color-filled to indicate value | |
| Dots with color preview | Circular dots positioned on timeline, showing color fill | |

**User's choice:** Diamond markers on track

### Question 3: Adding Keyframes

| Option | Description | Selected |
|--------|-------------|----------|
| Double-tap on track | Double-tap anywhere on segment track to add keyframe at that time | |
| FAB + tap position | Press floating action button, then tap timeline to place keyframe | |
| Long-press on track | Long-press brings up context menu with 'Add keyframe' option | ✓ |

**User's choice:** Long-press on track

### Question 4: Playhead Interaction

| Option | Description | Selected |
|--------|-------------|----------|
| Draggable vertical line + scrubbing | Red vertical line spans all tracks. Drag to scrub with live preview. | ✓ |
| Tap to move | Playhead jumps to tapped position. No drag scrubbing. | |
| Transport controls only | Playhead moves only during playback or via frame buttons | |

**User's choice:** Draggable vertical line + scrubbing

---

## Keyframe behavior

### Question 1: Keyframe Properties

| Option | Description | Selected |
|--------|-------------|----------|
| Color (RGB) | The color at this moment in time | ✓ |
| Segment/position | Which LED segment(s) are affected | ✓ |
| Brightness | Separate brightness keyframes independent of color | ✓ |
| Effect trigger | Trigger a hardware effect at this keyframe time | ✓ |

**User's choice:** All four properties (multi-select)

### Question 2: Color Interpolation

| Option | Description | Selected |
|--------|-------------|----------|
| Smooth blend (HSV) | Gradual color transition through hue space | |
| Smooth blend (RGB) | Linear RGB interpolation — can go through muddy colors | |
| Step/hold | Snap to keyframe color instantly, hold until next | |
| User choice per keyframe | Let user pick interpolation mode for each keyframe | ✓ |

**User's choice:** User choice per keyframe

### Question 3: Segment Track Organization

| Option | Description | Selected |
|--------|-------------|----------|
| One track per segment | 5 segments = 5 horizontal tracks. Independent keyframes per segment. | ✓ |
| Single track + segment dropdown | One track, each keyframe has segment selector | |
| Master + override tracks | Master affects all, optional per-segment overrides | |

**User's choice:** "Whatever provides the best user experience in customization" — resolved to one track per segment for clearest visualization.

---

## Animation playback

### Question 1: Preview Location

| Option | Description | Selected |
|--------|-------------|----------|
| Fixed preview above timeline | Tower preview at top, timeline below. Always visible. | ✓ |
| Floating overlay | Miniature preview floats in corner, draggable/dismissible | |
| Full-screen preview mode | Switch to full-screen, hides timeline | |

**User's choice:** Fixed preview above timeline

### Question 2: Play Behavior

| Option | Description | Selected |
|--------|-------------|----------|
| Preview only (in-app) | Animation plays in on-screen preview. Explicit 'Send to device' after. | |
| Device immediately | Play sends commands to connected tower(s) right away | |
| Both simultaneously | In-app preview + device update together for instant feedback | ✓ |

**User's choice:** Both simultaneously

### Question 3: Loop Options

| Option | Description | Selected |
|--------|-------------|----------|
| Loop toggle + one-shot | Simple toggle: Loop forever OR play once | |
| Loop count | Choose: once, 2x, 3x, infinite | |
| Ping-pong option | Loop forward-backward (A→B→A) plus normal loop | |

**User's choice:** "All options, whatever has more customization" — resolved to loop count + ping-pong mode

---

## Claude's Discretion

- Time ruler scale and zoom/pan gestures
- Keyframe selection UI (multi-select, lasso)
- Copy/paste keyframe functionality
- Easing curves beyond linear/step
- Preset thumbnail generation
- Animation duration limits

## Deferred Ideas

- Sound-reactive triggers — Phase 3
- Multi-tower choreography — Phase 4
- Pattern generators (sine waves) — evaluate as extension
- Advanced easing curves (bezier) — post-MVP
