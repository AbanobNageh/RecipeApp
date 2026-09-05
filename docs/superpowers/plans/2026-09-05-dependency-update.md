# Dependency & Toolchain Update Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Move every library, plugin, and build tool in RecipeApp to the newest version that keeps the app building, testing green, and running correctly — raising `compileSdk` from 36 to 37 to unlock the current AndroidX line.

**Architecture:** One task per independently-revertable batch of version bumps, ordered cheapest-and-safest first so that a failure late in the plan never costs the gains made earlier. Every task ends with the same verification gate (unit tests + debug APK build) and its own commit, so `git revert` of a single commit is always a clean fallback. Tasks that change rendering or runtime behavior additionally run the Compose instrumentation tests and a manual smoke test on the emulator. Every task carries an explicit **fallback ladder**: the ordered list of older versions to try if the newest one breaks.

**Tech Stack:** Gradle 9.5.0 (Groovy DSL), AGP 9.2.0, Kotlin 2.3.21, KSP, Hilt/Dagger, Jetpack Compose (Material 2), Retrofit + OkHttp + Gson, Landscapist/Glide, JUnit4 + Mockito + Truth + MockWebServer. Five modules: `:app`, `:core`, `:data`, `:dependencies`, `:feature-recipes`. Versions live in `gradle/libs.versions.toml`; SDK levels live in root `build.gradle` `ext`.

**Spec:** This plan's spec is the investigation recorded in `docs/superpowers/plans/2026-09-05-dependency-update-findings.md` (written as Task 0 below). All version numbers, `minCompileSdk` values, and compatibility ceilings in this plan were verified against Google Maven, Maven Central, and vendor release notes on 2026-09-05.

## Global Constraints

Every task's requirements implicitly include this section.

- **JDK stays at 17.** `compileOptions`/`jvmTarget` are `VERSION_17`/`JVM_17` in all five modules and CI uses Temurin 17. AGP 9.2 and 9.4 both require only JDK 17. Do not raise it.
- **`minSdk` stays at 23.** Hilt 2.60 raised its own floor to 23, which the project already meets. Do not raise `minSdk`.
- **`targetSdk` stays at 36** for the whole plan. Raising `targetSdk` opts the app into new runtime behavior and is a product decision, not a dependency update. `compileSdk` moves to 37; `targetSdk` does not.
- **`compileSdk` must be changed in TWO places** — they are separate values read by different modules:
  - `gradle/libs.versions.toml` → `compileSdk = "36"` (read by `:app` only, via `libs.versions.compileSdk.get().toInteger()`)
  - root `build.gradle` → `ext._compileSdkVersion = 36` (read by `:core`, `:data`, `:dependencies`, `:feature-recipes`)
  - Changing only one produces a build that compiles but resolves AndroidX inconsistently across modules.
- **The Compose compiler plugin version MUST equal the Kotlin version.** `jetbrainsKotlinPluginCompose` and `kotlinGradlePlugin` must always be the same string. They are currently mismatched (2.3.20 vs 2.3.21); Task 3 fixes this.
- **AGP 9.4.0 requires Gradle ≥ 9.6.0.** AGP 9.3.x requires Gradle ≥ 9.5.0. The wrapper is currently 9.5.0. Never raise AGP to 9.4.x without raising the wrapper first, in the same commit.
- **Do not add, remove, or rewrite application source code** to accommodate a version. If a library version requires source changes, that version is out of scope — drop to the fallback instead. The two exceptions are explicitly scoped in Task 8 (dead `ext` block) and Task 9 (unused `landscapist-placeholder` dependency), neither of which touches `src/`.
- **Do not "fix" the AGP 10 deprecation warnings.** The eight `android.*=false` flags in `gradle.properties` and the legacy-variant-API warnings are a separate migration. They must not get worse, but this plan does not address them.
- **Verification gate** (referred to below as "the standard gate"): `./gradlew clean testDebugUnitTest assembleDebug --console=plain` must exit 0, and the unit test count must be **19 tests, 0 failures, 0 errors**. A drop in test count is a failure even if the build is green.
- Shell commands are written for **Git Bash**. In PowerShell use `.\gradlew` instead of `./gradlew`.

---

## File Structure

Only build configuration changes. No `src/` files are modified by this plan.

| File | Responsibility | Tasks that touch it |
|---|---|---|
| `gradle/libs.versions.toml` | Single source of truth for all library/plugin versions and `compileSdk` for `:app` | 2, 3, 4, 5, 6, 7, 9 |
| `build.gradle` (root) | SDK levels for the four library modules (`ext._compileSdkVersion`), root plugin aliases, dead `ext` block | 2, 7, 8 |
| `gradle/wrapper/gradle-wrapper.properties` | Gradle distribution version | 7 |
| `dependencies/build.gradle` | The single module declaring all shared third-party deps | 9 |
| `.github/workflows/android.yml` | CI: JDK setup + `testDebugUnitTest` + `assembleDebug` | 10 |
| `.github/dependabot.yml` | Which ecosystems get update PRs | 10 |
| `docs/superpowers/plans/2026-09-05-dependency-update-findings.md` | Evidence record (created in Task 0) | 0 |

---

## Task 0: Record the baseline and the findings

**Files:**
- Create: `docs/superpowers/plans/2026-09-05-dependency-update-findings.md`

**Interfaces:**
- Produces: a committed record of the pre-update resolved dependency graph at `baseline-deps.txt` (git-ignored scratch, not committed) and the findings doc that later tasks cite for fallback versions.

- [ ] **Step 1: Create the working branch**

```bash
cd /d/Projects/RecipeApp
git checkout -b chore/dependency-update-2026-09
git status --short
```

Expected: `?? docs/` — this plan file is already on disk but not yet committed. Nothing else should be listed. If any tracked file shows as modified, stash or commit it first; this plan assumes a clean starting point.

- [ ] **Step 2: Confirm the green baseline**

```bash
./gradlew clean testDebugUnitTest assembleDebug --console=plain 2>&1 | tail -20
```

Expected: `BUILD SUCCESSFUL`. This takes ~3 minutes from clean.

- [ ] **Step 3: Confirm the baseline test count is 19/0/0**

```bash
find . -path "*/test-results/testDebugUnitTest/*.xml" -not -path "./build/*" \
  -exec grep -ho 'tests="[0-9]*" skipped="[0-9]*" failures="[0-9]*" errors="[0-9]*"' {} \; \
  | awk -F'"' '{t+=$2; f+=$6; e+=$8} END {print "tests="t, "failures="f, "errors="e}'
```

Expected exactly: `tests=19 failures=0 errors=0`

If this does not print 19/0/0, **stop** — the baseline is not what this plan assumes and every later comparison is invalid.

- [ ] **Step 4: Snapshot the resolved dependency graph for later diffing**

```bash
./gradlew :app:dependencies --configuration debugRuntimeClasspath --console=plain -q > baseline-deps.txt
./gradlew :dependencies:dependencies --configuration debugRuntimeClasspath --console=plain -q >> baseline-deps.txt
wc -l baseline-deps.txt
```

Expected: a few hundred lines. Keep this file on disk for the whole plan; it is scratch, **do not commit it**.

- [ ] **Step 5: Write the findings document**

Create `docs/superpowers/plans/2026-09-05-dependency-update-findings.md` with exactly this content:

```markdown
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
```

- [ ] **Step 6: Ensure the scratch snapshot is not committed, then commit the findings**

```bash
git status --short
git add docs/superpowers/plans/2026-09-05-dependency-update.md docs/superpowers/plans/2026-09-05-dependency-update-findings.md
git commit -m "docs: record dependency update plan and 2026-09-05 findings"
```

Expected: `baseline-deps.txt` appears in `git status --short` as untracked (`?? baseline-deps.txt`) and is **not** in the commit.

---

## Task 1: Raise compileSdk to 37

Done first because it is the single change that unlocks the largest number of later updates, it needs no library movement, and it is trivially revertable. Doing it before any library bump means that if it fails, nothing else has to be unwound.

**Files:**
- Modify: `gradle/libs.versions.toml` (the `compileSdk` entry in `[versions]`)
- Modify: `build.gradle` (root) — `ext._compileSdkVersion`

**Interfaces:**
- Consumes: green baseline from Task 0.
- Produces: `compileSdk = 37` across all five modules, which Tasks 4, 5, 6 and 9 all depend on.

- [ ] **Step 1: Confirm the API 37 platform is installed**

```bash
ls "$LOCALAPPDATA/Android/Sdk/platforms" 2>/dev/null || ls /c/Users/$USER/AppData/Local/Android/Sdk/platforms
```

Expected: a directory named `android-37.0` is present alongside `android-36`.

If `android-37.0` is missing, install it before continuing:

```bash
"$LOCALAPPDATA/Android/Sdk/cmdline-tools/latest/bin/sdkmanager.bat" "platforms;android-37"
```

- [ ] **Step 2: Change compileSdk in the version catalog**

In `gradle/libs.versions.toml`, in the `[versions]` block, change:

```toml
compileSdk = "36"
```

to:

```toml
compileSdk = "37"
```

- [ ] **Step 3: Change compileSdk for the library modules**

In root `build.gradle`, change:

```groovy
ext._compileSdkVersion = 36
```

to:

```groovy
ext._compileSdkVersion = 37
```

Leave `ext._minSdkVersion = 23` and `ext._targetSdkVersion = 36` untouched.

- [ ] **Step 4: Verify both values actually changed**

```bash
grep -n 'compileSdk' gradle/libs.versions.toml build.gradle
```

Expected exactly two lines: `compileSdk = "37"` and `ext._compileSdkVersion = 37`.

- [ ] **Step 5: Run the standard gate**

```bash
./gradlew clean testDebugUnitTest assembleDebug --console=plain 2>&1 | tail -20
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 6: Confirm the test count held at 19/0/0**

```bash
find . -path "*/test-results/testDebugUnitTest/*.xml" -not -path "./build/*" \
  -exec grep -ho 'tests="[0-9]*" skipped="[0-9]*" failures="[0-9]*" errors="[0-9]*"' {} \; \
  | awk -F'"' '{t+=$2; f+=$6; e+=$8} END {print "tests="t, "failures="f, "errors="e}'
```

Expected: `tests=19 failures=0 errors=0`

- [ ] **Step 7: Commit**

```bash
git add gradle/libs.versions.toml build.gradle
git commit -m "build: raise compileSdk from 36 to 37"
```

**Fallback ladder:** None. If `compileSdk 37` fails, this plan cannot proceed past Task 3 — revert this commit, run Tasks 2 and 3 only, and stop. Report the exact failure; do not attempt Tasks 4, 5, 6 or 9 on `compileSdk 36`, because every version they target declares `minCompileSdk=37` and AGP will fail the build with a clear "dependency requires compileSdk 37" error rather than anything subtle.

---

## Task 2: Bump the constraint-free third-party libraries

Hilt, OkHttp, AppCompat, Lifecycle and KSP. None of these has a `compileSdk 37` requirement, so this task would also succeed if Task 1 had to be reverted.

**Files:**
- Modify: `gradle/libs.versions.toml` (`dagger`, `daggerVersion`, `okhttpBom`, `okhttp`, `appcompat`, `lifecycleRuntimeKtx`, `googleDevtoolsKsp`)

**Interfaces:**
- Consumes: Task 1's `compileSdk 37`.
- Produces: Hilt 2.60.1 on the classpath, which Task 4's `androidx.hilt:1.4.0` sits on top of.

- [ ] **Step 1: Check the Hilt breaking changes do not apply**

Hilt 2.60 removed multidex support, raised its minSdk floor to 23, and made the Hilt Gradle plugin error on user-set plugin-controlled flags. Verify none of these bite:

```bash
grep -rn "multiDexEnabled\|dagger.hilt\." --include=*.gradle --include=gradle.properties . | grep -v "/build/"
grep -n "_minSdkVersion" build.gradle
```

Expected: no `multiDexEnabled` anywhere, no `dagger.hilt.*` properties set, and `ext._minSdkVersion = 23`. If any `dagger.hilt.*` property IS set in `gradle.properties`, remove it in this task — 2.60 turns it from ignored into a build error.

- [ ] **Step 2: Apply the version bumps**

In `gradle/libs.versions.toml`, in the `[versions]` block, make these seven changes. `dagger` and `daggerVersion` are duplicates that must move together — `dagger` feeds the Gradle plugin alias, `daggerVersion` feeds the library aliases, and a mismatch between them produces a confusing runtime failure rather than a build error.

```toml
appcompat = "1.8.0"
dagger = "2.60.1"
daggerVersion = "2.60.1"
googleDevtoolsKsp = "2.3.11"
lifecycleRuntimeKtx = "2.11.0"
okhttpBom = "5.5.0"
okhttp = "5.5.0"
```

- [ ] **Step 3: Verify the edits landed and no stale value remains**

```bash
grep -nE '^(appcompat|dagger|daggerVersion|googleDevtoolsKsp|lifecycleRuntimeKtx|okhttpBom|okhttp) =' gradle/libs.versions.toml
```

Expected: `appcompat = "1.8.0"`, `dagger = "2.60.1"`, `daggerVersion = "2.60.1"`, `googleDevtoolsKsp = "2.3.11"`, `lifecycleRuntimeKtx = "2.11.0"`, `okhttpBom = "5.5.0"`, `okhttp = "5.5.0"`.

- [ ] **Step 4: Run the standard gate**

```bash
./gradlew clean testDebugUnitTest assembleDebug --console=plain 2>&1 | tail -20
```

Expected: `BUILD SUCCESSFUL`.

The 7 test files importing `okhttp3.mockwebserver.MockWebServer` / `MockResponse` are the thing to watch here. Those classes are confirmed present in mockwebserver 5.5.0, so a failure would be a behavior change, not a missing class.

- [ ] **Step 5: Confirm the test count held at 19/0/0**

```bash
find . -path "*/test-results/testDebugUnitTest/*.xml" -not -path "./build/*" \
  -exec grep -ho 'tests="[0-9]*" skipped="[0-9]*" failures="[0-9]*" errors="[0-9]*"' {} \; \
  | awk -F'"' '{t+=$2; f+=$6; e+=$8} END {print "tests="t, "failures="f, "errors="e}'
```

Expected: `tests=19 failures=0 errors=0`

- [ ] **Step 6: Commit**

```bash
git add gradle/libs.versions.toml
git commit -m "build: update Hilt 2.60.1, OkHttp 5.5.0, AppCompat 1.8.0, Lifecycle 2.11.0, KSP 2.3.11"
```

**Fallback ladder** — bisect by reverting individual versions, not the whole task. In likelihood order:
1. **Hilt** fails (most likely — it is the only one with documented breaking changes): try `2.60` (the `.1` is a bugfix over it), then `2.59.2` (stay put).
2. **OkHttp** fails in the MockWebServer tests: try `5.4.0`, then `5.3.2` (stay put). Do **not** rewrite the tests onto `mockwebserver3` — that is source change, out of scope.
3. **KSP** fails: try `2.3.10`, `2.3.7`, then `2.3.6` (stay put).
4. **AppCompat / Lifecycle** fail: revert each to `1.7.1` / `2.10.0`. These have the widest compatibility windows and are the least likely culprits.

---

## Task 3: Align and update Kotlin and the Compose compiler plugin

The two versions are currently mismatched (KGP 2.3.21, Compose plugin 2.3.20). This task fixes the mismatch and moves both to 2.4.10 in one step, so that a failure is attributable to the version jump rather than to the pre-existing skew.

**Files:**
- Modify: `gradle/libs.versions.toml` (`kotlinGradlePlugin`, `jetbrainsKotlinPluginCompose`)

**Interfaces:**
- Consumes: Task 2's KSP 2.3.11, which is the KSP release that added Kotlin 2.4 support.
- Produces: Kotlin 2.4.10 language/compiler for all five modules.

- [ ] **Step 1: Prove the mismatch is real before changing it**

```bash
grep -nE '^(kotlinGradlePlugin|jetbrainsKotlinPluginCompose) =' gradle/libs.versions.toml
```

Expected: `kotlinGradlePlugin = "2.3.21"` and `jetbrainsKotlinPluginCompose = "2.3.20"` — two different values.

- [ ] **Step 2: Set both to the same new version**

In `gradle/libs.versions.toml`:

```toml
jetbrainsKotlinPluginCompose = "2.4.10"
kotlinGradlePlugin = "2.4.10"
```

- [ ] **Step 3: Verify they now match**

```bash
grep -nE '^(kotlinGradlePlugin|jetbrainsKotlinPluginCompose) =' gradle/libs.versions.toml
```

Expected: both read `"2.4.10"`.

- [ ] **Step 4: Confirm the root composeCompiler block still resolves**

Root `build.gradle` contains a `composeCompiler { enableStrongSkippingMode = true ... }` block. `enableStrongSkippingMode` still exists in the 2.4.10 plugin extension, so this must not error. Configure the build without running tasks:

```bash
./gradlew help --console=plain 2>&1 | tail -10
```

Expected: `BUILD SUCCESSFUL`. A failure here naming `enableStrongSkippingMode` means the DSL changed — see the fallback.

- [ ] **Step 5: Run the standard gate**

```bash
./gradlew clean testDebugUnitTest assembleDebug --console=plain 2>&1 | tail -20
```

Expected: `BUILD SUCCESSFUL`. This is a Kotlin **language version** bump (2.3 → 2.4), so watch for new compiler errors or newly-fatal warnings in `:core`, `:data` and `:feature-recipes`.

- [ ] **Step 6: Confirm the test count held at 19/0/0**

```bash
find . -path "*/test-results/testDebugUnitTest/*.xml" -not -path "./build/*" \
  -exec grep -ho 'tests="[0-9]*" skipped="[0-9]*" failures="[0-9]*" errors="[0-9]*"' {} \; \
  | awk -F'"' '{t+=$2; f+=$6; e+=$8} END {print "tests="t, "failures="f, "errors="e}'
```

Expected: `tests=19 failures=0 errors=0`

- [ ] **Step 7: Commit**

```bash
git add gradle/libs.versions.toml
git commit -m "build: update Kotlin and Compose compiler plugin to 2.4.10, fixing version skew"
```

**Fallback ladder** — always move both values together, never independently:
1. `2.4.10` (target)
2. `2.4.0`
3. `2.3.21` — the mismatch fix alone. This is still a real improvement over the starting state and must be committed even if the version bump is abandoned. Commit message: `build: align Compose compiler plugin with Kotlin 2.3.21`.

If Step 4 fails specifically on `enableStrongSkippingMode`, do not drop the Kotlin version — instead delete the `enableStrongSkippingMode = true` line from the root `composeCompiler` block (strong skipping has been the default since Kotlin 2.0.20, so the line is a no-op) and re-run Step 4 before falling back.

---

## Task 4: Update AndroidX core, navigation, and hilt extensions

**Files:**
- Modify: `gradle/libs.versions.toml` (`coreKtx`, `navigationCompose`, `hiltNavigationFragment`, `hiltCompiler`, `hiltNavigationCompose`)

**Interfaces:**
- Consumes: Task 1's `compileSdk 37` (all of these declare `minCompileSdk=37` except `hilt-navigation-fragment`, which needs 35) and Task 2's Hilt 2.60.1.
- Produces: `androidx.hilt` 1.4.0, which `hiltViewModel()` in `:feature-recipes` resolves against.

- [ ] **Step 1: Apply the version bumps**

In `gradle/libs.versions.toml`. Note that `hiltNavigationFragment`, `hiltCompiler` and `hiltNavigationCompose` are three separate refs that all point at the `androidx.hilt` group and must move together — they are released as one group and mixing 1.3.0 with 1.4.0 across them causes duplicate-class and missing-symbol errors at KSP time.

```toml
coreKtx = "1.19.0"
hiltNavigationFragment = "1.4.0"
hiltCompiler = "1.4.0"
hiltNavigationCompose = "1.4.0"
navigationCompose = "2.10.0"
```

- [ ] **Step 2: Verify all five landed**

```bash
grep -nE '^(coreKtx|hiltNavigationFragment|hiltCompiler|hiltNavigationCompose|navigationCompose) =' gradle/libs.versions.toml
```

Expected: `coreKtx = "1.19.0"`, the three `hilt*` refs all `"1.4.0"`, `navigationCompose = "2.10.0"`.

- [ ] **Step 3: Run the standard gate**

```bash
./gradlew clean testDebugUnitTest assembleDebug --console=plain 2>&1 | tail -20
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Confirm the test count held at 19/0/0**

```bash
find . -path "*/test-results/testDebugUnitTest/*.xml" -not -path "./build/*" \
  -exec grep -ho 'tests="[0-9]*" skipped="[0-9]*" failures="[0-9]*" errors="[0-9]*"' {} \; \
  | awk -F'"' '{t+=$2; f+=$6; e+=$8} END {print "tests="t, "failures="f, "errors="e}'
```

Expected: `tests=19 failures=0 errors=0`

- [ ] **Step 5: Commit**

```bash
git add gradle/libs.versions.toml
git commit -m "build: update core-ktx 1.19.0, navigation-compose 2.10.0, androidx.hilt 1.4.0"
```

**Fallback ladder:**
1. **androidx.hilt** fails (Hilt DI wiring is the most coupled thing here): drop all three refs together to `1.3.0`. Do not try mixed versions.
2. **navigation-compose** fails: try `2.9.8` (stay put). Note `:feature-recipes` uses `hiltViewModel()`, which bridges navigation and Hilt — if both this and androidx.hilt fail, revert them as a pair.
3. **core-ktx** fails: try `1.18.0` (stay put).

---

## Task 5: Update Compose via the BOM and remove the redundant version pins

The catalog currently pins `composeUi`, `foundation` and `materialVersion` explicitly *and* imports a BOM, and the pins are already being overridden at resolution time (`compose.ui` declared 1.10.6, resolves 1.11.2). This task makes the BOM the single source of truth so the catalog stops misreporting what ships.

**Files:**
- Modify: `gradle/libs.versions.toml` (`composeBom`; remove `composeUi`, `foundation`, `materialVersion`; rewrite six `[libraries]` entries to drop their `version.ref`)

**Interfaces:**
- Consumes: Task 1's `compileSdk 37` (Compose 1.12.0 declares `minCompileSdk=37`).
- Produces: all `androidx.compose.*` artifacts governed solely by `compose-bom = 2026.08.00` → Compose 1.12.0.

- [ ] **Step 1: Record what Compose actually resolves to today**

```bash
./gradlew :dependencies:dependencyInsight --configuration debugRuntimeClasspath \
  --dependency androidx.compose.ui:ui --console=plain -q 2>&1 | head -5
```

Note the version on the first line. This is the real "before" value — it will not be 1.10.6.

- [ ] **Step 2: Bump the BOM and delete the three redundant pins**

In `gradle/libs.versions.toml` `[versions]`, change:

```toml
composeBom = "2026.08.00"
```

and **delete** these three lines entirely:

```toml
composeUi = "1.10.6"
foundation = "1.11.2"
materialVersion = "1.10.6"
```

Leave `materialIconsCore = "1.7.8"` alone — `material-icons-core` is frozen at 1.7.8 and the BOM pins it there too, but it is clearer to keep the explicit ref. Also leave `material = "1.14.0"` alone — that is `com.google.android.material:material`, a different library entirely, and it is already current.

- [ ] **Step 3: Drop `version.ref` from the six Compose library entries**

In `[libraries]`, the BOM supplies these versions. These six entries are **not contiguous** — they sit at lines 43, 44, 46, 47, 66 and 67, separated by unrelated entries. Edit each one individually; do not replace a block.

Line 43 — change:
```toml
androidx-compose-foundation = { module = "androidx.compose.foundation:foundation", version.ref = "foundation" }
```
to:
```toml
androidx-compose-foundation = { module = "androidx.compose.foundation:foundation" }
```

Line 44 — change:
```toml
androidx-compose-material = { module = "androidx.compose.material:material", version.ref = "materialVersion" }
```
to:
```toml
androidx-compose-material = { module = "androidx.compose.material:material" }
```

Line 45 is `androidx-compose-material-icons-core` with `version.ref = "materialIconsCore"`. **Leave it exactly as it is** — that ref is not being deleted.

Line 46 — change:
```toml
androidx-compose-ui = { module = "androidx.compose.ui:ui", version.ref = "composeUi" }
```
to:
```toml
androidx-compose-ui = { module = "androidx.compose.ui:ui" }
```

Line 47 — change:
```toml
androidx-compose-ui-tooling = { module = "androidx.compose.ui:ui-tooling", version.ref = "composeUi" }
```
to:
```toml
androidx-compose-ui-tooling = { module = "androidx.compose.ui:ui-tooling" }
```

Line 66 — change:
```toml
androidx-compose-ui-test-junit4 = { group = "androidx.compose.ui", name = "ui-test-junit4", version.ref = "composeUi" }
```
to:
```toml
androidx-compose-ui-test-junit4 = { group = "androidx.compose.ui", name = "ui-test-junit4" }
```

Line 67 — change:
```toml
androidx-compose-ui-test-manifest = { group = "androidx.compose.ui", name = "ui-test-manifest", version.ref = "composeUi" }
```
to:
```toml
androidx-compose-ui-test-manifest = { group = "androidx.compose.ui", name = "ui-test-manifest" }
```

- [ ] **Step 4: Verify no dangling refs remain**

```bash
grep -nE 'composeUi|"foundation"|materialVersion' gradle/libs.versions.toml
```

Expected: **no output**. Any hit means a `[libraries]` entry still references a deleted `[versions]` key, which fails the build at configuration time with `Undefined version reference`.

- [ ] **Step 5: Run the standard gate**

```bash
./gradlew clean testDebugUnitTest assembleDebug --console=plain 2>&1 | tail -20
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 6: Confirm Compose actually moved to 1.12.0**

```bash
./gradlew :dependencies:dependencyInsight --configuration debugRuntimeClasspath \
  --dependency androidx.compose.ui:ui --console=plain -q 2>&1 | head -5
```

Expected: first line reads `androidx.compose.ui:ui:1.12.0`.

- [ ] **Step 7: Confirm the test count held at 19/0/0**

```bash
find . -path "*/test-results/testDebugUnitTest/*.xml" -not -path "./build/*" \
  -exec grep -ho 'tests="[0-9]*" skipped="[0-9]*" failures="[0-9]*" errors="[0-9]*"' {} \; \
  | awk -F'"' '{t+=$2; f+=$6; e+=$8} END {print "tests="t, "failures="f, "errors="e}'
```

Expected: `tests=19 failures=0 errors=0`

- [ ] **Step 8: Commit**

```bash
git add gradle/libs.versions.toml
git commit -m "build: update Compose BOM to 2026.08.00 and make it the single version source"
```

**Fallback ladder** — the pin removal and the BOM bump are separable; keep the pin removal even if the bump is abandoned:
1. `composeBom = "2026.08.00"` (Compose 1.12.0) — target
2. `composeBom = "2026.06.01"` — the BOM landscapist 2.11.0 depends on, so Task 9 will not push past it
3. `composeBom = "2026.05.01"` — what the project resolves to today, so this is a true no-op for shipped bytes while still fixing the misleading catalog

The app is Compose **Material 2** throughout. If a failure names an `androidx.compose.material.*` symbol (`Card`, `Scaffold`, `Surface`, `Text`, `MaterialTheme`, `ExperimentalMaterialApi`), do **not** migrate to Material 3 — that is a source rewrite and out of scope. Drop to the next BOM on the ladder instead.

---

## Task 6: Verify the app actually runs after the library updates

Unit tests cover networking, repositories, use-cases and view models — none of the rendering. Compose 1.12.0, androidx.hilt 1.4.0 and `compileSdk 37` all affect UI and DI wiring at runtime, so this task is the first that proves "the app still works" rather than "the app still compiles".

**Files:** None modified. This is a verification-only task.

**Interfaces:**
- Consumes: Tasks 1–5.
- Produces: a confirmed-working APK on API 36.1, which is the reference point Task 7's toolchain change is measured against.

- [ ] **Step 1: Boot the emulator**

```bash
"$LOCALAPPDATA/Android/Sdk/emulator/emulator.exe" -avd Medium_Phone_API_36.1 -no-snapshot-load &
```

Wait for it, then confirm:

```bash
"$LOCALAPPDATA/Android/Sdk/platform-tools/adb.exe" wait-for-device
"$LOCALAPPDATA/Android/Sdk/platform-tools/adb.exe" devices
```

Expected: one entry ending in `device` (not `offline`).

- [ ] **Step 2: Run the Compose instrumentation tests**

There are exactly 3: `AppBarTest`, `FoodCategoryChipTest`, `RecipeCardTest`, all in `:feature-recipes`.

```bash
./gradlew :feature-recipes:connectedDebugAndroidTest --console=plain 2>&1 | tail -25
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Confirm all 3 instrumentation tests actually ran and passed**

```bash
find feature-recipes/build/outputs/androidTest-results -name "*.xml" \
  -exec grep -ho 'tests="[0-9]*" skipped="[0-9]*" failures="[0-9]*" errors="[0-9]*"' {} \; \
  | awk -F'"' '{t+=$2; f+=$6; e+=$8} END {print "tests="t, "failures="f, "errors="e}'
```

Expected: a non-zero test count with `failures=0 errors=0`. A count of `0` means the tests were skipped, not passed — treat that as a failure.

- [ ] **Step 4: Install and launch the app**

```bash
./gradlew :app:installDebug --console=plain 2>&1 | tail -5
"$LOCALAPPDATA/Android/Sdk/platform-tools/adb.exe" shell am start -n com.abanobnageh.recipeapp/.MainActivity
```

Expected: `Starting: Intent { ... }` with no `Error type 3` / `Activity class does not exist`.

- [ ] **Step 5: Smoke test the two screens by hand**

The app is a recipe search client. Confirm, on the emulator screen:

1. The recipe list screen renders with the app bar and the food category chip row.
2. Search returns results and recipe cards show **images** (this exercises Landscapist + Glide, which no automated test covers).
3. Tapping a card opens the recipe detail screen with its image and text.
4. The shimmer placeholder appears while a list is loading.

- [ ] **Step 6: Check for runtime crashes and Hilt/Compose errors in logcat**

```bash
"$LOCALAPPDATA/Android/Sdk/platform-tools/adb.exe" logcat -d -t 500 \
  | grep -iE "FATAL|AndroidRuntime|Hilt|dagger|IllegalStateException|NoSuchMethodError|NoClassDefFoundError"
```

Expected: no output, or nothing referencing `com.abanobnageh.recipeapp`. `NoSuchMethodError` / `NoClassDefFoundError` here are the classic symptoms of a partially-updated library group and mean a fallback is needed even though the build was green.

- [ ] **Step 7: Record the result**

No commit — nothing changed. If everything passed, note in the task tracker that Tasks 1–5 are runtime-verified. If anything failed, identify the responsible task from the symptom and apply that task's fallback ladder, then re-run this whole task.

**Fallback ladder:** This task has no versions of its own. Map the symptom to the owning task:
- Missing/blank images → Task 9's landscapist (if already applied) or Task 5's Compose BOM
- Crash on launch mentioning Hilt/Dagger → Task 2 (Hilt 2.60.1) or Task 4 (androidx.hilt 1.4.0)
- Blank screen / composition error → Task 5 (Compose BOM) or Task 3 (Kotlin/Compose compiler)
- `hiltViewModel()` failure → Task 4

---

## Task 7: Update Gradle and AGP

Last among the version tasks because it is the most likely to churn and the least valuable if abandoned. Gradle and AGP move in the **same commit** — AGP 9.4.0 requires Gradle ≥ 9.6.0, so splitting them produces a knowingly-broken intermediate commit.

**Files:**
- Modify: `gradle/wrapper/gradle-wrapper.properties` (`distributionUrl`)
- Modify: `gradle/libs.versions.toml` (`agp`, `gradle`)

**Interfaces:**
- Consumes: everything above, runtime-verified by Task 6.
- Produces: the final toolchain.

- [ ] **Step 1: Update the wrapper distribution**

In `gradle/wrapper/gradle-wrapper.properties`, change:

```properties
distributionUrl=https\://services.gradle.org/distributions/gradle-9.5.0-bin.zip
```

to:

```properties
distributionUrl=https\://services.gradle.org/distributions/gradle-9.7.1-bin.zip
```

Edit the file directly rather than running `./gradlew wrapper --gradle-version 9.7.1`, which would also rewrite `gradlew`, `gradlew.bat` and the wrapper jar and make the diff much harder to review.

- [ ] **Step 2: Update AGP**

In `gradle/libs.versions.toml`, `agp` and `gradle` are duplicates that both mean AGP — `agp` feeds the plugin aliases, `gradle` feeds the `com.android.tools.build:gradle` library alias. Change both:

```toml
agp = "9.4.0"
gradle = "9.4.0"
```

- [ ] **Step 3: Verify all three values changed together**

```bash
grep -n 'distributionUrl' gradle/wrapper/gradle-wrapper.properties
grep -nE '^(agp|gradle) =' gradle/libs.versions.toml
```

Expected: `gradle-9.7.1-bin.zip`, `agp = "9.4.0"`, `gradle = "9.4.0"`. If AGP reads 9.4.x while the wrapper still reads 9.5.0, the build fails with an explicit minimum-Gradle-version error.

- [ ] **Step 4: Confirm the new Gradle downloads and the build configures**

```bash
./gradlew help --console=plain 2>&1 | tail -12
```

Expected: `Welcome to Gradle 9.7.1.` and `BUILD SUCCESSFUL`. The first run downloads the distribution.

- [ ] **Step 5: Run the standard gate**

```bash
./gradlew clean testDebugUnitTest assembleDebug --console=plain 2>&1 | tail -20
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 6: Confirm the test count held at 19/0/0**

```bash
find . -path "*/test-results/testDebugUnitTest/*.xml" -not -path "./build/*" \
  -exec grep -ho 'tests="[0-9]*" skipped="[0-9]*" failures="[0-9]*" errors="[0-9]*"' {} \; \
  | awk -F'"' '{t+=$2; f+=$6; e+=$8} END {print "tests="t, "failures="f, "errors="e}'
```

Expected: `tests=19 failures=0 errors=0`

- [ ] **Step 7: Confirm the deprecation warning count did not explode**

```bash
./gradlew help --console=plain --warning-mode all -Dorg.gradle.configuration-cache=false 2>&1 \
  | grep -c "WARNING\|Deprecated"
```

Record the number. The pre-existing baseline is roughly 40–60 warnings (the eight `android.*=false` flags plus legacy-variant-API and Groovy space-assignment warnings). A large jump means the new AGP deprecated something new — note it, but it is not a failure on its own.

- [ ] **Step 8: Commit**

```bash
git add gradle/wrapper/gradle-wrapper.properties gradle/libs.versions.toml
git commit -m "build: update Gradle to 9.7.1 and AGP to 9.4.0"
```

**Fallback ladder** — always change the wrapper and AGP as a matched pair:
1. Gradle `9.7.1` + AGP `9.4.0` — target
2. Gradle `9.6.1` + AGP `9.4.0` — AGP 9.4's stated Gradle minimum is **9.6.0**; 9.6.1 is the nearest patch above it and is already present in the local Gradle wrapper cache, so this rung isolates "AGP 9.4 is fine, Gradle 9.7 is not" without a fresh download
3. Gradle `9.5.0` (unchanged) + AGP `9.3.2` — AGP 9.3.x requires exactly Gradle 9.5.0, so this needs no wrapper change at all
4. Gradle `9.5.0` + AGP `9.2.1` — a patch bump only
5. Gradle `9.5.0` + AGP `9.2.0` — stay put

Expect deprecation *warnings* from the Kotlin plugin at rungs 1–2: KGP 2.4.10's fully-supported ceiling is Gradle 9.5.0 / AGP 9.1.0, so the target combination is outside the supported window on both axes. Warnings are acceptable; build failures are not. If rung 1 fails with a Kotlin plugin error specifically, also consider pairing rung 3 with Task 3's fallback of Kotlin 2.4.0.

---

## Task 8: Delete the dead ext block from root build.gradle

Seven `ext` properties in root `build.gradle` are referenced by no module — verified by grepping every `.gradle` file. They duplicate catalog values and are all stale (they claim Kotlin 2.3.10, Hilt 2.59.1, Compose compiler 1.5.15), so after this plan they would actively misinform.

**Files:**
- Modify: `build.gradle` (root)

**Interfaces:**
- Consumes: nothing. Independent of every other task.
- Produces: nothing. Pure cleanup.

- [ ] **Step 1: Re-prove the properties are unreferenced**

```bash
cd /d/Projects/RecipeApp
for v in compose_compiler_version compose_bom_version lifecycle_version kotlin_version \
         hilt_version landscapist_version okhttp_bom_version; do
  echo -n "$v: "
  grep -rl --include=*.gradle --include=*.kts "\$$v\|\"\$$v\"" . | grep -v "^\./build.gradle$" | tr '\n' ' '
  echo
done
```

Expected: every line shows only the variable name with no files after it. If any file IS listed, do not delete that property — leave it and note the exception.

- [ ] **Step 2: Delete the seven lines**

In root `build.gradle`, delete exactly these lines:

```groovy
ext.compose_compiler_version = "1.5.15"
ext.compose_bom_version = "2026.02.00"
ext.lifecycle_version = '2.10.0'
ext.kotlin_version = "2.3.10"
ext.hilt_version = "2.59.1"
ext.landscapist_version = '2.9.5'
ext.okhttp_bom_version = "5.3.2"
```

**Keep** these five — they are all live. `_minSdkVersion` is read by all five modules, `_compileSdkVersion` by the four library modules, and `_targetSdkVersion`/`_versionCode`/`_versionName` by `:app`:

```groovy
ext._minSdkVersion = 23
ext._compileSdkVersion = 37
ext._targetSdkVersion = 36
ext._versionCode = 1
ext._versionName = "1.0.0"
```

If this task runs before Task 1, `_compileSdkVersion` will still read `36` — that is fine, keep whatever value is there. This task must not change it.

- [ ] **Step 3: Verify only the live properties remain**

```bash
grep -n '^ext\.' build.gradle
```

Expected exactly five lines: `_minSdkVersion`, `_compileSdkVersion`, `_targetSdkVersion`, `_versionCode`, `_versionName`.

- [ ] **Step 4: Run the standard gate**

```bash
./gradlew clean testDebugUnitTest assembleDebug --console=plain 2>&1 | tail -20
```

Expected: `BUILD SUCCESSFUL`, `tests=19 failures=0 errors=0`. A `No such property` failure means Step 1 missed a reference — restore that one line.

- [ ] **Step 5: Commit**

```bash
git add build.gradle
git commit -m "build: remove unused ext version properties from root build script"
```

**Fallback ladder:** If the build fails with `No such property: <name>`, restore that single line and re-run. Deleting the other six is still worth committing.

---

## Task 9: Update Landscapist and drop the unused placeholder module

`landscapist-placeholder` is declared in `dependencies/build.gradle` but nothing under `src/` imports `com.skydoves.landscapist.placeholder` — the shimmer effect is hand-rolled in `ShimmerRecipeCard.kt` using Compose animation directly. Dropping it removes a dependency rather than updating it.

Placed after Task 6 because `landscapist-glide` is what renders every image in the app, and Task 6 established the working reference for that.

**Files:**
- Modify: `gradle/libs.versions.toml` (`landscapistGlide`; remove `landscapistPlaceholder` and its `[libraries]` entry)
- Modify: `dependencies/build.gradle` (remove the `api libs.landscapist.placeholder` line)

**Interfaces:**
- Consumes: Task 1's `compileSdk 37` (landscapist-glide 2.11.0 declares `minCompileSdk=37`).
- Produces: nothing downstream.

- [ ] **Step 1: Re-prove landscapist-placeholder is unused**

```bash
grep -rn "landscapist.placeholder\|landscapist\.shimmer\|PlaceholderPlugin\|ShimmerPlugin" \
  --include=*.kt . | grep -v "/build/"
```

Expected: **no output**. If anything IS found, skip the removal half of this task and only bump `landscapistGlide`; also bump `landscapistPlaceholder` to `2.11.0` in that case.

- [ ] **Step 2: Bump landscapist-glide and remove the placeholder version**

In `gradle/libs.versions.toml` `[versions]`, change:

```toml
landscapistGlide = "2.11.0"
```

and **delete**:

```toml
landscapistPlaceholder = "2.9.6"
```

- [ ] **Step 3: Remove the placeholder library alias**

In `[libraries]`, **delete**:

```toml
landscapist-placeholder = { module = "com.github.skydoves:landscapist-placeholder", version.ref = "landscapistPlaceholder" }
```

- [ ] **Step 4: Remove the dependency declaration**

In `dependencies/build.gradle`, **delete** the line:

```groovy
    api libs.landscapist.placeholder
```

leaving `api libs.landscapist.glide` immediately above it in place.

- [ ] **Step 5: Verify no dangling references**

```bash
grep -rn "landscapistPlaceholder\|landscapist.placeholder\|landscapist-placeholder" \
  gradle/libs.versions.toml dependencies/build.gradle
```

Expected: **no output**.

- [ ] **Step 6: Run the standard gate**

```bash
./gradlew clean testDebugUnitTest assembleDebug --console=plain 2>&1 | tail -20
```

Expected: `BUILD SUCCESSFUL`, `tests=19 failures=0 errors=0`.

- [ ] **Step 7: Re-verify images actually render**

Landscapist has no unit test coverage in this project, so the build passing proves nothing about rendering. With the emulator from Task 6 running:

```bash
./gradlew :app:installDebug --console=plain 2>&1 | tail -5
"$LOCALAPPDATA/Android/Sdk/platform-tools/adb.exe" shell am start -n com.abanobnageh.recipeapp/.MainActivity
```

Confirm on screen: recipe card images load on the list screen, the detail screen image loads, and the `empty_plate` drawable appears as the placeholder/error image rather than a blank box.

```bash
"$LOCALAPPDATA/Android/Sdk/platform-tools/adb.exe" logcat -d -t 300 | grep -iE "glide|landscapist|FATAL"
```

Expected: no fatal errors.

- [ ] **Step 8: Commit**

```bash
git add gradle/libs.versions.toml dependencies/build.gradle
git commit -m "build: update landscapist-glide to 2.11.0 and drop unused landscapist-placeholder"
```

**Fallback ladder** for `landscapistGlide`:
1. `2.11.0` — target
2. `2.10.0`
3. `2.9.8` — stay put

The placeholder removal is independent of the version bump: keep the removal even if the bump falls back. Note that landscapist-glide 2.11.0 depends on `compose-bom:2026.06.01`, which is *below* the 2026.08.00 set in Task 5, so it will not drag Compose backward — Gradle resolves to the higher version.

---

## Task 10: Update CI actions and widen Dependabot coverage

`.github/dependabot.yml` registers only the `gradle` ecosystem, so the GitHub Actions in the workflow have never received update PRs — they are three major versions behind. Adding the `github-actions` ecosystem prevents this from recurring.

**Files:**
- Modify: `.github/workflows/android.yml`
- Modify: `.github/dependabot.yml`

**Interfaces:**
- Consumes: nothing. Independent of every other task.
- Produces: CI that verifies the updated build on a clean machine — which is the real proof the plan worked, since the local machine has warm Gradle caches.

- [ ] **Step 1: Update the action versions**

In `.github/workflows/android.yml`, change:

```yaml
      - uses: actions/checkout@v3
      - name: set up JDK 17
        uses: actions/setup-java@v3
```

to:

```yaml
      - uses: actions/checkout@v7
      - name: set up JDK 17
        uses: actions/setup-java@v6
```

Leave `java-version: '17'`, `distribution: 'temurin'` and `cache: gradle` unchanged — the JDK stays at 17 per the global constraints.

- [ ] **Step 2: Add the github-actions ecosystem to Dependabot**

In `.github/dependabot.yml`, append below the existing `gradle` entry, keeping the same two-space list indentation:

```yaml
  # Updates for the GitHub Actions used in CI
  - package-ecosystem: github-actions
    directory: "/"
    schedule:
      interval: "weekly"
```

- [ ] **Step 3: Verify both files parse as YAML**

`pyyaml` is **not** installed on this machine, so install it first (one-off, into the user site-packages):

```bash
python -m pip install --quiet --user pyyaml
python -c "import yaml; [print(f, '->', 'OK' if yaml.safe_load(open(f)) else 'EMPTY') for f in ['.github/workflows/android.yml','.github/dependabot.yml']]"
```

Expected: both print `-> OK`.

If `pip install` is unavailable or blocked, skip this step — Step 4's greps plus the CI run in Task 11 Step 8 cover it. A YAML error in a workflow file surfaces on GitHub as a failed run with a parse error, not as a silent no-op.

- [ ] **Step 4: Confirm the intended versions are in place**

```bash
grep -nE "actions/(checkout|setup-java)@" .github/workflows/android.yml
grep -n "package-ecosystem" .github/dependabot.yml
```

Expected: `actions/checkout@v7`, `actions/setup-java@v6`, and two `package-ecosystem` lines (`gradle` and `github-actions`).

- [ ] **Step 5: Commit**

```bash
git add .github/workflows/android.yml .github/dependabot.yml
git commit -m "ci: update checkout/setup-java actions and add github-actions to Dependabot"
```

**Fallback ladder:** If CI fails on the new actions (Step 3 of Task 11 will reveal this), drop `actions/checkout` to `v6` and `actions/setup-java` to `v5`. Both are well within support.

---

## Task 11: Final end-to-end verification

**Files:** None modified. Verification only.

**Interfaces:**
- Consumes: every preceding task.
- Produces: the evidence needed to merge.

- [ ] **Step 1: Full clean build from an empty state**

Warm caches can hide resolution problems. Force a genuinely cold build.

A note on `clean`, because it is easy to get wrong: root `build.gradle` registers its own `clean` task as `delete rootProject.layout.buildDirectory`, which removes only `./build`. That looks like the module `build/` directories would survive — **they do not**. AGP applies Gradle's Base Plugin to every module, which registers a per-module `clean` ("Deletes the build directory"), and `./gradlew clean` matches that task name in all projects. Verified directly: after `./gradlew clean`, all five module `build/` directories are gone. So every task gate in this plan was a real clean build of module outputs.

What `clean` does *not* touch is `~/.gradle` — the dependency, transform and configuration caches. That is what makes a gate run in ~20s while a first-ever build takes minutes, and it is why this step adds `--refresh-dependencies`. The `rm -rf` below is belt-and-braces, not a workaround:

```bash
./gradlew clean --console=plain
rm -rf app/build core/build data/build dependencies/build feature-recipes/build
./gradlew testDebugUnitTest assembleDebug --refresh-dependencies --console=plain 2>&1 | tail -20
```

Expected: `BUILD SUCCESSFUL`, taking roughly 2–3 minutes with most tasks executed rather than up-to-date — `--refresh-dependencies` re-resolves every dependency, which is the slow part. If it finishes in seconds with everything up-to-date, the cold-ness did not take effect and the check is worthless.

- [ ] **Step 2: Confirm the final unit test count is still 19/0/0**

```bash
find . -path "*/test-results/testDebugUnitTest/*.xml" -not -path "./build/*" \
  -exec grep -ho 'tests="[0-9]*" skipped="[0-9]*" failures="[0-9]*" errors="[0-9]*"' {} \; \
  | awk -F'"' '{t+=$2; f+=$6; e+=$8} END {print "tests="t, "failures="f, "errors="e}'
```

Expected: `tests=19 failures=0 errors=0`

- [ ] **Step 3: Run the instrumentation tests on the emulator**

```bash
"$LOCALAPPDATA/Android/Sdk/platform-tools/adb.exe" devices
./gradlew :feature-recipes:connectedDebugAndroidTest --console=plain 2>&1 | tail -20
```

Expected: `BUILD SUCCESSFUL` with a non-zero test count and no failures.

- [ ] **Step 4: Full manual smoke test**

Install, launch, and confirm each of the following on the emulator screen:

1. The recipe list screen renders with the app bar and the food category chip row.
2. The shimmer placeholder animates while the list is loading.
3. Search returns results and recipe cards show **images** — this exercises Landscapist + Glide, which no automated test covers.
4. Tapping a card opens the recipe detail screen with its image and text.
5. Back navigation returns to the list with its state intact.
6. The `empty_plate` drawable appears for a recipe with no image, rather than a blank box.

```bash
./gradlew :app:installDebug --console=plain 2>&1 | tail -5
"$LOCALAPPDATA/Android/Sdk/platform-tools/adb.exe" shell am start -n com.abanobnageh.recipeapp/.MainActivity
"$LOCALAPPDATA/Android/Sdk/platform-tools/adb.exe" logcat -d -t 500 | grep -iE "FATAL|AndroidRuntime"
```

Expected: no fatal exceptions.

- [ ] **Step 5: Diff the resolved dependency graph against the Task 0 baseline**

```bash
./gradlew :app:dependencies --configuration debugRuntimeClasspath --console=plain -q > final-deps.txt
./gradlew :dependencies:dependencies --configuration debugRuntimeClasspath --console=plain -q >> final-deps.txt
diff baseline-deps.txt final-deps.txt | head -80
```

Read the diff. Every changed line should be explainable by a task in this plan. An unexpected **downgrade** is the thing to look for — it means a transitive dependency is holding something back and the catalog is again misreporting what ships.

- [ ] **Step 6: Review the full branch diff**

```bash
git log --oneline master..HEAD
git diff master..HEAD -- gradle/ build.gradle dependencies/build.gradle .github/
```

Expected: one commit per task, and **no changes under any `src/` directory**. If `src/` changed, a task exceeded its scope — investigate before merging.

- [ ] **Step 7: Clean up the scratch files**

```bash
rm -f baseline-deps.txt final-deps.txt
git status --short
```

Expected: clean working tree.

- [ ] **Step 8: Push and let CI verify on a cold machine**

```bash
git push -u origin chore/dependency-update-2026-09
```

The workflow triggers on `push` and runs `testDebugUnitTest` then `assembleDebug` on `ubuntu-latest` with JDK 17 and no warm caches. Wait for it to go green:

```bash
gh run watch
```

Expected: both steps pass. CI is the real gate — it is the only environment without the local machine's warm Gradle cache and pre-installed SDK platforms.

- [ ] **Step 9: Record the final state**

Append a "Final state" section to `docs/superpowers/plans/2026-09-05-dependency-update-findings.md` listing, for each entry in the "Available versions" table, the version actually landed on and — for any that fell back — which rung of the ladder was used and the error that forced it. Commit:

```bash
git add docs/superpowers/plans/2026-09-05-dependency-update-findings.md
git commit -m "docs: record final versions landed by the dependency update"
git push
```

**Fallback ladder:** If CI fails but local passes, the difference is almost always a missing SDK platform on the runner. `compileSdk 37` requires the runner to fetch `platforms;android-37`, which `actions/setup-java` does not do — AGP auto-downloads it if licenses are accepted. If CI fails with a missing-platform error, add an SDK setup step (`android-actions/setup-android@v3`) to the workflow before the Gradle steps.

---

## Task Summary

| # | Task | Risk | Revert cost if it fails |
|---|---|---|---|
| 0 | Baseline + findings | none | n/a |
| 1 | compileSdk 37 | low | **blocks 4, 5, 6, 9** |
| 2 | Hilt / OkHttp / AppCompat / Lifecycle / KSP | medium (Hilt has breaking changes) | isolated |
| 3 | Kotlin + Compose compiler 2.4.10 | medium (language version bump) | isolated |
| 4 | core-ktx / navigation / androidx.hilt | medium | isolated |
| 5 | Compose BOM + remove pins | medium (M2 surface) | isolated |
| 6 | Runtime verification | none | n/a |
| 7 | Gradle 9.7.1 + AGP 9.4.0 | **highest** (outside KGP's supported window) | isolated, 5-rung ladder |
| 8 | Delete dead ext block | very low | isolated |
| 9 | Landscapist + drop unused module | low | isolated |
| 10 | CI actions + Dependabot | very low | isolated |
| 11 | Final verification | none | n/a |

Tasks 8 and 10 are independent of everything and can be done at any point. Task 1 is the only task whose failure blocks others.
