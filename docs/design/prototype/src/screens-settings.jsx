// Settings — profile, rhythm, sound, notifications, appearance, privacy, about
function SettingsScreen({ state, setState, onBack }) {
  const [editingName, setEditingName] = React.useState(false);
  const [nameDraft, setNameDraft] = React.useState(state.name);
  const [confirmDelete, setConfirmDelete] = React.useState(false);
  const [exported, setExported] = React.useState(false);
  const [editingTime, setEditingTime] = React.useState(null); // 'morning' | 'evening' | null

  const SOUND_OPTIONS = [
    { k: 'waves',  label: 'Ocean waves',    hint: 'slow, low tide' },
    { k: 'birds',  label: 'Birds chirping', hint: 'dawn chorus' },
    { k: 'bowls',  label: 'Singing bowls',  hint: 'warm, resonant' },
    { k: 'music',  label: 'Calming music',  hint: 'soft piano' },
    { k: 'none',   label: 'No ambient',     hint: 'silence is fine' },
  ];

  const NOTIF_MODES = [
    { k: 'silent',  label: 'Silent',   hint: 'no sound, no buzz' },
    { k: 'vibrate', label: 'Vibrate',  hint: 'a gentle buzz' },
    { k: 'sound',   label: 'Sound',    hint: 'soft chime' },
  ];

  return (
    <div className="fade-in" style={{ padding: '16px 20px 120px' }}>
      <ScreenHeader eyebrow="Adjust gently" title="Settings" onBack={onBack} />

      {/* Profile */}
      <Card style={{ marginBottom: 20, display: 'flex', alignItems: 'center', gap: 14 }} padded={false}>
        <div style={{ padding: '16px 18px', display: 'flex', alignItems: 'center', gap: 14, width: '100%' }}>
          <div style={{
            width: 50, height: 50, borderRadius: '50%',
            background: 'radial-gradient(circle at 30% 30%, oklch(0.9 0.05 80), oklch(0.65 0.08 60))',
            flexShrink: 0,
          }} />
          <div style={{ flex: 1 }}>
            {editingName ? (
              <input autoFocus value={nameDraft}
                onChange={e => setNameDraft(e.target.value)}
                onBlur={() => { setState(s => ({ ...s, name: nameDraft.trim() || s.name })); setEditingName(false); }}
                onKeyDown={e => {
                  if (e.key === 'Enter') { setState(s => ({ ...s, name: nameDraft.trim() || s.name })); setEditingName(false); }
                  if (e.key === 'Escape') { setNameDraft(state.name); setEditingName(false); }
                }}
                className="serif"
                style={{
                  width: '100%', background: 'transparent', border: 'none', outline: 'none',
                  fontSize: 22, color: 'oklch(0.96 0.015 220)',
                  borderBottom: '1px solid oklch(0.78 0.08 215 / 0.4)', paddingBottom: 2,
                }} />
            ) : (
              <div className="serif" style={{ fontSize: 22, color: 'oklch(0.96 0.015 220)', cursor: 'pointer' }}
                onClick={() => { setNameDraft(state.name); setEditingName(true); }}>
                {state.name}
              </div>
            )}
            <div className="ui" style={{ fontSize: 11, color: 'rgba(240,248,255,0.5)', marginTop: 2 }}>
              joined 12 days ago · tap name to edit
            </div>
          </div>
        </div>
      </Card>

      {/* Rhythm */}
      <SectionLabel>Rhythm</SectionLabel>
      <div style={{ display: 'flex', flexDirection: 'column', gap: 8, marginBottom: 20 }}>
        <SettingRow label="Morning nudge" hint="when to invite the morning ritual">
          <TimePill value={state.morningNudge || '7:30 AM'}
            onClick={() => setEditingTime('morning')} />
        </SettingRow>
        <SettingRow label="Evening nudge" hint="when to invite the reflection">
          <TimePill value={state.eveningNudge || '9:00 PM'}
            onClick={() => setEditingTime('evening')} />
        </SettingRow>
        <SettingRow label="Quiet hours" hint="no nudges during this window">
          <div className="mono" style={{
            fontSize: 12, color: 'rgba(240,248,255,0.7)',
            padding: '6px 10px', borderRadius: 10,
            background: 'rgba(240,248,255,0.04)',
            border: '1px solid rgba(240,248,255,0.1)',
          }}>
            10 PM — 7 AM
          </div>
        </SettingRow>
      </div>

      {/* Sound */}
      <SectionLabel>Ambient sound</SectionLabel>
      <div style={{ display: 'flex', flexDirection: 'column', gap: 6, marginBottom: 14 }}>
        {SOUND_OPTIONS.map(o => (
          <OptionRow key={o.k}
            selected={state.ambientSound === o.k}
            onClick={() => setState(s => ({ ...s, ambientSound: o.k }))}
            label={o.label} hint={o.hint}
            icon={<SoundGlyph kind={o.k} />} />
        ))}
      </div>

      <SettingRow label="Tap chime" hint="soft sound on completion">
        <Toggle on={state.sound} onChange={v => setState(s => ({ ...s, sound: v }))} />
      </SettingRow>

      {/* Notifications mode */}
      <SectionLabel style={{ marginTop: 20 }}>Notifications</SectionLabel>
      <div style={{
        display: 'flex', gap: 6, padding: 4, borderRadius: 100,
        background: 'rgba(240,248,255,0.04)',
        border: '1px solid rgba(240,248,255,0.1)',
        marginBottom: 20,
      }}>
        {NOTIF_MODES.map(m => {
          const active = state.notifMode === m.k;
          return (
            <button key={m.k}
              onClick={() => setState(s => ({ ...s, notifMode: m.k }))}
              className="ui"
              style={{
                flex: 1, padding: '10px 8px', borderRadius: 100, cursor: 'pointer',
                background: active ? 'oklch(0.78 0.08 215 / 0.22)' : 'transparent',
                border: '1px solid ' + (active ? 'oklch(0.78 0.08 215 / 0.45)' : 'transparent'),
                color: active ? 'oklch(0.92 0.04 215)' : 'rgba(240,248,255,0.55)',
                fontSize: 13, fontWeight: active ? 600 : 500,
                transition: 'all 0.15s',
              }}>
              {m.label}
            </button>
          );
        })}
      </div>

      {/* Appearance */}
      <SectionLabel>Appearance</SectionLabel>
      <div style={{
        display: 'flex', gap: 10, marginBottom: 20,
      }}>
        {[
          { k: 'dark', label: 'Dark', hint: 'evening sky' },
          { k: 'light', label: 'Light', hint: 'morning light' },
        ].map(t => {
          const active = (state.theme || 'dark') === t.k;
          return (
            <button key={t.k}
              onClick={() => setState(s => ({ ...s, theme: t.k }))}
              className="ui"
              style={{
                flex: 1, padding: '16px 14px', borderRadius: 18,
                background: t.k === 'dark'
                  ? 'linear-gradient(160deg, oklch(0.22 0.06 240), oklch(0.14 0.04 245))'
                  : 'linear-gradient(160deg, oklch(0.96 0.02 85), oklch(0.88 0.04 70))',
                border: '1.5px solid ' + (active ? 'oklch(0.78 0.08 215 / 0.6)' : 'rgba(240,248,255,0.1)'),
                cursor: 'pointer',
                display: 'flex', flexDirection: 'column', alignItems: 'flex-start', gap: 8,
                position: 'relative',
              }}>
              {/* tiny preview dot — sun or moon */}
              <div style={{
                width: 22, height: 22, borderRadius: '50%',
                background: t.k === 'dark'
                  ? 'radial-gradient(circle at 35% 35%, oklch(0.92 0.03 80), oklch(0.7 0.05 70))'
                  : 'radial-gradient(circle at 35% 35%, oklch(0.98 0.02 85), oklch(0.85 0.08 75))',
                boxShadow: t.k === 'dark'
                  ? '0 0 18px oklch(0.85 0.06 75 / 0.5)'
                  : '0 0 22px oklch(0.95 0.1 80 / 0.7)',
              }} />
              <div className="serif" style={{
                fontSize: 17,
                color: t.k === 'dark' ? 'oklch(0.96 0.015 220)' : 'oklch(0.3 0.05 40)',
              }}>{t.label}</div>
              <div className="ui" style={{
                fontSize: 11,
                color: t.k === 'dark' ? 'rgba(240,248,255,0.55)' : 'oklch(0.4 0.04 40 / 0.7)',
              }}>{t.hint}</div>
              {active && (
                <div style={{
                  position: 'absolute', top: 10, right: 10,
                  width: 8, height: 8, borderRadius: '50%',
                  background: 'oklch(0.78 0.08 215)',
                }} />
              )}
            </button>
          );
        })}
      </div>

      {/* Privacy */}
      <SectionLabel>Privacy</SectionLabel>
      <div style={{ display: 'flex', flexDirection: 'column', gap: 8, marginBottom: 12 }}>
        <ActionRow label="Export my journal" hint="download all entries as text"
          onClick={() => { setExported(true); setTimeout(() => setExported(false), 2000); }} />
        <ActionRow label="Delete all data" hint="erase this app from the device" danger
          onClick={() => setConfirmDelete(true)} />
      </div>
      <div className="ui" style={{
        fontSize: 12, color: 'rgba(240,248,255,0.4)',
        padding: '0 4px', marginBottom: 24, lineHeight: 1.5,
        fontStyle: 'italic',
      }}>
        Your words stay on this device. Nothing is sent anywhere.
      </div>

      {/* About */}
      <div className="ui" style={{
        textAlign: 'center', fontSize: 11, color: 'rgba(240,248,255,0.35)',
        marginTop: 24, letterSpacing: 0.3, lineHeight: 1.8,
      }}>
        Perfectly Tranquillo · v0.3<br />
        <span className="serif" style={{ fontStyle: 'italic', fontSize: 14, color: 'rgba(240,248,255,0.5)' }}>
          small daily rituals, kept.
        </span>
      </div>

      {/* Time editor sheet */}
      {editingTime && (
        <TimeEditSheet
          label={editingTime === 'morning' ? 'Morning nudge' : 'Evening nudge'}
          value={editingTime === 'morning' ? (state.morningNudge || '7:30 AM') : (state.eveningNudge || '9:00 PM')}
          onSave={v => {
            setState(s => ({ ...s, [editingTime === 'morning' ? 'morningNudge' : 'eveningNudge']: v }));
            setEditingTime(null);
          }}
          onClose={() => setEditingTime(null)} />
      )}

      {/* Delete confirm sheet */}
      {confirmDelete && (
        <ConfirmSheet
          title="Delete all data?"
          body="Every entry, every shell, every note you've kept will be gone. This can't be undone."
          confirm="Delete everything"
          onConfirm={() => { setConfirmDelete(false); /* demo — we don't actually delete */ }}
          onCancel={() => setConfirmDelete(false)} />
      )}

      {/* Export toast */}
      {exported && (
        <div className="fade-in" style={{
          position: 'fixed', bottom: 110, left: 30, right: 30,
          padding: '14px 18px', borderRadius: 16,
          background: 'oklch(0.4 0.06 175 / 0.92)',
          border: '1px solid oklch(0.75 0.09 175 / 0.4)',
          color: 'oklch(0.96 0.02 175)',
          fontSize: 14, textAlign: 'center', zIndex: 100,
          backdropFilter: 'blur(20px)',
        }}>
          ✓ journal exported — 147 entries
        </div>
      )}
    </div>
  );
}

function SectionLabel({ children, style = {} }) {
  return (
    <div className="ui" style={{
      fontSize: 10, fontWeight: 600, letterSpacing: 1.6, textTransform: 'uppercase',
      color: 'rgba(240,248,255,0.45)', marginBottom: 10, padding: '0 4px',
      ...style,
    }}>{children}</div>
  );
}

function OptionRow({ selected, onClick, label, hint, icon }) {
  return (
    <button onClick={onClick} className="ui"
      style={{
        textAlign: 'left', padding: '12px 14px', borderRadius: 16, cursor: 'pointer',
        background: selected ? 'oklch(0.78 0.08 215 / 0.14)' : 'rgba(240,248,255,0.04)',
        border: '1px solid ' + (selected ? 'oklch(0.78 0.08 215 / 0.4)' : 'rgba(240,248,255,0.1)'),
        color: 'oklch(0.92 0.03 205)',
        display: 'flex', alignItems: 'center', gap: 12,
        transition: 'all 0.15s',
      }}>
      {icon && <div style={{ width: 28, display: 'flex', justifyContent: 'center' }}>{icon}</div>}
      <div style={{ flex: 1 }}>
        <div className="serif" style={{ fontSize: 16, color: 'oklch(0.96 0.015 220)' }}>{label}</div>
        {hint && <div className="ui" style={{ fontSize: 11, color: 'rgba(240,248,255,0.45)', marginTop: 1 }}>{hint}</div>}
      </div>
      {selected && (
        <div style={{ width: 8, height: 8, borderRadius: '50%', background: 'oklch(0.78 0.08 215)' }} />
      )}
    </button>
  );
}

function ActionRow({ label, hint, onClick, danger }) {
  return (
    <button onClick={onClick} className="ui"
      style={{
        textAlign: 'left', padding: '14px 16px', borderRadius: 16, cursor: 'pointer',
        background: 'rgba(240,248,255,0.04)',
        border: '1px solid rgba(240,248,255,0.1)',
        display: 'flex', alignItems: 'center', gap: 12,
      }}>
      <div style={{ flex: 1 }}>
        <div className="serif" style={{
          fontSize: 16,
          color: danger ? 'oklch(0.78 0.12 25)' : 'oklch(0.96 0.015 220)',
        }}>{label}</div>
        {hint && <div className="ui" style={{ fontSize: 11, color: 'rgba(240,248,255,0.45)', marginTop: 2 }}>{hint}</div>}
      </div>
      <div style={{ color: 'rgba(240,248,255,0.4)', fontSize: 18 }}>›</div>
    </button>
  );
}

function TimePill({ value, onClick }) {
  return (
    <button onClick={onClick} className="mono"
      style={{
        padding: '7px 14px', borderRadius: 100,
        background: 'oklch(0.78 0.08 215 / 0.14)',
        border: '1px solid oklch(0.78 0.08 215 / 0.35)',
        color: 'oklch(0.88 0.06 215)', fontSize: 13, cursor: 'pointer',
        letterSpacing: 0.4,
      }}>
      {value}
    </button>
  );
}

// Tiny glyph per sound type
function SoundGlyph({ kind }) {
  if (kind === 'waves') {
    return (
      <svg width="22" height="16" viewBox="0 0 22 16">
        <path d="M1 10 Q 4 5, 7 10 T 13 10 T 19 10" stroke="oklch(0.8 0.06 215)" strokeWidth="1.5" fill="none" strokeLinecap="round" />
        <path d="M1 14 Q 4 9, 7 14 T 13 14 T 19 14" stroke="oklch(0.7 0.07 220)" strokeWidth="1.5" fill="none" strokeLinecap="round" opacity="0.7" />
      </svg>
    );
  }
  if (kind === 'birds') {
    return (
      <svg width="22" height="16" viewBox="0 0 22 16">
        <path d="M3 9 Q 6 4, 9 9" stroke="oklch(0.85 0.05 80)" strokeWidth="1.5" fill="none" strokeLinecap="round" />
        <path d="M11 7 Q 14 2, 17 7" stroke="oklch(0.85 0.05 80)" strokeWidth="1.5" fill="none" strokeLinecap="round" />
        <circle cx="9" cy="13" r="1" fill="oklch(0.85 0.05 80)" />
        <circle cx="17" cy="11" r="1" fill="oklch(0.85 0.05 80)" />
      </svg>
    );
  }
  if (kind === 'bowls') {
    return (
      <svg width="22" height="16" viewBox="0 0 22 16">
        <ellipse cx="11" cy="9" rx="7" ry="5" stroke="oklch(0.82 0.07 75)" strokeWidth="1.3" fill="none" />
        <ellipse cx="11" cy="9" rx="4" ry="2.8" stroke="oklch(0.82 0.07 75)" strokeWidth="1" fill="none" opacity="0.6" />
        <line x1="11" y1="3" x2="11" y2="1" stroke="oklch(0.82 0.07 75)" strokeWidth="1" strokeLinecap="round" />
      </svg>
    );
  }
  if (kind === 'music') {
    return (
      <svg width="22" height="16" viewBox="0 0 22 16">
        <path d="M7 12 L 7 4 L 16 3 L 16 11" stroke="oklch(0.78 0.08 280)" strokeWidth="1.3" fill="none" strokeLinecap="round" strokeLinejoin="round" />
        <ellipse cx="5.5" cy="12" rx="2" ry="1.5" fill="oklch(0.78 0.08 280)" />
        <ellipse cx="14.5" cy="11" rx="2" ry="1.5" fill="oklch(0.78 0.08 280)" />
      </svg>
    );
  }
  return (
    <svg width="22" height="16" viewBox="0 0 22 16">
      <circle cx="11" cy="8" r="5" stroke="oklch(0.6 0.03 220)" strokeWidth="1.2" fill="none" />
      <line x1="6" y1="13" x2="16" y2="3" stroke="oklch(0.6 0.03 220)" strokeWidth="1.2" strokeLinecap="round" />
    </svg>
  );
}

function TimeEditSheet({ label, value, onSave, onClose }) {
  // very lightweight: parse h:mm AM/PM
  const parse = (v) => {
    const m = v.match(/(\d{1,2}):(\d{2})\s*(AM|PM)/i);
    if (!m) return { h: 7, min: 30, period: 'AM' };
    return { h: parseInt(m[1]), min: parseInt(m[2]), period: m[3].toUpperCase() };
  };
  const [t, setT] = React.useState(parse(value));
  const bump = (field, delta) => {
    setT(prev => {
      const next = { ...prev };
      if (field === 'h') next.h = ((prev.h - 1 + delta + 12) % 12) + 1;
      if (field === 'min') next.min = (prev.min + delta + 60) % 60;
      if (field === 'period') next.period = prev.period === 'AM' ? 'PM' : 'AM';
      return next;
    });
  };
  const format = () => `${t.h}:${String(t.min).padStart(2, '0')} ${t.period}`;

  return (
    <div style={{
      position: 'fixed', inset: 0, zIndex: 200,
      background: 'rgba(0,0,0,0.5)', backdropFilter: 'blur(8px)',
      display: 'flex', alignItems: 'flex-end',
    }} onClick={onClose}>
      <div onClick={e => e.stopPropagation()} className="fade-in" style={{
        width: '100%', padding: '24px 24px 36px',
        background: 'oklch(0.18 0.04 245)',
        borderTop: '1px solid rgba(240,248,255,0.15)',
        borderRadius: '28px 28px 0 0',
      }}>
        <div style={{ width: 40, height: 4, borderRadius: 2, background: 'rgba(240,248,255,0.2)', margin: '0 auto 18px' }} />
        <div className="serif" style={{ fontSize: 22, color: 'oklch(0.96 0.015 220)', textAlign: 'center', marginBottom: 4 }}>
          {label}
        </div>
        <div className="ui" style={{ fontSize: 12, color: 'rgba(240,248,255,0.5)', textAlign: 'center', marginBottom: 24 }}>
          choose a time that feels natural
        </div>

        <div style={{ display: 'flex', justifyContent: 'center', gap: 6, alignItems: 'center', marginBottom: 28 }}>
          <TimeSpinner label="hr" value={t.h} onUp={() => bump('h', 1)} onDown={() => bump('h', -1)} />
          <div className="mono" style={{ fontSize: 38, color: 'oklch(0.96 0.015 220)', padding: '0 2px' }}>:</div>
          <TimeSpinner label="min" value={String(t.min).padStart(2, '0')} onUp={() => bump('min', 5)} onDown={() => bump('min', -5)} />
          <TimeSpinner label="" value={t.period} onUp={() => bump('period')} onDown={() => bump('period')} />
        </div>

        <div style={{ display: 'flex', gap: 10 }}>
          <PrimaryBtn variant="ghost" onClick={onClose}>Cancel</PrimaryBtn>
          <PrimaryBtn onClick={() => onSave(format())}>Save</PrimaryBtn>
        </div>
      </div>
    </div>
  );
}

function TimeSpinner({ label, value, onUp, onDown }) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 2 }}>
      <button onClick={onUp} className="ui"
        style={{
          width: 40, height: 28, borderRadius: 10,
          background: 'rgba(240,248,255,0.06)', border: '1px solid rgba(240,248,255,0.1)',
          color: 'rgba(240,248,255,0.6)', fontSize: 12, cursor: 'pointer',
        }}>▲</button>
      <div className="mono" style={{
        minWidth: 56, textAlign: 'center',
        fontSize: 34, color: 'oklch(0.96 0.015 220)',
        padding: '6px 10px',
      }}>{value}</div>
      <button onClick={onDown} className="ui"
        style={{
          width: 40, height: 28, borderRadius: 10,
          background: 'rgba(240,248,255,0.06)', border: '1px solid rgba(240,248,255,0.1)',
          color: 'rgba(240,248,255,0.6)', fontSize: 12, cursor: 'pointer',
        }}>▼</button>
    </div>
  );
}

function ConfirmSheet({ title, body, confirm, onConfirm, onCancel }) {
  return (
    <div style={{
      position: 'fixed', inset: 0, zIndex: 200,
      background: 'rgba(0,0,0,0.5)', backdropFilter: 'blur(8px)',
      display: 'flex', alignItems: 'flex-end',
    }} onClick={onCancel}>
      <div onClick={e => e.stopPropagation()} className="fade-in" style={{
        width: '100%', padding: '26px 26px 36px',
        background: 'oklch(0.18 0.04 245)',
        borderTop: '1px solid rgba(240,248,255,0.15)',
        borderRadius: '28px 28px 0 0',
      }}>
        <div style={{ width: 40, height: 4, borderRadius: 2, background: 'rgba(240,248,255,0.2)', margin: '0 auto 18px' }} />
        <div className="serif" style={{ fontSize: 24, color: 'oklch(0.96 0.015 220)', textAlign: 'center', marginBottom: 10 }}>
          {title}
        </div>
        <div className="serif" style={{
          fontSize: 16, color: 'rgba(240,248,255,0.65)', textAlign: 'center',
          marginBottom: 24, lineHeight: 1.4, fontStyle: 'italic',
        }}>
          {body}
        </div>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
          <button onClick={onConfirm} className="ui"
            style={{
              padding: '14px', borderRadius: 100,
              background: 'oklch(0.42 0.15 25 / 0.3)',
              border: '1px solid oklch(0.6 0.15 25 / 0.5)',
              color: 'oklch(0.85 0.12 25)', fontSize: 15, fontWeight: 600, cursor: 'pointer',
            }}>
            {confirm}
          </button>
          <PrimaryBtn variant="ghost" onClick={onCancel}>Keep everything</PrimaryBtn>
        </div>
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
