# SnapDoc — Release Checklist & Runbook

Everything needed to ship SnapDoc to the Play Store, and the routine for
every release after. Store copy lives in `05_play_store_listing.md`.

Package: `com.snapdoc.app` · First version: `1.0.0` (versionCode 1)

---

## 1. Versioning
- `app/build.gradle.kts` → `versionCode = 1`, `versionName = "1.0.0"` — correct for the first upload.
- **Every** Play upload needs a **unique, higher `versionCode`** → increment by 1 each time (1 → 2 → 3…).
- `versionName` is the public label (e.g., `1.0.1`, `1.1.0`) — bump it for meaningful releases.

## 2. Signing key (one-time setup)
Modern apps use **Play App Signing**: Google holds the real app-signing key; you upload builds signed with your **upload key** (recoverable if lost — but back it up anyway).

1. Generate the upload keystore (keep it OUTSIDE the repo):
   ```bash
   keytool -genkey -v -keystore ~/snapdoc-release.jks \
     -keyalg RSA -keysize 2048 -validity 10000 -alias snapdoc
   ```
2. Add to `local.properties` (git-ignored — never committed):
   ```
   RELEASE_KEYSTORE_PATH=/absolute/path/to/snapdoc-release.jks
   RELEASE_KEYSTORE_PASSWORD=...
   RELEASE_KEY_ALIAS=snapdoc
   RELEASE_KEY_PASSWORD=...
   ```
3. **Back up** the `.jks` file + passwords (password manager + offsite). This is the master key to your app.

## 3. Secrets in local.properties (required for a real release)
```
ADMOB_APP_ID=ca-app-pub-XXXX~YYYY      # real IDs; debug uses Google test IDs
ADMOB_BANNER_ID=ca-app-pub-XXXX/...
ADMOB_INTERSTITIAL_ID=ca-app-pub-XXXX/...
ADMOB_REWARDED_ID=ca-app-pub-XXXX/...
ADMOB_NATIVE_ID=ca-app-pub-XXXX/...
```
No Gemini key goes here — Gemini access is via Firebase AI Logic, key held
server-side. Confirm `app/google-services.json` is present (Firebase plugins
are active; the build fails without it), and before the first release build:
- [ ] Firebase console → **Build → AI Logic** → Gemini Developer API is enabled.
- [ ] Firebase console → **Build → App Check** → app registered with the
      **release** keystore's SHA-256, Play Integrity provider linked to the
      Play Console project, enforcement turned on for AI Logic.

## 4. Build the release
```bash
./gradlew bundleRelease
```
Output: `app/build/outputs/bundle/release/app-release.aab` → upload this to Play.
- Confirm the build used **real** ad IDs (the build computes `USING_TEST_AD_IDS` — it should be `false` for release).

## 5. Play Console — first submission
- [ ] Create the app (package `com.snapdoc.app` — permanent).
- [ ] Upload the `.aab` to **Internal testing** (enrolls you in Play App Signing).
- [ ] **In-app product:** create `snapdoc_remove_ads` (one-time / managed product, NOT a subscription), set price ₹99 / $1.49, Activate.
- [ ] **License testing:** add tester Gmail accounts; install from the track link to test Buy + Restore.
- [ ] **AdMob:** create the app + banner/interstitial/rewarded/native ad units; publish `app-ads.txt` on your site; link AdMob to the Play app.
- [ ] Store listing: title, short & full description, screenshots, feature graphic (see `05_play_store_listing.md`); app icon is in `design/icon-source/play-store-512.png`.
- [ ] **Privacy Policy URL** (live, required).
- [ ] **Data safety** form (analytics, ad ID, crash; images stay on device; OCR text → Gemini on user action).
- [ ] **Advertising ID** declaration (the app holds the AD_ID permission).
- [ ] Content rating questionnaire (expect Everyone; declare ads + IAP).
- [ ] Target audience: not children.

## 6. Pre-release verification (on a real device)
- [ ] Scan → save → OCR → search works.
- [ ] **AI Summary works in a RELEASE build** (uses `gemini-2.5-flash`; confirm R8 didn't break JSON parsing).
- [ ] Auto-categorization assigns sensible categories (not always "Other").
- [ ] Buy + Restore "Remove Ads" via the internal testing track.
- [ ] Force a crash → confirm it appears in Firebase Crashlytics.
- [ ] Quick TalkBack/accessibility pass.
- [ ] Launcher icon + splash render correctly (not cropped) on a couple of devices.

## 7. Known deferrals (NOT blockers — conscious v1 cuts)
- Password-protected PDF export (AGPL licensing) → v2.
- Detailed export options dialog (quality/page size).
- Additional Indic OCR scripts (Tamil/Telugu/etc.) — English + Hindi ship now.
- Document translation & chat-with-document Q&A → v2.
- Optional: remove the unused `CAMERA` permission for a cleaner Data Safety story (the ML Kit scanner doesn't need it).
- EEA/UK only: add the Google UMP consent SDK before a broad EU launch.

## 8. Every release after v1.0.0
1. Bump `versionCode` (+1) and `versionName` in `app/build.gradle.kts`.
2. `./gradlew bundleRelease`.
3. Upload `.aab` → testing track → verify → promote to Production.
4. Update "What's new" notes.
