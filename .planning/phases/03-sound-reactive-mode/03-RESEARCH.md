# Phase 3: Sound-Reactive Mode - Research

**Researched:** 2026-03-28
**Domain:** ELK-BLEDOM Sound Protocol, Tower Microphone Control, Color Palette UI
**Confidence:** HIGH

## Summary

The MELK-OT21 tower has **built-in microphone hardware** that handles audio detection autonomously. The ELK-BLEDOM protocol provides documented commands for enabling the microphone, selecting from 8 sound-reactive effects (0x80-0x87), and adjusting sensitivity (0-100 scale). This is a "fire-and-forget" configuration model: the app sends settings via BLE, then the tower runs sound-reactive mode independently without requiring the app to stay connected.

The protocol does NOT support uploading custom animations to tower memory for sound-triggered playback. Per CONTEXT.md D-04, Phase 3 scope is limited to configuring the tower's built-in sound-reactive effects with custom palettes and sensitivity. Custom animation playback triggered by sound would require app-side audio processing (deferred to v2 per AUDIO-01, AUDIO-02).

**Primary recommendation:** Implement a SoundReactiveScreen that sends mic configuration commands (enable mic, set effect 0x80-0x87, set sensitivity 0-100). Add a palette picker that extracts colors from saved presets OR allows building a custom 2-5 color palette. The tower accepts a single color command, so cycle through palette colors based on selected effect.

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions
- **D-01:** Tower has internal microphone and handles audio detection in hardware - app does NOT capture audio from phone mic
- **D-02:** App sends configuration (effect, sensitivity, palette) to tower via BLE; tower runs sound-reactive mode autonomously
- **D-03:** App does not need to stay connected during sound-reactive operation - "fire and forget" configuration model
- **D-04:** If protocol research shows tower CANNOT store custom animations, Phase 3 scope is limited to configuring built-in sound-reactive effects with custom palette/sensitivity. Custom animation playback stays in Phase 2 (manual trigger, app-connected).
- **D-05:** User can define color palette for sound mode in two ways: Pick from a saved animation preset (extract its color palette) OR Build a custom palette via dedicated picker (2-5 colors for sound mode)

### Claude's Discretion
- Exact sensitivity UI (single slider vs. threshold + gain)
- Which built-in sound effects to expose
- VU meter implementation (if protocol supports audio data)
- Screen layout and navigation placement
- Palette picker UI (how many colors, swatch design)

### Deferred Ideas (OUT OF SCOPE)
- App-side audio capture (phone mic): Only if tower-native is impossible AND user requests it - per PROJECT.md constraint, phone mic is considered "useless"
- Frequency band mapping (bass/mids/highs to different colors): Requires app-side FFT, deferred to v2 (AUDIO-01, AUDIO-02)
- Beat detection with BPM sync: Would require app-side processing, deferred
- Multi-tower cascade sound reactions: Phase 4 (multi-tower orchestration)
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| SOUND-01 | User can enable sound-reactive mode using device's internal microphone | Protocol provides `enable_mic()` command: `[0x7e, 0x04, 0x07, 0x01, 0xff, 0xff, 0xff, 0x00, 0xef]` |
| SOUND-02 | User can adjust sound threshold (sensitivity) | Protocol provides `set_mic_sensitivity(0-100)` command: `[0x7e, 0x04, 0x06, <value>, 0xff, 0xff, 0xff, 0x00, 0xef]` |
| SOUND-03 | User can assign color palette for sound triggers | Use existing `setColor(r,g,b)` command before/after enabling mic effect; tower uses current color as base |
| SOUND-04 | User can trigger custom animations from sound (not just presets) | **LIMITATION DISCOVERED:** Protocol does NOT support uploading custom animations to tower memory. Fallback: configure built-in effects with custom palettes per D-04. Full custom animation playback requires app-connected mode (Phase 2 scope). |
| UX-04 | App has dedicated screen for sound-reactive configuration | New `SoundReactive` screen following ControlScreen pattern |
</phase_requirements>

## Standard Stack

### Core (Already in Project)
| Library | Version | Purpose | Status |
|---------|---------|---------|--------|
| Nordic Android-BLE-Library | 2.11.0 | BLE communication | Already integrated |
| Jetpack Compose | BOM 2025.12.00 | UI framework | Already integrated |
| Material 3 | 1.4.0 | Design system | Already integrated |
| Navigation Compose | 2.9.7 | Screen navigation | Already integrated |
| Room | 2.8.4 | Preset storage | Already integrated |
| GoDaddy ColorPicker | 0.7.0 | HSV color wheel | Already integrated |

### No New Dependencies Required
This phase extends existing infrastructure. All required components (BLE, UI, storage) are already available.

## Architecture Patterns

### Sound Mode Protocol Commands

**Verified from elkbledom source code (HIGH confidence):**

```kotlin
// Enable microphone sound-reactive mode
fun enableMic(): ByteArray = byteArrayOf(
    0x7E, 0x04, 0x07, 0x01,
    0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0x00,
    0xEF.toByte()
)

// Disable microphone
fun disableMic(): ByteArray = byteArrayOf(
    0x7E, 0x04, 0x07, 0x00,
    0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0x00,
    0xEF.toByte()
)

// Set microphone effect (0x80-0x87, 8 effects total)
fun setMicEffect(effectId: Byte): ByteArray = byteArrayOf(
    0x7E, 0x05, 0x03, effectId, 0x04,
    0xFF.toByte(), 0xFF.toByte(), 0x00,
    0xEF.toByte()
)

// Set sensitivity (0-100)
fun setMicSensitivity(value: Int): ByteArray = byteArrayOf(
    0x7E, 0x04, 0x06,
    value.coerceIn(0, 100).toByte(),
    0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0x00,
    0xEF.toByte()
)
```

### Recommended Project Structure

```
app/src/main/java/com/motherledisa/
├── domain/model/
│   └── SoundEffect.kt           # NEW: Sound effect enum (0x80-0x87)
├── data/ble/
│   └── MelkProtocol.kt          # EXTEND: Add sound mode commands
├── domain/usecase/
│   ├── EnableSoundModeUseCase.kt    # NEW: Enable mic + set effect + sensitivity
│   └── DisableSoundModeUseCase.kt   # NEW: Disable mic
├── ui/sound/
│   ├── SoundReactiveScreen.kt       # NEW: Main screen
│   ├── SoundReactiveViewModel.kt    # NEW: State management
│   └── components/
│       ├── SoundEffectSelector.kt   # NEW: Effect picker (8 effects)
│       ├── SensitivitySlider.kt     # NEW: 0-100 slider
│       └── PalettePickerSection.kt  # NEW: Color palette builder
└── ui/navigation/
    ├── Screen.kt                    # EXTEND: Add SoundReactive route
    └── NavGraph.kt                  # EXTEND: Add nav item + composable
```

### Pattern 1: Fire-and-Forget Configuration

**What:** Send all configuration commands in sequence, then tower operates autonomously.

**When to use:** Sound-reactive mode configuration (per D-02, D-03).

**Example:**
```kotlin
// Source: Phase 1 established BLE command pattern
class EnableSoundModeUseCase @Inject constructor(
    private val connectionManager: TowerConnectionManager
) {
    suspend operator fun invoke(
        address: String,
        effect: SoundEffect,
        sensitivity: Int,
        baseColor: Color
    ) {
        val queue = connectionManager.getCommandQueue(address)

        // Set base color first (tower uses this for effect)
        queue.enqueue(BleCommand.SetColor(baseColor.toRgb()))

        // Set effect and sensitivity
        queue.enqueue(BleCommand.SetMicEffect(effect.id))
        queue.enqueue(BleCommand.SetMicSensitivity(sensitivity))

        // Enable microphone last
        queue.enqueue(BleCommand.EnableMic())

        // App can disconnect now - tower runs independently
    }
}
```

### Pattern 2: Palette Extraction from Presets

**What:** Extract unique colors from a saved animation's keyframes for sound mode palette.

**When to use:** Per D-05, user picks a saved preset to use its color palette.

**Example:**
```kotlin
// Extract palette from animation keyframes
fun Animation.extractPalette(maxColors: Int = 5): List<Color> {
    return keyframes
        .map { Color(it.color) }
        .distinct()
        .take(maxColors)
}
```

### Pattern 3: Sound Screen Layout (Following ControlScreen Pattern)

**What:** Single scrollable screen with preview, effect selector, sensitivity slider, palette picker.

**When to use:** Per D-10, follow Phase 1/2 established patterns.

**Example:**
```kotlin
@Composable
fun SoundReactiveScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Device picker (reuse from ControlScreen)
        DevicePicker(...)

        // Tower preview (reuse TowerPreviewCanvas)
        TowerPreviewCanvas(...)

        // Sound mode toggle
        SoundModeToggle(...)

        // Effect selector (8 effects)
        SoundEffectSelector(...)

        // Sensitivity slider (0-100)
        SensitivitySlider(...)

        // Palette picker
        PalettePickerSection(...)
    }
}
```

### Anti-Patterns to Avoid

- **App-side audio capture:** Per PROJECT.md, phone mic is "useless". Do NOT implement AudioRecord/TarsosDSP for Phase 3.
- **Staying connected during sound mode:** Tower is autonomous. Don't maintain BLE connection just for sound reactivity.
- **Custom animation upload:** Protocol doesn't support it. Don't try to send keyframe data to tower memory.

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Color picker | Custom HSV wheel | GoDaddy ColorPicker (already in project) | Battle-tested, accessibility support |
| BLE command queue | Raw concurrent writes | TowerCommandQueue (already built) | Prevents GATT_ERROR 133 |
| Preset access | Direct DB queries | AnimationRepository (already built) | Proper domain boundaries |
| Navigation | Manual back stack | Navigation Compose with type-safe routes | Already established pattern |

**Key insight:** Phase 3 extends existing infrastructure. No new libraries needed.

## Sound Effect Enumeration

**Verified from elkbledom const.py (HIGH confidence):**

| Effect ID | Hex | Suggested Name | Description |
|-----------|-----|----------------|-------------|
| 128 | 0x80 | Energetic | Reactive flash pattern |
| 129 | 0x81 | Pulse | Rhythmic pulse effect |
| 130 | 0x82 | Fade | Smooth audio fade |
| 131 | 0x83 | Jump | Quick color jumps |
| 132 | 0x84 | Flow | Flowing audio pattern |
| 133 | 0x85 | Strobe | Strobe on audio |
| 134 | 0x86 | Rainbow | Rainbow audio sync |
| 135 | 0x87 | Wave | Wave pattern |

**Note:** Exact effect behaviors may vary. Names above are inferred from similar libraries. Recommend exposing all 8 and letting user discover favorites.

## Common Pitfalls

### Pitfall 1: Forgetting to Set Base Color Before Enabling Mic
**What goes wrong:** Mic effect activates but tower uses last color, not user's chosen palette.
**Why it happens:** Protocol uses current color state as base for effects.
**How to avoid:** Always send `setColor()` command BEFORE `enableMic()`.
**Warning signs:** Sound mode works but wrong colors appear.

### Pitfall 2: Not Disabling Mic Before Switching to Manual Mode
**What goes wrong:** User switches to Control screen but tower still responds to sound.
**Why it happens:** Sound mode persists on tower until explicitly disabled.
**How to avoid:** Add `disableMic()` call when navigating away from sound screen OR when user explicitly toggles sound mode off.
**Warning signs:** Tower flashes unexpectedly when music plays even though app shows Control screen.

### Pitfall 3: Sending Commands Without Connection Check
**What goes wrong:** App crashes or silently fails when tower disconnects mid-configuration.
**Why it happens:** BLE connection can drop between commands.
**How to avoid:** Check connection state before sending command batch. Reuse Phase 1 connection handling patterns.
**Warning signs:** Partial configuration applied (e.g., color set but mic not enabled).

### Pitfall 4: Palette with Too Many Colors
**What goes wrong:** User expects all 5 palette colors to cycle, but tower only uses base color for effects.
**Why it happens:** Protocol limitations - mic effects use single base color, not color sequences.
**How to avoid:**
  - Option A: Cycle through palette colors automatically from app (requires staying connected, violates D-03)
  - Option B: Let user pick PRIMARY color from palette, explain effects use single base color
  - **Recommendation:** Option B aligns with fire-and-forget architecture.
**Warning signs:** User confusion when all palette colors don't appear in sound mode.

### Pitfall 5: Navigation Tab Overcrowding
**What goes wrong:** Adding "Sound" tab makes bottom nav feel cramped (5 tabs: Devices, Control, Editor, Presets, Sound).
**Why it happens:** Material Design recommends 3-5 tabs maximum.
**How to avoid:** Consider making Sound a mode within Control screen rather than separate tab, OR remove Editor from bottom nav (accessible via Presets screen "+" button).
**Warning signs:** Tabs are too narrow, labels truncate.

## Code Examples

### SoundEffect Enum
```kotlin
// Source: Derived from elkbledom const.py
enum class SoundEffect(val id: Byte, val displayName: String) {
    ENERGETIC(0x80.toByte(), "Energetic"),
    PULSE(0x81.toByte(), "Pulse"),
    FADE(0x82.toByte(), "Fade"),
    JUMP(0x83.toByte(), "Jump"),
    FLOW(0x84.toByte(), "Flow"),
    STROBE(0x85.toByte(), "Strobe"),
    RAINBOW(0x86.toByte(), "Rainbow"),
    WAVE(0x87.toByte(), "Wave");

    companion object {
        val all = entries.toList()
    }
}
```

### MelkProtocol Extension
```kotlin
// Add to MelkProtocol.kt
object MelkProtocol {
    // ... existing code ...

    // ========== Sound Mode Commands ==========

    fun enableMic(): ByteArray = byteArrayOf(
        START_BYTE,
        0x04, 0x07, 0x01,
        0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0x00,
        END_BYTE
    )

    fun disableMic(): ByteArray = byteArrayOf(
        START_BYTE,
        0x04, 0x07, 0x00,
        0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0x00,
        END_BYTE
    )

    fun setMicEffect(effectId: Byte): ByteArray = byteArrayOf(
        START_BYTE,
        0x05, 0x03, effectId, 0x04,
        0xFF.toByte(), 0xFF.toByte(), 0x00,
        END_BYTE
    )

    fun setMicSensitivity(value: Int): ByteArray = byteArrayOf(
        START_BYTE,
        0x04, 0x06,
        value.coerceIn(0, 100).toByte(),
        0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0x00,
        END_BYTE
    )
}
```

### SensitivitySlider Component
```kotlin
// Source: Pattern from BrightnessSlider in Phase 1
@Composable
fun SensitivitySlider(
    sensitivity: Int,
    onSensitivityChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Sensitivity", style = MaterialTheme.typography.titleMedium)
            Text("$sensitivity%", style = MaterialTheme.typography.bodyMedium)
        }
        Slider(
            value = sensitivity.toFloat(),
            onValueChange = { onSensitivityChanged(it.toInt()) },
            valueRange = 0f..100f,
            steps = 9, // 10 positions: 0, 10, 20... 100
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Low", style = MaterialTheme.typography.labelSmall)
            Text("High", style = MaterialTheme.typography.labelSmall)
        }
    }
}
```

### Palette Extraction
```kotlin
// Extension function for Animation model
fun Animation.extractPalette(maxColors: Int = 5): List<Color> {
    return keyframes
        .map { Color(it.color) }
        .distinctBy {
            // Group similar colors (within ~10 hue degrees)
            val hsl = floatArrayOf(0f, 0f, 0f)
            android.graphics.Color.colorToHSL(it.toArgb(), hsl)
            (hsl[0] / 10).toInt() // Bucket by hue
        }
        .take(maxColors)
}
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| App-side audio processing | Tower-native microphone | Protocol design | Simpler architecture, better latency |
| Continuous BLE connection for audio | Fire-and-forget config | D-02, D-03 | Battery savings, reliability |

**Deprecated/outdated:**
- Phone microphone for LED control: User explicitly stated "useless" (PROJECT.md constraint)
- Complex FFT/beat detection: Deferred to v2 (AUDIO-01, AUDIO-02)

## Open Questions

1. **Palette Color Cycling**
   - What we know: Tower mic effects use single base color
   - What's unclear: Can multiple colors be used by rapidly cycling colors from app?
   - Recommendation: Per D-03, fire-and-forget is preferred. Document limitation that sound mode uses primary palette color only. If user strongly wants color cycling, it requires app-connected mode (Phase 2 animation playback).

2. **VU Meter / Audio Level Feedback (D-09)**
   - What we know: Protocol has parameters for "streaming external mic" suggesting possible bidirectional data
   - What's unclear: Does tower send audio level data via BLE notifications?
   - Recommendation: Skip VU meter for Phase 3. Would require BLE notification subscription and testing. Add as enhancement if time permits.

3. **Effect Names**
   - What we know: 8 effects exist (0x80-0x87)
   - What's unclear: Exact behavior of each effect on MELK-OT21 specifically
   - Recommendation: Use generic names (Effect 1-8) or test on hardware to determine appropriate names. Consider adding user-facing effect preview descriptions.

## Environment Availability

> SKIPPED (no external dependencies identified)

This phase uses only existing project infrastructure. No new tools, services, or runtimes required.

## Navigation Placement Recommendation

**Current bottom nav:** Devices | Control | Presets (3 tabs)

**Options:**

1. **Add Sound tab (4 tabs):** Devices | Control | Sound | Presets
   - Pro: Direct access, clear mental model
   - Con: Presets less prominent

2. **Mode toggle in Control screen:**
   - Pro: Fewer tabs, sound is a "control mode"
   - Con: Deeper navigation, may confuse users

3. **Sound accessible from Control via tab or toggle:**
   - Pro: Related controls grouped
   - Con: Requires UI redesign of Control screen

**Recommendation:** Option 1 (Add Sound tab). Four tabs is within Material Design guidelines. Sound mode is a major differentiating feature (per CONTEXT.md "main appeal") and deserves prominent nav placement.

**Icon suggestion:** `Icons.Default.GraphicEq` or `Icons.Default.Mic` for Sound tab.

## Project Constraints (from CLAUDE.md)

### Enforced Constraints
- **Audio Source:** "Device internal microphone only - phone mic is 'useless' per user" - Research confirms tower-native mic approach
- **Protocol:** "Must work with MELK-OT21 BLE characteristics" - Verified protocol commands from elkbledom
- **Platform:** Android (Kotlin) - Extends existing app architecture

### Applicable Stack Decisions
- Nordic BLE Library for command transmission (already integrated)
- Jetpack Compose for UI (already established)
- Room for preset storage (already used for palette extraction)
- MVVM + StateFlow pattern (already established)

## Sources

### Primary (HIGH confidence)
- [elkbledom/elkbledom.py](https://github.com/dave-code-ruiz/elkbledom/blob/main/custom_components/elkbledom/elkbledom.py) - Sound mode command bytes (enable_mic, disable_mic, set_mic_effect, set_mic_sensitivity)
- [elkbledom/const.py](https://github.com/dave-code-ruiz/elkbledom/blob/main/custom_components/elkbledom/const.py) - MIC_EFFECTS enum (0x80-0x87)
- Existing codebase: `MelkProtocol.kt`, `TowerCommandQueue.kt`, `ControlScreen.kt` - Established patterns

### Secondary (MEDIUM confidence)
- [FergusInLondon/ELK-BLEDOM Protocol](https://github.com/FergusInLondon/ELK-BLEDOM/blob/master/PROTCOL.md) - Protocol structure, notes sound features exist but not fully documented
- [elkbledom/definitions.json](https://github.com/dave-code-ruiz/elkbledom/blob/main/custom_components/elkbledom/definitions.json) - EFFECTS_STRIPX music effects (IDs 384-391) - may not apply to MELK-OT21
- [elkbledom Issue #90](https://github.com/dave-code-ruiz/elkbledom/issues/90) - MELK model variations, confirms MELK-OT supports scenes/modes

### Tertiary (LOW confidence - needs hardware verification)
- Sound effect names (0x80-0x87) - inferred from similar libraries, not device-tested
- Palette cycling behavior - protocol-limited, needs verification

## Metadata

**Confidence breakdown:**
- Protocol commands: HIGH - verified from elkbledom source code
- Sound effect IDs (0x80-0x87): HIGH - verified from const.py
- Effect names/behaviors: LOW - inferred, needs hardware testing
- Navigation recommendation: MEDIUM - follows Material Design, user preference may vary

**Research date:** 2026-03-28
**Valid until:** 2026-04-28 (30 days - protocol is stable)
