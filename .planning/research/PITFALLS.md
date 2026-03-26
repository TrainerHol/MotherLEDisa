# Domain Pitfalls: Android BLE LED Controller

**Domain:** Android BLE LED Controller with Sound-Reactive Features
**Researched:** 2026-03-25
**Confidence:** HIGH (verified across multiple authoritative sources)

---

## Critical Pitfalls

Mistakes that cause architecture rewrites, app rejection, or fundamental failures.

### Pitfall 1: BLE Operations Without Queuing

**What goes wrong:** Rapid-fire BLE commands (connect, discover services, write characteristics) cause GATT_ERROR (133), silent failures, and connection drops. The Android BLE stack cannot handle concurrent operations.

**Why it happens:** Developers treat BLE like synchronous HTTP calls. Android's BLE API requires waiting for each callback before initiating the next operation.

**Consequences:**
- Random disconnections during animation playback
- Lost commands (LED doesn't change color when expected)
- App appears unreliable to users
- Impossible to debug because failures are non-deterministic

**Prevention:**
```kotlin
// Implement an operation queue
class BleOperationQueue {
    private val queue = ConcurrentLinkedQueue<BleOperation>()
    private var pendingOperation: BleOperation? = null

    fun enqueue(operation: BleOperation) {
        queue.add(operation)
        if (pendingOperation == null) executeNext()
    }

    private fun onOperationComplete() {
        pendingOperation = null
        executeNext()
    }
}
```
- Queue ALL operations: connect, service discovery, MTU request, characteristic writes, disconnect
- Wait for callback before starting next operation
- Implement timeout handling (operations can hang indefinitely)

**Detection:** Random GATT_ERROR 133 in logs. Animations "skip frames" inconsistently. Multi-tower commands arrive out of order.

**Phase to address:** Phase 1 (BLE Foundation) - Must be built into architecture from start

**Sources:**
- [Punch Through - Android BLE Guide](https://punchthrough.com/android-ble-guide/)
- [Punch Through - Android BLE Operation Queue](https://punchthrough.com/android-ble-operation-queue/)

---

### Pitfall 2: MELK Device Initialization Sequence Missing

**What goes wrong:** MELK devices (including MELK-OT21) don't respond to commands after connection. App appears to connect successfully but LED tower doesn't react.

**Why it happens:** Unlike standard ELK-BLEDOM devices, MELK devices require initialization commands before accepting control commands. This is not documented in the device advertising data.

**Consequences:**
- App works with some devices, fails with target MELK-OT21 towers
- Users report "app connects but nothing happens"
- Wastes debugging time assuming protocol is wrong

**Prevention:**
Send initialization sequence after connecting, before any control commands:
```kotlin
// Required MELK initialization (from Home Assistant elkbledom)
suspend fun initializeMelkDevice(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
    writeCharacteristic(gatt, characteristic, byteArrayOf(0x7e, 0x07, 0x83.toByte()))
    delay(50) // Allow processing
    writeCharacteristic(gatt, characteristic, byteArrayOf(0x7e, 0x04, 0x04))
    delay(50)
    // Device may need power cycle after first-ever init
}
```
- Detect device type from name prefix (MELK- vs ELK-)
- Apply device-specific initialization
- Handle "first-ever connection" case (may need power cycle guidance for user)

**Detection:** Device connects (GATT state = CONNECTED) but ignores write commands. Works with ELK- devices but not MELK-.

**Phase to address:** Phase 1 (BLE Foundation) - Part of device connection flow

**Sources:**
- [elkbledom Home Assistant Integration](https://github.com/dave-code-ruiz/elkbledom)

---

### Pitfall 3: Background BLE Killed by Doze/Battery Optimization

**What goes wrong:** BLE connections drop or scans stop working when screen turns off. Sound-reactive mode stops controlling LEDs after phone goes to sleep. Users report "works fine until I put phone in pocket."

**Why it happens:** Android Doze mode and OEM battery optimization aggressively kill background processes. Samsung/Xiaomi/Huawei add additional layers beyond stock Android.

**Consequences:**
- Sound-reactive visualizations stop while music continues
- Multi-tower orchestration fails mid-animation
- Users must keep screen on, defeating mobile use case
- 1-star reviews about battery issues OR reliability issues

**Prevention:**
1. **Foreground Service (mandatory):**
```kotlin
// Android 14+: Must specify service type
<service
    android:name=".BleControlService"
    android:foregroundServiceType="connectedDevice"
    android:exported="false" />
```

2. **Request battery optimization exemption (with user consent):**
```kotlin
val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
intent.data = Uri.parse("package:$packageName")
startActivity(intent)
```

3. **OEM-specific guidance flow:**
   - Samsung: "Never Sleeping" list
   - Xiaomi: Auto-start manager + battery saver + background limiter
   - Huawei: App launch management

4. **Scan filters required (Android 7+):**
```kotlin
// Without filter, scans stop after 30 seconds - silently!
val filter = ScanFilter.Builder()
    .setDeviceName("MELK-")
    .build()
scanner.startScan(listOf(filter), settings, callback)
```

**Detection:** "Works on desk, fails in pocket." Works fine on Pixel, fails on Samsung. Logcat shows service killed after screen off.

**Phase to address:** Phase 1 (BLE Foundation) - Requires foreground service architecture upfront

**Sources:**
- [Android BLE Scanning in 2026 (Medium)](https://bleadvertiserapp.medium.com/android-ble-scanning-in-2026-why-your-app-stops-finding-devices-in-the-background-and-how-to-fix-ba5ae06c17c3)
- [Android Battery Optimization (ProAndroidDev)](https://proandroiddev.com/android-battery-optimization-for-avoiding-doze-mode-and-app-standby-83cd379ee75b)

---

### Pitfall 4: Audio Processing on Main Thread / Java Layer

**What goes wrong:** Sound-reactive mode has visible latency between sound and LED response. LEDs react to beats 100-200ms late, destroying the effect. UI becomes janky when audio processing is active.

**Why it happens:** Android audio latency is 20ms+ round-trip by design. Processing FFT in Java/Kotlin adds GC pauses. Main thread audio work causes frame drops in UI.

**Consequences:**
- Sound-reactive feature feels broken
- "LEDs don't sync with music" reviews
- Animation editor becomes sluggish when sound mode active
- Cannot achieve professional-grade (<10ms) responsiveness

**Prevention:**
1. **Use Oboe/AAudio for audio input (C++/NDK):**
```kotlin
// JNI wrapper for Oboe audio stream
external fun startAudioCapture(sampleRate: Int, framesPerBuffer: Int)
external fun getFrequencyData(): FloatArray
external fun stopAudioCapture()
```

2. **Process FFT in native code:**
   - Use Oboe with `PerformanceMode::LowLatency`
   - Use `SharingMode::Exclusive` for minimum latency
   - Set `AudioFormat::Int16` for Android < 9.0 (Float breaks fast track)

3. **Separate audio thread from UI thread:**
   - Audio callback runs on high-priority thread
   - Post frequency data to UI via handler/flow
   - Never block audio callback

4. **Accept latency budget:**
   - Audio capture: ~10ms (best case with Oboe)
   - FFT processing: ~2-5ms
   - BLE transmission: ~15-30ms per write
   - Total: 25-45ms minimum (still perceptible but acceptable)

**Detection:** Compare app to hardware sound-reactive controllers. If LED response feels "sluggish," latency is too high.

**Phase to address:** Phase 3 (Sound-Reactive Mode) - Requires NDK integration from start of audio work

**Sources:**
- [Android NDK Audio Latency Guide](https://developer.android.com/ndk/guides/audio/audio-latency)
- [Oboe Low Latency Audio](https://developer.android.com/games/sdk/oboe/low-latency-audio)

---

### Pitfall 5: Multi-Tower Clock Drift Without Synchronization

**What goes wrong:** In cascade/offset mode, towers gradually desync over time. An animation that starts perfectly choreographed drifts by 50-100ms after 30 seconds. Mirror mode shows visible flicker differences between towers.

**Why it happens:** Each BLE write takes variable time (15-50ms). No shared clock between phone and towers. Phone's scheduler introduces jitter between tower commands.

**Consequences:**
- Cascade animations look sloppy
- Offset timing effects drift apart
- Users cannot achieve professional multi-device shows
- "Works for 10 seconds then falls apart"

**Prevention:**
1. **Command batching with timing compensation:**
```kotlin
// Pre-calculate send times, compensate for measured latency per device
data class TimedCommand(
    val device: BleLedDevice,
    val command: ByteArray,
    val targetTime: Long, // When LED should change
    val sendOffset: Long  // Pre-calculated transmission time
)

// Send earlier to devices with higher measured latency
fun scheduleSynchronizedCommands(commands: List<TimedCommand>) {
    commands.forEach { cmd ->
        val sendTime = cmd.targetTime - cmd.sendOffset
        handler.postAtTime({ sendCommand(cmd) }, sendTime)
    }
}
```

2. **Measure per-device latency:**
   - Track time between write initiation and callback
   - Build latency profile per tower
   - Re-measure periodically (WiFi interference varies)

3. **Choreographer-synced animation loop:**
```kotlin
// Use Choreographer for consistent frame timing
Choreographer.getInstance().postFrameCallback { frameTimeNanos ->
    val frameTime = frameTimeNanos / 1_000_000 // Convert to ms
    updateAnimationState(frameTime)
    enqueueBleCommands(frameTime)
    Choreographer.getInstance().postFrameCallback(this)
}
```

4. **Reduce command frequency:**
   - Don't send every frame (60fps = 16ms, BLE can't keep up)
   - Target 20-30 updates/second per tower
   - Interpolate on-device if tower firmware supports

**Detection:** Run cascade animation for 60 seconds. Measure perceived delay between first and last tower. If > 100ms drift, synchronization is broken.

**Phase to address:** Phase 4 (Multi-Tower Orchestration) - Requires latency measurement infrastructure

**Sources:**
- [Nordic DevZone - BLE Time Synchronization](https://devzone.nordicsemi.com/f/nordic-q-a/97532/ble-time-synchronization-between-devices)
- [BlueSync IEEE Paper](https://ieeexplore.ieee.org/document/9555832/)

---

## Moderate Pitfalls

Cause significant bugs or performance issues but don't require architecture rewrites.

### Pitfall 6: MTU Size Not Negotiated

**What goes wrong:** Default MTU is 23 bytes (20 usable after headers). Sending animation data requires many more packets than necessary, increasing latency and battery drain.

**Prevention:**
- Call `gatt.requestMtu(255)` after connection (before service discovery)
- Handle `onMtuChanged` callback
- Don't request > 255 (causes write failures on some devices)
- ELK-BLEDOM protocol uses 9-byte packets anyway, but future features may need larger payloads

**Detection:** High BLE traffic for simple commands. Battery drain during animation playback.

**Phase to address:** Phase 1 (BLE Foundation)

---

### Pitfall 7: Connection Interval Ignored

**What goes wrong:** Android defaults to power-saving connection intervals (30-50ms). Animations appear choppy because commands can only be sent every 30-50ms.

**Prevention:**
```kotlin
// Request shorter connection interval (7.5ms minimum on Android)
val connectionPriorityResult = gatt.requestConnectionPriority(
    BluetoothGatt.CONNECTION_PRIORITY_HIGH // ~11.25ms interval
)
```
- Use HIGH priority during active animations
- Switch to BALANCED or LOW_POWER when idle
- Not all devices honor the request

**Detection:** Count time between characteristic writes. If > 20ms consistently, connection interval is limiting throughput.

**Phase to address:** Phase 2 (Timeline Animation) - When animation frame rate matters

---

### Pitfall 8: Android 13 Bonding Regression

**What goes wrong:** Reconnection to previously-connected devices fails randomly. Users must "forget" device and re-pair. Affects Android 13 pre-QPR1 heavily.

**Prevention:**
- Don't call `createBond()` unless device requires encrypted characteristics
- ELK-BLEDOM/MELK devices don't use bonding (no encrypted attributes)
- If bonding is needed: wait 1 second after bond loss before reconnecting
- Handle bond state changes gracefully

**Detection:** Works first time, fails on app restart. "Pairing not supported" errors in log.

**Phase to address:** Phase 1 (BLE Foundation)

**Sources:**
- [Android Issue Tracker - Android 13 BLE Reconnection](https://issuetracker.google.com/issues/242755161)

---

### Pitfall 9: Write Without Response vs Write With Response Confusion

**What goes wrong:** Using Write With Response (default) for animation commands causes command queuing delays. BLE waits for ACK before allowing next write, limiting throughput to ~20 commands/second.

**Prevention:**
```kotlin
// For animation commands, use Write Without Response
characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
gatt.writeCharacteristic(characteristic)
```
- Use WRITE_TYPE_NO_RESPONSE for animation/color commands (speed > reliability)
- Use WRITE_TYPE_DEFAULT for configuration changes (reliability > speed)
- ELK-BLEDOM characteristic (0xFFF3) supports both modes

**Detection:** Animation commands queue up. Visible lag between initiating color change and LED response.

**Phase to address:** Phase 2 (Timeline Animation)

**Sources:**
- [Punch Through - BLE Throughput](https://punchthrough.com/maximizing-ble-throughput-on-ios-and-android/)

---

### Pitfall 10: FFT Size Tradeoffs Misunderstood

**What goes wrong:** Using large FFT windows (8192+ samples) for "accuracy" causes 100ms+ audio latency. Using tiny windows (64 samples) causes noisy, jumpy frequency data.

**Prevention:**
- Use 512-2048 samples for real-time visualization (10-40ms latency at 44.1kHz)
- Apply windowing function (Hann/Hamming) to reduce spectral leakage
- Use overlapping windows (50%) for smoother updates
- Smoothing filter on frequency bins prevents LED flicker

**Detection:** Sound-reactive mode either laggy (too large) or flickery/noisy (too small).

**Phase to address:** Phase 3 (Sound-Reactive Mode)

---

## Minor Pitfalls

Cause confusion or minor bugs but are easily fixed.

### Pitfall 11: Hardcoded Device Names

**What goes wrong:** App only finds "ELK-BLEDOM" devices, missing MELK-, LEDBLE-, ELK-BULB variations.

**Prevention:**
```kotlin
val compatiblePrefixes = listOf("ELK-", "MELK-", "LEDBLE", "ELK-BULB", "ELK-LAMPL")
fun isCompatibleDevice(name: String?) =
    compatiblePrefixes.any { name?.startsWith(it) == true }
```

**Phase to address:** Phase 1 (BLE Foundation)

---

### Pitfall 12: Permission Model Changes Per Android Version

**What goes wrong:** App works on Android 11, crashes on Android 12+ due to new BLUETOOTH_SCAN, BLUETOOTH_CONNECT, BLUETOOTH_ADVERTISE permissions.

**Prevention:**
```kotlin
// Android 12+ (API 31+)
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    requestPermissions(arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT
    ))
} else {
    requestPermissions(arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION
    ))
}
```

**Phase to address:** Phase 1 (BLE Foundation)

---

### Pitfall 13: Service UUID vs Characteristic UUID Confusion

**What goes wrong:** Developer tries to write to Service UUID instead of Characteristic UUID. Writes fail with no clear error.

**Prevention:**
- Service UUID: `0000FFF0-0000-1000-8000-00805F9B34FB`
- Characteristic UUID: `0000FFF3-0000-1000-8000-00805F9B34FB` (this is what you write to)
- Always discover services and iterate characteristics, don't assume UUIDs

**Phase to address:** Phase 1 (BLE Foundation)

---

### Pitfall 14: Animation Frame Rate vs BLE Throughput Mismatch

**What goes wrong:** Animation engine generates 60fps, BLE can only transmit 20-30 commands/second. Command queue grows unboundedly, causing memory issues and increasing latency over time.

**Prevention:**
- Cap animation output to BLE-achievable rate (~30fps max per device)
- Implement frame dropping when queue depth exceeds threshold
- Consider local interpolation for smoother perceived animation

**Phase to address:** Phase 2 (Timeline Animation)

---

## Phase-Specific Warnings

| Phase Topic | Likely Pitfall | Mitigation |
|-------------|---------------|------------|
| BLE Foundation | Operations without queue (Pitfall 1) | Build operation queue as first architecture component |
| BLE Foundation | MELK init missing (Pitfall 2) | Device-specific connection handlers from day one |
| BLE Foundation | Background killed (Pitfall 3) | Foreground service architecture, OEM detection |
| Timeline Animation | BLE throughput limits (Pitfall 14) | Frame rate limiting, queue depth monitoring |
| Timeline Animation | Write type wrong (Pitfall 9) | Use WRITE_TYPE_NO_RESPONSE for animation |
| Sound-Reactive | Audio latency (Pitfall 4) | Plan for NDK/Oboe from start, don't prototype in Kotlin |
| Sound-Reactive | FFT size wrong (Pitfall 10) | Start with 1024 samples, tune based on testing |
| Multi-Tower | Clock drift (Pitfall 5) | Per-device latency measurement, compensated scheduling |
| Multi-Tower | Parallel connection issues | Limit to 3-4 devices simultaneously, queue connections |

---

## Sources

### Authoritative (HIGH confidence)
- [Android Developers - BLE Overview](https://developer.android.com/develop/connectivity/bluetooth/ble/ble-overview)
- [Android NDK - Audio Latency](https://developer.android.com/ndk/guides/audio/audio-latency)
- [Punch Through - Android BLE Guide](https://punchthrough.com/android-ble-guide/)
- [Punch Through - BLE Operation Queue](https://punchthrough.com/android-ble-operation-queue/)
- [Punch Through - BLE Throughput](https://punchthrough.com/maximizing-ble-throughput-on-ios-and-android/)

### Community/Verified (MEDIUM confidence)
- [elkbledom Home Assistant Integration](https://github.com/dave-code-ruiz/elkbledom)
- [ELK-BLEDOM Protocol Documentation](https://github.com/FergusInLondon/ELK-BLEDOM/blob/master/PROTCOL.md)
- [elk-led-controller Rust Library](https://github.com/b1scoito/elk-led-controller)
- [Nordic DevZone - BLE Time Synchronization](https://devzone.nordicsemi.com/f/nordic-q-a/97532/ble-time-synchronization-between-devices)
- [Android Issue Tracker - Android 13 BLE Reconnection](https://issuetracker.google.com/issues/242755161)

### Additional Research (MEDIUM confidence)
- [Memfault - BLE Throughput Primer](https://interrupt.memfault.com/blog/ble-throughput-primer)
- [Making Android BLE Work Series (Medium)](https://medium.com/@martijn.van.welie/making-android-ble-work-part-3-117d3a8aee23)
