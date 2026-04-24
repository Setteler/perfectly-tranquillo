// Mandala screen — dedicated tab for morning + evening resource/challenge entries per petal.
function MandalaScreen({ state, setState }) {
  const [phase, setPhase] = React.useState(() => {
    const h = new Date().getHours();
    return h < 17 ? 'am' : 'pm';
  });
  const [activeKey, setActiveKey] = React.useState(null);

  const entries = state.mandalaEntries || {};
  const active = activeKey ? SATIR_RESOURCES.find(r => r.key === activeKey) : null;

  const saveEntry = (key, phase, kind, text) => {
    setState(s => {
      const prev = s.mandalaEntries?.[key]?.[phase] || { resource: '', challenge: '' };
      const next = { ...prev, [kind]: text };
      const mandalaEntries = {
        ...(s.mandalaEntries || {}),
        [key]: {
          ...(s.mandalaEntries?.[key] || {}),
          [phase]: next,
        },
      };
      // update petal fill: resource adds, challenge softens
      const hasRes = !!next.resource.trim();
      const hasChal = !!next.challenge.trim();
      const fill = hasRes ? (hasChal ? 0.6 : 0.9) : (hasChal ? 0.3 : 0);
      const resources = {
        ...s.resources,
        [key]: { ...s.resources[key], [phase]: fill },
      };
      return { ...s, mandalaEntries, resources };
    });
  };

  const filledCount = SATIR_RESOURCES.filter(r => {
    const e = entries[r.key]?.[phase];
    return e && (e.resource?.trim() || e.challenge?.trim());
  }).length;

  return (
    <div className="fade-in" style={{ padding: '16px 20px 120px' }}>
      <ScreenHeader eyebrow="Your inner landscape" title="Mandala" />

      <div className="ui" style={{
        fontSize: 13, lineHeight: 1.55, color: 'rgba(240,248,255,0.6)',
        margin: '4px 0 16px',
      }}>
        Eight resources of the self. Each morning and evening, tap a petal and name one good thing it's receiving — and, if you need to, one thing that's asking for care.
      </div>

      {/* AM/PM toggle */}
      <div style={{
        display: 'flex', padding: 4, borderRadius: 100,
        background: 'rgba(240,248,255,0.06)',
        border: '1px solid rgba(240,248,255,0.1)',
        marginBottom: 18,
      }}>
        {[
          { k: 'am', l: 'Morning', glyph: '◐' },
          { k: 'pm', l: 'Evening', glyph: '◑' },
        ].map(t => (
          <button key={t.k} onClick={() => setPhase(t.k)} className="ui"
            style={{
              flex: 1, padding: '10px 12px', borderRadius: 100, cursor: 'pointer', border: 'none',
              background: phase === t.k ? 'oklch(0.88 0.07 85 / 0.22)' : 'transparent',
              color: phase === t.k ? 'oklch(0.95 0.06 85)' : 'rgba(240,248,255,0.6)',
              fontSize: 13, fontWeight: 600, letterSpacing: 0.3,
              display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 8,
            }}>
            <span style={{ fontSize: 13 }}>{t.glyph}</span>
            {t.l}
          </button>
        ))}
      </div>

      {/* Mandala — tap petals */}
      <div style={{
        display: 'flex', flexDirection: 'column', alignItems: 'center',
        padding: '16px 0 8px', position: 'relative',
      }}>
        <MandalaTappable
          resources={state.resources}
          entries={entries}
          phase={phase}
          activeKey={activeKey}
          onTap={(k) => setActiveKey(k)}
          size={300}
        />
        <div className="ui" style={{
          fontSize: 11, color: 'rgba(240,248,255,0.45)',
          marginTop: 8, letterSpacing: 0.3,
        }}>
          tap any petal to tend it · <span className="mono" style={{ color: 'oklch(0.9 0.07 85)' }}>{filledCount}/8</span> filled
        </div>
      </div>

      {/* Resource list — quick overview */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: 8, marginTop: 18 }}>
        {SATIR_RESOURCES.map(r => {
          const e = entries[r.key]?.[phase];
          const hasRes = !!e?.resource?.trim();
          const hasChal = !!e?.challenge?.trim();
          return (
            <button key={r.key} onClick={() => setActiveKey(r.key)} className="ui"
              style={{
                display: 'flex', alignItems: 'center', gap: 12,
                padding: '12px 14px', borderRadius: 16,
                background: activeKey === r.key
                  ? 'oklch(0.78 0.08 215 / 0.14)'
                  : 'rgba(240,248,255,0.04)',
                border: '1px solid ' + (activeKey === r.key
                  ? 'oklch(0.78 0.08 215 / 0.35)'
                  : 'rgba(240,248,255,0.1)'),
                cursor: 'pointer', textAlign: 'left',
              }}>
              <PetalDot resourceKey={r.key} filled={hasRes || hasChal} phase={phase} />
              <div style={{ flex: 1, minWidth: 0 }}>
                <div className="serif" style={{ fontSize: 16, color: 'oklch(0.96 0.015 220)' }}>
                  {r.label}
                </div>
                <div className="ui" style={{
                  fontSize: 11, color: 'rgba(240,248,255,0.5)',
                  marginTop: 2, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis',
                }}>
                  {hasRes ? `✿ ${e.resource}` : (hasChal ? `◌ ${e.challenge}` : r.hint)}
                </div>
              </div>
              <span style={{ color: 'rgba(240,248,255,0.3)', fontSize: 14 }}>›</span>
            </button>
          );
        })}
      </div>

      {/* Petal sheet */}
      {active && (
        <PetalSheet
          resource={active}
          phase={phase}
          entry={entries[active.key]?.[phase] || { resource: '', challenge: '' }}
          onClose={() => setActiveKey(null)}
          onSave={(kind, text) => saveEntry(active.key, phase, kind, text)}
        />
      )}
    </div>
  );
}

function PetalDot({ resourceKey, filled, phase }) {
  const col = phase === 'am' ? 'oklch(0.78 0.08 215)' : 'oklch(0.86 0.05 75)';
  return (
    <div style={{
      width: 32, height: 32, borderRadius: '50%',
      background: filled
        ? `radial-gradient(circle at 32% 30%, ${col}, oklch(0.35 0.07 230))`
        : 'rgba(240,248,255,0.06)',
      border: '1px solid ' + (filled ? 'transparent' : 'rgba(240,248,255,0.15)'),
      flexShrink: 0,
      display: 'flex', alignItems: 'center', justifyContent: 'center',
      fontSize: 10, color: 'oklch(0.96 0.015 220)',
    }}>
      {filled && <span style={{ fontSize: 12 }}>✿</span>}
    </div>
  );
}

function MandalaTappable({ resources, entries, phase, activeKey, onTap, size }) {
  // Reuse the real mandala SVG but overlay invisible hit wedges
  const cx = size / 2, cy = size / 2;
  const outerR = size * 0.46;
  const innerR = size * 0.16;
  const slice = 360 / 8;
  return (
    <div style={{ position: 'relative', width: size, height: size }}>
      <SatirMandala size={size} resources={resources} highlight={activeKey} complexity="full" />
      <svg width={size} height={size} viewBox={`0 0 ${size} ${size}`}
        style={{ position: 'absolute', inset: 0 }}>
        {SATIR_RESOURCES.map((r, i) => {
          const a1 = i * slice;
          const a2 = (i + 1) * slice;
          return (
            <path key={r.key}
              d={wedgePath(cx, cy, innerR, outerR, a1, a2)}
              fill="transparent"
              style={{ cursor: 'pointer' }}
              onClick={() => onTap(r.key)} />
          );
        })}
        {/* labels */}
        {SATIR_RESOURCES.map((r, i) => {
          const a = i * slice + slice / 2;
          const rad = (a - 90) * Math.PI / 180;
          const lx = cx + Math.cos(rad) * (outerR + 18);
          const ly = cy + Math.sin(rad) * (outerR + 18);
          const e = entries[r.key]?.[phase];
          const on = !!(e?.resource?.trim() || e?.challenge?.trim());
          return (
            <text key={r.key} x={lx} y={ly}
              fontSize="9" fontFamily="'Inter', sans-serif" fontWeight="600"
              fill={on ? 'oklch(0.95 0.06 85)' : 'oklch(0.92 0.03 205)'}
              fillOpacity={on ? 0.95 : 0.55}
              textAnchor="middle" dominantBaseline="middle"
              letterSpacing="0.1em" style={{ textTransform: 'uppercase', pointerEvents: 'none' }}>
              {r.label}
            </text>
          );
        })}
      </svg>
    </div>
  );
}

function PetalSheet({ resource, phase, entry, onClose, onSave }) {
  const [res, setRes] = React.useState(entry.resource || '');
  const [chal, setChal] = React.useState(entry.challenge || '');

  React.useEffect(() => {
    setRes(entry.resource || '');
    setChal(entry.challenge || '');
  }, [resource.key, phase]);

  const phaseLabel = phase === 'am' ? 'This morning' : 'This evening';
  const accent = phase === 'am' ? 'oklch(0.78 0.08 215)' : 'oklch(0.86 0.05 75)';

  return (
    <div onClick={onClose} style={{
      position: 'absolute', inset: 0, zIndex: 40,
      background: 'oklch(0.16 0.04 245 / 0.72)',
      backdropFilter: 'blur(10px)',
      display: 'flex', alignItems: 'flex-end',
      animation: 'fade 0.2s',
    }} className="fade-in">
      <div onClick={e => e.stopPropagation()} style={{
        width: '100%',
        background: 'linear-gradient(180deg, oklch(0.26 0.05 235) 0%, oklch(0.2 0.05 240) 100%)',
        borderTopLeftRadius: 28, borderTopRightRadius: 28,
        borderTop: '1px solid rgba(240,248,255,0.14)',
        padding: '20px 22px 24px',
        maxHeight: '75%', overflow: 'auto',
        boxShadow: '0 -20px 60px rgba(0,0,0,0.5)',
      }}>
        {/* handle */}
        <div style={{
          width: 44, height: 4, borderRadius: 100,
          background: 'rgba(240,248,255,0.2)',
          margin: '0 auto 18px',
        }} />

        <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 4 }}>
          <div style={{
            width: 34, height: 34, borderRadius: '50%',
            background: `radial-gradient(circle at 30% 28%, ${accent}, oklch(0.35 0.07 230))`,
            boxShadow: `0 0 16px ${accent}40`,
          }} />
          <div style={{ flex: 1 }}>
            <div className="ui" style={{
              fontSize: 9, fontWeight: 600, letterSpacing: 1.6, textTransform: 'uppercase',
              color: accent,
            }}>{phaseLabel}</div>
            <div className="serif" style={{ fontSize: 24, color: 'oklch(0.96 0.015 220)', lineHeight: 1 }}>
              {resource.label}
            </div>
          </div>
          <button onClick={onClose} className="ui" style={{
            width: 32, height: 32, borderRadius: '50%', border: 'none',
            background: 'rgba(240,248,255,0.08)',
            color: 'rgba(240,248,255,0.6)', fontSize: 18, cursor: 'pointer',
          }}>×</button>
        </div>
        <div className="ui" style={{
          fontSize: 12, color: 'rgba(240,248,255,0.55)', marginBottom: 18,
          fontStyle: 'italic',
        }}>
          {resource.hint}
        </div>

        {/* Resource field */}
        <FieldBlock
          label="Resource"
          icon="✿"
          accent={accent}
          placeholder={placeholderFor(resource.key, 'resource', phase)}
          value={res}
          onChange={v => { setRes(v); onSave('resource', v); }}
        />

        <div style={{ height: 14 }} />

        {/* Challenge field */}
        <FieldBlock
          label="Challenge"
          sublabel="optional"
          icon="◌"
          accent="oklch(0.78 0.1 25)"
          placeholder={placeholderFor(resource.key, 'challenge', phase)}
          value={chal}
          onChange={v => { setChal(v); onSave('challenge', v); }}
        />

        <div style={{ marginTop: 18 }}>
          <PrimaryBtn variant="sand" onClick={onClose}>
            Save this petal  ✿
          </PrimaryBtn>
        </div>
      </div>
    </div>
  );
}

function FieldBlock({ label, sublabel, icon, accent, placeholder, value, onChange }) {
  return (
    <div>
      <div style={{
        display: 'flex', alignItems: 'center', gap: 8, marginBottom: 8,
      }}>
        <div style={{
          width: 22, height: 22, borderRadius: '50%',
          background: `${accent}22`, border: `1px solid ${accent}50`,
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          color: accent, fontSize: 11,
        }}>{icon}</div>
        <span className="ui" style={{
          fontSize: 10, fontWeight: 600, letterSpacing: 1.8, textTransform: 'uppercase',
          color: accent,
        }}>{label}</span>
        {sublabel && (
          <span className="ui" style={{
            fontSize: 10, fontStyle: 'italic',
            color: 'rgba(240,248,255,0.4)',
          }}>· {sublabel}</span>
        )}
      </div>
      <textarea value={value} onChange={e => onChange(e.target.value)}
        placeholder={placeholder}
        rows={2}
        className="serif"
        style={{
          width: '100%', boxSizing: 'border-box',
          padding: '12px 14px', borderRadius: 14,
          background: 'rgba(240,248,255,0.04)',
          border: '1px solid rgba(240,248,255,0.12)',
          color: 'oklch(0.96 0.015 220)', fontSize: 15,
          lineHeight: 1.4, outline: 'none', resize: 'none',
        }} />
    </div>
  );
}

function placeholderFor(key, kind, phase) {
  if (kind === 'resource') {
    const am = {
      physical: 'slept deep, woke without alarm…',
      intellectual: 'a book waiting on the nightstand…',
      emotional: 'feeling soft, patient with myself…',
      sensory: 'cool air from the window…',
      interactional: 'a good morning text from…',
      nutritional: 'proper coffee, no rushing…',
      contextual: 'desk is tidy, room feels open…',
      spiritual: 'a reason to be glad it\'s today…',
    };
    const pm = {
      physical: 'body feels worked but not wrecked…',
      intellectual: 'learned one small thing about…',
      emotional: 'had a real laugh with…',
      sensory: 'the sunset over the rooftops…',
      interactional: 'honest conversation with…',
      nutritional: 'cooked something, ate slowly…',
      contextual: 'home is soft tonight…',
      spiritual: 'something felt meaningful when…',
    };
    return (phase === 'am' ? am : pm)[key] || 'one good thing…';
  }
  // challenge
  const c = {
    physical: 'tight shoulders, not enough rest…',
    intellectual: 'brain feels foggy, scattered…',
    emotional: 'feeling thin, touchy…',
    sensory: 'too much screen, not enough outside…',
    interactional: 'lonely corner of the day…',
    nutritional: 'ate past when I was hungry…',
    contextual: 'space feels cluttered, loud…',
    spiritual: 'everything feels flat today…',
  };
  return c[key] || 'one small ache…';
}

Object.assign(window, { MandalaScreen });
