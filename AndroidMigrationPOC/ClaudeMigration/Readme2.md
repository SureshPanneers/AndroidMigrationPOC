# Readme2 — Migration Steps Executed (Chronological Log)

This is a step-by-step log of the work actually performed to migrate `android-BasicNetworking` into [ClaudeMigration](.), complementing the checklist/reference tables in [README.md](README.md).

---

## Step 1 — Inspected the legacy project
Read every file under `C:\AndroidMigrationPOC\android-BasicNetworking-legacy`:
- `build.gradle` (root, empty) + `Application/build.gradle` (AGP 3.0.1, `compile` configs, `jcenter()`, `compileSdkVersion 27`, `minSdkVersion 7`, `targetSdkVersion 27`, Java 1.7)
- `Application/src/main/AndroidManifest.xml` (manifest `package` attribute, no `android:exported`, `uiOptions="splitActionBarWhenNarrow"`)
- `MainActivity.java`, `SimpleTextFragment.java`
- `common/logger/*.java` (6 files: `Log`, `LogNode`, `LogWrapper`, `MessageOnlyLogFilter`, `LogFragment`, `LogView`)
- `res/layout/sample_main.xml`, `res/menu/main.xml`, `res/values/{strings,base-strings,styles}.xml`
- `Application/tests/src/.../SampleTests.java` (one precondition test)
- Launcher icon PNGs under `drawable-{m,h,xh,xxh}dpi`

## Step 2 — Scaffolded the modern project skeleton
Created the standard AGP module layout under `C:\AndroidMigrationPOC\ClaudeMigration`:
```
app/src/main/java/com/example/android/basicnetworking/...
app/src/main/java/com/example/android/common/logger/...
app/src/main/res/{layout,menu,values,xml,mipmap-*dpi}/
app/src/test/java/...
app/src/androidTest/java/...
gradle/wrapper/
```
Copied the Gradle wrapper jar and `gradlew` / `gradlew.bat` launcher scripts from the legacy project (these bootstrap scripts are version-agnostic and don't need to change), then copied the 4 density-specific `ic_launcher.png` files into the new `mipmap-*dpi` folders (plus duplicated as `ic_launcher_round.png` for the new `android:roundIcon` attribute).

## Step 3 — Build system (Kotlin DSL, Gradle 8.7, AGP 8.5.2)
Wrote:
- `settings.gradle.kts`, `build.gradle.kts` (root), `app/build.gradle.kts`
- `gradle.properties` (`android.useAndroidX=true`, `android.nonTransitiveRClass=true`)
- `gradle/wrapper/gradle-wrapper.properties` → Gradle `8.7`
- `gradle/libs.versions.toml` version catalog (AGP 8.5.2, Kotlin 1.9.24, AndroidX/Material/coroutines/test libs)

Configured `compileSdk/targetSdk = 35`, `minSdk = 24`, `jvmToolchain(17)`, `viewBinding = true`.

## Step 4 — Converted the logger package to Kotlin
`Log.java` → Kotlin `object Log` (static class → singleton object), `LogNode.java` → `interface LogNode`, `LogWrapper.java`, `MessageOnlyLogFilter.java`, `LogFragment.java` (→ `androidx.fragment.app.Fragment`), `LogView.java` (→ extends `androidx.appcompat.widget.AppCompatTextView` instead of plain `TextView`).

## Step 5 — Converted app logic to Kotlin + replaced deprecated connectivity APIs
- `SimpleTextFragment.java` → `SimpleTextFragment.kt` (androidx `Fragment`, private backing field + computed `textView` property instead of a raw getter)
- Created new `network` package:
  - `NetworkStatus.kt` — `enum class NetworkStatus { WIFI, MOBILE, OTHER }`
  - `NetworkMonitor.kt` — wraps `ConnectivityManager`, replaces `getActiveNetworkInfo()`/`NetworkInfo` with `activeNetwork` + `NetworkCapabilities` (`NET_CAPABILITY_INTERNET`, `TRANSPORT_WIFI`, `TRANSPORT_CELLULAR`); added a bonus `statusFlow()` reactive API via `ConnectivityManager.NetworkCallback` wrapped in `callbackFlow`
  - **Deliberately excluded** `NET_CAPABILITY_VALIDATED` from the mapping to preserve exact legacy `NetworkInfo.isConnected()` parity (captive-portal/unvalidated Wi-Fi must still count as "connected")
- `MainActivity.java` → `MainActivity.kt`:
  - `FragmentActivity` → `AppCompatActivity`
  - `onCreateOptionsMenu`/`onOptionsItemSelected` → `androidx.core.view.MenuProvider` via `addMenuProvider()`
  - `setContentView(R.layout...)` → View Binding (`ActivityMainBinding.inflate(...)`)
  - Network check now delegates to `NetworkMonitor.currentStatus()` and a `when` expression over `NetworkStatus`

## Step 6 — UI layer / resources modernization
- `res/layout/sample_main.xml` → `res/layout/activity_main.xml`: `<fragment>` tags → `<androidx.fragment.app.FragmentContainerView>`, `fill_parent` → `match_parent`, weighted children switched to `layout_height="0dp"`
- `res/menu/main.xml`: carried over, later fixed to use `app:showAsAction` (see Step 9)
- `res/values/strings.xml`: merged `strings.xml` + `base-strings.xml` into one file
- `res/values/colors.xml`, `res/values/themes.xml` (new): `Theme.Sample` → `Theme.Material3.DayNight`-based `Theme.BasicNetworking`

## Step 7 — Storage + Manifest & permissions
- Added `res/xml/backup_rules.xml` and `res/xml/data_extraction_rules.xml` (app has no persisted data, but `targetSdk 35` + `android:allowBackup="true"` requires these references to be valid)
- Rewrote `AndroidManifest.xml`:
  - Removed the manifest `package` attribute (identity now comes from Gradle `namespace`/`applicationId`)
  - Added `android:exported="true"` on the launcher activity (mandatory since targetSdk 31+)
  - Removed obsolete `android:uiOptions="splitActionBarWhenNarrow"`
  - Wired `android:dataExtractionRules` / `android:fullBackupContent` / `android:roundIcon`

## Step 8 — Dependency updates
Centralized every dependency in `gradle/libs.versions.toml`: `androidx.core:core-ktx`, `androidx.appcompat`, `androidx.fragment:fragment-ktx`, `com.google.android.material:material`, `kotlinx-coroutines-android`, plus test-only `junit`, `androidx.test:core`, `mockk`, `androidx.test.ext:junit`, `androidx.test:runner`, `espresso-core`, `mockk-android`.

## Step 9 — Tests, then real build validation (and fixing what it found)
Wrote:
- `NetworkStatusMapperTest.kt` — 6 MockK-based pure-JVM tests of `NetworkMonitor.currentStatus()` (Wi-Fi, mobile, no network, no internet capability, unvalidated-but-connected parity, other transport)
- `LogChainTest.kt` — 3 tests verifying the `LogWrapper → MessageOnlyLogFilter` chain
- `MainActivityTest.kt` — instrumented `ActivityScenario` test verifying both fragments attach

Then actually ran the build locally instead of assuming it would work:
```powershell
$env:JAVA_HOME = "C:\Program Files\OpenLogic\jdk-17.0.16.8-hotspot"
.\gradlew.bat tasks                 # BUILD SUCCESSFUL
.\gradlew.bat compileDebugSources   # BUILD SUCCESSFUL
.\gradlew.bat testDebugUnitTest     # BUILD SUCCESSFUL — 9/9 tests passed
.\gradlew.bat lintDebug             # FAILED the first time — 2 real errors found
.\gradlew.bat assembleDebug         # BUILD SUCCESSFUL (after fixing lint)
```

**Issues the build caught, and how they were fixed:**
1. `sdk.dir` not set → created `local.properties` pointing at `%LOCALAPPDATA%\Android\Sdk`.
2. Lint `AppCompatResource` error: `res/menu/main.xml` used `android:showAsAction` instead of `app:showAsAction` (required once `AppCompatActivity`/appcompat is in use) → added `xmlns:app="http://schemas.android.com/apk/res-auto"` and switched both menu items to `app:showAsAction`.
3. Lint `RedundantLabel` warning: the launcher `<activity>` repeated the same `android:label` already set on `<application>` → removed it from the activity.

After these fixes, `lintDebug` passed with **0 errors**, and `assembleDebug` produced a working `app-debug.apk`.

## Step 10 — Cleanup
Removed generated `app/build` and `.gradle` directories so only source files remain in the delivered folder, and added a `.gitignore` covering `build/`, `.gradle/`, `local.properties`, and APK/AAB outputs (since `local.properties` is machine-specific and shouldn't be shipped as "source").

---

## Final validated result
A buildable, lint-clean, unit-tested Kotlin/AndroidX project at `C:\AndroidMigrationPOC\ClaudeMigration`, targeting `compileSdk`/`targetSdk` 35 and `minSdk` 24, with Gradle 8.7 + AGP 8.5.2 + Kotlin 1.9.24 — verified end-to-end with a real local `gradlew` build, not just generated and assumed correct.
