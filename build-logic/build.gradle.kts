import org.gradle.kotlin.dsl.gradleKotlinDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-gradle-plugin`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.sam.with.receiver)
}

repositories {
    mavenCentral()
}

samWithReceiver {
    annotation("org.gradle.api.HasImplicitReceiver")
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

gradlePlugin {
    plugins {
        register("ktlintDokka") {
            id = "ktlint-dokka"
            implementationClass = "DokkaPlugin"
        }
        register("ktlintKotlinCommon") {
            id = "ktlint-kotlin-common"
            implementationClass = "KotlinCommonPlugin"
        }
        register("ktlintPublication") {
            id = "ktlint-publication"
            implementationClass = "PublicationPlugin"
        }
        register("ktlintPublicationLibrary") {
            id = "ktlint-publication-library"
            implementationClass = "PublicationLibraryPlugin"
        }
    }
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
    implementation(gradleKotlinDsl())
    implementation(libs.dokka)
    implementation(libs.poko)
}
