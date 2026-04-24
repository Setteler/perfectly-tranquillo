# Sub-project #1: Foundation v2 update + Mandala DNA — Spec

**Parent:** Perfectly Tranquillo. Full design at `docs/design/README.md` (v2).

**This sub-project's job:** retrofit the Foundation to match the v2 design (font + palette system), then build the two reusable signature components used by every later sub-project: `SatirMandala` and `Stone` (sea pebble). Foundation #0 shipped a generic shell; this turns it into something that LOOKS like Perfectly Tranquillo.

---

## Scope

**In scope:**

1. **Typography swap** — default display font becomes **Caveat 500** (handwritten casual). Body becomes **Nunito 300/400/500/600**. Keep Fraunces + Quicksand wired as an alternate font pair (won't be user-pickable until #7, but must be ready). JetBrains Mono stays for numbers.
2. **Palette system** — introduce `Palette` data class + 4 named palettes (`DeepTide` default, `Tidepool`, `SeaGlass`, `KelpForest`). Each carries: 3 base gradient stops, warm-highlight color/alpha, cool-accent color/alpha, optional `waves` flag (only Kelp). `LocalPalette` CompositionLocal exposed app-wide. `SeaBackground` reads current palette. (Palette switching UI is #7.)
3. **Resource model** — Kotlin data classes for the 8 Satir resources (`physical, intellectual, emotional, sensory, interactional, nutritional, contextual, spiritual`), display labels (Body, Mind, Heart, Senses, Connection, Nourish, Place, Spirit), default ordering (clockwise from 12 o'clock).
4. **`SatirMandala` Composable** — `Canvas`-based, 8 wedges with AM/PM bands per the geometry in `docs/design/README.md` § "The mandala (custom drawing)". Props: `size`, `resources` (Map<ResourceKey, AmPmFill>), `highlight: ResourceKey?`, `onPetalTap: (ResourceKey) -> Unit`, `showLabels: Boolean`, `animate: Boolean`, `complexity: Simple|Full`.
5. **`Stone` Composable** — sea pebble. Organic oval (not a circle). Seeded variance per spec from v1 README. 5 colors: `Moon`, `Jade`, `Shell`, `Coral`, `Sand`. Props: `kind`, `size`, `dim`, `seed`. Used in #2 (intention pill), #3 (habit toggle), #5 (Garden jar), #4 (action rewards).
6. **`MiniMandala` Composable** — smaller-scale `SatirMandala` for week strip in #5. Same drawing routine, simplified labels off.
7. **Verification** — update `PlaceholderScreen` to render a small `SatirMandala` (140 dp) and a row of 5 `Stone`s under the title so we can visually confirm both components on every tab. Smoke test still passes.

**Out of scope:**
- Tap interactions on the mandala beyond the prop hook (no PetalSheet — that's #2)
- Animated petal-fill sweep (v1 hint to use PathMeasure) — fills are static for now; tap+animate comes in #2
- Palette picker UI (#7)
- Settings font picker (#7)
- Stone awarding logic (#5)
- Resource entry persistence (#2)

---

## File map

**Create:**
- `app/src/main/java/com/methoda/tranquillo/data/Resources.kt` — ResourceKey enum, label map, ordering
- `app/src/main/java/com/methoda/tranquillo/ui/theme/Palette.kt` — Palette data class, 4 named palettes, LocalPalette, helpers
- `app/src/main/java/com/methoda/tranquillo/ui/components/SatirMandala.kt`
- `app/src/main/java/com/methoda/tranquillo/ui/components/Stone.kt`
- `app/src/main/java/com/methoda/tranquillo/ui/components/MiniMandala.kt`

**Modify:**
- `app/src/main/java/com/methoda/tranquillo/ui/theme/Type.kt` — add Caveat + Nunito families, make Caveat the displayLarge/Medium/Small + headlineMedium font, Nunito takes over titleLarge / bodyLarge / bodyMedium / labelLarge / labelMedium. Eyebrow uses Nunito 600 with letter-spacing.
- `app/src/main/java/com/methoda/tranquillo/ui/theme/Theme.kt` — wire LocalPalette provider, default Deep Tide
- `app/src/main/java/com/methoda/tranquillo/ui/chrome/SeaBackground.kt` — use current palette via LocalPalette
- `app/src/main/java/com/methoda/tranquillo/ui/placeholder/PlaceholderScreen.kt` — render mini mandala + 5 stones under title (proof of components)
- `app/src/main/java/com/methoda/tranquillo/ui/theme/Color.kt` — keep existing color names but they become "Deep Tide" semantic; add palette-specific raw colors

**No new dependencies needed.**

---

## Mandala geometry (from v2 README)

- 8 wedges, each 45° minus a 2.5° gap on each side (so 40° drawn arc + 5° total gap)
- `outerR = size * 0.44`, `midR = size * 0.31`, `innerR = size * 0.18`
- AM band: innerR → midR
- PM band: midR → outerR
- Ring strokes at `Color(0xFFF5F1E8).copy(alpha = 0.28f)` between bands
- Soft pale core circle at center, radius `size * 0.08`
- Empty fill: subtle tint (alpha 0.04 on the band base color)
- AM filled: cool blue `oklch(0.82 0.07 210)` ≈ `Color(0xFFA5C7DE)` × 0.85 alpha
- PM filled: warm gold `oklch(0.86 0.09 78)` ≈ `Color(0xFFE8C57A)` × 0.85 alpha
- Order clockwise from 12 o'clock: physical, intellectual, emotional, sensory, interactional, nutritional, contextual, spiritual
- `animate = true` ⇒ subtle breathe scale on the central core (1.0 ↔ 1.06 over 6 s, ease-in-out)

## Stone geometry (from v1 README)

- Organic oval — `Modifier.clip(GenericShape { ... })` reproducing CSS `border-radius: 50% 48% 52% 50% / 50% 52% 48% 50%`
- 5 color kinds, each a 3-stop palette (a/b/c = highlight/mid/shadow). Approximate hex, refine later:
  - `Moon`: a `#F5EFD8` b `#E0D7B5` c `#998E66`
  - `Jade`: a `#C8E5D2` b `#86B097` c `#4D7563`
  - `Shell`: a `#F2E6D5` b `#D2BFA7` c `#8C7A65`
  - `Coral`: a `#F4C9B5` b `#D89778` c `#9D5F47`
  - `Sand`: a `#EAD9B0` b `#C9B58D` c `#7E6E50`
- Seeded variance per `seed: Int`:
  - aspect ratio in `[0.82, 0.96]`
  - tilt in `[-18°, 18°]`
  - highlight position (radial gradient center) at `(0.26..0.44, 0.22..0.36)` relative
- Speckles: 2 small dark dots, position seeded; only when `size >= 20.dp`
- Shadow: `Modifier.shadow(...)` with offset (0, 3.dp), blur 4.dp, color black 28% alpha

---

## Verification

- `./gradlew assembleDebug` succeeds
- App installs and launches on Pixel 6 emulator
- Each tab placeholder shows a rendered 140 dp mandala + a row of 5 stones (one per kind)
- Caveat is rendering (handwritten, not Fraunces) for the title text
- Nunito is rendering for body / labels
- The bottom tab bar still works
- Existing instrumentation test still passes
- New version: `versionName = "0.2.0"`, `versionCode = 2`
- Tag pushed: `sub1-complete`, GitHub Release v0.2.0 with attached APK

---

## Open questions

None at spec-write time. Implementer should fall back to design source files (`docs/design/prototype/src/`) if any visual ambiguity arises.
