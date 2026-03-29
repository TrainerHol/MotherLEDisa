# Roadmap: MotherLEDisa

## Overview

MotherLEDisa delivers custom LED animation control for MELK-OT21 tower lights through four phases: establishing reliable BLE connectivity with basic controls, building the core timeline animation editor with preset management, adding sound-reactive capabilities, and finally enabling multi-tower orchestration. Each phase delivers complete, verifiable user capabilities before moving forward.

## Phases

**Phase Numbering:**
- Integer phases (1, 2, 3, 4): Planned milestone work
- Decimal phases (2.1, 2.2): Urgent insertions (marked with INSERTED)

Decimal phases appear between their surrounding integers in numeric order.

- [x] **Phase 1: BLE Foundation & Basic Control** - Connect to towers and control power, color, brightness
- [x] **Phase 2: Timeline Animation & Presets** - Create keyframe animations and save them as presets
- [x] **Phase 3: Sound-Reactive Mode** - Configure tower's built-in microphone for autonomous sound response
- [ ] **Phase 4: Multi-Tower Orchestration** - Coordinate multiple towers with mirror, offset, and cascade modes

## Phase Details

### Phase 1: BLE Foundation & Basic Control
**Goal**: Users can discover, connect to, and control individual LED towers with basic commands
**Depends on**: Nothing (first phase)
**Requirements**: BLE-01, BLE-02, BLE-03, BLE-04, BLE-05, BLE-06, CTRL-01, CTRL-02, CTRL-03, CTRL-04, CTRL-05, UX-01, UX-02, UX-07, UX-08
**Success Criteria** (what must be TRUE):
  1. User can scan for nearby MELK/ELK devices and see them listed
  2. User can connect to a tower and see connection status
  3. User can turn tower on/off with a single tap
  4. User can change tower color using a color wheel and see change immediately
  5. User can adjust brightness with a slider and see real-time preview
**Plans**: 3 plans
Plans:
- [x] 01-01-PLAN.md — Android project foundation with BLE infrastructure and data layer
- [x] 01-02-PLAN.md — Device discovery screen with scanning, connection, and navigation
- [x] 01-03-PLAN.md — Control screen with power, color, brightness, and effects
**UI hint**: yes

### Phase 2: Timeline Animation & Presets
**Goal**: Users can create custom keyframe animations and save them for reuse
**Depends on**: Phase 1
**Requirements**: ANIM-01, ANIM-02, ANIM-03, ANIM-04, ANIM-05, ANIM-06, ANIM-07, PRESET-01, PRESET-02, PRESET-03, PRESET-04, PRESET-05, UX-03, UX-06
**Success Criteria** (what must be TRUE):
  1. User can create an animation with multiple keyframes on a timeline
  2. User can set color and position for each keyframe and drag to adjust timing
  3. User can preview animation in app before sending to device
  4. User can play, pause, and stop animation on connected tower
  5. User can save animation as preset and recall it later
**Plans**: 5 plans
Plans:
- [x] 02-01-PLAN.md — Domain models, Room entities, and database migration
- [x] 02-02-PLAN.md — Animation playback engine with HSV interpolation
- [x] 02-03-PLAN.md — Timeline editor UI components (Canvas, keyframes, playhead)
- [x] 02-04-PLAN.md — Animation Editor screen with ViewModel assembly
- [x] 02-05-PLAN.md — Preset Library screen with grid and options
**UI hint**: yes

### Phase 3: Sound-Reactive Mode
**Goal**: Users can configure tower's built-in microphone for autonomous sound-reactive effects with custom sensitivity and color palettes
**Depends on**: Phase 2
**Requirements**: SOUND-01, SOUND-02, SOUND-03, SOUND-04, UX-04
**Success Criteria** (what must be TRUE):
  1. User can enable sound-reactive mode using tower's internal microphone
  2. User can adjust sound sensitivity threshold (0-100)
  3. User can assign custom color palette to sound triggers
  4. User can select from 8 sound-reactive effects
**Plans**: 4 plans
Plans:
- [x] 03-01-PLAN.md — Sound mode protocol commands and domain use cases
- [x] 03-02-PLAN.md — Palette picker model and UI components
- [x] 03-03-PLAN.md — SoundReactiveScreen and ViewModel
- [x] 03-04-PLAN.md — Navigation integration with Sound tab
**UI hint**: yes

### Phase 4: Multi-Tower Orchestration
**Goal**: Users can coordinate multiple towers with synchronized and choreographed animation modes
**Depends on**: Phase 3
**Requirements**: MULTI-01, MULTI-02, MULTI-03, MULTI-04, MULTI-05, MULTI-06, UX-05
**Success Criteria** (what must be TRUE):
  1. User can control multiple connected towers simultaneously
  2. User can enable mirror mode (all towers show same animation)
  3. User can enable offset mode (staggered timing across towers)
  4. User can enable cascade mode (tower-to-tower relay animations)
  5. User can define tower ordering for choreographed modes
**Plans**: 4 plans
Plans:
- [ ] 04-01-PLAN.md — OrchestrationMode enum and OrchestrationManager domain logic
- [ ] 04-02-PLAN.md — UI components (mode selector, tower list, offset slider, independent config)
- [ ] 04-03-PLAN.md — OrchestrateScreen and OrchestrateViewModel
- [ ] 04-04-PLAN.md — Navigation integration with Orchestrate tab
**UI hint**: yes

## Progress

**Execution Order:**
Phases execute in numeric order: 1 -> 2 -> 3 -> 4

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 1. BLE Foundation & Basic Control | 3/3 | Complete | 2026-03-28 |
| 2. Timeline Animation & Presets | 5/5 | Complete | 2026-03-29 |
| 3. Sound-Reactive Mode | 4/4 | Complete | 2026-03-29 |
| 4. Multi-Tower Orchestration | 0/4 | In Progress | - |
