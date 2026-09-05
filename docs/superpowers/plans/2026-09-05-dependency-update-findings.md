# Dependency Update Findings — 2026-09-05

Verified against Google Maven, Maven Central, and vendor release notes on 2026-09-05.

## Baseline
Gradle 9.5.0, AGP 9.2.0, Kotlin 2.3.21, JDK 17, compileSdk/targetSdk 36, minSdk 23.
`clean testDebugUnitTest assembleDebug` green; 19 unit tests, 0 failures.

## Available versions

| Coordinate | Current | Target | Note |
|---|---|---|---|
| Gradle wrapper | 9.5.0 | 9.7.1 | AGP 9.4 needs >= 9.6.0 |
| com.android.tools.build:gradle (AGP) | 9.2.0 | 9.4.0 | 9.3.2 is the no-wrapper-change option |
| org.jetbrains.kotlin (KGP) | 2.3.21 | 2.4.10 | |
| Compose compiler plugin | 2.3.20 | 2.4.10 | MUST equal KGP; currently mismatched |
| com.google.devtools.ksp | 2.3.6 | 2.3.11 | KSP decoupled from Kotlin at 2.3.0 |
| androidx.compose:compose-bom | 2026.03.01 | 2026.08.00 | -> Compose 1.12.0 |
| androidx.core:core-ktx | 1.18.0 | 1.19.0 | minCompileSdk=37 |
| androidx.appcompat:appcompat | 1.7.1 | 1.8.0 | minCompileSdk=34 |
| androidx.lifecycle:* | 2.10.0 | 2.11.0 | minCompileSdk=34 |
| androidx.navigation:navigation-compose | 2.9.8 | 2.10.0 | minCompileSdk=37 |
| androidx.hilt:* (3 refs) | 1.3.0 | 1.4.0 | hilt-navigation-compose minCompileSdk=37 |
| com.google.dagger (Hilt) | 2.59.2 | 2.60.1 | |
| com.squareup.okhttp3 BOM | 5.3.2 | 5.5.0 | legacy okhttp3.mockwebserver package still present |
| com.github.skydoves:landscapist-* | 2.9.8 / 2.9.6 | 2.11.0 | minCompileSdk=37 |
| actions/checkout | v3 | v7.0.1 | |
| actions/setup-java | v3 | v6.0.0 | |

Already current, no action: activity-compose 1.13.0, material-icons-core 1.7.8 (frozen),
espresso-core 3.7.0, androidx.test.ext:junit 1.3.0, com.google.android.material 1.14.0,
retrofit 3.0.0, gson 2.14.0, kotlinx-coroutines 1.11.0, kotlinx-serialization-json 1.11.0,
junit 4.13.2, mockito-core 5.23.0, truth 1.4.5, foojay-resolver 1.0.0.

## Hard constraints
- compileSdk 37 required by: Compose 1.12.0, core-ktx 1.19.0, navigation 2.10.0,
  hilt-navigation-compose 1.4.0, landscapist 2.11.0. API 37 is stable; android-37.0 is
  installed locally. AGP 9.2 already supports max API level 37.0, so the compileSdk bump
  does not by itself require an AGP upgrade.
- AGP 9.4.0 requires Gradle >= 9.6.0. AGP 9.3.x requires Gradle >= 9.5.0.
- Compose compiler plugin version must equal the Kotlin version.
- Hilt 2.60 breaking changes: multidex support removed; minSdk floor 23 (project is 23);
  the Hilt Gradle plugin now errors instead of ignoring a plugin-controlled flag that the
  user sets; requires AGP >= 9.0.0.

## Soft constraint already exceeded before this plan
JetBrains lists KGP 2.4.0-2.4.10 as fully supported on Gradle 7.6.3-9.5.0 and AGP 8.5.2-9.1.0.
The project already runs AGP 9.2.0 on KGP 2.3.21 (whose stated maxima are AGP 9.0.0 /
Gradle 9.3.0) and builds fine. Going to Gradle 9.7.1 + KGP 2.4.10 exceeds the window on both
axes at once. This is warning-level, not an error, but it is the most likely source of
trouble in Task 7 and the reason Task 7 is last among the version tasks.

## Known-safe API surface (checked, no source changes needed)
- okhttp3.mockwebserver.MockWebServer/MockResponse still ship in mockwebserver 5.5.0,
  byte-identical class sizes to 5.3.2. The 7 test files using them are safe.
- Compose Material 2 (androidx.compose.material.*) still ships at 1.12.0. The app is M2
  throughout (MaterialTheme, Text, Surface, Scaffold, Card, ExperimentalMaterialApi).
  This plan does NOT migrate to Material 3.
- Landscapist GlideImage(imageModel, requestOptions, imageOptions, modifier,
  previewPlaceholder: Painter) is the 2.x signature used by RecipeCard.kt and RecipeScreen.kt.

## Pre-existing quirks confirmed by dependencyInsight
- Declared compose versions in libs.versions.toml are already overridden at resolution time:
  compose.ui resolves to 1.11.2 (not the declared 1.10.6) and compose-bom resolves to
  2026.05.01 (not the declared 2026.03.01), because landscapist-glide:2.9.8 depends on the
  higher BOM and the androidx.compose.* atomic group is aligned upward. Task 5 removes the
  redundant pins so the catalog stops lying about what ships.
- landscapist-placeholder is declared but never imported anywhere in src/. Task 9 removes it.
- Root build.gradle has an ext block (compose_compiler_version, compose_bom_version,
  lifecycle_version, kotlin_version, hilt_version, landscapist_version, okhttp_bom_version)
  that no module references. Task 8 deletes it.
