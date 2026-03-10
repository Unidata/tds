/*
 * Copyright (c) 2026 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

plugins {
  id("tds-java-base-conventions")
  alias(tdsLibs.plugins.spotless)
}

description = "The NSF Unidata THREDDS Data Server (TDS)."

// To upgrade gradle, update the version and expected checksum values below
// and run ./gradlew wrapper twice
tasks.wrapper {
  distributionType = Wrapper.DistributionType.ALL
  gradleVersion = "9.4.0"
  distributionSha256Sum = "b21468753cb43c167738ee04f10c706c46459cf8f8ae6ea132dc9ce589a261f2"
}

spotless {
  // check all gradle build scripts (build-logic-tds has its own formatting check)
  kotlinGradle {
    target("*.gradle.kts", "**/*.gradle.kts")
    targetExclude("build-logic-tds/**/*")
    ktfmt().googleStyle()
  }
}
