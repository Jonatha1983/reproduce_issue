plugins {
  kotlin("jvm") version "2.1.0"
  kotlin("plugin.serialization") version "2.1.0"
}

repositories {
  gradlePluginPortal()
  mavenCentral()
}

dependencies {
  implementation(gradleApi())
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
}

