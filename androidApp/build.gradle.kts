import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)

    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
}

val localProps = Properties().apply {
    val localPropsFile = rootProject.file("local.properties")
    if (localPropsFile.exists()) {
        load(FileInputStream(localPropsFile))
    }
}

android {
    namespace = "com.kito"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.kito"
        minSdk = 26
        targetSdk = 36
        versionCode = 42
        versionName = "4.4.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "PORTAL_BASE",
            "\"https://kiitportal.kiituniversity.net\"",
        )
        buildConfigField(
            "String",
            "WD_PATH",
            "\"/sap/bc/webdynpro/sap/ZWDA_HRIQ_ST_ATTENDANCE\""
        )
        buildConfigField(
            "String",
            "SUPABASE_URL",
            "\"${localProps.getProperty("SUPABASE_URL")}\""
        )
        buildConfigField(
            "String",
            "SUPABASE_ANON_KEY",
            "\"${localProps.getProperty("SUPABASE_ANON_KEY")}\""
        )
        buildConfigField(
            "String",
            "KG_API_KEY",
            "\"${localProps.getProperty("KHAOOGULLY_API_KEY")}\""
        )
        buildConfigField(
            "String",
            "KG_BASE_URL",
            "\"${localProps.getProperty("KHAOOGULLY_BASE_URL")}\""
        )
        buildConfigField(
            "String",
            "CDN_URL",
            "\"${localProps.getProperty("CDN_URL")}\""
        )
    }

    buildTypes {
        debug {
            resValue("string", "app_name", "KIITO (Debug)")
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            resValue("string", "app_name", "KIITO")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        create("internal_testing") {
            initWith(getByName("release"))

            resValue("string", "app_name", "KIITO (Testing)")
            applicationIdSuffix = ".testing"
            versionNameSuffix = "-testing"

            isDebuggable = false
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
        resValues = true
    }

}

dependencies {
    implementation(project(":composeApp"))

    // Compose (for setContent)
    implementation("androidx.activity:activity-compose:1.8.0")

    // Splash Screen
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Play Core (In-App Update)
    implementation("com.google.android.play:app-update:2.1.0")
    implementation("com.google.android.play:app-update-ktx:2.1.0")

    // Material (for Snackbar)
    implementation(libs.material)

    // Navigation Compose
    implementation(libs.jetbrains.navigation3.ui)


    // Compose Material3
    implementation(libs.compose.mp.material3)
    implementation(libs.compose.foundation)
    implementation(libs.compose.mp.ui)

    // Koin
    implementation(libs.koin.android)
    implementation(libs.koin.core)

    // Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Serialization (for navigation destinations)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.navigation3.runtime)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    debugImplementation(libs.composeUiTooling)
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}
