// Progress / Garden — jar of seashells + resource averages
function ProgressScreen({ state, setState, go }) {
  // Map stone kinds → seashell colors (each "collected ritual" = a shell)
  const KIND_TO_COLOR = {
    moon: 'pearl',   // mornings
    sand: 'butter',  // evenings
    jade: 'moss',    // habit completions
    coral: 'coral',  // breaks
    deep: 'sky',     // focus
  };
  const KIND_META = {
    moon:  { color: 'pearl',  label: 'mornings' },
    sand:  { color: 'butter', label: 'evenings' },
    jade:  { color: 'moss',   label: 'habits'   },
    coral: { color: 'coral',  label: 'breaks'   },
    deep:  { color: 'sky',    label: 'focus'    },
  };

  // Build the shell list from collected stones + seed with enough demo shells
  // so the jar looks inhabited on first open.
  const baseShells = state.stones.map(s => ({ color: KIND_TO_COLOR[s.kind] || 'pearl' }));
  const seedDemo = [
    'pearl','butter','moss','pearl','coral','sky','pearl','moss',
    'butter','pearl','coral','sky','pearl','moss','butter','pearl',
    'coral','pearl','moss','butter','pearl','sky','coral','pearl',
    'moss','butter','pearl','coral','sky','pearl','butter','moss',
  ].map(color => ({ color }));
  const shells = [...seedDemo, ...baseShells];

  // Counts for the legend (combine demo + real)
  const counts = shells.reduce((acc, s) => {
    acc[s.color] = (acc[s.color] || 0) + 1;
    return acc;
  }, {});

  return (
    <div className="fade-in" style={{ padding: '16px 20px 120px' }}>
      <ScreenHeader eyebrow="12 days tending" title="Your quiet garden" />

      {/* Jar of seashells */}
      <Card style={{ marginBottom: 14, paddingBottom: 18 }}>
        <div className="ui" style={{
          fontSize: 10, fontWeight: 600, letterSpacing: 1.6, textTransform: 'uppercase',
          color: 'rgba(240,248,255,0.45)', marginBottom: 6,
        }}>Shells collected</div>
        <div className="serif" style={{
          fontSize: 15, color: 'rgba(240,248,255,0.6)', marginBottom: 14, fontStyle: 'italic',
        }}>
          one shell for each ritual you kept.
        </div>

        <div style={{ display: 'flex', justifyContent: 'center' }}>
          <Jar shells={shells} width={300} height={320} />
        </div>

        <div style={{ display: 'flex', gap: 14, justifyContent: 'center', flexWrap: 'wrap', marginTop: 8 }}>
          {Object.entries(KIND_META).map(([kind, m]) => (
            <ShellLegend key={kind} color={m.color} label={m.label} count={counts[m.color] || 0} />
          ))}
        </div>
      </Card>

      {/* Resource averages */}
      <Card>
        <div className="ui" style={{
          fontSize: 10, fontWeight: 600, letterSpacing: 1.6, textTransform: 'uppercase',
          color: 'rgba(240,248,255,0.45)', marginBottom: 14,
        }}>Resources · 7 day average</div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
          {SATIR_RESOURCES.map((r, i) => {
            const avg = 0.3 + (i * 0.08) % 0.55;
            return (
              <div key={r.key} style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                <div className="ui" style={{
                  fontSize: 11, width: 78, color: 'rgba(240,248,255,0.7)',
                  letterSpacing: 0.4,
                }}>
                  {r.label}
                </div>
                <div style={{
                  flex: 1, height: 4, borderRadius: 2,
                  background: 'rgba(240,248,255,0.06)', overflow: 'hidden',
                }}>
                  <div style={{
                    width: `${avg * 100}%`, height: '100%',
                    background: 'linear-gradient(90deg, oklch(0.62 0.09 220), oklch(0.82 0.08 215))',
                    borderRadius: 2,
                  }} />
                </div>
                <div className="mono" style={{
                  fontSize: 10, color: 'rgba(240,248,255,0.45)',
                  width: 28, textAlign: 'right',
                }}>
                  {Math.round(avg * 100)}
                </div>
              </div>
            );
          })}
        </div>
      </Card>
    </div>
  );
}

function ShellLegend({ color, label, count }) {
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
      <Seashell color={color} size={16} seed={color.charCodeAt(0)} rotate={0} />
      <span className="ui" style={{ fontSize: 11, color: 'rgba(240,248,255,0.7)' }}>{label}</span>
      <span className="mono" style={{ fontSize: 10, color: 'rgba(240,248,255,0.4)' }}>{count}</span>
    </div>
  );
}

Object.assign(window, { ProgressScreen, ShellLegend });
