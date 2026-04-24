// Shared UI: bottom tab bar, cards, chips, buttons, progress ring, stone.

function Chip({ children, active, onClick, style = {} }) {
  return (
    <button onClick={onClick}
      className="ui"
      style={{
        border: '1px solid ' + (active ? 'oklch(0.78 0.08 215 / 0.6)' : 'rgba(240,248,255,0.12)'),
        background: active ? 'oklch(0.78 0.08 215 / 0.18)' : 'rgba(240,248,255,0.04)',
        color: 'oklch(0.92 0.03 205)',
        fontSize: 12, fontWeight: 500, letterSpacing: 0.2,
        padding: '8px 14px', borderRadius: 100, cursor: 'pointer',
        ...style,
      }}>
      {children}
    </button>
  );
}

function Card({ children, style = {}, padded = true, onClick, tone = 'default' }) {
  const tones = {
    default: { bg: 'rgba(240,248,255,0.04)', border: 'rgba(240,248,255,0.1)' },
    raised:  { bg: 'rgba(240,248,255,0.07)', border: 'rgba(240,248,255,0.14)' },
    sand:    { bg: 'oklch(0.86 0.05 75 / 0.08)', border: 'oklch(0.86 0.05 75 / 0.25)' },
  };
  const t = tones[tone];
  return (
    <div onClick={onClick}
      style={{
        background: t.bg,
        border: `1px solid ${t.border}`,
        borderRadius: 22,
        padding: padded ? 18 : 0,
        cursor: onClick ? 'pointer' : 'default',
        backdropFilter: 'blur(12px)',
        ...style,
      }}>
      {children}
    </div>
  );
}

function PrimaryBtn({ children, onClick, style = {}, variant = 'primary' }) {
  const styles = {
    primary: {
      background: 'linear-gradient(180deg, oklch(0.78 0.08 215) 0%, oklch(0.62 0.09 220) 100%)',
      color: 'oklch(0.12 0.03 250)',
      border: '1px solid oklch(0.78 0.08 215 / 0.6)',
      boxShadow: '0 8px 24px -8px oklch(0.52 0.09 225 / 0.5), inset 0 1px 0 rgba(255,255,255,0.3)',
    },
    ghost: {
      background: 'rgba(240,248,255,0.06)',
      color: 'oklch(0.92 0.03 205)',
      border: '1px solid rgba(240,248,255,0.14)',
    },
    sand: {
      background: 'linear-gradient(180deg, oklch(0.86 0.05 75) 0%, oklch(0.72 0.06 65) 100%)',
      color: 'oklch(0.22 0.03 70)',
      border: '1px solid oklch(0.86 0.05 75 / 0.6)',
      boxShadow: '0 8px 24px -8px oklch(0.72 0.06 65 / 0.4)',
    },
  };
  return (
    <button onClick={onClick}
      className="ui"
      style={{
        width: '100%', padding: '16px 20px', borderRadius: 100,
        fontSize: 15, fontWeight: 500, letterSpacing: 0.1,
        cursor: 'pointer', transition: 'transform 0.15s',
        ...styles[variant], ...style,
      }}
      onMouseDown={e => e.currentTarget.style.transform = 'scale(0.98)'}
      onMouseUp={e => e.currentTarget.style.transform = 'scale(1)'}
      onMouseLeave={e => e.currentTarget.style.transform = 'scale(1)'}>
      {children}
    </button>
  );
}

// Soft progress ring (no numbers)
function ProgressRing({ size = 40, value = 0.5, stroke = 3, color = 'oklch(0.78 0.08 215)', trackColor = 'rgba(240,248,255,0.12)', children }) {
  const r = (size - stroke) / 2;
  const c = 2 * Math.PI * r;
  return (
    <div style={{ position: 'relative', width: size, height: size }}>
      <svg width={size} height={size} style={{ transform: 'rotate(-90deg)' }}>
        <circle cx={size/2} cy={size/2} r={r} fill="none"
          stroke={trackColor} strokeWidth={stroke} />
        <circle cx={size/2} cy={size/2} r={r} fill="none"
          stroke={color} strokeWidth={stroke} strokeLinecap="round"
          strokeDasharray={c} strokeDashoffset={c * (1 - value)}
          style={{ transition: 'stroke-dashoffset 0.6s ease' }} />
      </svg>
      {children && (
        <div style={{
          position: 'absolute', inset: 0, display: 'flex',
          alignItems: 'center', justifyContent: 'center',
        }}>{children}</div>
      )}
    </div>
  );
}

// Sea pebble — smooth oval with layered highlight, soft shadow, subtle speckle
function Stone({ color = 'moon', size = 28, dim = false, seed = 0 }) {
  const palette = {
    moon:  { a: 'oklch(0.92 0.02 220)', b: 'oklch(0.74 0.04 230)', c: 'oklch(0.55 0.05 235)' },
    sand:  { a: 'oklch(0.94 0.04 80)',  b: 'oklch(0.78 0.06 70)',  c: 'oklch(0.58 0.07 60)' },
    coral: { a: 'oklch(0.88 0.06 30)',  b: 'oklch(0.7 0.09 28)',   c: 'oklch(0.48 0.1 25)' },
    deep:  { a: 'oklch(0.62 0.07 230)', b: 'oklch(0.4 0.08 235)',  c: 'oklch(0.24 0.07 240)' },
    jade:  { a: 'oklch(0.86 0.06 175)', b: 'oklch(0.64 0.07 180)', c: 'oklch(0.42 0.07 190)' },
  };
  const p = palette[color] || palette.moon;
  // deterministic-ish variation by seed: aspect ratio, tilt, highlight position
  const s = (seed * 9301 + 49297) % 233280 / 233280; // pseudo-random 0..1
  const ratio = 0.82 + s * 0.14;          // 0.82 – 0.96 (oval)
  const tilt = -18 + ((seed * 37) % 36);  // -18 – +18 deg
  const hlX = 26 + ((seed * 13) % 18);    // 26 – 44 %
  const hlY = 22 + ((seed * 7) % 14);     // 22 – 36 %
  const w = size;
  const h = Math.round(size * ratio);
  return (
    <div style={{
      width: w, height: h, flexShrink: 0, position: 'relative',
      transform: `rotate(${tilt}deg)`,
      opacity: dim ? 0.3 : 1,
      filter: dim ? 'none' : 'drop-shadow(0 3px 4px rgba(0,0,0,0.28))',
    }}>
      {/* pebble body */}
      <div style={{
        position: 'absolute', inset: 0,
        borderRadius: '50% 48% 52% 50% / 50% 52% 48% 50%',
        background: `
          radial-gradient(ellipse 70% 90% at ${hlX}% ${hlY}%, ${p.a} 0%, ${p.b} 55%, ${p.c} 100%)
        `,
        boxShadow: `
          inset -3px -4px 6px oklch(0.15 0.04 240 / 0.35),
          inset 2px 3px 5px oklch(1 0 0 / 0.22)
        `,
      }} />
      {/* glossy top highlight */}
      <div style={{
        position: 'absolute',
        left: `${hlX - 8}%`, top: `${hlY - 6}%`,
        width: '38%', height: '24%',
        borderRadius: '50%',
        background: 'radial-gradient(ellipse, rgba(255,255,255,0.55) 0%, rgba(255,255,255,0) 70%)',
        filter: 'blur(0.5px)',
        pointerEvents: 'none',
      }} />
      {/* tiny speckle */}
      {size >= 20 && (
        <>
          <div style={{
            position: 'absolute',
            left: `${55 + (seed % 5) * 4}%`, top: `${60 + (seed % 3) * 6}%`,
            width: 2, height: 2, borderRadius: '50%',
            background: p.c, opacity: 0.35,
          }} />
          <div style={{
            position: 'absolute',
            left: `${30 + (seed % 4) * 3}%`, top: `${70 + (seed % 2) * 5}%`,
            width: 1.5, height: 1.5, borderRadius: '50%',
            background: p.c, opacity: 0.3,
          }} />
        </>
      )}
    </div>
  );
}

// Bottom tab bar
function TabBar({ current, onNav }) {
  const tabs = [
    { id: 'home',     label: 'Home',    icon: IconHome },
    { id: 'habits',   label: 'Habits',  icon: IconSprout },
    { id: 'mandala',  label: 'Mandala', icon: IconMandala },
    { id: 'progress', label: 'Garden',  icon: IconFlower },
  ];
  return (
    <div style={{
      position: 'absolute', bottom: 40, left: 14, right: 14,
      background: 'oklch(0.22 0.06 230 / 0.85)',
      backdropFilter: 'blur(24px)',
      border: '1px solid rgba(240,248,255,0.14)',
      borderRadius: 28,
      padding: '8px 6px',
      display: 'flex', justifyContent: 'space-around',
      zIndex: 50,
      boxShadow: '0 12px 30px -10px rgba(0,0,0,0.4)',
    }}>
      {tabs.map(t => {
        const Icon = t.icon;
        const active = current === t.id;
        return (
          <button key={t.id} onClick={() => onNav(t.id)}
            className="ui"
            style={{
              flex: 1, background: 'transparent', border: 'none',
              padding: '8px 4px', cursor: 'pointer',
              display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 4,
              color: active ? 'oklch(0.88 0.06 215)' : 'rgba(240,248,255,0.45)',
              borderRadius: 18, transition: 'color 0.2s',
            }}>
            <Icon active={active} />
            <span style={{ fontSize: 10, fontWeight: 500, letterSpacing: 0.3 }}>{t.label}</span>
          </button>
        );
      })}
    </div>
  );
}

// Minimal line-drawn icons
function IconHome({ active }) {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={active ? 1.8 : 1.4} strokeLinecap="round">
      <circle cx="12" cy="12" r="8" />
      <circle cx="12" cy="12" r="3" fill={active ? 'currentColor' : 'none'} />
      <line x1="12" y1="2" x2="12" y2="6" />
      <line x1="12" y1="18" x2="12" y2="22" />
      <line x1="2" y1="12" x2="6" y2="12" />
      <line x1="18" y1="12" x2="22" y2="12" />
    </svg>
  );
}
function IconSprout({ active }) {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={active ? 1.8 : 1.4} strokeLinecap="round">
      <path d="M12 21v-9" />
      <path d="M12 12C8 12 6 9 6 6c3 0 6 2 6 6z" fill={active ? 'currentColor' : 'none'} fillOpacity="0.2" />
      <path d="M12 14c4 0 6-3 6-6-3 0-6 2-6 6z" fill={active ? 'currentColor' : 'none'} fillOpacity="0.2" />
    </svg>
  );
}
function IconCircle({ active }) {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={active ? 1.8 : 1.4}>
      <circle cx="12" cy="12" r="9" />
      <circle cx="12" cy="12" r="4" fill={active ? 'currentColor' : 'none'} />
    </svg>
  );
}
function IconMandala({ active }) {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={active ? 1.8 : 1.4} strokeLinecap="round">
      <circle cx="12" cy="12" r="2" fill={active ? 'currentColor' : 'none'} />
      {[0, 45, 90, 135, 180, 225, 270, 315].map(a => {
        const rad = a * Math.PI / 180;
        const x1 = 12 + Math.cos(rad) * 4;
        const y1 = 12 + Math.sin(rad) * 4;
        const x2 = 12 + Math.cos(rad) * 9;
        const y2 = 12 + Math.sin(rad) * 9;
        return <line key={a} x1={x1} y1={y1} x2={x2} y2={y2} />;
      })}
      <circle cx="12" cy="12" r="9" />
    </svg>
  );
}
function IconFlower({ active }) {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={active ? 1.8 : 1.4} strokeLinecap="round">
      {[0, 60, 120, 180, 240, 300].map(a => {
        const rad = (a - 90) * Math.PI / 180;
        const x = 12 + Math.cos(rad) * 5;
        const y = 12 + Math.sin(rad) * 5;
        return <circle key={a} cx={x} cy={y} r="2.5" fill={active ? 'currentColor' : 'none'} fillOpacity="0.3" />;
      })}
      <circle cx="12" cy="12" r="2" fill="currentColor" />
    </svg>
  );
}

function IconBack({ onClick }) {
  return (
    <button onClick={onClick} className="ui"
      style={{
        width: 40, height: 40, borderRadius: 100,
        background: 'rgba(240,248,255,0.06)', border: '1px solid rgba(240,248,255,0.1)',
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        cursor: 'pointer', color: 'oklch(0.92 0.03 205)',
      }}>
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round">
        <path d="M15 5l-7 7 7 7" />
      </svg>
    </button>
  );
}

function IconGear({ onClick }) {
  return (
    <button onClick={onClick} className="ui"
      style={{
        width: 40, height: 40, borderRadius: 100,
        background: 'rgba(240,248,255,0.06)', border: '1px solid rgba(240,248,255,0.1)',
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        cursor: 'pointer', color: 'oklch(0.92 0.03 205)',
      }}>
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.4">
        <circle cx="12" cy="12" r="3" />
        <path d="M12 2v3M12 19v3M4.2 4.2l2.1 2.1M17.7 17.7l2.1 2.1M2 12h3M19 12h3M4.2 19.8l2.1-2.1M17.7 6.3l2.1-2.1" strokeLinecap="round" />
      </svg>
    </button>
  );
}

function ScreenHeader({ eyebrow, title, onBack, action }) {
  return (
    <div style={{ padding: '6px 20px 12px', display: 'flex', alignItems: 'flex-end', gap: 12 }}>
      {onBack && <IconBack onClick={onBack} />}
      <div style={{ flex: 1 }}>
        {eyebrow && (
          <div className="ui" style={{
            fontSize: 10, fontWeight: 600, letterSpacing: 1.6, textTransform: 'uppercase',
            color: 'oklch(0.78 0.08 215)', marginBottom: 4,
          }}>{eyebrow}</div>
        )}
        <div className="serif" style={{ fontSize: 28, lineHeight: 1.05, color: 'oklch(0.96 0.015 220)' }}>
          {title}
        </div>
      </div>
      {action}
    </div>
  );
}

Object.assign(window, {
  Chip, Card, PrimaryBtn, ProgressRing, Stone, TabBar,
  IconHome, IconSprout, IconCircle, IconFlower, IconMandala, IconBack, IconGear,
  ScreenHeader,
});
