// Take a break — expanded library + Surprise me
function BreakScreen({ state, setState, onBack }) {
  const [chosen, setChosen] = React.useState(null);
  const [seconds, setSeconds] = React.useState(60);
  const [running, setRunning] = React.useState(false);
  const [filter, setFilter] = React.useState('all'); // 'all' | 'tiny' | 'sensory' | 'body' | 'world'
  const [pulse, setPulse] = React.useState(0); // rerender for surprise-me shuffle

  const breaks = [
    // Tiny (≤30s)
    { id: 'look-far',   kind: 'tiny',    title: 'Look far',              subtitle: '20 seconds, 20 feet away', duration: 20,  resource: 'sensory',
      prompt: 'Rest your eyes on the furthest thing you can see. A tree, a wall, a patch of sky.', color: 'oklch(0.78 0.08 215)' },
    { id: 'sun',        kind: 'world',   title: 'Go look at the sun',    subtitle: '30 seconds of warm light', duration: 30,  resource: 'sensory',
      prompt: 'Step to a window or outside. Close your eyes and let the light land on your face.', color: 'oklch(0.9 0.08 80)' },
    { id: 'sky',        kind: 'world',   title: 'Find a piece of sky',   subtitle: '30 seconds, no phone',      duration: 30,  resource: 'spiritual',
      prompt: 'Look up. Notice the color. Notice what is moving across it.', color: 'oklch(0.82 0.07 230)' },
    { id: 'water-sip',  kind: 'tiny',    title: 'Slow sip of water',     subtitle: '30 seconds, one glass',     duration: 30,  resource: 'nutritional',
      prompt: 'One glass, slower than you want to. Feel the temperature. Notice the first swallow.', color: 'oklch(0.84 0.06 200)' },
    { id: 'three-breaths', kind: 'tiny', title: 'Three long breaths',   subtitle: '30 seconds',                duration: 30,  resource: 'physical',
      prompt: 'In through your nose. Out through your mouth. Three times. That\u2019s it.', color: 'oklch(0.82 0.07 175)' },

    // Sensory (60s)
    { id: 'senses',     kind: 'sensory', title: 'Five senses',           subtitle: '60 seconds, one of each',   duration: 60,  resource: 'sensory',
      prompt: 'Name one thing you can see. Hear. Feel. Smell. Taste.', color: 'oklch(0.86 0.05 75)' },
    { id: 'flower',     kind: 'world',   title: 'Look for a flower',     subtitle: '60 seconds, a small hunt',  duration: 60,  resource: 'sensory',
      prompt: 'Step away and find one flower, anywhere. On a wall, a table, outside. Really look at it.', color: 'oklch(0.84 0.08 350)' },
    { id: 'cat',        kind: 'world',   title: 'Go pet your cat',       subtitle: '60 seconds of softness',    duration: 60,  resource: 'interactional',
      prompt: 'Pet your cat. Or dog. Or plant. Or a soft blanket. Something living or warm, slowly.', color: 'oklch(0.85 0.07 40)' },
    { id: 'cold-water', kind: 'sensory', title: 'Cold water, wrists',    subtitle: '60 seconds at the sink',    duration: 60,  resource: 'physical',
      prompt: 'Cool water over your wrists, then your face if you want. Notice the shift.', color: 'oklch(0.82 0.07 210)' },
    { id: 'smell',      kind: 'sensory', title: 'Find a good smell',     subtitle: '60 seconds',                duration: 60,  resource: 'sensory',
      prompt: 'Coffee, a candle, soap, a plant, the outside air. Breathe it in twice, slowly.', color: 'oklch(0.83 0.08 30)' },
    { id: 'song',       kind: 'sensory', title: 'One song, eyes closed', subtitle: 'About 3 minutes',          duration: 180, resource: 'emotional',
      prompt: 'Pick a song you love. Play it. Close your eyes. That\u2019s the whole break.', color: 'oklch(0.82 0.08 300)' },

    // Body (60\u201390s)
    { id: 'stretch',    kind: 'body',    title: 'Soft stretch',          subtitle: '90 seconds, slow',          duration: 90,  resource: 'physical',
      prompt: 'Roll your shoulders. Open your chest. Look up, then slowly side to side.', color: 'oklch(0.82 0.07 175)' },
    { id: 'shake',      kind: 'body',    title: 'Shake it off',          subtitle: '30 seconds, silly allowed', duration: 30,  resource: 'physical',
      prompt: 'Stand up. Shake your hands, arms, legs. Loosely. Make a noise if you want.', color: 'oklch(0.85 0.08 55)' },
    { id: 'walk',       kind: 'body',    title: 'A small walk',          subtitle: '2 minutes, no phone',       duration: 120, resource: 'physical',
      prompt: 'Anywhere. Kitchen, hallway, outside. Phone stays here.', color: 'oklch(0.8 0.07 150)' },
    { id: 'posture',    kind: 'body',    title: 'Tall spine',            subtitle: '30 seconds',                duration: 30,  resource: 'physical',
      prompt: 'Feet flat. Crown of head lifted. Shoulders down the back. Three breaths here.', color: 'oklch(0.82 0.06 180)' },

    // World (connection / environment)
    { id: 'text',       kind: 'world',   title: 'Text someone kind',     subtitle: '60 seconds, one message',   duration: 60,  resource: 'interactional',
      prompt: 'Send one sentence to someone you love. No reason needed. "thinking of you" is enough.', color: 'oklch(0.85 0.08 20)' },
    { id: 'tidy',       kind: 'world',   title: 'Tidy one small thing',  subtitle: '60 seconds, one surface',   duration: 60,  resource: 'contextual',
      prompt: 'Pick one surface. Clear just that. Leave the rest.', color: 'oklch(0.82 0.06 100)' },
    { id: 'window',     kind: 'world',   title: 'Open a window',         subtitle: '60 seconds of fresh air',   duration: 60,  resource: 'contextual',
      prompt: 'Crack a window. Stand near it. Let the outside in for a minute.', color: 'oklch(0.84 0.07 195)' },
    { id: 'nothing',    kind: 'tiny',    title: 'Do absolutely nothing', subtitle: '60 seconds of sky mind',    duration: 60,  resource: 'spiritual',
      prompt: 'No task. No plan. No phone. Just sit. Notice what arrives.', color: 'oklch(0.78 0.06 260)' },
  ];

  const filters = [
    { k: 'all',     l: 'All',      c: breaks.length },
    { k: 'tiny',    l: 'Tiny',     c: breaks.filter(b => b.kind === 'tiny').length },
    { k: 'sensory', l: 'Sensory',  c: breaks.filter(b => b.kind === 'sensory').length },
    { k: 'body',    l: 'Body',     c: breaks.filter(b => b.kind === 'body').length },
    { k: 'world',   l: 'World',    c: breaks.filter(b => b.kind === 'world').length },
  ];
  const visible = filter === 'all' ? breaks : breaks.filter(b => b.kind === filter);

  React.useEffect(() => {
    if (!running) return;
    if (seconds <= 0) { setRunning(false); return; }
    const t = setTimeout(() => setSeconds(s => s - 1), 1000);
    return () => clearTimeout(t);
  }, [running, seconds]);

  const start = (b) => { setChosen(b); setSeconds(b.duration); setRunning(true); };

  const surprise = () => {
    const pool = visible.length ? visible : breaks;
    const pick = pool[Math.floor(Math.random() * pool.length)];
    setPulse(p => p + 1);
    // brief reveal, then start
    setTimeout(() => start(pick), 450);
  };

  const finish = () => {
    setState(s => {
      const r = chosen?.resource || 'sensory';
      return {
        ...s,
        resources: {
          ...s.resources,
          [r]: { ...s.resources[r], pm: Math.min(1, s.resources[r].pm + 0.22) },
        },
        stones: [...s.stones, { kind: 'jade', label: 'break', when: Date.now() }],
      };
    });
    onBack();
  };

  if (chosen) {
    const progress = 1 - seconds / chosen.duration;
    return (
      <div className="fade-in" style={{ padding: '16px 20px 120px', minHeight: '100%', display: 'flex', flexDirection: 'column' }}>
        <ScreenHeader eyebrow="Taking a break" title={chosen.title} onBack={() => { setRunning(false); setChosen(null); }} />

        <div style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}>
          <ProgressRing size={240} value={progress} stroke={2} color={chosen.color} trackColor="rgba(240,248,255,0.08)">
            <div style={{ textAlign: 'center' }}>
              <div className="serif" style={{ fontSize: 56, color: 'oklch(0.96 0.015 220)', lineHeight: 1 }}>
                {seconds}
              </div>
              <div className="mono" style={{ fontSize: 10, color: 'rgba(240,248,255,0.5)', marginTop: 4, letterSpacing: 1 }}>
                seconds
              </div>
            </div>
          </ProgressRing>

          <div className="serif" style={{
            fontSize: 20, textAlign: 'center', lineHeight: 1.35,
            color: 'oklch(0.96 0.015 220)', maxWidth: 280, margin: '32px auto 0',
          }}>
            "{chosen.prompt}"
          </div>
        </div>

        {seconds <= 0 ? (
          <PrimaryBtn variant="sand" onClick={finish}>Return  \u25e6</PrimaryBtn>
        ) : (
          <PrimaryBtn variant="ghost" onClick={() => { setRunning(false); setChosen(null); }}>
            I'm back
          </PrimaryBtn>
        )}
      </div>
    );
  }

  return (
    <div className="fade-in" style={{ padding: '16px 20px 120px' }}>
      <ScreenHeader eyebrow="60 seconds will do" title="Take a break" onBack={onBack} />

      <div className="ui" style={{
        fontSize: 14, lineHeight: 1.5, color: 'rgba(240,248,255,0.55)',
        margin: '4px 0 16px',
      }}>
        Small resets beat heroic ones. Pick one, or let the app pick.
      </div>

      {/* Surprise me hero */}
      <button onClick={surprise} className="ui" key={pulse}
        style={{
          width: '100%', padding: '18px 18px', borderRadius: 22, cursor: 'pointer',
          background: 'linear-gradient(135deg, oklch(0.88 0.07 85 / 0.22), oklch(0.78 0.08 215 / 0.2))',
          border: '1px solid oklch(0.88 0.07 85 / 0.4)',
          color: 'oklch(0.96 0.015 220)', textAlign: 'left',
          display: 'flex', alignItems: 'center', gap: 14, marginBottom: 18,
          position: 'relative', overflow: 'hidden',
        }}>
        <div style={{
          width: 52, height: 52, borderRadius: '50%',
          background: 'conic-gradient(from 0deg, oklch(0.88 0.07 85), oklch(0.78 0.08 215), oklch(0.84 0.08 350), oklch(0.88 0.07 85))',
          flexShrink: 0, position: 'relative',
          boxShadow: '0 0 24px oklch(0.88 0.07 85 / 0.4)',
          animation: 'spin 8s linear infinite',
        }}>
          <div style={{
            position: 'absolute', inset: 6, borderRadius: '50%',
            background: 'oklch(0.22 0.04 250)',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            fontSize: 20,
          }}>\u2728</div>
        </div>
        <div style={{ flex: 1 }}>
          <div className="serif" style={{ fontSize: 22, lineHeight: 1.1 }}>
            Surprise me
          </div>
          <div className="ui" style={{ fontSize: 12, color: 'rgba(240,248,255,0.6)', marginTop: 4 }}>
            a random break from {filter === 'all' ? 'any kind' : filter}
          </div>
        </div>
        <span style={{ color: 'oklch(0.92 0.08 85)', fontSize: 18 }}>\u2192</span>
      </button>

      {/* Filter chips */}
      <div style={{ display: 'flex', gap: 6, marginBottom: 14, overflowX: 'auto', paddingBottom: 2 }}>
        {filters.map(f => (
          <button key={f.k} onClick={() => setFilter(f.k)} className="ui"
            style={{
              padding: '7px 14px', borderRadius: 100, cursor: 'pointer', flexShrink: 0,
              background: filter === f.k ? 'oklch(0.78 0.08 215 / 0.25)' : 'rgba(240,248,255,0.04)',
              border: '1px solid ' + (filter === f.k ? 'oklch(0.78 0.08 215 / 0.5)' : 'rgba(240,248,255,0.1)'),
              color: filter === f.k ? 'oklch(0.95 0.04 215)' : 'rgba(240,248,255,0.65)',
              fontSize: 12, fontWeight: 600, letterSpacing: 0.3,
              display: 'flex', alignItems: 'center', gap: 6,
            }}>
            {f.l}
            <span className="mono" style={{ fontSize: 9, opacity: 0.7 }}>{f.c}</span>
          </button>
        ))}
      </div>

      {/* List */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
        {visible.map(b => (
          <button key={b.id} onClick={() => start(b)} className="ui"
            style={{
              padding: 18, borderRadius: 20, textAlign: 'left', cursor: 'pointer',
              background: 'rgba(240,248,255,0.04)',
              border: '1px solid rgba(240,248,255,0.1)',
              color: 'oklch(0.92 0.03 205)',
              display: 'flex', alignItems: 'center', gap: 14,
            }}>
            <div style={{
              width: 40, height: 40, borderRadius: '50%',
              background: `radial-gradient(circle at 30% 30%, ${b.color}, oklch(0.3 0.06 230))`,
              flexShrink: 0,
              boxShadow: `0 0 14px ${b.color}30`,
            }} />
            <div style={{ flex: 1, minWidth: 0 }}>
              <div className="serif" style={{ fontSize: 18, color: 'oklch(0.96 0.015 220)', marginBottom: 2 }}>
                {b.title}
              </div>
              <div style={{ fontSize: 11, color: 'rgba(240,248,255,0.5)' }}>
                {b.subtitle}
              </div>
            </div>
            <span style={{ color: b.color, fontSize: 16, opacity: 0.7 }}>\u2192</span>
          </button>
        ))}
      </div>
    </div>
  );
}

// ensure spin keyframes exist
if (typeof document !== 'undefined' && !document.getElementById('break-kf')) {
  const s = document.createElement('style'); s.id = 'break-kf';
  s.textContent = '@keyframes spin { to { transform: rotate(360deg); } }';
  document.head.appendChild(s);
}

Object.assign(window, { BreakScreen });
