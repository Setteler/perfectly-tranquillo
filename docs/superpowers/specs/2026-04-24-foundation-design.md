# Foundation (Sub-project #0) — Design

**Parent project:** Perfectly Tranquillo — gamified mental-wellness Android app. Full design handoff at `docs/design/README.md`.

**This sub-project's job:** turn the empty Kotlin + Compose scaffold into a real project skeleton — design tokens, navigation, persistence, fonts, dependencies — so every subsequent sub-project has a consistent foundation to build on. **No feature behavior yet.** The app should install, run, show the four tabs with placeholder screens, and look and feel like Perfectly Tranquillo (colors, fonts, gradient background).

---

## Scope

**In scope:**
- Rename complete (already done)
- Add runtime dependencies needed by later sub-projects (navigation, persistence, background work, fonts, lifecycle)
- Set up design-token layer: colors, typography (Fraunces + Quicksand), spacing, radii, shadow tokens
- Background gradient (sea cool → warm sand blend) as a reusable Composable
- 4-tab navigation skeleton (Home / Habits / Mandala / Garden) using Navigation-Compose + Material3 `NavigationBar`
- Settings entry via gear icon in app chrome (top-right)
- 5 placeholder screens (4 tabs + Settings): each shows screen name + "coming soon"
- DataStore + Room set up empty (migrations, DI not needed — plain singletons)
- Build & install on Pixel 6 emulator; verify navigation, colors, fonts render correctly

**Out of scope** (deferred to later sub-projects):
- SatirMandala drawing, Stone pebble — sub-project #1
- Home content, Mandala tab interactions, PetalSheet — #2
- Habits UI, weekly grid, time-pickers — #3
- Morning / Evening / Breath / Focus / Break flows — #4
- Progress / Garden — #5
- Notifications / WorkManager scheduling — #6
- Settings functionality (font picker, sound, habits CRUD) — #7
- Ambient audio — #7
- Tests — will be added incrementally per sub-project where meaningful

---

## Dependency additions (Gradle)

Add to `gradle/libs.versions.toml`:

| Library | Purpose | Version |
|---------|---------|---------|
| `androidx.navigation:navigation-compose` | 4-tab + stack navigation | 2.8.4 |
| `androidx.lifecycle:lifecycle-viewmodel-compose` | ViewModels in Compose | 2.8.7 |
| `androidx.datastore:datastore-preferences` | Small user prefs (font, sound) | 1.1.1 |
| `androidx.room:room-runtime` + `room-ktx` | Persistence (habits, entries, stones) | 2.6.1 |
| `androidx.room:room-compiler` (ksp) | Annotation processor | 2.6.1 |
| `com.google.devtools.ksp` (plugin) | KSP for Room | 2.0.21-1.0.28 |
| `androidx.work:work-runtime-ktx` | Daily reminder scheduling (stubbed now, used in #6) | 2.10.0 |
| `com.google.accompanist:accompanist-navigation-animation` | Optional — skip if Navigation-Compose's built-in animations are adequate | — |

Google Fonts: use Compose's built-in `androidx.compose.ui:ui-text-google-fonts` + the Downloadable Fonts provider. No extra key; uses AndroidX default provider certs. Fraunces, Quicksand, JetBrains Mono.

---

## Package structure

```
com.methoda.tranquillo/
├── MainActivity.kt                 — hosts NavHost
├── nav/
│   ├── Routes.kt                   — sealed class of destinations
│   └── RootNavHost.kt              — NavHost + bottom bar wiring
├── ui/
│   ├── theme/
│   │   ├── Color.kt                — all tokens (Ink, Sky, Sand, etc.)
│   │   ├── Type.kt                 — Fraunces / Quicksand / JetBrains Mono text styles
│   │   ├── Shape.kt                — 14 / 18 / 20 / 22 / 28 / 100 radii
│   │   ├── Dimens.kt               — spacing tokens (16, 20, 120 padding, etc.)
│   │   └── Theme.kt                — PerfectlyTranquilloTheme
│   ├── chrome/
│   │   ├── SeaBackground.kt        — gradient background Composable
│   │   └── BottomTabBar.kt         — floating rounded tab bar
│   └── placeholder/
│       └── PlaceholderScreen.kt    — eyebrow + title + "coming soon"
├── data/
│   ├── AppDatabase.kt              — empty Room DB (entities added later)
│   └── PrefsStore.kt               — DataStore for font/sound (fields stubbed)
└── PerfectlyTranquilloApp.kt       — Application class, provides DB + prefs
```

Screens live at `screens/<name>/` in later sub-projects.

---

## Design tokens

### Colors (sRGB hex, converted from the design's oklch values)

Already present in `Color.kt`. Keep as-is unless visual QA says otherwise after background gradient is applied.

### Typography — `Type.kt`

Use `GoogleFont` provider with these roles:

- `display` — Fraunces, 400, italic, letter-spacing −0.005em. For titles, quotes, affirming body text.
- `ui` — Quicksand, 500. For buttons, labels, chips, hints.
- `mono` — JetBrains Mono, 400. For streak numbers, metadata, time inputs.

Text styles in a Material3 `Typography`:
- `displayLarge`, `displayMedium`, `displaySmall` → Fraunces italic
- `bodyLarge`, `bodyMedium`, `labelLarge` → Quicksand
- `labelSmall` → JetBrains Mono

### Shapes — `Shape.kt`

Material3 `Shapes`: `extraSmall 14.dp`, `small 18.dp`, `medium 20.dp`, `large 22.dp`, `extraLarge 28.dp`. Pills (100dp) applied inline.

### Dimens — `Dimens.kt`

Object with: `ScreenHorizontalPadding 20.dp`, `ScreenVerticalPadding 16.dp`, `ScreenBottomClearance 120.dp`, `CardPadding 18.dp`, `TabBarEdgeInset 14.dp`, `TabBarBottomOffset 40.dp`.

---

## Background gradient — `SeaBackground.kt`

Composable that paints the page gradient described in the design's "Page gradient" token. Implementation: `Box(Modifier.fillMaxSize().drawBehind { ... })` using two radial brushes (one warm, one cool) composited over a vertical linear brush. Brush params derived from the oklch spec — no runtime "warmth" knob needed in this sub-project (comes later in Settings if desired).

---

## Navigation

- Use `NavHostController` at `MainActivity` level.
- Routes sealed class with 5 destinations: `Home`, `Habits`, `Mandala`, `Garden`, `Settings`.
- `BottomTabBar` composable renders the 4 tab destinations (not Settings) using Material3 `NavigationBar` styled to the floating rounded look: `RoundedCornerShape(28.dp)`, `Modifier.padding(horizontal = 14.dp, bottom = 40.dp)`, surface color `Ink2` with 0.72 alpha, `graphicsLayer { clip = true }` + backdrop blur approximation via `Modifier.blur()` + inner translucent layer.
- Settings reached via a small gear icon `IconButton` overlaid top-right inside a `Scaffold` `topBar` slot — not a drawer, not a tab.
- Tab state persists across navigation via `rememberNavController()` + `NavBackStackEntry`.

---

## Placeholder screen

`PlaceholderScreen(title: String, eyebrow: String = "")` — a centered `Column` with the eyebrow (Quicksand `labelSmall` uppercase tracked) and the title (Fraunces `displayMedium` italic) plus a small italic line "coming soon · sub-project #N". Rendered over `SeaBackground`.

Each of the 5 destinations wires this with its own copy (eyebrow and title pulled from the design README).

---

## Data layer (scaffolded, not exercised)

- `AppDatabase` — abstract `RoomDatabase` subclass with no DAOs yet. Created with `Room.databaseBuilder(...).build()` in `PerfectlyTranquilloApp`. Later sub-projects add entities.
- `PrefsStore` — wraps `Context.dataStore` (Preferences DataStore). Exposes `sound: Flow<Boolean>` and `fontPair: Flow<String>` with sensible defaults (`true`, `"fraunces"`). No writers yet.
- App class exposes both as lazy properties. Not using Hilt/Koin — these are two singletons, DI is unnecessary overhead for this size.

---

## Verification

Foundation is "done" when:
1. `./gradlew assembleDebug` succeeds from the command line.
2. `adb install` places the APK on the Pixel 6 emulator and it launches without crash.
3. App opens on Home placeholder, sea gradient visible, Fraunces + Quicksand rendering (no fallback system fonts).
4. Tapping each of the 4 tabs shows the correct placeholder screen; active tab indicated in bottom bar.
5. Gear icon top-right navigates to Settings placeholder; back arrow returns.
6. No lint errors blocking build. No `TODO()` left in production code paths.

---

## Open questions (flag before implementation plan)

None for this sub-project — design tokens are pinned to the handoff, navigation model is explicit (README §"Navigation model"), and no feature interactions are in scope yet.

---

## Next

On approval, invoke `writing-plans` to produce an implementation plan from this spec.
