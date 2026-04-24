# Sub-project #2: Home + Mandala tab — Spec

**Parent:** Perfectly Tranquillo. Design at `docs/design/README.md`. Components from #1: `SatirMandala`, `Stone`, `MiniMandala`, palette system, Caveat+Nunito.

**This sub-project's job:** make the first two real screens. Home shows a single-glance status of today; Mandala lets the user fill the 8 resource petals AM/PM. Everything persists. No more placeholders for these two tabs.

---

## Scope

**In scope:**
- `AppState` data class + `AppViewModel` — single in-memory source of truth for today (`name, intent, goodThing, mandalaEntries, resources, morningDone, eveningDone, currentPhase`).
- Room entities/DAO for **mandala entries** (date, key, phase, kind=resource|challenge, text). Auto-derived `resources` fills (am/pm 0..1) computed from entries.
- Replace `SchemaMarker` placeholder with the real `MandalaEntryEntity` + DAO; bump Room version to 2 with destructive migration (it's only ever held the marker).
- `PrefsStore` adds `userName: Flow<String>` (default "friend") so greeting renders.
- **HomeScreen** per design § 1:
  - Eyebrow "DAY-OF-WEEK · DAY N" (cool blue, uppercase, tracked) — N = day-of-year mod 999 for now (real "since-installed" tracking is #5)
  - Greeting H1 "Bloom gently, {name}." — Caveat 36 sp, time-aware variants ("Bloom gently" AM, "Soften, " PM, "Drift on, " evening)
  - Intention pill (warm-gold outlined, full-round) showing `state.intent` or empty hint "set a gentle intention"
  - Hero `SatirMandala` 240 dp, `animate = true`, fills from `state.resources`
  - "Daily snapshot" row: 3 stat pills (mandala petals filled / habits done / streak) — habits show 0/0 until #3, streak 0
  - "One good thing" card: glass card, Caveat 22 sp text input, placeholder "the sound of rain before I even opened my eyes". Saves on focus loss.
  - Quick-action grid (2 columns) with 5 cards: Morning, Evening, Breath, Focus, Take a break. Each card = glass surface + small SVG-ish icon (use Material Icons for now) + Caveat title + 1-line subtitle (Nunito). All 5 navigate to their (non-yet-built) destinations — for now route to a `PlaceholderScreen` saying "coming soon · sub-project #4". Cards visually look real.
- **MandalaScreen** per design § 3:
  - Header: "Your mandala today" Caveat 28 sp + AM/PM toggle (two pills) — auto-selects current phase based on hour-of-day (`< 12 → AM`, else `PM`); user can override
  - 280 dp `SatirMandala` with `onPetalTap` wired to open PetalSheet
  - Resource list below: 8 rows, one per `ResourceKey`, each shows a small `PetalDot` (filled state per fill value), label (Caveat 20 sp), and a 1-line preview of the current entry (`✿ <resource text>` if set, else placeholder "tap to add")
- **PetalSheet** (Material3 `ModalBottomSheet`):
  - Drag handle
  - Header: phase glyph (small `Stone` of `Moon` for AM, `Sand` for PM) + eyebrow ("This morning" / "This evening") + Caveat 24 sp title (resource label) + close `×`
  - Italic Caveat hint line
  - Resource field: chip row with `✿` icon + "RESOURCE" eyebrow + accent-colored 2-row text field (sky for AM, sand for PM), Caveat 18 sp, placeholder rotates per resource+phase (use the `screens-mandala.jsx` placeholder map — implementer can grep)
  - Challenge field: chip row with `◌` + "CHALLENGE · optional" + same input pattern but accent coral
  - "Save this petal ✿" button (Sand-bg, full-round, on-surface darker text)
  - Saving inserts/updates a `MandalaEntryEntity`, recomputes `resources[key].am/pm` based on entry (resource only ⇒ 0.9; challenge only ⇒ 0.3; both ⇒ 0.6 per v1 README), closes the sheet, mandala animates the new fill (just an `animate*AsState` on the Float)

**Out of scope:**
- Real morning/evening flow content — destination still PlaceholderScreen (#4)
- Habit progress in the snapshot pill (still 0/0 — wired in #3)
- Notifications (#6)
- Pebble awarding on save (#5)
- Settings/font picker (#7)
- Midnight rollover & history archive (#5)
- Real "DAY N" since-installed counter (#5)

---

## File map

**Create:**
- `data/MandalaEntryEntity.kt` (replaces SchemaMarker — Entity + Dao + a small repository)
- `data/AppViewModel.kt` (Compose-friendly ViewModel; uses Application's db + prefs)
- `screens/home/HomeScreen.kt`
- `screens/home/IntentionPill.kt`
- `screens/home/QuickActionCard.kt`
- `screens/home/SnapshotRow.kt`
- `screens/home/GoodThingCard.kt`
- `screens/mandala/MandalaScreen.kt`
- `screens/mandala/PetalSheet.kt`
- `screens/mandala/PetalRow.kt` (the resource-list row with PetalDot)
- `screens/mandala/PetalDot.kt`
- `screens/mandala/Placeholders.kt` (the placeholder text map for the resource/challenge inputs by resource+phase — copy from prototype)
- `screens/PlaceholderDestination.kt` (small PlaceholderScreen wrapper used for the action routes — keeps existing PlaceholderScreen unchanged)

**Modify:**
- `data/AppDatabase.kt` — replace `SchemaMarker` with `MandalaEntryEntity`, bump version 2, fallbackToDestructiveMigration
- `data/PrefsStore.kt` — add `userName: Flow<String>` + `setUserName`
- `nav/Routes.kt` — add destinations: `MorningRoute`, `EveningRoute`, `BreathRoute`, `FocusRoute`, `BreakRoute`
- `nav/RootNavHost.kt` — Home + Mandala routes use real screens; the 5 action routes use `PlaceholderDestination` with friendly "coming soon · sub-project #4" content
- `ui/placeholder/PlaceholderScreen.kt` — REMOVE the mini mandala + stone row preview (it served #1's purpose); restore to plain eyebrow+title+subtitle for the remaining tabs (Habits, Garden) + the action stubs
- `app/build.gradle.kts` — bump versionCode 3, versionName "0.3.0"

**No new dependencies.**

---

## Verification

- `./gradlew assembleDebug` succeeds (note: Room schema change requires destructive migration — handled in builder)
- `connectedDebugAndroidTest` passes (existing nav test); add a quick test that taps a petal on the Mandala screen and asserts the bottom sheet opens
- Manual smoke:
  - Home shows greeting "Bloom gently, friend." (or time-of-day variant), an intention pill, a 240 dp mandala (mostly empty), 3 snapshot pills, the good-thing card, and 5 quick-action cards
  - Mandala tab shows AM/PM toggle (defaults to current phase), 280 dp mandala, 8 resource rows
  - Tapping a petal opens the bottom sheet with resource + challenge fields; typing into resource and pressing Save closes the sheet AND the mandala wedge fills with a soft animation; reopening the sheet shows the saved text
  - Quick-action cards on Home open a placeholder route ("coming soon")
  - Habits / Garden tabs still work (now plain placeholders, no mandala preview)
  - Gear → Settings still works
- v0.3.0 APK shipped to GitHub release v0.3.0; tag `sub2-complete`
