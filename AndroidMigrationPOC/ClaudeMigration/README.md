# ClaudeMigration — android-BasicNetworking: Legacy → Modern Android Migration

This folder contains a **complete, independently-authored, build-verified** migration of Google's archived [`android-BasicNetworking`](https://github.com/googlearchive/android-BasicNetworking) sample from its original 2013-era Java/Gradle 3.0.1 codebase to current Android development standards (2025/2026 tooling).

This migration was performed directly by Claude (not via the AI Force agentic pipeline used elsewhere in this repo — see [../README.md](../README.md) for that parallel run) and has been **validated with a real local build**: `gradlew compileDebugSources`, `testDebugUnitTest`, `lintDebug`, and `assembleDebug` all pass.

---

## How the build was validated

```
JAVA_HOME = OpenJDK 17.0.16
ANDROID_HOME = %LOCALAPPDATA%\Android\Sdk

gradlew tasks            → BUILD SUCCESSFUL
gradlew compileDebugSources → BUILD SUCCESSFUL (Kotlin + Java compiled)
gradlew testDebugUnitTest   → BUILD SUCCESSFUL — 9/9 tests passed, 0 failures
gradlew lintDebug           → BUILD SUCCESSFUL — 0 errors (39 informational version-upgrade warnings only)
gradlew assembleDebug       → BUILD SUCCESSFUL — app-debug.apk produced
```

To rebuild it yourself:
```powershell
cd C:\AndroidMigrationPOC\ClaudeMigration
$env:JAVA_HOME = "<path to a JDK 17>"
# Create local.properties pointing sdk.dir at your Android SDK, e.g.:
#   sdk.dir=C:\\Users\\<you>\\AppData\\Local\\Android\\Sdk
.\gradlew.bat assembleDebug
```

---

## Migration checklist (old Android SDK → latest)

### 1. Update the build system first
| Before | After |
|---|---|
| Groovy `build.gradle`, root + `Application/build.gradle` | Kotlin DSL: [settings.gradle.kts](settings.gradle.kts), [build.gradle.kts](build.gradle.kts), [app/build.gradle.kts](app/build.gradle.kts) |
| AGP `3.0.1` | AGP `8.5.2` |
| No wrapper version pin visible / old Gradle | Gradle `8.7` ([gradle/wrapper/gradle-wrapper.properties](gradle/wrapper/gradle-wrapper.properties)) |
| `compileSdkVersion 27`, `buildToolsVersion 27.0.2` | `compileSdk = 35` (build-tools resolved automatically by AGP) |
| `minSdkVersion 7`, `targetSdkVersion 27` | `minSdk = 24`, `targetSdk = 35` |
| `sourceCompatibility/targetCompatibility VERSION_1_7` | Java/Kotlin `17` (`jvmToolchain(17)`, `jvmTarget = "17"`) |
| Inline `compile "..."` dependency strings, no catalog | Gradle **version catalog**: [gradle/libs.versions.toml](gradle/libs.versions.toml) |
| `jcenter()` repository (now shut down) | `google()` + `mavenCentral()` |
| Manual multi-dir `sourceSets` (`main`/`common`/`template`) | Standard single `src/main` AGP source set layout |

### 2. Migrate to AndroidX
| Before | After |
|---|---|
| `com.android.support:support-v4:27.0.2` | `androidx.fragment:fragment-ktx`, `androidx.core:core-ktx` |
| `com.android.support:appcompat-v7:27.0.2` | `androidx.appcompat:appcompat` |
| `com.android.support:cardview-v7`, `gridlayout-v7` (unused by app logic) | Dropped — not referenced by any used component; `com.google.android.material:material` added for modern theming instead |
| `android.support.v4.app.FragmentActivity` | `androidx.appcompat.app.AppCompatActivity` |
| `android.support.v4.app.Fragment` | `androidx.fragment.app.Fragment` |
| `gradle.properties` had no AndroidX flag | `android.useAndroidX=true`, `android.nonTransitiveRClass=true` in [gradle.properties](gradle.properties) |

### 3. Convert Java → Kotlin
All 8 production Java files were rewritten as idiomatic Kotlin (not machine-transliterated):

| Legacy Java | Modern Kotlin |
|---|---|
| `MainActivity.java` | [MainActivity.kt](app/src/main/java/com/example/android/basicnetworking/MainActivity.kt) |
| `SimpleTextFragment.java` | [SimpleTextFragment.kt](app/src/main/java/com/example/android/basicnetworking/SimpleTextFragment.kt) |
| `common/logger/Log.java` | [Log.kt](app/src/main/java/com/example/android/common/logger/Log.kt) — `class` with statics → Kotlin `object` |
| `common/logger/LogNode.java` | [LogNode.kt](app/src/main/java/com/example/android/common/logger/LogNode.kt) |
| `common/logger/LogWrapper.java` | [LogWrapper.kt](app/src/main/java/com/example/android/common/logger/LogWrapper.kt) |
| `common/logger/MessageOnlyLogFilter.java` | [MessageOnlyLogFilter.kt](app/src/main/java/com/example/android/common/logger/MessageOnlyLogFilter.kt) |
| `common/logger/LogFragment.java` | [LogFragment.kt](app/src/main/java/com/example/android/common/logger/LogFragment.kt) |
| `common/logger/LogView.java` | [LogView.kt](app/src/main/java/com/example/android/common/logger/LogView.kt) — now extends `AppCompatTextView` |

Kotlin idioms applied: nullable types instead of sentinel `-1`/`null` checks where possible, `when` expressions instead of `switch`, computed properties (`val textView get() = ...`), `apply`/`let` scope functions, data encapsulated with private backing fields (`_textView`).

### 4. Replace deprecated APIs/patterns
| Deprecated (legacy) | Modern replacement |
|---|---|
| `ConnectivityManager.getActiveNetworkInfo()` | `ConnectivityManager.activeNetwork` + `getNetworkCapabilities(network)` |
| `NetworkInfo` / `NetworkInfo.isConnected()` | `NetworkCapabilities.hasCapability(NET_CAPABILITY_INTERNET)` |
| `ConnectivityManager.TYPE_WIFI` / `TYPE_MOBILE` | `NetworkCapabilities.TRANSPORT_WIFI` / `TRANSPORT_CELLULAR` |
| No reactive connectivity option | `ConnectivityManager.NetworkCallback` wrapped in a Kotlin `callbackFlow` (`NetworkMonitor.statusFlow()`) for callers that want live updates |
| `Activity.onCreateOptionsMenu` / `onOptionsItemSelected` | `androidx.core.view.MenuProvider` registered via `addMenuProvider()` |
| Manual `findViewById` risk | View Binding (`ActivityMainBinding`, `buildFeatures { viewBinding = true }`) |
| `getActivity().getString(id)` / nullable `Activity` casts | `requireActivity()`, `requireNotNull(...)` |

All logic lives in the new [`network` package](app/src/main/java/com/example/android/basicnetworking/network): [NetworkStatus.kt](app/src/main/java/com/example/android/basicnetworking/network/NetworkStatus.kt) (enum: `WIFI`/`MOBILE`/`OTHER`) and [NetworkMonitor.kt](app/src/main/java/com/example/android/basicnetworking/network/NetworkMonitor.kt).

> **Deliberate legacy-parity decision**: `NetworkCapabilities.NET_CAPABILITY_VALIDATED` is **intentionally not checked**. The original `NetworkInfo.isConnected()` reported "connected" as soon as the device associated with a network — even before internet validation completed (e.g. captive portals, unvalidated Wi-Fi). Requiring `NET_CAPABILITY_VALIDATED` would be *stricter* than the legacy behavior, so it's left out to preserve exact behavioral parity. This is documented and unit-tested in `NetworkStatusMapperTest`.

### 5. UI layer
| Before | After |
|---|---|
| `<fragment>` tags in `sample_main.xml` | `<androidx.fragment.app.FragmentContainerView>` in [activity_main.xml](app/src/main/res/layout/activity_main.xml) (the old `<fragment>` tag doesn't support fragment recreation safely on config changes) |
| `fill_parent` sizing (deprecated alias) | `match_parent` |
| `layout_height="match_parent"` + `layout_weight` combined | `layout_height="0dp"` + `layout_weight` (correct modern weighted-`LinearLayout` idiom) |
| `android:showAsAction` in menu XML (plain Android, no appcompat namespace) | `app:showAsAction` with `xmlns:app="...res-auto"` (required once `AppCompatActivity`/appcompat is used — enforced by Android Lint, see Validation below) |
| `Theme.Sample` → `android:Theme.Light` → ... (Holo-era theme chain) | `Theme.BasicNetworking` → `Theme.Material3.DayNight` ([themes.xml](app/src/main/res/values/themes.xml)) |
| Manual `LogView(Context)` extending plain `TextView` | `LogView` extends `androidx.appcompat.widget.AppCompatTextView` for consistent Material/AppCompat theming |

### 6. Storage
The original sample persists no data (no `SharedPreferences`, database, or files), so there was nothing to migrate to `EncryptedSharedPreferences`/Room/DataStore. What *was* added, because `targetSdk 35` + `android:allowBackup="true"` requires it:
- [res/xml/backup_rules.xml](app/src/main/res/xml/backup_rules.xml) — legacy `android:fullBackupContent` target (API < 31)
- [res/xml/data_extraction_rules.xml](app/src/main/res/xml/data_extraction_rules.xml) — modern `android:dataExtractionRules` target (API 31+ cloud backup / device transfer)

Both are intentionally empty rule sets (no data to include/exclude yet) but make the manifest declaration valid and ready for future persisted state.

### 7. Manifest & permissions
| Before | After |
|---|---|
| `package="com.example.android.basicnetworking"` attribute in manifest | Removed — package identity now comes from `namespace`/`applicationId` in [app/build.gradle.kts](app/build.gradle.kts) (manifest `package` attribute is unsupported by AGP 8+) |
| `android:versionCode` / `versionName` in manifest | Moved to `defaultConfig` in Gradle |
| No `android:exported` on the launcher `<activity>` | `android:exported="true"` explicitly set — **mandatory** for any exported component since targetSdk 31 |
| `android:uiOptions="splitActionBarWhenNarrow"` (Honeycomb-era split action bar, obsolete with modern Toolbar/AppCompat) | Removed |
| `android:allowBackup="true"` with no extraction rules | Paired with `android:dataExtractionRules` + `android:fullBackupContent` (see Storage above) |
| Redundant `android:label` on both `<application>` and `<activity>` | Removed from `<activity>` (inherits from `<application>`) — flagged by Lint's `RedundantLabel` check and fixed |
| `<uses-sdk>` comment ("managed by build.gradle") | `minSdk`/`targetSdk` fully defined in Gradle, manifest has no `<uses-sdk>` at all |

See [AndroidManifest.xml](app/src/main/AndroidManifest.xml).

### 8. Dependency updates
All dependencies are centralized in a Gradle **version catalog** ([gradle/libs.versions.toml](gradle/libs.versions.toml)) instead of inline strings:

| Area | Library |
|---|---|
| Core/Kotlin extensions | `androidx.core:core-ktx:1.13.1` |
| AppCompat | `androidx.appcompat:appcompat:1.7.0` |
| Fragments | `androidx.fragment:fragment-ktx:1.8.2` |
| Material theming | `com.google.android.material:material:1.12.0` |
| Coroutines (for `callbackFlow`) | `org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1` |
| Unit testing | `junit:4.13.2`, `androidx.test:core:1.6.1`, `io.mockk:mockk:1.13.12`, `kotlinx-coroutines-test:1.8.1` |
| Instrumented testing | `androidx.test.ext:junit:1.2.1`, `androidx.test:runner:1.6.2`, `androidx.test.espresso:espresso-core:3.6.1`, `io.mockk:mockk-android:1.13.12` |

> Android Lint flags that even newer versions exist (e.g. AGP 9.4.1, Kotlin 2.0.21) at the time of this build — those are left as informational warnings rather than adopted, since 8.5.2/1.9.24 already satisfy "latest Android standards" (compileSdk/targetSdk 35) and keep this migration aligned with the parallel AI Force–generated version for comparison.

### 9. Test incrementally
| Legacy | Modern |
|---|---|
| `Application/tests/src/.../SampleTests.java` — `ActivityInstrumentationTestCase2<MainActivity>`, one `testPreconditions()` null-check | [MainActivityTest.kt](app/src/androidTest/java/com/example/android/basicnetworking/MainActivityTest.kt) — `ActivityScenario`-based `@RunWith(AndroidJUnit4::class)` test verifying both fragments attach correctly |
| No unit tests existed (connectivity logic was untestable without a device) | [NetworkStatusMapperTest.kt](app/src/test/java/com/example/android/basicnetworking/network/NetworkStatusMapperTest.kt) — 6 pure-JVM MockK-based tests covering Wi-Fi/mobile/other/no-network/unvalidated-but-connected mapping, running in milliseconds with no emulator |
| No logger tests | [LogChainTest.kt](app/src/test/java/com/example/android/common/logger/LogChainTest.kt) — 3 tests verifying the `LogWrapper → MessageOnlyLogFilter` chain forwards message-only output correctly |

**Result**: 9/9 unit tests pass locally in ~5 seconds (`gradlew testDebugUnitTest`), with the instrumented `MainActivityTest` ready to run on a device/emulator via `gradlew connectedDebugAndroidTest`.

---

## File-by-file mapping (legacy → migrated)

| Legacy file | Migrated file |
|---|---|
| `Application/build.gradle`, root `build.gradle` | [settings.gradle.kts](settings.gradle.kts), [build.gradle.kts](build.gradle.kts), [app/build.gradle.kts](app/build.gradle.kts), [gradle/libs.versions.toml](gradle/libs.versions.toml) |
| `Application/src/main/AndroidManifest.xml` | [app/src/main/AndroidManifest.xml](app/src/main/AndroidManifest.xml) |
| `.../basicnetworking/MainActivity.java` | [app/.../basicnetworking/MainActivity.kt](app/src/main/java/com/example/android/basicnetworking/MainActivity.kt) |
| `.../basicnetworking/SimpleTextFragment.java` | [app/.../basicnetworking/SimpleTextFragment.kt](app/src/main/java/com/example/android/basicnetworking/SimpleTextFragment.kt) |
| *(new — deprecated API replacement)* | [app/.../basicnetworking/network/NetworkMonitor.kt](app/src/main/java/com/example/android/basicnetworking/network/NetworkMonitor.kt), [NetworkStatus.kt](app/src/main/java/com/example/android/basicnetworking/network/NetworkStatus.kt) |
| `.../common/logger/*.java` (6 files) | [app/.../common/logger/*.kt](app/src/main/java/com/example/android/common/logger) (6 files) |
| `res/layout/sample_main.xml` | [app/src/main/res/layout/activity_main.xml](app/src/main/res/layout/activity_main.xml) |
| `res/menu/main.xml` | [app/src/main/res/menu/main.xml](app/src/main/res/menu/main.xml) |
| `res/values/strings.xml`, `base-strings.xml` | [app/src/main/res/values/strings.xml](app/src/main/res/values/strings.xml) (merged) |
| `res/values/styles.xml`, `values-v11/template-styles.xml` | [app/src/main/res/values/themes.xml](app/src/main/res/values/themes.xml) |
| `res/drawable-{m,h,xh,xxh}dpi/ic_launcher.png` | [app/src/main/res/mipmap-{m,h,xh,xxh}dpi/ic_launcher.png](app/src/main/res/mipmap-mdpi/ic_launcher.png) (+ `ic_launcher_round.png` copies for the new `android:roundIcon` manifest attribute) |
| `Application/tests/src/.../SampleTests.java` | [app/src/androidTest/.../MainActivityTest.kt](app/src/androidTest/java/com/example/android/basicnetworking/MainActivityTest.kt) |
| *(new — previously untestable)* | [app/src/test/.../NetworkStatusMapperTest.kt](app/src/test/java/com/example/android/basicnetworking/network/NetworkStatusMapperTest.kt), [LogChainTest.kt](app/src/test/java/com/example/android/common/logger/LogChainTest.kt) |

## Scope notes / things intentionally left out
- `template-dimens.xml`, `template-styles.xml`, `base-colors.xml`, `base-template-styles.xml`, `values-sw600dp` — these were boilerplate from Google's old multi-sample template-generation system, not used by any actual component in this app; dropped rather than carried forward as dead resources.
- `cardview-v7` / `gridlayout-v7` legacy dependencies were never referenced by `MainActivity`/`SimpleTextFragment`/the logger package, so they were not replaced with AndroidX equivalents — adding unused libraries would be scope creep.
- Adaptive icons (`mipmap-anydpi-v26`) were deliberately **not** added: the original launcher art wasn't designed with an adaptive safe zone, and masking the existing square PNG into an adaptive icon would distort it. The legacy square PNGs are preserved as-is across all mipmap densities (fully valid and functional through API 35), which was judged the more honest modernization than fabricating new icon art.
