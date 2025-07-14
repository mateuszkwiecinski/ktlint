plugins {
    id("ktlint-publication-library")
    alias(libs.plugins.shadow)
    signing
}

tasks.jar {
    manifest {
        attributes("Main-Class" to "com.pinterest.ktlint.Main")
        attributes("Implementation-Version" to version)
    }
}

tasks.shadowJar {
    mergeServiceFiles()

    // strip unnecessary files from `-all` jar
    exclude("**/*.kotlin_metadata")
    exclude("**/*.kotlin_module")
    exclude("**/*.kotlin_builtins")
    exclude("**/module-info.class")
    exclude("META-INF/maven/**")
    exclude("META-INF/*.version")
}

configurations {
    register("r8")
}

dependencies {
    implementation(projects.ktlintLogger)
    implementation(projects.ktlintCliReporterBaseline)
    implementation(projects.ktlintCliReporterCore)
    implementation(projects.ktlintCliReporterPlain)
    implementation(projects.ktlintRuleEngine)
    implementation(projects.ktlintRulesetStandard)
    implementation(libs.clikt)
    implementation(libs.logback)

    runtimeOnly(projects.ktlintCliReporterCheckstyle)
    runtimeOnly(projects.ktlintCliReporterJson)
    runtimeOnly(projects.ktlintCliReporterFormat)
    runtimeOnly(projects.ktlintCliReporterHtml)
    runtimeOnly(projects.ktlintCliReporterPlainSummary)
    runtimeOnly(projects.ktlintCliReporterSarif)

    "r8"(libs.r8)

    testImplementation(projects.ktlintTest)

    testImplementation(libs.junit5.jupiter)
    // Since Gradle 8 the platform launcher needs explicitly be defined as runtime dependency to avoid classpath problems
    // https://docs.gradle.org/8.12/userguide/upgrading_version_8.html#test_framework_implementation_dependencies
    testRuntimeOnly(libs.junit5.platform.launcher)
}

val r8File =
    layout.buildDirectory
        .file("libs/${base.archivesName.get()}-$version-r8.jar")
        .get()
        .asFile
val rulesFile = project.file("src/main/rules.pro")
val r8Jar by tasks.registering(JavaExec::class) {
    val fatJar = tasks.shadowJar.get()
    val fatJarFile = fatJar.archiveFile
    dependsOn(fatJar)
    inputs.file(fatJarFile)
    inputs.file(rulesFile)
    outputs.file(r8File)

    classpath(configurations.named("r8"))
    mainClass.set("com.android.tools.r8.R8")
    args =
        listOf(
            "--release",
            "--classfile",
            "--output",
            r8File.path,
            "--pg-conf",
            rulesFile.path,
            "--lib",
            System.getProperty("java.home").toString(),
            fatJarFile.get().toString(),
        )
}

// Directory for files to be distributed as Ktlint CLI
val ktlintCliOutputRoot = layout.buildDirectory.dir("run")
val ktlintCliFiles by tasks.registering(KtlintCliTask::class) {
    dependsOn(r8Jar)

    ktlintCliJarFile.set(r8File)
    ktlintCliWindowsBatchScriptSource.set(layout.projectDirectory.file("src/main/scripts/ktlint.bat"))
    ktlintCliOutputDirectory.set(ktlintCliOutputRoot)

    finalizedBy("signKtlintCliFiles")
}

val signKtlintCliFiles by tasks.registering(Sign::class) {
    dependsOn(ktlintCliFiles)

    sign(ktlintCliFiles.flatMap { it.ktlintCliExecutable }.get())
}

tasks.withType<Test>().configureEach {
    dependsOn(ktlintCliFiles)

    // TODO: Use providers directly after https://github.com/gradle/gradle/issues/12247 is fixed.
    val ktlintCliExecutableFilePath = ktlintCliFiles.flatMap { it.ktlintCliExecutable }.map { it.absolutePath }.get()
    val ktlintVersion = providers.provider { version }.get()
    doFirst {
        systemProperty(
            "ktlint-cli",
            ktlintCliExecutableFilePath,
        )
        systemProperty("ktlint-version", ktlintVersion)
    }
}
