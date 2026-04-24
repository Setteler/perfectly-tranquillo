// Home — big mandala + today snapshot + quick actions
function HomeScreen({ state, setState, go }) {
  const [showMandalaInfo, setShowMandalaInfo] = React.useState(false);
  const habitsDone = state.habits.filter(h => h.done).length;
  const habitsTotal = state.habits.length;
  const avgFill = (() => {
    const vals = Object.values(state.resources);
    const s = vals.reduce((a, r) => a + r.am + r.pm, 0);
    return s / (vals.length * 2);
  })();

  return (
    <div className="fade-in" style={{ padding: '16px 20px 120px' }}>
      {/* Top row */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 8 }}>
        <div>
          <div className="ui" style={{
            fontSize: 10, fontWeight: 600, letterSpacing: 1.6, textTransform: 'uppercase',
            color: 'oklch(0.78 0.08 215)',
          }}>Tuesday · day 12</div>
          <div className="serif" style={{ fontSize: 26, color: 'oklch(0.96 0.015 220)', lineHeight: 1.1, marginTop: 2 }}>
            Bloom gently, {state.name}.
          </div>
        </div>
        <IconGear onClick={() => go('settings')} />
      </div>

      {/* Intention card */}
      {state.intent && (
        <div style={{
          marginTop: 14, padding: '12px 16px', borderRadius: 100,
          background: 'oklch(0.86 0.05 75 / 0.1)',
          border: '1px solid oklch(0.86 0.05 75 / 0.25)',
          display: 'flex', alignItems: 'center', gap: 10,
        }}>
          <div style={{
            width: 8, height: 8, borderRadius: '50%',
            background: 'oklch(0.86 0.05 75)',
            boxShadow: '0 0 8px oklch(0.86 0.05 75 / 0.6)',
          }} />
          <span className="ui" style={{ fontSize: 11, fontWeight: 600, letterSpacing: 1.4, textTransform: 'uppercase', color: 'oklch(0.86 0.05 75)' }}>
            intention
          </span>
          <span className="serif" style={{ fontSize: 18, color: 'oklch(0.96 0.015 220)' }}>
            {state.intent}
          </span>
        </div>
      )}

      {/* Mandala */}
      <div style={{ position: 'relative', margin: '12px auto 0', display: 'flex', justifyContent: 'center' }}>
        <SatirMandala size={300} resources={state.resources} showLabels={true} complexity={state.complexity} />
        <button onClick={() => setShowMandalaInfo(v => !v)}
          className="ui"
          style={{
            position: 'absolute', top: 6, right: 8,
            width: 26, height: 26, borderRadius: '50%',
            background: 'rgba(240,248,255,0.1)',
            border: '1px solid rgba(240,248,255,0.2)',
            color: 'oklch(0.92 0.04 205)',
            fontSize: 13, fontWeight: 600, cursor: 'pointer',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
          }}>?</button>
      </div>

      {showMandalaInfo ? (
        <div className="fade-in" style={{
          margin: '6px 0 12px',
          padding: '14px 16px', borderRadius: 18,
          background: 'oklch(0.88 0.07 85 / 0.12)',
          border: '1px solid oklch(0.88 0.07 85 / 0.3)',
        }}>
          <div className="ui" style={{
            fontSize: 10, fontWeight: 600, letterSpacing: 1.6, textTransform: 'uppercase',
            color: 'oklch(0.88 0.07 85)', marginBottom: 8,
          }}>Your resource mandala</div>
          <div className="serif" style={{ fontSize: 16, lineHeight: 1.45, color: 'oklch(0.96 0.03 200)', marginBottom: 10 }}>
            Eight petals — the parts of you that need tending.
          </div>
          <div className="ui" style={{ fontSize: 12.5, lineHeight: 1.55, color: 'rgba(240,248,255,0.75)' }}>
            Based on Virginia Satir's self-mandala. Each petal is a resource:
            <b style={{ color: 'oklch(0.95 0.04 200)' }}> Body, Mind, Heart, Senses, Connection, Nourish, Place, Spirit.</b>
            <div style={{ marginTop: 10, display: 'flex', alignItems: 'center', gap: 8 }}>
              <div style={{ width: 16, height: 16, borderRadius: '50%', background: 'oklch(0.75 0.08 215)', flexShrink: 0 }} />
              <span><b>Inner ring</b> fills in the morning as you check in.</span>
            </div>
            <div style={{ marginTop: 6, display: 'flex', alignItems: 'center', gap: 8 }}>
              <div style={{ width: 16, height: 16, borderRadius: '50%', background: 'oklch(0.82 0.09 75)', flexShrink: 0 }} />
              <span><b>Outer ring</b> fills through the day — every habit, break, focus and gratitude feeds a petal.</span>
            </div>
            <p style={{ margin: '12px 0 0', fontStyle: 'italic', color: 'rgba(240,248,255,0.55)' }}>
              A full mandala isn't the goal — noticing which petals are thin is.
            </p>
          </div>
        </div>
      ) : (
        <div className="ui" style={{
          textAlign: 'center', fontSize: 11, color: 'rgba(240,248,255,0.55)',
          letterSpacing: 0.6, marginTop: 4, marginBottom: 18,
        }}>
          inner ring · morning   ·   outer ring · evening
        </div>
      )}

      {/* Quick actions grid */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10, marginBottom: 10 }}>
        <QuickAction onClick={() => go('breath')}
          title="Breathe" subtitle="4·4·4·4" accent="oklch(0.78 0.08 215)" />
        <QuickAction onClick={() => go('break')}
          title="Take a break" subtitle="60 seconds" accent="oklch(0.86 0.05 75)" />
        <QuickAction onClick={() => go('focus')}
          title="Focus" subtitle="25 min" accent="oklch(0.72 0.10 35)" />
        <QuickAction onClick={() => go('habits')}
          title={`Habits`} subtitle={`${habitsDone} of ${habitsTotal} today`} accent="oklch(0.82 0.07 175)" />
      </div>

      {/* Today's good thing */}
      {state.goodThing && (
        <Card style={{ marginTop: 10 }}>
          <div className="ui" style={{
            fontSize: 10, fontWeight: 600, letterSpacing: 1.6, textTransform: 'uppercase',
            color: 'oklch(0.78 0.08 215)', marginBottom: 6,
          }}>Looking forward to</div>
          <div className="serif" style={{ fontSize: 20, lineHeight: 1.3, color: 'oklch(0.96 0.015 220)' }}>
            "{state.goodThing}"
          </div>
        </Card>
      )}

      {/* Evening nudge if AM done and evening not */}
      {state.morningDone && !state.eveningDone && (
        <Card tone="sand" style={{ marginTop: 10 }} onClick={() => go('evening')}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
            <div style={{
              width: 42, height: 42, borderRadius: '50%',
              background: 'radial-gradient(circle at 30% 30%, oklch(0.9 0.06 80), oklch(0.65 0.08 60))',
              flexShrink: 0,
            }} />
            <div style={{ flex: 1 }}>
              <div className="serif" style={{ fontSize: 17, color: 'oklch(0.96 0.015 220)' }}>
                Evening reflection
              </div>
              <div className="ui" style={{ fontSize: 12, color: 'rgba(240,248,255,0.55)', marginTop: 2 }}>
                Fill the outer ring · 90 seconds
              </div>
            </div>
            <span style={{ color: 'oklch(0.86 0.05 75)', fontSize: 20 }}>→</span>
          </div>
        </Card>
      )}
    </div>
  );
}

function QuickAction({ title, subtitle, accent, onClick }) {
  return (
    <button onClick={onClick}
      className="ui"
      style={{
        textAlign: 'left', padding: 16, borderRadius: 20,
        background: 'rgba(240,248,255,0.04)',
        border: '1px solid rgba(240,248,255,0.1)',
        cursor: 'pointer', color: 'oklch(0.92 0.03 205)',
        position: 'relative', overflow: 'hidden',
      }}>
      <div style={{
        position: 'absolute', top: 12, right: 12,
        width: 6, height: 6, borderRadius: '50%', background: accent,
        boxShadow: `0 0 8px ${accent}`,
      }} />
      <div className="serif" style={{ fontSize: 20, color: 'oklch(0.96 0.015 220)', marginBottom: 2 }}>
        {title}
      </div>
      <div style={{ fontSize: 11, color: 'rgba(240,248,255,0.5)', letterSpacing: 0.3 }}>
        {subtitle}
      </div>
    </button>
  );
}

Object.assign(window, { HomeScreen });
