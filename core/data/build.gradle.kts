plugins {
    id("chromatic.android.library")
    id("chromatic.hilt")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kover)
}

android { namespace = "com.example.chromatic.core.data" }

dependencies {
    api(projects.core.color)                        // Rgb/Hsl leak through the API by design
    api(libs.kotlinx.coroutines.android)            // Flow types leak through the API by design
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)
    implementation(libs.androidx.datastore.preferences)   // storage hidden behind repos (v9 §6a)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.core.ktx)

    testImplementation(projects.core.testing)
    testImplementation(libs.androidx.room.testing)
    kspTest(libs.hilt.compiler)
}

koverReport { defaults { verify { rule { bound { minValue = 55 } } } } }
