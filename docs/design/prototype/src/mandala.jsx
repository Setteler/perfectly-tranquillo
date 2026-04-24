// Satir Resource Mandala — 8 petals around a central self.
// Each petal has a morning (inner) and evening (outer) fill arc.
// resources: { physical: {am:0-1, pm:0-1}, ... } same keys for all 8.

const SATIR_RESOURCES = [
  { key: 'physical',      label: 'Body',        hint: 'Movement, rest, sensation' },
  { key: 'intellectual',  label: 'Mind',        hint: 'Thought, curiosity, learning' },
  { key: 'emotional',     label: 'Heart',       hint: 'Feelings, expression' },
  { key: 'sensory',       label: 'Senses',      hint: 'See, hear, touch, taste' },
  { key: 'interactional', label: 'Connection',  hint: 'People, conversation' },
  { key: 'nutritional',   label: 'Nourish',     hint: 'Food, water, intake' },
  { key: 'contextual',    label: 'Place',       hint: 'Space, time, environment' },
  { key: 'spiritual',     label: 'Spirit',      hint: 'Meaning, purpose' },
];

function polarToXY(cx, cy, r, angleDeg) {
  const a = (angleDeg - 90) * (Math.PI / 180);
  return [cx + r * Math.cos(a), cy + r * Math.sin(a)];
}

// describe a petal-wedge path (annular sector) from r1->r2 between a1->a2
function wedgePath(cx, cy, r1, r2, a1, a2) {
  const [x1, y1] = polarToXY(cx, cy, r2, a1);
  const [x2, y2] = polarToXY(cx, cy, r2, a2);
  const [x3, y3] = polarToXY(cx, cy, r1, a2);
  const [x4, y4] = polarToXY(cx, cy, r1, a1);
  const large = (a2 - a1) > 180 ? 1 : 0;
  return `M ${x1} ${y1} A ${r2} ${r2} 0 ${large} 1 ${x2} ${y2} L ${x3} ${y3} A ${r1} ${r1} 0 ${large} 0 ${x4} ${y4} Z`;
}

function SatirMandala({
  size = 300,
  resources = {},
  highlight = null,           // key currently focused
  onPetalTap = null,
  showLabels = false,
  complexity = 'full',        // 'simple' | 'full'
  animate = true,
}) {
  const cx = size / 2, cy = size / 2;
  const outerR = size * 0.46;
  const innerR = size * 0.16;
  const ringGap = 1.5;
  const slice = 360 / 8;
  const padDeg = 1.2;

  // Inner-petal (morning) band: innerR -> mid
  // Outer-petal (evening) band: mid -> outerR
  const mid = (outerR + innerR) / 2;

  return (
    <svg width={size} height={size} viewBox={`0 0 ${size} ${size}`} style={{ display: 'block', overflow: 'visible' }}>
      <defs>
        <radialGradient id="sri-core" cx="50%" cy="50%" r="50%">
          <stop offset="0%"  stopColor="oklch(0.92 0.05 90)" stopOpacity="1" />
          <stop offset="60%" stopColor="oklch(0.78 0.08 215)" stopOpacity="0.9" />
          <stop offset="100%" stopColor="oklch(0.52 0.09 225)" stopOpacity="0.5" />
        </radialGradient>
        <linearGradient id="am-grad" x1="0%" y1="0%" x2="0%" y2="100%">
          <stop offset="0%" stopColor="oklch(0.78 0.08 215)" stopOpacity="0.9" />
          <stop offset="100%" stopColor="oklch(0.62 0.09 220)" stopOpacity="0.6" />
        </linearGradient>
        <linearGradient id="pm-grad" x1="0%" y1="0%" x2="0%" y2="100%">
          <stop offset="0%" stopColor="oklch(0.86 0.05 75)" stopOpacity="0.85" />
          <stop offset="100%" stopColor="oklch(0.72 0.06 65)" stopOpacity="0.55" />
        </linearGradient>
        <filter id="soft-glow" x="-50%" y="-50%" width="200%" height="200%">
          <feGaussianBlur stdDeviation="2" />
        </filter>
      </defs>

      {/* Outer decorative rings (Satir-inspired geometry) */}
      {complexity === 'full' && (
        <g>
          <circle cx={cx} cy={cy} r={outerR + 16} fill="none"
            stroke="oklch(0.78 0.08 215)" strokeOpacity="0.15" strokeWidth="0.6" />
          <circle cx={cx} cy={cy} r={outerR + 10} fill="none"
            stroke="oklch(0.78 0.08 215)" strokeOpacity="0.25" strokeWidth="0.6" strokeDasharray="1 3" />
          {/* 16-ray star behind */}
          {Array.from({ length: 16 }).map((_, i) => {
            const a = (i * (360 / 16) - 90) * Math.PI / 180;
            const r1 = outerR + 4;
            const r2 = outerR + 14;
            return (
              <line key={i}
                x1={cx + r1 * Math.cos(a)} y1={cy + r1 * Math.sin(a)}
                x2={cx + r2 * Math.cos(a)} y2={cy + r2 * Math.sin(a)}
                stroke="oklch(0.92 0.03 205)" strokeOpacity="0.25" strokeWidth="0.5" />
            );
          })}
          {/* 2 downward triangles + 2 upward triangles = Satir 'self' geometry */}
          <g opacity="0.22" stroke="oklch(0.92 0.03 205)" strokeWidth="0.6" fill="none">
            {[0, 45].map((rot) => (
              <polygon key={`up-${rot}`}
                points={triPts(cx, cy, outerR * 0.62, 0 + rot, 120 + rot, 240 + rot)} />
            ))}
            {[0, 45].map((rot) => (
              <polygon key={`dn-${rot}`}
                points={triPts(cx, cy, outerR * 0.62, 60 + rot, 180 + rot, 300 + rot)} />
            ))}
          </g>
        </g>
      )}

      {/* Petal rings: background */}
      {SATIR_RESOURCES.map((res, i) => {
        const a1 = i * slice + padDeg;
        const a2 = (i + 1) * slice - padDeg;
        const isHL = highlight === res.key;
        return (
          <g key={res.key} style={{ cursor: onPetalTap ? 'pointer' : 'default' }}
            onClick={() => onPetalTap && onPetalTap(res.key)}>
            {/* bg */}
            <path d={wedgePath(cx, cy, innerR + ringGap, mid - 0.5, a1, a2)}
              fill="oklch(0.92 0.03 205)" fillOpacity={isHL ? 0.18 : 0.08} />
            <path d={wedgePath(cx, cy, mid + 0.5, outerR, a1, a2)}
              fill="oklch(0.92 0.03 205)" fillOpacity={isHL ? 0.14 : 0.06} />
            {/* divider lines */}
            <line
              {...lineFromPolar(cx, cy, innerR, outerR, i * slice)}
              stroke="oklch(0.92 0.03 205)" strokeOpacity="0.18" strokeWidth="0.6" />
          </g>
        );
      })}

      {/* Petal rings: fills (AM inner, PM outer) */}
      {SATIR_RESOURCES.map((res, i) => {
        const a1 = i * slice + padDeg;
        const a2 = (i + 1) * slice - padDeg;
        const r = resources[res.key] || { am: 0, pm: 0 };
        // scale each band by fill (0..1) from inner edge outward for am, from mid outward for pm
        const amR = innerR + ringGap + (mid - 0.5 - innerR - ringGap) * r.am;
        const pmR = mid + 0.5 + (outerR - mid - 0.5) * r.pm;
        return (
          <g key={res.key + '-fill'}>
            {r.am > 0 && (
              <path d={wedgePath(cx, cy, innerR + ringGap, amR, a1, a2)}
                fill="url(#am-grad)" />
            )}
            {r.pm > 0 && (
              <path d={wedgePath(cx, cy, mid + 0.5, pmR, a1, a2)}
                fill="url(#pm-grad)" />
            )}
          </g>
        );
      })}

      {/* mid ring (AM/PM divider) */}
      <circle cx={cx} cy={cy} r={mid} fill="none"
        stroke="oklch(0.92 0.03 205)" strokeOpacity="0.3" strokeWidth="0.6" />
      <circle cx={cx} cy={cy} r={outerR} fill="none"
        stroke="oklch(0.92 0.03 205)" strokeOpacity="0.4" strokeWidth="0.8" />
      <circle cx={cx} cy={cy} r={innerR} fill="none"
        stroke="oklch(0.92 0.03 205)" strokeOpacity="0.35" strokeWidth="0.8" />

      {/* Labels */}
      {showLabels && SATIR_RESOURCES.map((res, i) => {
        const mAngle = i * slice + slice / 2;
        const [lx, ly] = polarToXY(cx, cy, outerR + 22, mAngle);
        return (
          <text key={res.key} x={lx} y={ly}
            fontSize="9" fontFamily="'Inter', sans-serif" fontWeight="500"
            fill="oklch(0.92 0.03 205)" fillOpacity="0.75"
            textAnchor="middle" dominantBaseline="middle"
            letterSpacing="0.08em" style={{ textTransform: 'uppercase' }}>
            {res.label}
          </text>
        );
      })}

      {/* Core */}
      <circle cx={cx} cy={cy} r={innerR - 2} fill="url(#sri-core)"
        style={animate ? { animation: 'breathe 6s ease-in-out infinite', transformOrigin: `${cx}px ${cy}px` } : {}} />
      <circle cx={cx} cy={cy} r={innerR - 2} fill="none"
        stroke="oklch(0.92 0.03 205)" strokeOpacity="0.5" strokeWidth="0.5" />
    </svg>
  );
}

function triPts(cx, cy, r, a1, a2, a3) {
  const [x1,y1] = polarToXY(cx,cy,r,a1);
  const [x2,y2] = polarToXY(cx,cy,r,a2);
  const [x3,y3] = polarToXY(cx,cy,r,a3);
  return `${x1},${y1} ${x2},${y2} ${x3},${y3}`;
}

function lineFromPolar(cx, cy, r1, r2, angle) {
  const [x1,y1] = polarToXY(cx,cy,r1,angle);
  const [x2,y2] = polarToXY(cx,cy,r2,angle);
  return { x1, y1, x2, y2 };
}

// Tiny mandala (for list items / progress dots)
function MiniMandala({ size = 28, fill = 0.5 }) {
  const cx = size/2, cy = size/2, r = size * 0.44;
  return (
    <svg width={size} height={size} viewBox={`0 0 ${size} ${size}`}>
      <circle cx={cx} cy={cy} r={r} fill="none"
        stroke="oklch(0.92 0.03 205)" strokeOpacity="0.3" strokeWidth="1" />
      {Array.from({ length: 8 }).map((_, i) => {
        const on = i < Math.round(fill * 8);
        const a1 = i * 45 + 2;
        const a2 = (i + 1) * 45 - 2;
        return (
          <path key={i}
            d={wedgePath(cx, cy, r * 0.35, r, a1, a2)}
            fill={on ? 'oklch(0.78 0.08 215)' : 'oklch(0.92 0.03 205)'}
            fillOpacity={on ? 0.8 : 0.1} />
        );
      })}
      <circle cx={cx} cy={cy} r={r*0.28} fill="oklch(0.92 0.05 90)" fillOpacity="0.9" />
    </svg>
  );
}

Object.assign(window, { SatirMandala, MiniMandala, SATIR_RESOURCES, wedgePath, polarToXY });
