import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

kotlin {
    // All modules, the CLI included, must have an explicit API
    explicitApi()
    jvmToolchain(20)
}

val targetJavaVersion = JavaVersion.VERSION_1_8
tasks.withType<JavaCompile>().configureEach {
    options.release.set(targetJavaVersion.majorVersion.toInt())
}
tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = targetJavaVersion.toString()
}

listOf(8, 11, 17).forEach { majorVersion ->
    val jdkTest =
        tasks.register<Test>("testJdk$majorVersion") {
            javaLauncher =
                javaToolchains.launcherFor {
                    languageVersion = JavaLanguageVersion.of(majorVersion)
                }

            description = "Runs the test suite on JDK $majorVersion"
            group = LifecycleBasePlugin.VERIFICATION_GROUP

            // Copy inputs from normal Test task.
            val testTask = tasks.test.get()
            classpath = testTask.classpath
            testClassesDirs = testTask.testClassesDirs
        }
}

val skipTests: String = providers.systemProperty("skipTests").getOrElse("false")
tasks.withType<Test>().configureEach {
    if (skipTests == "false") {
        useJUnitPlatform()
    } else {
        logger.warn("Skipping tests for task '$name' as system property 'skipTests=$skipTests'")
    }

    maxParallelForks =
        if (System.getenv("CI") != null) {
            Runtime.getRuntime().availableProcessors()
        } else {
            // https://docs.gradle.org/8.0/userguide/performance.html#execute_tests_in_parallel
            (Runtime.getRuntime().availableProcessors() / 2).takeIf { it > 0 } ?: 1
        }

    if (javaLauncher.get().metadata.languageVersion.canCompileOrRun(JavaLanguageVersion.of(11))) {
        // https://docs.gradle.org/7.5/userguide/upgrading_version_7.html#removes_implicit_add_opens_for_test_workers
        jvmArgs("--add-opens=java.base/java.lang=ALL-UNNAMED")
    }
}
