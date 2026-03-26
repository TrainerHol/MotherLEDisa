# Architecture Patterns

**Domain:** Android BLE LED Controller with Timeline Animation
**Researched:** 2025-03-25
**Confidence:** HIGH (multiple authoritative sources, established patterns)

## Recommended Architecture

```
+------------------------------------------------------------------+
|                        PRESENTATION LAYER                        |
|  +------------------+  +------------------+  +------------------+ |
|  |   Compose UI     |  |   ViewModels     |  |   UI State       | |
|  |   - Screens      |  |   - DeviceVM     |  |   (StateFlow)    | |
|  |   - Canvas       |  |   - TimelineVM   |  |                  | |
|  |   - Animations   |  |   - AudioVM      |  |                  | |
|  +------------------+  +------------------+  +------------------+ |
+------------------------------------------------------------------+
                              |
                    (Use Cases / Interactors)
                              |
+------------------------------------------------------------------+
|                         DOMAIN LAYER                             |
|  +------------------+  +------------------+  +------------------+ |
|  |   Use Cases      |  |   Entities       |  |   Repositories   | |
|  |   - ConnectDevice|  |   - Tower        |  |   (Interfaces)   | |
|  |   - PlayAnimation|  |   - Animation    |  |                  | |
|  |   - SyncTowers   |  |   - Preset       |  |                  | |
|  +------------------+  +------------------+  +------------------+ |
+------------------------------------------------------------------+
                              |
              (Repository Implementations)
                              |
+------------------------------------------------------------------+
|                          DATA LAYER                              |
|  +------------------+  +------------------+  +------------------+ |
|  |   BLE Service    |  |   Room Database  |  |   Audio Engine   | |
|  |   - BLESSED lib  |  |   - Presets      |  |   - FFT (Noise)  | |
|  |   - Command Queue|  |   - Animations   |  |   - Beat Detect  | |
|  |   - Multi-device |  |   - Tower Config |  |                  | |
|  +------------------+  +------------------+  +------------------+ |
+------------------------------------------------------------------+
```

### Component Boundaries

| Component | Responsibility | Communicates With |
|-----------|---------------|-------------------|
| **Compose UI** | Render screens, handle gestures, display real-time preview | ViewModels (observes StateFlow) |
| **ViewModels** | UI logic, state aggregation, use case orchestration | Use Cases, Compose UI (emits StateFlow) |
| **Use Cases** | Business logic, orchestrate data operations | Repository interfaces, Entities |
| **Entities** | Domain models (Tower, Animation, Keyframe, Preset) | Pure data, no dependencies |
| **BLE Service** | Device discovery, connection, command dispatch | Foreground Service, Repository impl |
| **Command Queue** | Serialize BLE operations, ensure one-at-a-time | BLE Service internal |
| **Room Database** | Persist presets, animations, tower configurations | Repository impl |
| **Audio Engine** | Capture mic input, FFT analysis, frequency mapping | Repository impl, triggers animations |
| **Animation Engine** | Keyframe interpolation, timeline playback | Domain layer, triggers BLE commands |

### Data Flow

```
User Interaction                     Device State
      |                                    ^
      v                                    |
[Compose UI] <--StateFlow-- [ViewModel] --|
      |                          |
      |                   [Use Cases]
      |                     /    \
      v                    v      v
[Animation Engine] --> [BLE Repository] --> [BLE Service]
      ^                                          |
      |                                          v
[Audio Engine] <-- mic input           [MELK-OT21 Towers]
```

**Animation Playback Flow:**
1. User triggers animation playback in UI
2. ViewModel invokes `PlayAnimationUseCase`
3. Animation Engine starts keyframe interpolation loop (~60Hz)
4. Each frame: calculate interpolated color values per segment
5. Animation Engine calls BLE Repository with LED commands
6. BLE Service queues commands, dispatches to connected devices
7. Preview Canvas renders same interpolated values for visual feedback

**Audio Reactive Flow:**
1. Audio Engine captures device mic via AudioRecord
2. FFT processor (Noise library) extracts frequency bands (~30Hz update)
3. Frequency-to-color mapper applies user's palette rules
4. Mapped colors feed into Animation Engine as "live keyframes"
5. Same dispatch path as animation playback

**Multi-Tower Coordination:**
1. Tower Coordinator holds ordered list of connected devices
2. Sync mode: same command to all devices simultaneously
3. Offset mode: delay commands by user-defined ms per tower index
4. Cascade mode: completion callback triggers next tower's sequence

## Core Modules

### 1. BLE Module (Data Layer)

**Purpose:** Abstract all Bluetooth Low Energy operations.

**Key Classes:**
```kotlin
// Service running in foreground for background BLE
class BleConnectionService : LifecycleService()

// Manages all connected devices
class TowerConnectionManager(
    private val centralManager: BluetoothCentralManager  // BLESSED
) {
    val connectedTowers: StateFlow<List<Tower>>
    suspend fun connect(address: String): Result<Tower>
    suspend fun disconnect(tower: Tower)
    fun discoverDevices(): Flow<DiscoveredDevice>
}

// Per-device command handling
class TowerCommandQueue(
    private val peripheral: BluetoothPeripheral
) {
    suspend fun setColor(segment: Int, r: Int, g: Int, b: Int)
    suspend fun setBrightness(level: Int)
    suspend fun setEffect(effect: Effect, speed: Int)
    suspend fun powerOn()
    suspend fun powerOff()
}
```

**Protocol Implementation (ELK-BLEDOM compatible):**
```kotlin
object MelkProtocol {
    private const val SERVICE_UUID = "0000fff0-0000-1000-8000-00805f9b34fb"
    private const val CHAR_UUID = "0000fff3-0000-1000-8000-00805f9b34fb"

    // Command format: 7e [cmd] [params...] ef
    fun powerOn() = byteArrayOf(0x7e, 0x00, 0x04, 0x01, 0x00, 0x00, 0x00, 0x00, 0xef)
    fun powerOff() = byteArrayOf(0x7e, 0x00, 0x04, 0x00, 0x00, 0x00, 0xff, 0x00, 0xef)
    fun setColor(r: Int, g: Int, b: Int) = byteArrayOf(
        0x7e, 0x00, 0x05, 0x03, r.toByte(), g.toByte(), b.toByte(), 0x00, 0xef
    )
    fun setBrightness(level: Int) = byteArrayOf(
        0x7e, 0x00, 0x01, level.toByte(), 0x00, 0x00, 0x00, 0x00, 0xef
    )
}
```

### 2. Animation Module (Domain Layer)

**Purpose:** Keyframe timeline management and interpolation engine.

**Key Classes:**
```kotlin
// Core animation entities
data class Keyframe(
    val timeMs: Long,
    val segmentColors: Map<Int, Color>,  // segment index -> color
    val interpolator: Interpolator = LinearInterpolator
)

data class Animation(
    val id: String,
    val name: String,
    val durationMs: Long,
    val keyframes: List<Keyframe>,
    val loop: Boolean = false
)

// Playback engine
class AnimationEngine(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    private val _currentFrame: MutableStateFlow<FrameState>
    val currentFrame: StateFlow<FrameState>

    fun play(animation: Animation, towers: List<Tower>)
    fun pause()
    fun stop()
    fun seek(timeMs: Long)

    // Internal: runs at ~60Hz, emits interpolated frames
    private fun interpolationLoop()
}

// Interpolation (reuse Android's)
typealias Interpolator = TimeInterpolator
```

### 3. Audio Module (Data Layer)

**Purpose:** Real-time audio capture and frequency analysis.

**Key Classes:**
```kotlin
class AudioCaptureEngine(
    private val sampleRate: Int = 44100,
    private val fftSize: Int = 512
) {
    private val noise: Noise = Noise.real(fftSize)

    val frequencyBands: StateFlow<FrequencyBands>

    fun startCapture()
    fun stopCapture()
}

data class FrequencyBands(
    val bass: Float,      // 0-200Hz normalized 0-1
    val lowMid: Float,    // 200-800Hz
    val highMid: Float,   // 800-3200Hz
    val treble: Float     // 3200Hz+
)

class FrequencyColorMapper(
    private val palette: ColorPalette,
    private val thresholds: AudioThresholds
) {
    fun mapToSegmentColors(bands: FrequencyBands): Map<Int, Color>
}
```

### 4. Persistence Module (Data Layer)

**Purpose:** Store presets, animations, tower configurations.

**Room Entities:**
```kotlin
@Entity(tableName = "presets")
data class PresetEntity(
    @PrimaryKey val id: String,
    val name: String,
    val createdAt: Long,
    val animationJson: String  // Serialized Animation
)

@Entity(tableName = "tower_configs")
data class TowerConfigEntity(
    @PrimaryKey val address: String,
    val displayName: String,
    val orderIndex: Int,
    val segmentCount: Int
)

@Dao
interface PresetDao {
    @Query("SELECT * FROM presets ORDER BY createdAt DESC")
    fun getAllPresets(): Flow<List<PresetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePreset(preset: PresetEntity)
}
```

### 5. Presentation Module (UI Layer)

**Purpose:** Jetpack Compose UI with real-time preview.

**Key Screens:**
```kotlin
// Device discovery and connection
@Composable
fun DeviceListScreen(viewModel: DeviceViewModel)

// Timeline editor (After Effects-style)
@Composable
fun TimelineEditorScreen(viewModel: TimelineViewModel)

// Real-time LED preview using Canvas
@Composable
fun TowerPreviewCanvas(
    towerState: TowerState,
    modifier: Modifier
) {
    Canvas(modifier) {
        // Draw tower segments with current colors
        // Update at 60Hz from StateFlow
    }
}

// Audio reactive configuration
@Composable
fun AudioReactiveScreen(viewModel: AudioViewModel)
```

### 6. Coordination Module (Domain Layer)

**Purpose:** Multi-tower orchestration modes.

```kotlin
sealed class CoordinationMode {
    object Mirror : CoordinationMode()  // All same
    data class Offset(val delayMs: Long) : CoordinationMode()  // Staggered
    data class Cascade(val order: List<String>) : CoordinationMode()  // Relay
    object Independent : CoordinationMode()  // Each controlled separately
}

class TowerCoordinator(
    private val connectionManager: TowerConnectionManager,
    private val animationEngine: AnimationEngine
) {
    var mode: CoordinationMode = CoordinationMode.Mirror
    val towerOrder: MutableStateFlow<List<Tower>>

    suspend fun dispatchFrame(frame: FrameState) {
        when (mode) {
            is Mirror -> towers.forEach { sendFrame(it, frame) }
            is Offset -> towers.forEachIndexed { i, t ->
                delay(mode.delayMs * i)
                sendFrame(t, frame)
            }
            // ...
        }
    }
}
```

## Patterns to Follow

### Pattern 1: Unidirectional Data Flow (UDF)

**What:** State flows down (ViewModel -> UI), events flow up (UI -> ViewModel).

**When:** All UI state management.

**Example:**
```kotlin
class TimelineViewModel(
    private val playAnimationUseCase: PlayAnimationUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(TimelineUiState())
    val uiState: StateFlow<TimelineUiState> = _uiState.asStateFlow()

    fun onEvent(event: TimelineEvent) {
        when (event) {
            is TimelineEvent.Play -> viewModelScope.launch {
                _uiState.update { it.copy(isPlaying = true) }
                playAnimationUseCase(event.animation)
            }
            is TimelineEvent.AddKeyframe -> {
                _uiState.update { it.addKeyframe(event.keyframe) }
            }
        }
    }
}
```

### Pattern 2: BLE Command Queue

**What:** Serialize all BLE operations; never issue concurrent commands.

**When:** All BLE characteristic writes.

**Example:**
```kotlin
class TowerCommandQueue(private val peripheral: BluetoothPeripheral) {
    private val commandChannel = Channel<BleCommand>(Channel.UNLIMITED)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        scope.launch {
            for (command in commandChannel) {
                executeCommand(command)
                delay(20) // Small gap between commands
            }
        }
    }

    suspend fun enqueue(command: BleCommand) {
        commandChannel.send(command)
    }

    private suspend fun executeCommand(command: BleCommand) {
        peripheral.writeCharacteristic(
            command.characteristic,
            command.data,
            WriteType.WITH_RESPONSE
        )
    }
}
```

### Pattern 3: Foreground Service for BLE

**What:** Run BLE operations in a Foreground Service for background reliability.

**When:** App needs to maintain connections when backgrounded.

**Example:**
```kotlin
class BleConnectionService : LifecycleService() {
    private lateinit var connectionManager: TowerConnectionManager

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, createNotification())
        connectionManager = TowerConnectionManager(
            BluetoothCentralManager(this)
        )
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("MotherLEDisa")
            .setContentText("Connected to ${connectedCount} towers")
            .setSmallIcon(R.drawable.ic_led)
            .setForegroundServiceBehavior(FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }
}
```

### Pattern 4: Frame-Rate Limited Updates

**What:** Cap update rate to avoid overwhelming BLE and UI.

**When:** Animation playback, audio reactive updates.

**Example:**
```kotlin
class AnimationEngine {
    private val frameIntervalMs = 16L  // ~60 FPS for preview
    private val bleIntervalMs = 33L    // ~30 FPS for BLE (protocol limit)

    private fun interpolationLoop() = flow {
        var lastBleUpdate = 0L
        while (isPlaying) {
            val now = System.currentTimeMillis()
            val frame = interpolateFrame(now)

            emit(frame)  // Always emit for preview

            if (now - lastBleUpdate >= bleIntervalMs) {
                dispatchToBle(frame)
                lastBleUpdate = now
            }

            delay(frameIntervalMs)
        }
    }
}
```

## Anti-Patterns to Avoid

### Anti-Pattern 1: Concurrent BLE Operations

**What:** Issuing multiple BLE writes without waiting for callbacks.

**Why bad:** Commands get dropped, status 133 errors, unreliable state.

**Instead:** Use command queue pattern (see above). BLESSED library handles this internally, but respect its async nature.

### Anti-Pattern 2: BLE in Main Thread

**What:** Performing BLE operations on the UI thread.

**Why bad:** UI freezes, ANRs, poor user experience.

**Instead:** Use coroutines with `Dispatchers.IO` for all BLE operations. BLESSED already handles threading, but wrap calls in appropriate dispatchers.

### Anti-Pattern 3: Direct UI-to-BLE Coupling

**What:** UI components directly calling BLE methods.

**Why bad:** Tight coupling, untestable, lifecycle issues.

**Instead:** UI -> ViewModel -> UseCase -> Repository -> BLE Service. Each layer has single responsibility.

### Anti-Pattern 4: Unbounded Animation Loops

**What:** Animation loop without frame limiting or cancellation.

**Why bad:** Battery drain, thermal issues, BLE queue backup.

**Instead:** Use structured concurrency with proper cancellation, frame rate limiting, and graceful pause/stop.

### Anti-Pattern 5: Storing Large Animations as JSON Blobs

**What:** Serializing entire complex animations into single JSON strings.

**Why bad:** Memory issues, slow queries, no partial updates.

**Instead:** Normalize animation data: separate tables for animations, keyframes, segment states. Use relations.

## Scalability Considerations

| Concern | 1 Tower | 4 Towers | 8+ Towers |
|---------|---------|----------|-----------|
| **Connection** | Simple connect | Manage 4 BluetoothGatt instances | Near Android limit (~8); batch autoConnect |
| **Command Rate** | 30 FPS easy | 30 FPS shared across 4 | Reduce to 15-20 FPS per device |
| **Coordination** | N/A | Offset/cascade timing | Consider staggered dispatch, priority queues |
| **Preview Render** | Single Canvas | 4 Canvas instances | Virtualized list, lazy render |
| **State Management** | Single StateFlow | Combined StateFlow with tower map | Consider per-tower StateFlows, selective updates |

## Build Order (Dependencies)

Based on component dependencies, recommended build order:

### Phase 1: Foundation
1. **BLE Module** - Core connectivity, no dependencies
   - BLESSED integration
   - MELK protocol implementation
   - Single device connect/disconnect
   - Basic command dispatch

2. **Persistence Module** - Room setup, entities
   - Database configuration
   - Tower config entities
   - Basic DAOs

### Phase 2: Core Features
3. **Animation Module** - Depends on BLE Module
   - Keyframe/Animation entities
   - Interpolation engine
   - Basic playback (single tower)

4. **Presentation Module** - Depends on Animation Module
   - Device list screen
   - Basic preview Canvas
   - Simple timeline UI

### Phase 3: Enhancement
5. **Audio Module** - Can be built in parallel
   - FFT integration (Noise library)
   - Frequency band extraction
   - Color mapping

6. **Coordination Module** - Depends on BLE + Animation
   - Multi-tower management
   - Sync/offset/cascade modes
   - Tower ordering

### Phase 4: Polish
7. **Full Timeline Editor** - Depends on all above
   - After Effects-style UI
   - Keyframe editing
   - Preview playback

8. **Preset System** - Depends on Persistence + Animation
   - Save/load animations
   - Preset library UI

## Technology Decisions

| Layer | Technology | Rationale |
|-------|------------|-----------|
| BLE | BLESSED-Kotlin | Handles queuing, multi-device, modern Kotlin API |
| UI | Jetpack Compose | Modern, declarative, excellent Canvas support |
| State | StateFlow/SharedFlow | Built-in, coroutine-native, lifecycle-aware |
| Persistence | Room | Official, compile-time SQL verification, Flow support |
| DI | Hilt | Standard 2025, minimal boilerplate |
| Audio FFT | Noise (paramsen) | Native performance, Kotlin wrapper, proven |
| Architecture | MVVM + Clean | Testable, maintainable, industry standard |

## Sources

- [Punch Through: Android BLE Guide](https://punchthrough.com/android-ble-guide/) - Comprehensive BLE patterns
- [Punch Through: BLE Operation Queue](https://punchthrough.com/android-ble-operation-queue/) - Command queue pattern
- [BLESSED-Kotlin GitHub](https://github.com/weliem/blessed-kotlin) - BLE library architecture
- [Android Developers: StateFlow](https://developer.android.com/kotlin/flow/stateflow-and-sharedflow) - State management
- [Android Developers: Room](https://developer.android.com/training/data-storage/room) - Persistence
- [Android Developers: Compose Graphics](https://developer.android.com/develop/ui/compose/graphics) - Canvas rendering
- [elk-led-controller](https://github.com/b1scoito/elk-led-controller) - Reference protocol implementation
- [elkbledom GitHub](https://github.com/dave-code-ruiz/elkbledom) - Protocol command details
- [Noise FFT Library](https://github.com/paramsen/noise) - Audio FFT for Android
- [FreeCodeCamp: Android Bluetooth Patterns](https://www.freecodecamp.org/news/system-design-patterns-in-android-bluetooth-full-handbook/) - System design patterns
