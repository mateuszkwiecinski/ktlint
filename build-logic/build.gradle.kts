import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(20)
}

// `build-logic` can produce Java 20 compatible code, but only after the plugin is built with kotlin 1.9,
// kotlin 1.9 has a fix for https://youtrack.jetbrains.com/issue/KT-57495/Add-JVM-target-bytecode-version-20
// temporairly using 17, since this is the highest, preinstalled java version on the CI
val targetJavaVersion = JavaVersion.VERSION_17
tasks.withType<JavaCompile>().configureEach {
    options.release.set(targetJavaVersion.majorVersion.toInt())
}
tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = targetJavaVersion.toString()
}

dependencies {
    val kotlinPlugin =
        if (providers.gradleProperty("kotlinDev").orNull.toBoolean()) {
            // Pass '-PkotlinDev' to command line to enable kotlin-in-development version
            logger.warn("Enabling kotlin dev version!")
            libs.kotlin.plugin.dev
        } else {
            libs.kotlin.plugin.asProvider()
        }
    implementation(kotlinPlugin)
    implementation(libs.dokka)
}
