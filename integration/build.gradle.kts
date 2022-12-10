@file:Suppress("DSL_SCOPE_VIOLATION")

plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.ksp)
}

kotlin.sourceSets {
  main { kotlin.srcDir("$buildDir/generated/ksp/main/kotlin") }
  test { kotlin.srcDir("$buildDir/generated/ksp/test/kotlin") }
}

dependencies {
  ksp(projects.codegen)

  implementation(platform(libs.okHttp.bom))

  implementation(projects.runtime)
  implementation(libs.ktor.serialization)
  implementation(libs.ktor.client.okHttp)
  implementation(libs.ktor.client.logging)
  implementation(libs.ktor.client.contentNegotiation)
  implementation("com.squareup.retrofit2:retrofit:2.9.0")

}
