// Ambient ocean pad — Web Audio, no external asset.
// Plays a slow-breathing sine pad with chorus + filtered noise waves.

function makeAmbient() {
  let ctx, masterGain, oscNodes = [], noiseNode, lfo1, lfo2, started = false, waveTimer = null;

  const start = () => {
    if (started) return;
    ctx = new (window.AudioContext || window.webkitAudioContext)();
    masterGain = ctx.createGain();
    masterGain.gain.value = 0;
    masterGain.connect(ctx.destination);

    // Pad: three detuned sines on a gentle chord (D minor 7-ish: D, F, A, C)
    const chord = [146.83, 174.61, 220.00, 261.63]; // soft low ocean pad
    const padGain = ctx.createGain();
    padGain.gain.value = 0.15;
    const padFilter = ctx.createBiquadFilter();
    padFilter.type = 'lowpass';
    padFilter.frequency.value = 900;
    padFilter.Q.value = 0.6;
    padGain.connect(padFilter);
    padFilter.connect(masterGain);

    chord.forEach((f, i) => {
      const osc = ctx.createOscillator();
      osc.type = 'sine';
      osc.frequency.value = f;
      const detune = ctx.createOscillator();
      detune.type = 'sine';
      detune.frequency.value = 0.08 + i * 0.03;
      const detuneGain = ctx.createGain();
      detuneGain.gain.value = 6 + i * 2;
      detune.connect(detuneGain);
      detuneGain.connect(osc.detune);
      detune.start();
      const g = ctx.createGain();
      g.gain.value = 0.25 / chord.length;
      osc.connect(g);
      g.connect(padGain);
      osc.start();
      oscNodes.push(osc, detune);
    });

    // Slow LFO on filter — breathing motion
    lfo1 = ctx.createOscillator();
    lfo1.type = 'sine';
    lfo1.frequency.value = 0.06; // ~16s period
    const lfoGain = ctx.createGain();
    lfoGain.gain.value = 300;
    lfo1.connect(lfoGain);
    lfoGain.connect(padFilter.frequency);
    lfo1.start();

    // Soft pink-ish noise = ocean
    const bufSize = ctx.sampleRate * 3;
    const buf = ctx.createBuffer(1, bufSize, ctx.sampleRate);
    const data = buf.getChannelData(0);
    let b0=0,b1=0,b2=0,b3=0,b4=0,b5=0,b6=0;
    for (let i = 0; i < bufSize; i++) {
      const w = Math.random() * 2 - 1;
      b0 = 0.99886*b0+w*0.0555179; b1=0.99332*b1+w*0.0750759;
      b2 = 0.96900*b2+w*0.1538520; b3=0.86650*b3+w*0.3104856;
      b4 = 0.55000*b4+w*0.5329522; b5=-0.7616*b5-w*0.0168980;
      data[i] = (b0+b1+b2+b3+b4+b5+b6+w*0.5362)*0.08;
      b6 = w*0.115926;
    }
    noiseNode = ctx.createBufferSource();
    noiseNode.buffer = buf;
    noiseNode.loop = true;
    const noiseFilter = ctx.createBiquadFilter();
    noiseFilter.type = 'lowpass';
    noiseFilter.frequency.value = 650;
    const noiseGain = ctx.createGain();
    noiseGain.gain.value = 0.55;
    noiseNode.connect(noiseFilter);
    noiseFilter.connect(noiseGain);
    noiseGain.connect(masterGain);

    // LFO for noise amplitude — wave motion
    lfo2 = ctx.createOscillator();
    lfo2.type = 'sine';
    lfo2.frequency.value = 0.11;
    const lfo2Gain = ctx.createGain();
    lfo2Gain.gain.value = 0.35;
    lfo2.connect(lfo2Gain);
    lfo2Gain.connect(noiseGain.gain);
    lfo2.start();
    noiseNode.start();

    // occasional chime
    waveTimer = setInterval(() => {
      if (!ctx || ctx.state !== 'running') return;
      const chime = ctx.createOscillator();
      chime.type = 'sine';
      const notes = [523.25, 587.33, 659.25, 783.99, 880, 1046.5]; // C5..C6
      chime.frequency.value = notes[Math.floor(Math.random() * notes.length)];
      const cg = ctx.createGain();
      cg.gain.setValueAtTime(0, ctx.currentTime);
      cg.gain.linearRampToValueAtTime(0.06, ctx.currentTime + 0.4);
      cg.gain.exponentialRampToValueAtTime(0.0001, ctx.currentTime + 5);
      chime.connect(cg);
      cg.connect(masterGain);
      chime.start();
      chime.stop(ctx.currentTime + 5.2);
    }, 9000);

    // fade in
    masterGain.gain.linearRampToValueAtTime(0.55, ctx.currentTime + 2.5);
    started = true;
  };

  const stop = () => {
    if (!started) return;
    try {
      masterGain.gain.cancelScheduledValues(ctx.currentTime);
      masterGain.gain.linearRampToValueAtTime(0, ctx.currentTime + 1.2);
      setTimeout(() => {
        try { oscNodes.forEach(o => o.stop()); } catch(e) {}
        try { noiseNode.stop(); } catch(e) {}
        try { lfo1.stop(); lfo2.stop(); } catch(e) {}
        try { ctx.close(); } catch(e) {}
        started = false;
        oscNodes = [];
      }, 1400);
      if (waveTimer) clearInterval(waveTimer);
    } catch(e) { started = false; }
  };

  const setVolume = (v) => {
    if (!started || !masterGain || !ctx) return;
    masterGain.gain.cancelScheduledValues(ctx.currentTime);
    masterGain.gain.linearRampToValueAtTime(Math.max(0, Math.min(1, v)), ctx.currentTime + 0.4);
  };

  return { start, stop, setVolume, isStarted: () => started };
}

// Floating music button
function MusicButton({ playing, onToggle }) {
  return (
    <button onClick={onToggle}
      className="ui"
      style={{
        position: 'absolute', top: 12, right: 12, zIndex: 20,
        width: 44, height: 44, borderRadius: '50%',
        background: playing
          ? 'radial-gradient(circle at 30% 30%, oklch(0.88 0.07 85), oklch(0.7 0.1 70))'
          : 'rgba(240,248,255,0.08)',
        border: '1px solid ' + (playing ? 'oklch(0.88 0.07 85 / 0.6)' : 'rgba(240,248,255,0.2)'),
        boxShadow: playing ? '0 0 20px oklch(0.88 0.07 85 / 0.4)' : 'none',
        color: playing ? 'oklch(0.28 0.05 70)' : 'oklch(0.92 0.03 205)',
        cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center',
        transition: 'all 0.3s',
      }}>
      {playing ? (
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round">
          <path d="M9 9 Q 12 6 15 9" />
          <path d="M7 13 Q 12 9 17 13" opacity="0.7"/>
          <path d="M5 17 Q 12 12 19 17" opacity="0.5"/>
        </svg>
      ) : (
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.4" strokeLinecap="round">
          <path d="M9 18V5l12-2v13" />
          <circle cx="6" cy="18" r="3" />
          <circle cx="18" cy="16" r="3" />
          <line x1="3" y1="3" x2="21" y2="21" strokeWidth="1.2" opacity="0.6" />
        </svg>
      )}
    </button>
  );
}

Object.assign(window, { makeAmbient, MusicButton });
