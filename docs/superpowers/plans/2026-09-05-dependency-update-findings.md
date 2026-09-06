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
| androidx.navigation:navigation-compose | 2.9.8 | **held at 2.9.8** | 2.10.0 needs minSdk 24; project is 23 |
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
  NOTE: navigation 2.10.0 appears in that list because compileSdk 37 is one of its
  requirements, but it was NOT adopted — it is additionally blocked by a minSdk 24 floor
  this project does not meet. See the navigation row above and the Final state section.
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

## Final state — what actually landed (2026-09-05)

| Coordinate | Baseline | Final | Note |
|---|---|---|---|
| Gradle wrapper | 9.5.0 | **9.7.1** | ladder rung 1 |
| AGP (`agp` + `gradle`) | 9.2.0 | **9.4.0** | ladder rung 1; needs Gradle >= 9.6.0, satisfied |
| Kotlin (`kotlinGradlePlugin`) | 2.3.21 | **2.4.10** | ladder rung 1 |
| Compose compiler plugin | 2.3.20 | **2.4.10** | pre-existing 2.3.20/2.3.21 skew fixed |
| KSP | 2.3.6 | **2.3.11** | |
| compileSdk | 36 | **37** | changed in both locations |
| targetSdk | 36 | 36 | deliberately unchanged |
| minSdk | 23 | 23 | deliberately unchanged |
| Dagger/Hilt | 2.59.2 | **2.60.1** | |
| OkHttp (bom + okhttp) | 5.3.2 | **5.5.0** | legacy okhttp3.mockwebserver still present |
| AppCompat | 1.7.1 | **1.8.0** | |
| Lifecycle | 2.10.0 | **2.11.0** | |
| core-ktx | 1.18.0 | **1.19.0** | |
| androidx.hilt (3 refs) | 1.3.0 | **1.4.0** | all three moved together |
| Compose BOM | 2026.03.01 | **2026.08.00** | -> Compose 1.12.0 |
| composeUi / foundation / materialVersion pins | 1.10.6 / 1.11.2 / 1.10.6 | **deleted** | BOM is now the single source |
| landscapist-glide | 2.9.8 | **2.11.0** | |
| landscapist-placeholder | 2.9.6 | **removed** | unused in src/ |
| navigation-compose | 2.9.8 | **2.9.8 (held)** | blocked by minSdk 24 — see above |
| actions/checkout | v3 | **v7** | |
| actions/setup-java | v3 | **v6** | |
| Dependabot ecosystems | gradle | **gradle + github-actions** | |

Unchanged because already current: activity-compose 1.13.0, material-icons-core 1.7.8 (frozen
upstream), espresso-core 3.7.0, androidx.test.ext:junit 1.3.0, com.google.android.material 1.14.0,
retrofit 3.0.0, gson 2.14.0, kotlinx-coroutines 1.11.0, kotlinx-serialization-json 1.11.0,
junit 4.13.2, mockito-core 5.23.0, truth 1.4.5, foojay-resolver 1.0.0.

### Only one library did not reach latest

navigation-compose, held at 2.9.8. Taking 2.10.0 would require raising minSdk from 23 to 24, which
drops Android 6.0 devices — a product decision, not a dependency update. Reversible in one line if
that trade is ever accepted.

### One authorised source change

`NetworkInfoImpl` previously decided connectivity by opening a raw TCP socket to 8.8.8.8:53 with a
1.5s timeout, and `RecipeRepositoryImpl` short-circuits to `NoInternetError` without attempting the
request when that probe fails. On networks that do not route outbound TCP to an external DNS
resolver — the Android emulator's user-mode NAT being the common case — every request failed on a
device with working internet. Replaced with `ConnectivityManager`'s view of the active network;
added `ACCESS_NETWORK_STATE`. The `NetworkInfo` interface is unchanged, so no test needed changing.
Note the semantic shift: `NoInternetError` now means "no network interface", not "no usable
internet", so a captive portal now surfaces `UnknownError` instead.

### Test suite state (updated 2026-09-06)

All tests green: **24 unit tests** and **12 Compose instrumentation tests**, zero failures.

The 12 instrumentation tests previously failed with
`NoSuchMethodException: android.hardware.input.InputManager.getInstance`. The diagnosis of
"Espresso 3.7.0 vs Android 16, unfixable" was WRONG — it read the version from the catalog rather
than from the failing module's resolved classpath. `:feature-recipes` never declared its own test
dependencies, so it silently inherited **espresso-core 3.5.0** from Compose ui-test's POM, while
`:core` and `:dependencies` pin 3.7.0. Declaring espresso-core explicitly fixed that, and exposed a
second missing dependency: `createComposeRule()` launches `androidx.activity.ComponentActivity`,
which only reaches the debug manifest via `ui-test-manifest`. `:app` had it; `:feature-recipes` did
not. Two build-file lines; both tracked in commit 80219f6.

This matters beyond the test count: Compose moved 1.11.2 -> 1.12.0 in this update, and until that
fix the entire Compose UI layer had no automated verification at all.

`NetworkInfoImpl` also gained five unit tests (commit 678e88a), each verified by mutation to fail
when the branch it covers is broken.

NOTE: CI still does not run `connectedAndroidTest` — `.github/workflows/android.yml` runs only
`testDebugUnitTest` and `assembleDebug`. The 12 instrumentation tests pass locally but are still
not gating anything.

### Known issues NOT addressed by this update (all pre-existing)

- Gradle-10 deprecations from Groovy space-assignment syntax (`compose true`, `namespace '...'`) in
  all five module build scripts. Becomes a hard error on Gradle 10.
- Eight deprecated `android.*=false` flags in gradle.properties plus legacy variant API usage; all
  removed in AGP 10. `android.builtInKotlin=false` and `android.newDsl=false` are the load-bearing
  ones and are a real migration, not a flag deletion.
- `hiltViewModel()` deprecated, moved to `androidx.hilt.lifecycle.viewmodel.compose`
  (RecipeListScreen.kt:47, RecipeScreen.kt:36).
- `androidx.transition` resolves 1.6.0 -> 1.5.0. Not a regression: Compose UI 1.11.2's POM declared
  transition:1.6.0 and 1.12.0's POM drops it, so it now resolves to what AppCompat/Material
  actually require. No source file references androidx.transition.
- The recipe API key is hardcoded in `core/.../RecipeRetrofitService.kt` and is already public in
  this repo's history. It should be ROTATED, not merely relocated.
