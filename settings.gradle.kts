enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
  repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
  }
}

dependencyResolutionManagement {
  repositories {
    mavenCentral()
    google()
    maven("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
  }
}

gradle.beforeProject {
  group = "dev.efemoney"
  version = "0.1.0"
}

include("runtime")
include("codegen")
include("integration")