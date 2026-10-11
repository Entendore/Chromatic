import com.diffplug.gradle.spotless.SpotlessExtension

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.roborazzi) apply false
    alias(libs.plugins.androidx.baselineprofile) apply false
    alias(libs.plugins.kover)
    alias(libs.plugins.spotless) apply false
}

dependencies {
    kover(project(":core:color"))
    kover(project(":core:data"))
    kover(project(":core:designsystem"))
    kover(project(":core:testing"))
    kover(project(":feature:learn"))
    kover(project(":feature:quiz"))
    kover(project(":feature:tools"))
    kover(project(":feature:settings"))
    kover(project(":app"))
    kover(project(":desktop"))
}

kover {
    filters {
        excludes {
            packages("*.di")
            classes("*Module*", "*_Factory", "*_Impl", "Hilt_*", "*_HiltModules*", "*Res*")
            annotatedWith(
                "dagger.hilt.android.AndroidEntryPoint",
                "dagger.hilt.android.HiltAndroidApp",
                "androidx.compose.ui.tooling.preview.Preview",
            )
        }
    }
}

koverReport {
    defaults {
        xml { onCheck = true }
        html { onCheck = true }
        log { onCheck = true }
        verify { rule { bound { minValue = 60 } } }
    }
}

subprojects {
    apply(plugin = "com.diffplug.spotless")
    configure<SpotlessExtension> {
        kotlin {
            target("**/*.kt")
            ktlint("1.3.1").editorConfigOverride(mapOf("ktlint_standard_filename" to "disabled"))
        }
        kotlinGradle { target("**/*.gradle.kts") }
    }
}
