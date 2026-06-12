import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)

    id("com.google.devtools.ksp")
    alias(libs.plugins.koin.compiler)
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

    jvm("desktop")

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

            // Ktor engine for Android
            implementation(libs.ktor.client.okhttp)

            // Compose dependencies (Android specific)
            implementation("androidx.activity:activity-compose:1.8.0")

            // Custom Tabs — used by Supabase OAuth redirect flow on Android
            implementation("androidx.browser:browser:1.8.0")

            // Credential Manager — native Google account picker (Compose Auth googleNativeLogin)
            implementation("androidx.credentials:credentials:1.3.0")
            implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
            implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

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
            implementation(libs.koin.annotations)
            // implementation(libs.koin.compose.viewmodel)

            // Supabase (auth / GoTrue only — REST stays on raw Ktor + anon key)
            implementation(libs.supabase.auth)
            implementation(libs.supabase.compose.auth)


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

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.ktor.client.okhttp)
                implementation(libs.sqlite.bundled)
                // Provides Dispatchers.Main on JVM desktop (Swing event thread)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.10.2")
            }
        }

        val iosSimulatorArm64Test by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
            implementation(libs.ktor.client.mock)
            implementation(libs.compose.ui.test)
        }

        val desktopTest by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
    }
    // On iOS, exclude Google's androidx.lifecycle to prevent duplicate symbols.
    // JetBrains lifecycle wrappers (2.9.6) bundle the lifecycle code for iOS;
    // Google's artifacts also now publish iOS klibs (2.10.0-rc01 via transitives), causing duplication.
    configurations.matching { name.contains("ios", ignoreCase = true) }.configureEach {
        exclude(group = "androidx.lifecycle")
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
    }
}

dependencies {
    add("kspAndroid", libs.room.compiler)
    add("kspIosSimulatorArm64", libs.room.compiler)
    add("kspIosArm64", libs.room.compiler)
    add("kspDesktop", libs.room.compiler)
    add("androidRuntimeClasspath", libs.compose.mp.uiTooling)
}

configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlin" && requested.name == "kotlin-metadata-jvm") {
            useVersion("2.3.20")
        }
    }
}