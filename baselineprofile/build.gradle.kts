plugins {
    id("com.android.test")
    alias(libs.plugins.androidx.baselineprofile)
}

android {
    namespace = "com.example.chromatic.baselineprofile"
    compileSdk = 34
    defaultConfig {
        minSdk = 28
        targetSdk = 34
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    targetProjectPath = ":app"
    experimentalProperties["android.experimental.self-instrumenting"] = true
    buildTypes {
        create("benchmark") {
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
            isDebuggable = false
        }
    }
}

androidComponents {
    beforeVariants(selector().all()) { variant -> variant.enable = variant.buildType == "benchmark" }
}

dependencies { implementation(libs.androidx.benchmark.macro) }
