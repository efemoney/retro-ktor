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
  implementation(projects.runtime)
  implementation(platform(libs.okHttp.bom))
  implementation(libs.ktor.client.okHttp)
  implementation("com.squareup.retrofit2:retrofit:2.9.0")
}
