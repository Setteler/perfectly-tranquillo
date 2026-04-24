// Settings — edit habits, sound, font
function SettingsScreen({ state, setState, onBack }) {
  const [newHabit, setNewHabit] = React.useState('');

  const toggleHabit = (id) => {
    setState(s => ({
      ...s,
      habits: s.habits.filter(h => h.id !== id),
    }));
  };

  const addHabit = () => {
    if (!newHabit.trim()) return;
    setState(s => ({
      ...s,
      habits: [...s.habits, {
        id: 'custom-' + Date.now(),
        label: newHabit.trim(),
        hint: 'custom habit',
        done: false, streak: 0,
      }],
    }));
    setNewHabit('');
  };

  return (
    <div className="fade-in" style={{ padding: '16px 20px 120px' }}>
      <ScreenHeader eyebrow="Adjust gently" title="Settings" onBack={onBack} />

      {/* Profile */}
      <Card style={{ marginBottom: 14, display: 'flex', alignItems: 'center', gap: 14 }} padded={false}>
        <div style={{ padding: '16px 18px', display: 'flex', alignItems: 'center', gap: 14, width: '100%' }}>
          <div style={{
            width: 50, height: 50, borderRadius: '50%',
            background: 'radial-gradient(circle at 30% 30%, oklch(0.9 0.05 80), oklch(0.65 0.08 60))',
            flexShrink: 0,
          }} />
          <div style={{ flex: 1 }}>
            <div className="serif" style={{ fontSize: 20, color: 'oklch(0.96 0.015 220)' }}>
              {state.name}
            </div>
            <div className="ui" style={{ fontSize: 11, color: 'rgba(240,248,255,0.5)' }}>
              joined 12 days ago
            </div>
          </div>
        </div>
      </Card>

      {/* Sound */}
      <SettingRow label="Sound" hint="Soft chimes on completion">
        <Toggle on={state.sound} onChange={v => setState(s => ({ ...s, sound: v }))} />
      </SettingRow>

      {/* Font */}
      <div className="ui" style={{
        fontSize: 10, fontWeight: 600, letterSpacing: 1.6, textTransform: 'uppercase',
        color: 'rgba(240,248,255,0.45)', marginTop: 22, marginBottom: 10, padding: '0 4px',
      }}>Font pairing</div>
      <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
        {[
          { k: 'instrument', title: 'Instrument & Inter', sub: 'serene, editorial' },
          { k: 'fraunces',   title: 'Fraunces & DM Sans', sub: 'warm, playful' },
          { k: 'cormorant',  title: 'Cormorant & Inter',  sub: 'poetic, light' },
        ].map(f => (
          <button key={f.k} onClick={() => setState(s => ({ ...s, font: f.k }))}
            className="ui"
            style={{
              textAlign: 'left', padding: '14px 16px', borderRadius: 18, cursor: 'pointer',
              background: state.font === f.k ? 'oklch(0.78 0.08 215 / 0.14)' : 'rgba(240,248,255,0.04)',
              border: '1px solid ' + (state.font === f.k ? 'oklch(0.78 0.08 215 / 0.4)' : 'rgba(240,248,255,0.1)'),
              color: 'oklch(0.92 0.03 205)',
              display: 'flex', alignItems: 'center', gap: 12,
            }}>
            <div style={{ flex: 1 }}>
              <div style={{ fontSize: 15, color: 'oklch(0.96 0.015 220)' }}>{f.title}</div>
              <div style={{ fontSize: 11, color: 'rgba(240,248,255,0.45)', marginTop: 2 }}>{f.sub}</div>
            </div>
            {state.font === f.k && (
              <div style={{ width: 8, height: 8, borderRadius: '50%', background: 'oklch(0.78 0.08 215)' }} />
            )}
          </button>
        ))}
      </div>

      {/* Habits edit */}
      <div className="ui" style={{
        fontSize: 10, fontWeight: 600, letterSpacing: 1.6, textTransform: 'uppercase',
        color: 'rgba(240,248,255,0.45)', marginTop: 22, marginBottom: 10, padding: '0 4px',
      }}>Habits · tap × to remove</div>

      <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
        {state.habits.map(h => (
          <div key={h.id} style={{
            display: 'flex', alignItems: 'center', gap: 12,
            padding: '12px 14px', borderRadius: 14,
            background: 'rgba(240,248,255,0.04)',
            border: '1px solid rgba(240,248,255,0.08)',
          }}>
            <Stone color="jade" size={16} />
            <div className="serif" style={{ flex: 1, fontSize: 16, color: 'oklch(0.96 0.015 220)' }}>
              {h.label}
            </div>
            <button onClick={() => toggleHabit(h.id)}
              className="ui"
              style={{
                width: 24, height: 24, borderRadius: '50%',
                background: 'rgba(240,248,255,0.06)',
                border: '1px solid rgba(240,248,255,0.15)',
                color: 'rgba(240,248,255,0.6)', fontSize: 12,
                cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center',
              }}>
              ×
            </button>
          </div>
        ))}
      </div>

      <div style={{ display: 'flex', gap: 8, marginTop: 10 }}>
        <input
          value={newHabit}
          onChange={e => setNewHabit(e.target.value)}
          placeholder="add a habit…"
          className="ui"
          style={{
            flex: 1, padding: '12px 16px', borderRadius: 100,
            background: 'rgba(240,248,255,0.04)',
            border: '1px solid rgba(240,248,255,0.12)',
            color: 'oklch(0.96 0.015 220)',
            fontSize: 13, outline: 'none',
          }}
          onKeyDown={e => e.key === 'Enter' && addHabit()}
        />
        <button onClick={addHabit} className="ui"
          style={{
            padding: '0 20px', borderRadius: 100,
            background: 'oklch(0.78 0.08 215 / 0.2)',
            border: '1px solid oklch(0.78 0.08 215 / 0.4)',
            color: 'oklch(0.88 0.06 215)', fontSize: 14,
            cursor: 'pointer',
          }}>
          add
        </button>
      </div>

      <div className="ui" style={{
        textAlign: 'center', fontSize: 11, color: 'rgba(240,248,255,0.3)',
        marginTop: 32, letterSpacing: 0.3,
      }}>
        Perfectly Tranquillo · v0.1 · made with care
      </div>
    </div>
  );
}

function SettingRow({ label, hint, children }) {
  return (
    <div style={{
      display: 'flex', alignItems: 'center', gap: 12,
      padding: '14px 16px', borderRadius: 18,
      background: 'rgba(240,248,255,0.04)',
      border: '1px solid rgba(240,248,255,0.1)',
      marginBottom: 8,
    }}>
      <div style={{ flex: 1 }}>
        <div className="serif" style={{ fontSize: 17, color: 'oklch(0.96 0.015 220)' }}>{label}</div>
        {hint && <div className="ui" style={{ fontSize: 11, color: 'rgba(240,248,255,0.45)', marginTop: 2 }}>{hint}</div>}
      </div>
      {children}
    </div>
  );
}

function Toggle({ on, onChange }) {
  return (
    <button onClick={() => onChange(!on)}
      style={{
        width: 44, height: 26, borderRadius: 100,
        background: on ? 'oklch(0.78 0.08 215)' : 'rgba(240,248,255,0.1)',
        border: '1px solid ' + (on ? 'oklch(0.78 0.08 215)' : 'rgba(240,248,255,0.15)'),
        cursor: 'pointer', position: 'relative',
        transition: 'background 0.2s',
      }}>
      <div style={{
        position: 'absolute', top: 2, left: on ? 20 : 2,
        width: 20, height: 20, borderRadius: '50%',
        background: on ? 'oklch(0.12 0.03 250)' : 'oklch(0.92 0.03 205)',
        transition: 'left 0.2s',
      }} />
    </button>
  );
}

// Evening reflection - quick 60s fill
function EveningScreen({ state, setState, onBack }) {
  const [step, setStep] = React.useState(0);
  const [learned, setLearned] = React.useState('');
  const [grateful, setGrateful] = React.useState('');

  const finish = () => {
    setState(s => ({
      ...s,
      learned, grateful, eveningDone: true,
      resources: {
        ...s.resources,
        intellectual: { ...s.resources.intellectual, pm: Math.min(1, s.resources.intellectual.pm + 0.4) },
        emotional: { ...s.resources.emotional, pm: Math.min(1, s.resources.emotional.pm + 0.35) },
        interactional: { ...s.resources.interactional, pm: Math.min(1, s.resources.interactional.pm + 0.25) },
        spiritual: { ...s.resources.spiritual, pm: Math.min(1, s.resources.spiritual.pm + 0.3) },
      },
      stones: [...s.stones, { kind: 'sand', label: 'evening', when: Date.now() }],
    }));
    onBack();
  };

  return (
    <div className="fade-in" style={{ padding: '24px 24px 120px' }}>
      <div className="ui" style={{
        fontSize: 10, fontWeight: 600, letterSpacing: 1.6, textTransform: 'uppercase',
        color: 'oklch(0.86 0.05 75)', marginBottom: 24,
      }}>Evening · Tuesday · 9:41 PM</div>

      {step === 0 && (
        <div className="fade-in">
          <div className="serif" style={{ fontSize: 32, lineHeight: 1.1, color: 'oklch(0.96 0.015 220)', marginBottom: 8 }}>
            One thing the day taught you.
          </div>
          <div className="ui" style={{ fontSize: 14, color: 'rgba(240,248,255,0.55)', marginBottom: 24 }}>
            A sentence is enough.
          </div>
          <textarea value={learned} onChange={e => setLearned(e.target.value)}
            className="serif"
            placeholder="today I learned…"
            style={{
              width: '100%', minHeight: 120, padding: 18, borderRadius: 18,
              background: 'rgba(240,248,255,0.04)',
              border: '1px solid rgba(240,248,255,0.12)',
              color: 'oklch(0.96 0.015 220)', fontSize: 20, lineHeight: 1.4,
              outline: 'none', resize: 'none', boxSizing: 'border-box', marginBottom: 24,
            }} />
          <PrimaryBtn onClick={() => setStep(1)}
            style={{ opacity: learned.trim() ? 1 : 0.4, pointerEvents: learned.trim() ? 'auto' : 'none' }}>
            Continue
          </PrimaryBtn>
        </div>
      )}

      {step === 1 && (
        <div className="fade-in">
          <div className="serif" style={{ fontSize: 32, lineHeight: 1.1, color: 'oklch(0.96 0.015 220)', marginBottom: 8 }}>
            A small gratitude.
          </div>
          <div className="ui" style={{ fontSize: 14, color: 'rgba(240,248,255,0.55)', marginBottom: 24 }}>
            Not the grand kind. The tiny kind.
          </div>
          <textarea value={grateful} onChange={e => setGrateful(e.target.value)}
            className="serif"
            placeholder="I'm grateful for…"
            style={{
              width: '100%', minHeight: 120, padding: 18, borderRadius: 18,
              background: 'rgba(240,248,255,0.04)',
              border: '1px solid rgba(240,248,255,0.12)',
              color: 'oklch(0.96 0.015 220)', fontSize: 20, lineHeight: 1.4,
              outline: 'none', resize: 'none', boxSizing: 'border-box', marginBottom: 24,
            }} />
          <div style={{ display: 'flex', gap: 10 }}>
            <PrimaryBtn variant="ghost" onClick={() => setStep(0)}>Back</PrimaryBtn>
            <PrimaryBtn variant="sand" onClick={finish}
              style={{ opacity: grateful.trim() ? 1 : 0.4, pointerEvents: grateful.trim() ? 'auto' : 'none' }}>
              Rest well  ◦
            </PrimaryBtn>
          </div>
        </div>
      )}
    </div>
  );
}

Object.assign(window, { SettingsScreen, EveningScreen, SettingRow, Toggle });
