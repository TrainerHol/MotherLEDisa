---
phase: 01-ble-foundation-basic-control
plan: 03
subsystem: UI/Control
tags: [control-screen, color-picker, effects, brightness, power-toggle, device-picker, debouncing]
completed: 2026-03-28
duration: 47min
dependency_graph:
  requires:
    - 01-01-BLE-infrastructure
    - 01-02-device-discovery
  provides:
    - control-screen
    - color-picker
    - effects-menu
    - brightness-control
    - power-toggle
    - device-picker
  affects:
    - TowerConnectionManager
    - ControlViewModel
tech_stack:
  added:
    - HarmonyColorPicker (Compose color wheel)
    - AnimatedVisibility (Compose animation)
    - LazyColumn with stickyHeader (effects list)
  patterns:
    - Debounced state updates (30fps for brightness, 50ms for color)
    - Single/multi-device targeting (null = all devices)
    - Category-grouped effects with sticky headers
key_files:
  created:
    - app/src/main/java/com/motherledisa/ui/control/ControlScreen.kt
    - app/src/main/java/com/motherledisa/ui/control/ControlViewModel.kt
    - app/src/main/java/com/motherledisa/ui/control/components/TowerPreviewCanvas.kt
    - app/src/main/java/com/motherledisa/ui/control/components/PowerToggleButton.kt
    - app/src/main/java/com/motherledisa/ui/control/components/ColorPickerSection.kt
    - app/src/main/java/com/motherledisa/ui/control/components/BrightnessSlider.kt
    - app/src/main/java/com/motherledisa/ui/control/components/DevicePicker.kt
    - app/src/main/java/com/motherledisa/ui/control/components/EffectsSection.kt
    - app/src/main/java/com/motherledisa/ui/control/components/SpeedSlider.kt
    - app/src/main/java/com/motherledisa/domain/usecase/TogglePowerUseCase.kt
    - app/src/main/java/com/motherledisa/domain/usecase/SetColorUseCase.kt
    - app/src/main/java/com/motherledisa/domain/usecase/SetBrightnessUseCase.kt
    - app/src/main/java/com/motherledisa/domain/usecase/SetEffectUseCase.kt
  modified: []
decisions:
  - Use HarmonyColorPicker (godaddy/android-colorpicker) for circular HSV color wheel
  - Debounce brightness to 30fps (33ms) and color to 50ms to prevent BLE queue flooding
  - Device picker shows "All devices" option (null address) for multi-tower control
  - Effects grouped by category (Static, Fade, Jump, Breathe, Strobe, Multi-color) with sticky headers
  - Speed slider appears/disappears via AnimatedVisibility based on activeEffect state
  - Tower preview uses Canvas with 60% width, 8dp corner radius, 4dp segment gaps
  - Power button: 64dp diameter, yellow (#FFEB3B) when ON, gray (#666666) when OFF
  - Preset color swatches: 8 colors (Red, Orange, Yellow, Green, Cyan, Blue, Purple, White) at 48dp each
metrics:
  tasks_completed: 3
  files_created: 13
  lines_added: ~1200
  commits: 3
---

# Phase 01 Plan 03: Control Screen with Power, Color, Brightness, and Effects Summary

**One-liner:** Complete control screen with debounced color picker (HSV wheel + 8 presets), brightness slider (30fps), power toggle (yellow/gray), effects menu (28+ grouped by category), speed control, and device picker for multi-tower orchestration.

## What Was Built

A fully functional control interface for MELK LED towers with real-time preview and debounced BLE commands. The screen provides:

1. **Tower Preview Canvas** - Vertical tower visualization showing 5 segments with current colors, 60% width, rounded corners
2. **Power Toggle** - 64dp circular button (yellow when ON, gray when OFF) with tap feedback
3. **Color Picker** - HSV color wheel with continuous updates + 8 preset color swatches for quick access
4. **Brightness Slider** - 0-100% range with debounced updates at 30fps to prevent queue flooding
5. **Effects Menu** - 28+ hardware effects grouped by category (Fades, Jumps, Strobe, etc.) with sticky section headers
6. **Speed Control** - Slider that appears when an effect is active to adjust effect speed (0-100%)
7. **Device Picker** - Dropdown for multi-tower scenarios with "All devices" option to control multiple towers simultaneously

### Architecture

**Use Cases (Domain Layer):**
- `TogglePowerUseCase` - Turn tower(s) on/off
- `SetColorUseCase` - Send RGB color commands
- `SetBrightnessUseCase` - Adjust brightness (0-100)
- `SetEffectUseCase` - Start hardware effects with speed

Each use case has `invoke(tower)` for single-device and `invokeAll()` for multi-device control.

**ControlViewModel:**
- Manages UI state (power, color, brightness, activeEffect, effectSpeed)
- Debounces brightness updates to 33ms (~30fps) using `Flow.debounce()`
- Debounces color updates to 50ms to prevent BLE queue flooding
- Updates UI state immediately for 60fps preview
- Handles device selection (null = all devices)
- Groups effects by category for organized display

**UI Components:**
- `TowerPreviewCanvas` - Custom Canvas drawing with segment gaps and rounded corners
- `PowerToggleButton` - Animated color transition between states
- `ColorPickerSection` - HarmonyColorPicker + LazyRow of preset swatches
- `BrightnessSlider` - Material Slider with percentage display
- `DevicePicker` - ExposedDropdownMenu (only visible when multiple towers connected)
- `EffectsSection` - LazyColumn with sticky category headers
- `SpeedSlider` - Material Slider for effect speed adjustment

## Requirements Fulfilled

- **CTRL-01:** User can turn tower on/off with single tap ✓
- **CTRL-02:** User can select any RGB color via color wheel ✓
- **CTRL-03:** User can adjust brightness with real-time preview ✓
- **CTRL-04:** User can select from built-in hardware effects ✓
- **CTRL-05:** User can adjust effect speed ✓
- **UX-02:** Dedicated control screen with all elements ✓
- **UX-08:** Real-time preview shows current tower state ✓

## Design Decisions

### 1. Debouncing Strategy
**Decision:** Brightness debounced to 33ms (~30fps), color to 50ms
**Rationale:** Prevents BLE queue flooding while maintaining smooth UI updates at 60fps. UI state updates immediately for preview, BLE commands are throttled.
**Impact:** Smooth user experience without overwhelming the BLE command queue (20ms per command minimum).

### 2. Device Selection Pattern
**Decision:** `null` address = all devices, specific address = single device
**Rationale:** Simple, type-safe way to handle multi-tower control without complex state management.
**Impact:** Each use case has `invoke(tower)` and `invokeAll()` methods. ViewModel checks selected address and routes accordingly.

### 3. Effects Organization
**Decision:** Group effects by category (Static, Fade, Jump, Breathe, Strobe, Multi-color) with sticky headers
**Rationale:** 28+ effects organized by type is easier to navigate than flat list.
**Impact:** Used LazyColumn with `stickyHeader {}` for category sections. Improves discoverability of effects.

### 4. Speed Control Visibility
**Decision:** Speed slider appears via `AnimatedVisibility` only when effect is active
**Rationale:** Speed is irrelevant for static colors, clutters UI when not needed.
**Impact:** Clean interface that adapts to current state.

### 5. Color Picker Library
**Decision:** Use HarmonyColorPicker (godaddy/android-colorpicker) for circular HSV wheel
**Rationale:** Mature library with proper HSV wheel implementation, no need to build custom Canvas solution.
**Impact:** Dependency on external library, but saves significant development time and provides professional UX.

## Verification Results

**Checkpoint approved by user.** All verification steps passed:

1. ✓ Tower preview displays at top
2. ✓ Power button toggles tower on/off with color feedback
3. ✓ Color wheel updates tower in real-time
4. ✓ Preset swatches change tower color instantly
5. ✓ Brightness slider adjusts tower brightness smoothly
6. ✓ Effects grouped by category
7. ✓ Effect selection starts hardware effect
8. ✓ Speed slider appears when effect active
9. ✓ Speed adjustment changes effect speed
10. ✓ Clear button stops effect
11. ✓ Device picker works for multi-tower control
12. ✓ "All devices" option affects all connected towers

## Technical Highlights

### Debounced State Pattern
```kotlin
// In ControlViewModel
_brightness
    .debounce(33)  // ~30fps
    .distinctUntilChanged()
    .onEach { brightness ->
        sendBrightness(brightness)
    }
    .launchIn(viewModelScope)
```

UI state updates immediately (`_towerState.update { ... }`), but BLE commands are debounced. This gives smooth 60fps preview with throttled hardware commands.

### Canvas Tower Preview
```kotlin
// TowerPreviewCanvas.kt
val segmentWidth = size.width * 0.6f  // 60% width per UI-SPEC
for (i in 0 until segmentCount) {
    val segmentColor = towerState.segmentColors[i] ?: towerState.currentColor
    drawRoundRect(
        color = if (towerState.isPoweredOn) segmentColor else Color.DarkGray,
        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
    )
}
```

### Multi-Device Targeting
```kotlin
// SetColorUseCase.kt
suspend operator fun invoke(tower: Tower, color: Color) {
    // Single device
    connectionManager.setColor(tower, r, g, b)
}

suspend fun invokeAll(color: Color) {
    // All connected devices
    connectionManager.connectedTowers.value.forEach { tower ->
        connectionManager.setColor(tower, r, g, b)
    }
}
```

## Deviations from Plan

None - plan executed exactly as written. All tasks completed without issues.

## Known Issues / Technical Debt

None identified. Code follows established patterns from Plans 01 and 02.

## Next Steps

Phase 01 complete. All three plans (BLE infrastructure, device discovery, control screen) are implemented and verified. The app now supports:
- ✓ BLE scanning and connection
- ✓ Device discovery with known devices list
- ✓ Full control interface with power, color, brightness, and effects

**Ready for Phase 02:** Timeline animation editor with keyframe support.

## Commits

| Hash | Message | Files |
|------|---------|-------|
| eae2861 | feat(01-03): add control use cases and ViewModel with debounced state | TogglePowerUseCase.kt, SetColorUseCase.kt, SetBrightnessUseCase.kt, SetEffectUseCase.kt, ControlViewModel.kt |
| 31ac6be | feat(01-03): add control screen UI components | TowerPreviewCanvas.kt, PowerToggleButton.kt, ColorPickerSection.kt, BrightnessSlider.kt, DevicePicker.kt |
| f6a9267 | feat(01-03): add effects section and assemble ControlScreen | EffectsSection.kt, SpeedSlider.kt, ControlScreen.kt |

## Self-Check: PASSED

**Files verified:**
```
FOUND: app/src/main/java/com/motherledisa/ui/control/ControlScreen.kt
FOUND: app/src/main/java/com/motherledisa/ui/control/ControlViewModel.kt
FOUND: app/src/main/java/com/motherledisa/ui/control/components/TowerPreviewCanvas.kt
FOUND: app/src/main/java/com/motherledisa/ui/control/components/PowerToggleButton.kt
FOUND: app/src/main/java/com/motherledisa/ui/control/components/ColorPickerSection.kt
FOUND: app/src/main/java/com/motherledisa/ui/control/components/BrightnessSlider.kt
FOUND: app/src/main/java/com/motherledisa/ui/control/components/DevicePicker.kt
FOUND: app/src/main/java/com/motherledisa/ui/control/components/EffectsSection.kt
FOUND: app/src/main/java/com/motherledisa/ui/control/components/SpeedSlider.kt
FOUND: app/src/main/java/com/motherledisa/domain/usecase/TogglePowerUseCase.kt
FOUND: app/src/main/java/com/motherledisa/domain/usecase/SetColorUseCase.kt
FOUND: app/src/main/java/com/motherledisa/domain/usecase/SetBrightnessUseCase.kt
FOUND: app/src/main/java/com/motherledisa/domain/usecase/SetEffectUseCase.kt
```

**Commits verified:**
```
FOUND: eae2861
FOUND: 31ac6be
FOUND: f6a9267
```

All files created. All commits exist. Summary matches execution.
