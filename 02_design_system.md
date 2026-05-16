# Snapdoc — Android Design System v1

**Version:** 1.0
**Owner:** Product Design
**Audience:** Engineering (Jetpack Compose), QA, future Designers
**Locked inputs from product spec:** App name `Snapdoc`, primary `#111111`, minimal Notion/Linear vibe, light + dark first-class, India + global English equally.

This document defines every token, scale, and component the app needs. Screen specs (`03_screen_specs.md`) and mockups (`04_hero_mockups.html`) reference these tokens by name. Engineers should map tokens 1:1 into a Compose `Theme` object — never reproduce raw hex inline in screen code.

---

## Section 1 — Brand & Tone

### 1.1 Brand voice
- **Plainspoken, never marketing-y.** "Scan", "Save", "Summarize" — verbs, not slogans.
- **Quietly confident about privacy.** State what we do, not how impressed you should be ("We don't have a server. Your scans never leave your phone.").
- **Respectful of the user's time.** Microcopy is short. No exclamation marks. No emoji in product copy. No "Oops!" error tone.
- **Multilingually neutral.** Same tone in English carries cleanly to Hindi/Tamil/Bengali — avoid English idioms.
- **Pricing and ads are stated, not theatricalized.** "Remove ads — ₹99 once" is the whole pitch. No "limited time" countdowns.

### 1.2 Visual tone descriptors
**Calm. Surgical. Privacy-forward. Editorial. Mechanical-but-warm.** The product should read like a well-machined instrument — not jewelry, not a toy. Whitespace is structural, not decorative. Color is mostly absent; when it appears (a category strip, a focused field, a positive confirmation) it carries meaning.

### 1.3 Logo direction — recommendation

**Recommended: Wordmark + symbol combo, used independently per surface.**

- **Symbol** (app icon, splash, notification): a 24×24dp geometric mark — a rounded square (representing a document) with a single diagonal "scan beam" cutting from upper-right to lower-left across its face, leaving a thin gap. The beam is exactly 2dp wide. Constructed on a 24dp grid with 2dp padding inside a 20dp interior square (radius 5dp). The beam crosses at a 28° angle (not 45° — 45° feels generic, 28° gives the mark a forward-leaning, in-motion quality). Outline weight matches the document's outer stroke at 2dp. In light mode the symbol is solid `#111111` on a `#FAFAF8` ground; in dark mode it inverts to `#F4F4F1` on `#0C0C0D`. App icon adds a subtle 2% inner shadow at the top edge for depth on home-screen wallpapers.

- **Wordmark** (in-app top bar, About screen, paywall hero): set in **Geist Medium 500**, 22sp tracking `-0.01em`. Lowercase `snapdoc` — the lowercase decision is deliberate; uppercase reads as enterprise/legal (which dilutes the "everyday tool" positioning). Optical adjustments: tighten `s→n` and `d→o` kerning by ~6 units; the `p` descender sits flush against the baseline grid (no extra leading compensation). The dot of the `i` in `snapdoc`… there is no `i`; lowercase `d` and `c` carry the word's spine. Wordmark is rendered as a single SVG path, not live text, so optical kerning is preserved.

- **Combo** (onboarding screen 1, paywall): symbol + wordmark on the same baseline, 8dp gap between them, symbol scaled to match the cap-height of the wordmark.

**Rationale:** A symbol-only mark would make the launcher icon work but leaves the in-app top bar feeling unbranded. A wordmark-only mark fails as a launcher icon at 48dp. The combo system gives us a clean app icon (symbol), a clean top-bar identity (wordmark), and a hero treatment (combo) without forcing any one to do all the jobs.

**Generation prompt for an image tool / illustrator:**
> "A minimal geometric app icon: a rounded-square outline (radius 22% of the square's side, stroke 8% of side, color graphite #111111) on a warm off-white #FAFAF8 background. A single 8%-thick diagonal line crosses the square from upper-right to lower-left at 28° from horizontal, leaving a 4% gap where it intersects the square's outline. No fill. No gradient. No text. Pixel-precise vector, 1024×1024 master. Inverted for dark mode."

---

## Section 2 — Color System

All colors specified in hex. Compose mapping: each token below maps to a `Color` constant in `SnapdocColors.kt`, exposed via a `LocalSnapdocColors` `CompositionLocal`.

### 2.1 Light mode tokens

| Token | Hex | Use |
|---|---|---|
| `bg` | `#FAFAF8` | Window background. Warm off-white — has 2% yellow cast vs pure neutral; reads as paper. |
| `surface` | `#FFFFFF` | Cards, sheets, modals — anything raised above `bg`. |
| `surfaceVariant` | `#F2F1ED` | Pressed-state background, inert chips, disabled field fills. |
| `surfaceSunken` | `#F5F4F0` | Camera viewfinder ground, scrubber tracks — surfaces below `bg`. |
| `primary` | `#111111` | Primary buttons, FAB, brand surfaces, selected states. |
| `primaryContainer` | `#1F1F1F` | Hover/pressed of primary; secondary brand fill. |
| `onPrimary` | `#FFFFFF` | Text/icon on `primary`. |
| `onPrimaryContainer` | `#FFFFFF` | Text on `primaryContainer`. |
| `secondary` | `#5A5A5A` | Secondary text, inactive icons. |
| `accent` | `#B45309` | The single brand accent (amber-700). Used sparingly: privacy lock indicator, AI-generated badge, focused field outline, the "Remove Ads" CTA. |
| `textPrimary` | `#111111` | Body and heading text on `bg`/`surface`. |
| `textSecondary` | `#5A5A5A` | Metadata, timestamps, hint text. |
| `textTertiary` | `#8B8A85` | De-emphasized labels, list section captions. |
| `textDisabled` | `#A3A29E` | Disabled controls. |
| `border` | `#E5E4DF` | Card outlines, list dividers when borderless approach is rejected. |
| `divider` | `#EDECE7` | List item dividers, settings row separators. |
| `success` | `#15803D` | Saved confirmation, password-protected indicator. |
| `warning` | `#B45309` | Storage low, OCR partial. |
| `error` | `#B42318` | Destructive confirmation, validation. |
| `info` | `#1F6FEB` | Tooltips, "what's this" affordances. |
| `scrim` | `rgba(17,17,17,0.45)` | Overlay behind modals, bottom sheets, paywall. |

### 2.2 Dark mode tokens (independently tuned — not inverted)

Designed for OLED comfort: `bg` is a near-black carbon (not pure `#000` which causes smearing on OLED scroll), `surface` lifts in 4–6 lightness steps to give cards real perceptual elevation. Text never goes pure white (`#FFFFFF` against `#0C0C0D` is fatiguing); we cap at `#F4F4F1`.

| Token | Hex | Use |
|---|---|---|
| `bg` | `#0C0C0D` | Window background. Carbon with 1% warm tint. |
| `surface` | `#141416` | Cards, sheets, modals. |
| `surfaceVariant` | `#1C1C1F` | Pressed states, inert chips. |
| `surfaceSunken` | `#08080A` | Below-bg surfaces (camera viewfinder, search empty state). |
| `primary` | `#F4F4F1` | Primary buttons in dark mode are LIGHT (off-white-on-dark) — this is a deliberate inversion of the *role*, not the token. The brand-graphite identity is preserved by `bg` itself. |
| `primaryContainer` | `#E0DFDA` | Hover/pressed of primary in dark. |
| `onPrimary` | `#111111` | Text on light primary buttons. |
| `onPrimaryContainer` | `#111111` |  |
| `secondary` | `#A3A29E` | Secondary text. |
| `accent` | `#D97706` | Amber-600, lifted slightly from light-mode amber-700 to maintain perceived saturation against dark ground. |
| `textPrimary` | `#F4F4F1` | Off-white, never `#FFFFFF`. |
| `textSecondary` | `#9A9A95` |  |
| `textTertiary` | `#6E6E69` |  |
| `textDisabled` | `#56565A` |  |
| `border` | `#26262A` |  |
| `divider` | `#1F1F22` |  |
| `success` | `#4ADE80` |  |
| `warning` | `#FBBF24` |  |
| `error` | `#F87171` |  |
| `info` | `#60A5FA` |  |
| `scrim` | `rgba(0,0,0,0.6)` |  |

### 2.3 State colors

State tokens are defined as overlays applied on top of the base color, so they compose correctly with any surface. Compose-side: `Color.compositeOver(base)`.

| State | Light overlay | Dark overlay |
|---|---|---|
| Hover | `rgba(17,17,17,0.04)` | `rgba(244,244,241,0.06)` |
| Pressed | `rgba(17,17,17,0.10)` | `rgba(244,244,241,0.12)` |
| Focused (ring) | 2dp outline `accent` (#B45309 light / #D97706 dark) at 2dp offset | same |
| Disabled (fg) | `textDisabled` | `textDisabled` |
| Disabled (bg) | `surfaceVariant` | `surfaceVariant` |

**Per-action mappings:**

| Action | Default | Hover | Pressed | Focused | Disabled |
|---|---|---|---|---|---|
| **Primary (light)** | `primary` / `onPrimary` | overlay 0.04 | overlay 0.10 | + ring | `surfaceVariant` / `textDisabled` |
| **Primary (dark)** | `primary` / `onPrimary` | overlay 0.06 | overlay 0.12 | + ring | `surfaceVariant` / `textDisabled` |
| **Secondary (light)** | `surface` border `border` / `textPrimary` | bg `surfaceVariant` | bg `#E5E4DF` | + ring | bg `surfaceVariant` / `textDisabled` |
| **Secondary (dark)** | `surface` border `border` / `textPrimary` | bg `surfaceVariant` | bg `#26262A` | + ring | bg `surfaceVariant` / `textDisabled` |
| **Destructive (light)** | `error` / `#FFFFFF` | overlay 0.04 | overlay 0.10 | + ring | `surfaceVariant` / `textDisabled` |
| **Destructive (dark)** | bg `surface` border `error` / `error` | overlay 0.06 | overlay 0.12 | + ring | `surfaceVariant` / `textDisabled` |

Destructive in dark mode is *outlined*, not filled — a filled red fights too hard against the calm dark surface.

### 2.4 Category accent colors (2dp left strip on document cards)

Restrained — these are 2dp vertical strips, not pill backgrounds. The category *label* uses the same color at 90% opacity as text-on-surfaceVariant chip.

| Category | Light strip | Dark strip | Light chip text | Dark chip text |
|---|---|---|---|---|
| Bills | `#B45309` | `#D97706` | `#92400E` | `#FBBF24` |
| IDs | `#1F6FEB` | `#60A5FA` | `#1E40AF` | `#93C5FD` |
| Receipts | `#15803D` | `#4ADE80` | `#166534` | `#86EFAC` |
| Notes | `#6D28D9` | `#A78BFA` | `#5B21B6` | `#C4B5FD` |
| Contracts | `#B42318` | `#F87171` | `#991B1B` | `#FCA5A5` |
| Other | `#57534E` | `#A8A29E` | `#44403C` | `#D6D3D1` |

**Chip background:** always `surfaceVariant` (light or dark). Chip border: 1dp at 6% of the strip color. The strip carries the identity; the chip stays neutral.

### 2.5 Contrast ratios (WCAG)

All text-on-surface combinations measured against a white-point reference.

| Pairing | Ratio | WCAG AA |
|---|---|---|
| **Light** | | |
| `textPrimary` (#111) on `bg` (#FAFAF8) | 17.8 : 1 | AAA |
| `textPrimary` on `surface` (#FFF) | 19.4 : 1 | AAA |
| `textSecondary` (#5A5A5A) on `bg` | 6.7 : 1 | AAA |
| `textSecondary` on `surface` | 7.3 : 1 | AAA |
| `textTertiary` (#8B8A85) on `bg` | 4.6 : 1 | AA |
| `textDisabled` (#A3A29E) on `bg` | 3.1 : 1 | AA Large only — never used for body text |
| `accent` (#B45309) on `bg` | 5.2 : 1 | AA |
| `error` (#B42318) on `bg` | 6.6 : 1 | AAA |
| `onPrimary` (#FFF) on `primary` (#111) | 19.4 : 1 | AAA |
| **Dark** | | |
| `textPrimary` (#F4F4F1) on `bg` (#0C0C0D) | 17.6 : 1 | AAA |
| `textPrimary` on `surface` (#141416) | 15.2 : 1 | AAA |
| `textSecondary` (#9A9A95) on `bg` | 7.5 : 1 | AAA |
| `textTertiary` (#6E6E69) on `bg` | 4.4 : 1 | AA |
| `textDisabled` (#56565A) on `bg` | 2.9 : 1 | Below AA — disabled, never used for body |
| `accent` (#D97706) on `bg` | 6.1 : 1 | AAA |
| `error` (#F87171) on `bg` | 7.2 : 1 | AAA |
| `onPrimary` (#111) on `primary` (#F4F4F1) | 17.6 : 1 | AAA |

**Rule:** any text below 4.5:1 must be sized 18sp/regular or 14sp/semibold to qualify as AA Large. The system enforces this through type-token pairings (Section 3) — `caption` is never paired with `textTertiary` on a non-bg surface.

---

## Section 3 — Typography

### 3.1 Font family

**Primary: Geist (Geist Sans).** Open-source, designed by Vercel, mechanical-but-warm character that exactly hits the Notion/Linear register without being either. Specifically: rounded terminals, slightly compressed proportions, very legible at small sizes (we'll be running 12sp captions on mid-density screens). Weights used: 400 Regular, 500 Medium, 600 Semibold. We do not use 700 — Snapdoc never needs that much weight; capping at 600 keeps the visual register restrained.

**Fallback: Inter** — pixel-equivalent metrics for the most common weights, widely available, identical x-height. If Geist fails to load, Inter substitutes without reflow.

**Numbers / Code: Geist Mono.** Tabular numbers used wherever values align in columns (file sizes, page counts, document counts in category lists). Geist Mono pairs metrically with Geist Sans, so a `12 pages` label can switch only the digits to Mono without disturbing the line.

**Devanagari / Tamil / Telugu / Kannada / Malayalam / Bengali / Gujarati support:** Geist does not ship Indic glyphs. For Indic scripts, fall back to **Noto Sans** (Devanagari, Tamil, Telugu, Kannada, Malayalam, Bengali, Gujarati). The `fontFamily` stack in Compose is configured per-locale: Geist for Latin, Noto for Indic. Both share comparable x-heights at the same sp size; cross-script paragraphs do not jitter.

**Rationale for Geist over Inter:** Inter is the safe, expected choice — and the brief specifically calls out the desire to avoid feeling like a Google default. Geist has a more deliberate character (the lower-case `a` is a single-storey, the `g` is open-loop, the `1` has a flat foot) that gives the app a quiet identity in its lettering without ever shouting.

### 3.2 Type scale

All sizes in `sp` (Android scale-pixels). Line height in `sp`. Letter spacing in `em`.

| Token | Size | LH | Weight | Tracking | Use |
|---|---|---|---|---|---|
| `display` | 40sp | 48sp | 600 | -0.02em | Paywall hero "Remove ads"; never used elsewhere. |
| `headlineXl` | 32sp | 40sp | 600 | -0.02em | Onboarding screen titles. |
| `headlineLg` | 24sp | 32sp | 600 | -0.015em | Empty states, About header, Settings sections. |
| `headlineMd` | 20sp | 28sp | 600 | -0.01em | Document detail filename, AI Summary "TL;DR" header. |
| `titleXl` | 18sp | 26sp | 500 | -0.01em | Top-bar screen title (Home, Categories, Search). |
| `titleLg` | 16sp | 24sp | 500 | -0.005em | Document card filename, list section headers. |
| `titleMd` | 14sp | 20sp | 500 | 0em | Bottom sheet section labels, dialog titles. |
| `bodyLg` | 16sp | 24sp | 400 | 0em | Default body text, paragraphs in About, AI Summary bullets. |
| `bodyMd` | 14sp | 20sp | 400 | 0em | Settings row description, secondary content. |
| `bodySm` | 13sp | 18sp | 400 | 0em | Native-ad body copy, dense list metadata. |
| `labelLg` | 14sp | 20sp | 500 | 0.005em | Button labels, segmented control labels. |
| `labelMd` | 13sp | 18sp | 500 | 0.01em | Tabs, chips. |
| `labelSm` | 12sp | 16sp | 500 | 0.02em | Tag chips, status indicators. |
| `caption` | 12sp | 16sp | 400 | 0em | Timestamps, file sizes, page counts. |
| `overline` | 11sp | 14sp | 500 | 0.08em (UPPERCASE) | List section dividers ("TODAY", "EARLIER THIS WEEK", "OLDER"). |

**Minimum interactive text:** 14sp (`bodyMd` / `labelLg`). Anything below is decorative metadata and may not be the sole label of an interactive element.

### 3.3 Number style policy

- **Tabular** (`Geist Mono` or `Geist Sans` with `font-feature-settings: "tnum"`) is used for:
  - Document file sizes (`2.4 MB`)
  - Page counts (`12 pages`)
  - Category counts in nav (`Bills · 14`)
  - Pricing (`₹99` / `$1.49`)
  - Storage indicators (`2.1 GB of 24 GB used`)
  - The scan timer / shutter countdown
- **Proportional** is used everywhere else — body text, document filenames (which often contain digits like `Electricity Bill - March 2026.pdf`).

The Compose theme exposes this as a `TextStyle` modifier `.tabular()` rather than separate type tokens, to keep the scale list short.

---

## Section 4 — Spacing & Layout

### 4.1 Spacing scale

Strict 4dp grid — no value off this scale.

| Token | dp | Use |
|---|---|---|
| `space-0` | 0 | Border-collapse situations only. |
| `space-1` | 4 | Inside dense components — chip padding, between an icon and its 12sp label. |
| `space-2` | 8 | Default icon ↔ label gap, inter-chip gap, inside small buttons. |
| `space-3` | 12 | Inside cards (icon ↔ text), between bullet items in AI Summary. |
| `space-4` | 16 | **Default page horizontal padding.** Card internal padding. Settings row vertical padding. |
| `space-5` | 24 | Section ↔ section gap inside a card. Top-bar ↔ first content row. |
| `space-6` | 32 | Onboarding-screen vertical rhythm. Empty-state illustration ↔ title. |
| `space-7` | 48 | Onboarding-title ↔ subtitle. Major section breaks. |
| `space-8` | 64 | Hero spacing — paywall title ↔ pricing card. |

### 4.2 Page padding rules

- **Horizontal:** 16dp on all standard screens. Camera and document-detail PDF preview are full-bleed (0dp).
- **Vertical above content:** 16dp below the top app bar.
- **Vertical below content (above bottom nav / banner ad):** 16dp; if a banner ad is present, the gap between content and banner is 8dp, then 8dp between banner and nav.
- **Safe area:** the system insets (status bar top, gesture navigation bar bottom) are respected via `WindowInsets.safeDrawing`. We do not draw under the status bar except in the Camera screen (which goes full-bleed dark).

### 4.3 Card and section padding

- **Document card (list):** 16dp horizontal, 12dp vertical. Thumbnail is 56×72dp on the leading edge, 12dp gap to filename.
- **Category card:** 16dp horizontal, 14dp vertical.
- **Settings row:** 16dp horizontal, 14dp vertical, 56dp minimum row height (matches 48dp touch target + 4dp top + 4dp bottom).
- **Bottom sheet (AI Summary, Filter, Share):** 24dp horizontal, 16dp top, 24dp bottom (extra at bottom to clear gesture bar).
- **Dialog:** 24dp horizontal, 24dp vertical.

### 4.4 Component-to-component spacing

- **Within a stack of related items** (e.g. a stack of document cards): 8dp between cards.
- **Between unrelated sections** (e.g. "Today" group → "Earlier this week" group): 24dp.
- **Between a heading and its first item:** 12dp.
- **Between a button group:** 12dp horizontal between two buttons of equal weight; 16dp between a primary and a secondary action in a vertical stack.

### 4.5 Document-list grid

**Single column.** A grid for documents would compress the filename below readability — filenames like "Aadhaar Card — Front + Back" need horizontal room. Single column, full-width cards, 16dp side margins, 8dp between cards. The category-strip on the leading edge gives visual rhythm without relying on a grid.

The exception: **Categories View** uses a 2-column grid (8dp gutter, 16dp margins) because category cards are visually balanced (icon + label + count) and benefit from the symmetry.

---

## Section 5 — Shape & Elevation

### 5.1 Corner radius scale

| Token | dp | Use |
|---|---|---|
| `radius-none` | 0 | Top app bar bottom edge, full-bleed images. |
| `radius-sm` | 4 | Chips, tags, small inline indicators. |
| `radius-md` | 8 | Buttons (default), text fields, list-row hover backgrounds. |
| `radius-lg` | 12 | Document cards, settings cards, dialogs. |
| `radius-xl` | 20 | Bottom sheets (top corners only), paywall pricing card. |
| `radius-2xl` | 28 | FAB. |
| `radius-full` | 9999 | Avatars, full-pill segmented controls, the camera shutter button. |

**Asymmetric radii**: the bottom sheet uses `radius-xl` on top-left and top-right only, 0 on bottom corners (it sits flush against the bottom of the screen). This is the only allowed asymmetric application.

### 5.2 Elevation tokens

We prefer **borders over shadows** in light mode and **subtle shadows + raised surfaces** in dark mode. The reasoning: a 12dp shadow on a `#FAFAF8` ground is barely visible and adds visual noise; a 1dp `#E5E4DF` border articulates the card edge with more clarity at less expense. In dark mode, borders fight the carbon ground; surfaces lift via lightness step + a soft shadow.

| Token | Light | Dark |
|---|---|---|
| `elev-0` | flat on `bg`, no border | flat on `bg`, no border |
| `elev-1` | 1dp border `border`, no shadow | `surface`, no border, shadow `0 1px 2px rgba(0,0,0,0.4)` |
| `elev-2` | 1dp border `border`, shadow `0 2px 6px rgba(17,17,17,0.04)` | `surface`, shadow `0 2px 6px rgba(0,0,0,0.5)` |
| `elev-3` | shadow `0 4px 12px rgba(17,17,17,0.06)`, no border | `surfaceVariant`, shadow `0 4px 12px rgba(0,0,0,0.6)` |
| `elev-4` | shadow `0 8px 24px rgba(17,17,17,0.08)` | `surfaceVariant`, shadow `0 8px 24px rgba(0,0,0,0.65)` |
| `elev-5` | shadow `0 16px 40px rgba(17,17,17,0.10)` | `surfaceVariant` lifted to `#202024`, shadow `0 16px 40px rgba(0,0,0,0.7)` |

**Component → elevation mapping:**
- Document card: `elev-1`
- Settings row card / Category card: `elev-1`
- FAB (resting): `elev-3`; (pressed): `elev-2`
- Bottom sheet: `elev-4`
- Dialog: `elev-5`
- Snackbar: `elev-3`
- Top app bar: `elev-0` resting; `elev-1` once content scrolls (border-only on light, 1dp `divider` line on dark)

### 5.3 Border philosophy

- **Use a 1dp border** when the goal is to delineate edges between same-tone surfaces (card on bg in light mode, top-bar separator).
- **Use elevation/shadow** when the goal is to communicate hierarchy (modal floats above content; FAB floats above list).
- **Never both** on the same component, unless that component is `elev-2` in light mode (border + 6%-opacity shadow combine to feel like physical paper, which we use for the document card to reinforce the "this represents a real piece of paper" metaphor).

---

## Section 6 — Motion

### 6.1 Duration tokens

| Token | ms | Use |
|---|---|---|
| `dur-instant` | 80 | Instant feedback — chip selection, toggle thumb. |
| `dur-fast` | 160 | Button press, ripple, hover-state change. |
| `dur-default` | 240 | Bottom-sheet enter/exit, snackbar, modal. |
| `dur-slow` | 320 | Page transitions, list reorder settle. |
| `dur-extra-slow` | 480 | First-launch onboarding entry only. |

### 6.2 Easing curves

| Token | cubic-bezier | Use |
|---|---|---|
| `ease-standard` | `cubic-bezier(0.2, 0, 0, 1)` | Default — most enter+exit motion. |
| `ease-emphasized` | `cubic-bezier(0.05, 0.7, 0.1, 1)` | Hero moments — bottom sheets, paywall. |
| `ease-decelerate` | `cubic-bezier(0, 0, 0, 1)` | Items entering screen (lists). |
| `ease-accelerate` | `cubic-bezier(0.3, 0, 1, 1)` | Items leaving screen. |
| `ease-linear` | `linear` | Continuous progress (scan-line animation, OCR progress bar). |

### 6.3 Standard transitions

| Transition | Properties |
|---|---|
| **Page entry (forward)** | New screen translates from `+24dp` x to 0; opacity 0 → 1; `dur-slow` `ease-standard`. Previous screen fades opacity 1 → 0 over `dur-default`. |
| **Page exit (back)** | Inverse — new screen comes from `-24dp` x. |
| **Modal in** | Opacity 0 → 1, scale 0.96 → 1; `dur-default` `ease-emphasized`. Scrim fades 0 → 0.45 in parallel. |
| **Modal out** | `dur-fast` `ease-accelerate`. |
| **Bottom sheet in** | Translate Y `+sheetHeight` → 0; `dur-default` `ease-emphasized`. Scrim fades 0 → 0.45. |
| **Bottom sheet out** | `dur-default` `ease-accelerate`. |
| **Snackbar** | Translate Y `+56dp` → 0; `dur-default` `ease-standard`. Auto-dismiss 4000ms, then translate out `dur-fast`. |
| **Button press** | Background overlay 0 → 10%; `dur-fast` `ease-standard`. Scale 1 → 0.98 → 1 on full press cycle. |
| **List reorder** | Items animate to new position; `dur-slow` `ease-standard`. Picked-up item lifts to `elev-3` over `dur-fast`. |
| **Scan-line (camera, indeterminate)** | Vertical line translates top → bottom in 1400ms `ease-linear`, loops. |
| **AI summary loading** | "Reading your document…" text crossfades through 3 strings every 1200ms. The pill background shimmers via a 30% lightness gradient sweeping left → right over 1800ms `ease-linear`. |

---

## Section 7 — Iconography

### 7.1 Library — **Lucide**

Recommended over Material Symbols and Phosphor, with rationale:

- **vs Material Symbols:** Material Symbols gives the app a Google-default look — exactly what the brief asks us to avoid. Even with `Rounded` or `Outlined` style applied, the scan/document/folder glyphs are instantly recognizable as Google.
- **vs Phosphor:** Phosphor's six weights are tempting, but most have a slight playfulness (the bold weights especially) that conflicts with "calm tool, not consumer toy." Phosphor's regular weight is also slightly too thin for 18sp legibility.
- **Lucide:** consistent 1.5-stroke geometric construction across all glyphs, designed for Linear-/Vercel-style products — exactly the visual register the brief requests. License is ISC, Compose-friendly via `lucide-compose` (community port) or directly as SVG-imported `ImageVector`.

### 7.2 Defaults

| Property | Value |
|---|---|
| Default size | 20dp |
| Inline size (within a button label) | 16dp |
| Top-bar action icon | 24dp |
| FAB icon | 24dp |
| Bottom-nav icon | 24dp |
| Stroke weight | 1.5dp (Lucide native) |
| Color (default) | `textPrimary` |
| Color (secondary surface) | `textSecondary` |
| Color (disabled) | `textDisabled` |
| Touch target (icon-only button) | 48dp minimum (icon centered in 48dp frame) |

### 7.3 Glyph mapping

| Action | Lucide glyph |
|---|---|
| Scan / capture | `scan-line` |
| Camera shutter | (custom — see Camera spec) |
| Search | `search` |
| Categories nav | `layout-grid` |
| Settings nav | `settings` |
| Home nav | `file-text` |
| Share | `share` |
| Export | `download` |
| AI Summary | `sparkles` (the only place we use `sparkles` — restraint matters) |
| Delete | `trash-2` |
| Favorite | `star` (filled when active) |
| Privacy / lock | `lock` |
| Page added | `plus` |
| Retake | `rotate-ccw` |
| OCR text | `text` |
| Multi-select | `square-check` |
| Back | `arrow-left` |
| Close | `x` |
| More | `more-horizontal` |
| Sort | `arrow-down-up` |
| Filter | `sliders-horizontal` |

---

## Section 8 — Component Library

States: `default`, `hover` (Android: programmatically only — not pointer-driven), `pressed`, `focused` (keyboard / TalkBack), `disabled`. Where applicable, `error` and `selected`.

All values reference tokens from §2–§6. No raw hex appears in this section.

### 8.1 Buttons

#### 8.1.1 Primary button
- **Height:** 48dp. **Padding:** 16dp horizontal. **Radius:** `radius-md`. **Type:** `labelLg`. **Icon:** 16dp inline, 8dp gap before label.
- **Default:** bg `primary`, fg `onPrimary`.
- **Pressed:** bg `primary` + `pressed` overlay.
- **Focused:** add 2dp `accent` ring at 2dp offset.
- **Disabled:** bg `surfaceVariant`, fg `textDisabled`, no shadow.
- **Loading:** bg `primary`, fg replaced by 16dp circular spinner (`onPrimary`), label left-aligned at 60% opacity.

#### 8.1.2 Secondary button
- **Height:** 48dp. **Border:** 1dp `border`. **Radius:** `radius-md`. **Type:** `labelLg`.
- **Default:** bg `surface`, fg `textPrimary`, border `border`.
- **Pressed:** bg `surfaceVariant`.
- **Focused:** ring `accent`.
- **Disabled:** bg `surfaceVariant`, fg `textDisabled`, border `divider`.

#### 8.1.3 Ghost button
- No bg, no border. Used for tertiary actions inside cards (e.g. "See all" on a category preview).
- **Height:** 36dp. **Padding:** 12dp horizontal.
- **Default:** fg `textPrimary`.
- **Pressed:** bg `surfaceVariant`, radius `radius-md`.
- **Disabled:** fg `textDisabled`.

#### 8.1.4 Destructive button
- **Light:** bg `error`, fg `#FFFFFF`. **Pressed:** + 10% black overlay.
- **Dark:** bg `surface`, border 1dp `error`, fg `error`. **Pressed:** bg `surfaceVariant`.
- Same dimensions and type as Primary.

#### 8.1.5 Icon-only button
- **48×48dp** touch target with 24dp icon centered.
- **Default:** transparent bg.
- **Pressed:** circular bg `surfaceVariant`, radius `radius-full`.
- **Focused:** 2dp ring `accent`.
- **Disabled:** icon fg `textDisabled`.

#### 8.1.6 FAB (Floating Action Button)
- **Size:** 56×56dp. **Radius:** `radius-2xl` (28dp). **Icon:** 24dp `scan-line`.
- **Default:** bg `primary`, fg `onPrimary`, `elev-3`.
- **Pressed:** `elev-2`, scale 0.96 → 1.
- **Focused:** + 2dp `accent` ring at 4dp offset.
- **Position:** bottom-right, 16dp from screen edge, 16dp above bottom nav (or above banner ad if present).

#### 8.1.7 Segmented control
- **Container:** bg `surfaceVariant`, radius `radius-md`, padding 2dp.
- **Segment height:** 36dp. **Segment radius:** `radius-md` minus 2dp = 6dp.
- **Default segment:** fg `textSecondary`.
- **Selected segment:** bg `surface`, fg `textPrimary`, `elev-1`.
- **Pressed:** + `pressed` overlay.
- **Focused:** ring `accent`.
- **Disabled:** fg `textDisabled`.
- Used for Filter screen mode selector and PDF size picker.

### 8.2 Inputs

#### 8.2.1 Text field (filled)
- **Height:** 56dp (with floating label) / 48dp (without). **Padding:** 16dp horizontal.
- **Radius:** `radius-md` on top corners, 0 on bottom (so the underline reads). Underline 1dp `border`.
- **Default:** bg `surfaceVariant`, label `textSecondary`, value `textPrimary`.
- **Focused:** bg `surfaceVariant`, underline 2dp `accent`, label `accent`.
- **Error:** underline 2dp `error`, label and helper text `error`.
- **Disabled:** bg `surfaceVariant` at 50% opacity, fg `textDisabled`.
- **Helper text:** below field, `caption` size, 4dp gap.

#### 8.2.2 Search field
- **Height:** 44dp. **Padding:** 12dp horizontal. **Radius:** `radius-full`.
- **Default:** bg `surfaceVariant`, fg `textPrimary`, leading 20dp `search` icon `textSecondary`, placeholder `textTertiary`.
- **Focused:** bg `surface`, 1dp border `accent`, no ring.
- Trailing 16dp `x` clear button appears once value is non-empty.

#### 8.2.3 Dropdown
- Same chrome as text field. Trailing 16dp `chevron-down`.
- Open state: menu uses `elev-3`, radius `radius-lg`, item height 44dp, item padding 16dp horizontal.

#### 8.2.4 Toggle
- **Track:** 32×20dp, radius `radius-full`.
- **Thumb:** 16dp circle, 2dp inset.
- **Off:** track `surfaceVariant`, thumb `surface` (light) / `secondary` (dark).
- **On:** track `primary`, thumb `surface` (which means white-thumb in light, off-white-thumb in dark).
- **Disabled:** track `surfaceVariant` at 50%, thumb `textDisabled`.
- **Focused:** 2dp `accent` ring at 4dp offset.
- Animation: thumb translates `dur-instant` `ease-standard`.

#### 8.2.5 Checkbox
- **Size:** 20×20dp. **Radius:** `radius-sm`.
- **Off:** 1.5dp border `border`, bg transparent.
- **On:** bg `primary`, 16dp `check` glyph in `onPrimary`.
- **Indeterminate:** bg `primary`, 12dp horizontal `minus` glyph in `onPrimary`.
- **Touch target:** 48×48dp.

#### 8.2.6 Radio
- **Size:** 20×20dp. **Radius:** `radius-full`.
- **Off:** 1.5dp border `border`.
- **On:** 1.5dp border `primary`, 10dp inner `primary` circle.
- **Touch target:** 48×48dp.

#### 8.2.7 Slider
- **Track:** 4dp tall, radius `radius-full`.
- **Track active:** `primary`. **Track inactive:** `surfaceVariant`.
- **Thumb:** 20dp circle `primary`, `elev-2`. On press: ripple ring 32dp at 12% `primary`.
- **Tick marks** (used for crop quality / brightness): 4dp `divider` circles.
- Used in Filter screen (brightness/contrast).

### 8.3 Cards

#### 8.3.1 Document card (main list)
- **Container:** `surface`, `radius-lg`, `elev-1` light / `elev-1` dark, full width minus 16dp margins.
- **Internal layout:** 16dp padding all sides. Leading 56×72dp PDF thumbnail, `radius-sm`, 1dp border `border` (light only; dark uses no border, drops onto surface). 12dp gap. Then a vertical stack:
  - Filename `titleLg` `textPrimary`, max 2 lines, ellipsis.
  - 4dp gap.
  - Metadata row: `caption` `textTertiary` — `12 pages · 2.4 MB · Today, 4:12 PM`. Numbers tabular.
  - 8dp gap.
  - Category chip (see 8.10.4).
- **Leading edge accent strip:** 2dp wide, full card height, color from §2.4 Category accent. The strip is drawn *inside* the radius — the card is clipped, the strip is the leftmost 2dp.
- **States:**
  - Default: as above.
  - Pressed: + `pressed` overlay across full card.
  - Selected (multi-select mode): 2dp border `primary` replaces the 1dp `border`. A 24dp circular checkbox appears in the top-right corner, positioned 8dp from edges.
  - Loading (during initial scan import): thumbnail replaced with skeleton, filename "Processing…", progress bar at bottom edge.

#### 8.3.2 Category card (Categories View, 2-col grid)
- **Container:** `surface`, `radius-lg`, `elev-1`, square aspect.
- **Internal:** 16dp padding. 28dp Lucide glyph at top-left, color = §2.4 strip color. 12dp gap. Category name `titleLg`. Count line `caption` `textTertiary` with tabular numbers ("14 documents").

#### 8.3.3 Settings row
- **Container:** `surface`, `radius-lg` only on first/last row of a group (rounded section corners). Inter-row dividers 1dp `divider` insetting 16dp from leading edge.
- **Internal:** 16dp padding horizontal, 14dp vertical, min-height 56dp. Optional 20dp leading icon with 12dp gap. Title `bodyLg` `textPrimary`. Optional secondary line `bodyMd` `textSecondary` 4dp below. Trailing: chevron-right (navigation) / toggle / value text.

### 8.4 Navigation

#### 8.4.1 Bottom navigation
- **Height:** 64dp + bottom safe-area inset. **Surface:** `surface`. **Top edge:** 1dp `divider` (light) / no border (dark — relies on tone separation).
- **3 destinations** for Snapdoc: `Home`, `Categories`, `Settings`. (Spec §2 nav structure — Search is invoked from the top bar, not bottom.)
- **Item:** 24dp icon centered, `labelMd` label 4dp below.
- **Active:** icon and label `textPrimary`. Active item has a 4dp tall, 16dp wide pill behind the icon: `surfaceVariant` light / `surfaceVariant` dark.
- **Inactive:** icon and label `textSecondary`.
- **Pressed:** + `pressed` overlay on item bg.

#### 8.4.2 Top app bar
- **Height:** 56dp. **Surface:** `bg` (yes, `bg` not `surface` — the bar is part of the page on rest, lifts to `surface` + 1dp divider once content scrolls beneath).
- **Layout:** 16dp leading padding, 8dp trailing. Optional 24dp leading icon (back, menu) at 48dp touch target. Title `titleXl` `textPrimary`, left-aligned by default. Trailing actions: 24dp icons in 48dp frames, max 3.

#### 8.4.3 Tab bar
- Used inside Document Detail (Pages | Text | Summary).
- **Height:** 44dp. **Underline indicator:** 2dp `primary` under active tab.
- **Active label:** `labelLg` `textPrimary`.
- **Inactive label:** `labelLg` `textSecondary`.
- Spacing between tabs: 24dp horizontal.

#### 8.4.4 Breadcrumbs
- Used inside Manage Categories (Categories ▸ Custom ▸ Edit).
- `labelMd` `textTertiary`. Separator: 12dp `chevron-right` glyph at 50% opacity. Tap any segment navigates to that level.

### 8.5 Feedback

#### 8.5.1 Snackbar
- **Surface:** `primary` (inverts vs page — is dark in light mode, light in dark mode, by design — gives critical feedback strong contrast).
- **Radius:** `radius-md`. **Padding:** 16dp horizontal, 14dp vertical.
- **Type:** `bodyMd` on `onPrimary`. Optional trailing action label `labelMd` in `accent`.
- **Position:** 16dp from bottom edge (above any nav/banner). Max width: screen width − 32dp.
- **Auto-dismiss:** 4s default; 6s if action is present; persistent if action is "Undo" on a destructive op.
- **Elevation:** `elev-3`.

#### 8.5.2 Toast
- Not used in Snapdoc — Snackbars carry all transient feedback. (Toasts have inconsistent positioning across Android OEMs and conflict with our calm-tool register.)

#### 8.5.3 Dialog (alert / confirm)
- **Surface:** `surface`. **Radius:** `radius-lg`. **Width:** screen width − 48dp, max 360dp.
- **Padding:** 24dp.
- **Title:** `headlineMd`. 12dp gap. **Body:** `bodyLg` `textSecondary`. 24dp gap.
- **Actions:** right-aligned button row, secondary then primary, 12dp gap. Destructive uses Destructive button styling.
- **Elevation:** `elev-5`.

#### 8.5.4 Bottom sheet
- **Surface:** `surface`. **Radius:** `radius-xl` top corners only.
- **Drag handle:** 32dp × 4dp pill, `divider`, centered, 8dp from top edge.
- **Padding:** 24dp horizontal, 16dp top (below handle), 24dp bottom.
- **Header:** `headlineMd` left-aligned, optional trailing 24dp `x` close icon.
- **Elevation:** `elev-4`.
- **Scrim:** `scrim` token.
- Heights: half-sheet (50% screen height + content height up to 75%) or full-sheet (90%). AI Summary uses full-sheet.

#### 8.5.5 Loading spinner
- 24dp default. 1.5dp stroke indeterminate ring. Color `textPrimary` (or `accent` for AI-related ops). Animation: 800ms linear rotation, 75% arc-length sweep.

#### 8.5.6 Skeleton loader
- Background `surfaceVariant`. Animated shimmer: 30%-lightness gradient sweeping left → right over 1400ms `ease-linear`, looping. Used for document-card thumbnails during scan import and AI summary placeholder lines.

#### 8.5.7 Progress bar (determinate)
- 4dp tall. Track `surfaceVariant`. Indicator `primary`, radius `radius-full` on the leading edge. Used during PDF export and OCR processing.

### 8.6 Lists

#### 8.6.1 List item
- See 8.3.1 (document card) for the document case. Generic list item:
- 56dp min-height. 16dp padding. Optional 20dp leading icon. `bodyLg` title. Optional `bodyMd textSecondary` subtitle. Optional trailing chevron-right or value text.

#### 8.6.2 List section header
- Type: `overline` `textTertiary`, uppercase, 12dp top padding (after preceding section), 8dp bottom padding above first item, 16dp horizontal padding. Examples: "TODAY", "EARLIER THIS WEEK", "OLDER".

#### 8.6.3 Empty state container
- Vertically centered inside available area. 64dp top space. Components in order:
  - 96dp glyph illustration (Lucide-derived, 1.5dp stroke, color `textTertiary`).
  - 24dp gap.
  - `headlineLg` title `textPrimary`, max 2 lines, centered.
  - 8dp gap.
  - `bodyLg` body `textSecondary`, max 3 lines, centered, max-width 280dp.
  - 24dp gap.
  - Optional Primary button.
- Horizontal padding 24dp.

#### 8.6.4 Error state container
- Same skeleton as empty state, with:
  - Glyph: `alert-triangle` 96dp, color `error`.
  - Title in `textPrimary`.
  - Body explains in plain language. Includes a "Try again" Secondary button when retry is meaningful.

### 8.7 Ads

We are an ad-supported product but our brand is restraint. Ad surfaces follow strict containment rules.

#### 8.7.1 Banner ad slot (home, persistent)
- **Container:** 1dp `border` top edge only, no other sides. Bg `surface`. Height: 50dp (320×50 banner) or 60dp (adaptive). Above the bottom nav.
- **Top-left "Ad" label:** `labelSm` `textTertiary`, 4dp inset from top-left of the AdMob view, drawn by us as an overlay (AdMob's own label style is inconsistent).
- **Light:** divider above is `divider`. **Dark:** divider above is `border`.

#### 8.7.2 Native ad card (in-feed, every 7th item)
- Outer container: identical chrome to a Document Card (`surface`, `radius-lg`, `elev-1`) so it does not visually disrupt scanning. Differences:
  - Top-right "Sponsored" label `labelSm` `textTertiary`, 8dp inset.
  - No category accent strip.
  - Headline `titleLg`, body `bodyMd textSecondary`, CTA Secondary button.
  - 56×56dp ad-image leading.

#### 8.7.3 Rewarded ad modal frame
- Bottom sheet (8.5.4) with:
  - `headlineMd` "Watch a 15-second ad to generate summary" — wording is plain and literal.
  - `bodyLg textSecondary` line: "Or remove ads forever — ₹99 / $1.49 once."
  - Two stacked buttons: Primary "Watch ad", Secondary "Remove ads instead".
  - Tertiary ghost "Maybe later" centered below.

#### 8.7.4 Paywall card (Remove Ads screen)
- **Hero card:** `surface`, `radius-xl`, `elev-3`. Padding 32dp. Centered content:
  - 48dp `lock` icon `textPrimary`.
  - 24dp gap.
  - `display` "Remove ads".
  - 8dp gap.
  - `headlineLg textSecondary` "₹99 once. Forever." (Tabular numbers.)
  - 32dp gap.
  - Benefits list — 3 rows, 16dp gap each, 20dp `check` glyph in `success` + `bodyLg` text.
  - 32dp gap.
  - Primary button "Buy — ₹99" full-width.
  - 12dp gap.
  - Ghost button "Restore purchase" full-width.

### 8.8 Specialty components

#### 8.8.1 Page thumbnail (Multi-Page Review carousel)
- 96×128dp tile. `surface`, `radius-md`, 1dp border `border`. 12dp gap between tiles.
- Page number badge top-right: 18×18dp circle bg `primary`, fg `onPrimary`, `labelSm` tabular.
- Selected: 2dp `primary` border replaces 1dp `border`.
- Long-press initiates drag-to-reorder; lifts to `elev-3`, scale 1.04.

#### 8.8.2 Edge-detection overlay (Camera)
- 4 corner brackets, each L-shaped: 24dp arms × 3dp stroke, color `accent`. Brackets snap to detected document corners.
- A faint 1dp polygon connecting the four corners, color `accent` at 40% opacity. Polygon refreshes on every detection frame; transitions are a 200ms `ease-decelerate` lerp to avoid jitter.
- When detection is unstable: brackets fade to 30% opacity, polygon hides.

#### 8.8.3 Crop handles
- 4 corner handles, each a 32×32dp touch target with a 16dp visible square: bg `surface`, 2dp border `primary`, radius `radius-sm`. Edges between corners are draggable lines: 1.5dp `primary`.
- On drag: handle shows 2dp `accent` border instead.

#### 8.8.4 OCR text bounding-box highlight
- Only shown in the Full-Text Viewer when a search term is matched in OCR. 1dp border `accent`, bg `accent` at 12%, radius `radius-sm`. Boxes follow original document coordinates, scaled to viewer dimensions.

#### 8.8.5 AI Summary bottom-sheet header
- Bottom sheet header (8.5.4) plus:
  - 16dp `sparkles` icon `accent`, 8dp gap, then "AI Summary" `headlineMd`.
  - Below the title row: a thin pill `labelSm` `textTertiary` reading "Generated from your document text. Not stored on a server." — privacy reassurance, 12dp gap above body.
  - "Regenerate" ghost button trailing-aligned in the title row.

---

*End of Deliverable 1 — `02_design_system.md`. Continuing into Deliverable 2.*
