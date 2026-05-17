# Snapdoc — Session Handoff

**Paste this entire file at the start of the next Claude Code session.**

Last updated: 2026-05-16 (post ad-wiring pass).
Branch: `claude/setup-android-clean-architecture-cNGeQ`.
Repo: `seo123webdesigner-pixel/scanner`.

---

## 0. Read first

You are continuing work on **Snapdoc**, a privacy-first AI document scanner for Android. The previous session scaffolded the v1 project end-to-end and wired all three ad surfaces with test IDs. The user is a non-developer founder; communicate plainly, no jargon dumps, no half-finished refactors.

The source of truth for product behavior is the four input files in the repo root, in this order of authority:
1. `01_product_spec.md` — what the app does, all 26 screens, locked decisions
2. `03_screen_specs.md` — per-screen layout and behavior
3. `02_design_system.md` — color/type/spacing/shape/motion tokens
4. `04_hero_mockups.html` — visual mockups for 8 hero screens

`README.md` documents how to run the app. `BUILD_LOG.md` documents what each phase shipped and what was deferred.

---

## 1. Locked product decisions

- **App name:** Snapdoc.
- **Package / applicationId:** `com.snapdoc.app` (debug and release — the `.debug` suffix was deliberately removed so a single Firebase app registration covers both).
- **Min SDK:** API 26. **Target SDK:** API 35.
- **Tech stack (do not substitute without asking):** Kotlin 2.1, Jetpack Compose, Material 3, Hilt, Room, DataStore Preferences, Coroutines + Flow, OkHttp, kotlinx.serialization, Coil, Timber, ML Kit Document Scanner, ML Kit Text Recognition v2, Gemini Flash via REST, AdMob, Play Billing v7, Firebase Analytics + Crashlytics.
- **Monetization:** free + ads, one-time IAP "Remove Ads" at ₹99 / $1.49, product ID `snapdoc_remove_ads`.
- **Privacy contract — non-negotiable:** Document images **never leave the device**. Only extracted OCR text is sent to Gemini, and only when the user explicitly invokes AI Summary or auto-categorization. This is enforced at one chokepoint, `core/network/GeminiClient.kt` — every public method on that class takes a `String` (OCR text). Do not add an overload that takes a Bitmap, Uri, byte[], or filename without explicit user sign-off.

---

## 2. Architecture

Clean-architecture-inspired layering, single Activity, Compose Navigation.

```
app/src/main/java/com/snapdoc/app/
├── SnapdocApplication.kt        — Hilt entry; initializes AdMob + Billing on startup
├── MainActivity.kt              — single Activity, sets Compose content
├── CrashlyticsTree.kt           — Timber tree that forwards WARN+ to Crashlytics
├── di/
│   ├── DatabaseModule.kt        — provides Room DB + DAOs
│   ├── RepositoryModule.kt      — @Binds for the four repositories
│   └── DispatcherModule.kt      — qualified dispatchers (IoDispatcher etc.)
├── core/
│   ├── ui/theme/                — Color, Type, Shape, Spacing, Motion + SnapdocTheme accessor
│   ├── ui/components/           — Buttons, Cards, TextFields, Bars, States, Feedback, Chips
│   ├── data/
│   │   ├── db/                  — Entities, Daos, SnapdocDatabase
│   │   ├── repository/          — Document/Ocr/Summary/Category repos
│   │   ├── model/               — Document, Page, AiSummary, BuiltInCategory
│   │   └── Mappers.kt           — entity ↔ domain model
│   ├── network/GeminiClient.kt  — REST client with redacting OkHttp interceptor
│   ├── ml/OcrEngine.kt          — ML Kit Text Recognition wrapper (Latin + Devanagari)
│   ├── ads/
│   │   ├── AdsManager.kt        — initializes AdMob, preloads interstitial + rewarded
│   │   └── BannerAd.kt          — composable banner; short-circuits when owned
│   ├── billing/BillingManager.kt — Play Billing v7 wrapper
│   ├── storage/
│   │   ├── FileStorage.kt       — scoped-storage paths + FileProvider URIs + sanitizer
│   │   └── UserPreferences.kt   — DataStore-backed flags
│   └── util/Format.kt           — file size, timestamp, metadata formatters
├── feature/
│   ├── onboarding/              — SplashScreen, OnboardingScreen (3-page pager)
│   ├── home/                    — HomeScreen + HomeViewModel + category color helper
│   ├── scanner/                 — ScannerLauncher, ScannerFlowScreen, SaveDocumentViewModel
│   ├── document/                — DocumentDetailScreen + ViewModel (AI summary sheet inline)
│   ├── categories/              — CategoriesScreen (2-col grid)
│   ├── search/                  — SearchScreen with 180ms debounce, joins ocr_text
│   ├── settings/                — SettingsScreen + ManageCategoriesScreen + ViewModels
│   ├── paywall/                 — PaywallScreen + ViewModel reads BillingManager
│   └── about/                   — AboutScreen (static)
└── navigation/
    ├── SnapdocRoute.kt          — sealed route hierarchy with arg helpers
    └── SnapdocNavGraph.kt       — outer Scaffold owns the bottom nav, NavHost inside
```

Tests live in `app/src/test/java/...` (unit) and `app/src/androidTest/java/...` (instrumented). One unit test for `DocumentRepository` exists as a pattern example.

---

## 3. What is wired vs what is stubbed

### Fully wired and functional
- Compose theme system (all design system tokens, light + dark).
- Component library (buttons, cards, text fields, bars, states, feedback).
- Room with cascade-deleting Document → Pages / OcrText / AiSummary / Categories.
- DataStore preferences (onboarding flag, auto-OCR, AI suggestion, save count, ads owned).
- ML Kit Document Scanner integration via `ActivityResultContracts.StartIntentSenderForResult`.
- ML Kit Text Recognition v2 (Latin + Devanagari run in parallel; longest result wins).
- Gemini REST client with retry (3 attempts, 1s/2s/4s) and a redacting OkHttp interceptor.
- AdMob initialization, interstitial preload, rewarded preload, banner composable.
- Play Billing v7 connection, product query, purchase launch, restore, acknowledgement.
- Firebase Analytics + Crashlytics (plugins enabled; user has dropped `google-services.json` in `app/`).
- **Banner ad on Home** — mounted as `HomeScreen`'s `Scaffold(bottomBar)`. Renders only for non-owners.
- **Interstitial after save** — `ScannerFlowScreen` calls `viewModel.maybeShowInterstitial(activity)` after each save, before navigating to Document Detail. Throttled to every 3rd save + 60s cooldown.
- **Rewarded gate before AI Summary** — `DocumentDetailViewModel.requestSummary(activity)` calls `AdsManager.showRewarded` first; only runs the Gemini call if the user earned the reward (or owns Remove Ads, in which case `showRewarded` short-circuits and returns `true`).
- Splash → Onboarding (first launch) or Home (subsequent).
- Scan → save → OCR → auto-categorize → persist pipeline.
- Document detail: PDF page list, AI Summary bottom sheet, share, favorite, delete.
- Search: filename + OCR full-text via SQL JOIN.
- Settings, Manage Categories (add/delete custom), Paywall, About.

### Wired infra but not yet mounted on a screen
- **Multi-select toolbar UI.** `HomeViewModel` tracks `selectedIds` and exposes `toggleSelection` / `deleteSelected`; the long-press → toolbar UI is not yet rendered in `HomeScreen`.
- **Native ad cards every 7th item.** AdMob native unit ID is in BuildConfig but the in-list render is not done.
- **Per-category drill-down.** Tapping a category card in `CategoriesScreen` currently does nothing — needs a filtered list screen + route.

### Deferred entirely (with reason)
- **Password-protected PDF export.** iText 7 is AGPL → incompatible with Play distribution under standard license terms. Spec §6.5 explicitly said NOT to silently include AGPL code. Options for v2: PDFBox-Android (Apache 2.0) or `android.graphics.pdf.PdfDocument` + Bouncy Castle for the AES dict.
- **Manual Crop / Filter / Multi-Page Review screens** (spec screens 10, 11, 12). ML Kit Document Scanner's `SCANNER_MODE_FULL` provides its own in-flow UI for these. Separate screens were not implemented because the system overlay covers the same UX with less code.
- **Detailed Export Options dialog** (spec screen 20: quality / page size). Document detail currently shares the saved PDF as-is via system Send intent.
- **WhatsApp quick-share button.** Already in the system share sheet; a dedicated button is a one-line add to Document Detail when wanted.
- **Component Gallery debug screen.** Nice-to-have, not required for ship.
- **Geist fonts.** Not bundled (binary files). System sans-serif is in use. Drop `Geist-Regular.ttf`, `Geist-Medium.ttf`, `Geist-SemiBold.ttf`, `GeistMono-Regular.ttf`, `GeistMono-Medium.ttf` into `app/src/main/res/font/` and update `core/ui/theme/Type.kt` to reference them.
- **Tamil, Telugu, Kannada, Malayalam, Bengali, Gujarati OCR.** Add the matching ML Kit recognizer artifacts (each script is a separate Gradle dep) and append to the `recognizers` list in `OcrEngine.kt`.
- **Explicit Camera permission prompt.** ML Kit Document Scanner currently handles its own camera permission. Spec wants it upfront; minor follow-up.

---

## 4. Build fixes already applied — do not undo

These were live-debugged with the user during the previous session. They are deliberate, not artifacts.

1. **`com.google.mlkit:text-recognition` version is `16.0.1`.** I originally set it to `19.0.1` — that version number exists only on the unbundled `play-services-mlkit-text-recognition` artifact, not on the bundled `com.google.mlkit:text-recognition` artifact. Don't change it back.

2. **Manifest has `<property android:name="android.adservices.AD_SERVICES_CONFIG" ... tools:replace="android:resource" />`.** AdMob and Firebase Analytics both declare this property pointing at different XML files. Tools-replace resolves the conflict in AdMob's favor (its config is the superset).

3. **XML themes extend `android:Theme.Material.*`, not `Theme.Material3.*`.** Compose Material 3 ships no XML styles. The XML theme is only used for the splash window, and the platform `android:Theme.Material.Light.NoActionBar` / `android:Theme.Material.NoActionBar` work without adding the Material Components dependency.

4. **`DispatcherModule` `@Provides` functions are named `provideIo`, `provideDefault`, `provideMain`.** Originally `io`, `default`, `main` — `default` is a Java reserved word and JavaPoet rejects it when Dagger generates the factory class. All three were renamed for consistency.

5. **OCR aggregation in `SaveDocumentViewModel` uses a `for` loop, not `joinToString { suspend }`.** `joinToString`'s transform lambda is not inline, so it cannot host suspend calls. Same applies to `map`, `filter`, `forEach`. If you need to apply a suspend function across a collection, use an explicit for-loop or `flow { ... }`.

6. **Debug build no longer applies `applicationIdSuffix = ".debug"`.** Both build types install as `com.snapdoc.app` so a single Firebase app registration covers both. If the user later wants side-by-side debug/release installs, add the suffix back AND register the `.debug` package in Firebase.

7. **`SnapdocNavGraph` wraps `NavHost` in an outer `Scaffold` whose `bottomBar` owns the bottom-nav slot.** This was a refactor from the original "overlay-on-top-of-NavHost" pattern. Necessary so per-screen Scaffolds can use their own `bottomBar` (specifically: Home's banner ad) without being covered by the bottom nav. Do not collapse this back into an overlay — banner placement breaks if you do.

8. **Activity-touching ads pass through ViewModel methods that take `Activity` as a parameter; ViewModels do not hold Activity refs.** `SaveDocumentViewModel.maybeShowInterstitial(activity)` and `DocumentDetailViewModel.requestSummary(activity)` are the patterns. Activity comes from `LocalContext.current as? Activity` in the composable.

---

## 5. External setup state (from the user)

- **Firebase project:** Created. Registered with package `com.snapdoc.app`. `google-services.json` is in `app/`. Plugins are enabled in `app/build.gradle.kts`.
- **Gemini API key:** Set in `local.properties` as `GEMINI_API_KEY`. AI Summary and auto-categorization are live.
- **AdMob IDs:** Debug builds use Google's official test IDs (hardcoded in `app/build.gradle.kts` as `AdMobTestIds.*`). User plans to swap in real IDs in `local.properties` in a later session.
- **Signing keystore:** Not yet created. Release config only signs when `RELEASE_KEYSTORE_PATH` is set.
- **`snapdoc_remove_ads` IAP product:** Not yet created in Play Console. Paywall will load with no live price until then; Buy button will fail. Not blocking until Play submission.

---

## 6. Recent build errors the user hit and how they were resolved

Listed so you can recognize repeats:

| Error | Cause | Fix |
|---|---|---|
| `Failed to resolve: com.google.mlkit:text-recognition:19.0.1` | Wrong version line | Set to `16.0.1` |
| `Manifest merger failed... AD_SERVICES_CONFIG` | AdMob + Firebase both declare it | `tools:replace="android:resource"` on the `<property>` |
| `Theme.Material3.DayNight.NoActionBar not found` | Material 3 has no XML styles | Use `android:Theme.Material.*` platform parents |
| `IllegalArgumentException: not a valid name: default` (KSP / Dagger) | `default` is a Java reserved word | Renamed all `DispatcherModule` provides functions |
| `Suspension functions can only be called within coroutine body` in `joinToString` | `joinToString`'s lambda isn't inline | Replaced with for-loop |

If the user reports a new compile error, check `BUILD_LOG.md` first for context, then fix at the smallest scope. Commit + push each fix individually with a clear "Fix X — Y" subject line.

---

## 7. Commit and branch protocol

- Always commit and push to branch `claude/setup-android-clean-architecture-cNGeQ`.
- The user has explicitly said: end-to-end execution, pause only on blockers. Don't ask for permission to make obvious fixes.
- Commit messages: one-line subject, why-not-what. Examples already in `git log`:
  - "Drop .debug applicationId suffix so a single Firebase app registration covers both build types"
  - "Replace joinToString { suspend } with a for-loop — joinToString's transform lambda isn't inline so it can't host suspend calls"
  - "Wire all three ad surfaces — banner on Home, interstitial after save, rewarded gate before AI Summary"
- Do **not** create a PR unless the user explicitly asks.
- Do **not** push to `main`.

---

## 8. Phase status & priority queue

### Per-phase status (vs. original 11-phase plan)

| Phase | Status |
|---|---|
| 0 Project setup | ✅ Complete |
| 1 Design system | 🟡 ~85% — Geist fonts + Component Gallery deferred |
| 2 Data layer | ✅ Complete |
| 3 Onboarding + permissions | 🟡 ~80% — Camera permission prompt deferred |
| 4 Core scanning flow | 🟡 ~75% — Crop/Filter/Multi-page deferred (ML Kit overlay covers it) |
| 5 OCR + search | ✅ Complete (English/Hindi shipping; other Indic scripts deferred) |
| 6 AI features | ✅ Complete |
| 7 Sharing + export | 🟡 ~50% — Export Options dialog, multi-select toolbar, password PDF deferred |
| 8 Settings + categories | 🟡 ~85% — per-category drill-down deferred |
| 9 Monetization | ✅ ~95% — banner/interstitial/rewarded all wired with test IDs; native ad in 7th list slot deferred |
| 10 Error states + polish | 🟡 ~50% — snackbar, haptics, motion polish, TalkBack pass deferred |
| 11 Release readiness | 🟡 ~60% — needs device verification, real keystore, real ad IDs, Play product creation |

Roughly **~85%** of v1 scope shipped end-to-end. What remains is UI polish + verification.

### What's left, ordered by priority

**Must-do before Play Store submission:**
1. Verify the full scan → save → OCR → summary loop on a real device.
2. Create signing keystore (`keytool` cmd in `local.properties.example` header), fill `RELEASE_KEYSTORE_*`, build a signed `.aab`.
3. Trigger a test crash in release, confirm it lands in Firebase Crashlytics.
4. Create `snapdoc_remove_ads` IAP in Play Console (managed product, one-time, ₹99 / $1.49) before testing the real billing flow.
5. Swap test ad IDs for real ones in `local.properties` before release.
6. Final accessibility pass — run through every screen with TalkBack once.

**Should-do before Play Store submission:**
7. Bundle Geist fonts (or accept system sans-serif as v1).
8. Explicit Camera permission prompt before Scanner entry.
9. Per-category drill-down screen.
10. Multi-select toolbar on Home (VM state is wired, just needs UI).

**Nice-to-have / v2 candidates:**
11. Native ad cards every 7th item in Home `LazyColumn`.
12. Export Options dialog (quality / page size).
13. Snackbar host + haptic feedback + motion polish.
14. Component Gallery debug screen.
15. Additional Indic OCR recognizers.
16. WhatsApp quick-share button.

**Known blockers — not pickable in v1:**
- Password-protected PDF (AGPL licensing). Defer to v2 via PDFBox-Android or custom AES.

---

## 9. Where to pick up

If the user opens the next session with no specific request, the most useful next moves in order are:

1. **Confirm everything builds and runs on their device.** If they hit a Logcat error or visual bug, that's job #1.
2. **Wire items 7–10 from the "Should-do" list above.** All small, all mechanical, no architectural decisions needed.

Do **not** start a deep refactor or rename without checking with the user first. The codebase is intentionally small, idiomatic, and matches the spec's "no half-finished implementations" rule.

---

## 10. Things to remember about this user

- Non-developer founder building this for the first time. Explain commands and concepts when they matter; skip jargon dumps.
- Wants the app shipped to Play Store. Treat every architectural decision through that lens.
- Spec authority order is product spec > screen specs > design system > hero mockups. If two files conflict, follow that order.
- Privacy contract is the brand — visible on onboarding and in the Play Store listing. Any change that risks exposing user document data must be flagged loudly.
- The "Geist font not bundled" and "AGPL password-protect blocker" are KNOWN deferrals the user agreed to. Don't surface them as new issues.
- Firebase is now active. Don't reintroduce the "uncomment the plugin lines" instruction.
- Gemini key is set. AI Summary + auto-categorization should "just work."
- All three ad surfaces are mounted and using test IDs. User will swap in real IDs in a future session.

---

## 11. Sanity-check the next session

After pasting this handoff, you should be able to answer without re-reading the code:
- What's the privacy contract and where is it enforced? (`core/network/GeminiClient.kt`, OCR text only)
- What's the IAP product ID? (`snapdoc_remove_ads`)
- What were the eight build fixes already applied? (see §4)
- Which screens are stubbed vs functional? (see §3)
- Where do I commit? (branch `claude/setup-android-clean-architecture-cNGeQ`, no PR)
- What's the user's external setup state? (see §5: Firebase live, Gemini key set, test ad IDs in debug)

If you can't answer those, re-read this file before touching code.
