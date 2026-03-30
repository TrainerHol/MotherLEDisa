// MotherLEDisa — Main App Controller
(() => {
  'use strict';

  // --- State ---
  let currentColor = { r: 255, g: 0, b: 0 };
  let currentAnimation = Animation.createAnimation('Untitled', 5000);
  let selectedKeyframe = null;
  let animPlayer = null;
  let currentTab = 'devices';
  let soundModeActive = false;
  let selectedMicEffect = 0x80;
  const SEGMENTS = 5;
  const LABEL_W = 56;

  // Presets (localStorage)
  function loadPresets() {
    try { return JSON.parse(localStorage.getItem('motherledisa_presets') || '[]'); }
    catch { return []; }
  }
  function savePresetsStore(p) { localStorage.setItem('motherledisa_presets', JSON.stringify(p)); }

  // --- Tabs ---
  document.querySelectorAll('.tab').forEach(tab => {
    tab.addEventListener('click', () => {
      document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
      document.querySelectorAll('.tab-content').forEach(tc => tc.classList.remove('active'));
      tab.classList.add('active');
      document.getElementById(`tab-${tab.dataset.tab}`).classList.add('active');
      currentTab = tab.dataset.tab;
      if (currentTab === 'animation') requestAnimationFrame(renderTimeline);
      if (currentTab === 'presets') renderPresets();
    });
  });

  // --- Connection Status ---
  BLE.onChange(connected => {
    const dot = document.getElementById('status-dot');
    const text = document.getElementById('status-text');
    if (connected.length === 0) {
      dot.className = 'dot disconnected';
      text.textContent = 'No devices';
    } else {
      dot.className = 'dot connected';
      text.textContent = `${connected.length} device${connected.length > 1 ? 's' : ''}`;
    }
    updateTargetSelectors(connected);
    renderDevices(connected);
  });

  function updateTargetSelectors(connected) {
    ['control-target', 'sound-target'].forEach(id => {
      const sel = document.getElementById(id);
      const v = sel.value;
      sel.innerHTML = '<option value="all">All Devices</option>';
      connected.forEach(d => {
        const o = document.createElement('option');
        o.value = d.id; o.textContent = d.name;
        sel.appendChild(o);
      });
      sel.value = v || 'all';
    });
  }

  // --- Devices ---
  document.getElementById('btn-scan').addEventListener('click', async () => {
    try {
      const device = await BLE.scan();
      await BLE.connect(device);
    } catch (e) {
      if (e.name !== 'NotFoundError') alert('Connection failed: ' + e.message);
    }
  });

  function renderDevices(connected) {
    const list = document.getElementById('device-list');
    const empty = document.getElementById('device-empty');
    list.innerHTML = '';
    empty.style.display = connected.length === 0 ? 'block' : 'none';
    connected.forEach(d => {
      const card = document.createElement('div');
      card.className = 'device-card';
      card.innerHTML = `
        <div>
          <div class="device-name">${d.name}</div>
          <div class="device-id">${d.id.substring(0, 20)}</div>
        </div>
        <button class="btn btn-small btn-danger" data-dc="${d.id}">Disconnect</button>
      `;
      card.querySelector('[data-dc]').onclick = () => BLE.disconnect(d.id);
      list.appendChild(card);
    });
  }

  // --- Control: Color Wheel ---
  const colorWheel = document.getElementById('color-wheel');
  const colorCtx = colorWheel.getContext('2d');
  const colorPreview = document.getElementById('color-preview');
  const colorHex = document.getElementById('color-hex');

  function drawColorWheel() {
    const cx = colorWheel.width / 2, cy = colorWheel.height / 2;
    const r = Math.min(cx, cy) - 4;
    for (let a = 0; a < 360; a++) {
      const g = colorCtx.createRadialGradient(cx, cy, 0, cx, cy, r);
      g.addColorStop(0, `hsl(${a},0%,100%)`);
      g.addColorStop(0.5, `hsl(${a},100%,50%)`);
      g.addColorStop(1, `hsl(${a},100%,20%)`);
      colorCtx.beginPath();
      colorCtx.moveTo(cx, cy);
      colorCtx.arc(cx, cy, r, (a - 1) * Math.PI / 180, (a + 1) * Math.PI / 180);
      colorCtx.closePath();
      colorCtx.fillStyle = g;
      colorCtx.fill();
    }
  }
  drawColorWheel();

  function pickColor(e) {
    const rect = colorWheel.getBoundingClientRect();
    const x = (e.clientX - rect.left) * (colorWheel.width / rect.width);
    const y = (e.clientY - rect.top) * (colorWheel.height / rect.height);
    const px = colorCtx.getImageData(x, y, 1, 1).data;
    if (px[3] === 0) return;
    currentColor = { r: px[0], g: px[1], b: px[2] };
    updateColorUI();
    BLE.setColor(document.getElementById('control-target').value, px[0], px[1], px[2]);
  }

  let wheelDown = false;
  colorWheel.addEventListener('mousedown', e => { wheelDown = true; pickColor(e); });
  colorWheel.addEventListener('mousemove', e => { if (wheelDown) pickColor(e); });
  document.addEventListener('mouseup', () => wheelDown = false);
  colorWheel.addEventListener('touchstart', e => { e.preventDefault(); pickColor(e.touches[0]); });
  colorWheel.addEventListener('touchmove', e => { e.preventDefault(); pickColor(e.touches[0]); });

  colorHex.addEventListener('change', () => {
    currentColor = Animation.hexToRgb(colorHex.value);
    updateColorUI();
    BLE.setColor(document.getElementById('control-target').value, currentColor.r, currentColor.g, currentColor.b);
  });

  function updateColorUI() {
    const hex = Animation.rgbToHex(currentColor.r, currentColor.g, currentColor.b);
    colorPreview.style.background = hex;
    colorHex.value = hex;
  }

  // Quick color swatches
  document.querySelectorAll('.qcolor').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.qcolor').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      const rgb = Animation.hexToRgb(btn.dataset.c);
      currentColor = rgb;
      updateColorUI();
      BLE.setColor(document.getElementById('control-target').value, rgb.r, rgb.g, rgb.b);
    });
  });

  // Power
  document.getElementById('btn-power-on').onclick = () => BLE.powerOn(document.getElementById('control-target').value);
  document.getElementById('btn-power-off').onclick = () => BLE.powerOff(document.getElementById('control-target').value);

  // Brightness
  const brightSlider = document.getElementById('brightness-slider');
  const brightVal = document.getElementById('brightness-value');
  brightSlider.addEventListener('input', () => brightVal.textContent = brightSlider.value + '%');
  brightSlider.addEventListener('change', () => BLE.setBrightness(document.getElementById('control-target').value, +brightSlider.value));

  // Speed
  const speedSlider = document.getElementById('speed-slider');
  const speedVal = document.getElementById('speed-value');
  speedSlider.addEventListener('input', () => speedVal.textContent = speedSlider.value);
  speedSlider.addEventListener('change', () => {
    const target = document.getElementById('control-target').value;
    BLE.setSpeed(target, +speedSlider.value);
    // Re-send active effect with new speed
    if (activeEffect !== null) BLE.setEffect(target, activeEffect, +speedSlider.value);
  });

  // Effects grid — grouped by category
  const effectGrid = document.getElementById('effect-grid');
  let activeEffect = null;

  // "None" button to clear effects
  const noneBtn = document.createElement('button');
  noneBtn.className = 'effect-btn effect-none active';
  noneBtn.textContent = 'Static Color';
  noneBtn.onclick = () => {
    effectGrid.querySelectorAll('.effect-btn').forEach(b => b.classList.remove('active'));
    noneBtn.classList.add('active');
    activeEffect = null;
    // Send current color to clear effect
    BLE.setColor(document.getElementById('control-target').value, currentColor.r, currentColor.g, currentColor.b);
  };
  effectGrid.appendChild(noneBtn);

  let lastCat = '';
  BLE.EFFECTS.forEach(ef => {
    // Category divider
    if (ef.cat !== lastCat) {
      lastCat = ef.cat;
      const divider = document.createElement('div');
      divider.className = 'effect-cat-divider';
      divider.textContent = ef.cat.toUpperCase();
      effectGrid.appendChild(divider);
    }
    const btn = document.createElement('button');
    btn.className = 'effect-btn';
    btn.textContent = ef.name;
    btn.onclick = () => {
      effectGrid.querySelectorAll('.effect-btn').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      activeEffect = ef.id;
      BLE.setEffect(document.getElementById('control-target').value, ef.id, +speedSlider.value);
    };
    effectGrid.appendChild(btn);
  });

  // ======================
  // SOUND REACTIVE TAB
  // ======================
  const soundGrid = document.getElementById('sound-effect-grid');
  const soundToggle = document.getElementById('sound-toggle');
  const soundStatus = document.getElementById('sound-status');
  const soundStatusLabel = soundStatus.querySelector('.sound-status-label');
  const micSens = document.getElementById('mic-sensitivity');
  const micSensVal = document.getElementById('mic-sensitivity-value');

  // Build sound effect buttons
  BLE.MIC_EFFECTS.forEach(ef => {
    const btn = document.createElement('button');
    btn.className = 'sound-effect-btn' + (ef.id === selectedMicEffect ? ' active' : '');
    btn.textContent = ef.name;
    btn.onclick = () => {
      soundGrid.querySelectorAll('.sound-effect-btn').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      selectedMicEffect = ef.id;
      if (soundModeActive) {
        BLE.setMicEffect(document.getElementById('sound-target').value, ef.id);
      }
    };
    soundGrid.appendChild(btn);
  });

  // Toggle sound mode
  soundToggle.addEventListener('click', async () => {
    const target = document.getElementById('sound-target').value;
    if (!soundModeActive) {
      // Activate: enable mic -> set effect -> set sensitivity
      await BLE.activateSoundMode(target, selectedMicEffect, +micSens.value);
      soundModeActive = true;
      soundStatus.classList.add('on');
      soundStatus.classList.remove('off');
      soundStatusLabel.textContent = 'Sound Mode: ON';
      soundToggle.textContent = 'Disable Sound Mode';
      soundToggle.className = 'btn btn-danger btn-wide';
    } else {
      await BLE.deactivateSoundMode(target);
      soundModeActive = false;
      soundStatus.classList.remove('on');
      soundStatus.classList.add('off');
      soundStatusLabel.textContent = 'Sound Mode: OFF';
      soundToggle.textContent = 'Enable Sound Mode';
      soundToggle.className = 'btn btn-success btn-wide';
    }
  });

  // Sensitivity
  micSens.addEventListener('input', () => micSensVal.textContent = micSens.value);
  micSens.addEventListener('change', () => {
    if (soundModeActive) {
      BLE.setMicSensitivity(document.getElementById('sound-target').value, +micSens.value);
    }
  });

  // --- Sound source tab switching ---
  document.querySelectorAll('.sound-src-tab').forEach(tab => {
    tab.addEventListener('click', () => {
      document.querySelectorAll('.sound-src-tab').forEach(t => t.classList.remove('active'));
      document.querySelectorAll('.sound-section').forEach(s => s.classList.remove('active'));
      tab.classList.add('active');
      document.getElementById(`sound-${tab.dataset.src}`).classList.add('active');
    });
  });

  // ======================
  // CUSTOM SOUND REACTIVE
  // ======================
  let customSoundActive = false;
  let selectedCustomMode = 'frequency';
  const customToggle = document.getElementById('custom-sound-toggle');
  const customStatus = document.getElementById('custom-sound-status');
  const customStatusLabel = customStatus.querySelector('.sound-status-label');

  // Build mode buttons
  const customModeGrid = document.getElementById('custom-mode-grid');
  AudioReactive.MODES.forEach(m => {
    const btn = document.createElement('button');
    btn.className = 'sound-effect-btn' + (m.id === selectedCustomMode ? ' active' : '');
    btn.innerHTML = `<strong>${m.name}</strong><br><span style="font-size:0.65rem;color:var(--text-muted)">${m.desc}</span>`;
    btn.onclick = () => {
      customModeGrid.querySelectorAll('.sound-effect-btn').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      selectedCustomMode = m.id;
      if (customSoundActive) AudioReactive.setConfig({ mode: m.id });
    };
    customModeGrid.appendChild(btn);
  });

  // Custom toggle
  customToggle.addEventListener('click', async () => {
    if (!customSoundActive) {
      try {
        const c1 = Animation.hexToRgb(document.getElementById('custom-color1').value);
        const c2 = Animation.hexToRgb(document.getElementById('custom-color2').value);
        const useSystem = document.querySelector('input[name="audio-src"]:checked').value === 'system';
        await AudioReactive.start({
          mode: selectedCustomMode,
          color1: c1, color2: c2,
          sensitivity: +document.getElementById('custom-sensitivity').value,
          sendRate: +document.getElementById('custom-rate').value,
          useSystemAudio: useSystem,
        });

        // Visualizer with gradient bars and color swatch
        const vizCanvas = document.getElementById('audio-viz');
        const vizCtx = vizCanvas.getContext('2d');
        const swatch = document.getElementById('viz-color-swatch');
        AudioReactive.onFrame(({ color, freqData, rms }) => {
          // Background
          vizCtx.fillStyle = '#0a0e14';
          vizCtx.fillRect(0, 0, vizCanvas.width, vizCanvas.height);

          // Frequency bars with gradient
          const barW = Math.max(2, vizCanvas.width / freqData.length - 1);
          for (let i = 0; i < freqData.length; i++) {
            const h = (freqData[i] / 255) * vizCanvas.height;
            const x = i * (barW + 1);
            // Color gradient from bottom (dim) to top (bright)
            const grad = vizCtx.createLinearGradient(x, vizCanvas.height, x, vizCanvas.height - h);
            grad.addColorStop(0, `rgba(${color.r},${color.g},${color.b},0.3)`);
            grad.addColorStop(1, `rgb(${color.r},${color.g},${color.b})`);
            vizCtx.fillStyle = grad;
            vizCtx.fillRect(x, vizCanvas.height - h, barW, h);
          }

          // RMS level indicator line
          const rmsY = vizCanvas.height - (Math.min(1, rms * 2) * vizCanvas.height);
          vizCtx.strokeStyle = `rgba(${color.r},${color.g},${color.b},0.5)`;
          vizCtx.lineWidth = 1;
          vizCtx.setLineDash([4, 4]);
          vizCtx.beginPath();
          vizCtx.moveTo(0, rmsY);
          vizCtx.lineTo(vizCanvas.width, rmsY);
          vizCtx.stroke();
          vizCtx.setLineDash([]);

          // Update swatch
          swatch.style.background = `rgb(${color.r},${color.g},${color.b})`;
        });

        customSoundActive = true;
        customStatus.classList.add('on');
        customStatus.classList.remove('off');
        customStatusLabel.textContent = 'Custom Sound: ON';
        customToggle.textContent = 'Stop';
        customToggle.className = 'btn btn-danger btn-wide';
      } catch (e) {
        alert('Audio error: ' + e.message);
      }
    } else {
      AudioReactive.stop();
      customSoundActive = false;
      customStatus.classList.remove('on');
      customStatus.classList.add('off');
      customStatusLabel.textContent = 'Custom Sound: OFF';
      customToggle.textContent = 'Start';
      customToggle.className = 'btn btn-success btn-wide';
    }
  });

  // Custom sensitivity
  const customSens = document.getElementById('custom-sensitivity');
  const customSensVal = document.getElementById('custom-sensitivity-value');
  customSens.addEventListener('input', () => {
    customSensVal.textContent = customSens.value + 'x';
    if (customSoundActive) AudioReactive.setConfig({ sensitivity: +customSens.value });
  });

  // Custom send rate
  const customRate = document.getElementById('custom-rate');
  const customRateVal = document.getElementById('custom-rate-value');
  customRate.addEventListener('input', () => {
    customRateVal.textContent = customRate.value + 'ms';
    if (customSoundActive) AudioReactive.setConfig({ sendRate: +customRate.value });
  });

  // Custom colors
  document.getElementById('custom-color1').addEventListener('input', (e) => {
    if (customSoundActive) AudioReactive.setConfig({ color1: Animation.hexToRgb(e.target.value) });
  });
  document.getElementById('custom-color2').addEventListener('input', (e) => {
    if (customSoundActive) AudioReactive.setConfig({ color2: Animation.hexToRgb(e.target.value) });
  });

  // ======================
  // ANIMATION TAB
  // ======================
  function initTimeline() {
    const tracks = document.getElementById('timeline-tracks');
    tracks.innerHTML = '';
    for (let i = 0; i < SEGMENTS; i++) {
      const track = document.createElement('div');
      track.className = 'track';
      track.innerHTML = `<div class="track-label">T${i + 1}</div><div class="track-lane" data-seg="${i}"></div>`;
      track.querySelector('.track-lane').addEventListener('click', (e) => {
        if (e.target.classList.contains('keyframe-marker')) return;
        const rect = e.currentTarget.getBoundingClientRect();
        const ratio = (e.clientX - rect.left) / rect.width;
        const kf = Animation.createKeyframe(
          Math.round(ratio * currentAnimation.durationMs), i,
          document.getElementById('kf-color').value,
          document.getElementById('kf-interp').value
        );
        currentAnimation.keyframes.push(kf);
        selectedKeyframe = kf.id;
        renderTimeline();
      });
      tracks.appendChild(track);
    }
    renderTimeline();
  }

  function renderTimeline() {
    const container = document.getElementById('timeline-container');
    const ruler = document.getElementById('time-ruler');
    ruler.width = container.clientWidth;
    const rctx = ruler.getContext('2d');
    rctx.clearRect(0, 0, ruler.width, ruler.height);
    rctx.fillStyle = '#1c2333';
    rctx.fillRect(0, 0, ruler.width, ruler.height);

    const laneW = ruler.width - LABEL_W;
    const dur = currentAnimation.durationMs;

    // Time markers
    rctx.font = '9px monospace';
    rctx.textAlign = 'center';
    const step = dur <= 3000 ? 500 : dur <= 10000 ? 1000 : dur <= 30000 ? 2000 : 5000;
    for (let t = 0; t <= dur; t += step) {
      const x = LABEL_W + (t / dur) * laneW;
      rctx.fillStyle = '#30363d';
      rctx.fillRect(x, 20, 1, 10);
      rctx.fillStyle = '#8b949e';
      rctx.fillText((t / 1000).toFixed(1) + 's', x, 16);
    }

    // Keyframes + gradient
    document.querySelectorAll('.track-lane').forEach(lane => {
      lane.querySelectorAll('.keyframe-marker').forEach(m => m.remove());
      const seg = +lane.dataset.seg;
      currentAnimation.keyframes.filter(kf => kf.segment === seg).forEach(kf => {
        const m = document.createElement('div');
        m.className = 'keyframe-marker' + (kf.id === selectedKeyframe ? ' selected' : '');
        m.style.left = (kf.timeMs / dur) * 100 + '%';
        m.style.background = kf.color;
        m.title = `${(kf.timeMs / 1000).toFixed(2)}s`;
        m.addEventListener('mousedown', (e) => {
          e.stopPropagation();
          selectedKeyframe = kf.id;
          document.getElementById('kf-color').value = kf.color;
          document.getElementById('kf-interp').value = kf.interpolation;
          updateKfInfo();
          renderTimeline();
          startKfDrag(kf, lane, e);
        });
        lane.appendChild(m);
      });
      drawLaneGradient(lane, seg);
    });

    renderPreviewCanvas();
  }

  function drawLaneGradient(lane, seg) {
    let c = lane.querySelector('canvas');
    if (!c) {
      c = document.createElement('canvas');
      c.style.cssText = 'position:absolute;top:0;left:0;width:100%;height:100%;pointer-events:none;opacity:0.25;';
      lane.style.position = 'relative';
      lane.insertBefore(c, lane.firstChild);
    }
    c.width = lane.clientWidth || 400;
    c.height = 38;
    const ctx = c.getContext('2d');
    ctx.clearRect(0, 0, c.width, c.height);
    const kfs = currentAnimation.keyframes.filter(k => k.segment === seg);
    if (!kfs.length) return;
    for (let px = 0; px < c.width; px++) {
      const f = Animation.evaluate(currentAnimation, (px / c.width) * currentAnimation.durationMs);
      ctx.fillStyle = f.segments[seg].color;
      ctx.fillRect(px, 0, 1, c.height);
    }
  }

  function startKfDrag(kf, lane, startEvt) {
    const rect = lane.getBoundingClientRect();
    const dur = currentAnimation.durationMs;
    function onMove(e) {
      const cx = e.clientX ?? e.touches?.[0]?.clientX;
      if (cx == null) return;
      kf.timeMs = Math.round(Math.max(0, Math.min(1, (cx - rect.left) / rect.width)) * dur);
      renderTimeline();
    }
    function onUp() {
      document.removeEventListener('mousemove', onMove);
      document.removeEventListener('mouseup', onUp);
    }
    document.addEventListener('mousemove', onMove);
    document.addEventListener('mouseup', onUp);
  }

  // Scrubber / playhead drag
  const scrubber = document.getElementById('scrubber-handle');
  let scrubbing = false;

  function scrubToX(clientX) {
    const container = document.getElementById('timeline-container');
    const rect = container.getBoundingClientRect();
    const laneW = rect.width - LABEL_W;
    const x = clientX - rect.left - LABEL_W;
    const ratio = Math.max(0, Math.min(1, x / laneW));
    const t = ratio * currentAnimation.durationMs;
    setPlayheadTime(t);
    // Preview frame
    const frame = Animation.evaluate(currentAnimation, t);
    renderPreviewCanvas(frame);
    // Send to device if live
    if (document.getElementById('anim-live').checked && BLE.getConnected().length > 0) {
      const s = frame.segments[0];
      BLE.setColor('all', s.r, s.g, s.b);
    }
  }

  scrubber.addEventListener('mousedown', (e) => {
    e.preventDefault();
    scrubbing = true;
    if (animPlayer) animPlayer.pause();
  });

  document.getElementById('time-ruler').addEventListener('mousedown', (e) => {
    scrubbing = true;
    if (animPlayer) animPlayer.pause();
    scrubToX(e.clientX);
  });

  document.addEventListener('mousemove', (e) => {
    if (scrubbing) scrubToX(e.clientX);
  });

  document.addEventListener('mouseup', () => { scrubbing = false; });

  function setPlayheadTime(t) {
    const container = document.getElementById('timeline-container');
    const laneW = container.clientWidth - LABEL_W;
    const x = LABEL_W + (t / currentAnimation.durationMs) * laneW;
    document.getElementById('playhead-line').style.left = x + 'px';
    scrubber.style.left = x + 'px';
    document.getElementById('time-display').textContent =
      `${(t / 1000).toFixed(2)}s / ${(currentAnimation.durationMs / 1000).toFixed(2)}s`;
  }

  // Keyframe info display
  function updateKfInfo() {
    const infoText = document.getElementById('kf-info-text');
    const timeInput = document.getElementById('kf-time-input');
    if (!selectedKeyframe) {
      infoText.textContent = 'No keyframe selected';
      timeInput.disabled = true;
      timeInput.value = 0;
      return;
    }
    const kf = currentAnimation.keyframes.find(k => k.id === selectedKeyframe);
    if (!kf) { selectedKeyframe = null; updateKfInfo(); return; }
    infoText.textContent = `T${kf.segment + 1} | ${kf.interpolation}`;
    timeInput.disabled = false;
    timeInput.value = kf.timeMs;
    timeInput.max = currentAnimation.durationMs;
  }

  // Keyframe time input
  document.getElementById('kf-time-input').addEventListener('change', () => {
    const kf = currentAnimation.keyframes.find(k => k.id === selectedKeyframe);
    if (kf) {
      kf.timeMs = Math.max(0, Math.min(currentAnimation.durationMs, +document.getElementById('kf-time-input').value));
      renderTimeline();
    }
  });

  // Add keyframe
  document.getElementById('kf-add').addEventListener('click', () => {
    const seg = +document.getElementById('kf-segment').value;
    const kf = Animation.createKeyframe(
      Math.round(currentAnimation.durationMs / 2), seg,
      document.getElementById('kf-color').value,
      document.getElementById('kf-interp').value
    );
    currentAnimation.keyframes.push(kf);
    selectedKeyframe = kf.id;
    renderTimeline();
    updateKfInfo();
  });

  // Delete keyframe
  document.getElementById('kf-delete').addEventListener('click', () => {
    if (!selectedKeyframe) return;
    currentAnimation.keyframes = currentAnimation.keyframes.filter(k => k.id !== selectedKeyframe);
    selectedKeyframe = null;
    renderTimeline();
    updateKfInfo();
  });

  // Clear all keyframes
  document.getElementById('kf-clear').addEventListener('click', () => {
    if (currentAnimation.keyframes.length === 0) return;
    currentAnimation.keyframes = [];
    selectedKeyframe = null;
    renderTimeline();
    updateKfInfo();
  });

  // Update selected keyframe properties
  document.getElementById('kf-color').addEventListener('input', () => {
    const kf = currentAnimation.keyframes.find(k => k.id === selectedKeyframe);
    if (kf) { kf.color = document.getElementById('kf-color').value; renderTimeline(); }
  });

  document.getElementById('kf-interp').addEventListener('change', () => {
    const kf = currentAnimation.keyframes.find(k => k.id === selectedKeyframe);
    if (kf) { kf.interpolation = document.getElementById('kf-interp').value; renderTimeline(); updateKfInfo(); }
  });

  // Duration
  document.getElementById('anim-duration').addEventListener('change', () => {
    currentAnimation.durationMs = +document.getElementById('anim-duration').value || 5000;
    renderTimeline();
  });

  // Loop mode
  document.getElementById('anim-loop').addEventListener('change', () => {
    currentAnimation.loopMode = document.getElementById('anim-loop').value;
  });

  // Preview canvas (mini tower)
  function renderPreviewCanvas(frame) {
    const canvas = document.getElementById('anim-preview');
    const ctx = canvas.getContext('2d');
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    const f = frame || Animation.evaluate(currentAnimation, 0);
    const sh = canvas.height / SEGMENTS;
    for (let i = 0; i < SEGMENTS; i++) {
      ctx.fillStyle = f.segments[i].color;
      ctx.shadowColor = f.segments[i].color;
      ctx.shadowBlur = 6;
      ctx.fillRect(3, i * sh + 1, canvas.width - 6, sh - 2);
    }
    ctx.shadowBlur = 0;
  }

  // Playback
  document.getElementById('anim-play').addEventListener('click', () => {
    if (animPlayer) animPlayer.stop();
    currentAnimation.loopMode = document.getElementById('anim-loop').value;
    const live = document.getElementById('anim-live').checked;
    animPlayer = Animation.createPlayer(currentAnimation, (frame, t) => {
      renderPreviewCanvas(frame);
      setPlayheadTime(t);
      if (live && BLE.getConnected().length > 0) {
        BLE.setColor('all', frame.segments[0].r, frame.segments[0].g, frame.segments[0].b);
      }
    });
    animPlayer.play();
  });

  document.getElementById('anim-pause').onclick = () => { if (animPlayer) animPlayer.pause(); };
  document.getElementById('anim-stop').onclick = () => {
    if (animPlayer) animPlayer.stop();
    setPlayheadTime(0);
    renderPreviewCanvas();
  };

  // Animation templates
  const TEMPLATES = {
    rainbow: { duration: 4000, keyframes: [
      { t: 0, s: 0, c: '#ff0000' }, { t: 800, s: 0, c: '#ff8800' }, { t: 1600, s: 0, c: '#ffff00' },
      { t: 2400, s: 0, c: '#00ff00' }, { t: 3200, s: 0, c: '#0088ff' }, { t: 4000, s: 0, c: '#ff0000' },
    ]},
    pulse: { duration: 2000, keyframes: [
      { t: 0, s: 0, c: '#ff0044' }, { t: 500, s: 0, c: '#ff0044' },
      { t: 600, s: 0, c: '#ffffff' }, { t: 800, s: 0, c: '#ff0044' },
      { t: 1000, s: 0, c: '#0044ff' }, { t: 1500, s: 0, c: '#0044ff' },
      { t: 1600, s: 0, c: '#ffffff' }, { t: 1800, s: 0, c: '#0044ff' },
      { t: 2000, s: 0, c: '#ff0044' },
    ]},
    fire: { duration: 3000, keyframes: [
      { t: 0, s: 0, c: '#ff2200' }, { t: 500, s: 0, c: '#ff6600' },
      { t: 1000, s: 0, c: '#ffaa00' }, { t: 1500, s: 0, c: '#ff4400' },
      { t: 2000, s: 0, c: '#ff8800' }, { t: 2500, s: 0, c: '#ff2200' },
      { t: 3000, s: 0, c: '#ff2200' },
    ]},
    ocean: { duration: 5000, keyframes: [
      { t: 0, s: 0, c: '#003366' }, { t: 1250, s: 0, c: '#0077aa' },
      { t: 2500, s: 0, c: '#00bbcc' }, { t: 3750, s: 0, c: '#0077aa' },
      { t: 5000, s: 0, c: '#003366' },
    ]},
    strobe: { duration: 1000, keyframes: [
      { t: 0, s: 0, c: '#ffffff', i: 'step' }, { t: 125, s: 0, c: '#000000', i: 'step' },
      { t: 250, s: 0, c: '#ffffff', i: 'step' }, { t: 375, s: 0, c: '#000000', i: 'step' },
      { t: 500, s: 0, c: '#ffffff', i: 'step' }, { t: 625, s: 0, c: '#000000', i: 'step' },
      { t: 750, s: 0, c: '#ffffff', i: 'step' }, { t: 875, s: 0, c: '#000000', i: 'step' },
    ]},
    sunset: { duration: 6000, keyframes: [
      { t: 0, s: 0, c: '#ff4400' }, { t: 1500, s: 0, c: '#ff6644' },
      { t: 3000, s: 0, c: '#cc3366' }, { t: 4500, s: 0, c: '#663399' },
      { t: 6000, s: 0, c: '#1a1a44' },
    ]},
  };

  document.querySelectorAll('.anim-template').forEach(btn => {
    btn.addEventListener('click', () => {
      const tpl = TEMPLATES[btn.dataset.tpl];
      if (!tpl) return;
      currentAnimation = Animation.createAnimation(btn.textContent, tpl.duration);
      currentAnimation.keyframes = tpl.keyframes.map(k =>
        Animation.createKeyframe(k.t, k.s, k.c, k.i || 'smooth')
      );
      document.getElementById('anim-duration').value = tpl.duration;
      selectedKeyframe = null;
      initTimeline();
      updateKfInfo();
    });
  });

  // Save
  document.getElementById('anim-save').addEventListener('click', () => {
    const name = document.getElementById('anim-name').value.trim();
    if (!name) return alert('Enter an animation name.');
    const presets = loadPresets();
    presets.push({ ...currentAnimation, name, savedAt: Date.now() });
    savePresetsStore(presets);
    document.getElementById('anim-name').value = '';
    alert(`Saved "${name}"`);
  });

  // ======================
  // PRESETS TAB
  // ======================
  function renderPresets() {
    const presets = loadPresets();
    const list = document.getElementById('preset-list');
    const none = document.getElementById('no-presets');
    if (!presets.length) { list.innerHTML = ''; none.style.display = 'block'; return; }
    none.style.display = 'none';
    list.innerHTML = '';
    presets.forEach((p, idx) => {
      const card = document.createElement('div');
      card.className = 'preset-card';

      const prev = document.createElement('div');
      prev.className = 'preset-preview';
      const cvs = document.createElement('canvas');
      cvs.width = 200; cvs.height = 32;
      prev.appendChild(cvs);
      const pctx = cvs.getContext('2d');
      for (let px = 0; px < 200; px++) {
        const f = Animation.evaluate(p, (px / 200) * p.durationMs);
        pctx.fillStyle = f.segments[0]?.color || '#000';
        pctx.fillRect(px, 0, 1, 32);
      }
      card.appendChild(prev);

      const h = document.createElement('h3');
      h.textContent = p.name;
      card.appendChild(h);

      const meta = document.createElement('div');
      meta.className = 'preset-meta';
      meta.textContent = `${(p.durationMs / 1000).toFixed(1)}s / ${p.keyframes.length} kf / ${p.loopMode}`;
      card.appendChild(meta);

      const acts = document.createElement('div');
      acts.className = 'preset-actions';

      const loadB = document.createElement('button');
      loadB.className = 'btn btn-small btn-primary';
      loadB.textContent = 'Load';
      loadB.onclick = () => {
        currentAnimation = JSON.parse(JSON.stringify(p));
        document.getElementById('anim-duration').value = currentAnimation.durationMs;
        document.getElementById('anim-loop').value = currentAnimation.loopMode;
        selectedKeyframe = null;
        document.querySelector('.tab[data-tab="animation"]').click();
        initTimeline();
      };

      const playB = document.createElement('button');
      playB.className = 'btn btn-small btn-success';
      playB.textContent = 'Play';
      playB.onclick = () => {
        if (animPlayer) animPlayer.stop();
        const a = JSON.parse(JSON.stringify(p));
        animPlayer = Animation.createPlayer(a, (frame) => {
          if (BLE.getConnected().length > 0) BLE.setColor('all', frame.segments[0].r, frame.segments[0].g, frame.segments[0].b);
        });
        animPlayer.play();
      };

      const delB = document.createElement('button');
      delB.className = 'btn btn-small btn-danger';
      delB.textContent = 'Del';
      delB.onclick = () => {
        if (confirm(`Delete "${p.name}"?`)) {
          presets.splice(idx, 1);
          savePresetsStore(presets);
          renderPresets();
        }
      };

      acts.append(loadB, playB, delB);
      card.appendChild(acts);
      list.appendChild(card);
    });
  }

  // Export presets as JSON file
  document.getElementById('preset-export').addEventListener('click', () => {
    const presets = loadPresets();
    if (!presets.length) return alert('No presets to export.');
    const blob = new Blob([JSON.stringify(presets, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `motherledisa-presets-${new Date().toISOString().slice(0,10)}.json`;
    a.click();
    URL.revokeObjectURL(url);
  });

  // Import presets from JSON file
  document.getElementById('preset-import').addEventListener('change', async (e) => {
    const file = e.target.files[0];
    if (!file) return;
    try {
      const text = await file.text();
      const imported = JSON.parse(text);
      if (!Array.isArray(imported)) throw new Error('Invalid format');
      const existing = loadPresets();
      const merged = [...existing, ...imported];
      savePresetsStore(merged);
      renderPresets();
      alert(`Imported ${imported.length} preset(s).`);
    } catch (err) {
      alert('Import failed: ' + err.message);
    }
    e.target.value = '';
  });

  // ======================
  // DEV / PROTOCOL EXPLORER
  // ======================
  const devLog = document.getElementById('dev-log');
  function dlog(msg, cls = '') {
    const line = document.createElement('div');
    if (cls) line.className = cls;
    line.textContent = `[${new Date().toLocaleTimeString()}] ${msg}`;
    devLog.appendChild(line);
    devLog.scrollTop = devLog.scrollHeight;
  }

  // Intercept BLE console logs for the dev panel
  const origWarn = console.warn;
  const origLog = console.log;
  console.log = (...args) => {
    origLog(...args);
    const msg = args.map(a => typeof a === 'object' ? JSON.stringify(a) : String(a)).join(' ');
    if (msg.includes('[BLE')) dlog(msg, 'log-info');
  };
  console.warn = (...args) => {
    origWarn(...args);
    const msg = args.map(a => typeof a === 'object' ? JSON.stringify(a) : String(a)).join(' ');
    if (msg.includes('[BLE')) dlog(msg, 'log-err');
  };

  // Send raw hex
  document.getElementById('dev-send-raw').addEventListener('click', () => {
    const hex = document.getElementById('dev-raw').value.trim();
    const bytes = hex.split(/\s+/).map(h => parseInt(h, 16));
    if (bytes.length !== 9 || bytes.some(isNaN)) {
      dlog('ERROR: Need exactly 9 hex bytes', 'log-err');
      return;
    }
    dlog(`TX: ${bytes.map(b => b.toString(16).padStart(2, '0')).join(' ')}`);
    BLE.sendRaw('all', bytes);
  });

  // Quick experiments
  document.querySelectorAll('.dev-exp').forEach(btn => {
    btn.addEventListener('click', async () => {
      const exp = btn.dataset.exp;
      const micEff = +document.getElementById('dev-mic-effect').value;
      const param = +document.getElementById('dev-param').value;
      const rgb = Animation.hexToRgb(document.getElementById('dev-color').value);
      const target = 'all';

      dlog(`Running experiment: ${exp}`, 'log-info');

      switch (exp) {
        case 'mic-then-color':
          await BLE.testMicWithColor(target, micEff, param, rgb.r, rgb.g, rgb.b);
          dlog(`Mic effect 0x${micEff.toString(16)} + color rgb(${rgb.r},${rgb.g},${rgb.b})`);
          break;
        case 'color-then-mic':
          await BLE.testColorThenMic(target, rgb.r, rgb.g, rgb.b, micEff, param);
          dlog(`Color rgb(${rgb.r},${rgb.g},${rgb.b}) then mic effect 0x${micEff.toString(16)}`);
          break;
        case 'mic-with-effect':
          await BLE.testMicWithEffect(target, micEff, 0x89, param, 70);
          dlog(`Mic 0x${micEff.toString(16)} + HW effect 0x89 (Crossfade RGB)`);
          break;
        case 'mic-brightness':
          await BLE.testMicWithBrightness(target, micEff, 70, param);
          dlog(`Mic 0x${micEff.toString(16)} + brightness ${param}`);
          break;
        case 'mic-speed':
          await BLE.testMicWithSpeed(target, micEff, 70, param);
          dlog(`Mic 0x${micEff.toString(16)} + speed ${param}`);
          break;
        case 'cmd-0d':
          await BLE.testStreamCmd(target, 0x0D, param);
          dlog(`Cmd 0x0D (streaming mic sensitivity?) param=${param}`);
          break;
        case 'cmd-0e':
          await BLE.testStreamCmd(target, 0x0E, param);
          dlog(`Cmd 0x0E (toggle streaming?) param=${param}`);
          break;
        case 'cmd-0f':
          await BLE.testStreamCmd(target, 0x0F, param);
          dlog(`Cmd 0x0F (external mic EQ?) param=${param}`);
          break;
        case 'symphony':
          const sp = +document.getElementById('dev-symphony').value;
          await BLE.testSymphony(target, sp);
          dlog(`Symphony point=${sp}`);
          break;
        case 'scene':
          await BLE.testScene(target, param);
          dlog(`Scene id=${param}`);
          break;
        case 'mic-symphony':
          const sp2 = +document.getElementById('dev-symphony').value;
          await BLE.testMicWithSymphony(target, micEff, param, sp2);
          dlog(`Mic 0x${micEff.toString(16)} + Symphony point=${sp2}`);
          break;
      }
    });
  });

  // Scene buttons
  document.querySelectorAll('.dev-scene').forEach(btn => {
    btn.addEventListener('click', async () => {
      const id = +btn.dataset.id;
      dlog(`Scene ${id}: ${btn.textContent}`, 'log-info');
      await BLE.testScene('all', id);
    });
  });

  // Symphony slider live update
  const symSlider = document.getElementById('dev-symphony');
  const symVal = document.getElementById('dev-symphony-val');
  symSlider.addEventListener('input', () => symVal.textContent = symSlider.value);

  document.getElementById('dev-clear-log').onclick = () => devLog.innerHTML = '';

  // --- Init ---
  updateColorUI();
  initTimeline();
  window.addEventListener('resize', () => { if (currentTab === 'animation') renderTimeline(); });

  // BLE support check
  const supportDiv = document.getElementById('ble-support-status');
  const checks = [
    { label: 'Web Bluetooth API', ok: !!navigator.bluetooth },
    { label: 'Secure Context (HTTPS/localhost)', ok: window.isSecureContext },
  ];
  if (navigator.bluetooth?.getAvailability) {
    navigator.bluetooth.getAvailability().then(avail => {
      checks.push({ label: 'Bluetooth Available', ok: avail });
      renderChecks();
    });
  } else {
    renderChecks();
  }
  function renderChecks() {
    supportDiv.innerHTML = checks.map(c =>
      `<div class="ble-check ${c.ok ? 'ble-check-ok' : 'ble-check-fail'}">${c.ok ? '&#10003;' : '&#10007;'} ${c.label}</div>`
    ).join('');
  }
})();
