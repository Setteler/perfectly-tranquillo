# Handoff: Perfectly Tranquillo

A gamified mental-wellness Android app for quick (≤60s) daily check-ins, built around **Virginia Satir's 8 self-resources mandala**. Designed for someone who wants the benefits of journaling/meditation apps without the heaviness — no long prompts, no AI chat, just small actions that fill in a living mandala across the day.

---

## About the design files

Everything in `/prototype` is a **design reference** — an interactive HTML + React (via inline Babel) prototype that shows the intended look, interaction model, and state transitions. It is **not production code** and should not be shipped or literally ported.

The task is to **recreate these designs natively for Android** (Jetpack Compose recommended — animations and custom canvas drawing for the mandala will be dramatically easier than in XML/Views). If the team prefers cross-platform, React Native + Reanimated + Skia is a reasonable alternative. Either way, re-implement using the codebase's established patterns, navigation library, theming system, and component conventions — don't wrap a WebView.

Speaker-to-developer: open `prototype/index.html` in a browser to see the full app; the top-right gear leads to Settings, bottom nav cycles Home/Habits/Mandala/Garden, and the floating Tweaks button (bottom-right of page) flips theming knobs.

---

## Fidelity

**High fidelity (hifi).** Colors, typography, spacing, animation timings, and interaction details are intentional. Recreate the visual language pixel-closely. Where this document doesn't specify something, pull the exact value from the prototype source (files listed at the end).

---

## Product summary

**Name:** Perfectly Tranquillo (Spanish-ish phrasing chosen by the user)
**Platform:** Android phone, single user, offline-first
**Audience:** Someone burned out on journaling apps and AI-chat wellness tools — wants quick gamified check-ins
**Core loop:** Open app → see today's mandala → tap a few habits as the day goes → fill mandala petals with a resource + optional challenge morning & evening → collect sea pebbles
**Philosophy:** "Small things, often." No points, no XP, no streakshaming. Soft progress rings + collectible sea pebbles.

---

## Navigation model (important — this was iterated on)

**Bottom tab bar (4 tabs):**
1. **Home** — today's mandala, intention, quick-action cards for Morning/Evening/Breath/Focus/Break
2. **Habits** — tabbed: Daily (7 everyday habits) + Weekly (day-of-week habits)
3. **Mandala** — dedicated screen for filling Satir resource entries (AM/PM)
4. **Garden** — progress: week strip, collected pebbles, 7-day resource averages

**Not in bottom nav — reached from Home cards or chrome:**
- Morning intention flow (quick-action card on Home)
- Evening reflection (quick-action card on Home)
- Breath (box breathing 4·4·4·4)
- Focus (10/15/25/45-min timer)
- Take a break (19 micro-breaks + surprise-me)
- Settings (gear icon, top-right of phone chrome)

---

## Design tokens

### Color palette (oklch — convert to hex via your tooling if needed)

**Base (sea/night):**
- `--ink:       oklch(0.28 0.06 230)` · deep sea blue (backgrounds)
- `--ink-2:     oklch(0.34 0.07 225)`
- `--ink-3:     oklch(0.42 0.08 220)`
- `--deep:      oklch(0.50 0.09 220)`
- `--sea:       oklch(0.65 0.10 215)` · mid ocean
- `--sky:       oklch(0.82 0.09 210)` · AM accent (primary)
- `--foam:      oklch(0.95 0.03 200)` · text
- `--mist:      oklch(0.97 0.02 210)`

**Warm (sand/dawn):**
- `--sand:      oklch(0.88 0.07 85)` · PM accent / sand stones / highlights
- `--sand-2:    oklch(0.82 0.09 75)`
- `--stone:     oklch(0.78 0.06 70)`
- `--coral:     oklch(0.78 0.10 40)` · weekly-habit stones, challenge markers
- `--shell:     oklch(0.92 0.05 60)`

**Page gradient (top→bottom):**
Radial highlights at 85% 95% (warm sand) and 10% 10% + 80% 30% (sea), over a vertical linear from `oklch(0.40 0.09 220)` → `oklch(0.28 0.07 225)` → `oklch(0.22 0.06 230)`. A "warmth" knob (0–100) blends the warm radials in/out against the cool ones.

### Typography

Three font roles, loaded from Google Fonts. Default theme = Fraunces + Quicksand (the "whimsical" pairing).

| Role      | Class   | Font                     | Weight | Style                   | Use                                         |
|-----------|---------|--------------------------|--------|-------------------------|---------------------------------------------|
| Display   | `.serif`| Fraunces                 | 400    | italic, letter −0.005em | Titles, quotes, affirming copy              |
| UI        | `.ui`   | Quicksand                | 500    | normal                  | Buttons, labels, hints                      |
| Mono      | `.mono` | JetBrains Mono           | 400    | normal                  | Streak counts, numbers, metadata            |

Font-pair alternates (user-selectable in Settings):
- **fraunces** (default): Fraunces italic + Quicksand
- **instrument**: Instrument Serif + Inter
- **caveat**: Caveat handwritten + Nunito
- **cormorant**: Cormorant Garamond italic + Inter

### Spacing & radii

- Screen padding: `16px 20px` horizontal, `120px` bottom to clear tab bar
- Card padding: `18px`
- Card radius: `22px` (large cards), `20px` (inline), `18px` (rows), `14px` (inputs)
- Tab-bar radius: `28px`, floats `bottom: 40px` inside device, `left/right: 14px`
- Pill/chip radius: `100px`
- Stone size: typically `20–28px`

### Shadows & glows

- Primary button: `0 8px 24px -8px oklch(0.52 0.09 225 / 0.5)` + inset highlight
- Stone: `drop-shadow(0 3px 4px rgba(0,0,0,0.28))` + inset radial highlight & dark rim
- Notification toast: `0 14px 32px -8px rgba(0,0,0,0.5)`
- Tab bar: `0 12px 30px -10px rgba(0,0,0,0.4)`, backdrop blur 24px

### Motion

- Screen fade-in: 500ms ease-out (`@keyframes fade-in` translating 6px up + fading)
- Mandala core: `breathe` 6s infinite (scale 1 → 1.08 → 1)
- Progress ring: `stroke-dashoffset 0.6s ease`
- Surprise-me stone: `spin` 8s linear infinite
- Toast entrance: translate-Y −120% → 0, cubic-bezier(0.2, 0.9, 0.3, 1.2), 400ms
- Petal bottom-sheet: fade/slide from bottom

---

## Screens

### 1. Home

Living mandala + today's intention + quick-action cards.

- **Top**: greeting row with user's name, time-based (morning/afternoon/evening)
- **Mandala hero**: 280px SatirMandala, with a small `?` button that opens an inline explainer of the 8 resources
- **Today's intention card**: one line (from Morning flow), italic Fraunces, with gentle sand stone next to it
- **Quick-action cards** (grid, 2 per row): Morning, Evening, Breath, Focus, Break. Each = small card with circular gradient stone on left, serif title, `.ui` subtitle, chevron right.
- **Today's resource peek**: small strip of 8 PetalDots for the 8 Satir resources, showing which have entries

### 2. Habits (tabbed: Daily / Weekly)

- **Tab pill** at top, shows count pill `done/total` per tab
- **Daily tab**:
  - Summary card at top: ProgressRing (52px) + "N left to tend" + subtitle "every day · soft green stones"
  - Rows: `HabitRow` components — toggle check, label (Fraunces 17px), hint (11px), streak pill, bell icon that opens a time picker inline, optional remove
- **Weekly tab**:
  - Day strip: 7 buttons S/M/T/W/T/F/S with a mini conic-gradient progress ring per day showing done/total for that day; amber dot under "today"; tap to select day
  - Selected-day panel: "Tuesday" serif 22px + "today" badge if applicable
  - Rows for that day's weekly habits (coral stones when done)
  - Add-habit input at bottom: "add a Tuesday habit…" + `+` button
- **HabitRow reminder UI** (bell icon → expanded state):
  - `<input type="time">` native, colorScheme dark, padding 6×10, radius 10, JetBrains Mono
  - "Clear" button when set
  - Italic preview: `you'll get a soft reminder: "It's time for [habit] in the garden"`

### 3. Mandala

Dedicated per-petal journal screen.

- Header: eyebrow "Your inner landscape", title "Mandala"
- Explanation paragraph: "Eight resources of the self. Each morning and evening, tap a petal and name one good thing it's receiving — and, if you need to, one thing that's asking for care."
- **AM/PM toggle** (auto-selects by hour of day): `◐ Morning` / `◑ Evening`
- **Tappable mandala** (300px): base SatirMandala rendered with label text around the perimeter; an overlay SVG of invisible `wedgePath` hit regions per petal
- Subtitle: `tap any petal to tend it · N/8 filled`
- **Resource list** (below mandala): 8 rows, one per Satir resource, each with a PetalDot (filled state if has entry), label, and a preview of the current entry (`✿ [resource text]` or `◌ [challenge text]`) or the default hint
- **PetalSheet** (bottom sheet when a petal is tapped):
  - Slides up from bottom, translucent dark backdrop (`oklch(0.16 0.04 245 / 0.72)` + 10px blur)
  - Handle bar (44×4px) at top
  - Header: phase glyph stone + eyebrow ("This morning" / "This evening") + serif title (resource label, 24px) + close ×
  - Italic hint line under header
  - **Resource field** (accent: sky blue for AM, sand for PM):
    - Icon chip with `✿` + label "RESOURCE"
    - Textarea 2 rows, Fraunces 15px, placeholder rotates per-resource per-phase (see placeholders in `screens-mandala.jsx`)
  - **Challenge field** (accent: coral):
    - Icon chip with `◌` + label "CHALLENGE" + sublabel "optional"
    - Same textarea pattern
  - Primary button "Save this petal ✿" (sand variant)
- Entries live-update the mandala petal fill: resource-only = 0.9, challenge-only = 0.3, both = 0.6 (softens)

### 4. Garden (Progress)

Week strip, stones collected, 7-day resource averages.

- **Week strip**: 7 mini mandalas showing each day's overall fill, today highlighted
- **Stones collected** card: scattered pebbles in a 110px canvas, with a legend below showing counts by kind (moon/jade/sand/coral/deep)
- **Resource strand**: 8 horizontal mini bars, one per Satir resource, showing 7-day average of AM+PM fill

### 5. Morning intention

Three-screen soft-edge flow (skippable).

- Sunrise art (SVG gradient sun, low horizon)
- Rotating inspirational message (7 messages, one per day-of-year mod 7)
- Step 1: "one good thing ahead today?" — optional textarea; skip button
- Final: "you'll collect a moon stone for this morning"
- Completion adds a `moon` pebble + fills `intellectual.am` resource if text entered

### 6. Evening reflection

- Single screen with two quick prompts
- Adds a `sand` pebble on completion + fills `spiritual.pm`

### 7. Breath (box breathing 4·4·4·4)

- Full-screen breathing orb that scales/fades over 4-sec phases: Inhale / Hold / Exhale / Hold
- 6 cycles by default, text cycles with phase
- On complete: fills `emotional.pm` + 1 moon pebble

### 8. Focus

- 4 chips: 10 / 15 / 25 / 45 min
- Large mandala slowly filling as session progresses (uses `resources` with `am` growing)
- On complete: adds `deep` pebble + fills `physical.pm` and `intellectual.pm`

### 9. Take a break

**19 breaks across 4 categories** — see `screens-break.jsx` for full text, but the key surprise here is a large **Surprise me** hero button at the top.

Categories & counts (filter chips):
- **Tiny** (≤30s): Look far, Three long breaths, Slow sip of water, Do absolutely nothing, Shake it off
- **Sensory**: Five senses, Cold water on wrists, Find a good smell, One song eyes closed
- **Body**: Soft stretch, Tall spine, A small walk, Shake it off, Posture
- **World**: Go look at the sun, Find a piece of sky, Look for a flower, **Go pet your cat**, Text someone kind, Tidy one small thing, Open a window

Each break has:
- `id`, `kind` (tiny/sensory/body/world), `title`, `subtitle`, `duration` (sec), `resource` (Satir key it feeds), `prompt` (spoken-voice instruction), `color` (accent stone color)

**Surprise me button** at top: large card, conic-gradient spinning stone with ✨, picks a random break from the currently-active filter. Brief 450ms reveal pulse, then auto-starts the timer.

During break: 240px ProgressRing with countdown seconds, serif prompt below. On complete: +0.22 fill to the break's mapped resource + 1 jade pebble.

### 10. Settings

- Your habits list (with add/remove)
- Font pairing picker (4 options listed above)
- Sound toggle
- "Trigger demo notification" button

---

## Key reusable components

### Stone (sea pebble)

Oval pebble with layered radial gradient, glossy highlight, subtle speckle, randomized tilt. **Not a circle.**

```
props: { color, size = 28, dim = false, seed = 0 }
colors: moon | sand | coral | deep | jade
```

Each color is a 3-stop palette (a = highlight, b = mid, c = shadow). Body shape is a fancy border-radius `50% 48% 52% 50% / 50% 52% 48% 50%` to look organic. Pseudo-random seed drives aspect ratio (0.82–0.96), tilt (±18°), and highlight position (26–44% × 22–36%). Speckles (2 dots) appear at ≥20px.

See `src/ui.jsx` for the exact styling.

### SatirMandala

Custom SVG with:
- 8 petals in a ring (45° slice each, 1.2° padding)
- Each petal has two bands: AM (inner) and PM (outer), fill = `resources[key].am` and `.pm`
- Decorative: 16-ray star, 2 upward + 2 downward rotated triangles (Satir's "self" geometry), outer dashed ring
- Animated core circle with `breathe` pulse
- Props: `size, resources, highlight (key), onPetalTap, showLabels, complexity (simple|full), animate`

See `src/mandala.jsx`. Port this carefully — the geometry is the signature of the whole app.

### NotifToast (Android-style notification)

Slides in from top, translucent white rounded rect, app icon tile (mandala glyph), small app-name/time eyebrow, serif body "It's time for [habit] in the garden", close ×. Tap-through opens Habits screen.

### HabitRow

Row with toggle stone, label, hint + bell+time chip, streak pill, bell button (toggles inline reminder editor), optional remove ×. Expanded state reveals native `<input type="time">` with dark colorScheme.

---

## State shape

```js
{
  name: string,
  morningDone: bool, eveningDone: bool,
  goodThing: string, intent: string, morningMood: string,

  habits: [{ id, label, hint, done, streak, remindAt: "HH:mm" | "" }],
  weeklyHabits: [{ id, label, hint, day: 0-6, done, streak, remindAt }],

  resources: {                         // 8 Satir keys
    physical: { am: 0-1, pm: 0-1 },
    intellectual: {...}, emotional: {...}, sensory: {...},
    interactional: {...}, nutritional: {...}, contextual: {...}, spiritual: {...},
  },

  mandalaEntries: {
    [resourceKey]: {
      am: { resource: string, challenge: string },
      pm: { resource: string, challenge: string },
    }
  },

  stones: [{ kind: 'moon'|'jade'|'sand'|'coral'|'deep', label, when }],

  sound: bool, font: string, complexity: 'simple'|'full',
}
```

### Resource↔habit/action mapping

When a habit toggles on, its action maps to a Satir resource petal (adds ~0.15 fill to `.pm`). Mapping:

| Habit / action       | Resource          |
|----------------------|-------------------|
| no-phone, deep-clean | `contextual`      |
| workout, long-walk, sleep | `physical`  |
| break (generic)      | `sensory`         |
| eat, water, meal-prep| `nutritional`     |
| gratitude, therapy   | `emotional`       |
| call-mom, date-night | `interactional`   |
| read                 | `intellectual`    |
| morning flow         | `intellectual` + `moon` pebble |
| evening flow         | `spiritual` + `sand` pebble    |
| breath session       | `emotional` + `moon` pebble    |
| focus session        | `physical` + `intellectual` + `deep` pebble |
| each break (varies)  | its mapped resource + `jade` pebble |

---

## Reminders / notifications

Each habit has an optional `remindAt: "HH:mm"`. The app should schedule a local notification for that time each day. Notification copy template:

> **Perfectly Tranquillo** · HH:mm
> *It's time for [habit label] in the garden*

Tapping the notification deep-links to the **Habits** screen (and scrolls to the specific habit — enhancement; not in current prototype). Use Android's `NotificationManager` + `AlarmManager` (or `WorkManager` for daily repeats).

Weekly habits should only fire on their configured `day` of the week.

---

## Tweaks (design-time toggles, not shipped UX)

The floating Tweaks panel in the prototype is a designer/reviewer tool only. **Do not ship it.** The settings that matter to production are: Font pairing, Sound on/off, Habits add/remove — all of which live in the Settings screen.

---

## Files in this handoff

**`/prototype`** — the full interactive HTML prototype (open `index.html`)
- `index.html` — loads fonts, CSS tokens, boots React+Babel, wires scripts
- `android-frame.jsx` — device bezel (from starter)
- `tweaks-panel.jsx` — tweak panel starter (design-time only, don't ship)
- `src/app.jsx` — root, state, routing, tweaks, ambient music, demo notif timer
- `src/ui.jsx` — **shared UI: Stone (sea pebble), TabBar, Card, PrimaryBtn, ProgressRing, ScreenHeader, icons**
- `src/mandala.jsx` — **SatirMandala + MiniMandala + SATIR_RESOURCES constant**
- `src/frame.jsx` — PTDevice (custom device chrome) + MusicButton
- `src/ambient.jsx` — WebAudio ocean loop (can skip in prod or use a real asset)
- `src/notifications.jsx` — **NotifToast** (Android notification visual)
- `src/screens-home.jsx` — Home screen
- `src/screens-habits.jsx` — **Habits: DailyView, WeeklyView, HabitRow with time-picker**
- `src/screens-mandala.jsx` — **Mandala tab: MandalaTappable, PetalSheet, FieldBlock, per-resource placeholders**
- `src/screens-morning.jsx` — Morning intention flow
- `src/screens-breath.jsx` — Box breathing
- `src/screens-focus.jsx` — Focus timer
- `src/screens-break.jsx` — **Take a break: 19 breaks, filters, Surprise me**
- `src/screens-progress.jsx` — Garden / progress
- `src/screens-settings.jsx` — Settings + Evening reflection

---

## Implementation notes for Claude Code

1. **Start with the mandala**. It's the app's DNA. Port `SatirMandala` first to Jetpack Compose Canvas / SwiftUI Canvas / RN-Skia. Get the geometry right before anything else.
2. **Next: sea-pebble Stone**. Reusable. Used everywhere. Layered radial gradients on an organic oval, small random variance per seed.
3. **State is simple** — a single source of truth is fine (ViewModel / Redux / Zustand / whatever the codebase uses). Persist to local storage.
4. **Notifications need real platform scheduling** (AlarmManager/WorkManager on Android). The prototype's `setTimeout` is just a demo.
5. **The Tweaks panel is design-only** — don't ship it. Port the Settings screen's font/sound/habits controls instead.
6. **Fonts**: Fraunces is the signature. If unavailable, fall back to a contrast-y transitional italic serif; do not substitute Inter/system.
7. **No AI**. This was an explicit product constraint from the user — the category is crowded with AI-chat wellness apps and the user doesn't want that.
