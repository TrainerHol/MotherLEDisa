// MotherLEDisa — Web Audio Sound Reactive Engine
// Captures system/mic audio, analyzes with FFT, sends custom colors to tower

const AudioReactive = (() => {
  let audioCtx = null;
  let analyser = null;
  let source = null;
  let stream = null;
  let running = false;
  let rafId = null;
  let freqData = null;
  let timeData = null;

  // Config
  let config = {
    mode: 'frequency',     // frequency | energy | pulse | spectrum
    color1: { r: 255, g: 0, b: 0 },
    color2: { r: 0, g: 0, b: 255 },
    sensitivity: 1.5,
    smoothing: 0.8,
    minBrightness: 5,
    maxBrightness: 100,
    sendRate: 30,          // ms between BLE writes
  };

  let lastSend = 0;
  let onFrameCallback = null;

  // Visualization modes
  const MODES = [
    { id: 'frequency', name: 'Frequency Color', desc: 'Maps dominant frequency to color gradient' },
    { id: 'energy', name: 'Energy Brightness', desc: 'Audio volume controls brightness of chosen color' },
    { id: 'pulse', name: 'Color Pulse', desc: 'Pulses between two colors based on bass energy' },
    { id: 'spectrum', name: 'Spectrum Flow', desc: 'Sweeps through color spectrum based on frequency bands' },
    { id: 'strobe', name: 'Beat Strobe', desc: 'Flashes on beat detection using chosen color' },
    { id: 'breathe', name: 'Audio Breathe', desc: 'Smooth breathing effect modulated by audio level' },
  ];

  async function start(opts = {}) {
    Object.assign(config, opts);

    if (!audioCtx) {
      audioCtx = new (window.AudioContext || window.webkitAudioContext)();
    }

    try {
      // Try to get system audio (screen share) or fall back to mic
      if (opts.useSystemAudio) {
        // Request display media for system audio capture
        stream = await navigator.mediaDevices.getDisplayMedia({
          video: { width: 1, height: 1 }, // minimal video
          audio: true,
        });
        // Stop the video track, we only want audio
        stream.getVideoTracks().forEach(t => t.stop());
      } else {
        stream = await navigator.mediaDevices.getUserMedia({
          audio: {
            echoCancellation: false,
            noiseSuppression: false,
            autoGainControl: false,
          }
        });
      }
    } catch (e) {
      console.error('[Audio] Failed to get audio stream:', e);
      throw new Error('Could not access audio: ' + e.message);
    }

    source = audioCtx.createMediaStreamSource(stream);
    analyser = audioCtx.createAnalyser();
    analyser.fftSize = 256;
    analyser.smoothingTimeConstant = config.smoothing;
    source.connect(analyser);

    freqData = new Uint8Array(analyser.frequencyBinCount);
    timeData = new Uint8Array(analyser.fftSize);

    running = true;
    lastSend = 0;
    tick();
    console.log('[Audio] Started, mode:', config.mode);
  }

  function stop() {
    running = false;
    if (rafId) cancelAnimationFrame(rafId);
    if (stream) stream.getTracks().forEach(t => t.stop());
    if (source) { try { source.disconnect(); } catch {} }
    stream = null;
    source = null;
    console.log('[Audio] Stopped');
  }

  function setConfig(opts) {
    Object.assign(config, opts);
    if (analyser && opts.smoothing !== undefined) {
      analyser.smoothingTimeConstant = opts.smoothing;
    }
  }

  function onFrame(fn) { onFrameCallback = fn; }

  function tick() {
    if (!running) return;
    rafId = requestAnimationFrame(tick);

    analyser.getByteFrequencyData(freqData);
    analyser.getByteTimeDomainData(timeData);

    const now = Date.now();
    if (now - lastSend < config.sendRate) return;
    lastSend = now;

    let color, brightness;

    switch (config.mode) {
      case 'frequency':
        ({ color, brightness } = modeFrequency());
        break;
      case 'energy':
        ({ color, brightness } = modeEnergy());
        break;
      case 'pulse':
        ({ color, brightness } = modePulse());
        break;
      case 'spectrum':
        ({ color, brightness } = modeSpectrum());
        break;
      case 'strobe':
        ({ color, brightness } = modeStrobe());
        break;
      case 'breathe':
        ({ color, brightness } = modeBreathe());
        break;
      default:
        ({ color, brightness } = modeEnergy());
    }

    // Clamp
    color.r = Math.round(Math.max(0, Math.min(255, color.r)));
    color.g = Math.round(Math.max(0, Math.min(255, color.g)));
    color.b = Math.round(Math.max(0, Math.min(255, color.b)));
    brightness = Math.round(Math.max(config.minBrightness, Math.min(config.maxBrightness, brightness)));

    // Send to device
    if (BLE.getConnected().length > 0) {
      BLE.setColor('all', color.r, color.g, color.b);
    }

    // Callback for UI visualization
    if (onFrameCallback) {
      onFrameCallback({ color, brightness, freqData: new Uint8Array(freqData), rms: getRMS() });
    }
  }

  // --- Analysis helpers ---
  function getRMS() {
    let sum = 0;
    for (let i = 0; i < timeData.length; i++) {
      const v = (timeData[i] - 128) / 128;
      sum += v * v;
    }
    return Math.sqrt(sum / timeData.length);
  }

  function getEnergy(startBin, endBin) {
    let sum = 0;
    const end = Math.min(endBin, freqData.length);
    for (let i = startBin; i < end; i++) sum += freqData[i];
    return sum / (end - startBin) / 255;
  }

  function getDominantFreqBin() {
    let maxVal = 0, maxBin = 0;
    for (let i = 1; i < freqData.length; i++) {
      if (freqData[i] > maxVal) { maxVal = freqData[i]; maxBin = i; }
    }
    return { bin: maxBin, value: maxVal / 255 };
  }

  function lerpColor(c1, c2, t) {
    return {
      r: c1.r + (c2.r - c1.r) * t,
      g: c1.g + (c2.g - c1.g) * t,
      b: c1.b + (c2.b - c1.b) * t,
    };
  }

  function hslToRgb(h, s, l) {
    h = ((h % 360) + 360) % 360;
    s = Math.max(0, Math.min(1, s));
    l = Math.max(0, Math.min(1, l));
    const c = (1 - Math.abs(2 * l - 1)) * s;
    const x = c * (1 - Math.abs((h / 60) % 2 - 1));
    const m = l - c / 2;
    let r, g, b;
    if (h < 60) { r = c; g = x; b = 0; }
    else if (h < 120) { r = x; g = c; b = 0; }
    else if (h < 180) { r = 0; g = c; b = x; }
    else if (h < 240) { r = 0; g = x; b = c; }
    else if (h < 300) { r = x; g = 0; b = c; }
    else { r = c; g = 0; b = x; }
    return { r: (r + m) * 255, g: (g + m) * 255, b: (b + m) * 255 };
  }

  // --- Visualization Modes ---

  function modeFrequency() {
    const { bin, value } = getDominantFreqBin();
    const t = bin / freqData.length; // 0=low freq, 1=high freq
    const color = lerpColor(config.color1, config.color2, t);
    const brightness = value * config.sensitivity * config.maxBrightness;
    return { color, brightness };
  }

  function modeEnergy() {
    const rms = getRMS() * config.sensitivity;
    const brightness = rms * config.maxBrightness;
    const t = Math.min(1, rms * 2);
    const color = lerpColor(config.color1, config.color2, t);
    return { color, brightness };
  }

  function modePulse() {
    const bass = getEnergy(0, 8) * config.sensitivity;
    const t = Math.min(1, bass * 3);
    const color = lerpColor(config.color1, config.color2, t);
    const brightness = (0.3 + bass * 0.7) * config.maxBrightness;
    return { color, brightness };
  }

  function modeSpectrum() {
    const { bin } = getDominantFreqBin();
    const hue = (bin / freqData.length) * 360;
    const rms = getRMS() * config.sensitivity;
    const color = hslToRgb(hue, 1, 0.3 + Math.min(0.2, rms));
    const brightness = Math.max(30, rms * config.maxBrightness);
    return { color, brightness };
  }

  let strobeState = false;
  let lastBeat = 0;
  let beatThreshold = 0.5;

  function modeStrobe() {
    const bass = getEnergy(0, 6) * config.sensitivity;
    const now = Date.now();
    if (bass > beatThreshold && now - lastBeat > 100) {
      strobeState = !strobeState;
      lastBeat = now;
    }
    if (bass < beatThreshold * 0.6) strobeState = false;
    const color = strobeState ? config.color1 : { r: 0, g: 0, b: 0 };
    const brightness = strobeState ? config.maxBrightness : config.minBrightness;
    return { color, brightness };
  }

  let breathePhase = 0;
  function modeBreathe() {
    const rms = getRMS() * config.sensitivity;
    breathePhase += 0.02 + rms * 0.1;
    const wave = (Math.sin(breathePhase) + 1) / 2;
    const modulated = wave * (0.3 + rms * 0.7);
    const color = lerpColor(config.color1, config.color2, modulated);
    const brightness = modulated * config.maxBrightness;
    return { color, brightness };
  }

  return {
    start, stop, setConfig, onFrame,
    isRunning: () => running,
    MODES,
    getConfig: () => ({ ...config }),
  };
})();
