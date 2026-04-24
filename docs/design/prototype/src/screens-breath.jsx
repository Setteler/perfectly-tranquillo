// Breathwork — box breathing 4-4-4-4 with animated ring
function BreathScreen({ state, setState, onBack }) {
  const [running, setRunning] = React.useState(false);
  const [phase, setPhase] = React.useState(0); // 0 in, 1 hold, 2 out, 3 hold
  const [count, setCount] = React.useState(4);
  const [cycles, setCycles] = React.useState(0);
  const phases = ['Breathe in', 'Hold', 'Breathe out', 'Hold'];
  const phaseColors = ['oklch(0.78 0.08 215)', 'oklch(0.88 0.06 215)', 'oklch(0.62 0.09 220)', 'oklch(0.88 0.06 215)'];

  React.useEffect(() => {
    if (!running) return;
    const t = setInterval(() => {
      setCount(c => {
        if (c > 1) return c - 1;
        setPhase(p => {
          const np = (p + 1) % 4;
          if (np === 0) setCycles(x => x + 1);
          return np;
        });
        return 4;
      });
    }, 1000);
    return () => clearInterval(t);
  }, [running]);

  const finish = () => {
    setRunning(false);
    setState(s => ({
      ...s,
      resources: {
        ...s.resources,
        physical: { ...s.resources.physical, pm: Math.min(1, s.resources.physical.pm + 0.2) },
        emotional: { ...s.resources.emotional, pm: Math.min(1, s.resources.emotional.pm + 0.15) },
      },
      stones: [...s.stones, { kind: 'moon', label: 'breath', when: Date.now() }],
    }));
    onBack();
  };

  // breath ring scale by phase
  const scale = phase === 0 ? 1.25 : phase === 1 ? 1.25 : phase === 2 ? 0.8 : 0.8;

  return (
    <div className="fade-in" style={{ padding: '16px 20px 120px', minHeight: '100%', display: 'flex', flexDirection: 'column' }}>
      <ScreenHeader eyebrow="Box breathing · 4·4·4·4" title="Breath" onBack={onBack} />

      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', padding: '20px 0' }}>
        {/* Ring */}
        <div style={{ position: 'relative', width: 260, height: 260, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          {/* outer decorative */}
          <svg width="260" height="260" style={{ position: 'absolute', inset: 0 }}>
            <circle cx="130" cy="130" r="120" fill="none"
              stroke="oklch(0.78 0.08 215 / 0.15)" strokeWidth="1" strokeDasharray="2 4" />
          </svg>
          {/* breathing orb */}
          <div style={{
            width: 160, height: 160, borderRadius: '50%',
            background: `radial-gradient(circle at 35% 35%, oklch(0.85 0.08 215), ${phaseColors[phase]})`,
            boxShadow: `0 0 80px ${phaseColors[phase]}80, inset -6px -6px 12px rgba(0,0,0,0.2), inset 4px 4px 8px rgba(255,255,255,0.25)`,
            transform: `scale(${running ? scale : 1})`,
            transition: 'transform 4s ease-in-out, box-shadow 4s ease',
          }} />
          {/* count in center */}
          <div style={{ position: 'absolute', textAlign: 'center' }}>
            <div className="serif" style={{ fontSize: 56, color: 'oklch(0.12 0.03 250)', lineHeight: 1 }}>
              {running ? count : '—'}
            </div>
          </div>
        </div>

        <div className="serif" style={{
          fontSize: 24, color: 'oklch(0.96 0.015 220)', marginTop: 36,
          opacity: running ? 1 : 0.5,
        }}>
          {running ? phases[phase] : 'Ready when you are'}
        </div>
        <div className="mono" style={{ fontSize: 11, color: 'rgba(240,248,255,0.4)', marginTop: 8, letterSpacing: 0.5 }}>
          cycle {cycles} · 4 seconds each
        </div>
      </div>

      <div style={{ display: 'flex', gap: 10 }}>
        {!running ? (
          <PrimaryBtn onClick={() => { setRunning(true); setPhase(0); setCount(4); }}>
            Begin  ◦
          </PrimaryBtn>
        ) : (
          <>
            <PrimaryBtn variant="ghost" onClick={() => setRunning(false)}>Pause</PrimaryBtn>
            <PrimaryBtn variant="sand" onClick={finish}>
              {cycles > 0 ? `Finish (${cycles})` : 'End'}
            </PrimaryBtn>
          </>
        )}
      </div>
    </div>
  );
}
Object.assign(window, { BreathScreen });
