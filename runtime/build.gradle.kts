plugins {
  kotlin("multiplatform")
}

kotlin {
  jvm { withJava() }
}

dependencies {
  commonMainImplementation(libs.kotlin.stdlib.common)
  commonMainImplementation(libs.ktor.client.core)
}
