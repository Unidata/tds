/*
 * Copyright (c) 2026 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

dependencyResolutionManagement {
  repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
  repositories {
    mavenCentral()
    gradlePluginPortal()
  }
  versionCatalogs { create("tdsLibs") { from(files("../gradle/tds.libs.versions.toml")) } }
}

rootProject.name = "build-logic-tds"
