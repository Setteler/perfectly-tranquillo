// App — root, state, routing, tweaks, music
const { useState, useEffect, useRef } = React;

const DEFAULT_HABITS = [
  { id: 'no-phone',  label: "Don't open phone first thing", hint: '15 minutes past waking', done: false, streak: 4,  remindAt: '07:00' },
  { id: 'water',     label: 'Drink water',                   hint: 'First glass by 10am',   done: true,  streak: 7,  remindAt: '09:30' },
  { id: 'gratitude', label: 'Three good things ahead',       hint: 'Any time in morning',   done: true,  streak: 11, remindAt: '08:15' },
  { id: 'break',     label: 'Take a mindful break',          hint: 'Between tasks',         done: false, streak: 2,  remindAt: '14:00' },
  { id: 'workout',   label: 'Move your body',                hint: '20 minutes, any kind',  done: false, streak: 3,  remindAt: '17:30' },
  { id: 'eat',       label: 'Eat a real meal',               hint: 'Slow, unhurried',       done: false, streak: 5,  remindAt: '' },
  { id: 'sleep',     label: 'Sleep by 11:00',                hint: 'Phone away by 10:30',   done: false, streak: 8,  remindAt: '22:30' },
];

const DEFAULT_RESOURCES = {
  physical:      { am: 0.4, pm: 0.2 },
  intellectual:  { am: 0.5, pm: 0.1 },
  emotional:     { am: 0.6, pm: 0.15 },
  sensory:       { am: 0.3, pm: 0.1 },
  interactional: { am: 0.35, pm: 0.05 },
  nutritional:   { am: 0.55, pm: 0.25 },
  contextual:    { am: 0.45, pm: 0.1 },
  spiritual:     { am: 0.65, pm: 0.2 },
};

const INITIAL_STATE = {
  name: 'Sofía',
  morningDone: true,
  eveningDone: false,
  goodThing: "the first sip of coffee on the balcony",
  intent: 'Gentle focus',
  morningMood: 'bright',
  habits: DEFAULT_HABITS,
  weeklyHabits: [
    { id: 'therapy',    label: 'Therapy session',    hint: 'Mondays, 6pm',      day: 1, done: true,  streak: 6, remindAt: '17:45' },
    { id: 'long-walk',  label: 'A long slow walk',   hint: 'Tuesdays, outside', day: 2, done: false, streak: 3, remindAt: '16:00' },
    { id: 'call-mom',   label: 'Call mom',           hint: 'Tuesdays',          day: 2, done: false, streak: 4, remindAt: '19:00' },
    { id: 'deep-clean', label: 'Tidy one room',      hint: 'Wednesdays',        day: 3, done: false, streak: 2, remindAt: '' },
    { id: 'read',       label: 'Read, just for me',  hint: 'Thursday evenings', day: 4, done: false, streak: 1, remindAt: '21:00' },
    { id: 'meal-prep',  label: 'Meal prep',          hint: 'Sundays, midday',   day: 0, done: false, streak: 5, remindAt: '' },
    { id: 'date-night', label: 'Date night',         hint: 'Friday evenings',   day: 5, done: false, streak: 2, remindAt: '19:30' },
  ],
  resources: DEFAULT_RESOURCES,
  mandalaEntries: {
    emotional:  { am: { resource: 'woke up calm for the first time this week', challenge: '' } },
    nutritional:{ am: { resource: 'coffee and a real breakfast, slowly', challenge: '' } },
    spiritual:  { am: { resource: 'looking forward to the sun on the balcony', challenge: '' } },
  },
  stones: [
    { kind: 'moon', label: 'morning' },
    { kind: 'jade', label: 'water' },
    { kind: 'jade', label: 'gratitude' },
    { kind: 'moon', label: 'breath' },
  ],
  sound: true,
  font: 'fraunces',
  complexity: 'full',
};

const TWEAK_DEFAULTS = /*EDITMODE-BEGIN*/{
  "font": "fraunces",
  "music": true,
  "musicVolume": 55,
  "warmth": 65,
  "sound": true
}/*EDITMODE-END*/;

const ambient = makeAmbient();

function App() {
  const [route, setRoute] = useState('home');
  const [state, setState] = useState(INITIAL_STATE);
  const [tweaks, setTweak] = useTweaks(TWEAK_DEFAULTS);
  const [musicOn, setMusicOn] = useState(false);
  const [musicGreeting, setMusicGreeting] = useState(true);
  const [notif, setNotif] = useState(null); // {habitLabel, hint, id} or null

  useEffect(() => { setState(s => ({ ...s, font: tweaks.font, sound: tweaks.sound })); }, [tweaks.font, tweaks.sound]);

  const toggleMusic = () => {
    if (musicOn) { ambient.stop(); setMusicOn(false); }
    else { ambient.start(); setMusicOn(true); setMusicGreeting(false); }
  };
  useEffect(() => { if (!tweaks.music && musicOn) { ambient.stop(); setMusicOn(false); } }, [tweaks.music]);
  useEffect(() => { if (musicOn) ambient.setVolume((tweaks.musicVolume || 50) / 100); }, [tweaks.musicVolume, musicOn]);

  // fake notification demo — fire one 7 seconds after load for an unticked habit with a reminder
  useEffect(() => {
    const t = setTimeout(() => {
      const pick = state.habits.find(h => !h.done && h.remindAt);
      if (pick) setNotif({ habitLabel: pick.label, hint: pick.remindAt, id: pick.id });
    }, 8000);
    return () => clearTimeout(t);
  }, []);

  const triggerDemoNotif = () => {
    const pick = state.habits.find(h => !h.done && h.remindAt)
              || state.habits[0];
    setNotif({ habitLabel: pick.label, hint: pick.remindAt || 'now', id: pick.id });
  };

  const themeClass =
    tweaks.font === 'instrument' ? 'theme-instrument' :
    tweaks.font === 'caveat' ? 'theme-caveat' :
    tweaks.font === 'cormorant' ? 'theme-cormorant' : '';

  const warmth = (tweaks.warmth || 65) / 100;

  const renderScreen = () => {
    switch (route) {
      case 'morning':  return <MorningScreen state={state} setState={setState} onDone={() => setRoute('home')} />;
      case 'evening':  return <EveningScreen state={state} setState={setState} onBack={() => setRoute('home')} />;
      case 'home':     return <HomeScreen state={state} setState={setState} go={setRoute} />;
      case 'habits':   return <HabitsScreen state={state} setState={setState} />;
      case 'mandala':  return <MandalaScreen state={state} setState={setState} />;
      case 'breath':   return <BreathScreen state={state} setState={setState} onBack={() => setRoute('home')} />;
      case 'focus':    return <FocusScreen state={state} setState={setState} onBack={() => setRoute('home')} />;
      case 'break':    return <BreakScreen state={state} setState={setState} onBack={() => setRoute('home')} />;
      case 'progress': return <ProgressScreen state={state} setState={setState} go={setRoute} />;
      case 'settings': return <SettingsScreen state={state} setState={setState} onBack={() => setRoute('home')} triggerDemoNotif={triggerDemoNotif} />;
      default: return null;
    }
  };

  const showTabs = ['home', 'habits', 'mandala', 'progress'].includes(route);
  const tabMap = { home: 'home', habits: 'habits', mandala: 'mandala', progress: 'progress' };

  const pageBg = `
    radial-gradient(ellipse at 85% 95%, oklch(0.82 0.09 80 / ${0.15 + warmth * 0.35}) 0%, transparent 55%),
    radial-gradient(ellipse at 10% 10%, oklch(0.7 0.12 210 / ${0.9 - warmth * 0.35}) 0%, transparent 50%),
    radial-gradient(ellipse at 80% 30%, oklch(0.58 0.13 215 / ${0.85 - warmth * 0.3}) 0%, transparent 55%),
    linear-gradient(180deg, oklch(${0.4 + warmth * 0.05} 0.09 ${220 - warmth * 20}) 0%, oklch(0.28 0.07 225) 50%, oklch(0.22 0.06 230) 100%)
  `;

  // Nav model reminder for the designer (visible once in light text under the device)
  return (
    <div className={themeClass} style={{
      minHeight: '100vh', width: '100%',
      background: pageBg,
      display: 'flex', flexDirection: 'column', alignItems: 'center',
      padding: '48px 20px 80px', boxSizing: 'border-box', gap: 24,
      transition: 'background 0.6s',
    }}>
      {/* Brandmark */}
      <div style={{ textAlign: 'center', maxWidth: 520 }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 10, marginBottom: 8 }}>
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
            <path d="M2 14 Q 6 10 10 14 T 18 14 T 26 14" stroke="oklch(0.88 0.07 85)" strokeWidth="1.4" strokeLinecap="round" fill="none" opacity="0.9" />
            <path d="M2 18 Q 6 14 10 18 T 18 18 T 26 18" stroke="oklch(0.82 0.09 210)" strokeWidth="1.4" strokeLinecap="round" fill="none" opacity="0.9" />
          </svg>
          <span className="ui" style={{
            fontSize: 11, fontWeight: 600, letterSpacing: 2.8, textTransform: 'uppercase',
            color: 'oklch(0.95 0.03 200)',
          }}>Perfectly&nbsp;Tranquillo</span>
        </div>
        <div className="serif" style={{ fontSize: 24, color: 'oklch(0.95 0.04 85)', lineHeight: 1.2 }}>
          a gentle daily practice
        </div>
      </div>

      {/* Phone */}
      <div style={{ position: 'relative' }}>
        <PTDevice>
          <MusicButton playing={musicOn} onToggle={toggleMusic} />
          <SettingsGear onClick={() => setRoute('settings')} />

          <div key={route} style={{
            position: 'absolute', inset: 0,
            overflow: 'auto', paddingBottom: showTabs ? 100 : 20,
            boxSizing: 'border-box',
          }} className="scroll">
            {renderScreen()}
          </div>

          {showTabs && <TabBar current={tabMap[route]} onNav={setRoute} />}

          {/* Quick-access pill for non-tab screens (morning/evening/breath/focus/break) when on home */}
          {route === 'home' && <QuickPeek go={setRoute} />}

          {musicGreeting && !musicOn && (
            <div className="ui fade-in" style={{
              position: 'absolute', top: 62, right: 56, zIndex: 25,
              padding: '8px 14px', borderRadius: 14,
              background: 'oklch(0.95 0.05 85 / 0.95)',
              color: 'oklch(0.28 0.05 70)', fontSize: 11, fontWeight: 600,
              boxShadow: '0 8px 20px -4px rgba(0,0,0,0.3)',
              whiteSpace: 'nowrap',
            }}>
              tap for ocean sounds ♪
            </div>
          )}

          {/* Notification toast */}
          {notif && (
            <NotifToast notif={notif} onClose={() => setNotif(null)}
              onOpen={() => { setRoute('habits'); setNotif(null); }} />
          )}
        </PTDevice>

        {/* Designer note: nav model */}
        <div className="ui" style={{
          textAlign: 'center', maxWidth: 380, margin: '18px auto 0',
          fontSize: 10.5, color: 'rgba(240,248,255,0.35)', lineHeight: 1.6,
          letterSpacing: 0.2,
        }}>
          Bottom nav: Home · Habits · Mandala · Garden.<br />
          Morning, Evening, Breath, Focus, Break are reached from Home cards. Settings is the gear at top-right.
        </div>
      </div>

      {/* Tweaks panel */}
      <TweaksPanel title="Tweaks">
        <TweakSection label="Feel" />
        <TweakRadio label="Font" value={tweaks.font}
          options={['fraunces', 'caveat', 'instrument', 'cormorant']}
          onChange={v => setTweak('font', v)} />
        <TweakSlider label="Warmth" value={tweaks.warmth} min={0} max={100} unit="%"
          onChange={v => setTweak('warmth', v)} />

        <TweakSection label="Sound" />
        <TweakToggle label="Ocean music" value={tweaks.music && musicOn}
          onChange={v => { setTweak('music', v); if (v && !musicOn) toggleMusic(); if (!v && musicOn) toggleMusic(); }} />
        <TweakSlider label="Music volume" value={tweaks.musicVolume} min={0} max={100} unit="%"
          onChange={v => setTweak('musicVolume', v)} />
        <TweakToggle label="Chime on tap" value={tweaks.sound}
          onChange={v => setTweak('sound', v)} />

        <TweakSection label="Demo" />
        <TweakButton label="Trigger reminder" onClick={triggerDemoNotif} />
      </TweaksPanel>
    </div>
  );
}

// Small floating settings gear (top-right of device)
function SettingsGear({ onClick }) {
  return (
    <button onClick={onClick} className="ui"
      style={{
        position: 'absolute', top: 64, right: 16, zIndex: 30,
        width: 36, height: 36, borderRadius: '50%',
        background: 'oklch(0.22 0.06 230 / 0.6)',
        border: '1px solid rgba(240,248,255,0.15)',
        backdropFilter: 'blur(16px)',
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        cursor: 'pointer', color: 'rgba(240,248,255,0.8)',
      }}>
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
        <circle cx="12" cy="12" r="3" />
        <path d="M12 2v3M12 19v3M4.2 4.2l2.1 2.1M17.7 17.7l2.1 2.1M2 12h3M19 12h3M4.2 19.8l2.1-2.1M17.7 6.3l2.1-2.1" strokeLinecap="round" />
      </svg>
    </button>
  );
}

// Quick peek — chips for Morning/Evening/Breath/Focus/Break, visible on Home only
function QuickPeek({ go }) {
  // this is actually rendered inside HomeScreen cards now; keep as a no-op for reservation
  return null;
}

ReactDOM.createRoot(document.getElementById('root')).render(<App />);
