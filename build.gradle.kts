@file:Suppress("UnstableApiUsage")

import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

fun properties(key: String) = providers.gradleProperty(key)

plugins {
    alias(libs.plugins.kotlin) //`jvm-test-suite`
    alias(libs.plugins.intelliJPlatform)
    alias(libs.plugins.changelog)
    alias(libs.plugins.qodana)
    kotlin("plugin.serialization") version "2.1.0"
    alias(libs.plugins.kover)
}


group = properties("pluginGroup").get()
version = properties("pluginVersion").get()


repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()

    }
}



dependencies {
    intellijPlatform {
        create(
            providers.gradleProperty("platformType"), providers.gradleProperty("platformVersion"), useInstaller = false
        )
        bundledPlugins(providers.gradleProperty("platformBundledPlugins").map { it.split(',') })
        jetbrainsRuntime()
        pluginVerifier()

        zipSigner()
        testFramework(TestFrameworkType.Starter)
        testFramework(TestFrameworkType.JUnit5)
        testFramework(TestFrameworkType.Platform)
    }

    testImplementation("junit:junit:4.13.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.0") { isTransitive = false }
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.18.2") { isTransitive = false }
    compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")




    testImplementation(libs.bundles.kTest)
    testImplementation("org.opentest4j:opentest4j:1.3.0")
    testImplementation("com.fasterxml.jackson.core:jackson-databind:2.17.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testImplementation("org.junit.platform:junit-platform-launcher:1.11.4")
    testImplementation("org.kodein.di:kodein-di-jvm:7.25.0")

}

// Set the JVM language level used to build the project. Use Java 11 for 2020.3+, and Java 17 for 2022.2+.
kotlin {
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(21)
        vendor = JvmVendorSpec.JETBRAINS
    }

}

intellijPlatform {

    pluginConfiguration {
        version = providers.gradleProperty("pluginVersion")
        description = providers.fileContents(layout.projectDirectory.file("README.md")).asText.map {
            val start = "<!-- Plugin description -->"
            val end = "<!-- Plugin description end -->"
            with(it.lines()) {
                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end)).joinToString("\n").let(::markdownToHTML)
            }
        }

        val changelog = project.changelog
        changeNotes = providers.gradleProperty("pluginVersion").map { pluginVersion ->
            with(changelog) {
                renderItem(
                    (getOrNull(pluginVersion) ?: getUnreleased()).withHeader(false).withEmptySections(false),
                    Changelog.OutputType.HTML,
                )
            }
        }

        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
            untilBuild = providers.gradleProperty("pluginUntilBuild")
        }
    }

    pluginVerification {

        ides {
            recommended()
        }
    }

    signing {
        certificateChain = providers.environmentVariable("CERTIFICATE_CHAIN")
        privateKey = providers.environmentVariable("PRIVATE_KEY")
        password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
    }

    publishing {
        token = providers.environmentVariable("PUBLISH_TOKEN")
        channels = providers.gradleProperty("pluginVersion").map {
            listOf(it.substringAfter('-', "").substringBefore('.').ifEmpty { "default" })
        }
    }
}

changelog {
    groups.empty()
    repositoryUrl = providers.gradleProperty("pluginRepositoryUrl")
    headerParserRegex.set("""(\d+\.\d+\.\d+)""".toRegex())
}


kover {
    reports {
        total {
            xml {
                onCheck = true
            }
            html {
                onCheck = true
            }
        }
    }
}

tasks {
    wrapper {
        gradleVersion = properties("gradleVersion").get()
    }



    runIde {
        jvmArgs = listOf("-Xmx4G")
        systemProperties(
            "ide.native.launcher" to true,
            "ide.browser.jcef.enabled" to true,
            "ide.experimental.ui" to "true",
            "ide.show.tips.on.startup.default.value" to false,
            "idea.trust.all.projects" to true,
            "jb.consents.confirmation.enabled" to false
        )
    }




    test {
        useJUnitPlatform {
            excludeTags("ui")
        }

        systemProperty("idea.home.path", buildPlugin.get().archiveFile.get().asFile.absolutePath)

        jvmArgs(
            "-Didea.trust.all.projects=true"
        )

        dependsOn("buildPlugin")

    }


}


