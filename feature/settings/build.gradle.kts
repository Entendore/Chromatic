plugins {
    id("chromatic.android.library.compose")
    id("chromatic.hilt")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kover)
}

android { namespace = "com.example.chromatic.feature.settings" }

dependencies {
    implementation(projects.core.designsystem)
    implementation(projects.core.data)
    implementation(projects.core.testing)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.androidx.navigation.compose)

    testImplementation(projects.core.testing)
    kspTest(libs.hilt.compiler)
}

koverReport { defaults { verify { rule { bound { minValue = 50 } } } } }
