# Foundation (Sub-project #0) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Turn the current bare Kotlin + Compose scaffold into an installable shell of Perfectly Tranquillo with design tokens, 4-tab navigation, gear-icon Settings, background gradient, and empty data/prefs plumbing — no feature behavior.

**Architecture:** Single-activity Compose app. `PerfectlyTranquilloApp` (Application) owns a Room DB and a DataStore prefs store (both scaffolded empty). `MainActivity` hosts `RootNavHost`, which embeds a `NavHost` over the Material3 `Scaffold` slots (topBar = gear IconButton, bottomBar = `BottomTabBar`). 5 placeholder screens (Home / Habits / Mandala / Garden / Settings).

**Tech Stack:** Kotlin 2.0.21, Jetpack Compose (BOM 2024.12.01), Material3, Navigation-Compose 2.8.4, Room 2.6.1 + KSP, DataStore 1.1.1, WorkManager 2.10.0, Google Fonts Compose (Fraunces + Quicksand + JetBrains Mono), min SDK 24, compile/target SDK 35, AGP 8.7.3.

**Spec:** `docs/superpowers/specs/2026-04-24-foundation-design.md`
**Full product design:** `docs/design/README.md`

**Working dir for all commands:** `/Users/metclaude/Desktop/perfectly-tranquillo`

---

## File map

**Modify:**
- `gradle/libs.versions.toml` — add versions + libraries + KSP plugin
- `build.gradle.kts` (root) — alias KSP plugin
- `app/build.gradle.kts` — apply KSP, wire deps
- `app/src/main/AndroidManifest.xml` — register `PerfectlyTranquilloApp` via `android:name`
- `app/src/main/java/com/methoda/tranquillo/MainActivity.kt` — replace Greeting with `RootNavHost`
- `app/src/main/java/com/methoda/tranquillo/ui/theme/Theme.kt` — wire typography + shapes

**Create:**
- `app/src/main/java/com/methoda/tranquillo/PerfectlyTranquilloApp.kt`
- `app/src/main/java/com/methoda/tranquillo/nav/Routes.kt`
- `app/src/main/java/com/methoda/tranquillo/nav/RootNavHost.kt`
- `app/src/main/java/com/methoda/tranquillo/ui/theme/Type.kt`
- `app/src/main/java/com/methoda/tranquillo/ui/theme/Shape.kt`
- `app/src/main/java/com/methoda/tranquillo/ui/theme/Dimens.kt`
- `app/src/main/java/com/methoda/tranquillo/ui/chrome/SeaBackground.kt`
- `app/src/main/java/com/methoda/tranquillo/ui/chrome/BottomTabBar.kt`
- `app/src/main/java/com/methoda/tranquillo/ui/placeholder/PlaceholderScreen.kt`
- `app/src/main/java/com/methoda/tranquillo/data/AppDatabase.kt`
- `app/src/main/java/com/methoda/tranquillo/data/PrefsStore.kt`

**Testing note:** Foundation is structural scaffolding — there's no business logic to TDD. Verification is: `./gradlew assembleDebug` passes + `adb install` succeeds + manual smoke test (open each tab, confirm gear navigates to Settings, confirm Fraunces/Quicksand render). The last task adds one Compose instrumentation test that exercises tab navigation, to give future sub-projects a harness to extend.

---

### Task 1: Add dependency versions + KSP to version catalog

**Files:**
- Modify: `gradle/libs.versions.toml`

- [ ] **Step 1: Replace entire file contents**

```toml
[versions]
agp = "8.7.3"
kotlin = "2.0.21"
ksp = "2.0.21-1.0.28"
coreKtx = "1.15.0"
lifecycleRuntimeKtx = "2.8.7"
lifecycleViewmodelCompose = "2.8.7"
activityCompose = "1.9.3"
composeBom = "2024.12.01"
navigationCompose = "2.8.4"
datastorePreferences = "1.1.1"
room = "2.6.1"
workManager = "2.10.0"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycleRuntimeKtx" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycleViewmodelCompose" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
androidx-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
androidx-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-ui-text-google-fonts = { group = "androidx.compose.ui", name = "ui-text-google-fonts" }
androidx-material3 = { group = "androidx.compose.material3", name = "material3" }
androidx-material-icons-extended = { group = "androidx.compose.material", name = "material-icons-extended" }
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }
androidx-datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastorePreferences" }
androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
androidx-work-runtime-ktx = { group = "androidx.work", name = "work-runtime-ktx", version.ref = "workManager" }
androidx-ui-test-manifest = { group = "androidx.compose.ui", name = "ui-test-manifest" }
androidx-ui-test-junit4 = { group = "androidx.compose.ui", name = "ui-test-junit4" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
```

- [ ] **Step 2: Commit**

```bash
git add gradle/libs.versions.toml
git commit -m "chore: add nav/room/datastore/ksp/fonts/workmanager to version catalog"
```

---

### Task 2: Alias KSP in root build script

**Files:**
- Modify: `build.gradle.kts`

- [ ] **Step 1: Replace file contents**

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
}
```

- [ ] **Step 2: Commit**

```bash
git add build.gradle.kts
git commit -m "chore: alias KSP plugin at root"
```

---

### Task 3: Apply KSP and wire dependencies in app module

**Files:**
- Modify: `app/build.gradle.kts`

- [ ] **Step 1: Replace file contents**

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.methoda.tranquillo"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.methoda.tranquillo"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.ui.text.google.fonts)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.work.runtime.ktx)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
}
```

- [ ] **Step 2: Verify Gradle can resolve the catalog**

Run:
```bash
./gradlew help -q
```

Expected: exit code 0, no `Unresolved reference` errors. First run will download dependencies (minutes). If you see a "Gradle distribution" download, that's expected for the first sync.

- [ ] **Step 3: Commit**

```bash
git add app/build.gradle.kts
git commit -m "feat: wire nav/room/datastore/fonts/workmanager into app module"
```

---

### Task 4: Add spacing tokens

**Files:**
- Create: `app/src/main/java/com/methoda/tranquillo/ui/theme/Dimens.kt`

- [ ] **Step 1: Create file**

```kotlin
package com.methoda.tranquillo.ui.theme

import androidx.compose.ui.unit.dp

object Dimens {
    val ScreenHorizontalPadding = 20.dp
    val ScreenVerticalPadding = 16.dp
    val ScreenBottomClearance = 120.dp
    val CardPadding = 18.dp
    val TabBarEdgeInset = 14.dp
    val TabBarBottomOffset = 40.dp
    val CardRadiusLarge = 22.dp
    val CardRadiusInline = 20.dp
    val RowRadius = 18.dp
    val InputRadius = 14.dp
    val TabBarRadius = 28.dp
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/methoda/tranquillo/ui/theme/Dimens.kt
git commit -m "feat: add spacing/radii tokens (Dimens)"
```

---

### Task 5: Add Material3 shapes

**Files:**
- Create: `app/src/main/java/com/methoda/tranquillo/ui/theme/Shape.kt`

- [ ] **Step 1: Create file**

```kotlin
package com.methoda.tranquillo.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val PerfectlyTranquilloShapes = Shapes(
    extraSmall = RoundedCornerShape(14.dp),
    small = RoundedCornerShape(18.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/methoda/tranquillo/ui/theme/Shape.kt
git commit -m "feat: add Material3 shape scale"
```

---

### Task 6: Add typography with Google Fonts (Fraunces / Quicksand / JetBrains Mono)

The `com_google_android_gms_fonts_certs` array is bundled in `androidx.core:core` ≥ 1.7.0, so no `res/values/font_certs.xml` is needed. First launch fetches the fonts via Play Services; subsequent launches use cache. If the device has no Play Services, the family falls back gracefully to the next `Font` entry — for this app the fallback is acceptable (emulator image we use is a Google APIs image with GMS).

**Files:**
- Create: `app/src/main/java/com/methoda/tranquillo/ui/theme/Type.kt`

- [ ] **Step 1: Create file**

```kotlin
package com.methoda.tranquillo.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.methoda.tranquillo.R

private val fontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

private val Fraunces = FontFamily(
    Font(googleFont = GoogleFont("Fraunces"), fontProvider = fontProvider, weight = FontWeight.Normal, style = FontStyle.Italic),
    Font(googleFont = GoogleFont("Fraunces"), fontProvider = fontProvider, weight = FontWeight.Normal, style = FontStyle.Normal),
    Font(googleFont = GoogleFont("Fraunces"), fontProvider = fontProvider, weight = FontWeight.Medium, style = FontStyle.Italic)
)

private val Quicksand = FontFamily(
    Font(googleFont = GoogleFont("Quicksand"), fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Quicksand"), fontProvider = fontProvider, weight = FontWeight.Medium),
    Font(googleFont = GoogleFont("Quicksand"), fontProvider = fontProvider, weight = FontWeight.SemiBold)
)

private val JetBrainsMono = FontFamily(
    Font(googleFont = GoogleFont("JetBrains Mono"), fontProvider = fontProvider, weight = FontWeight.Normal)
)

val PerfectlyTranquilloTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Normal,
        fontSize = 34.sp, letterSpacing = (-0.005).em
    ),
    displayMedium = TextStyle(
        fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Normal,
        fontSize = 28.sp, letterSpacing = (-0.005).em
    ),
    displaySmall = TextStyle(
        fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Normal,
        fontSize = 22.sp, letterSpacing = (-0.005).em
    ),
    headlineMedium = TextStyle(
        fontFamily = Fraunces, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Normal,
        fontSize = 20.sp
    ),
    titleLarge = TextStyle(
        fontFamily = Quicksand, fontWeight = FontWeight.SemiBold, fontSize = 18.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Quicksand, fontWeight = FontWeight.Normal, fontSize = 16.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Quicksand, fontWeight = FontWeight.Normal, fontSize = 14.sp
    ),
    labelLarge = TextStyle(
        fontFamily = Quicksand, fontWeight = FontWeight.Medium, fontSize = 14.sp
    ),
    labelMedium = TextStyle(
        fontFamily = Quicksand, fontWeight = FontWeight.Medium, fontSize = 12.sp
    ),
    labelSmall = TextStyle(
        fontFamily = JetBrainsMono, fontWeight = FontWeight.Normal, fontSize = 11.sp
    )
)
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/methoda/tranquillo/ui/theme/Type.kt
git commit -m "feat: add typography (Fraunces/Quicksand/JetBrains Mono via Google Fonts)"
```

---

### Task 7: Wire typography + shapes into theme

**Files:**
- Modify: `app/src/main/java/com/methoda/tranquillo/ui/theme/Theme.kt`

- [ ] **Step 1: Replace file contents**

```kotlin
package com.methoda.tranquillo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val SeaColorScheme = darkColorScheme(
    primary = Sky,
    secondary = Sand,
    tertiary = Coral,
    background = Ink,
    surface = Ink2,
    surfaceVariant = Ink3,
    onPrimary = Ink,
    onSecondary = Ink,
    onTertiary = Ink,
    onBackground = Foam,
    onSurface = Foam,
    onSurfaceVariant = Mist
)

@Composable
fun PerfectlyTranquilloTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SeaColorScheme,
        typography = PerfectlyTranquilloTypography,
        shapes = PerfectlyTranquilloShapes,
        content = content
    )
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/methoda/tranquillo/ui/theme/Theme.kt
git commit -m "feat: apply typography + shapes to PerfectlyTranquilloTheme"
```

---

### Task 8: Add sea-gradient background Composable

The prototype's page gradient is a linear vertical fade (cool-to-deep) with two radial accents — one warm at 85 % × 95 %, one cool at 10 % × 10 % and 80 % × 30 %. Reproduced with `Modifier.drawBehind` composing one vertical `Brush.verticalGradient` + two radial gradients painted on top.

**Files:**
- Create: `app/src/main/java/com/methoda/tranquillo/ui/chrome/SeaBackground.kt`

- [ ] **Step 1: Create file**

```kotlin
package com.methoda.tranquillo.ui.chrome

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.methoda.tranquillo.ui.theme.Deep
import com.methoda.tranquillo.ui.theme.Ink
import com.methoda.tranquillo.ui.theme.Ink2
import com.methoda.tranquillo.ui.theme.Sand
import com.methoda.tranquillo.ui.theme.Sea
import com.methoda.tranquillo.ui.theme.Sky

@Composable
fun SeaBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Deep, Ink2, Ink)
                    ),
                    size = Size(size.width, size.height)
                )
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Sand.copy(alpha = 0.35f), Color.Transparent),
                        center = Offset(size.width * 0.85f, size.height * 0.95f),
                        radius = size.minDimension * 0.9f
                    )
                )
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Sky.copy(alpha = 0.22f), Color.Transparent),
                        center = Offset(size.width * 0.10f, size.height * 0.10f),
                        radius = size.minDimension * 0.75f
                    )
                )
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Sea.copy(alpha = 0.18f), Color.Transparent),
                        center = Offset(size.width * 0.80f, size.height * 0.30f),
                        radius = size.minDimension * 0.7f
                    )
                )
            }
    ) {
        content()
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/methoda/tranquillo/ui/chrome/SeaBackground.kt
git commit -m "feat: add SeaBackground gradient composable"
```

---

### Task 9: Add placeholder screen

**Files:**
- Create: `app/src/main/java/com/methoda/tranquillo/ui/placeholder/PlaceholderScreen.kt`

- [ ] **Step 1: Create file**

```kotlin
package com.methoda.tranquillo.ui.placeholder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.methoda.tranquillo.ui.theme.Dimens

@Composable
fun PlaceholderScreen(
    title: String,
    eyebrow: String,
    subprojectNumber: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                horizontal = Dimens.ScreenHorizontalPadding,
                vertical = Dimens.ScreenVerticalPadding
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = eyebrow.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Text(
            text = title,
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            text = "coming soon · sub-project #$subprojectNumber",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/methoda/tranquillo/ui/placeholder/PlaceholderScreen.kt
git commit -m "feat: add PlaceholderScreen composable"
```

---

### Task 10: Define navigation routes

**Files:**
- Create: `app/src/main/java/com/methoda/tranquillo/nav/Routes.kt`

- [ ] **Step 1: Create file**

```kotlin
package com.methoda.tranquillo.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FilterVintage
import androidx.compose.material.icons.outlined.Home
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Route(val path: String) {
    data object Home : Route("home")
    data object Habits : Route("habits")
    data object Mandala : Route("mandala")
    data object Garden : Route("garden")
    data object Settings : Route("settings")
}

data class TabDestination(
    val route: Route,
    val label: String,
    val icon: ImageVector
)

val TabDestinations: List<TabDestination> = listOf(
    TabDestination(Route.Home, "Home", Icons.Outlined.Home),
    TabDestination(Route.Habits, "Habits", Icons.Outlined.CheckCircle),
    TabDestination(Route.Mandala, "Mandala", Icons.Outlined.AutoAwesome),
    TabDestination(Route.Garden, "Garden", Icons.Outlined.FilterVintage)
)
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/methoda/tranquillo/nav/Routes.kt
git commit -m "feat: define nav routes + tab destinations"
```

---

### Task 11: Add floating BottomTabBar

Material3 `NavigationBar` themed to Perfectly Tranquillo's floating look: `RoundedCornerShape(28.dp)`, insets 14 dp sides / 40 dp bottom, surface of `Ink2 @ 0.72`, elevated shadow via `Card`. We don't try to reproduce the prototype's `backdrop-filter: blur(24px)` exactly — Compose can't cheaply blur behind a widget as of BOM 2024.12.01 without the `RenderEffect` API (API 31+), and the translucent fill is visually close enough for now.

**Files:**
- Create: `app/src/main/java/com/methoda/tranquillo/ui/chrome/BottomTabBar.kt`

- [ ] **Step 1: Create file**

```kotlin
package com.methoda.tranquillo.ui.chrome

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navOptions
import com.methoda.tranquillo.nav.Route
import com.methoda.tranquillo.nav.TabDestinations
import com.methoda.tranquillo.ui.theme.Dimens

@Composable
fun BottomTabBar(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStack?.destination

    Surface(
        modifier = modifier.padding(
            horizontal = Dimens.TabBarEdgeInset,
            vertical = Dimens.TabBarBottomOffset / 2
        ),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
        tonalElevation = 6.dp,
        shadowElevation = 12.dp
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp
        ) {
            TabDestinations.forEach { tab ->
                val selected = currentDestination?.hierarchy?.any { it.route == tab.route.path } == true
                NavigationBarItem(
                    selected = selected,
                    onClick = {
                        navController.navigate(tab.route.path, navOptions {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        })
                    },
                    icon = { Icon(tab.icon, contentDescription = tab.label) },
                    label = { Text(tab.label, style = MaterialTheme.typography.labelMedium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                    )
                )
            }
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/methoda/tranquillo/ui/chrome/BottomTabBar.kt
git commit -m "feat: add floating BottomTabBar"
```

---

### Task 12: Assemble RootNavHost

**Files:**
- Create: `app/src/main/java/com/methoda/tranquillo/nav/RootNavHost.kt`

- [ ] **Step 1: Create file**

```kotlin
package com.methoda.tranquillo.nav

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.methoda.tranquillo.ui.chrome.BottomTabBar
import com.methoda.tranquillo.ui.chrome.SeaBackground
import com.methoda.tranquillo.ui.placeholder.PlaceholderScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootNavHost() {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    val tabRoutes = setOf(
        Route.Home.path, Route.Habits.path, Route.Mandala.path, Route.Garden.path
    )

    SeaBackground {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                if (currentRoute in tabRoutes) {
                    CenterAlignedTopAppBar(
                        title = {},
                        actions = {
                            IconButton(onClick = { navController.navigate(Route.Settings.path) }) {
                                Icon(
                                    imageVector = Icons.Outlined.Settings,
                                    contentDescription = "Settings"
                                )
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = Color.Transparent
                        )
                    )
                }
            },
            bottomBar = {
                if (currentRoute in tabRoutes) {
                    BottomTabBar(navController)
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Route.Home.path,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Route.Home.path) {
                    PlaceholderScreen(
                        title = "Home",
                        eyebrow = "today's mandala",
                        subprojectNumber = 2
                    )
                }
                composable(Route.Habits.path) {
                    PlaceholderScreen(
                        title = "Habits",
                        eyebrow = "small things, often",
                        subprojectNumber = 3
                    )
                }
                composable(Route.Mandala.path) {
                    PlaceholderScreen(
                        title = "Mandala",
                        eyebrow = "your inner landscape",
                        subprojectNumber = 2
                    )
                }
                composable(Route.Garden.path) {
                    PlaceholderScreen(
                        title = "Garden",
                        eyebrow = "seven days of stones",
                        subprojectNumber = 5
                    )
                }
                composable(Route.Settings.path) {
                    PlaceholderScreen(
                        title = "Settings",
                        eyebrow = "tune the sea",
                        subprojectNumber = 7
                    )
                }
            }
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/methoda/tranquillo/nav/RootNavHost.kt
git commit -m "feat: add RootNavHost with 5 placeholder destinations"
```

---

### Task 13: Wire MainActivity to RootNavHost

**Files:**
- Modify: `app/src/main/java/com/methoda/tranquillo/MainActivity.kt`

- [ ] **Step 1: Replace file contents**

```kotlin
package com.methoda.tranquillo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.methoda.tranquillo.nav.RootNavHost
import com.methoda.tranquillo.ui.theme.PerfectlyTranquilloTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PerfectlyTranquilloTheme {
                RootNavHost()
            }
        }
    }
}
```

- [ ] **Step 2: Compile check**

Run:
```bash
./gradlew assembleDebug -q
```

Expected: BUILD SUCCESSFUL. Fix any unresolved imports before moving on.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/methoda/tranquillo/MainActivity.kt
git commit -m "feat: MainActivity hosts RootNavHost"
```

---

### Task 14: Add empty Room database

**Files:**
- Create: `app/src/main/java/com/methoda/tranquillo/data/AppDatabase.kt`

Room rejects `@Database` with an empty `entities` list. To keep the scaffolded DB compiling without introducing a real entity prematurely, this task adds a private `SchemaMarker` entity + DAO that will be removed in sub-project #2 when real entities arrive.

- [ ] **Step 1: Create file**

```kotlin
package com.methoda.tranquillo.data

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase

@Entity(tableName = "schema_marker")
internal data class SchemaMarker(
    @PrimaryKey val id: Int = 0,
    @ColumnInfo(name = "created_at") val createdAt: Long = 0L
)

@Dao
internal interface SchemaMarkerDao {
    @Query("SELECT COUNT(*) FROM schema_marker")
    suspend fun count(): Int
}

@Database(
    entities = [SchemaMarker::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    internal abstract fun schemaMarkerDao(): SchemaMarkerDao

    companion object {
        private const val DB_NAME = "perfectly_tranquillo.db"

        @Volatile private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                ).build().also { instance = it }
            }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/methoda/tranquillo/data/AppDatabase.kt
git commit -m "feat: add empty AppDatabase (Room) with schema marker entity"
```

---

### Task 15: Add PrefsStore for font + sound

**Files:**
- Create: `app/src/main/java/com/methoda/tranquillo/data/PrefsStore.kt`

- [ ] **Step 1: Create file**

```kotlin
package com.methoda.tranquillo.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "perfectly_tranquillo_prefs")

class PrefsStore(private val context: Context) {

    val sound: Flow<Boolean> =
        context.dataStore.data.map { it[KEY_SOUND] ?: DEFAULT_SOUND }

    val fontPair: Flow<String> =
        context.dataStore.data.map { it[KEY_FONT_PAIR] ?: DEFAULT_FONT_PAIR }

    suspend fun setSound(enabled: Boolean) {
        context.dataStore.edit { it[KEY_SOUND] = enabled }
    }

    suspend fun setFontPair(pair: String) {
        context.dataStore.edit { it[KEY_FONT_PAIR] = pair }
    }

    companion object {
        const val DEFAULT_SOUND = true
        const val DEFAULT_FONT_PAIR = "fraunces"
        private val KEY_SOUND = booleanPreferencesKey("sound")
        private val KEY_FONT_PAIR = stringPreferencesKey("font_pair")
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/methoda/tranquillo/data/PrefsStore.kt
git commit -m "feat: add PrefsStore (DataStore) for sound + font pair"
```

---

### Task 16: Add Application class and register it

**Files:**
- Create: `app/src/main/java/com/methoda/tranquillo/PerfectlyTranquilloApp.kt`
- Modify: `app/src/main/AndroidManifest.xml`

- [ ] **Step 1: Create Application class**

```kotlin
package com.methoda.tranquillo

import android.app.Application
import com.methoda.tranquillo.data.AppDatabase
import com.methoda.tranquillo.data.PrefsStore

class PerfectlyTranquilloApp : Application() {
    val db: AppDatabase by lazy { AppDatabase.get(this) }
    val prefs: PrefsStore by lazy { PrefsStore(this) }
}
```

- [ ] **Step 2: Register Application in manifest**

Replace AndroidManifest.xml contents:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <application
        android:name=".PerfectlyTranquilloApp"
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.PerfectlyTranquillo">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:label="@string/app_name"
            android:theme="@style/Theme.PerfectlyTranquillo">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
```

- [ ] **Step 3: Full build**

Run:
```bash
./gradlew assembleDebug
```

Expected: BUILD SUCCESSFUL. If KSP complains about Room schema, check that the `@Database` block matches the entity in `AppDatabase.kt` verbatim.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/methoda/tranquillo/PerfectlyTranquilloApp.kt app/src/main/AndroidManifest.xml
git commit -m "feat: Application class + manifest wiring for DB + prefs"
```

---

### Task 17: Install + smoke-test on Pixel 6 emulator

**Files:** none (verification only)

- [ ] **Step 1: Confirm emulator is running**

Run:
```bash
~/Library/Android/sdk/platform-tools/adb devices
```

Expected output contains `emulator-5554    device`. If it's missing, relaunch with:
```bash
~/Library/Android/sdk/emulator/emulator -avd Pixel_6 > /tmp/emulator.log 2>&1 &
```

- [ ] **Step 2: Install and launch the APK**

Run:
```bash
./gradlew installDebug
~/Library/Android/sdk/platform-tools/adb shell am start -n com.methoda.tranquillo/.MainActivity
```

Expected: emulator shows Perfectly Tranquillo with a sea gradient background, Home tab selected, placeholder text in italic serif, and a floating bottom tab bar with 4 tabs + a gear icon top-right.

- [ ] **Step 3: Manual smoke checklist**

Tap through:
- [ ] **Home** tab — "today's mandala · Home · coming soon · sub-project #2"
- [ ] **Habits** tab — "small things, often · Habits · coming soon · sub-project #3"
- [ ] **Mandala** tab — "your inner landscape · Mandala · coming soon · sub-project #2"
- [ ] **Garden** tab — "seven days of stones · Garden · coming soon · sub-project #5"
- [ ] **Gear icon top-right** — navigates to "tune the sea · Settings · coming soon · sub-project #7"; system back returns to previous tab; no crash
- [ ] **Fonts** — title is italic serif (Fraunces). If it's a system fallback, check `adb logcat | grep GoogleFont` for fetch errors.

If all check: continue. If not: debug before marking complete.

- [ ] **Step 4: No-commit — this is verification only**

---

### Task 18: Navigation smoke test (instrumented)

One test that asserts the bottom tab bar renders all 4 tabs and clicking Habits changes the visible content. Future sub-projects extend this file.

**Files:**
- Create: `app/src/androidTest/java/com/methoda/tranquillo/NavigationSmokeTest.kt`

- [ ] **Step 1: Create test**

```kotlin
package com.methoda.tranquillo

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.methoda.tranquillo.nav.RootNavHost
import com.methoda.tranquillo.ui.theme.PerfectlyTranquilloTheme
import org.junit.Rule
import org.junit.Test

class NavigationSmokeTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun allFourTabsPresentAndHabitsNavigates() {
        composeRule.onNodeWithText("Home").assertIsDisplayed()
        composeRule.onNodeWithText("Habits").assertIsDisplayed()
        composeRule.onNodeWithText("Mandala").assertIsDisplayed()
        composeRule.onNodeWithText("Garden").assertIsDisplayed()

        composeRule.onNodeWithText("Habits").performClick()
        composeRule.onNodeWithText("SMALL THINGS, OFTEN").assertIsDisplayed()
    }
}
```

- [ ] **Step 2: Run**

Run:
```bash
./gradlew connectedDebugAndroidTest
```

Expected: 1 test, passing. If it fails, most likely reason: the bottom bar matches the tab label "Home" but also the screen title "Home" — the assertion `onNodeWithText("Home").assertIsDisplayed()` returns multiple matches. If you see that error, change the label/title assertions to use `onAllNodesWithText("Home").assertCountEquals(2)`.

- [ ] **Step 3: Commit**

```bash
git add app/src/androidTest/java/com/methoda/tranquillo/NavigationSmokeTest.kt
git commit -m "test: smoke test for 4-tab navigation"
```

---

### Task 19: Push and close out the sub-project

- [ ] **Step 1: Push to remote**

```bash
git push origin main
```

Expected: 16+ new commits pushed. Verify in browser: https://github.com/Setteler/perfectly-tranquillo

- [ ] **Step 2: Tag sub-project completion**

```bash
git tag -a foundation-complete -m "Sub-project #0: Foundation complete"
git push origin foundation-complete
```

- [ ] **Step 3: Report to user**

Summarize what was delivered and suggest next step (sub-project #1 — Mandala DNA).

---

## Self-review

- **Spec coverage** — Every item in `2026-04-24-foundation-design.md` § "Scope (In scope)" has a task:
  - Dependencies → Tasks 1–3
  - Design tokens (colors, typography, shapes, dimens) → already-done `Color.kt` + Tasks 4–7
  - Background gradient → Task 8
  - 4-tab nav + gear Settings → Tasks 10–13
  - Placeholder screens → Task 9 + 12
  - Room + DataStore scaffolded empty → Tasks 14–16
  - Build + install + verify → Tasks 17–18
  - (Out-of-scope items correctly absent.)

- **Placeholder scan** — No "TBD", "TODO", or "add appropriate X" steps. Every code block is complete.

- **Type consistency** — `RootNavHost`, `BottomTabBar`, `Route`, `TabDestinations`, `PlaceholderScreen(title, eyebrow, subprojectNumber)` signatures consistent across tasks.

- **Known fragilities** (flagged inline so the executor isn't surprised):
  - Google Fonts require Play Services on the device. Pixel 6 emulator uses a Google APIs image; verified OK.
  - Room requires at least one entity — added `SchemaMarker` placeholder (will be replaced in #2).
  - Compose backdrop blur for the tab bar is approximated with translucency; acceptable for foundation.

---

## Execution handoff

On execution, the runner should use `superpowers:subagent-driven-development` (recommended, one fresh subagent per task with review between) or `superpowers:executing-plans` (inline batch). Pick per the runner's preference.
