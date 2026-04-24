// Satir Resource Mandala — simplified: 8 clean petals + inner/outer ring + soft core.
// No ornamental stars, triangles, or extra decoration — just the data.
// resources: { physical: {am:0-1, pm:0-1}, ... } same keys for all 8.

const SATIR_RESOURCES = [
  { key: 'physical',      label: 'Body',       hint: 'Movement, rest, sensation' },
  { key: 'intellectual',  label: 'Mind',       hint: 'Thought, curiosity, learning' },
  { key: 'emotional',     label: 'Heart',      hint: 'Feelings, expression' },
  { key: 'sensory',       label: 'Senses',     hint: 'See, hear, touch, taste' },
  { key: 'interactional', label: 'Connection', hint: 'People, conversation' },
  { key: 'nutritional',   label: 'Nourish',    hint: 'Food, water, intake' },
  { key: 'contextual',    label: 'Place',      hint: 'Space, time, environment' },
  { key: 'spiritual',     label: 'Spirit',     hint: 'Meaning, purpose' },
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

// Simplified fill: just binary "has any AM entry" / "has any PM entry".
// Petal is split in two halves: inner-half = AM, outer-half = PM. Either can be lit.
function petalPhases(r) {
  if (!r) return { am: false, pm: false };
  return {
    am: (r.am || 0) > 0,
    pm: (r.pm || 0) > 0,
  };
}

function SatirMandala({
  size = 300,
  resources = {},
  highlight = null,
  showLabels = false,
  animate = true,
}) {
  const cx = size / 2, cy = size / 2;
  const outerR = size * 0.44;
  const midR   = size * 0.31;
  const innerR = size * 0.18;
  const slice = 360 / 8;
  const padDeg = 2.5;

  // warm paper + soft sky accents; no rainbow gradients
  const amColor = 'oklch(0.82 0.07 210)';         // cool morning blue
  const pmColor = 'oklch(0.86 0.09 78)';          // warm evening gold
  const ringStroke = 'rgba(245,241,232,0.28)';

  return (
    <svg width={size} height={size} viewBox={`0 0 ${size} ${size}`} style={{ display: 'block', overflow: 'visible' }}>
      <defs>
        <radialGradient id="sri-core" cx="50%" cy="50%" r="50%">
          <stop offset="0%"   stopColor="oklch(0.95 0.06 85)" stopOpacity="1" />
          <stop offset="70%"  stopColor="oklch(0.82 0.09 80)" stopOpacity="0.85" />
          <stop offset="100%" stopColor="oklch(0.62 0.10 70)" stopOpacity="0.55" />
        </radialGradient>
      </defs>

      {/* outer + inner framing rings — just two clean circles */}
      <circle cx={cx} cy={cy} r={outerR} fill="none"
        stroke={ringStroke} strokeWidth="1" />
      <circle cx={cx} cy={cy} r={innerR} fill="none"
        stroke={ringStroke} strokeWidth="1" />

      {/* 8 petal wedges — AM (inner half) + PM (outer half) */}
      {SATIR_RESOURCES.map((res, i) => {
        const a1 = i * slice + padDeg;
        const a2 = (i + 1) * slice - padDeg;
        const { am, pm } = petalPhases(resources[res.key]);
        const isHL = highlight === res.key;
        return (
          <g key={res.key}>
            {/* background petal */}
            <path d={wedgePath(cx, cy, innerR, outerR, a1, a2)}
              fill="rgba(245,241,232,0.04)"
              stroke={ringStroke}
              strokeWidth={isHL ? 1 : 0.4}
              strokeOpacity={isHL ? 0.6 : 0.3} />
            {/* AM half — inner */}
            {am && (
              <path d={wedgePath(cx, cy, innerR, midR, a1, a2)}
                fill={amColor} fillOpacity={0.55} />
            )}
            {/* PM half — outer */}
            {pm && (
              <path d={wedgePath(cx, cy, midR, outerR, a1, a2)}
                fill={pmColor} fillOpacity={0.6} />
            )}
            {/* divider */}
            <path d={`M ${polarToXY(cx,cy,midR,a1).join(' ')} A ${midR} ${midR} 0 0 1 ${polarToXY(cx,cy,midR,a2).join(' ')}`}
              fill="none" stroke="rgba(245,241,232,0.12)" strokeWidth="0.5" />
          </g>
        );
      })}

      {/* labels */}
      {showLabels && SATIR_RESOURCES.map((res, i) => {
        const mAngle = i * slice + slice / 2;
        const [lx, ly] = polarToXY(cx, cy, outerR + 22, mAngle);
        return (
          <text key={res.key} x={lx} y={ly}
            fontSize="11" fontFamily="'Inter', system-ui, sans-serif" fontWeight="500"
            fill="rgba(245,241,232,0.8)"
            textAnchor="middle" dominantBaseline="middle"
            letterSpacing="0.08em" style={{ textTransform: 'uppercase' }}>
            {res.label}
          </text>
        );
      })}

      {/* core */}
      <circle cx={cx} cy={cy} r={innerR - 3} fill="url(#sri-core)"
        style={animate ? { animation: 'breathe 6s ease-in-out infinite', transformOrigin: `${cx}px ${cy}px` } : {}} />
    </svg>
  );
}

// Tiny mandala for list items / progress dots — matched to the clean style
function MiniMandala({ size = 28, fill = 0.5 }) {
  const cx = size/2, cy = size/2, r = size * 0.44;
  return (
    <svg width={size} height={size} viewBox={`0 0 ${size} ${size}`}>
      <circle cx={cx} cy={cy} r={r} fill="none"
        stroke="rgba(245,241,232,0.3)" strokeWidth="1" />
      {Array.from({ length: 8 }).map((_, i) => {
        const on = i < Math.round(fill * 8);
        const a1 = i * 45 + 2;
        const a2 = (i + 1) * 45 - 2;
        return (
          <path key={i}
            d={wedgePath(cx, cy, r * 0.4, r, a1, a2)}
            fill={on ? 'oklch(0.88 0.08 85)' : 'rgba(245,241,232,0.08)'}
            fillOpacity={on ? 0.75 : 1} />
        );
      })}
      <circle cx={cx} cy={cy} r={r*0.3} fill="oklch(0.92 0.06 85)" fillOpacity="0.9" />
    </svg>
  );
}

Object.assign(window, { SatirMandala, MiniMandala, SATIR_RESOURCES, wedgePath, polarToXY });
