---
phase: 01-ble-foundation-basic-control
plan: 02
subsystem: ui-device-discovery
tags: [ble, ui, jetpack-compose, navigation, device-management]
completed: 2026-03-28
duration: 45min

dependency_graph:
  requires:
    - 01-01 (BLE infrastructure, database, service)
  provides:
    - Device discovery UI with continuous scanning
    - Device list with badges (connected/available/last-connected)
    - Single-tap connection flow
    - Device rename capability
    - Bottom navigation structure
  affects:
    - 01-03 (Control screen will use navigation structure)

tech_stack:
  added:
    - Jetpack Compose UI framework
    - Material 3 design system
    - Navigation Compose with type-safe routes
    - StateFlow for reactive UI state
    - Hilt ViewModel injection
  patterns:
    - MVVM architecture (ViewModel + Composables)
    - Lifecycle-aware scanning (DisposableEffect)
    - Combined state streams (merge scan + known devices)
    - Type-safe navigation with @Serializable routes

key_files:
  created:
    - app/src/main/java/com/motherledisa/ui/theme/Color.kt
    - app/src/main/java/com/motherledisa/ui/theme/Type.kt
    - app/src/main/java/com/motherledisa/ui/theme/Theme.kt
    - app/src/main/java/com/motherledisa/ui/navigation/Screen.kt
    - app/src/main/java/com/motherledisa/ui/navigation/NavGraph.kt
    - app/src/main/java/com/motherledisa/domain/model/DeviceListItem.kt
    - app/src/main/java/com/motherledisa/ui/device/DeviceViewModel.kt
    - app/src/main/java/com/motherledisa/ui/device/DeviceListScreen.kt
    - app/src/main/java/com/motherledisa/ui/device/components/SignalStrengthBars.kt
    - app/src/main/java/com/motherledisa/ui/device/components/DeviceListItemRow.kt
    - app/src/main/java/com/motherledisa/ui/device/components/PermissionDialog.kt
    - app/src/main/java/com/motherledisa/ui/device/components/RenameDialog.kt
  modified:
    - app/src/main/java/com/motherledisa/MainActivity.kt (added Compose UI, permission requests)
    - app/src/main/java/com/motherledisa/ui/control/ControlScreen.kt (placeholder for navigation)

decisions:
  - Used combine() to merge scanned + known devices into single list per D-05 spec
  - Type-safe navigation with @Serializable routes (stable since Navigation 2.8.0)
  - Lifecycle-aware scanning with DisposableEffect (start on resume, stop on pause)
  - RSSI thresholds from UI-SPEC: -50, -65, -80 for 4-bar signal strength
  - Dark-only theme per UI-SPEC (no light mode toggle)
---

# Phase 01 Plan 02: Device Discovery & Navigation Summary

Device discovery screen with continuous BLE scanning, connection flow, and navigation structure using Jetpack Compose and Material 3.

## What Was Built

Complete device discovery UI with:
- **Theme system**: Dark-only Material 3 theme with purple accent (#7C4DFF) for connected states and yellow accent (#FFEB3B) for scanning
- **Navigation structure**: Type-safe bottom navigation with Devices and Control tabs
- **Device list**: Merged list showing both scanned (available) and known (previously connected) devices per D-05 spec
- **Signal strength**: Custom Canvas component with 4-bar RSSI display (thresholds: -50, -65, -80 dBm)
- **Connection flow**: Single-tap connect with loading indicator and "Connected" badge
- **Device badges**: Connected (purple), Last connected (time-relative)
- **Rename capability**: Long-press device to rename via dialog
- **Lifecycle-aware scanning**: Automatically starts scanning on screen resume, stops on pause

## Implementation Details

### Architecture

**MVVM pattern with StateFlow:**
- DeviceViewModel combines 3 StateFlows (scanned devices + known devices + connected towers)
- UI components collect state and emit events upward
- Hilt provides ViewModel injection with `hiltViewModel()`

**Navigation:**
- Type-safe routes with `@Serializable` data classes
- Bottom NavigationBar with Material 3 NavigationBarItem
- NavHost manages screen composition

### Key Components

**DeviceListItem (sealed class):**
```kotlin
sealed class DeviceListItem {
    data class Available(...)  // Currently visible via scan
    data class Known(...)      // Previously connected, not visible
}
```

**DeviceViewModel:**
- `combine()` merges scanner.discoveredDevices + towerConfigDao.getAllKnownDevices() + connectionManager.connectedTowers
- Sorts by lastConnected timestamp (most recent first)
- Exposes `deviceList: StateFlow<List<DeviceListItem>>`

**SignalStrengthBars:**
- Custom Canvas drawing 4 bars with rounded corners
- RSSI to bars mapping per UI-SPEC
- Accessibility contentDescription: "Signal strength [excellent/good/fair/weak]"

**DeviceListScreen:**
- DisposableEffect with LifecycleEventObserver for scan lifecycle
- LaunchedEffect navigates to Control screen after successful connection
- LazyColumn with combinedClickable (tap = connect, long-press = rename)

## Verification Results

**User approved checkpoint verification:**
- App builds and installs successfully
- Device list shows nearby MELK/ELK devices with signal bars
- Single tap connects with loading indicator
- Connected devices show purple "Connected" badge
- Known devices show "Last connected" text when out of range
- Bottom navigation works between screens
- Rename dialog appears on long-press

## Deviations from Plan

None - plan executed exactly as written. All tasks completed without bugs, missing critical functionality, or blocking issues.

## Requirements Satisfied

- **BLE-01**: Scan for nearby MELK/ELK devices (continuous scanning while on screen)
- **BLE-02**: Connect to device with single tap
- **BLE-03**: Device connection state tracking (via ConnectionState flow)
- **BLE-05**: Display connected devices with badges
- **BLE-06**: Remember known devices (merged with scan results per D-05)
- **UX-01**: Device list screen with navigation
- **UX-07**: Bottom navigation between Devices and Control screens

## Known Limitations

None. All D-xx requirements from UI-SPEC implemented:
- D-01: Continuous scanning ✓
- D-02: Signal bars and badges ✓
- D-03: Single-tap connection ✓
- D-04: Empty state with retry ✓
- D-05: Merged device list (scanned + known) ✓
- D-16: Long-press rename ✓
- D-17: Permission dialog (in MainActivity) ✓

## Testing Notes

Manual verification performed:
- Scanning lifecycle (starts on resume, stops on pause)
- Connection flow (loading indicator, navigation on success)
- Device badges (connected, last connected)
- Signal strength bars (4 thresholds)
- Rename dialog (long-press to open, save updates database)
- Bottom navigation (tap switches screens)

## Next Steps

**For Plan 01-03 (Control Screen):**
- Use `Screen.Control(deviceAddress)` from navigation
- Implement power toggle, color wheel, brightness slider
- Add effects grid with built-in MELK modes
- Send commands via TowerConnectionManager

## Self-Check: PASSED

**Created files verified:**
```
✓ app/src/main/java/com/motherledisa/ui/theme/Color.kt
✓ app/src/main/java/com/motherledisa/ui/theme/Type.kt
✓ app/src/main/java/com/motherledisa/ui/theme/Theme.kt
✓ app/src/main/java/com/motherledisa/ui/navigation/Screen.kt
✓ app/src/main/java/com/motherledisa/ui/navigation/NavGraph.kt
✓ app/src/main/java/com/motherledisa/domain/model/DeviceListItem.kt
✓ app/src/main/java/com/motherledisa/ui/device/DeviceViewModel.kt
✓ app/src/main/java/com/motherledisa/ui/device/DeviceListScreen.kt
✓ app/src/main/java/com/motherledisa/ui/device/components/SignalStrengthBars.kt
✓ app/src/main/java/com/motherledisa/ui/device/components/DeviceListItemRow.kt
✓ app/src/main/java/com/motherledisa/ui/device/components/PermissionDialog.kt
✓ app/src/main/java/com/motherledisa/ui/device/components/RenameDialog.kt
```

**Commits verified:**
```
✓ 5731120 feat(01-02): create theme, navigation structure, and permission handling
✓ 9922c33 feat(01-02): create DeviceViewModel with scanning and connection logic
✓ 7ad2cb8 feat(01-02): create DeviceListScreen and components
```

All files exist, all commits present, all requirements satisfied.
