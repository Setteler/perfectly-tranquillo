// Morning — inspiring message → optional "one good thing" → straight to mandala/habits
const INSPIRATIONS = [
  { a: "Today begins slowly.", b: "You don't have to hurry." },
  { a: "The tide comes in.", b: "The tide goes out. You are here." },
  { a: "Small things, often.", b: "That is the whole practice." },
  { a: "You woke up. That's already something.", b: "" },
  { a: "Let the day unfold.", b: "You don't have to push it." },
  { a: "Be gentle with the one you are becoming.", b: "" },
  { a: "One breath at a time.", b: "That's the only pace there is." },
];

function MorningScreen({ state, setState, onDone }) {
  const [step, setStep] = React.useState(0);
  const [draftGood, setDraftGood] = React.useState(state.goodThing || '');
  const insp = React.useMemo(() => INSPIRATIONS[Math.floor(Math.random() * INSPIRATIONS.length)], []);

  const finishAndGoHabits = () => {
    setState(s => ({
      ...s,
      goodThing: draftGood || s.goodThing,
      morningDone: true,
      resources: {
        ...s.resources,
        emotional: { ...s.resources.emotional, am: Math.max(0.55, s.resources.emotional.am) },
        spiritual: { ...s.resources.spiritual, am: Math.max(0.6, s.resources.spiritual.am) },
        intellectual: { ...s.resources.intellectual, am: draftGood ? 0.7 : s.resources.intellectual.am },
      },
      stones: [...s.stones, { kind: 'moon', label: 'morning', when: Date.now() }],
    }));
    onDone();
  };

  // Step 0 — soft welcome + inspiration
  if (step === 0) {
    return (
      <div className="fade-in" style={{
        padding: '40px 28px 40px', minHeight: '100%',
        display: 'flex', flexDirection: 'column',
      }}>
        <div className="ui" style={{
          fontSize: 10, fontWeight: 600, letterSpacing: 2, textTransform: 'uppercase',
          color: 'oklch(0.88 0.07 85)', marginBottom: 28,
        }}>Tuesday morning · 6:42</div>

        {/* Sun + wave art */}
        <div style={{
          display: 'flex', justifyContent: 'center', margin: '20px 0 36px',
          animation: 'float-slow 8s ease-in-out infinite',
        }}>
          <svg width="180" height="180" viewBox="0 0 200 200">
            <defs>
              <radialGradient id="sun-rise" cx="50%" cy="50%">
                <stop offset="0%"  stopColor="oklch(0.95 0.08 85)" />
                <stop offset="70%" stopColor="oklch(0.85 0.1 70)" stopOpacity="0.9" />
                <stop offset="100%" stopColor="oklch(0.75 0.1 60)" stopOpacity="0" />
              </radialGradient>
            </defs>
            <circle cx="100" cy="100" r="90" fill="url(#sun-rise)" />
            <circle cx="100" cy="100" r="42" fill="oklch(0.93 0.08 80)" opacity="0.95" />
            {/* rays */}
            {Array.from({ length: 12 }).map((_, i) => {
              const a = (i * 30) * Math.PI / 180;
              return (
                <line key={i}
                  x1={100 + 54 * Math.cos(a)} y1={100 + 54 * Math.sin(a)}
                  x2={100 + 72 * Math.cos(a)} y2={100 + 72 * Math.sin(a)}
                  stroke="oklch(0.9 0.08 80)" strokeWidth="1.6" strokeLinecap="round" opacity="0.7" />
              );
            })}
            {/* waves */}
            <path d="M 10 150 Q 40 140 70 150 T 130 150 T 190 150"
              stroke="oklch(0.82 0.09 210)" strokeWidth="2" fill="none" strokeLinecap="round" />
            <path d="M 10 165 Q 40 157 70 165 T 130 165 T 190 165"
              stroke="oklch(0.75 0.1 215)" strokeWidth="2" fill="none" strokeLinecap="round" opacity="0.7" />
            <path d="M 10 180 Q 40 173 70 180 T 130 180 T 190 180"
              stroke="oklch(0.7 0.1 220)" strokeWidth="2" fill="none" strokeLinecap="round" opacity="0.5" />
          </svg>
        </div>

        <div className="serif" style={{
          fontSize: 32, lineHeight: 1.15,
          textAlign: 'center',
          color: 'oklch(0.96 0.04 85)',
          marginBottom: insp.b ? 10 : 32,
        }}>
          {insp.a}
        </div>
        {insp.b && (
          <div className="serif" style={{
            fontSize: 22, lineHeight: 1.3,
            textAlign: 'center',
            color: 'oklch(0.92 0.03 200 / 0.8)',
            marginBottom: 32,
          }}>
            {insp.b}
          </div>
        )}

        <div className="ui" style={{
          textAlign: 'center', fontSize: 12, color: 'rgba(240,248,255,0.55)',
          marginBottom: 28, letterSpacing: 0.3,
        }}>
          take a slow breath · no rush
        </div>

        <div style={{ flex: 1 }} />

        <PrimaryBtn onClick={() => setStep(1)}>
          Good morning  ◦
        </PrimaryBtn>
      </div>
    );
  }

  // Step 1 — optional "one good thing ahead"
  if (step === 1) {
    return (
      <div className="fade-in" style={{ padding: '28px 24px 40px', minHeight: '100%' }}>
        <div className="ui" style={{
          fontSize: 10, fontWeight: 600, letterSpacing: 2, textTransform: 'uppercase',
          color: 'oklch(0.88 0.07 85)', marginBottom: 18,
        }}>A small anticipation</div>

        <div className="serif" style={{ fontSize: 32, lineHeight: 1.1, color: 'oklch(0.96 0.04 85)', marginBottom: 8 }}>
          One good thing ahead today?
        </div>
        <div className="ui" style={{ fontSize: 13, lineHeight: 1.5, color: 'rgba(240,248,255,0.6)', marginBottom: 22 }}>
          Even a cup of tea counts. Skip if you like.
        </div>

        <textarea
          value={draftGood}
          onChange={e => setDraftGood(e.target.value)}
          placeholder="Today I'm looking forward to…"
          className="serif"
          style={{
            width: '100%', minHeight: 120, padding: 18, borderRadius: 20,
            background: 'rgba(240,248,255,0.05)',
            border: '1px solid rgba(240,248,255,0.14)',
            color: 'oklch(0.96 0.03 200)',
            fontSize: 22, lineHeight: 1.4, outline: 'none',
            resize: 'none', boxSizing: 'border-box',
            marginBottom: 14,
          }}
        />

        <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8, marginBottom: 28 }}>
          {['the walk to the café', 'lunch outside', 'finishing one thing', 'a long shower'].map(s => (
            <Chip key={s} onClick={() => setDraftGood(s)}>{s}</Chip>
          ))}
        </div>

        <PrimaryBtn variant="sand" onClick={finishAndGoHabits}>
          {draftGood.trim() ? 'Begin the day  ◦' : 'Skip & begin  ◦'}
        </PrimaryBtn>

        <div className="ui" style={{
          marginTop: 18, fontSize: 11, textAlign: 'center',
          color: 'rgba(240,248,255,0.4)', letterSpacing: 0.3,
        }}>
          you'll collect a <span style={{ color: 'oklch(0.9 0.07 85)' }}>moon stone</span> for this morning
        </div>
      </div>
    );
  }
}
Object.assign(window, { MorningScreen });
