/*
 * Copyright (c) 2026 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

plugins {
  `kotlin-dsl`
  alias(tdsLibs.plugins.protobuf)
  alias(tdsLibs.plugins.spotless)
}

dependencies {
  implementation(plugin(tdsLibs.plugins.protobuf))
  implementation(plugin(tdsLibs.plugins.spotless))
}

spotless {
  kotlinGradle {
    target("*.gradle.kts", "**/*.gradle.kts")
    ktfmt().googleStyle()
  }
}

// Helper function that transforms a plugin alias from the version catalog
// into a valid dependency notation
fun plugin(plugin: Provider<PluginDependency>) =
  plugin.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" }
