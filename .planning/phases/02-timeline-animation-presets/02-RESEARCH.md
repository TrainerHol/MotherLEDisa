# Phase 2: Timeline Animation & Presets - Research

**Researched:** 2026-03-28
**Domain:** Timeline Animation Editor, Keyframe Interpolation, Preset Persistence
**Confidence:** HIGH

## Summary

Phase 2 delivers the core creative tooling that differentiates MotherLEDisa from preset-only apps: a timeline-based animation editor with keyframe control over color, position (segment), and timing. The implementation builds on existing Jetpack Compose Canvas patterns from Phase 1 (TowerPreviewCanvas) and extends the Room database (AppDatabase) with animation/preset entities.

The architecture splits into three major areas: (1) Timeline Editor UI using Compose Canvas with horizontal scrolling and gesture detection for keyframe manipulation, (2) Animation Engine using coroutine-based playback loops that leverage the existing TowerCommandQueue for BLE command scheduling, and (3) Preset Persistence using Room with Kotlinx Serialization TypeConverters for complex keyframe data.

**Primary recommendation:** Build a custom timeline composable using Canvas + pointerInput with horizontal scroll, reuse ColorPickerSection and TowerPreviewCanvas from Phase 1, store keyframe data as JSON in Room entities with @ProvidedTypeConverter for serialization, and implement playback via a coroutine-based ticker that respects the 30fps debouncing established in Phase 1.

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions
- **D-01:** Horizontal timeline layout (After Effects-style) — time flows left-to-right, segment tracks stacked vertically
- **D-02:** Diamond markers on tracks for keyframes, colored by the keyframe's color value
- **D-03:** Long-press on track to add new keyframe — brings up context menu with "Add keyframe" option
- **D-04:** Playhead is draggable vertical line spanning all tracks — drag to scrub with live preview
- **D-05:** One track per tower segment (5 segments = 5 horizontal tracks) — clearest visualization for customization
- **D-06:** Each keyframe controls: Color (RGB), Segment/position, Brightness, and optional Effect trigger
- **D-07:** User chooses interpolation mode per keyframe — smooth blend (HSV) vs step/hold
- **D-08:** HSV interpolation for smooth color transitions (red->orange->yellow->green flow)
- **D-09:** Fixed tower preview above timeline — always visible during editing
- **D-10:** Play button triggers both in-app preview AND device simultaneously for instant feedback
- **D-11:** Full loop options: loop count (once, 2x, 3x, infinite) plus ping-pong mode (A->B->A)
- **D-12:** Transport controls: Play, Pause, Stop — consistent with Phase 1 debounced command pattern (~30fps)
- **D-13:** Grid layout with animation name and visual thumbnail (Preset Library)
- **D-14:** Single tap to preview, long-press for options (rename, delete, duplicate)
- **D-15:** Presets stored in Room database with JSON-serialized keyframe data
- **D-16:** "Apply" sends preset animation to currently selected device(s)

### Claude's Discretion
- Time ruler scale and zoom/pan gestures
- Keyframe selection UI (multi-select, lasso, etc.)
- Copy/paste keyframe functionality
- Easing curves beyond linear/step
- Preset thumbnail generation approach
- Animation duration limits (if any)

### Deferred Ideas (OUT OF SCOPE)
- Sound-reactive animation triggers — Phase 3
- Multi-tower choreography (offset/cascade timing) — Phase 4
- Pattern generators (sine waves, gradients) — could be separate tool or Phase 2 extension
- Advanced easing curves (bezier) — evaluate after basic interpolation works
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| ANIM-01 | User can create animation with keyframes on a timeline | Timeline Canvas with horizontal scroll, long-press to add keyframes (D-03) |
| ANIM-02 | User can set color for each keyframe | ColorPickerSection reuse, Color stored in Keyframe entity |
| ANIM-03 | User can set position/segment for each keyframe | 5 horizontal tracks (D-05), segment index in Keyframe entity |
| ANIM-04 | User can drag keyframes to adjust timing | detectDragGestures + pointerInput on Canvas |
| ANIM-05 | User can preview animation in app | Animatable-driven playback updating TowerPreviewCanvas |
| ANIM-06 | User can play animation on connected tower(s) | Coroutine playback loop + TowerCommandQueue |
| ANIM-07 | User can pause and stop animation playback | PlaybackState enum with transport controls |
| ANIM-08 | User can create patterns (sine waves, gradients) | DEFERRED per CONTEXT.md |
| PRESET-01 | User can save current animation as named preset | Room AnimationEntity with JSON keyframes |
| PRESET-02 | User can view list of saved presets | LazyVerticalGrid with preset cards |
| PRESET-03 | User can apply saved preset to connected tower(s) | Load from Room, start playback |
| PRESET-04 | User can delete saved presets | Room DAO delete operation |
| PRESET-05 | Presets persist across app restarts | Room database storage |
| UX-03 | App has dedicated screen for timeline animation editor | AnimationEditor screen + navigation route |
| UX-06 | App has dedicated screen for preset library | PresetLibrary screen + navigation route |
</phase_requirements>

## Project Constraints (from CLAUDE.md)

The following constraints from CLAUDE.md MUST be honored:

- **Platform:** Android (Kotlin) — native BLE required for low-latency control
- **Stack:** Jetpack Compose with BOM 2025.x, Material 3, Navigation Compose with @Serializable routes
- **BLE:** Nordic Android-BLE-Library 2.11.0 — use existing TowerCommandQueue pattern
- **State:** StateFlow + MVVM architecture, ViewModel with SavedStateHandle
- **DI:** Hilt with KSP
- **Database:** Room with KSP code generation
- **Serialization:** Kotlinx Serialization for JSON (preset export/import)
- **No RxJava/KAPT/LiveData** — use coroutines and StateFlow

## Standard Stack

### Core (Already in build.gradle.kts)
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Compose BOM | 2025.01.01 | UI Framework | Already configured, Canvas APIs included |
| Room | 2.6.1 | Local Database | Already configured; upgrade to 2.8.4 recommended for better migrations |
| Kotlinx Serialization | 1.7.3 | JSON Encoding | Already configured for keyframe data |
| Navigation Compose | 2.8.5 | Screen Navigation | Already configured with type-safe routes |

### Supporting (No New Dependencies Required)
| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| ColorUtils (androidx.core) | 1.15.0 | HSL Color Conversion | Already included via core-ktx; use for HSV interpolation |
| Coroutines | 1.9.0 | Animation Loop | Already configured; use tickerFlow pattern for playback |
| compose-color-picker | 0.7.0 | Color Selection | Already configured; reuse ColorPickerSection |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| ColorUtils HSL | colormath library | colormath adds 3rd party dep; ColorUtils is already bundled and sufficient for HSV lerp |
| Custom Canvas timeline | 3rd party timeline lib | No mature Compose timeline libraries exist; custom Canvas is standard approach |
| Flow-based ticker | Animatable keyframes spec | Compose keyframes is for UI animation, not BLE command scheduling |

**Installation:**
No new dependencies required. Recommend upgrading Room:
```kotlin
// In build.gradle.kts
implementation("androidx.room:room-runtime:2.8.4")
implementation("androidx.room:room-ktx:2.8.4")
ksp("androidx.room:room-compiler:2.8.4")
```

**Version verification:**
- Room 2.8.4: Latest stable (November 2025) with improved schema validation
- Current project uses 2.6.1 — migration path is straightforward

## Architecture Patterns

### Recommended Project Structure
```
app/src/main/java/com/motherledisa/
├── domain/
│   ├── model/
│   │   ├── Animation.kt          # Animation with keyframes
│   │   ├── Keyframe.kt           # Single keyframe data
│   │   └── Preset.kt             # Preset wrapper
│   └── usecase/
│       ├── PlayAnimationUseCase.kt
│       ├── SavePresetUseCase.kt
│       └── LoadPresetsUseCase.kt
├── data/
│   ├── local/
│   │   ├── AnimationEntity.kt    # Room entity
│   │   ├── AnimationDao.kt       # Room DAO
│   │   ├── KeyframeConverter.kt  # JSON TypeConverter
│   │   └── AppDatabase.kt        # Add new entities
│   └── repository/
│       └── AnimationRepository.kt
├── ui/
│   ├── animation/
│   │   ├── AnimationEditorScreen.kt
│   │   ├── AnimationEditorViewModel.kt
│   │   └── components/
│   │       ├── TimelineCanvas.kt      # Main timeline editor
│   │       ├── TimelineTrack.kt       # Single segment track
│   │       ├── KeyframeMarker.kt      # Diamond keyframe
│   │       ├── PlayheadLine.kt        # Draggable playhead
│   │       ├── TimeRuler.kt           # Time scale at top
│   │       └── TransportControls.kt   # Play/Pause/Stop
│   └── preset/
│       ├── PresetLibraryScreen.kt
│       ├── PresetViewModel.kt
│       └── components/
│           ├── PresetCard.kt
│           └── PresetDialog.kt
└── navigation/
    └── Screen.kt                 # Add new routes
```

### Pattern 1: Timeline Canvas with Gesture Detection
**What:** Custom Canvas composable with horizontal scroll and drag gestures
**When to use:** For the timeline editor (D-01, D-04)
**Example:**
```kotlin
// Source: Official Android docs + SmartToolFactory tutorials
@Composable
fun TimelineCanvas(
    keyframes: List<Keyframe>,
    playheadPosition: Float,
    onKeyframeDragged: (Keyframe, Float) -> Unit,
    onPlayheadDragged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var scale by remember { mutableFloatStateOf(1f) }

    Box(
        modifier = modifier
            .horizontalScroll(scrollState)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.5f, 3f)
                }
            }
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxHeight()
                .width((1000 * scale).dp)  // Scaled width
                .pointerInput(keyframes) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            // Hit-test keyframes
                        },
                        onDrag = { change, dragAmount ->
                            // Update keyframe position
                            change.consume()
                        }
                    )
                }
        ) {
            // Draw tracks, keyframes, playhead
        }
    }
}
```

### Pattern 2: HSV Color Interpolation
**What:** Smooth color transitions using HSL (Android's ColorUtils) with shortest-path hue handling
**When to use:** Animation playback between keyframes (D-07, D-08)
**Example:**
```kotlin
// Source: Android ColorUtils API + color theory best practices
fun interpolateColorHSV(color1: Color, color2: Color, fraction: Float): Color {
    val hsl1 = FloatArray(3)
    val hsl2 = FloatArray(3)

    ColorUtils.colorToHSL(color1.toArgb(), hsl1)
    ColorUtils.colorToHSL(color2.toArgb(), hsl2)

    // Handle hue wrapping (shortest path around color wheel)
    var hueDiff = hsl2[0] - hsl1[0]
    if (hueDiff > 180) hueDiff -= 360
    if (hueDiff < -180) hueDiff += 360

    val resultHsl = floatArrayOf(
        (hsl1[0] + hueDiff * fraction + 360) % 360,
        hsl1[1] + (hsl2[1] - hsl1[1]) * fraction,
        hsl1[2] + (hsl2[2] - hsl1[2]) * fraction
    )

    return Color(ColorUtils.HSLToColor(resultHsl))
}
```

### Pattern 3: Room Entity with JSON TypeConverter
**What:** Store complex keyframe data as JSON string in Room
**When to use:** Animation/preset persistence (D-15, PRESET-05)
**Example:**
```kotlin
// Source: Official Room docs + best practices
@Serializable
data class Keyframe(
    val timeMs: Long,
    val segment: Int,
    val color: Int,  // ARGB int for serialization
    val brightness: Int,
    val effectId: Byte? = null,
    val interpolation: InterpolationMode = InterpolationMode.SMOOTH
)

@Serializable
enum class InterpolationMode { SMOOTH, STEP }

@Entity(tableName = "animations")
data class AnimationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val durationMs: Long,
    val keyframesJson: String,  // JSON-serialized List<Keyframe>
    val loopMode: LoopMode = LoopMode.ONCE,
    val loopCount: Int = 1,
    val createdAt: Long = System.currentTimeMillis()
)

@ProvidedTypeConverter
class KeyframeListConverter(private val json: Json) {
    @TypeConverter
    fun fromJson(value: String): List<Keyframe> =
        json.decodeFromString(value)

    @TypeConverter
    fun toJson(keyframes: List<Keyframe>): String =
        json.encodeToString(keyframes)
}
```

### Pattern 4: Coroutine Animation Playback Loop
**What:** Flow-based ticker for animation playback synced with BLE commands
**When to use:** Playing animations on device (D-10, D-12, ANIM-06)
**Example:**
```kotlin
// Source: Kotlin coroutines + Phase 1 debouncing pattern
class AnimationPlayer(
    private val connectionManager: TowerConnectionManager,
    private val setColorUseCase: SetColorUseCase
) {
    private val _playbackState = MutableStateFlow(PlaybackState.STOPPED)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private var playbackJob: Job? = null

    fun play(animation: Animation, targetAddress: String?) {
        playbackJob?.cancel()
        _playbackState.value = PlaybackState.PLAYING

        playbackJob = CoroutineScope(Dispatchers.Default).launch {
            val frameDelayMs = 33L  // ~30fps (D-12)
            var currentTimeMs = 0L

            while (isActive && currentTimeMs <= animation.durationMs) {
                val frameColors = animation.evaluateAt(currentTimeMs)

                // Send to device via existing use cases
                frameColors.forEach { (segment, color) ->
                    setColorUseCase.invokeSegment(targetAddress, segment, color)
                }

                delay(frameDelayMs)
                currentTimeMs += frameDelayMs

                // Handle looping
                if (currentTimeMs > animation.durationMs) {
                    when (animation.loopMode) {
                        LoopMode.INFINITE -> currentTimeMs = 0
                        LoopMode.PING_PONG -> { /* reverse direction */ }
                        LoopMode.COUNT -> { /* decrement counter */ }
                        else -> break
                    }
                }
            }

            _playbackState.value = PlaybackState.STOPPED
        }
    }

    fun pause() {
        playbackJob?.cancel()
        _playbackState.value = PlaybackState.PAUSED
    }

    fun stop() {
        playbackJob?.cancel()
        _playbackState.value = PlaybackState.STOPPED
    }
}

enum class PlaybackState { STOPPED, PLAYING, PAUSED }
```

### Anti-Patterns to Avoid
- **Using Animatable for BLE commands:** Compose Animatable is for UI animation, not for scheduling BLE writes. Use coroutine loops instead.
- **Storing Color objects directly in Room:** Use Int (ARGB) for serialization, convert to/from Compose Color at UI layer.
- **Single Path for all keyframes:** Draw each keyframe as separate shape for hit-testing; don't combine into single Path.
- **Blocking main thread during playback:** All animation evaluation and BLE commands must be on Dispatchers.Default/IO.
- **Ignoring MTU and command queue:** Respect 20ms delay between BLE commands per Phase 1 learnings.

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| HSL color interpolation | Custom HSL math | ColorUtils.colorToHSL/HSLToColor | Already in androidx.core, handles edge cases |
| Timeline scroll + zoom | Custom scroll state | Modifier.horizontalScroll + transformable | Built-in, handles fling, momentum |
| JSON serialization | Gson or manual parsing | Kotlinx Serialization | Already configured, compile-time safe |
| BLE command scheduling | Raw handlers | TowerCommandQueue | Already handles queueing, delays, errors |
| Database migrations | Manual SQL | Room auto-migrations | Room 2.4+ handles simple additions |

**Key insight:** The existing Phase 1 codebase already solved BLE timing, debouncing, and command queueing. Animation playback MUST use these patterns rather than bypassing them.

## Common Pitfalls

### Pitfall 1: Timeline Gesture Conflicts
**What goes wrong:** Horizontal scroll fights with keyframe drag gestures
**Why it happens:** Multiple pointerInput modifiers competing for same touch events
**How to avoid:** Use nested gesture detection — outer layer for scroll, inner layer for keyframe manipulation. Consume events appropriately.
**Warning signs:** Erratic scrolling, keyframes jumping unexpectedly

### Pitfall 2: BLE Command Flooding During Playback
**What goes wrong:** Animation sends commands faster than device can process
**Why it happens:** Ignoring the 20ms delay requirement from Phase 1
**How to avoid:** Cap playback to 30fps (33ms delay), use existing debouncing pattern, enqueue commands through TowerCommandQueue
**Warning signs:** GATT_ERROR 133, dropped commands, device becomes unresponsive

### Pitfall 3: HSV Hue Wrapping
**What goes wrong:** Red to Purple interpolation goes through green/cyan instead of magenta
**Why it happens:** Linear interpolation from 0 to 300 goes through 150 instead of -60
**How to avoid:** Calculate shortest path around hue wheel (see Pattern 2)
**Warning signs:** Unexpected intermediate colors during smooth transitions

### Pitfall 4: Room Migration Failure
**What goes wrong:** App crashes on update with "Room cannot verify data integrity"
**Why it happens:** Missing migration path from version 1 to 2
**How to avoid:** Write explicit Migration object, test with MigrationTestHelper
**Warning signs:** Crash on first launch after update

### Pitfall 5: Memory Leaks from Playback Jobs
**What goes wrong:** Animation continues after leaving screen, multiple jobs stack up
**Why it happens:** CoroutineScope not bound to ViewModel lifecycle
**How to avoid:** Use viewModelScope for playback jobs, cancel in onCleared()
**Warning signs:** Multiple simultaneous playback loops, battery drain

### Pitfall 6: Preset Thumbnail Generation Blocking UI
**What goes wrong:** UI jank when loading preset library
**Why it happens:** Generating thumbnails on main thread
**How to avoid:** Generate thumbnails on Dispatchers.Default, cache in database, show placeholder while loading
**Warning signs:** Visible stutter when opening preset library

## Code Examples

Verified patterns from official sources and existing codebase:

### Navigation Route Addition
```kotlin
// Source: Existing Screen.kt pattern
@Serializable
sealed class Screen {
    // ... existing routes

    @Serializable
    data object AnimationEditor : Screen()

    @Serializable
    data class PresetLibrary(val selectMode: Boolean = false) : Screen()
}
```

### Room Database Migration
```kotlin
// Source: Official Room docs
@Database(
    entities = [TowerConfigEntity::class, AnimationEntity::class],
    version = 2,
    exportSchema = true
)
@TypeConverters(KeyframeListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun towerConfigDao(): TowerConfigDao
    abstract fun animationDao(): AnimationDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS animations (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        durationMs INTEGER NOT NULL,
                        keyframesJson TEXT NOT NULL,
                        loopMode TEXT NOT NULL DEFAULT 'ONCE',
                        loopCount INTEGER NOT NULL DEFAULT 1,
                        createdAt INTEGER NOT NULL
                    )
                """)
            }
        }
    }
}
```

### Diamond Keyframe Drawing
```kotlin
// Source: Compose Canvas API
fun DrawScope.drawKeyframeDiamond(
    center: Offset,
    size: Float,
    color: Color,
    isSelected: Boolean
) {
    val path = Path().apply {
        moveTo(center.x, center.y - size)  // Top
        lineTo(center.x + size, center.y)  // Right
        lineTo(center.x, center.y + size)  // Bottom
        lineTo(center.x - size, center.y)  // Left
        close()
    }

    drawPath(path, color)

    if (isSelected) {
        drawPath(
            path,
            color = Color.White,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}
```

### ViewModel with Animation Playback
```kotlin
// Source: Existing ControlViewModel pattern
@HiltViewModel
class AnimationEditorViewModel @Inject constructor(
    private val animationRepository: AnimationRepository,
    private val animationPlayer: AnimationPlayer,
    private val connectionManager: TowerConnectionManager
) : ViewModel() {

    private val _animation = MutableStateFlow(Animation.empty())
    val animation: StateFlow<Animation> = _animation.asStateFlow()

    val playbackState: StateFlow<PlaybackState> = animationPlayer.playbackState
    val connectedTowers: StateFlow<List<ConnectedTower>> = connectionManager.connectedTowers

    private val _selectedTowerAddress = MutableStateFlow<String?>(null)
    val selectedTowerAddress: StateFlow<String?> = _selectedTowerAddress.asStateFlow()

    fun addKeyframe(segment: Int, timeMs: Long, color: Color) {
        _animation.update { anim ->
            anim.copy(keyframes = anim.keyframes + Keyframe(
                timeMs = timeMs,
                segment = segment,
                color = color.toArgb(),
                brightness = 100
            ))
        }
    }

    fun play() {
        animationPlayer.play(_animation.value, _selectedTowerAddress.value)
    }

    fun pause() = animationPlayer.pause()
    fun stop() = animationPlayer.stop()

    fun saveAsPreset(name: String) {
        viewModelScope.launch {
            animationRepository.save(_animation.value.copy(name = name))
        }
    }
}
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| LiveData for UI state | StateFlow | 2023 | Better Compose integration, no observeAsState() |
| KAPT annotation processing | KSP | 2024 | 2x faster builds |
| Room version 2.6.x | Room 2.8.4 | Nov 2025 | Better migrations, schema validation |
| ticker channel | tickerFlow | 2024 | Non-obsolete API, structured concurrency |

**Deprecated/outdated:**
- `ticker` channel: Marked @ObsoleteCoroutineApi, use Flow-based ticker instead
- Room auto-migrations without @AutoMigration: Still works but explicit migrations preferred for complex changes
- ValueAnimator for sequenced animations: Use Compose Animatable or coroutine loops

## Open Questions

1. **Segment-specific BLE commands**
   - What we know: Protocol supports RGB color commands
   - What's unclear: Does MELK protocol support per-segment addressing?
   - Recommendation: Research protocol further; if not supported, timeline shows full-tower keyframes only (all segments same color per keyframe)

2. **Preset thumbnail generation**
   - What we know: Need visual preview in preset grid (D-13)
   - What's unclear: Best approach — render to Bitmap, store as blob, or generate on-demand?
   - Recommendation: Generate on-demand using small Canvas snapshot, cache in memory

3. **Maximum animation duration**
   - What we know: User decision deferred to Claude's discretion
   - What's unclear: Should there be a limit?
   - Recommendation: 5-minute soft limit with warning, no hard limit

## Sources

### Primary (HIGH confidence)
- [Android Graphics in Compose](https://developer.android.com/develop/ui/compose/graphics/draw/overview) - Canvas, DrawScope, Path APIs
- [Android Multitouch](https://developer.android.com/develop/ui/compose/touch-input/pointer-input/multi-touch) - transformable, detectTransformGestures
- [Android Room Migrations](https://developer.android.com/training/data-storage/room/migrating-db-versions) - Migration objects, auto-migrations
- [Kotlinx Serialization](https://kotlinlang.org/docs/serialization.html) - JSON encoding/decoding
- Existing codebase: ControlViewModel.kt, TowerPreviewCanvas.kt, TowerCommandQueue.kt

### Secondary (MEDIUM confidence)
- [SmartToolFactory Compose Tutorials](https://github.com/SmartToolFactory/Jetpack-Compose-Tutorials) - Canvas + gesture patterns
- [detectDragGestures Guide](https://medium.com/@ramadan123sayed/the-complete-guide-to-detectdraggestures-and-pointerinput-in-jetpack-compose-08f7f367d9bc) - Gesture implementation details
- [Room TypeConverters](https://dev.to/mzgreen/room-provided-type-converters-explained-2hfd) - @ProvidedTypeConverter with DI
- [ColorUtils API](https://developer.android.com/reference/kotlin/androidx/core/graphics/ColorUtils) - HSL conversion methods

### Tertiary (LOW confidence)
- [colormath library](https://github.com/ajalt/colormath) - Multiplatform color library (not recommended for this project)
- [Zoomable library](https://github.com/usuiat/Zoomable) - Compose zoom library (evaluate if built-in insufficient)

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH - Using existing dependencies, no new libraries needed
- Architecture: HIGH - Patterns consistent with Phase 1, well-documented APIs
- Pitfalls: HIGH - Based on Phase 1 learnings and official documentation

**Research date:** 2026-03-28
**Valid until:** 2026-04-28 (30 days - stable domain)

---

*Phase: 02-timeline-animation-presets*
*Research completed: 2026-03-28*
