plugins {
  kotlin("jvm")
}

dependencies {
  implementation(libs.kotlin.stdlib.jdk8)
  implementation(libs.kotlin.reflect)
  implementation(libs.ktor.client.core)
  implementation(projects.runtime)
  implementation(libs.ksp.api)
  implementation(libs.kotlinpoet.ksp)
}
