# Handoff: Perfectly Tranquillo

A gamified mental-wellness Android app for quick (≤60s) daily check-ins, built around **Virginia Satir's 8 self-resources mandala**. Designed for someone who wants the benefits of journaling/meditation apps without the heaviness — no long prompts, no AI chat, just small soft actions that fill in a living mandala across the day.

---

## About the design files

Everything in `/prototype` is a **design reference** — an interactive HTML + React (via inline Babel) prototype that shows the intended look, interaction model, and state transitions. It is **not production code** and should not be shipped or literally ported.

The task is to **recreate these designs natively for Android** (Jetpack Compose strongly recommended — the animations and custom mandala canvas will be dramatically easier than in XML/Views). If the team prefers cross-platform, React Native + Reanimated + Skia is a reasonable alternative. Either way, re-implement using the codebase's established patterns, navigation library, theming system, and component conventions — don't wrap a WebView.

**To preview:** open `prototype/index.html` in a browser. The top-right gear opens Settings; bottom nav cycles Home/Habits/Mandala/Garden; the floating "Tweaks" button (bottom-right of page, not of device) exposes palette + sound + demo toggles. A fake notification fires ~8s after load.

---

## Fidelity

**High fidelity (hifi).** Colors, typography, spacing, animation timings, and interaction details are intentional. Recreate the visual language pixel-closely. Where this document doesn't specify something, pull the exact value from the prototype source (files listed at the end).

---

## Product summary

- **Name:** Perfectly Tranquillo
- **Platform:** Android phone, single-user, offline-first (no accounts)
- **Audience:** Someone burned out on journaling apps and AI-chat wellness tools — wants quick gamified check-ins
- **Core loop:** Open app → see today's mandala → tap habits through the day → fill mandala petals with a resource + optional challenge, morning & evening → collect sea pebbles in the Garden
- **Philosophy:** "Small things, often." No points, no XP, no streakshaming. Soft progress rings + collectible sea pebbles + gentle language.

---

## Navigation model

**Bottom tab bar (4 tabs):**
1. **Home** — today's mandala, intention, quick-action cards for Morning/Evening/Breath/Focus/Break
2. **Habits** — tabbed: Daily (7 everyday habits) + Weekly (day-of-week habits)
3. **Mandala** — dedicated screen for filling Satir resource entries (AM/PM) — tap petal → enter resource + optional challenge
4. **Garden** (code name: `progress`) — week strip, collected pebbles, 7-day resource averages, archive of past mandala entries

**Not in bottom nav — reached from Home cards or chrome:**
- Morning intention flow (quick-action card on Home)
- Evening reflection (quick-action card on Home)
- Breath (box breathing 4·4·4·4)
- Focus (10/15/25/45-min timer)
- Take a break (19 micro-breaks + surprise-me)
- Settings (gear icon, top-right of phone chrome)

---

## Design tokens

### Color palette — **four named ocean palettes, user-switchable**

The app supports 4 palette directions. Default is **Deep Tide**. Each palette sets a page background (the wallpaper around the device in the prototype — ignore for native) and a device background (what matters for the Android app). Kelp Forest also overlays a subtle wavy SVG texture on the device bg.

All colors authored in `oklch()`. Convert to hex via your tooling.

**Deep Tide** (default) — dark turquoise + warm sand
- base stops: `oklch(0.30 0.07 195)` → `oklch(0.22 0.06 200)` → `oklch(0.16 0.05 205)`
- warm highlight: `oklch(0.88 0.06 80 / 0.22)` top-right radial
- cool accent: `oklch(0.55 0.09 195)` top-left radial

**Tidepool** — cleaned-up deep blue (less purple than original)
- base: `oklch(0.24 0.07 230)` → `oklch(0.18 0.06 232)` → `oklch(0.14 0.05 235)`

**Sea Glass** — brighter teal, twilight lagoon
- base: `oklch(0.36 0.08 200)` → `oklch(0.26 0.07 205)` → `oklch(0.20 0.06 210)`

**Kelp Forest** — deep teal, with repeating SVG wave texture overlay
- base: `oklch(0.22 0.07 180)` → `oklch(0.16 0.06 182)` → `oklch(0.12 0.05 185)`
- `waves: true` — adds a repeating wavy-line SVG pattern at low opacity on top of the bg

The wave-texture overlay (Kelp only) is an SVG pattern of horizontal wavy lines, ~25% opacity, tiled over the device background. Source: `prototype/src/app.jsx` constant `PALETTES.kelp`.

### Accent / content colors (shared across palettes)
- warm gold (highlights, PM mandala petals, intention chip): `oklch(0.86 0.05 75)` through `oklch(0.88 0.07 85)`
- cool blue (AM mandala petals, eyebrow labels): `oklch(0.78 0.08 215)` through `oklch(0.82 0.07 210)`
- body text: `oklch(0.96 0.015 220)` (near-white, faint blue cast)
- muted text: `rgba(245,241,232,0.55)` — `rgba(245,241,232,0.75)`
- thin divider lines: `rgba(245,241,232,0.10)`

### Typography

Fonts are Google-hosted. The prototype ships a "font" tweak with four options — **Caveat + Nunito is the ship default**.

- **Display serif (default):** Caveat 500 (handwritten casual) — greetings, headings, card titles. Runs small, so sizes are ~1.25× other serifs.
- **Body:** Nunito 300/400/500/600 — all UI text, buttons, labels

Type scale (caveat default):
- Greeting H1: ~36–42px Caveat 500, line-height 1.05
- Section title: ~26px Caveat 500
- Card title: ~20px Caveat 500
- Body: 13–14px Nunito 400/500
- Eyebrow (uppercase small-caps): 10–11px Nunito 600, letter-spacing 1.6–2.8px, `text-transform: uppercase`

Other font options exposed but deprioritized: Instrument Serif + Inter, Fraunces italic + Quicksand, Cormorant Garamond italic + Inter. **Ship with Caveat + Nunito unless product disagrees.**

### Spacing scale

Rough, but consistent in the prototype:
- 4, 6, 8, 10, 12, 14, 16, 18, 20, 24, 30, 40
- Screen horizontal padding: 16–20px
- Card padding: 14–18px
- Section gap: 14–20px
- Device bottom padding when tab bar visible: 100px (to clear the floating tab pill)

### Border radius

- Cards: 18–20px
- Pills (intention chip, tab bar): 22–100px (full round for pill shapes)
- Icons/avatars: full round
- Buttons: 14–16px

### Shadows & glass

- Glassmorphic cards: `backdrop-filter: blur(10–20px)`, bg `oklch(...  / 0.10–0.15)`, 1px border at `rgba(255,255,255,0.10–0.25)`
- Floating elements (tab bar, gear): `backdrop-filter: blur(20px)`, stronger bg opacity (~0.5–0.6)
- Device drop shadow: `0 30px 60px -20px rgba(0,0,0,0.65)`

### Paper grain + vignette

On the device background, a subtle SVG turbulence noise is layered at `opacity: 0.35, mix-blend-mode: overlay` for warmth. A radial vignette darkens corners at ~25% black. See `index.html` `.grain` and `.vignette` rules. Optional but adds a lot.

---

## Screens

### 1. Home (`screens-home.jsx`)
**Purpose:** single-glance status of today. User sees mood, today's mandala, quick actions.

**Layout (top → bottom):**
- Eyebrow: "TUESDAY · DAY 12" (cool blue, uppercase, tiny letter-spaced) + serif greeting "Bloom gently, {name}." + top-right gear icon
- **Intention pill** (full-round, warm-gold outlined): small dot + "INTENTION" eyebrow + serif intent text (e.g. "Gentle focus")
- **Satir Mandala** (240px, centered): simplified 8-petal ring. AM petals fill cool-blue, PM petals warm-gold. Info "?" button in top-right corner expands a tutorial card about the 8 resources.
- **Daily snapshot row:** 2–3 stat pills showing habits done / mandala petals filled / streak
- **"One good thing" card:** empty by default each day, large Caveat text field, placeholder "the sound of rain before I even opened my eyes". Resets at midnight.
- **Quick-action cards (2-col grid):** Morning intention · Evening reflection · Breath · Focus · Take a break — each a glass card with an inline SVG icon and serif title
- Bottom: floating tab bar

### 2. Habits (`screens-habits.jsx`)
**Purpose:** check off the day's habits; see streaks.

**Layout:**
- Header "Habits" + sub-tab toggle: **Daily · Weekly**
- Daily tab: list of 7 habit rows. Each row: checkbox circle (fills warm-gold when tapped) + label (serif) + hint (muted, small) + streak chip ("🌱 4") + optional reminder time (eyebrow, right-aligned, e.g. "07:00")
- Weekly tab: same row style, with a day-of-week pill ("MON", "TUE", …) instead of reminder time
- Tapping a row toggles `done`; animated soft flash + optional chime
- Settings icon on each row opens per-habit editor (label, hint, reminder time)

**Reminder time:** optional `remindAt: 'HH:MM'` per habit. If present and `notifMode !== 'silent'`, a fake Android-style notification fires at that time (see Notifications below).

### 3. Mandala (`screens-mandala.jsx`)
**Purpose:** the centerpiece. Fill each of 8 resources with a morning and/or evening entry.

**Layout:**
- Header "Your mandala today" + AM/PM phase toggle at top (two pills)
- Large interactive mandala (~280px): tapping any of 8 petals opens an entry modal
- Below: grid of 8 resource chips (Body, Mind, Heart, Senses, Connection, Nourish, Place, Spirit) — each shows current state (empty · AM filled · PM filled · both)
- **Entry modal:** opens when a petal is tapped. Two fields:
  - **Resource** (required): "what nourished this today?" — multiline soft input, serif
  - **Challenge** (optional): "what was hard?" — smaller, muted field
  - Save button (warm-gold full-round), cancel returns without saving
- On save, the petal animates fill (easing 0.6s, soft glow)

Entries are stored on the `state.mandalaEntries` object, keyed by resource + phase. At midnight they flush into `mandalaHistory` (see Data model).

### 4. Garden / Progress (`screens-progress.jsx`)
**Purpose:** gentle progress view. No points, no leaderboards.

**Layout:**
- Week strip at top: 7 days, each a small mandala thumbnail showing that day's fill
- **Sea pebbles collection:** a flat jar-of-pebbles visual. Each pebble is a kind: `moon` (pale gold), `jade` (green), `shell` (cream), etc. Pebbles are awarded for each completed day segment (morning / evening / daily-habit-run). Shown as stacked/scattered in the jar, tap one to see its source.
- **7-day resource averages:** small bar-per-petal showing how full each resource has been on average — reveals what's being tended vs. neglected
- **Archive:** tap-through to past mandala entries (list view, grouped by date)

### 5. Morning (`screens-morning.jsx`)
Short flow (≤60s), 3–4 steps:
1. Mood selection: 4 soft illustrated mood cards (bright · cloudy · heavy · unsure)
2. Intention picker: 6–8 chip options + custom input ("Gentle focus", "Stay curious", "Slow down", etc.)
3. Optional: one-line "what's one small good thing ahead?"
4. Confirmation: soft animation, return to Home with state set.

Sets `state.morningDone = true` + `state.intent` + `state.morningMood` + optionally `state.goodThing`.

### 6. Evening (`screens-morning.jsx` second export)
Short flow: reflection, mandala-phase nudge (PM), optional note. Sets `state.eveningDone = true`.

### 7. Breath (`screens-breath.jsx`)
Box breathing, 4·4·4·4 (inhale / hold / exhale / hold). Full-screen, single expanding/contracting circle with a soft glow, phase label underneath. Sessions: 1min / 3min / 5min. Haptic on phase change (native).

### 8. Focus (`screens-focus.jsx`)
Simple countdown timer. Four presets: 10 / 15 / 25 / 45 min. Big circular progress ring, minutes-remaining in center. Play/pause/stop.

### 9. Break (`screens-break.jsx`)
19 curated micro-break suggestions + "surprise me" button. List cards with icon + title + 1-line description + duration (30s–3min).

### 10. Settings (`screens-settings.jsx`)
Sections:
- **Sound:** toggles for chime-on-tap, ambient music, music volume slider
- **Ambient sound picker:** waves · birds · singing bowls · music · none
- **Notification mode:** silent · sound · vibrate (radio)
- **Appearance:** palette picker (4 palettes), font picker, dark/light (only dark is shipped currently)
- **Notifications:** "trigger demo reminder" button for QA
- **Data:** export, reset day (clears today, flushes to history), clear all (wipe)

---

## Interactions & behavior

### State persistence
- Everything in the prototype's `INITIAL_STATE` (`src/app.jsx`) is in-memory only. In the real app, persist to Room / DataStore.
- The `mandalaHistory` grows append-only; old entries are shown in Garden archive.

### Midnight rollover (important)
At 00:00 local, the app:
1. Flushes today's `mandalaEntries` into `mandalaHistory` (each entry: `{date, key, phase, kind: 'resource'|'challenge', text}`)
2. Clears `goodThing`, `morningDone`, `eveningDone`
3. Resets all `habits.done` to `false` (streaks keep their value — only reset if yesterday was missed)
4. Resets `resources` to all zeros

Prototype has a `Simulate new day` tweak button that forces this for demo.

### Notifications (`notifications.jsx`)
An Android-style notification toast slides in from the top when:
- A habit with `remindAt` hits its time and isn't done
- The QA "trigger demo reminder" is tapped

Design: translucent dark card, app icon (wave glyph) + "Perfectly Tranquillo" + habit label + hint + dismiss X. Tapping it routes to Habits.

### Animations
- Fade-in on route change: 0.5s ease-out, 6px translateY
- Mandala petal fill: 0.6s ease-out with soft glow burst
- Breathing circle: scale 1 ↔ 1.08 on breath phases
- Slow float on floating icons (music button, quick-peek): 6px Y, infinite
- All transitions should feel gentle — no bounce, no overshoot.

### Sound
- Tap chime: a single soft bowl/chime (~200ms) on habit tick and mandala save
- Ambient: looping underscore based on user's pick (waves / birds / bowls / music). Auto-ducks on notifications.
- See `src/ambient.jsx` for the web-audio implementation; on Android use ExoPlayer with a looped asset per ambient kind.

### Demo states in the prototype
- `state.habits[*].streak` is a pre-seeded number (2–11) for richer UI
- `state.mandalaHistory` is seeded with ~26 entries across the last 14 days (see `INITIAL_STATE` in `app.jsx`) so Garden isn't empty on first open
- `state.stones` is seeded with 4 pebbles

---

## Data model

```ts
type Habit = {
  id: string;
  label: string;
  hint: string;
  done: boolean;
  streak: number;
  remindAt: string; // 'HH:MM', optional
  day?: number;     // 0–6 (weekly habits only)
};

type Resources = Record<
  'physical' | 'intellectual' | 'emotional' | 'sensory' |
  'interactional' | 'nutritional' | 'contextual' | 'spiritual',
  { am: number; pm: number } // 0–1 fills
>;

type MandalaEntry = {
  resource?: string;   // what nourished (required to count)
  challenge?: string;  // what was hard (optional)
};

type MandalaHistoryRow = {
  date: string;        // 'YYYY-MM-DD'
  key: keyof Resources;
  phase: 'am' | 'pm';
  kind: 'resource' | 'challenge';
  text: string;
};

type Stone = { kind: 'moon' | 'jade' | 'shell' | 'coral'; label: string };

type AppState = {
  name: string;
  morningDone: boolean;
  eveningDone: boolean;
  goodThing: string;
  intent: string;
  morningMood: 'bright' | 'cloudy' | 'heavy' | 'unsure';
  habits: Habit[];
  weeklyHabits: Habit[];
  resources: Resources;
  mandalaEntries: Record<keyof Resources, { am?: MandalaEntry; pm?: MandalaEntry }>;
  mandalaHistory: MandalaHistoryRow[];
  stones: Stone[];
  sound: boolean;
  ambientSound: 'waves' | 'birds' | 'bowls' | 'music' | 'none';
  notifMode: 'silent' | 'sound' | 'vibrate';
  theme: 'dark' | 'light';
  font: 'caveat' | 'instrument' | 'cormorant' | 'fraunces';
  complexity: 'minimal' | 'full';
};
```

The eight Satir resource keys (order matters for mandala petal position, starting 12 o'clock, clockwise):
`physical, intellectual, emotional, sensory, interactional, nutritional, contextual, spiritual`

Labels shown to the user:
Body · Mind · Heart · Senses · Connection · Nourish · Place · Spirit

---

## The mandala (custom drawing)

Source: `src/mandala.jsx` — `<SatirMandala size resources highlight showLabels animate />`

**Geometry:**
- 8 equal wedges around the center, each 45° minus a 2.5° gap on each side for separation
- Three concentric radii: `outerR = size * 0.44`, `midR = size * 0.31`, `innerR = size * 0.18`
- Inner half of each wedge (innerR → midR) = AM fill
- Outer half (midR → outerR) = PM fill
- Ring strokes at `rgba(245,241,232,0.28)` separating the radii
- Soft pale core at center (size * 0.08)

**Fill:**
- Empty: subtle tint `oklch(... / 0.04)`
- AM filled: `oklch(0.82 0.07 210)` cool blue, 0.85 opacity
- PM filled: `oklch(0.86 0.09 78)` warm gold, 0.85 opacity
- `highlight` prop glows the selected wedge with a drop shadow

**In Compose:** build with `Canvas` + `drawArc` for the annular sectors. Save yourself pain: use `PathMeasure` to animate fills as a radial sweep from inner → outer on tap.

---

## Assets

- **Icons:** inline SVGs throughout, no external icon font. Stroke-based, weight 1.4–1.5. Replace with Material Symbols (weight 300, filled off) or keep the custom SVGs — both are fine.
- **Wave glyph (brand mark):** two hand-drawn stacked wavy lines, one warm one cool. Used in the top brandmark and notification icon. See `index.html` for the SVG.
- **Mood illustrations (morning flow):** 4 tiny SVG illustrations (bright sun, cloudy sky, heavy rain, question mark) — regenerate or keep.
- **Pebbles:** drawn as gradient-filled SVG circles with soft inner shadow, per kind. See `src/screens-progress.jsx`.
- **No photography / no generated imagery** — the app is pure SVG + CSS.

---

## Accessibility

- Minimum hit target: 44×44dp (bump the small "?" info buttons to 44dp in native)
- Text contrast: greeting / body on device bg is ≥ 4.5:1. Muted text (55% alpha) is intentional for de-emphasis, use only on decorative/hint text, never on interactive controls.
- Prefers-reduced-motion: in native, disable breathing circle scale + fade transitions.
- Screen reader: mandala petals should be discrete buttons with labels like "Body, morning not filled, double tap to add entry".

---

## What's in `/prototype`

```
prototype/
  index.html              — entry point, global fonts + colors + Babel imports
  tweaks-panel.jsx        — floating tweaks UI (dev-only)
  android-frame.jsx       — fake Android device chrome (dev-only — remove in native)
  src/
    app.jsx               — root, routing, state, palettes, notification scheduler
    frame.jsx             — PTDevice (device chrome component)
    ambient.jsx           — web-audio ambient-sound engine (→ replace with ExoPlayer)
    mandala.jsx           — <SatirMandala/> — the 8-petal component
    ui.jsx                — shared primitives (chips, cards, buttons, icons)
    notifications.jsx     — NotifToast
    screens-home.jsx
    screens-habits.jsx
    screens-mandala.jsx
    screens-morning.jsx   — morning + evening flows
    screens-breath.jsx
    screens-focus.jsx
    screens-break.jsx
    screens-progress.jsx  — Garden tab
    screens-settings.jsx
```

**Exact values that aren't in this README** — pull from source. Grep tips:
- Palettes: `PALETTES` in `app.jsx`
- Initial state / demo data: `INITIAL_STATE` in `app.jsx`
- Mandala geometry: `mandala.jsx` top
- Typography utilities: `index.html` `.serif / .ui / .mono / .theme-*` rules

---

## Open questions for product

A few things the designer and user haven't fully locked — flag these with your PM:

1. **Light mode.** The prototype is dark-only. A daylight palette (bright sky-blue + pale sand + slate ink) was sketched but not built — see `Palette Study Daytime.html` for early direction. If light mode ships, every card/text style needs a second variant.
2. **Habit reminders.** Per-habit `remindAt` is defined but only the notification UI is built — no scheduler. Android: `AlarmManager` + `NotificationCompat`.
3. **Weekly habits rollover.** When does a weekly habit's "done" reset? End of the day, or end of the week? Prototype resets at midnight same as dailies — probably wrong.
4. **Streak truncation.** No rule yet for missed days breaking a streak — currently streaks only go up in prototype.
5. **Pebble-awarding logic.** Garden shows pebbles but award rules aren't codified. Suggested: 1 pebble per completed AM mandala entry, 1 per PM, 1 per daily-habit completion. Kinds randomized within a soft palette.
