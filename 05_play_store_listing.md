# SnapDoc — Play Store Listing Kit

Copy-paste source for the Google Play Console listing. Brand name: **SnapDoc**.
Package: `com.snapdoc.app`.

---

## App title (max 30 chars)
**SnapDoc - Private PDF Scanner** *(29)*

Alternatives:
- `SnapDoc: AI Document Scanner` *(28)*
- `SnapDoc - Scan PDF & OCR` *(24)*

## Short description (max 80 chars)
**Private AI document scanner — scan to PDF, OCR search & instant summaries** *(72)*

Alternatives:
- `Scan to PDF, search any text & summarize with AI. 100% private, no signup` *(74)*
- `Free PDF scanner with AI summaries. Your scans never leave your phone.` *(69)*

## Full description (max 4000 chars)

```
SnapDoc is the privacy-first document scanner that turns paper into crisp, searchable PDFs in seconds — then uses AI to summarize and auto-organize every scan, so you actually find things later.

No account. No forced cloud uploads. No watermarks. Your documents stay on your phone.

▶ SCAN ANYTHING, INSTANTLY
• Sharp, automatic edge detection and cropping
• Multi-page documents in one PDF
• Enhancement filters: Auto, Color, Grayscale, and Magic Color for faded pages
• Bills, receipts, IDs, contracts, notes, whiteboards, handwriting and more

▶ FIND ANY DOCUMENT BY WHAT'S INSIDE IT
• On-device OCR (text recognition) reads every scan — English and Hindi
• Full-text search across filenames AND the text inside your documents
• Stop scrolling through folders — just type what you remember

▶ AI THAT ACTUALLY HELPS
• One-tap AI Summary: a quick TL;DR plus key bullet points for any document
• Smart auto-categorization sorts scans into Bills, IDs, Receipts, Notes, Contracts and more
• Create your own custom categories

▶ TRULY PRIVATE — THIS IS OUR PROMISE
We don't have a server. Your scanned images never leave your device. When you ask AI to summarize, only the extracted text is sent — never your original image, filename, or any personal data. No sign-up, no email, no account, ever.

▶ NO SUBSCRIPTIONS. EVER.
SnapDoc is free and ad-supported. One simple, one-time "Remove Ads" purchase removes all ads and unlocks unlimited AI summaries. No monthly fees, no traps.

▶ BUILT TO BE FAST AND CLEAN
• Minimal, modern, clutter-free design with light & dark mode
• Works offline — scan, read, search and organize with no connection
• Share any document as a PDF to WhatsApp, email, or anywhere

WHY SNAPDOC INSTEAD OF OTHER SCANNERS?
Most scanner apps push subscriptions, accounts, watermarks and cloud uploads you didn't ask for. SnapDoc gives you a fast, beautiful scanner that respects your data and your wallet — with AI that saves you real time.

Coming soon: document translation and chat-with-your-document Q&A.

Scan smarter. Stay private. Download SnapDoc today.
```

## ASO keywords (weave into title + short + full description — no separate field on Play)
**Primary:** document scanner, PDF scanner, scan to PDF, OCR, scanner app, AI scanner, free scanner
**Secondary:** scan documents, text scanner, receipt scanner, bill scanner, ID scanner, doc scanner, image to PDF, private scanner, no subscription scanner

Don't repeat any term more than ~4–5 times; Google penalizes keyword stuffing.

## Graphics checklist
| Asset | Spec | Status |
|---|---|---|
| App icon | 512×512 PNG | done (`design/icon-source/play-store-512.png`) |
| Feature graphic | 1024×500 PNG | needed |
| Phone screenshots | 2–8, 9:16 (e.g. 1080×1920) | needed |
| Promo video | YouTube URL | optional |

**Feature graphic concept:** cream background (#FAFAF8), SnapDoc icon on the left, headline "Scan. Search. Summarize." on the right, subline "Private AI document scanner — no subscription." Keep text away from the edges.

## Screenshot plan (with caption overlays)
1. Home / document list — "Every scan, neatly organized."
2. Scanner capturing a page — "Sharp scans in seconds."
3. AI Summary sheet — "Understand any document instantly."
4. Search results — "Find anything by typing what's inside."
5. Categories grid — "Auto-sorted: bills, IDs, receipts & more."
6. Privacy / onboarding screen — "Your scans never leave your phone."
7. Remove Ads / paywall — "No subscriptions. One-time unlock."
8. Dark mode home — "Clean, fast, light & dark."

## Store settings
- **Category:** Productivity *(alt: Business)*
- **Contact email:** <your support email>
- **Website:** <your landing page> (optional)
- **Privacy Policy URL:** REQUIRED — must be live before submission

## Data safety form (summary)
- **Collected:** App activity & diagnostics (Firebase Analytics/Crashlytics, anonymous); Device/Advertising ID (AdMob).
- **Shared:** with Google (ads/analytics). Document text is sent to Google Gemini only when the user triggers a summary/auto-categorization.
- **Not collected:** scanned images never leave the device; no name, email, or account.
- **Encrypted in transit:** Yes. **Deletion:** data is on-device; uninstalling removes it.

## Content rating
Expect Everyone / PEGI 3. Declare: contains ads = Yes; in-app purchases = Yes (₹99 / $1.49 one-time); no violence/UGC.

## "What's new" (v1.0.0 release notes)
```
Welcome to SnapDoc! The privacy-first AI document scanner.
• Scan to crisp, multi-page PDFs
• On-device OCR + full-text search
• One-tap AI summaries and smart auto-sorting
• 100% private — no account, no cloud uploads
```
