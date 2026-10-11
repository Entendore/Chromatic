# Decision Log (ADR)

| # | Decision | Release |
|---|----------|---------|
| 1 | :core:color is pure Kotlin, zero Android — payoff: Wear, desktop, iOS | v1→v5/v7/v11 |
| 2 | Room for relational data; DataStore for settings; corruption handled at the factory, never by blanket catches | v2, v9 |
| 3 | Perceptual core = OKLab/OKLCH; gamut issues REDUCE CHROMA, never clip channels | v3 |
| 4 | The engine holds no display strings; consumers own and localize text | v5, v10, v16 |
| 5 | Two CVD models, honestly labeled: Machado (linear) = truth; sRGB matrices = live preview | v4, v8 |
| 6 | Caches store search STATE, never quantized RESULTS | v10 |
| 7 | @JvmInline value class for Hsv/Hsl — REJECTED; measured cost was elsewhere | v9 |
| 8 | SecondaryNavigationSuiteScaffold — REJECTED; trigger: a real global second pane | v11 |
| 9 | Chips suppress TouchTargetSizeCheck — M3's own density trade-off, recorded | v13 |
| 10 | Suspend work is owned by viewModelScope; only @ApplicationContext crosses singleton borders | v8, v9 |
| 11 | Mutation gate = regression vs committed baseline first; absolute threshold only after measuring | v13, v15, v16 |
| 12 | QuizText.Raw exists solely for ColorNamer output — color-name corpus is not localized | v16 |
| 13 | **Fully local**: no INTERNET permission, enforced by OS + CI; crash reporting is local-only with user-initiated share. Supersedes the v14 allowlist approach; retracts the telemetry-driven roadmap | v17 |
| 14 | **Paid upfront, no billing SDK**: Play enforces the sale store-side, so the local contract survives. Accepted risk: APK extraction. One artifact for all channels | v18 |
| 15 | Publishing via fastlane `supply` (draft→internal) instead of GPP — a conditional `plugins{}` block is not expressible in Gradle; same outcome | v18 |
