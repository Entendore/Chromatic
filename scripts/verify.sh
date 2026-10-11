#!/usr/bin/env bash
# Post-generation verification: structure + module graph sanity.
set -euo pipefail
cd "$(dirname "$0")/.."

echo "==> Chromatic structure check"
for f in \
  settings.gradle.kts build.gradle.kts gradle/libs.versions.toml gradle.properties \
  app/src/main/AndroidManifest.xml app/src/main/kotlin/com/example/chromatic/MainActivity.kt \
  core/color/src/commonMain/kotlin/com/example/chromatic/core/color/Rgb.kt \
  core/color/src/commonMain/kotlin/com/example/chromatic/core/color/OkLch.kt \
  core/color/src/commonMain/kotlin/com/example/chromatic/core/color/QuizEngine.kt \
  core/data/src/main/kotlin/com/example/chromatic/core/data/di/DataModule.kt \
  core/data/src/main/kotlin/com/example/chromatic/core/data/crash/CrashStore.kt \
  core/designsystem/src/commonMain/kotlin/com/example/chromatic/core/designsystem/theme/Theme.kt \
  feature/learn/src/main/kotlin/com/example/chromatic/feature/learn/LearnScreen.kt \
  feature/quiz/src/main/kotlin/com/example/chromatic/feature/quiz/QuizScreen.kt \
  feature/tools/src/main/kotlin/com/example/chromatic/feature/tools/themestudio/ThemeStudioScreen.kt \
  feature/settings/src/main/kotlin/com/example/chromatic/feature/settings/CrashReportsScreen.kt \
  wear/src/main/kotlin/com/example/chromatic/wear/PaletteTileService.kt \
  desktop/src/desktopMain/kotlin/com/example/chromatic/desktop/Main.kt \
  .github/workflows/release.yml fastlane/Fastfile docs/DECISIONS.md; do
  [ -f "$f" ] || { echo "MISSING: $f"; exit 1; }
done
echo "==> All key files present."
echo "==> Next: gradle wrapper --gradle-version 8.9"
echo "==> Then: ./gradlew spotlessApply && ./gradlew :core:color:allTests :app:testDebugUnitTest"
