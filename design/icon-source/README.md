# App icon — source art

Vector source for the Snapdoc app icon. Edit these, then regenerate the
Android resources from them.

| File | Purpose |
|------|---------|
| `snapdoc-icon-master.svg` | Full icon (background + artwork). Reference / Play Store art. |
| `snapdoc-icon-foreground.svg` | Adaptive-icon foreground layer (artwork only, 91% safe-zone scale). |
| `snapdoc-icon-background.svg` | Adaptive-icon background layer (solid `#FAFAF8`). |
| `snapdoc-icon-monochrome.svg` | Themed-icon (Android 13+) monochrome layer. |
| `snapdoc-icon-notification.svg` | Status-bar notification icon (white silhouette). |
| `play-store-512.png` | 512×512 listing icon — upload to the Play Console (not bundled in the app). |

## Where these map in the app

- Adaptive icon: `app/src/main/res/mipmap-anydpi-v26/ic_launcher{,_round}.xml`
  → `drawable/ic_launcher_foreground.xml`, `drawable/ic_launcher_background.xml`,
  `drawable/ic_launcher_monochrome.xml` (all VectorDrawables).
- Legacy raster fallback: `app/src/main/res/mipmap-{m,h,xh,xxh,xxxh}dpi/ic_launcher{,_round}.png`.
- Notification: `app/src/main/res/drawable/ic_notification.xml`.

The foreground/background/monochrome/notification drawables are hand-converted
VectorDrawables of the SVGs above (SVG `<rect>`/`<line>` become `<path>`; the
`scale(0.91)` group becomes a `<group>` with `scaleX/scaleY` about pivot 512,512).
