plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.compose.compiler) apply false
    id("com.google.devtools.ksp") version "2.3.7" apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    id("com.google.dagger.hilt.android") version "2.57.2" apply false
    alias(libs.plugins.kover) apply false
}

// Ensure gradlew is executable on all platforms — runs automatically on every Gradle sync
tasks.register("setGradlewExecutable") {
    doLast { file("gradlew").setExecutable(true) }
}
gradle.projectsEvaluated { tasks.findByName("setGradlewExecutable")?.let { it } }
rootProject.tasks.matching { it.name == "prepareKotlinBuildScriptModel" }.configureEach {
    dependsOn("setGradlewExecutable")
}