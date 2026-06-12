# REDU Android App Bug Report

Date: 2026-06-10
Scope: `android-app/`

## Validation Summary

Commands run from `android-app/`:

| Check | Result | Evidence |
| --- | --- | --- |
| `./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug --console=plain` | Passed | Unit tests, lint task, and debug APK assembly complete successfully. |
| `./gradlew :app:assembleRelease --console=plain` | Failed | Native CMake configure fails in vendored `llama.cpp/tools/mtmd/CMakeLists.txt:48`. |
| `./gradlew :app:connectedDebugAndroidTest --console=plain` | Blocked | Fails with `DeviceException: No connected devices!`. |
| `adb devices` | No devices | Output contains only `List of devices attached`. |
| `emulator -list-avds` | Blocked | `emulator` command is not on PATH. |
| Model URL HEAD checks | Passed | Hugging Face linked sizes and linked SHA-256 etags match `ModelDownloadManager.MODEL_FILES`. |

Lint generated `app/build/reports/lint-results-debug.txt` and includes warnings relevant to this report, especially API-level receiver flags and `PromptPresenter` static view leakage.

## Confirmed Bugs

### P0 - Release Build Is Blocked

**Impact:** The app cannot produce a release APK/AAB from the current tree.

**Validation:** `./gradlew :app:assembleRelease --console=plain` fails reproducibly.

**Evidence:**
- `app/src/main/cpp/llama.cpp/tools/mtmd/CMakeLists.txt:48`
- Error: `set_target_properties called with incorrect number of arguments`
- Task: `:app:configureCMakeRelWithDebInfo[arm64-v8a]`

**Likely cause:** The vendored `mtmd` CMake target sets version properties in a way that CMake 3.22.1 rejects in the Android release/`RelWithDebInfo` native configure path.

**Recommended fix:**
- Patch or pin the vendored `llama.cpp`/`mtmd` CMake so `set_target_properties(mtmd PROPERTIES VERSION ... SOVERSION ... MACHO_CURRENT_VERSION ...)` always receives valid key/value pairs.
- Add `:app:assembleRelease` to CI or the thesis release checklist.

### P1 - API 33 Receiver Flags Used While `minSdk` Is 26

**Impact:** Accessibility service startup is at risk on API 26-32 devices.

**Validation:** Android lint flags `Context.RECEIVER_NOT_EXPORTED` as requiring API 33 while `minSdk = 26`.

**Evidence:**
- `app/build.gradle.kts:13` sets `minSdk = 26`
- `app/src/main/java/edu/feutech/redu/capture/ReduAccessibilityService.kt:173`
- `app/src/main/java/edu/feutech/redu/capture/ReduAccessibilityService.kt:175`
- Lint issue: `InlinedApi`

**Recommended fix:**
- Replace direct platform calls with `ContextCompat.registerReceiver(...)`, or guard the API 33 overload with `Build.VERSION.SDK_INT >= 33` and use the older overload on lower versions.

### P1 - Intervention Timing Feels Random Because Risk Is Not Required To Be Sustained

**Impact:** Users can receive interventions during intentional or benign scrolling, undermining trust in the intervention.

**Validation:** Code review and numeric risk simulation. `PromptPolicy` triggers as soon as the live risk snapshot reaches a prompt-eligible level after the 15-minute minimum duration; there is no sustained-risk window or consecutive-item confirmation.

**Evidence:**
- `app/src/main/java/edu/feutech/redu/capture/ReduAccessibilityService.kt:397`
- `app/src/main/java/edu/feutech/redu/prompt/PromptPolicy.kt:27`
- `app/src/main/java/edu/feutech/redu/prompt/PromptPolicy.kt:35`

**Reproduced model behavior:**
- `meanDwell=12s`, `NSD=50%`, `duration=15m` => score `50.00`, `WARNING`, `L2_PAUSE`
- `meanDwell=8s`, `NSD=50%`, `duration=15m` => score `50.00`, `WARNING`, `L2_PAUSE`
- `meanDwell=12s`, `NSD=0%`, `duration=15m` => score `50.00`, `WARNING`, `L2_PAUSE`

The last case is important: a non-negative session can still produce an L2 prompt from dwell and duration alone.

**Recommended fix:**
- Require sustained risk before prompting, for example `risk >= WARNING` for 60 seconds or 2-3 item transitions.
- Separate "duration-only" prompts from "negative-content" prompts so the intervention text matches the trigger.
- Persist trigger reasons with prompt events for auditability.

### P1 - NSD Can False-Trigger From Small Evidence Counts

**Impact:** One negative classification in a tiny sample can produce `50%` NSD and trigger intervention on content the user perceives as non-negative.

**Validation:** Code review and numeric simulation.

**Evidence:**
- `app/src/main/java/edu/feutech/redu/capture/SessionTracker.kt:291` computes `negativeUnits / resolvableUnits * 100.0`
- `app/src/main/java/edu/feutech/redu/risk/FuzzyRiskEngine.kt:50` centers medium NSD at `50.0`
- `app/src/main/java/edu/feutech/redu/prompt/PromptPolicy.kt:35` maps warning scores at or above `50.0` to `L2_PAUSE`

**Recommended fix:**
- Require a minimum evidence count before NSD can drive prompts, e.g. `resolvableUnits >= 5`.
- Use confidence or agreement gates for VLM/text sentiment.
- Export the exact prompt trigger inputs so false positives can be audited.

### P1 - Prompt Design Is Easy To Disregard And Does Not Adapt

**Impact:** User observation says interventions are often ignored and do not prevent doomscroll continuation.

**Validation:** Code review confirms prompt outcomes are logged, but future prompt strategy does not adapt to `CONTINUE`, `DISMISSED`, `TAKE_BREAK`, or rapid return to scrolling.

**Evidence:**
- `app/src/main/java/edu/feutech/redu/prompt/PromptPolicy.kt:13`
- `app/src/main/java/edu/feutech/redu/capture/ReduAccessibilityService.kt:870`
- `app/src/main/java/edu/feutech/redu/prompt/PromptPresenter.kt:150` includes a direct continue option for L2
- `app/src/main/java/edu/feutech/redu/prompt/PromptPresenter.kt:316` includes a skip path for L3

**Recommended fix:**
- Feed prompt outcomes back into policy.
- Track post-prompt continuation and time-to-return.
- Consider stronger but still ethical friction: delayed continue, short reflection, or escalating copy after repeated disregards.

### P1 - Active Prompt Overlay Can Leak After Service Teardown

**Impact:** An active L2/L3 overlay can hold a `View`/`WindowManager` after the accessibility service is destroyed.

**Validation:** Lint reports `StaticFieldLeak`; code review confirms `onDestroy()` does not dismiss active prompts.

**Evidence:**
- `app/src/main/java/edu/feutech/redu/prompt/PromptPresenter.kt:29`
- `app/src/main/java/edu/feutech/redu/capture/ReduAccessibilityService.kt:447`
- `app/src/main/java/edu/feutech/redu/capture/ReduAccessibilityService.kt:549` only dismisses prompts during explicit active-session discard, not service teardown

**Recommended fix:**
- Call `PromptPresenter.dismissActivePrompt()` in `ReduAccessibilityService.onDestroy()` before native/model cleanup.
- Prefer a lifecycle-owned presenter over a singleton storing view references.

### P1 - Native VLM Failures Can Crash Monitoring Instead Of Degrading

**Impact:** A JNI load/init/inference failure can escape into the accessibility service and interrupt monitoring.

**Validation:** Code review. The Kotlin resolver only catches cancellation, not native load/init/inference exceptions or `UnsatisfiedLinkError`/`OutOfMemoryError`.

**Evidence:**
- `app/src/main/java/edu/feutech/redu/vlm/MoondreamLlamaNative.kt:5`
- `app/src/main/java/edu/feutech/redu/sentiment/NativeVisualSentimentResolver.kt:23`
- `app/src/main/java/edu/feutech/redu/capture/ReduAccessibilityService.kt:831`

**Recommended fix:**
- Catch native failures around model validation, `initModels`, and `inferenceImage`.
- Return `VisualSentimentLabel.UNRESOLVED` and log a reliability event instead of letting the service crash.

### P2 - Service Teardown Can Block On Native Cleanup

**Impact:** `onDestroy()` can block the service thread while finalizing sessions and closing native VLM models.

**Validation:** Code review.

**Evidence:**
- `app/src/main/java/edu/feutech/redu/capture/ReduAccessibilityService.kt:457`
- `app/src/main/java/edu/feutech/redu/sentiment/NativeVisualSentimentResolver.kt:35`

**Recommended fix:**
- Avoid long `runBlocking` work in service teardown.
- Add bounded timeout and non-blocking cleanup for native model close.

### P2 - Model Download Polling Leaks Cursors And Has Brittle Completion Parsing

**Impact:** Download progress polling can leak cursors; download completion can throw if expected columns or local URI are absent.

**Validation:** Code review.

**Evidence:**
- `app/src/main/java/edu/feutech/redu/vlm/ModelDownloadManager.kt:312` does not close a non-null cursor when `moveToFirst()` is false
- `app/src/main/java/edu/feutech/redu/vlm/ModelDownloadManager.kt:243` reads status without index validation
- `app/src/main/java/edu/feutech/redu/vlm/ModelDownloadManager.kt:260` force unwraps parsed local path

**Recommended fix:**
- Use `cursor?.use { ... }` everywhere.
- Validate column indexes and local URI.
- Convert invalid completion metadata into `ModelState.Error(...)`.

### P2 - Setup Can Complete With No Platforms Enabled

**Impact:** The app can show dashboard/history/export as ready even though the service will record no monitored sessions.

**Validation:** Code review.

**Evidence:**
- `app/src/main/java/edu/feutech/redu/ui/ReduAppScreen.kt:112` defines setup completion as participant code plus accessibility only
- `app/src/main/java/edu/feutech/redu/data/Entities.kt:64` platform toggles default false
- `app/src/main/java/edu/feutech/redu/ui/ReduAppScreen.kt:384` unlocks main destinations based on `setupComplete`

**Recommended fix:**
- Include `anyPlatformEnabled` in `setupComplete`.
- Keep the user in setup until at least one platform is selected.

### P2 - Export ZIP Reuses One Filename

**Impact:** Repeated exports can overwrite `redu-export.zip` while a share target may still be reading it.

**Validation:** Code review.

**Evidence:**
- `app/src/main/java/edu/feutech/redu/export/CsvExporter.kt:48`
- `app/src/main/java/edu/feutech/redu/ui/ReduAppScreen.kt:246`

**Recommended fix:**
- Use timestamped/study-code ZIP filenames, e.g. `redu-export-P001-20260610-1510.zip`.

### P2 - Export Omits Raw Sentiment Audit Counters

**Impact:** Exported `sessions.csv` includes NSD and OOV, but not the raw `resolvableUnits` and `negativeUnits` needed to audit NSD.

**Validation:** Code review.

**Evidence:**
- Stored fields: `app/src/main/java/edu/feutech/redu/data/Entities.kt:93`
- Export header: `app/src/main/java/edu/feutech/redu/export/CsvExporter.kt:75`

**Recommended fix:**
- Add `resolvable_units` and `negative_units` columns to `sessions.csv`.

### P3 - Daily Summaries Depend On Export-Time Timezone

**Impact:** A participant changing timezone before export can shift sessions into different daily summaries.

**Validation:** Code review.

**Evidence:**
- `app/src/main/java/edu/feutech/redu/export/CsvExporter.kt:180`

**Recommended fix:**
- Store capture timezone/local date on session creation, or use one fixed study timezone for summaries.

### P3 - ARM64-Only ABI Limits Emulator/ChromeOS Coverage

**Impact:** The app packages only `arm64-v8a`, which limits x86/x86_64 emulator and ChromeOS testing.

**Validation:** Lint reports `ChromeOsAbiSupport`.

**Evidence:**
- `app/build.gradle.kts:20`

**Recommended fix:**
- Keep ARM64 only if field devices are strictly ARM64.
- Otherwise add x86_64 native support or document ARM64-only testing requirements.

## Validation Gaps

Live emulator/UI validation could not be completed in this environment:

- `adb devices` found no connected devices.
- `emulator` command is not available on PATH.
- `:app:connectedDebugAndroidTest` failed with `No connected devices!`.

This means accessibility-service runtime behavior, actual overlay dismissal, DownloadManager broadcasts, and real social-app extraction flows still need device validation.

## Non-Issues Checked

- Debug build currently assembles successfully.
- JVM unit tests pass.
- Lint task completes successfully, though it reports warnings.
- The two configured Hugging Face model URLs are reachable and their linked sizes/hashes match the hardcoded metadata.

## Fix Status (2026-06-10)

| Bug | Status | Fix |
| --- | --- | --- |
| P0 Release build blocked | Fixed | `app/src/main/cpp/CMakeLists.txt` now defines `LLAMA_INSTALL_VERSION` before adding `tools/mtmd`; the vendored `set_target_properties` call had been expanding an undefined variable. `:app:assembleRelease` builds. |
| P1 API 33 receiver flags | Fixed | All three service receivers register via `ContextCompat.registerReceiver(..., RECEIVER_NOT_EXPORTED)`; lint `InlinedApi` warnings are gone. |
| P1 Risk not sustained | Fixed | `PromptPolicy` requires prompt-eligible risk to persist 60 s (`DEFAULT_SUSTAINED_RISK_MILLIS`) within the same session before showing; dropping to SAFE or a session change resets the window. |
| P1 NSD false-trigger | Fixed | NSD only feeds the live prompt risk evaluation once `resolvableUnits >= PromptPolicy.MIN_NSD_EVIDENCE_UNITS` (5); below that, prompts use the dwell+duration fallback rules. Stored session NSD is unchanged for analysis. |
| P1 Prompt easy to disregard / no adaptation | Fixed | Prompt outcomes feed back into `PromptPolicy`: two consecutive disregards (CONTINUE/DISMISSED) escalate L1 to L2; taking a break resets the streak. L2 "Continue scrolling" unlocks only after 3 s. Duration-only WARNING prompts are now L1 (awareness copy) and L2 requires negative-content evidence, so text matches trigger. Trigger reason (`DURATION_DWELL` / `NEGATIVE_CONTENT`) is persisted on prompt events (DB v6) and exported as `trigger_reason`. |
| P1 Overlay leak on teardown | Fixed | `onDestroy()` calls `PromptPresenter.dismissActivePrompt()` before cleanup. (Presenter remains a singleton; the lint `StaticFieldLeak` warning persists by design.) |
| P1 Native VLM crash | Fixed | `NativeVisualSentimentResolver` catches native load/init/inference failures (`UnsatisfiedLinkError`, OOM, JNI exceptions) and returns `UNRESOLVED`; the service already logs a `VLM_UNRESOLVED` reliability event for that label. |
| P2 Blocking teardown | Fixed | DB finalization in `onDestroy()` is bounded by a 5 s timeout; native model close runs on a separate thread. |
| P2 Download cursor leaks / brittle parsing | Fixed | All `DownloadManager` cursors use `use {}`; completion handling validates column indexes and local URI and converts bad metadata into `ModelState.Error`. |
| P2 Setup without platforms | Fixed | `setupComplete` now also requires at least one platform toggle to be enabled. |
| P2 Export ZIP filename reuse | Fixed | Exports are named `redu-export-<code>-<yyyyMMdd-HHmm>.zip` (study timezone), so repeated exports never overwrite a shared file. |
| P2 Export missing audit counters | Fixed | `sessions.csv` now includes `resolvable_units` and `negative_units`. |
| P3 Export-time timezone drift | Fixed | Daily summaries (and export timestamps) use the fixed study timezone `Asia/Manila`. |
| P3 ARM64-only ABI | Documented | Intentional for the field study; documented at the `abiFilters` declaration in `app/build.gradle.kts`. Instrumented tests require an ARM64 device/emulator image. |

Remaining: live device validation (accessibility runtime behavior, overlay dismissal, DownloadManager broadcasts, social-app extraction) still needs a connected ARM64 device, and the new 5→6 migration test should be run on-device.

## Second-Pass Findings (2026-06-10) — Fixed / Documented

Found in a follow-up review after the fixes above. Code review only; the P1 needs device confirmation.

### Second-Pass Fix Status (2026-06-12)

| Bug | Status | Fix |
| --- | --- | --- |
| P1 Release app-exit detection | Fixed, needs device confirmation | Release and debug now both leave `AccessibilityServiceInfo.packageNames = null`, preserving non-target package events for app-exit detection while existing in-code platform filtering handles monitored apps. |
| P2 First VLM inference timeout | Fixed | `VisualSentimentResolver.warmUp()` loads native models before screenshot/inference timeout accounting; the native hot path now validates model size without re-hashing ~1.8 GB every service start. |
| P2 Per-item capture state thread-safety | Fixed | Shared per-item/VLM state is volatile where cross-thread reads remain, and compound reset paths now run on the tracker dispatcher. |
| P2 Export lockout when monitoring is off | Fixed | Incomplete setup with existing sessions keeps History, Export, and Settings reachable; fresh installs still show Setup only. |
| P2 Accessibility-enabled check | Fixed | Enabled services are parsed as `ComponentName`s, so full and short flattened component forms both match REDU's service. |
| P2 VLM model provenance | Documented | `ModelDownloadManager.MODEL_FILES` now documents the third-party Q4_K_M text quant, ggml-org 20250414 projector pairing, SHA-256 pinning, and on-device validation rationale. |
| P3 Export rotation brick | Fixed | `isExporting` is no longer saveable, so recreation drops stale in-flight export state. |
| P3 Premature personalization lock | Fixed | Saved default-prior locks with no personalized quantile bounds are recomputed when enough baseline sessions later exist; locks with real bounds remain immutable. |
| P3 Main-thread model disk I/O | Fixed | `ModelDownloadManager` starts as `NotDownloaded` and performs initial file checks asynchronously under the IO manager scope. |
| P3 Missing `typeViewLongClicked` | Fixed | The accessibility-service event mask now includes long-click events for dwell extension. |
| P3 Unused `peakRiskLevel` | Fixed | The dashboard now shows "Today's peak pattern" from `summary.peakRiskLevel`. |
| P3 Breathing phase timing | Fixed | The cycle animator is linear for wall-clock phase boundaries, with easing applied inside inhale/exhale phases only. |
| P3 JNI null-check gap | Fixed | `GetByteArrayElements` failure now logs and returns `UNRESOLVED` instead of crashing. |
| P3 Export staging leftovers | Fixed | CSV staging directories are deleted on failed export, and old `redu-export-*.zip` cache files are pruned after 24 h. |

Remaining device validation: release app-exit detection, VLM warm-up timing, and long-click dwell extension still need confirmation on a connected ARM64 Android device.

### P1 - Release Builds Cannot Detect Leaving The App, Inflating Session Durations

**Impact:** In release builds a session keeps accruing foreground time after the user presses Home or switches to an unmonitored app, until screen-off or return. Duration metrics, risk scores, and prompt eligibility are inflated. Debug builds mask the bug entirely, so all testing to date looked fine.

**Evidence:**
- `app/src/main/java/edu/feutech/redu/capture/ReduAccessibilityService.kt:200` sets `packageNames` to only the monitored packages in release (`null` in debug).
- `app/src/main/java/edu/feutech/redu/capture/ReduAccessibilityService.kt:222` (`handleNonTargetEvent`) is the only app-exit path, and it requires events from *non-target* packages — which the release filter blocks.

**Recommended fix:** Keep `packageNames = null` in release and filter by package in code (debug already effectively does this), or detect exit another way (e.g. windows-changed signals).

### P2 - First VLM Inference After Every Service Start Nearly Always Times Out

**Impact:** The first no-text item after a service start is lost as `vlm_inference_timeout`, and the device re-hashes ~1.8 GB of model files on every cold VLM init.

**Evidence:**
- `app/src/main/java/edu/feutech/redu/sentiment/NativeVisualSentimentResolver.kt:89` runs `validateModels()` with full SHA-256 hashing, plus a ~1.7 GB model load, inside
- the 25 s per-item timeout at `app/src/main/java/edu/feutech/redu/capture/ReduAccessibilityService.kt:67` / `:871`. Init is not cancellable, so it completes anyway and later requests work — but the triggering item is always lost.

**Recommended fix:** Warm up models off the request path, validate by size only on the hot path (full hash already runs after download), or exclude init time from the inference timeout.

### P2 - Per-Item Capture State Is Mutated From Four Threads Without Synchronization

**Impact:** Intermittent stale VLM captures applied to the wrong item, missed cancellations, and fingerprint state bleeding across sessions.

**Evidence:** `lastObservedTransitionFingerprint`, `blankNoTextSequence`, `currentVlmRequest`, `vlmCaptureJob`, and `targetInForeground` are written from the tracker dispatcher (`updateTrackerForScreen`), the main thread (`discardActiveSession`, `cancelPendingVlm`), IO threads (`resetPerItemState`, `observeSettings`), and Default threads (`scheduleVlmCapture`) with no `@Volatile`/locking. `resetPerItemStateLocked` (`ReduAccessibilityService.kt:1066`) runs off the tracker thread despite its name.

**Recommended fix:** Confine all per-item state to the tracker dispatcher (route every mutation through `withContext(trackerDispatcher)`), or guard it with a single lock.

### P2 - Participants Cannot Export Data Once Monitoring Is Off

**Impact:** When setup is "incomplete" (accessibility off — and, after the platform-gating fix above, all platform toggles off) the bottom nav collapses to Setup only, so a participant who disables monitoring at the end of the study cannot reach Export, History, or Settings despite having data.

**Evidence:** `app/src/main/java/edu/feutech/redu/ui/ReduAppScreen.kt` (`availableDestinationsFor`).

**Recommended fix:** Keep Export (and Settings) reachable whenever any sessions exist, regardless of setup state.

### P2 - Accessibility-Enabled Check Is String-Fragile

**Impact:** `ENABLED_ACCESSIBILITY_SERVICES` can contain the short component form (`edu.feutech.redu/.capture.ReduAccessibilityService`) when enabled via adb or some OEM restore flows; the manual string comparison then fails and the app stays stuck on Setup while the service is running.

**Evidence:** `app/src/main/java/edu/feutech/redu/MainActivity.kt:26`.

**Recommended fix:** Use `AccessibilityManager.getEnabledAccessibilityServiceList`, or compare via `ComponentName.unflattenFromString`.

### P2 - VLM Text Model And Projector Come From Unrelated Sources

**Impact:** The text model downloads from a third-party repo (`salivosa/moondream2-gguf`) while the vision projector comes from ggml-org's 2025-04-14 build. Pinned SHA-256 guarantees integrity but not that the pair is from the same moondream2 revision; a mismatched pair degrades VLM labels silently (research-validity risk).

**Evidence:** `app/src/main/java/edu/feutech/redu/vlm/ModelDownloadManager.kt:57`.

**Recommended fix:** Verify the pairing once and document it, or source both files from one release.

### P3 - Minor Issues

- **Rotation during export bricks the Export button:** `isExporting` is `rememberSaveable` (`ReduAppScreen.kt:104`); on configuration change the state saves as `true` before the export coroutine's `finally` runs, and the restored composition has no running job — the button stays disabled until process death.
- **Premature personalization lock is permanent:** enabling Week-2 prompts even briefly during Week 1 saves a lock (possibly from 0 baseline sessions); `lockForWeek2` always returns the existing row afterwards (`RiskPersonalization.kt:19`). Only a full study-data reset clears it.
- **Main-thread disk I/O at startup:** `ModelDownloadManager`'s constructor checks file sizes and runs `mkdirs` synchronously (`ModelDownloadManager.kt:78`) and is first touched from the UI.
- **`typeViewLongClicked` missing from `redu_accessibility_service.xml`** even though `isUserInteractionEvent()` treats long-clicks as dwell-extending interactions — long-presses never extend the 45 s idle window.
- **`dashboardSummary.peakRiskLevel` is computed but never displayed** (`SessionUiModels.kt`); the "Current pattern level" card shows the latest session's level instead.
- **Breathing circle phase timing is distorted:** `AccelerateDecelerateInterpolator` spans the whole 11 s cycle (`BreathingCircleView.kt:77`), so the 4-3-4 inhale/hold/exhale boundaries don't match wall-clock seconds.
- **JNI null-check gap:** `inferenceImage` doesn't check `GetByteArrayElements` for null (`app/src/main/cpp/redu_llama_jni.cpp:213`); under memory pressure that's a native crash instead of an `UNRESOLVED` result.
- **Export staging leftovers:** if `exportAll()` throws after creating its staging dir, the dir leaks in cache; timestamped zips are never pruned (cache-managed, low impact).
