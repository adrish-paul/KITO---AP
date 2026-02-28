import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)

    id("com.google.devtools.ksp")
}

kotlin {
    // Android Library Configuration
    androidLibrary {
        namespace = "com.kito.shared"
        compileSdk = 36
        minSdk = 26
        
        // Enable Android resources in KMP
        experimentalProperties["android.experimental.kmp.enableAndroidResources"] = true
        
        with(compilerOptions) {
            jvmTarget.set(JvmTarget.JVM_17)
            freeCompilerArgs.add("-Xexpect-actual-classes")
        }
    }

    applyDefaultHierarchyTemplate()
    
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            binaryOption("bundleId", "com.kito.composeApp")
        }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.appcompat)
            implementation(libs.material)
            implementation(libs.androidx.activity)
            implementation(libs.androidx.constraintlayout)
            implementation(libs.composeUiTooling)

            // Ktor engine for Android
            implementation(libs.ktor.client.okhttp)

            // Compose dependencies (Android specific)
            implementation("androidx.activity:activity-compose:1.8.0")

            // DataStore (proto - Android only)
            implementation("androidx.datastore:datastore:1.1.2")
            
            // DataStore Preferences (Android) - needed for Context.preferencesDataStore delegate
            implementation(libs.datastore.preferences.android)

            implementation(libs.androidx.work.runtime.ktx)

            //Glance
            implementation("androidx.glance:glance-appwidget:1.1.1")
            implementation("androidx.glance:glance-material3:1.1.1")
            implementation("androidx.glance:glance-material:1.1.1")

            //EncryptedSharedPreferences
            implementation("androidx.security:security-crypto:1.1.0-alpha06")
            
            // Google Tink (Encryption)
            implementation(libs.tink.android)

            //koin
            implementation(libs.koin.android)

            implementation("io.coil-kt.coil3:coil-network-okhttp:3.3.0")
        }
        
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.mp.material3)
            implementation(libs.compose.mp.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.mp.uiToolingPreview)
            // implementation(libs.androidx.lifecycle.viewmodel.compose)
            // implementation(libs.androidx.lifecycle.runtime.compose)
            
            // Ktor (KMP HTTP Client)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)
            
            // Ksoup (KMP HTML/XML Parser)
            implementation(libs.ksoup)

            // Kotlinx DateTime
            implementation(libs.kotlinx.datetime)

            // DataStore Preferences KMP
            implementation(libs.datastore.preferences.core)

            // kotlinx-collections-immutable
            implementation(libs.kotlinx.collections.immutable)

            // kotlinx-serialization
            implementation(libs.kotlinx.serialization.json)

            // Haze (Frosted Glass)
            implementation(libs.haze)
            implementation(libs.haze.materials)

            // Navigation Compose


            // Room KMP
            implementation(libs.room.runtime)
            implementation(libs.compottie)
            
            // Icons (Explicit version to resolve build issue)
            implementation("org.jetbrains.compose.material:material-icons-extended:1.7.0")


            // Koin
            api(libs.koin.core)
            implementation(libs.koin.compose)
            // implementation(libs.koin.compose.viewmodel)


            // Navigation 3
            api(libs.jetbrains.navigation3.ui)
            // Lifecycle (JetBrains KMP wrappers with iOS support)
            implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel:2.9.6")
            implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.9.6")
            implementation("org.jetbrains.androidx.lifecycle:lifecycle-runtime-compose:2.9.6")

            // Image loading (KMP)
            implementation("io.coil-kt.coil3:coil-compose:3.3.0")
            implementation("io.coil-kt.coil3:coil-network-ktor3:3.3.0")
        }

        iosMain.dependencies {
            // Ktor engine for iOS
            implementation(libs.ktor.client.darwin)
            implementation(libs.sqlite.bundled)
        }

        val iosSimulatorArm64Test by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        
        commonTest.dependencies {
             implementation(kotlin("test"))
        }
    }
    // On iOS, exclude Google's androidx.lifecycle to prevent duplicate symbols.
    // JetBrains lifecycle wrappers (2.9.6) bundle the lifecycle code for iOS;
    // Google's artifacts also now publish iOS klibs (2.10.0-rc01 via transitives), causing duplication.
    configurations.matching { name.contains("ios", ignoreCase = true) }.configureEach {
        exclude(group = "androidx.lifecycle")
    }
}

dependencies {
    add("kspAndroid", libs.room.compiler)
    add("kspIosSimulatorArm64", libs.room.compiler)
    add("kspIosArm64", libs.room.compiler)
}

// Force Kotlin metadata library version for Dagger/Hilt compatibility with Kotlin 2.3.0
configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlin" && requested.name == "kotlin-metadata-jvm") {
            useVersion("2.3.0")
        }
    }
}