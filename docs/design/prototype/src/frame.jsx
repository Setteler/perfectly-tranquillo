// Custom dark-blue Android frame for Perfectly Tranquillo
// Keeps Material 3 proportions but themed to sea/ink palette.

function PTStatusBar() {
  const c = 'rgba(240,248,255,0.92)';
  return (
    <div style={{
      height: 36, display: 'flex', alignItems: 'center',
      justifyContent: 'space-between', padding: '0 20px',
      position: 'relative', flexShrink: 0, zIndex: 5,
      fontFamily: "'Inter', system-ui, sans-serif",
    }}>
      <span style={{ fontSize: 13, fontWeight: 500, color: c, letterSpacing: 0.2 }}>6:42</span>
      <div style={{
        position: 'absolute', left: '50%', top: 8, transform: 'translateX(-50%)',
        width: 20, height: 20, borderRadius: 100, background: '#0a0e18',
      }} />
      <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
        <svg width="14" height="14" viewBox="0 0 16 16">
          <path d="M8 13.3L.67 5.97a10.37 10.37 0 0114.66 0L8 13.3z" fill={c}/>
        </svg>
        <svg width="14" height="14" viewBox="0 0 16 16">
          <path d="M14.67 14.67V1.33L1.33 14.67h13.34z" fill={c}/>
        </svg>
        <svg width="18" height="12" viewBox="0 0 18 12">
          <rect x="0.5" y="0.5" width="14" height="11" rx="2" fill="none" stroke={c} strokeWidth="1"/>
          <rect x="2" y="2" width="9" height="8" rx="1" fill={c}/>
          <rect x="15" y="4" width="2" height="4" rx="0.5" fill={c}/>
        </svg>
      </div>
    </div>
  );
}

function PTNavBar() {
  return (
    <div style={{
      height: 22, display: 'flex', alignItems: 'center', justifyContent: 'center',
      flexShrink: 0, zIndex: 5,
    }}>
      <div style={{
        width: 128, height: 4, borderRadius: 2,
        background: 'rgba(240,248,255,0.5)',
      }} />
    </div>
  );
}

function PTDevice({ children, width = 392, height = 812 }) {
  return (
    <div style={{
      width, height,
      borderRadius: 44,
      padding: 6,
      background: 'linear-gradient(145deg, #1a2030 0%, #0a0e18 100%)',
      boxShadow: '0 40px 80px -20px rgba(0,0,0,0.6), 0 0 0 1px rgba(255,255,255,0.06) inset',
      flexShrink: 0,
    }}>
      <div style={{
        width: '100%', height: '100%',
        borderRadius: 38, overflow: 'hidden',
        background: `
          radial-gradient(ellipse at 80% 100%, oklch(0.88 0.08 85 / 0.35) 0%, transparent 60%),
          radial-gradient(ellipse at 20% -10%, oklch(0.72 0.10 210 / 0.55) 0%, transparent 55%),
          linear-gradient(180deg, oklch(0.42 0.09 220) 0%, oklch(0.3 0.08 225) 55%, oklch(0.36 0.09 215) 100%)
        `,
        display: 'flex', flexDirection: 'column',
        position: 'relative',
      }}>
        <div className="stars" />
        <PTStatusBar />
        <div style={{ flex: 1, overflow: 'hidden', position: 'relative', zIndex: 2 }}>
          {children}
        </div>
        <PTNavBar />
      </div>
    </div>
  );
}

Object.assign(window, { PTDevice, PTStatusBar, PTNavBar });
