// Android-style notification toast + top notification bar
function NotifToast({ notif, onClose, onOpen }) {
  const [entered, setEntered] = React.useState(false);
  React.useEffect(() => {
    const t = setTimeout(() => setEntered(true), 20);
    return () => clearTimeout(t);
  }, []);

  return (
    <div style={{
      position: 'absolute', top: 48, left: 12, right: 12, zIndex: 60,
      transform: entered ? 'translateY(0)' : 'translateY(-120%)',
      opacity: entered ? 1 : 0,
      transition: 'transform 0.4s cubic-bezier(0.2, 0.9, 0.3, 1.2), opacity 0.3s',
    }}>
      <div style={{
        background: 'oklch(0.97 0.01 220 / 0.98)',
        borderRadius: 20,
        padding: '12px 14px',
        display: 'flex', alignItems: 'center', gap: 12,
        boxShadow: '0 14px 32px -8px rgba(0,0,0,0.5), 0 0 0 1px rgba(0,0,0,0.04)',
        color: 'oklch(0.25 0.05 240)',
        cursor: 'pointer',
      }} onClick={onOpen}>
        {/* app icon */}
        <div style={{
          width: 36, height: 36, borderRadius: 10, flexShrink: 0,
          background: 'linear-gradient(135deg, oklch(0.58 0.12 220), oklch(0.32 0.08 240))',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          position: 'relative', overflow: 'hidden',
        }}>
          <svg width="22" height="22" viewBox="0 0 24 24">
            <circle cx="12" cy="12" r="2.5" fill="oklch(0.95 0.04 85)" />
            {[0, 45, 90, 135, 180, 225, 270, 315].map(a => {
              const rad = a * Math.PI / 180;
              return (
                <ellipse key={a}
                  cx={12 + Math.cos(rad) * 5}
                  cy={12 + Math.sin(rad) * 5}
                  rx="2" ry="1.1"
                  transform={`rotate(${a}, ${12 + Math.cos(rad) * 5}, ${12 + Math.sin(rad) * 5})`}
                  fill="oklch(0.88 0.06 200)" fillOpacity="0.9" />
              );
            })}
          </svg>
        </div>
        <div style={{ flex: 1, minWidth: 0 }}>
          <div style={{
            display: 'flex', alignItems: 'center', gap: 6,
            fontSize: 10, fontWeight: 600, letterSpacing: 0.5,
            color: 'oklch(0.45 0.05 240)', marginBottom: 2,
          }}>
            <span>PERFECTLY TRANQUILLO</span>
            <span style={{ opacity: 0.4 }}>·</span>
            <span style={{ fontWeight: 500, opacity: 0.7 }}>{notif.hint}</span>
          </div>
          <div className="serif" style={{
            fontSize: 15, lineHeight: 1.25, color: 'oklch(0.2 0.04 240)',
            fontFamily: "'Fraunces', serif", fontStyle: 'italic',
          }}>
            It's time for <span style={{ fontWeight: 600 }}>{notif.habitLabel}</span> in the garden
          </div>
        </div>
        <button onClick={(e) => { e.stopPropagation(); onClose(); }}
          className="ui"
          style={{
            width: 24, height: 24, borderRadius: '50%', flexShrink: 0,
            background: 'oklch(0.9 0.02 220)', border: 'none',
            color: 'oklch(0.4 0.05 240)', fontSize: 14, cursor: 'pointer',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
          }}>×</button>
      </div>
    </div>
  );
}

Object.assign(window, { NotifToast });
