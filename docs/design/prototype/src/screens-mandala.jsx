// Mandala screen — dedicated tab for morning + evening resource/challenge entries per petal.
// Simplified: inline editor (no modal sheet), no "filled count", quieter artwork.

// Archive history comes from state.mandalaHistory — entries are flushed there at midnight
// (see resetForNewDay in app.jsx). Each entry: { date, key, phase, kind: 'resource'|'challenge', text }.
// The Mandala petals only reflect TODAY's entries (state.resources), so they reset visually at midnight.

function daysAgo(iso) {
  const then = new Date(iso);
  const today = new Date();
  then.setHours(0,0,0,0); today.setHours(0,0,0,0);
  return Math.round((today - then) / 86400000);
}

function formatAgo(days) {
  if (days === 0) return 'today';
  if (days === 1) return 'yesterday';
  if (days < 7) return days + ' days ago';
  if (days < 14) return 'last week';
  return Math.floor(days / 7) + ' weeks ago';
}

// per-resource hint of color for the archive; all in the warm/earth family so
// they harmonize with the mandala palette instead of fighting it.
const RESOURCE_TINT = {
  physical:      'oklch(0.78 0.09 38)',
  intellectual:  'oklch(0.8 0.07 220)',
  emotional:     'oklch(0.78 0.1 20)',
  sensory:       'oklch(0.82 0.09 140)',
  interactional: 'oklch(0.8 0.1 340)',
  nutritional:   'oklch(0.84 0.09 90)',
  contextual:    'oklch(0.76 0.05 260)',
  spiritual:     'oklch(0.85 0.08 75)',
};

function phaseForNow() {
  // Morning: 5:00 AM – 1:59 PM. Evening: 2:00 PM – 4:59 AM.
  const h = new Date().getHours();
  return (h >= 5 && h < 14) ? 'am' : 'pm';
}

function MandalaScreen({ state, setState }) {
  const [phase, setPhase] = React.useState(phaseForNow);
  // Re-check the clock whenever the screen re-renders AND on a 1-min interval,
  // so if the user leaves the screen open across the cutoff it flips automatically.
  React.useEffect(() => {
    const tick = () => setPhase(phaseForNow());
    tick();
    const id = setInterval(tick, 60 * 1000);
    return () => clearInterval(id);
  }, []);
  const [activeKey, setActiveKey] = React.useState(null);
  const [historyRange, setHistoryRange] = React.useState('week'); // week | month

  const entries = state.mandalaEntries || {};

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
      // binary: any entry (resource OR challenge) lights this phase
      const hasAny = !!next.resource.trim() || !!next.challenge.trim();
      const fill = hasAny ? 1 : 0;
      const resources = {
        ...s.resources,
        [key]: { ...s.resources[key], [phase]: fill },
      };
      return { ...s, mandalaEntries, resources };
    });
  };

  const active = activeKey ? SATIR_RESOURCES.find(r => r.key === activeKey) : null;

  return (
    <div className="fade-in" style={{ padding: '16px 20px 120px' }}>
      <ScreenHeader title="Mandala" />

      {/* Phase indicator — automatic, based on time of day */}
      <div style={{
        display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 10,
        padding: '11px 16px', borderRadius: 100,
        background: phase === 'am'
          ? 'oklch(0.82 0.07 210 / 0.12)'
          : 'oklch(0.86 0.09 78 / 0.12)',
        border: '1px solid ' + (phase === 'am'
          ? 'oklch(0.82 0.07 210 / 0.3)'
          : 'oklch(0.86 0.09 78 / 0.3)'),
        marginBottom: 18,
      }}>
        <span style={{
          fontSize: 16,
          color: phase === 'am' ? 'oklch(0.86 0.08 210)' : 'oklch(0.92 0.08 82)',
        }}>{phase === 'am' ? '◐' : '◑'}</span>
        <span className="serif" style={{
          fontSize: 18,
          color: phase === 'am' ? 'oklch(0.92 0.05 210)' : 'oklch(0.94 0.06 82)',
          letterSpacing: 0.1,
        }}>
          {phase === 'am' ? 'Morning' : 'Evening'}
        </span>
        <span className="ui" style={{
          fontSize: 10, fontStyle: 'italic',
          color: 'rgba(245,241,232,0.35)',
        }}>
          · {phase === 'am' ? 'until 2:00 pm' : 'from 2:00 pm'}
        </span>
      </div>

      {/* Mandala — display only, no tap */}
      <div style={{
        display: 'flex', flexDirection: 'column', alignItems: 'center',
        padding: '8px 0 4px',
      }}>
        <SatirMandala
          size={260}
          resources={state.resources}
          highlight={activeKey}
          showLabels={true}
        />
      </div>

      {/* Resource list — the actual input surface */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: 8, marginTop: 20 }}>
        {SATIR_RESOURCES.map(r => {
          const e = entries[r.key]?.[phase];
          const hasRes = !!e?.resource?.trim();
          const hasChal = !!e?.challenge?.trim();
          const isOpen = activeKey === r.key;
          return (
            <div key={r.key}>
              <button onClick={() => setActiveKey(isOpen ? null : r.key)} className="ui"
                style={{
                  width: '100%',
                  display: 'flex', alignItems: 'center', gap: 12,
                  padding: '12px 14px',
                  borderRadius: isOpen ? '16px 16px 0 0' : 16,
                  background: isOpen
                    ? 'oklch(0.86 0.09 78 / 0.14)'
                    : 'rgba(245,241,232,0.04)',
                  border: '1px solid ' + (isOpen
                    ? 'oklch(0.86 0.09 78 / 0.3)'
                    : 'rgba(245,241,232,0.08)'),
                  borderBottom: isOpen ? 'none' : undefined,
                  cursor: 'pointer', textAlign: 'left',
                }}>
                <PetalDot filled={hasRes || hasChal} accent={phase} />
                <div style={{ flex: 1, minWidth: 0 }}>
                  <div className="serif" style={{
                    fontSize: 17,
                    color: 'oklch(0.96 0.02 85)',
                    lineHeight: 1.2,
                  }}>
                    {r.label}
                  </div>
                  <div className="ui" style={{
                    fontSize: 11, color: 'rgba(245,241,232,0.5)',
                    marginTop: 2, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis',
                  }}>
                    {hasRes ? `✿ ${e.resource}` : (hasChal ? `◌ ${e.challenge}` : r.hint)}
                  </div>
                </div>
                <span style={{
                  color: 'rgba(245,241,232,0.3)', fontSize: 14,
                  transform: isOpen ? 'rotate(90deg)' : 'none',
                  transition: 'transform 0.2s',
                }}>›</span>
              </button>

              {isOpen && (
                <div className="fade-in" style={{
                  padding: '14px 14px 16px',
                  borderRadius: '0 0 16px 16px',
                  background: 'oklch(0.86 0.09 78 / 0.08)',
                  border: '1px solid oklch(0.86 0.09 78 / 0.3)',
                  borderTop: 'none',
                }}>
                  <InlineEditor
                    resource={r}
                    phase={phase}
                    entry={entries[r.key]?.[phase] || { resource: '', challenge: '' }}
                    onSave={(kind, text) => saveEntry(r.key, phase, kind, text)}
                    onClose={() => setActiveKey(null)}
                  />
                </div>
              )}
            </div>
          );
        })}
      </div>

      {/* History / archive — reads from state.mandalaHistory */}
      <MandalaHistory
        history={state.mandalaHistory || []}
        resources={SATIR_RESOURCES}
        range={historyRange}
        setRange={setHistoryRange}
      />
    </div>
  );
}

function MandalaHistory({ history, resources, range, setRange }) {
  const [archiveOpen, setArchiveOpen] = React.useState(false);
  const [openCategory, setOpenCategory] = React.useState(null); // resource key or null

  const limit = range === 'week' ? 7 : 30;
  // only entries within range; compute ago on the fly.
  const inRange = (history || [])
    .map(e => ({ ...e, ago: daysAgo(e.date) }))
    .filter(e => e.ago >= 0 && e.ago <= limit)
    // newest first
    .sort((a, b) => a.ago - b.ago);

  // group by resource key, then split into resources + challenges
  const grouped = {};
  resources.forEach(r => { grouped[r.key] = { resource: [], challenge: [] }; });
  inRange.forEach(e => {
    if (grouped[e.key] && grouped[e.key][e.kind]) {
      grouped[e.key][e.kind].push(e);
    }
  });

  return (
    <div style={{ marginTop: 30 }}>
      <div style={{
        height: 1,
        background: 'linear-gradient(90deg, transparent, rgba(245,241,232,0.14), transparent)',
        margin: '0 -4px 18px',
      }}></div>

      {/* Archive header — tap to expand whole section */}
      <button onClick={() => setArchiveOpen(v => !v)} className="ui"
        style={{
          width: '100%', padding: '4px 2px 6px',
          display: 'flex', alignItems: 'center', justifyContent: 'space-between',
          background: 'transparent', border: 'none', cursor: 'pointer',
          textAlign: 'left',
        }}>
        <div>
          <div className="ui" style={{
            fontSize: 10, fontWeight: 600, letterSpacing: 2, textTransform: 'uppercase',
            color: 'rgba(245,241,232,0.4)', marginBottom: 4,
          }}>Archive</div>
          <div className="serif" style={{
            fontSize: 22, color: 'oklch(0.96 0.02 85)',
            letterSpacing: -0.2,
          }}>What's been feeding you</div>
        </div>
        <span style={{
          color: 'rgba(245,241,232,0.5)', fontSize: 20,
          transform: archiveOpen ? 'rotate(90deg)' : 'none',
          transition: 'transform 0.2s',
          marginLeft: 8,
        }}>›</span>
      </button>

      {archiveOpen && (
        <div className="fade-in" style={{ marginTop: 14 }}>
          {/* 7d / 30d toggle */}
          <div style={{
            display: 'flex', justifyContent: 'flex-end', marginBottom: 12,
          }}>
            <div style={{
              display: 'flex', padding: 3, borderRadius: 100,
              background: 'rgba(245,241,232,0.04)',
              border: '1px solid rgba(245,241,232,0.08)',
            }}>
              {[{ k: 'week', l: '7d' }, { k: 'month', l: '30d' }].map(t => (
                <button key={t.k} onClick={() => setRange(t.k)} className="mono"
                  style={{
                    padding: '5px 11px', borderRadius: 100, border: 'none', cursor: 'pointer',
                    background: range === t.k ? 'rgba(245,241,232,0.12)' : 'transparent',
                    color: range === t.k ? 'oklch(0.96 0.02 85)' : 'rgba(245,241,232,0.5)',
                    fontSize: 11, letterSpacing: 0.5,
                  }}>{t.l}</button>
              ))}
            </div>
          </div>

          {/* Category rows */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
            {resources.map(r => {
              const bucket = grouped[r.key] || { resource: [], challenge: [] };
              const resCount = bucket.resource.length;
              const chalCount = bucket.challenge.length;
              const total = resCount + chalCount;
              const tint = RESOURCE_TINT[r.key];
              const isOpen = openCategory === r.key;
              return (
                <div key={r.key}>
                  <button onClick={() => setOpenCategory(isOpen ? null : r.key)} className="ui"
                    style={{
                      width: '100%',
                      display: 'flex', alignItems: 'center', gap: 12,
                      padding: '12px 14px',
                      borderRadius: isOpen ? '14px 14px 0 0' : 14,
                      background: isOpen
                        ? 'rgba(245,241,232,0.05)'
                        : 'rgba(245,241,232,0.03)',
                      border: '1px solid rgba(245,241,232,0.08)',
                      borderBottom: isOpen ? 'none' : undefined,
                      cursor: 'pointer', textAlign: 'left',
                    }}>
                    <span style={{
                      width: 9, height: 9, borderRadius: '50%',
                      background: tint,
                      opacity: total > 0 ? 0.9 : 0.3,
                      boxShadow: total > 0 ? `0 0 8px ${tint}55` : 'none',
                      flexShrink: 0,
                    }}></span>
                    <span className="serif" style={{
                      flex: 1,
                      fontSize: 20,
                      color: total > 0 ? 'oklch(0.96 0.02 85)' : 'rgba(245,241,232,0.55)',
                      letterSpacing: -0.1,
                    }}>{r.label}</span>
                    {total > 0 && (
                      <div style={{
                        display: 'flex', alignItems: 'center', gap: 6,
                      }}>
                        {resCount > 0 && (
                          <span className="ui" style={{
                            display: 'inline-flex', alignItems: 'center', gap: 3,
                            fontSize: 10,
                            color: 'oklch(0.88 0.09 78)',
                          }}>
                            <span style={{ fontSize: 11 }}>✿</span>
                            {resCount}
                          </span>
                        )}
                        {chalCount > 0 && (
                          <span className="ui" style={{
                            display: 'inline-flex', alignItems: 'center', gap: 3,
                            fontSize: 10,
                            color: 'oklch(0.72 0.11 30)',
                          }}>
                            <span style={{ fontSize: 11 }}>◌</span>
                            {chalCount}
                          </span>
                        )}
                      </div>
                    )}
                    {total === 0 && (
                      <span className="mono" style={{
                        fontSize: 11, color: 'rgba(245,241,232,0.35)',
                      }}>—</span>
                    )}
                    <span style={{
                      color: 'rgba(245,241,232,0.3)', fontSize: 13,
                      transform: isOpen ? 'rotate(90deg)' : 'none',
                      transition: 'transform 0.2s',
                    }}>›</span>
                  </button>

                  {isOpen && (
                    <div className="fade-in" style={{
                      padding: total === 0 ? '14px' : '12px 12px 14px',
                      borderRadius: '0 0 14px 14px',
                      background: 'rgba(245,241,232,0.025)',
                      border: '1px solid rgba(245,241,232,0.08)',
                      borderTop: 'none',
                    }}>
                      {total === 0 ? (
                        <div className="ui" style={{
                          fontSize: 12, fontStyle: 'italic',
                          color: 'rgba(245,241,232,0.4)',
                          textAlign: 'center', padding: '8px 0',
                        }}>
                          nothing noted here yet
                        </div>
                      ) : (
                        <>
                          {resCount > 0 && (
                            <ArchiveSubList
                              label="Good things"
                              icon="✿"
                              accent="oklch(0.88 0.09 78)"
                              entries={bucket.resource}
                            />
                          )}
                          {resCount > 0 && chalCount > 0 && (
                            <div style={{ height: 10 }} />
                          )}
                          {chalCount > 0 && (
                            <ArchiveSubList
                              label="Small aches"
                              icon="◌"
                              accent="oklch(0.72 0.11 30)"
                              entries={bucket.challenge}
                            />
                          )}
                        </>
                      )}
                    </div>
                  )}
                </div>
              );
            })}
          </div>

          <div className="ui" style={{
            marginTop: 14, textAlign: 'center',
            fontSize: 11, fontStyle: 'italic',
            color: 'rgba(245,241,232,0.35)',
          }}>
            {inRange.length} entr{inRange.length === 1 ? 'y' : 'ies'} · {range === 'week' ? 'past 7 days' : 'past 30 days'}
          </div>
        </div>
      )}
    </div>
  );
}

function ArchiveSubList({ label, icon, accent, entries }) {
  return (
    <div>
      <div style={{
        display: 'flex', alignItems: 'center', gap: 6, marginBottom: 6,
        padding: '0 4px',
      }}>
        <span style={{
          display: 'inline-flex', alignItems: 'center', justifyContent: 'center',
          width: 18, height: 18, borderRadius: '50%',
          background: `color-mix(in oklch, ${accent}, transparent 80%)`,
          color: accent, fontSize: 10,
        }}>{icon}</span>
        <span className="ui" style={{
          fontSize: 10, fontWeight: 600, letterSpacing: 1.4, textTransform: 'uppercase',
          color: accent,
        }}>{label}</span>
        <span className="mono" style={{
          fontSize: 10, color: 'rgba(245,241,232,0.35)',
          marginLeft: 'auto',
        }}>{entries.length}</span>
      </div>
      <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
        {entries.map((e, i) => (
          <div key={i} style={{
            padding: '9px 12px', borderRadius: 10,
            background: 'rgba(245,241,232,0.035)',
          }}>
            <div className="mono" style={{
              fontSize: 10, color: 'rgba(245,241,232,0.4)',
              letterSpacing: 0.5, marginBottom: 3,
            }}>
              {formatAgo(e.ago)} · {e.phase === 'am' ? 'morning' : 'evening'}
            </div>
            <div className="serif" style={{
              fontSize: 16, lineHeight: 1.35,
              color: 'oklch(0.94 0.03 85)',
            }}>{e.text}</div>
          </div>
        ))}
      </div>
    </div>
  );
}

function PetalDot({ filled, accent }) {
  const col = accent === 'am' ? 'oklch(0.78 0.08 215)' : 'oklch(0.86 0.09 78)';
  return (
    <div style={{
      width: 28, height: 28, borderRadius: '50%',
      background: filled
        ? `radial-gradient(circle at 32% 30%, ${col}, oklch(0.35 0.06 260))`
        : 'rgba(245,241,232,0.06)',
      border: '1px solid ' + (filled ? 'transparent' : 'rgba(245,241,232,0.12)'),
      flexShrink: 0,
      display: 'flex', alignItems: 'center', justifyContent: 'center',
      fontSize: 10, color: 'oklch(0.96 0.02 85)',
    }}>
      {filled && <span style={{ fontSize: 11 }}>✿</span>}
    </div>
  );
}

function InlineEditor({ resource, phase, entry, onSave, onClose }) {
  const [res, setRes] = React.useState(entry.resource || '');
  const [chal, setChal] = React.useState(entry.challenge || '');

  // sync when switching petals
  React.useEffect(() => {
    setRes(entry.resource || '');
    setChal(entry.challenge || '');
  }, [resource.key, phase]);

  return (
    <div>
      <div className="ui" style={{
        fontSize: 11, fontStyle: 'italic',
        color: 'rgba(245,241,232,0.55)', marginBottom: 12,
      }}>
        {resource.hint}
      </div>

      <FieldBlock
        label="One good thing"
        icon="✿"
        accent="oklch(0.88 0.09 78)"
        placeholder={placeholderFor(resource.key, 'resource', phase)}
        value={res}
        onChange={v => { setRes(v); onSave('resource', v); }}
      />

      <div style={{ height: 12 }} />

      <FieldBlock
        label="One small ache"
        sublabel="optional"
        icon="◌"
        accent="oklch(0.72 0.11 30)"
        placeholder={placeholderFor(resource.key, 'challenge', phase)}
        value={chal}
        onChange={v => { setChal(v); onSave('challenge', v); }}
      />

      <button onClick={onClose} className="ui"
        style={{
          marginTop: 14, width: '100%',
          padding: '11px 14px', borderRadius: 12, cursor: 'pointer', border: 'none',
          background: 'oklch(0.86 0.09 78 / 0.25)',
          color: 'oklch(0.94 0.06 82)',
          fontSize: 13, fontWeight: 600, letterSpacing: 0.3,
        }}>
        Done  ✿
      </button>
    </div>
  );
}

function FieldBlock({ label, sublabel, icon, accent, placeholder, value, onChange }) {
  return (
    <div>
      <div style={{
        display: 'flex', alignItems: 'center', gap: 8, marginBottom: 7,
      }}>
        <div style={{
          width: 20, height: 20, borderRadius: '50%',
          background: `color-mix(in oklch, ${accent}, transparent 80%)`,
          border: `1px solid color-mix(in oklch, ${accent}, transparent 55%)`,
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          color: accent, fontSize: 10,
        }}>{icon}</div>
        <span className="ui" style={{
          fontSize: 10, fontWeight: 600, letterSpacing: 1.6, textTransform: 'uppercase',
          color: accent,
        }}>{label}</span>
        {sublabel && (
          <span className="ui" style={{
            fontSize: 10, fontStyle: 'italic',
            color: 'rgba(245,241,232,0.4)',
          }}>· {sublabel}</span>
        )}
      </div>
      <textarea value={value} onChange={e => onChange(e.target.value)}
        placeholder={placeholder}
        rows={2}
        className="serif"
        style={{
          width: '100%', boxSizing: 'border-box',
          padding: '11px 13px', borderRadius: 12,
          background: 'rgba(245,241,232,0.05)',
          border: '1px solid rgba(245,241,232,0.12)',
          color: 'oklch(0.96 0.02 85)', fontSize: 15,
          lineHeight: 1.4, outline: 'none', resize: 'none',
          fontStyle: 'italic',
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

Object.assign(window, { MandalaScreen, MandalaHistory, ArchiveSubList });
