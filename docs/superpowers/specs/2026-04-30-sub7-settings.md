# Sub-project #7: Settings — Spec

**Parent:** Perfectly Tranquillo. Designs at `docs/design/README.md` § 10 (Settings).

**Job:** Replace the Settings placeholder with a real screen. Persist preferences. Wire the palette so the bg actually changes when the user picks one.

## Scope (in)

- **PrefsStore** additions: `palette` (string id, default `deep_tide`), `notifMode` (string `silent` | `sound` | `vibrate`, default `sound`).
- **Theme:** `PerfectlyTranquilloTheme` accepts a `palette: Palette` and provides `LocalPalette`. `MainActivity` reads the chosen palette from `PrefsStore` (collected lazily) and passes it in.
- **Settings sections (in order):**
  1. **About you** — single text field for `userName`.
  2. **Appearance** — 4 palette cards (Deep Tide / Tidepool / Sea Glass / Kelp Forest), each a small swatch + name + selected state.
  3. **Notifications** — radio (silent / sound / vibrate) + a "Send a sample reminder" button that calls `viewModel.fireDemoReminder()`.
  4. **Sound** — chime-on-tap toggle (cosmetic; audio engine is not in scope).
  5. **Data** — "Reset today" (with confirm) clears today's mandala entries + habit fills + action stones; "Clear all" (with confirm) wipes all rows.
- **Notif mode honored** by `HabitReminderWorker`: if mode is `silent`, skip posting.
- Replace `Route.Settings` placeholder with `SettingsScreen(viewModel)`.

## Out of scope (defer)

- Font picker — needs `Typography` refactor (multiple FontFamilies). Stub label "More fonts coming soon".
- Ambient sound picker (waves / birds / bowls / music) — needs ExoPlayer + assets.
- Light mode — prototype is dark-only.
- Notification mode "vibrate-only" semantics with channel-level audio override (Android O+ ignores per-notif sound). For v0.8.0 we treat `vibrate` as `sound` for posting; `silent` skips entirely.

## File map

**Create:**
- `screens/settings/SettingsScreen.kt`
- `screens/settings/PaletteCard.kt`
- `screens/settings/SettingsSection.kt` (small reusable card+title shell)

**Modify:**
- `data/PrefsStore.kt` — add `palette`, `notifMode` keys + flows + setters; add `clearAll()` for "Clear all" data option; defaults exposed as constants.
- `data/AppViewModel.kt` — expose `palette`, `notifMode`, setters; add `resetToday()` and `clearAll()` data ops; add `setUserName`, `setSoundEnabled`, `setNotifMode`, `setPalette`.
- `data/AppDatabase.kt` — add a `clearAll` method touching all DAOs (or use `clearAllTables()` on the DB).
- `ui/theme/Theme.kt` — accept `palette` param; provide `LocalPalette`.
- `MainActivity.kt` — collect chosen palette from `PrefsStore`, pass to theme.
- `notifications/HabitReminderWorker.kt` — read `notifMode`; if `silent`, skip post.
- `nav/RootNavHost.kt` — wire `SettingsScreen`.
- `app/build.gradle.kts` — bump versionCode 8, versionName 0.8.0.

## Verification

- assembleDebug clean
- Manual: pick Tidepool → background gradient swaps live; close + reopen app → still Tidepool; pick Silent → demo reminder doesn't post; reset today → today's mandala fills + habit checks vanish; clear all → garden empties.
- v0.8.0 commit visible.
