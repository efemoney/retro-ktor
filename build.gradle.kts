@file:Suppress("DSL_SCOPE_VIOLATION", "NOTHING_TO_INLINE", "UNCHECKED_CAST", "MemberVisibilityCanBePrivate")

import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget

plugins {
  alias(libs.plugins.ksp) apply false
  alias(libs.plugins.kotlin.jvm) apply false
  alias(libs.plugins.kotlin.multiplatform) apply false
  id("com.github.ben-manes.versions") version "0.42.0"
}

subprojects {
  pluginManager.withPlugin("java") {
    the<JavaPluginExtension>().sourceSets.configureEach {
      java.setSrcDirs(listOf(simpleName(name, "src")))
      resources.setSrcDirs(listOf(simpleName(name, "resources")))
    }
  }
  pluginManager.withKotlinJvm {
    kotlin.sourceSets.configureEach {
      kotlin.setSrcDirs(listOf(simpleName(name, "src")))
      resources.setSrcDirs(listOf(simpleName(name, "resources")))
    }
  }
  pluginManager.withKotlinMultiplatform {
    kotlin.sourceSets.configureEach {
      when {
        name.endsWith("main", ignoreCase = true) -> {
          kotlin.setSrcDirs(setOf(name.dropLast(4)))
          resources.setSrcDirs(setOf(name.dropLast(4) + "-resources"))
        }
        name.endsWith("test", ignoreCase = true) -> {
          kotlin.setSrcDirs(setOf(name.dropLast(4) + "-test"))
          resources.setSrcDirs(setOf(name.dropLast(4) + "-test-resources"))
        }
      }
    }
  }
  pluginManager.withAnyKotlinPlugin {
    kotlin.configureEachCompilation {
      kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + listOf(
          "-opt-in=kotlin.RequiresOptIn",
          "-opt-in=kotlin.contracts.ExperimentalContracts",
          "-opt-in=com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview",
          "-Xcontext-receivers",
        )
      }
    }
  }

  dependencies {
    configurations
      .matchingImplementation()
      .configureEach { name(platform(libs.kotlin.bom)) }
  }
}

tasks.dependencyUpdates {
  rejectVersionIf { "ide" in candidate.version }
}

// region Dsl

fun PluginManager.withAnyPlugin(vararg plugins: String, action: Action<AppliedPlugin>) =
  plugins.forEach { withPlugin(it, action) }

fun PluginManager.withAnyKotlinPlugin(action: Action<AppliedKotlinPlugin<KotlinProjectExtension>>) {
  withAnyPlugin(
    "org.jetbrains.kotlin.js",
    "org.jetbrains.kotlin.jvm",
    "org.jetbrains.kotlin.android",
  ) { action.execute(AppliedKotlinPlugin(this)) }
}

fun PluginManager.withKotlinJvm(action: Action<AppliedKotlinPlugin<KotlinJvmProjectExtension>>) =
  withPlugin("org.jetbrains.kotlin.jvm") { action.execute(AppliedKotlinPlugin(this)) }

fun PluginManager.withKotlinMultiplatform(action: Action<AppliedKotlinPlugin<KotlinMultiplatformExtension>>) =
  withPlugin("org.jetbrains.kotlin.multiplatform") { action.execute(AppliedKotlinPlugin(this)) }

fun ConfigurationContainer.matchingImplementation() = matching { it.name.contains("implementation", ignoreCase = true) }

fun simpleName(name: String, suffix: String) = if (name == "main") suffix else "$name-$suffix"

class AppliedKotlinPlugin<T : KotlinProjectExtension>(appliedPlugin: AppliedPlugin) : AppliedPlugin by appliedPlugin {

  inline val Project.kotlin get() = extensions.getByName<KotlinProjectExtension>("kotlin") as T

  inline fun Project.kotlin(action: Action<T>) = extensions.configure("kotlin", action)

  fun T.configureEachTarget(
    includeMetadata: Boolean = true,
    action: Action<KotlinTarget>,
  ) = when (this) {
    is KotlinSingleTargetExtension -> action.execute(target)
    is KotlinMultiplatformExtension -> {
      if (includeMetadata) action.execute(metadata())
      targets.configureEach(action)
    }
    else -> error("")
  }

  fun T.configureEachCompilation(
    includeMetadata: Boolean = true,
    action: Action<KotlinCompilation<KotlinCommonOptions>>
  ) = configureEachTarget(includeMetadata) {
    compilations.configureEach(action)
  }
}

// endregion
