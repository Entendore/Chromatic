plugins {
    id("chromatic.android.library")
    id("chromatic.hilt")
}

android { namespace = "com.example.chromatic.core.testing" }

dependencies {
    api(projects.core.color)
    api(projects.core.data)
    api(libs.kotlinx.coroutines.test)
    api(libs.junit)
    api(libs.turbine)
    api(libs.robolectric)
    api(libs.androidx.junit)
    api(libs.androidx.compose.ui.test.junit4)
    api(libs.hilt.android.testing)
    ksp(libs.hilt.compiler)
}
