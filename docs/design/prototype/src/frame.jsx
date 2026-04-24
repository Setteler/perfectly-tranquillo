// Perfectly Tranquillo — Android-style device frame with sophisticated deep palette
// and warm paper-grain texture inside.

function PTStatusBar({ time = '6:42' }) {
  const c = 'rgba(245,241,232,0.94)';
  return (
    <div style={{
      height: 40, display: 'flex', alignItems: 'center',
      justifyContent: 'space-between', padding: '0 22px',
      position: 'relative', flexShrink: 0, zIndex: 5,
      fontFamily: "'Inter', 'Roboto', system-ui, sans-serif",
    }}>
      <span style={{ fontSize: 13, fontWeight: 500, color: c, letterSpacing: 0.2 }}>
        {time}
      </span>
      {/* punch-hole camera */}
      <div style={{
        position: 'absolute', left: '50%', top: 10, transform: 'translateX(-50%)',
        width: 10, height: 10, borderRadius: '50%', background: '#05040a',
        boxShadow: '0 0 0 1.5px rgba(255,255,255,0.04)',
      }} />
      <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
        {/* signal triangle */}
        <svg width="14" height="14" viewBox="0 0 16 16">
          <path d="M8 13.3L.67 5.97a10.37 10.37 0 0114.66 0L8 13.3z" fill={c}/>
        </svg>
        {/* wifi */}
        <svg width="14" height="14" viewBox="0 0 16 16">
          <path d="M14.67 14.67V1.33L1.33 14.67h13.34z" fill={c}/>
        </svg>
        {/* battery */}
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
      height: 24, display: 'flex', alignItems: 'center', justifyContent: 'center',
      flexShrink: 0, zIndex: 5,
    }}>
      <div style={{
        width: 128, height: 4, borderRadius: 2,
        background: 'rgba(245,241,232,0.5)',
      }} />
    </div>
  );
}

function PTDevice({ children, width = 392, height = 812, palette }) {
  const pal = palette || {
    deviceBg: `
      radial-gradient(ellipse at 82% 95%, oklch(0.78 0.12 65 / 0.20) 0%, transparent 55%),
      radial-gradient(ellipse at 18% 8%,  oklch(0.40 0.10 255 / 0.55) 0%, transparent 50%),
      radial-gradient(ellipse at 78% 25%, oklch(0.32 0.08 260 / 0.65) 0%, transparent 55%),
      linear-gradient(180deg, oklch(0.22 0.06 260) 0%, oklch(0.17 0.05 265) 45%, oklch(0.14 0.04 270) 100%)
    `,
    waves: false,
  };
  return (
    <div style={{
      width, height,
      borderRadius: 44,
      padding: 5,
      background: 'linear-gradient(160deg, #2a2620 0%, #12100d 50%, #1d1915 100%)',
      boxShadow:
        '0 50px 100px -25px rgba(0,0,0,0.75),' +
        ' 0 0 0 1.5px rgba(255,255,255,0.05) inset,' +
        ' 0 2px 1px rgba(255,255,255,0.08) inset',
      flexShrink: 0,
      position: 'relative',
    }}>
      {/* side buttons — volume + power */}
      <div style={{ position: 'absolute', left: -2, top: 140, width: 4, height: 32, borderRadius: 2, background: '#05040a' }} />
      <div style={{ position: 'absolute', left: -2, top: 188, width: 4, height: 54, borderRadius: 2, background: '#05040a' }} />
      <div style={{ position: 'absolute', right: -2, top: 180, width: 4, height: 80, borderRadius: 2, background: '#05040a' }} />

      <div style={{
        width: '100%', height: '100%',
        borderRadius: 39, overflow: 'hidden',
        position: 'relative',
        background: pal.deviceBg,
        display: 'flex', flexDirection: 'column',
      }}>
        {/* wave texture overlay */}
        {pal.waves && <WaveTexture />}
        {/* paper grain */}
        <div className="grain" />
        <div className="vignette" />

        <PTStatusBar />
        <div style={{ flex: 1, overflow: 'hidden', position: 'relative', zIndex: 2 }}>
          {children}
        </div>
        <PTNavBar />
      </div>
    </div>
  );
}

// Subtle wave texture — repeating SVG of soft wave crests
function WaveTexture() {
  const svg = encodeURIComponent(`
    <svg xmlns='http://www.w3.org/2000/svg' width='220' height='60' viewBox='0 0 220 60'>
      <g fill='none' stroke='rgba(200,240,235,0.06)' stroke-width='1.2' stroke-linecap='round'>
        <path d='M0 18 Q 20 6, 40 18 T 80 18 T 120 18 T 160 18 T 200 18 T 240 18' />
        <path d='M0 36 Q 22 24, 44 36 T 88 36 T 132 36 T 176 36 T 220 36 T 264 36' opacity='0.7'/>
        <path d='M0 54 Q 18 44, 38 54 T 76 54 T 114 54 T 152 54 T 190 54 T 228 54' opacity='0.5'/>
      </g>
    </svg>
  `);
  return (
    <div style={{
      position: 'absolute', inset: 0,
      backgroundImage: `url("data:image/svg+xml;utf8,${svg}")`,
      backgroundSize: '220px 60px',
      backgroundRepeat: 'repeat',
      opacity: 0.75,
      mixBlendMode: 'screen',
      pointerEvents: 'none',
      zIndex: 1,
    }} />
  );
}

Object.assign(window, { PTDevice, PTStatusBar, PTNavBar });
