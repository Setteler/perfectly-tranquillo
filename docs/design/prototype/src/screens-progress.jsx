// Progress / Garden — week grid, stones, streaks
function ProgressScreen({ state, setState, go }) {
  // build a fake 7-day view
  const days = [
    { d: 'M', v: 0.55, done: true },
    { d: 'T', v: 0.7,  done: true },
    { d: 'W', v: 0.4,  done: true },
    { d: 'T', v: 0.85, done: true },
    { d: 'F', v: 0.6,  done: true },
    { d: 'S', v: 0.3,  done: true },
    { d: 'S', v: (() => {
      const v = Object.values(state.resources);
      return v.reduce((a,r) => a + r.am + r.pm, 0) / (v.length * 2);
    })(), done: false },
  ];

  const stoneCounts = state.stones.reduce((acc, s) => {
    acc[s.kind] = (acc[s.kind] || 0) + 1;
    return acc;
  }, { moon: 8, sand: 3, coral: 2, jade: 5, deep: 1 });

  return (
    <div className="fade-in" style={{ padding: '16px 20px 120px' }}>
      <ScreenHeader eyebrow="12 days tending" title="Your quiet garden" />

      {/* Week strip of mini mandalas */}
      <div style={{
        padding: 18, borderRadius: 22,
        background: 'rgba(240,248,255,0.04)',
        border: '1px solid rgba(240,248,255,0.1)',
        marginBottom: 14,
      }}>
        <div className="ui" style={{
          fontSize: 10, fontWeight: 600, letterSpacing: 1.6, textTransform: 'uppercase',
          color: 'rgba(240,248,255,0.45)', marginBottom: 14,
        }}>This week</div>
        <div style={{ display: 'flex', gap: 4, justifyContent: 'space-between' }}>
          {days.map((day, i) => (
            <div key={i} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 6, flex: 1 }}>
              <div style={{ opacity: day.done ? 1 : 0.4 }}>
                <MiniMandala size={36} fill={day.v} />
              </div>
              <div className="mono" style={{
                fontSize: 10, letterSpacing: 0.4,
                color: i === 6 ? 'oklch(0.86 0.05 75)' : 'rgba(240,248,255,0.5)',
                fontWeight: i === 6 ? 600 : 400,
              }}>
                {day.d}
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Stones collected */}
      <Card style={{ marginBottom: 14 }}>
        <div className="ui" style={{
          fontSize: 10, fontWeight: 600, letterSpacing: 1.6, textTransform: 'uppercase',
          color: 'rgba(240,248,255,0.45)', marginBottom: 14,
        }}>Stones collected</div>

        {/* Scatter of stones */}
        <div style={{
          position: 'relative', height: 110,
          background: 'radial-gradient(ellipse at 50% 80%, oklch(0.3 0.06 230 / 0.4), transparent 70%)',
          borderRadius: 16, overflow: 'hidden',
        }}>
          {[
            { x: 14, y: 70, c: 'moon', s: 22 },
            { x: 40, y: 84, c: 'sand', s: 18 },
            { x: 68, y: 76, c: 'jade', s: 20 },
            { x: 96, y: 88, c: 'moon', s: 16 },
            { x: 122, y: 74, c: 'coral', s: 24 },
            { x: 152, y: 84, c: 'deep', s: 18 },
            { x: 180, y: 78, c: 'moon', s: 20 },
            { x: 208, y: 86, c: 'jade', s: 18 },
            { x: 234, y: 74, c: 'sand', s: 22 },
            { x: 262, y: 82, c: 'moon', s: 16 },
            { x: 290, y: 78, c: 'coral', s: 20 },
            { x: 22, y: 44, c: 'jade', s: 14 },
            { x: 62, y: 36, c: 'moon', s: 12 },
            { x: 148, y: 30, c: 'deep', s: 12 },
            { x: 212, y: 40, c: 'sand', s: 14 },
            { x: 272, y: 44, c: 'moon', s: 12 },
          ].map((s, i) => (
            <div key={i} style={{ position: 'absolute', left: s.x, top: s.y }}>
              <Stone color={s.c} size={s.s} />
            </div>
          ))}
        </div>

        <div style={{ display: 'flex', gap: 14, marginTop: 16, flexWrap: 'wrap' }}>
          <StoneLegend color="moon"  label="mornings"   count={stoneCounts.moon} />
          <StoneLegend color="jade"  label="habits"     count={stoneCounts.jade} />
          <StoneLegend color="sand"  label="evenings"   count={stoneCounts.sand} />
          <StoneLegend color="coral" label="breaks"     count={stoneCounts.coral} />
          <StoneLegend color="deep"  label="focus"      count={stoneCounts.deep} />
        </div>
      </Card>

      {/* Resource averages */}
      <Card>
        <div className="ui" style={{
          fontSize: 10, fontWeight: 600, letterSpacing: 1.6, textTransform: 'uppercase',
          color: 'rgba(240,248,255,0.45)', marginBottom: 14,
        }}>Resources · 7 day average</div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
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

function StoneLegend({ color, label, count }) {
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
      <Stone color={color} size={14} />
      <span className="ui" style={{ fontSize: 11, color: 'rgba(240,248,255,0.7)' }}>{label}</span>
      <span className="mono" style={{ fontSize: 10, color: 'rgba(240,248,255,0.4)' }}>{count}</span>
    </div>
  );
}

Object.assign(window, { ProgressScreen, StoneLegend });
