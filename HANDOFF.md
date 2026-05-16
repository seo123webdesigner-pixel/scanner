# Snapdoc — Session Handoff

**Paste this entire file at the start of the next Claude Code session.**

Last updated: 2026-05-16.
Branch: `claude/setup-android-clean-architecture-cNGeQ`.
Repo: `seo123webdesigner-pixel/scanner`.

---

## 0. Read first

You are continuing work on **Snapdoc**, a privacy-first AI document scanner for Android. The previous session scaffolded the entire v1 project end-to-end. The user is a non-developer founder; communicate plainly, no jargon dumps, no half-finished refactors.

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
    └── SnapdocNavGraph.kt       — NavHost + bottom nav overlay
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
- Firebase Analytics + Crashlytics dependencies (plugins commented out until `google-services.json` is dropped in).
- Splash → Onboarding (first launch) or Home (subsequent).
- Scan → save → OCR → auto-categorize → persist pipeline.
- Document detail: PDF page list, AI Summary bottom sheet, share, favorite, delete.
- Search: filename + OCR full-text via SQL JOIN.
- Settings, Manage Categories (add/delete custom), Paywall, About.

### Wired infra but not yet mounted on a screen
- **Banner ad on Home.** Composable `core/ads/BannerAd.kt` exists; add it to `HomeScreen`'s `Scaffold(bottomBar = ...)` to mount.
- **Interstitial after save.** `AdsManager.maybeShowInterstitial(activity)` exists; call it from a `LaunchedEffect(state.saved)` in `ScannerFlowScreen` or `SaveDocumentViewModel` (needs an Activity reference, pass via `LocalContext.current as Activity`).
- **Rewarded gate before AI Summary.** `AdsManager.showRewarded(activity)` exists; gate `DocumentDetailViewModel.requestSummary` on it for non-owners.
- **Multi-select toolbar UI.** `HomeViewModel` tracks `selectedIds` and exposes `toggleSelection` / `deleteSelected`; the long-press → toolbar UI is not yet rendered in `HomeScreen`.

### Deferred entirely (with reason)
- **Password-protected PDF export.** iText 7 is AGPL → incompatible with Play distribution under standard license terms. Spec §6.5 explicitly said NOT to silently include AGPL code. Options for v2: PDFBox-Android (Apache 2.0) or `android.graphics.pdf.PdfDocument` + Bouncy Castle for the AES dict.
- **Manual Crop / Filter / Multi-Page Review screens** (spec screens 10, 11, 12). ML Kit Document Scanner's `SCANNER_MODE_FULL` provides its own in-flow UI for these. Separate screens were not implemented because the system overlay covers the same UX with less code.
- **Native ad cards every 7th item.** AdMob native unit ID is in BuildConfig but the in-list render is a follow-up.
- **Detailed Export Options dialog** (spec screen 20: quality / page size). Document detail currently shares the saved PDF as-is via system Send intent.
- **WhatsApp quick-share button.** Already in the system share sheet; a dedicated button is a one-line add to Document Detail when wanted.
- **Component Gallery debug screen.** Nice-to-have, not required for ship.
- **Per-category drill-down screen.** Tapping a category card in `CategoriesScreen` currently does nothing — TODO to navigate to a filtered list.
- **Geist fonts.** Not bundled (binary files). System sans-serif is in use. Drop `Geist-Regular.ttf`, `Geist-Medium.ttf`, `Geist-SemiBold.ttf`, `GeistMono-Regular.ttf`, `GeistMono-Medium.ttf` into `app/src/main/res/font/` and update `core/ui/theme/Type.kt` to reference them.
- **Tamil, Telugu, Kannada, Malayalam, Bengali, Gujarati OCR.** Add the matching ML Kit recognizer artifacts (each script is a separate Gradle dep) and append to the `recognizers` list in `OcrEngine.kt`.

---

## 4. Build fixes already applied — do not undo

These were live-debugged with the user during the previous session. They are deliberate, not artifacts.

1. **`com.google.mlkit:text-recognition` version is `16.0.1`.** I originally set it to `19.0.1` — that version number exists only on the unbundled `play-services-mlkit-text-recognition` artifact, not on the bundled `com.google.mlkit:text-recognition` artifact. Don't change it back.

2. **Manifest has `<property android:name="android.adservices.AD_SERVICES_CONFIG" ... tools:replace="android:resource" />`.** AdMob and Firebase Analytics both declare this property pointing at different XML files. Tools-replace resolves the conflict in AdMob's favor (its config is the superset).

3. **XML themes extend `android:Theme.Material.*`, not `Theme.Material3.*`.** Compose Material 3 ships no XML styles. The XML theme is only used for the splash window, and the platform `android:Theme.Material.Light.NoActionBar` / `android:Theme.Material.NoActionBar` work without adding the Material Components dependency.

4. **`DispatcherModule` `@Provides` functions are named `provideIo`, `provideDefault`, `provideMain`.** Originally `io`, `default`, `main` — `default` is a Java reserved word and JavaPoet rejects it when Dagger generates the factory class. All three were renamed for consistency.

5. **OCR aggregation in `SaveDocumentViewModel` uses a `for` loop, not `joinToString { suspend }`.** `joinToString`'s transform lambda is not inline, so it cannot host suspend calls. Same applies to `map`, `filter`, `forEach`. If you need to apply a suspend function across a collection, use an explicit for-loop or `flow { ... }`.

6. **Debug build no longer applies `applicationIdSuffix = ".debug"`.** Both build types install as `com.snapdoc.app` so a single Firebase app registration covers both. If the user later wants side-by-side debug/release installs, add the suffix back AND register the `.debug` package in Firebase.

---

## 5. External setup state (from the user)

- **Firebase project:** User is creating one under package `com.snapdoc.app`. They have not yet downloaded `google-services.json` as of the last message. Once they do:
  1. Drop file into `app/google-services.json`
  2. Uncomment these lines in `app/build.gradle.kts`:
     ```kotlin
     alias(libs.plugins.google.services)
     alias(libs.plugins.firebase.crashlytics)
     ```
  3. Re-sync Gradle
- **Gemini API key:** User was directed to <https://aistudio.google.com/apikey>. Goes into `local.properties` as `GEMINI_API_KEY`. Without it, AI Summary and auto-categorization fail gracefully (category falls back to "Other", AI Summary shows a network error).
- **AdMob IDs:** Debug builds use Google's official test IDs (hardcoded in `app/build.gradle.kts` as `AdMobTestIds.*`). Release reads from `local.properties` and falls back to test IDs with a warning log if blank.
- **Signing keystore:** Not created yet. Release config only signs when `RELEASE_KEYSTORE_PATH` is set in `local.properties`.

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
- Do **not** create a PR unless the user explicitly asks.
- Do **not** push to `main`.

---

## 8. Where to pick up

The user has Android Studio open and is iterating on `./gradlew assembleDebug`. The current pending items, in priority order if they ask "what next":

1. **Three small ads wirings** (banner mount in Home, interstitial after save, rewarded before summary). User already said the infra is there and asked about it explicitly; offered to wire in one pass but the offer is open.
2. **Per-category drill-down screen.** Tapping a category in `CategoriesScreen` currently does nothing.
3. **Multi-select toolbar on Home.** VM state is wired, just needs the long-press → toolbar UI.
4. **Export Options dialog** on Document Detail.
5. **Native ad in Home `LazyColumn` every 7 items.**
6. **Component Gallery debug screen** for visual QA against the design system mockups.

Do **not** start a deep refactor or rename without checking with the user first. The codebase is intentionally small, idiomatic, and matches the spec's "no half-finished implementations" rule.

---

## 9. Things to remember about this user

- Non-developer founder building this for the first time. Explain commands and concepts when they matter; skip jargon dumps.
- Wants the app shipped to Play Store. Treat every architectural decision through that lens.
- Spec authority order is product spec > screen specs > design system > hero mockups. If two files conflict, follow that order.
- Privacy contract is the brand — visible on onboarding and in the Play Store listing. Any change that risks exposing user document data must be flagged loudly.
- The "Geist font not bundled" and "AGPL password-protect blocker" are KNOWN deferrals the user agreed to. Don't surface them as new issues.

---

## 10. Sanity-check the next session

After pasting this handoff, you should be able to answer without re-reading the code:
- What's the privacy contract and where is it enforced? (`core/network/GeminiClient.kt`, OCR text only)
- What's the IAP product ID? (`snapdoc_remove_ads`)
- What were the five build fixes already applied? (see §4)
- Which screens are stubbed vs functional? (see §3)
- Where do I commit? (branch `claude/setup-android-clean-architecture-cNGeQ`, no PR)

If you can't answer those, re-read this file before touching code.
