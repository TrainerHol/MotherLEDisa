// MotherLEDisa — Animation Engine

const Animation = (() => {
  // Keyframe: { timeMs, segment, color: '#rrggbb', interpolation: 'smooth'|'step' }
  // Animation: { name, durationMs, keyframes: [], loopMode: 'once'|'infinite'|'pingpong' }

  function createKeyframe(timeMs, segment, color = '#ff0000', interpolation = 'smooth') {
    return { timeMs, segment, color, interpolation, id: crypto.randomUUID() };
  }

  function createAnimation(name = 'Untitled', durationMs = 5000) {
    return {
      id: crypto.randomUUID(),
      name,
      durationMs,
      keyframes: [],
      loopMode: 'infinite',
    };
  }

  // Parse hex color to {r, g, b}
  function hexToRgb(hex) {
    const h = hex.replace('#', '');
    return {
      r: parseInt(h.substring(0, 2), 16),
      g: parseInt(h.substring(2, 4), 16),
      b: parseInt(h.substring(4, 6), 16),
    };
  }

  function rgbToHex(r, g, b) {
    return '#' + [r, g, b].map(c => Math.round(c).toString(16).padStart(2, '0')).join('');
  }

  // HSL conversion for smooth interpolation
  function rgbToHsl(r, g, b) {
    r /= 255; g /= 255; b /= 255;
    const max = Math.max(r, g, b), min = Math.min(r, g, b);
    let h, s, l = (max + min) / 2;
    if (max === min) {
      h = s = 0;
    } else {
      const d = max - min;
      s = l > 0.5 ? d / (2 - max - min) : d / (max + min);
      switch (max) {
        case r: h = ((g - b) / d + (g < b ? 6 : 0)) / 6; break;
        case g: h = ((b - r) / d + 2) / 6; break;
        case b: h = ((r - g) / d + 4) / 6; break;
      }
    }
    return { h, s, l };
  }

  function hslToRgb(h, s, l) {
    let r, g, b;
    if (s === 0) {
      r = g = b = l;
    } else {
      const hue2rgb = (p, q, t) => {
        if (t < 0) t += 1;
        if (t > 1) t -= 1;
        if (t < 1/6) return p + (q - p) * 6 * t;
        if (t < 1/2) return q;
        if (t < 2/3) return p + (q - p) * (2/3 - t) * 6;
        return p;
      };
      const q = l < 0.5 ? l * (1 + s) : l + s - l * s;
      const p = 2 * l - q;
      r = hue2rgb(p, q, h + 1/3);
      g = hue2rgb(p, q, h);
      b = hue2rgb(p, q, h - 1/3);
    }
    return { r: Math.round(r * 255), g: Math.round(g * 255), b: Math.round(b * 255) };
  }

  // Interpolate between two colors in HSL space (shortest hue path)
  function interpolateColor(hex1, hex2, t) {
    const rgb1 = hexToRgb(hex1);
    const rgb2 = hexToRgb(hex2);
    const hsl1 = rgbToHsl(rgb1.r, rgb1.g, rgb1.b);
    const hsl2 = rgbToHsl(rgb2.r, rgb2.g, rgb2.b);

    // Shortest hue path
    let dh = hsl2.h - hsl1.h;
    if (dh > 0.5) dh -= 1;
    if (dh < -0.5) dh += 1;

    const h = (hsl1.h + dh * t + 1) % 1;
    const s = hsl1.s + (hsl2.s - hsl1.s) * t;
    const l = hsl1.l + (hsl2.l - hsl1.l) * t;

    const rgb = hslToRgb(h, s, l);
    return rgbToHex(rgb.r, rgb.g, rgb.b);
  }

  // Evaluate animation at a given time, returns { segments: [{color, r, g, b}] }
  function evaluate(animation, timeMs) {
    const segments = [];
    for (let seg = 0; seg < 5; seg++) {
      const kfs = animation.keyframes
        .filter(kf => kf.segment === seg)
        .sort((a, b) => a.timeMs - b.timeMs);

      if (kfs.length === 0) {
        segments.push({ color: '#000000', r: 0, g: 0, b: 0 });
        continue;
      }

      // Find surrounding keyframes
      let before = null, after = null;
      for (let i = 0; i < kfs.length; i++) {
        if (kfs[i].timeMs <= timeMs) before = kfs[i];
        if (kfs[i].timeMs >= timeMs && !after) after = kfs[i];
      }

      if (!before && after) {
        const rgb = hexToRgb(after.color);
        segments.push({ color: after.color, ...rgb });
      } else if (before && !after) {
        const rgb = hexToRgb(before.color);
        segments.push({ color: before.color, ...rgb });
      } else if (before && after && before.id === after.id) {
        const rgb = hexToRgb(before.color);
        segments.push({ color: before.color, ...rgb });
      } else if (before && after) {
        if (before.interpolation === 'step') {
          const rgb = hexToRgb(before.color);
          segments.push({ color: before.color, ...rgb });
        } else {
          const t = (timeMs - before.timeMs) / (after.timeMs - before.timeMs);
          const color = interpolateColor(before.color, after.color, t);
          const rgb = hexToRgb(color);
          segments.push({ color, ...rgb });
        }
      } else {
        segments.push({ color: '#000000', r: 0, g: 0, b: 0 });
      }
    }
    return { segments };
  }

  // Playback controller
  function createPlayer(animation, onFrame, onComplete) {
    let running = false;
    let paused = false;
    let startTime = 0;
    let pausedAt = 0;
    let direction = 1; // 1 forward, -1 backward (pingpong)
    let rafId = null;

    function tick() {
      if (!running || paused) return;
      const elapsed = Date.now() - startTime;
      let t = elapsed % animation.durationMs;

      if (animation.loopMode === 'once' && elapsed >= animation.durationMs) {
        const frame = evaluate(animation, animation.durationMs);
        onFrame(frame, animation.durationMs);
        stop();
        if (onComplete) onComplete();
        return;
      }

      if (animation.loopMode === 'pingpong') {
        const cycle = Math.floor(elapsed / animation.durationMs);
        if (cycle % 2 === 1) {
          t = animation.durationMs - t;
        }
      }

      const frame = evaluate(animation, t);
      onFrame(frame, t);
      rafId = requestAnimationFrame(tick);
    }

    function play() {
      if (running && paused) {
        // Resume
        startTime = Date.now() - pausedAt;
        paused = false;
        rafId = requestAnimationFrame(tick);
        return;
      }
      running = true;
      paused = false;
      startTime = Date.now();
      rafId = requestAnimationFrame(tick);
    }

    function pause() {
      if (running) {
        paused = true;
        pausedAt = Date.now() - startTime;
        if (rafId) cancelAnimationFrame(rafId);
      }
    }

    function stop() {
      running = false;
      paused = false;
      if (rafId) cancelAnimationFrame(rafId);
    }

    function isPlaying() { return running && !paused; }

    return { play, pause, stop, isPlaying };
  }

  return {
    createKeyframe, createAnimation, evaluate, createPlayer,
    hexToRgb, rgbToHex, interpolateColor,
  };
})();
