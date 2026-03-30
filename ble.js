// MotherLEDisa — BLE Communication Layer (Web Bluetooth API)
// Supports ELK-BLEDOM, MELK, and MELK-OT21 tower lights

const BLE = (() => {
  const SERVICE_UUID = '0000fff0-0000-1000-8000-00805f9b34fb';
  const CHAR_UUID = '0000fff3-0000-1000-8000-00805f9b34fb';

  const devices = new Map();
  const listeners = new Set();
  const MIN_CMD_GAP = 20;
  let lastWrite = 0;

  function notify() { listeners.forEach(fn => fn(getConnected())); }
  function onChange(fn) { listeners.add(fn); return () => listeners.delete(fn); }

  function getConnected() {
    return Array.from(devices.entries()).map(([id, d]) => ({
      id, name: d.name, connected: d.server?.connected ?? false,
    }));
  }

  async function scan() {
    if (!navigator.bluetooth) throw new Error('Web Bluetooth not supported. Use Chrome or Edge.');
    if (!window.isSecureContext) throw new Error('Web Bluetooth requires HTTPS or localhost.');
    console.log('[BLE] Requesting device...');
    return navigator.bluetooth.requestDevice({
      filters: [
        { namePrefix: 'ELK-' },
        { namePrefix: 'MELK-' },
        { namePrefix: 'LEDBLE' },
        { namePrefix: 'LED-' },
      ],
      optionalServices: [SERVICE_UUID],
    });
  }

  async function connect(device) {
    const id = device.id;
    if (devices.has(id) && devices.get(id).server?.connected) return devices.get(id);
    devices.delete(id);

    console.log(`[BLE] Connecting to ${device.name}...`);
    const server = await device.gatt.connect();
    const service = await server.getPrimaryService(SERVICE_UUID);
    const characteristic = await service.getCharacteristic(CHAR_UUID);
    const props = characteristic.properties;

    const entry = {
      device, server, characteristic,
      name: device.name || 'Unknown',
      writeMethod: props.writeWithoutResponse ? 'no-resp' : 'resp',
    };

    // MELK init sequence
    if (device.name?.toUpperCase().startsWith('MELK')) {
      console.log('[BLE] MELK init...');
      try {
        await writeBytes(entry, [0x7E, 0x07, 0x83, 0x00, 0x00, 0x00, 0x00, 0x00, 0xEF]);
        await sleep(100);
        await writeBytes(entry, [0x7E, 0x04, 0x04, 0xF0, 0x00, 0x01, 0xFF, 0x00, 0xEF]);
        await sleep(100);
      } catch (e) { console.warn('[BLE] MELK init warning:', e.message); }
    }

    devices.set(id, entry);
    device.addEventListener('gattserverdisconnected', () => {
      console.log(`[BLE] ${entry.name} disconnected`);
      devices.delete(id);
      notify();
    });
    notify();
    console.log(`[BLE] Connected: ${entry.name}`);
    return entry;
  }

  async function writeBytes(entry, bytes) {
    const wait = MIN_CMD_GAP - (Date.now() - lastWrite);
    if (wait > 0) await sleep(wait);
    lastWrite = Date.now();
    const data = new Uint8Array(bytes);
    if (entry.writeMethod === 'no-resp') {
      await entry.characteristic.writeValueWithoutResponse(data);
    } else {
      await entry.characteristic.writeValueWithResponse(data);
    }
  }

  async function disconnect(id) {
    const e = devices.get(id);
    if (e) { try { e.device.gatt.disconnect(); } catch {} devices.delete(id); notify(); }
  }

  async function write(id, bytes) {
    const entry = devices.get(id);
    if (!entry) return;
    try { await writeBytes(entry, bytes); }
    catch (e) {
      console.warn(`[BLE] Write fail (${entry.name}):`, e.message);
      if (!entry.server?.connected) { devices.delete(id); notify(); }
    }
  }

  async function writeAll(bytes) {
    await Promise.allSettled(Array.from(devices.keys()).map(id => write(id, bytes)));
  }

  async function writeTarget(t, bytes) {
    if (t === 'all') await writeAll(bytes); else await write(t, bytes);
  }

  function clamp(v, lo, hi) { return Math.max(lo, Math.min(hi, Math.round(v))); }
  function sleep(ms) { return new Promise(r => setTimeout(r, ms)); }

  // --- Protocol Commands (9-byte fixed) ---
  const CMD = {
    powerOn:        () => [0x7E, 0x04, 0x04, 0xF0, 0x00, 0x01, 0xFF, 0x00, 0xEF],
    powerOff:       () => [0x7E, 0x04, 0x04, 0x00, 0x00, 0x00, 0xFF, 0x00, 0xEF],
    setColor:    (r,g,b) => [0x7E, 0x07, 0x05, 0x03, r&0xFF, g&0xFF, b&0xFF, 0x10, 0xEF],
    setBrightness: (v) => [0x7E, 0x04, 0x01, clamp(v,0,100), 0xFF, 0x00, 0xFF, 0x00, 0xEF],
    setEffect:  (id,spd) => [0x7E, 0x05, 0x03, id&0xFF, clamp(spd,0,100), 0xFF, 0xFF, 0x00, 0xEF],
    setSpeed:      (v) => [0x7E, 0x04, 0x02, clamp(v,0,100), 0xFF, 0xFF, 0xFF, 0x00, 0xEF],
    micEnable:      () => [0x7E, 0x04, 0x07, 0x01, 0xFF, 0xFF, 0xFF, 0x00, 0xEF],
    micDisable:     () => [0x7E, 0x04, 0x07, 0x00, 0xFF, 0xFF, 0xFF, 0x00, 0xEF],
    micEffect:    (id) => [0x7E, 0x05, 0x03, id&0xFF, 0x04, 0xFF, 0xFF, 0x00, 0xEF],
    micSensitivity:(v) => [0x7E, 0x04, 0x06, clamp(v,0,100), 0xFF, 0xFF, 0xFF, 0x00, 0xEF],
    disableEffects: () => [0x7E, 0x00, 0x05, 0x01, 0x00, 0x00, 0x00, 0x00, 0xEF],

    // MELK-OA21 extended commands
    // Symphony: audio sync point (10-1000, little-endian 16-bit)
    setSymphony: (point) => {
      const p = Math.max(10, Math.min(1000, Math.round(point)));
      return [0x7E, 0x07, 0x21, p & 0xFF, (p >> 8) & 0xFF, 0x00, 0x00, 0x00, 0xEF];
    },
    // Scene: 28 preset scenes (0-27)
    setScene: (id) => [0x7E, 0x05, 0x31, id & 0xFF, 0x07, 0xFF, 0xFF, 0x01, 0xEF],
  };

  // High-level API
  async function powerOn(t)           { await writeTarget(t, CMD.powerOn()); }
  async function powerOff(t)          { await writeTarget(t, CMD.powerOff()); }
  async function setColor(t, r, g, b) { await writeTarget(t, CMD.setColor(r, g, b)); }
  async function setBrightness(t, v)  { await writeTarget(t, CMD.setBrightness(v)); }
  async function setEffect(t, id, s)  { await writeTarget(t, CMD.setEffect(id, s)); }
  async function setSpeed(t, v)       { await writeTarget(t, CMD.setSpeed(v)); }
  async function enableMic(t)         { await writeTarget(t, CMD.micEnable()); }
  async function disableMic(t)        { await writeTarget(t, CMD.micDisable()); }
  async function setMicEffect(t, id)  { await writeTarget(t, CMD.micEffect(id)); }
  async function setMicSensitivity(t,v){ await writeTarget(t, CMD.micSensitivity(v)); }

  // Activate sound reactive: enable mic, set effect, set sensitivity
  async function activateSoundMode(t, effectId, sensitivity) {
    await writeTarget(t, CMD.micEnable());
    await sleep(50);
    await writeTarget(t, CMD.micEffect(effectId));
    await sleep(50);
    await writeTarget(t, CMD.micSensitivity(sensitivity));
  }

  // Deactivate sound reactive
  async function deactivateSoundMode(t) {
    await writeTarget(t, CMD.micDisable());
  }

  // --- Experimental: reverse engineering commands ---
  // Send raw 9-byte command for testing
  async function sendRaw(t, bytes) {
    console.log('[BLE:RAW]', bytes.map(b => b.toString(16).padStart(2,'0')).join(' '));
    await writeTarget(t, bytes);
  }

  // Test: set color while mic mode is active (does it tint the effect?)
  async function testMicWithColor(t, effectId, sensitivity, r, g, b) {
    await writeTarget(t, CMD.micEnable());
    await sleep(50);
    await writeTarget(t, CMD.micEffect(effectId));
    await sleep(50);
    await writeTarget(t, CMD.micSensitivity(sensitivity));
    await sleep(50);
    // Try sending color on top of mic mode
    await writeTarget(t, CMD.setColor(r, g, b));
    console.log('[BLE:EXP] Sent color on top of mic mode');
  }

  // Test: set color first, then enable mic (does mic use the color as base?)
  async function testColorThenMic(t, r, g, b, effectId, sensitivity) {
    await writeTarget(t, CMD.setColor(r, g, b));
    await sleep(100);
    await writeTarget(t, CMD.micEnable());
    await sleep(50);
    await writeTarget(t, CMD.micEffect(effectId));
    await sleep(50);
    await writeTarget(t, CMD.micSensitivity(sensitivity));
    console.log('[BLE:EXP] Set color then mic mode');
  }

  // Test: undocumented streaming commands
  async function testStreamCmd(t, cmdId, param) {
    const cmd = [0x7E, 0x04, cmdId & 0xFF, param & 0xFF, 0xFF, 0xFF, 0xFF, 0x00, 0xEF];
    console.log('[BLE:EXP] Stream cmd', cmdId.toString(16), 'param', param.toString(16));
    await writeTarget(t, cmd);
  }

  // Test: set effect while mic mode is on (combine hardware effect with mic?)
  async function testMicWithEffect(t, micEffectId, hwEffectId, speed, sensitivity) {
    await writeTarget(t, CMD.micEnable());
    await sleep(50);
    await writeTarget(t, CMD.micEffect(micEffectId));
    await sleep(50);
    await writeTarget(t, CMD.micSensitivity(sensitivity));
    await sleep(50);
    // Now try a hardware effect on top
    await writeTarget(t, CMD.setEffect(hwEffectId, speed));
    console.log('[BLE:EXP] Sent hw effect on top of mic mode');
  }

  // Test: brightness during mic mode
  async function testMicWithBrightness(t, effectId, sensitivity, brightness) {
    await writeTarget(t, CMD.micEnable());
    await sleep(50);
    await writeTarget(t, CMD.micEffect(effectId));
    await sleep(50);
    await writeTarget(t, CMD.micSensitivity(sensitivity));
    await sleep(50);
    await writeTarget(t, CMD.setBrightness(brightness));
    console.log('[BLE:EXP] Set brightness during mic mode');
  }

  // Test: symphony command (MELK-OA21 audio sync point)
  async function testSymphony(t, point) {
    const cmd = CMD.setSymphony(point);
    console.log('[BLE:EXP] Symphony point', point, '->', cmd.map(b=>b.toString(16).padStart(2,'0')).join(' '));
    await writeTarget(t, cmd);
  }

  // Test: scene command (MELK-OA21 preset scenes)
  async function testScene(t, sceneId) {
    const cmd = CMD.setScene(sceneId);
    console.log('[BLE:EXP] Scene', sceneId, '->', cmd.map(b=>b.toString(16).padStart(2,'0')).join(' '));
    await writeTarget(t, cmd);
  }

  // Test: symphony while mic is active
  async function testMicWithSymphony(t, micEffect, sensitivity, symphonyPoint) {
    await writeTarget(t, CMD.micEnable());
    await sleep(50);
    await writeTarget(t, CMD.micEffect(micEffect));
    await sleep(50);
    await writeTarget(t, CMD.micSensitivity(sensitivity));
    await sleep(50);
    await writeTarget(t, CMD.setSymphony(symphonyPoint));
    console.log('[BLE:EXP] Mic + Symphony point', symphonyPoint);
  }

  // Test: speed during mic mode
  async function testMicWithSpeed(t, effectId, sensitivity, speed) {
    await writeTarget(t, CMD.micEnable());
    await sleep(50);
    await writeTarget(t, CMD.micEffect(effectId));
    await sleep(50);
    await writeTarget(t, CMD.micSensitivity(sensitivity));
    await sleep(50);
    await writeTarget(t, CMD.setSpeed(speed));
    console.log('[BLE:EXP] Set speed during mic mode');
  }

  // Effects library
  const EFFECTS = [
    { id: 0x87, name: 'Jump RGB',        cat: 'jump' },
    { id: 0x88, name: 'Jump Rainbow',     cat: 'jump' },
    { id: 0x89, name: 'Crossfade RGB',    cat: 'fade' },
    { id: 0x8A, name: 'Crossfade Rainbow',cat: 'fade' },
    { id: 0x8B, name: 'Crossfade Red',    cat: 'fade' },
    { id: 0x8C, name: 'Crossfade Green',  cat: 'fade' },
    { id: 0x8D, name: 'Crossfade Blue',   cat: 'fade' },
    { id: 0x8E, name: 'Crossfade Yellow', cat: 'fade' },
    { id: 0x8F, name: 'Crossfade Cyan',   cat: 'fade' },
    { id: 0x90, name: 'Crossfade Purple', cat: 'fade' },
    { id: 0x91, name: 'Crossfade White',  cat: 'fade' },
    { id: 0x92, name: 'Crossfade R-G',    cat: 'fade' },
    { id: 0x93, name: 'Crossfade R-B',    cat: 'fade' },
    { id: 0x94, name: 'Crossfade G-B',    cat: 'fade' },
    { id: 0x95, name: 'Strobe Rainbow',   cat: 'strobe' },
    { id: 0x96, name: 'Strobe Red',       cat: 'strobe' },
    { id: 0x97, name: 'Strobe Green',     cat: 'strobe' },
    { id: 0x98, name: 'Strobe Blue',      cat: 'strobe' },
    { id: 0x99, name: 'Strobe Yellow',    cat: 'strobe' },
    { id: 0x9A, name: 'Strobe Cyan',      cat: 'strobe' },
    { id: 0x9B, name: 'Strobe Purple',    cat: 'strobe' },
    { id: 0x9C, name: 'Strobe White',     cat: 'strobe' },
  ];

  const MIC_EFFECTS = [
    { id: 0x80, name: 'Energetic' },
    { id: 0x81, name: 'Rhythm' },
    { id: 0x82, name: 'Spectrum' },
    { id: 0x83, name: 'Rolling' },
    { id: 0x84, name: 'Pulse' },
    { id: 0x85, name: 'Cascade' },
    { id: 0x86, name: 'Rainbow' },
    { id: 0x87, name: 'Wave' },
  ];

  return {
    scan, connect, disconnect, getConnected, onChange,
    powerOn, powerOff, setColor, setBrightness,
    setEffect, setSpeed, enableMic, disableMic,
    setMicEffect, setMicSensitivity,
    activateSoundMode, deactivateSoundMode,
    // Experimental
    sendRaw, testMicWithColor, testColorThenMic,
    testStreamCmd, testMicWithEffect,
    testMicWithBrightness, testMicWithSpeed,
    testSymphony, testScene, testMicWithSymphony,
    write, writeAll, writeTarget,
    CMD, EFFECTS, MIC_EFFECTS, devices, sleep,
  };
})();
