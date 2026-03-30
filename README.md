# MotherLEDisa

Web app for controlling MELK-OT21 / ELK-BLEDOM Bluetooth tower lights directly from your browser.

**[Open App](https://trainerHol.github.io/MotherLEDisa/)** (requires Chrome/Edge with Web Bluetooth)

## Features

- **Color Control** — Color wheel, hex input, and 12 quick-tap color swatches
- **Effects** — 22 built-in LED effects (crossfade, strobe, jump patterns)
- **Sound Reactive** — Two modes:
  - **Tower Mic** — Uses the tower's built-in microphone with 8 preset effects
  - **Custom Audio** — Captures system/mic audio on your computer and sends custom colors to the tower with 6 visualization modes (Frequency Color, Energy Brightness, Color Pulse, Spectrum Flow, Beat Strobe, Audio Breathe)
- **Animation Editor** — Timeline-based keyframe editor with draggable scrubber, color interpolation, and live device preview
- **Animation Templates** — One-click presets: Rainbow Sweep, Fire, Ocean, Strobe, Sunset Fade, Color Pulse
- **Preset Library** — Save, load, export/import animations as JSON
- **Protocol Explorer** — Dev tab for sending raw BLE commands and testing MELK-OT21 features (Symphony, Scene modes)

## Requirements

- Chrome, Edge, or Brave browser (Web Bluetooth API)
- HTTPS or localhost (Web Bluetooth requires secure context)
- MELK-OT21 or ELK-BLEDOM compatible tower light

## Development

```bash
node serve.js
# Open http://localhost:8080
```

No build tools, no dependencies. Pure vanilla HTML/CSS/JS.

## Protocol

Based on the ELK-BLEDOM BLE protocol:
- Service UUID: `0000fff0-0000-1000-8000-00805f9b34fb`
- Write Characteristic: `0000fff3-0000-1000-8000-00805f9b34fb`
- 9-byte fixed-length commands: `[0x7E, b1, CMD, p1, p2, p3, p4, b7, 0xEF]`

## License

MIT
