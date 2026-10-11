import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kover)
}

kotlin {
    jvm()
    iosArm64()
    iosSimulatorArm64()
    explicitApi = ExplicitApiMode.Warning

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
        }
        commonTest.dependencies { implementation(libs.kotlin.test) }
    }
}

koverReport {
    defaults { verify { rule { bound { minValue = 85 } } } }
}
