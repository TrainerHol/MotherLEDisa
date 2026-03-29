# Phase 3: Sound-Reactive Mode - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-03-28
**Phase:** 03-sound-reactive-mode
**Areas discussed:** Audio Source & Permissions, Sound-to-Animation Mapping, Audio Trigger Behavior, Configuration Screen UX

---

## Audio Source & Permissions

| Option | Description | Selected |
|--------|-------------|----------|
| Only on Sound screen | Audio capture starts when user opens Sound-Reactive screen, stops when leaving (Recommended — battery friendly, predictable) | |
| Toggle in screen | User manually enables/disables listening with a button on Sound screen | |
| Global toggle + service | Sound mode can run in background while using other screens | |

**User's choice:** "The app should listen to the audio. That's a hardware thing that towers do. It is an audio trigger by the tower itself not the app."

**Clarification Q: Tower notifies app or app polls?**

| Option | Description | Selected |
|--------|-------------|----------|
| Tower notifies app | Tower sends BLE notifications when sound threshold is crossed; app reacts by sending animation commands | |
| App polls tower | App periodically asks tower for current audio level | |
| Need to research protocol | Not sure how the bidirectional communication works | |

**User's choice:** "The app only sets settings on the towers. It doesn't need to communicate. It doesn't need to be constantly connected."

**Confirmation: Tower is autonomous?**

| Option | Description | Selected |
|--------|-------------|----------|
| Yes, tower is autonomous | App configures tower settings (animation, sensitivity, palette), tower runs sound-reactive behavior on its own | ✓ |
| No, app must stay connected | App needs to receive sound events and send commands in real-time | |

**User's choice:** Confirmed autonomous. "The MELK towers, you set them, and they stay on even if I turn off the app or whatever. It's just a setting set."

**Notes:** This is a critical architecture clarification. The tower has its own microphone and handles all audio detection internally. The app is a configuration tool, not a real-time controller.

---

## Sound-to-Animation Mapping

| Option | Description | Selected |
|--------|-------------|----------|
| Custom requires app | Tower only stores built-in effects. For custom animations, app must be connected and actively sending color commands. | |
| Tower stores custom | Tower can store custom animation data sent by app, then play it autonomously on sound triggers | |
| Needs protocol research | Unknown whether ELK-BLEDOM supports custom animation upload | ✓ |

**User's choice:** "Needs protocol research"

**Follow-up: Fallback if tower can't store custom?**

| Option | Description | Selected |
|--------|-------------|----------|
| App listens via phone mic | If tower can't store custom, app captures audio via phone mic and sends commands in real-time | |
| Sound triggers built-in only | Sound-reactive mode limited to tower's built-in effects, but with customizable palette/sensitivity | ✓ |
| Hybrid: research both paths | Try tower-native first. If unsupported, implement app-side audio capture as fallback | |

**User's choice:** "Sound triggers built-in only" — custom animations stay in Phase 2 manual playback if protocol doesn't support upload.

**Palette definition:**

| Option | Description | Selected |
|--------|-------------|----------|
| Pick from saved presets | User selects a saved animation preset from Phase 2; its color palette is extracted | |
| Custom palette picker | Dedicated UI to pick 2-5 colors specifically for sound mode | |
| Both options | User can either pick from presets or build a custom sound palette | ✓ |

**User's choice:** "Both options"

**Notes:** "This is very important. The main appeal of this app is being able to customize the sound reacting effects because right now you can just do like some presets that some apps come with but we want to be able to customize effects and then trigger those either via sound reaction or looping etc."

---

## Audio Trigger Behavior

| Option | Description | Selected |
|--------|-------------|----------|
| Single threshold slider | One slider: lower = more sensitive, higher = only reacts to loud sounds | |
| Threshold + gain | Threshold slider plus a 'gain/amplification' slider | |
| You decide | Claude picks based on what the protocol supports | ✓ |

**User's choice:** "You decide"

**Sound effects selection:**

| Option | Description | Selected |
|--------|-------------|----------|
| All sound effects | Show all ELK-BLEDOM sound-reactive effects | |
| Curated selection | Pick the best 3-5 sound effects | |
| You decide | Claude picks based on research | ✓ |

**User's choice:** "You decide"

---

## Configuration Screen UX

| Option | Description | Selected |
|--------|-------------|----------|
| Yes, show VU meter | Live bar showing audio level from tower | |
| No visualization needed | Just settings controls | |
| Only if tower sends audio data | Show visualization if protocol supports reading audio level | ✓ |

**User's choice:** "Only if tower sends audio data"

**Screen layout:**

| Option | Description | Selected |
|--------|-------------|----------|
| Single scrollable screen | Preview at top, then effect selector, sensitivity slider, palette picker | |
| Tabbed sections | Tabs for 'Effect', 'Sensitivity', 'Palette' | |
| You decide | Claude picks based on Phase 1/2 patterns | ✓ |

**User's choice:** "You decide"

**Navigation:**

| Option | Description | Selected |
|--------|-------------|----------|
| Bottom nav tab | Add 'Sound' as a new tab in bottom navigation | |
| Inside Control screen | Sound mode is a section within existing Control screen | |
| From Editor screen | Sound config accessible via Animation Editor | |

**User's choice:** "Whoever gives the best user experience, just make sure you check the current layout and then add it cleanly."

---

## Claude's Discretion

- Sensitivity UI design (based on protocol research)
- Which sound effects to expose (based on protocol research)
- VU meter implementation (only if protocol supports audio level data)
- Screen layout (follow Phase 1/2 patterns)
- Navigation placement (integrate cleanly with current layout)

## Deferred Ideas

- App-side audio capture via phone mic — per PROJECT.md, phone mic is "useless"
- Frequency band mapping (bass/mids/highs) — v2
- Beat detection with BPM sync — v2
- Multi-tower cascade sound reactions — Phase 4
