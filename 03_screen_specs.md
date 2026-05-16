# Snapdoc — Screen Specifications v1

**Version:** 1.0
**Companion to:** `02_design_system.md` (tokens) and `04_hero_mockups.html` (visual reference for 8 hero screens)
**Audience:** Engineering. Every token referenced below is defined in `02_design_system.md`. No raw hex appears here.

**Convention:**
- "Top-bar / app-bar" refers to the Snapdoc top app bar (component 8.4.2). Status bar is the Android system status bar — its color is set per screen.
- "Bottom nav" refers to component 8.4.1. It is hidden on full-flow screens (camera, crop, multi-page review, save, document detail, paywall, onboarding, splash, dialogs).
- "Banner ad" placement obeys §5.1 of the product spec.
- Animation references (`dur-slow`, `ease-emphasized` etc.) are defined in §6 of the design system.

---

## Screen 1: Splash

**Purpose:** Hold the user for ~800ms while the app warms cold-start, then route to onboarding (first launch) or Home.

**Layout (top to bottom):**
| Region | Element | Component | Notes |
|---|---|---|---|
| Status bar | Translucent over `bg` | system | Light icons in dark mode, dark icons in light. |
| Full-bleed centered | Snapdoc symbol (logo §1.3) | SVG, 64×64dp | Centered both axes. |
| Below symbol | Wordmark `snapdoc` | SVG path | 24dp gap below symbol; `titleXl` cap-height equivalent. |
| Bottom 32dp | Loading indicator | spinner 16dp `textTertiary` | Visible only if cold-start exceeds 600ms. |

**States:**
- Default: logo + wordmark, no spinner.
- Slow cold-start: spinner fades in after 600ms.
- Error (rare — DB migration failure): replace logo with `error` glyph 48dp + `bodyMd textPrimary` "Snapdoc could not start. Reinstall to recover." This is the only catastrophic path.

**Interactions:** none. The splash is non-interactive.

**Transitions:**
- Enter from: cold launch — fade in 80ms.
- Exit to: Onboarding 1 (first launch) or Home (subsequent). Crossfade 240ms `ease-standard`.

**Dark mode notes:** logo inverts; bg uses `bg` token for the mode.

**Ad placement:** None.

**Edge cases:**
- Migration in progress: persist splash until migration completes, max 4s. After 4s show indeterminate progress bar at bottom edge.
- DB corrupt: error path above.

---

## Screen 2: Onboarding 1 — Privacy

**Purpose:** Establish privacy-first positioning before any other product narrative.

**Layout (top to bottom):**
| Region | Element | Component | Notes |
|---|---|---|---|
| Status bar | `bg` | system | |
| Top safe-area (24dp below status) | Skip ghost button trailing | ghost | "Skip" `labelLg textSecondary`, taps to Home. |
| Top third (vertical center of upper 40%) | Privacy hero glyph | 96dp `lock` Lucide, `textPrimary` | A single 24×24dp `accent` dot sits inside the lock body — the brand's only visual flair. |
| 48dp below glyph | Title | `headlineXl textPrimary` | "Your scans stay on your phone." |
| 16dp below title | Body | `bodyLg textSecondary` | "We don't have a server. Documents are saved locally — only the typed-out text is ever sent to AI when you ask for a summary." Centered, max-width 320dp. |
| Bottom 80dp | Page indicator | 3 dots, 6dp diameter, 8dp gap | Active dot `primary`, inactive `divider`. |
| Bottom 24dp | Primary button "Continue" | primary, full-width minus 24dp | 48dp height. |

**States:**
- Default: as above.
- Loading: never — onboarding is fully local.
- Error: never.
- Success: tapping Continue advances.

**Interactions:**
- Tap Continue → advance to Onboarding 2.
- Tap Skip → set `onboarding_complete=true`, navigate to Home.
- Swipe left → advance to Onboarding 2.
- Swipe right → no-op (we are on the first page).

**Transitions:**
- Enter from: Splash, crossfade 240ms `ease-standard`. Then on first paint, the glyph and title fade-up 12dp over `dur-extra-slow` `ease-decelerate`.
- Exit to: Onboarding 2 — slide left `dur-slow` `ease-standard`.

**Dark mode notes:** glyph stroke `textPrimary` (off-white). The `accent` dot brightens to `D97706`.

**Ad placement:** None.

**Edge cases:**
- User backgrounds → returns: resume on same page.
- Locale change mid-onboarding: re-render with localized text; do not reset position.

---

## Screen 3: Onboarding 2 — AI Summary

**Purpose:** Show the AI summary value prop as the second pillar.

**Layout (top to bottom):**
| Region | Element | Component | Notes |
|---|---|---|---|
| Status bar | `bg` | | |
| Top safe-area | Skip | ghost | trailing. |
| Hero (upper 40%) | Mini "summary card" mock | static SVG illustration | A schematic document with a thin horizontal line stack on the left (the doc), connected by a 1.5dp line to a small surface card on the right with 1 long bar (TL;DR) + 3 short bars (bullets). All strokes 1.5dp `textPrimary`; `accent` highlight on the TL;DR bar. ~280dp tall. |
| 48dp below | Title | `headlineXl` | "Understand any doc in seconds." |
| 16dp below | Body | `bodyLg textSecondary` | "Snapdoc reads what's inside and gives you a TL;DR plus the key points. Only the text leaves your phone — never the image." Centered. |
| Page indicator | dot 2 active | | |
| Primary button | "Continue" | | |

**States:** mirror Onboarding 1.

**Interactions:**
- Tap Continue / swipe left → Onboarding 3.
- Swipe right → Onboarding 1.

**Transitions:** slide left/right `dur-slow`.

**Dark mode notes:** the schematic's strokes go to `textPrimary` (off-white).

**Ad placement:** None.

**Edge cases:** see Screen 2.

---

## Screen 4: Onboarding 3 — Auto-Organize

**Purpose:** Close with the "find anything" promise, request permissions, route to Home.

**Layout (top to bottom):**
| Region | Element | Component | Notes |
|---|---|---|---|
| Status bar | `bg` | | |
| Top safe-area | (no Skip — this is the final step) | | |
| Hero | Search-result schematic | SVG | A search field outline (matches our search-field style) with the cursor caret blinking, and 3 stacked "result" rows beneath: each a 1.5dp document outline with a tiny `accent` highlight on a "matched" portion of an internal text bar. ~280dp tall. |
| 48dp below | Title | `headlineXl` | "Find anything by typing what's inside." |
| 16dp below | Body | `bodyLg textSecondary` | "Every scan is auto-organized into Bills, IDs, Receipts, Notes, Contracts. Search across them all — even text inside a document." |
| Page indicator | dot 3 active | | |
| Primary button | "Allow camera & start" | primary | Triggers permission prompt. |
| 8dp below | Tertiary | "How permissions work" ghost | Opens a small dialog explaining camera + storage rationale. |

**States:**
- Default.
- Permission denied: dialog "Snapdoc needs camera access to scan. Enable it from Settings." Buttons: "Open settings" (primary) / "Not now" (ghost).
- Permission granted: navigate to Home.

**Interactions:**
- Tap "Allow camera & start" → system permission dialog.
- Tap "How permissions work" → info dialog.
- Swipe right → Onboarding 2.

**Transitions:**
- Exit to Home (granted): crossfade 240ms; FAB enters with `dur-slow` scale 0.9 → 1 `ease-emphasized`.
- Exit to Settings deep-link (denied + tapped Open settings): system handles.

**Dark mode notes:** the SVG cursor blink uses `accent` for visibility.

**Ad placement:** None.

**Edge cases:**
- User denies permission and returns: Home shows but tapping FAB triggers a snackbar "Camera access required. Enable in Settings." with action "Open".

---

## Screen 5: Home — Document List

**Purpose:** The default landing screen post-onboarding. Chronological list of all scans, primary entry to scan + search + organize.

**Layout (top to bottom):**
| Region | Element | Component | Notes |
|---|---|---|---|
| Status bar | `bg` | system | Dark icons (light mode) / light icons (dark mode). |
| App bar (56dp) | Wordmark "snapdoc" leading; trailing icons: `search`, `multi-select` (`square-check`), `sort` (`arrow-down-up`) | top-bar | All trailing icons 24dp in 48dp frames. |
| Content (scroll) | Scrollable list of document cards (8.3.1) grouped by section header (8.6.2): "TODAY", "EARLIER THIS WEEK", "OLDER" | list | 16dp horizontal margins. 8dp inter-card. Native ad card every 7th item. |
| Banner ad | 50–60dp banner, 1dp top divider | ads §8.7.1 | Always visible (free tier). |
| Bottom nav (64dp + safe area) | Home (active), Categories, Settings | bottom-nav | |
| FAB | `scan-line` 56dp `primary` | FAB | Bottom-right, 16dp from edge, sits 16dp above banner ad. |

**States:**
- Empty (no docs): redirect to Screen 6 — Empty State.
- Loading (cold app, large library): show 6 skeleton document cards.
- Error (DB read failure): error state container with "We couldn't load your documents. Try again." Secondary button to retry.
- Success: rendered list.
- Pull-to-refresh: 32dp circular spinner `primary` slides down from top, refreshes thumbnails + OCR cache.

**Interactions:**
- Tap document card → Screen 14 Document Detail. Shared-element transition on the thumbnail: thumbnail morphs 56×72dp → full-bleed PDF preview over `dur-slow` `ease-emphasized`.
- Long-press card → enter Multi-Select Mode (Screen 21).
- Tap `search` → Screen 8 Search Screen.
- Tap `sort` → bottom sheet with Newest / Oldest / Name (A-Z) / Name (Z-A) / Largest / Smallest.
- Tap FAB → Screen 9 Camera. FAB scales 1 → 1.04 → 0.96, then expands as the entry origin for the camera view.
- Pull down → refresh.
- Scroll up → top-bar gains 1dp `divider` separator.

**Transitions:**
- Enter from: Onboarding 3, Splash, or back from any sub-screen — page transition `dur-slow` `ease-standard`.
- Exit to: Search (slide-up modal); Camera (FAB expand); Categories tab (instant tab switch); Document Detail (shared-element).

**Dark mode notes:** the category accent strip on each card retains its hue — strips are the loudest color anywhere in the dark UI, deliberately.

**Ad placement:**
- Banner ad bottom (always for free tier).
- Native ad card every 7th list item (component 8.7.2).

**Edge cases:**
- 1–6 documents: no native ads in feed yet (need 7+ to inject).
- Long filename: `titleLg` truncates at 2 lines with ellipsis.
- Banner ad fails to load: collapse to 0dp (don't leave empty space); FAB drops 8dp toward bottom nav.
- User has paid Remove Ads: hide banner, hide native ads.

---

## Screen 6: Home — Empty State

**Purpose:** First-time experience after onboarding completes; prompts the first scan.

**Layout (top to bottom):**
| Region | Element | Component | Notes |
|---|---|---|---|
| Status bar | `bg` | | |
| App bar (56dp) | Wordmark "snapdoc" leading; trailing: `search` (disabled — `textDisabled`) | | Search disabled until first doc exists. |
| Content (vertical center) | Empty-state container 8.6.3 | | Glyph: 96dp `file-plus` `textTertiary`. Title: "Scan your first document." Body: "Tap the camera to capture anything — a bill, an ID, a receipt. Snapdoc reads what's inside and saves it on your phone." Primary button: "Scan now" (full-width minus 48dp horizontal). |
| Bottom nav | as Screen 5 | | |
| FAB | `scan-line` | | Same position; primary path is the in-empty-state button, but the FAB is also visible. |

**States:** single state.

**Interactions:**
- Tap "Scan now" or FAB → Screen 9 Camera.
- Tap Settings or Categories tab → respective screens. Categories tab on empty state shows Screen 7 with all-zero counts.

**Transitions:**
- Enter from: Onboarding 3 (first launch) or Home List when user deletes their last document.
- Exit to: Camera.

**Dark mode notes:** glyph at `textTertiary` is barely visible; this is intentional — the empty state should feel quiet, not insistent.

**Ad placement:** None — no banner on empty state. (Showing ads to a user with zero content would feel hostile; the pricing is in onboarding subtext anyway.)

**Edge cases:**
- User deletes last document → animate transition from List to Empty over `dur-slow`: cards fade out, empty-state fades up.

---

## Screen 7: Categories View

**Purpose:** Browse documents by category; access from bottom nav.

**Layout (top to bottom):**
| Region | Element | Component | Notes |
|---|---|---|---|
| Status bar | `bg` | | |
| App bar (56dp) | Title "Categories" `titleXl` leading; trailing `search` | top-bar | |
| Content | 2-column grid of Category Cards (8.3.2) | grid | 16dp margins, 8dp gutter. Order: Bills, IDs, Receipts, Notes, Contracts, Other, then any custom categories (alphabetical). |
| 24dp below grid | "Manage categories" ghost button | full-width text-aligned center | Routes to Screen 23. |
| Banner ad | 50–60dp | | |
| Bottom nav | Categories active | | |

**States:**
- Default: 6 system categories + n custom.
- Loading (cold): skeleton placeholders for grid cells.
- Empty per-category (count 0): card still renders, count line shows "No documents".

**Interactions:**
- Tap category card → category-filtered list view (visual identical to Home List but filtered, with app-bar title showing "Bills · 14" using tabular numbers).
- Long-press card → context menu: Rename, Delete (custom only), Set color (custom only — picker from §2.4 strip palette).
- Tap "Manage categories" → Screen 23.

**Transitions:**
- From Home tab: instant.
- To filtered list: slide left `dur-slow`.

**Dark mode notes:** category icon color (the §2.4 strip color) drives the only color in the cell.

**Ad placement:** Banner only.

**Edge cases:**
- 0 documents total: cards still render with 0 counts; category-tap routes to that category's empty state.
- Custom category deleted while documents inside it exist: documents revert to "Other"; show snackbar "Documents moved to Other. Undo".

---

## Screen 8: Search Screen

**Purpose:** Live full-text + filename search; only entry from Home/Categories top-bar.

**Layout (top to bottom):**
| Region | Element | Component | Notes |
|---|---|---|---|
| Status bar | `bg` | | |
| App bar (56dp) | Leading 24dp `arrow-left` (back); search field 8.2.2 inline (focused immediately, keyboard up); trailing `x` clear when value present | | The search field replaces the title — no app-bar title on this screen. |
| Recent searches (when query empty) | List section: `overline` "RECENT" + up to 5 `bodyLg` rows with leading `clock` 16dp, trailing `arrow-up-left` 16dp | list-item | |
| Suggested categories (when query empty) | `overline` "JUMP TO" + horizontal scroll of category chips | chips | |
| Results (when query non-empty) | Section "Filenames" with matching filenames; section "Content" with documents whose OCR text matches; each result is a Document Card (8.3.1) with the matched substring highlighted (`accent` text color on matched chars). | list | |
| Banner ad | (none) | | Search hides the banner — calm, focused state. |
| Bottom nav | (hidden) | | Search occupies the full screen below the app bar. |

**States:**
- Empty query: recent searches + jump-to.
- Loading (typing fast, debounced 200ms): existing results dim to 60% opacity; spinner 16dp `accent` appears trailing in app-bar.
- No results: empty-state container "No matches for '{query}'. Try a different word or scan more documents."
- Error (search index corrupt): error-state container with "Rebuild index" Secondary button (rebuild runs OCR-derived index).
- Success: results list.

**Interactions:**
- Type → debounced live search.
- Tap recent → re-execute that query.
- Tap result → Document Detail.
- Tap `arrow-left` → exit Search.

**Transitions:**
- Enter from: Home/Categories — slide up `dur-default` `ease-emphasized` with a subtle bg fade `bg → bg`.
- Exit: slide down `dur-default` `ease-accelerate`.

**Dark mode notes:** the highlighted match in result text uses `accent` (`#D97706`) on `surface`.

**Ad placement:** None.

**Edge cases:**
- Query mixed-script (Hindi + English): ML Kit OCR handles both; ranking blends across scripts by token TF.
- Query is a long phrase: tokenize and AND-match across tokens.
- 1000+ results: virtualized list, paginated render in 50-result pages.

---

## Screen 9: Camera / Scanner Capture

**Purpose:** Capture document images with edge detection.

**Layout (top to bottom):**
| Region | Element | Component | Notes |
|---|---|---|---|
| Status bar | translucent over `surfaceSunken` | system | Light icons regardless of mode (camera ground is dark in both). |
| Top app bar (56dp, transparent over viewfinder) | Leading `x` 24dp `#FFFFFF` (close); trailing icons: flash toggle (`zap` / `zap-off`), gallery picker (`image`) | top-bar | |
| Viewfinder (full-bleed) | Live camera feed; edge-detection overlay (8.8.2) drawn over the feed; instruction pill at top: bg `surfaceVariant` at 80% opacity, `bodySm onPrimary` "Position document inside the frame" | overlay | |
| Mode selector (above shutter, 16dp gap) | Segmented control: Single | Multi | ID-card | segmented control 8.1.7 | The first two trigger different review flows; ID-card shows two-page corner alignment guide. |
| Shutter zone (bottom 144dp + safe area) | Centered shutter: 72dp double-ring (outer 72dp, inner 56dp white fill, 4dp gap), trailing `image` 32dp gallery import, leading captured-page-count badge when in Multi mode (`labelMd onPrimary` on `surfaceVariant`-80% pill) | custom shutter | Shutter is `radius-full`. |

**States:**
- Default: live feed.
- Detecting: edge brackets pulse 80% → 100% opacity `dur-fast` loop until 4 corners stable for 600ms.
- Auto-capture (when stable, optional setting): 1.2s countdown ring around the shutter.
- Capture: shutter scales to 0.9 then back, screen flashes white 80ms.
- Permission denied: full-screen empty state "Camera access needed", primary "Open settings".
- Low light: snackbar "Low light — try the flash" once per session.

**Interactions:**
- Tap shutter → capture, then advance to Crop (Single mode) or stay in Camera with badge incremented (Multi mode).
- Tap mode segment → switch capture flow.
- Tap flash → cycle off / on / auto.
- Tap gallery → system image picker.
- Long-press shutter (Multi mode only) → end multi-capture, advance to Multi-Page Review.
- Tap `x` → exit, prompt if pages captured.

**Transitions:**
- Enter from: Home FAB — FAB expands radially into the shutter, viewfinder fades up `dur-slow` `ease-emphasized`.
- Exit to: Crop (Single) — shared-element transition on the captured frame; the captured image translates to fill the Crop screen viewport over `dur-slow` `ease-emphasized`.

**Dark mode notes:** Camera is mode-agnostic — the viewfinder is black either way. The top-bar icons stay `#FFFFFF`. The instruction pill's `surfaceVariant` honors the active mode for the snackbar overlay underlying ground.

**Ad placement:** None.

**Edge cases:**
- Permission denied at this entry: full-screen empty state above; back returns to Home.
- Storage near full (<100MB): snackbar "Storage low. Free up space before scanning more."
- Phone rotates: viewfinder rotates; overlay corner brackets follow.
- Battery thermal throttle: drop to 720p preview silently.

---

## Screen 10: Crop & Adjust

**Purpose:** Review and adjust auto-detected crop on a single captured page.

**Layout (top to bottom):**
| Region | Element | Component | Notes |
|---|---|---|---|
| Status bar | `surfaceSunken` | | |
| App bar (56dp) | Leading `arrow-left`; title "Crop" `titleXl`; trailing ghost "Reset" `labelLg accent` | | |
| Content | Captured image full-width with 16dp margins, on `surfaceSunken` ground, with corner crop handles (8.8.3) and connecting edge lines | | The image extends as far as needed within remaining vertical space. |
| Bottom action row (96dp + safe area) | Three actions: leading "Retake" ghost (`rotate-ccw` + label), center primary "Continue" 48dp full-width minus 32dp + outer ghosts, trailing "Rotate" icon-only (`rotate-cw`) | | Layout: [Retake] · [— Continue —] · [Rotate], outer items 56dp each, primary fills middle. |

**States:**
- Default: handles at auto-detected corners.
- Drag in progress: dragged handle shows 2dp `accent` border; magnifier loupe (40×40dp circular `surface` `elev-3`) appears 16dp above finger position showing the cropped corner zoomed at 2.5×.
- Reset: handles snap back to ML-Kit auto-detection.
- Manual rectangular: if handles form a strict rectangle, crop is treated as no-perspective; otherwise perspective transform applies.

**Interactions:**
- Drag any corner handle → crop polygon updates.
- Drag any edge line → both adjacent corners translate together along the perpendicular.
- Tap Retake → return to Camera, discard this frame.
- Tap Rotate → rotate image 90° CW; handles re-fit.
- Tap Reset → reset to auto-detection.
- Tap Continue → advance: if Single-mode → Filter (Screen 12); if Multi-mode and there are still un-cropped pages → next page's Crop; once all cropped → Multi-Page Review.
- Pinch-zoom on image → scale crop view 1×–4× to refine handle placement.

**Transitions:**
- Enter from: Camera shared-element, image in place.
- Exit to: Filter (slide left `dur-default`) or Multi-Page Review (slide left).

**Dark mode notes:** ground is `surfaceSunken` in both modes; the captured image displays its actual brightness.

**Ad placement:** None.

**Edge cases:**
- Auto-detection failed (low contrast doc): handles default to image's outer 4 corners (no crop). Show snackbar "Couldn't detect edges — adjust manually."
- User crops to <10% of image: clamp at 10%, show snackbar.

---

## Screen 11: Multi-Page Review (HERO)

**Purpose:** Review all captured pages, reorder, retake, add, delete; then advance to Filter or Save.

**Layout (top to bottom):**
| Region | Element | Component | Notes |
|---|---|---|---|
| Status bar | `bg` | | |
| App bar (56dp) | Leading `arrow-left`; title "{n} pages" `titleXl` (tabular); trailing icons: `download` (export shortcut, optional), `more-horizontal` | | |
| Active page preview | Currently selected page rendered full-width with 24dp horizontal margins, on `surfaceSunken`, max 60% of available vertical space, `radius-md`. Page number badge top-left of image: 24dp `surface` `radius-full` `elev-1` with `labelMd primary` text "3 / 7". | | |
| Carousel strip (96dp tall) | Horizontal scroll of Page Thumbnails (8.8.1), 16dp leading/trailing padding, 12dp gap. Plus a trailing "Add page" tile: 96×128dp dashed 1.5dp `border` outline, `radius-md`, centered `plus` 32dp `textSecondary` + `labelSm textSecondary` "Add page". | | |
| Bottom action row (96dp + safe area) | Leading icon-buttons: Retake (`rotate-ccw`), Filter (`sliders-horizontal`), Delete page (`trash-2`); centered Primary button "Continue" full-width minus the icon row. | | The icon-buttons act on the *currently selected* page. Delete is destructive — opens a confirm dialog. |

**States:**
- Default.
- Reorder: long-press a thumbnail → it lifts (`elev-3`, scale 1.04, drag-shadow); other thumbnails shift `dur-slow` `ease-standard` to make room.
- Single page only: Delete button disabled (`textDisabled`) — can't delete the last page; user must use back button to discard the whole scan.
- Loading (page processing post-crop): the thumbnail shows skeleton until cropped image is rasterized.

**Interactions:**
- Tap thumbnail → set as active page (preview swaps with `dur-default` crossfade).
- Long-press thumbnail → enter reorder mode.
- Tap "Add page" tile → return to Camera (Multi-mode resumes).
- Tap Retake → Camera with current page slot reserved.
- Tap Filter → Screen 12 (Filter applies to all pages by default; per-page override available there).
- Tap Delete page → confirm dialog "Delete page 3?" / [Cancel] [Delete].
- Tap Continue → Screen 13 Save Document.
- Tap `more-horizontal` → bottom sheet: Apply filter to all, Reorder pages (alternative to drag), Cancel scan.
- Pinch on active page preview → 1×–3× zoom; pan; double-tap to reset.

**Transitions:**
- Enter from: Crop (multi) or Camera-multi long-press shutter.
- Exit to: Save Document (slide left).

**Dark mode notes:** the dashed "Add page" border uses `border` token; `surface` carousel cells stay above `bg` by lightness step.

**Ad placement:** None.

**Edge cases:**
- 50+ pages (Snapdoc soft cap is 100): warning snackbar "Large documents may be slow to process."
- Last page deleted via confirm: navigate back to Camera (no pages to review).
- Reorder cancelled (drop outside): snap back `dur-fast`.

---

## Screen 12: Filter / Enhancement Selector

**Purpose:** Apply a visual enhancement mode to the document; tune brightness/contrast.

**Layout (top to bottom):**
| Region | Element | Component | Notes |
|---|---|---|---|
| Status bar | `bg` | | |
| App bar (56dp) | Leading `arrow-left`; title "Filter" `titleXl`; trailing ghost "Apply to all pages" toggle | | "Apply to all pages" is a small toggle inline; off by default if user came from per-page entry, on by default if entered from Multi-Page Review. |
| Page preview | Same chrome as Multi-Page Review's active preview; updates live as filter changes | | |
| Mode selector | 4-option segmented control (8.1.7 extended): Auto / Color / Grayscale / Magic | | Each option label is `labelMd`; the active option's segment is `surface` `elev-1`. |
| Brightness slider | `bodyMd` label "Brightness" leading; slider 8.2.7; trailing tabular value `caption` | | |
| Contrast slider | same pattern | | |
| Bottom action row | Cancel ghost / Done primary | | |

**States:**
- Default: Auto selected, sliders at 0.
- Live preview while sliding: 60fps render; on release, full-resolution rerender (within ~150ms).
- Per-page mode: trailing badge in app-bar shows "Page 3 only".

**Interactions:**
- Tap mode → preview updates `dur-fast`.
- Drag slider → preview updates immediately (debounced GPU pipeline).
- Tap Cancel → revert to original.
- Tap Done → return to Multi-Page Review with applied filter.
- Toggle "Apply to all pages" → reapply current setting across all pages.

**Transitions:**
- Slide left from Multi-Page Review; slide right back.

**Dark mode notes:** preview ground is `surfaceSunken`; the document image's actual brightness reads true regardless of mode.

**Ad placement:** None.

**Edge cases:**
- GPU filter unsupported on device: fall back to CPU pipeline (≤500ms apply); show inline 16dp spinner near "Done" button while processing.

---

## Screen 13: Save Document

**Purpose:** Confirm filename + category before commit; launches OCR + AI categorization in background after save.

**Layout (top to bottom):**
| Region | Element | Component | Notes |
|---|---|---|---|
| Status bar | `bg` | | |
| App bar (56dp) | Leading `arrow-left`; title "Save" `titleXl` | | |
| Content | Form (24dp horizontal padding): | | |
| | Filename label `labelMd textSecondary` + text field 8.2.1, prefilled with auto-suggested filename (date + first OCR words, e.g. "Electricity Bill — Mar 2026") | | |
| | 24dp gap | | |
| | Category label + dropdown 8.2.3 prefilled with auto-detected category (with small `accent` "Auto" badge `labelSm` trailing the value) | | The badge disappears once user manually changes category. |
| | 24dp gap | | |
| | "Password protect (free for everyone)" toggle row 8.2.4. Default OFF. When ON, a password field appears below. | | |
| Below form (16dp gap) | Privacy reassurance line | `caption textTertiary` "Saved to your phone. Not uploaded." with leading 14dp `lock` glyph. | |
| Bottom action row | Cancel ghost / Save primary | | Save 48dp full-width-minus-cancel. |

**States:**
- Default: prefilled filename + auto-detected category visible.
- Saving (between tap and persistence): Save button shows loading state, ~600ms.
- Auto-categorize loading: category dropdown shows "Detecting…" with 14dp spinner inline; user can override at any time.
- Auto-categorize failed (no internet): category falls back to "Other"; snackbar "Couldn't auto-organize. Set category manually."
- Filename empty: Save disabled.
- Filename collides with existing: append "(2)" automatically; show inline `caption` "Renamed to avoid conflict".

**Interactions:**
- Edit filename → live validation.
- Tap category → open dropdown menu.
- Toggle password → show/hide password field; password field uses standard text-field with trailing eye-toggle.
- Tap Save → persist + navigate to Document Detail (Screen 14). After 3rd save in session, fire interstitial ad before Document Detail.

**Transitions:**
- Enter from: Multi-Page Review or Filter.
- Exit to: Document Detail. If interstitial: full-screen ad → on close → Document Detail.

**Dark mode notes:** the `lock` privacy glyph stays `textTertiary`.

**Ad placement:**
- Interstitial after every 3rd save, 60-second cooldown (per spec §5.1).

**Edge cases:**
- No internet at save time: filename + category default to local heuristics; AI re-tries on next foreground if the AI-Summary-suggestion setting is on.
- User cancels mid-save (during ~600ms write): rollback partial files; show snackbar "Save cancelled."

---

## Screen 14: Document Detail (HERO)

**Purpose:** View a saved document; launch all per-document actions.

**Layout (top to bottom):**
| Region | Element | Component | Notes |
|---|---|---|---|
| Status bar | `bg` | | |
| App bar (56dp) | Leading `arrow-left`; title = filename `titleXl`, ellipsis at end; trailing icons `star` (favorite toggle), `more-horizontal` | | |
| Sub-bar (44dp) | Tab bar 8.4.3: Pages / Text / Summary | | The tabs have meaning: Pages = PDF preview, Text = OCR full-text, Summary = AI summary state. |
| Pages tab content | Vertical scroll of page renders, each full-width minus 16dp margins, `radius-md`, 1dp `border`, 12dp gap. Top of stack: page-count caption "12 pages · 2.4 MB" `caption textTertiary` tabular. | | Long-press a page enters single-page actions (export only this page, delete). |
| Text tab content | Full-Text Viewer (Screen 18 inline) | | |
| Summary tab content | If summary cached: AI Summary inline (TL;DR + bullets, no sheet — uses 24dp page padding). If not: empty state with `sparkles` 96dp + Primary "Summarize this document" button. Free tier: button shows "Watch ad → Summarize" with `play-circle` leading. | | |
| Action toolbar (above bottom edge, sticky) | 4 evenly-spaced icon buttons over `surface` 1dp `divider` top: Share `share`, Export `download`, Summarize `sparkles`, Delete `trash-2` | | Each button is icon + label vertical (`labelMd textSecondary`). On primary route (Summarize) the label is `accent` if no summary exists. |

**States:**
- Default per-tab.
- Summary loading: Screen 16 sheet appears.
- Summary cached: tab pre-renders.
- Delete confirm: dialog "Delete '{filename}'? This cannot be undone." [Cancel] [Delete destructive].
- Share/Export route to Screens 19/20.

**Interactions:**
- Swipe left/right on page-render → navigate pages (also paginates). Pinch-zoom up to 4×.
- Tap `star` → favorite toggle, animation: outline → fill, scale 1 → 1.2 → 1, color `accent`.
- Tap `more-horizontal` → bottom sheet: Rename, Move to category, Show file info, Print.
- Tap Share → Screen 19.
- Tap Export → Screen 20.
- Tap Summarize → if cached, scroll Summary tab into view; else Screen 17 (free) or Screen 16 (paid) directly.
- Tap Delete → confirm dialog → on Delete: animate card out, return to Home, show snackbar "Deleted '{filename}'. Undo".

**Transitions:**
- Enter from: Home List (shared-element thumbnail → first page) or Save flow (slide left).
- Exit to: Summary sheet (bottom sheet `dur-default`); Share sheet; Export modal.

**Dark mode notes:** PDF page renders show their actual ink — pages do not invert. The page card has no border in dark mode (relies on `elev-1` shadow against `bg`).

**Ad placement:** None on Document Detail itself; rewarded ad fires on Summary intent (free tier).

**Edge cases:**
- File missing on disk (storage moved/cleared): show error state container with "This document file is missing. The metadata is still here, but the PDF cannot be opened. [Remove from library]".
- Very large PDF (>50MB): paginated rendering; loading per-page spinner inline.

---

## Screen 15: AI Summary Bottom Sheet (HERO)

**Purpose:** Display the generated TL;DR + bullets, allow regeneration.

**Layout (top to bottom):**
| Region | Element | Component | Notes |
|---|---|---|---|
| Scrim | `scrim` token over Document Detail | | |
| Sheet (full-sheet 90% height) | Drag handle 32×4dp `divider` | | |
| Header row (8.8.5) | Leading `sparkles` 16dp `accent` + "AI Summary" `headlineMd`; trailing ghost "Regenerate" + icon-only `x` | | |
| Privacy pill | bg `surfaceVariant`, padding 8dp horizontal 6dp vertical, `radius-full`, leading 12dp `lock` `textSecondary`, `labelSm textSecondary` "Generated from your document text — not stored on a server." | | |
| TL;DR section | `overline` "TL;DR" + 8dp gap + `bodyLg textPrimary` 1-line summary (allowed to wrap to 2 lines on long docs). | | |
| 24dp gap | | | |
| Bullets section | `overline` "KEY POINTS" + 8dp gap + 3–5 bullet rows. Each bullet: 4dp circle `accent` + 12dp gap + `bodyLg textPrimary`. | | |
| 24dp gap | | | |
| Footer row | Two ghost buttons: "Copy" (`copy`) and "Share summary" (`share`) | | Inline horizontally, 12dp gap. |
| Bottom safe-area | 24dp padding | | |

**States:**
- Default: summary rendered.
- Regenerating: TL;DR and bullets replaced with skeleton bars (8.5.6); regenerate button shows loading state.
- Stale (model version updated since cache): small `caption textTertiary` "Cached 8 days ago" between header and TL;DR; tapping it triggers regenerate.

**Interactions:**
- Drag handle down → dismiss sheet.
- Tap "Regenerate" → re-fetch (free tier triggers Screen 17 first).
- Tap "Copy" → copy summary text to clipboard, snackbar "Summary copied."
- Tap "Share summary" → Android share sheet with text payload.
- Tap `x` → dismiss.

**Transitions:**
- Enter: bottom sheet in `dur-default` `ease-emphasized`; scrim fades 0 → 0.45.
- Exit: out `dur-default` `ease-accelerate`.

**Dark mode notes:** privacy pill bg uses `surfaceVariant` for the mode; `accent` lifts to `D97706`.

**Ad placement:** None on the sheet itself. Rewarded ad gates entry for free users (Screen 17).

**Edge cases:**
- Empty doc (no OCR text): show "We couldn't find readable text on this document. Try retaking pages with better lighting." Single Secondary "Open scanner" button.

---

## Screen 16: AI Summary Loading State

**Purpose:** Show progress while Gemini Flash call resolves (~1–4s typical).

**Layout (top to bottom):**
| Region | Element | Component | Notes |
|---|---|---|---|
| Scrim + sheet | Same chrome as Screen 15 | | |
| Header | Same | | |
| Body | Vertical centered: 64dp animated `sparkles` glyph (color `accent`, opacity pulses 60%↔100% over 1200ms `ease-linear`); 24dp below: `bodyLg textPrimary` rotating string (crossfade `dur-fast` every 1200ms): "Reading your document…" → "Finding the main points…" → "Writing the summary…"; 16dp below: `caption textTertiary` privacy pill same as Screen 15. | | |
| Bottom | Optional Secondary "Cancel" button if op exceeds 4s | | |

**States:**
- 0–4s: animation rotates strings.
- >4s: cancel button appears.
- >12s: timeout — switch to error state in-place: glyph swaps to `wifi-off` 64dp `error`; message "Couldn't reach the AI service. Check your connection."; Primary "Try again" + Ghost "Cancel".

**Interactions:**
- Tap Cancel → abort request, dismiss sheet, snackbar "Summary cancelled."
- Drag handle down → same as Cancel.

**Transitions:**
- Same as Screen 15 (the sheet stays mounted; only its inner content swaps as the response arrives — crossfade `dur-default`).

**Dark mode notes:** `sparkles` glyph color stays `accent`.

**Ad placement:** None directly. The rewarded ad (Screen 17) precedes this loading state for free-tier users.

**Edge cases:**
- Network drops mid-request: timeout to error state.
- API quota exceeded (rare): error state with "Service is busy. Try again in a moment."

---

## Screen 17: Rewarded Ad Modal

**Purpose:** Free-tier gate before AI Summary call.

**Layout (top to bottom):**
| Region | Element | Component | Notes |
|---|---|---|---|
| Scrim | `scrim` | | |
| Sheet (8.7.3) | half-sheet | | |
| Header | `sparkles` 16dp `accent` + "Generate AI Summary" `headlineMd`; trailing `x` | | |
| Body | Two-line copy: line 1 `bodyLg textPrimary` "Watch a short ad to generate this summary."; line 2 `bodyMd textSecondary` "Or remove ads forever for ₹99 / $1.49 once." | | |
| Action stack | Primary "Watch ad — 15s" with leading `play-circle`; Secondary "Remove ads — ₹99"; Ghost centered "Maybe later" | | |
| Bottom safe area | 24dp | | |

**States:**
- Default.
- Loading ad (after Watch ad tap): spinner replaces button label "Loading ad…" 1–3s.
- Ad failed to load: snackbar "Ad couldn't load. Try again?" + retry; Remove Ads button promoted.
- Ad watched ≥80%: dismiss sheet → Screen 16 loading.
- Ad skipped early: snackbar "Watch the full ad to unlock summary."

**Interactions:**
- Watch ad → AdMob rewarded video → on completion, advance.
- Remove ads → Screen 24 Paywall.
- Maybe later / `x` → dismiss.

**Transitions:**
- Half-sheet in/out `dur-default`.
- After ad: video player fades out, half-sheet fades out, Screen 16 fades in (cross-sequenced).

**Dark mode notes:** none beyond standard sheet behavior.

**Ad placement:** This screen IS the rewarded-ad surface.

**Edge cases:**
- Offline: error inline "No internet. AI Summary requires a connection." Primary "Try again" / Ghost "Close".
- User has Remove Ads: this screen is never shown; goes straight to Screen 16.

---

## Screen 18: Full-Text Viewer

**Purpose:** Show OCR-extracted plain text from a document; allow copy and search.

**Layout (top to bottom):**
| Region | Element | Component | Notes |
|---|---|---|---|
| Surface | If reached as a tab inside Document Detail: inline. If reached as standalone (deep-link): own screen with app-bar "Extracted text" + back. | | |
| Top utility row | Search field 8.2.2 (placeholder "Find in this document"); trailing ghost "Copy all" `labelLg accent` | | |
| Body (scroll) | Plain text in `bodyLg textPrimary`, 16dp horizontal padding, line height per type token. Matches highlighted via 8.8.4 bounding-box style. | | |
| Status footer | `caption textTertiary` "Extracted by on-device OCR · 3 pages" | | |

**States:**
- Default.
- No text (OCR found nothing): empty state "No readable text on these pages."
- Searching: matched substrings get `accent` highlight bg `radius-sm`.
- Copy all: toast "Text copied to clipboard."

**Interactions:**
- Tap "Copy all" → clipboard + snackbar.
- Long-press text → text selection cursor; standard Android selection toolbar.
- Type in search → highlight matches; "Next match" / "Prev match" nav arrows appear in field trailing.

**Transitions:**
- As tab: instant (tab change).
- As screen: slide left from Document Detail.

**Dark mode notes:** highlight bg `accent` at 12% works in both modes; matched text remains `textPrimary`.

**Ad placement:** None.

**Edge cases:**
- Mixed-script text: rendered with proper script fallbacks (Geist Latin + Noto Devanagari etc.).
- Very long extraction: virtualized scroll.

---

## Screen 19: Share Sheet

**Purpose:** Choose share format and target.

**Layout (top to bottom):**
| Region | Element | Component | Notes |
|---|---|---|---|
| Bottom sheet 8.5.4 (half) | Drag handle | | |
| Header | "Share" `headlineMd` + trailing `x` | | |
| Format selector | Segmented control: PDF / Images / Text | | Default: PDF. Text option enabled only if OCR text exists. |
| Quick targets row | Horizontal scroll of icon-buttons (each 56×56dp + label below): WhatsApp (custom green icon, `radius-full`), Email (`mail`), Copy link (`link`), Save to Files (`folder`), More (`more-horizontal`) | | The WhatsApp button is high-emphasis per India market; uses the actual WhatsApp green inside our chrome. |
| Below targets | "Share via…" Secondary button full-width — opens Android system share sheet | | |
| Bottom safe area | 24dp | | |

**States:**
- Default.
- Generating PDF (if not cached): inline progress bar above the targets row, `bodyMd textSecondary` "Preparing PDF…".
- WhatsApp not installed: that icon tile shows `textDisabled`; tap shows snackbar "WhatsApp not installed."

**Interactions:**
- Tap format → switch format silently.
- Tap target → invoke share intent for that target.
- Tap "Share via…" → system share.

**Transitions:**
- Sheet in/out `dur-default`.

**Dark mode notes:** WhatsApp green stays `#25D366` in both modes.

**Ad placement:** None.

**Edge cases:**
- Multi-select share (entered from Screen 21): Header reads "Share 3 documents". Format defaults to ZIP for >1 doc.

---

## Screen 20: Export Options

**Purpose:** Choose export quality, page size, optional password before producing the file.

**Layout (top to bottom):**
| Region | Element | Component | Notes |
|---|---|---|---|
| Bottom sheet (half) | | | |
| Header | "Export" `headlineMd` + trailing `x` | | |
| Form | Format radio group: PDF / JPEG / PNG / ZIP of images. 16dp gap. | | |
| | Quality segmented control: Low / Medium / High. Default Medium. Below the control: `caption textTertiary` "Estimated size: 2.4 MB" tabular, updates live. | | |
| | Page size dropdown: Auto / A4 / Letter / Legal. Default Auto. | | |
| | Password protect toggle (PDF only). When ON, password field appears with strength meter (4dp tall, `error` → `warning` → `success` segments). | | |
| Action | Primary "Export" full-width | | |

**States:**
- Default.
- Computing estimate: `caption` "Calculating size…" with inline 12dp spinner.
- Export running: Primary button → loading state with progress bar at bottom edge of sheet (4dp `primary`).
- Export complete: snackbar "Saved to Files / Downloads / Share" with Action "Open" routing to system file viewer.

**Interactions:**
- Adjust any option → estimate recalculates with 250ms debounce.
- Tap Export → run; on complete, dismiss sheet and show snackbar.

**Transitions:**
- Sheet in/out `dur-default`.

**Dark mode notes:** strength meter colors keep their semantic tokens.

**Ad placement:** None.

**Edge cases:**
- Storage low: warning row above form "Storage low — exports may fail." `warning` text.
- Password set but field empty: Export disabled.

---

## Screen 21: Multi-Select Mode

**Purpose:** Select multiple documents on Home / Categories list to perform batch ops.

**Layout (top to bottom):**
| Region | Element | Component | Notes |
|---|---|---|---|
| Status bar | `bg` | | |
| App bar (56dp, replaces standard top-bar) | Leading `x` (exit multi-select); title "{n} selected" `titleXl` (tabular); trailing ghost "Select all" / "Deselect all" | | |
| Content | Same list as Screen 5/7 but every Document Card shows a 24dp circular checkbox top-right corner; tapping a card toggles selection (no detail open). Selected cards have 2dp `primary` border. | | |
| Banner ad | hidden — multi-select replaces normal home chrome | | |
| Bottom action bar (replaces bottom nav) | 4 action icon-buttons evenly distributed: Share `share`, Move to category `folder`, Export `download`, Delete `trash-2` (icon `error`); each with `labelMd` below. | | Sticky 64dp + safe area, surface `surface`, top edge 1dp `divider`. |

**States:**
- 1+ selected: action bar enabled.
- 0 selected (after deselect): action bar all `textDisabled`; alternative — auto-exit multi-select after 0-state for 2s with snackbar undo.
- Move loading / Delete confirm dialog standard.

**Interactions:**
- Tap any card → toggle select.
- Tap Select all → select every visible card.
- Tap action → fire batch op (Share → Screen 19 in multi mode; Move → category picker dialog; Delete → confirm dialog).
- Tap `x` → exit multi-select.

**Transitions:**
- Enter: from Home, app-bar fades from "snapdoc" to "1 selected" `dur-fast`; bottom nav swaps for action bar `dur-default` slide up; checkboxes fade in `dur-default`.
- Exit: reverse.

**Dark mode notes:** selected border `primary` is light in dark mode (off-white) — high contrast.

**Ad placement:** Hidden in multi-select.

**Edge cases:**
- 50+ selected: Delete confirm shows count "Delete 73 documents?" with red emphasis.
- User backgrounds in multi-select: state preserved on return.

---

## Screen 22: Settings

**Purpose:** All app-level preferences.

**Layout (top to bottom):**
| Region | Element | Component | Notes |
|---|---|---|---|
| Status bar | `bg` | | |
| App bar (56dp) | Title "Settings" `titleXl` | | |
| Content (scroll) | Sections separated by 24dp gaps + 11sp `overline` headers: | | |
| | **SCAN** | | |
| | Default mode (dropdown: Auto/Color/Grayscale/Magic) | settings row | |
| | Default page size (dropdown: Auto/A4/Letter/Legal) | settings row | |
| | Auto-OCR (toggle) | settings row | |
| | Auto-suggest AI summary (toggle) | settings row | |
| | **CATEGORIES** | | |
| | Manage categories (chevron) | settings row | Routes to Screen 23. |
| | **PURCHASES** | | |
| | Remove ads (chevron with trailing tabular price `labelLg accent`) | settings row | Or "Active" `success` if purchased. Routes to Screen 24. |
| | Restore purchase (chevron) | settings row | Spinner during operation; snackbar on result. |
| | **APP** | | |
| | Theme (dropdown: System / Light / Dark) | settings row | |
| | Language (dropdown: System default + 8 Indic + English) | settings row | |
| | **ABOUT** | | |
| | Privacy Policy (chevron, external link icon) | settings row | Routes to Screen 25. |
| | Rate Snapdoc (chevron, external) | settings row | |
| | Share Snapdoc (chevron, external) | settings row | |
| | Version (read-only, value `caption textTertiary` tabular "1.0.0 (build 123)") | settings row | |
| Banner ad | bottom 50–60dp | | |
| Bottom nav | Settings active | | |

**States:**
- Default.
- Restore loading: row shows spinner.
- Toggle change: instant; non-blocking persistence.

**Interactions:**
- Standard for each row.

**Transitions:**
- Tab switch from Home/Categories: instant.
- Sub-screens: slide left.

**Dark mode notes:** when "Theme" is changed, the entire app crossfades `dur-slow` to the new mode.

**Ad placement:** Banner.

**Edge cases:**
- Locale change: app re-renders all strings; settings state preserved.
- Purchase already active: "Remove ads" row shows "Active" + leading `check` `success`.

---

## Screen 23: Manage Categories

**Purpose:** Add / rename / delete custom categories.

**Layout (top to bottom):**
| Region | Element | Component | Notes |
|---|---|---|---|
| Status bar | `bg` | | |
| App bar (56dp) | Leading `arrow-left`; title "Manage categories" `titleXl`; trailing ghost "Add" with `plus` 16dp + `labelLg accent` | | |
| Content | Two sections separated 24dp: **DEFAULTS** (read-only — Bills, IDs, Receipts, Notes, Contracts, Other) and **CUSTOM** (user-created). Each row: leading 20dp dot in the category's strip color, name `bodyLg`, count `caption textTertiary` tabular. Custom rows have trailing ghost `more-horizontal`. Defaults: trailing `lock` 16dp `textDisabled`. | | |
| Bottom 24dp | If custom is empty: inline empty-state "No custom categories yet." `bodyMd textSecondary` centered + 8dp gap + Primary "Create category". | | |

**States:**
- Default.
- Adding: bottom sheet "New category" with name field + 8 color swatches (the §2.4 palette plus 2 alt hues); Primary "Create".
- Renaming: same sheet prefilled.
- Deleting: confirm "Delete '{name}'? Documents in this category will move to Other."

**Interactions:**
- Tap Add or Create → bottom sheet.
- Tap row `more-horizontal` → menu (Rename, Set color, Delete).
- Tap default category row → no-op (defaults are read-only); toast "Default categories can't be edited."

**Transitions:**
- Slide left from Settings.
- Sheet `dur-default`.

**Dark mode notes:** default `lock` glyph stays `textDisabled`.

**Ad placement:** None.

**Edge cases:**
- 30+ custom categories: warning before Create "You have many custom categories. Consider consolidating for a cleaner library."
- Name collision: validation in sheet "A category with this name exists."

---

## Screen 24: Remove Ads Paywall (HERO)

**Purpose:** Sell the one-time IAP.

**Layout (top to bottom):**
| Region | Element | Component | Notes |
|---|---|---|---|
| Status bar | `bg` | | |
| App bar (56dp) | Leading `x`; no title; trailing ghost "Restore" `labelLg accent` | | |
| Content (centered, 24dp horizontal padding) | Hero card 8.7.4 | | |
| | 48dp `lock` `textPrimary` | | |
| | 24dp gap | | |
| | "Remove ads" `display` | | |
| | 8dp gap | | |
| | "₹99 once. Forever." `headlineLg textSecondary` (tabular) | | |
| | 32dp gap | | |
| | Benefits (3 rows, 16dp gap): "No banners on Home" / "No interstitials between scans" / "Unlimited AI summaries — no ad gate." Each: 20dp `check` `success` + `bodyLg textPrimary`. | | |
| | 32dp gap | | |
| | Primary "Buy — ₹99" full-width | | Locale: shows ₹99 (India) / $1.49 (other) using IETF detected currency from Play. |
| | 12dp gap | | |
| | Ghost "Restore purchase" full-width | | |
| Below card (24dp gap) | `caption textTertiary` "One-time purchase. No subscription. Linked to your Google Play account." | | |
| Bottom safe area | 32dp | | |

**States:**
- Default.
- Buying: Primary loading state; full-screen 60% scrim while Play Billing is open.
- Purchase success: confetti restraint — a brief 200ms scale-up of the `check` glyph from each benefit (sequential 80ms each), then full-screen success view: 96dp `check-circle` `success` + `headlineXl` "You're set." + Primary "Done" → routes back to wherever paywall was opened from.
- Purchase cancelled: snackbar "Purchase cancelled."
- Restore success: snackbar "Purchase restored."
- Restore no-purchase: dialog "We couldn't find a previous purchase on this Play account."

**Interactions:**
- Tap Buy → Play Billing.
- Tap Restore → Play Billing query.
- Tap `x` → dismiss; snackbar if user tapped Buy and cancelled mid-flow.

**Transitions:**
- Enter from: Settings, rewarded modal "Remove ads instead" link, post-5-interstitial trigger — all use slide up `dur-slow` `ease-emphasized`.
- Exit: slide down `dur-default` or reverse.

**Dark mode notes:** the hero card's `elev-3` shadow reads strongly against `bg`.

**Ad placement:** None.

**Edge cases:**
- Play services unavailable: dialog "Google Play required. Update Play services to continue."
- Network error mid-purchase: Play handles; we show snackbar on return.
- Already purchased on this account: "Buy" replaced by `success` row "Active on this account" + `check`. Ghost "Restore" replaced by ghost "Manage subscription" → opens Play.

---

## Screen 25: About / Privacy Policy

**Purpose:** App version, the privacy stance verbatim, links.

**Layout (top to bottom):**
| Region | Element | Component | Notes |
|---|---|---|---|
| Status bar | `bg` | | |
| App bar (56dp) | Leading `arrow-left`; title "About" `titleXl` | | |
| Content | Centered hero (24dp horizontal): | | |
| | Combo logo (symbol + wordmark) at 96dp wide | | |
| | 8dp gap | | |
| | Version `caption textTertiary` tabular "Version 1.0.0 · Build 123" | | |
| | 32dp gap | | |
| Body sections (left-aligned) | "OUR PRIVACY STANCE" `overline` + 8dp + `bodyLg textPrimary` "We don't have a server. Your scans never leave your phone unless you ask AI to summarize them — and even then, only the typed-out text is sent, never the image." Quoted block: 1dp `border` left edge, 16dp inset padding, italic. | | |
| | 24dp gap | | |
| | Settings rows (links): "Read full privacy policy" (external `external-link` icon trailing) / "Terms of use" / "Open-source licenses" / "Contact support" | | |
| Bottom 32dp | `caption textTertiary` centered "Made for privacy. Designed in India and for the world." | | |

**States:** static.

**Interactions:**
- Tap any external link → in-app web view (Custom Tabs).
- Tap "Open-source licenses" → in-app screen with scrollable license list.

**Transitions:**
- Slide left from Settings.

**Dark mode notes:** the quoted-block left border uses `border` token.

**Ad placement:** None.

**Edge cases:** none material.

---

## Screen 26: Error States (system-wide patterns)

**Purpose:** Consistent error treatment across the app. This is a *pattern* spec, not a single screen — it specifies how errors look anywhere they occur.

### 26.1 OCR Failed

**Where:** Document Detail Text tab; can also surface as snackbar after save if OCR job fails.
**Layout:** Error state container (8.6.4) with glyph `text` 96dp `textTertiary` (de-emphasized — not red, OCR failure is not catastrophic), title "We couldn't read this document.", body "OCR may need clearer scans. You can still view, share, or summarize using just the images.", Secondary button "Retry OCR" + Ghost "Skip".
**States:** retry running shows spinner inline.
**Edge cases:** repeated failure (3×) hides Retry button, keeps Skip — silently records to crashlytics.

### 26.2 AI Network Error

**Where:** AI Summary loading (>12s) or after explicit failure response.
**Layout:** within the bottom sheet, see Screen 16 timeout state.
**Specific copy:**
- Connection issue: "Couldn't reach the AI service. Check your connection."
- Quota: "Service is busy. Try again in a moment."
- Bad response: "Something went wrong on our side. Try regenerating."
**States:** Primary "Try again" / Ghost "Cancel".

### 26.3 No Internet (general)

**Where:** anytime an internet-required action is attempted offline.
**Layout:** snackbar persistent (no auto-dismiss): bg `error`, fg `#FFFFFF`, leading 16dp `wifi-off`, "No internet — AI features need a connection.", action "Retry" once connection returns; auto-dismiss 2s after recovery.

### 26.4 Low Storage

**Where:** before Camera entry, before Save, before Export. Triggered when free space <100MB.
**Layout:** dialog: title "Storage low", body "Snapdoc needs at least 100 MB free to scan. You have {n} MB left.", Primary "Open settings" (opens Android Storage panel), Ghost "Dismiss".
**Edge cases:** during a multi-page scan, if storage drops below 50MB mid-flow, show full-screen takeover, save partial scan as draft, return to Home.

### 26.5 Permission Denied

**Where:** anywhere that requires camera or storage permission.
**Layout:** error state container with glyph `lock` 96dp `textTertiary`, title "Camera access required", body "Snapdoc needs your camera to scan documents. We don't see anything else — only what you point it at.", Primary "Open settings", Ghost "Cancel".

### 26.6 Cloud / IAP Errors

**Where:** Paywall flows.
**Layout:** dialog or snackbar per Screen 24.

### 26.7 General fallback ("Something went wrong")

**Where:** unexpected exceptions caught at boundary.
**Layout:** error state container with glyph `alert-triangle` 96dp `error`, title "Something went wrong.", body "We've logged this. Try again, and if it keeps happening, contact us from Settings.", Primary "Try again" / Ghost "Go home".

**Transitions:** error states fade in over current content `dur-fast`. Snackbars per 8.5.1.

**Dark mode notes:** error glyph reads `error` token (slightly muted in dark — `#F87171`); dialog bg uses `surface`.

**Ad placement:** Hidden during full-screen errors.

**Edge cases:** `something went wrong` is the fallback — copy is intentionally unblamed. We do not say "Network error" or "Server error" because we don't have a server.

---

*End of Deliverable 2 — `03_screen_specs.md`. Continuing into Deliverable 3.*
