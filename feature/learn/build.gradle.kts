plugins {
    id("chromatic.android.library.compose")
    id("chromatic.hilt")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kover)
}

android { namespace = "com.example.chromatic.feature.learn" }

dependencies {
    implementation(projects.core.designsystem)
    implementation(projects.core.data)
    implementation(projects.core.testing)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.androidx.compose.material3.adaptive.navigation)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.play.review)
    implementation(libs.play.review.ktx)

    testImplementation(projects.core.testing)
    kspTest(libs.hilt.compiler)
}

koverReport { defaults { verify { rule { bound { minValue = 50 } } } } }
