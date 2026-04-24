// Habits tracker — tabbed: Daily + Weekly (day-of-week assignments)
const DAYS_SHORT = ['S','M','T','W','T','F','S'];
const DAYS_FULL = ['Sunday','Monday','Tuesday','Wednesday','Thursday','Friday','Saturday'];

function HabitsScreen({ state, setState }) {
  const [tab, setTab] = React.useState('daily'); // 'daily' | 'weekly'
  const todayIdx = 2; // Tuesday for the demo

  const toggle = (id, scope) => {
    setState(s => {
      const key = scope === 'weekly' ? 'weeklyHabits' : 'habits';
      const list = s[key] || [];
      const next = list.map(h => h.id === id ? { ...h, done: !h.done } : h);
      const doneNow = next.find(h => h.id === id).done;

      const resources = { ...s.resources };
      const map = {
        'no-phone': 'contextual', 'workout': 'physical', 'break': 'sensory',
        'eat': 'nutritional', 'water': 'nutritional', 'sleep': 'physical',
        'gratitude': 'emotional',
        'therapy': 'emotional', 'call-mom': 'interactional', 'meal-prep': 'nutritional',
        'long-walk': 'physical', 'deep-clean': 'contextual', 'read': 'intellectual',
        'date-night': 'interactional',
      };
      const r = map[id];
      if (r) {
        const delta = doneNow ? 0.15 : -0.15;
        resources[r] = {
          ...resources[r],
          pm: Math.max(0, Math.min(1, resources[r].pm + delta)),
        };
      }
      const stones = doneNow
        ? [...s.stones, { kind: scope === 'weekly' ? 'coral' : 'jade', label: id }]
        : s.stones;
      return { ...s, [key]: next, resources, stones };
    });
  };

  const dailyDone = state.habits.filter(h => h.done).length;
  const weekly = state.weeklyHabits || [];
  const todaysWeekly = weekly.filter(h => h.day === todayIdx);

  return (
    <div className="fade-in" style={{ padding: '16px 20px 120px' }}>
      <ScreenHeader eyebrow="Today's tending" title="Small things, often" />

      {/* Tabs */}
      <div style={{
        display: 'flex', padding: 4, borderRadius: 100,
        background: 'rgba(240,248,255,0.06)',
        border: '1px solid rgba(240,248,255,0.1)',
        marginBottom: 16,
      }}>
        {[
          { k: 'daily',  l: 'Daily',  c: dailyDone, t: state.habits.length },
          { k: 'weekly', l: 'Weekly', c: weekly.filter(h => h.done).length, t: weekly.length },
        ].map(t => (
          <button key={t.k} onClick={() => setTab(t.k)} className="ui"
            style={{
              flex: 1, padding: '10px 12px', borderRadius: 100, cursor: 'pointer', border: 'none',
              background: tab === t.k ? 'oklch(0.88 0.07 85 / 0.2)' : 'transparent',
              color: tab === t.k ? 'oklch(0.95 0.06 85)' : 'rgba(240,248,255,0.6)',
              fontSize: 13, fontWeight: 600, letterSpacing: 0.3,
              display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 8,
            }}>
            {t.l}
            <span className="mono" style={{
              fontSize: 10, padding: '2px 8px', borderRadius: 100,
              background: 'rgba(240,248,255,0.08)',
              color: 'rgba(240,248,255,0.7)',
            }}>
              {t.c}/{t.t}
            </span>
          </button>
        ))}
      </div>

      {tab === 'daily' && (
        <DailyView state={state} setState={setState} toggle={toggle} />
      )}

      {tab === 'weekly' && (
        <WeeklyView state={state} setState={setState} toggle={toggle} todayIdx={todayIdx} todaysWeekly={todaysWeekly} />
      )}
    </div>
  );
}

function DailyView({ state, setState, toggle }) {
  const done = state.habits.filter(h => h.done).length;
  const progress = done / state.habits.length;
  return (
    <div className="fade-in">
      <div style={{
        display: 'flex', alignItems: 'center', gap: 14,
        padding: '14px 18px', borderRadius: 20,
        background: 'rgba(240,248,255,0.05)',
        border: '1px solid rgba(240,248,255,0.1)',
        marginBottom: 14,
      }}>
        <ProgressRing size={52} value={progress} stroke={3}>
          <span className="serif" style={{ fontSize: 20, color: 'oklch(0.96 0.03 200)' }}>
            {done}
          </span>
        </ProgressRing>
        <div style={{ flex: 1 }}>
          <div className="serif" style={{ fontSize: 18, color: 'oklch(0.96 0.03 200)' }}>
            {done === state.habits.length ? 'All tended.' : `${state.habits.length - done} left to tend`}
          </div>
          <div className="ui" style={{ fontSize: 12, color: 'rgba(240,248,255,0.55)', marginTop: 2 }}>
            every day · soft green stones
          </div>
        </div>
      </div>

      <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
        {state.habits.map(h => (
          <HabitRow key={h.id} habit={h} onToggle={() => toggle(h.id, 'daily')} stoneColor="jade"
            onUpdate={(patch) => setState(s => ({
              ...s,
              habits: s.habits.map(x => x.id === h.id ? { ...x, ...patch } : x),
            }))} />
        ))}
      </div>
    </div>
  );
}

function WeeklyView({ state, setState, toggle, todayIdx, todaysWeekly }) {
  const [selectedDay, setSelectedDay] = React.useState(todayIdx);
  const weekly = state.weeklyHabits || [];
  const forDay = weekly.filter(h => h.day === selectedDay);
  const doneOnDay = forDay.filter(h => h.done).length;

  return (
    <div className="fade-in">
      {/* Day strip */}
      <div style={{
        display: 'flex', gap: 6, marginBottom: 14,
      }}>
        {DAYS_SHORT.map((d, i) => {
          const count = weekly.filter(h => h.day === i).length;
          const done = weekly.filter(h => h.day === i && h.done).length;
          const isToday = i === todayIdx;
          const isSelected = i === selectedDay;
          const fill = count > 0 ? done / count : 0;
          return (
            <button key={i} onClick={() => setSelectedDay(i)} className="ui"
              style={{
                flex: 1, padding: '10px 2px', borderRadius: 14,
                background: isSelected ? 'oklch(0.88 0.07 85 / 0.22)' : 'rgba(240,248,255,0.04)',
                border: '1px solid ' + (isSelected ? 'oklch(0.88 0.07 85 / 0.5)' : 'rgba(240,248,255,0.08)'),
                cursor: 'pointer',
                display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 6,
                color: isSelected ? 'oklch(0.95 0.06 85)' : 'rgba(240,248,255,0.75)',
                position: 'relative',
              }}>
              <div style={{ fontSize: 11, fontWeight: 600, letterSpacing: 0.4 }}>
                {d}
              </div>
              <div style={{ position: 'relative' }}>
                <div style={{
                  width: 22, height: 22, borderRadius: '50%',
                  background: count > 0
                    ? `conic-gradient(oklch(0.88 0.07 85) ${fill * 360}deg, rgba(240,248,255,0.08) 0)`
                    : 'rgba(240,248,255,0.05)',
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                }}>
                  <div style={{
                    width: 14, height: 14, borderRadius: '50%',
                    background: 'oklch(0.3 0.07 225)',
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                    fontSize: 8, color: 'rgba(240,248,255,0.7)',
                  }}>
                    {count || ''}
                  </div>
                </div>
              </div>
              {isToday && (
                <div style={{
                  position: 'absolute', bottom: 3, width: 4, height: 4, borderRadius: '50%',
                  background: 'oklch(0.88 0.07 85)',
                }} />
              )}
            </button>
          );
        })}
      </div>

      {/* Selected day panel */}
      <div style={{
        padding: '14px 18px', borderRadius: 20,
        background: 'rgba(240,248,255,0.05)',
        border: '1px solid rgba(240,248,255,0.1)',
        marginBottom: 14,
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 4 }}>
          <div className="serif" style={{ fontSize: 22, color: 'oklch(0.96 0.03 200)' }}>
            {DAYS_FULL[selectedDay]}
          </div>
          {selectedDay === todayIdx && (
            <span className="ui" style={{
              fontSize: 9, fontWeight: 600, letterSpacing: 1.2, textTransform: 'uppercase',
              padding: '3px 8px', borderRadius: 100,
              background: 'oklch(0.88 0.07 85 / 0.2)',
              color: 'oklch(0.9 0.07 85)',
            }}>
              today
            </span>
          )}
        </div>
        <div className="ui" style={{ fontSize: 12, color: 'rgba(240,248,255,0.55)' }}>
          {forDay.length === 0
            ? 'nothing scheduled · tap + to add'
            : `${doneOnDay} of ${forDay.length} tended · coral stones`}
        </div>
      </div>

      {/* Habits for day */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: 10, marginBottom: 14 }}>
        {forDay.length === 0 && (
          <div className="ui" style={{
            textAlign: 'center', padding: '24px 20px', borderRadius: 18,
            background: 'rgba(240,248,255,0.03)',
            border: '1px dashed rgba(240,248,255,0.14)',
            color: 'rgba(240,248,255,0.5)', fontSize: 13,
            fontStyle: 'italic',
          }}>
            a gentle day — nothing scheduled
          </div>
        )}
        {forDay.map(h => (
          <HabitRow key={h.id} habit={h} onToggle={() => toggle(h.id, 'weekly')} stoneColor="coral"
            onUpdate={(patch) => setState(s => ({
              ...s,
              weeklyHabits: s.weeklyHabits.map(x => x.id === h.id ? { ...x, ...patch } : x),
            }))}
            showRemove onRemove={() => {
              setState(s => ({ ...s, weeklyHabits: s.weeklyHabits.filter(x => x.id !== h.id) }));
            }} />
        ))}
      </div>

      <WeeklyAdd selectedDay={selectedDay} setState={setState} />
    </div>
  );
}

function WeeklyAdd({ selectedDay, setState }) {
  const [label, setLabel] = React.useState('');
  const add = () => {
    if (!label.trim()) return;
    setState(s => ({
      ...s,
      weeklyHabits: [...(s.weeklyHabits || []), {
        id: 'w-' + Date.now(),
        label: label.trim(),
        hint: DAYS_FULL[selectedDay] + 's',
        day: selectedDay,
        done: false, streak: 0,
      }],
    }));
    setLabel('');
  };
  return (
    <div style={{ display: 'flex', gap: 8 }}>
      <input value={label} onChange={e => setLabel(e.target.value)}
        onKeyDown={e => e.key === 'Enter' && add()}
        placeholder={`add a ${DAYS_FULL[selectedDay]} habit…`}
        className="ui"
        style={{
          flex: 1, padding: '12px 16px', borderRadius: 100,
          background: 'rgba(240,248,255,0.05)',
          border: '1px solid rgba(240,248,255,0.14)',
          color: 'oklch(0.96 0.03 200)', fontSize: 13, outline: 'none',
        }} />
      <button onClick={add} className="ui"
        style={{
          padding: '0 20px', borderRadius: 100,
          background: 'oklch(0.88 0.07 85 / 0.25)',
          border: '1px solid oklch(0.88 0.07 85 / 0.5)',
          color: 'oklch(0.95 0.06 85)', fontSize: 14, fontWeight: 600,
          cursor: 'pointer',
        }}>
        +
      </button>
    </div>
  );
}

function HabitRow({ habit, onToggle, stoneColor = 'jade', showRemove, onRemove, onUpdate }) {
  const [editing, setEditing] = React.useState(false);
  const hasRemind = !!habit.remindAt;
  return (
    <div style={{
      display: 'flex', flexDirection: 'column',
      padding: '12px 14px', borderRadius: 18,
      background: habit.done ? 'oklch(0.78 0.08 215 / 0.12)' : 'rgba(240,248,255,0.04)',
      border: '1px solid ' + (habit.done ? 'oklch(0.78 0.08 215 / 0.35)' : 'rgba(240,248,255,0.1)'),
      transition: 'all 0.25s',
    }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
      <button onClick={onToggle} className="ui"
        style={{
          flex: 1, display: 'flex', alignItems: 'center', gap: 14,
          background: 'transparent', border: 'none', padding: 0,
          cursor: 'pointer', textAlign: 'left', color: 'oklch(0.92 0.03 205)',
        }}>
        <div style={{
          width: 28, height: 28, borderRadius: '50%',
          background: habit.done
            ? (stoneColor === 'coral'
                ? 'radial-gradient(circle at 30% 28%, oklch(0.85 0.1 35), oklch(0.62 0.12 30))'
                : 'radial-gradient(circle at 30% 28%, oklch(0.88 0.06 215), oklch(0.55 0.08 225))')
            : 'rgba(240,248,255,0.04)',
          border: habit.done ? 'none' : '1px solid rgba(240,248,255,0.25)',
          flexShrink: 0,
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          boxShadow: habit.done ? 'inset -2px -2px 3px rgba(0,0,0,0.3), inset 2px 2px 2px rgba(255,255,255,0.3)' : 'none',
        }}>
          {habit.done && (
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="oklch(0.2 0.03 250)" strokeWidth="2.5" strokeLinecap="round">
              <path d="M5 12l5 5 9-11" />
            </svg>
          )}
        </div>
        <div style={{ flex: 1, minWidth: 0 }}>
          <div className="serif" style={{
            fontSize: 17, color: 'oklch(0.96 0.03 200)',
            textDecoration: habit.done ? 'line-through' : 'none',
            textDecorationColor: 'rgba(240,248,255,0.3)',
          }}>
            {habit.label}
          </div>
          <div style={{ fontSize: 11, color: 'rgba(240,248,255,0.5)', marginTop: 2, display: 'flex', alignItems: 'center', gap: 6 }}>
            <span>{habit.hint}</span>
            {hasRemind && (
              <>
                <span style={{ opacity: 0.5 }}>·</span>
                <span style={{ display: 'inline-flex', alignItems: 'center', gap: 3, color: 'oklch(0.88 0.07 85)' }}>
                  <svg width="9" height="9" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
                    <path d="M18 16V11a6 6 0 10-12 0v5l-2 2h16z" />
                    <path d="M10 20a2 2 0 004 0" />
                  </svg>
                  {habit.remindAt}
                </span>
              </>
            )}
          </div>
        </div>
      </button>
      {habit.streak > 0 && !editing && (
        <div style={{
          display: 'flex', alignItems: 'center', gap: 4,
          padding: '4px 10px', borderRadius: 100,
          background: 'rgba(240,248,255,0.04)',
        }}>
          <div style={{ width: 6, height: 6, borderRadius: '50%', background: 'oklch(0.88 0.07 85)' }} />
          <span className="mono" style={{ fontSize: 10, color: 'oklch(0.9 0.07 85)' }}>
            {habit.streak}d
          </span>
        </div>
      )}
      <button onClick={() => setEditing(e => !e)} className="ui"
        style={{
          width: 28, height: 28, borderRadius: '50%', flexShrink: 0,
          background: editing || hasRemind ? 'oklch(0.88 0.07 85 / 0.2)' : 'rgba(240,248,255,0.06)',
          border: '1px solid ' + (editing || hasRemind ? 'oklch(0.88 0.07 85 / 0.4)' : 'rgba(240,248,255,0.12)'),
          color: editing || hasRemind ? 'oklch(0.92 0.07 85)' : 'rgba(240,248,255,0.5)',
          cursor: 'pointer',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
        }}>
        <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round">
          <path d="M18 16V11a6 6 0 10-12 0v5l-2 2h16z" />
          <path d="M10 20a2 2 0 004 0" />
        </svg>
      </button>
      {showRemove && (
        <button onClick={onRemove} className="ui"
          style={{
            width: 24, height: 24, borderRadius: '50%',
            background: 'rgba(240,248,255,0.06)',
            border: '1px solid rgba(240,248,255,0.15)',
            color: 'rgba(240,248,255,0.6)', fontSize: 12,
            cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center',
          }}>×</button>
      )}
      </div>

      {editing && (
        <div className="fade-in" style={{
          marginTop: 10, paddingTop: 10,
          borderTop: '1px solid rgba(240,248,255,0.08)',
          display: 'flex', alignItems: 'center', gap: 10, flexWrap: 'wrap',
        }}>
          <span className="ui" style={{
            fontSize: 10, fontWeight: 600, letterSpacing: 1.2, textTransform: 'uppercase',
            color: 'rgba(240,248,255,0.55)',
          }}>Remind me at</span>
          <input type="time" value={habit.remindAt || ''}
            onChange={e => onUpdate && onUpdate({ remindAt: e.target.value })}
            className="mono"
            style={{
              padding: '6px 10px', borderRadius: 10,
              background: 'rgba(240,248,255,0.06)',
              border: '1px solid rgba(240,248,255,0.15)',
              color: 'oklch(0.96 0.03 200)', fontSize: 13,
              colorScheme: 'dark', outline: 'none',
            }} />
          {hasRemind && (
            <button onClick={() => onUpdate && onUpdate({ remindAt: '' })}
              className="ui"
              style={{
                padding: '6px 12px', borderRadius: 100, cursor: 'pointer',
                background: 'transparent',
                border: '1px solid rgba(240,248,255,0.15)',
                color: 'rgba(240,248,255,0.6)', fontSize: 11,
              }}>
              clear
            </button>
          )}
          <span className="ui" style={{
            fontSize: 10, fontStyle: 'italic',
            color: 'rgba(240,248,255,0.35)', flexBasis: '100%',
            marginTop: 2,
          }}>
            you'll get a soft reminder: <em>"It's time for {habit.label.toLowerCase()} in the garden"</em>
          </span>
        </div>
      )}
    </div>
  );
}

Object.assign(window, { HabitsScreen, HabitRow, DailyView, WeeklyView });
