# Privacy-First AI Document Scanner — Product Specification

**Version:** 1.0
**Status:** v1 Build Ready
**Working Name:** ScanFlow *(replace globally once final name is locked — see §1.5)*

---

## 1. Product Overview

### 1.1 Vision
A clean, fast, privacy-first document scanner for Android that turns paper into searchable PDFs in seconds — and uses AI to summarize and auto-organize what you scan, so you actually find things later instead of drowning in folders.

### 1.2 Core Positioning
Privacy-first alternative to CamScanner and Adobe Scan, built India-out for the world. No mandatory signup, no hidden cloud uploads, no watermarks on PDF content. AI features run privately — only extracted OCR text is ever sent to AI providers, never document images.

### 1.3 Target Audience
- **Primary (India + Global English equally):** Working professionals, students, freelancers, and small business owners aged 22–45 who scan documents weekly (bills, IDs, receipts, contracts, notes) and want a clean tool that doesn't trade their data for free use.
- **Secondary:** Privacy-conscious users actively distrustful of CamScanner due to its history of malware incidents and Chinese ownership.

### 1.4 Monetization Model
- **Free tier:** All features available, ad-supported (banner + native + interstitial + rewarded ads for AI features).
- **One-time IAP — "Remove Ads":** ₹99 / $1.49 one-time purchase. Removes all ads and unlocks unlimited AI summaries (no rewarded-ad gate). No subscriptions, ever.

### 1.5 Naming Shortlist
Pick one before design phase begins:
1. **Scribe** — clean, short, hints at writing and documents
2. **DocVault** — privacy-forward
3. **ScanFlow** — emphasizes smooth UX
4. **PaperPilot** — friendly, memorable
5. **Snapdoc** — simple, descriptive
6. **PrivyScan** — privacy-explicit

### 1.6 Visual Personality
Minimal and clean — Notion / Linear vibe. Plenty of whitespace, restrained color palette, modern typography, no skeuomorphism, no clutter. The product should feel like a serious tool, not a consumer toy.

---

## 2. Core Features (v1 Scope)

### 2.1 Document Scanning
- Camera capture with real-time edge detection (Google ML Kit Document Scanner API)
- Manual crop adjustment with corner handles
- Multi-page support — combine multiple pages into one document
- Page reorder via drag-and-drop
- Retake or delete individual pages
- Image enhancement modes:
  - **Auto** (default — automatic color correction)
  - **Color** (preserve original colors)
  - **Grayscale** (B&W with sharp text)
  - **Magic Color** (high contrast cleanup for faded docs)
- Manual brightness and contrast sliders

### 2.2 OCR — Text Extraction
- Fully on-device OCR via Google ML Kit Text Recognition v2
- Supported scripts: English + Hindi/Devanagari, Tamil, Telugu, Kannada, Malayalam, Bengali, Gujarati
- Extracted text stored in local DB linked to each document
- Powers in-app full-text search across all scans
- "Copy text" action available on document detail view
- Runs automatically after scan completes (toggleable in Settings)

### 2.3 AI Summary
- One-line TL;DR plus 3–5 bullet point summary of any scanned document
- Powered by Google Gemini Flash API (latest stable model at build time)
- **Only extracted OCR text is sent — never the original image**
- Result cached locally so re-opening the document doesn't re-trigger an API call
- **Free tier:** gated behind a single rewarded video ad per summary
- **Remove Ads tier:** unlimited, instant, no ad gate

### 2.4 Smart Auto-Categorization
- Every scanned document is auto-tagged into one of: **Bills, IDs, Receipts, Notes, Contracts, Other**
- Classification runs on OCR text via Gemini API in a single low-cost call (~$0.0001 per doc, well within free tier for early users)
- User can manually override category at any time
- Supports custom user-created categories beyond the defaults

### 2.5 PDF / File Export
- Export formats: **PDF** (default), **JPEG/PNG images**, **ZIP of images**
- Quality options: Low / Medium / High (affects file size)
- Page size: Auto / A4 / Letter / Legal
- Optional **password-protected PDF** — free for all users (privacy is the brand)

### 2.6 File Management
- Home screen is a chronological document list, newest first
- Folder / category views with auto-grouped sections
- Search across:
  - Document filename
  - **Full OCR text content** (find anything by typing what's inside it)
  - Category
- Multi-select mode for batch delete / move / share
- Per-document actions: rename, favorite, delete, share, export
- Quick filters: Recent, Favorites, by Category

### 2.7 Sharing
- Standard Android Share Sheet (PDF / image / text)
- **WhatsApp quick-share** button (high priority for India market)
- Email quick-share
- "Copy extracted text" action

### 2.8 Settings
- Default scan mode
- Default page size
- Toggle: Auto-OCR on/off (on by default)
- Toggle: AI Summary auto-suggestion on/off
- Manage Categories
- Remove Ads (with Restore Purchase)
- Privacy Policy link
- Rate the app
- Share the app
- About / Version

### 2.9 Onboarding (First Launch Only)
- 3-screen carousel:
  1. **"Privacy-first scanner"** — your docs stay on your phone
  2. **"AI-powered summaries"** — understand any document in seconds
  3. **"Auto-organized"** — find anything by typing what's inside
- Permission request: Camera (required), Storage (required for older Android)
- **No signup, no email, no account** — straight to home screen after onboarding

---

## 3. Complete Screen Inventory

Every screen the app needs. This list drives the design prompt — every screen below must have a mockup.

| # | Screen | Purpose |
|---|--------|---------|
| 1 | Splash | App logo, ~1 second, transition to home or onboarding |
| 2 | Onboarding 1 — Privacy | "Your docs stay on your phone" |
| 3 | Onboarding 2 — AI Summary | "Understand any doc in seconds" |
| 4 | Onboarding 3 — Auto-Organize | "Find anything by typing what's inside" |
| 5 | Home — Document List | Chronological list, FAB to scan, bottom nav |
| 6 | Home — Empty State | First-time user, no documents yet |
| 7 | Categories View | Filter by Bills / IDs / Receipts / Notes / Contracts / Other |
| 8 | Search Screen | Live results across filenames + OCR content |
| 9 | Camera / Scanner Capture | Live edge detection overlay, capture button |
| 10 | Crop & Adjust | Manual corner-handle adjustment after capture |
| 11 | Multi-Page Review | Carousel of pages, reorder, retake, add page, delete |
| 12 | Filter / Enhancement Selector | Auto / Color / Grayscale / Magic Color preview |
| 13 | Save Document | Auto-suggested filename, auto-detected category, save button |
| 14 | Document Detail | PDF preview, action toolbar (share, export, summarize, delete) |
| 15 | AI Summary Bottom Sheet | TL;DR + bullets, regenerate button |
| 16 | AI Summary Loading State | Animated loading while Gemini call resolves |
| 17 | Rewarded Ad Modal | "Watch a 15-second ad to generate summary" + CTA |
| 18 | Full-Text Viewer | Extracted OCR text, copyable, scrollable |
| 19 | Share Sheet | PDF / image / text options |
| 20 | Export Options | Quality + size + optional password |
| 21 | Multi-Select Mode | Checkbox state on doc list, batch action toolbar |
| 22 | Settings | Full settings menu |
| 23 | Manage Categories | Add / rename / delete custom categories |
| 24 | Remove Ads Paywall | Pricing, benefits, Buy + Restore Purchase buttons |
| 25 | About / Privacy Policy | App version, privacy stance, links |
| 26 | Error States | OCR failed, AI network error, no internet, low storage |

**Total: 26 screens to design.**

---

## 4. Privacy & Data Architecture

### 4.1 What Stays On Device
- All scanned images, PDFs, and metadata
- OCR text extracted from documents
- User preferences and settings
- Local search index

### 4.2 What Leaves The Device (only when user explicitly triggers AI)
- Extracted OCR text only — sent to Google Gemini API for summary or categorization
- **Never sent:** original images, filenames, metadata, location, device identifiers

### 4.3 No Required Signup
- Zero accounts in v1
- Anonymous Firebase Analytics + Crashlytics for aggregate usage and crash reporting only — no personal identifiers
- IAP linked to Google Play account, not to any in-app account

### 4.4 Privacy Policy Headline (use in marketing + onboarding)
> "We don't have a server. Your scans never leave your phone unless you ask AI to summarize them — and even then, only the typed-out text is sent, never the image."

---

## 5. Monetization Implementation Detail

### 5.1 Ad Placements (Free Tier)
| Placement | Format | Frequency |
|-----------|--------|-----------|
| Home — bottom | Banner (320x50 / adaptive) | Always visible |
| Document list | Native ad | Every 7th item |
| After document save | Interstitial | Every 3rd save, 60-second cooldown |
| Before AI Summary | Rewarded video (15–30s) | One per summary request |
| Remove Ads paywall surface | — | Triggered after 5 interstitials shown to a user |

### 5.2 Remove Ads IAP
- **Price:** ₹99 (India) / $1.49 (global) — one-time, non-consumable
- **Removes:** all banners, natives, interstitials
- **Unlocks:** unlimited AI summaries (no rewarded-ad gate)
- **Restorable** via Google Play

### 5.3 Ad Network
- **Primary:** Google AdMob
- **Mediation:** to be added post-launch (Meta Audience Network, AppLovin) once volume justifies setup

---

## 6. Technical Stack

| Layer | Choice |
|-------|--------|
| Language | Kotlin |
| UI | Jetpack Compose |
| Min SDK | Android 8.0 (API 26) — covers ~95% of devices |
| Target SDK | Latest stable (API 35+ at build time) |
| Document Scanning | Google ML Kit Document Scanner API |
| OCR | Google ML Kit Text Recognition v2 |
| AI Summary + Categorization | Google Gemini Flash via REST API |
| PDF Generation | ML Kit Document Scanner output (PDF) + Android `PdfDocument` for custom exports |
| Local Database | Room (document metadata, OCR text, search index) |
| File Storage | App-specific external storage, Scoped Storage compliant |
| Ads | Google AdMob SDK |
| In-App Purchase | Google Play Billing v7 |
| Analytics | Firebase Analytics — anonymous only |
| Crash Reporting | Firebase Crashlytics |
| Architecture | MVVM + single-activity + Compose Navigation |
| DI | Hilt |
| Async | Kotlin Coroutines + Flow |

**No backend required.** Everything is client-side except direct device-to-Gemini API calls.

---

## 7. v2 / Future Scope (Out of v1)

- AI Translation of scanned documents (already a chosen v2 feature)
- Document Q&A — chat with your scanned doc (already a chosen v2 feature)
- Searchable PDF with embedded OCR text layer
- Optional encrypted cloud sync (E2E)
- Document signing / e-signature
- PDF form filling
- Export to Word / Excel
- Bulk import from gallery
- Home screen widget for quick scan
- Tablet-optimized layout
- Dark mode polish (basic dark mode is in v1 scope, see §9)

---

## 8. Success Metrics — First 90 Days Post-Launch

| Metric | Target |
|--------|--------|
| Total installs | 50,000+ organic |
| Day 1 retention | ≥ 40% |
| Day 7 retention | ≥ 20% |
| DAU / MAU ratio | ≥ 15% |
| Avg sessions per user per week | ≥ 3 |
| Play Store rating | ≥ 4.4 |
| Remove Ads IAP conversion | ≥ 2% of MAU |
| Effective ad RPM (India) | ₹40–80 per 1,000 sessions |
| Crash-free rate | ≥ 99.5% |

---

## 9. Open Decisions to Lock Before Design Phase

- [ ] **Final app name** — pick from §1.5
- [ ] **Brand primary color** — recommended: cool indigo `#4F46E5`, trust-blue `#2563EB`, or graphite-black `#111111`
- [ ] **App icon direction** — modern minimal mark vs slightly illustrative
- [ ] **Light + Dark mode at v1** — recommended yes (Compose makes it cheap to support)
- [ ] **Logo wordmark + symbol** — design vs commission

---

*End of v1 Product Specification.*
