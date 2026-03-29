# Phase 4: Multi-Tower Orchestration - Research

**Researched:** 2026-03-29
**Domain:** Multi-device BLE coordination, UI orchestration patterns, timed animation playback
**Confidence:** HIGH

## Summary

Multi-tower orchestration extends the existing `TowerConnectionManager` and `AnimationPlayer` infrastructure to coordinate animations across multiple connected towers. The phase requires: (1) a new Orchestrate tab with mode selection via Material 3 `SegmentedButton`, (2) drag-to-reorder tower ordering via the `Reorderable` library, (3) timing logic for offset/cascade modes built into `AnimationPlayer`, and (4) independent per-tower animation assignment.

The codebase is well-prepared for this phase. `TowerConnectionManager` already has `*All()` methods for multi-tower control. `TowerConfigEntity` already has a `position` field for tower ordering. `AnimationPlayer` already supports `targetTower: ConnectedTower?` where `null` means all towers. The primary work is adding offset/cascade timing logic and the new Orchestrate UI screen.

**Primary recommendation:** Implement orchestration modes as state in a new `OrchestrationManager` that wraps `AnimationPlayer` calls with timing delays. Use `SingleChoiceSegmentedButtonRow` for mode selection and `sh.calvin.reorderable:reorderable` for drag-drop tower ordering.

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions
- **D-01:** Drag-and-drop vertical list for tower ordering. Tap and hold to drag, familiar reorderable list pattern.
- **D-02:** Global default tower order saved in app preferences, with per-animation override option when saving presets.
- **D-03:** Horizontal segmented control for mode switching: Mirror | Offset | Cascade | Independent. Quick visual toggle.
- **D-04:** Mode selection is global across all towers when orchestrating -- not per-tower setting.
- **D-05:** Dropdown per tower to select which animation/preset each tower plays. Compact, fits existing DevicePicker pattern.
- **D-06:** Single delay slider (0-2000ms range) -- same delay applied between each consecutive tower pair. Tower 1 starts, tower 2 starts after delay, tower 3 starts after another delay, etc.
- **D-07:** Immediate handoff -- tower 2 starts instantly when tower 1 completes its animation cycle. Clean relay effect with no overlap or gap.
- **D-08:** Accept BLE latency variance (~20-50ms between towers). No compensation logic -- slight variance is acceptable for home use.
- **D-09:** Dedicated "Orchestrate" tab in bottom navigation for multi-tower settings.
- **D-10:** Tab order: Devices | Control | Orchestrate | Sound | Presets (5 tabs total).
- **D-11:** Orchestrate tab visible but disabled when <2 towers connected. Shows "Connect more towers" message.

### Claude's Discretion
- Exact slider range for offset delay
- Orchestrate tab icon choice
- Empty state design for <2 towers
- Tower preview visualization in orchestrate screen
- Whether to show live preview of offset/cascade timing

### Deferred Ideas (OUT OF SCOPE)
- Per-pair offset delays (more granular control) -- evaluate after basic offset works
- Overlap/gap options for cascade mode -- could add later if requested
- Visual room layout for tower positioning -- complex UI, defer to v2
- BLE latency compensation -- accept variance for now

</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| MULTI-01 | User can control multiple towers simultaneously | Existing `TowerConnectionManager.*All()` methods; Mirror mode uses existing pattern |
| MULTI-02 | User can enable mirror mode (all towers show same animation) | `AnimationPlayer.play(animation, tower=null)` already sends to all towers |
| MULTI-03 | User can enable offset mode (staggered timing across towers) | New `OrchestrationManager` with `delay()` between tower starts per D-06 |
| MULTI-04 | User can enable cascade mode (tower-to-tower relay) | Detect animation completion via `playbackState` flow, trigger next tower per D-07 |
| MULTI-05 | User can define tower ordering for offset/cascade modes | Existing `TowerConfigEntity.position` + `TowerConfigDao.getOrderedDevices()` + Reorderable library |
| MULTI-06 | User can enable independent mode (each tower controlled separately) | Per-tower animation assignment map + dropdown selector per D-05 |
| UX-05 | App has dedicated screen for multi-tower orchestration | New `OrchestrateScreen` composable + 5th nav tab per D-09/D-10 |

</phase_requirements>

## Standard Stack

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Material 3 SegmentedButton | via Compose BOM 2025.01.01 | Mode selection UI | Official M3 component for single-choice horizontal toggles |
| sh.calvin.reorderable | 3.0.0 | Drag-drop tower reordering | Industry standard for Compose, uses `Modifier.animateItem` API, well-maintained |
| Compose Foundation | via BOM | LazyColumn for tower list | Core framework, provides `rememberLazyListState` |

### Supporting
| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| kotlinx.coroutines | 1.9.0 (existing) | Timing delays for offset/cascade | `delay()` for staggered tower starts |
| Room | 2.6.1 (existing) | Tower position persistence | Save/load tower ordering via `TowerConfigDao` |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| Reorderable library | Custom drag modifier | More code, edge cases for scroll + drag, no animation support |
| SegmentedButton | FilterChip row | FilterChip used in Phase 2 for loop modes, but SegmentedButton is more appropriate for mutually exclusive mode selection |
| Material Icons.Default.Hub | Icons.Default.Sync | Hub better conveys multi-device orchestration concept |

**Installation:**
```bash
# Add to app/build.gradle.kts dependencies:
implementation("sh.calvin.reorderable:reorderable:3.0.0")
```

**Version verification:** The Reorderable library version 3.0.0 is current as of March 2026 per Maven Central. Uses the new `Modifier.animateItem` API introduced in Compose Foundation 1.7.0.

## Architecture Patterns

### Recommended Project Structure
```
app/src/main/java/com/motherledisa/
├── domain/
│   └── orchestration/
│       └── OrchestrationManager.kt    # Orchestration timing logic
├── ui/
│   └── orchestrate/
│       ├── OrchestrateScreen.kt       # Main orchestration screen
│       ├── OrchestrateViewModel.kt    # Screen state management
│       └── components/
│           ├── OrchestrationModeSelector.kt  # SegmentedButton mode picker
│           ├── TowerOrderList.kt              # Reorderable tower list
│           ├── OffsetDelaySlider.kt           # 0-2000ms delay slider
│           └── IndependentTowerConfig.kt      # Per-tower animation dropdown
└── navigation/
    └── Screen.kt                       # Add Orchestrate route
```

### Pattern 1: OrchestrationManager as Coordinator
**What:** A singleton that wraps `AnimationPlayer` and `TowerConnectionManager` to add timing logic for offset/cascade modes.
**When to use:** When playing animations with multi-tower coordination.
**Example:**
```kotlin
// Source: Project architecture pattern
@Singleton
class OrchestrationManager @Inject constructor(
    private val connectionManager: TowerConnectionManager,
    private val animationPlayer: AnimationPlayer
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _orchestrationMode = MutableStateFlow(OrchestrationMode.MIRROR)
    val orchestrationMode: StateFlow<OrchestrationMode> = _orchestrationMode.asStateFlow()

    private val _offsetDelayMs = MutableStateFlow(500)
    val offsetDelayMs: StateFlow<Int> = _offsetDelayMs.asStateFlow()

    fun playOrchestrated(animation: Animation, orderedTowers: List<ConnectedTower>) {
        when (_orchestrationMode.value) {
            OrchestrationMode.MIRROR -> playMirror(animation, orderedTowers)
            OrchestrationMode.OFFSET -> playOffset(animation, orderedTowers)
            OrchestrationMode.CASCADE -> playCascade(animation, orderedTowers)
            OrchestrationMode.INDEPENDENT -> { /* handled separately */ }
        }
    }

    private fun playOffset(animation: Animation, towers: List<ConnectedTower>) {
        scope.launch {
            towers.forEachIndexed { index, tower ->
                delay(index * _offsetDelayMs.value.toLong())
                animationPlayer.play(animation, tower)
            }
        }
    }
}
```

### Pattern 2: Cascade Mode via PlaybackState Observation
**What:** Monitor `AnimationPlayer.playbackState` flow to detect when one tower completes, then trigger next.
**When to use:** Cascade mode (D-07: immediate handoff).
**Example:**
```kotlin
// Source: Project architecture pattern
private fun playCascade(animation: Animation, towers: List<ConnectedTower>) {
    scope.launch {
        var currentIndex = 0

        while (currentIndex < towers.size) {
            val tower = towers[currentIndex]
            animationPlayer.play(animation, tower)

            // Wait for this tower's animation to complete
            animationPlayer.playbackState
                .filter { it == PlaybackState.STOPPED }
                .first()

            currentIndex++
        }
    }
}
```

### Pattern 3: SegmentedButton for Mode Selection
**What:** Material 3 `SingleChoiceSegmentedButtonRow` for the 4 orchestration modes.
**When to use:** D-03 mode selection UI.
**Example:**
```kotlin
// Source: Official Android documentation
@Composable
fun OrchestrationModeSelector(
    selectedMode: OrchestrationMode,
    onModeSelected: (OrchestrationMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val modes = OrchestrationMode.entries

    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        modes.forEachIndexed { index, mode ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = index, count = modes.size),
                onClick = { onModeSelected(mode) },
                selected = mode == selectedMode,
                label = { Text(mode.displayName) }
            )
        }
    }
}
```

### Pattern 4: Reorderable LazyColumn for Tower Ordering
**What:** Use `sh.calvin.reorderable` for drag-and-drop tower reordering.
**When to use:** D-01 tower ordering UI.
**Example:**
```kotlin
// Source: Calvin-LL/Reorderable GitHub README
@Composable
fun TowerOrderList(
    towers: List<ConnectedTower>,
    onReorder: (from: Int, to: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        onReorder(from.index, to.index)
    }

    LazyColumn(
        state = lazyListState,
        modifier = modifier
    ) {
        items(towers, key = { it.address }) { tower ->
            ReorderableItem(reorderableLazyListState, key = tower.address) { isDragging ->
                val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp)

                Surface(tonalElevation = elevation) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.DragHandle,
                            contentDescription = "Reorder",
                            modifier = Modifier.draggableHandle()
                        )
                        Spacer(Modifier.width(16.dp))
                        Text(tower.name)
                    }
                }
            }
        }
    }
}
```

### Anti-Patterns to Avoid
- **Anti-pattern: Hardcoded tower ordering** -- Always use the persisted position from `TowerConfigEntity`, not connection order.
- **Anti-pattern: Blocking BLE calls in timing logic** -- Use `delay()` between tower starts, not synchronous waits for BLE confirmation.
- **Anti-pattern: Multiple AnimationPlayer instances** -- Reuse single `AnimationPlayer`; orchestration coordinates WHEN each tower starts.
- **Anti-pattern: Modifying list during reorder** -- Use `rememberReorderableLazyListState` callback to update state, never mutate list directly.

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Drag-and-drop reordering | Custom pointer input + offset tracking | `sh.calvin.reorderable` | Handles scroll detection, animations, edge cases |
| Mode toggle buttons | Row of Buttons with manual selection state | `SingleChoiceSegmentedButtonRow` | Official M3 component with proper styling, accessibility |
| Tower position persistence | SharedPreferences for ordering | Existing Room `TowerConfigDao.updatePosition()` | Already set up, type-safe, observable via Flow |
| Staggered delays | Manual Thread.sleep or Handler | `kotlinx.coroutines.delay()` | Non-blocking, cancellable, integrates with structured concurrency |

**Key insight:** The existing codebase already has infrastructure for multi-tower control (`*All()` methods), tower position storage (`TowerConfigEntity.position`), and animation targeting (`AnimationPlayer.targetTower`). Phase 4 orchestrates these existing pieces with timing logic rather than building new BLE primitives.

## Common Pitfalls

### Pitfall 1: BLE Command Queue Flooding
**What goes wrong:** Starting multiple tower animations simultaneously floods BLE command queues, causing GATT errors.
**Why it happens:** Each tower has its own command queue, but Bluetooth radio is shared.
**How to avoid:** Per D-08, accept 20-50ms variance. Add small delay (10-20ms) between initial commands to different towers even in Mirror mode.
**Warning signs:** GATT_ERROR 133 in logs, commands not reaching devices.

### Pitfall 2: Tower Order Not Persisted
**What goes wrong:** User reorders towers, but on app restart order reverts to connection order.
**Why it happens:** Forgetting to call `TowerConfigDao.updatePosition()` after drag-drop.
**How to avoid:** In `onReorder` callback, immediately update positions in Room. Use `viewModelScope.launch` for the database update.
**Warning signs:** Tower order changes lost after navigating away from Orchestrate screen.

### Pitfall 3: Cascade Mode Not Detecting Completion
**What goes wrong:** Cascade mode doesn't trigger next tower; animation plays on one tower only.
**Why it happens:** `AnimationPlayer.playbackState` not being observed correctly, or filter condition wrong.
**How to avoid:** Use `animationPlayer.playbackState.filter { it == PlaybackState.STOPPED }.first()` with proper coroutine scope.
**Warning signs:** Only first tower plays in cascade mode.

### Pitfall 4: Independent Mode Presets Not Found
**What goes wrong:** User selects a preset for a tower in Independent mode, but animation doesn't exist.
**Why it happens:** Preset deleted after being assigned; ID stored but not resolved.
**How to avoid:** Store animation ID, resolve to Animation object at play time; handle null gracefully with toast.
**Warning signs:** No animation plays for specific tower, others work fine.

### Pitfall 5: SegmentedButton Shape Issues
**What goes wrong:** Segmented buttons have wrong corner radius or gaps between them.
**Why it happens:** Not using `SegmentedButtonDefaults.itemShape(index, count)` for each button.
**How to avoid:** Always pass index and total count to `itemShape()` to get proper start/middle/end shapes.
**Warning signs:** Buttons look like separate chips rather than connected segments.

### Pitfall 6: Reorderable Scroll Conflicts
**What goes wrong:** Dragging items near screen edges doesn't auto-scroll the list.
**Why it happens:** Missing `scrollThresholdPadding` for system bars (navigation bar, status bar).
**How to avoid:** Configure `rememberReorderableLazyListState` with `scrollThresholdPadding = WindowInsets.systemBars.asPaddingValues()`.
**Warning signs:** Can't drag items to beginning/end of long list.

## Code Examples

Verified patterns from official sources and project conventions:

### OrchestrationMode Enum
```kotlin
// Source: Project convention
enum class OrchestrationMode(val displayName: String) {
    MIRROR("Mirror"),
    OFFSET("Offset"),
    CASCADE("Cascade"),
    INDEPENDENT("Independent")
}
```

### OrchestrateViewModel Pattern
```kotlin
// Source: Project convention (following SoundReactiveViewModel pattern)
@HiltViewModel
class OrchestrateViewModel @Inject constructor(
    private val connectionManager: TowerConnectionManager,
    private val orchestrationManager: OrchestrationManager,
    private val towerConfigDao: TowerConfigDao,
    private val animationRepository: AnimationRepository
) : ViewModel() {

    /** Connected towers in user-defined order */
    val orderedTowers: StateFlow<List<ConnectedTower>> = combine(
        connectionManager.connectedTowers,
        towerConfigDao.getOrderedDevices()
    ) { connected, ordered ->
        // Sort connected towers by their saved position
        connected.sortedBy { tower ->
            ordered.find { it.address == tower.address }?.position ?: Int.MAX_VALUE
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val orchestrationMode: StateFlow<OrchestrationMode> = orchestrationManager.orchestrationMode
    val offsetDelayMs: StateFlow<Int> = orchestrationManager.offsetDelayMs

    /** Independent mode: animation ID per tower address */
    private val _towerAnimations = MutableStateFlow<Map<String, Long>>(emptyMap())
    val towerAnimations: StateFlow<Map<String, Long>> = _towerAnimations.asStateFlow()

    /** Available animations for dropdown */
    val animations: StateFlow<List<Animation>> = animationRepository
        .getAllAnimations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setMode(mode: OrchestrationMode) {
        orchestrationManager.setMode(mode)
    }

    fun setOffsetDelay(delayMs: Int) {
        orchestrationManager.setOffsetDelay(delayMs)
    }

    fun reorderTowers(fromIndex: Int, toIndex: Int) {
        viewModelScope.launch {
            val towers = orderedTowers.value.toMutableList()
            val item = towers.removeAt(fromIndex)
            towers.add(toIndex, item)

            // Persist new positions
            towers.forEachIndexed { index, tower ->
                towerConfigDao.updatePosition(tower.address, index)
            }
        }
    }

    fun setTowerAnimation(towerAddress: String, animationId: Long) {
        _towerAnimations.update { it + (towerAddress to animationId) }
    }
}
```

### Screen Route Addition
```kotlin
// Source: Project convention (Screen.kt pattern)
@Serializable
data object Orchestrate : Screen()
```

### NavGraph 5-Tab Addition
```kotlin
// Source: Project convention (NavGraph.kt pattern)
// Insert between Control and Sound per D-10
NavigationBarItem(
    selected = currentDestination?.hasRoute<Screen.Orchestrate>() == true,
    onClick = {
        navController.navigate(Screen.Orchestrate) {
            popUpTo(Screen.DeviceList) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    },
    icon = { Icon(Icons.Default.Hub, contentDescription = "Orchestrate") },
    label = { Text("Orchestrate") },
    enabled = connectedTowerCount >= 2  // D-11: disabled when <2 towers
)
```

### Empty State for <2 Towers (D-11)
```kotlin
// Source: Project convention (following ControlScreen empty state pattern)
if (orderedTowers.size < 2) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.DevicesOther,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Connect more towers",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Orchestration requires at least 2 connected towers",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
    return
}
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| `Modifier.animateItemPlacement()` | `Modifier.animateItem()` | Compose Foundation 1.7.0-alpha06 | Reorderable library v3.0 uses new API |
| Custom segmented buttons | `SingleChoiceSegmentedButtonRow` | Material 3 1.2.0+ | Official M3 component available |
| ItemTouchHelper (Views) | Reorderable library | Compose migration | Different paradigm, library abstracts complexity |

**Deprecated/outdated:**
- `Modifier.animateItemPlacement()`: Replaced by `Modifier.animateItem()` in Compose Foundation 1.7.0+

## Open Questions

1. **Icon for Orchestrate tab**
   - What we know: `Icons.Default.Hub` conveys multi-device concept well. Alternatives: `Icons.Default.Sync`, `Icons.Default.DevicesOther`, `Icons.Default.AccountTree`
   - What's unclear: User preference for visual metaphor
   - Recommendation: Use `Icons.Default.Hub` as it visually represents connected devices radiating from center. Falls under Claude's discretion.

2. **Live preview of offset/cascade timing**
   - What we know: D-09 mentions tower preview visualization is Claude's discretion
   - What's unclear: Performance impact of animating 4+ tower previews simultaneously
   - Recommendation: Start with static tower cards showing position numbers. Add animated preview in future iteration if requested.

3. **Per-animation tower order override (D-02)**
   - What we know: Global order saved in `TowerConfigEntity.position`. Per-animation override mentioned.
   - What's unclear: Data model for per-animation override -- add to `AnimationEntity`?
   - Recommendation: Defer per-animation override to future iteration. Implement global ordering first. If needed, add `towerOrderJson` field to `AnimationEntity` later.

## Environment Availability

> Skip -- Phase 4 requires no external dependencies beyond what is already installed for Phases 1-3.

## Sources

### Primary (HIGH confidence)
- [Android Segmented Button Docs](https://developer.android.com/develop/ui/compose/components/segmented-button) - Official API and usage
- [Calvin-LL/Reorderable GitHub](https://github.com/Calvin-LL/Reorderable) - Drag-drop library v3.0.0
- Project codebase: `TowerConnectionManager.kt`, `AnimationPlayer.kt`, `TowerConfigDao.kt`

### Secondary (MEDIUM confidence)
- [Punch Through Android BLE Guide](https://punchthrough.com/android-ble-guide/) - Multi-device BLE patterns
- [Material Symbols Icons](https://m3.material.io/styles/icons) - Icon naming conventions

### Tertiary (LOW confidence)
- Various Medium articles on Compose drag-drop -- verified against official library docs

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH - Official M3 components, well-maintained library
- Architecture: HIGH - Extends existing patterns from Phases 1-3
- Pitfalls: HIGH - Based on established BLE patterns in codebase

**Research date:** 2026-03-29
**Valid until:** 2026-04-28 (30 days - stable domain)
