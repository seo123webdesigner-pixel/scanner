# Snapdoc

Privacy-first AI document scanner for Android. Built per `01_product_spec.md`,
`02_design_system.md`, `03_screen_specs.md`, and `04_hero_mockups.html`.

## What's in v1

- **Stack:** Kotlin 2.1, Jetpack Compose, Material 3, Hilt, Room, DataStore,
  Coroutines + Flow, OkHttp + kotlinx.serialization, Coil, Timber.
- **Scanning:** Google ML Kit Document Scanner (system overlay handles capture,
  crop, multi-page review, and filter selection).
- **OCR:** Google ML Kit Text Recognition v2 (Latin + Devanagari shipping; other
  Indic scripts wire in as additional recognizer modules).
- **AI:** Google Gemini Flash via REST. **Only extracted OCR text is ever sent
  to Gemini — never the original image.** See `core/network/GeminiClient.kt`.
- **Ads:** Google AdMob — banner, native, interstitial (3rd-save / 60s cooldown),
  rewarded (gate before AI summary in free tier). Debug builds use Google's
  official test IDs; release builds read real IDs from `local.properties`.
- **IAP:** Google Play Billing v7 — one-time "Remove Ads" product
  (`snapdoc_remove_ads`) at ₹99 / $1.49.
- **Analytics & crashes:** Firebase Analytics + Crashlytics (wired, but plugin
  applications are commented out until you drop in `google-services.json`).

## Project structure

```
app/
├── di/                            # Hilt modules
├── core/
│   ├── ui/theme/                  # Color, Type, Shape, Spacing, Motion tokens
│   ├── ui/components/             # Buttons, Cards, TextFields, States, etc.
│   ├── data/                      # Entities, DAOs, repositories, mappers
│   ├── network/                   # Gemini client (REST + redacting logger)
│   ├── ml/                        # ML Kit OCR wrapper
│   ├── ads/                       # AdMob manager + Banner composable
│   ├── billing/                   # Play Billing wrapper
│   ├── storage/                   # FileStorage + UserPreferences (DataStore)
│   └── util/                      # Formatters
├── feature/
│   ├── onboarding/                # Splash + 3-screen onboarding
│   ├── home/                      # Home list + empty state
│   ├── scanner/                   # ML Kit scanner launcher + save flow
│   ├── document/                  # Document detail + AI summary bottom sheet
│   ├── categories/                # Categories grid
│   ├── search/                    # Full-text search across filenames + OCR
│   ├── settings/                  # Settings + Manage Categories
│   ├── paywall/                   # Remove Ads paywall
│   └── about/                     # About / Privacy
├── navigation/                    # NavGraph, routes
├── SnapdocApplication.kt
├── MainActivity.kt
└── CrashlyticsTree.kt
```

## First-time setup

1. Install **JDK 17** and **Android Studio** (Hedgehog or later).
2. Open the project. Let Gradle sync and download the SDK as needed.
3. Copy `local.properties.example` → `local.properties` and fill in:
    - `GEMINI_API_KEY` — get from <https://aistudio.google.com/apikey>
    - `ADMOB_*_ID` — optional; debug always uses Google's test IDs
    - `RELEASE_KEYSTORE_*` — only needed when building a signed release
4. **Firebase (optional for first run):** Drop `google-services.json` into
   `app/`, then uncomment the two `google-services` / `firebase-crashlytics`
   plugin lines at the top of `app/build.gradle.kts`.
5. Run on an emulator or device (API 26+).

## Building a release

```bash
./gradlew bundleRelease       # produces app/build/outputs/bundle/release/app-release.aab
```

The bundle is only signed if `RELEASE_KEYSTORE_PATH` etc. are set in
`local.properties`.

## Tests

```bash
./gradlew :app:testDebugUnitTest      # unit tests
./gradlew :app:connectedDebugAndroidTest   # instrumented (needs a device)
```

## Known v1 deferrals

- Manual Crop & Adjust / Filter Selector / Multi-page reorder screens are not
  implemented as standalone screens — they are surfaced by ML Kit Document
  Scanner's own in-flow UI (spec §3, screens 9–12).
- Password-protected PDF export is not implemented. iText 7 is AGPL, which is
  incompatible with Play Store distribution under our license terms. Spec §6.5
  asked for this flag; an alternative library (or rolling our own with
  PDFBox-Android, Apache 2.0) is a follow-up.
- Component Gallery debug screen is not implemented.
- Native ad cards every 7th item are not yet rendered (the AdMob manager and
  build config are in place; the in-list render is a UI follow-up).
- Detailed export options dialog (quality / page size) is not yet implemented;
  share sheet on Document Detail uses the system Send intent with the saved
  PDF as-is.
- See `BUILD_LOG.md` for the full per-phase status.

## Privacy contract

This is the single most important guarantee Snapdoc makes:

> Document images never leave the device. Only extracted OCR text is ever sent
> to Google Gemini, and only when the user explicitly invokes AI Summary or
> auto-categorization.

This is enforced at one chokepoint: `core/network/GeminiClient.kt`. Every
public method on that class accepts only a `String` parameter (OCR text). Do
not add an overload that takes images, bitmaps, URIs, or filenames. If you
must, get explicit sign-off and update the Play Store listing first.
