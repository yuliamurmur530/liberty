# Liberty website design QA — mobile light and localized screenshots

## Evidence

- Source mobile view: `C:\Users\coc\AppData\Local\Temp\codex-clipboard-d9eeb5a7-d207-43ba-a961-53cc27d2b52f.png`
- Rendered English mobile view: `C:\Users\coc\Documents\Codex\2026-08-22\new-chat\outputs\liberty-mobile-en-qa.png`
- Rendered Russian mobile view: `C:\Users\coc\Documents\Codex\2026-08-22\new-chat\outputs\liberty-mobile-ru-qa.png`
- Rendered desktop view: `C:\Users\coc\Documents\Codex\2026-08-22\new-chat\outputs\liberty-desktop-qa.png`
- Source/implementation comparison: `C:\Users\coc\Documents\Codex\2026-08-22\new-chat\outputs\liberty-mobile-source-vs-qa-8bit.png`
- Tested viewports: 390 × 844 CSS px and 1920 × 1080 CSS px.

## Findings

- No actionable P0/P1/P2 findings remain.
- Mobile background is positioned at 90% horizontally, so the warm reflection on the wet asphalt remains visible behind the first screen.
- Desktop keeps its centered background composition and the original visual hierarchy.
- RU mode uses the original Russian screenshots; EN mode swaps all four displayed app images to dedicated English screenshots.
- The language switch no longer shares an attribute with the document locale marker, preventing page content from being replaced by the switch label.
- Both mobile modes have no horizontal overflow.
- Fresh-page console check returned no warnings or errors.

## User-directed departure

- English app screenshots are faithful localized copies for the website only. Layout, logo, colors, controls, and Android chrome are preserved; only visible interface text changes.

## Implementation checklist

- [x] Keep the gold asphalt reflection visible on mobile.
- [x] Keep the existing desktop composition.
- [x] Show Russian screenshots in Russian mode.
- [x] Show English screenshots in English mode.
- [x] Preserve responsive layout without horizontal overflow.
- [x] Verify a clean browser console.

final result: passed
