# Sub-project #4: Action screens — Spec

**Parent:** Perfectly Tranquillo. Designs at `docs/design/README.md` § 5–9 + `docs/design/prototype/src/screens-{morning,breath,focus,break}.jsx` (evening exports also live in `screens-morning.jsx`).

**Job:** build the 5 quick-action flows reachable from Home cards: Morning intention, Evening reflection, Breath (4·4·4·4 box breathing), Focus (10/15/25/45 timer), Take a break (19 micro-breaks + Surprise-Me). Each completion awards a Stone (in-memory list — actual persistence is #5) and bumps a mapped Satir resource.

## Scope (in)

- **Morning intention flow** — 3-step soft-edge flow per design § 5: mood selection (4 cards), intention picker (chips + custom input), optional good-thing one-liner. On finish: sets `state.morningDone = true`, `state.intent`, `state.morningMood`, optional `state.goodThing`, awards a `moon` stone, fills `intellectual.am += 0.4`.
- **Evening reflection** — single screen with two prompts (mandala-phase nudge, optional note). Sets `state.eveningDone = true`, awards `sand` stone, fills `spiritual.pm += 0.4`.
- **Breath** — full-screen box breathing, single expanding/contracting circle with phase label (Inhale/Hold/Exhale/Hold each 4s). Sessions: 1 / 3 / 5 min picker. On complete: awards `moon` stone, fills `emotional.pm += 0.3`.
- **Focus** — countdown timer with 4 chip presets (10/15/25/45). Big circular progress ring + minutes-remaining text + play/pause/stop. On complete: awards `deep`-grade pebble (re-use `Sand` since deep isn't in StoneKind enum yet — or add `Deep` to StoneKind), fills `physical.pm += 0.3` and `intellectual.pm += 0.2`.
- **Take a break** — 19 curated breaks across 4 categories (Tiny / Sensory / Body / World) with category filter chips + Surprise-Me hero card at top. Tapping a break starts a 240 dp countdown ring with the prompt text below. On complete: awards `jade` stone, fills the break's mapped resource +0.22.

- All 5 screens share a soft fade-in (500ms ease-out, 6 dp Y translate).
- ViewModel additions: `awardStone(kind)`, `addToResourceFill(key, phase, delta)`. Stones list in-memory only for #4 (persisted in #5 via Stones table).
- Replace the "coming soon" placeholders for `MorningRoute/EveningRoute/BreathRoute/FocusRoute/BreakRoute` with these real screens.

## Out of scope

- Real local notifications (#6)
- Stones list persistence (#5)
- Audio chime / ambient sound (#7)
- Haptic feedback (nice-to-have, defer)

## File map

**Create:**
- `screens/actions/MorningScreen.kt` (3-step Pager flow)
- `screens/actions/EveningScreen.kt`
- `screens/actions/BreathScreen.kt`
- `screens/actions/FocusScreen.kt`
- `screens/actions/BreakScreen.kt` (list + filter + Surprise-Me + countdown sub-screen)
- `screens/actions/Breaks.kt` — the 19 break definitions (id, kind, title, subtitle, durationSec, mappedResource, prompt, color)
- `screens/actions/PrimaryActionButton.kt` — full-round Sand button used across action screens
- `data/StoneKind.kt` — add `Deep` (jade-deep variant) if needed; verify enum currently has Moon/Jade/Shell/Coral/Sand and add Deep if missing

**Modify:**
- `data/AppViewModel.kt` — add stones state (in-memory `List<StoneKind>` for now), `addStone`, `addResourceFill`
- `nav/RootNavHost.kt` — wire 5 routes to real screens (replace PlaceholderDestination)
- `app/build.gradle.kts` — bump versionCode 5, versionName 0.5.0

## Verification

- assembleDebug clean
- Existing instrumentation tests pass
- Manual: each Home action card opens the right flow; finishing a flow returns to Home and visibly fills the right mandala wedge AM/PM band; Breath orb pulses; Focus countdown runs.
- v0.5.0 APK on GitHub release with both filenames
