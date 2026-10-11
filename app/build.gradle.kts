plugins {
    id("chromatic.android.application.compose")
    id("chromatic.hilt")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.androidx.baselineprofile)
}

fun Project.git(vararg args: String): String? = runCatching {
    providers.exec { commandLine("git", *args); workingDir(rootDir) }
        .standardOutput.asText.get().trim()
}.getOrNull()

android {
    namespace = "com.example.chromatic"

    defaultConfig {
        applicationId = "com.example.chromatic"
        resourceConfigurations += setOf("en", "es")
        versionCode = git("rev-list", "--count", "HEAD")?.toIntOrNull() ?: 1
        versionName = git("describe", "--tags", "--abbrev=0")
            ?.removePrefix("v")?.takeIf { it.isNotEmpty() } ?: "1.0.0"
        testInstrumentationRunner = "com.example.chromatic.HiltTestRunner"
    }

    val keystorePath: String? = System.getenv("CHROMATIC_KEYSTORE_PATH")
    signingConfigs {
        if (keystorePath != null) {
            create("release") {
                storeFile = rootProject.file(keystorePath)
                storePassword = System.getenv("CHROMATIC_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("CHROMATIC_KEY_ALIAS")
                keyPassword = System.getenv("CHROMATIC_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        debug { isPseudoLocalesEnabled = true }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // Test-only classes survive the app's own R8 (v15 §4 release smoke).
            testProguardFile("proguard-test-rules.pro")
            signingConfig = if (keystorePath != null) signingConfigs.getByName("release")
            else signingConfigs.getByName("debug")
        }
    }

    testBuildType = if (project.hasProperty("releaseSmoke")) "release" else "debug"

    testOptions { unitTests { isIncludeAndroidResources = true } }

    lint {
        checkDependencies = true
        disable += setOf("GradleDependency", "ObsoleteLintCustomCheck")
        baseline = file("lint-baseline.xml")
    }

    baselineProfile {
        managedDevices += "pixel6Api34"
        useConnectedDevices = false
    }
    managedDevices {
        devices {
            create("pixel6Api34") {
                device = "Pixel 6"
                apiLevel = 34
                systemImageSource = "aosp-atd"
            }
        }
    }
}

dependencies {
    implementation(projects.core.designsystem)
    implementation(projects.core.data)
    implementation(projects.feature.learn)
    implementation(projects.feature.quiz)
    implementation(projects.feature.tools)
    implementation(projects.feature.settings)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.profileinstaller)
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.hilt.work)
    implementation(libs.androidx.metrics.performance)
    ksp(libs.androidx.hilt.compiler)
    implementation(libs.timber)

    debugImplementation(libs.leakcanary.android)

    testImplementation(projects.core.testing)
    kspTest(libs.hilt.compiler)

    androidTestImplementation(projects.core.testing)
    androidTestImplementation(libs.androidx.espresso.accessibility)
    kspAndroidTest(libs.hilt.compiler)

    baselineProfile(project(":baselineprofile"))
}
