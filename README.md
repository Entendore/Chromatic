# Chromatic 🎨

Learn color theory by doing — and use the same engine as a production tool.

| CI | Coverage | Size | Privacy |
|----|----------|------|---------|
| build badge | engine ≥85% / global ≥60% | ≤12 MB download | **No INTERNET permission** |

An offline-first Android app (Kotlin, Compose, Material 3) with a pure-Kotlin
multiplatform color engine (OKLab/OKLCH, WCAG, APCA, CVD simulation), a Wear tile
+ complication, and a Compose Multiplatform desktop build. **Fully local**: the
merged release manifest contains no network permission — Android itself prevents
network access.

## Modules
:core:color (pure KMP: jvm + iOS) · :core:data · :core:designsystem (KMP:
android + desktop) · :feature:{learn,quiz,tools,settings} · :app · :wear ·
:desktop · :baselineprofile

## Testing pyramid
engine (commonTest, JVM+Native) → ViewModel (fakes + Turbine) → repository
(real Room/DataStore on Robolectric) → app-flow (Hilt + real NavHost + robots) →
visual (Roborazzi + preview matrix) → perf (macrobenchmark frame timing) →
mutation (Pitest, regression-gated).

## Build & run
./gradlew :app:assembleDebug      # phone
./gradlew :desktop:run            # desktop studio
./gradlew :wear:assembleDebug     # watch

## Releasing
See RELEASE_CHECKLIST.md — tag `vX.Y.Z` and CI ships a signed AAB draft to the
internal track, gated on size, permissions, language splits, and listing limits.
Docs: ARCHITECTURE.md, DECISIONS.md.
