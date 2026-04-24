// Focus session — pick duration, mandala fills as you focus
function FocusScreen({ state, setState, onBack }) {
  const [minutes, setMinutes] = React.useState(25);
  const [running, setRunning] = React.useState(false);
  const [remaining, setRemaining] = React.useState(25 * 60);
  const [doneOnce, setDoneOnce] = React.useState(false);

  React.useEffect(() => { if (!running) setRemaining(minutes * 60); }, [minutes, running]);

  React.useEffect(() => {
    if (!running) return;
    if (remaining <= 0) { setRunning(false); setDoneOnce(true); return; }
    const t = setInterval(() => setRemaining(r => r - 1), 1000);
    return () => clearInterval(t);
  }, [running, remaining]);

  const progress = 1 - remaining / (minutes * 60);
  const mm = String(Math.floor(remaining / 60)).padStart(2, '0');
  const ss = String(remaining % 60).padStart(2, '0');

  // simulated resource fill as we focus
  const focusResources = {
    ...state.resources,
    intellectual: { am: state.resources.intellectual.am, pm: Math.min(1, state.resources.intellectual.pm + progress * 0.4) },
    contextual:   { am: state.resources.contextual.am,   pm: Math.min(1, state.resources.contextual.pm + progress * 0.3) },
  };

  const finish = () => {
    setState(s => ({
      ...s,
      resources: focusResources,
      stones: [...s.stones, { kind: 'deep', label: 'focus', when: Date.now() }],
    }));
    onBack();
  };

  return (
    <div className="fade-in" style={{ padding: '16px 20px 120px', minHeight: '100%' }}>
      <ScreenHeader eyebrow="Focus session" title="One thing, quietly" onBack={onBack} />

      {/* Mandala preview */}
      <div style={{ display: 'flex', justifyContent: 'center', margin: '8px 0 16px' }}>
        <SatirMandala size={220} resources={focusResources} complexity={state.complexity} animate={running} />
      </div>

      {/* Timer */}
      <div style={{ textAlign: 'center', margin: '8px 0 24px' }}>
        <div className="serif" style={{ fontSize: 64, color: 'oklch(0.96 0.015 220)', lineHeight: 1 }}>
          {mm}<span style={{ color: 'oklch(0.78 0.08 215)' }}>:</span>{ss}
        </div>
        <div className="mono" style={{ fontSize: 10, color: 'rgba(240,248,255,0.45)', letterSpacing: 1.4, textTransform: 'uppercase', marginTop: 6 }}>
          {running ? 'tending' : `${minutes} minute session`}
        </div>
      </div>

      {/* Duration picker */}
      {!running && (
        <div style={{ display: 'flex', gap: 8, justifyContent: 'center', marginBottom: 24 }}>
          {[10, 15, 25, 45].map(m => (
            <Chip key={m} active={minutes === m} onClick={() => setMinutes(m)}
              style={{ fontSize: 13, padding: '10px 16px' }}>
              {m} min
            </Chip>
          ))}
        </div>
      )}

      {/* Controls */}
      <div style={{ display: 'flex', gap: 10 }}>
        {!running && !doneOnce && (
          <PrimaryBtn onClick={() => setRunning(true)}>Begin focus  ◦</PrimaryBtn>
        )}
        {running && (
          <>
            <PrimaryBtn variant="ghost" onClick={() => setRunning(false)}>Pause</PrimaryBtn>
            <PrimaryBtn variant="sand" onClick={finish}>End early</PrimaryBtn>
          </>
        )}
        {!running && doneOnce && (
          <PrimaryBtn variant="sand" onClick={finish}>Complete  ◦</PrimaryBtn>
        )}
      </div>

      <div className="ui" style={{
        textAlign: 'center', fontSize: 11, color: 'rgba(240,248,255,0.35)',
        marginTop: 18, letterSpacing: 0.3,
      }}>
        phone silenced · notifications held · mandala tended
      </div>
    </div>
  );
}
Object.assign(window, { FocusScreen });
