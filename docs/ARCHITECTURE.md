# Architecture

Dependency direction: features → designsystem → color; features → data → color.

- **:core:color** — pure Kotlin multiplatform (jvm + iOS targets). All color math:
  sRGB/HSL/HSV, OKLab/OKLCH with gamut mapping, WCAG 2.1, APCA, CVD (Machado
  linear + sRGB preview matrices), harmonies, tonal scales, styles, namer,
  extractor, blender, exporter, quiz generation (key-based text), theme generator.
  Zero Android/Compose. 256-entry sRGB LUT; exact warm-start chroma cache.
- **:core:data** — Room (palettes, quiz results), DataStore (settings/progress;
  corruption handled at the factory only), crash capture (local files), policy
  logic (Review, Achievements, ThemeSource), AppClock/AppInfo. Contracts only —
  LessonRepository's impl is content-owned by :feature:learn.
- **:core:designsystem** — KMP shared UI: theme (expect/actual dynamic color,
  applied-scheme override), components (wheel with draw-phase reads, picker with
  gamut-capped chroma, swatches, sliders, chips, inspector), compose.resources
  strings, preview registry, cvdPreview expect/actual.
- **:feature:*** — MVVM + UDF; StateFlow + collectAsStateWithLifecycle;
  stateIn(WhileSubscribed(5s)). Each owns its routes, deep links, strings.
- **:app** — shell: NavigationSuiteScaffold, Hilt, WorkManager (on-demand init,
  midnight self-rescheduling worker), Glance widget, shortcuts, splash tied to
  stored-settings load, applied-theme, local crash handler installation.
- **:wear** — tile + complication driven by the same ColorOfDay pure function.
- **:desktop** — CMP Palette Studio on shared designsystem components.
- **:baselineprofile** — macrobenchmarks (startup, grid-scroll frame timing).
