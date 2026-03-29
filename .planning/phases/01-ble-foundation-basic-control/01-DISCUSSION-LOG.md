# Phase 1: BLE Foundation & Basic Control - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2025-03-25
**Phase:** 01-ble-foundation-basic-control
**Areas discussed:** Device discovery UX, Control layout, Connection behavior, Built-in effects

---

## Device Discovery UX

| Option | Description | Selected |
|--------|-------------|----------|
| Pull-to-refresh | User pulls down to scan. Stops after 10-15 seconds. | |
| Auto-scan on screen open | Start scanning when device list opens, stop after timeout. | |
| Continuous scan | Always scanning when on device screen. Shows live devices. | ✓ |

**User's choice:** Continuous scan
**Notes:** None

---

| Option | Description | Selected |
|--------|-------------|----------|
| Name + signal + status | Device name, signal strength bars, connected/available badge | ✓ |
| Name + status only | Device name and connection status. No signal. | |
| Name + signal + last seen | Device name, signal bars, "last seen" timestamp | |

**User's choice:** Name + signal + status (Recommended)
**Notes:** None

---

| Option | Description | Selected |
|--------|-------------|----------|
| Direct connect | Single tap connects immediately with loading indicator. | ✓ |
| Detail sheet then connect | Single tap opens bottom sheet with device info + Connect button | |
| Long press for actions | Single tap selects, long press shows connect/rename/forget | |

**User's choice:** Direct connect (Recommended)
**Notes:** None

---

| Option | Description | Selected |
|--------|-------------|----------|
| Illustration + tips | Friendly graphic + troubleshooting tips + Scan button | |
| Text only | Simple message: "No devices found. Tap to scan again." | ✓ |
| Troubleshooting checklist | Expandable checklist for debugging | |

**User's choice:** Text only
**Notes:** None

---

| Option | Description | Selected |
|--------|-------------|----------|
| Yes, show section | "Known Devices" section at top. Tap to reconnect. | |
| Yes, but merge with scan | Known devices in same list with "Last connected" badge. | ✓ |
| No, scan only | Always fresh scan. No device memory. | |

**User's choice:** Yes, but merge with scan
**Notes:** None

---

## Control Layout

| Option | Description | Selected |
|--------|-------------|----------|
| Single screen, scrollable | All controls on one screen: preview, power, color, brightness, effects | ✓ |
| Tabs (Color / Effects) | Tab bar switching between Color controls and Effects controls | |
| Bottom sheet controls | Main area shows preview. Controls slide up from bottom sheet. | |

**User's choice:** Single screen, scrollable
**Notes:** None

---

| Option | Description | Selected |
|--------|-------------|----------|
| Tower visualization | Vertical LED tower graphic showing segment colors, real-time updates | ✓ |
| Color swatch only | Large color circle showing current color. Simple. | |
| Minimal - no preview | Focus on controls. User looks at physical tower. | |

**User's choice:** Tower visualization (Recommended)
**Notes:** None

---

| Option | Description | Selected |
|--------|-------------|----------|
| Circular wheel + saturation | Color wheel for hue, vertical slider for saturation/brightness | ✓ |
| HSV sliders | Three sliders: Hue, Saturation, Value. Precise but technical. | |
| Color palette grid | Grid of preset colors. Quick but limited. | |

**User's choice:** Circular wheel + saturation (Recommended)
**Notes:** None

---

| Option | Description | Selected |
|--------|-------------|----------|
| Large toggle button | Prominent on/off toggle at top of control area | ✓ |
| FAB in corner | Floating action button for power | |
| In app bar | Power toggle in top app bar with device name | |

**User's choice:** Large toggle button (Recommended)
**Notes:** None

---

| Option | Description | Selected |
|--------|-------------|----------|
| Continuous with debounce | Slider sends commands as you drag, ~30fps. Real-time feedback. | ✓ |
| Commit on release | Only send brightness when finger lifts. Less traffic. | |
| Discrete steps | Snap to 10% increments. Simple but limited. | |

**User's choice:** Continuous with debounce (Recommended)
**Notes:** None

---

| Option | Description | Selected |
|--------|-------------|----------|
| Yes, 8-10 swatches | Row of common colors for quick taps | ✓ |
| No presets | Just the color wheel. Minimal. | |
| User's recent colors | Show last 5-8 colors for quick re-selection | |

**User's choice:** Yes, 8-10 swatches (Recommended)
**Notes:** None

---

| Option | Description | Selected |
|--------|-------------|----------|
| Device picker chip | Chip/dropdown showing selected device, "All" option | ✓ |
| Horizontal tabs | Tab for each connected device plus "All" | |
| One at a time | Control screen shows one device, navigate to switch | |

**User's choice:** Device picker chip (Recommended)
**Notes:** None

---

## Connection Behavior

| Option | Description | Selected |
|--------|-------------|----------|
| Silent retry 3x | Try reconnecting 3 times, show error if all fail | |
| Immediate notification | Show snackbar immediately on disconnect | |
| User-initiated only | Show "Disconnected" badge, user taps to reconnect | |

**User's choice:** (Free text) "Just keep reconnecting if it finds some"
**Notes:** Persistent reconnection — keep trying whenever device is visible, no retry limit

---

| Option | Description | Selected |
|--------|-------------|----------|
| Minimal persistent | Small notification: "Connected to X tower(s)". Low priority. | |
| Controls in notification | Notification with power toggle and brightness buttons | |
| Only when active | Notification only during animation playback | |

**User's choice:** (Free text) "Background stuff doesn't matter because we're just setting it and sometimes changing"
**Notes:** App is primarily active-use. Minimal foreground service for connection stability, not heavy background features.

---

| Option | Description | Selected |
|--------|-------------|----------|
| Instant switch | Immediately switch control focus. Both devices stay connected. | ✓ |
| Disconnect old, connect new | Single connection at a time | |
| Add to selection | Selecting adds to active devices | |

**User's choice:** Instant switch (Recommended)
**Notes:** None

---

| Option | Description | Selected |
|--------|-------------|----------|
| Yes, editable name | Long-press or edit icon to set custom name | ✓ |
| No custom names | Always show BLE advertised name | |

**User's choice:** Yes, editable name (Recommended)
**Notes:** None

---

| Option | Description | Selected |
|--------|-------------|----------|
| In-context explanation | Dialog explaining why permissions needed, "Grant" button | ✓ |
| Block with message | Screen showing "Permission required" with settings link | |
| Request on first action | Only request when user taps Scan | |

**User's choice:** In-context explanation (Recommended)
**Notes:** None

---

## Built-in Effects

| Option | Description | Selected |
|--------|-------------|----------|
| Core 4 + speed | Fade, Jump, Blink, Breathing with speed slider | |
| Full hardware menu | All effects from ELK-BLEDOM protocol (~20+) | ✓ |
| Just static color | No hardware effects in Phase 1 | |

**User's choice:** Full hardware menu
**Notes:** None

---

| Option | Description | Selected |
|--------|-------------|----------|
| Scrollable list with icons | Vertical list with name + icon, tap to activate | ✓ |
| Grid of effect cards | 2-3 column grid with cards | |
| Horizontal carousel | Swipe through effects, current centered | |

**User's choice:** Scrollable list with icons (Recommended)
**Notes:** None

---

| Option | Description | Selected |
|--------|-------------|----------|
| Slider when selected | Speed slider appears below list when effect active | ✓ |
| Per-effect speed in list | Each effect row has inline speed slider | |
| Fixed speeds only | Slow / Medium / Fast presets | |

**User's choice:** Slider when selected (Recommended)
**Notes:** None

---

| Option | Description | Selected |
|--------|-------------|----------|
| Yes, affects base color | Color wheel changes base color used by effect | |
| Yes, but switches to solid | Changing color turns off effect | |
| No, disable color picker | Color picker grayed out during effects | |

**User's choice:** (Free text) "Full customization. This is important for later when we have a timeline for the user that lets the user customize effects like that. i.e. 5 color gradient + breathe -> blue breathing after 3 seconds, etc. in the after effects like timeline"
**Notes:** Color wheel modifies base color of active effect. Foundation for Phase 2 timeline editing.

---

| Option | Description | Selected |
|--------|-------------|----------|
| Flat list with sections | Grouped by type (Fades, Jumps, etc.) with section headers | ✓ |
| Flat alphabetical | All effects alphabetically | |
| You decide | Claude's discretion | |

**User's choice:** Flat list with sections (Recommended)
**Notes:** None

---

## Claude's Discretion

- Connection timeout duration
- Exact signal strength thresholds for bar display
- Effect icon/preview design
- Landscape orientation support
- Animation transitions between screens

## Deferred Ideas

- Timeline-based effect sequencing — Phase 2
- Advanced background operation — if needed later
- Landscape orientation — evaluate after portrait
