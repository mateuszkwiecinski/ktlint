plugins {
    // TODO: Remove if the build is migrated to Gradle 9
    kotlin("jvm") version libs.versions.kotlin.get() apply false // Enforce higher Kotlin version to make `poko-gradle-plugin` compatible.
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(
        libs
            .versions
            .java
            .compilation
            .get()
            .toInt(),
    )
}

dependencies {
    val kotlinPlugin =
        if (hasProperty("kotlinDev")) {
            // Pass '-PkotlinDev' to command line to enable kotlin-in-development version
            logger.warn("Enabling kotlin dev version!")
            libs.kotlin.plugin.dev
        } else {
            libs.kotlin.plugin.asProvider()
        }
    implementation(kotlinPlugin)
    implementation(libs.dokka)
    implementation(libs.poko)
}
