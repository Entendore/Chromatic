plugins { `kotlin-dsl` }

dependencies {
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.compose.compiler.gradle.plugin)
    compileOnly(libs.ksp.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") { id = "chromatic.android.application"; implementationClass = "AndroidApplicationConventionPlugin" }
        register("androidApplicationCompose") { id = "chromatic.android.application.compose"; implementationClass = "AndroidApplicationComposeConventionPlugin" }
        register("androidLibrary") { id = "chromatic.android.library"; implementationClass = "AndroidLibraryConventionPlugin" }
        register("androidLibraryCompose") { id = "chromatic.android.library.compose"; implementationClass = "AndroidLibraryComposeConventionPlugin" }
        register("hilt") { id = "chromatic.hilt"; implementationClass = "HiltConventionPlugin" }
        register("kotlinJvm") { id = "chromatic.kotlin.jvm"; implementationClass = "KotlinJvmConventionPlugin" }
    }
}
