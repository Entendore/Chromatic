plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.chromatic.wear"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.example.chromatic.wear"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(projects.core.color)
    implementation(libs.androidx.wear.tiles)
    implementation(libs.androidx.wear.tiles.material)
    implementation(libs.androidx.wear.complications.datasource.ktx)
    implementation(libs.kotlinx.coroutines.android)
}
